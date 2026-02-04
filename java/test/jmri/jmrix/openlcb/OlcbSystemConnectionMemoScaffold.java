package jmri.jmrix.openlcb;

import java.util.List;
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
public class OlcbSystemConnectionMemoScaffold extends jmri.jmrix.can.CanSystemConnectionMemo {

    public OlcbSystemConnectionMemoScaffold() {
        //super("M", "OpenLCB");
        register(); // registers general type
        InstanceManager.store(this, OlcbSystemConnectionMemoScaffold.class); // also register as specific type
    }

    public OlcbSystemConnectionMemoScaffold(String prefix) {
        super(prefix);
        register(); // registers general type
        InstanceManager.store(this, OlcbSystemConnectionMemoScaffold.class); // also register as specific type
    }

    final jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Tells which managers this class provides.
     * {@inheritDoc}
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (type.equals(jmri.GlobalProgrammerManager.class)) {
            if (programmerManager == null) {
                return false;
            }
            return programmerManager.isGlobalProgrammerAvailable();
        }
        if (type.equals(jmri.AddressedProgrammerManager.class)) {
            if (programmerManager == null) {
                return false;
            }
            return programmerManager.isAddressedModePossible();
        }
        if (type.equals(jmri.SensorManager.class)) {
            return true;
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        }
        if (type.equals(jmri.ThrottleManager.class)) {
            return true;
        }
        return super.provides(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<T> T) {
        if (getDisabled()) {
            return null;
        }
        if (T.equals(jmri.GlobalProgrammerManager.class)) {
            if (programmerManager == null) {
                return null;
            }
            if (!programmerManager.isGlobalProgrammerAvailable()) {
                return null;
            }
            return (T)programmerManager;
        }
        if (T.equals(jmri.AddressedProgrammerManager.class)) {
            if (programmerManager == null) {
                return null;
            }
            if (!programmerManager.isAddressedModePossible()) {
                return null;
            }
            return (T)programmerManager;
        }
        if (T.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        if (T.equals(jmri.ThrottleManager.class)) {
            return (T) getThrottleManager();
        }
        if (T.equals(OlcbInterface.class)) {
            return (T) getInterface();
        }
        return super.get(T);
    }

    /**
     * Configure the common managers for the connection. This puts the common
     * manager config in one place.
     */
    @Override
    public void configureManagers() {

        InstanceManager.setSensorManager(getSensorManager());

        InstanceManager.setTurnoutManager(getTurnoutManager());

        InstanceManager.store(getThrottleManager(), jmri.ThrottleManager.class);
    }

    /*
     * Provides access to the ... for this particular connection.
     */
    protected OlcbProgrammerManager programmerManager;

    public OlcbProgrammerManager getProgrammerManager() {
        return programmerManager;
    }

    public void setProgrammerManager(OlcbProgrammerManager p) {
        programmerManager = p;
        if (p.isAddressedModePossible()) {
            InstanceManager.store(p, jmri.AddressedProgrammerManager.class);
        }
        if (p.isGlobalProgrammerAvailable()) {
            InstanceManager.store(p, GlobalProgrammerManager.class);
        }
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

    protected OlcbThrottleManager throttleManager;

    public OlcbThrottleManager getThrottleManager() {
        if (getDisabled()) {
            return null;
        }
        if (throttleManager == null) {
            throttleManager = new OlcbThrottleManager(this);
        }
        return throttleManager;
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
        if (olcbInterface != null) {
            return olcbInterface;
        }
        // We check if someone instantiated an OlcbConfigurationManager in the test or the fixture. If so, we use the
        // interface from that object. (The superclass CanSystemConnectionMemo does instantiate an object like this and
        // forwards the get<T>() calls to it, which does find the OlcbInterface there.)
        List<OlcbConfigurationManager> l = InstanceManager.getList(OlcbConfigurationManager.class);
        if (!l.isEmpty()) {
            return l.get(l.size() - 1).getInterface();
        }
        return null;
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
        InstanceManager.deregister(this, OlcbSystemConnectionMemoScaffold.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        if (turnoutManager != null) {
            InstanceManager.deregister(turnoutManager, OlcbTurnoutManager.class);
        }
        if (sensorManager != null) {
            InstanceManager.deregister(sensorManager, OlcbSensorManager.class);
        }
        if (throttleManager != null) {
            InstanceManager.deregister(throttleManager, OlcbThrottleManager.class);
        }
        if (programmerManager != null) {
            InstanceManager.deregister(programmerManager, jmri.AddressedProgrammerManager.class);
            InstanceManager.deregister(programmerManager, GlobalProgrammerManager.class);
        }
        super.dispose();
    }

}
