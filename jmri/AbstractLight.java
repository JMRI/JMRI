// AbstractLight.java

package jmri;

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
 * <P>
 * Based in concept on AbstractSignalHead.java
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @version     $Revision: 1.5 $
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
    protected String mControlSensorSystemName = "";
    protected int mFastClockOnHour = 0;
    protected int mFastClockOnMin = 0;
    protected int mFastClockOffHour = 0;
    protected int mFastClockOffMin = 0;
    protected String mControlTurnoutSystemName = "";
    protected int mTurnoutState = Turnout.CLOSED;
    
    /**
     *  System independent operational instance variables (not saved between runs)
     */
    protected boolean mActive = false;
    protected Sensor mControlSensor = null;
    protected java.beans.PropertyChangeListener mSensorListener = null;
    protected Turnout mControlTurnout = null;
    protected java.beans.PropertyChangeListener mTurnoutListener = null;
    
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
                (controlType==TURNOUT_STATUS_CONTROL) ) {
            mControlType = controlType;
        }
        else {
            mControlType = NO_CONTROL;
        }
    }
    
    /**
     *  Return the controlling Sensor if there is one, else null
     */    
    public String getControlSensorSystemName() { return mControlSensorSystemName; }    
    /**
     *  Set the controlling Sensor if there is one, else null
     */    
    public void setControlSensor(String sensorSystemName) { 
        mControlSensorSystemName = sensorSystemName;
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
        }
        else {
            log.error("Light time off minute not 0 - 59, but is "+offMin);
            mFastClockOffMin = 0;
        }
    }
    
    /**
     *  Return the controlling Turnout if there is one, else null.
     */    
    public String getControlTurnoutSystemName() { return mControlTurnoutSystemName; }
    /** 
     *  Set the controlling Turnout.  This is the Turnout whose state
     *     controls the ON and OFF of this Light.
     */
    public void setControlTurnout(String turnoutSystemName) {
        mControlTurnoutSystemName = turnoutSystemName;
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

	abstract public void setState(int value);
	
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
                    mControlSensor = InstanceManager.sensorManagerInstance().
                                            provideSensor(mControlSensorSystemName);
                    if (mControlSensor!=null) {
                        mControlSensor.addPropertyChangeListener(mSensorListener =
                                                new java.beans.PropertyChangeListener() {
                                public void propertyChange(java.beans.PropertyChangeEvent e) {
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
                                             mControlSensorSystemName);
                        return;
                    }
                    break;
                    
                case FAST_CLOCK_CONTROL:
                    break;
                case TURNOUT_STATUS_CONTROL:
                    mControlTurnout = InstanceManager.turnoutManagerInstance().
                                            provideTurnout(mControlTurnoutSystemName);
                    if (mControlTurnout!=null) {
                        mControlTurnout.addPropertyChangeListener(mTurnoutListener =
                                                new java.beans.PropertyChangeListener() {
                                public void propertyChange(java.beans.PropertyChangeEvent e) {
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
                                             mControlSensorSystemName);
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
                    break;
                case TURNOUT_STATUS_CONTROL:
                    if (mTurnoutListener!=null) {
                        mControlTurnout.removePropertyChangeListener(mTurnoutListener);
                        mTurnoutListener = null;
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
}

/* @(#)AbstractLight.java */
