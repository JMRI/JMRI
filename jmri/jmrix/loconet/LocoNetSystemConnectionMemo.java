// LocoNetSystemConnectionMemo.java

package jmri.jmrix.loconet;

import jmri.InstanceManager;

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
public class LocoNetSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public LocoNetSystemConnectionMemo(LnTrafficController lt,
                                        SlotManager sm) {
        super("L"+(instanceCount>1?""+instanceCount:""), "LocoNet"+(instanceCount>1?""+instanceCount:""));
        this.lt = lt;
        this.sm = sm;
        count = instanceCount++;
        register();
    }
    
    private static int instanceCount = 1;
    
    private int count;
    
    /**
     * Provides access to the SlotManager for this
     * particular connection.
     */
    public SlotManager getSlotManager() { return sm; }
    private SlotManager sm;
    
    /**
     * Provides access to the TrafficController for this
     * particular connection.
     */
    public LnTrafficController getLnTrafficController() { return lt; }
    private LnTrafficController lt;
    
    /**
     * Provide a menu with all items attached to this system connection
     */
    public javax.swing.JMenu getMenu() {
        return new LocoNetMenu(this);
    }

    String suffix() { return count>1?""+count:""; }
    
    /**
     * Configure the programming manager and "command station" objects
     * @param mCanRead
     * @param mProgPowersOff
     * @param name Command station type name
     */
    public void configureCommandStation(boolean mCanRead, boolean mProgPowersOff, String name) {

        // loconet.SlotManager to do programming (the Programmer instance is registered
        // when the SlotManager is created)
        jmri.jmrix.loconet.SlotManager.instance();
        // set slot manager's read capability
        jmri.jmrix.loconet.SlotManager.instance().setCanRead(mCanRead);
        jmri.jmrix.loconet.SlotManager.instance().setProgPowersOff(mProgPowersOff);
        jmri.jmrix.loconet.SlotManager.instance().setCommandStationType(name);
        
        // store as CommandStation object
        jmri.InstanceManager.setCommandStation(jmri.jmrix.loconet.SlotManager.instance());

    }

    /**
     * Configure the common managers for LocoNet connections.
     * This puts the common manager config in one
     * place.  This method is static so that it can be referenced
     * from classes that don't inherit, including hexfile.HexFileFrame
     * and locormi.LnMessageClient
     */
    public void configureManagers(LnTrafficController controller) {
    
        LocoNetThrottledTransmitter tm = new LocoNetThrottledTransmitter(controller);
        
        InstanceManager.setPowerManager(new jmri.jmrix.loconet.LnPowerManager());

        InstanceManager.setTurnoutManager(new jmri.jmrix.loconet.LnTurnoutManager(controller, tm, getSystemPrefix()));

        InstanceManager.setLightManager(new jmri.jmrix.loconet.LnLightManager());

        InstanceManager.setSensorManager(new jmri.jmrix.loconet.LnSensorManager());

        InstanceManager.setThrottleManager(new jmri.jmrix.loconet.LnThrottleManager());

        InstanceManager.setReporterManager(new jmri.jmrix.loconet.LnReporterManager());

        InstanceManager.addClockControl(new jmri.jmrix.loconet.LnClockControl());

    }
}


/* @(#)LocoNetSystemConnectionMemo.java */
