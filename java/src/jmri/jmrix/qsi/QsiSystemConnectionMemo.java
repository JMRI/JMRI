package jmri.jmrix.qsi;

import java.util.Comparator;
import java.util.ResourceBundle;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.managers.DefaultProgrammerManager;
import jmri.util.NamedBeanComparator;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class QsiSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public QsiSystemConnectionMemo(QsiTrafficController st) {
        super("Q", "Quantum Programmer");
        this.st = st;
        register();
        InstanceManager.store(this, QsiSystemConnectionMemo.class); // also register as specific type
        InstanceManager.store(cf = new jmri.jmrix.qsi.swing.QsiComponentFactory(this),
        jmri.jmrix.swing.ComponentFactory.class);
    }

    public QsiSystemConnectionMemo() {
        super("Q", "Quantum Programmer"); // "Quantum Programmer"
        register(); // registers general type
        InstanceManager.store(this, QsiSystemConnectionMemo.class); // also register as specific type

        InstanceManager.store(cf = new jmri.jmrix.qsi.swing.QsiComponentFactory(this),
        jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     */
    public QsiTrafficController getQsiTrafficController() {
        return st;
    }

    public void setQsiTrafficController(QsiTrafficController st) {
        this.st = st;
    }
    private QsiTrafficController st;

    /**
     * Provide a menu with all items attached to this system connection
     */
    public javax.swing.JMenu getMenu() {
        return new QSIMenu("QSI",this);
    }

    /**
     * Configure the programming manager and "command station" objects
     */
    public void configureCommandStation() {

    }

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (type.equals(jmri.GlobalProgrammerManager.class)) {
            return getProgrammerManager().isGlobalProgrammerAvailable();
        }
        if (type.equals(jmri.AddressedProgrammerManager.class)) {
            return getProgrammerManager().isAddressedModePossible();
        }
        return false; // nothing, by default
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (T.equals(jmri.GlobalProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.AddressedProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        return null; // nothing, by default
    }

    /**
     * Configure the common managers for Qsi connections. This puts the common
     * manager config in one place.
     */
    public void configureManagers() {
        InstanceManager.store(getProgrammerManager(), jmri.AddressedProgrammerManager.class);
        InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
    }

    private DefaultProgrammerManager programmerManager;

    public DefaultProgrammerManager getProgrammerManager() {
        if (programmerManager == null) {
            programmerManager = new jmri.managers.DefaultProgrammerManager(new jmri.jmrix.qsi.QsiProgrammer(this), this);
        }
        return programmerManager;
    }

    public void setProgrammerManager(DefaultProgrammerManager p) {
        programmerManager = p;
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.qsi.QsiActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        st = null;
        InstanceManager.deregister(this, QsiSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

}
