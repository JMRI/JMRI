package jmri.jmrix.xpa;

import java.util.Comparator;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.PowerManager;
import jmri.ThrottleManager;
import jmri.TurnoutManager;
import jmri.jmrix.SystemConnectionMemo;
import jmri.util.NamedBeanComparator;

/**
 * Provide the required SystemConnectionMemo for the XPA+Modem adapters.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 * @author Paul Bender Copyright (C) 2016
 */
public class XpaSystemConnectionMemo extends SystemConnectionMemo {

    public XpaSystemConnectionMemo() {
        this("P", "XPA"); // Prefix from XpaTurnoutManager, UserName from XpaThrottleManager
    }

    public XpaSystemConnectionMemo(String prefix, String userName){
        super(prefix, userName); 
        register(); // registers general type
        InstanceManager.store(this,XpaSystemConnectionMemo.class); // also register as specific type

        // create and register the XNetComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.xpa.swing.XpaComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    jmri.jmrix.swing.ComponentFactory cf = null;


    /* manage the associated traffic controller */
    private XpaTrafficController tc = null;
    
    /**
     * Set the XpaTrafficController associated with this memo.
     *
     * @param t is the XpaTrafficController memo to set
     */
    public void setXpaTrafficController(XpaTrafficController t){
       if(t == null) throw new java.lang.IllegalArgumentException("Traffic Controller cannot be set to null.");
       tc = t;
    }

    /**
     * Get the XpaTrafficController associated with this memo.
     *
     * @return XpaTrafficController assocated with this memo
     */
    public XpaTrafficController getXpaTrafficController(){
       return tc;
    }

    /*
     * Provide access to the Throttle Manager for this particular connection.
     */
    public ThrottleManager getThrottleManager() {
        if (throttleManager == null) {
            throttleManager = new XpaThrottleManager(this);
        }
        return throttleManager;
    }


    public void setThrottleManager(ThrottleManager t) {
        throttleManager = t;
    }

    private ThrottleManager throttleManager;

    /*
     * Provide access to the PowerManager for this particular connection.
     */
    public PowerManager getPowerManager() {
        if (powerManager == null) {
            powerManager = new XpaPowerManager(getXpaTrafficController());
        }
        return powerManager;

    }

    public void setPowerManager(PowerManager p) {
        powerManager = p;
    }

    private PowerManager powerManager;

    /*
     * Provide access to the TurnoutManager for this particular connection.
     */
    public TurnoutManager getTurnoutManager() {
        if (turnoutManager == null) {
            turnoutManager = new XpaTurnoutManager(this);
        }
        return turnoutManager;
    }

    public void setTurnoutManager(TurnoutManager t) {
        turnoutManager = t;
    }

    private TurnoutManager turnoutManager = null;

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        } else if (type.equals(jmri.ThrottleManager.class)) {
            return true;
        } else if (type.equals(jmri.PowerManager.class)) {
            return true;
        } else if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
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
        return null; // nothing, by default
   }

    @Override
    public void dispose() {
        tc = null;
        InstanceManager.deregister(this, XpaSystemConnectionMemo.class);
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        super.dispose();
    }

}
