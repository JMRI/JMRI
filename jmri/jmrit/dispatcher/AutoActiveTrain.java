// AutoActiveTrain.java

package jmri.jmrit.dispatcher;

import java.util.ArrayList;
import java.util.ResourceBundle;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.ThrottleListener;

/**
 * This class holds information and options for an ActiveTrain when it is running in AUTOMATIC mode.  It ia
 *   an extension to Active Train for automatic running.
 * <P>
 * This class is linked via it's parent ActiveTrain object.
 * <P>
 * This file is part of JMRI.
 * <P>
 * JMRI is open source software; you can redistribute it and/or modify it 
 * under the terms of version 2 of the GNU General Public License as 
 * published by the Free Software Foundation. See the "COPYING" file for 
 * a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * The AutoEngineer sub class is based on code by Pete Cressman contained in Warrants.java
 *
 * @author	Dave Duchamp  Copyright (C) 2010
 * @version	$Revision: 1.1 $
 */
public class AutoActiveTrain implements ThrottleListener {
	
	/**
	 * Main constructor method
	 */
	public AutoActiveTrain(ActiveTrain at) {
		_activeTrain = at;
		at.setAutoActiveTrain(this);
	}
	
	static final ResourceBundle rb = ResourceBundle
						.getBundle("jmri.jmrit.dispatcher.DispatcherBundle");
	
    /* Speed aspects as defined by Doughlas A. Kerr - "Rail Signal Aspects and Indications"
     * doug.kerr.home.att.net/pumpkin/Rail_Signal_Aspects.pdf (from Pete Cressman)
     */
    public static final int SPEED_MASK      = 0x07;     // least significant 3 bits
    public static final int STOP_SPEED      = 0x01;     // No Speed
    public static final int RESTRICTED_SPEED= 0x02;     // Train able to stop within 1/2 visual range (10-15mph)
    public static final int SLOW_SPEED      = 0x03;     // Typically 15 mph  (25% of NORMAL)
    public static final int MEDIUM_SPEED    = 0x04;     // Typically 30 mph (50% of NORMAL)
    public static final int LIMITED_SPEED   = 0x05;     // Typically 40-45 mph  (75% of NORMAL)
    public static final int NORMAL_SPEED    = 0x06;     // Varies with road and location
    public static final int MAXIMUM_SPEED   = 0x07;     // "full" throttle

    private Float[] _speedRatio = { -1.0F, 0.0F, 0.15F, 0.25F, 0.50F, 0.75F, 1.0F, 1.25F };
	
	/* The ramp rates below are in addition to what the decoder itself does
	 */
	public static final int RAMP_NONE       = 0x00;		// No ramping - set speed immediately
	public static final int RAMP_FAST		= 0x01;     // Fast ramping
	public static final int RAMP_MEDIUM		= 0x02;		// Medium ramping
	public static final int RAMP_MED_SLOW	= 0x03;		// Medium/slow ramping
	public static final int RAMP_SLOW		= 0x04;		// Slow ramping

	// operational instance variables
	private AutoActiveTrain _instance = this;
	private ActiveTrain _activeTrain = null;
	private DccThrottle _throttle = null;
	private AutoEngineer _autoEngineer = null;
	private int _address = -1;
	private boolean _forward = true;		
	private int _speedType = NORMAL_SPEED;
	private float _targetSpeed = 0.0f;
	private int _savedStatus = ActiveTrain.RUNNING;
	
	// persistent instance variables (saved with train info)
	private int _rampRate = RAMP_NONE;
	private float _speedFactor = 1.0f;	
	private boolean _resistanceWheels = true;  // true if all train cars show occupancy
	private float _maxTrainLength = 18.0f;  // in inches
	
	// accessor functions
	public ActiveTrain getActiveTrain() {return _activeTrain;}
	public AutoEngineer getAutoEngineer() {return _autoEngineer;}
	public boolean getForward() {return _forward;}
	public void setForward(boolean set) {_forward = set;}
	public float getTargetSpeed() {return _targetSpeed;}
	public void setTargetSpeed(float speed) {_targetSpeed = speed;}
	public int getSavedStatus() {return _savedStatus;}
	public void setSavedStatus(int status) {_savedStatus=status;}
	public int getRampRate() {return _rampRate;}
	public void setRampRate(int rate) {_rampRate = rate;} 
	public float getSpeedFactor() {return _speedFactor;}
	public void setSpeedFactor (float factor) {_speedFactor = factor;}
	public boolean getResistanceWheels() {return _resistanceWheels;}
	public void setResistanceWheels(boolean set) {_resistanceWheels = set;}
	public float getMaxTrainLength() {return _maxTrainLength;}
	public void setMaxTrainLength (float length) {_maxTrainLength = length;}
	
