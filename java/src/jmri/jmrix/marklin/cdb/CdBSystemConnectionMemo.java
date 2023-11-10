package jmri.jmrix.marklin.cdb;

import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.*;
import jmri.util.NamedBeanComparator;

import jmri.jmrix.marklin.MarklinTrafficController;
//import jmri.jmrix.marklin.MarklinProgrammer;
//import jmri.jmrix.marklin.MarklinProgrammerManager;
import jmri.jmrix.marklin.MarklinSensorManager;
import jmri.jmrix.marklin.MarklinThrottleManager;
import jmri.jmrix.marklin.MarklinTurnoutManager;
import jmri.jmrix.marklin.MarklinPowerManager;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;

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
public class CdBSystemConnectionMemo extends MarklinSystemConnectionMemo {

    public CdBSystemConnectionMemo(MarklinTrafficController et) {
        super("M", "CdB");
        this.et = et;
        et.setAdapterMemo(this);
        InstanceManager.store(this, CdBSystemConnectionMemo.class);
        InstanceManager.store(cf = new jmri.jmrix.marklin.swing.MarklinComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    public CdBSystemConnectionMemo() {
        super("M", "CdB");
        InstanceManager.store(this, CdBSystemConnectionMemo.class);
        InstanceManager.store(cf = new jmri.jmrix.marklin.swing.MarklinComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     * @return Marklin Traffic Controller.
     */
    @Override
    public MarklinTrafficController getTrafficController() {
        return et;
    }

    @Override
    public void setMarklinTrafficController(MarklinTrafficController et) {
        this.et = et;
        et.setAdapterMemo(this);
    }
    private MarklinTrafficController et;

    /**
     * This puts the common manager config in one place.
     */
    @Override
    public void configureManagers() {

        MarklinPowerManager powerManager = new MarklinPowerManager(getTrafficController());
        InstanceManager.store(powerManager, jmri.PowerManager.class);
        store(powerManager, jmri.PowerManager.class);

/*        MarklinProgrammerManager programmerManager = getProgrammerManager();
        InstanceManager.store(programmerManager, GlobalProgrammerManager.class);
        InstanceManager.store(programmerManager, AddressedProgrammerManager.class);*/

        TurnoutManager turnoutManager = new MarklinTurnoutManager(this);
        InstanceManager.setTurnoutManager(turnoutManager);
        store(turnoutManager,TurnoutManager.class);

        ThrottleManager throttleManager = new MarklinThrottleManager(this);
        InstanceManager.setThrottleManager(throttleManager);
        store(throttleManager,ThrottleManager.class);

        SensorManager sensorManager = new MarklinSensorManager(this);
        InstanceManager.setSensorManager(sensorManager);
        store(throttleManager,ThrottleManager.class);

        register();
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.marklin.MarklinActionListBundle");
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
    /*  public MarklinProgrammerManager getProgrammerManager() {
        return (MarklinProgrammerManager) classObjectMap.computeIfAbsent(MarklinProgrammerManager.class, (Class<?> c) -> new MarklinProgrammerManager(new MarklinProgrammer(getTrafficController()),this));
    }

    public void setProgrammerManager(MarklinProgrammerManager p) {
        store(p,TamsProgrammerManager.class);
    }*/

    @Override
    public MarklinTurnoutManager getTurnoutManager() {
        return (MarklinTurnoutManager) classObjectMap.computeIfAbsent(TurnoutManager.class, (Class<?> c) -> { return new MarklinTurnoutManager(this); });
    }

    @Override
    public MarklinSensorManager getSensorManager() {
        return (MarklinSensorManager) classObjectMap.computeIfAbsent(SensorManager.class, (Class<?> c) -> { return new MarklinSensorManager(this); });
    }

    @Override
    public MarklinThrottleManager getThrottleManager() {
        return (MarklinThrottleManager) classObjectMap.computeIfAbsent(ThrottleManager.class, (Class<?> c) -> { return new MarklinThrottleManager(this); });
    }

    @Override
    public MarklinPowerManager getPowerManager() {
        return (MarklinPowerManager) classObjectMap.computeIfAbsent(PowerManager.class, (Class<?> c) -> { return new MarklinPowerManager(getTrafficController()); });
    }

    @Override
    public void dispose() {
        et = null;
        InstanceManager.deregister(this, CdBSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }

        super.dispose();
    }
}



