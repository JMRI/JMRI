package jmri.managers;

import jmri.PowerManager;

/**
 * Base PowerManager implementation for controlling layout power.
 * <p>
 * These are registered when they are added to the InstanceManager
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2001, 2003, 2010
 */
abstract public class AbstractPowerManager implements PowerManager {

    public AbstractPowerManager(jmri.jmrix.SystemConnectionMemo memo) {
        this.userName = memo.getUserName();
    }

    public String getUserName() {
        return userName;
    }

    String userName;

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
