// DefaultRoute.java

package jmri;

 /**
 * Class providing the basic logic of the Route interface.
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @version     $Revision: 1.2 $
 */
public class DefaultRoute extends AbstractNamedBean
    implements Route, java.io.Serializable {

    public DefaultRoute(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultRoute(String systemName) {
        super(systemName);
    }

    /**
     *  Persistant instance variables (saved between runs)
     */
    protected String[] mRouteTurnout = new String[MAX_TURNOUTS_PER_ROUTE];
    protected int[] mRouteTurnoutState = new int[MAX_TURNOUTS_PER_ROUTE];
    protected String[] mControlSensors = new String[MAX_CONTROL_SENSORS];

    /**
     *  Operational instance variables (not saved between runs)
     */
    protected int mNumTurnouts = 0;
    protected int mNumSensors = 0;
    protected Sensor[] mSensors = new Sensor[MAX_CONTROL_SENSORS];
    protected java.beans.PropertyChangeListener[] mSensorListener = 
                    new java.beans.PropertyChangeListener[MAX_CONTROL_SENSORS];
     
    /**
     * Method to add a Turnout to the list of Turnouts in this Route
     * 'turnoutState' must be Turnout.CLOSED or Turnout.THROWN, depending
     *      on how the Turnout is to be switched when this Route is set
     */
    public boolean addTurnoutToRoute(String turnoutSystemName, int turnoutState) {
        if (mNumTurnouts >= MAX_TURNOUTS_PER_ROUTE) {
            // reached maximum
            log.warn("Reached maximum number of turnouts for Route: "+getSystemName() );
            return false;
        }
        if ((turnoutState!=Turnout.THROWN) && (turnoutState!=Turnout.CLOSED)) {
            log.warn("Illegal Turnout state for Route: "+getSystemName() );
            return false;
        }        
        mRouteTurnout[mNumTurnouts] = turnoutSystemName;
        mRouteTurnoutState[mNumTurnouts] = turnoutState;
        mNumTurnouts ++;
        return true;
    }

    /**
     * Method to add a Sensor to the list of control Sensors for this Route
     */
    public boolean addSensorToRoute(String sensorSystemName) {
        if (mNumSensors >= MAX_CONTROL_SENSORS) {
            // reached maximum
            log.warn("Reached maximum number of control Sensors for Route: "+
                                                            getSystemName() );
            return false;
        }
        mControlSensors[mNumSensors] = sensorSystemName;
        mNumSensors ++;
        return true;
    }
    
    /**
     * Method to delete all Turnouts from the list of Turnouts in this Route
     */
    public void clearRouteTurnouts() {
        mNumTurnouts = 0;
    }

    /**
     * Method to delete all control Sensors from this Route
     */
    public void clearRouteSensors() {
        mNumSensors = 0;
    }
    
    /**
     * Method to get a Route Turnout System Name by Index
     *  Returns null if there is no turnout with that index
     */
    public String getRouteTurnoutByIndex(int index) {
        if ( (index >= mNumTurnouts) || (index < 0) ) {
            return null;
        }
        return mRouteTurnout[index];
    }
    
    /**
     * Method to inquire if a Turnout is included in this Route
     */
    public boolean isTurnoutIncluded(String turnoutSystemName) {
        for (int i = 0; i<mNumTurnouts; i++) {
            if( turnoutSystemName.equals(mRouteTurnout[i]) ) {
                // Found turnout
                return true;
            }
        }
        return false;
    }

    /**
     * Method to get the Set State of a Turnout included in this Route
     *   If the Turnout is not found, -1 is returned.
     */
    public int getTurnoutSetState(String turnoutSystemName) {
        for (int i = 0; i<mNumTurnouts; i++) {
            if( turnoutSystemName.equals(mRouteTurnout[i]) ) {
                // Found turnout
                return mRouteTurnoutState[i];
            }
        }
        return -1;
    }

    /**
     * Method to get the SystemName of a control Sensor in this Route
     *  'index' is the index in the Sensor array of the requested 
     *      Sensor.  
     *  If there is no Sensor with that 'index', or if 'index'
     *      is not in the range 0 thru MAX_SENSORS-1, null is returned.
     */
    public String getRouteSensor(int index) {
        if (index < 0 || index >= mNumSensors) {
            return (null);
        }
        return (mControlSensors[index]);
    }

    /**
     * Method to set the Route
     * Sets all Route Turnouts to the state shown in the Route definition
     */
    public void setRoute() {
        if (mNumTurnouts>0) {
            for (int k = 0; k < mNumTurnouts; k++) {
                Turnout t = InstanceManager.turnoutManagerInstance().
                                            getBySystemName(mRouteTurnout[k]);
                if ( (t!=null) && (t.getKnownState() != mRouteTurnoutState[k]) ) {
                    t.setCommandedState(mRouteTurnoutState[k]);
                }
            }
        }
    }

    /**
     * Method to activate the Route via Sensors
     * Sets up for Route activation based on a list of Sensors
     */
    public void activateRoute() {
        if (mNumSensors>0) {
            for (int k = 0;k < mNumSensors;k++) {
                mSensors[k] = InstanceManager.sensorManagerInstance().
                                            getBySystemName(mControlSensors[k]);
                if (mSensors[k]!=null) {
                    mSensors[k].addPropertyChangeListener(mSensorListener[k] =
                                                new java.beans.PropertyChangeListener() {
                            public void propertyChange(java.beans.PropertyChangeEvent e) {
                                if (e.getPropertyName().equals("KnownState")) {
                                    int now = ((Integer) e.getNewValue()).intValue();
                                    if (now==Sensor.ACTIVE) { 
                                        setRoute();
                                    }
                                }
                            }
                    });
                }
                else {
                    // control sensor does not exist
                    log.error("Route "+getSystemName()+" is linked to a Sensor that does not exist: "+
                                             mControlSensors[k]);
                }
            }
        }
    }

    /**
     * Method to deactivate the Route via Sensors
     * Deactivates Route based on a list of Sensors
     */
    public void deActivateRoute() {
        if (mNumSensors > 0) {
            for (int k = 0;k<mNumSensors;k++) {
                if (mSensorListener[k]!=null) {
                    mSensors[k].removePropertyChangeListener(mSensorListener[k]);
                    mSensorListener[k] = null;
                }
            }
        }
    }
    
    /**
     * Not needed for Routes - included to complete implementation of the NamedBean interface.
     */
    public int getState() {
        log.warn("Unexpected call to getState in DefaultRoute.");
        return UNKNOWN;
    }
    
    /**
     * Not needed for Routes - included to complete implementation of the NamedBean interface.
     */
    public void setState(int state) {
        log.warn("Unexpected call to setState in DefaultRoute.");
        return;
    }    
}
     
/* @(#)DefaultRoute.java */
