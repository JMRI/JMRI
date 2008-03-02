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
 * @author			Bob Jacobsen Copyright (C) 2008
 * @version			$Revision: 1.11 $
 */
public interface Light extends NamedBean {

    /** State value indicating output intensity is at or above maxIntensity */
    public static final int ON          = 0x01;
    
    /** State value indicating output intensity is at or below minIntensity */
    public static final int OFF         = 0x00;
    
    /** State value indicating output intensity is
     * less than maxIntensity and more than minIntensity, 
     * and no transition is in progress */
    public static final int INTERMEDIATE         = 0x02;
    
    
    /** State value indicating output intensity is currently changing toward higher intensity, and will
        continue until full ON is reached */
    public static final int TRANSITIONINGTOFULLON   = 0x38;
    
    /** State value indicating output intensity is currently changing toward higher intensity. The current
        transition will stop before full ON is reached. */
    public static final int TRANSITIONINGHIGHER 	      = 0x28;
    
    /** State value indicating output intensity is currently changing toward lower intensity. The current
        transition will stop before full OFF is reached. */
    public static final int TRANSITIONINGLOWER        = 0x18;
    
    /** State value indicating output intensity is currently changing toward lower intensity, and will
        continue until full OFF is reached */
    public static final int TRANSITIONINGTOFULLOFF = 0x08;
    
    /** State value mask representing status where output is changing due to a request to transition. */
    public static final int TRANSITIONING         = 0x08;
    
    
    /**
     * Set the demanded output state. Valid values are ON and OFF.
     * ON corresponds to the maxIntensity setting, and OFF
     * corresponds to minIntensity.
     * <p>
     * Bound parameter.
     * <p>
     * Note that the state may have other values, such as INTERMEDIATE
     * or a form of transitioning, but that these may not be directly set.
     * <p>
     * @throws IllegalArgumentException if invalid newState provided
    */
    public void setState(int newState);
    
    /**
       Get the current state of the Light's output.
    */
    public int getState();
    
    // control types - initially 3 types defined
    public static final int SENSOR_CONTROL          = 0x01;
    public static final int FAST_CLOCK_CONTROL      = 0x02;
    public static final int TURNOUT_STATUS_CONTROL  = 0x03;
    public static final int TIMED_ON_CONTROL		= 0x04;
    public static final int NO_CONTROL              = 0x00;
    
    /** 
     * Control type is an instance variable.  Its value is one of the
     *      types noted above.
     */
    public int getControlType();
    public void setControlType(int controlType);
    
    /** Check if this object can handle variable intensity.
        <P>
        Unbound property.
        @return false if only ON/OFF is available.
    */
    public boolean isIntensityVariable();
    
   /** Set the intended new intensity value for the Light.
    *  If transitions are in use, they will be applied.
    *  <p>
    *  Bound property between 0 and 1. 
    *  <p>
    *  A value of 0.0 corresponds to full off, and 
    *  a value of 1.0 corresponds to full on.
    *  <p>
    *  Values at or below
    *  the MinIntensity property will result in the Light going
    *  to the OFF state at the end of the transition. 
    *  Values at or above the MaxIntensity property
    *  will result in the Light going to the ON state at the end
    *  of the transition.  
    * <p>
    *  All others result in the INTERMEDIATE state. 
    * <p>
    *  Light implementations
    *  with isIntensityVariable false may not have their TargetIntensity
    *  set to values between MinIntensity and MaxIntensity, which would
    *  result in the INTERMEDIATE state, as that is invalid for them.
    *  <P>
    *  If a non-zero value is set in the transitionTime property,
    *  the state will be one of TRANSITIONTOFULLON, TRANSITIONHIGHER, TRANSITIONLOWER
    *  or TRANSITIONTOFULLOFF until the transition is complete.
    *  <P>
    *  @throws IllegalArgumentException when intensity is less than 0.0 or more than 1.0
    *  @throws IllegalArgumentException if isIntensityVariable is false 
    *       and the new value is between MaxIntensity and MinIntensity
    */
    public void setTargetIntensity(double intensity);
    
    /** Get the current intensity value.
     * If the Light is currently transitioning, this may be either
     * an intermediate or final value.
     * <p>
     * A value of 0.0 corresponds to full off, and 
     * a value of 1.0 corresponds to full on.
    */
    public double getCurrentIntensity();
    
    /** Get the target intensity value for the 
     * current transition, if any. If the Light is not currently
     * transitioning, this is the current intensity value.
     * <p>
     * A value of 0.0 corresponds to full off, and 
     * a value of 1.0 corresponds to full on.
     * <p>
     * Bound property
    */
    public double  getTargetIntensity();
    
    
    /** Set the value of the maxIntensity property.
     * <p>
     * Bound property between 0 and 1. 
     * <p>
     * A value of 0.0 corresponds to full off, and 
     * a value of 1.0 corresponds to full on.
     * @throws IllegalArgumentException when intensity is less than 0.0 or more than 1.0
     * @throws IllegalArgumentException when intensity is not greater than the current value of the minIntensity property
    */
    public void setMaxIntensity(double intensity);
    
    /** Get the current value of the maxIntensity property.
     * <p>
     * A value of 0.0 corresponds to full off, and 
     * a value of 1.0 corresponds to full on.
    */
    public double getMaxIntensity();
    
    /** Set the value of the minIntensity property.
     * <p>
     * Bound property between 0 and 1. 
     * <p>
     * A value of 0.0 corresponds to full off, and 
     * a value of 1.0 corresponds to full on.
     * @throws IllegalArgumentException when intensity is less than 0.0 or more than 1.0
     * @throws IllegalArgumentException when intensity is not less than the current value of the maxIntensity property
    */
    public void setMinIntensity(double intensity);
    
    /** Get the current value of the minIntensity property.
        <p>
        A value of 0.0 corresponds to full off, and 
        a value of 1.0 corresponds to full on.
    */
    public double getMinIntensity();
    
    /** Can the Light change it's intensity setting slowly?
        <p>
        If true, this Light supports a non-zero value of the 
        transitionTime property, which controls how long the Light
        will take to change from one intensity level to another.
        <p>
        Unbound property
    */
    public boolean isTransitionAvailable();
    
    /** Set the fast-clock duration for a 
        transition from full ON to full OFF or vice-versa.
        <P>
        Note there is no guarantee of how this scales when other
        changes in intensity take place.  In particular, some Light implementations
        will change at a constant fraction per fastclock minute and some will take
        a fixed duration regardless of the size of the intensity change.
        <p>
        Bound property
        <p>
        @throws IllegalArgumentException if isTransitionAvailable() is false and minutes is not 0.0
        @throws IllegalArgumentException if minutes is negative
    */
    public void setTransitionTime(double minutes);
    
    /** Get the number of fastclock minutes taken by a transition from
        full ON to full OFF or vice versa.
        <p>
        @return 0.0 if the output intensity transition is instantaneous
    */
    public double getTransitionTime();
    
    /** 
        Convenience method for checking if the intensity of the light is currently
        changing due to a transition.
        <p>
        Bound property so that listeners can conveniently learn when the transition is over.
    */
    public boolean isTransitioning();
    
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
