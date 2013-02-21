package jmri.jmrit.logix;

import org.apache.log4j.Logger;
import java.util.concurrent.locks.ReentrantLock;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Sensor;

/**
 * Execute a throttle command script for a warrant
 *
 * @version $Revision$
 * @author	Pete Cressman  Copyright (C) 2009, 2010, 2011
 */
 
    /************************** Thread running the train *****************/

public class Engineer extends Thread implements Runnable, java.beans.PropertyChangeListener {

    private int     _idxCurrentCommand;     // current throttle command
    private float   _speed;
    private String  _speedType = "Normal";
    private float   _minSpeed = 1.0f/127;
    private boolean _abort = false;
    private boolean _halt = false;  // halt/resume from user's control
    private boolean _waitForClear = false;  // waits for signals/occupancy/allocation to clear
    private boolean _waitForSync = false;  // waits for train to catch up to commands
    private boolean _waitForSensor = false; // wait for sensor event
    private boolean _speedOverride = false; // speed changing due to signal or occupancy
    private int     _syncIdx;			// block order index of current command
    private DccThrottle _throttle;
    private Warrant _warrant;
    private Sensor 	_waitSensor;
    private int		_sensorWaitState;

    final ReentrantLock _lock = new ReentrantLock();

    Engineer(Warrant warrant, DccThrottle throttle) {
        _warrant = warrant;
        _idxCurrentCommand = -1;
        _throttle = throttle;
        _syncIdx = 0;
        setSpeedStepMode(_throttle.getSpeedStepMode());
    }

    public void run() {
        if (log.isDebugEnabled()) log.debug("Engineer started warrant "+_warrant.getDisplayName());

        int cmdBlockIdx = 0;
        while (_idxCurrentCommand+1 < _warrant._commands.size()) {
            long et = System.currentTimeMillis();
            ThrottleSetting ts = _warrant._commands.get(_idxCurrentCommand+1);
            int idx = _warrant.getIndexOfBlock(ts.getBlockName(), cmdBlockIdx);
            if (idx>=0) {
            	cmdBlockIdx = idx;
            }
            long time = ts.getTime();
            if (!_warrant._tempRunBlind && cmdBlockIdx < _warrant._idxCurrentOrder) {
            	// Train advancing too fast, need to process commands more quickly,
            	// allowing half second for whistle toots etc.
            	time = Math.min(time, 500);
            }
            String command = ts.getCommand().toUpperCase();
            // actual playback total elapsed time is "ts.getTime()" before record time.
            // current block at playback may also be before current block at record
            synchronized(this) {
                if (_abort) { break; }
                try {
                    if (time > 0) {
                        wait(time);
                    }
                    if (_abort) { break; }
                    //if (!command.equals("SET SENSOR") && !command.equals("WAIT SENSOR")
                    //			&& !command.equals("RUN WARRANT")) {
                        _syncIdx = cmdBlockIdx;
                        // Having waited, time=ts.getTime(), so blocks should agree.  if not,
                        // wait for train to arrive at block and send sync notification.
                        // note, blind runs cannot detect entrance.
                        if (!_warrant._tempRunBlind && _syncIdx > _warrant._idxCurrentOrder) {
                            // commands are ahead of current train position 
                            if (log.isDebugEnabled()) log.debug("Command Block \""+ts.getBlockName()+
                                                      "\" wait for train to enter. "+_warrant.getDisplayName());
                            _waitForSync = true;
                            _warrant.fireRunStatus("Command", Integer.valueOf(_idxCurrentCommand), Integer.valueOf(_idxCurrentCommand+1));
                            wait();
                        }
                    //}
                } catch (InterruptedException ie) {
                    log.error("InterruptedException "+ie);
                } catch (java.lang.IllegalArgumentException iae) {
                    log.error("IllegalArgumentException "+iae);
                }

                _waitForSensor = false;
                _waitForSync = false;
                if (_abort) { break; }

                _idxCurrentCommand++;
                try {
                    if (_waitForClear || _halt) {
                        wait();
                    }
                } catch (InterruptedException ie) {
                    log.error("InterruptedException "+ie);
                }
                if (_abort) { break; }
            }

            try {
                if (command.equals("SPEED")) {
                    float speed = Float.parseFloat(ts.getValue());
                    _lock.lock();
                    try {
                        setSpeed(modifySpeed(speed, _speedType));
                    } finally {
                      _lock.unlock();
                    }
                } else if (command.equals("SPEEDSTEP")) {
                    int step = Integer.parseInt(ts.getValue());
                    setStep(step);
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
                } else if (command.equals("RUN WARRANT")) {
                    runWarrant(ts);
                }
                _warrant.fireRunStatus("Command", Integer.valueOf(_idxCurrentCommand), Integer.valueOf(_idxCurrentCommand+1));
                et = System.currentTimeMillis()-et;
                if (log.isDebugEnabled()) log.debug("Cmd #"+(_idxCurrentCommand+1)+": "+
                                                    ts.toString()+" et= "+et+" warrant "+_warrant.getDisplayName());
            } catch (NumberFormatException e) {
                  log.error("Command failed! "+ts.toString()+" - "+e);
            }
         }
        // shut down
        abort();
    }

