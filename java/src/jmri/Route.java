package jmri;

/**
 * Routes represent a collection of Turnouts that may be set at the same time.
 * <P>
 * When a user adds a Turnout to a Route, the user specifies whether the Turnout
 * state is to be set to CLOSED or THROWN when the Route is invoked (set).
 * <P>
 * Initially, Routes will have a fixed maximum number of sensors for simplicity
 * of implementation. We can revise this later to use Java Collections if this
 * becomes a problem.
 * <P>
 * To allow control via fascia panel pushbuttons, Routes may optionally be
 * invoked by one or more Sensors (up to the maximum allowed).
 * <p>
 * A route can be enabled or not. By default it is enabled, and will act when
 * it's specified input conditions become satisfied. When not enabled (the
 * enabled parameter is false), the route will not act even if the specified
 * input conditions are satisfied. When the route transitions from disabled to
 * enabled, it may act, depending on the conditions: Edge triggered conditions
 * will not be satisfied, but level-conditions may be.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @author	Bob Jacobsen Copyright (C) 2007
 * @author Simon Reader Copyright (C) 2008
 *
 * @version	$Revision$
 */
public interface Route extends NamedBean {

    public static final int TOGGLE = 0x08;
    static final int MAX_CONTROL_SENSORS = 3;

    /**
     * Set enabled status.
     */
    public void setEnabled(boolean state);

    /**
     * Get enabled status
     */
    public boolean getEnabled();

    /**
     * Set locked status.
     */
    public void setLocked(boolean state);

    /**
     * Get locked status.
     */
    public boolean getLocked();

    /**
     * Has at least one lockable turnout.
     */
    public boolean canLock();
    // new interface for outputs 

    /**
     * Add an output Turnout to this Route.
     *
     * @param systemName The turnout system name
     * @param state      must be Turnout.CLOSED, Turnout.THROWN, or
     *                   Route.TOGGLE, which determines how the Turnout is to be
     *                   switched when this Route is set
     */
    public boolean addOutputTurnout(String systemName, int state);

    /**
     * Delete all output Turnouts from this Route.
     */
    public void clearOutputTurnouts();

    public int getNumOutputTurnouts();

    /**
     * Inquire if a Turnout is included in this Route as an output.
     */
    public boolean isOutputTurnoutIncluded(String systemName);

    /**
     * Method to get the Set State of an output Turnout.
     *
     * @return -1 if the Turnout is not found
     */
    public int getOutputTurnoutSetState(String systemName);

    /**
     * Get an output Turnout system name by Index.
     *
     * @return null if there is no turnout with that index
     */
    public String getOutputTurnoutByIndex(int index);

    /**
     * Method to get the 'k'th output Turnout of the Route.
     *
     * @return null if there are less than 'k' Turnouts defined
     */
    public Turnout getOutputTurnout(int k);

    /**
     * Method to get the desired state of 'k'th Turnout of the Route.
     *
     * @return -1 if there are less than 'k' Turnouts defined
     */
    public int getOutputTurnoutState(int k);

    /**
     * Add an output Sensor to this Route.
     *
     * @param systemName The sensor system name
     * @param state      must be Sensor.ACTIVE, Sensor.INACTIVE, or
     *                   Route.TOGGLE, which determines how the Sensor is to be
     *                   switched when this Route is set
     */
    public boolean addOutputSensor(String systemName, int state);

    /**
     * Delete all output Sensors from this Route.
     */
    public void clearOutputSensors();

    public int getNumOutputSensors();

    /**
     * Inquire if a Sensor is included in this Route as an output.
     */
    public boolean isOutputSensorIncluded(String systemName);

    /**
     * Method to get the Set State of an output Sensor.
     *
     * @return -1 if the Sensor is not found
     */
    public int getOutputSensorSetState(String systemName);

    /**
     * Get an output Sensor system name by Index.
     *
     * @return null if there is no sensor with that index
     */
    public String getOutputSensorByIndex(int index);

    /**
     * Get the 'k'th output Sensor of the Route.
     *
     * @return null if there are less than 'k' Sensor defined
     */
    public Sensor getOutputSensor(int k);

    /**
     * Get the desired state of 'k'th Sensor of the Route.
     *
     * @return -1 if there are less than 'k' Sensors defined
     */
    public int getOutputSensorState(int k);

    /**
     * Set name of script file to be run when Route is fired.
     */
    public void setOutputScriptName(String filename);

    /**
     * Get name of script file to be run when Route is fired.
     */
    public String getOutputScriptName();

