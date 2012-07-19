// AbstractSensor.java

package jmri.implementation;

import jmri.Sensor;
import jmri.Reporter;

/**
 * Abstract class providing the basic logic of the Sensor interface
 * <p>
 * Sensor system names are always upper case.
 *
 * @author			Bob Jacobsen Copyright (C) 2001, 2009
 * @version         $Revision$
 */
public abstract class AbstractSensor extends AbstractNamedBean implements Sensor, java.io.Serializable {

    // ctor takes a system-name string for initialization
    public AbstractSensor(String systemName) {
        super(systemName.toUpperCase());
    }

    public AbstractSensor(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
    }

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //		public void firePropertyChange(String propertyName,
    //					       Object oldValue,
    //					       Object newValue)
    // _once_ if anything has changed state

    public int getKnownState() {return _knownState;}

    protected long sensorDebounceGoingActive = 0L;
    protected long sensorDebounceGoingInActive = 0L;
    protected boolean useDefaultTimerSettings = false;
    
    public void setSensorDebounceGoingActiveTimer(long time) { 
        if(sensorDebounceGoingActive == time)
            return;
        long oldValue = sensorDebounceGoingActive;
        sensorDebounceGoingActive = time;
        firePropertyChange("ActiveTimer", oldValue, sensorDebounceGoingActive);
        
    }
    public long getSensorDebounceGoingActiveTimer() { return sensorDebounceGoingActive; }
    
    public void setSensorDebounceGoingInActiveTimer(long time) {
        if(sensorDebounceGoingInActive == time)
            return;
        long oldValue = sensorDebounceGoingInActive;
        sensorDebounceGoingInActive = time;
        firePropertyChange("InActiveTimer", oldValue, sensorDebounceGoingInActive);
    }
    public long getSensorDebounceGoingInActiveTimer() { return sensorDebounceGoingInActive; }
    
    public void useDefaultTimerSettings(boolean boo) {
        if(boo==useDefaultTimerSettings)
            return;
        useDefaultTimerSettings = boo;
        if(useDefaultTimerSettings){
            sensorDebounceGoingActive = jmri.InstanceManager.sensorManagerInstance().getDefaultSensorDebounceGoingActive();
            sensorDebounceGoingInActive = jmri.InstanceManager.sensorManagerInstance().getDefaultSensorDebounceGoingInActive();
        }
        firePropertyChange("GlobalTimer", !boo, boo);
    }
    
    public boolean useDefaultTimerSettings() { return useDefaultTimerSettings; }
    
    protected Thread thr;
    protected Runnable r;
    /* 
     * Before going active or inactive or checking that we can go active, we will wait 500ms
     * for things to settle down to help prevent a race condition
     */
    protected void sensorDebounce(){
        final int lastKnownState = _knownState;
        r = new Runnable() {
            public void run() {
                try {
                    long sensorDebounceTimer = sensorDebounceGoingInActive;
                    if(_rawState==ACTIVE)
                        sensorDebounceTimer = sensorDebounceGoingActive;
                    Thread.sleep(sensorDebounceTimer);
                    restartcount=0;
                    _knownState=_rawState;
                    firePropertyChange("KnownState", Integer.valueOf(lastKnownState), Integer.valueOf(_knownState));
                    
                } catch (InterruptedException ex) {
                    restartcount ++;
                }
            }
        };
        
        thr = new Thread(r);
        thr.start();
    }
    
    int restartcount = 0;
    // setKnownState() for implementations that can't
    // actually do it on the layout. Not intended for use by implementations
    // that can
    public void setKnownState(int s) throws jmri.JmriException {
        if (_rawState != s){
            if(((s==ACTIVE) && (sensorDebounceGoingActive>0)) || 
                ((s==INACTIVE) && (sensorDebounceGoingInActive>0))){
            
                int oldRawState = _rawState;
                _rawState = s;
                if (thr!=null){
                    try {
                        thr.interrupt();
                    } catch (Exception ie) {
                    //Can be considered normal.
                    }
                }
                if((restartcount!=0) && (restartcount % 10 ==0)){
                    log.warn("Sensor " + getDisplayName() + " state keeps flapping " + restartcount);
                }
                firePropertyChange("RawState", Integer.valueOf(oldRawState), Integer.valueOf(s));
                sensorDebounce();
                return;
            } else {
                 //we shall try to stop the thread as one of the state changes 
                 //might start the thread, while the other may not.
                if(thr!=null){
                    try {
                        thr.interrupt();
                    } catch (Exception ie) {
                    //Can be considered normal.
                    }
                }
                _rawState=s;
            }
        } 
        if (_knownState != s) {
            int oldState = _knownState;
            _knownState = s;
            firePropertyChange("KnownState", Integer.valueOf(oldState), Integer.valueOf(_knownState));
        }
    }

