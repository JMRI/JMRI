package jmri;

import java.util.Date;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Each LightControl object is linked to a specific Light, and provides one of
 * the controls available for switching the Light ON/OFF in response to time or
 * events occurring on the layout.
 * <p>
 * Each LightControl holds the information for one control of the parent Light.
 * <p>
 * Each Light may have as many controls as desired by the user. It is the user's
 * responsibility to ensure that the various control mechanisms do not conflict
 * with one another.
 * <p>
 * Available control types are those defined in the Light.java interface.
 * Control types: SENSOR_CONTROL FAST_CLOCK_CONTROL TURNOUT_STATUS_CONTROL
 * TIMED_ON_CONTROL TWO_SENSOR_CONTROL
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Dave Duchamp      Copyright (C) 2010
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public interface LightControl {

    /**
     * Get the control type used by the Control
     *
     * @return the Control Type, eg. FAST_CLOCK_CONTROL
     */
    public int getControlType();

    /**
     * Set the control type used by the Control
     * Does NOT update any changelisteners
     *
     * @param type the Control Type, eg. FAST_CLOCK_CONTROL
     */
    public void setControlType(int type);

    /**
     * Set Sensor 1 used by the 1 Sensor and 2 Sensor Control
     * Does NOT update any changelisteners
     * If Sensor not present and name not empty, is provided by the SensorManager
     * when #activateLightControl() is called
     *
     * @param sensorName the Sensor name
     */
    public void setControlSensorName(String sensorName);

    /**
     * Get the Sensor State used by the 1 Sensor Control
     *
     * @return Sensor.ACTIVE or Sensor.INACTIVE
     */
    public int getControlSensorSense();

    /**
     * Get the Sensor 1 name for 1 and 2 Sensor Control Types.
     *
     * @return  If a Sensor is registered, returns the Sensor.getName()
     *          else the Sensor Name as set by #setControlSensorName
     */
    public String getControlSensorName();

    /**
     * Set the Sensor State used by the Control
     * Does NOT update any changelisteners
     *
     * @param sense The state to react to, eg. Sensor.ACTIVE or Sensor.INACTIVE
     */
    public void setControlSensorSense(int sense);

    /**
     * Get the Fast Clock On Hour.
     *
     * @return  On Hour value
     */
    public int getFastClockOnHour();

    /**
     * Get the Fast Clock On Minute.
     *
     * @return  On Minute value
     */
    public int getFastClockOnMin();

    /**
     * Get the Fast Clock On Hours and Minutes Combined
     * Convenience method of separate getFastClockOnHour() and getFastClockOnMin()
     * @return  Total combined Minute value
     */
    public int getFastClockOnCombined();

    /**
     * Get the Fast Clock Off Hour.
     *
     * @return  Off Hour value
     */
    public int getFastClockOffHour();

    /**
     * Get the Fast Clock Off Minute.
     *
     * @return  Off Minute value
     */
    public int getFastClockOffMin();
    
    /**
     * Get the Fast Clock Off Hours and Minutes Combined
     * Convenience method of separate getFastClockOnHour() and getFastClockOnMin()
     * @return  Total combined Minute value
     */
    public int getFastClockOffCombined();

    /**
     * Set a Fast Clock LightControl Schedule.
     *
     * @param onHour Hour the Light should switch On
     * @param onMin Minute the Light should switch On
     * @param offHour Hour the Light should switch Off
     * @param offMin Minute the Light should switch Off     * 
     */
    public void setFastClockControlSchedule(int onHour, int onMin, int offHour, int offMin);

    /**
     * Get the LightControl Turnout Name.
     *
     * @return  The Turnout name
     */
    public String getControlTurnoutName();

    /**
     * Set the Turnout used by the Control
     * Does NOT update any changelisteners
     * <p>
     * A Turnout of this name is provided by the TurnoutManager 
     * on LightControl Initialisation
     *
     * @param turnoutName The Turnout name
     */
    public void setControlTurnout(String turnoutName);

    /**
     * Get the LightControl Turnout Name.
     *
     * @return  The Turnout name
     */
    public int getControlTurnoutState();

    /**
     * Set the Turnout State used by the Control
     * Does NOT update any changelisteners
     *
     * @param state Turnout state to act on, eg. Turnout.CLOSED or Turnout.THROWN
     */
    public void setControlTurnoutState(int state);

    /**
     * Get the Timed On Trigger Sensor name.
     *
     * @return  The Sensor Name as set by #setControlTimedOnSensorName
     */
    public String getTimedSensorName();

    /**
     * Get the Timed On Trigger Sensor name.
     *
     * @return  If a Sensor is registered, returns the Sensor.getName()
     *          else the Sensor Name as set by #setControlTimedOnSensorName
     */
    public String getControlTimedOnSensorName();

    /**
     * Set Sensor used by the Timed On Control
     * Does NOT update any changelisteners
     *
     * @param sensorName the Sensor name to be used for the On Trigger
     */
    public void setControlTimedOnSensorName(String sensorName);

    /**
     * Get the Timed On Control Duration
     *
     * @return duration in ms
     */
    public int getTimedOnDuration();

    /**
     * Set Duration used by the Timed On Control
     * Does NOT update any changeListeners
     *
     * @param duration in ms following the Sensor On Trigger
     */
    public void setTimedOnDuration(int duration);

    /**
     * Get the Second Sensor name.
     * as used in the 2 Sensor Control Group.
     *
     * @return  If a 2nd Sensor is registered, returns the Sensor.getName()
     *          else the 2nd Sensor Name as set by #setControlSensor2Name
     */
    public String getControlSensor2Name();

    /**
     * Set Sensor 2 used by the 2 Sensor Control
     * Does NOT update any changelisteners
     *
     * @param sensorName the Sensor 2 name
     */
    public void setControlSensor2Name(String sensorName);

    /**
     * Set Light to control
     * Does NOT update any changelisteners
     *
     * @param l the Light object to control
     */
    public void setParentLight(Light l);

    /**
     * Get a Textual Description
     * eg. Light Control TestLight ON when TestSensor is Active
     * eg. Light Control ON at 14:00, OFF at 15:00.
     * 
     * @param lightName the Light Name, can be empty.
     * @return An I18N full-text description of thiscontrol
     */
    public String getDescriptionText(String lightName);

    /**
     * Activates a Light Control by control type. This method tests the control
     * type, and set up a control mechanism, appropriate for the control type.
     * Adds PropertyChangeListeners to Sensors / Turnout / Fast Clock as necessary
     */
    public void activateLightControl();
    
    /**
     * Check to see if we have the FastClock Follower has unique times for a single Light Control.
     * <p>
     * Hour / Minute combination must be unique for each Light to avoid flicker.
     * 
     * @return true if the clock on time equals the off time, otherwise false.
     */
    public boolean onOffTimesFaulty();
    
    /**
     * Check to see if we have the FastClock Follower has unique times for a single Light.
     * <p>
     * Hour / Minute combination must be unique for each Light to avoid flicker.
     * 
     * @param compareList the ArrayList of other Light Controls to compare against
     * @return true if there are multiple exact same times
     */
    public boolean areFollowerTimesFaulty( List<LightControl> compareList );
    
    /**
     * Deactivates a LightControl by control type. This method tests the control
     * type, and deactivates the control mechanism, appropriate for the control
     * type.
     */
    public void deactivateLightControl();

}
