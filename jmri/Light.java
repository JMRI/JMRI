// Light.java

package jmri;

/**
 * Represent a single Light or digital output bit on the layout. 
 * <P>
 * Light objects require a number of instance variables.  Since 
 *     Light objects are created using the standard JMRI 
 *     systemName/userName concept, accessor routines are provided
 *     for setting and editting these instance variables.
 * <P>
 * Initially, this allows access to explicit information. 
 * <P>
 * Based on SignalHead.java
 *
 * @author			Dave Duchamp Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */
public interface Light extends NamedBean {

    // states
    public static final int ON          = 0x01;
    public static final int OFF         = 0x00;
    
    // control types
    public static final int SENSOR_CONTROL          = 0x01;
    public static final int FAST_CLOCK_CONTROL      = 0x02;
    public static final int PANEL_SWITCH_CONTROL    = 0x03;
    public static final int SIGNAL_HEAD_CONTROL     = 0x04;
    public static final int TURNOUT_STATUS_CONTROL  = 0x05;
    public static final int NO_CONTROL              = 0x00;

    /**
     * State is a bound parameter. Value values are ON and OFF
     */
    public int getState();
    public void setState(int newState);
    
    /** 
     * Control type is an instance variable.  Its value is one of the
     *      types noted above.
     */
    public int getControlType();
    public void setControlType(int controlType);
    
    /**
     * Control type information, valid by control type
     */
    public Sensor getControlSensor();           // controlling Sensor if SENSOR_CONTROL
    public int getControlSensorSense();         // sense of Sensor for Light ON
//    public Schedule getFastClockSchedule();     // On/Off schedule if FAST_CLOCK_CONTROL 
//    public Switch getControlSwitch();           // controlling panel switch if PANEL_SWITCH_CONTROL
    public SignalHead getControlSignalHead();   // controlling signal head if SIGNAL_HEAD_CONTROL
    public int getControlSignalHeadAspect();    // signal head aspect corresponding to this Light ON
    public Turnout getControlTurnout();         // turnout whose status is shown if TURNOUT_STATUS_CONTROL
    public int getControlTurnoutState();        // turnout state corresponding to this Light ON

    public void setControlSensor(Sensor sensor);        // controlling Sensor if SENSOR_CONTROL
    public void setControlSensorSense(int sense);       // sense of Sensor for Light ON
//    public void setFastClockSchedule(Schedule s);       // On/Off schedule if FAST_CLOCK_CONTROL 
//    public void setControlSwitch(Switch switch);        // controlling panel switch if PANEL_SWITCH_CONTROL
    public void setControlSignalHead(SignalHead sh);    // controlling signal head if SIGNAL_HEAD_CONTROL
    public void setControlSignalHeadAspect(int aspect); // signal head aspect corresponding to this Light ON
    public void setControlTurnout(Turnout t);           // turnout whose status is shown if TURNOUT_STATUS_CONTROL
    public void setControlTurnoutState(int ts);         // turnout state corresponding to this Light ON
    
    /**
     * Activates a light by control type.  This method tests the 
     *   control type, and set up a control mechanism, appropriate 
     *   for the control type.  Some lights, e.g. signal head lights,
     *   are controlled by the signal head, so no activation is needed
     *   here.
     */
    public void activateLight();
}

/* @(#)Light.java */
