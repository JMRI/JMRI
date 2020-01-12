package jmri;

/**
 * Routes represent a collection of Turnouts that may be set at the same time.
 * <p>
 * When a user adds a Turnout to a Route, the user specifies whether the Turnout
 * state is to be set to CLOSED or THROWN when the Route is invoked (set).
 * <p>
 * Initially, Routes will have a fixed maximum number of sensors for simplicity
 * of implementation. We can revise this later to use Java Collections if this
 * becomes a problem.
 * <p>
 * To allow control via fascia panel pushbuttons, Routes may optionally be
 * invoked by one or more Sensors (up to the maximum allowed).
 * <p>
 * A route can be enabled or not. By default it is enabled, and will act when
 * its specified input conditions become satisfied. When not enabled (the
 * enabled parameter is false), the route will not act even if the specified
 * input conditions are satisfied. When the route transitions from disabled to
 * enabled, it may act, depending on the conditions: Edge triggered conditions
 * will not be satisfied, but level-conditions may be.
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
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Simon Reader Copyright (C) 2008
 */
public interface Route extends NamedBean {

    public static final int TOGGLE = 0x08;
    static final int MAX_CONTROL_SENSORS = 3;

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
     * Set enabled status.
     *
     * @param state true if enabled; false otherwise
     */
    public void setEnabled(boolean state);

    /**
     * Get enabled status.
     *
     * @return true if enabled; false otherwise
     */
    public boolean getEnabled();

    /**
     * Set locked status.
     *
     * @param state true if locked; false otherwise
     */
    public void setLocked(boolean state);

    /**
     * Get locked status.
     *
     * @return true if locked; false otherwise
     */
    public boolean getLocked();

    /**
     * Has at least one lockable turnout.
     *
     * @return true if lockable; false otherwise
     */
    public boolean canLock();

    /**
     * Add an output Turnout to this Route.
     *
     * @param systemName The turnout system name
     * @param state      must be Turnout.CLOSED, Turnout.THROWN, or
     *                   Route.TOGGLE, which determines how the Turnout is to be
     *                   switched when this Route is set
     * @return true if the output turnout was added
     */
    public boolean addOutputTurnout(String systemName, int state);

    /**
     * Delete all output Turnouts from this Route.
     */
    public void clearOutputTurnouts();

    public int getNumOutputTurnouts();

    /**
     * Inquire if a Turnout is included in this Route as an output.
     *
     * @param systemName the system name of the turnout
     * @return true if the named turnout is an output; false otherwise
     */
    public boolean isOutputTurnoutIncluded(String systemName);

    /**
     * get the Set State of an output Turnout.
     *
     * @param systemName the system name of the turnout
     * @return the state or -1 if the Turnout is not found
     */
    public int getOutputTurnoutSetState(String systemName);

    /**
     * Get an output Turnout system name by index.
     *
     * @param index the index of the turnout
     * @return the turnout system name or null if no turnout exists at index
     */
    public String getOutputTurnoutByIndex(int index);

    /**
     * Get the output Turnout by index.
     *
     * @param index the index of the turnout
     * @return the turnout or null if no turnout exists at index
     */
    public Turnout getOutputTurnout(int index);

    /**
     * Get the desired state of the Turnout by index.
     *
     * @param index the index of the turnout
     * @return the turnout state or -1 if no turnout exists at index
     */
    public int getOutputTurnoutState(int index);

    /**
     * Add an output Sensor to this Route.
     *
     * @param systemName the sensor system name
     * @param state      the state the sensor switches to when the Route is set;
     *                   must be one of Sensor.ACTIVE, Sensor.INACTIVE, or
     *                   Route.TOGGLE
     * @return true if the sensor was added; false otherwise
     */
    public boolean addOutputSensor(String systemName, int state);

    /**
     * Delete all output Sensors from this Route.
     */
    public void clearOutputSensors();

    public int getNumOutputSensors();

    /**
     * Inquire if a Sensor is included in this Route as an output.
     *
     * @param systemName the Sensor system name
     * @return true if the sensor is an output in this Route
     */
    public boolean isOutputSensorIncluded(String systemName);

    /**
     * Get the Set State of an output Sensor.
     *
     * @param systemName the system name of the Sensor
     * @return -1 if the Sensor is not found
     */
    public int getOutputSensorSetState(String systemName);

    /**
     * Get an output Sensor system name by index.
     *
     * @param index the index of the sensor
     * @return the sensor or null if no sensor exists at index
     */
    public String getOutputSensorByIndex(int index);

    /**
     * Get the output Sensor by index.
     *
     * @param index the index of the sensor
     * @return the sensor or null if no sensor exists at index
     */
    public Sensor getOutputSensor(int index);

