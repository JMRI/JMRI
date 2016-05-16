// SprogSystemConnectionMemo.javaf
package jmri.jmrix.sprog;

import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.ProgrammerManager;
import jmri.ThrottleManager;
import jmri.jmrix.sprog.SprogConstants.SprogMode;
import jmri.jmrix.sprog.update.SprogType;
import jmri.jmrix.sprog.update.SprogVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 * @version $Revision$
 */
public class SprogSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public SprogSystemConnectionMemo(SprogTrafficController st, SprogMode sm) {
        super("S", "Sprog");
        this.st = st;
        sprogMode = sm;  // static
        sprogVersion = new SprogVersion(new SprogType(SprogType.UNKNOWN));
        register();
        InstanceManager.store(this, SprogSystemConnectionMemo.class); // also register as specific type
    }

    public SprogSystemConnectionMemo(SprogMode sm) {
        super("S", "Sprog");
        sprogMode = sm;  // static
        sprogVersion = new SprogVersion(new SprogType(SprogType.UNKNOWN));
        register();
        InstanceManager.store(this, SprogSystemConnectionMemo.class); // also register as specific type
    }

    public SprogSystemConnectionMemo(SprogMode sm, SprogType type) {
        super("S", "Sprog");
        sprogMode = sm;  // static
        sprogVersion = new SprogVersion(type);
        register();
        InstanceManager.store(this, SprogSystemConnectionMemo.class); // also register as specific type
    }

    public SprogSystemConnectionMemo() {
        super("S", "Sprog");
        register(); // registers general type
        sprogVersion = new SprogVersion(new SprogType(SprogType.UNKNOWN));
        InstanceManager.store(this, SprogSystemConnectionMemo.class); // also register as specific type

        //Needs to be implemented
        /*InstanceManager.store(cf = new jmri.jmrix.ecos.swing.ComponentFactory(this), 
         jmri.jmrix.swing.ComponentFactory.class);*/
    }

    /**
     * Set the SPROG mode for this connection
     * 
     * @param mode 
     */
    public void setSprogMode(SprogMode mode) {
        sprogMode = mode;
    }

    /**
     * Return the SPROG mode for this connection
     * 
     * @return SprogMode 
     */
    public SprogMode getSprogMode() {
        return sprogMode;
    }
    private SprogMode sprogMode;// = SprogMode.SERVICE;
    
    /**
     * Return the SPROG version object for this connection
     * 
     * @return SprogVersion
     */
    public SprogVersion getSprogVersion() {
        return sprogVersion;
    }

    /**
     * Set the SPROG version object for this connection
     */
    public void setSprogVersion(SprogVersion version) {
        sprogVersion = version;
    }

    private SprogVersion sprogVersion;

    /**
     * Return the type of SPROG connected
     * 
     * @return SprogType
     */
    public SprogType getSprogType() {
        return sprogVersion.sprogType;
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     */
    public SprogTrafficController getSprogTrafficController() {
        return st;
    }

    public void setSprogTrafficController(SprogTrafficController st) {
        this.st = st;
    }
    private SprogTrafficController st;

    private Thread slotThread;

    /**
     * Configure the programming manager and "command station" objects
     */
    public void configureCommandStation() {
        switch (sprogMode) {
            case OPS:
                log.debug("start command station queuing thread");
                slotThread = new Thread(jmri.jmrix.sprog.SprogCommandStation.instance());
                SprogCommandStation.instance().setSystemConnectionMemo(this);
                slotThread.start();
                jmri.InstanceManager.setCommandStation(SprogCommandStation.instance());
                break;
            case SERVICE:
                break;
        }
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

        if (type.equals(jmri.PowerManager.class)) {
            return true;
        }
        if (type.equals(jmri.ThrottleManager.class)) {
            return true;
        }
        if ((type.equals(jmri.CommandStation.class))) {
            if (sprogMode == null) {
                return false;
            }
            switch (sprogMode) {
                case OPS:
                    return true;
                case SERVICE:
                    return false;
            }
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

        if (T.equals(jmri.PowerManager.class)) {
            return (T) getPowerManager();
        }
        if (T.equals(jmri.ThrottleManager.class)) {
            return (T) getThrottleManager();
        }
        if (T.equals(jmri.CommandStation.class)) {
            return (T) SprogCommandStation.instance();
        }
        return null; // nothing, by default
    }

    /**
     * Configure the common managers for Sprog connections. This puts the common
     * manager config in one place. This method is static so that it can be
     * referenced from classes that don't inherit, including
     * hexfile.HexFileFrame and locormi.LnMessageClient
     */
    public void configureManagers() {

        jmri.InstanceManager.setProgrammerManager(
                getProgrammerManager());

        powerManager = new jmri.jmrix.sprog.SprogPowerManager(this);
        jmri.InstanceManager.store(powerManager, jmri.PowerManager.class);

        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.sprog.SprogTurnoutManager());

        switch (sprogMode) {
            case OPS:
                sprogCSThrottleManager = new jmri.jmrix.sprog.SprogCSThrottleManager(this);
                jmri.InstanceManager.setThrottleManager(sprogCSThrottleManager);
                break;
            case SERVICE:
                sprogThrottleManager = new jmri.jmrix.sprog.SprogThrottleManager(this);
                jmri.InstanceManager.setThrottleManager(sprogThrottleManager);
                break;

        }

    }

    private ProgrammerManager programmerManager;
    private SprogCSThrottleManager sprogCSThrottleManager;
    private SprogThrottleManager sprogThrottleManager;
    private SprogPowerManager powerManager;

    public ProgrammerManager getProgrammerManager() {
        if (programmerManager == null) {
            programmerManager = new SprogProgrammerManager(new SprogProgrammer(), sprogMode, this);
        }
        return programmerManager;
    }

    public void setProgrammerManager(ProgrammerManager p) {
        programmerManager = p;
    }

    public SprogPowerManager getPowerManager() {
        return powerManager;
    }

    public ThrottleManager getThrottleManager() {
        if (sprogMode == null) {
            log.error("Sprog Mode not set");
            return null;
        }
        switch (sprogMode) {
            case OPS:
                return sprogCSThrottleManager;
            case SERVICE:
                return sprogThrottleManager;
        }
        return null;
    }

    protected ResourceBundle getActionModelResourceBundle() {
        //No actions that can be loaded at startup
        return null;
    }

    public void dispose() {
        st = null;
        InstanceManager.deregister(this, SprogSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(SprogSystemConnectionMemo.class.getName());
}


/* @(#)SprogSystemConnectionMemo.java */
