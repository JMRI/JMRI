// Mx1SystemConnectionMemo.javaf
package jmri.jmrix.zimo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.ProgrammerManager;
import java.util.ResourceBundle;

/**
 * Lightweight class to denote that a system is active,
 * and provide general information.
 * <p>
 * Objects of specific subtypes are registered
 * in the instance manager to activate their
 * particular system.
 *
 * @author		Kevin Dickerson Copyright (C) 2012
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision: 19712 $
 */
public class Mx1SystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public Mx1SystemConnectionMemo(Mx1TrafficController st) {
        super("Z", "MX-1");
        this.st = st;
        register();
        InstanceManager.store(this, Mx1SystemConnectionMemo.class); // also register as specific type
    }
    
     public Mx1SystemConnectionMemo() {
        super("Z", "MX-1");
        register(); // registers general type
        InstanceManager.store(this, Mx1SystemConnectionMemo.class); // also register as specific type

        //Needs to be implemented
        /*InstanceManager.store(cf = new jmri.jmrix.ecos.swing.ComponentFactory(this), 
                        jmri.jmrix.swing.ComponentFactory.class);*/
    }
    
    jmri.jmrix.swing.ComponentFactory cf = null;
      
    /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public Mx1TrafficController getMx1TrafficController() { return st; }
    public void setMx1TrafficController(Mx1TrafficController st) { this.st = st; }
    private Mx1TrafficController st;
    
    /**
     * Provide a menu with all items attached to this system connection
     */
    public javax.swing.JMenu getMenu() {
        return new Mx1Menu(this);
    }
    
    /**
     * Configure the programming manager and "command station" objects
     */
    public void configureCommandStation() {

    }

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled())
            return false;
        if (type.equals(jmri.ProgrammerManager.class))
            return true;
        if (type.equals(jmri.PowerManager.class))
            return true;
        if ((type.equals(jmri.CommandStation.class))){
            return true;
        }
        return false; // nothing, by default
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled())
            return null;
        if (T.equals(jmri.ProgrammerManager.class))
            return (T)getProgrammerManager();
        if (T.equals(jmri.PowerManager.class))
            return (T)getPowerManager();
        if (T.equals(jmri.CommandStation.class))
            return (T)commandStation;
        return null; // nothing, by default
    }
    /**
     * Configure the common managers for Mx1 connections.
     * This puts the common manager config in one
     * place.  This method is static so that it can be referenced
     * from classes that don't inherit, including hexfile.HexFileFrame
     * and locormi.LnMessageClient
     */
    public void configureManagers() {

        jmri.InstanceManager.setProgrammerManager(
            getProgrammerManager());

        powerManager = new jmri.jmrix.zimo.Mx1PowerManager();
        jmri.InstanceManager.setPowerManager(powerManager);
    }

    private ProgrammerManager programmerManager;
    private Mx1PowerManager powerManager;

    public ProgrammerManager getProgrammerManager() {
        if (programmerManager == null){
            programmerManager = new jmri.managers.DefaultProgrammerManager(jmri.jmrix.zimo.Mx1Programmer.instance(), this);
        }
        return programmerManager;
    }
    public void setProgrammerManager(ProgrammerManager p) {
        programmerManager = p;
    }
    
    public void setCommandStation(Mx1CommandStation cs){
        commandStation=cs;
    }
    
    private Mx1CommandStation commandStation;

    public Mx1PowerManager getPowerManager() { return powerManager; }
    
    protected ResourceBundle getActionModelResourceBundle(){
        //No actions that can be loaded at startup
        return null;
    }
    
    public void dispose(){
        st = null;
        InstanceManager.deregister(this, Mx1SystemConnectionMemo.class);
        if (cf != null) 
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        super.dispose();
    }
    
    static Logger log = LoggerFactory.getLogger(Mx1SystemConnectionMemo.class.getName());
}


/* @(#)Mx1SystemConnectionMemo.java */
