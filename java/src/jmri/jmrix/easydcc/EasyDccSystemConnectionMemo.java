package jmri.jmrix.easydcc;

import java.util.Comparator;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.managers.DefaultProgrammerManager;
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
public class EasyDccSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

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
        log.debug("EasyDCC SystemConnectionMemo with TC");
        InstanceManager.store(this, EasyDccSystemConnectionMemo.class);
        // create and register the ComponentFactory for the GUI (menu)
        InstanceManager.store(cf = new jmri.jmrix.easydcc.swing.EasyDccComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created EasyDccSystemConnectionMemo");
    }

    public EasyDccSystemConnectionMemo(@Nonnull String prefix, @Nonnull String name) {
        super(prefix, name);
        log.debug("EasyDCC SystemConnectionMemo prefix={}", prefix);
        InstanceManager.store(this, EasyDccSystemConnectionMemo.class);
        // create and register the ComponentFactory for the GUI (menu)
        InstanceManager.store(cf = new jmri.jmrix.easydcc.swing.EasyDccComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created EasyDccSystemConnectionMemo");
    }

    jmri.jmrix.swing.ComponentFactory cf = null;
    private EasyDccTrafficController et;

    /**
     * Provide access to the TrafficController for this particular connection.
     * @return traffic controller, provided if null.
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
        InstanceManager.store(getProgrammerManager(), AddressedProgrammerManager.class);

        InstanceManager.store(getPowerManager(), jmri.PowerManager.class);

        InstanceManager.setTurnoutManager(getTurnoutManager());

        InstanceManager.setThrottleManager(getThrottleManager());

        InstanceManager.store(getConsistManager(), ConsistManager.class);

        EasyDccCommandStation commandStation = new jmri.jmrix.easydcc.EasyDccCommandStation(this);
        InstanceManager.store(commandStation,CommandStation.class);
        store(commandStation,CommandStation.class);

        register(); // registers general type
    }

    public EasyDccPowerManager getPowerManager() {
        if (getDisabled()) {
            return null;
        }
        return (EasyDccPowerManager) classObjectMap.computeIfAbsent(PowerManager.class,
                (Class<?> c) -> new jmri.jmrix.easydcc.EasyDccPowerManager(this));
    }

    public ThrottleManager getThrottleManager() {
        if (getDisabled()) {
            return null;
        }
        return (ThrottleManager) classObjectMap.computeIfAbsent(ThrottleManager.class,
                (Class<?> c) -> new jmri.jmrix.easydcc.EasyDccThrottleManager(this));
    }

    public void setThrottleManager(ThrottleManager t) {
        store(t,ThrottleManager.class);
    }

    public EasyDccTurnoutManager getTurnoutManager() {
        if (getDisabled()) {
            return null;
        }
        return (EasyDccTurnoutManager) classObjectMap.computeIfAbsent(TurnoutManager.class,
                (Class<?> c) -> new jmri.jmrix.easydcc.EasyDccTurnoutManager(this));
    }

    @Override
    public EasyDccConsistManager getConsistManager() {
        if (getDisabled()) {
            return null;
        }
        return (EasyDccConsistManager) classObjectMap.computeIfAbsent(ConsistManager.class,
                (Class<?> c) -> new jmri.jmrix.easydcc.EasyDccConsistManager(this));
    }

    public EasyDccProgrammerManager getProgrammerManager() {
         return (EasyDccProgrammerManager) classObjectMap.computeIfAbsent(DefaultProgrammerManager.class,
                 (Class<?> c) -> new EasyDccProgrammerManager(new EasyDccProgrammer(this), this));
    }

    public void setProgrammerManager(EasyDccProgrammerManager p) {
        store(p, DefaultProgrammerManager.class);
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.easydcc.EasyDccActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public boolean provides(Class<?> c) {
        if (!getDisabled() && c.equals(ConsistManager.class)) {
            return true;
        }
        return super.provides(c);
    }
    
    @Override
    public void dispose() {
        et = null;
        InstanceManager.deregister(this, EasyDccSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccSystemConnectionMemo.class);

}
