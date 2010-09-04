// AbstractSensor.java

package jmri.implementation;

import jmri.Sensor;

/**
 * Abstract class providing the basic logic of the Sensor interface
 * <p>
 * Sensor system names are always upper case.
 *
 * @author			Bob Jacobsen Copyright (C) 2001, 2009
 * @version         $Revision: 1.5 $
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

    // setKnownState() for implementations that can't
    // actually do it on the layout. Not intended for use by implementations
    // that can
    public void setKnownState(int s) throws jmri.JmriException {
        if (_knownState != s) {
            int oldState = _knownState;
            _knownState = s;
            firePropertyChange("KnownState", Integer.valueOf(oldState), Integer.valueOf(_knownState));
        }
    }

    /**
     * Set out internal state information, and notify bean listeners.
     */
    public void setOwnState(int s) {
        if (_knownState != s) {
            int oldState = _knownState;
            _knownState = s;
            firePropertyChange("KnownState", Integer.valueOf(oldState), Integer.valueOf(_knownState));
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
            firePropertyChange("inverted", new Boolean(oldInverted), new Boolean(_inverted));
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

}

/* @(#)AbstractSensor.java */
