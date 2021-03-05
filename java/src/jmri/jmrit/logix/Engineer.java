package jmri.jmrit.logix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantLock;
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
    private int _idxSkipToSpeedCommand;   // skip to this index to reset script when ramping
    private float _normalSpeed = 0;       // current commanded throttle setting (unmodified)
    private String _speedType = Warrant.Normal;    // current speed name
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
    final ReentrantLock _lock = new ReentrantLock(true);    // Ramp uses to block script speeds
    private Object _rampLockObject = new Object(); // used for synchronizing threads for _ramp
    private ThrottleRamp _ramp;
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
                    log.debug("Skip Cmd #{}: {} Warrant {}", _idxCurrentCommand+1, ts, _warrant.getDisplayName());
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
                    log.debug("Train reached block \"{}\" before script et={}ms . Warrant {}",
                            _warrant.getCurrentBlockName(), _currentCommand.getTime(), _warrant.getDisplayName());
            }
            if (_abort) {
                break;
            }

            long cmdStart = System.currentTimeMillis();
            if (log.isDebugEnabled()) 
                log.debug("Start Cmd #{} for block \"{}\" currently in \"{}\". wait {}ms to do cmd {}. Warrant {}",
                    _idxCurrentCommand+1, _currentCommand.getBeanDisplayName(), _warrant.getCurrentBlockName(), 
                    cmdWaitTime, command.toString(), _warrant.getDisplayName());
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
                    log.error("At time wait {}", ie);
                    _warrant.debugInfo();
                    Thread.currentThread().interrupt();
                } catch (java.lang.IllegalArgumentException iae) {
                    log.error("At time wait {}", iae);
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
                        log.error("At _waitForSync {}", ie);
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
                        log.error("At _atClear {}", ie);
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
                        log.error("At _atHalt {}", ie);
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
                long et;
                if (_ramp != null && !_ramp.ready) {
                    int idx = _idxCurrentCommand;
                    try {
                        if (log.isDebugEnabled()) 
                            log.debug("Waiting for ramp to finish at Cmd #{}.  Warrant {}", 
                                    _idxCurrentCommand+1, _warrant.getDisplayName());
                        wait();
                    } catch (InterruptedException ie) {
                        _warrant.debugInfo();
                        Thread.currentThread().interrupt();
                    }
                    // ramp will decide whether to skip or execute _currentCommand
                    et = System.currentTimeMillis() - cmdStart;
                    if (log.isDebugEnabled()) {
                        log.debug("Cmd #{} held for {}ms. {} warrant {}",
                                idx+1, et, _currentCommand, _warrant.getDisplayName());
                    }
                } else {
                    executeComand(_currentCommand);
                    et = System.currentTimeMillis() - cmdStart;
                    _idxCurrentCommand++;
                    if (log.isDebugEnabled()) {
                        log.debug("Cmd #{} done. et={}. {} warrant {}",
                                _idxCurrentCommand, et, _currentCommand, _warrant.getDisplayName());
                    }
                }
            }

        }
        // shut down
        setSpeed(0.0f); // for safety to be sure train stops                               
        _warrant.stopWarrant(_abort, true);
    }

    private void executeComand(ThrottleSetting ts) {
        Command command = ts.getCommand();
        CommandValue cmdVal = ts.getValue();
        switch (command) {
            case SPEED:
                float throttle = cmdVal.getFloat();
                _normalSpeed = throttle;
                float speedMod = _speedUtil.modifySpeed(throttle, _speedType);
                if (Math.abs(throttle - speedMod) > .0001f) {
                    _timeRatio = throttle / speedMod;
                } else {
                    _timeRatio = 1.0f;
                }
                setSpeed(speedMod);                                
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
//        if (log.isTraceEnabled()) 
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
            log.debug("setRunOnET {} command #{} warrant {}", set, _idxCurrentCommand+1, _warrant.getDisplayName());
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
     * If waiting to sync entrance to a block boundary with recorded wait time,
     * or waiting for clearance ahead for rogue occupancy, stop aspect or
     * sharing of turnouts, this call will free the wait.
     */
    protected synchronized void clearWaitForSync() {
        if (_waitForSync) {
            if (log.isDebugEnabled()) 
                log.debug("_waitForSync={} clearWaitForSync() calls notify()", _waitForSync);
            notifyAll();   // if wait is cleared, this sets _waitForSync= false
        } else {
            ThrottleSetting ts = _commands.get(_idxCurrentCommand);
            OBlock block = _warrant.getCurrentBlockOrder().getBlock();
            // block went active. if waiting on cmdWaitTime, clear it
            if (ts.getCommand().equals(Command.NOOP) && ts.getBeanDisplayName().equals(block.getDisplayName())) {
                if (log.isDebugEnabled()) 
                    log.debug("_waitForSync={} clearWaitForSync() calls notify()", _waitForSync);
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
            } else if (!_ramp.ready) {
                // for repeated command
                if (_ramp.duplicate(endSpeedType, endBlockIdx, useIndex)) {
                    return;
                }
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
                synchronized (_rampLockObject) {
                    _rampLockObject.notifyAll(); // free wait at ThrottleRamp.run()
                    log.debug("rampSpeedTo called notify _ramp.ready={}", _ramp.ready);
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

    private void cancelRamp(boolean die) {
        if (_ramp != null && !_ramp.ready) {
            _ramp.quit(die);
        }
    }

    /**
     * do throttle setting
     * @param s throttle setting
     */
     protected void setSpeed(float s) {
        float speed = s;
        _speedUtil.speedChange();   // call before changing throttle setting
        _throttle.setSpeedSetting(speed);       // CAN MISS SETTING SPEED! (as done when runOnLayoutEventually used) ??
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
        if (Math.abs(getSpeedSetting() - newSpeed) < .002f) {
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
    public synchronized void setHalt(boolean halt) {
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
    protected synchronized void setWaitforClear(boolean stop) {
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
        StringBuilder buf = new StringBuilder("Engineer flags: _waitForClear= ");
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
    public synchronized void setStop(boolean eStop, boolean setHalt) {
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

    public synchronized int getRunState() {
        if (_stopPending) {
            if (_halt) {
                return Warrant.RAMP_HALT;
            }
            return Warrant.STOP_PENDING;
        } else if (_resumePending) {
            return Warrant.RAMPING_UP;            
        } else if (_waitForClear) {
            return Warrant.WAIT_FOR_CLEAR;
        } else if (_waitForSensor) {
            return Warrant.WAIT_FOR_SENSOR;
        } else if (_halt) {
            return Warrant.HALT;
        } else if (_abort) {
            return Warrant.ABORT;
        } else if (_waitForSync) {
            return Warrant.WAIT_FOR_TRAIN;
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
        
        int num = Math.round(cmdVal.getFloat());
        if (num <= 0) {
            return;
        }
        num--;
        cmdVal.setFloat(num);
        java.awt.Color color = java.awt.Color.red;

        String msg = null;
        if (_warrant.getSpeedUtil().getDccAddress().equals(warrant.getSpeedUtil().getDccAddress())) {
            // Same loco, perhaps different warrant
            log.debug("Loco address {} finishes warrant {} and starts warrant {}",
                    warrant.getSpeedUtil().getDccAddress(), _warrant.getDisplayName(), warrant.getDisplayName());
            Thread checker = new CheckForTermination(_warrant, warrant, num);
            checker.start();
            if (log.isDebugEnabled()) log.debug("Exit runWarrant");
            return;
        } else {
            log.debug("Loco address {} on warrant {} and starts loco {} on warrant {}",
                    _warrant.getSpeedUtil().getDccAddress(), _warrant.getDisplayName(),
                    warrant.getSpeedUtil().getDccAddress(), warrant.getDisplayName());
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
        ThreadingUtil.runOnLayoutEventually(()-> WarrantTableFrame.getDefault().setStatusText(m, c, true));
        log.debug("Exit runWarrant - {}",msg);
    }

    private static class CheckForTermination extends Thread {
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
        public void run() {
            OBlock endBlock = oldWarrant.getLastOrder().getBlock();
            long time = 0;
            String msg = null;
            while (time < 10000) {
                if (oldWarrant.getRunMode() == Warrant.MODE_NONE) {
                    break;
                }
                int priority = Thread.currentThread().getPriority();
                try {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    Thread.sleep(100);
                    time += 100;
                } catch (InterruptedException ie) {
                    time = 10000;
                    msg = Bundle.getMessage("CannotRun", newWarrant.getDisplayName(), ie);
                } finally {
                    Thread.currentThread().setPriority(priority);
                }
                if (time >= 10000) {
                    msg = Bundle.getMessage("cannotLaunch",
                            newWarrant.getDisplayName(), oldWarrant.getDisplayName(), endBlock.getDisplayName());
                }
            }
            if (log.isDebugEnabled()) log.debug("CheckForTermination waited {}ms. runMode={} ", time, oldWarrant.getRunMode());

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
            ThreadingUtil.runOnLayoutEventually(() -> // delay until current warrant can complete
                WarrantTableFrame.getDefault().setStatusText(m, c, true));
        }
    }

    private void rampDone(boolean stop, String type) {
        if (!stop) {
            _warrant.fireRunStatus("RampDone", _halt, type);
        }
        if (!_atHalt && !_atClear) {
            synchronized (this) {
                notifyAll();  // let engineer run script
                log.debug("rampDone called notify");
            }
            if (_currentCommand.getCommand().equals(Command.NOOP)) {
                _idxCurrentCommand--;   // notify advances command.  Repeat wait for entry to next block
            }
        }
        if (log.isDebugEnabled())
            log.debug("ThrottleRamp done: {} for \"{}\" at speed= {}. _normalScript={}, Thread.State= {} resume index= {}, current Index= {} on warrant {}",
                    (stop?"stopped":"completed"), type, getSpeedSetting(), _normalSpeed, (_ramp != null?_ramp.getState():"_ramp is null!"),
                    _idxSkipToSpeedCommand+1, _idxCurrentCommand+1, _warrant.getDisplayName());
        // Note: command indexes biased from 0 to 1 to match Warrant display of commands.
    }

    /*
     * *************************************************************************************
     */

     class ThrottleRamp extends Thread {

         private RampData _rampData;
         private String _endSpeedType;
         private int _endBlockIdx;   // index of block where down ramp ends - not used for up ramps.
         private boolean _useIndex;  // false if endBlockIdx should not be considered
         private boolean stop = false;   // aborts ramping
         boolean ready = false;      // ready for call doRamp
         private boolean _die = false;    // kills ramp for good

         ThrottleRamp() {
            setName("Ramp(" + _warrant.getTrainName() +")");
         }

         void quit(boolean die) {
             log.debug("ThrottleRamp.quit({}) _die= {}", die, _die);
             stop = true;
             if (die) { // once set to true, do not allow resetting to false
                 _die = die;    // permanent shutdown, warrant running ending
             }
             synchronized (_rampLockObject) {
                 log.debug("ThrottleRamp.quit calls notify()");
                 _rampLockObject.notifyAll(); // free waits at ramp time interval
             }
         }

        void setParameters(String endSpeedType, int endBlockIdx, boolean useIndex) {
            _endSpeedType = endSpeedType;
            _endBlockIdx = endBlockIdx;
            _useIndex = useIndex;
            _stopPending = endSpeedType.equals(Warrant.Stop);                    
        }

        boolean duplicate(String endSpeedType, int endBlockIdx, boolean useIndex) {
            if (endBlockIdx != _endBlockIdx || 
                    !endSpeedType.equals(_endSpeedType) || useIndex != _useIndex) {
                return false;
            }
            return true;                    
        }

        RampData getRampData () {
            return _rampData;
        }

        /**
         * 
         * @param blockIdx  index of block order where ramp finishes
         * @param cmdIdx   current command index
         * @return command index of block where commands should not be executed 
         */
        int getCommandIndexLimit(int blockIdx, int cmdIdx) {
            // get next block
            int limit = _commands.size();
            String curBlkName = _warrant.getCurrentBlockName();
            String endBlkName = _warrant.getBlockAt(blockIdx).getDisplayName();
            if (_useIndex) {
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
            }
            log.debug("getCommandIndexLimit: in current block {}, limitIdx = {} in block {}", curBlkName, limit+1, endBlkName);
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
            long rampTime = 0;      // accumulating time doing the ramp
            float rampDist = 0;     // accumulating distance of ramp
            float rampLen = _rampData.getRampLength();
            float scriptSpeed = _normalSpeed;
            float distToCmd = _currentCommand.getTrackSpeed() * _currentCommand.getTime();   // distance to next command

            int commandIndexLimit = getCommandIndexLimit(_endBlockIdx, _idxCurrentCommand);
            if (log.isDebugEnabled()) 
                log.debug("ThrottleRamp for \"{}\". At Cmd#{} limit#{}. rampLen= {} distToCmd= {}. useIndex= {}. on warrant {}",
                   _endSpeedType, _idxCurrentCommand+1, commandIndexLimit+1, rampLen, distToCmd, _useIndex, _warrant.getDisplayName());

            synchronized (this) {
                try {
                     _lock.lock();
                     // _normalSpeed typically is the last setThrottleSetting done. However it also
                     // may be reset after a down ramp to be the setting expected to be resumed at the
                     // point skipped to by the down ramp.

                    if (_rampData.isUpRamp()) {
                        _resumePending = true;
                        // The ramp up will take time and the script may have other speed commands while
                        // ramping up. So the actual script speed may not match the endSpeed when ramp up distance
                        // is traveled.  Adjust 'endSpeed' to match that 'scriptSpeed'.
                        // Up rampLen is distance from current throttle speed to endSpeed of ramp.
                        if (log.isDebugEnabled()) {
                            log.debug("RAMP UP \"{}\" speed from {}, to {}. distToCmd= {}, Ramp: {}mm {}steps {}ms, Currentdx= {}, SkipToIdx= {}",
                                    _endSpeedType, speed, endSpeed, distToCmd, rampLen, _rampData.getNumSteps(), _rampData.getRamptime(),
                                    _idxCurrentCommand+1, _idxSkipToSpeedCommand+1);
                                // Note: command indexes biased from 0 to 1 to match Warrant display of commands.
                        }
                        // during ramp up the script may have non-speed commands that should be executed.
                        ListIterator<Float> iter = _rampData.speedIterator(true);
                        float prevSpeed = iter.next().floatValue();   // skip repeat of current speed
                        float prevScriptSpeed;

                        while (iter.hasNext()) { // do ramp up
                            if (stop) {
                                break;
                            }
                            speed = iter.next().floatValue();

                            setSpeed(speed);
                            rampDist += _speedUtil.getDistanceOfSpeedChange(prevSpeed, speed, timeIncrement);
                            prevSpeed = speed;

                            try {
                                wait(timeIncrement);
                            } catch (InterruptedException ie) {
                                _lock.unlock();
                                stop = true;
                            }
                            rampTime += timeIncrement;
                            rampDist += _speedUtil.getDistanceOfSpeedChange(prevSpeed, speed, timeIncrement);
                            prevSpeed = speed;

                            // Execute the non-Speed commands during the ramp
                            if (distToCmd < rampDist && _idxCurrentCommand < commandIndexLimit) {
                                CommandValue cmdVal = _currentCommand.getValue();
                                if (!cmdVal.getType().equals(ThrottleSetting.ValueType.VAL_FLOAT)) {
                                    executeComand(_currentCommand);
                                    if (log.isDebugEnabled()) {
                                        log.debug("Cmd #{} done.  rampTime={}. distToCmd={} rampDist={}",
                                                _idxCurrentCommand+1, rampTime, distToCmd, rampDist);
                                    }
                                    distToCmd += scriptSpeed * _currentCommand.getTime();
                                } else {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Cmd #{} skipped. rampTime={}. distToCmd={} rampDist={}",
                                                _idxCurrentCommand+1, rampTime, distToCmd, rampDist);
                                    }
                                    prevScriptSpeed = scriptSpeed;
                                    scriptSpeed = _currentCommand.getValue().getFloat();
                                    distToCmd += _speedUtil.getDistanceOfSpeedChange(prevScriptSpeed, scriptSpeed, timeIncrement);
                                    if (_speedUtil.modifySpeed(scriptSpeed, _endSpeedType) < speed) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("Ramp stopped at speed {}. Cmd #{} rampTime={}. distToCmd={} rampDist={}",
                                                    speed, _idxCurrentCommand+1, rampTime, distToCmd, rampDist);
                                        }
                                        executeComand(_currentCommand);
                                        stop = true;    // let script take over from here.
                                    }
                                 }
                                _currentCommand = _commands.get(++_idxCurrentCommand);
                            }
                            advanceToCommandIndex(_idxCurrentCommand); // skip up to this command
                        }
                    } else {     // decreasing, ramp down to a modified speed
                        // Down ramp may advance the train beyond the point where the script is paused.
                        // Any down ramp requested with _useIndex==true is expected to end at the end of
                        // a block i.e. the block of BlockOrder indexed by _endBlockIdx.
                        // Therefore script should resume at the exit to this block.
                        // During ramp down the script may have other Non speed commands that should be executed.
                        if (log.isDebugEnabled()) {
                            // Note: command indexes biased from 0 to 1 to match Warrant display of commands.
                            log.debug("RAMP DOWN to \"{}\". curSpeed= {}, endSpeed= {}, endBlock= {}",
                                    _endSpeedType, speed, endSpeed, _warrant.getBlockAt(_endBlockIdx).getDisplayName());
                        }
                        ListIterator<Float> iter = _rampData.speedIterator(false);
                        float prevSpeed = iter.previous().floatValue();   // skip repeat of current throttle setting
                        float prevScriptSpeed;
 
                        while (iter.hasPrevious()) {
                            if (stop) {
                                break;
                            }
                            speed = iter.previous().floatValue();

                            if (_useIndex) {    // correction code for ramps that are too long or too short
                                if ( _warrant._idxCurrentOrder > _endBlockIdx) {
                                    // loco overran end block.  Set end speed and leave ramp
                                    speed = endSpeed;
                                    stop = true;
                                } else if ( _warrant._idxCurrentOrder < _endBlockIdx && 
                                        _endSpeedType.equals(Warrant.Stop) && Math.abs(speed - endSpeed) <.001f) {
                                    // At last speed change to set throttle to 0.0, but train has not 
                                    // reached the last block. Let loco creep to end block at current setting.
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

                            rampTime += timeIncrement;
                            rampDist += _speedUtil.getDistanceOfSpeedChange(prevSpeed, speed, timeIncrement);
                            prevSpeed = speed;

                            // Execute the non-Speed commands during the ramp
                            if (distToCmd < rampDist && _idxCurrentCommand < commandIndexLimit) {
                                CommandValue cmdVal = _currentCommand.getValue();
                                if (!cmdVal.getType().equals(ThrottleSetting.ValueType.VAL_FLOAT)) {
                                    executeComand(_currentCommand);
                                    if (log.isDebugEnabled()) {
                                        log.debug("Cmd #{} done. rampTime={}. distToCmd={} rampDist={}",
                                                _idxCurrentCommand+1, rampTime, distToCmd, rampDist);
                                    }
                                    distToCmd += scriptSpeed * _currentCommand.getTime();
                                } else {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Cmd #{} skipped. rampTime={}.  distToCmd={} rampDist={}",
                                                _idxCurrentCommand+1, rampTime, distToCmd, rampDist);
                                    }
                                    prevScriptSpeed = scriptSpeed;
                                    scriptSpeed = _currentCommand.getValue().getFloat();
                                    distToCmd += _speedUtil.getDistanceOfSpeedChange(prevScriptSpeed, scriptSpeed, timeIncrement);
                                 }
                                _currentCommand = _commands.get(++_idxCurrentCommand);
                            }
                        }
                        // ramp done.
                        if (log.isDebugEnabled()) {
                            log.debug("Ramp Down done. _idxCurrentCommand={} commandIndexLimit={}. warrant {}",
                                    _idxCurrentCommand+1, commandIndexLimit, _warrant.getDisplayName());
                        }
                        if (_useIndex) {
                            while (_idxCurrentCommand < commandIndexLimit) {
                                NamedBean bean = _currentCommand.getNamedBeanHandle().getBean();
                                if (bean instanceof OBlock) {
                                    OBlock blk = (OBlock)bean;
                                    if (_endBlockIdx < _warrant.getIndexOfBlock(blk, _endBlockIdx)) {
                                        // script is past end point, command should be NOOP
                                        break;
                                    }
                                }
                                CommandValue cmdVal = _currentCommand.getValue();
                                if (!cmdVal.getType().equals(ThrottleSetting.ValueType.VAL_FLOAT)) {
                                    executeComand(_currentCommand);
                                    if (log.isDebugEnabled()) {
                                        log.debug("Cmd #{} command=\"{}\' executed. warrant {}",
                                                _idxCurrentCommand+1, _currentCommand.getCommand(), _warrant.getDisplayName());
                                    }
                                } else {
                                    _normalSpeed = cmdVal.getFloat();
                                    if (log.isDebugEnabled()) {
                                        log.debug("Cmd #{} command=\"{}\' skipped. warrant {}",
                                                _idxCurrentCommand+1, _currentCommand.getCommand(), _warrant.getDisplayName());
                                    }
                                }
                                _currentCommand = _commands.get(++_idxCurrentCommand);
                            }
                            advanceToCommandIndex(_idxCurrentCommand); // skip up to this command

                            if (log.isDebugEnabled()) 
                                log.debug("End Blk= {}, Cmd Blk= {}, idxCurrentCommand={}, normalSpeed= {}",
                                        _warrant.getBlockAt(_endBlockIdx).getDisplayName(),
                                        _commands.get(_idxCurrentCommand).getNamedBeanHandle().getBean().getDisplayName(),
                                        _normalSpeed); // Note: command indexes biased from 0 to 1 to match Warrant display of commands.
                        }
                        
                        _stopPending = false;
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
            ready = true;
            rampDone(stop, _endSpeedType);
            stop = false;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(Engineer.class);
}
