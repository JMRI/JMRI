// LI100XNetInitializationManager.java
package jmri.jmrix.lenz.li100;

import jmri.jmrix.lenz.AbstractXNetInitializationManager;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs Command Station dependant initilization for XPressNet. It
 * adds the appropriate Managers via the Initialization Manager based on the
 * Command Station Type.
 *
 * @author	Paul Bender Copyright (C) 2003
 * @version	$Revision$
 */
public class LI100XNetInitializationManager extends AbstractXNetInitializationManager {

    public LI100XNetInitializationManager(XNetSystemConnectionMemo memo) {
        super(memo);
    }

    protected void init() {
        if (log.isDebugEnabled()) {
            log.debug("Init called");
        }
        float CSSoftwareVersion = systemMemo.getXNetTrafficController()
                .getCommandStation()
                .getCommandStationSoftwareVersion();
        int CSType = systemMemo.getXNetTrafficController()
                .getCommandStation()
                .getCommandStationType();

        if (CSSoftwareVersion < 0) {
            log.warn("Command Station disconnected, or powered down assuming LZ100/LZV100 V3.x");
            jmri.InstanceManager.store(systemMemo.getPowerManager(), jmri.PowerManager.class);
            jmri.InstanceManager.setThrottleManager(systemMemo.getThrottleManager());
            systemMemo.setProgrammerManager(new jmri.jmrix.lenz.XNetProgrammerManager(new jmri.jmrix.lenz.li100.LI100XNetProgrammer(systemMemo.getXNetTrafficController()), systemMemo));
            jmri.InstanceManager.setProgrammerManager(systemMemo.getProgrammerManager());
            /* the "raw" Command Station only works on systems that support   
             Ops Mode Programming */
            /* systemMemo.setCommandStation(systemMemo.getXNetTrafficController()
             jmri.InstanceManager.setCommandStation(systemMemo.getCommandStation());
             */
            /* the consist manager has to be set up AFTER the programmer, to 
             prevent the default consist manager from being loaded on top of it */

            systemMemo.setConsistManager(new jmri.jmrix.lenz.XNetConsistManager(systemMemo));
            jmri.InstanceManager.setConsistManager(systemMemo.getConsistManager());
            systemMemo.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
            jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
            systemMemo.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
            jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
            systemMemo.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
            jmri.InstanceManager.setSensorManager(systemMemo.getSensorManager());

        } else if (CSSoftwareVersion < 3.0) {
            log.error("Command Station does not support XPressNet Version 3 Command Set");
        } else {
            /* First, we load things that should work on all systems */
            jmri.InstanceManager.store(systemMemo.getPowerManager(), jmri.PowerManager.class);
            jmri.InstanceManager.setThrottleManager(systemMemo.getThrottleManager());

            /* Next we check the command station type, and add the 
             apropriate managers */
            if (CSType == 0x02) {
                if (log.isDebugEnabled()) {
                    log.debug("Command Station is Commpact/Commander/Other");
                }
                systemMemo.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
                jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
                systemMemo.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
                jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
                /* the consist manager has to be set up AFTER the programmer, to 
                 prevent the default consist manager from being loaded on top of it */
                systemMemo.setConsistManager(new jmri.jmrix.lenz.XNetConsistManager(systemMemo));
                jmri.InstanceManager.setConsistManager(systemMemo.getConsistManager());
            } else if (CSType == 0x01) {
                if (log.isDebugEnabled()) {
                    log.debug("Command Station is LH200");
                }
            } else if (CSType == 0x00) {
                if (log.isDebugEnabled()) {
                    log.debug("Command Station is LZ100/LZV100");
                }
                systemMemo.setProgrammerManager(new jmri.jmrix.lenz.XNetProgrammerManager(new jmri.jmrix.lenz.li100.LI100XNetProgrammer(systemMemo.getXNetTrafficController()), systemMemo));
                jmri.InstanceManager.setProgrammerManager(systemMemo.getProgrammerManager());
                /* the "raw" Command Station only works on systems that support   
                 Ops Mode Programming */
                systemMemo.setCommandStation(systemMemo.getXNetTrafficController().getCommandStation());
                jmri.InstanceManager.setCommandStation(systemMemo.getCommandStation());
                /* the consist manager has to be set up AFTER the programmer, to 
                 prevent the default consist manager from being loaded on top of it */
                systemMemo.setConsistManager(new jmri.jmrix.lenz.XNetConsistManager(systemMemo));
                jmri.InstanceManager.setConsistManager(systemMemo.getConsistManager());
                systemMemo.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
                jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
                systemMemo.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
                jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
                systemMemo.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
                jmri.InstanceManager.setSensorManager(systemMemo.getSensorManager());
            } else if (CSType == 0x10) {
                if (log.isDebugEnabled()) {
                    log.debug("Command Station is multiMaus");
                }
                systemMemo.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
                jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
                systemMemo.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
                jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
                systemMemo.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
                jmri.InstanceManager.setSensorManager(systemMemo.getSensorManager());
                systemMemo.setProgrammerManager(new jmri.jmrix.lenz.XNetProgrammerManager(new jmri.jmrix.lenz.li100.LI100XNetProgrammer(systemMemo.getXNetTrafficController()), systemMemo));
                jmri.InstanceManager.setProgrammerManager(systemMemo.getProgrammerManager());
                systemMemo.setCommandStation(systemMemo.getXNetTrafficController().getCommandStation());
                jmri.InstanceManager.setCommandStation(systemMemo.getCommandStation());
                // multMaus does not support XpressNET consist commands. Let's the default consist manager be loaded.
            } else {
                /* If we still don't  know what we have, load everything */
                if (log.isDebugEnabled()) {
                    log.debug("Command Station is Unknown type");
                }
                systemMemo.setProgrammerManager(new jmri.jmrix.lenz.XNetProgrammerManager(new jmri.jmrix.lenz.li100.LI100XNetProgrammer(systemMemo.getXNetTrafficController()), systemMemo));
                jmri.InstanceManager.setProgrammerManager(systemMemo.getProgrammerManager());
                /* the "raw" Command Station only works on systems that support   
                 Ops Mode Programming */
                systemMemo.setCommandStation(systemMemo.getXNetTrafficController().getCommandStation());
                jmri.InstanceManager.setCommandStation(systemMemo.getCommandStation());

                /* the consist manager has to be set up AFTER the programmer, to 
                 prevent the default consist manager from being loaded on top of it */
                systemMemo.setConsistManager(new jmri.jmrix.lenz.XNetConsistManager(systemMemo));
                jmri.InstanceManager.setConsistManager(systemMemo.getConsistManager());
                systemMemo.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
                jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
                systemMemo.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
                jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
                systemMemo.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
                jmri.InstanceManager.setSensorManager(systemMemo.getSensorManager());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("XPressNet Initialization Complete");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LI100XNetInitializationManager.class.getName());

}
