package jmri.jmrit.logix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.Color;
import java.util.List;
import java.util.ListIterator;
import jmri.DccThrottle;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.util.ThreadingUtil;
import jmri.jmrit.logix.ThrottleSetting.Command;
import jmri.jmrit.logix.ThrottleSetting.CommandValue;
import jmri.jmrit.logix.ThrottleSetting.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Execute a throttle command script for a warrant.
 * <p>
 * This generally operates on its own thread, but calls the warrant
 * thread via Warrant.fireRunStatus to show status. fireRunStatus uses
 * ThreadingUtil.runOnLayoutEventually to display on the layout thread.
 *
 * @author Pete Cressman Copyright (C) 2009, 2010, 2020
 */
/*
 * ************************ Thread running the train ****************
 */
public class Engineer extends Thread implements java.beans.PropertyChangeListener {

    private int _idxCurrentCommand;     // current throttle command
    private ThrottleSetting _currentCommand;
    private long _commandTime = 0;      // system time when command was executed.
    private int _idxSkipToSpeedCommand;   // skip to this index to reset script when ramping
    private float _normalSpeed = 0;       // current commanded throttle setting from script (unmodified)
    // speed name of current motion. When train stopped, is the speed that will be restored when movement is permitted
    private String _speedType = Warrant.Normal; // is never Stop or EStop   
    private float _timeRatio = 1.0f;     // ratio to extend scripted time when speed is modified
    private boolean _abort = false;
    private boolean _halt = false;  // halt/resume from user's control
    private boolean _stopPending = false;   // ramp slow down in progress
    private boolean _waitForClear = false;  // waits for signals/occupancy/allocation to clear
    private boolean _waitForSensor = false; // wait for sensor event
    private boolean _runOnET = false;   // Execute commands on ET only - do not synch
    private boolean _setRunOnET = false; // Need to delay _runOnET from the block that set it
    protected DccThrottle _throttle;
    private final Warrant _warrant;
    private final List<ThrottleSetting> _commands;
    private Sensor _waitSensor;
    private int _sensorWaitState;
    private Object _rampLockObject = new Object(); // used for synchronizing threads for _ramp
    private ThrottleRamp _ramp;
    private boolean _atHalt = false;
    private boolean _atClear = false;
    private final SpeedUtil _speedUtil;
    private OBlock _synchBlock = null;

    Engineer(Warrant warrant, DccThrottle throttle) {
        _warrant = warrant;
        _throttle = throttle;
        _speedUtil = warrant.getSpeedUtil();
        _commands = _warrant.getThrottleCommands();
        _idxCurrentCommand = 0;
        _idxSkipToSpeedCommand = 0;
        _waitForSensor = false;
        setName("Engineer(" + _warrant.getTrainName() +")");
    }

    @Override
    @SuppressFBWarnings(value="UW_UNCOND_WAIT", justification="waits may be indefinite until satisfied or thread aborted")
    public void run() {
        if (log.isDebugEnabled())
            log.debug("Engineer started warrant {} _throttle= {}", _warrant.getDisplayName(), _throttle.getClass().getName());

        int cmdBlockIdx = 0;
        while (_idxCurrentCommand < _commands.size()) {
            while (_idxSkipToSpeedCommand > _idxCurrentCommand) {
                if (log.isDebugEnabled()) {
                    ThrottleSetting ts = _commands.get(_idxCurrentCommand);
                    log.debug("{}: Skip Cmd #{}: {} Warrant {}", _warrant.getDisplayName(), _idxCurrentCommand+1, ts);
                    // Note: command indexes biased from 0 to 1 to match Warrant display of commands.
                }
                _idxCurrentCommand++;
            }
            _currentCommand = _commands.get(_idxCurrentCommand);
            long cmdWaitTime = _currentCommand.getTime();    // time to wait before executing command
            ThrottleSetting.Command command = _currentCommand.getCommand();
            _runOnET = _setRunOnET;     // OK to set here
            if (command.hasBlockName()) {
                int idx = _warrant.getIndexOfBlock(_currentCommand.getBeanDisplayName(), cmdBlockIdx);
                if (idx >= 0) {
                    cmdBlockIdx = idx;
                }
            }
            if (cmdBlockIdx < _warrant.getCurrentOrderIndex() ||
                    (command.equals(Command.NOOP) && (cmdBlockIdx <= _warrant.getCurrentOrderIndex()))) {
                // Train advancing too fast, need to process commands more quickly,
                // allow some time for whistle toots etc.
                cmdWaitTime = Math.min(cmdWaitTime, 200); // 200ms per command should be enough for toots etc.
                if (log.isDebugEnabled())
                    log.debug("{}: Train reached block \"{}\" before script et={}ms",
                            _warrant.getDisplayName(), _warrant.getCurrentBlockName(), _currentCommand.getTime());
            }
            if (_abort) {
                break;
            }

            long cmdStart = System.currentTimeMillis();
            if (log.isDebugEnabled()) 
                log.debug("{}: Start Cmd #{} for block \"{}\" currently in \"{}\". wait {}ms to do cmd {}",
                    _warrant.getDisplayName(), _idxCurrentCommand+1, _currentCommand.getBeanDisplayName(), 
                    _warrant.getCurrentBlockName(), cmdWaitTime, command.toString());
                    // Note: command indexes biased from 0 to 1 to match Warrant display of commands.
            synchronized (this) {
                if (!Warrant.Normal.equals(_speedType)) {
                    cmdWaitTime = (long)(cmdWaitTime*_timeRatio); // extend et when speed has been modified from scripted speed
                }
                try {
                    if (cmdWaitTime > 0) {
                        wait(cmdWaitTime);
                    }
                } catch (InterruptedException ie) {
                    log.debug("InterruptedException during time wait {}", ie);
                    _warrant.debugInfo();
                    Thread.currentThread().interrupt();
                } catch (java.lang.IllegalArgumentException iae) {
                    log.error("At time wait {}", iae);
                }
            }
            if (_abort) {
                break;
            }

            // Having waited, time=ts.getTime(), so blocks should agree.  if not,
            // wait for train to arrive at block and send sync notification.
            // note, blind runs cannot detect entrance.
            if (!_runOnET && cmdBlockIdx > _warrant.getCurrentOrderIndex()) {
                // commands are ahead of current train position
                // When the next block goes active or a control command is made, a clear sync call
                // will test these indexes again and can trigger a notify() to free the wait

                synchronized (this) {
                    _synchBlock = _warrant.getBlockAt(cmdBlockIdx);
                    boolean bumpedSpeed = false;
                    float speed = _throttle.getSpeedSetting();
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("{}: Wait for train to enter \"{}\".",
                                    _warrant.getDisplayName(), _synchBlock.getDisplayName());
                        }
                        _warrant.fireRunStatus("WaitForSync", _idxCurrentCommand - 1, _idxCurrentCommand);
                        int waittime = 36000000;
                        float throttleIncrement = _speedUtil.getRampThrottleIncrement();
                        float useSpeedHelp = WarrantPreferences.getDefault().getSpeedAssistance();
                        if (speed < useSpeedHelp) {
                                waittime = 10000;    // 10 seconds
                        }
                        while (!_synchBlock.equals(_warrant.getCurrentBlockOrder().getBlock())) {
                            wait(waittime);
                            if (bumpedSpeed) {
                                setSpeed(speed);
                            }
                            if (speed < useSpeedHelp) {
                                speed += throttleIncrement;
                                bumpedSpeed = true;
                            } else {  
                                waittime = 36000000;
                                 // no more speed increases. just wait 10 hours
                            }
                            if (_abort) {
                                break;
                            }
                        }
                    } catch (InterruptedException ie) {
                        log.debug("InterruptedException during _waitForSync {}", ie);
                        _warrant.debugInfo();
                        Thread.currentThread().interrupt();
                    }
                    finally {
                        if (bumpedSpeed) {
                            // restore current speed
                            int idx = _warrant.getCurrentOrderIndex();
                            _normalSpeed = _speedUtil.getBlockSpeedInfo(idx).getEntranceSpeed();
                            float entrySpeed = _speedUtil.modifySpeed(_speedUtil.getBlockSpeedInfo(idx).getEntranceSpeed(), _speedType);
                            setSpeed(entrySpeed);
                        }
                        _synchBlock = null;
                    }
                }
/*
                synchronized (this) {
                    _synchBlock = _warrant.getBlockAt(cmdBlockIdx);
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("{}: Wait for train to enter \"{}\".",
                                    _warrant.getDisplayName(), _synchBlock.getDisplayName());
                        }
                        _warrant.fireRunStatus("WaitForSync", _idxCurrentCommand - 1, _idxCurrentCommand);
                        wait();
                    } catch (InterruptedException ie) {
                        log.error("At _waitForSync {}", ie);
                        _warrant.debugInfo();
                        Thread.currentThread().interrupt();
                    }
                    finally {
                        _synchBlock = null;
                    }
                }*/
            }
            if (_abort) {
                break;
            }

