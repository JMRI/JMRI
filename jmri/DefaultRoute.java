// DefaultRoute.java

package jmri;

 /**
 * Class providing the basic logic of the Route interface.
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @version     $Revision: 1.10 $
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
    protected int[] mSensorMode = new int[MAX_CONTROL_SENSORS];
    protected String mControlTurnout = "";
    protected int mControlTurnoutState = jmri.Turnout.THROWN;

    /**
     *  Operational instance variables (not saved between runs)
     */
    protected int mNumTurnouts = 0;
    protected int mNumSensors = 0;
    protected Sensor[] mSensors = new Sensor[MAX_CONTROL_SENSORS];
    protected java.beans.PropertyChangeListener[] mSensorListener = 
                    new java.beans.PropertyChangeListener[MAX_CONTROL_SENSORS];
    protected Turnout mTurnout = null;
    protected java.beans.PropertyChangeListener mTurnoutListener = null;
	private boolean busy = false;
     
    /**
     * Method to add a Turnout to the list of Turnouts in this Route
     * 'turnoutState' must be Turnout.CLOSED, Turnout.THROWN, or Route.TOGGLE, depending
     *      on how the Turnout is to be switched when this Route is set
     */
    public boolean addTurnoutToRoute(String turnoutSystemName, int turnoutState) {
        if (mNumTurnouts >= MAX_TURNOUTS_PER_ROUTE) {
            // reached maximum
            log.warn("Reached maximum number of turnouts for Route: "+getSystemName() );
            return false;
        }
        if ((turnoutState!=Turnout.THROWN) && (turnoutState!=Turnout.CLOSED)
								&& (turnoutState!=Route.TOGGLE)) {
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
    public boolean addSensorToRoute(String sensorSystemName, int mode) {
        if (mNumSensors >= MAX_CONTROL_SENSORS) {
            // reached maximum
            log.warn("Reached maximum number of control Sensors for Route: "+
                                                            getSystemName() );
            return false;
        }
        mControlSensors[mNumSensors] = sensorSystemName;
        mSensorMode[mNumSensors] = mode;
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
    public String getRouteSensorName(int index) {
        if (index < 0 || index >= mNumSensors) {
            return (null);
        }
        return (mControlSensors[index]);
    }
    /**
     * Method to get the mode associated with a control Sensor in this Route
     *  'index' is the index in the Sensor array of the requested 
     *      Sensor.  
     *  If there is no Sensor with that 'index', or if 'index'
     *      is not in the range 0 thru MAX_SENSORS-1, 
     *      ONACTIVE is returned
     */
    public int getRouteSensorMode(int index) {
        if (index < 0 || index >= mNumSensors) {
            return ONACTIVE;
        }
        return (mSensorMode[index]);
    }

    /**
     * Method to set the SystemName of a control Turnout for this Route
     */
    public void setControlTurnout(String turnoutSystemName) {
        mControlTurnout = turnoutSystemName;
        if (mControlTurnout.length()<=2) mControlTurnout = null;
    }

    /**
     * Method to get the SystemName of a control Turnout for this Route
     */
    public String getControlTurnout() {
        return mControlTurnout;
    }

    /**
     * Method to set the State of control Turnout that fires this Route
     */
    public void setControlTurnoutState(int turnoutState) {
        if ( (turnoutState == Turnout.THROWN) || 
                                        (turnoutState == Turnout.CLOSED) ) {
            mControlTurnoutState = turnoutState;
        }
        else {
            log.error("Attempt to set invalid control Turnout state for Route.");
        }
    }

    /**
     * Method to get the State of control Turnout that fires this Route
     */
    public int getControlTurnoutState() {
        return (mControlTurnoutState);
    }

    /**
     * Method to set the Route
     * Sets all Route Turnouts to the state shown in the Route definition
	 * This call is ignored if the Route is 'busy', i.e., if there is a 
	 *    thread currently sending commands to this Route's turnouts.
     */
    public void setRoute() {
        if (mNumTurnouts>0) {
			if (!busy) {
				setRouteBusy();
				SetRouteThread thread = new SetRouteThread(this);
				thread.start();
			}
        }
    }

    /**
     * Handle sensor update event to see if that will set the route.
     * Called when a "KnownState" event is received.
     */
    protected void checkSensor(int state, Sensor sensor) {
        String name = sensor.getSystemName();
        if (log.isDebugEnabled()) log.debug("check Sensor "+name+" for "+getSystemName());
        boolean activated = false;  // need to have a sensor hitting active
        for (int i = 0; i < mNumSensors; i++) {
            if (getRouteSensorName(i).equals(name)) {
                // here for match, check mode & handle onActive, onInactive
                int mode = getRouteSensorMode(i);
                if (log.isDebugEnabled()) log.debug("match mode: "+mode+" state: "+state);
                if (  ( (mode==ONACTIVE) && (state!=Sensor.ACTIVE) )
                    || ( (mode==ONINACTIVE) && (state!=Sensor.INACTIVE) ) )
                    return;
                if (  ( (mode==ONACTIVE) && (state==Sensor.ACTIVE) )
                    || ( (mode==ONINACTIVE) && (state==Sensor.INACTIVE) ) )
                   activated = true;
                // if any other modes, just skip
                else return;
            }
        }
        
        log.debug("check activated");
        if (!activated) return;
        
        log.debug("check for veto");
        // if we got here, now check any vetos
        for (int i = 0; i < mNumSensors; i++) {
            int s = mSensors[i].getKnownState();
            int mode = getRouteSensorMode(i);
            if (  ( (mode==VETOACTIVE) && (s==Sensor.ACTIVE) )
                    || ( (mode==VETOINACTIVE) && (s==Sensor.INACTIVE) ) )
                 return;
        }
        // and finally set the route
        if (log.isDebugEnabled()) log.debug("call setRoute for "+getSystemName());
        setRoute();
    }
    
    /**
     * Method to activate the Route via Sensors and control Turnout
     * Sets up for Route activation based on a list of Sensors and a control Turnout
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
                                    checkSensor(now, (Sensor)e.getSource());
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
        if ( (mControlTurnout!=null) && (mControlTurnout.length() > 2)) {
            mTurnout = InstanceManager.turnoutManagerInstance().
                                            getBySystemName(mControlTurnout);
            if (mTurnout!=null) {
                mTurnout.addPropertyChangeListener(mTurnoutListener =
                                                new java.beans.PropertyChangeListener() {
                        public void propertyChange(java.beans.PropertyChangeEvent e) {
                            if (e.getPropertyName().equals("KnownState")) {
                                int now = ((Integer) e.getNewValue()).intValue();
                                if (now==mControlTurnoutState) { 
                                    setRoute();
                                }
                            }
                        }
                    });
            }
            else {
                // control turnout does not exist
                log.error("Route "+getSystemName()+" is linked to a Turnout that does not exist: "+
                                             mControlTurnout);
            }
        }
    }

    /**
     * Method to deactivate the Route 
     * Deactivates Route based on a list of Sensors and a control Turnout
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
        if (mTurnoutListener!=null) {
            mTurnout.removePropertyChangeListener(mTurnoutListener);
            mTurnoutListener = null;
        }
    }

    /**
     * Method to set Route busy when commands are being issued to 
     *   Route turnouts
	 */
    public void setRouteBusy() {
		busy = true;
	}

    /**
     * Method to set Route not busy when all commands have been
     *   issued to Route turnouts
	 */
    public void setRouteNotBusy() {
		busy = false;
	}

    /**
     * Method to query if Route is busy (returns true if commands are
     *   being issued to Route turnouts)
	 */
    public boolean isRouteBusy() {
		return (busy);
	}

    /**
     * Method to return the 'k'th Turnout of the Route.
     *   Returns null if there are less than 'k' Turnouts defined
	 */
    public Turnout getRouteTurnout(int k) {
		if (mNumTurnouts<=k) {
			return (null);
		}
		else {
			return (InstanceManager.turnoutManagerInstance().
                                            getBySystemName(mRouteTurnout[k]));
		}
	}
	
    /**
     * Method to get the desired state of 'k'th Turnout of the Route.
     *   Returns -1 if there are less than 'k' Turnouts defined
	 */
    public int getRouteTurnoutState(int k) {
		if (mNumTurnouts<=k) {
			return (-1);
		}
		else {
			return (mRouteTurnoutState[k]);
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

/**
 * Class providing a thread to set route turnouts
 */
class SetRouteThread extends Thread
{
	/**
	 * Constructs the thread
	 */
	public SetRouteThread (DefaultRoute aRoute) {
		r = aRoute;
	}
	
	/** 
	 * Runs the thread - sends commands to Route Turnouts
	 */
	public void run () {
		for (int k = 0; k < Route.MAX_TURNOUTS_PER_ROUTE; k++) {
			Turnout t = r.getRouteTurnout(k);
			if (t!=null) {
				int state = r.getRouteTurnoutState(k);
				if (state!=-1) {
					if (state==Route.TOGGLE) {
						int st = t.getKnownState();
						if (st==Turnout.CLOSED) {
							state = Turnout.THROWN;
						}
						else {
							state = Turnout.CLOSED;
						}
					}
					t.setCommandedState(state);
					try {
						Thread.sleep(250);
					}
					catch (InterruptedException e) {
						break;
					}
				}
			}
			else {
				break;
			}
		}
		r.setRouteNotBusy();
	}
	
	private DefaultRoute r;
}

/* @(#)DefaultRoute.java */
