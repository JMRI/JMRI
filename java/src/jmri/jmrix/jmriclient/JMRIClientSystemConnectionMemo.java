//JMRIClientSystemConnectionMemo.java

package jmri.jmrix.jmriclient;

import jmri.*;
import java.util.ResourceBundle;

/**
 * Lightweight class to denote that a system is active
 * and provide general information
 * <p>
 * Objects of specific subtypes are registered in the 
 * instance manager to activate their particular system.
 *
 * @author   Paul Bender Copyright (C) 2010
 * @version  $Revision$
 */

public class JMRIClientSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

   public JMRIClientSystemConnectionMemo(JMRIClientTrafficController jt){
     super("J","JMRI Client");
     this.jt=jt;
     register(); // registers general type
     InstanceManager.store(this,JMRIClientSystemConnectionMemo.class); // also register as specific type

     // create and register the JMRIClientComponentFactory
     InstanceManager.store(cf=new jmri.jmrix.jmriclient.swing.JMRIClientComponentFactory(this),
                           jmri.jmrix.swing.ComponentFactory.class);

   }

   public JMRIClientSystemConnectionMemo(){
     super("J","JMRIClient");
     this.jt=new JMRIClientTrafficController();
     register(); // registers general type
     InstanceManager.store(this,JMRIClientSystemConnectionMemo.class); // also register as specific type

     // create and register the JMRIClientComponentFactory
     InstanceManager.store(cf=new jmri.jmrix.jmriclient.swing.JMRIClientComponentFactory(this),                            jmri.jmrix.swing.ComponentFactory.class);

   }

   jmri.jmrix.swing.ComponentFactory cf = null;

    /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public JMRIClientTrafficController getJMRIClientTrafficController() { return jt; }
    private JMRIClientTrafficController jt;
    public void setJMRIClientTrafficController(JMRIClientTrafficController jt) { this.jt = jt; }

    public void dispose() {
        jt = null;
        InstanceManager.deregister(this, JMRIClientSystemConnectionMemo.class);
        if (cf != null)
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        super.dispose();
    }


    /**
     * Configure the common managers for Internal connections.
     * This puts the common manager config in one
     * place.  
     */
    public void configureManagers() {

        jmri.InstanceManager.setPowerManager(new jmri.jmrix.jmriclient.JMRIClientPowerManager(this));
        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.jmriclient.JMRIClientTurnoutManager(this));
        jmri.InstanceManager.setSensorManager(new jmri.jmrix.jmriclient.JMRIClientSensorManager(this));
        jmri.InstanceManager.setLightManager(new jmri.jmrix.jmriclient.JMRIClientLightManager(this));
        jmri.InstanceManager.setReporterManager(new jmri.jmrix.jmriclient.JMRIClientReporterManager(this));
    }

    public void setTransmitPrefix(String tPrefix){
       transmitPrefix=tPrefix;
    }

    public String getTransmitPrefix(){
      if(transmitPrefix == null ) return getSystemPrefix();
      return transmitPrefix;
    }

    private String transmitPrefix = null;
    
    protected ResourceBundle getActionModelResourceBundle(){
        //No actions that can be loaded at startup
        return null;
    }

}
/* @(#)JMRIClientSystemConnectionMemo.java */
