// SprogSystemConnectionMemo.java

package jmri.jmrix.sprog;

import jmri.InstanceManager;
import jmri.jmrix.sprog.SprogConstants.SprogMode;
//import jmri.jmrix.sprog.SprogTrafficController;
//import jmri.jmrix.sprog.SprogCommandStation;
//import jmri.jmrix.sprog.SprogProgrammer;
//import jmri.jmrix.sprog.SprogProgrammerManager;

/**
 * Lightweight class to denote that a system is active,
 * and provide general information.
 * <p>
 * Objects of specific subtypes are registered
 * in the instance manager to activate their
 * particular system.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision: 1.5 $
 */
public class SprogSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public SprogSystemConnectionMemo(SprogTrafficController st, SprogMode sm) {
        super("S", "Sprog");
        this.st = st;
        sprogMode = sm;  // static
        register();
    }
    
     public SprogSystemConnectionMemo() {
        super("S", "Sprog");
        register(); // registers general type
        InstanceManager.store(this, SprogSystemConnectionMemo.class); // also register as specific type
        //Needs to be implemented
        /*InstanceManager.store(cf = new jmri.jmrix.ecos.swing.ComponentFactory(this), 
                        jmri.jmrix.swing.ComponentFactory.class);*/
    }
    
    public void setSprogMode(SprogMode mode){ sprogMode=mode; }
    public SprogMode getSprogMode() { return sprogMode; }
    private SprogMode sprogMode;

    jmri.jmrix.swing.ComponentFactory cf = null;
      
    /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public SprogTrafficController getSprogTrafficController() { return st; }
    public void setSprogTrafficController(SprogTrafficController st) { this.st = st; }
    private SprogTrafficController st;
    
    /**
     * Provide a menu with all items attached to this system connection
     */
    public javax.swing.JMenu getMenu() {
        switch (sprogMode){
            case OPS : return new SPROGMenu(this);
            case SERVICE : return new SPROGCSMenu(this);
        }
        return new SPROGMenu(this);
    }

    private Thread slotThread;
    
    /**
     * Configure the programming manager and "command station" objects
     */
    public void configureCommandStation() {
        switch (sprogMode){
            case OPS : 
                    log.debug("start command station queuing thread");
                    slotThread = new Thread(jmri.jmrix.sprog.SprogCommandStation.instance());
                    slotThread.start();
                    break;
            case SERVICE :
                    jmri.InstanceManager.setCommandStation(new jmri.jmrix.sprog.SprogCommandStation());
                    break;
        }
        log.debug("start command station queuing thread");
        slotThread = new Thread(jmri.jmrix.sprog.SprogCommandStation.instance());
        slotThread.start();
        jmri.InstanceManager.setCommandStation(SprogCommandStation.instance());
    }

    /**
     * Configure the common managers for Sprog connections.
     * This puts the common manager config in one
     * place.  This method is static so that it can be referenced
     * from classes that don't inherit, including hexfile.HexFileFrame
     * and locormi.LnMessageClient
     */
    public void configureManagers() {
    
        jmri.InstanceManager.setProgrammerManager(new SprogProgrammerManager(new SprogProgrammer(), sprogMode));

        jmri.InstanceManager.setPowerManager(new jmri.jmrix.sprog.SprogPowerManager());

        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.sprog.SprogTurnoutManager());
        

        switch (sprogMode){
            case OPS : jmri.InstanceManager.setThrottleManager(new jmri.jmrix.sprog.SprogCSThrottleManager());
                break;
            case SERVICE : jmri.InstanceManager.setThrottleManager(new jmri.jmrix.sprog.SprogThrottleManager());
                break;

        }

    }
    
    public void dispose(){
        st = null;
        InstanceManager.deregister(this, SprogSystemConnectionMemo.class);
        if (cf != null) 
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        super.dispose();
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SprogSystemConnectionMemo.class.getName());
}


/* @(#)SprogSystemConnectionMemo.java */