    /**
     * Get the desired state of an output Sensor by index.
     *
     * @param index the index of the sensor
     * @return the sensor state or -1 if no sensor exists at index
     */
    public int getOutputSensorState(int index);

    /**
     * Set name of script file to be run when Route is fired.
     *
     * @param filename path to script
     */
    public void setOutputScriptName(String filename);

    /**
     * Get name of script file to be run when Route is fired.
     *
     * @return script path or null if not defined
     */
    public String getOutputScriptName();

    /**
     * Set name of sound file to be played when Route is fired.
     *
     * @param filename path to sound
     */
    public void setOutputSoundName(String filename);

    /**
     * Get name of sound file to be played when Route is fired.
     *
     * @return sound file path or null if not defined
     */
    public String getOutputSoundName();

    /**
     * Set a sensor to be the turnouts aligned sensor.
     *
     * @param sensorSystemName the system name of the sensor; pass null to
     *                         disassociate any sensor from this route
     */
    public void setTurnoutsAlignedSensor(String sensorSystemName);

    /**
     * Get the system name of the turnouts aligned sensor.
     *
     * @return the name or null if not defined
     */
    public String getTurnoutsAlignedSensor();

    /**
     * Get the turnouts aligned sensor.
     *
     * @return the sensor or null if not defined
     */
    public Sensor getTurnoutsAlgdSensor();

    /**
     * Add a Sensor to the list of control Sensors for this Route.
     *
     * @param sensorSystemName system name of the sensor
     * @param mode             the default state of the sensor
     * @return true if added; false otherwise
     */
    public boolean addSensorToRoute(String sensorSystemName, int mode);

    /**
     * Delete all control Sensors from this Route.
     */
    public void clearRouteSensors();

    /**
     * Get the SystemName of a control Sensor in this Route.
     *
     * @param index The index in the Sensor array of the requested Sensor
     * @return null If there is no Sensor at index
     */
    public String getRouteSensorName(int index);

    /**
     * Get the Sensor of a control Sensor in this Route.
     *
     * @param index The index in the Sensor array of the requested Sensor
     * @return null If there is no Sensor with at index
     */
    public Sensor getRouteSensor(int index);

    /**
     * Get the state of a particular Sensor in this Route.
     *
     * @param index The index in the Sensor array of the requested Sensor
     * @return ONACTIVE if there is no Sensor with at index
     */
    public int getRouteSensorMode(int index);

    /**
     * Set the control Turnout for this Route.
     *
     * @param turnoutSystemName the system name of a turnout
     */
    public void setControlTurnout(String turnoutSystemName);

    /**
     * Get the SystemName of the control Turnout for this Route.
     *
     * @return the name of the control turnout or null if not set
     */
    public String getControlTurnout();

    /**
     * Get the Turnout of a control Turnout for this Route.
     *
     * @return the control turnout or null if not set
     */
    public Turnout getCtlTurnout();

    /**
     * Set the State of control Turnout that fires this Route.
     *
     * @param turnoutState the turnout state
     */
    public void setControlTurnoutState(int turnoutState);

    /**
     * Get the State of control Turnout that fires this Route.
     *
     * @return the turnout state
     */
    public int getControlTurnoutState();

    /**
     * Set the lock control Turnout for this Route.
     *
     * @param turnoutSystemName the system name of the turnout
     */
    public void setLockControlTurnout(String turnoutSystemName);

    /**
     * Get the SystemName of the lock control Turnout for this Route.
     *
     * @return the system name or null if not defined
     */
    public String getLockControlTurnout();

    /**
     * Get the Turnout of a lock control Turnout for this Route.
     *
     * @return the turnout or null if not defined
     */
    public Turnout getLockCtlTurnout();

    /**
     * Set the State of the lock control Turnout for this Route.
     *
     * @param turnoutState the turnout state
     */
    public void setLockControlTurnoutState(int turnoutState);

    /**
     * Get the State of the lock control Turnout that locks this Route.
     *
     * @return the turnout state
     */
    public int getLockControlTurnoutState();

    /**
     * Set the delay between issuing Turnout commands on this route.
     *
     * @param delay the delay in milliseconds
     */
    public void setRouteCommandDelay(int delay);

    /**
     * Get the delay between issuing Turnout commands on this route.
     *
     * @return the delay in milliseconds
     */
    public int getRouteCommandDelay();

    /**
     * Set the Route.
     * <p>
     * Sets all Route Turnouts to the directed state in the Route definition.
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
     * processing. A Route must be deactivated before its input and output
     * definitions are changed.
     */
    public void deActivateRoute();

}
