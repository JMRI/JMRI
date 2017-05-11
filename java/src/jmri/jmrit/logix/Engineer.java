package jmri.jmrit.logix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
    private int _idxNoSpeedCommand;     // make non-speed commands only untilndex
    private float _normalSpeed = 0;       // current commanded throttle setting (unmodified)
    private String _speedType = Warrant.Normal;    // current speed name
    private long et;    // elapsed time while waiting to do current command
    private float _timeRatio = 1.0f;     // ratio to extend scripted time when speed is modified
    private boolean _abort = false;
    private boolean _halt = false;  // halt/resume from user's control
    private boolean _waitForClear = false;  // waits for signals/occupancy/allocation to clear
    private boolean _waitForSync = false;  // waits for train to catch up to commands
    private boolean _waitForSensor = false; // wait for sensor event
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
    private static float SCALE_FACTOR = 65; // With _scale, gives a rough first correction for track speed

    Engineer(Warrant warrant, DccThrottle throttle) {
        _warrant = warrant;
        _idxCurrentCommand = 0;
        _idxNoSpeedCommand = -1;
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
    @SuppressFBWarnings(value="UW_UNCOND_WAIT", justification="waits may be indefinite until satisfied or thread aborted")
    public void run() {
        if (log.isDebugEnabled()) log.debug("Engineer started warrant {} _throttle= {}", 
                _warrant.getDisplayName(), _throttle.getClass().getName());

        cmdBlockIdx = 0;
        while (_idxCurrentCommand < _warrant._commands.size()) {
            et = System.currentTimeMillis();
            ThrottleSetting ts = _warrant._commands.get(_idxCurrentCommand);
            _runOnET = _setRunOnET;     // OK to set here
            long time = ts.getTime();
            synchronized (this) {
                if (_abort) {
                    break;
                }
                if (getSpeed() > 0.0f) {
                    time = (long)(time*_timeRatio); // extend et when speed has been modified from scripted speed
                }
            }
            String command = ts.getCommand().toUpperCase();
            if (log.isDebugEnabled()) log.debug("Start Cmd #{} for block \"{}\" currently in \"{}\". wait {}ms to do cmd {}. Warrant {}",
                    _idxCurrentCommand+1, ts.getBeanDisplayName(), _warrant.getCurrentBlockName(), time, command, _warrant.getDisplayName());
            if (!"SET SENSOR".equals(command) && !"WAIT SENSOR".equals(command) && !"RUN WARRANT".equals(command)) {
                int idx = _warrant.getIndexOfBlock(ts.getBeanDisplayName(), cmdBlockIdx);
                if (idx >= 0) {
                    cmdBlockIdx = idx;
                }                
            }
            if (cmdBlockIdx < _warrant.getCurrentOrderIndex() || (command.equals("NOOP") && (cmdBlockIdx <= _warrant.getCurrentOrderIndex()))) {
                // Train advancing too fast, need to process commands more quickly,
                // allowing half second for whistle toots etc.
                if (log.isDebugEnabled()) log.debug("Train reached block \"{}\" before et={}ms . Warrant {}", 
                        ts.getBeanDisplayName(), time, _warrant.getDisplayName());
                time = Math.min(time, 250); // 1/4 sec per command should be enough for toots etc.
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
                    log.error("At time wait {}", ie.toString());
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
                // will test these indexes again and can trigger a notifyAll() to free the wait
                if (log.isDebugEnabled()) log.debug("Wait for train to enter \"{}\". Warrant {}",
                        _warrant.getBlockAt(_syncIdx).getDisplayName(), _warrant.getDisplayName());
                ThreadingUtil.runOnLayoutEventually(() -> {
                    _warrant.fireRunStatus("Command", _idxCurrentCommand - 1, _idxCurrentCommand);
                });
                synchronized (this) {
                    try {
                        _waitForSync = true;
                        wait();
                    } catch (InterruptedException ie) {
                        log.error("At _waitForSync {}", ie.toString());
                        Thread.currentThread().interrupt();
                    }
                    finally {
                        _waitForSync = false;
                    }
                    if (_abort) {
                        break;
                    }
                }
            }

            synchronized (this) {
                // block position and elapsed time are as expected, but track conditions 
                // such as signals, occupancy  may required waiting
                if (_waitForClear) {
                    if (log.isDebugEnabled()) log.debug("Waiting for clearance. _waitForClear= {} _halt= {} \"{}\".  Warrant {}",
                            _waitForClear, _halt, _warrant.getBlockAt(cmdBlockIdx).getDisplayName(), _warrant.getDisplayName());
                    try {
                        _atClear = true;
                        wait();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("At _atClear {}" + ie.toString());
                    }
                    finally {
                        _waitForClear = false;
                        _atClear = false;
                    }
                    if (_abort) {
                        break;
                    }
                }
            }
                
            synchronized (this) {
                // user's command to halt requires waiting
                if (_halt) {
                    if (log.isDebugEnabled()) log.debug("Waiting to Resume. _halt= {}, _waitForClear= {}, Block \"{}\".  Warrant {}",
                            _halt, _waitForClear, _warrant.getBlockAt(cmdBlockIdx).getDisplayName(), _warrant.getDisplayName());
                    try {
                        _atHalt = true;
                        wait();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("At _atHalt {}", ie.toString());
                    }
                    finally {
                        _halt = false;
                        _atHalt = false;
                    }
                    if (_abort) {
                        break;
                    }
                }
            }

            try {
                if (command.equals("SPEED")) {
                    synchronized (this) {
                        if (!_halt && !_waitForClear) {
                            float speed = Float.parseFloat(ts.getValue());
                            _normalSpeed = speed;
                            float speedMod = modifySpeed(speed, _speedType);
                            if (Math.abs(speed - speedMod) > .0001f) {
                                _timeRatio = speed / speedMod;
                            } else {
                                _timeRatio = 1.0f;
                            }
                            setSpeed(speedMod);
                        }
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
                    setSensor(ts.getBeanSystemName(), ts.getValue());
                } else if (command.equals("WAIT SENSOR")) {
                    getSensor(ts.getBeanSystemName(), ts.getValue());
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
            } catch (NumberFormatException nfe) {
                log.error("Command failed! {} {}", ts.toString(), nfe.toString());
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

    /**
     * Delayed ramp has started.
     * Currently informational only  
     * Do non-speed commands only until idx is reached?  maybe not.
     * @param idx index
     */
    protected void advanceToCommandIndex(int idx) {
//        _idxNoSpeedCommand = idx;
//        if (log.isDebugEnabled()) log.debug("_idxCurrentCommand= {} _normalSpeed= {} _speedType= {} speed= {}",
//                _idxCurrentCommand, _normalSpeed, _speedType, _throttle.getSpeedSetting());
    }

    private void setSpeedStepMode(int step) {
        int stepMode = DccThrottle.SpeedStepMode128;
        switch (step) {
            case 14:
                stepMode = DccThrottle.SpeedStepMode14;
                break;
            case 27:
                stepMode = DccThrottle.SpeedStepMode27;
                break;
            case 28:
                stepMode = DccThrottle.SpeedStepMode28;
                break;
            case 128:
                stepMode = DccThrottle.SpeedStepMode128;
                break;
            case DccThrottle.SpeedStepMode28Mot:
                stepMode = DccThrottle.SpeedStepMode28Mot;
                break;
            default:
        }
        _throttle.setSpeedStepMode(stepMode);
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

    /**
     * If waiting to sync entrance to a block boundary with recorded wait time,
     * or waiting for clearance ahead for rogue occupancy, stop aspect or
     * sharing of turnouts, this call will free the wait.
     */
    synchronized protected void clearWaitForSync() {
        if (_waitForSync) {
            if (log.isDebugEnabled()) log.debug("clearWaitForSync calls notifyAll()");
            notifyAll();   // if wait is cleared, this sets _waitForSync= false
        }
        if (log.isDebugEnabled()) log.debug("clearWaitForSync() _waitForClear= {}",
                _waitForClear);            
    }
    
    /**
     * Occupancy of blocks, user halts and aspects of Portal signals will modify
     * normal scripted train speeds.
     * Ramp speed change for smooth prototypical look.
     *
     * @param endSpeedType one of {@link Warrant#Stop}, {@link Warrant#EStop},
     * {@link Warrant#Normal}, or {@link Warrant#Clear}
     */
    protected void rampSpeedTo(String endSpeedType) {
        if (!setSpeedRatio(endSpeedType)) {
            return;
        }
        if (log.isDebugEnabled()) log.debug("rampSpeedTo {}. warrant {}",
                endSpeedType, _warrant.getDisplayName());
        
        synchronized (this) {
            _ramp = new ThrottleRamp(endSpeedType);
            Thread t= new Thread(_ramp);
            t.setPriority(Thread.MAX_PRIORITY);
            t.start();               
        }
    }
    
    protected void cancelRamp() {
        if (_ramp != null) {
            _ramp.quit();
            _ramp = null;
        }        
    }

    /**
     * Modify a current or commanded throttle setting to a throttle setting constrained
     * to a named speed restriction
     * @param tSpeed throttle setting
     * @param sType named speed restriction
     * @return modified throttle setting
     */
    protected float modifySpeed(float tSpeed, String sType) {
        if (log.isTraceEnabled()) log.trace("modifySpeed speed= {} for SpeedType= \"{}\"", tSpeed, sType);
        if (sType.equals(Warrant.Stop)) {
            return 0.0f;
        }
        if (sType.equals(Warrant.EStop)) {
            return -1.0f;
        }
        float throttleSpeed = tSpeed;       // throttleSpeed is a throttle setting
        if (sType.equals(Warrant.Normal)) {
            return throttleSpeed;
        }
        float signalSpeed = _speedMap.getSpeed(sType);

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

            case SignalSpeedMap.SPEED_MPH:          // convert miles per hour to track speed
                signalSpeed = signalSpeed / _speedMap.getLayoutScale();
                signalSpeed = signalSpeed / 2.2369363f;  // layout track speed mph as mm/ms
                float trackSpeed = getTrackSpeed(throttleSpeed);
                if (signalSpeed < trackSpeed) {
                    throttleSpeed = getThrottleSetting(signalSpeed);
                }
                break;

            case SignalSpeedMap.SPEED_KMPH:
                signalSpeed = signalSpeed / _speedMap.getLayoutScale();
                signalSpeed = signalSpeed / 3.6f;  // layout track speed mm/ms for kmph
                trackSpeed = getTrackSpeed(throttleSpeed);
                if (signalSpeed < trackSpeed) {
                    throttleSpeed = getThrottleSetting(signalSpeed);
                }
                break;
            default:
                log.error("Unknown speed interpretation {}", _speedMap.getInterpretation());
                throw new java.lang.IllegalArgumentException("Unknown speed interpretation " + _speedMap.getInterpretation());
        }
        if (log.isTraceEnabled()) log.trace("modifySpeed: from {}, to {}, signalSpeed= {} using interpretation {}",
                tSpeed, throttleSpeed, signalSpeed, _speedMap.getInterpretation());
        return throttleSpeed;
    }

    synchronized protected void setSpeed(float s) {
        if (log.isTraceEnabled()) log.trace("setSpeed({})", s);
        float speed = s;
        _throttle.setSpeedSetting(speed);
        // Do asynchronously, already within a synchronized block
        ThreadingUtil.runOnLayoutEventually(() -> {
            _warrant.fireRunStatus("SpeedChange", null, _speedType);
        });
        if (log.isDebugEnabled()) log.debug("Speed Set to {}, _speedType={},  _waitForClear= {} _waitForSync= {}, _halt= {}, warrant {}",
                speed, _speedType,  _waitForClear, _waitForSync, _halt, _warrant.getDisplayName());
    }
    
    synchronized protected float getSpeed() {
        float speed = _throttle.getSpeedSetting();
        if (speed < 0.0) {
            speed = 0.0f;
            _throttle.setSpeedSetting(speed);
        }
        return speed;
    }

    /**
     * Utility for unscripted speed changes.
     * Records current type and sets time ratio.
     * EStop is set immediately (do not ramp)
     * @param speedType name of speed change type
     * @return true to continue, false to return
     */
    private boolean setSpeedRatio(String speedType) {
        if (speedType == null) {
            return false;
        }
        Float speed = getSpeed();
        if (Math.abs(speed - modifySpeed(_normalSpeed, speedType)) < 0.0001f) {
            // already at speed, no need to reset throttle
            _speedType = speedType;
            return false;
        }
        
        cancelRamp();

        synchronized (this) {
            if (speedType.equals(Warrant.EStop)) {
                setSpeed(-0.1f);        // always do immediate EStop
                return false;
            } else if (speedType.equals(Warrant.Stop)) {
                return true;
            } else {
                _speedType = speedType;
                float speedMod = modifySpeed(1.0f, _speedType);
                if (Math.abs(1.0f - speedMod) > .0001f) {
                    _timeRatio = 1.0f / speedMod;
                } else {
                    _timeRatio = 1.0f;
                }
            }            
        }
        return true;
    }

    protected void setSpeedToType(String speedType) {
        if (log.isTraceEnabled()) log.trace("setSpeedToType({})", speedType);
        if (!setSpeedRatio(speedType)) {
            return;
        }
        if (speedType.equals(Warrant.Stop)) {
            setSpeed(0.0f);                    
            return;
        }
        setSpeed(modifySpeed(_normalSpeed, speedType));                
    }
    
    protected float getExpectedSpeed(String speedType) {
        return modifySpeed(_normalSpeed, speedType);
    }

    /**
     * Smooth ramped stop (or resume speed) command from Warrant.controlRunTrain()
     * User's override of throttle script.
     * @param halt true if train should halt
     */
    synchronized public void setHalt(boolean halt) {
        if (!halt) {    // resume normal running
            _halt = false;
            if (_atHalt) {
                notifyAll();
                if (log.isDebugEnabled()) log.debug("setHalt({}) calls notifyAll()", halt);
            }
        } else {
            _halt = true;
            rampSpeedTo(Warrant.Stop);     
        }
        if (log.isDebugEnabled()) log.debug("setHalt({}): _halt= {}, throttle speed= {}, _waitForClear= {}, _waitForSync= {}, warrant {}",
                halt, _halt,  _throttle.getSpeedSetting(), _waitForClear, _waitForSync, _warrant.getDisplayName());
    }
    
    /**
     * Smooth ramped stop (or resume speed) command from Warrant 
     * signal or occupation stopping condition ahead. 
     * Track condition override of throttle script.
     * @param stop true if train should halt
     */
    synchronized protected void setWaitforClear(boolean stop) {
        if (!stop) {    // resume normal running
            _waitForClear = false;
            if (_atClear) {
                notifyAll();
                if (log.isDebugEnabled()) log.debug("setWaitforClear({}) calls notifyAll()", stop);
            }
        } else {
            _waitForClear = true;
            rampSpeedTo(Warrant.Stop);     
        }
        if (log.isDebugEnabled()) log.debug("setWaitforClear({}): _halt= {}, throttle speed= {}, _waitForClear= {}, _waitForSync= {}, warrant {}",
                stop, _halt,  _throttle.getSpeedSetting(), _waitForClear, _waitForSync, _warrant.getDisplayName());
    }
    

    /**
     * Immediate stop command from Warrant.controlRunTrain()
     * Do not ramp.
     * @param eStop true for emergency stop
     */
    synchronized public void setStop(boolean eStop) {
        _halt = true;
        cancelRamp();
        if (eStop) {
            setSpeed(-0.1f);                        
        } else {
            setSpeed(0.0f);             
        }
    }

    synchronized public int getRunState() {
        if (_halt) {
            return Warrant.HALT;
        } else if (_waitForClear) {
            return Warrant.WAIT_FOR_CLEAR;
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

    /**
     * Calculates the scale speed of the current throttle setting for display
     * @return text message
     */
    @SuppressFBWarnings(value="IS2_INCONSISTENT_SYNC", justification="speed type name in message is ok")
    public String getSpeedMessage() {
        float curSpeed = getSpeed();
        float speed = getTrackSpeed(curSpeed) * _speedMap.getLayoutScale();

        String units;
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
     * Flag from user to end run
     */
    synchronized public void abort() {
        _abort = true;
        cancelRamp();
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
                        ThreadingUtil.runOnLayoutEventually(() -> {
                            _warrant.fireRunStatus("Command", _idxCurrentCommand - 1, _idxCurrentCommand);
                        });
                        wait();
                        clearSensor();
                    } catch (InterruptedException ie) {
                        log.error("Engineer interrupted at _waitForSensor " + ie);
                        break;
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
                if (!_halt && !_waitForClear) {
                    clearSensor();
                    this.notifyAll();

                }
            }
        }
    }

    /**
     * @param Throttle setting
     */
    private void runWarrant(ThrottleSetting ts) {
        Warrant w =  (Warrant)ts.getNamedBeanHandle().getBean();
        if (w == null) {
            log.warn("Warrant \"{}\" not found.", ts.getBeanDisplayName());
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
                     _warrant.getDisplayName(), ts.getBeanDisplayName());
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

    // todo replace generic factors with factors specific to locomotive for next 3 methods.
    // Unless great care has been taken to match speeds of locos and their speed curves,
    // _speedMap.getDefaultThrottleFactor() is a very unreliable parameter to
    // calculate times or distances.  A speedProfile per loco is needed.
    protected float getDistanceTraveled(float speedSetting, String speedtype, long time) {
        float speed = modifySpeed(speedSetting, speedtype);
        float distance;
        boolean isForward = _throttle.getIsForward();
        if (_speedProfile != null) {
            distance = _speedProfile.getSpeed(speed, isForward) * time / 1000;
        } else {
            float distanceFactor = _speedMap.getDefaultThrottleFactor() * SCALE_FACTOR / _speedMap.getLayoutScale();
            distance = speed * time * distanceFactor;
        }
        if (log.isTraceEnabled()) log.trace("getDistanceTraveled = {} in time={}ms from speedSetting= {}, type {} by {}",
                distance, time, speedSetting, speedtype,
                (_speedProfile != null ? "SpeedProfile" : "Factor=" + _speedMap.getDefaultThrottleFactor()));
        return distance;
    }

    protected long getTimeForDistance(float speed, float distance) {
        boolean isForward = _throttle.getIsForward();
        float time;
        if (_speedProfile != null) {
            time = distance * 1000 / _speedProfile.getSpeed(speed, isForward);
        } else {
            float distanceFactor = _speedMap.getDefaultThrottleFactor() * SCALE_FACTOR / _speedMap.getLayoutScale();
            time = distance / (distanceFactor * speed);
        }
        if (log.isTraceEnabled()) log.trace("getTimeForDistance = {}ms from speedSetting= {} in distance {} by {}",
                time, speed, distance, 
                (_speedProfile != null ? "SpeedProfile" : "Factor=" + _speedMap.getDefaultThrottleFactor()));
        return (long) time;
    }

    protected RampData rampLengthForSpeedChange(float curSpeed, String curSpeedType, String toSpeedType) {
        if (curSpeedType.equals(toSpeedType)) {
            return new RampData(0.0f, 0);
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
        int incr = _speedMap.getStepDelay();
        boolean isForward = _throttle.getIsForward();
        float speed = fromSpeed;
        int steps = 0;
        while (speed >= toSpeed) {
            float dist;
            if (_speedProfile != null) {
                dist = _speedProfile.getSpeed((speed - delta / 2), isForward) * incr / 1000;
            } else {
                float distanceFactor = _speedMap.getDefaultThrottleFactor() * SCALE_FACTOR / _speedMap.getLayoutScale();
                dist = (speed - delta / 2) * incr * distanceFactor;
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
            steps++;
        }
        int rampTime = incr*steps;
        if (log.isTraceEnabled()) log.trace("rampLengthForSpeedChange()= {} in {}ms for speed= {}, {} to {}, speed= {} using {}",
                rampLength, rampTime, fromSpeed, curSpeedType, toSpeedType, toSpeed,
                (_speedProfile != null ? "SpeedProfile" : "Factor=" + _speedMap.getDefaultThrottleFactor()));
        return new RampData(rampLength, rampTime);   // add 1cm for safety (all scales)
    }

    // return millimeters per millisecond (= meters/sec)
    private float getTrackSpeed(float throttleSetting) {
        if (_speedProfile != null) {
            return _speedProfile.getSpeed(throttleSetting, _throttle.getIsForward()) / 1000;
        }
        return throttleSetting *_speedMap.getDefaultThrottleFactor() * SCALE_FACTOR / _speedMap.getLayoutScale();
    }

    private float getThrottleSetting(float trackSpeed) {
        float setting;
        if (_speedProfile != null) {
            setting = _speedProfile.getThrottleSetting(trackSpeed, _throttle.getIsForward());
        } else {
            setting = trackSpeed * _speedMap.getLayoutScale() / (SCALE_FACTOR *_speedMap.getDefaultThrottleFactor());            
        }
        return setting;
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
            if (log.isDebugEnabled()) log.debug("ThrottleRamp.quit calls notifyAll()");
            notifyAll();
        }

        @Override
        public void run() {
            // the time 'right now' is at having done _idxCurrentCommand-1 and is waiting 
            // to do _idxCurrentCommand.  A non-scripted speed change is to begin now.
            float endSpeed = modifySpeed(_normalSpeed, endSpeedType);   // requested endspeed
            float speed = getSpeed();
            float incr = _speedMap.getStepIncrement();
            int delay = _speedMap.getStepDelay();
            if (log.isDebugEnabled()) log.debug("Current expected throttleSpeed= {}, actual throttleSpeed= {}",
                    modifySpeed(_normalSpeed, _speedType), speed);
            // endSpeed should not exceed scripted speed modified by _speedType
            float scriptSpeed = 0.0f;
            for (int idx = 0; idx < _idxCurrentCommand; idx++) {
                ThrottleSetting ts = _warrant._commands.get(idx);
                if ("SPEED".equals(ts.getCommand().toUpperCase())) {
                    scriptSpeed = Float.parseFloat(ts.getValue());
                }
            }
            // this assumes ramp will end waiting on the current command
            endSpeed = Math.min(modifySpeed(scriptSpeed, _speedType), endSpeed);
           
            try {
                _lock.lock();
                if (log.isDebugEnabled()) log.debug("ThrottleRamp for \"{}\". step increment= {} step interval= {}. Ramp {} to {} on warrant {}",
                        endSpeedType, incr, delay, speed, endSpeed, _warrant.getDisplayName());

                if (endSpeed > speed) {
                    synchronized (this) {
                        while (speed < endSpeed) {
                            speed += incr;
                            if (speed > endSpeed) { // don't overshoot
                                speed = endSpeed;
                            }
                            setSpeed(speed);
                            try {
                                wait(delay);
                            } catch (InterruptedException ie) {
//                                _lock.unlock();
                                log.error("ThrottleRamp interrupted " + ie);
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
//                                _lock.unlock();
                                log.error("ThrottleRamp interrupted " + ie);
                                stop = true;
                            }
                            if (stop) {
                                break;
                            }
                        }
                    }
                }
                if (!endSpeedType.equals(Warrant.Stop) && 
                        !endSpeedType.equals(Warrant.EStop) /*&& speed > 0.0001f */) {
                    synchronized (this) {
                        // speed restored, clear any stop waits
                        if (_waitForClear) {
                            setWaitforClear(false);                            
                        }
                        if (_halt) {
                            setHalt(false);
                        }                        
                    }
                 }
            } finally {
                _lock.unlock();
            }
            synchronized (this) {
                if (stop) {
                    log.info("ThrottleRamp stopped before completion");
                }
            }
            ThreadingUtil.runOnLayoutEventually(() -> {
                _warrant.fireRunStatus("Command", _idxCurrentCommand - 1, _idxCurrentCommand);
            });
            if (log.isDebugEnabled())
                log.debug("ThrottleRamp {} for \"{}\" _waitForClear= {} _halt= {} on warrant {}",
                        (stop?"stopped":"completed"), endSpeedType, _waitForClear, _halt, _warrant.getDisplayName());
        }
    }
    
    public static class RampData {
        float _length;
        int _time;
        RampData(float length, int time) {
            _length = length;
            _time = time;
        }
        
        float getLength() {
            return _length;
        }
        
        int getTime() {
            return _time;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Engineer.class);
}