            synchronized (this) {
                // block position and elapsed time are as expected, but track conditions
                // such as signals or rogue occupancy requires waiting
                if (_waitForClear) {
                    try {
                        _atClear = true;
                        if (log.isDebugEnabled()) 
                            log.debug("{}: Waiting for clearance. _waitForClear= {} _halt= {} at block \"{}\" Cmd#{}.",
                                _warrant.getDisplayName(), _waitForClear, _halt, 
                                _warrant.getBlockAt(cmdBlockIdx).getDisplayName(), _idxCurrentCommand+1);
                        wait();
                    } catch (InterruptedException ie) {
                        log.debug("InterruptedException during _atClear {}", ie);
                        _warrant.debugInfo();
                        Thread.currentThread().interrupt();
                    }
                    finally {
                        _waitForClear = false;
                        _atClear = false;
                    }
                }
            }
            if (_abort) {
                break;
            }

            synchronized (this) {
                // user's command to halt requires waiting
                if (_halt) {
                    try {
                        _atHalt = true;
                        if (log.isDebugEnabled()) 
                            log.debug("{}: Waiting to Resume. _halt= {}, _waitForClear= {}, Block \"{}\".",
                                _warrant.getDisplayName(), _halt, _waitForClear, 
                                _warrant.getBlockAt(cmdBlockIdx).getDisplayName());
                        wait();
                    } catch (InterruptedException ie) {
                        log.debug("InterruptedException during _atHalt {}", ie);
                        _warrant.debugInfo();
                        Thread.currentThread().interrupt();
                    }
                    finally {
                        _halt = false;
                        _atHalt = false;
                    }
                }
            }
            if (_abort) {
                break;
            }

