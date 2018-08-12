package jmri.jmrix.tmcc;

import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.ThrottleManager;
import jmri.TurnoutManager;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Provide the required SystemConnectionMemo.
 *
 * Migrated to multi-system support. Added a configureManagers() method
 * that creates local objects and remove the instance() variables.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 * @author Egbert Broerse Copyright (C) 2017
 */
public class TmccSystemConnectionMemo extends SystemConnectionMemo {

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
        register(); // registers general type
        log.debug("TMCC SystemConnectionMemo with TC");
        InstanceManager.store(this, TmccSystemConnectionMemo.class); // also register as specific type
        // create and register the ComponentFactory for the GUI (menu)
        InstanceManager.store(cf = new jmri.jmrix.tmcc.swing.TmccComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created TMCCSystemConnectionMemo");
    }

    public TmccSystemConnectionMemo(@Nonnull String prefix, @Nonnull String name) {
        super(prefix, name);
        register(); // registers general type
        log.debug("TMCC SystemConnectionMemo prefix={}", prefix);
        InstanceManager.store(this, TmccSystemConnectionMemo.class); // also register as specific type
        // create and register the ComponentFactory for the GUI (menu)
        InstanceManager.store(cf = new jmri.jmrix.tmcc.swing.TmccComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created TMCCSystemConnectionMemo");
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
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
        InstanceManager.setTurnoutManager(getTurnoutManager());
        InstanceManager.setThrottleManager(getThrottleManager());
    }

    /**
     * Tells which managers this class provides.
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }

        if (type.equals(ThrottleManager.class)) {
            return true;
        }

        if (type.equals(TurnoutManager.class)) {
            return true;
        }

        return super.provides(type);
    }

    /**
     * Provide manager by class.
     */
    @SuppressWarnings({"unchecked", "deprecation"})
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }

        if (T.equals(ThrottleManager.class)) {
            return (T) getThrottleManager();
        }

        if (T.equals(TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        return super.get(T);
    }

    private ThrottleManager throttleManager;

    public ThrottleManager getThrottleManager() {
        if (getDisabled()) {
            return null;
        }
        if (throttleManager == null) {
            throttleManager = new jmri.jmrix.tmcc.SerialThrottleManager(this);
        }
        return throttleManager;
    }

    public void setThrottleManager(ThrottleManager t) {
        throttleManager = t;
    }

    private SerialTurnoutManager turnoutManager;

    public SerialTurnoutManager getTurnoutManager() {
        if (getDisabled()) {
            return null;
        }
        if (turnoutManager == null) {
            turnoutManager = new jmri.jmrix.tmcc.SerialTurnoutManager(this);
        }
        return turnoutManager;
    }


    @Override
    public void dispose() {
        trafficController = null;
        InstanceManager.deregister(this, TmccSystemConnectionMemo.class);

        super.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TmccSystemConnectionMemo.class);

}
