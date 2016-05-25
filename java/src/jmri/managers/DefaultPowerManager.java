package jmri.managers;

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

    public void setPower(int v) {
        int oldvalue = power;
        power = v; // make change immediately in this implementation
        firePropertyChange("Power", oldvalue, power);
    }

    public String getUserName() {
        return "Internal";
    }

    public int getPower() {
        return power;
    }

    // to free resources when no longer used
    public void dispose() throws JmriException {
    }

    // to hear of changes
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

}
