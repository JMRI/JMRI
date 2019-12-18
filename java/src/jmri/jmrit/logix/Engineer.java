package jmri.jmrit.logix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantLock;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SpeedStepMode;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute a throttle command script for a warrant.
 * <p>
 * This generally operates on its own thread, but switches back to the Layout
 * thread when asking the Warrant to perform actions.
 *
 * @author Pete Cressman Copyright (C) 2009, 2010, 2011
 */
/*
 * ************************ Thread running the train ****************
 */
public class Engineer extends Thread implements java.beans.PropertyChangeListener {

    private int _idxCurrentCommand;     // current throttle command
    private String _currentCommand;
    private int _idxSkipToSpeedCommand;   // skip to this index to reset script when ramping
    private float _normalSpeed = 0;       // current commanded throttle setting (unmodified)
    private String _speedType = Warrant.Normal;    // current speed name
    private long et;    // actual elapsed time while waiting to do current command
    private float _timeRatio = 1.0f;     // ratio to extend scripted time when speed is modified
    private boolean _abort = false;
    private boolean _halt = false;  // halt/resume from user's control
    private boolean _stopPending = false;   // ramp slow down in progress
    private boolean _resumePending = false;   // ramp up to clear flags in progress
    private boolean _waitForClear = false;  // waits for signals/occupancy/allocation to clear
    private boolean _waitForSync = false;  // waits for train to catch up to commands
    private boolean _waitForSensor = false; // wait for sensor event
    private boolean _runOnET = false;   // Execute commands on ET only - do not synch
    private boolean _setRunOnET = false; // Need to delay _runOnET from the block that set it
    private int _syncIdx;           // block order index of current command
    protected DccThrottle _throttle;
    private final Warrant _warrant;
    private final List<ThrottleSetting> _commands;
    private Sensor _waitSensor;
    private int _sensorWaitState;
    private ThrottleRamp _ramp;
    final ReentrantLock _lock = new ReentrantLock(true);    // Ramp needs to block script speeds
    private boolean _atHalt = false;
    private boolean _atClear = false;
    private final SpeedUtil _speedUtil;

    Engineer(Warrant warrant, DccThrottle throttle) {
        _warrant = warrant;
        _throttle = throttle;
        _speedUtil = warrant.getSpeedUtil();
        _commands = _warrant.getThrottleCommands();
        _idxCurrentCommand = 0;
        _idxSkipToSpeedCommand = 0;
        _syncIdx = -1;
        _waitForSensor = false;
        setName("Engineer(" + _warrant.getTrainName() +")");
    }

    int cmdBlockIdx = 0;

