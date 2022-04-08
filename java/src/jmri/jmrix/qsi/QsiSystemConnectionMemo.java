package jmri.jmrix.qsi;

import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.managers.DefaultProgrammerManager;
import jmri.util.NamedBeanComparator;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Kevin Dickerson Copyright (C) 2012
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class QsiSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    public QsiSystemConnectionMemo(QsiTrafficController st) {
        super("Q", "Quantum Programmer");
        this.st = st;
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
     * @return the QSI traffic controller.
     */
    public QsiTrafficController getQsiTrafficController() {
        return st;
    }

    public void setQsiTrafficController(QsiTrafficController st) {
        this.st = st;
    }
    private QsiTrafficController st;

    /**
     * Provide a menu with all items attached to this system connection.
     * @return new QSIMenu.
     */
    public javax.swing.JMenu getMenu() {
        return new QSIMenu("QSI",this);
    }

    /**
     * Configure the common managers for Qsi connections. This puts the common
     * manager config in one place.
     */
    public void configureManagers() {
        store(getProgrammerManager(), jmri.AddressedProgrammerManager.class);
        InstanceManager.store(getProgrammerManager(), jmri.AddressedProgrammerManager.class);
        store(getProgrammerManager(), GlobalProgrammerManager.class);
        InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        register();
    }

    public DefaultProgrammerManager getProgrammerManager() {
        return (DefaultProgrammerManager) classObjectMap.computeIfAbsent(DefaultProgrammerManager.class,(Class<?> c) -> new DefaultProgrammerManager(new QsiProgrammer(this),this));
    }

    public void setProgrammerManager(DefaultProgrammerManager p) {
        store(p,DefaultProgrammerManager.class);
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