    /**
     * Set name of sound file to be played when Route is fired.
     */
    public void setOutputSoundName(String filename);

    /**
     * Get name of sound file to be played when Route is fired.
     */
    public String getOutputSoundName();

    /**
     * Method to set turnouts aligned sensor
     */
    public void setTurnoutsAlignedSensor(String sensorSystemName);

    /**
     * Method to get system name of turnouts aligned sensor.
     */
    public String getTurnoutsAlignedSensor();

    /**
     * Method to get sensor of turnouts aligned sensor.
     */
    public Sensor getTurnoutsAlgdSensor();

    // Interface for control inputs
    /**
     * Method to add a Sensor to the list of control Sensors for this Route.
     */
    public boolean addSensorToRoute(String sensorSystemName, int mode);

    static final int ONACTIVE = 0;    // route fires if sensor goes active
    static final int ONINACTIVE = 1;  // route fires if sensor goes inactive
    static final int VETOACTIVE = 2;  // sensor must be active for route to fire
    static final int VETOINACTIVE = 3;  // sensor must be inactive for route to fire

    static final int ONCHANGE = 32;   // route fires if turnout or sensor changes

    static final int ONCLOSED = 2;    // route fires if turnout goes closed
    static final int ONTHROWN = 4;  // route fires if turnout goes thrown
    static final int VETOCLOSED = 8;  // turnout must be closed for route to fire
    static final int VETOTHROWN = 16;  // turnout must be thrown for route to fire

    /**
     * Method to delete all control Sensors from this Route.
     */
    public void clearRouteSensors();

    /**
     * Method to get the SystemName of a control Sensor in this Route.
     *
     * @param index The index in the Sensor array of the requested Sensor.
     * @return null If there is no Sensor with that 'index', or if 'index' is
     *         not in the range 0 thru MAX_SENSORS-1.
     */
    public String getRouteSensorName(int index);

    /**
     * Method to get the Sensor of a control Sensor in this Route.
     *
     * @param index The index in the Sensor array of the requested Sensor.
     * @return null If there is no Sensor with that 'index', or if 'index' is
     *         not in the range 0 thru MAX_SENSORS-1.
     */
    public Sensor getRouteSensor(int index);

    /**
     * Method to get the mode of a particular Sensor in this Route.
     *
     * @param index The index in the Sensor array of the requested Sensor.
     * @return ONACTIVE if there is no Sensor with that 'index', or if 'index'
     *         is not in the range 0 thru MAX_SENSORS-1
     */
    public int getRouteSensorMode(int index);

    /**
     * Method to set the SystemName of a control Turnout for this Route.
     */
    public void setControlTurnout(String turnoutSystemName);

    /**
     * Method to get the SystemName of a control Turnout for this Route.
     */
    public String getControlTurnout();

    /**
     * Method to get the Turnout of a control Turnout for this Route.
     */
    public Turnout getCtlTurnout();

    /**
     * Method to set the State of control Turnout that fires this Route.
     */
    public void setControlTurnoutState(int turnoutState);

    /**
     * Method to get the State of control Turnout that fires this Route.
     */
    public int getControlTurnoutState();

    /**
     * Method to set the SystemName of a lock control Turnout for this Route.
     */
    public void setLockControlTurnout(String turnoutSystemName);

    /**
     * Method to get the SystemName of a lock control Turnout for this Route.
     */
    public String getLockControlTurnout();

    /**
     * Method to get the Turnout of a lock control Turnout for this Route.
     */
    public Turnout getLockCtlTurnout();

    /**
     * Method to set the State of the lock control Turnout that locks this
     * Route.
     */
    public void setLockControlTurnoutState(int turnoutState);

    /**
     * Method to get the State of the lock control Turnout that locks this
     * Route.
     */
    public int getLockControlTurnoutState();

    /**
     * Method to set delay (milliseconds) between issuing Turnout commands.
     */
    public void setRouteCommandDelay(int delay);

    /**
     * Method to get delay (milliseconds) between issuing Turnout commands.
     */
    public int getRouteCommandDelay();

    /**
     * Method to set the Route.
     * <p>
     * Sets all Route Turnouts to the state shown in the Route definition.
     */
    public void setRoute();

    /**
     * Activate the Route.
     * <p>
     * This starts route processing by connecting to inputs, etc. A Route must
     * be activated before it will fire.
     */
    public void activateRoute();

    /**
     * Deactivate the Route.
     * <p>
     * This disconnects the Route from all other objects and stops it from
     * processing. A Route must be deactivated before it's input and output
     * definitions are changed.
     */
    public void deActivateRoute();

}
