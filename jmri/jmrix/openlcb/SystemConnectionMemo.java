// SystemConnectionMemo.java

package jmri.jmrix.openlcb;

import jmri.*;

/**
 * Lightweight class to denote that a system is active,
 * and provide general information.
 * <p>
 * Objects of specific subtypes are registered
 * in the instance manager to activate their
 * particular system.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision: 1.2 $
 */
public class SystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {
    
    public SystemConnectionMemo() {
        super("M", "OpenLCB");
        log.debug("SystemConnectionMemo ctor");
        register(); // registers general type
        InstanceManager.store(this, SystemConnectionMemo.class); // also register as specific type
        
        // create and register the ComponentFactory
        InstanceManager.store(cf = new jmri.jmrix.openlcb.swing.OpenLcbComponentFactory(this), 
                                jmri.jmrix.swing.ComponentFactory.class);
    }
    
    jmri.jmrix.swing.ComponentFactory cf = null;
    
    /**
     * Provides access to the SlotManager for this
     * particular connection.
     */
    //public SlotManager getSlotManager() { return sm; }
    //private SlotManager sm;
    //public void setSlotManager(SlotManager sm){ this.sm = sm;}
        
        
    /**
     * Configure the common managers.
     * This puts the common manager config in one
     * place.  
     */
    public void configureManagers() {

        log.debug("configureManagers");

        new org.openlcb.can.AliasMap();
    
//         LocoNetThrottledTransmitter tm = new LocoNetThrottledTransmitter(getLnTrafficController());
//         
//         InstanceManager.setPowerManager(
//             new jmri.jmrix.loconet.LnPowerManager(this));
// 
//         InstanceManager.setTurnoutManager(
//             new jmri.jmrix.loconet.LnTurnoutManager(getLnTrafficController(), tm, getSystemPrefix()));
// 
//         InstanceManager.setLightManager(
//             new jmri.jmrix.loconet.LnLightManager(getLnTrafficController(), getSystemPrefix()));
// 
//         InstanceManager.setSensorManager(
//             new jmri.jmrix.loconet.LnSensorManager(getLnTrafficController(),getSystemPrefix()));
// 
//         InstanceManager.setThrottleManager(
//             new jmri.jmrix.loconet.LnThrottleManager(getSlotManager()));
// 
//         jmri.InstanceManager.setProgrammerManager(
//             getProgrammerManager());
// 
//         InstanceManager.setReporterManager(
//             new jmri.jmrix.loconet.LnReporterManager(getLnTrafficController(), getSystemPrefix()));
// 
//         InstanceManager.addClockControl(
//             new jmri.jmrix.loconet.LnClockControl(getSlotManager(), getLnTrafficController()));

    }
    
    public void dispose() {
//         lt = null;
//         sm = null;
        InstanceManager.deregister(this, SystemConnectionMemo.class);
        if (cf != null) 
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        super.dispose();
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SystemConnectionMemo.class.getName());
}


/* @(#)SystemConnectionMemo.java */