    @Override
    @SuppressFBWarnings(value="UW_UNCOND_WAIT", justification="waits may be indefinite until satisfied or thread aborted")
    public void run() {
        if (log.isDebugEnabled()) 
            log.debug("Engineer started warrant {} _throttle= {}", _warrant.getDisplayName(), _throttle.getClass().getName());

        cmdBlockIdx = 0;
        while (_idxCurrentCommand < _commands.size()) {
            while (_idxSkipToSpeedCommand > _idxCurrentCommand) {
                if (log.isDebugEnabled()) {
                    ThrottleSetting ts = _commands.get(_idxCurrentCommand);
                    log.debug("Skip Cmd #{}: {} Warrant {}", _idxCurrentCommand+1, ts.toString(), _warrant.getDisplayName());
                    // Note: command indexes biased from 0 to 1 to match Warrant display of commands.
                }
                _idxCurrentCommand++;
            }
            et = System.currentTimeMillis();
            ThrottleSetting ts = _commands.get(_idxCurrentCommand);
            long cmdWaitTime = ts.getTime();    // time to wait before executing command
            _currentCommand = ts.getCommand().toUpperCase();
            _runOnET = _setRunOnET;     // OK to set here
            if (!"SET SENSOR".equals(_currentCommand) && !"WAIT SENSOR".equals(_currentCommand) &&
                    !"RUN WARRANT".equals(_currentCommand)) {
                int idx = _warrant.getIndexOfBlock(ts.getBeanDisplayName(), cmdBlockIdx);
                if (idx >= 0) {
                    cmdBlockIdx = idx;
                }
            }
            if (cmdBlockIdx < _warrant.getCurrentOrderIndex() || 
                    (_currentCommand.equals("NOOP") && (cmdBlockIdx <= _warrant.getCurrentOrderIndex()))) {
                // Train advancing too fast, need to process commands more quickly,
                // allow some time for whistle toots etc.
                cmdWaitTime = Math.min(cmdWaitTime, 200); // 200ms per command should be enough for toots etc.
                if (log.isDebugEnabled())
                    log.debug("Train reached block \"{}\" before script et={}ms . Warrant {}",
                            _warrant.getCurrentBlockName(), ts.getTime(), _warrant.getDisplayName());
            }
            if (_abort) {
                break;
            }
            if (log.isDebugEnabled()) 
                log.debug("Start Cmd #{} for block \"{}\" currently in \"{}\". wait {}ms to do cmd {}. Warrant {}",
                    _idxCurrentCommand+1, ts.getBeanDisplayName(), _warrant.getCurrentBlockName(), 
                    cmdWaitTime, _currentCommand, _warrant.getDisplayName());
                    // Note: command indexes biased from 0 to 1 to match Warrant display of commands.
            synchronized (this) {
                if (!Warrant.Normal.equals(_speedType)) {
                    cmdWaitTime = (long)(cmdWaitTime*_timeRatio); // extend et when speed has been modified from scripted speed
                }                
                try {
                    if (cmdWaitTime > 0) {
                        wait(cmdWaitTime);
                    }
                    if (_abort) {
                        break;
                    }
                } catch (InterruptedException ie) {
                    log.error("At time wait {}", ie.toString());
                    _warrant.debugInfo();
                    Thread.currentThread().interrupt();
                } catch (java.lang.IllegalArgumentException iae) {
                    log.error("At time wait {}", iae.toString());
                }
            }

            _syncIdx = cmdBlockIdx;
            // Having waited, time=ts.getTime(), so blocks should agree.  if not,
            // wait for train to arrive at block and send sync notification.
            // note, blind runs cannot detect entrance.
            if (!_runOnET && _syncIdx > _warrant.getCurrentOrderIndex()) {
                // commands are ahead of current train position
                // When the next block goes active or a control command is made, a clear sync call
                // will test these indexes again and can trigger a notify() to free the wait
                synchronized (this) {
                    try {
                        _waitForSync = true;
                        if (log.isDebugEnabled()) 
                            log.debug("Wait for train to enter \"{}\". Warrant {}",
                                _warrant.getBlockAt(_syncIdx).getDisplayName(), _warrant.getDisplayName());
                        _warrant.fireRunStatus("WaitForSync", _idxCurrentCommand - 1, _idxCurrentCommand);
                        wait();
                    } catch (InterruptedException ie) {
                        log.error("At _waitForSync {}", ie.toString());
                        _warrant.debugInfo();
                        Thread.currentThread().interrupt();
                    }
                    finally {
                        _waitForSync = false;
                    }
                }
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
                            log.debug("Waiting for clearance. _waitForClear= {} _halt= {} \"{}\".  Warrant {}",
                                _waitForClear, _halt, _warrant.getBlockAt(cmdBlockIdx).getDisplayName(), _warrant.getDisplayName());
                        wait();
                    } catch (InterruptedException ie) {
                        log.error("At _atClear {}" + ie.toString());
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
                            log.debug("Waiting to Resume. _halt= {}, _waitForClear= {}, Block \"{}\".  Warrant {}",
                                _halt, _waitForClear, _warrant.getBlockAt(cmdBlockIdx).getDisplayName(), _warrant.getDisplayName());
                        wait();
                    } catch (InterruptedException ie) {
                        log.error("At _atHalt {}", ie.toString());
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
            if (_currentCommand.equals("SPEED")) {
                synchronized (this) {
                    if (_ramp != null && !_ramp.ready) {
                        try {
                            if (log.isDebugEnabled()) 
                                log.debug("Waiting for ramp to finish.  Warrant {}", _warrant.getDisplayName());
                            wait();
                        } catch (InterruptedException ie) {
                            _warrant.debugInfo();
                            Thread.currentThread().interrupt();
                        }
                        finally {
                        }
                    }
                    if (_idxCurrentCommand >= _idxSkipToSpeedCommand) {
                        try {
                            _lock.lock();
                            float throttle = Float.parseFloat(ts.getValue());
                            /* attempt to use dynamic speed measuring - too many variables
                            // If recording speed is known, get throttle setting for that speed
                            float speed = ts.getSpeed();
                            if (speed > 0.0f) {
                                speed = _speedUtil.getThrottleSetting(speed);
                                if (speed > 0.0f) {
                                    throttle = speed;
                                }
                            }*/
                            _normalSpeed = throttle;
                            float speedMod = _speedUtil.modifySpeed(throttle, _speedType);
                            if (Math.abs(throttle - speedMod) > .0001f) {
                                _timeRatio = throttle / speedMod;
                            } else {
                                _timeRatio = 1.0f;
                            }
                            setSpeed(speedMod);                                
                        } finally {
                            _lock.unlock();                                
                        }
                    }
                }
            } else {    // let non-speed commands go before wait
                try {
                    if (_currentCommand.equals("SPEEDSTEP")) {
                        SpeedStepMode mode = SpeedStepMode.getByName(ts.getValue());
                        _throttle.setSpeedStepMode(mode);
                    } else if (_currentCommand.equals("FORWARD")) {
                        boolean isForward = Boolean.parseBoolean(ts.getValue());
                        _throttle.setIsForward(isForward);
                        _speedUtil.setIsForward(isForward);
                    } else if (_currentCommand.startsWith("F")) {
                        int cmdNum = Integer.parseInt(_currentCommand.substring(1));
                        boolean isTrue = Boolean.parseBoolean(ts.getValue());
                        setFunction(cmdNum, isTrue);
                    } else if (_currentCommand.startsWith("LOCKF")) {
                        int cmdNum = Integer.parseInt(_currentCommand.substring(5));
                        boolean isTrue = Boolean.parseBoolean(ts.getValue());
                        setLockFunction(cmdNum, isTrue);
                    } else if (_currentCommand.equals("SET SENSOR")) {
                        setSensor(ts.getBeanSystemName(), ts.getValue());
                    } else if (_currentCommand.equals("WAIT SENSOR")) {
                        getSensor(ts.getBeanSystemName(), ts.getValue());
                    } else if (_currentCommand.equals("START TRACKER")) {
                        ThreadingUtil.runOnLayout(() -> {
                            _warrant.startTracker();
                        });
                    } else if (_currentCommand.equals("RUN WARRANT")) {
                        runWarrant(ts);
                    } else if (_runOnET && _currentCommand.equals("NOOP")) {    // let warrant know engineer expects entry into dark block
                        ThreadingUtil.runOnLayout(() -> {
                            _warrant.goingActive(_warrant.getBlockAt(cmdBlockIdx));
                        });
                    }
                } catch (NumberFormatException nfe) {
                    log.error("Command failed! {} {}", ts.toString(), nfe.toString());
                }
                
            }
            et = System.currentTimeMillis() - et;
            _idxCurrentCommand++;
            if (log.isDebugEnabled()) 
                log.debug("Cmd #{} done. et={}. {} warrant {}", _idxCurrentCommand, et, ts.toString(), _warrant.getDisplayName());

        }
        // shut down
        setSpeed(0.0f); // for safety to be sure train stops                               
        _warrant.stopWarrant(_abort);
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
            log.debug("advanceToCommandIndex to {} - {}", _idxSkipToSpeedCommand+1, _commands.get(idx).toString());
            // Note: command indexes biased from 0 to 1 to match Warrant display of commands, which are 1-based.
    }

    /**
     * Cannot set _runOnET to true until current NOOP command completes
     * so there is the intermediate flag _setRunOnET
     * @param set true to run on elapsed time calculations only, false to
     *            consider other inputs
     */
    protected void setRunOnET(Boolean set) {
        if (log.isDebugEnabled()) 
            log.debug("setRunOnET {} command #{} warrant {}", set, _idxCurrentCommand+1, _warrant.getDisplayName());
            // Note: command indexes biased from 0 to 1 to match Warrant display of commands.
        _setRunOnET = set;
        if (!set) { // OK to be set false immediately
            _runOnET = false;
        }
    }

    protected boolean getRunOnET() {
        return _setRunOnET;
    }

    /**
     * If waiting to sync entrance to a block boundary with recorded wait time,
     * or waiting for clearance ahead for rogue occupancy, stop aspect or
     * sharing of turnouts, this call will free the wait.
     */
    synchronized protected void clearWaitForSync() {
        if (_waitForSync) {
            if (log.isDebugEnabled()) 
                log.debug("clearWaitForSync() calls notify()");
            notifyAll();   // if wait is cleared, this sets _waitForSync= false
        } else {
            ThrottleSetting ts = _commands.get(_idxCurrentCommand);
            OBlock block = _warrant.getCurrentBlockOrder().getBlock();
            // block went active. if waiting on cmdWaitTime, clear it
            if (ts.getCommand().toUpperCase().equals("NOOP") && ts.getBeanDisplayName().equals(block.getDisplayName())) {
                if (log.isDebugEnabled()) 
                    log.debug("clearWaitForSync() calls notify()");
                notifyAll();
            }
        }
    }

    /**
     * Occupancy of blocks, user halts and aspects of Portal signals will modify
     * normal scripted train speeds.
     * Ramp speed change for smooth prototypical look.
     *
     * @param endSpeedType signal aspect speed name
     * @param endBlockIdx BlockOrder index of where ramp is to end.
     * @param useIndex false if endBlockIdx should not be considered 
     */
    protected void rampSpeedTo(String endSpeedType, int endBlockIdx, boolean useIndex) {
        if (!setSpeedRatio(endSpeedType)) {
            if (!endSpeedType.equals(Warrant.Stop) && !endSpeedType.equals(Warrant.EStop)) {
                setWaitforClear(false);
            }
            return;
        }
        synchronized (this) {
            if (log.isDebugEnabled()) 
                log.debug("rampSpeedTo type= {}, throttle from {} to {}. warrant {}",
                    endSpeedType, getSpeedSetting(), 
                    _speedUtil.modifySpeed(_normalSpeed, endSpeedType), 
                    _warrant.getDisplayName());

            if (_ramp == null) {
                _ramp = new ThrottleRamp();
                _ramp.start();
            } else {
            	_ramp.quit(false);            	
            }
            long time = 0;
            int waitTime = _speedUtil.getRampTimeIncrement() + 20;
            while (time < waitTime && !_ramp.ready) {
                // may need a bit of time for quit() or start() to get ready
                try {
                    wait(20);
                    time += 20;
                }
                catch (InterruptedException ie) { // ignore
                }
            }
            if (_ramp.ready) {
                _ramp.setParameters(endSpeedType, endBlockIdx, useIndex);
                synchronized (_ramp) {
                    _ramp.notifyAll(); // free wait at ThrottleRamp.run()
                    log.debug("rampSpeedTo called notify _ramp.ready={}", _ramp.ready);
                }
            } else {
                log.error("Can't launch ramp for speed {}! _ramp Thread.State= {}. Waited {}ms",
                        endSpeedType, _ramp.getState(), time-20);
                _warrant.debugInfo();
            }
        }
    }

    private void cancelRamp(boolean die) {
        if (_ramp != null && !_ramp.ready) {
            _ramp.quit(die);
        }
    }

    @SuppressFBWarnings(value= "IS2_INCONSISTENT_SYNC", justification="display of _speedType for viewing only")
    private void rampDone(boolean stop, String type) {
        // ignore "IS2_INCONSISTENT_SYNC" warning here
        if (log.isDebugEnabled())
            log.debug("ThrottleRamp done: {} for \"{}\" at speed= {}. _normalScript={}, Thread.State= {} resume index= {}, current Index= {} on warrant {}",
                    (stop?"stopped":"completed"), type, getSpeedSetting(), _normalSpeed, (_ramp != null?_ramp.getState():"_ramp is null!"), 
                    _idxSkipToSpeedCommand+1, _idxCurrentCommand+1, _warrant.getDisplayName());
                    // Note: command indexes biased from 0 to 1 to match Warrant display of commands.
        if (!stop) {
            _warrant.fireRunStatus("RampDone", _halt, type);
         }
        if (!_atHalt && !_atClear) {
            synchronized (this) {
                notifyAll();  // let engineer run script
                log.debug("rampDone called notify");
            }
            if (_currentCommand.equals("NOOP")) {
                _idxCurrentCommand--;   // notify advances command.  Repeat wait for entry to next block
            }
        }
    }

    /**
     * do throttle setting
     * @param s throttle setting
     */
    @SuppressFBWarnings(value="IS2_INCONSISTENT_SYNC", justification="display of _speedType on GUI for viewing only")
     protected void setSpeed(float s) {
        float speed = s;
        // Whether, runOnLayoutEventually, runOnLayout, or no thread change used, sometimes when multiple ramps need to be used,
        // throttle seems to become unresponsive - i.e. speed settings not made even though this thread runs as scheduled.
//        ThreadingUtil.runOnLayoutEventually(() -> { // invoke later. CAN GET WAY OUT OF SYNC!! and although logged, engine speed not changed.
//        jmri.util.ThreadingUtil.runOnLayout(() -> { // move to layout-handling thread.  NO! CAN HANG GUI! Then must kill Java process.
              _speedUtil.speedChange();   // call before changing throttle setting
              _throttle.setSpeedSetting(speed);       // CAN MISS SETTING SPEED! (as done when runOnLayoutEventually used) ??
/*              if (log.isDebugEnabled()) 
                  log.debug("On Layout thread, _throttle.setSpeedSetting({}) called, _speedType={}.  warrant {}",
                          speed, _speedType, _warrant.getDisplayName()); */
//        });
        // Late update to GUI is OK, this is just an informational status display
        _warrant.fireRunStatus("SpeedChange", null, _speedType);
        if (log.isDebugEnabled()) 
            log.debug("_throttle.setSpeedSetting({}) called, ({}).  warrant {}",
                    speed, _speedType, _warrant.getDisplayName());
    }

    protected float getSpeedSetting() {
        return _throttle.getSpeedSetting();
    }

    protected float getScriptSpeed() {
        return _normalSpeed;
    }
    /**
     * Utility for unscripted speed changes.
     * Records current type and sets time ratio.
     * @param speedType name of speed change type
     * @return true to continue, false to skip setting a speed
     */
    private boolean setSpeedRatio(String speedType) {
        float newSpeed = _speedUtil.modifySpeed(_normalSpeed, speedType);
        if (log.isTraceEnabled()) {
            float scriptSpeed = _speedUtil.modifySpeed(_normalSpeed, _speedType);
            log.debug("setSpeedRatio: \"{}\" speed setting= {}, calculated current speed = {},  newSpeed= {}. - {}",
                    speedType, getSpeedSetting(), scriptSpeed, newSpeed, _warrant.getDisplayName());
        }

        if (!speedType.equals(Warrant.Stop) && !speedType.equals(Warrant.EStop)) {
            _speedType = speedType;     // set type regardless of return
            synchronized (this) {
                float speedMod = _speedUtil.modifySpeed(1.0f, _speedType);
                if (Math.abs(1.0f - speedMod) > .0001f) {
                    _timeRatio = 1.0f / speedMod;
                } else {
                    _timeRatio = 1.0f;
                }
            }
        }
        if (Math.abs(getSpeedSetting() - newSpeed) < .002) {
        	setHalt(false);
            return false;
        }
        return true;
    }

    /*
     * Do immediate speed change.
     */
    protected void setSpeedToType(String speedType) {
        cancelRamp(false);
        if (speedType.equals(Warrant.EStop)) {
            setSpeed(-0.1f);        // always do immediate EStop
            _waitForClear = true;
            advanceToCommandIndex(_idxCurrentCommand + 1);  // skip current command
        } else if (speedType.equals(Warrant.Stop)) {
            setSpeed(0.0f);
            _waitForClear = true;
            advanceToCommandIndex(_idxCurrentCommand + 1);  // skip current command
        } else {
            if (setSpeedRatio(speedType)) {
                setSpeed(_speedUtil.modifySpeed(_normalSpeed, speedType));
            }
        }
        if (log.isDebugEnabled()) 
            log.debug("setSpeedToType({}) scriptSpeed= {}", speedType, _normalSpeed);
    }

    /**
     * Command to stop (or resume speed) of train from Warrant.controlRunTrain()
     * of user's override of throttle script.  Also from error conditions
     * such as losing detection of train's location.
     * @param halt true if train should halt
     */
    synchronized public void setHalt(boolean halt) {
        if (log.isDebugEnabled()) 
            log.debug("setHalt({}): _atHalt= {}, _waitForClear= {}, _waitForSync= {}, warrant {}",
                halt, _atHalt, _waitForClear, _waitForSync, _warrant.getDisplayName());
        if (!halt) {    // resume normal running
            _halt = false;
            if (_atHalt) {
                if (log.isDebugEnabled()) 
                    log.debug("setHalt calls notify()");
                notifyAll();   // free wait at _atHalt
            }
        } else {
            _halt = true;
        }
    }

    /**
     * Command to stop or smoothly resume speed. Stop due to
     * signal or occupation stopping condition ahead.  Caller
     * follows with call for type of stop to make.
     * Track condition override of throttle script.
     * @param stop true if train should stop
     */
    synchronized protected void setWaitforClear(boolean stop) {
        if (log.isDebugEnabled()) 
            log.debug("setWaitforClear({}): _atClear= {}, throttle speed= {}, _halt= {}, _waitForSync= {}, warrant {}",
                stop, _atClear,  _throttle.getSpeedSetting(), _halt, _waitForSync, _warrant.getDisplayName());
        if (!stop) {    // resume normal running
            _waitForClear = false;
            if (_atClear) {
                if (log.isDebugEnabled()) 
                    log.debug("setWaitforClear calls notify");
                notifyAll();   // free wait at _atClear
            }
        } else {
            _waitForClear = true;
        }
    }

    String getFlags() {
        StringBuffer buf = new StringBuffer("Engineer flags: _waitForClear= ");
        buf.append(_waitForClear);
        buf.append(", _atclear= "); buf.append(_atClear);
        buf.append(", _halt= "); buf.append(_halt);
        buf.append(", _atHalt= "); buf.append(_atHalt);
        buf.append(", _waitForSync= "); buf.append(_waitForSync);
        return buf.toString();
    }

    ThrottleRamp getRamp() {
        return _ramp;
    }
    /**
     * Immediate stop command from Warrant.controlRunTrain()-user
     * or from Warrant.goingInactive()-train lost
     * or from setMovement()-overrun, possible collision risk.
     * Do not ramp.
     * @param eStop true for emergency stop
     * @param setHalt for user restart needed, otherwise some kind of clear
     */
    synchronized public void setStop(boolean eStop, boolean setHalt) {
        cancelRamp(false);
        if (setHalt) {
            _halt = true;
        } else {
            _waitForClear = true;
        }
        if (eStop) {
            setSpeed(-0.1f);
        } else {
            setSpeed(0.0f);
        }
    }

    synchronized public int getRunState() {
        if (_stopPending) {
            if (_halt) {
                return Warrant.RAMP_HALT;
            }
            return Warrant.STOP_PENDING;
        } else if (_resumePending) {
            return Warrant.RAMPING_UP;            
        } else if (_waitForClear) {
            return Warrant.WAIT_FOR_CLEAR;
        } else if (_halt) {
            return Warrant.HALT;
        } else if (_abort) {
            return Warrant.ABORT;
        } else if (_waitForSync) {
            return Warrant.WAIT_FOR_TRAIN;
        } else if (_waitForSensor) {
            return Warrant.WAIT_FOR_SENSOR;
        } else if (!_speedType.equals(Warrant.Normal)) {
            return Warrant.SPEED_RESTRICTED;
        } else if (_idxCurrentCommand < 0) {
            return Warrant.STOP;
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
                _throttle.setSpeedSetting(-1.0f);
                setSpeed(0.0f);     // prevent creep after EStop - according to Jim Betz
            }
            if (abort && turnOffFunctions) {
                _throttle.setF0(false);
                _throttle.setF1(false);
                _throttle.setF2(false);
                _throttle.setF3(false);
            }
            _warrant.releaseThrottle(_throttle);
        }
    }

