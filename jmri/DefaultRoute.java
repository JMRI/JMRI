// DefaultRoute.java

package jmri;

 /**
 * Class providing the basic logic of the Route interface.
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @version     $Revision: 1.13 $
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
    static final int MAX_OUTPUT_TURNOUTS_PER_ROUTE = 100;
    static final int MAX_OUTPUT_SENSORS_PER_ROUTE = 100;

    protected String[] mOutputTurnout = new String[MAX_OUTPUT_TURNOUTS_PER_ROUTE];
    protected int[] mOutputTurnoutState = new int[MAX_OUTPUT_TURNOUTS_PER_ROUTE];
    protected String[] mOutputSensor = new String[MAX_OUTPUT_SENSORS_PER_ROUTE];
    protected int[] mOutputSensorState = new int[MAX_OUTPUT_SENSORS_PER_ROUTE];

    protected String[] mControlSensors = new String[MAX_CONTROL_SENSORS];
    protected int[] mSensorMode = new int[MAX_CONTROL_SENSORS];
    protected String mControlTurnout = "";
    protected int mControlTurnoutState = jmri.Turnout.THROWN;
	protected int mDelay = 0;

    /**
     *  Operational instance variables (not saved between runs)
     */
    protected int mNumOutputTurnouts = 0;
    protected int mNumOutputSensors = 0;

    protected int mNumSensors = 0;
    protected Sensor[] mSensors = new Sensor[MAX_CONTROL_SENSORS];
    protected java.beans.PropertyChangeListener[] mSensorListener = 
                    new java.beans.PropertyChangeListener[MAX_CONTROL_SENSORS];
    protected Turnout mTurnout = null;
    protected java.beans.PropertyChangeListener mTurnoutListener = null;
	private boolean busy = false;
     
    /**
     * @deprecated
     */
    public boolean addTurnoutToRoute(String turnoutSystemName, int turnoutState) {
        return addOutputTurnout(turnoutSystemName, turnoutState);
    } 
    
    /**
     * Add an output Turnout to this Route
     * @param systemName The turnout system name
     * @param state must be Turnout.CLOSED, Turnout.THROWN, or Route.TOGGLE, 
     *      which determines how the Turnout is to be switched when this Route is set
     */
    public boolean addOutputTurnout(String turnoutSystemName, int turnoutState) {
        if (mNumOutputTurnouts >= MAX_OUTPUT_TURNOUTS_PER_ROUTE) {
            // reached maximum
            log.warn("Reached maximum number of turnouts for Route: "+getSystemName() );
            return false;
        }
        if ((turnoutState!=Turnout.THROWN) && (turnoutState!=Turnout.CLOSED)
								&& (turnoutState!=Route.TOGGLE)) {
            log.warn("Illegal Turnout state for Route: "+getSystemName() );
            return false;
        }        
        mOutputTurnout[mNumOutputTurnouts] = turnoutSystemName;
        mOutputTurnoutState[mNumOutputTurnouts] = turnoutState;
        mNumOutputTurnouts++;
        return true;
    }

    /**
     * @deprecated
     */
    public void clearRouteTurnouts() {
        clearOutputTurnouts();
    }
    /**
     * Delete all output Turnouts from this Route
     */
    public void clearOutputTurnouts() {
        mNumOutputTurnouts = 0;
    }

    /**
     * @deprecated
     */
    public String getRouteTurnoutByIndex(int index) {
        return getOutputTurnoutByIndex(index);
    }

    /**
     * Method to get a Route Turnout System Name by Index
     *  Returns null if there is no turnout with that index
     */
    public String getOutputTurnoutByIndex(int index) {
        if ( (index >= mNumOutputTurnouts) || (index < 0) ) {
            return null;
        }
        return mOutputTurnout[index];
    }
    
    /**
     * @deprecated
     */
    public boolean isTurnoutIncluded(String turnoutSystemName){
        return isOutputTurnoutIncluded(turnoutSystemName);
    }
    /**
     * Method to inquire if a Turnout is included in this Route
     */
    public boolean isOutputTurnoutIncluded(String turnoutSystemName) {
        for (int i = 0; i<mNumOutputTurnouts; i++) {
            if( turnoutSystemName.equals(mOutputTurnout[i]) ) {
                // Found turnout
                return true;
            }
        }
        return false;
    }

    /**
     * @deprecated
     */
    public int getTurnoutSetState(String turnoutSystemName) {
        return getOutputTurnoutSetState(turnoutSystemName);
    }
    
    /**
     * Method to get the Set State of a Turnout included in this Route
     * @return -1 if there are less than 'k' Turnouts defined
     */
    public int getOutputTurnoutSetState(String turnoutSystemName) {
        for (int i = 0; i<mNumOutputTurnouts; i++) {
            if( turnoutSystemName.equals(mOutputTurnout[i]) ) {
                // Found turnout
                return mOutputTurnoutState[i];
            }
        }
        return -1;
    }

    /**
     * @deprecated
	 */
    public Turnout getRouteTurnout(int k) {
        return getOutputTurnout(k);
    }
    /**
     * Method to return the 'k'th Turnout of the Route.
     * @return null if there are less than 'k' Turnouts defined
	 */
    public Turnout getOutputTurnout(int k) {
		if (mNumOutputTurnouts<=k) {
			return (null);
		}
		else {
			return (InstanceManager.turnoutManagerInstance().
                                            getBySystemName(mOutputTurnout[k]));
		}
	}
	
    /**
     * @deprecated
	 */
    public int getRouteTurnoutState(int k) {
        return getOutputTurnoutState(k);
    }
    /**
     * Method to get the desired state of 'k'th Turnout of the Route.
     *   Returns -1 if there are less than 'k' Turnouts defined
	 */
    public int getOutputTurnoutState(int k) {
		if (mNumOutputTurnouts<=k) {
			return (-1);
		}
		else {
			return (mOutputTurnoutState[k]);
		}
	}

    // output sensors (new interface only)

    /**
     * Add an output Sensor to this Route
     * @param systemName The sensor system name
     * @param state must be Sensor.ACTIVE, Sensor.INACTIVE, or Route.TOGGLE, 
     *      which determines how the Sensor is to be set when this Route is set
     */
    public boolean addOutputSensor(String systemName, int state) {
        if (mNumOutputSensors >= MAX_OUTPUT_SENSORS_PER_ROUTE) {
            // reached maximum
            log.warn("Reached maximum number of sensors for Route: "+getSystemName() );
            return false;
        }
        if ((state!=Sensor.ACTIVE) && (state!=Sensor.INACTIVE)
								&& (state!=Route.TOGGLE)) {
            log.warn("Illegal Sensor state for Route: "+getSystemName() );
            return false;
        }        
        mOutputSensor[mNumOutputSensors] = systemName;
        mOutputSensorState[mNumOutputSensors] = state;
        mNumOutputSensors ++;
        return true;
    }

    /**
     * Delete all output Turnouts from this Route
     */
    public void clearOutputSensors() {
        mNumOutputSensors = 0;
    }
    
    /**
     * Method to get an ouput Sensor system name by Index
     *  Returns null if there is no turnout with that index
     */
    public String getOutputSensorByIndex(int index) {
        if ( (index >= mNumOutputSensors) || (index < 0) ) {
            return null;
        }
        return mOutputSensor[index];
    }
    
    /**
     * Method to inquire if a Sensor is included in this Route
     */
    public boolean isOutputSensorIncluded(String systemName) {
        for (int i = 0; i<mNumOutputSensors; i++) {
            if( systemName.equals(mOutputSensor[i]) ) {
                // Found sensor
                return true;
            }
        }
        return false;
    }

    /**
     * Method to get the Set State of a Sensor included in this Route
     *   If the Sensor is not found, -1 is returned.
     */
    public int getOutputSensorSetState(String systemName) {
        for (int i = 0; i<mNumOutputSensors; i++) {
            if( systemName.equals(mOutputSensor[i]) ) {
                // Found turnout
                return mOutputSensorState[i];
            }
        }
        return -1;
    }
 
    /**
     * Method to return the 'k'th Sensor of the Route.
     * @return null if there are less than 'k' Sensors defined
	 */
    public Sensor getOutputSensor(int k) {
		if (mNumOutputSensors<=k) {
			return (null);
		}
		else {
			return (InstanceManager.sensorManagerInstance().
                                            getBySystemName(mOutputSensor[k]));
		}
	}
	
    /**
     * Method to get the desired state of 'k'th Sensor of the Route.
     *   Returns -1 if there are less than 'k' Sensors defined
	 */
    public int getOutputSensorState(int k) {
		if (mNumOutputSensors<=k) {
			return (-1);
		}
		else {
			return (mOutputSensorState[k]);
		}
	}
	
	
    // Inputs ----------------

    /**
     * Method to delete all control Sensors from this Route
     */
    public void clearRouteSensors() {
        mNumSensors = 0;
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
     * Method to set delay (milliseconds) between issuing Turnout commands
     */
    public void setRouteCommandDelay(int delay) {
		if (delay >= 0)
			mDelay = delay;
	}

    /**
     * Method to get delay (milliseconds) between issuing Turnout commands
     */
    public int getRouteCommandDelay() {
		return mDelay;
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
        if ((mNumOutputTurnouts>0) || (mNumOutputSensors>0)) {
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
        
        // check for veto of change
        if (isVetoed()) return; // don't fire

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
                                    // turnout OK, check for vetoes
                                    if (isVetoed()) return; // skip setting route
                                    // OK, passed all checks, set the route
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
     * Internal method to check whether
     * operation of the route has been vetoed by a sensor
     * or turnout setting.
     * @returns true if veto, i.e. don't fire route; false if no veto, OK to fire
     */
    boolean isVetoed() {
        log.debug("check for veto");
        // check sensors
        for (int i = 0; i < mNumSensors; i++) {
            int s = mSensors[i].getKnownState();
            int mode = getRouteSensorMode(i);
            if (  ( (mode==VETOACTIVE) && (s==Sensor.ACTIVE) )
                    || ( (mode==VETOINACTIVE) && (s==Sensor.INACTIVE) ) )
                 return true;  // veto set
        }
        return false;
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
		int delay = r.getRouteCommandDelay();			
		for (int k = 0; k < DefaultRoute.MAX_OUTPUT_TURNOUTS_PER_ROUTE; k++) {
			Turnout t = r.getOutputTurnout(k);
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
						Thread.sleep(250 + delay);
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
		for (int k = 0; k < DefaultRoute.MAX_OUTPUT_SENSORS_PER_ROUTE; k++) {
			Sensor t = r.getOutputSensor(k);
			if (t!=null) {
				int state = r.getOutputSensorState(k);
				if (state!=-1) {
					if (state==Route.TOGGLE) {
						int st = t.getKnownState();
						if (st==Sensor.ACTIVE) {
							state = Sensor.INACTIVE;
						}
						else {
							state = Sensor.ACTIVE;
						}
					}
					try {
					    t.setKnownState(state);
					} catch (JmriException e) {
					    log.warn("Exception setting sensor "+t.getSystemName()+" in route");
					}
					try {
						Thread.sleep(50);
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
    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DefaultRoute.class.getName());
}

/* @(#)DefaultRoute.java */
