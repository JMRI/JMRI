// IEEE802154SystemConnectionMemo.java

package jmri.jmrix.ieee802154;

import jmri.*;
import java.util.ResourceBundle;

/**
 * Lightweight class to denote that a system is active,
 * and provide general information.
 * <p>
 * Objects of specific subtypes are registered
 * in the instance manager to activate their
 * particular system.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * copied from NCE into PowerLine for multiple connections by
 * @author		Ken Cameron Copyright (C) 2011
 * copied from PowerLine into IEEE802154 by
 * @author		Paul Bender Copyright (C) 2013
 * @version             $Revision$
 */
public class IEEE802154SystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {
    public IEEE802154SystemConnectionMemo() {
        super("Z", "IEEE802.15.4");
        register(); // registers general type
        InstanceManager.store(this, IEEE802154SystemConnectionMemo.class); // also register as specific type
        
        // create and register the ComponentFactory
      InstanceManager.store(componentFactory = new jmri.jmrix.ieee802154.swing.IEEE802154ComponentFactory(this), 
                               jmri.jmrix.swing.ComponentFactory.class);
    }
    
    jmri.jmrix.swing.ComponentFactory componentFactory = null;

    /**
     * Traffic Controller for this instance.
     */
    public void setTrafficController(IEEE802154TrafficController newtc)
         { _tc = newtc; }
    public IEEE802154TrafficController getTrafficController() { return _tc; }
    private IEEE802154TrafficController _tc = null;

        
    /**
     * Always null as ieee802154 doesn't have a programmer
     */
    public ProgrammerManager getProgrammerManager() {
        //Do not want to return a programmer ever
        return null;
    }
    
    public void setProgrammerManager(ProgrammerManager p) {
        // no programmer supported, should I throw an Exception??
    }
    
    /** 
     * Tells which managers this provides by class
     */
    public boolean provides(Class<?> type) {
    	if (getDisabled())
    		return false;
        return false; // nothing, by default
    }
    
    /** 
     * Provide manager by class
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled())
            return null;
        return null; // nothing, by default
    }

    /**
     * Configure the common managers for IEEE802154 connections.
     * This puts the common manager config in one
     * place.  
     */
    public void configureManagers() {
    	// now does nothing here, it's done by the specific class
    }

    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.ieee802154.IEEE802154ActionListBundle");
    }
    
    public void dispose() {
        InstanceManager.deregister(this, IEEE802154SystemConnectionMemo.class);
        super.dispose();
    }
    
}


/* @(#)IEEE802154SystemConnectionMemo.java */
