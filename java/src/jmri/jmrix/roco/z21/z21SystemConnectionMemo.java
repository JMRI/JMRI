// z21SystemConnectionMemo.java

package jmri.jmrix.roco.z21;

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
 * copied from PowerLine into z21 by
 * @author		Paul Bender Copyright (C) 2013
 * @version             $Revision$
 */
public class z21SystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {
    public z21SystemConnectionMemo() {
        this("Z", "Z21");
    }

    public z21SystemConnectionMemo(String prefix, String userName) {
        super(prefix, userName);
        register(); // registers general type
        InstanceManager.store(this, z21SystemConnectionMemo.class); // also register as specific type
        init();
    }

    /*
     * Override the init function for any subtype specific 
     * registration into init.  init is called by the generic contstructor.
     */
    protected void init() {        
        // create and register the ComponentFactory
      InstanceManager.store(componentFactory = new jmri.jmrix.roco.z21.swing.z21ComponentFactory(this), 
                               jmri.jmrix.swing.ComponentFactory.class);
    

    }
 
    jmri.jmrix.swing.ComponentFactory componentFactory = null;

    /**
     * Traffic Controller for this instance.
     */
    public void setTrafficController(z21TrafficController newtc)
         { _tc = newtc; }
    public z21TrafficController getTrafficController() { return _tc; }
    private z21TrafficController _tc = null;

        
    public ProgrammerManager getProgrammerManager() {
        //Do not want to return a programmer for now
        return null;
    }
    
    public void setProgrammerManager(ProgrammerManager p) {
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
     * Configure the common managers for z21 connections.
     * This puts the common manager config in one
     * place.  
     */
    public void configureManagers() {
    	// now does nothing here, it's done by the specific class
    }

    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.roco.z21.z21ActionListBundle");
    }
    
    public void dispose() {
        InstanceManager.deregister(this, z21SystemConnectionMemo.class);
        super.dispose();
    }
    
}


/* @(#)z21SystemConnectionMemo.java */
