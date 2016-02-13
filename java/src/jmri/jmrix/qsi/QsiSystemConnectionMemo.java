// QsiSystemConnectionMemo.javaf
package jmri.jmrix.qsi;

import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.ProgrammerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 * @author	Bob Jacobsen Copyright (C) 2010
 * @version $Revision: 19712 $
 */
public class QsiSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public QsiSystemConnectionMemo(QsiTrafficController st) {
        super("Q", "Quantum Programmer");
        this.st = st;
        register();
        InstanceManager.store(this, QsiSystemConnectionMemo.class); // also register as specific type
    }

    public QsiSystemConnectionMemo() {
        super("Q", "Quantum Programmer"); //"Quantum Programmer"
        register(); // registers general type
        InstanceManager.store(this, QsiSystemConnectionMemo.class); // also register as specific type

        //Needs to be implemented
        /*InstanceManager.store(cf = new jmri.jmrix.ecos.swing.ComponentFactory(this), 
         jmri.jmrix.swing.ComponentFactory.class);*/
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
        return new QSIMenu("QSI");
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
        if (type.equals(jmri.ProgrammerManager.class)) {
            return true;
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
        if (T.equals(jmri.ProgrammerManager.class)) {
            return (T) getProgrammerManager();
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
     * manager config in one place. This method is static so that it can be
     * referenced from classes that don't inherit, including
     * hexfile.HexFileFrame and locormi.LnMessageClient
     */
    public void configureManagers() {
        jmri.InstanceManager.setProgrammerManager(
                getProgrammerManager());
    }

    private ProgrammerManager programmerManager;

    public ProgrammerManager getProgrammerManager() {
        if (programmerManager == null) {
            programmerManager = new jmri.managers.DefaultProgrammerManager(jmri.jmrix.qsi.QsiProgrammer.instance(), this);
        }
        return programmerManager;
    }

    public void setProgrammerManager(ProgrammerManager p) {
        programmerManager = p;
    }

    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.qsi.QsiActionListBundle");
    }

    public void dispose() {
        st = null;
        InstanceManager.deregister(this, QsiSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(QsiSystemConnectionMemo.class.getName());
}


/* @(#)QsiSystemConnectionMemo.java */
