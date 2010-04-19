// SprogSystemConnectionMemo.java

package jmri.jmrix.sprog;

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
 * @version             $Revision: 1.1 $
 */
public class SprogSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public SprogSystemConnectionMemo(SprogTrafficController st, SprogMode sprogMode) {
        super("S"+(instanceCount>1?""+instanceCount:""), "Sprog"+(instanceCount>1?""+instanceCount:""));
        this.st = st;
        this.sprogMode = sprogMode;
        count = instanceCount++;
        register();
    }
    
    private static int instanceCount = 1;
    
    private static SprogMode sprogMode;
    
    private int count;
      
    /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public SprogTrafficController getSprogTrafficController() { return st; }
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

    String suffix() { return count>1?""+count:""; }
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
     * from classes that don't inherit.
     */
    public void configureManagers() {
    
        jmri.InstanceManager.setProgrammerManager(new SprogProgrammerManager(new SprogProgrammer(), sprogMode));

        jmri.InstanceManager.setPowerManager(new jmri.jmrix.sprog.SprogPowerManager());

        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.sprog.SprogTurnoutManager());
        
        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.sprog.SprogThrottleManager());

    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SprogSystemConnectionMemo.class.getName());
}


/* @(#)SprogSystemConnectionMemo.java */
