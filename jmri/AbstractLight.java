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
 * @version     $Revision: 1.2 $
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
    protected Sensor mControlSensor = null;
// placeholder for future Schedule object for FastClock control
//    protected Schedule mControlSchedule = null;   
// placeholder for future Switch object for Panel Switch control
//    protected Switch mControlSwitch = null;
    protected SignalHead mControlSignalHead = null;
    protected int mSignalHeadAspect = SignalHead.RED;
    protected Turnout mControlTurnout = null;
    protected int mTurnoutState = Turnout.CLOSED;
    
    /**
     *  System independent operational instance variables (not saved between runs)
     */
    protected boolean mActive = false;
    protected java.beans.PropertyChangeListener mSensorListener = null;
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
                (controlType==PANEL_SWITCH_CONTROL) ||
                (controlType==SIGNAL_HEAD_CONTROL) ||
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
    public Sensor getControlSensor() { return mControlSensor; }    
    /**
     *  Set the controlling Sensor if there is one, else null
     */    
    public void setControlSensor(Sensor sensor) { 
        mControlSensor = sensor;
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
    
// placeholder for future Schedule object for FastClock control
//    /**
//     *  Return the On/Off Schedule if FAST_CLOCK_CONTROL
//     */        
//    public Schedule getFastClockControlSchedule() { return mControlSchedule; }
//    /**
//     *  Set the On/Off Schedule if FAST_CLOCK_CONTROL
//     */        
//    public void setFastClockControlSchedule(Schedule schedule) { 
//        mControlSchedule = schedule;
//    }
    
// placeholder for future Switch object for Panel Switch control
//    /**
//     *  Return the controlling Panel Switch if PANEL_SWITCH_CONTROL
//     */            
//    public Switch getControlSwitch() { return mControlSwitch; }
//    /**
//     *  Set the controlling Panel Switch if PANEL_SWITCH_CONTROL
//     */            
//    public void setControlSwitch(Switch switch) { 
//        mControlSwitch = switch;
//    }
    
    /**
     *  Return the controlling Signal Head if there is one, else null
     */    
    public SignalHead getControlSignalHead() { return mControlSignalHead; }
    /**
     *  Set the controlling Signal Head if there is one, else null
     */    
    public void setControlSignalHead(SignalHead sh) { 
        mControlSignalHead = sh;
    }    
    
    /**
     *  Return the controlling Signal Head Aspect.  This is the light
     *     in the signal head that corresponds to this Light.  This
     *     light should be ON if and only if this is the current aspect
     *     of the controlling signal head.
     */    
    public int getControlSignalHeadAspect() { return mSignalHeadAspect; }
    /**
     *  Set the controlling Signal Head Aspect.  This is the light
     *     in the signal head that corresponds to this Light.
     *  This call is ignored if 'aspect' is not one of the supported 
     *     Signal Head aspects.
     */    
    public void setControlSignalHeadAspect(int aspect) { 
        if ( (aspect == SignalHead.RED) || (aspect == SignalHead.GREEN) ||
                (aspect == SignalHead.YELLOW) ) {
            mSignalHeadAspect = aspect;
        }
    }    
    
    /**
     *  Return the controlling Turnout if there is one, else null.
     */    
    public Turnout getControlTurnout() { return mControlTurnout; }
    /** 
     *  Set the controlling Turnout.  This is the Turnout whose state
     *     controls the ON and OFF of this Light.
     */
    public void setControlTurnout(Turnout turnout) {
        mControlTurnout = turnout;
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
                    }
                    break;
                    
                case FAST_CLOCK_CONTROL:
                    break;
                case PANEL_SWITCH_CONTROL:
                    break;
                case SIGNAL_HEAD_CONTROL:
                    // No activation required.  Light is controlled by the 
                    //     Signal Head logic.
                    break;
                case TURNOUT_STATUS_CONTROL:
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
                    }
                    break;
                case NO_CONTROL:
                    // No control mechanism specified
                    break;
                default:
                    log.warn("Unexpected control type when activating Light: "+getSystemName());
            }
            mActive = true;
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
                case PANEL_SWITCH_CONTROL:
                    break;
                case SIGNAL_HEAD_CONTROL:
                    // No activation required.  Light is controlled by the 
                    //     Signal Head logic.
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
