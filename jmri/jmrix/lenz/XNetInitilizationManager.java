// XNetInitilizationManager.java

package jmri.jmrix.lenz;

/**
 * This class performs Command Station dependant initilization for 
 * XPressNet.  
 * It adds the appropriate Managers via the Initilization Manager
 * based on the Command Station Type.
 *
 * @author			Paul Bender  Copyright (C) 2003-2010
 * @author			Giorgio Terdina  Copyright (C) 2007
 * @version			$Revision: 2.15 $
 */
public class XNetInitilizationManager extends AbstractXNetInitilizationManager{

    public XNetInitilizationManager(XNetSystemConnectionMemo memo){
      super(memo);
    }

    protected void init() {
	if (log.isDebugEnabled()) log.debug("Init called");
        float CSSoftwareVersion = systemMemo.getXNetTrafficController()
            .getCommandStation()
            .getCommandStationSoftwareVersion();
        int CSType = systemMemo.getXNetTrafficController()
            .getCommandStation()
            .getCommandStationType();
        
        if (CSSoftwareVersion<0)
            {
                log.warn("Command Station disconnected, or powered down assuming LZ100/LZV100 V3.x");
                jmri.InstanceManager.setPowerManager(new jmri.jmrix.lenz.XNetPowerManager(systemMemo));
                jmri.InstanceManager.setThrottleManager(new jmri.jmrix.lenz.XNetThrottleManager(systemMemo));
                jmri.InstanceManager.setProgrammerManager(systemMemo.getProgrammerManager());
                /* the "raw" Command Station only works on systems that support   
                   Ops Mode Programming */
                 jmri.InstanceManager.setCommandStation(systemMemo.getXNetTrafficController().getCommandStation());
                 systemMemo.getXNetTrafficController()
                           .getCommandStation()
                           .setTrafficController(systemMemo.getXNetTrafficController());
                /* the consist manager has to be set up AFTER the programmer, to 
                   prevent the default consist manager from being loaded on top of it */
                jmri.InstanceManager.setConsistManager(new jmri.jmrix.lenz.XNetConsistManager(systemMemo));
                jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
                jmri.InstanceManager.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
                jmri.InstanceManager.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
            } else if (CSSoftwareVersion<3.0) {
                log.error("Command Station does not support XPressNet Version 3 Command Set");
            } else {
                /* First, we load things that should work on all systems */
                jmri.InstanceManager.setPowerManager(new jmri.jmrix.lenz.XNetPowerManager(systemMemo));
                jmri.InstanceManager.setThrottleManager(new jmri.jmrix.lenz.XNetThrottleManager(systemMemo));
                
                /* Next we check the command station type, and add the 
                   apropriate managers */
                if (CSType==0x02) {
                    if (log.isDebugEnabled()) log.debug("Command Station is Commpact/Commander/Other");
                    jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
                    jmri.InstanceManager.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
                    /* the consist manager has to be set up AFTER the programmer, to 
                       prevent the default consist manager from being loaded on top of it */
                    jmri.InstanceManager.setConsistManager(new jmri.jmrix.lenz.XNetConsistManager(systemMemo));
                } else if (CSType==0x01) {
                    if (log.isDebugEnabled()) log.debug("Command Station is LH200");
                } else if (CSType==0x00) {
                    if (log.isDebugEnabled()) log.debug("Command Station is LZ100/LZV100");
                    jmri.InstanceManager.setProgrammerManager(systemMemo.getProgrammerManager());
                    /* the "raw" Command Station only works on systems that support   
                       Ops Mode Programming */
                    jmri.InstanceManager.setCommandStation(systemMemo.getXNetTrafficController()
                                                           .getCommandStation());
                    systemMemo.getXNetTrafficController()
                           .getCommandStation()
                           .setTrafficController(systemMemo.getXNetTrafficController());
                    /* the consist manager has to be set up AFTER the programmer, to 
                       prevent the default consist manager from being loaded on top of it */
                    jmri.InstanceManager.setConsistManager(new jmri.jmrix.lenz.XNetConsistManager(systemMemo));
                    jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
                    jmri.InstanceManager.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
                    jmri.InstanceManager.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
                } else if (CSType==0x10) {
                    if (log.isDebugEnabled()) log.debug("Command Station is multiMaus");
                    jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
                    jmri.InstanceManager.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
                    jmri.InstanceManager.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
                    jmri.InstanceManager.setProgrammerManager(systemMemo.getProgrammerManager());
                    jmri.InstanceManager.setCommandStation(systemMemo.getXNetTrafficController()
                                                           .getCommandStation());
                    // multMaus does not support XpressNET consist commands. Let's the default consist manager be loaded.
                } else {
                    /* If we still don't  know what we have, load everything */
                    if (log.isDebugEnabled()) log.debug("Command Station is Unknown type");
                    jmri.InstanceManager.setProgrammerManager(systemMemo.getProgrammerManager());
                    /* the "raw" Command Station only works on systems that support   
                       Ops Mode Programming */
                    jmri.InstanceManager.setCommandStation(systemMemo.getXNetTrafficController()
                                                           .getCommandStation());
                    systemMemo.getXNetTrafficController()
                           .getCommandStation()
                           .setTrafficController(systemMemo.getXNetTrafficController());
                    /* the consist manager has to be set up AFTER the programmer, to 
                       prevent the default consist manager from being loaded on top of it */
                    jmri.InstanceManager.setConsistManager(new jmri.jmrix.lenz.XNetConsistManager(systemMemo));
                    jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
                    jmri.InstanceManager.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
                    jmri.InstanceManager.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager(systemMemo.getXNetTrafficController(),systemMemo.getSystemPrefix()));
                }
            }
	if (log.isDebugEnabled()) log.debug("XPressNet Initilization Complete");
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XNetInitilizationManager.class.getName());
    
}