            synchronized (this) {
                while (_ramp != null && (!_ramp.ready || _ramp.holdRamp)) {
                    int idx = _idxCurrentCommand;
                    try {
                        if (log.isDebugEnabled()) 
                            log.debug("{}: Waiting for ramp to finish at Cmd #{}.", 
                                  _warrant.getDisplayName(), _idxCurrentCommand+1);
                        wait();
                    } catch (InterruptedException ie) {
                        _warrant.debugInfo();
                        Thread.currentThread().interrupt();
                    }
                    // ramp will decide whether to skip or execute _currentCommand
                    if (log.isDebugEnabled()) {
                        log.debug("{}: Cmd #{} held for {}ms. {}", _warrant.getDisplayName(),
                                idx+1, System.currentTimeMillis() - cmdStart, _currentCommand);
                    }
                }
                if (_idxSkipToSpeedCommand <= _idxCurrentCommand) {
                    executeComand(_currentCommand, System.currentTimeMillis() - cmdStart);
                    _idxCurrentCommand++;
                }
            }
        }
        // shut down
        setSpeed(0.0f); // for safety to be sure train stops
        _warrant.stopWarrant(_abort, true);
    }

    private void executeComand(ThrottleSetting ts, long et) {
        Command command = ts.getCommand();
        CommandValue cmdVal = ts.getValue();
        switch (command) {
            case SPEED:
                _normalSpeed = cmdVal.getFloat();
                float speedMod = _speedUtil.modifySpeed(_normalSpeed, _speedType);
                if (_normalSpeed > speedMod) {
                    _timeRatio = _normalSpeed / speedMod;
                    setSpeed(speedMod);
                } else {
                    _timeRatio = 1.0f;
                    setSpeed(_normalSpeed);
                }
                break;
            case NOOP:
                break;
            case SET_SENSOR:
                ThreadingUtil.runOnLayoutEventually(() ->
                    setSensor(ts.getNamedBeanHandle(), cmdVal));
                break;
            case FKEY:
                setFunction(ts.getKeyNum(), cmdVal.getType());
                break;
            case FORWARD:
                setForward(cmdVal.getType());
                break;
            case LATCHF:
                setFunctionMomentary(ts.getKeyNum(), cmdVal.getType());
                break;
            case WAIT_SENSOR:
                waitForSensor(ts.getNamedBeanHandle(), cmdVal);
                break;
            case RUN_WARRANT:
                runWarrant(ts.getNamedBeanHandle(), cmdVal);
                break;
            case SPEEDSTEP:
                break;
            default:
        }
        _commandTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("{}: Cmd #{} done. et={}. {}",
                   _warrant.getDisplayName(), _idxCurrentCommand + 1, et, ts);
        }
    }

    protected int getCurrentCommandIndex() {
        return _idxCurrentCommand;
    }

    /**
     * Delayed ramp has started.
     * Currently informational only
     * Do non-speed commands only until idx is reached?  maybe not.
     * @param idx index
     */
    private void advanceToCommandIndex(int idx) {
        _idxSkipToSpeedCommand = idx;
      if (log.isTraceEnabled())
            log.debug("advanceToCommandIndex to {} - {}", _idxSkipToSpeedCommand+1, _commands.get(idx));
            // Note: command indexes biased from 0 to 1 to match Warrant display of commands, which are 1-based.
    }

    /**
     * Cannot set _runOnET to true until current NOOP command completes
     * so there is the intermediate flag _setRunOnET
     * @param set true to run on elapsed time calculations only, false to
     *            consider other inputs
     */
    protected void setRunOnET(boolean set) {
        if (log.isDebugEnabled() && _setRunOnET != set) {
            log.debug("{}: setRunOnET {} command #{}", _warrant.getDisplayName(),
                    set, _idxCurrentCommand+1);
            // Note: command indexes biased from 0 to 1 to match Warrant display of commands.
        }
        _setRunOnET = set;
        if (!set) { // OK to be set false immediately
            _runOnET = false;
        }
    }

    protected boolean getRunOnET() {
        return _setRunOnET;
    }

    /**
     * Called by the warrant when a the block ahead of a moving train goes occupied.
     * typically when this thread is on a timed wait. The call will free the wait.
     * @param block going active.
     */
    protected void clearWaitForSync(OBlock block) {
        // block went active. if waiting on sync, clear it
        boolean waitForSync = true;
        if (_synchBlock != null && !_atClear && !_halt && !isRamping()) {
            synchronized (this) {
                if (_synchBlock.equals(block)) {
                    notifyAll();
                    waitForSync = false;
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("{}: clearWaitForSync from block \"{}\". notifyAll() {} called.  isRamping()={}", 
                    _warrant.getDisplayName(), block.getDisplayName(), 
                    (waitForSync ? "NOT":""), isRamping());
        }
    }

    private static void setFrameStatusText(String m, Color c, boolean save) {
        ThreadingUtil.runOnLayoutEventually(()-> WarrantTableFrame.getDefault().setStatusText(m, c, true));
    }

    /**
     * Occupancy of blocks, user halts and aspects of Portal signals will modify
     * normal scripted train speeds.
     * Ramp speed change for smooth prototypical look.
     *
     * @param endSpeedType signal aspect speed name
     * @param endBlockIdx BlockOrder index of the block where ramp is to end.
     *        -1 if an end block is not specified.
     */
    protected synchronized void rampSpeedTo(String endSpeedType, int endBlockIdx) {
        setWaitforClear(true);
        if (endSpeedType.equals(Warrant.Stop) && getSpeedSetting() == 0.0f) {
            return;
        }
        if (endSpeedType.equals(Warrant.EStop)) {
            setStop(true);
            return;
        }

        synchronized (this) {
            if (log.isDebugEnabled()) {
                log.debug("{}: rampSpeedTo: type= {}, throttle from {} to {}.", 
                    _warrant.getDisplayName(), endSpeedType, getSpeedSetting(), 
                    _speedUtil.modifySpeed(_normalSpeed, endSpeedType));
            }
            if (_ramp == null) {
                _ramp = new ThrottleRamp();
                _ramp.start();
            } else if (!_ramp.ready) {
                // for repeated command
                if (_ramp.duplicate(endSpeedType, endBlockIdx)) {
                    return;
                }
                // stop the ramp and replace it
                _ramp.holdRamp = true;
                _ramp.quit(false);
            }

            long time = 0;
            int pause = _speedUtil.getRampTimeIncrement() + 20;
            while (time < pause && !_ramp.ready) {
                // may need a bit of time for quit() or start() to get ready
                try {
                    wait(20);
                    time += 20;
                }
                catch (InterruptedException ie) { // ignore
                }
            }
            if (_ramp.ready) {
                _ramp.setParameters(endSpeedType, endBlockIdx);
                _ramp.holdRamp = false;
                synchronized (_rampLockObject) {
                    _rampLockObject.notifyAll(); // free wait at ThrottleRamp.run()
                    log.debug("{}: rampSpeedTo called notify _ramp.ready= {}", _warrant.getDisplayName(), _ramp.ready);
                }
            } else {
                log.error("Can't launch ramp for speed {}! _ramp Thread.State= {}. Waited {}ms",
                        endSpeedType, _ramp.getState(), time-20);
                _warrant.debugInfo();
            }
        }
    }

    protected boolean isRamping() {
        if (_ramp == null || _ramp.ready) {
            return false;
        }
        return true;
    }

    /**
     * Get the Speed type name. _speedType is the type when moving. Used to restore
     * speeds aspects of signals when halts or other conditions have stopped the train.
     * If rampType is true return the absolute speed of the train, i.e. 'Stop' if
     * train is not moving.
     * @param rampType  which speed type is wanted
     * @return speed type
     */
    protected String getSpeedType(boolean rampType) {
        if (rampType) {
            if (isRamping()) {
                return _ramp._endSpeedType;
            } else if (_waitForClear || _halt) {
                return Warrant.Stop;
            }
        }
        return _speedType;
    }

    /*
     * warrant.cancelDelayRamp()  called for immediate Stop commands
     * When die==true for ending the warrant run.
     */
    synchronized protected boolean cancelRamp(boolean die) {
        // _ramp.quit sets "stop" and notifies "waits"
        if (_ramp != null) {
            if (die) {
                _ramp.quit(true);
            } else {
                if(!_ramp.ready) {
                    _ramp.quit(false);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * do throttle setting
     * @param speed throttle setting. Modified to sType if from script.
     * UnModified if from ThrottleRamp or stop speeds.
     */
     protected void setSpeed(float speed) {
        _speedUtil.speedChange(speed);  // call before this setting to compute travel of last setting
        _throttle.setSpeedSetting(speed);
        // Late update to GUI is OK, this is just an informational status display
        _warrant.fireRunStatus("SpeedChange", null, null);
        if (log.isDebugEnabled()) 
            log.debug("{}: _throttle.setSpeedSetting({}) called, ({}).",
                    _warrant.getDisplayName(), speed, _speedType);
    }

    protected float getSpeedSetting() {
        float speed = _throttle.getSpeedSetting();
        if (speed < 0.0f) {
            _throttle.setSpeedSetting(0.0f);
            speed = _throttle.getSpeedSetting();
        }
        return speed;
    }

    protected float getScriptSpeed() {
        return _normalSpeed;
    }

    /**
     * Utility for unscripted speed changes.
     * Records current type and sets time ratio.
     * @param speedType name of speed change type
     */
    private void setSpeedRatio(String speedType) {
        if (speedType.equals(Warrant.Normal)) {
            _timeRatio = 1.0f;
        } else {
            float speedMod = _speedUtil.modifySpeed(1.0f, speedType);
            _timeRatio = 1.0f / speedMod;
        }
    }

    /*
     * Do immediate speed change.
     */
    protected synchronized void setSpeedToType(String speedType) {
        float speed = getSpeedSetting();
        if (log.isDebugEnabled())  {
            log.debug("setSpeedToType({}) speed={} scriptSpeed={}", speedType, speed, _normalSpeed);
        }
        if (speedType.equals(Warrant.EStop)) {
            setStop(true);
            advanceToCommandIndex(_idxCurrentCommand + 1);  // skip current command
        } else if (speedType.equals(Warrant.Stop)) {
            setStop(false);
            advanceToCommandIndex(_idxCurrentCommand + 1);  // skip current command
        } else if (speed <= 0.0f || (_ramp != null && _ramp._endSpeedType.equals(Warrant.Stop))) {
            return;
        } else {
            if (speedType.equals(getSpeedType(true))) {
                return;
            }
            _speedType = speedType;     // set speedType regardless
            setSpeedRatio(speedType);
        }
    }

    /**
     * Command to stop (or resume speed) of train from Warrant.controlRunTrain()
     * of user's override of throttle script.  Also from error conditions
     * such as losing detection of train's location.
     * @param halt true if train should halt
     */
    public synchronized void setHalt(boolean halt) {
        if (log.isDebugEnabled()) 
            log.debug("{}: setHalt({}): _atHalt= {}, _waitForClear= {}",
                  _warrant.getDisplayName(), halt, _atHalt, _waitForClear);
        if (!halt) {    // resume normal running
            _halt = false;
            if (!_atClear) {
                if (log.isDebugEnabled()) 
                    log.debug("setHalt calls notify()");
                notifyAll();   // free wait at _atHalt or _atClear
            }
        } else {
            _halt = true;
        }
    }

    private long getTimeToNextCommand() {
        if (_commandTime > 0) {
            // millisecs already moving on pending command's time.
            long elapsedTime = System.currentTimeMillis() - _commandTime;
            return Math.max(0, (_currentCommand.getTime() - elapsedTime));
        }
        return 0;
    }
    
    /**
     * Command to stop or smoothly resume speed. Stop due to
     * signal or occupation stopping condition ahead.  Caller
     * follows with call for type of stop to make.
     * Track condition override of throttle script.
     * @param wait true if train should stop
     */
    private synchronized void setWaitforClear(boolean wait) {
        if (log.isDebugEnabled()) 
            log.debug("{}: setWaitforClear({}): _atClear= {}, throttle speed= {}, _halt= {}",
                   _warrant.getDisplayName(), wait, _atClear,  getSpeedSetting(), _halt);
        if (!wait) {    // resume normal running
            _waitForClear = false;
            if (!_atHalt) {
                if (log.isDebugEnabled()) 
                    log.debug("setWaitforClear calls notify");
                notifyAll();   // free wait at _atClear
            }
        } else {
            _waitForClear = true;
        }
    }

    String debugInfo() {
        StringBuffer info = new StringBuffer("\nEngineer ");
        info.append(getName()); info.append(", Thread.State= "); info.append(getState());
        info.append(", isAlive= "); info.append(isAlive());
        info.append(", isInterrupted= "); info.append(isInterrupted());
        info.append("\n\tThrottle setting= "); info.append(getSpeedSetting());
        info.append(", scriptSpeed= "); info.append(getScriptSpeed());
        info.append(". runstate= "); info.append(Warrant.RUN_STATE[getRunState()]);
        int cmdIdx = getCurrentCommandIndex();
         
        if (cmdIdx < _commands.size()) {
            info.append("\n\tCommand #"); info.append(cmdIdx + 1); 
            info.append(": "); info.append(_commands.get(cmdIdx).toString());
        } else {
            info.append("\n\t\tAt last command.");
        }
        // Note: command indexes biased from 0 to 1 to match Warrant's 1-based display of commands.
        info.append("\n\tEngineer flags: _waitForClear= "); info.append(_waitForClear);
        info.append(", _atclear= "); info.append(_atClear);
        info.append(", _halt= "); info.append(_halt); 
        info.append(", _atHalt= "); info.append(_atHalt);
        if (_synchBlock != null) {
            info.append("\n\t\tWaiting for Sync at \"");info.append(_synchBlock.getDisplayName());
        }
        info.append("\"\n\t\t_setRunOnET= "); info.append(_setRunOnET); 
        info.append(", _runOnET= "); info.append(_runOnET);
        info.append("\n\t\t_stopPending= "); info.append(_stopPending); 
        info.append(", _abort= "); info.append(_abort);
        info.append("\n\t\t_speedType= \""); info.append(_speedType);
        info.append("\""); info.append("\n\tStack trace:");
        for (StackTraceElement elem :getStackTrace()) {
            info.append("\n\t\t");
            info.append(elem.getClassName()); info.append("."); info.append(elem.getMethodName());
            info.append(", line "); info.append(elem.getLineNumber());
        }
        if (_ramp != null) {
            info.append("\n\tRamp Thread.State= "); info.append(_ramp.getState());
            info.append(", isAlive= "); info.append(_ramp.isAlive());
            info.append(", isInterrupted= "); info.append(_ramp.isInterrupted());
            info.append("\n\tRamp flags: ready= "); info.append(_ramp.isReady());
            info.append(", holdRamp= "); info.append(_ramp.holdRamp);
            info.append(", stop= "); info.append(_ramp.stop);
            info.append(", _die= "); info.append(_ramp._die);
            info.append("\n\t\tEndSpeedType= \""); info.append(_ramp._endSpeedType);
            info.append("\""); info.append("\n\tStack trace:");
            for (StackTraceElement elem : _ramp.getStackTrace()) {
                info.append("\n\t\t");
                info.append(elem.getClassName()); info.append("."); info.append(elem.getMethodName());
                info.append(", line "); info.append(elem.getLineNumber());
            }
        } else {
            info.append("\n\tNo ramp.");
        }
        return info.toString();
    }

    /**
     * Immediate stop command from Warrant.controlRunTrain()-user
     * or from Warrant.goingInactive()-train lost
     * or from setMovement()-overrun, possible collision risk.
     * Do not ramp.
     * @param eStop true for emergency stop
     */
    private synchronized void setStop(boolean eStop) {
        float speed = _throttle.getSpeedSetting();
        if (speed > 0.0f) {
            cancelRamp(false);
        }
        if (eStop) {
            setHalt(true);
            setSpeed(-0.1f);
            setSpeed(0.0f);
        } else {
            setSpeed(0.0f);
            setWaitforClear(true);
        }
        if (log.isDebugEnabled()) 
            log.debug("{}: setStop({}) speed={} scriptSpeed={}", _warrant.getDisplayName(), speed, _normalSpeed);
    }

    public int getRunState() {
        if (_stopPending) { // 
            if (_halt) {
                return Warrant.RAMP_HALT;
            }
            return Warrant.STOP_PENDING;
        } else if (_halt) {
            return Warrant.HALT;
        } else if (_waitForClear) {
            return Warrant.WAIT_FOR_CLEAR;
        } else if (_waitForSensor) {
            return Warrant.WAIT_FOR_SENSOR;
        } else if (_abort) {
            return Warrant.ABORT;
        } else if (_synchBlock != null) {
            return Warrant.WAIT_FOR_TRAIN;
        } else if (isRamping()) {
            return Warrant.SPEED_RESTRICTED;
        }
        return Warrant.RUNNING;
    }

    public void stopRun(boolean abort, boolean turnOffFunctions) {
        if (abort) {
            _abort =true;
        }
        if (_waitSensor != null) {
            _waitSensor.removePropertyChangeListener(this);
        }
        cancelRamp(true);

        if (_throttle != null) {
            if (_throttle.getSpeedSetting() > 0.0f) {
                if (abort) {
                    _throttle.setSpeedSetting(-1.0f);
                }
                setSpeed(0.0f);
                if (turnOffFunctions) {
                    _throttle.setF0(false);
                    _throttle.setF1(false);
                    _throttle.setF2(false);
                    _throttle.setF3(false);
                }
            }
            _warrant.releaseThrottle(_throttle);
        }
    }

    private void setForward(ValueType type) {
        if (type == ValueType.VAL_TRUE) {
            _throttle.setIsForward(true);
        } else if (type == ValueType.VAL_FALSE) {
            _throttle.setIsForward(false);
        } else {
            throw new java.lang.IllegalArgumentException("setForward type " + type + " wrong");
        }
    }

    private void setFunction(int cmdNum, ValueType type) {
        if ( cmdNum < 0 || cmdNum > 28 ) {
            throw new java.lang.IllegalArgumentException("setFunction " + cmdNum + " out of range");
        }
        if (type == ValueType.VAL_ON) {
            _throttle.setFunction(cmdNum, true);
        } else if (type == ValueType.VAL_OFF) {
            _throttle.setFunction(cmdNum,false);
        } else {
            throw new java.lang.IllegalArgumentException("setFunction type " + type + " wrong");
        }
    }

    private void setFunctionMomentary(int cmdNum, ValueType type) {
        if ( cmdNum < 0 || cmdNum > 28 ) {
            log.error("Function value {} out of range",cmdNum);
            throw new java.lang.IllegalArgumentException("setFunctionMomentary " + cmdNum + " out of range");
        }
        if (type == ValueType.VAL_ON) {
            _throttle.setFunctionMomentary(cmdNum, true);
        } else if (type == ValueType.VAL_OFF) {
            _throttle.setFunctionMomentary(cmdNum,false);
        } else {
            throw new java.lang.IllegalArgumentException("setFunctionMomentary type " + type + " wrong");
        }
    }

    /**
     * Set Sensor state
     */
    private void setSensor(NamedBeanHandle<?> handle, CommandValue cmdVal) {
        NamedBean bean = handle.getBean();
        if (!(bean instanceof Sensor)) {
            log.error("setSensor: {} not a Sensor!", bean.getDisplayName());
            return;
        }
        jmri.Sensor s = (Sensor)bean;
        ValueType type = cmdVal.getType();
        try {
            if (type == ValueType.VAL_ACTIVE) {
                s.setKnownState(jmri.Sensor.ACTIVE);
            } else if (type == ValueType.VAL_INACTIVE) {
                s.setKnownState(jmri.Sensor.INACTIVE);
            } else {
                throw new java.lang.IllegalArgumentException("setSensor type " + type + " wrong");
            }
            _warrant.fireRunStatus("SensorSetCommand", type.toString(), s.getDisplayName());
        } catch (jmri.JmriException e) {
            log.warn("Exception setting sensor {} in action", handle.toString());
        }
    }

    /**
     * Wait for Sensor state event
     */
    private void waitForSensor(NamedBeanHandle<?> handle, CommandValue cmdVal) {
        if (_waitSensor != null) {
            _waitSensor.removePropertyChangeListener(this);
        }
        NamedBean bean = handle.getBean();
        if (!(bean instanceof Sensor)) {
            log.error("setSensor: {} not a Sensor!", bean.getDisplayName());
            return;
        }
        _waitSensor = (Sensor)bean;
        ThrottleSetting.ValueType type = cmdVal.getType();
        if (type == ValueType.VAL_ACTIVE) {
            _sensorWaitState = Sensor.ACTIVE;
        } else if (type == ValueType.VAL_INACTIVE) {
            _sensorWaitState = Sensor.INACTIVE;
        } else {
            throw new java.lang.IllegalArgumentException("waitForSensor type " + type + " wrong");
        }
        int state = _waitSensor.getKnownState();
        if (state == _sensorWaitState) {
            log.info("Engineer: state of event sensor {} already at state {}", _waitSensor.getDisplayName(), type.toString());
            return;
        }
        _waitSensor.addPropertyChangeListener(this);
        if (log.isDebugEnabled())
            log.debug("Listen for propertyChange of {}, wait for State= {}", _waitSensor.getDisplayName(), _sensorWaitState);
        // suspend commands until sensor changes state
        synchronized (this) {   // DO NOT USE _waitForSensor for synch
            _waitForSensor = true;
            while (_waitForSensor) {
                try {
                    _warrant.fireRunStatus("SensorWaitCommand", type.toString(), _waitSensor.getDisplayName());
                    wait();
                    String name =  _waitSensor.getDisplayName();    // save name, _waitSensor will be null 'eventually'
                    _warrant.fireRunStatus("SensorWaitCommand", null, name);
                } catch (InterruptedException ie) {
                    log.error("Engineer interrupted at _waitForSensor ",ie);
                    _warrant.debugInfo();
                    Thread.currentThread().interrupt();
                } finally {
                    clearSensor();
                }
            }
        }
    }

    private void clearSensor() {
        if (_waitSensor != null) {
            _waitSensor.removePropertyChangeListener(this);
        }
        _sensorWaitState = 0;
        _waitForSensor = false;
        _waitSensor = null;
    }

    protected Sensor getWaitSensor() {
        return _waitSensor;
    }

    @Override
    @SuppressFBWarnings(value = "NN_NAKED_NOTIFY", justification="Sensor change on another thread is expected even when Engineer (this) has not done any modifing")
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (log.isDebugEnabled())
            log.debug("propertyChange {} new value= {}", evt.getPropertyName(), evt.getNewValue());
        if ((evt.getPropertyName().equals("KnownState")
                && ((Number) evt.getNewValue()).intValue() == _sensorWaitState)) {
            synchronized (this) {
                    notifyAll();  // free sensor wait
            }
        }
    }

    private void runWarrant(NamedBeanHandle<?> handle, CommandValue cmdVal) {
        NamedBean bean = handle.getBean();
        if (!(bean instanceof Warrant)) {
            log.error("runWarrant: {} not a warrant!", bean.getDisplayName());
            return;
        }
        Warrant warrant =  (Warrant)bean;

        int num = Math.round(cmdVal.getFloat());    // loops only
        if (num <= 0) { // do the countdown for all linked warrants.
            return;
        }
        num--;  // decrement loop count
        cmdVal.setFloat(num);
        java.awt.Color color = java.awt.Color.red;

        String msg = null;
        if (_warrant.getSpeedUtil().getDccAddress().equals(warrant.getSpeedUtil().getDccAddress())) {
            // Same loco, perhaps different warrant
            if (log.isDebugEnabled()) {
                log.debug("Loco address {} finishes warrant {} and starts warrant {}",
                        warrant.getSpeedUtil().getDccAddress(), _warrant.getDisplayName(), warrant.getDisplayName());
            }
            Thread checker = new CheckForTermination(_warrant, warrant, num, _currentCommand.getTime());
            checker.start();
            if (log.isDebugEnabled()) log.debug("Exit runWarrant");
            return;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Loco address {} on warrant {} and starts loco {} on warrant {}",
                        _warrant.getSpeedUtil().getDccAddress(), _warrant.getDisplayName(),
                        warrant.getSpeedUtil().getDccAddress(), warrant.getDisplayName());
            }
            msg = WarrantTableFrame.getDefault().runTrain(warrant, Warrant.MODE_RUN);
            if (msg != null) {
                msg = Bundle.getMessage("CannotRun", warrant.getDisplayName(), msg);
            } else {
                msg = Bundle.getMessage("linkedLaunch",
                        warrant.getDisplayName(), _warrant.getDisplayName(),
                        warrant.getfirstOrder().getBlock().getDisplayName(),
                        _warrant.getfirstOrder().getBlock().getDisplayName());
                color = WarrantTableModel.myGreen;
           }
        }
        final String m = msg;
        java.awt.Color c = color;
        Engineer.setFrameStatusText(m, c, true);
        log.debug("Exit runWarrant - {}",msg);
    }

    private static class CheckForTermination extends Thread {
        Warrant oldWarrant;
        Warrant newWarrant;
        int num;
        long timeLimit;

        CheckForTermination(Warrant oldWar, Warrant newWar, int n, long time) {
            oldWarrant = oldWar;
            newWarrant = newWar;
            num = n;
            timeLimit = time + 10000L;    // max wait time to launch is command et + 10 seconds..
            if (log.isDebugEnabled()) log.debug("checkForTermination({}, {}, {}, {})",
                    oldWarrant.getDisplayName(), newWarrant.getDisplayName(), num, time);
         }

        @Override
        public void run() {
            OBlock endBlock = oldWarrant.getLastOrder().getBlock();
            long time = 0;
            String msg = null;
            while (time <= timeLimit) {
                if (oldWarrant.getRunMode() == Warrant.MODE_NONE) {
                    break;
                }
                int priority = Thread.currentThread().getPriority();
                try {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    Thread.sleep(100);
                    time += 100;
                } catch (InterruptedException ie) {
                    time = timeLimit;
                    msg = Bundle.getMessage("CannotRun", newWarrant.getDisplayName(), ie);
                } finally {
                    Thread.currentThread().setPriority(priority);
                }
            }
            if (time >= timeLimit || log.isDebugEnabled()) {
                log.info("Waited {}ms for warrant \"{}\" to terminate. runMode={}",
                        time, oldWarrant.getDisplayName(), oldWarrant.getRunMode());
            }
            if (oldWarrant.getRunMode() != Warrant.MODE_NONE) {
                log.error(Bundle.getMessage("cannotLaunch",
                        newWarrant.getDisplayName(), oldWarrant.getDisplayName(), endBlock.getDisplayName()));
                return;
            }

            java.awt.Color color;
            msg = WarrantTableFrame.getDefault().runTrain(newWarrant, Warrant.MODE_RUN);
            if (msg != null) {
                msg = Bundle.getMessage("CannotRun", newWarrant.getDisplayName(), msg);
                color = java.awt.Color.red;
            } else {
                if (oldWarrant.equals(newWarrant)) {
                    msg = Bundle.getMessage("reLaunch", oldWarrant.getDisplayName(), (num<0 ? "unlimited" : num));
                } else {
                    msg = Bundle.getMessage("linkedLaunch",
                            newWarrant.getDisplayName(), oldWarrant.getDisplayName(),
                            newWarrant.getfirstOrder().getBlock().getDisplayName(),
                            endBlock.getDisplayName());
                }
                color = WarrantTableModel.myGreen;
            }
            String m = msg;
            java.awt.Color c = color;
            Engineer.setFrameStatusText(m, c, true);
        }
    }

    @SuppressFBWarnings(value = "NN_NAKED_NOTIFY", justification="rampDone is called by ramp thread to clear Engineer waiting for it to finish")
    private void rampDone(boolean stop, String speedType) {
        if (_synchBlock != null) {
            clearWaitForSync(_synchBlock);
        }
        if (!stop && !speedType.equals(Warrant.Stop)) {
            _speedType = speedType;
            setSpeedRatio(speedType);
            setWaitforClear(false);
            setHalt(false);
        }
        _stopPending = false;
        if (!_waitForClear && !_atHalt && !_atClear && !_ramp.holdRamp) {
            synchronized (this) {
                notifyAll();
            }
            log.debug("{}: rampDone called notify.", _warrant.getDisplayName());
            if (_currentCommand.getCommand().equals(Command.NOOP)) {
                _idxCurrentCommand--;   // notify advances command.  Repeat wait for entry to next block
            }
        }
        if (!stop) {
            _warrant.fireRunStatus("RampDone", _halt, speedType);
        }
        if (log.isDebugEnabled()) {
            log.debug("{}: ThrottleRamp {} for speedType \"{}\". Thread.State= {}}", _warrant.getDisplayName(), 
                    (stop?"stopped":"completed"), speedType, (_ramp != null?_ramp.getState():"_ramp is null!"));
        }
    }

    /*
     * *************************************************************************************
     */

     class ThrottleRamp extends Thread {

         private String _endSpeedType;
         private int _endBlockIdx;   // index of block where down ramp ends.
                     // not used for up ramps or control commands.
         private boolean stop = false;    // aborts ramping
         private boolean ready = false;   // ready for call doRamp
         private boolean _die = false;    // kills ramp for good
         private boolean holdRamp = false;

         ThrottleRamp() {
            setName("Ramp(" + _warrant.getTrainName() +")");
            _endBlockIdx = -1;
         }
         boolean isReady() {
             return ready;
         }

         @SuppressFBWarnings(value = "NN_NAKED_NOTIFY", justification="quit is called another thread to clear all ramp waits")
         void quit(boolean die) {
             stop = true;
//             log.debug("{}: ThrottleRamp.quit({})", _warrant.getDisplayName(), die);
             if (die) { // once set to true, do not allow resetting to false
                 _die = die;    // permanent shutdown, warrant running ending
             }
             synchronized (_rampLockObject) {
                 _rampLockObject.notifyAll(); // free waits at ramp time interval
                 log.debug("{}: ThrottleRamp clears _ramp waits", _warrant.getDisplayName());
                 synchronized (this) {
                     notifyAll();
                 }
                 log.debug("{}: ThrottleRamp clears engineer waits", _warrant.getDisplayName());
            }
         }

        void setParameters(String endSpeedType, int endBlockIdx) {
            _endSpeedType = endSpeedType;
            _endBlockIdx = endBlockIdx;
            _stopPending = endSpeedType.equals(Warrant.Stop);                    
        }

        boolean duplicate(String endSpeedType, int endBlockIdx) {
            if (endBlockIdx != _endBlockIdx || 
                    !endSpeedType.equals(_endSpeedType)) {
                return false;
            }
            return true;
        }

        int getEndBlockIndex() {
            return _endBlockIdx;
        }

        /**
         * @param blockIdx  index of block order where ramp finishes
         * @param cmdIdx   current command index
         * @return command index of block where commands should not be executed
         */
        int getCommandIndexLimit(int blockIdx, int cmdIdx) {
            // get next block
            int limit = _commands.size();
            String curBlkName = _warrant.getCurrentBlockName();
            String endBlkName = _warrant.getBlockAt(blockIdx).getDisplayName();
            if (!curBlkName.contentEquals(endBlkName)) {
                for (int cmd = cmdIdx; cmd < _commands.size(); cmd++) {
                    ThrottleSetting ts = _commands.get(cmd);
                    if (ts.getBeanDisplayName().equals(endBlkName) ) {
                        cmdIdx = cmd;
                        break;
                    }
                }
            }
            endBlkName = _warrant.getBlockAt(blockIdx+1).getDisplayName();
            for (int cmd = cmdIdx; cmd < _commands.size(); cmd++) {
                ThrottleSetting ts = _commands.get(cmd);
                if (ts.getBeanDisplayName().equals(endBlkName) && 
                        ts.getValue().getType().equals(ValueType.VAL_NOOP)) {
                    limit = cmd;
                    break;
                }
            }
            log.debug("getCommandIndexLimit: in end block {}, limitIdx = {} in block {}",
                    curBlkName, limit+1, _warrant.getBlockAt(blockIdx).getDisplayName());
            return limit;
        }

        @Override
        @SuppressFBWarnings(value="UW_UNCOND_WAIT", justification="waits may be indefinite until satisfied or thread aborted")
        public void run() {
            ready = true;
            while (!_die) {
                synchronized (_rampLockObject) {
                    try {
                        _rampLockObject.wait(); // wait until notified by rampSpeedTo() calls quit()
                    } catch (InterruptedException ie) {
                        log.debug("As expected {}", ie);
                    }
                }
                ready = false;
                stop = false;
                holdRamp = false;
                if (!_die) {
                    doRamp();
                }
            }
        }

//        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "DLS_DEAD_LOCAL_STORE", justification = "Engineer needs _normalSpeed to be updated")
        public void doRamp() {
            // At the the time 'right now' is the command indexed by _idxCurrentCommand-1"
            // is done. The main thread (Engineer) is waiting to do the _idxCurrentCommand.
            // A non-scripted speed change is to begin now.
            // If moving, the current speed is _normalSpeed modified by the current _speedType,
            // that is, the actual throttle setting.
            // If _endBlockIdx >= 0, this indexes the block where the the speed change must be
            // completed. the final speed change should occur just before entry into the next
            // block. This final speed change must be the exit speed of block '_endBlockIdx'
            // modified by _endSpeedType.
            // If _endBlockIdx < 0, for down ramps this should be a user initiated stop (Halt)
            // the endSpeed should be 0.
            // For up ramps, the _endBlockIdx and endSpeed are unknown. The script may have 
            // speed changes scheduled during the time needed to up ramp. Note the code below
            // to negotiate and modify the RampData so that the end speed of the ramp makes a
            // smooth transition to the speed of the script (modified by _endSpeedType) 
            // when the script resumes.
            // Non-speed commands are executed at their proper times during ramps.
            // Ramp calculations are based on the fact that the distance traveled during the
            // ramp is the same as the distance the unmodified script would travel, albeit
            // the times of travel are quite different.
            // Note on ramp up endSpeed should match scripted speed modified by endSpeedType
            float speed = getSpeedSetting();  // current speed setting
            float endSpeed;   // requested end speed
            int commandIndexLimit;
            if (_endBlockIdx >= 0) {
                commandIndexLimit = getCommandIndexLimit(_endBlockIdx, _idxCurrentCommand);
                endSpeed = _speedUtil.getBlockSpeedInfo(_endBlockIdx).getExitSpeed();
                endSpeed = _speedUtil.modifySpeed(endSpeed, _endSpeedType);
            } else {
                commandIndexLimit = _commands.size();
                endSpeed = _speedUtil.modifySpeed(_normalSpeed, _endSpeedType);
            }
            CommandValue cmdVal = _currentCommand.getValue();
            long timeToSpeedCmd = getTimeToNextCommand();

            if (log.isDebugEnabled()) {
                log.debug("RAMP {} \"{}\" speed from {}, to {}, at EndBlock \"{}\" at Cmd#{} to Cmd#{}. timeToNextCmd= {}",
                       (endSpeed > speed ? "UP" : "DOWN"), _endSpeedType, speed, endSpeed,
                       (_endBlockIdx>=0?_warrant.getBlockAt(_endBlockIdx).getDisplayName():"not required"),
                       _idxCurrentCommand+1, commandIndexLimit, timeToSpeedCmd);
                       // Note: command indexes biased from 0 to 1 to match Warrant display of commands.
            }
            float throttleIncrement = _speedUtil.getRampThrottleIncrement();
            float scriptTrackSpeed = _speedUtil.getTrackSpeed(_normalSpeed);

            synchronized (this) {
                try {
                    if (endSpeed > speed) {
                        // Up ramp may advance the train beyond the point where the script is interrupted.
                        // The ramp up will take time and the script may have other speed commands while
                        // ramping up. So the actual script speed may not match the endSpeed when the ramp
                        // up distance is traveled.  We must compare 'endSpeed' to 'scriptSpeed' at each
                        // step and skip scriptSpeeds to insure that endSpeed matches scriptSpeed when
                        // the ramp ends.
                        RampData rampData = _speedUtil.getRampForSpeedChange(speed, 1.0f);
                        int timeIncrement = rampData.getRampTimeIncrement();
                        ListIterator<Float> iter = rampData.speedIterator(true);
                        speed = iter.next().floatValue();   // skip repeat of current speed

                        float rampDist = 0;
                        float cmdDist = timeToSpeedCmd * scriptTrackSpeed;
                        
                        while (!stop && iter.hasNext()) {
                            speed = iter.next().floatValue();
                            if (speed >= _speedUtil.modifySpeed(_normalSpeed, _endSpeedType)) {
                                break;
                            }
                            setSpeed(speed);

                            // during ramp down the script may have non-speed commands that should be executed.
                            if (!stop && rampDist >= cmdDist && _idxCurrentCommand < commandIndexLimit) {
                                cmdVal = _currentCommand.getValue();
                                if (cmdVal.getType().equals(ThrottleSetting.ValueType.VAL_FLOAT)) {
                                    _normalSpeed = cmdVal.getFloat();
                                    scriptTrackSpeed = _speedUtil.getTrackSpeed(_normalSpeed);
                                    if (log.isDebugEnabled()) {
                                        log.debug("Cmd #{} for speed= {} skipped.",
                                                _idxCurrentCommand+1, _normalSpeed);
                                    }
                                    cmdDist = 0;
                                } else {
                                    executeComand(_currentCommand, timeIncrement);
                                }
                                _currentCommand = _commands.get(++_idxCurrentCommand);
                                cmdDist = scriptTrackSpeed * _currentCommand.getTime();
                                rampDist = 0;
                                advanceToCommandIndex(_idxCurrentCommand); // skip up to this command
                            }

                            try {
                                wait(timeIncrement);
                            } catch (InterruptedException ie) {
                                stop = true;
                            }

                            rampDist += _speedUtil.getDistanceTraveled(speed, _speedType, timeIncrement);
                       }                        

                    } else {     // decreasing, ramp down to a modified speed
                        // Down ramp may advance the train beyond the point where the script is interrupted.
                        // Any down ramp requested with _endBlockIdx >= 0 is expected to end at the end of
                        // a block i.e. the block of BlockOrder indexed by _endBlockIdx.
                        // Therefore script should resume at the exit to this block.
                        // During ramp down the script may have other Non speed commands that should be executed.

                        RampData rampData = _speedUtil.getRampForSpeedChange(speed, endSpeed);
                        int timeIncrement = rampData.getRampTimeIncrement();
                        ListIterator<Float> iter = rampData.speedIterator(false);
                        speed = iter.previous().floatValue();   // skip repeat of current throttle setting

                        float rampDist = 0;
                        float cmdDist = timeToSpeedCmd * scriptTrackSpeed;
 
                        while (!stop && iter.hasPrevious()) {
                            speed = iter.previous().floatValue();
                            setSpeed(speed);

                            if (_endBlockIdx >= 0) {    // correction code for ramps that are too long or too short
                                if ( _warrant.getCurrentOrderIndex() > _endBlockIdx) {
                                    // loco overran end block.  Set end speed and leave ramp
                                    setSpeed(endSpeed);
                                    stop = true;
                                } else if ( _warrant.getCurrentOrderIndex() < _endBlockIdx && 
                                        _endSpeedType.equals(Warrant.Stop) && Math.abs(speed - endSpeed) <.001f) {
                                    // At last speed change to set throttle was 0.0, but train has not 
                                    // reached the last block. Let loco creep to end block at current setting.
                                    if (log.isDebugEnabled())
                                        log.debug("Extending ramp to reach block {}. speed= {}",
                                                _warrant.getBlockAt(_endBlockIdx).getDisplayName(), speed);
                                    int waittime = 0;
                                    while (_endBlockIdx > _warrant.getCurrentOrderIndex() && waittime <= 60*timeIncrement && getSpeedSetting() > 0) {
                                        // Until loco reaches end block, continue current speed.
                                        if (waittime == 5*timeIncrement || waittime == 10*timeIncrement || 
                                                waittime == 15*timeIncrement || waittime == 20*timeIncrement) {
                                            // maybe train stalled on previous speed step. Bump speed up a notch at 3s, another at 9
                                            setSpeed(getSpeedSetting() + throttleIncrement);
                                        }
                                        try {
                                            wait(timeIncrement);
                                            waittime += timeIncrement;
                                        } catch (InterruptedException ie) {
                                            stop = true;
                                        }
                                    }
                                    try {
                                        wait(timeIncrement);
                                    } catch (InterruptedException ie) {
                                        stop = true;
                                    }   
                                }
                            }

                            // during ramp down the script may have non-speed commands that should be executed.
                            if (!stop && rampDist >= cmdDist && _idxCurrentCommand < commandIndexLimit) {
                                cmdVal = _currentCommand.getValue();
                                if (cmdVal.getType().equals(ThrottleSetting.ValueType.VAL_FLOAT)) {
                                    _normalSpeed = cmdVal.getFloat();
                                    scriptTrackSpeed = _speedUtil.getTrackSpeed(_normalSpeed);
                                    if (log.isDebugEnabled()) {
                                        log.debug("Cmd #{} for speed= {} skipped.",
                                                _idxCurrentCommand+1, _normalSpeed);
                                    }
                                    cmdDist = 0;
                                } else {
                                    executeComand(_currentCommand, timeIncrement);
                                }
                                _currentCommand = _commands.get(++_idxCurrentCommand);
                                cmdDist = scriptTrackSpeed * _currentCommand.getTime();
                                rampDist = 0;
                                advanceToCommandIndex(_idxCurrentCommand); // skip up to this command
                            }

                            try {
                                wait(timeIncrement);
                            } catch (InterruptedException ie) {
                                stop = true;
                            }

                            rampDist += _speedUtil.getDistanceTraveled(speed, _speedType, timeIncrement);
                       }

                       if (_endBlockIdx >= 0) {
                            long cmdStart = System.currentTimeMillis();
                            while (_idxCurrentCommand < commandIndexLimit) {
                                NamedBean bean = _currentCommand.getNamedBeanHandle().getBean();
                                if (bean instanceof OBlock) {
                                    OBlock blk = (OBlock)bean;
                                    if (_endBlockIdx < _warrant.getIndexOfBlock(blk, _endBlockIdx)) {
                                        // script is past end point, command should be NOOP.
                                        // regardless, don't execute any commands.
                                        break;
                                    }
                                }
                                // Still in endBlock. Execute any remaining non-speed commands.
                                cmdVal = _currentCommand.getValue();
                                if (!cmdVal.getType().equals(ThrottleSetting.ValueType.VAL_FLOAT)) {
                                    executeComand(_currentCommand, System.currentTimeMillis() - cmdStart);
                                } else {
                                    _normalSpeed = cmdVal.getFloat();
                                    if (log.isDebugEnabled()) {
                                        log.debug("Cmd #{} for speed {} skipped. warrant {}",
                                                _idxCurrentCommand+1, _normalSpeed, _warrant.getDisplayName());
                                    }
                                }
                                _currentCommand = _commands.get(++_idxCurrentCommand);
                                advanceToCommandIndex(_idxCurrentCommand); // skip up to this command
                            }
                        }
                    }

                } finally {
                    if (log.isDebugEnabled()) {
                        log.debug("Ramp Done. End Blk= {}, _idxCurrentCommand={} resumeIdx={}, commandIndexLimit={}. warrant {}",
                                (_endBlockIdx>=0?_warrant.getBlockAt(_endBlockIdx).getDisplayName():"not required"),
                                _idxCurrentCommand+1, _idxSkipToSpeedCommand, commandIndexLimit, _warrant.getDisplayName());
                    }
                }
            }
            ready = true;
            rampDone(stop, _endSpeedType);
            stop = false;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(Engineer.class);
}
