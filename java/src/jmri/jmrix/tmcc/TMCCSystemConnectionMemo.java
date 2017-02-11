package jmri.jmrix.tmcc;

import java.util.ResourceBundle;

import jmri.*;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Provide the minimal required SystemConnectionMemo.
 * 
 * This is still single-system code, using the turnout and throttle
 * instance() methods. To migrate to multiple systems, add a configureManagers()
 * method that creates local objects and remove the instance() variables.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class TMCCSystemConnectionMemo extends SystemConnectionMemo {

    public TMCCSystemConnectionMemo() {
        super("T", "Lionel TMCC"); // Prefix from SerialTurnoutManager, UserName from SerialThrottleManager
        register(); // registers general type
        InstanceManager.store(this, TMCCSystemConnectionMemo.class); // also register as specific type
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

    /**
     * Provides access to the TrafficController for this particular connection.
     */
    public SerialTrafficController getTrafficController() {
        return trafficController;
    }
    private SerialTrafficController trafficController;

    public void setTrafficController(SerialTrafficController tc) {
        trafficController = tc;
    }

    /**
     * Configure the common managers for tmcc connections. This puts the common
     * manager config in one place.
     */
    @SuppressWarnings("deprecation")
    public void configureManagers() {
        log.debug("configureManagers");
        jmri.InstanceManager.setTurnoutManager(jmri.jmrix.tmcc.SerialTurnoutManager.instance());
        jmri.InstanceManager.setThrottleManager(jmri.jmrix.tmcc.SerialThrottleManager.instance());
    }
    
    /**
     * Tells which managers this provides by class
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }

        if (type.equals(jmri.ThrottleManager.class)) {
            return true;
        }

        if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        }

        return false; // nothing, by default
    }

    /**
     * Provide manager by class
     */
    @SuppressWarnings({"unchecked", "deprecation"})
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }

        if (T.equals(jmri.ThrottleManager.class)) {
            return (T) getThrottleManager();
        }

        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        return null; // nothing, by default
    }

    public TurnoutManager getTurnoutManager() {
        return SerialTurnoutManager.instance();
    }

    public ThrottleManager getThrottleManager() {
        return SerialThrottleManager.instance();
    }

    @Override
    public void dispose() {
        trafficController = null;
        InstanceManager.deregister(this, TMCCSystemConnectionMemo.class);

        super.dispose();
    }
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TMCCSystemConnectionMemo.class.getName());

}
