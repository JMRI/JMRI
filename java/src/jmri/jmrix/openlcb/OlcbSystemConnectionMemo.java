package jmri.jmrix.openlcb;

import java.util.ResourceBundle;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import org.openlcb.OlcbInterface;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class OlcbSystemConnectionMemo extends jmri.jmrix.can.CanSystemConnectionMemo {

    public OlcbSystemConnectionMemo() {
        //super("M", "OpenLCB");
        register(); // registers general type
        InstanceManager.store(this, OlcbSystemConnectionMemo.class); // also register as specific type
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Tells which managers are provides by class
     */
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

        if (type.equals(jmri.SensorManager.class)) {
            return true;
        }
        if (type.equals(jmri.TurnoutManager.class)) {
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
        if (T.equals(jmri.GlobalProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.AddressedProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }

        if (T.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        if (T.equals(OlcbInterface.class)) {
            return (T) getInterface();
        }
        return null; // nothing, by default
    }

    /**
     * Configure the common managers for the connection. This puts the common
     * manager config in one place.
     */
    @Override
    public void configureManagers() {

        InstanceManager.setSensorManager(getSensorManager());

        InstanceManager.setTurnoutManager(getTurnoutManager());

        if (getProgrammerManager().isAddressedModePossible()) {
            InstanceManager.setAddressedProgrammerManager(getProgrammerManager());
        }
        if (getProgrammerManager().isGlobalProgrammerAvailable()) {
            InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        }

    }

    /*
     * Provides access to the ... for this particular connection.
     */
    protected OlcbProgrammerManager programmerManager;

    public OlcbProgrammerManager getProgrammerManager() {
        if (programmerManager == null) {
            programmerManager = new OlcbProgrammerManager(new OlcbProgrammer());
        }
        return programmerManager;
    }

    public void setProgrammerManager(OlcbProgrammerManager p) {
        programmerManager = p;
    }

    protected OlcbTurnoutManager turnoutManager;

    public OlcbTurnoutManager getTurnoutManager() {
        if (getDisabled()) {
            return null;
        }
        if (turnoutManager == null) {
            turnoutManager = new OlcbTurnoutManager(this);
        }
        return turnoutManager;
    }

    protected OlcbSensorManager sensorManager;

    public OlcbSensorManager getSensorManager() {
        if (getDisabled()) {
            return null;
        }
        if (sensorManager == null) {
            sensorManager = new OlcbSensorManager(this);
        }
        return sensorManager;
    }

    protected OlcbInterface olcbInterface;

    public OlcbInterface getInterface() {
        return olcbInterface;
    }

    public void setInterface(OlcbInterface iface) {
        olcbInterface = iface;
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.openlcb.OlcbActionListBundle");
    }

    @Override
    public void dispose() {
        InstanceManager.deregister(this, OlcbSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        if (turnoutManager != null) {
            InstanceManager.deregister(turnoutManager, OlcbTurnoutManager.class);
        }
        if (sensorManager != null) {
            InstanceManager.deregister(sensorManager, OlcbSensorManager.class);
        }
        super.dispose();
    }
}