    private void setFunction(int cmdNum, boolean isSet) {
        switch (cmdNum) {
            case 0:
                _throttle.setF0(isSet);
                break;
            case 1:
                _throttle.setF1(isSet);
                break;
            case 2:
                _throttle.setF2(isSet);
                break;
            case 3:
                _throttle.setF3(isSet);
                break;
            case 4:
                _throttle.setF4(isSet);
                break;
            case 5:
                _throttle.setF5(isSet);
                break;
            case 6:
                _throttle.setF6(isSet);
                break;
            case 7:
                _throttle.setF7(isSet);
                break;
            case 8:
                _throttle.setF8(isSet);
                break;
            case 9:
                _throttle.setF9(isSet);
                break;
            case 10:
                _throttle.setF10(isSet);
                break;
            case 11:
                _throttle.setF11(isSet);
                break;
            case 12:
                _throttle.setF12(isSet);
                break;
            case 13:
                _throttle.setF13(isSet);
                break;
            case 14:
                _throttle.setF14(isSet);
                break;
            case 15:
                _throttle.setF15(isSet);
                break;
            case 16:
                _throttle.setF16(isSet);
                break;
            case 17:
                _throttle.setF17(isSet);
                break;
            case 18:
                _throttle.setF18(isSet);
                break;
            case 19:
                _throttle.setF19(isSet);
                break;
            case 20:
                _throttle.setF20(isSet);
                break;
            case 21:
                _throttle.setF21(isSet);
                break;
            case 22:
                _throttle.setF22(isSet);
                break;
            case 23:
                _throttle.setF23(isSet);
                break;
            case 24:
                _throttle.setF24(isSet);
                break;
            case 25:
                _throttle.setF25(isSet);
                break;
            case 26:
                _throttle.setF26(isSet);
                break;
            case 27:
                _throttle.setF27(isSet);
                break;
            case 28:
                _throttle.setF28(isSet);
                break;
            default:
                log.error("Function value " + cmdNum + " out of range");
                throw new java.lang.IllegalArgumentException("Function Value " + cmdNum + " out of range");
        }
    }