    private void setStep(int step) {
        setSpeedStepMode(step);
        _throttle.setSpeedStepMode(step);
    }

    private void setSpeedStepMode(int step) {
        switch (step) {
            case DccThrottle.SpeedStepMode14:
                _minSpeed = 1.0f/15;
                break;
            case DccThrottle.SpeedStepMode27:
                _minSpeed = 1.0f/28;
                break;
            case DccThrottle.SpeedStepMode28:
                _minSpeed = 1.0f/29;
                break;
            default:
                _minSpeed = 1.0f/127;
                break;
        }
    }

    public int getCurrentCommandIndex() {
        return _idxCurrentCommand;
    }

    /**
    * If waiting to sync entrance to a block boundary with record time,
    * this call will free the wait.
    * @param  block train has just entered.
    */
    synchronized public void synchNotify(OBlock block) {
//        if (!_halt && !_wait) {
        if (!_halt) {
            if (_syncIdx <= _warrant._idxCurrentOrder) { 
                this.notify();
            }
        }
        if (log.isDebugEnabled()) {
            if (block!=null) log.debug("synchNotify from block "+block.getDisplayName()+
    				" _waitForClear= "+_waitForClear+" for warrant "+_warrant.getDisplayName());
            else  log.debug("synchNotify: _waitForSync= "+_waitForSync+
    				" _waitForClear= "+_waitForClear+" for warrant "+_warrant.getDisplayName());       	
        }
    }

    /**
    * Occupancy of blocks and aspects of Portal signals may modify normal train speed
    * Ramp speed change.
    */
    synchronized public void rampSpeedTo(String endSpeedType, long waitTime) {
        if (_speedType.equals(endSpeedType)) {
            return;
        }
        ThrottleRamp ramp = new ThrottleRamp(endSpeedType, waitTime);
        new Thread(ramp).start();
    }

    /**
    * Get the last normal speed setting.  Regress through commends, if necessary.  
    */
    private float getLastSpeedCommand(int currentIndex) {
        float speed = 0.0f;
        if (currentIndex<0) {
            return speed;
        }
        ThrottleSetting ts = _warrant._commands.get(currentIndex);
        String command = ts.getCommand().toUpperCase();
        try {
            if (command.equals("SPEED")) {
                speed = Float.parseFloat(ts.getValue());
            }
            int idx = currentIndex;
            while (!command.equals("SPEED") && idx>0) {
                idx--;
                ts = _warrant._commands.get(idx);
                command = ts.getCommand().toUpperCase();
                if (command.equals("SPEED")) {
                    speed = Float.parseFloat(ts.getValue());
                }
            }
            if (log.isDebugEnabled()) log.debug("getLastSpeedCommand: speed= "+speed+", from Command #"+idx+
            		" warrant "+_warrant.getDisplayName());
        } catch (NumberFormatException nfe) {
              log.warn(ts.toString()+" - "+nfe);
        }
        return speed;
    }

    private float modifySpeed(float s, String sType) {
        jmri.implementation.SignalSpeedMap map = Warrant.getSpeedMap();
        float speed = map.getSpeed(sType)/100;

        if (map.isRatioOfNormalSpeed()) {
            speed *= s;
        } else { // max speed specified by signal aspect
        	// use the minimum of recorded speed and aspect specified speed
        	if (s<speed) {
        		speed = s;
        	}
        }
        speed *= _warrant._throttleFactor;
        if (0.0f < speed && speed < _minSpeed) {
            speed = 0.0f;
        }
        return speed;
    }

