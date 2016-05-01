package jmri.jmrix.xpa;

import java.util.ResourceBundle;
import jmri.jmrix.SystemConnectionMemo;
import jmri.InstanceManager;

/**
 * Provide the required SystemConnectionMemo for the XPA+Modem adapters.
 * <p>
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
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
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

    /* manage the associated traffic controller */
    private XpaTrafficController tc = null;
    
    /**
     * Set the XpaTrafficController associated with this memo
     * <P>
     * @param t is the XpaTrafficController memo to set
     */
    public void setXpaTrafficController(XpaTrafficController t){
       if(t == null) throw new java.lang.IllegalArgumentException("Traffic Controller cannot be set to null.");
       tc = t;
    }

    /**
     * Get the XpaTrafficController associated with this memo
     * <p>
     * @return XpaTrafficController assocated with this memo.
     */
    public XpaTrafficController getXpaTrafficController(){
       return tc;
    }

}
