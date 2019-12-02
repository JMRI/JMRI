package jmri.jmrix.easydcc;

import java.util.Comparator;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;

import jmri.ConsistManager;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.ThrottleManager;
import jmri.util.NamedBeanComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 * <p>
 * Migrated for multiple connections, multi char connection prefix and Simulator.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Kevin Dickerson
 * @author Egbert Broerse Copyright (C) 2017
 */
public class EasyDccSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public EasyDccSystemConnectionMemo() {
        this("E", EasyDccConnectionTypeList.EASYDCC);
    }

    /**
     * Ctor
     *
     * @param et the associated TrafficController
     */
    public EasyDccSystemConnectionMemo(EasyDccTrafficController et) {
        super("E", EasyDccConnectionTypeList.EASYDCC);
        this.et = et;
        register(); // registers general type
        log.debug("EasyDCC SystemConnectionMemo with TC");
        InstanceManager.store(this, EasyDccSystemConnectionMemo.class); // also register as specific type
        // create and register the ComponentFactory for the GUI (menu)
        InstanceManager.store(cf = new jmri.jmrix.easydcc.swing.EasyDccComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created EasyDccSystemConnectionMemo");
    }

    public EasyDccSystemConnectionMemo(@Nonnull String prefix, @Nonnull String name) {
        super(prefix, name);
        register(); // registers general type
        log.debug("EasyDCC SystemConnectionMemo prefix={}", prefix);
        InstanceManager.store(this, EasyDccSystemConnectionMemo.class); // also register as specific type
        // create and register the ComponentFactory for the GUI (menu)
        InstanceManager.store(cf = new jmri.jmrix.easydcc.swing.EasyDccComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created EasyDccSystemConnectionMemo");
    }

    jmri.jmrix.swing.ComponentFactory cf = null;
    private EasyDccTrafficController et;

    /**
     * Provide access to the TrafficController for this particular connection.
     */
    public EasyDccTrafficController getTrafficController() {
        if (et == null) {
            setEasyDccTrafficController(new EasyDccTrafficController(this));
            log.debug("Auto create of EasyDccTrafficController for initial configuration");
        }
        return et;
    }

    public void setEasyDccTrafficController(EasyDccTrafficController et) {
        this.et = et;
        // in addition to setting the TrafficController in this object,
        // set the systemConnectionMemo in the traffic controller
        et.setSystemConnectionMemo(this);
    }

    /**
     * Configure the common managers for EasyDCC connections. This puts the
     * common manager config in one place. This method is static so that it can
     * be referenced from classes that don't inherit.
     */
    public void configureManagers() {

        InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        InstanceManager.store(getProgrammerManager(), jmri.AddressedProgrammerManager.class);

        InstanceManager.store(getPowerManager(), jmri.PowerManager.class);

        InstanceManager.setTurnoutManager(getTurnoutManager());

        InstanceManager.setThrottleManager(getThrottleManager());

        InstanceManager.store(getConsistManager(), ConsistManager.class);

        commandStation = new jmri.jmrix.easydcc.EasyDccCommandStation(this);

        InstanceManager.store(commandStation,jmri.CommandStation.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (type.equals(jmri.GlobalProgrammerManager.class)) {
            return getProgrammerManager().isGlobalProgrammerAvailable();
        }
        if (type.equals(jmri.AddressedProgrammerManager.class)) {
            return getProgrammerManager().isAddressedModePossible();
        }

        if (type.equals(jmri.ThrottleManager.class)) {
            return true;
        }
        if (type.equals(jmri.PowerManager.class)) {
            return true;
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        }
        if (type.equals(jmri.ConsistManager.class)) {
            return true;
        }
        if (type.equals(jmri.CommandStation.class)) {
            return true;
        }
        return super.provides(type); // nothing, by default
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (T.equals(jmri.GlobalProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.AddressedProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }

        if (T.equals(jmri.ThrottleManager.class)) {
            return (T) getThrottleManager();
        }
        if (T.equals(jmri.PowerManager.class)) {
            return (T) getPowerManager();
        }
        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        if (T.equals(jmri.ConsistManager.class)) {
            return (T) getConsistManager();
        }
        if (T.equals(jmri.CommandStation.class)) {
            return (T) commandStation;
        }
        return super.get(T); // nothing, by default
    }

    private EasyDccPowerManager powerManager;

    public EasyDccPowerManager getPowerManager() {
        if (getDisabled()) {
            return null;
        }
        if (powerManager == null) {
            powerManager = new jmri.jmrix.easydcc.EasyDccPowerManager(this);
        }
        return powerManager;
    }

    private ThrottleManager throttleManager;

    public ThrottleManager getThrottleManager() {
        if (getDisabled()) {
            return null;
        }
        if (throttleManager == null) {
            throttleManager = new jmri.jmrix.easydcc.EasyDccThrottleManager(this);
        }
        return throttleManager;
    }

    public void setThrottleManager(ThrottleManager t) {
        throttleManager = t;
    }

    private EasyDccTurnoutManager turnoutManager;

    public EasyDccTurnoutManager getTurnoutManager() {
        if (getDisabled()) {
            return null;
        }
        if (turnoutManager == null) {
            turnoutManager = new jmri.jmrix.easydcc.EasyDccTurnoutManager(this);
        }
        return turnoutManager;
    }

    private EasyDccConsistManager consistManager;

    @Override
    public EasyDccConsistManager getConsistManager() {
        if (getDisabled()) {
            return null;
        }
        if (consistManager == null) {
            consistManager = new jmri.jmrix.easydcc.EasyDccConsistManager(this);
        }
        return consistManager;
    }

    private EasyDccProgrammerManager programmerManager;

    public EasyDccProgrammerManager getProgrammerManager() {
        if (programmerManager == null) {
            programmerManager = new EasyDccProgrammerManager(new EasyDccProgrammer(this), this);
        }
        return programmerManager;
    }

    public void setProgrammerManager(EasyDccProgrammerManager p) {
        programmerManager = p;
    }

    private EasyDccCommandStation commandStation;

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.easydcc.EasyDccActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        et = null;
        InstanceManager.deregister(this, EasyDccSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        if (powerManager != null) {
            InstanceManager.deregister(powerManager, jmri.jmrix.easydcc.EasyDccPowerManager.class);
        }
        if (turnoutManager != null) {
            InstanceManager.deregister(turnoutManager, jmri.jmrix.easydcc.EasyDccTurnoutManager.class);
        }
        if (throttleManager != null) {
            InstanceManager.deregister(((EasyDccThrottleManager) throttleManager), jmri.jmrix.easydcc.EasyDccThrottleManager.class);
        }
        if (consistManager != null) {
            InstanceManager.deregister(consistManager, jmri.jmrix.easydcc.EasyDccConsistManager.class);
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccSystemConnectionMemo.class);

}