    private void setLockFunction(int cmdNum, boolean isTrue) {
        switch (cmdNum) {
            case 0:
                _throttle.setF0Momentary(!isTrue);
                break;
            case 1:
                _throttle.setF1Momentary(!isTrue);
                break;
            case 2:
                _throttle.setF2Momentary(!isTrue);
                break;
            case 3:
                _throttle.setF3Momentary(!isTrue);
                break;
            case 4:
                _throttle.setF4Momentary(!isTrue);
                break;
            case 5:
                _throttle.setF5Momentary(!isTrue);
                break;
            case 6:
                _throttle.setF6Momentary(!isTrue);
                break;
            case 7:
                _throttle.setF7Momentary(!isTrue);
                break;
            case 8:
                _throttle.setF8Momentary(!isTrue);
                break;
            case 9:
                _throttle.setF9Momentary(!isTrue);
                break;
            case 10:
                _throttle.setF10Momentary(!isTrue);
                break;
            case 11:
                _throttle.setF11Momentary(!isTrue);
                break;
            case 12:
                _throttle.setF12Momentary(!isTrue);
                break;
            case 13:
                _throttle.setF13Momentary(!isTrue);
                break;
            case 14:
                _throttle.setF14Momentary(!isTrue);
                break;
            case 15:
                _throttle.setF15Momentary(!isTrue);
                break;
            case 16:
                _throttle.setF16Momentary(!isTrue);
                break;
            case 17:
                _throttle.setF17Momentary(!isTrue);
                break;
            case 18:
                _throttle.setF18Momentary(!isTrue);
                break;
            case 19:
                _throttle.setF19Momentary(!isTrue);
                break;
            case 20:
                _throttle.setF20Momentary(!isTrue);
                break;
            case 21:
                _throttle.setF21Momentary(!isTrue);
                break;
            case 22:
                _throttle.setF22Momentary(!isTrue);
                break;
            case 23:
                _throttle.setF23Momentary(!isTrue);
                break;
            case 24:
                _throttle.setF24Momentary(!isTrue);
                break;
            case 25:
                _throttle.setF25Momentary(!isTrue);
                break;
            case 26:
                _throttle.setF26Momentary(!isTrue);
                break;
            case 27:
                _throttle.setF27Momentary(!isTrue);
                break;
            case 28:
                _throttle.setF28Momentary(!isTrue);
                break;
            default:
                log.error("Function value " + cmdNum + " out of range");
                throw new java.lang.IllegalArgumentException("Function Value " + cmdNum + " out of range");
        }
    }