    private void setSpeed(float speed) {
        _speed = speed;
        _throttle.setSpeedSetting(_speed);
        if (log.isDebugEnabled()) log.debug("_speedType="+_speedType+", Speed set to "+
        		_speed+" _waitForClear= "+_waitForClear+" warrant "+_warrant.getDisplayName());
    }

    synchronized public int getRunState() {
        if (_abort) {
            return Warrant.ABORT;
        } else if (_waitForClear) {
            return Warrant.WAIT_FOR_CLEAR;
        } else if (_waitForSync) {
            return Warrant.WAIT_FOR_TRAIN;
        } else if (_waitForSensor) {
            return Warrant.WAIT_FOR_SENSOR;
        } else  if (_halt) {
            return Warrant.HALT;
        } else if (!_speedType.equals("Normal")) {
            return Warrant.SPEED_RESTRICTED;
        }
        return Warrant.RUNNING;
    }

    public String getSpeedRestriction() {
        if (_speedOverride) {
            return "Change to "+_speedType;
        }
        else if (_speed == 0.0f) {
            return "At Stop";
        } else {
            return _speedType;
        }
    }

    /**
    * Flag from user's control
    */
    synchronized public void setHalt(boolean halt) {
        _halt = halt;
        if (!_halt) { 
            _lock.lock();
            try {
                setSpeed(modifySpeed(getLastSpeedCommand(_idxCurrentCommand), _speedType));
                if (!_waitForClear) {
                    this.notify();
                }
            } finally {
                _lock.unlock();
            }
        } else {
            _throttle.setSpeedSetting(0.0f);
        }
        if (log.isDebugEnabled()) log.debug("setHalt("+halt+"): throttle speed= "+_throttle.getSpeedSetting()+
                					" _waitForClear= "+_waitForClear+" warrant "+_warrant.getDisplayName());
    }

    /**
    * Flag from user to end run
    */
    synchronized public void abort() {
        _abort = true;
        if (_throttle != null) {
            _throttle.setSpeedSetting(-1.0f);
            _throttle.setSpeedSetting(0.0f);
            try {
                InstanceManager.throttleManagerInstance().releaseThrottle(_throttle, _warrant);
            } catch (Exception e) {
                // null pointer catch and maybe other such.
                log.warn("Throttle release and cancel threw: "+e);
            }
        }
        _warrant.setRunMode(Warrant.MODE_NONE, null, null, null, false);
        if (log.isDebugEnabled()) log.debug("Engineer shut down. warrant "+_warrant.getDisplayName());
    }

    private void setFunction(int cmdNum, boolean isSet) {
        switch (cmdNum)
        {
            case 0: _throttle.setF0(isSet); break;
            case 1: _throttle.setF1(isSet); break;
            case 2: _throttle.setF2(isSet); break;
            case 3: _throttle.setF3(isSet); break;
            case 4: _throttle.setF4(isSet); break;
            case 5: _throttle.setF5(isSet); break;
            case 6: _throttle.setF6(isSet); break;
            case 7: _throttle.setF7(isSet); break;
            case 8: _throttle.setF8(isSet); break;
            case 9: _throttle.setF9(isSet); break;
            case 10: _throttle.setF10(isSet); break;
            case 11: _throttle.setF11(isSet); break;
            case 12: _throttle.setF12(isSet); break;
            case 13: _throttle.setF13(isSet); break;
            case 14: _throttle.setF14(isSet); break;
            case 15: _throttle.setF15(isSet); break;
            case 16: _throttle.setF16(isSet); break;
            case 17: _throttle.setF17(isSet); break;
            case 18: _throttle.setF18(isSet); break;
            case 19: _throttle.setF19(isSet); break;
            case 20: _throttle.setF20(isSet); break;
            case 21: _throttle.setF21(isSet); break;
            case 22: _throttle.setF22(isSet); break;
            case 23: _throttle.setF23(isSet); break;
            case 24: _throttle.setF24(isSet); break;
            case 25: _throttle.setF25(isSet); break;
            case 26: _throttle.setF26(isSet); break;
            case 27: _throttle.setF27(isSet); break;
            case 28: _throttle.setF28(isSet); break;
        }
    }

