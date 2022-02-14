package jmri.jmrix.ecos;

import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.*;
import jmri.managers.DefaultProgrammerManager;
import jmri.util.NamedBeanComparator;
import org.python.antlr.op.Pow;


/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class EcosSystemConnectionMemo extends jmri.jmrix.DefaultSystemConnectionMemo {

    public EcosSystemConnectionMemo(EcosTrafficController et) {
        super("U", "ECoS");
        this.et = et;
        et.setAdapterMemo(this);
        InstanceManager.store(this, EcosSystemConnectionMemo.class); // also register as specific type
        InstanceManager.store(cf = new jmri.jmrix.ecos.swing.EcosComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
        store(new EcosPreferences(this), EcosPreferences.class);
    }

    public EcosSystemConnectionMemo() {
        super("U", "ECoS");
        InstanceManager.store(this, EcosSystemConnectionMemo.class); // also register as specific type
        //Needs to be implemented
        InstanceManager.store(cf = new jmri.jmrix.ecos.swing.EcosComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
        store(new EcosPreferences(this), EcosPreferences.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     * @return Ecos traffic controller.
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

        PowerManager powerManager = new EcosPowerManager(getTrafficController());
        jmri.InstanceManager.store(powerManager, PowerManager.class);
        store(powerManager, PowerManager.class);

        TurnoutManager turnoutManager = new EcosTurnoutManager(this);
        jmri.InstanceManager.setTurnoutManager(turnoutManager);
        store(turnoutManager,TurnoutManager.class);

        EcosLocoAddressManager locoManager = new EcosLocoAddressManager(this);
        store(locoManager,EcosLocoAddressManager.class);

        ThrottleManager throttleManager = new EcosDccThrottleManager(this);
        jmri.InstanceManager.setThrottleManager(throttleManager);
        store(throttleManager,ThrottleManager.class);

        ReporterManager reporterManager = new EcosReporterManager(this);
        jmri.InstanceManager.setReporterManager(reporterManager);
        store(reporterManager,ReporterManager.class);

        SensorManager sensorManager = new jmri.jmrix.ecos.EcosSensorManager(this);
        jmri.InstanceManager.setSensorManager(sensorManager);
        store(sensorManager, SensorManager.class);

        jmri.InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        store(getProgrammerManager(), GlobalProgrammerManager.class);

        jmri.InstanceManager.store(getProgrammerManager(), jmri.AddressedProgrammerManager.class);
        store(getProgrammerManager(), jmri.AddressedProgrammerManager.class);

        register(); // registers general type
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.ecos.EcosActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    public EcosLocoAddressManager getLocoAddressManager() {
        return get(EcosLocoAddressManager.class);
    }

    public EcosTurnoutManager getTurnoutManager() {
        return get(TurnoutManager.class);
    }

    public EcosSensorManager getSensorManager() {
        return get(SensorManager.class);
    }

    public EcosPreferences getPreferenceManager() {
        return get(EcosPreferences.class);
    }

    public EcosDccThrottleManager getThrottleManager() {
        return get(ThrottleManager.class);
    }

    public EcosPowerManager getPowerManager() {
        return get(PowerManager.class);
    }

    public EcosReporterManager getReporterManager() {
        return get(ReporterManager.class);
    }

    public EcosProgrammerManager getProgrammerManager() {
        return (EcosProgrammerManager) classObjectMap.computeIfAbsent(EcosProgrammerManager.class, (Class<?> c) ->
            new EcosProgrammerManager(new EcosProgrammer(getTrafficController()), this));
    }

    @Override
    public void dispose() {
        EcosLocoAddressManager locoManager = get(EcosLocoAddressManager.class);
        if (locoManager != null) {
            locoManager.dispose();
            deregister(locoManager, EcosLocoAddressManager.class);
        }
        et = null;
        InstanceManager.deregister(this, EcosSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        EcosPreferences prefManager = get(EcosPreferences.class);
        if (prefManager != null) {
            InstanceManager.getDefault(ShutDownManager.class).deregister(prefManager.ecosPreferencesShutDownTask);
            deregister(prefManager, EcosPreferences.class);
        }
        super.dispose();
    }

    // private final static Logger log = LoggerFactory.getLogger(EcosSystemConnectionMemo.class);

}
