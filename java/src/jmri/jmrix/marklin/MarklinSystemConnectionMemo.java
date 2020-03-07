package jmri.jmrix.marklin;

import java.util.Comparator;
import java.util.ResourceBundle;
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
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class MarklinSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public MarklinSystemConnectionMemo(MarklinTrafficController et) {
        super("M", "Marklin-CS2");
        this.et = et;
        et.setAdapterMemo(this);
        register();
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

        powerManager = new jmri.jmrix.marklin.MarklinPowerManager(getTrafficController());
        jmri.InstanceManager.store(powerManager, jmri.PowerManager.class);

        turnoutManager = new jmri.jmrix.marklin.MarklinTurnoutManager(this);
        jmri.InstanceManager.setTurnoutManager(turnoutManager);

        /*locoManager = new jmri.jmrix.marklin.MarklinLocoAddressManager(this);*/
        throttleManager = new jmri.jmrix.marklin.MarklinThrottleManager(this);
        jmri.InstanceManager.setThrottleManager(throttleManager);

        sensorManager = new jmri.jmrix.marklin.MarklinSensorManager(this);
        jmri.InstanceManager.setSensorManager(sensorManager);

        /*reporterManager = new jmri.jmrix.marklin.MarklinReporterManager(this);
         jmri.InstanceManager.setReporterManager(reporterManager);*/
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.marklin.MarklinActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    private MarklinSensorManager sensorManager;
    private MarklinTurnoutManager turnoutManager;
    /*private MarklinLocoAddressManager locoManager;
     private MarklinPreferences prefManager;*/
    private MarklinThrottleManager throttleManager;
    private MarklinPowerManager powerManager;
    //private MarklinReporterManager reporterManager;

    /*public MarklinLocoAddressManager getLocoAddressManager() { return locoManager; }*/
    public MarklinTurnoutManager getTurnoutManager() {
        return turnoutManager;
    }

    public MarklinSensorManager getSensorManager() {
        return sensorManager;
    }
    /*public MarklinPreferences getPreferenceManager() { return prefManager; }*/

    public MarklinThrottleManager getThrottleManager() {
        return throttleManager;
    }

    public MarklinPowerManager getPowerManager() {
        return powerManager;
    }
    //public MarklinReporterManager getReporterManager() { return reporterManager; }

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
        if (type.equals(jmri.SensorManager.class)) {
            return true;
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        }
        /*if (type.equals(jmri.ReporterManager.class))
         return true;*/
        return false; // nothing, by default
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
        if (T.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        /*if (T.equals(jmri.ReporterManager.class))
         return (T)getReporterManager();*/
        return null; // nothing, by default
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
        /*if(reporterManager!=null){
         reporterManager.dispose();
         reporterManager=null;
         }*/

        if (powerManager != null) {
            InstanceManager.deregister(powerManager, jmri.jmrix.marklin.MarklinPowerManager.class);
        }

        if (throttleManager != null) {
            InstanceManager.deregister(throttleManager, jmri.jmrix.marklin.MarklinThrottleManager.class);
        }

        et = null;
        InstanceManager.deregister(this, MarklinSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }

        super.dispose();
    }

}
