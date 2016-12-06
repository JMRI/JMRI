package jmri.jmrit.logix;

import java.util.concurrent.locks.ReentrantLock;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute a throttle command script for a warrant.
 * <p>
 * This generally operates on it's own thread, but switches back to the Layout
 * thread when asking the Warrant to perform actions.
 *
 * @author Pete Cressman Copyright (C) 2009, 2010, 2011
 */
/*
 * ************************ Thread running the train ****************
 */
public class Engineer extends Thread implements Runnable, java.beans.PropertyChangeListener {
    
    private int _idxCurrentCommand;     // current throttle command
    private float _normalSpeed = 0;       // current commanded throttle setting (unmodified)
    private String _speedType = Warrant.Normal;    // current speed name
    private float _timeRatio = 1.0f;     // ratio to extend scripted time when speed is modified
    private boolean _abort = false;
    private boolean _halt = false;  // halt/resume from user's control
    private boolean _waitForClear = false;  // waits for signals/occupancy/allocation to clear
    private boolean _waitForSync = false;  // waits for train to catch up to commands
    private boolean _waitForSensor = false; // wait for sensor event
    private boolean _speedOverride = false; // speed changing due to signal or occupancy
    private boolean _runOnET = false;   // Execute commands on ET only - do not synch
    private boolean _setRunOnET = false; // Need to delay _runOnET from the block that set it
    private int _syncIdx;           // block order index of current command
    protected DccThrottle _throttle;
    private final Warrant _warrant;
    private Sensor _waitSensor;
    private int _sensorWaitState;
    private ThrottleRamp _ramp;
    final ReentrantLock _lock = new ReentrantLock(true);
    private boolean _atHalt = false;
    private boolean _atClear = false;
    SignalSpeedMap _speedMap;
    RosterSpeedProfile _speedProfile;

