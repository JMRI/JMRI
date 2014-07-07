// DefaultRoute.java

package jmri.implementation;

 /**
 * Class providing the basic logic of the Route interface.
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2006, 2007
 * @author Simon Reader Copyright (C) 2008
 * @author Pete Cressman Copyright (C) 2009
 *
 * @version     $Revision$
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import jmri.*;

public class DefaultRoute extends AbstractNamedBean
    implements Route, java.io.Serializable, java.beans.VetoableChangeListener {

    public DefaultRoute(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
        jmri.InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
        jmri.InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
    }

    public DefaultRoute(String systemName) {
        super(systemName.toUpperCase());
        jmri.InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
        jmri.InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
    }
    
    /**
     *  Persistant instance variables (saved between runs)
     */
    protected String mControlTurnout = "";
    protected NamedBeanHandle<Turnout> mControlNamedTurnout = null;
    protected int mControlTurnoutState = jmri.Turnout.THROWN;
	protected int mDelay = 0;
	
    protected String mLockControlTurnout = "";
    protected NamedBeanHandle<Turnout> mLockControlNamedTurnout = null;
    protected int mLockControlTurnoutState = jmri.Turnout.THROWN;

    protected String mTurnoutsAlignedSensor = "";
    protected NamedBeanHandle<Sensor> mTurnoutsAlignedNamedSensor = null;
    
    protected String soundFilename;
    protected String scriptFilename;
    
    protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
    
    private static final long serialVersionUID = 1L;
    
    /**
     *  Operational instance variables (not saved between runs)
     */
    ArrayList <OutputSensor> _outputSensorList = new ArrayList<OutputSensor>();
    private class OutputSensor {
        //Sensor _sensor;
        NamedBeanHandle<Sensor> _sensor;
        int _state = Sensor.ACTIVE;
        OutputSensor(String name) {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(name);
            _sensor = nbhm.getNamedBeanHandle(name, sensor);
        }

        String getName() {
            if (_sensor != null)
            {
                return _sensor.getName();
            }
            return null;
        }
        boolean setState(int state) {
            if (_sensor == null) {
                return false;
            }
            if ((state!=Sensor.ACTIVE) && (state!=Sensor.INACTIVE) && (state!=Route.TOGGLE)) {
                log.warn("Illegal Sensor state for Route: "+getName() );
                return false;
            }        
            _state = state;
            return true;
        }
        int getState() {
            return _state;
        }
        Sensor getSensor() {
            if(_sensor !=null)
                return _sensor.getBean();
            return null;
        }
    }

    ArrayList <ControlSensor> _controlSensorList = new ArrayList<ControlSensor>();
    private class ControlSensor extends OutputSensor implements PropertyChangeListener {

        ControlSensor (String name) {
            super(name);
        }
        boolean setState(int state) {
            if (_sensor == null) {
                return false;
            }
            _state = state;
            return true;
        }
        void addListener() {
            if(_sensor!=null)
                _sensor.getBean().addPropertyChangeListener(this, getName(), "Route " + getDisplayName() + "Output Sensor");
        }
        void removeListener() {
            if(_sensor!=null)
                _sensor.getBean().removePropertyChangeListener(this);
        }
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("KnownState")) {
                int now = ((Integer) e.getNewValue()).intValue();
                int then = ((Integer) e.getOldValue()).intValue();
                checkSensor(now, then, (Sensor)e.getSource());
            }
        }
    }
    //protected Turnout mTurnout = null;
    protected transient PropertyChangeListener mTurnoutListener = null;
    //protected Turnout mLockTurnout = null;
    protected transient PropertyChangeListener mLockTurnoutListener = null;
    
    ArrayList <OutputTurnout> _outputTurnoutList = new ArrayList<OutputTurnout>();
    private class OutputTurnout implements PropertyChangeListener {
        NamedBeanHandle<Turnout> _turnout;
        //Turnout _turnout;
        int _state;

        OutputTurnout(String name) {
            Turnout turnout = InstanceManager.turnoutManagerInstance().provideTurnout(name);
            _turnout = nbhm.getNamedBeanHandle(name, turnout);
            
        }

        String getName() {
            if (_turnout != null)
            {
                return _turnout.getName();
            }
            return null;
        }
        boolean setState(int state) {
            if (_turnout == null) {
                return false;
            }
            if ((state!=Turnout.THROWN) && (state!=Turnout.CLOSED) && (state!=Route.TOGGLE)) {
                log.warn("Illegal Turnout state for Route: "+getName() );
                return false;
            }        
            _state = state;
            return true;
        }
        int getState() {
            return _state;
        }
        Turnout getTurnout() {
            if (_turnout!=null)
                return _turnout.getBean();
            return null;
        }
        void addListener() {
            if(_turnout!=null)
                _turnout.getBean().addPropertyChangeListener(this, getName(), "Route " + getDisplayName() + " Output Turnout");
        }
        void removeListener() {
            if(_turnout!=null)
                _turnout.getBean().removePropertyChangeListener(this);
        }
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("KnownState")
                         || e.getPropertyName().equals("CommandedState")) {
                //check alignement of all turnouts in route
                checkTurnoutAlignment();
            }
        }
    }
	private boolean busy = false;     
    private boolean _enabled = true;

    public boolean getEnabled() { 
        return _enabled; 
    }
    public void setEnabled(boolean v) { 
        boolean old = _enabled;
        _enabled = v;
        if (old != v) firePropertyChange("Enabled", Boolean.valueOf(old), Boolean.valueOf(v));
    }
    
    private boolean _locked = false;
    public boolean getLocked() {
         return _locked; 
    }
    public void setLocked(boolean v) {
        lockTurnouts(v);
        boolean old = _locked;
        _locked = v;
        if (old != v) firePropertyChange("Locked", Boolean.valueOf(old), Boolean.valueOf(v));
    }
    /**
	 * Determine if route can be locked. Requres at least one turnout that can
	 * be locked
	 */
	public boolean canLock() {
        for (int i=0; i<_outputTurnoutList.size(); i++) {
            if (_outputTurnoutList.get(i).getTurnout().canLock(Turnout.CABLOCKOUT)) {
				return true;
            }
        }
		return false;
	}
 
    
    /**
     * Add an output Turnout to this Route
     * @param turnoutName The turnout system name
     * @param turnoutState must be Turnout.CLOSED, Turnout.THROWN, or Route.TOGGLE, 
     *      which determines how the Turnout is to be switched when this Route is set
     */
    public boolean addOutputTurnout(String turnoutName, int turnoutState) {
        OutputTurnout outputTurnout = new OutputTurnout(turnoutName);
        if (!outputTurnout.setState(turnoutState) ) {
            return false;
        }
        _outputTurnoutList.add(outputTurnout);
        return true;
    }

    /**
     * Delete all output Turnouts from this Route
     */
    public void clearOutputTurnouts() {
        _outputTurnoutList = new ArrayList<OutputTurnout>();
    }

    public int getNumOutputTurnouts() {
        return _outputTurnoutList.size();
    }

    /**
     * Method to get a Route Turnout Name by Index
     *  Returns null if there is no turnout with that index
     */
    public String getOutputTurnoutByIndex(int index) {
        try {
            return _outputTurnoutList.get(index).getName();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }
    
    /**
     * Method to inquire if a Turnout is included in this Route.
     * <P>
     * Complicated by the fact that either the argument or the
     * internal names might be user or system names
     */
    public boolean isOutputTurnoutIncluded(String turnoutName) {
        Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(turnoutName);
        return isOutputTurnoutIncluded(t1);
    }
    
    boolean isOutputTurnoutIncluded(Turnout t1){
        for (int i=0; i<_outputTurnoutList.size(); i++) {
            if ( _outputTurnoutList.get(i).getTurnout() == t1 ) {
                // Found turnout
                return true;
            }
        }
        return false;
    }
    
    void deleteOutputTurnout(Turnout t){
        int index = -1;
        for (int i=0; i<_outputTurnoutList.size(); i++) {
            if( _outputTurnoutList.get(i).getTurnout() == t){
                index = i;
                break;
            }
        }
        if(index!=-1)
            _outputTurnoutList.remove(index);
    
    }
    
    /**
     * Method to get the Set State of a Turnout included in this Route
     * <P>
     * Noth the input and internal names can be either a user or system name
     * @return -1 if there are less than 'k' Turnouts defined
     */
    public int getOutputTurnoutSetState(String name) {
        Turnout t1 = InstanceManager.turnoutManagerInstance().provideTurnout(name);
        for (int i=0; i<_outputTurnoutList.size(); i++) {
            if( _outputTurnoutList.get(i).getTurnout() == t1 ) {
                // Found turnout
                return _outputTurnoutList.get(i).getState();
            }
        }
        return -1;
    }

    /**
     * Method to return the 'k'th Turnout of the Route.
     * @return null if there are less than 'k' Turnouts defined
	 */
    public Turnout getOutputTurnout(int k) {
        try {
            return _outputTurnoutList.get(k).getTurnout();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
	}
	
    /**
     * Method to get the desired state of 'k'th Turnout of the Route.
     *   Returns -1 if there are less than 'k' Turnouts defined
	 */
    public int getOutputTurnoutState(int k) {
        try {
            return _outputTurnoutList.get(k).getState();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
	}

    // output sensors (new interface only)

    /**
     * Add an output Sensor to this Route
     * @param sensorName The sensor name either system or user
     * @param state must be Sensor.ACTIVE, Sensor.INACTIVE, or Route.TOGGLE, 
     *      which determines how the Sensor is to be set when this Route is set
     */
    public boolean addOutputSensor(String sensorName, int state) {
        OutputSensor outputSensor = new OutputSensor(sensorName);
        if (!outputSensor.setState(state) ) {
            return false;
        }
        _outputSensorList.add(outputSensor);
        return true;
    }

    /**
     * Delete all output Sensors from this Route
     */
    public void clearOutputSensors() {
        _outputSensorList = new ArrayList<OutputSensor>();
    }
    
    public int getNumOutputSensors() {
        return _outputSensorList.size();
    }

    /**
     * Method to get an ouput Sensor name by Index
     *  Returns null if there is no sensor with that index
     */
    public String getOutputSensorByIndex(int index) {
        try {
            return _outputSensorList.get(index).getName();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }
    
    /**
     * Method to inquire if a Sensor is included in this Route
     */
    public boolean isOutputSensorIncluded(String sensorName) {
        Sensor s1 = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
        return isOutputSensorIncluded(s1);
    }
    
    boolean isOutputSensorIncluded(Sensor s1){
        for (int i=0; i<_outputSensorList.size(); i++) {
            if ( _outputSensorList.get(i).getSensor() == s1 ) {
                // Found turnout
                return true;
            }
        }
        return false;
    }

    /**
     * Method to get the Set State of a Sensor included in this Route
     *   If the Sensor is not found, -1 is returned.
     * <P>
     * Both the input or internal names can be either system or user names
     */
    public int getOutputSensorSetState(String name) {
        Sensor s1 = InstanceManager.sensorManagerInstance().provideSensor(name);
        for (int i=0; i<_outputSensorList.size(); i++) {
            if( _outputSensorList.get(i).getSensor() == s1 ) {
                // Found turnout
                return _outputSensorList.get(i).getState();
            }
        }
        return -1;
    }
 
    /**
     * Method to return the 'k'th Sensor of the Route.
     * @return null if there are less than 'k' Sensors defined
	 */
    public Sensor getOutputSensor(int k) {
        try {
            return _outputSensorList.get(k).getSensor();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
	}
	
    /**
     * Method to get the desired state of 'k'th Sensor of the Route.
     *   Returns -1 if there are less than 'k' Sensors defined
	 */
    public int getOutputSensorState(int k) {
        try {
            return _outputSensorList.get(k).getState();
        } catch (IndexOutOfBoundsException ioob) {
            return -1;
        }
	}
	
    void removeOutputSensor(Sensor s){
        int index = -1;
        for (int i=0; i<_outputSensorList.size(); i++) {
            if( _outputSensorList.get(i).getSensor() == s){
                index = i;
                break;
            }
        }
        if(index!=-1)
            _outputSensorList.remove(index);
    }
    
    /** 
     * Set name of script file to be run when Route is fired
     */
    public void setOutputScriptName(String filename) {
        scriptFilename = filename;
    }
    
    /** 
     * Get name of script file to be run when Route is fired
     */
    public String getOutputScriptName() {
        return scriptFilename;
    }
    
    /** 
     * Set name of sound file to be played when Route is fired
     */
    public void setOutputSoundName(String filename) {
        soundFilename = filename;
    }
    
    /** 
     * Get name of sound file to be played when Route is fired
     */
    public String getOutputSoundName() {
        return soundFilename;
    }
    
    /**
     * Method to set turnouts aligned sensor
     */
    public void setTurnoutsAlignedSensor(String sensorName){
        if (log.isDebugEnabled()) log.debug("setTurnoutsAlignedSensor "+getSystemName()+" "+sensorName);
        
        mTurnoutsAlignedSensor = sensorName;
        if( mTurnoutsAlignedSensor==null || mTurnoutsAlignedSensor.equals("")){
            mTurnoutsAlignedNamedSensor = null;
            return;
        }
        Sensor s = InstanceManager.sensorManagerInstance().
                                            provideSensor(mTurnoutsAlignedSensor);
        if(s!=null)
           mTurnoutsAlignedNamedSensor = nbhm.getNamedBeanHandle(mTurnoutsAlignedSensor, s);
    }
    
    /**
     * Method to get turnouts aligned sensor
     */
    public String getTurnoutsAlignedSensor(){
        if(mTurnoutsAlignedNamedSensor!=null){
            return mTurnoutsAlignedNamedSensor.getName();
        }
        return mTurnoutsAlignedSensor;
    }
    
    public Sensor getTurnoutsAlgdSensor(){
        if(mTurnoutsAlignedNamedSensor!=null){
            return mTurnoutsAlignedNamedSensor.getBean();
        }
        else if ( mTurnoutsAlignedSensor!=null && !mTurnoutsAlignedSensor.equals("")){ 
            Sensor s = InstanceManager.sensorManagerInstance().
                                                provideSensor(mTurnoutsAlignedSensor);
            if(s!=null)
               mTurnoutsAlignedNamedSensor = nbhm.getNamedBeanHandle(mTurnoutsAlignedSensor, s);
            else {
                log.error("Unable to create Turnout aligned Sensor " + mTurnoutsAlignedSensor + " for Route " + getDisplayName());
                return null;
            }
        }
        return null;
        
    }
    // Inputs ----------------

    /**
     * Method to delete all control Sensors from this Route
     */
    public void clearRouteSensors() {
        _controlSensorList = new ArrayList<ControlSensor>();
    }
    
    /**
     * Method to add a Sensor to the list of control Sensors for this Route.
     * @param sensorName either a system or username of a sensor
     */
    public boolean addSensorToRoute(String sensorName, int mode) {
        if (_controlSensorList.size() >= MAX_CONTROL_SENSORS) {
            // reached maximum
            log.warn("Reached maximum number of control Sensors for Route: "+
                                                            getSystemName() );
        }
        ControlSensor sensor = new ControlSensor(sensorName);
        if (log.isDebugEnabled()) log.debug("addSensorToRoute "+getSystemName()+" "+sensorName);
        if (!sensor.setState(mode) )
        {
            return false;
        }
        _controlSensorList.add(sensor);
        return true;
    }
    
    /**
     * Method to get the Name of a control Sensor in this Route
     *  'index' is the index in the Sensor array of the requested 
     *      Sensor.  
     *  If there is no Sensor with that 'index', or if 'index'
     *      is not in the range 0 thru MAX_SENSORS-1, null is returned.
     */
    public String getRouteSensorName(int index) {
        try {
            return _controlSensorList.get(index).getName();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }
    /**
     * Method to get the control Sensor in this Route
     *  'index' is the index in the Sensor array of the requested 
     *      Sensor.  
     *  If there is no Sensor with that 'index', or if 'index'
     *      is not in the range 0 thru MAX_SENSORS-1, null is returned.
     */
    public Sensor getRouteSensor(int index) {
        try {
            return _controlSensorList.get(index).getSensor();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
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
        try {
            return _controlSensorList.get(index).getState();
        } catch (IndexOutOfBoundsException ioob) {
            return 0;
        }
    }
    
    boolean isRouteSensorIncluded(Sensor s){
        for (int i=0; i< _controlSensorList.size(); i++) {
            if ( _controlSensorList.get(i).getSensor() == s ) {
                // Found turnout
                return true;
            }
        }
        return false;
    }
    
    void removeRouteSensor(Sensor s){
        int index = -1;
        for (int i=0; i<_controlSensorList.size(); i++) {
            if( _controlSensorList.get(i).getSensor() == s){
                index = i;
                break;
            }
        }
        if(index!=-1)
            _controlSensorList.remove(index);
    }
    /**
     * Method to set the Name of a control Turnout for this Route
     */
    public void setControlTurnout(String turnoutName) {
        mControlTurnout = turnoutName;
        if( mControlTurnout==null || mControlTurnout.equals("")){
            mControlNamedTurnout = null;
            return;
        }
        Turnout t = InstanceManager.turnoutManagerInstance().
                                            provideTurnout(mControlTurnout);
        if(t!=null)
           mControlNamedTurnout = nbhm.getNamedBeanHandle(mControlTurnout, t);
    }

    /**
     * Method to get the Name of a control Turnout for this Route
     */
    public String getControlTurnout() {
        if(mControlNamedTurnout!=null)
            return mControlNamedTurnout.getName();
        return mControlTurnout;
    }
    
    public Turnout getCtlTurnout(){
        if(mControlNamedTurnout!=null){
            return mControlNamedTurnout.getBean();
        }
        else if( mControlTurnout!=null && !mControlTurnout.equals("")){
            Turnout t = InstanceManager.turnoutManagerInstance().
                                                provideTurnout(mControlTurnout);
            if(t!=null)
               mControlNamedTurnout = nbhm.getNamedBeanHandle(mControlTurnout, t);
            else {
                log.error("Unable to create control turnout " + mControlTurnout + " for Route " + getDisplayName());
                return null;
            }
        }
        return null;
    }
    
    /**
     * Method to set the Name of a lock control Turnout for this Route
     */
    public void setLockControlTurnout(String turnoutName) {
        mLockControlTurnout = turnoutName;
        if( mLockControlTurnout==null || mLockControlTurnout.equals("")){
            mLockControlNamedTurnout = null;
            return;
        }
        Turnout t = InstanceManager.turnoutManagerInstance().
                                            provideTurnout(mLockControlTurnout);
        if(t!=null)
           mLockControlNamedTurnout = nbhm.getNamedBeanHandle(mLockControlTurnout, t);
    }

    /**
     * Method to get the Name of a lock control Turnout for this Route
     */
    public String getLockControlTurnout() {
        if(mLockControlNamedTurnout!=null)
            return mLockControlNamedTurnout.getName();
        return mLockControlTurnout;
    }
    
    public Turnout getLockCtlTurnout(){
        if(mLockControlNamedTurnout!=null){
            return mLockControlNamedTurnout.getBean();
        }
        else if( mLockControlTurnout!=null && !mLockControlTurnout.equals("")){ 
            Turnout t = InstanceManager.turnoutManagerInstance().
                                                provideTurnout(mLockControlTurnout);
            if(t!=null)
               mLockControlNamedTurnout = nbhm.getNamedBeanHandle(mLockControlTurnout, t);
            else {
                log.error("Unable to create Lock control turnout " + mLockControlTurnout + " for Route " + getDisplayName());
                return null;
            }
        }
        return null;
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
        if ( (turnoutState == Route.ONTHROWN) 
               || (turnoutState == Route.ONCLOSED) 
               || (turnoutState == Route.ONCHANGE) 
               || (turnoutState == Route.VETOCLOSED) 
               || (turnoutState == Route.VETOTHROWN) 
            ) {
            mControlTurnoutState = turnoutState;
        } else {
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
     * Method to set the State of lock control Turnout
     */
    public void setLockControlTurnoutState(int turnoutState) {
		if ((turnoutState == Route.ONTHROWN)
				|| (turnoutState == Route.ONCLOSED)
				|| (turnoutState == Route.ONCHANGE)) {
			mLockControlTurnoutState = turnoutState;
		} else {
			log.error("Attempt to set invalid lock control Turnout state for Route.");
		}
	}

    /**
	 * Method to get the State of lock control Turnout
	 */
    public int getLockControlTurnoutState() {
        return (mLockControlTurnoutState);
    }
    
	/**
	 * Lock or unlock turnouts that are part of a route
	 */
	private void lockTurnouts(boolean lock) {
		// determine if turnout should be locked
		for (int i=0; i < _outputTurnoutList.size(); i++) {
			_outputTurnoutList.get(i).getTurnout().setLocked(
                Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, lock);
		}
	}


    /**
     * Method to set the Route
     * Sets all Route Turnouts to the state shown in the Route definition
	 * This call is ignored if the Route is 'busy', i.e., if there is a 
	 *    thread currently sending commands to this Route's turnouts.
     */
    public void setRoute() {
        if ((_outputTurnoutList.size()>0) 
                || (_outputSensorList.size()>0)
                || (soundFilename != null)
                || (scriptFilename != null)
            ) {
			if (!busy) {
				setRouteBusy();
				SetRouteThread thread = new SetRouteThread(this);
				thread.start();
			}
        }
    }

    /**
     * Handle sensor update event to see if that will set the route.
     * <P>
     * Called when a "KnownState" event is received, it assumes that
     * only one sensor is changing right now, so can use state calls 
     * for everything other than this sensor.
     *<P>
     * This will fire the route if the conditions are correct
     * <P>
     * Returns noting explicitly, but has the side effect of firing route
     */
    protected void checkSensor(int newState, int oldState, Sensor sensor) {
        // check for veto of change
        if (isVetoed()) return; // don't fire

        String name = sensor.getSystemName();
        if (log.isDebugEnabled()) log.debug("check Sensor "+name+" for "+getSystemName());
        boolean fire = false;  // dont fire unless we find something
        for (int i=0; i<_controlSensorList.size(); i++) {
            if (getRouteSensor(i).equals(sensor)) {
                // here for match, check mode & handle onActive, onInactive
                int mode = getRouteSensorMode(i);
                if (log.isDebugEnabled()) log.debug("match mode: "+mode+" new state: "+newState+" old state: "+oldState);

                // if in target mode, note whether to act
                if (  ( (mode==ONACTIVE) && (newState==Sensor.ACTIVE) )
                    || ( (mode==ONINACTIVE) && (newState==Sensor.INACTIVE) )
                    || ( (mode==ONCHANGE) && (newState!=oldState) )
                    )
                   fire = true;
                   
                // if any other modes, just skip because
                // the sensor might be in list more than once
            }
        }
        
        log.debug("check activated");
        if (!fire) return;
        
        // and finally set the route
        if (log.isDebugEnabled()) log.debug("call setRoute for "+getSystemName());
        setRoute();
    }
    
    /**
     * Turnout has changed, check to see if this fires.
     * Will fire route if appropriate
     */
     void checkTurnout(int newState, int oldState, Turnout t) {
        if (isVetoed()) return; // skip setting route
        switch (mControlTurnoutState) {
            case ONCLOSED:
                if (newState == Turnout.CLOSED) setRoute();
                return;
            case ONTHROWN:
                if (newState == Turnout.THROWN) setRoute();
                return;
            case ONCHANGE:
                if (newState != oldState) setRoute();
                return;
            default:
                // if not a firing state, return
                return;
        }
    }
     
     /**
      * Turnout has changed, check to see if this 
      * will lock or unlock route
      */
      void checkLockTurnout(int newState, int oldState, Turnout t) {
         switch (mLockControlTurnoutState) {
         case ONCLOSED:
             if (newState == Turnout.CLOSED) setLocked(true);
                else setLocked(false);
             return;
         case ONTHROWN:
             if (newState == Turnout.THROWN) setLocked(true);
                else setLocked(false);
             return;
         case ONCHANGE:
             if (newState != oldState){
            	 if (getLocked())
                        setLocked(false);
            	 else
            		 setLocked(true);
             }
             return;
         default:
             // if none, return
             return;
         }
     }
     
    /**
     * Method to check if the turnouts for this route are correctly aligned.
     *  Sets turnouits aligned sensor (if there is one) to active if the turnouts are aligned.
     *  Sets the sensor to inactive if they are not aligned
     */
    public void checkTurnoutAlignment(){
        
        //check each of the output turnouts in turn
        //turnouts are deemed not aligned if:
        // - commanded and known states don't agree
        // - non-toggle turnouts known state not equal to desired state
        // turnouts aligned sensor is then set accordingly
        
        if (getTurnoutsAlgdSensor()!=null) {
            boolean aligned = true;
            for (int k=0; k<_outputTurnoutList.size(); k++) {
                Turnout t = _outputTurnoutList.get(k).getTurnout();
                if (!t.isConsistentState()) {
                    aligned= false;
                    break;
                }
                int targetState = _outputTurnoutList.get(k).getState();
                if (targetState!=Route.TOGGLE && targetState!=t.getKnownState()) {
                    aligned= false;
                    break;
                }
            }
            try {
                if (aligned) {
                    getTurnoutsAlgdSensor().setKnownState(jmri.Sensor.ACTIVE);
                } else {
                    getTurnoutsAlgdSensor().setKnownState(jmri.Sensor.INACTIVE);
                }
            } catch (JmriException ex) {
                    log.warn("Exception setting sensor "+getTurnoutsAlignedSensor()+" in route");
            }
        }
    }
    
    
    /**
     * Method to activate the Route via Sensors and control Turnout
     * Sets up for Route activation based on a list of Sensors and a control Turnout
     * Registers to receive known state changes for output turnouts
     */
    public void activateRoute() {
        activatedRoute = true;
        
        //register output turnouts to return Known State if a turnouts aligned sensor is defined
        if (!getTurnoutsAlignedSensor().equals("")) {
            
            for (int k=0; k< _outputTurnoutList.size(); k++) {
                _outputTurnoutList.get(k).addListener();
            }
        }         
        
        for (int k=0; k< _controlSensorList.size(); k++) {
            _controlSensorList.get(k).addListener();
        }
        if (getCtlTurnout()!=null) {
            getCtlTurnout().addPropertyChangeListener(mTurnoutListener =
                                            new java.beans.PropertyChangeListener() {
                    public void propertyChange(java.beans.PropertyChangeEvent e) {
                        if (e.getPropertyName().equals("KnownState")) {
                            int now = ((Integer) e.getNewValue()).intValue();
                            int then = ((Integer) e.getOldValue()).intValue();
                            checkTurnout(now, then, (Turnout)e.getSource());
                        }
                    }
                }, getControlTurnout(), "Route " + getDisplayName());
        }
        
        if (getLockCtlTurnout()!=null) {
            getLockCtlTurnout().addPropertyChangeListener(mLockTurnoutListener =
                                            new java.beans.PropertyChangeListener() {
                    public void propertyChange(java.beans.PropertyChangeEvent e) {
                        if (e.getPropertyName().equals("KnownState")) {
                            int now = ((Integer) e.getNewValue()).intValue();
                            int then = ((Integer) e.getOldValue()).intValue();
                            checkLockTurnout(now, then, (Turnout)e.getSource());
                        }
                    }
                }, getLockControlTurnout(), "Route " + getDisplayName());
        }
        
        checkTurnoutAlignment();
// register for updates to the Output Turnouts

        
        
    }
    /**
     * Internal method to check whether
     * operation of the route has been vetoed by a sensor
     * or turnout setting.
     * @return true if veto, i.e. don't fire route; false if no veto, OK to fire
     */
    boolean isVetoed() {
        log.debug("check for veto");
        // check this route not enabled
        if (!_enabled) return true;
        
        // check sensors
        for (int i=0; i<_controlSensorList.size(); i++) {
            ControlSensor controlSensor = _controlSensorList.get(i);
            int s = controlSensor.getSensor().getKnownState();
            int mode = controlSensor.getState();
            if (  ( (mode==VETOACTIVE) && (s==Sensor.ACTIVE) )
                    || ( (mode==VETOINACTIVE) && (s==Sensor.INACTIVE) ) )
                 return true;  // veto set
        }
        // check control turnout
        if (getCtlTurnout() != null) {
            int tstate = getCtlTurnout().getKnownState();
            if (mControlTurnoutState==Route.VETOCLOSED && tstate==Turnout.CLOSED) return true;
            if (mControlTurnoutState==Route.VETOTHROWN && tstate==Turnout.THROWN) return true;
        }
        return false;
    }
    
    /**
     * Method to deactivate the Route 
     * Deactivates Route based on a list of Sensors and two control Turnouts
     */
    public void deActivateRoute() {
        //Check that the route isn't already deactived.
        if(!activatedRoute)
            return;
            
        activatedRoute = false;
        // remove control turnout if there's one 
        for (int k=0; k<_controlSensorList.size(); k++) {
            _controlSensorList.get(k).removeListener();
        }
        if (mTurnoutListener!=null) {
            getCtlTurnout().removePropertyChangeListener(mTurnoutListener);
            mTurnoutListener = null;
        }
        // remove lock control turnout if there's one 
        if (mLockTurnoutListener!=null) {
            getLockCtlTurnout().removePropertyChangeListener(mLockTurnoutListener);
            mLockTurnoutListener = null;
        }
        //remove listeners on output turnouts if there are any
        if (mTurnoutsAlignedSensor!=""){
            for (int k=0; k< _outputTurnoutList.size(); k++) {
                _outputTurnoutList.get(k).removeListener();
            }
        }
    }
    
    boolean activatedRoute = false;
    
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
    
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        NamedBean nb = (NamedBean) evt.getOldValue();
        if("CanDelete".equals(evt.getPropertyName())){ //IN18N
            StringBuilder message = new StringBuilder();
            message.append(Bundle.getMessage("InUseRouteHeader", getDisplayName())); //IN18N
            boolean found = false;
            if(nb instanceof Turnout){
                if(isOutputTurnoutIncluded((Turnout)nb)){
                    message.append(Bundle.getMessage("InUseRouteOutputTurnout")); //IN18N
                    found = true;
                }
                if(getCtlTurnout()!=null && getCtlTurnout().equals(nb)){
                    message.append(Bundle.getMessage("InUseRouteControlTurnout")); //IN18N
                    found = true;
                }
                if(getLockCtlTurnout()!=null && getLockCtlTurnout().equals(nb)){
                    message.append(Bundle.getMessage("InUseRouteLockTurnout")); //IN18N
                    found = true;
                }
            }
            else if(nb instanceof Sensor){
                if(isOutputSensorIncluded((Sensor)nb)){
                    message.append(Bundle.getMessage("InUseRouteOutputSensor")); //IN18N
                    found = true;
                }
                if(getTurnoutsAlgdSensor()!=null && getTurnoutsAlgdSensor().equals(nb)){
                    message.append(Bundle.getMessage("InUseRouteAlignSensor")); //IN18N
                    found = true; 
                }
                if(isRouteSensorIncluded((Sensor)nb)){
                    message.append(Bundle.getMessage("InUseRouteSensor")); //IN18N
                    found = true;
                }
                
            }
            if(found){
                message.append(Bundle.getMessage("InUseRouteFinish")); //IN18N
                throw new java.beans.PropertyVetoException(message.toString(), evt);
            }
            
        } else if ("DoDelete".equals(evt.getPropertyName())){ //IN18N
            if(nb instanceof Turnout){
                if(isOutputTurnoutIncluded((Turnout)nb)){
                    deActivateRoute();
                    deleteOutputTurnout((Turnout)evt.getOldValue());
                } if(getCtlTurnout()!=null && getCtlTurnout().equals(nb)){
                    deActivateRoute();
                    setControlTurnout(null);
                }
                if(getLockCtlTurnout()!=null && getLockCtlTurnout().equals(nb)){
                    deActivateRoute();
                    setLockControlTurnout(null);
                }
            } 
            else if(nb instanceof Sensor){
                if(isOutputSensorIncluded((Sensor)nb)){
                    deActivateRoute();

                }
                if(getTurnoutsAlgdSensor()!=null && getTurnoutsAlgdSensor().equals(nb)){
                    deActivateRoute();
                    setTurnoutsAlignedSensor(null);
                }
                if(isRouteSensorIncluded((Sensor)nb)){
                    deActivateRoute();

                }
            
            }
            activateRoute();
        }
    }

    static final Logger log = LoggerFactory.getLogger(DefaultRoute.class.getName());
    
    
}

/**
 * Class providing a thread to set route turnouts
 */
class SetRouteThread extends Thread {
	/**
	 * Constructs the thread
	 */
    public SetRouteThread(DefaultRoute aRoute) {
		r = aRoute;
	}
	
	/** 
	 * Runs the thread - performs operations in the order:
	 * <ul>
	 * <li>Run script (can run in parallel)
	 * <li>Play Sound (runs in parallel)
	 * <li>Set Turnouts
	 * <li>Set Sensors
	 * </UL>
	 */
    public void run() {
	
        // run script defined for start of route set
	    if ((r.getOutputScriptName() != null) && (!r.getOutputScriptName().equals(""))) {
	        jmri.util.PythonInterp.runScript(jmri.util.FileUtil.getExternalFilename(r.getOutputScriptName()));
	    }
	    
        // play sound defined for start of route set
	    if ((r.getOutputSoundName() != null) && (!r.getOutputSoundName().equals(""))) {
	        jmri.jmrit.Sound snd = new jmri.jmrit.Sound(jmri.util.FileUtil.getExternalFilename(r.getOutputSoundName()));
	        snd.play();
	    }
	    
        // set sensors at
        for (int k = 0; k < r.getNumOutputSensors(); k++) {
            Sensor t = r.getOutputSensor(k);
            int state = r.getOutputSensorState(k);
            if (state==Route.TOGGLE) {
                int st = t.getKnownState();
                if (st==Sensor.ACTIVE) {
                    state = Sensor.INACTIVE;
                } else {
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
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
            }
		}
		
        // set turnouts
        int delay = r.getRouteCommandDelay();

        for (int k=0; k<r.getNumOutputTurnouts(); k++) {
            Turnout t = r.getOutputTurnout(k);
            int state = r.getOutputTurnoutState(k);
            if (state==Route.TOGGLE) {
                int st = t.getKnownState();
                if (st==Turnout.CLOSED) {
                    state = Turnout.THROWN;
                } else {
                    state = Turnout.CLOSED;
                }
            }
            t.setCommandedState(state);
            try {
                Thread.sleep(250 + delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
            }
        }
        //set route not busy
		r.setRouteNotBusy();
        //check turnout alignment
        r.checkTurnoutAlignment();
	}
	
	private DefaultRoute r;
    
    static final Logger log = LoggerFactory.getLogger(SetRouteThread.class.getName());
}

/* @(#)DefaultRoute.java */
