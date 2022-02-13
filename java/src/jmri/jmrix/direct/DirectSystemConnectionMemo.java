package jmri.jmrix.direct;

import java.util.Comparator;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;

import jmri.CommandStation;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.util.NamedBeanComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimum required SystemConnectionMemo.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class DirectSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    jmri.jmrix.swing.ComponentFactory cf = null;

    public DirectSystemConnectionMemo() {
        this("N", "Others");
    }

    public DirectSystemConnectionMemo(@Nonnull String prefix, @Nonnull String userName) {
        super(prefix, userName);

        InstanceManager.store(this, DirectSystemConnectionMemo.class);

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
        store(tc, CommandStation.class);
        InstanceManager.store(tc,CommandStation.class);
    }

    /**
     * Get the traffic controller instance associated with this connection memo.
     * @return traffic controller, provided if null.
     */
    public TrafficController getTrafficController(){
        if (tc == null) {
            setTrafficController(new TrafficController(this));
            log.debug("Auto create of TrafficController for initial configuration");
        }
        return tc;
    }

    /**
     * Set the traffic controller instance associated with this connection memo.
     *
     * @param s jmri.jmrix.direct.ThrottleManager object to use.
     */
    public void setThrottleManager(ThrottleManager s){
        store(s,ThrottleManager.class);
        InstanceManager.store(get(ThrottleManager.class),ThrottleManager.class);
    }

    /**
     * Get the ThrottleManager instance associated with this connection memo.
     * @return throttle manager, provided if null.
     */
    public ThrottleManager getThrottleManager(){
        return (ThrottleManager) classObjectMap.computeIfAbsent(ThrottleManager.class,
                (Class<?> c) -> {
                    setThrottleManager(new ThrottleManager(this));
                    log.debug("Auto create of ThrottleManager for initial configuration");
                    return get(ThrottleManager.class);
                });
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    public void configureManagers(){
        setThrottleManager(new ThrottleManager(this));
        register();
    }

    private final static Logger log = LoggerFactory.getLogger(DirectSystemConnectionMemo.class);

}
