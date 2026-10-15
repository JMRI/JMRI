package jmri.jmrix.dcc4pc;

import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.jmrix.SystemConnectionMemoManager;
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
    @Override
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
        return (Dcc4PcReporterManager) classObjectMap.computeIfAbsent((ReporterManager.class), (Class<?> c) ->
            new Dcc4PcReporterManager(getDcc4PcTrafficController(), this));
    }

    public Dcc4PcSensorManager getSensorManager() {
        if (getDisabled()) {
            return null;
        }
        return (Dcc4PcSensorManager) classObjectMap.computeIfAbsent(SensorManager.class,
                (Class<?> c) -> new Dcc4PcSensorManager(getDcc4PcTrafficController(), this));
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
        Dcc4PcSensorManager sm = (Dcc4PcSensorManager) classObjectMap.get(SensorManager.class);
        if (sm != null) {
            sm.dispose();
        }
        super.dispose();
    }

    /**
     * Provide Programming on Main through another connection's ops-mode
     * programmer: it sends the packets, and the readback arrives as RailCom
     * through this connection. Service mode stays with the other connection.
     *
     * @param <T> a manager for both modes
     * @param dpm the other connection's programmer manager
     */
    public <T extends AddressedProgrammerManager & GlobalProgrammerManager> void setRealProgramManager(T dpm) {
        provideProgrammer(new Dcc4PcProgrammerManager(dpm, this));
        log.info("{} reads CVs on the main through {}", getUserName(), dpm.getUserName());
    }

    private void provideProgrammer(Dcc4PcProgrammerManager pm) {
        store(pm, AddressedProgrammerManager.class);
        InstanceManager.store(pm, AddressedProgrammerManager.class);
    }

    /**
     * {@inheritDoc}
     * <p>
     * No consist manager, although this connection provides an ops-mode
     * programmer: that only forwards to another connection, which provides
     * its own consists.
     */
    @Override
    public boolean provides(Class<?> c) {
        boolean provided = super.provides(c);
        return provided && !c.equals(ConsistManager.class);
    }

    /**
     * Use another connection's ops-mode programmer for Programming on Main
     * (the connection's "Programmer" option): that connection's command
     * station sends the packets, and the RailCom reply comes back through
     * this one. With no connection named, the default Command Station's
     * connection (Preferences, Defaults) is used.
     * <p>
     * This connection offers its ops-mode programmer straight away, so it can
     * be chosen as the default whichever connection starts first; the other
     * connection is looked up each time a programmer is needed.
     *
     * @param userName the other connection's name, or empty to use the
     *                 default command station
     */
    public void useProgrammerFrom(String userName) {
        boolean useDefault = userName == null || userName.isEmpty();
        Supplier<SystemConnectionMemo> other = () -> connectionNamed(useDefault ? defaultCommandStation() : userName);
        provideProgrammer(new Dcc4PcProgrammerManager(
                () -> managerOf(other.get(), AddressedProgrammerManager.class),
                () -> managerOf(other.get(), CommandStation.class),
                this));
        log.info("{} reads CVs on the main through {}", getUserName(), useDefault ? "the default command station" : userName);
    }

    /** @return the name of the connection set as the default Command Station, or null if none */
    private static String defaultCommandStation() {
        jmri.managers.ManagerDefaultSelector defaults = InstanceManager.getNullableDefault(jmri.managers.ManagerDefaultSelector.class);
        return defaults == null ? null : defaults.getDefault(CommandStation.class);
    }

    /** @return the named connection other than this one, or null if it isn't running */
    private SystemConnectionMemo connectionNamed(String userName) {
        if (userName == null) {
            return null;
        }
        SystemConnectionMemo other = SystemConnectionMemoManager.getDefault().getSystemConnectionMemoForUserName(userName);
        return other == this ? null : other;
    }

    private static <T> T managerOf(SystemConnectionMemo memo, Class<T> type) {
        return memo == null ? null : memo.get(type);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Dcc4PcSystemConnectionMemo.class);
}
