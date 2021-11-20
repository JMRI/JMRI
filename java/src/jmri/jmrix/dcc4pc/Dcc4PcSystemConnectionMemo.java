package jmri.jmrix.dcc4pc;

import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.managers.DefaultProgrammerManager;
import jmri.util.NamedBeanComparator;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class Dcc4PcSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    public Dcc4PcSystemConnectionMemo(Dcc4PcTrafficController tc) {
        super("D", "Dcc4Pc");
        this.tc = tc;
        tc.setAdapterMemo(this);
    }

    public Dcc4PcSystemConnectionMemo() {
        super("D", "Dcc4Pc");
        InstanceManager.store(this, Dcc4PcSystemConnectionMemo.class);
        //Needs to be implemented
        InstanceManager.store(cf = new jmri.jmrix.dcc4pc.swing.Dcc4PcComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     * @return traffic controller.
     */
    public Dcc4PcTrafficController getDcc4PcTrafficController() {
        return tc;
    }

    public void setDcc4PcTrafficController(Dcc4PcTrafficController tc) {
        this.tc = tc;
    }
    private Dcc4PcTrafficController tc;

    /**
     * Configure the common managers for Dcc4Pc connections. This puts the
     * common manager config in one place. This method is static so that it can
     * be referenced from classes that don't inherit, including
     * hexfile.HexFileFrame and locormi.LnMessageClient
     */
    public void configureManagers() {

        InstanceManager.setReporterManager(
                getReporterManager());

        InstanceManager.setSensorManager(
                getSensorManager());

        register();
    }

    public Dcc4PcReporterManager getReporterManager() {
        if (getDisabled()) {
            return null;
        }
        return (Dcc4PcReporterManager) classObjectMap.computeIfAbsent((ReporterManager.class), (Class c) ->
            new Dcc4PcReporterManager(getDcc4PcTrafficController(), this));
    }

    public Dcc4PcSensorManager getSensorManager() {
        if (getDisabled()) {
            return null;
        }
        return (Dcc4PcSensorManager) classObjectMap.computeIfAbsent(SensorManager.class,
                (Class c) -> new Dcc4PcSensorManager(getDcc4PcTrafficController(), this));
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.dcc4pc.Dcc4PcActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        tc = null;
        InstanceManager.deregister(this, Dcc4PcSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    public <T extends AddressedProgrammerManager & GlobalProgrammerManager> void setRealProgramManager(T dpm) {
        store(dpm,GlobalProgrammerManager.class);
        store(dpm, AddressedProgrammerManager.class);
    }

    private String progManager;

    public void setDefaultProgrammer(String prog) {
        progManager = prog;
    }
}