    /**
     * Set our internal state information, and notify bean listeners.
     */
    public void setOwnState(int s) {
        if (_rawState != s){
            if(((s==ACTIVE) && (sensorDebounceGoingActive>0)) || 
                ((s==INACTIVE) && (sensorDebounceGoingInActive>0))){
            
                int oldRawState = _rawState;
                _rawState = s;
                if (thr!=null){
                    try {
                        thr.interrupt();
                    } catch (Exception ie) {
                    //Can be considered normal.
                    }
                }

                if((restartcount!=0) && (restartcount % 10 ==0)){
                    log.warn("Sensor " + getDisplayName() + " state keeps flapping " + restartcount);
                }
                firePropertyChange("RawState", Integer.valueOf(oldRawState), Integer.valueOf(s));
                sensorDebounce();
                return;
            } else {
                 //we shall try to stop the thread as one of the state changes 
                 //might start the thread, while the other may not.
                if(thr!=null){
                    try {
                        thr.interrupt();
                    } catch (Exception ie) {
                    //Can be considered normal.
                    }
                }
                _rawState=s;
            }
        } 
        if (_knownState != s) {
            int oldState = _knownState;
            _knownState = s;
            firePropertyChange("KnownState", Integer.valueOf(oldState), Integer.valueOf(_knownState));
        }
    }
    
    public int getRawState(){
        return _rawState;
    }

    /**
     * Implement a shorter name for setKnownState.
     *<P>
     * This generally shouldn't be used by Java code; use 
     * setKnownState instead.  The is provided to make Jython
     * script access easier to read.  
     */
    public void setState(int s) throws jmri.JmriException { setKnownState(s); }
    
    /**
     * Implement a shorter name for getKnownState.
     *<P>
     * This generally shouldn't be used by Java code; use 
     * getKnownState instead.  The is provided to make Jython
     * script access easier to read.  
     */
    public int getState() { return getKnownState(); }

    /**
     * Control whether the actual sensor input is
     * considered to be inverted, e.g. the normal
     * electrical signal that results in an ACTIVE state
     * now results in an INACTIVE state.
     */
    public void setInverted(boolean inverted) {
        boolean oldInverted = _inverted;
        _inverted = inverted;
        if (oldInverted != _inverted) {
            firePropertyChange("inverted", Boolean.valueOf(oldInverted), Boolean.valueOf(_inverted));
            int state = _knownState;
            if (state == ACTIVE) {
                setOwnState(INACTIVE);
            } else if (state == INACTIVE) {
                setOwnState(ACTIVE);
            }
        }
    }
    
    /**
     * Get the inverted state.  If true, the 
     * electrical signal that results in an ACTIVE state
     * now results in an INACTIVE state.
     * <P>
     * Used in polling loops in system-specific code, 
     * so made final to allow optimization.
     */
    final public boolean getInverted() { return _inverted; }


    protected boolean _inverted = false;
    
    // internal data members
    protected int _knownState     = UNKNOWN;
    protected int _rawState       = UNKNOWN;
    
    Reporter reporter = null;
    
    /**
     * Some sensor boards also serve the function of being able to report
     * back train identities via such methods as RailCom.
     * The setting and creation of the reporter against the sensor should be
     * done when the sensor is created.  This information is not saved.
     * <p>
     * returns null if there is no direct reporter.
     */
    public void setReporter(Reporter er){
        if(reporter!=null && reporter!=er)
            reporter = er;
    }
    
    public Reporter getReporter(){
        return reporter;
    }

}

/* @(#)AbstractSensor.java */
