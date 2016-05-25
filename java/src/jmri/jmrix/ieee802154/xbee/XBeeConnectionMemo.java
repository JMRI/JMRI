// XBeeConnectionMemo.java

package jmri.jmrix.ieee802154.xbee;

import jmri.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * copied from NCE into powerline for multiple connections by
 * @author		Ken Cameron Copyright (C) 2011
 * copied from powerline into IEEE802154 for multiple connections by
 * @author		Paul Bender Copyright (C) 2013
 * @version             $Revision$
 */
public class XBeeConnectionMemo extends jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo {
   
    public XBeeConnectionMemo(){
      super();
      register(); // registers the general type
      InstanceManager.store(this, XBeeConnectionMemo.class); // also register as specific type
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
     * Configure the common managers for XBee connections.
     * This puts the common manager config in one
     * place.  
     */
    @Override
    public void configureManagers() {
         log.error("Configuring Managers for XBee Connection");
         _NodeManager = new XBeeNodeManager((XBeeTrafficController)getTrafficController());
    }

    /*
     * get the Node Manager
     */
   public XBeeNodeManager getXBeeNodeManager() { return _NodeManager; }
   /*
    * set the Node Manager
    */
   public void setXBeeNodeManager(XBeeNodeManager manager) { _NodeManager=manager; }
 
   private XBeeNodeManager _NodeManager=null;

    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.ieee802154.IEEE802154ActionListBundle");
    }
    
    public void dispose() {
        InstanceManager.deregister(this, XBeeConnectionMemo.class);
        super.dispose();
    }

      static Logger log = LoggerFactory.getLogger(XBeeConnectionMemo.class.getName());
    
}


/* @(#)XBeeConnectionMemo.java */
