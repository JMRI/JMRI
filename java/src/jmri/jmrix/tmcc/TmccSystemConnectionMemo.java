package jmri.jmrix.tmcc;

import java.util.Comparator;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.util.NamedBeanComparator;

/**
 * Provide the required SystemConnectionMemo.
 *
 * Migrated to multi-system support. Added a configureManagers() method
 * that creates local objects and remove the instance() variables.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 * @author Egbert Broerse Copyright (C) 2017
 */
public class TmccSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    public TmccSystemConnectionMemo() {
        this("T", "Lionel TMCC");
    }

    /**
     * Ctor
     *
     * @param tc the associated TrafficController
     */
    public TmccSystemConnectionMemo(SerialTrafficController tc) {
        super("T", "Lionel TMCC");
        trafficController = tc;
        log.debug("TMCC SystemConnectionMemo with TC");
        InstanceManager.store(this, TmccSystemConnectionMemo.class);
        // create and register the ComponentFactory for the GUI (menu)
        InstanceManager.store(cf = new jmri.jmrix.tmcc.swing.TmccComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created TMCCSystemConnectionMemo");
    }

    public TmccSystemConnectionMemo(@Nonnull String prefix, @Nonnull String name) {
        super(prefix, name);
        log.debug("TMCC SystemConnectionMemo prefix={}", prefix);
        InstanceManager.store(this, TmccSystemConnectionMemo.class);
        // create and register the ComponentFactory for the GUI (menu)
        InstanceManager.store(cf = new jmri.jmrix.tmcc.swing.TmccComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created TMCCSystemConnectionMemo");
    }

    jmri.jmrix.swing.ComponentFactory cf;

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    private SerialTrafficController trafficController;

    /**
     * Provide access to the TrafficController for this particular connection.
     *
     * @return the traffic controller for this connection
     */
    public SerialTrafficController getTrafficController() {
        if (trafficController == null) {
            setTrafficController(new SerialTrafficController(this));
            log.debug("Auto create of TMCC SerialTrafficController for initial configuration");
        }
        return trafficController;
    }

    /**
     * @param tc the new TrafficController to set
     */
    public void setTrafficController(SerialTrafficController tc) {
        trafficController = tc;
        // in addition to setting the TrafficController in this object,
        // set the systemConnectionMemo in the traffic controller
        tc.setSystemConnectionMemo(this);
    }

    /**
     * Configure the common managers for tmcc connections. This puts the common
     * manager config in one place.
     */
    public void configureManagers() {
        log.debug("configureManagers");
        TurnoutManager turnoutManager = getTurnoutManager();
        store(turnoutManager,TurnoutManager.class);
        InstanceManager.setTurnoutManager(getTurnoutManager());
        ThrottleManager throttleManager = getThrottleManager();
        store(throttleManager,ThrottleManager.class);
        InstanceManager.setThrottleManager(getThrottleManager());
        register();
    }

    public ThrottleManager getThrottleManager() {
        if (getDisabled()) {
            return null;
        }
        return (SerialThrottleManager) classObjectMap.computeIfAbsent(ThrottleManager.class, (Class<?> c) -> new SerialThrottleManager(this));
    }

    public void setThrottleManager(ThrottleManager t) {
        classObjectMap.put(ThrottleManager.class,t);
    }

    public SerialTurnoutManager getTurnoutManager() {
        if (getDisabled()) {
            return null;
        }
        return (SerialTurnoutManager) classObjectMap.computeIfAbsent(TurnoutManager.class,(Class<?> c) -> new SerialTurnoutManager(this));
    }


    @Override
    public void dispose() {
        trafficController = null;
        InstanceManager.deregister(this, TmccSystemConnectionMemo.class);
        super.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TmccSystemConnectionMemo.class);

}