    private void setLockFunction(int cmdNum, boolean isTrue) {
        switch (cmdNum)
        {
            case 0: _throttle.setF0Momentary(!isTrue); break;
            case 1: _throttle.setF1Momentary(!isTrue); break;
            case 2: _throttle.setF2Momentary(!isTrue); break;
            case 3: _throttle.setF3Momentary(!isTrue); break;
            case 4: _throttle.setF4Momentary(!isTrue); break;
            case 5: _throttle.setF5Momentary(!isTrue); break;
            case 6: _throttle.setF6Momentary(!isTrue); break;
            case 7: _throttle.setF7Momentary(!isTrue); break;
            case 8: _throttle.setF8Momentary(!isTrue); break;
            case 9: _throttle.setF9Momentary(!isTrue); break;
            case 10: _throttle.setF10Momentary(!isTrue); break;
            case 11: _throttle.setF11Momentary(!isTrue); break;
            case 12: _throttle.setF12Momentary(!isTrue); break;
            case 13: _throttle.setF13Momentary(!isTrue); break;
            case 14: _throttle.setF14Momentary(!isTrue); break;
            case 15: _throttle.setF15Momentary(!isTrue); break;
            case 16: _throttle.setF16Momentary(!isTrue); break;
            case 17: _throttle.setF17Momentary(!isTrue); break;
            case 18: _throttle.setF18Momentary(!isTrue); break;
            case 19: _throttle.setF19Momentary(!isTrue); break;
            case 20: _throttle.setF20Momentary(!isTrue); break;
            case 21: _throttle.setF21Momentary(!isTrue); break;
            case 22: _throttle.setF22Momentary(!isTrue); break;
            case 23: _throttle.setF23Momentary(!isTrue); break;
            case 24: _throttle.setF24Momentary(!isTrue); break;
            case 25: _throttle.setF25Momentary(!isTrue); break;
            case 26: _throttle.setF26Momentary(!isTrue); break;
            case 27: _throttle.setF27Momentary(!isTrue); break;
            case 28: _throttle.setF28Momentary(!isTrue); break;
        }
    }

    /**
     * Set Sensor state 
     * @param sensorName
     * @param action
     */
    private void setSensor(String sensorName, String action) {
        action = action.toUpperCase();    
        jmri.Sensor s = InstanceManager.sensorManagerInstance().getSensor(sensorName);
        if (s != null) {
            try {
                if ("ACTIVE".equals(action)) {
                    s.setKnownState(jmri.Sensor.ACTIVE);
                } else if ("INACTIVE".equals(action)) {
                    s.setKnownState(jmri.Sensor.INACTIVE);
                }
            } catch (jmri.JmriException e) {
                log.warn("Exception setting sensor "+sensorName+" in action");
            }
        } else {
            log.warn("Sensor "+sensorName+" not found.");
        }
    }

    /**
     * Wait for Sensor state event 
     * @param sensorName
     * @param action
     */
    private void getSensor(String sensorName, String action) {
        action = action.toUpperCase();    
        _waitSensor = InstanceManager.sensorManagerInstance().getSensor(sensorName);
        if (_waitSensor != null) {
            int state = _waitSensor.getKnownState();
            if ("ACTIVE".equals(action)) {
                _sensorWaitState = Sensor.ACTIVE;        	
            } else if ("INACTIVE".equals(action)) {
                _sensorWaitState = Sensor.INACTIVE;        	        	
            }
            if (state==_sensorWaitState) {
            	log.warn("Engineer: state of event sensor "+sensorName+" already at state "+action);
            	return;
            }
        	_waitSensor.addPropertyChangeListener(this);
            if (log.isDebugEnabled()) log.debug("Listen for propertyChange of "+
            		_waitSensor.getDisplayName()+", wait for State= "+_sensorWaitState);
            // suspend commands until sensor changes state
            synchronized(this) {
                try {
                    _waitForSensor = true;            	
                    _warrant.fireRunStatus("Command", Integer.valueOf(_idxCurrentCommand), Integer.valueOf(_idxCurrentCommand+1));
                    wait();                    	
                } catch (InterruptedException ie) {
                    log.error("InterruptedException "+ie);
                }
            }
       } else {
            log.warn("Sensor "+sensorName+" not found.");
        }
    }
    
    protected Sensor getWaitSensor() {
    	return _waitSensor;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (log.isDebugEnabled()) log.debug("propertyChange "+evt.getPropertyName()+
        		" new value= "+evt.getNewValue());
		if ((evt.getPropertyName().equals("KnownState") && 
                ((Number)evt.getNewValue()).intValue()== _sensorWaitState) ) {
            synchronized(this) {
    	        if (!_halt) {
    	            this.notify();
    	            _waitSensor.removePropertyChangeListener(this);
    	            _sensorWaitState = 0;
    	            _waitSensor = null;
    	        }
            }
		}
    }

