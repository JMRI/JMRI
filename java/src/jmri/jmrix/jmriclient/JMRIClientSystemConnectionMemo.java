package jmri.jmrix.jmriclient;

import java.util.Comparator;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.NamedBean;
import jmri.PowerManager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.NamedBeanComparator;

/**
 * Lightweight class to denote that a system is active and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Paul Bender Copyright (C) 2010
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

    @Override
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

        getTurnoutManager().getNamedBeanSet().forEach((turn) -> {
            ((JMRIClientTurnout)(turn)).requestUpdateFromLayout();
        }); 
        getSensorManager().getNamedBeanSet().forEach((sen) -> {
            ((JMRIClientSensor)(sen)).requestUpdateFromLayout();
        }); 
        getLightManager().getNamedBeanSet().forEach((light) -> {
            ((JMRIClientLight)light).requestUpdateFromLayout();
        }); 
        getReporterManager().getNamedBeanSet().forEach((rep) -> {
            ((JMRIClientReporter)(rep)).requestUpdateFromLayout();
        }); 
    }

    /*
     * Provides access to the PowerManager for this particular connection.
     */
    public PowerManager getPowerManager() {
        return powerManager;
    }

    public void setPowerManager(PowerManager p) {
        powerManager = p;
    }

    private PowerManager powerManager;

    /*
     * Provides access to the SensorManager for this particular connection.
     */
    public SensorManager getSensorManager() {
        return sensorManager;

    }

    public void setSensorManager(SensorManager s) {
        sensorManager = s;
    }

    private SensorManager sensorManager = null;

    /*
     * Provides access to the TurnoutManager for this particular connection.
     * NOTE: TurnoutManager defaults to NULL
     */
    public TurnoutManager getTurnoutManager() {
        return turnoutManager;

    }

    public void setTurnoutManager(TurnoutManager t) {
        turnoutManager = t;
    }

    private TurnoutManager turnoutManager = null;

    /*
     * Provides access to the LightManager for this particular connection.
     * NOTE: LightManager defaults to NULL
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
        transmitPrefix = tPrefix;
    }

    public String getTransmitPrefix() {
        if (transmitPrefix == null) {
            return getSystemPrefix();
        }
        return transmitPrefix;
    }

    private String transmitPrefix = null;

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        //No actions that can be loaded at startup
        return null;
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
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
        return super.get(T);
    }

    /**
     * Tells which managers this class provides.
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
        return super.provides(type);
    }

}
