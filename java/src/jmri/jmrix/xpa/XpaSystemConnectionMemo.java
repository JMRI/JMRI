package jmri.jmrix.xpa;

import java.util.Comparator;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.PowerManager;
import jmri.ThrottleManager;
import jmri.TurnoutManager;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.util.NamedBeanComparator;

/**
 * Provide the required SystemConnectionMemo for the XPA+Modem adapters.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 * @author Paul Bender Copyright (C) 2016,2020
 */
public class XpaSystemConnectionMemo extends DefaultSystemConnectionMemo {

    public XpaSystemConnectionMemo() {
        this("P", "XPA"); // Prefix from XpaTurnoutManager, UserName from XpaThrottleManager
    }

    public XpaSystemConnectionMemo(String prefix, String userName){
        super(prefix, userName); 
        InstanceManager.store(this,XpaSystemConnectionMemo.class);
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

    final jmri.jmrix.swing.ComponentFactory cf;

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
        return (ThrottleManager)classObjectMap.computeIfAbsent(ThrottleManager.class, (Class c) -> { return new XpaThrottleManager(this);});
    }


    public void setThrottleManager(ThrottleManager t) {
        store(t,ThrottleManager.class);
    }

    /*
     * Provide access to the PowerManager for this particular connection.
     */
    public PowerManager getPowerManager() {
        return (PowerManager) classObjectMap.computeIfAbsent(PowerManager.class,(Class c) -> { return new XpaPowerManager(this); });
    }

    public void setPowerManager(PowerManager p) {
        store(p,PowerManager.class);
    }

    /*
     * Provide access to the TurnoutManager for this particular connection.
     */
    public TurnoutManager getTurnoutManager() {
        return (TurnoutManager) classObjectMap.computeIfAbsent(TurnoutManager.class,(Class c) -> { return  new XpaTurnoutManager(this); });
    }

    public void setTurnoutManager(TurnoutManager t) {
        store(t,TurnoutManager.class);
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
