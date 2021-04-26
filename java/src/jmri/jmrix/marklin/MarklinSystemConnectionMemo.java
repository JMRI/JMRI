package jmri.jmrix.marklin;

import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.util.NamedBeanComparator;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class MarklinSystemConnectionMemo extends jmri.jmrix.DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    public MarklinSystemConnectionMemo(MarklinTrafficController et) {
        super("M", "Marklin-CS2");
        this.et = et;
        et.setAdapterMemo(this);
        InstanceManager.store(this, MarklinSystemConnectionMemo.class); // also register as specific type
        InstanceManager.store(cf = new jmri.jmrix.marklin.swing.MarklinComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    public MarklinSystemConnectionMemo() {
        super("M", "Marklin-CS2");
        register(); // registers general type
        InstanceManager.store(this, MarklinSystemConnectionMemo.class); // also register as specific type
        //Needs to be implemented
        InstanceManager.store(cf = new jmri.jmrix.marklin.swing.MarklinComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     * @return the traffic controller.
     */
    public MarklinTrafficController getTrafficController() {
        return et;
    }

    public void setMarklinTrafficController(MarklinTrafficController et) {
        this.et = et;
        et.setAdapterMemo(this);
    }
    private MarklinTrafficController et;

    /**
     * This puts the common manager config in one place.
     */
    public void configureManagers() {

        PowerManager powerManager = new MarklinPowerManager(getTrafficController());
        store(powerManager, PowerManager.class);
        jmri.InstanceManager.store(powerManager, PowerManager.class);

        TurnoutManager turnoutManager = new MarklinTurnoutManager(this);
        store(turnoutManager, TurnoutManager.class);
        jmri.InstanceManager.setTurnoutManager(turnoutManager);

        ThrottleManager throttleManager = new MarklinThrottleManager(this);
        store(throttleManager, ThrottleManager.class);
        jmri.InstanceManager.setThrottleManager(throttleManager);

        SensorManager sensorManager = new jmri.jmrix.marklin.MarklinSensorManager(this);
        store(sensorManager, SensorManager.class);
        jmri.InstanceManager.setSensorManager(sensorManager);

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

    public MarklinTurnoutManager getTurnoutManager() {
        return get(TurnoutManager.class);
    }

    public MarklinSensorManager getSensorManager() {
        return get(SensorManager.class);
    }

    public MarklinThrottleManager getThrottleManager() {
        return get(ThrottleManager.class);
    }

    public MarklinPowerManager getPowerManager() {
        return get(PowerManager.class);
    }

    @Override
    public void dispose() {
        et = null;
        InstanceManager.deregister(this, MarklinSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }

        super.dispose();
    }

}
