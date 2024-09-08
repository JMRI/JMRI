package jmri.jmrit.logix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.Nonnull;

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

/**
 * Execute a throttle command script for a warrant.
 * <p>
 * This generally operates on its own thread, but calls the warrant
 * thread via Warrant.fireRunStatus to show status. fireRunStatus uses
 * ThreadingUtil.runOnGUIEventually to display on the layout thread.
 *
 * @author Pete Cressman Copyright (C) 2009, 2010, 2020
 */
/*
 * ************************ Thread running the train ****************
 */
class Engineer extends Thread implements java.beans.PropertyChangeListener {

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
    private final Object _rampLockObject = new Object();
    private final Object _synchLockObject = new Object();
    private final Object _clearLockObject = new Object();
    private boolean _atHalt = false;
    private boolean _atClear = false;
    private final SpeedUtil _speedUtil;
    private OBlock _synchBlock = null;
    private Thread _checker = null;

    private ThrottleRamp _ramp;
    private boolean _holdRamp = false;
    private boolean _isRamping = false;

    Engineer(Warrant warrant, DccThrottle throttle) {
        _warrant = warrant;
        _throttle = throttle;
        _speedUtil = warrant.getSpeedUtil();
        _commands = _warrant.getThrottleCommands();
        _idxCurrentCommand = 0;
        _currentCommand = _commands.get(_idxCurrentCommand);
        _idxSkipToSpeedCommand = 0;
        _waitForSensor = false;
        setName("Engineer(" + _warrant.getTrainName() +")");
    }

