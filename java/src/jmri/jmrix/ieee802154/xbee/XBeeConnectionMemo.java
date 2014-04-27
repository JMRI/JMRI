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
   
    jmri.jmrix.swing.ComponentFactory componentFactory = null;

    public XBeeConnectionMemo(){
      super("Z","XBee");
      register(); // registers the general type
      InstanceManager.store(this, XBeeConnectionMemo.class); // also register as specific type
    }

    @Override
    protected void init() {
      // create and register the XBeeComponentFactory
     InstanceManager.store(componentFactory=new jmri.jmrix.ieee802154.xbee.swing.XBeeComponentFactory(this),
                           jmri.jmrix.swing.ComponentFactory.class);

    }


 
    /** 
     * Tells which managers this provides by class
     */
    @Override
    public boolean provides(Class<?> type) {
    	if (getDisabled())
    		return false;
         if (type.equals(jmri.SensorManager.class))
             return true;
         if (type.equals(jmri.LightManager.class))
             return true;
         if (type.equals(jmri.TurnoutManager.class))
             return true;
        return false; // nothing, by default
    }
    
    /** 
     * Provide manager by class
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled())
            return null;
        if (T.equals(jmri.SensorManager.class))
             return (T)getSensorManager();
        if (T.equals(jmri.LightManager.class))
             return (T)getLightManager();
        if (T.equals(jmri.TurnoutManager.class))
             return (T)getTurnoutManager();
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

         XBeeTrafficController cont = (XBeeTrafficController)getTrafficController();
         // before we start the managers, request the hardware
         // version.
         cont.sendXBeeMessage(XBeeMessage.getHardwareVersionRequest(),null);
         // and the firmware revision.
         cont.sendXBeeMessage(XBeeMessage.getFirmwareVersionRequest(),null);

         // the start the managers.
         _NodeManager = new XBeeNodeManager(cont);
         
 	setSensorManager(new XBeeSensorManager(cont,getSystemPrefix()));
        jmri.InstanceManager.setSensorManager(getSensorManager());
 	setLightManager(new XBeeLightManager(cont,getSystemPrefix()));
        jmri.InstanceManager.setLightManager(getLightManager());
 	setTurnoutManager(new XBeeTurnoutManager(cont,getSystemPrefix()));
        jmri.InstanceManager.setTurnoutManager(getTurnoutManager());

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

    /*
     * Provides access to the Sensor Manager for this particular connection.
     * NOTE: Sensor manager defaults to NULL
     */
    public SensorManager getSensorManager(){
        return sensorManager;

    }
    public void setSensorManager(SensorManager s){
         sensorManager = s;
    }

    private SensorManager sensorManager=null;

    /*
     * Provides access to the Light Manager for this particular connection.
     * NOTE: Light manager defaults to NULL
     */
    public LightManager getLightManager(){
        return lightManager;

    }
    public void setLightManager(LightManager s){
         lightManager = s;
    }

    private LightManager lightManager=null;

    /*
     * Provides access to the Turnout Manager for this particular connection.
     * NOTE: Turnout manager defaults to NULL
     */
    public TurnoutManager getTurnoutManager(){
        return turnoutManager;

    }
    public void setTurnoutManager(TurnoutManager s){
         turnoutManager = s;
    }

    private TurnoutManager turnoutManager=null;

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