    /**
     * Set Sensor state
     */
    private void setSensor(String sensorName, String act) {
        String action = act.toUpperCase();
        jmri.Sensor s = InstanceManager.sensorManagerInstance().getSensor(sensorName);
        if (s != null) {
            try {
                if ("ACTIVE".equals(action)) {
                    s.setKnownState(jmri.Sensor.ACTIVE);
                } else if ("INACTIVE".equals(action)) {
                    s.setKnownState(jmri.Sensor.INACTIVE);
                }
                _warrant.fireRunStatus("SensorSetCommand", act, s.getDisplayName());
            } catch (jmri.JmriException e) {
                log.warn("Exception setting sensor " + sensorName + " in action");
            }
        } else {
            log.warn("Sensor " + sensorName + " not found.");
        }
    }

    /**
     * Wait for Sensor state event
     */
    private void getSensor(String sensorName, String act) {
        String action = act.toUpperCase();
        if (_waitSensor != null) {
            _waitSensor.removePropertyChangeListener(this);
        }
        _waitSensor = InstanceManager.sensorManagerInstance().getSensor(sensorName);
        if (_waitSensor != null) {
            if ("ACTIVE".equals(action)) {
                _sensorWaitState = Sensor.ACTIVE;
            } else if ("INACTIVE".equals(action)) {
                _sensorWaitState = Sensor.INACTIVE;
            } else {
                log.error("Bad Sensor command \"" + action + "\"+ for sensor " + sensorName);
                return;
            }
            int state = _waitSensor.getKnownState();
            if (state == _sensorWaitState) {
                log.info("Engineer: state of event sensor " + sensorName + " already at state " + action);
                return;
            }
            _waitSensor.addPropertyChangeListener(this);
            if (log.isDebugEnabled()) 
                log.debug("Listen for propertyChange of {}, wait for State= {}", _waitSensor.getDisplayName(), _sensorWaitState);
            // suspend commands until sensor changes state
            synchronized (this) {
                _waitForSensor = true;
                while (_waitForSensor) {
                    try {
                        _warrant.fireRunStatus("SensorWaitCommand", act, _waitSensor.getDisplayName());
                        wait();
                        String name =  _waitSensor.getDisplayName();    // save name, _waitSensor will be null 'eventually' 
                        _warrant.fireRunStatus("SensorWaitCommand", null, name);
                    } catch (InterruptedException ie) {
                        log.error("Engineer interrupted at _waitForSensor " + ie);
                        _warrant.debugInfo();
                        Thread.currentThread().interrupt();
                    } finally {
                        clearSensor();
                    }
                }
            }
        } else {
            log.warn("Sensor " + sensorName + " not found.");
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
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NN_NAKED_NOTIFY", justification="Notify passing event, not state")
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (log.isDebugEnabled()) 
            log.debug("propertyChange {} new value= {}", evt.getPropertyName(), evt.getNewValue());
        if ((evt.getPropertyName().equals("KnownState")
                && ((Number) evt.getNewValue()).intValue() == _sensorWaitState)) {
            synchronized (this) {
//                if (!_halt && !_waitForClear) {
                    this.notifyAll();  // free sensor wait
//                }
            }
        }
    }


    private void runWarrant(ThrottleSetting ts) {
        NamedBean bean = ts.getNamedBeanHandle().getBean();
        if (!(bean instanceof Warrant)) {
            log.error("runWarrant: {} not a warrant!", bean.getDisplayName());
            return;
        }
        Warrant warrant =  (Warrant)bean;
        String msg = null;
        int num = 0;
        try {
            num = Integer.parseInt(ts.getValue());
        } catch (NumberFormatException nfe) {
            msg = Bundle.getMessage("InvalidNumber", ts.getValue());
        }
        if (num > 0) {
            num--;
        }
        ts.setValue(Integer.toString(num));
        java.awt.Color color = java.awt.Color.red;

        if (msg == null) {
            if (_warrant.getSpeedUtil().getDccAddress().equals(warrant.getSpeedUtil().getDccAddress())) {
                cmdBlockIdx = 0;    // reset block command number  
                Thread checker = new CheckForTermination(_warrant, warrant, num);
                checker.start();
                if (log.isDebugEnabled()) log.debug("Exit runWarrant");
                return;
            } else {
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
        } else {
            msg = Bundle.getMessage("CannotRun", warrant.getDisplayName(), msg);
        }
        final String m = msg;
        java.awt.Color c = color;
        ThreadingUtil.runOnLayout(()->{
            WarrantTableFrame.getDefault().setStatusText(m, c, true);
        });
        if (log.isDebugEnabled()) log.debug("Exit runWarrant - " + msg);
    }

    static private class CheckForTermination extends Thread {

        Warrant oldWarrant;
        Warrant newWarrant;
        int num;

        CheckForTermination(Warrant oldWar, Warrant newWar, int n) {
            oldWarrant = oldWar;
            newWarrant = newWar;
            num = n;
            if (log.isDebugEnabled()) log.debug("checkForTermination({}, {}, {})",
                    oldWarrant.getDisplayName(), newWarrant.getDisplayName(), num);
         }

        @Override
        @SuppressFBWarnings(value = "UW_UNCOND_WAIT", justification="false postive, guarded by while statement")
        public void run() {
            OBlock endBlock = oldWarrant.getLastOrder().getBlock();
            long time = 0;
            String msg = null;
            try {
                while (time < 10000) {
                    if (oldWarrant.getRunMode() == Warrant.MODE_NONE) {
                        break;
                    }
                    synchronized (this) {
                        wait(200);
                        time += 200;
                    }
                }
                if (time >= 10000) {
                    msg = Bundle.getMessage("cannotLaunch",
                            newWarrant.getDisplayName(), oldWarrant.getDisplayName(), endBlock.getDisplayName());
                }
            } catch (InterruptedException ie) {
                log.warn("Warrant \"{}\" InterruptedException message= \"{}\" time= {}",
                        oldWarrant.getDisplayName(), ie.toString(), time);
                Thread.currentThread().interrupt();
            }
            if (log.isDebugEnabled()) log.debug("CheckForTermination waited {}ms. runMode={} ", time, oldWarrant.getRunMode());

            java.awt.Color color = java.awt.Color.red;
            msg = newWarrant.setRoute(false, null);
            if (msg == null) {
                msg = newWarrant.setRunMode(Warrant.MODE_RUN, null, null, null, false);
            }
            if (msg != null) {
                msg = Bundle.getMessage("CannotRun", newWarrant.getDisplayName(), msg);
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
            ThreadingUtil.runOnLayoutEventually(() -> { // delay until current warrant can complete
                WarrantTableFrame.getDefault().setStatusText(m, c, true);
            });
        }
    }

    /*
     * *************************************************************************************
     */
     class ThrottleRamp extends Thread {

         private RampData _rampData;
         private String _endSpeedType;
         private int _endBlockIdx;   // index of block where down ramp ends - not used for up ramps.
         private boolean _useIndex;
         private boolean stop = false;   // aborts ramping
         boolean ready = false;   // ready for call doRamp
         private boolean _die = false;    // kills ramp for good

         ThrottleRamp() {
            setName("Ramp(" + _warrant.getTrainName() +")");
         }

         void quit(boolean die) {
             log.debug("ThrottleRamp.quit die={})", die);
             stop = true;
             if (die) { // once set to true, do not allow resetting to false
                 _die = die;
             }
             synchronized (_ramp) {
                 log.debug("ThrottleRamp.quit calls notify)");
                 _ramp.notifyAll(); // free waits at ramp time interval
             }
         }

        void setParameters(String endSpeedType, int endBlockIdx, boolean useIndex) {
            _endSpeedType = endSpeedType;
            _endBlockIdx = endBlockIdx;
            _useIndex = useIndex;
            _stopPending = endSpeedType.equals(Warrant.Stop);                    
        }

        RampData getRampData () {
            return _rampData;
        }

        @Override
        @SuppressFBWarnings(value="UW_UNCOND_WAIT", justification="waits may be indefinite until satisfied or thread aborted")
        public void run() {
            ready = true;
            while (!_die) {
                synchronized (this) {
                    try {
                        wait(); // wait until notified by rampSpeedTo() calls quit()
                    } catch (InterruptedException ie) {
                        log.debug("As expected {}", ie.toString());
                    }
                }
                doRamp();
            }
        }

        public void doRamp() {
            // the time 'right now' is at having done _idxCurrentCommand-1 and is waiting
            // to do the _idxCurrentCommand.  A non-scripted speed change is to begin now.
            // current speed at _idxCurrentCommand is (should be) _normalSpeed modified by _speedType
            // Note on ramp down the _normalSpeed value may be modified. 
            // "idxSkipToSpeedCommand" may be used rather than "_idxCurrentCommand".
            // Note on ramp up endSpeed should match scripted speed modified by endSpeedType
            ready = false;
            stop = false;
            float endSpeed = _speedUtil.modifySpeed(_normalSpeed, _endSpeedType);   // requested end speed
            float speed = _throttle.getSpeedSetting();  // current speed setting
            if (speed < 0.0f) {
                speed = 0.0f;
            }
            _rampData = _speedUtil.getRampForSpeedChange(speed, endSpeed);
            int timeIncrement = _rampData.getRampTimeIncrement();

            synchronized (this) {
                try {
                     _lock.lock();
                     if (log.isDebugEnabled()) 
                         log.debug("ThrottleRamp for \"{}\". Ramp {} to {}. normalSpeed= {}. on warrant {}",
                            _endSpeedType, speed, endSpeed, _normalSpeed, _warrant.getDisplayName());
                     // _normalSpeed typically is the last setThrottleSetting done. However it also
                     // may be reset after a down ramp to be the setting expected to be resumed at the
                     // point skipped to by the down ramp.

                    if (_rampData.isUpRamp()) {
                        _resumePending = true;
                        // The ramp up will take time and the script may have other speed commands while
                        // ramping up. So 'scriptSpeed' may not be actual script speed when ramp up distance
                        // is traveled.  Adjust 'endSpeed' to match that 'scriptSpeed'.
                        // Up rampDist is distance from current throttle speed to endSpeed.
                        float rampDist = _rampData.getRampLength();
                        long scriptTime = 0;
                        float scriptDist = 0;   // distance traveled at speed 'scriptSpeed' to next speed command
                        float scriptSpeed = _normalSpeed;
                        boolean hasSpeed = (scriptSpeed > 0);
                        int idx = Math.max(_idxSkipToSpeedCommand, _idxCurrentCommand);
                        // look ahead for point in script where ramp will finish and match the settings
                        while (idx < _commands.size()) {
                            ThrottleSetting ts = _commands.get(idx);
                            scriptTime = ts.getTime();
                            String cmd = ts.getCommand().toUpperCase();
                            if (hasSpeed) {
                                scriptDist += _speedUtil.getDistanceTraveled(scriptSpeed, _endSpeedType, scriptTime);
                                if (scriptDist >= rampDist) {   // up ramp will be complete within this distance
                                    advanceToCommandIndex(idx); // don't let script set speeds up to here
                                    break;
                                }
                            }
                            if ("SPEED".equals(cmd)) {
                                scriptSpeed = Float.parseFloat(ts.getValue());
                                hasSpeed = (scriptSpeed > 0);
                                endSpeed = _speedUtil.modifySpeed(scriptSpeed, _endSpeedType);
                                _rampData = _speedUtil.getRampForSpeedChange(speed, endSpeed);
                                rampDist = _rampData.getRampLength();
                                advanceToCommandIndex(idx); // don't let script set speeds up to here
                            }
                            idx++;
                        }
                        _normalSpeed = scriptSpeed;

                        if (log.isDebugEnabled()) 
                            log.debug("Ramp up for \"{}\". speedType= {}, endSpeed= {}, scriptDist= {}, resumeIndex= {}, nextSpeedIdx= {}, rampDist= {}",
                                    _endSpeedType, speed, endSpeed, scriptSpeed, _idxSkipToSpeedCommand+1, _idxCurrentCommand+1, rampDist);
                                // Note: command indexes biased from 0 to 1 to match Warrant display of commands.

                        ListIterator<Float> iter = _rampData.speedIterator(true);
                        if (iter.hasNext()) {
                            speed = iter.next().floatValue();   // current setting
                        }
                        while (iter.hasNext()) { // do ramp up
                            if (stop) {
                                break;
                            }
                            speed = iter.next().floatValue();
                            setSpeed(speed);

                            try {
                                wait(timeIncrement);
                            } catch (InterruptedException ie) {
                                _lock.unlock();
                                stop = true;
                            }
                        }
                    } else {     // decreasing, ramp down to a modified speed
                        if (log.isDebugEnabled()) 
                            log.debug("Ramp down for \"{}\". curSpeed= {}, endSpeed= {}, startIdx={} BlockOrderIdx= {}",
                                    _endSpeedType, speed, endSpeed, _idxCurrentCommand+1, _endBlockIdx);
                        // Note: command indexes biased from 0 to 1 to match Warrant display of commands.
                        ListIterator<Float> iter = _rampData.speedIterator(false);
                        if (iter.hasPrevious()) {
                            speed = iter.previous().floatValue();   // current setting
                        }
                        while (iter.hasPrevious()) {
                            if (stop) {
                                break;
                            }
                            speed = iter.previous().floatValue();
                            if (_useIndex) {
                                if ( _warrant._idxCurrentOrder > _endBlockIdx) { // loco overran end block
                                    speed = endSpeed;
                                } else if ( _warrant._idxCurrentOrder < _endBlockIdx && 
                                        _endSpeedType.equals(Warrant.Stop) && Math.abs(speed - endSpeed) <.001f) {
                                    // at last speed change. let loco creep to end block
                                    if (log.isDebugEnabled()) 
                                        log.debug("Extending ramp to reach block {}. speed= {}",
                                                _warrant.getBlockAt(_endBlockIdx).getDisplayName(), speed);
                                    while (_endBlockIdx - _warrant._idxCurrentOrder > 0) {
                                        // Until loco reaches end block, continue current speed
                                        try {
                                            wait(timeIncrement);
                                        } catch (InterruptedException ie) {
                                            _lock.unlock();
                                            stop = true;
                                        }   
                                    }
                                }
                            }
                            setSpeed(speed);

                            try {
                                wait(timeIncrement);
                            } catch (InterruptedException ie) {
                                _lock.unlock();
                                stop = true;
                            }
                        }
                        _stopPending = false;

                        // Down ramp may advance the train beyond the point where the script is paused.
                        // Any down ramp requested with _useIndex==true is expected to end at the end of
                        // a block i.e. the block of BlockOrder indexed by _endBlockIdx.
                        // Therefore script should resume at the exit to this block.
                        // advanceToCommandIndex() sets the resume point of script.
                        if (_useIndex) {
                            int idx = _idxCurrentCommand;
                            while (idx < _commands.size()) {
                                ThrottleSetting ts = _commands.get(idx);
                                NamedBean bean = ts.getNamedBeanHandle().getBean();
                                if (bean instanceof OBlock) {
                                    OBlock blk = (OBlock)bean;
                                    if (_endBlockIdx < _warrant.getIndexOfBlock(blk, _endBlockIdx)) {
                                        // script is past end point, command should be NOOP
                                        break;
                                    }
                                }
                                if (ts.getCommand().toUpperCase().equals("SPEED")) {
                                    _normalSpeed = Float.parseFloat(ts.getValue()); // modify 'resume' speed to be last speed
                                }
                                idx++;
                            }
                            advanceToCommandIndex(idx); // skip up to this command

                            if (log.isDebugEnabled()) 
                                log.debug("endBlkName= {}, cmdBlkName= {}, _idxCurrentCommand={}, skipToBlkName= {}, skipToIdx= {}, _normalSpeed= {}",
                                        _warrant.getBlockAt(_endBlockIdx).getDisplayName(),
                                        _commands.get(_idxCurrentCommand).getNamedBeanHandle().getBean().getDisplayName(), _idxCurrentCommand+1,
                                        _commands.get(idx).getNamedBeanHandle().getBean().getDisplayName(), idx+1,
                                        _normalSpeed); // Note: command indexes biased from 0 to 1 to match Warrant display of commands.
                       }
                    }
                    
                } finally {
                    _lock.unlock();
                    if (!_endSpeedType.equals(Warrant.Stop) &&
                            !_endSpeedType.equals(Warrant.EStop)) {
                        // speed restored, clear any stop waits
                        // If flags already off, OK to repeat setting false
                        setWaitforClear(false);
                        setHalt(false);
                    }
                    _resumePending = false;
                }
            }
            rampDone(stop, _endSpeedType);
            ready = true;
            stop = false;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Engineer.class);
}
