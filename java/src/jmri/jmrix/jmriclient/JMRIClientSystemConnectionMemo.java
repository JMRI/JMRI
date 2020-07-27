package jmri.jmrix.jmriclient;

import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
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
public class JMRIClientSystemConnectionMemo extends jmri.jmrix.DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    public JMRIClientSystemConnectionMemo(JMRIClientTrafficController jt) {
        super("J", "JMRI Client");
        this.jt = jt;
        InstanceManager.store(this, JMRIClientSystemConnectionMemo.class);

        // create and register the JMRIClientComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.jmriclient.swing.JMRIClientComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    public JMRIClientSystemConnectionMemo() {
        super("J", "JMRIClient");
        this.jt = new JMRIClientTrafficController();
        InstanceManager.store(this, JMRIClientSystemConnectionMemo.class);

        // create and register the JMRIClientComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.jmriclient.swing.JMRIClientComponentFactory(this), jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     * @return traffic controller.
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

        register();
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
        return get(PowerManager.class);
    }

    public void setPowerManager(PowerManager p) {
        store(p,PowerManager.class);
    }

    /*
     * Provides access to the SensorManager for this particular connection.
     */
    public SensorManager getSensorManager() {
        return get(SensorManager.class);

    }

    public void setSensorManager(SensorManager s) {
        store(s,SensorManager.class);
    }

    /*
     * Provides access to the TurnoutManager for this particular connection.
     * NOTE: TurnoutManager defaults to NULL
     */
    public TurnoutManager getTurnoutManager() {
        return get(TurnoutManager.class);

    }

    public void setTurnoutManager(TurnoutManager t) {
         store(t,TurnoutManager.class);
    }

    /*
     * Provides access to the LightManager for this particular connection.
     * NOTE: LightManager defaults to NULL
     */
    public LightManager getLightManager() {
        return get(LightManager.class);
    }

    public void setLightManager(LightManager t) {
        store(t,LightManager.class);
    }

    /*
     * Provides access to the Reporter Manager for this particular connection.
     * NOTE: Reporter manager defaults to NULL
     */
    public ReporterManager getReporterManager() {
        return get(ReporterManager.class);
    }

    public void setReporterManager(ReporterManager t) {
        store(t,ReporterManager.class);
    }


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

}
