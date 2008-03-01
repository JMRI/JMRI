// Light.java

package jmri;

/**
 * Represent a single visible Light on the physical
 *     layout. 
 * <P>
 * Light objects require a number of instance variables.  Since 
 *     Light objects are created using the standard JMRI 
 *     systemName/userName concept, accessor routines are provided
 *     for setting and editing these instance variables.
 * <P>
 * Light objects are implemented in a hardware system independent
 *     manner.  The initial system implementation is SerialLight in
 *     the C/MRI system.
 * <P>
 * Based in part on SignalHead.java
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author			Dave Duchamp Copyright (C) 2004
 * @author			Ken Cameron Copyright (C) 2008
 * @version			$Revision: 1.10 $
 */
public interface Light extends NamedBean {

    // states
    public static final int ON          = 0x01;
    public static final int OFF         = 0x00;
    
    // control types - initially 3 types defined
    public static final int SENSOR_CONTROL          = 0x01;
    public static final int FAST_CLOCK_CONTROL      = 0x02;
    public static final int TURNOUT_STATUS_CONTROL  = 0x03;
    public static final int TIMED_ON_CONTROL		= 0x04;
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
     * for a dimmable light, uses true and false
     */
    public boolean isDimSupported();	// true if dimmable light
    public boolean isCanDim();	// true if dimmable light
    public void setCanDim(boolean flag);	// sets light as dimmiable
    public double getDimRequest();			// dim is zero to 1, returns requested dim, will differ from current when rate in effect
    public double getDimCurrent();			// dim is zero to 1
    public void setDimRequest(double v);	// dim is zero to 1
    public double getDimRate();		// time in fast minutes to go 0 to 1, 0 being immediate
    public void setDimRate(double fastMinutes);		// time in fast minutes to go 0 to 1, 0 being immediate
    public boolean hasBeenDimmed();	// init is false, triggers dim init first time a setDim is used
    public void setDimMin(double v);	// sets minimum dim level
    public double getDimMin();	// sets minimum dim level
    public void setDimMax(double v);	// sets maximum dim level
    public double getDimMax();	// sets maximum dim level
    
    /**
     * Control type information, valid by control type
     */
    public String getControlSensorName(); // controlling Sensor if SENSOR_CONTROL
    public int getControlSensorSense();         // sense of Sensor for Light ON
    
    public int getFastClockOnHour();            // on Hour if FAST_CLOCK_CONTROL
    public int getFastClockOnMin();             // on Minute if FAST_CLOCK_CONTROL
    public int getFastClockOffHour();           // off Hour if FAST_CLOCK_CONTROL
    public int getFastClockOffMin();            // off Minute if FAST_CLOCK_CONTROL
    
    public String getControlTurnoutName(); // turnout whose status is shown if TURNOUT_STATUS_CONTROL
    public int getControlTurnoutState();        // turnout state corresponding to this Light ON
    
	public String getControlTimedOnSensorName(); // trigger Sensor if TIMED_ON_CONTROL
	public int getTimedOnDuration();            // duration (milliseconds) if TIMED_ON_CONTROL

    public void setControlSensor(String sensorSystemName);  // controlling Sensor if SENSOR_CONTROL
    public void setControlSensorSense(int sense);       // sense of Sensor for Light ON
    // Set the On/Off Schedule if FAST_CLOCK_CONTROL
    public void setFastClockControlSchedule(int onHour,int onMin,int offHour, int offMin);
    
    public void setControlTurnout(String turnoutSystemName); // turnout whose status is shown if TURNOUT_STATUS_CONTROL
    public void setControlTurnoutState(int ts);         // turnout state corresponding to this Light ON
    
	public void setControlTimedOnSensor(String sensorSystemName); // trigger Sensor if TIMED_ON_CONTROL
	public void setTimedOnDuration(int duration);   // duration (milliseconds) if TIMED_ON_CONTROL

    /**
     * Set enabled status
     */
    public void setEnabled(boolean state);

    /**
     * Get enabled status
    */
    public boolean getEnabled();
    
    /**
     * Activates a light by control type.  This method tests the 
     *   control type, and set up a control mechanism, appropriate 
     *   for the control type.  
     */
    public void activateLight();
    
    /**
     * Deactivates a light by control type.  This method tests the 
     *   control type, and deactivates the control mechanism, appropriate 
     *   for the control type.  
     */
    public void deactivateLight();
}

/* @(#)Light.java */
