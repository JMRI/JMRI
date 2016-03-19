// Mx1SystemConnectionMemo.javaf
package jmri.jmrix.zimo;

import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.ProgrammerManager;

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
public class Mx1SystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public Mx1SystemConnectionMemo(Mx1TrafficController st) {
        super("Z", "MX-1");
        this.st = st;
        register();
        InstanceManager.store(this, Mx1SystemConnectionMemo.class); // also register as specific type
        // create and register the ComponentFactory
        InstanceManager.store(componentFactory = new jmri.jmrix.zimo.swing.Mx1ComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory componentFactory = null;

    public Mx1SystemConnectionMemo() {
        super("Z", "MX-1");
        register(); // registers general type
        InstanceManager.store(this, Mx1SystemConnectionMemo.class); // also register as specific type

        InstanceManager.store(componentFactory = new jmri.jmrix.zimo.swing.Mx1ComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    public final static int MX1 = 0x00;
    public final static int MXULF = 0x01;
    public final static int MX10 = 0x02;

    int connectionType = 0x00;

    public void setConnectionType(int connection) {
        connectionType = connection;
    }

    public int getConnectionType() {
        return connectionType;
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     */
    public Mx1TrafficController getMx1TrafficController() {
        return st;
    }

    public void setMx1TrafficController(Mx1TrafficController st) {
        this.st = st;
    }
    private Mx1TrafficController st;

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

        if (type.equals(jmri.PowerManager.class)) {
            return true;
        }
        if (type.equals(jmri.ThrottleManager.class)) {
            return true;
        }
        if (getProtocol() == Mx1Packetizer.BINARY) {
            if (type.equals(jmri.TurnoutManager.class)) {
                return true;
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
        if (getProtocol() == Mx1Packetizer.BINARY) {
            if (T.equals(jmri.TurnoutManager.class)) {
                return (T) getTurnoutManager();
            }
        }
        /*if (T.equals(jmri.CommandStation.class))
         return (T)commandStation;*/
        return null; // nothing, by default
    }

    /**
     * Configure the common managers for Mx1 connections. This puts the common
     * manager config in one place. This method is static so that it can be
     * referenced from classes that don't inherit, including
     * hexfile.HexFileFrame and locormi.LnMessageClient
     */
    public void configureManagers() {

        jmri.InstanceManager.setProgrammerManager(
                getProgrammerManager());

        powerManager = new jmri.jmrix.zimo.Mx1PowerManager(this);
        jmri.InstanceManager.setPowerManager(powerManager);

        throttleManager = new jmri.jmrix.zimo.Mx1ThrottleManager(this);
        InstanceManager.setThrottleManager(throttleManager);
        if (getProtocol() == Mx1Packetizer.BINARY) {
            turnoutManager = new Mx1TurnoutManager(getMx1TrafficController(), getSystemPrefix());
            InstanceManager.setTurnoutManager(turnoutManager);
        }
    }

    boolean getProtocol() {
        if (getMx1TrafficController() != null) {
            return getMx1TrafficController().getProtocol();
        }
        return Mx1Packetizer.ASCII;
    }

    private ProgrammerManager programmerManager;
    private Mx1PowerManager powerManager;
    private Mx1ThrottleManager throttleManager;
    private Mx1TurnoutManager turnoutManager;

    public ProgrammerManager getProgrammerManager() {
        if (programmerManager == null) {
            if (getProtocol() == Mx1Packetizer.BINARY) {
                programmerManager = new Mx1ProgrammerManager(new Mx1Programmer(getMx1TrafficController()), this);
            } else {
                programmerManager = new jmri.managers.DefaultProgrammerManager(new jmri.jmrix.zimo.Mx1Programmer(getMx1TrafficController()), this);
            }
        }
        return programmerManager;
    }

    public void setProgrammerManager(ProgrammerManager p) {
        programmerManager = p;
    }

    public Mx1TurnoutManager getTurnoutManager() {
        return turnoutManager;
    }

    public void setCommandStation(Mx1CommandStation cs) {
        //commandStation=cs;
    }

    //private Mx1CommandStation commandStation;
    public Mx1PowerManager getPowerManager() {
        return powerManager;
    }

    public Mx1ThrottleManager getThrottleManager() {
        return throttleManager;
    }

    protected ResourceBundle getActionModelResourceBundle() {
        //No actions that can be loaded at startup
        return null;
    }

    public void dispose() {
        st = null;
        InstanceManager.deregister(this, Mx1SystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        if (throttleManager != null) {
            InstanceManager.deregister(throttleManager, jmri.jmrix.zimo.Mx1ThrottleManager.class);
        }
        super.dispose();
    }
}


/* @(#)Mx1SystemConnectionMemo.java */
