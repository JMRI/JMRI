package jmri.jmrix.tams;

import java.util.Comparator;
import java.util.ResourceBundle;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.util.NamedBeanComparator;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * Based on work by Bob Jacobsen
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 */
public class TamsSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public TamsSystemConnectionMemo(TamsTrafficController et) {
        super("T", "Tams");
        this.et = et;
        et.setAdapterMemo(this);
        register();
        InstanceManager.store(this, TamsSystemConnectionMemo.class); // also register as specific type
        InstanceManager.store(cf = new jmri.jmrix.tams.swing.TamsComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    public TamsSystemConnectionMemo() {
        super("T", "Tams");
        register(); // registers general type
        InstanceManager.store(this, TamsSystemConnectionMemo.class); // also register as specific type
        //Needs to be implemented
        InstanceManager.store(cf = new jmri.jmrix.tams.swing.TamsComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     */
    public TamsTrafficController getTrafficController() {
        return et;
    }

    public void setTamsTrafficController(TamsTrafficController et) {
        this.et = et;
        et.setAdapterMemo(this);
    }
    private TamsTrafficController et;

    /**
     * This puts the common manager config in one place.
     */
    public void configureManagers() {

        powerManager = new jmri.jmrix.tams.TamsPowerManager(getTrafficController());
        jmri.InstanceManager.store(powerManager, jmri.PowerManager.class);

        InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        InstanceManager.store(getProgrammerManager(), jmri.AddressedProgrammerManager.class);

        turnoutManager = new jmri.jmrix.tams.TamsTurnoutManager(this);
        jmri.InstanceManager.setTurnoutManager(turnoutManager);

        throttleManager = new jmri.jmrix.tams.TamsThrottleManager(this);
        jmri.InstanceManager.setThrottleManager(throttleManager);

        sensorManager = new jmri.jmrix.tams.TamsSensorManager(this);
        jmri.InstanceManager.setSensorManager(sensorManager);

    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.tams.TamsActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    /**
     * Provides access to the Programmer for this particular connection. NOTE:
     * Programmer defaults to null
     */
    public TamsProgrammerManager getProgrammerManager() {
        if (programmerManager == null) {
            programmerManager = new TamsProgrammerManager(new TamsProgrammer(getTrafficController()), this);
        }
        return programmerManager;
    }

    public void setProgrammerManager(TamsProgrammerManager p) {
        programmerManager = p;
    }

    private TamsProgrammerManager programmerManager = null;

    private TamsSensorManager sensorManager;
    private TamsTurnoutManager turnoutManager;
    private TamsThrottleManager throttleManager;
    private TamsPowerManager powerManager;

    public TamsTurnoutManager getTurnoutManager() {
        return turnoutManager;
    }

    public TamsSensorManager getSensorManager() {
        return sensorManager;
    }

    public TamsThrottleManager getThrottleManager() {
        return throttleManager;
    }

    public TamsPowerManager getPowerManager() {
        return powerManager;
    }

    /**
     * Tells which managers this class provides.
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (type.equals(jmri.ThrottleManager.class)) {
            return true;
        }
        if (type.equals(jmri.PowerManager.class)) {
            return true;
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
        return super.provides(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (T.equals(jmri.ThrottleManager.class)) {
            return (T) getThrottleManager();
        }
        if (T.equals(jmri.PowerManager.class)) {
            return (T) getPowerManager();
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
        return super.get(T);
    }

    @Override
    public void dispose() {
        if (sensorManager != null) {
            sensorManager.dispose();
            sensorManager = null;
        }
        if (turnoutManager != null) {
            turnoutManager.dispose();
            turnoutManager = null;
        }

        if (powerManager != null) {
            InstanceManager.deregister(powerManager, jmri.jmrix.tams.TamsPowerManager.class);
        }

        if (throttleManager != null) {
            InstanceManager.deregister(throttleManager, jmri.jmrix.tams.TamsThrottleManager.class);
        }

        et = null;
        InstanceManager.deregister(this, TamsSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }

        super.dispose();
    }
}



