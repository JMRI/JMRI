// SRCPBusConnectionMemo.java

package jmri.jmrix.srcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.*;
import java.util.ResourceBundle;
import jmri.jmrix.srcp.parser.*;

/**
 * Lightweight class to denote that a system is active,
 * and provide general information.
 * <p>
 * Objects of specific subtypes are registered
 * in the instance manager to activate their
 * particular system.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision$
 */
public class SRCPBusConnectionMemo extends jmri.jmrix.SystemConnectionMemo implements SRCPListener {

    private int _bus = 0;

    public SRCPBusConnectionMemo(SRCPTrafficController et,String Prefix,int bus) {
        super(Prefix + bus, "SRCP");
        this.et = et;
        //this.et.setSystemConnectionMemo(this);
        _bus=bus;
        register();
        log.debug("Created SRCPBusConnectionMemo for bus " + bus);
        et.addSRCPListener(this);
        et.sendSRCPMessage(new SRCPMessage("GET " + bus +" DESCRIPTION\n"),null);
    }
    
    jmri.jmrix.swing.ComponentFactory cf = null;
    
     /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public SRCPTrafficController getTrafficController() { return et; }
    public void setTrafficController(SRCPTrafficController et) 
    { 
       this.et = et; 
    }

    private SRCPTrafficController et;
    
    /**
     * Configure the common managers for Internal connections.
     * This puts the common manager config in one
     * place.  
     */
    public void configureManagers() {
    

    }

    /**
     * package protected function to get the bus associated with
     * this memo.
     * @return integer bus number.
     */
    int getBus() { return _bus; }
    
    /**
     * Provides access to the Programmer for this particular connection.
     * NOTE: Programmer defaults to null
     */
    public ProgrammerManager getProgrammerManager() {
        return programmerManager;
    }
    public void setProgrammerManager(ProgrammerManager p) {
        programmerManager = p;
    }

    private ProgrammerManager programmerManager=null;

    /*
     * Provides access to the Throttle Manager for this particular connection.
     */
    public ThrottleManager getThrottleManager(){
        if (throttleManager == null)
            throttleManager = new SRCPThrottleManager(this);
        return throttleManager;

    }
    public void setThrottleManager(ThrottleManager t){
         throttleManager = t;
    }

    private ThrottleManager throttleManager;

    /*
     * Provides access to the Power Manager for this particular connection.
     */
    public PowerManager getPowerManager(){
        return powerManager;
   }
    public void setPowerManager(PowerManager p){
         powerManager = p;
    }

    private PowerManager powerManager;

    /*
     * Provides access to the Sensor Manager for this particular connection.
     */
    public SensorManager getSensorManager(){
        return sensorManager;

    }
    public void setSensorManager(SensorManager s){
         sensorManager = s;
    }

    private SensorManager sensorManager=null;

    /*
     * Provides access to the Turnout Manager for this particular connection.
     * NOTE: Turnout manager defaults to NULL
     */
    public TurnoutManager getTurnoutManager(){
        return turnoutManager;

    }
    public void setTurnoutManager(TurnoutManager t){
         turnoutManager = t;
    }
    
    private TurnoutManager turnoutManager=null;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled())
            return null;
        if (T.equals(jmri.ProgrammerManager.class))
            return (T)getProgrammerManager();
        if (T.equals(jmri.PowerManager.class))
            return (T)getPowerManager();
        if (T.equals(jmri.ThrottleManager.class))
            return (T)getThrottleManager();
        if (T.equals(jmri.SensorManager.class))
            return (T)getSensorManager();
        if (T.equals(jmri.TurnoutManager.class))
            return (T)getTurnoutManager();
        return null; // nothing, by default
    }
    
    /** 
     * Tells which managers this provides by class
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled())
            return false;
        if (type.equals(jmri.ProgrammerManager.class))
            return (null != programmerManager);
        if (type.equals(jmri.ThrottleManager.class))
            return (null != throttleManager);
        if (type.equals(jmri.PowerManager.class))
            return (null != powerManager);
        if (type.equals(jmri.SensorManager.class))
            return (null != sensorManager);
        if (type.equals(jmri.TurnoutManager.class))
            return (null != turnoutManager);
        return false; // nothing, by default
    }
    
    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.srcp.SrcpActionListBundle");
    }
   
    public void dispose(){
        et = null;
        InstanceManager.deregister(this, SRCPBusConnectionMemo.class);
        if (cf != null) 
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        super.dispose();
    }

    // functions for the SRCP Listener interface.
    public void message(SRCPMessage m){
    }

    public void reply(SRCPReply m){
    }

    public void reply(jmri.jmrix.srcp.parser.SimpleNode n){
       log.debug("SimpleNode Reply called with " + n.toString());
       reply(new SRCPReply(n));
       if(n.jjtGetChild(1)  instanceof ASTinfo) {
          jmri.jmrix.srcp.parser.SimpleNode infonode =
                 (jmri.jmrix.srcp.parser.SimpleNode) n.jjtGetChild(1);
          if( !((String)((SimpleNode)(infonode.jjtGetChild(0))).jjtGetValue()).equals("" + _bus ))
              return; // not for this bus.
          // Look for description information for this bus, and configure the
          // managers for this bus.
          if(infonode.jjtGetChild(1) instanceof jmri.jmrix.srcp.parser.ASTdescription) {
           SimpleNode descnode = (SimpleNode) infonode.jjtGetChild(1);
           for(int i=0;i<descnode.jjtGetNumChildren();i++) {
              jmri.jmrix.srcp.parser.SimpleNode child =
                    (jmri.jmrix.srcp.parser.SimpleNode) descnode.jjtGetChild(i);
                 log.debug("child node type " + child.toString() + 
                           " value " + (String)child.jjtGetValue() );
                 if(child instanceof jmri.jmrix.srcp.parser.ASTdevicegroup ) {
                    String DeviceType=(String)child.jjtGetValue();
                    if( DeviceType.equals("FB")) {
                         setSensorManager(new jmri.jmrix.srcp.SRCPSensorManager(this,_bus)); 
                         jmri.InstanceManager.setSensorManager(getSensorManager());
                   
                    } else if(DeviceType.equals("GA")) {
                         setTurnoutManager(new jmri.jmrix.srcp.SRCPTurnoutManager(this,_bus)); 
                         jmri.InstanceManager.setTurnoutManager(getTurnoutManager());
                    } else if(DeviceType.equals("SM")) {
                        setProgrammerManager(new SRCPProgrammerManager(new SRCPProgrammer(this), this));
                        jmri.InstanceManager.setProgrammerManager(getProgrammerManager());
                    } else if(DeviceType.equals("POWER")) {
                        setPowerManager(new jmri.jmrix.srcp.SRCPPowerManager(this,_bus)); 
                        jmri.InstanceManager.setPowerManager(getPowerManager());
                    } else if(DeviceType.equals("GL")) {
                        setThrottleManager(new jmri.jmrix.srcp.SRCPThrottleManager(this)); 
                        jmri.InstanceManager.setThrottleManager(getThrottleManager());
                    }
                 }
             }
          }
       }      
    }

    static Logger log = LoggerFactory.getLogger(SRCPBusConnectionMemo.class.getName());

}


/* @(#)SRCPBusConnectionMemo.java */
