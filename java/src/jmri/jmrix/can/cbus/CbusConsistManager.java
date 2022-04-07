package jmri.jmrix.can.cbus;

import java.util.Vector;

import jmri.implementation.*;
import jmri.CommandStation;
import jmri.ConsistManager.EnableListener;

/**
 * The CBUS Consist Manager which uses the NmraConsist class for
 * the consists it builds. This implementation just tracks the
 * consist via a table of {@link jmri.implementation.DccConsist} objects
 * that handle the actual operations.
 *
 * @author Andrew Crosland Copyright (C) 2022
 */
public class CbusConsistManager extends NmraConsistManager implements EnableListener {

    public CbusConsistManager(CommandStation cs) {
        super(cs);
    }

    /**
     * Can this consist manager be disabled?
     * 
     * @return true if the manager can be disabled, false otherwise
     */
    @Override
    public boolean canBeDisabled() {
        return true;
    }

    protected static final Vector<EnableListener> enableListeners = new Vector<EnableListener>();
    protected boolean _enabled = false;
    
    /**
     * Register a listener that is called if this manager is enabled or disabled.
     * @param listener the listener
     */
    @Override
    public void registerEnableListener(EnableListener listener) {
        // add only if not already registered
        if (listener == null) {
            throw new java.lang.NullPointerException();
        }
        if (!enableListeners.contains(listener)) {
            enableListeners.addElement(listener);
        }
    }

    /**
     * Unregister a listener that is called if this manager is enabled or disabled.
     * @param listener the listener
     */
    @Override
    public void unregisterEnableListener(EnableListener listener) {
        if (enableListeners.contains(listener)) {
            enableListeners.removeElement(listener);
        }
    }

    /**
     * Check if this manager is enabled
     * @return true if enabled
     */
    @Override
    public boolean isEnabled() {
        return _enabled;
    }

    /**
     * A listener that listens to whether the manager is enabled or disabled.
     * 
     * Call each listeners setEnabled() method.
     * 
     * @param value true to enable
     */
    @Override
    public void setEnabled(boolean value) {
        _enabled = value;
        for (int i = 0; i < enableListeners.size(); i++) {
            enableListeners.elementAt(i).setEnabled(value);
        }
    }

}
