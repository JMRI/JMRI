// EasyDccSystemConnectionMemo.java
package jmri.jmrix.easydcc;

import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.ProgrammerManager;
import jmri.ThrottleManager;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author	Bob Jacobsen Copyright (C) 2010 Kevin Dickerson
 * @version $Revision$
 */
public class EasyDccSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public EasyDccSystemConnectionMemo(EasyDccTrafficController et) {
        super("E", "EasyDCC");
        this.et = et;
        register();
        InstanceManager.store(this, EasyDccSystemConnectionMemo.class); // also register as specific type
        /*InstanceManager.store(cf = new jmri.jmrix.easydcc.swing.ComponentFactory(this),
         jmri.jmrix.swing.ComponentFactory.class);*/
    }

    public EasyDccSystemConnectionMemo() {
        super("E", "EasyDCC");
        register(); // registers general type
        InstanceManager.store(this, EasyDccSystemConnectionMemo.class); // also register as specific type
        //Needs to be implemented
        /*InstanceManager.store(cf = new jmri.jmrix.easydcc.swing.ComponentFactory(this),
         jmri.jmrix.swing.ComponentFactory.class);*/
    }

    /* Temp until it is refactored to completely for multiple connections*/
    public EasyDccSystemConnectionMemo(String prefix, String name) {
        super(prefix, name);
        register(); // registers general type
        InstanceManager.store(this, EasyDccSystemConnectionMemo.class); // also register as specific type
        //Needs to be implemented
        /*InstanceManager.store(cf = new jmri.jmrix.easydcc.swing.ComponentFactory(this),
         jmri.jmrix.swing.ComponentFactory.class);*/
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     */
    public EasyDccTrafficController getTrafficController() {
        return et;
    }

    public void setEasyDccTrafficController(EasyDccTrafficController et) {
        this.et = et;
    }
    private EasyDccTrafficController et;

    /**
     * Configure the common managers for Easy DCC connections. This puts the
     * common manager config in one place. This method is static so that it can
     * be referenced from classes that don't inherit.
     */
    public void configureManagers() {

        InstanceManager.setProgrammerManager(getProgrammerManager());

        InstanceManager.store(getPowerManager(), jmri.PowerManager.class);

        InstanceManager.setTurnoutManager(getTurnoutManager());

        InstanceManager.setThrottleManager(getThrottleManager());

        InstanceManager.setConsistManager(getConsistManager());

        commandStation = new jmri.jmrix.easydcc.EasyDccCommandStation(this);

        InstanceManager.setCommandStation(commandStation);
    }

    /**
     * Tells which managers this provides by class
     */
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

        if (type.equals(jmri.ThrottleManager.class)) {
            return true;
        }
        if (type.equals(jmri.PowerManager.class)) {
            return true;
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        }
        if (type.equals(jmri.ConsistManager.class)) {
            return true;
        }
        if (type.equals(jmri.CommandStation.class)) {
            return true;
        }
        return false; // nothing, by default
    }

    @SuppressWarnings("unchecked")
    @Override
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

        if (T.equals(jmri.ThrottleManager.class)) {
            return (T) getThrottleManager();
        }
        if (T.equals(jmri.PowerManager.class)) {
            return (T) getPowerManager();
        }
        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        if (T.equals(jmri.ConsistManager.class)) {
            return (T) getConsistManager();
        }
        if (T.equals(jmri.CommandStation.class)) {
            return (T) commandStation;
        }
        return null; // nothing, by default
    }

    private EasyDccPowerManager powerManager;

    public EasyDccPowerManager getPowerManager() {
        if (getDisabled()) {
            return null;
        }
        if (powerManager == null) {
            powerManager = new jmri.jmrix.easydcc.EasyDccPowerManager(this);
        }
        return powerManager;
    }

    private ThrottleManager throttleManager;

    public ThrottleManager getThrottleManager() {
        if (getDisabled()) {
            return null;
        }
        if (throttleManager == null) {
            throttleManager = new jmri.jmrix.easydcc.EasyDccThrottleManager(this);
        }
        return throttleManager;
    }

    public void setThrottleManager(ThrottleManager t) {
        throttleManager = t;
    }

    private EasyDccTurnoutManager turnoutManager;

    public EasyDccTurnoutManager getTurnoutManager() {
        if (getDisabled()) {
            return null;
        }
        if (turnoutManager == null) {
            turnoutManager = jmri.jmrix.easydcc.EasyDccTurnoutManager.instance();
        }
        return turnoutManager;
    }

    private EasyDccConsistManager consistManager;

    public EasyDccConsistManager getConsistManager() {
        if (getDisabled()) {
            return null;
        }
        if (consistManager == null) {
            consistManager = new jmri.jmrix.easydcc.EasyDccConsistManager();
        }
        return consistManager;
    }

    private ProgrammerManager programmerManager;

    public ProgrammerManager getProgrammerManager() {
        if (programmerManager == null) {
            programmerManager = new EasyDccProgrammerManager(new EasyDccProgrammer(), this);
        }
        return programmerManager;
    }

    public void setProgrammerManager(ProgrammerManager p) {
        programmerManager = p;
    }

    private EasyDccCommandStation commandStation;

    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.easydcc.EasyDccActionListBundle");
    }

    public void dispose() {
        et = null;
        InstanceManager.deregister(this, EasyDccSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        if (powerManager != null) {
            InstanceManager.deregister(powerManager, jmri.jmrix.easydcc.EasyDccPowerManager.class);
        }
        if (turnoutManager != null) {
            InstanceManager.deregister(turnoutManager, jmri.jmrix.easydcc.EasyDccTurnoutManager.class);
        }
        if (throttleManager != null) {
            InstanceManager.deregister(((EasyDccThrottleManager) throttleManager), jmri.jmrix.easydcc.EasyDccThrottleManager.class);
        }
        if (consistManager != null) {
            InstanceManager.deregister(consistManager, jmri.jmrix.easydcc.EasyDccConsistManager.class);
        }
        super.dispose();
    }
}


/* @(#)InternalSystemConnectionMemo.java */
