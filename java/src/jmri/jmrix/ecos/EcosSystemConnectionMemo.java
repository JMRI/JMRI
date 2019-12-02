package jmri.jmrix.ecos;

import java.util.Comparator;
import java.util.ResourceBundle;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.util.NamedBeanComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class EcosSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public EcosSystemConnectionMemo(EcosTrafficController et) {
        super("U", "ECoS");
        this.et = et;
        et.setAdapterMemo(this);
        register();
        InstanceManager.store(this, EcosSystemConnectionMemo.class); // also register as specific type
        InstanceManager.store(cf = new jmri.jmrix.ecos.swing.EcosComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
        prefManager = new jmri.jmrix.ecos.EcosPreferences(this);
    }

    public EcosSystemConnectionMemo() {
        super("U", "ECoS");
        register(); // registers general type
        InstanceManager.store(this, EcosSystemConnectionMemo.class); // also register as specific type
        //Needs to be implemented
        InstanceManager.store(cf = new jmri.jmrix.ecos.swing.EcosComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
        //jmri.InstanceManager.store(new jmri.jmrix.ecos.EcosPreferences(thie), jmri.jmrix.ecos.EcosPreferences.class);
        prefManager = new jmri.jmrix.ecos.EcosPreferences(this);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     */
    public EcosTrafficController getTrafficController() {
        return et;
    }

    public void setEcosTrafficController(EcosTrafficController et) {
        this.et = et;
        et.setAdapterMemo(this);
    }
    private EcosTrafficController et;

    /**
     * This puts the common manager config in one place.
     */
    public void configureManagers() {

        powerManager = new jmri.jmrix.ecos.EcosPowerManager(getTrafficController());
        jmri.InstanceManager.store(powerManager, jmri.PowerManager.class);

        turnoutManager = new jmri.jmrix.ecos.EcosTurnoutManager(this);
        jmri.InstanceManager.setTurnoutManager(turnoutManager);

        locoManager = new jmri.jmrix.ecos.EcosLocoAddressManager(this);

        throttleManager = new jmri.jmrix.ecos.EcosDccThrottleManager(this);
        jmri.InstanceManager.setThrottleManager(throttleManager);

        reporterManager = new jmri.jmrix.ecos.EcosReporterManager(this);
        jmri.InstanceManager.setReporterManager(reporterManager);

        sensorManager = new jmri.jmrix.ecos.EcosSensorManager(this);
        jmri.InstanceManager.setSensorManager(sensorManager);

        jmri.InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        jmri.InstanceManager.store(getProgrammerManager(), jmri.AddressedProgrammerManager.class);

    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.ecos.EcosActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    private EcosSensorManager sensorManager;
    private EcosTurnoutManager turnoutManager;
    protected EcosLocoAddressManager locoManager;
    private EcosPreferences prefManager;
    private EcosDccThrottleManager throttleManager;
    private EcosPowerManager powerManager;
    private EcosReporterManager reporterManager;
    private EcosProgrammerManager programmerManager;

    public EcosLocoAddressManager getLocoAddressManager() {
        return locoManager;
    }

    public EcosTurnoutManager getTurnoutManager() {
        return turnoutManager;
    }

    public EcosSensorManager getSensorManager() {
        return sensorManager;
    }

    public EcosPreferences getPreferenceManager() {
        return prefManager;
    }

    public EcosDccThrottleManager getThrottleManager() {
        return throttleManager;
    }

    public EcosPowerManager getPowerManager() {
        return powerManager;
    }

    public EcosReporterManager getReporterManager() {
        return reporterManager;
    }

    public EcosProgrammerManager getProgrammerManager() {
        if (programmerManager == null) {
            programmerManager = new EcosProgrammerManager(new EcosProgrammer(getTrafficController()), this);
        }
        return programmerManager;
    }

    /**
     * Tell which managers this class provides.
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
        if (type.equals(jmri.ReporterManager.class)) {
            return true;
        }
        if (type.equals(jmri.GlobalProgrammerManager.class)) {
            return true;
        }
        if (type.equals(jmri.AddressedProgrammerManager.class)) {
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
        if (T.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        if (T.equals(jmri.ReporterManager.class)) {
            return (T) getReporterManager();
        }
        if (T.equals(jmri.GlobalProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.AddressedProgrammerManager.class)) {
            return (T) getProgrammerManager();
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
        if (reporterManager != null) {
            reporterManager.dispose();
            reporterManager = null;
        }
        if (locoManager != null) {
            locoManager.terminateThreads();
            locoManager = null;
        }
        if (programmerManager != null) {
            InstanceManager.deregister(programmerManager, jmri.jmrix.ecos.EcosProgrammerManager.class);
        }

        if (powerManager != null) {
            InstanceManager.deregister(powerManager, jmri.jmrix.ecos.EcosPowerManager.class);
        }

        if (throttleManager != null) {
            InstanceManager.deregister(throttleManager, jmri.jmrix.ecos.EcosDccThrottleManager.class);
        }

        et = null;
        InstanceManager.deregister(this, EcosSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }

        super.dispose();
    }

    // private final static Logger log = LoggerFactory.getLogger(EcosSystemConnectionMemo.class);

}
