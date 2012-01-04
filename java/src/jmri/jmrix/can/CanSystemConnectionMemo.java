// CanSystemConnectionMemo.java

package jmri.jmrix.can;

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
 * @version             $Revision: 19602 $
 */
public class CanSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public CanSystemConnectionMemo(String protocol, String prefix, String systemName) {
        super(prefix, systemName);
        this.protocol=protocol;
        register(); // registers general type
        InstanceManager.store(this, CanSystemConnectionMemo.class); // also register as specific type
        
        // create and register the LnComponentFactory
        /*InstanceManager.store(cf = new jmri.jmrix.loconet.swing.LnComponentFactory(this),
                                jmri.jmrix.swing.ComponentFactory.class);*/
    }
    
    public CanSystemConnectionMemo(String protocol) {
        super("M", "MERG");
        this.protocol=protocol;
        register(); // registers general type
        InstanceManager.store(this, CanSystemConnectionMemo.class); // also register as specific type
        
        // create and register the LnComponentFactory
        /*InstanceManager.store(cf = new jmri.jmrix.loconet.swing.LnComponentFactory(this),
                                jmri.jmrix.swing.ComponentFactory.class);*/
                                
    }
    
    public CanSystemConnectionMemo() {
        super("M", "MERG");
        this.protocol=protocol;
        register(); // registers general type
        InstanceManager.store(this, CanSystemConnectionMemo.class); // also register as specific type
        
        // create and register the LnComponentFactory
        /*InstanceManager.store(cf = new jmri.jmrix.loconet.swing.LnComponentFactory(this),
                                jmri.jmrix.swing.ComponentFactory.class);*/
                                
    }
    
    jmri.jmrix.swing.ComponentFactory cf = null;
    
    protected TrafficController tm;
    
    public void setTrafficController(TrafficController tm){
        this.tm=tm;
    }
    
    public TrafficController getTrafficController() { return tm; }
    
    private String protocol;
    
    private jmri.jmrix.can.ConfigurationManager manager;
    
    /** 
     * Tells which managers this provides by class
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled())
            return false;
        if(manager==null)
            return false;
        return manager.provides(type);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled())
            return null;
        if(manager!=null)
            return (T)manager.get(T);
        return null; // nothing, by default
    }
    
    public void setProtocol(String protocol){
        this.protocol = protocol;
        if (ConfigurationManager.MERGCBUS.equals(this.protocol)) {
            manager = new jmri.jmrix.can.cbus.CbusConfigurationManager(this);
        }
        
    }
    
    /**
     * Configure the common managers for Can connections.
     * This puts the common manager config in one
     * place.  
     */
    public void configureManagers() {
        if(manager!=null)
            manager.configureManagers();
    }
    
    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.can.CanActionListBundle");
    }
    
    public void dispose() {
        if(manager!=null)
            manager.dispose();
        tm=null;
        super.dispose();

    }
        // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CanSystemConnectionMemo.class.getName());
}


/* @(#)CanSystemConnectionMemo.java */
