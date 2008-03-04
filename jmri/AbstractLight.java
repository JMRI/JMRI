// AbstractLight.java

package jmri;
import javax.swing.Timer;
import java.util.Date;

 /**
 * Abstract class providing partial implementation of the basic 
 *      logic of the Light interface.
 * <P>
 * Light objects require a number of instance variables.  Since 
 *     Light objects are created using the standard JMRI 
 *     systemName/userName concept, accessor routines are provided
 *     for setting and editting these instance variables.
 * <P>
 * Instance variables are divided into system-independent and
 *    system dependent categories.  System independent instance
 *    variables are defined here, and their accessor routines are
 *    implemented here.
 *
 * <p>
 * This implementation provides a notional implementation of 
 * intensity and transitions.  The user can set intensity
 * so long as it's at least the max value (default 1.0) or
 * no more than the minimum value (default 0.0). In that case,
 * the setTargetIntensity operations become a setState to ON
 * or OFF.  Setting a target intensity between the min and max
 * is an error, because this type of Light does not support
 * a true analog intensity.
 * Transitions never happen, and setting a TransitionTime
 * greater than 0.0 gives an exception.
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @author	Ken Cameron Copyright (C) 2008
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version     $Revision: 1.15 $
 */
public abstract class AbstractLight extends AbstractNamedBean
    implements Light, java.io.Serializable {

    public AbstractLight(String systemName, String userName) {
        super(systemName, userName);
    }

    public AbstractLight(String systemName) {
        super(systemName);
    }
    
    /**
     *  System independent instance variables (saved between runs)
     */
    protected int mControlType = NO_CONTROL;
    protected int mControlSensorSense = Sensor.ACTIVE;
    protected String mControlSensorName = "";
    protected int mFastClockOnHour = 0;
    protected int mFastClockOnMin = 0;
    protected int mFastClockOffHour = 0;
    protected int mFastClockOffMin = 0;
    protected String mControlTurnoutName = "";
    protected int mTurnoutState = Turnout.CLOSED;
    protected String mTimedSensorName = "";
	protected int mTimeOnDuration = 0;
	
	protected double mMaxIntensity = 1.0;
	protected double mMinIntensity = 0.0;
	
    /**
     *  System independent operational instance variables (not saved between runs)
     */
    protected boolean mActive = false;
    protected Sensor mControlSensor = null;
    protected java.beans.PropertyChangeListener mSensorListener = null;
	protected java.beans.PropertyChangeListener mTimebaseListener = null;
	protected Timebase mClock = null;
	protected int mTimeOn = 0;
	protected int mTimeOff = 0;
    protected Turnout mControlTurnout = null;
    protected java.beans.PropertyChangeListener mTurnoutListener = null;
    protected boolean mTimedActive = false;
    protected Sensor mTimedControlSensor = null;
    protected java.beans.PropertyChangeListener mTimedSensorListener = null;
	protected Timer mTimedControlTimer = null;
	protected java.awt.event.ActionListener mTimedControlListener = null;
	protected boolean mLightOnTimerActive = false;
    protected boolean mEnabled = true;
    
    protected double mCurrentIntensity = 0.0;
    protected int mState = OFF;

    /**
     * Get enabled status
    */
    public boolean getEnabled() { return mEnabled; }
    /**
     * Set enabled status
     */
    public void setEnabled(boolean v) { 
        boolean old = mEnabled;
        mEnabled = v;
        if (old != v) firePropertyChange("Enabled", new Boolean(old), new Boolean(v));
    }

    /** Check if this object can handle variable intensity.
     * <P>
     * @return false, as this abstract class does not implement
     * variable intensity. See e.g. {@link AbstractVariableLight} for 
     * an abstract implementation of variable intensity.
    */
    public boolean isIntensityVariable() {
        return false;
    }
    
   /** Set the intended new intensity value for the Light.
    *  If transitions are in use, they will be applied.
    *  <p>
    *  Bound property between 0 and 1. 
    *  <p>
    *  A value of 0.0 corresponds to full off, and 
    *  a value of 1.0 corresponds to full on.
    *  <p>
    *  Values at or below
    *  the minIntensity property will result in the Light going
    *  to the OFF state immediately. 
    *  Values at or above the maxIntensity property
    *  will result in the Light going to the ON state immediately.  
    *  <p>
    *  All others result in an exception, instead of the
    *  INTERMEDIATE state, because this class does not implement analog intensity
    *  <P>
    *  @throws IllegalArgumentException when intensity is less than 0.0 or more than 1.0
    *  @throws IllegalArgumentException when intensity is more than MinIntensity and less than MaxIntensity
    */
    public void setTargetIntensity(double intensity) {
        if (intensity<0.0 || intensity>1.0) 
            throw new IllegalArgumentException("Target intensity value "+intensity+" not in legal range");

        // limit
        if (intensity > mMaxIntensity ) intensity = mMaxIntensity;
        if (intensity < mMinIntensity ) intensity = mMinIntensity;
        
        // move directly to target, if possible
        if (intensity >= mMaxIntensity) {
            updateIntensityHigh(intensity);
        } else if (intensity <= mMinIntensity) {
            updateIntensityLow(intensity);
        } else {
            updateIntensityIntermediate(intensity);
        }
    }
    
    /**
     * Method for further implementation of 
     * setTargetIntensity at or below the minimum
     */
    protected void updateIntensityLow(double intensity) {
        double oldValue = mCurrentIntensity;
        // set to minimum, so go to OFF state
        mCurrentIntensity = intensity;
        if (oldValue != intensity)
            firePropertyChange("TargetIntensity", new Double(oldValue), new Double(intensity));
        setState(OFF);
    }
    
    /**
     * Method for further implementation of 
     * setTargetIntensity between min and max
     */
    protected void updateIntensityIntermediate(double intensity) {
        // not in value range!
        throw new IllegalArgumentException("intensity value "+intensity+" between min "+mMinIntensity+" and max "+mMaxIntensity);
    }
    
    /**
     * Method for further implementation of 
     * setTargetIntensity at or above the maximum
     */
    protected void updateIntensityHigh(double intensity) {
        double oldValue = mCurrentIntensity;
        // set to maximum, so go to ON state
        mCurrentIntensity = intensity;
        if (oldValue != intensity)
            firePropertyChange("TargetIntensity", new Double(oldValue), new Double(intensity));
        setState(ON);
    }
    
    /** Get the current intensity value.
     * If the Light is currently transitioning, this may be either
     * an intermediate or final value.
     * <p>
     * A value of 0.0 corresponds to full off, and 
     * a value of 1.0 corresponds to full on.
    */
    public double getCurrentIntensity() {
        return mCurrentIntensity;
    }
    
    /** Get the target intensity value for the 
     * current transition, if any. If the Light is not currently
     * transitioning, this is the current intensity value.
     * <p>
     * A value of 0.0 corresponds to full off, and 
     * a value of 1.0 corresponds to full on.
     * <p>
     * Bound property
    */
    public double  getTargetIntensity() {
        return mCurrentIntensity;
    }
    
    /** Set the value of the maxIntensity property.
     * <p>
     * Bound property between 0 and 1. 
     * <p>
     * A value of 0.0 corresponds to full off, and 
     * a value of 1.0 corresponds to full on.
     * @throws IllegalArgumentException when intensity is less than 0.0 or more than 1.0
     * @throws IllegalArgumentException when intensity is not greater than the current value of the minIntensity property
    */
    public void setMaxIntensity(double intensity) {
        if (intensity<0.0 || intensity>1.0) 
            throw new IllegalArgumentException("Illegal intensity value: "+intensity);
        if (intensity <= mMinIntensity) 
            throw new IllegalArgumentException("Requested intensity "+intensity+" not less than minIntensity "+mMinIntensity);

        double oldValue = mMaxIntensity;
        mMaxIntensity = intensity;
        if (oldValue != intensity)
            firePropertyChange("MaxIntensity", new Double(oldValue), new Double(intensity));
    }
    
    /** Get the current value of the maxIntensity property.
     * <p>
     * A value of 0.0 corresponds to full off, and 
     * a value of 1.0 corresponds to full on.
    */
    public double getMaxIntensity() {
        return mMaxIntensity;
    }
    
    /** Set the value of the minIntensity property.
     * <p>
     * Bound property between 0 and 1. 
     * <p>
     * A value of 0.0 corresponds to full off, and 
     * a value of 1.0 corresponds to full on.
     * @throws IllegalArgumentException when intensity is less than 0.0 or more than 1.0
     * @throws IllegalArgumentException when intensity is not less than the current value of the maxIntensity property
    */
    public void setMinIntensity(double intensity) {
        if (intensity<0.0 || intensity>1.0) 
            throw new IllegalArgumentException("Illegal intensity value: "+intensity);
        if (intensity >= mMaxIntensity) 
            throw new IllegalArgumentException("Requested intensity "+intensity+" not more than maxIntensity "+mMaxIntensity);

        double oldValue = mMinIntensity;
        mMinIntensity = intensity;
        if (oldValue != intensity)
            firePropertyChange("MinIntensity", new Double(oldValue), new Double(intensity));
    }
    
    /** 
     * Get the current value of the minIntensity property.
     * <p>
     * A value of 0.0 corresponds to full off, and 
     * a value of 1.0 corresponds to full on.
     */
    public double getMinIntensity() {
        return mMinIntensity;
    }
    
    /** 
     * Can the Light change it's intensity setting slowly?
     * <p>
     * If true, this Light supports a non-zero value of the 
     * transitionTime property, which controls how long the Light
     * will take to change from one intensity level to another.
     * <p>
     * Unbound property
     */
    public boolean isTransitionAvailable() { return false; }
    
    /** 
     * Set the fast-clock duration for a 
     * transition from full ON to full OFF or vice-versa.
     * <P>
     * This class does not implement transitions, so this property
     * cannot be set from zero.
     * <p>
     * Bound property
     * <p>
     * @throws IllegalArgumentException if minutes is not 0.0
     */
    public void setTransitionTime(double minutes) {
        if (minutes != 0.0) throw new IllegalArgumentException("Illegal transition time: "+minutes);
    }
    
    /** 
     * Get the number of fastclock minutes taken by a transition from
     * full ON to full OFF or vice versa.
     * <p>
     * @return 0.0 if the output intensity transition is instantaneous
     */
    public double getTransitionTime() { return 0.0; }
    
    /** 
     * Convenience method for checking if the intensity of the light is currently
     * changing due to a transition.
     * <p>
     * Bound property so that listeners can conveniently learn when the transition is over.
     */
    public boolean isTransitioning() { return false; }

    /**
     *  Return the control type of this Light
     */    
    public int getControlType() { return mControlType; }
    /**
     *  Set the control type of this Light
     */    
    public void setControlType(int controlType) {
        if ( (controlType==SENSOR_CONTROL) || 
                (controlType==FAST_CLOCK_CONTROL) ||
                (controlType==TURNOUT_STATUS_CONTROL) ||
				(controlType==TIMED_ON_CONTROL) ) {
            mControlType = controlType;
        }
        else {
            mControlType = NO_CONTROL;
        }
    }
    
    /**
     *  Return the controlling Sensor if there is one, else null
     */    
    public String getControlSensorName() { return mControlSensorName; }    
    /**
     *  Set the controlling Sensor if there is one, else null
     */    
    public void setControlSensor(String sensorName) { 
        mControlSensorName = sensorName;
    }    
    
    /**
     *  Return the controlling Sensor Sense. This is the state of the 
     *     controlling Sensor that corresponds to this Light being
     *     ON.
     */    
    public int getControlSensorSense() { return mControlSensorSense; }    
    /**
     *  Set the controlling Sensor Sense.  This is the state of the 
     *     controlling Sensor that corresponds to this Light being
     *     ON.
     *  If 'sense' does not correspond to one of the allowed states of
     *     a Sensor, this call is ignored.
     */    
    public void setControlSensorSense(int sense) {
        if ( (sense==Sensor.ACTIVE) || (sense==Sensor.INACTIVE) ) {
            mControlSensorSense = sense;
        }
    }    
    
    /**
     *  Return the On/Off Schedule if FAST_CLOCK_CONTROL
     */        
    public int getFastClockOnHour() { return mFastClockOnHour; }
    public int getFastClockOnMin() { return mFastClockOnMin; }
    public int getFastClockOffHour() { return mFastClockOffHour; }
    public int getFastClockOffMin() { return mFastClockOffMin; }
    /**
     *  Set the On/Off Schedule if FAST_CLOCK_CONTROL
     */        
    public void setFastClockControlSchedule(int onHour,int onMin,int offHour, int offMin) { 
        if ( (onHour >= 0) && (onHour <= 24) ) {
            // legal value, set it
            mFastClockOnHour = onHour;
        }
        else {
            log.error("Light time on hour not 0 - 24, but is "+onHour);
            mFastClockOnHour = 0;
        }
        if ( (onMin >= 0) && (onMin <= 59) ) {
            // legal value, set it
            mFastClockOnMin = onMin;
        }
        else {
            log.error("Light time on minute not 0 - 59, but is "+onMin);
            mFastClockOnMin = 0;
        }
        if ( (offHour >= 0) && (offHour <= 24) ) {
            // legal value, set it
            mFastClockOffHour = offHour;
        }
        else {
            log.error("Light time off hour not 0 - 24, but is "+offHour);
            mFastClockOffHour = 0;
        }
        if ( (offMin >= 0) && (offMin <= 59) ) {
            // legal value, set it
            mFastClockOffMin = offMin;
        } else {
            log.error("Light time off minute not 0 - 59, but is "+offMin);
            mFastClockOffMin = 0;
        }
    }
    
    /**
     *  Return the controlling Turnout if there is one, else null.
     */    
    public String getControlTurnoutName() { return mControlTurnoutName; }
    /** 
     *  Set the controlling Turnout.  This is the Turnout whose state
     *     controls the ON and OFF of this Light.
     */
    public void setControlTurnout(String turnoutName) {
        mControlTurnoutName = turnoutName;
    }
    /**
     *  Return the state of the controlling Turnout that corresponds to
     *    this light being ON.
     */    
    public int getControlTurnoutState() { return mTurnoutState; }
    /** 
     *  Set the state of the controlling Turnout that corresponds to
     *    this light being ON.
     *  If 'ts' is not a valid state for a turnout, this call is ignored.
     */
    public void setControlTurnoutState(int ts) {
        if ( (ts==Turnout.CLOSED) || (ts==Turnout.THROWN) ) {
            mTurnoutState = ts;
        }
    }
    /**
     *  Return the trigger Sensor system name. This is the Sensor which triggers
     *     the Timed ON state of the light when it moves from inactive to active.
     */    
	public String getControlTimedOnSensorName() {
		return mTimedSensorName;
	}
    /**
     *  Set the trigger Sensor system name. This is the Sensor which triggers
     *     the Timed ON state of the light when it moves from inactive to active.
     */    
	public void setControlTimedOnSensor(String sensorName) {
		mTimedSensorName = sensorName;
	}
    /**
     *  Return the duration (milliseconds) light is to remain ON after
     *    it has been triggered.
     */    
	public int getTimedOnDuration() { return mTimeOnDuration; }            
    /**
     *  Set the duration (milliseconds) light is to remain ON after
     *    it has been triggered.
     */    
	public void setTimedOnDuration(int duration) {
		mTimeOnDuration = duration;
	}

	public void setState(int newState) {
	    int oldState = mState;
	    if ( newState != ON && newState != OFF) 
	        throw new IllegalArgumentException("cannot set state value "+newState);
	    double intensity = getTargetIntensity();
	    if (newState == ON && intensity < getMaxIntensity() ) {
	        setTargetIntensity(getMaxIntensity());
	        // stop if state change was done as part of setTargetIntensity
	        if (getState() == ON) return;
	    }
	    if (newState == OFF && intensity > getMinIntensity() ) {
	        setTargetIntensity(getMinIntensity());
	        // stop if state change was done as part of setTargetIntensity
	        if (getState() == OFF) return;
        }
        // do the state change
	    mState = newState;
	    doNewState(oldState, newState);
	    if (oldState!=newState)
	        firePropertyChange("KnownState", new Integer(oldState), new Integer(newState));
	}
	
	/**
	 * Implement the specific change of state needed by hardware
	 */
	protected void doNewState(int oldState, int newState) {}

	public int getState() {
	    return mState;
	}

    /**
	 *  Updates the status of a Light under FAST_CLOCK_CONTROL.  This
	 *   method is called every FastClock minute.
	 */
	public void updateClockControlLight() {
		if (mClock!=null) {
			Date now = mClock.getTime();
			int timeNow = now.getHours() * 60 + now.getMinutes();
			int state = getState();
			if (mTimeOn <= mTimeOff) {
				// on and off the same day
				if ( (timeNow<mTimeOn) || (timeNow>=mTimeOff) ) {
					// Light should be OFF
					if (state == ON) setState(OFF);
				}
				else { 
					// Light should be ON
					if (state == OFF) setState(ON);
				}
			}
			else {
				// on and off - different days
				if ( (timeNow>=mTimeOn) || (timeNow<mTimeOff) ) {
					// Light should be ON
					if (state == OFF) setState(ON);
				}
				else { 
					// Light should be OFF
					if (state == ON) setState(OFF);
				}
			}
		}
	}
	 
    /**
     * Activates a light by control type.  This method tests the 
     *   control type, and set up a control mechanism, appropriate 
     *   for the control type.  Some lights, e.g. signal head lights,
     *   are controlled by the signal head, so no activation is needed
     *   here.
     */
    public void activateLight() {
        // skip if Light is already active
        if (!mActive) {
            // activate according to control type
            switch (mControlType) {
                case SENSOR_CONTROL:
                    mControlSensor = null;
                    if (mControlSensorName.length()>0)
                            mControlSensor= InstanceManager.sensorManagerInstance().
                                                provideSensor(mControlSensorName);
                    if (mControlSensor!=null) {
						// if sensor state is currently known, set light accordingly
						int kState = mControlSensor.getKnownState();
						if (kState==Sensor.ACTIVE) { 
							if (mControlSensorSense==Sensor.ACTIVE) {
								// Turn light on
								setState(ON);
							}
							else {
								// Turn light off
								setState(OFF);
							}
						}
						else if (kState==Sensor.INACTIVE) {
							if (mControlSensorSense==Sensor.INACTIVE) {
								// Turn light on
								setState(ON);
							}
							else {
								// Turn light off
								setState(OFF);
							}
						}
					
						// listen for change in sensor state
                        mControlSensor.addPropertyChangeListener(mSensorListener =
                                                new java.beans.PropertyChangeListener() {
                                public void propertyChange(java.beans.PropertyChangeEvent e) {
									if (!mEnabled) return;  // ignore property change if user disabled light
                                    if (e.getPropertyName().equals("KnownState")) {
                                        int now = mControlSensor.getKnownState();
                                        if (now==Sensor.ACTIVE) { 
                                            if (mControlSensorSense==Sensor.ACTIVE) {
                                                // Turn light on
                                                setState(ON);
                                            }
                                            else {
                                                // Turn light off
                                                setState(OFF);
                                            }
                                        }
                                        else if (now==Sensor.INACTIVE) { 
                                            if (mControlSensorSense==Sensor.INACTIVE) {
                                                // Turn light on
                                                setState(ON);
                                            }
                                            else {
                                                // Turn light off
                                                setState(OFF);
                                            }
                                        }
                                    }
                                }
                        });
                        mActive = true;
                    }
                    else {
                        // control sensor does not exist
                        log.error("Light "+getSystemName()+" is linked to a Sensor that does not exist: "+
                                             mControlSensorName);
                        return;
                    }
                    break;
                    
                case FAST_CLOCK_CONTROL:
					if (mClock==null) {
						mClock = InstanceManager.timebaseInstance();
					}
					// set up time as minutes in a day
					mTimeOn = mFastClockOnHour * 60 + mFastClockOnMin;
					mTimeOff = mFastClockOffHour * 60 + mFastClockOffMin;
					// initialize light based on current fast time
					updateClockControlLight ();
					// set up to listen for time changes on a minute basis
					mClock.addMinuteChangeListener( mTimebaseListener = 
						new java.beans.PropertyChangeListener() {
							public void propertyChange(java.beans.PropertyChangeEvent e) {
								if (mEnabled) {
									// update control if light is enabled
									updateClockControlLight();
								}
							}
						});
					mActive = true;
                    break;
                case TURNOUT_STATUS_CONTROL:
                    mControlTurnout = InstanceManager.turnoutManagerInstance().
                                            provideTurnout(mControlTurnoutName);
                    if (mControlTurnout!=null) {
						// set light based on current turnout state if known
						int tState = mControlTurnout.getKnownState();
						if (tState==Turnout.CLOSED) { 
							if (mTurnoutState==Turnout.CLOSED) {
								// Turn light on
								setState(ON);
							}
							else {
								// Turn light off
								setState(OFF);
							}
						}
						else if (tState==Turnout.THROWN) { 
							if (mTurnoutState==Turnout.THROWN) {
								// Turn light on
								setState(ON);
							}
							else {
								// Turn light off
								setState(OFF);
							}
						}
						
						// listen for change in turnout state
                        mControlTurnout.addPropertyChangeListener(mTurnoutListener =
                                                new java.beans.PropertyChangeListener() {
                                public void propertyChange(java.beans.PropertyChangeEvent e) {
									if (!mEnabled) return;  // ignore property change if user disabled light
									if (e.getPropertyName().equals("KnownState")) {
                                        int now = mControlTurnout.getKnownState();
                                        if (now==Turnout.CLOSED) { 
                                            if (mTurnoutState==Turnout.CLOSED) {
                                                // Turn light on
                                                setState(ON);
                                            }
                                            else {
                                                // Turn light off
                                                setState(OFF);
                                            }
                                        }
                                        else if (now==Turnout.THROWN) { 
                                            if (mTurnoutState==Turnout.THROWN) {
                                                // Turn light on
                                                setState(ON);
                                            }
                                            else {
                                                // Turn light off
                                                setState(OFF);
                                            }
                                        }
                                    }
                                }
                        });
                        mActive = true;
                    }
                    else {
                        // control turnout does not exist
                        log.error("Light "+getSystemName()+" is linked to a Turnout that does not exist: "+
                                             mControlSensorName);
                        return;
                    }
                    break;
                case TIMED_ON_CONTROL:
                    mTimedControlSensor = InstanceManager.sensorManagerInstance().
                                            provideSensor(mTimedSensorName);
                    if (mTimedControlSensor!=null) {
						// set initial state off
						setState(OFF);
						// listen for change in timed control sensor state
                        mTimedControlSensor.addPropertyChangeListener(mTimedSensorListener =
                                                new java.beans.PropertyChangeListener() {
                                public void propertyChange(java.beans.PropertyChangeEvent e) {
									if (!mEnabled) return;  // ignore property change if user disabled light
									if (e.getPropertyName().equals("KnownState")) {
                                        int now = mTimedControlSensor.getKnownState();
										if (!mLightOnTimerActive) {
											if (now==Sensor.ACTIVE) { 
                                                // Turn light on
                                                setState(ON);
												// Create a timer if one does not exist
												if (mTimedControlTimer==null) {
													mTimedControlListener = new TimeLight();
													mTimedControlTimer = new Timer(mTimeOnDuration,
															mTimedControlListener);
												}
												// Start the Timer to turn the light OFF
												mLightOnTimerActive = true;
												mTimedControlTimer.start();
                                            }
                                        }
                                    }
                                }
                        });
                        mActive = true;
                    }
                    else {
                        // timed control sensor does not exist
                        log.error("Light "+getSystemName()+" is linked to a Sensor that does not exist: "+
                                             mTimedSensorName);
                        return;
                    }
                    break;
                case NO_CONTROL:
                    // No control mechanism specified
                    break;
                default:
                    log.warn("Unexpected control type when activating Light: "+getSystemName());
            }
        }    
    }
    
    /**
     * Deactivates a light by control type.  This method tests the 
     *   control type, and deactivates the control mechanism, appropriate 
     *   for the control type.  Some lights, e.g. signal head lights,
     *   are controlled by the signal head, so no deactivation is needed
     *   here.
     */
    public void deactivateLight() {
        // skip if Light is not active
        if (mActive) {
            // deactivate according to control type
            switch (mControlType) {
                case SENSOR_CONTROL:
                    if (mSensorListener!=null) {
                        mControlSensor.removePropertyChangeListener(mSensorListener);
                        mSensorListener = null;
                    }
                    break;
                case FAST_CLOCK_CONTROL:
					if ( (mClock!=null) && (mTimebaseListener!=null) ){
						mClock.removeMinuteChangeListener(mTimebaseListener);
						mTimebaseListener = null;
					}
                    break;
                case TURNOUT_STATUS_CONTROL:
                    if (mTurnoutListener!=null) {
                        mControlTurnout.removePropertyChangeListener(mTurnoutListener);
                        mTurnoutListener = null;
                    }
                    break;
                case TIMED_ON_CONTROL:
                    if (mTimedSensorListener!=null) {
                        mTimedControlSensor.removePropertyChangeListener(mTimedSensorListener);
                        mTimedSensorListener = null;
                    }
					if (mLightOnTimerActive) {
						mTimedControlTimer.stop();
						mLightOnTimerActive = false;
					}
					if (mTimedControlTimer!=null) {
						if (mTimedControlListener!=null) {
							mTimedControlTimer.removeActionListener(mTimedControlListener);
							mTimedControlListener = null;
						}
						mTimedControlTimer = null;
					}
                    break;
                case NO_CONTROL:
                    // No control mechanism specified
                    break;
                default:
                    log.warn("Unexpected control type when activating Light: "+getSystemName());
            }
            mActive = false;
        }    
    }

	/**
	 *	Class for defining ActionListener for TIMED_ON_CONTROL
	 */
	class TimeLight implements java.awt.event.ActionListener 
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			// Turn Light OFF
			setState(OFF);
			// Turn Timer OFF
			mTimedControlTimer.stop();
			mLightOnTimerActive = false;
		}
	}
}

/* @(#)AbstractLight.java */