    /**
     * Wait for Sensor state event 
     * @param sensorName
     * @param action
     */
    private void runWarrant(ThrottleSetting ts) {
    	Warrant w = InstanceManager.warrantManagerInstance().getWarrant(ts.getBlockName());
    	if (w==null) {
            log.warn("Warrant \""+ts.getBlockName()+"\" not found.");
            return;
    	}
    	int num = 0;
        try {
            num = Integer.parseInt(ts.getValue());
        } catch (NumberFormatException nfe) {
        	log.error("Could not parse \""+ts.getValue()+"\". "+nfe);
        }
    	if (num==0) {
            log.info("Warrant \""+_warrant.getDisplayName()+"\" completed last launch of \""+ts.getBlockName()+"\".");
            return;
    	}
    	if (num>0) {
        	num--;
            ts.setValue(Integer.toString(num));    		
    	}
        String msg = null;
        if (_warrant.equals(w)) {
            _idxCurrentCommand = -1;
        	w.startupWarrant();
        } else {
            msg = w.setRunMode(Warrant.MODE_RUN, null, null, null, false);        	
        }
        if (msg!=null) {
        	log.error("Continuing warrant lanch from \""+_warrant.getDisplayName()+"\": "+msg);        	
        }
    }
    
    class ThrottleRamp implements Runnable {
        String endSpeedType;

        ThrottleRamp(String type, long startWait) {
            endSpeedType = type;
            synchronized(this) {
                if (startWait>0.0) {
                    try {
                        wait(startWait);
                    } catch (InterruptedException ie) {
                        log.error("InterruptedException "+ie);
                    }
                }
            }
            _speedOverride = true;
        }

        public void run() {
            _lock.lock();
            try {
                float endSpeed = getLastSpeedCommand(_idxCurrentCommand);
                endSpeed = modifySpeed(endSpeed, endSpeedType);
                String old = _speedType;
                _speedType = endSpeedType;   // transition
                if (log.isDebugEnabled()) log.debug("rampSpeed from \""+old+"\" to \""+endSpeedType+
                					"\" on warrant "+_warrant.getDisplayName());
                float speed = _throttle.getSpeedSetting();
                _warrant.fireRunStatus("SpeedRestriction", old, 
                                   (endSpeed > speed ? "increasing" : "decreasing"));

                synchronized(this) {
                    if (!_speedType.equals("Stop")) {
                        notify();
                        _waitForClear = false;
                    } else {
                    	_waitForClear = true;
                    }
                }

                float incr = Math.max(_throttle.getSpeedIncrement(), _minSpeed);
                switch (_throttle.getSpeedStepMode()) {
                    case DccThrottle.SpeedStepMode14:
                        break;
                    case DccThrottle.SpeedStepMode27:
                    case DccThrottle.SpeedStepMode28:
                        incr *= 2;
                        break;
                    default:    // SpeedStepMode128
                        incr *= 4;
                        break;
                }
                jmri.implementation.SignalSpeedMap map = Warrant.getSpeedMap();
                incr *= map.getNumSteps();
                int delay = map.getStepDelay();

                if (endSpeed > speed) {
                    synchronized(this) {
                        while (speed < endSpeed) {
                            speed += incr;
                            if (speed > endSpeed) { // don't overshoot
                                speed = endSpeed;
                            }
                            setSpeed(speed);
                            try {
                                wait(delay);
                            } catch (InterruptedException ie) {
                                log.error("InterruptedException "+ie);
                            }
                        }
                    }
                } else {
                    synchronized(this) {
                        while (speed > endSpeed) {
                            speed -= incr;
                            if (speed < endSpeed) { // don't undershoot
                                speed = endSpeed;
                            }
                            setSpeed(speed);
                            try {
                                wait(delay);
                            } catch (InterruptedException ie) {
                                log.error("InterruptedException "+ie);
                            }
                        }
                    }
                }
                _warrant.fireRunStatus("SpeedRestriction", old, _speedType);
            } finally {
                _speedOverride = false;
                _lock.unlock();
            }
            if (log.isDebugEnabled()) log.debug("rampSpeed complete to \""+endSpeedType+
					"\" on warrant "+_warrant.getDisplayName());
            synchNotify(null);
        }
    }

    static Logger log = Logger.getLogger(Engineer.class.getName());
}

