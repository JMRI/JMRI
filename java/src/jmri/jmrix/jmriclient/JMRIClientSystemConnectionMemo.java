//JMRIClientSystemConnectionMemo.java
package jmri.jmrix.jmriclient;

import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.PowerManager;
import jmri.ReporterManager;
import jmri.SensorManager;
import jmri.TurnoutManager;

/**
 * Lightweight class to denote that a system is active and provide general
 * information
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision$
 */
public class JMRIClientSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public JMRIClientSystemConnectionMemo(JMRIClientTrafficController jt) {
        super("J", "JMRI Client");
        this.jt = jt;
        register(); // registers general type
        InstanceManager.store(this, JMRIClientSystemConnectionMemo.class); // also register as specific type

        // create and register the JMRIClientComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.jmriclient.swing.JMRIClientComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

    }

    public JMRIClientSystemConnectionMemo() {
        super("J", "JMRIClient");
        this.jt = new JMRIClientTrafficController();
        register(); // registers general type
        InstanceManager.store(this, JMRIClientSystemConnectionMemo.class); // also register as specific type

        // create and register the JMRIClientComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.jmriclient.swing.JMRIClientComponentFactory(this), jmri.jmrix.swing.ComponentFactory.class);

    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     */
    public JMRIClientTrafficController getJMRIClientTrafficController() {
        return jt;
    }
    private JMRIClientTrafficController jt;

    public void setJMRIClientTrafficController(JMRIClientTrafficController jt) {
        this.jt = jt;
    }

    public void dispose() {
        jt = null;
        InstanceManager.deregister(this, JMRIClientSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    /**
     * Configure the common managers for Internal connections. This puts the
     * common manager config in one place.
     */
    public void configureManagers() {

        setPowerManager(new jmri.jmrix.jmriclient.JMRIClientPowerManager(this));
        jmri.InstanceManager.store(getPowerManager(), jmri.PowerManager.class);
        setTurnoutManager(new jmri.jmrix.jmriclient.JMRIClientTurnoutManager(this));
        jmri.InstanceManager.setTurnoutManager(getTurnoutManager());
        setSensorManager(new jmri.jmrix.jmriclient.JMRIClientSensorManager(this));
        jmri.InstanceManager.setSensorManager(getSensorManager());
        setLightManager(new jmri.jmrix.jmriclient.JMRIClientLightManager(this));
        jmri.InstanceManager.setLightManager(getLightManager());
        setReporterManager(new jmri.jmrix.jmriclient.JMRIClientReporterManager(this));
        jmri.InstanceManager.setReporterManager(getReporterManager());
    }

    /**
     * Request all status from the configured managers.
     */
    public void requestAllStatus() {

        getTurnoutManager().getSystemNameList().forEach((t) -> {
           ((JMRIClientTurnout)(getTurnoutManager().getTurnout(t))).requestUpdateFromLayout();
        }); 
        getSensorManager().getSystemNameList().forEach((s) -> {
           ((JMRIClientSensor)(getSensorManager().getSensor(s))).requestUpdateFromLayout();
        }); 
        getLightManager().getSystemNameList().forEach((l) -> {
           ((JMRIClientLight)(getLightManager().getLight(l))).requestUpdateFromLayout();
        }); 
        getReporterManager().getSystemNameList().forEach((r) -> {
           ((JMRIClientReporter)(getReporterManager().getReporter(r))).requestUpdateFromLayout();
        }); 
    }

    /*
     * Provides access to the Power Manager for this particular connection.
     */
    public PowerManager getPowerManager() {
        return powerManager;
    }

    public void setPowerManager(PowerManager p) {
        powerManager = p;
    }

    private PowerManager powerManager;

    /*
     * Provides access to the Sensor Manager for this particular connection.
     */
    public SensorManager getSensorManager() {
        return sensorManager;

    }

    public void setSensorManager(SensorManager s) {
        sensorManager = s;
    }

    private SensorManager sensorManager = null;

    /*
     * Provides access to the Turnout Manager for this particular connection.
     * NOTE: Turnout manager defaults to NULL
     */
    public TurnoutManager getTurnoutManager() {
        return turnoutManager;

    }

    public void setTurnoutManager(TurnoutManager t) {
        turnoutManager = t;
    }

    private TurnoutManager turnoutManager = null;

    /*
     * Provides access to the Light Manager for this particular connection.
     * NOTE: Light manager defaults to NULL
     */
    public LightManager getLightManager() {
        return lightManager;
    }

    public void setLightManager(LightManager t) {
        lightManager = t;
    }
    private LightManager lightManager = null;

    /*
     * Provides access to the Reporter Manager for this particular connection.
     * NOTE: Reporter manager defaults to NULL
     */
    public ReporterManager getReporterManager() {
        return reporterManager;
    }

    public void setReporterManager(ReporterManager t) {
        reporterManager = t;
    }

    private ReporterManager reporterManager = null;

    public void setTransmitPrefix(String tPrefix) {
        transmitPrefix = tPrefix.toUpperCase();
    }

    public String getTransmitPrefix() {
        if (transmitPrefix == null) {
            return getSystemPrefix();
        }
        return transmitPrefix;
    }

    private String transmitPrefix = null;

    protected ResourceBundle getActionModelResourceBundle() {
        //No actions that can be loaded at startup
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
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
        if (T.equals(jmri.LightManager.class)) {
            return (T) getLightManager();
        }
        if (T.equals(jmri.ReporterManager.class)) {
            return (T) getReporterManager();
        }
        return null; // nothing, by default
    }

    /**
     * Tells which managers this provides by class
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (type.equals(jmri.PowerManager.class)) {
            return (null != powerManager);
        }
        if (type.equals(jmri.SensorManager.class)) {
            return (null != sensorManager);
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return (null != turnoutManager);
        }
        if (type.equals(jmri.LightManager.class)) {
            return (null != lightManager);
        }
        if (type.equals(jmri.ReporterManager.class)) {
            return (null != reporterManager);
        }
        return false; // nothing, by default
    }

}
/* @(#)JMRIClientSystemConnectionMemo.java */
