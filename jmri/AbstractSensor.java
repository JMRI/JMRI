// AbstractSensor.java

package jmri;
import jmri.Sensor;

/**
 * Abstract class providing the basic logic of the Sensor interface
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version         $Revision: 1.9 $
 */
public abstract class AbstractSensor extends AbstractNamedBean implements Sensor, java.io.Serializable {

    // ctor takes a system-name string for initialization
    public AbstractSensor(String systemName) {
        super(systemName);
    }

    public AbstractSensor(String systemName, String userName) {
        super(systemName, userName);
    }

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //		public void firePropertyChange(String propertyName,
    //					       Object oldValue,
    //					       Object newValue)
    // _once_ if anything has changed state

    public int getKnownState() {return _knownState;}

    // setKnownState() for implementations that can't
    // actually do it on the layout. Not intended for use by implementations
    // that can
    public void setKnownState(int s) throws jmri.JmriException {
        if (_knownState != s) {
            int oldState = _knownState;
            _knownState = s;
            firePropertyChange("KnownState", new Integer(oldState), new Integer(_knownState));
        }
    }

    /**
     * Set out internal state information, and notify bean listeners.
     */
    public void setOwnState(int s) {
        if (_knownState != s) {
            int oldState = _knownState;
            _knownState = s;
            firePropertyChange("KnownState", new Integer(oldState), new Integer(_knownState));
        }
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

    // internal data members
    private String _id;
    protected int _knownState     = UNKNOWN;

    // since we can't do a "super(this)" in the ctor to inherit from PropertyChangeSupport, we'll
    // reflect to it
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

}

/* @(#)AbstractSensor.java */