    Engineer(Warrant warrant, DccThrottle throttle) {
        _warrant = warrant;
        _idxCurrentCommand = 0;
        _throttle = throttle;
        _syncIdx = -1;
        _waitForSensor = false;
        _speedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class);
        jmri.jmrit.roster.RosterEntry ent = _warrant.getRosterEntry();
        if (ent != null) {
            _speedProfile = ent.getSpeedProfile();
        }
        if (_speedProfile == null) {
            log.warn("RosterSpeedProfile not found. Using default ThrottleFactor " + _speedMap.getDefaultThrottleFactor());
        }
    }

    int cmdBlockIdx = 0;

    @Override
    public void run() {
        if (log.isDebugEnabled()) log.debug("Engineer started warrant {}", _warrant.getDisplayName());

        cmdBlockIdx = 0;
        while (_idxCurrentCommand < _warrant._commands.size()) {
            long et = System.currentTimeMillis();
            ThrottleSetting ts = _warrant._commands.get(_idxCurrentCommand);
            int idx = _warrant.getIndexOfBlock(ts.getBlockName(), cmdBlockIdx);
            if (idx >= 0) {
                cmdBlockIdx = idx;
            }
            _runOnET = _setRunOnET;     // OK to set here
            long time = (long) (ts.getTime() * _timeRatio);
            String command = ts.getCommand().toUpperCase();
            if (log.isDebugEnabled()) log.debug("Start Cmd #{} for block \"{}\" currently in \"{}\". wait {}ms to do cmd {}. Warrant {}",
                    _idxCurrentCommand+1, ts.getBlockName(), _warrant.getCurrentBlockName(), time, command, _warrant.getDisplayName());
            if (_abort) {
                break;
            }
            if (cmdBlockIdx < _warrant.getCurrentOrderIndex()) {
                // Train advancing too fast, need to process commands more quickly,
                // allowing half second for whistle toots etc.
                time = Math.min(time, 500);
            }
            // actual playback total elapsed time is "ts.getTime()" before record time.
            // current block at playback may also be before current block at record
            synchronized (this) {
                try {
                    if (time > 0) {
                        wait(time);
                    }
                    if (_abort) {
                        break;
                    }
                } catch (InterruptedException ie) {
                    log.error("InterruptedException " + ie);
                } catch (java.lang.IllegalArgumentException iae) {
                    log.error("IllegalArgumentException " + iae);
                }
            }
            
            _syncIdx = cmdBlockIdx;
            // Having waited, time=ts.getTime(), so blocks should agree.  if not,
            // wait for train to arrive at block and send sync notification.
            // note, blind runs cannot detect entrance.
            if (!_runOnET && _syncIdx > _warrant.getCurrentOrderIndex()) {
                // commands are ahead of current train position
                // When the next block goes active or a control command is made, a call to rampSpeedTo()
                // will test these indexes again and can trigger a notify() to free the wait
                if (log.isDebugEnabled()) log.debug("Wait for train to enter \"{}\". Warrant {}", ts.getBlockName(), _warrant.getDisplayName());
                synchronized (this) {
                    try {
                        _waitForSync = true;
                      ThreadingUtil.runOnLayoutEventually(() -> {
                          _warrant.fireRunStatus("Command", _idxCurrentCommand - 1, _idxCurrentCommand);
                      });
                        wait();
                        _waitForSync = false;
                    } catch (InterruptedException ie) {
                        log.error("InterruptedException at _waitForSync " + ie);
                    } catch (java.lang.IllegalArgumentException iae) {
                        log.error("IllegalArgumentException " + iae);
                    }
                }
                if (_abort) {
                    break;
                }
            }

            // block position and elapsed time are as expected, but track conditions 
            // such as signals, occupancy  may required waiting
            if (_waitForClear) {
                if (log.isDebugEnabled()) log.debug("Waiting for clearance. _waitForClear= {} _halt= {} \"{}\".  Warrant {}",
                        _waitForClear, _halt, ts.getBlockName(), _warrant.getDisplayName());
                synchronized (this) {
                    try {
                        _atClear = true;
                        wait();
                        _waitForClear = false;
                        _atClear = false;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("InterruptedException at _atClear " + ie);
                    }
                }
                if (_abort) {
                    break;
                }
            }
                
            // user's command to halt requires waiting
            if (_halt) {
                if (log.isDebugEnabled()) log.debug("Waiting to Resume. _halt= {}, _waitForClear= {}, Block \"{}\".  Warrant {}",
                        _halt, _waitForClear, ts.getBlockName(), _warrant.getDisplayName());
                synchronized (this) {
                    try {
                        _atHalt = true;
                        wait();
                        _halt = false;
                        _atHalt = false;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("InterruptedException at _atHalt " + ie);
                    }
                }
                if (_abort) {
                    break;
                }
            }

            try {
                if (command.equals("SPEED")) {
                    float speed = Float.parseFloat(ts.getValue());
                    _lock.lock();
                    if (log.isTraceEnabled()) log.trace("SPEED CMD: speed= "+speed+" type= "+_speedType);
                    try {
                        _normalSpeed = speed;
                        float speedMod = modifySpeed(speed, _speedType);
                        if (Math.abs(speed - speedMod) > .0001f) {
                            _timeRatio = speed / speedMod;
                        } else {
                            _timeRatio = 1.0f;
                        }
                        setSpeed(speedMod);
                    } finally {
                        _lock.unlock();
                    }
                } else if (command.equals("SPEEDSTEP")) {
                    int step = Integer.parseInt(ts.getValue());
                    setSpeedStepMode(step);
                } else if (command.equals("FORWARD")) {
                    boolean isForward = Boolean.parseBoolean(ts.getValue());
                    _throttle.setIsForward(isForward);
                } else if (command.startsWith("F")) {
                    int cmdNum = Integer.parseInt(command.substring(1));
                    boolean isTrue = Boolean.parseBoolean(ts.getValue());
                    setFunction(cmdNum, isTrue);
                } else if (command.startsWith("LOCKF")) {
                    int cmdNum = Integer.parseInt(command.substring(5));
                    boolean isTrue = Boolean.parseBoolean(ts.getValue());
                    setLockFunction(cmdNum, isTrue);
                } else if (command.equals("SET SENSOR")) {
                    setSensor(ts.getBlockName(), ts.getValue());
                } else if (command.equals("WAIT SENSOR")) {
                    getSensor(ts.getBlockName(), ts.getValue());
                } else if (command.equals("START TRACKER")) {
                    ThreadingUtil.runOnLayout(() -> {
                        _warrant.startTracker();
                    });
                } else if (command.equals("RUN WARRANT")) {
                    runWarrant(ts);
                } else if (_runOnET && command.equals("NOOP")) {    // let warrant know engineer expects entry into dark block
                    ThreadingUtil.runOnLayout(() -> {
                        _warrant.goingActive(_warrant.getBlockAt(cmdBlockIdx));
                    });
                }
                et = System.currentTimeMillis() - et;
                _idxCurrentCommand++;
                if (log.isDebugEnabled()) log.debug("Cmd #{}: {} et={} warrant {}", _idxCurrentCommand, ts.toString(), et, _warrant.getDisplayName());
            } catch (NumberFormatException e) {
                log.error("Command failed! " + ts.toString() + " - " + e);
            }
        }
        // shut down
        ThreadingUtil.runOnLayout(() -> {
            _warrant.stopWarrant(false);
        });
    }

    protected int getCurrentCommandIndex() {
        return _idxCurrentCommand;
    }

    protected void setCurrentCommandIndex(int idx) {
        _idxCurrentCommand = idx;
    }

    private void setSpeedStepMode(int stepMode) {
        _lock.lock();
        try {
            _throttle.setSpeedStepMode(stepMode);
        } finally {
            _lock.unlock();
        }
    }

    /**
     * Cannot set _runOnET until current NOOP command completes
     * so there is the intermediate flag _setRunOnET
     * @param set true to run on elapsed time calculations only, false to
     *            consider other inputs
     */
    protected void setRunOnET(Boolean set) {
        if (log.isDebugEnabled()) log.debug("setRunOnET {} command #{} warrant {}", set, _idxCurrentCommand, _warrant.getDisplayName());
//        checkHalt();
        _setRunOnET = set;
        if (!set) {
            _runOnET = set;
        }
    }

    protected boolean getRunOnET() {
        return _setRunOnET;
    }

    synchronized protected void setWaitforClear(boolean set) {
        if ( !set) {
            _waitForClear = false;
            if (_atClear) {
                if (log.isDebugEnabled()) log.debug("setWaitforClear({}) calls notify()",set);
                notify();   // if wait is cleared, this sets _waitForClear= false                
            }
        } else {
            _waitForClear = true;
        }
        // if not at the clear wait (_atClear=false) and set= false, _waitForClear remains true
        if (log.isDebugEnabled()) log.debug("setWaitforClear({}) sets _waitForClear= {}",
                set, _waitForClear);            
    }

    /**
     * If waiting to sync entrance to a block boundary with recorded wait time,
     * or waiting for clearance ahead for rogue occupancy, stop aspect or
     * sharing of turnouts, this call will free the wait.
     */
    synchronized protected void clearWaitForSync() {
        _waitForClear = false;
        if (_waitForSync) {
            if (log.isDebugEnabled()) log.debug("clearWaitForSync calls notify()");
            notify();   // if wait is cleared, this sets _waitForSync= false
        }
        if (log.isDebugEnabled()) log.debug("clearWaitForSync() _waitForClear= {}",
                _waitForClear);            
    }

    /**
     * Occupancy of blocks, user halts and aspects of Portal signals may modify
     * normal train speeds.
     * Ramp speed change.
     *
     * @param endSpeedType one of {@link Warrant#Stop}, {@link Warrant#EStop},
     * {@link Warrant#Normal}, or {@link Warrant#Clear}
     */
    protected void rampSpeedTo(String endSpeedType) {
        if (!_waitForClear && _speedType.equals(endSpeedType) && 
                _throttle.getSpeedSetting()==modifySpeed(_normalSpeed, endSpeedType)) {
            return;
        }

        if (endSpeedType!=null) {
            if (endSpeedType.equals(Warrant.Stop) || endSpeedType.equals(Warrant.EStop)) {
                if (_throttle.getSpeedSetting() <= 0) {
                    return;
                }
                // keep train commands halted until speed is restored
                if (endSpeedType.equals(Warrant.EStop)) {
                    setSpeed(-0.1f);
                    return;
                }
            } else {
                if (_halt) {
                    return;
                }
                _speedType = endSpeedType;
                float speedMod = modifySpeed(1.0f, _speedType);
                if (Math.abs(1.0f - speedMod) > .0001f) {
                    _timeRatio = 1.0f / speedMod;
                } else {
                    _timeRatio = 1.0f;
                }
            }
        } else {
            log.error("speedType == null ", new Exception("traceback"));
            return;
        }
        synchronized (this) {
            if (_ramp != null) {
                _ramp.quit();
                _ramp = null;
            }
            _ramp = new ThrottleRamp(endSpeedType);
            Thread t= new Thread(_ramp);
            t.setPriority(Thread.MAX_PRIORITY);
            t.start();
        }
    }
    
    protected boolean ramping() {
        return _speedOverride;
    }

    protected float modifySpeed(float tSpeed, String sType) {
        if (log.isTraceEnabled()) log.trace("modifySpeed speed= "+tSpeed+" for SpeedType= "+sType);
        if (sType.equals(Warrant.Stop)) {
            return 0.0f;
        }
        if (sType.equals(Warrant.EStop)) {
            return -1.0f;
        }
        float throttleSpeed = tSpeed;
        if (sType.equals(Warrant.Normal)) {
            return throttleSpeed;
        }
        float signalSpeed = _speedMap.getSpeed(sType);
        if (log.isTraceEnabled()) log.trace("modifySpeed signalSpeed= "+signalSpeed);

        switch (_speedMap.getInterpretation()) {
            case SignalSpeedMap.PERCENT_NORMAL:
                throttleSpeed *= signalSpeed / 100;      // ratio of normal
                break;
            case SignalSpeedMap.PERCENT_THROTTLE:
                signalSpeed = signalSpeed / 100;            // ratio of full throttle setting
                if (signalSpeed < throttleSpeed) {
                    throttleSpeed = signalSpeed;
                }
                break;

            case SignalSpeedMap.SPEED_MPH:          // miles per hour
                signalSpeed = signalSpeed / _speedMap.getLayoutScale();
                signalSpeed = signalSpeed / 2.2369363f;  // layout track speed mph
                signalSpeed = signalSpeed / getThrottleFactor(throttleSpeed);
                if (signalSpeed < throttleSpeed) {
                    throttleSpeed = signalSpeed;
                }
                break;

            case SignalSpeedMap.SPEED_KMPH:
                signalSpeed = signalSpeed / _speedMap.getLayoutScale();
                signalSpeed = signalSpeed / 3.6f;  // layout track speed mm/ms = kmph
                signalSpeed = signalSpeed / getThrottleFactor(throttleSpeed);
                if (signalSpeed < throttleSpeed) {
                    throttleSpeed = signalSpeed;
                }
                break;
            default:
                log.error("Unknown speed interpretation {}", _speedMap.getInterpretation());
                throw new java.lang.IllegalArgumentException("Unknown speed interpretation " + _speedMap.getInterpretation());
        }
        if (log.isTraceEnabled()) log.trace("modifySpeed: from {}, to {}, speedtype= {} using interpretation {}",
                tSpeed, throttleSpeed, sType, _speedMap.getInterpretation());
        return throttleSpeed;
    }

    protected void setSpeed(float s) {
        if (log.isTraceEnabled()) log.trace("setSpeed("+s+")");
        float speed = s;
        _throttle.setSpeedSetting(speed);
        // Do asynchronously, already within a synchronized block
        ThreadingUtil.runOnLayoutEventually(() -> {
            _warrant.fireRunStatus("SpeedChange", null, _speedType);
        });
        if (log.isDebugEnabled()) log.debug("Speed Set to {}, _speedType={},  _waitForClear= {} _waitForSync= {}, _halt= {}, warrant {}",
                speed, _speedType,  _waitForClear, _waitForSync, _halt, _warrant.getDisplayName());
    }
    
    protected void setSpeedToType(String speedType) {
        if (log.isTraceEnabled()) log.trace("setSpeedToType("+speedType+")");
        float speed = _throttle.getSpeedSetting();
        if (speedType!=null) {
            if (speedType.equals(Warrant.Stop) || speedType.equals(Warrant.EStop)) {
                if (_throttle.getSpeedSetting() <= 0) {
                    return;
                }
                // keep train commands halted until speed is restored
                if (speedType.equals(Warrant.EStop)) {
                    setSpeed(-0.1f);
                    return;
                }
            } else {
                _speedType = speedType;
                float speedMod = modifySpeed(1.0f, _speedType);
                if (Math.abs(1.0f - speedMod) > .0001f) {
                    _timeRatio = 1.0f / speedMod;
                } else {
                    _timeRatio = 1.0f;
                }
            }
            setSpeed(modifySpeed(speed, speedType));
        } else {
            log.error("speedType == null ", new Exception("traceback"));
        }
    }

    protected float getSpeed() {
        return _throttle.getSpeedSetting();
    }

    synchronized public int getRunState() {
        if (_abort) {
            return Warrant.ABORT;
        } else if(_speedOverride) {
            return Warrant.SPEED_RESTRICTED;
        } else if (_halt) {
            return Warrant.HALT;
        } else if (_waitForClear) {
            return Warrant.WAIT_FOR_CLEAR;
        } else if (_waitForSync) {
            return Warrant.WAIT_FOR_TRAIN;
        } else if (_waitForSensor) {
            return Warrant.WAIT_FOR_SENSOR;
        } else if (!_speedType.equals(Warrant.Normal)) {
            return Warrant.SPEED_RESTRICTED;
        } else if (_idxCurrentCommand < 0) {
            return 0;
        }
        return Warrant.RUNNING;
    }

    public String getSpeedRestriction() {
        float curSpeed = _throttle.getSpeedSetting();
        String units;
        float scale = _speedMap.getLayoutScale();
        float speed;
        if (_speedProfile != null) {
            speed = scale * _speedProfile.getSpeed(curSpeed, _throttle.getIsForward()) / 1000;
        } else {
            speed = curSpeed * _speedMap.getDefaultThrottleFactor() * 100;
        }
//        speed = speed * jmri.InstanceManager.getDefault(SignalSpeedMap.class).getLayoutScale();

        if (_speedMap.getInterpretation() == SignalSpeedMap.SPEED_KMPH) {
            units = "Kmph";
            speed = speed * 3.6f;
        } else {
            units = "Mph";
            speed = speed * 2.2369363f;
        }
        return Bundle.getMessage("atSpeed", _speedType, Math.round(speed), units);
    }

    /**
     * Flag from user's control
     * Does an immediate speed change
     * @param halt true if train should halt
     */
    synchronized public void setHalt(boolean halt) {
        if (!halt) {    // resume normal running
            _halt = false;
            if (_atHalt) {
                notify();  // if wait is freed this sets _halt = false;
                if (log.isDebugEnabled()) log.debug("setHalt({}) calls notify()", halt);
            }
            if (!_waitForClear) {
                rampSpeedTo(_speedType);                            
            }
        } else {
            _halt = true;
            if (_ramp != null) {
                _ramp.quit();
                _ramp = null;
            }
            rampSpeedTo(Warrant.Stop);     
        }
        if (log.isDebugEnabled()) log.debug("setHalt({}): _halt= {}, throttle speed= {}, _waitForClear= {}, _waitForSync= {}, warrant {}",
                halt, _halt,  _throttle.getSpeedSetting(), _waitForClear, _waitForSync, _warrant.getDisplayName());
    }
    
    synchronized public void setStop(boolean eStop) {
        _halt = true;
        if (_ramp != null) {
            _ramp.quit();
            _ramp = null;
        }
        if (eStop) {
            setSpeed(-0.1f);                        
        } else {
            setSpeed(0.0f);             
        }
    }

    /**
     * Flag from user to end run
     */
    synchronized public void abort() {
        _abort = true;
        if (_waitSensor != null) {
            _waitSensor.removePropertyChangeListener(this);
        }
        if (_throttle != null) {
            _throttle.setSpeedSetting(-1.0f);
            setSpeed(0.0f);     // prevent creep after EStop - according to Jim Betz
            for (int i = 0; i < 10; i++) {
                setFunction(i, false);
            }
            try {
                InstanceManager.throttleManagerInstance().releaseThrottle(_throttle, _warrant);
            } catch (Exception e) {
                // null pointer catch and maybe other such.
                log.warn("Throttle release and cancel threw: " + e);
            }
        }
        if (log.isDebugEnabled()) log.debug("Abort called. Engineer shut down. warrant {}", _warrant.getDisplayName());
    }

    protected void releaseThrottle() {
        InstanceManager.throttleManagerInstance().releaseThrottle(_throttle, _warrant);
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
    static private void setSensor(String sensorName, String act) {
        String action = act.toUpperCase();
        jmri.Sensor s = InstanceManager.sensorManagerInstance().getSensor(sensorName);
        if (s != null) {
            try {
                if ("ACTIVE".equals(action)) {
                    s.setKnownState(jmri.Sensor.ACTIVE);
                } else if ("INACTIVE".equals(action)) {
                    s.setKnownState(jmri.Sensor.INACTIVE);
                }
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
            if (log.isDebugEnabled()) log.debug("Listen for propertyChange of {}, wait for State= {}", _waitSensor.getDisplayName(), _sensorWaitState);
            // suspend commands until sensor changes state
            synchronized (this) {
                _waitForSensor = true;
                while (_waitForSensor) {
                    try {
/*                        ThreadingUtil.runOnLayout(() -> {
                            _warrant.fireRunStatus("Command", _idxCurrentCommand - 1, _idxCurrentCommand);
                        });*/
                        wait();
                        clearSensor();
                    } catch (InterruptedException ie) {
                        log.error("InterruptedException " + ie);
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
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (log.isDebugEnabled()) log.debug("propertyChange {} new value= {}", 
                evt.getPropertyName(), evt.getNewValue());
        if ((evt.getPropertyName().equals("KnownState")
                && ((Number) evt.getNewValue()).intValue() == _sensorWaitState)) {
            synchronized (this) {
//                if (!_halt && !_waitForClear) {
                    clearSensor();
                    this.notify();

//                }
            }
        }
    }

    /**
     * @param Throttle setting
     */
    private void runWarrant(ThrottleSetting ts) {
        Warrant w = InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class
        ).getWarrant(ts.getBlockName());
        if (w == null) {
            log.warn("Warrant \"{}\" not found.", ts.getBlockName());
            return;
        }
        int num = 0;
        try {
            num = Integer.parseInt(ts.getValue());
        } catch (NumberFormatException nfe) {
            log.error("Could not parse \"" + ts.getValue() + "\". " + nfe);
        }
        if (num == 0) {
            log.info("Warrant \"{}\" completed last launch of \"{}\".",
                     _warrant.getDisplayName(), ts.getBlockName());
            return;
        }
        if (num > 0) {
            num--;
            ts.setValue(Integer.toString(num));
        }
        String msg;
        WarrantTableFrame f = WarrantTableFrame.getInstance();
        if (_warrant.equals(w)) {
            _idxCurrentCommand = 0;
            w.startupWarrant();
            msg = "Launching warrant \"" + _warrant.getDisplayName() + "\" again.";
        } else {
            if (w.getDccAddress().equals(_warrant.getDccAddress())) {
                OBlock block = w.getfirstOrder().getBlock();
                block.deAllocate(_warrant);     // insure w can start
            }
            msg = f.runTrain(w, Warrant.MODE_RUN);
            if (msg != null) {
                w.stopWarrant(true);
            } else {
                msg = "Launching warrant \"" + w.getDisplayName() + 
                        "\" from warrant \"" + _warrant.getDisplayName() + "\".";
            }
        }
        f.setStatusText(msg, java.awt.Color.red, true);
        if (log.isDebugEnabled()) log.debug(msg);
    }

    protected float getDistanceTraveled(float speedSetting, String speedtype, long time) {
        float speed = modifySpeed(speedSetting, speedtype);
        float distance;
        boolean isForward = _throttle.getIsForward();
        if (_speedProfile != null) {
            distance = _speedProfile.getSpeed(speed, isForward) * time / 1000;
        } else {
            distance = (speed * time) * _speedMap.getDefaultThrottleFactor();
        }
        return distance;
    }

    protected long getTimeForDistance(float speed, float distance) {
        boolean isForward = _throttle.getIsForward();
        float time;
        if (_speedProfile != null) {
            time = distance * 1000 / _speedProfile.getSpeed(speed, isForward);
        } else {
            time = distance / (_speedMap.getDefaultThrottleFactor() * speed);
        }
        return (long) time;
    }

    protected float rampLengthForSpeedChange(float curSpeed, String curSpeedType, String toSpeedType) {
        if (curSpeedType.equals(toSpeedType)) {
            return 0.0f;
        }
        float fromSpeed = modifySpeed(curSpeed, curSpeedType);
        float toSpeed = modifySpeed(curSpeed, toSpeedType);
        if (toSpeed > fromSpeed) {
            float tmp = fromSpeed;
            fromSpeed = toSpeed;
            toSpeed = tmp;
        }
        float rampLength = 0.0f;
        float delta = _speedMap.getStepIncrement();
        if (delta <= 0.007f) {
            log.error("SignalSpeedMap StepIncrement is not set correctly.  Check Preferences->Warrants.");
            return 100.0f;
        }
        int time = _speedMap.getStepDelay();
        boolean isForward = _throttle.getIsForward();
        float speed = fromSpeed;
//        int steps = 0;
        while (speed >= toSpeed) {
            float dist;
            if (_speedProfile != null) {
                dist = _speedProfile.getSpeed((speed - delta / 2), isForward) * time / 1000;
            } else {
                dist = (speed - delta / 2) * time * _speedMap.getDefaultThrottleFactor();
            }
            if (dist <= 0.0f) {
                break;
            }
            speed -= delta;
            if (speed >= toSpeed) {
                rampLength += dist;
            } else {
                rampLength += (speed+delta - toSpeed) * dist / delta;
            }
//            steps++;
        }
/*        if (log.isDebugEnabled()) log.debug("rampLengthForSpeedChange()= {} in {}ms for speed= {}, {} to {}, speed= {} using {}",
                rampLength, time*steps, fromSpeed, curSpeedType, toSpeedType, toSpeed,
                (_speedProfile != null ? "SpeedProfile" : "Factor=" + getThrottleFactor(curSpeed)));*/
        return rampLength;
    }

    private float getThrottleFactor(float speedStep) {
        if (_speedProfile != null) {
            return _speedProfile.getSpeed(speedStep, _throttle.getIsForward()) / (speedStep * 1000);
        }
        return _speedMap.getDefaultThrottleFactor();
    }

    // return a boolean so minSpeedType() can return a non-null String if possible
    protected boolean secondGreaterThanFirst(String speed1, String speed2) {
        if (speed1 == null) {
            return false;
        }
        if (speed2 == null) {
            return true;
        }
        float s1 = modifySpeed(1.0f, speed1);
        float s2 = modifySpeed(1.0f, speed2);
        return (s1 < s2);
    }

    protected DccThrottle getThrottle() {
        return _throttle;

    }

    /*
     * *************************************************************************************
     */
    private class ThrottleRamp implements Runnable {

        String endSpeedType;
        boolean stop = false;   // aborts ramping

        ThrottleRamp(String type) {
            endSpeedType = type;
        }

        synchronized void quit() {
            stop = true;
            if (log.isDebugEnabled()) log.debug("ThrottleRamp.quit calls notify()");
            notify();
        }

        @Override
        public void run() {
            _lock.lock();
            _speedOverride = true;
            try {
                float endSpeed = modifySpeed(_normalSpeed, endSpeedType);
                float speed = _throttle.getSpeedSetting();
                float incr = _speedMap.getStepIncrement();
                int delay = _speedMap.getStepDelay();

                if (log.isDebugEnabled()) log.debug("ThrottleRamp for \"{}\". step increment= {} step interval= {}. Ramp {} to {} on warrant {}",
                        endSpeedType, incr, delay, speed, endSpeed, _warrant.getDisplayName());

                if (endSpeed > speed) {
                    synchronized (this) {
                        _waitForClear = false;  // clear the run state, but hold train at _atClear until speed is restored
                        while (speed < endSpeed) {
                            speed += incr;
                            if (speed > endSpeed) { // don't overshoot
                                speed = endSpeed;
                            }
                            setSpeed(speed);
                            try {
                                wait(delay);
                            } catch (InterruptedException ie) {
                                _lock.unlock();
                                log.error("InterruptedException " + ie);
                                stop = true;
                            }
                            if (stop) {
                                break;
                            }
                        }
                    }
                } else {
                    synchronized (this) {
                        while (speed > endSpeed) {
                            speed -= incr;
                            if (speed < endSpeed) { // don't undershoot
                                speed = endSpeed;
                            }
                            setSpeed(speed);
                            try {
                                wait(delay);
                            } catch (InterruptedException ie) {
                                _lock.unlock();
                                log.error("InterruptedException " + ie);
                                stop = true;
                            }
                            if (stop) {
                                break;
                            }
                        }
                    }
                }
                if (stop) {
                    log.info("ThrottleRamp stopped before completion");
                } else if (!endSpeedType.equals(Warrant.Stop) && 
                        !endSpeedType.equals(Warrant.EStop) /*&& speed > 0.0001f */) {
                    setWaitforClear(false); // speed restored                            
                }
            } finally {
                _speedOverride = false;
                _lock.unlock();
            }
            ThreadingUtil.runOnLayout(() -> {
                _warrant.fireRunStatus("Command", _idxCurrentCommand - 1, _idxCurrentCommand);
            });
            if (log.isDebugEnabled()) log.debug("ThrottleRamp complete to \"{}\" _waitForClear= {} _halt= {} on warrant {}",
                    endSpeedType, _waitForClear, _halt, _warrant.getDisplayName());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Engineer.class);
}