// Route.java

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
 *
 * @author			Dave Duchamp Copyright (C) 2004
 * @version			$Revision: 1.3 $
 */
public interface Route extends NamedBean {

    public static final int MAX_TURNOUTS_PER_ROUTE = 50;
    public static final int MAX_CONTROL_SENSORS = 3;

    /**
     * Method to add a Turnout to the list of Turnouts in this Route
     * 'turnoutState' must be Turnout.CLOSED or Turnout.THROWN, depending
     *      on how the Turnout is to be switched when this Route is set
     */
    public boolean addTurnoutToRoute(String turnoutSystemName, int turnoutState);

    /**
     * Method to delete all Turnouts from the list of Turnouts in this Route
     */
    public void clearRouteTurnouts();
    
    /**
     * Method to get a Route Turnout System Name by Index
     *  Returns null if there is no turnout with that index
     */
    public String getRouteTurnoutByIndex(int index);

    /**
     * Method to inquire if a Turnout is included in this Route
     */
    public boolean isTurnoutIncluded(String turnoutSystemName);

    /**
     * Method to get the Set State of a Turnout included in this Route
     *   If the Turnout is not found, -1 is returned.
     */
    public int getTurnoutSetState(String turnoutSystemName);

    /**
     * Method to add a Sensor to the list of control Sensors for this Route
     */
    public boolean addSensorToRoute(String sensorSystemName, int mode);

    static final int ONACTIVE = 0;    // route fires if sensor goes active
    static final int ONINACTIVE = 1;  // route fires if sensor goes inactive
    static final int VETOACTIVE = 2;  // sensor must be active for route to fire
    static final int VETOINACTIVE = 3;  // sensor must be inactive for route to fire
    
    /**
     * Method to delete all control Sensors from this Route
     */
    public void clearRouteSensors();

    /**
     * Method to get the SystemName of a control Sensor in this Route
     *  'index' is the index in the Sensor array of the requested 
     *      Sensor.  
     *  If there is no Sensor with that 'index', or if 'index'
     *      is not in the range 0 thru MAX_SENSORS-1, null is returned.
     */
    public String getRouteSensorName(int index);
    
    /**
     * Method to get the mode of a particular Sensor in this Route
     *  'index' is the index in the Sensor array of the requested 
     *      Sensor.  
     *  If there is no Sensor with that 'index', or if 'index'
     *      is not in the range 0 thru MAX_SENSORS-1, ONACTIVE is returned.
     */
    public int getRouteSensorMode(int index);

    /**
     * Method to set the SystemName of a control Turnout for this Route
     */
    public void setControlTurnout(String turnoutSystemName);

    /**
     * Method to get the SystemName of a control Turnout for this Route
     */
    public String getControlTurnout();

    /**
     * Method to set the State of control Turnout that fires this Route
     */
    public void setControlTurnoutState(int turnoutState);

    /**
     * Method to get the State of control Turnout that fires this Route
     */
    public int getControlTurnoutState();

    /**
     * Method to set the Route
     * Sets all Route Turnouts to the state shown in the Route definition
     */
    public void setRoute();

    /**
     * Method to activate the Route via Sensors
     * Sets up for Route activation based on a list of Sensors
     */
    public void activateRoute();

    /**
     * Method to deactivate the Route via Sensors
     * Deactivates Route based on a list of Sensors
     */
    public void deActivateRoute();

}

/* @(#)Route.java */
