package jmri.jmrix.direct;

import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.jmrix.SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimum required SystemConnectionMemo.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class DirectSystemConnectionMemo extends SystemConnectionMemo {

    jmri.jmrix.swing.ComponentFactory cf = null;

    public DirectSystemConnectionMemo() {
        this("N", "Others");
    }

    public DirectSystemConnectionMemo(@Nonnull String prefix, @Nonnull String userName) {
        super(prefix, userName);

        register(); // registers general type
        InstanceManager.store(this, DirectSystemConnectionMemo.class); // also register as specific type

        // create and register the ComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.direct.swing.DirectComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created DirectSystemConnectionMemo");
    }

    private TrafficController tc = null;

    /**
     * Set the traffic controller instance associated with this connection memo.
     *
     * @param s jmri.jmrix.direct.TrafficController object to use.
     */
    public void setTrafficController(TrafficController s){
        tc = s;
        InstanceManager.store(tc,jmri.CommandStation.class);
    }

    /**
     * Get the traffic controller instance associated with this connection memo.
     */
    public TrafficController getTrafficController(){
        if (tc == null) {
            setTrafficController(new TrafficController());
            log.debug("Auto create of TrafficController for initial configuration");
        }
        return tc;
    }

    private ThrottleManager tm = null;

    /**
     * Set the traffic controller instance associated with this connection memo.
     *
     * @param s jmri.jmrix.direct.ThrottleManager object to use.
     */
    public void setThrottleManager(ThrottleManager s){
        tm = s;
        InstanceManager.store(tm,jmri.ThrottleManager.class);
    }

    /**
     * Get the throttle manager instance associated with this connection memo.
     */
    public ThrottleManager getThrottleManager(){
        if (tm == null) {
            setThrottleManager(new ThrottleManager(getTrafficController()));
            log.debug("Auto create of ThrottleManager for initial configuration");
        }
        return tm;
    }


    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

    public void configureManagers(){
    }

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        } else if (type.equals(jmri.ThrottleManager.class)) {
            return true;
        } else if (type.equals(jmri.CommandStation.class)) {
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
        if (T.equals(jmri.CommandStation.class)) {
            return (T) getTrafficController(); // tc is a command station.
        }
        return super.get(T);
    }


    private final static Logger log = LoggerFactory.getLogger(DirectSystemConnectionMemo.class);

}
