package jmri.jmrix.tams;

import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.DefaultSystemConnectionMemo;
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
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class TamsSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    public TamsSystemConnectionMemo(TamsTrafficController et) {
        super("T", "Tams");
        this.et = et;
        et.setAdapterMemo(this);
        InstanceManager.store(this, TamsSystemConnectionMemo.class);
        InstanceManager.store(cf = new jmri.jmrix.tams.swing.TamsComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    public TamsSystemConnectionMemo() {
        super("T", "Tams");
        InstanceManager.store(this, TamsSystemConnectionMemo.class);
        InstanceManager.store(cf = new jmri.jmrix.tams.swing.TamsComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     * @return Tams Traffic Controller.
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

        TamsPowerManager powerManager = new TamsPowerManager(getTrafficController());
        InstanceManager.store(powerManager, jmri.PowerManager.class);
        store(powerManager, jmri.PowerManager.class);

        TamsProgrammerManager programmerManager = getProgrammerManager();
        InstanceManager.store(programmerManager, GlobalProgrammerManager.class);
        InstanceManager.store(programmerManager, AddressedProgrammerManager.class);

        TurnoutManager turnoutManager = new TamsTurnoutManager(this);
        InstanceManager.setTurnoutManager(turnoutManager);
        store(turnoutManager,TurnoutManager.class);

        ThrottleManager throttleManager = new TamsThrottleManager(this);
        InstanceManager.setThrottleManager(throttleManager);
        store(throttleManager,ThrottleManager.class);

        SensorManager sensorManager = new TamsSensorManager(this);
        InstanceManager.setSensorManager(sensorManager);
        store(throttleManager,ThrottleManager.class);

        register();
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
     * Provides access to the Programmer for this particular connection.
     * NOTE: Programmer defaults to null
     * @return programmer manager.
     */
    public TamsProgrammerManager getProgrammerManager() {
        return (TamsProgrammerManager) classObjectMap.computeIfAbsent(TamsProgrammerManager.class, (Class c) -> new TamsProgrammerManager(new TamsProgrammer(getTrafficController()),this));
    }

    public void setProgrammerManager(TamsProgrammerManager p) {
        store(p,TamsProgrammerManager.class);
    }

    public TamsTurnoutManager getTurnoutManager() {
        return (TamsTurnoutManager) classObjectMap.computeIfAbsent(TurnoutManager.class, (Class c) -> { return new TamsTurnoutManager(this); });
    }

    public TamsSensorManager getSensorManager() {
        return (TamsSensorManager) classObjectMap.computeIfAbsent(SensorManager.class, (Class c) -> { return new TamsSensorManager(this); });
    }

    public TamsThrottleManager getThrottleManager() {
        return (TamsThrottleManager) classObjectMap.computeIfAbsent(ThrottleManager.class, (Class c) -> { return new TamsThrottleManager(this); });
    }

    public TamsPowerManager getPowerManager() {
        return (TamsPowerManager) classObjectMap.computeIfAbsent(PowerManager.class, (Class c) -> { return new TamsPowerManager(getTrafficController()); });
    }

    @Override
    public void dispose() {
        et = null;
        InstanceManager.deregister(this, TamsSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }

        super.dispose();
    }
}