	// initialize new Auto Active Train 
	// (called by ActivateTrainFrame after all items have been set)
	public boolean initialize() {
		// get decoder address
		_address = Integer.valueOf(_activeTrain.getDccAddress()).intValue();
		if ( (_address<1) || (_address>9999) ) {
			log.warn("invalid dcc address for "+_activeTrain.getTrainName());
			return false;
		}
		// request a throttle for automatic operation, throttle returned via callback below
		boolean ok = true;
        ok = InstanceManager.throttleManagerInstance().requestThrottle(_address,this); 
		if (!ok) {
			log.warn("Throttle for locomotive address "+_address+" could not be setup.");
			_activeTrain.setMode(ActiveTrain.DISPATCHED);
			return false;
		}
		return true;
	}
	public void notifyThrottleFound(DccThrottle t) {
		_throttle = t;
		if (_throttle==null) {
			javax.swing.JOptionPane.showMessageDialog(null,java.text.MessageFormat.format(rb.getString(
					"Error28"),new Object[] { _activeTrain.getTrainName() }), rb.getString("InformationTitle"),
						javax.swing.JOptionPane.INFORMATION_MESSAGE);
			log.warn("null throttle returned for train  "+_activeTrain.getTrainName()+ "during automatic initialization.");
			_activeTrain.setMode(ActiveTrain.DISPATCHED);
			return;
		}
		_autoEngineer = new AutoEngineer();
		new Thread(_autoEngineer).start();
		_activeTrain.setMode(ActiveTrain.AUTOMATIC);
	}
	
	
	public void terminate() {
		// here add code to stop the train and release its throttle if it is in autoRun
		_autoEngineer.abort();
		_throttle.release();
	}
    
	public void dispose() {
		
	}

// _________________________________________________________________________________________
	
	// This class runs the train in a separate thread.
	// This class is based on code by Pete Cressman contained in Warrants.java
	
    class AutoEngineer implements Runnable {

        AutoEngineer() {
        }
		
		// operational instance variables and flags
        private float   _minSpeedStep = 1.0f;
        private boolean _abort = false;
        private boolean _halt = false;  // halt/resume from user's control
        private boolean _wait = false;  // waits for signals/occupancy/allocation to clear
		private boolean _halted = false; // true if previously halted
		private boolean _ramping = false;  // true if ramping speed to _targetSpeed;
		private float   _currentSpeed = 0.0f;
		private boolean _currentForward = true;    

        public void run() {
			_throttle.setIsForward(_forward);
			_currentForward = _forward;
			_throttle.setSpeedSetting(_currentSpeed);
			setSpeedStep(_throttle.getSpeedStepMode());		
			// this is the running loop
            while (!_abort) {				
				if (_halt && !_halted) {
					_throttle.setSpeedSetting(-1.0f);
					_throttle.setSpeedSetting(0.0f);
					_currentSpeed = 0.0f;
					_halted = true;
				}
				if (_wait && !_halt) {
					// test if conditions allow resuming travel
					if (_activeTrain.getStatus()==ActiveTrain.RUNNING) {
						_wait = false;
					}
				}
				if (!_wait) {
					// test if need to change direction
					if (_currentForward!=_forward) {
						_throttle.setIsForward (_forward);
						_currentForward = _forward;
					}
					// test if need to change speed
					if (_currentSpeed != _targetSpeed) {
						if ( !_ramping || (_rampRate==RAMP_NONE) ) {
							_throttle.setSpeedSetting(_targetSpeed);
							_currentSpeed = _targetSpeed;
						}					
					}
				}
				// delay
				synchronized(this) {
					try {
						if (_wait) {
							wait(250);
						}
					} catch (InterruptedException ie) {
						log.error("InterruptedException in AutoEngineer"+ie);
					}
				}
             }
            // shut down
        }

        private void setSpeedStep(int step) {
            switch (step) {
                case DccThrottle.SpeedStepMode14:
                    _minSpeedStep = 1.0f/15;
                    break;
                case DccThrottle.SpeedStepMode27:
                    _minSpeedStep = 1.0f/28;
                    break;
                case DccThrottle.SpeedStepMode28:
                    _minSpeedStep = 1.0f/29;
                    break;
                default:
                    _minSpeedStep = 1.0f/127;
                    break;
            }
        }

        public boolean isWaiting() {
            return _wait;
        }

        /**
        * Flag from user's control
		* Note: Halt here invokes emergency stop.
        */
        public synchronized void setHalt(boolean halt) {
            _halt = halt;
            if (_halt) { 
                _wait = true;
            }
			else {
				_halted = false;
			}
        }

        /**
        * Flag from user to end run
        */
        public void abort() {
            _abort = true;
            _throttle.setSpeedSetting(-1.0f);
            _throttle.setSpeedSetting(0.0f);
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
    }
	
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AutoActiveTrain.class.getName());
}

/* @(#)AutoActiveTrain.java */
