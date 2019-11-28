package jmri.managers;

import java.beans.PropertyChangeListener;

import jmri.JmriException;
import jmri.PowerManager;

/**
 * Default implementation for controlling layout power
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008, 2010
 */
public class DefaultPowerManager implements PowerManager {

    public DefaultPowerManager() {
    }

    int power = UNKNOWN;

    @Override
    public void setPower(int v) {
        int oldvalue = power;
        power = v; // make change immediately in this implementation
        firePropertyChange("Power", oldvalue, power);
    }

    @Override
    public String getUserName() {
        return "Internal";
    }

    @Override
    public int getPower() {
        return power;
    }

    // to free resources when no longer used
    @Override
    public void dispose() throws JmriException {
    }

    // to hear of changes
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    @Override
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    @Override
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

}