    @Override
    @SuppressFBWarnings(value="UW_UNCOND_WAIT", justification="waits may be indefinite until satisfied or thread aborted")
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("Engineer started warrant {} _throttle= {}", _warrant.getDisplayName(), _throttle.getClass().getName());
        }
        int cmdBlockIdx = 0;
        while (_idxCurrentCommand < _commands.size()) {
            while (_idxSkipToSpeedCommand > _idxCurrentCommand) {
                if (log.isDebugEnabled()) {
                    ThrottleSetting ts = _commands.get(_idxCurrentCommand);
                    log.debug("{}: Skip Cmd #{}: {} Warrant", _warrant.getDisplayName(), _idxCurrentCommand+1, ts);
                    // Note: command indexes biased from 0 to 1 to match Warrant display of commands.
                }
                _idxCurrentCommand++;
            }
            if (_idxCurrentCommand == _commands.size()) {
                // skip commands on last block may advance too far. Due to looking for a NOOP
                break;
            }
            _currentCommand = _commands.get(_idxCurrentCommand);
            long cmdWaitTime = _currentCommand.getTime();    // time to wait before executing command
            ThrottleSetting.Command command = _currentCommand.getCommand();
            _runOnET = _setRunOnET;     // OK to set here
            if (command.hasBlockName()) {
                int idx = _warrant.getIndexOfBlockAfter((OBlock)_currentCommand.getBean(), cmdBlockIdx);
                if (idx >= 0) {
                    cmdBlockIdx = idx;
                }
            }
            if (cmdBlockIdx < _warrant.getCurrentOrderIndex() ||
                    (command.equals(Command.NOOP) && (cmdBlockIdx <= _warrant.getCurrentOrderIndex()))) {
                // Train advancing too fast, need to process commands more quickly,
                // allow some time for whistle toots etc.
                cmdWaitTime = Math.min(cmdWaitTime, 200); // 200ms per command should be enough for toots etc.
                if (log.isDebugEnabled()) {
                    log.debug("{}: Train reached block \"{}\" before script et={}ms",
                            _warrant.getDisplayName(), _warrant.getCurrentBlockName(), _currentCommand.getTime());
                }
            }
            if (_abort) {
                break;
            }

            long cmdStart = System.currentTimeMillis();
            if (log.isDebugEnabled()) {
                log.debug("{}: Start Cmd #{} for block \"{}\" currently in \"{}\". wait {}ms to do cmd {}",
                    _warrant.getDisplayName(), _idxCurrentCommand+1, _currentCommand.getBeanDisplayName(),
                    _warrant.getCurrentBlockName(), cmdWaitTime, command);
                    // Note: command indexes biased from 0 to 1 to match Warrant display of commands.
            }
            synchronized (this) {
                if (!Warrant.Normal.equals(_speedType)) {
                    // extend it when speed has been modified from scripted speed
                    cmdWaitTime = (long)(cmdWaitTime*_timeRatio);
                }
                try {
                    if (cmdWaitTime > 0) {
                        wait(cmdWaitTime);
                    }
                } catch (InterruptedException ie) {
                    log.debug("InterruptedException during time wait", ie);
                    _warrant.debugInfo();
                    Thread.currentThread().interrupt();
                    _abort = true;
                } catch (java.lang.IllegalArgumentException iae) {
                    log.error("At time wait", iae);
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

                synchronized (_synchLockObject) {
                    _synchBlock = _warrant.getBlockAt(cmdBlockIdx);
                    _warrant.fireRunStatus("WaitForSync", _idxCurrentCommand - 1, _idxCurrentCommand);
                    if (log.isDebugEnabled()) {
                        log.debug("{}: Wait for train to enter \"{}\".",
                                _warrant.getDisplayName(), _synchBlock.getDisplayName());
                    }
                    try {
                        _synchLockObject.wait();
                        _synchBlock = null;
                    } catch (InterruptedException ie) {
                        log.debug("InterruptedException during _waitForSync", ie);
                        _warrant.debugInfo();
                        Thread.currentThread().interrupt();
                        _abort = true;
                    }
                }
                if (_abort) {
                    break;
                }
            }

            synchronized (_clearLockObject) {
                // block position and elapsed time are as expected, but track conditions
                // such as signals or rogue occupancy requires waiting
                if (_waitForClear) {
                    try {
                        _atClear = true;
                        if (log.isDebugEnabled()) {
                            log.debug("{}: Waiting for clearance. _waitForClear= {} _halt= {} at block \"{}\" Cmd#{}.",
                                _warrant.getDisplayName(), _waitForClear, _halt,
                                _warrant.getBlockAt(cmdBlockIdx).getDisplayName(), _idxCurrentCommand+1);
                        }
                        _clearLockObject.wait();
                        _waitForClear = false;
                        _atClear = false;
                    } catch (InterruptedException ie) {
                        log.debug("InterruptedException during _atClear", ie);
                        _warrant.debugInfo();
                        Thread.currentThread().interrupt();
                        _abort = true;
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
                        if (log.isDebugEnabled()) {
                            log.debug("{}: Waiting to Resume. _halt= {}, _waitForClear= {}, Block \"{}\".",
                                _warrant.getDisplayName(), _halt, _waitForClear,
                                _warrant.getBlockAt(cmdBlockIdx).getDisplayName());
                        }
                        wait();
                        _halt = false;
                        _atHalt = false;
                    } catch (InterruptedException ie) {
                        log.debug("InterruptedException during _atHalt", ie);
                        _warrant.debugInfo();
                        Thread.currentThread().interrupt();
                        _abort = true;
                    }
                }
            }
            if (_abort) {
                break;
            }

            synchronized (this) {
                while (_isRamping || _holdRamp) {
                    int idx = _idxCurrentCommand;
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("{}: Waiting for ramp to finish at Cmd #{}.",
                                  _warrant.getDisplayName(), _idxCurrentCommand+1);
                        }
                        wait();
                    } catch (InterruptedException ie) {
                        _warrant.debugInfo();
                        Thread.currentThread().interrupt();
                        _abort = true;
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
                    float trackSpeed = _speedUtil.getTrackSpeed(speedMod);
                    _timeRatio = _speedUtil.getTrackSpeed(_normalSpeed) / trackSpeed;
                    _speedUtil.speedChange(speedMod);  // call before this setting to compute travel of last setting
                    setSpeed(speedMod);
                } else {
                    _timeRatio = 1.0f;
                    _speedUtil.speedChange(_normalSpeed);  // call before this setting to compute travel of last setting
                    setSpeed(_normalSpeed);
                }
                break;
            case NOOP:
                break;
            case SET_SENSOR:
                ThreadingUtil.runOnGUIEventually(() ->
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
                ThreadingUtil.runOnGUIEventually(() ->
                    runWarrant(ts.getNamedBeanHandle(), cmdVal));
                break;
            case SPEEDSTEP:
                break;
            case SET_MEMORY:
                ThreadingUtil.runOnGUIEventually(() ->
                    setMemory(ts.getNamedBeanHandle(), cmdVal));
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
        if (log.isTraceEnabled()) {
            log.debug("advanceToCommandIndex to {} - {}", _idxSkipToSpeedCommand+1, _commands.get(idx));
            // Note: command indexes biased from 0 to 1 to match Warrant display of commands, which are 1-based.
        }
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

    protected OBlock getSynchBlock() {
        return _synchBlock;
    }

    /**
     * Called by the warrant when a the block ahead of a moving train goes occupied.
     * typically when this thread is on a timed wait. The call will free the wait.
     * @param block going active.
     */
    protected void clearWaitForSync(OBlock block) {
        // block went active. if waiting on sync, clear it
        if (_synchBlock != null) {
            synchronized (_synchLockObject) {
                if (block.equals(_synchBlock)) {
                    _synchLockObject.notifyAll();
                    if (log.isDebugEnabled()) {
                        log.debug("{}: clearWaitForSync from block \"{}\". notifyAll() called.  isRamping()={}",
                                _warrant.getDisplayName(), block.getDisplayName(), isRamping());
                    }
                    return;
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("{}: clearWaitForSync from block \"{}\". _synchBlock= {} SpeedState={} _atClear={} _atHalt={}",
                    _warrant.getDisplayName(), block.getDisplayName(),
                    (_synchBlock==null?"null":_synchBlock.getDisplayName()), getSpeedState(), _atClear, _atHalt);
        }
    }

    /**
     * Set the Warrant Table Frame Status Text.
     * Saves status to log.
     * @param m the status String.
     * @param c the status colour.
     */
    private static void setFrameStatusText(String m, Color c ) {
        ThreadingUtil.runOnGUIEventually(()-> WarrantTableFrame.getDefault().setStatusText(m, c, true));
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
    @SuppressFBWarnings(value="SLF4J_FORMAT_SHOULD_BE_CONST", justification="False assumption")
    protected synchronized void rampSpeedTo(@Nonnull String endSpeedType, int endBlockIdx) {
        float speed = _speedUtil.modifySpeed(_normalSpeed, endSpeedType);
        if (log.isDebugEnabled()) {
            log.debug("{}: rampSpeedTo: type= {}, throttle from {} to {}.",
                _warrant.getDisplayName(), endSpeedType, getSpeedSetting(),
                speed);
        }
        _speedUtil.speedChange(-1); // Notify not to measure speed for speedProfile
        if (endSpeedType.equals(Warrant.EStop)) {
            setStop(true);
            return;
        }
        if (endSpeedType.equals(Warrant.Stop) && getSpeedSetting() <= 0) {
            setStop(false);
            return; // already stopped, do nothing
        }
        if (_isRamping) {
            if (endSpeedType.equals(_ramp._endSpeedType)) {
                return; // already ramping to speedType
            }
        } else if (speed == getSpeedSetting()){
            // to be sure flags and notification is done
            rampDone(false, endSpeedType, endBlockIdx);
            return; // already at speedType speed
        }
        if (_ramp == null) {
            _ramp = new ThrottleRamp();
            _ramp.start();
        } else if (_isRamping) {
            // for repeated command already ramping
            if (_ramp.duplicate(endSpeedType, endBlockIdx)) {
                return;
            }
            // stop the ramp and replace it
            _holdRamp = true;
            _ramp.quit(false);
        }
        long time = 0;
        int pause = 2 *_speedUtil.getRampTimeIncrement();
        do {
            // may need a bit of time for quit() or start() to get ready
            try {
                wait(40);
                time += 40;
                _ramp.quit(false);
            }
            catch (InterruptedException ie) { // ignore
            }
        } while (time <= pause && _isRamping);

        if (!_isRamping) {
            if (Warrant._trace || log.isDebugEnabled()) {
                log.info(Bundle.getMessage("RampStart", _warrant.getTrainName(),
                        endSpeedType, _warrant.getCurrentBlockName()));
            }
            _ramp.setParameters(endSpeedType, endBlockIdx);
            synchronized (_rampLockObject) {
                _ramp._rampDown = (endBlockIdx >= 0) || endSpeedType.equals(Warrant.Stop);
//                setIsRamping(true);
                _holdRamp = false;
                setWaitforClear(true);
                _rampLockObject.notifyAll(); // free wait at ThrottleRamp.run()
                log.debug("{}: rampSpeedTo calls notify _rampLockObject", _warrant.getDisplayName());
            }
        } else {
            log.error("Can't launch ramp for speed {}! _ramp Thread.State= {}. Waited {}ms",
                    endSpeedType, _ramp.getState(), time);
            _warrant.debugInfo();
            setSpeedToType(endSpeedType);
            _ramp.quit(true);
            _ramp.interrupt();
            _ramp = null;
        }
    }

    protected boolean isRamping() {
        return _isRamping;
    }
    private void setIsRamping(boolean set) {
        _isRamping = set;
    }

    /**
     * Get the Speed type name. _speedType is the type when moving. Used to restore
     * speeds aspects of signals when halts or other conditions have stopped the train.
     * If 'absolute' is true return the absolute speed of the train, i.e. 'Stop' if
     * train is not moving.
     * @param absolute  which speed type, absolute or allowed movement
     * @return speed type
     */
    protected String getSpeedType(boolean absolute) {
        if (absolute) {
            if (isRamping()) {   // return pending type
                return _ramp._endSpeedType;
            }
            if (_waitForClear || _halt) {
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
                _ramp.interrupt();
            } else {
                if(_isRamping) {
                    _ramp.quit(false);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * do throttle setting
     * @param speed throttle setting about to be set. Modified to sType if from script.
     * UnModified if from ThrottleRamp or stop speeds.
     */
     protected void setSpeed(float speed) {
        _throttle.setSpeedSetting(speed);
        // Late update to GUI is OK, this is just an informational status display
        if (!_abort) {
            _warrant.fireRunStatus("SpeedChange", null, null);
        }
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
        } else if (_normalSpeed > 0.0f) {
            float speedMod = _speedUtil.modifySpeed(_normalSpeed, _speedType);
            if (_normalSpeed > speedMod) {
                float trackSpeed = _speedUtil.getTrackSpeed(speedMod);
                _timeRatio = _speedUtil.getTrackSpeed(_normalSpeed) / trackSpeed;
            } else {
                _timeRatio = 1.0f;
            }
        } else {
            _timeRatio = 1.0f;
        }
    }

    /*
     * Do immediate speed change.
     */
    protected synchronized void setSpeedToType(String speedType) {
        float speed = getSpeedSetting();
        if (log.isDebugEnabled())  {
            log.debug("{}: setSpeedToType({}) speed={} scriptSpeed={}", _warrant.getDisplayName(), speedType, speed, _normalSpeed);
        }
        if (speedType.equals(Warrant.Stop)) {
            setStop(false);
            advanceToCommandIndex(_idxCurrentCommand + 1);  // skip current command
        } else if (speedType.equals(Warrant.EStop)) {
            setStop(true);
            advanceToCommandIndex(_idxCurrentCommand + 1);  // skip current command
        } else if (speedType.equals(getSpeedType(true))) {
            return;
        } else {
            _speedType = speedType;     // set speedType regardless
            setSpeedRatio(speedType);
            float speedMod = _speedUtil.modifySpeed(_normalSpeed, _speedType);
            _speedUtil.speedChange(speedMod);  // call before this setting to compute travel of last setting
            setSpeed(speedMod);
        }
    }

    /**
     * Command to stop (or resume speed) of train from Warrant.controlRunTrain()
     * of user's override of throttle script.  Also from error conditions
     * such as losing detection of train's location.
     * @param halt true if train should halt
     */
    protected synchronized void setHalt(boolean halt) {
        if (log.isDebugEnabled())
            log.debug("{}: setHalt({}): _atHalt= {}, _waitForClear= {}",
                  _warrant.getDisplayName(), halt, _atHalt, _waitForClear);
        if (!halt) {    // resume normal running
            _halt = false;
            if (!_atClear) {
                log.debug("setHalt calls notify()");
                notifyAll();   // free wait at _atHalt
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
    protected void setWaitforClear(boolean wait) {
        if (log.isDebugEnabled())
            log.debug("{}: setWaitforClear({}): _atClear= {}, throttle speed= {}, _halt= {}",
                   _warrant.getDisplayName(), wait, _atClear,  getSpeedSetting(), _halt);
        if (!wait) {    // resume normal running
            synchronized (_clearLockObject) {
                log.debug("setWaitforClear calls notify");
                _waitForClear = false;
                _clearLockObject.notifyAll();   // free wait at _atClear
            }
        } else {
            _waitForClear = true;
        }
    }

    String debugInfo() {
        StringBuilder info = new StringBuilder("\n");
        info.append(getName()); info.append(" on warrant= "); info.append(_warrant.getDisplayName());
        info.append("\nThread.State= "); info.append(getState());
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
        info.append("\n\t_speedType= \""); info.append(_speedType); info.append("\" SpeedState= ");
        info.append(getSpeedState().toString()); info.append("\n\tStack trace:");
        for (StackTraceElement elem : getStackTrace()) {
            info.append("\n\t\t");
            info.append(elem.getClassName()); info.append("."); info.append(elem.getMethodName());
            info.append(", line "); info.append(elem.getLineNumber());
        }
        if (_ramp != null) {
            info.append("\n\tRamp Thread.State= "); info.append(_ramp.getState());
            info.append(", isAlive= "); info.append(_ramp.isAlive());
            info.append(", isInterrupted= "); info.append(_ramp.isInterrupted());
            info.append("\n\tRamp flags: _isRamping= "); info.append(_isRamping);
            info.append(", stop= "); info.append(_ramp.stop);
            info.append(", _die= "); info.append(_ramp._die);
            info.append("\n\tRamp Type: "); info.append(_ramp._rampDown ? "DOWN" : "UP");info.append(" ramp");
            info.append("\n\t\tEndSpeedType= \""); info.append(_ramp._endSpeedType);
            int endIdx = _ramp.getEndBlockIndex();
            info.append("\"\n\t\tEndBlockIdx= "); info.append(endIdx);
            if (endIdx >= 0) {
                info.append(" EndBlock= \"");
                info.append(_warrant.getBlockAt(endIdx).getDisplayName());
            }
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
        if (speed <= 0.0f && (_waitForClear || _halt)) {
            return;
        }
        cancelRamp(false);
        if (eStop) {
            setHalt(true);
            setSpeed(-0.1f);
            setSpeed(0.0f);
        } else {
            setSpeed(0.0f);
            setWaitforClear(true);
        }
        log.debug("{}: setStop({}) from speed={} scriptSpeed={}",
            _warrant.getDisplayName(), eStop, speed, _normalSpeed);
    }

    protected Warrant.SpeedState getSpeedState() {
        if (isRamping()) {
            if (_ramp._rampDown) {
                return Warrant.SpeedState.RAMPING_DOWN;
            } else {
                return Warrant.SpeedState.RAMPING_UP;
            }
        }
        return Warrant.SpeedState.STEADY_SPEED;
    }

    protected int getRunState() {
        if (_stopPending) {
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

    @SuppressFBWarnings(value = "NN_NAKED_NOTIFY", justification="quit is called another thread to clear all ramp waits")
    public void stopRun(boolean abort, boolean turnOffFunctions) {
        if (abort) {
            _abort =true;
        }

        synchronized (_synchLockObject) {
            _synchLockObject.notifyAll();
        }
        synchronized (_clearLockObject) {
            _clearLockObject.notifyAll();
        }
        synchronized (this) {
            notifyAll();
        }

        cancelRamp(true);
        if (_waitSensor != null) {
            _waitSensor.removePropertyChangeListener(this);
        }

        if (_throttle != null) {
            if (_throttle.getSpeedSetting() > 0.0f) {
                if (abort) {
                    _throttle.setSpeedSetting(-1.0f);
                }
                setSpeed(0.0f);
                if (turnOffFunctions) {
                    _throttle.setFunction(0, false);
                    _throttle.setFunction(1, false);
                    _throttle.setFunction(2, false);
                    _throttle.setFunction(3, false);
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
     * Set Memory value
     */
    private void setMemory(NamedBeanHandle<?> handle, CommandValue cmdVal) {
        NamedBean bean = handle.getBean();
        if (!(bean instanceof jmri.Memory)) {
            log.error("setMemory: {} not a Memory!", bean );
            return;
        }
        jmri.Memory m = (jmri.Memory)bean;
        ValueType type = cmdVal.getType();

        if (Warrant._trace || log.isDebugEnabled()) {
            log.info("{} : Set memory", Bundle.getMessage("setMemory",
                        _warrant.getTrainName(), m.getDisplayName(), cmdVal.getText()));
        }
        _warrant.fireRunStatus("MemorySetCommand", type.toString(), m.getDisplayName());
        m.setValue(cmdVal.getText());
    }

    /**
     * Set Sensor state
     */
    private void setSensor(NamedBeanHandle<?> handle, CommandValue cmdVal) {
        NamedBean bean = handle.getBean();
        if (!(bean instanceof Sensor)) {
            log.error("setSensor: {} not a Sensor!", bean );
            return;
        }
        jmri.Sensor s = (Sensor)bean;
        ValueType type = cmdVal.getType();
        try {
            if (Warrant._trace || log.isDebugEnabled()) {
                log.info("{} : Set Sensor", Bundle.getMessage("setSensor",
                            _warrant.getTrainName(), s.getDisplayName(), type.toString()));
            }
            _warrant.fireRunStatus("SensorSetCommand", type.toString(), s.getDisplayName());
            if (type == ValueType.VAL_ACTIVE) {
                s.setKnownState(jmri.Sensor.ACTIVE);
            } else if (type == ValueType.VAL_INACTIVE) {
                s.setKnownState(jmri.Sensor.INACTIVE);
            } else {
                throw new java.lang.IllegalArgumentException("setSensor type " + type + " wrong");
            }
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
            log.error("setSensor: {} not a Sensor!", bean );
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
            log.info("Engineer: state of event sensor {} already at state {}",
                _waitSensor.getDisplayName(), type.toString());
            return;
        }
        _waitSensor.addPropertyChangeListener(this);
        if (log.isDebugEnabled()) {
            log.debug("Listen for propertyChange of {}, wait for State= {}",
                _waitSensor.getDisplayName(), _sensorWaitState);
        }
        // suspend commands until sensor changes state
        synchronized (this) {   // DO NOT USE _waitForSensor for synch
            _waitForSensor = true;
            while (_waitForSensor) {
                try {
                    if (Warrant._trace || log.isDebugEnabled()) {
                        log.info("{} : waitSensor", Bundle.getMessage("waitSensor",
                            _warrant.getTrainName(), _waitSensor.getDisplayName(), type.toString()));
                    }
                    _warrant.fireRunStatus("SensorWaitCommand", type.toString(), _waitSensor.getDisplayName());
                    wait();
                    if (!_abort ) {
                        String name =  _waitSensor.getDisplayName();    // save name, _waitSensor will be null 'eventually'
                        if (Warrant._trace || log.isDebugEnabled()) {
                            log.info("{} : wait Sensor Change", Bundle.getMessage("waitSensorChange",
                                    _warrant.getTrainName(), name));
                        }
                        _warrant.fireRunStatus("SensorWaitCommand", null, name);
                    }
                } catch (InterruptedException ie) {
                    log.error("Engineer interrupted at waitForSensor \"{}\"", _waitSensor.getDisplayName(), ie);
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
        if (log.isDebugEnabled()) {
            log.debug("propertyChange {} new value= {}", evt.getPropertyName(), evt.getNewValue());
        }
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
            log.error("runWarrant: {} not a warrant!", bean );
            return;
        }
        Warrant warrant =  (Warrant)bean;

        int num = Math.round(cmdVal.getFloat());    // repeated loops
        if (num == 0) {
            return;
        }
        if (num > 0) { // do the countdown for all linked warrants.
            num--;  // decrement loop count
            cmdVal.setFloat(num);
        }

        if (_warrant.getSpeedUtil().getDccAddress().equals(warrant.getSpeedUtil().getDccAddress())) {
            // Same loco, perhaps different warrant
            if (log.isDebugEnabled()) {
                log.debug("Loco address {} finishes warrant {} and starts warrant {}",
                        warrant.getSpeedUtil().getDccAddress(), _warrant.getDisplayName(), warrant.getDisplayName());
            }
            long time =  0;
            for (int i = _idxCurrentCommand+1; i < _commands.size(); i++) {
                ThrottleSetting cmd = _commands.get(i);
                time += cmd.getTime();
            }
            // same address so this warrant (_warrant) must release the throttle before (warrant) can acquire it
            _checker = new CheckForTermination(_warrant, warrant, num, time);
            _checker.start();
            log.debug("Exit runWarrant");
        } else {
            java.awt.Color color = java.awt.Color.red;
            String msg = WarrantTableFrame.getDefault().runTrain(warrant, Warrant.MODE_RUN);
            if (msg == null) {
                msg = Bundle.getMessage("linkedLaunch",
                        warrant.getDisplayName(), _warrant.getDisplayName(),
                        warrant.getfirstOrder().getBlock().getDisplayName(),
                        _warrant.getfirstOrder().getBlock().getDisplayName());
                color = WarrantTableModel.myGreen;
            }
            if (Warrant._trace || log.isDebugEnabled()) {
                log.info("{} : Warrant Status", msg);
            }
            Engineer.setFrameStatusText(msg, color);
        }
    }

    private class CheckForTermination extends Thread {
        Warrant oldWarrant;
        Warrant newWarrant;
        long waitTime; // time to finish remaining commands

        CheckForTermination(Warrant oldWar, Warrant newWar, int num, long limit) {
            oldWarrant = oldWar;
            newWarrant = newWar;
            waitTime = limit;
            if (log.isDebugEnabled()) {
                log.debug("checkForTermination of \"{}\", before launching \"{}\". waitTime= {})",
                    oldWarrant.getDisplayName(), newWarrant.getDisplayName(), waitTime);
            }
        }

        @Override
        public void run() {
            long time = 0;
            synchronized (this) {
                while (time <= waitTime || oldWarrant.getRunMode() != Warrant.MODE_NONE) {
                    try {
                        wait(100);
                        time += 100;
                    } catch (InterruptedException ie) {
                        log.error("Engineer interrupted at CheckForTermination of \"{}\"",
                            oldWarrant.getDisplayName(), ie);
                        _warrant.debugInfo();
                        Thread.currentThread().interrupt();
                        time = waitTime;
                    } finally {
                    }
                }
            }
            if (time > waitTime || log.isDebugEnabled()) {
                log.info("Waited {}ms for warrant \"{}\" to terminate. runMode={}",
                        time, oldWarrant.getDisplayName(), oldWarrant.getRunMode());
            }
            checkerDone(oldWarrant, newWarrant);
        }

        // send the messages on success of linked launch completion
        private void checkerDone(Warrant oldWarrant, Warrant newWarrant) {
            OBlock endBlock = oldWarrant.getLastOrder().getBlock();
            if (oldWarrant.getRunMode() != Warrant.MODE_NONE) {
                log.error("{} : Cannot Launch", Bundle.getMessage("cannotLaunch",
                        newWarrant.getDisplayName(), oldWarrant.getDisplayName(), endBlock.getDisplayName()));
                return;
            }

            String msg = WarrantTableFrame.getDefault().runTrain(newWarrant, Warrant.MODE_RUN);
            java.awt.Color color = java.awt.Color.red;
            if (msg == null) {
                CommandValue cmdVal = _currentCommand.getValue();
                int num = Math.round(cmdVal.getFloat());
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
            if (Warrant._trace || log.isDebugEnabled()) {
                log.info("{} : Launch", msg);
            }
            Engineer.setFrameStatusText(msg, color);
            _checker = null;
        }

    }

    @SuppressFBWarnings(value = "NN_NAKED_NOTIFY", justification="rampDone is called by ramp thread to clear Engineer waiting for it to finish")
    private void rampDone(boolean stop, String speedType, int endBlockIdx) {
        setIsRamping(false);
        if (!stop && !speedType.equals(Warrant.Stop)) {
            _speedType = speedType;
            setSpeedRatio(speedType);
            setWaitforClear(false);
            setHalt(false);
        }
        _stopPending = false;
        if (!_waitForClear && !_atHalt && !_atClear && !_holdRamp) {
            synchronized (this) {
                notifyAll();
            }
            log.debug("{}: rampDone called notify.", _warrant.getDisplayName());
            if (_currentCommand != null && _currentCommand.getCommand().equals(Command.NOOP)) {
                _idxCurrentCommand--;   // notify advances command.  Repeat wait for entry to next block
            }
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
        private int _endBlockIdx = -1;     // index of block where down ramp ends.
        private boolean stop = false;      // aborts ramping
        private boolean _rampDown = true;
        private boolean _die = false;      // kills ramp for good
        RampData rampData;

        ThrottleRamp() {
            setName("Ramp(" + _warrant.getTrainName() +")");
            _endBlockIdx = -1;
        }

        @SuppressFBWarnings(value = "NN_NAKED_NOTIFY", justification="quit is called by another thread to clear all ramp waits")
        void quit(boolean die) {
            stop = true;
            synchronized (this) {
                notifyAll();    // free waits at the ramping time intervals
            }
            if (die) { // once set to true, do not allow resetting to false
                _die = die;    // permanent shutdown, warrant running ending
                synchronized (_rampLockObject) {
                    _rampLockObject.notifyAll(); // free wait at ramp run
                }
            }
            log.debug("{}: ThrottleRamp clears _ramp waits", _warrant.getDisplayName());
        }

        void setParameters(String endSpeedType, int endBlockIdx) {
            _endSpeedType = endSpeedType;
            _endBlockIdx = endBlockIdx;
            _stopPending = endSpeedType.equals(Warrant.Stop);
        }

        boolean duplicate(String endSpeedType, int endBlockIdx) {
            return !(endBlockIdx != _endBlockIdx ||
                !endSpeedType.equals(_endSpeedType));
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
            while (!_die) {
                setIsRamping(false);
                synchronized (_rampLockObject) {
                    try {
                        _rampLockObject.wait(); // wait until notified by rampSpeedTo() calls quit()
                        setIsRamping(true);
                    } catch (InterruptedException ie) {
                        log.debug("As expected", ie);
                    }
                }
                if (_die) {
                    break;
                }
                stop = false;
                doRamp();
            }
        }

        @SuppressFBWarnings(value="SLF4J_FORMAT_SHOULD_BE_CONST", justification="False assumption")
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
            _rampDown = endSpeed <= speed;

            if (log.isDebugEnabled()) {
                log.debug("RAMP {} \"{}\" speed from {}, to {}, at block \"{}\" at Cmd#{} to Cmd#{}. timeToNextCmd= {}",
                       (_rampDown ? "DOWN" : "UP"), _endSpeedType, speed, endSpeed,
                       (_endBlockIdx>=0?_warrant.getBlockAt(_endBlockIdx).getDisplayName():"ahead"),
                       _idxCurrentCommand+1, commandIndexLimit, timeToSpeedCmd);
                       // Note: command indexes biased from 0 to 1 to match Warrant display of commands.
            }
            float scriptTrackSpeed = _speedUtil.getTrackSpeed(_normalSpeed);

            int warBlockIdx = _warrant.getCurrentOrderIndex();  // block of current train position
            int cmdBlockIdx = -1;    // block of script commnd's train position
            int cmdIdx = _idxCurrentCommand;
            while (cmdIdx >= 0) {
                ThrottleSetting cmd  = _commands.get(--cmdIdx);
                if (cmd.getCommand().hasBlockName()) {
                    OBlock blk = (OBlock)cmd.getBean();
                    int idx = _warrant.getIndexOfBlockBefore(warBlockIdx, blk);
                    if (idx >= 0) {
                        cmdBlockIdx = idx;
                    } else {
                        cmdBlockIdx = _warrant.getIndexOfBlockAfter(blk, warBlockIdx);
                    }
                    break;
                }
            }
            if (cmdBlockIdx < 0) {
                cmdBlockIdx = warBlockIdx;
           }

            synchronized (this) {
                try {
                    if (!_rampDown) {
                        // Up ramp may advance the train beyond the point where the script is interrupted.
                        // The ramp up will take time and the script may have other speed commands while
                        // ramping up. So the actual script speed may not match the endSpeed when the ramp
                        // up distance is traveled.  We must compare 'endSpeed' to 'scriptSpeed' at each
                        // step and skip scriptSpeeds to insure that endSpeed matches scriptSpeed when
                        // the ramp ends.
                        rampData = _speedUtil.getRampForSpeedChange(speed, 1.0f);
                        int timeIncrement = rampData.getRampTimeIncrement();
                        ListIterator<Float> iter = rampData.speedIterator(true);
                        speed = iter.next();   // skip repeat of current speed

                        float rampDist = 0;
                        float cmdDist = timeToSpeedCmd * scriptTrackSpeed;

                        while (!stop && iter.hasNext()) {
                            speed = iter.next();
                            float s = _speedUtil.modifySpeed(_normalSpeed, _endSpeedType);
                            if (speed > s) {
                                setSpeed(s);
                                break;
                            }
                            setSpeed(speed);

                            // during ramp down the script may have non-speed commands that should be executed.
                            if (!stop && rampDist >= cmdDist && _idxCurrentCommand < commandIndexLimit) {
                                warBlockIdx = _warrant.getCurrentOrderIndex();  // current train position
                                if (_currentCommand.getCommand().hasBlockName()) {
                                    int idx = _warrant.getIndexOfBlockBefore(warBlockIdx, (OBlock)_currentCommand.getBean());
                                    if (idx >= 0) {
                                        cmdBlockIdx = idx;
                                    }
                                }
                                if (cmdBlockIdx <= warBlockIdx) {
                                    Command cmd = _currentCommand.getCommand();
                                    if (cmd.equals(Command.SPEED)) {
                                        cmdVal = _currentCommand.getValue();
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
                                    if (_idxCurrentCommand < _commands.size() - 1) {
                                        _currentCommand = _commands.get(++_idxCurrentCommand);
                                        cmdDist = scriptTrackSpeed * _currentCommand.getTime();
                                    } else {
                                        cmdDist = 0;
                                    }
                                    rampDist = 0;
                                    advanceToCommandIndex(_idxCurrentCommand); // skip up to this command
                                }   // else Do not advance script commands of block ahead of train position
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
                        _warrant.downRampBegun(_endBlockIdx);

                        rampData = _speedUtil.getRampForSpeedChange(speed, endSpeed);
                        int timeIncrement = rampData.getRampTimeIncrement();
                        ListIterator<Float> iter = rampData.speedIterator(false);
                        speed = iter.previous();   // skip repeat of current throttle setting

                        float rampDist = 0;
                        float cmdDist = timeToSpeedCmd * scriptTrackSpeed;

                        while (!stop && iter.hasPrevious()) {
                            speed = iter.previous();
                            setSpeed(speed);

                            if (_endBlockIdx >= 0) {    // correction code for ramps that are too long or too short
                                int curIdx = _warrant.getCurrentOrderIndex();
                                if (curIdx > _endBlockIdx) {
                                    // loco overran end block.  Set end speed and leave ramp
                                    setSpeed(endSpeed);
                                    stop = true;
                                    log.warn("\"{}\" Ramp to speed \"{}\" ended due to overrun into block \"{}\". throttle {} set to {}.\"{}\"",
                                            _warrant.getTrainName(), _endSpeedType, _warrant.getBlockAt(curIdx).getDisplayName(),
                                            speed, endSpeed, _warrant.getDisplayName());
                                } else if ( curIdx < _endBlockIdx &&
                                        _endSpeedType.equals(Warrant.Stop) && Math.abs(speed - endSpeed) <.001f) {
                                    // At last speed change to set throttle was endSpeed, but train has not
                                    // reached the last block. Let loco creep to end block at current setting.
                                    if (log.isDebugEnabled()) {
                                        log.debug("Extending ramp to reach block {}. speed= {}",
                                                _warrant.getBlockAt(_endBlockIdx).getDisplayName(), speed);
                                    }
                                    int waittime = 0;
                                    float throttleIncrement = _speedUtil.getRampThrottleIncrement();
                                    while (_endBlockIdx > _warrant.getCurrentOrderIndex()
                                        && waittime <= 60*timeIncrement && getSpeedSetting() > 0) {
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
                                warBlockIdx = _warrant.getCurrentOrderIndex();  // current train position
                                if (_currentCommand.getCommand().hasBlockName()) {
                                    int idx = _warrant.getIndexOfBlockBefore(warBlockIdx, (OBlock)_currentCommand.getBean());
                                    if (idx >= 0) {
                                        cmdBlockIdx = idx;
                                    }
                                }
                                if (cmdBlockIdx <= warBlockIdx) {
                                    Command cmd = _currentCommand.getCommand();
                                    if (cmd.equals(Command.SPEED)) {
                                        cmdVal = _currentCommand.getValue();
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
                                    if (_idxCurrentCommand < _commands.size() - 1) {
                                        _currentCommand = _commands.get(++_idxCurrentCommand);
                                        cmdDist = scriptTrackSpeed * _currentCommand.getTime();
                                    } else {
                                        cmdDist = 0;
                                    }
                                    rampDist = 0;
                                    advanceToCommandIndex(_idxCurrentCommand); // skip up to this command
                                }   // else Do not advance script commands of block ahead of train position
                            }

                            try {
                                wait(timeIncrement);
                            } catch (InterruptedException ie) {
                                stop = true;
                            }

                            rampDist += _speedUtil.getDistanceTraveled(speed, _speedType, timeIncrement);   // _speedType or Warrant.Normal??
                            //rampDist += getTrackSpeed(speed) * timeIncrement;
                       }

                        // Ramp done, still in endBlock. Execute any remaining non-speed commands.
                       if (_endBlockIdx >= 0 && commandIndexLimit < _commands.size()) {
                            long cmdStart = System.currentTimeMillis();
                            while (_idxCurrentCommand < commandIndexLimit) {
                                NamedBean bean = _currentCommand.getBean();
                                if (bean instanceof OBlock) {
                                    if (_endBlockIdx < _warrant.getIndexOfBlockAfter((OBlock)bean, _endBlockIdx)) {
                                        // script is past end point, command should be NOOP.
                                        // regardless, don't execute any more commands.
                                        break;
                                    }
                                }
                                Command cmd = _currentCommand.getCommand();
                                if (cmd.equals(Command.SPEED)) {
                                    cmdVal = _currentCommand.getValue();
                                    _normalSpeed = cmdVal.getFloat();
                                    if (log.isDebugEnabled()) {
                                        log.debug("Cmd #{} for speed {} skipped. warrant {}",
                                                _idxCurrentCommand+1, _normalSpeed, _warrant.getDisplayName());
                                    }
                                } else {
                                    executeComand(_currentCommand, System.currentTimeMillis() - cmdStart);
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
            rampDone(stop, _endSpeedType, _endBlockIdx);
            if (!stop) {
                _warrant.fireRunStatus("RampDone", _halt, _endSpeedType);   // normal completion of ramp
                if (Warrant._trace || log.isDebugEnabled()) {
                    log.info(Bundle.getMessage("RampSpeed", _warrant.getTrainName(),
                        _endSpeedType, _warrant.getCurrentBlockName()));
                }
            } else {
                if (Warrant._trace || log.isDebugEnabled()) {
                    log.info(Bundle.getMessage("RampSpeed", _warrant.getTrainName(),
                            _endSpeedType, _warrant.getCurrentBlockName()) + "-Interrupted!");
                }

            }
            stop = false;

            if (_rampDown) {    // check for overrun status last
                _warrant.downRampDone(stop, _halt, _endSpeedType, _endBlockIdx);
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Engineer.class);

}
