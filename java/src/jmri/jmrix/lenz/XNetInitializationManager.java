package jmri.jmrix.lenz;

import jmri.GlobalProgrammerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs Command Station dependent initialization for XpressNet.
 * It adds the appropriate Managers via the Initialization Manager based on the
 * Command Station Type.
 *
 * @author Paul Bender Copyright (C) 2003-2010
 * @author Giorgio Terdina Copyright (C) 2007
 */
public class XNetInitializationManager extends AbstractXNetInitializationManager {

    public XNetInitializationManager(XNetSystemConnectionMemo memo) {
        super(memo);
    }

    @Override
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
            systemMemo.setProgrammerManager(new XNetProgrammerManager(new XNetProgrammer(systemMemo.getXNetTrafficController()), systemMemo));
            if (systemMemo.getProgrammerManager().isAddressedModePossible()) {
                jmri.InstanceManager.store(systemMemo.getProgrammerManager(), jmri.AddressedProgrammerManager.class);
            }
            if (systemMemo.getProgrammerManager().isGlobalProgrammerAvailable()) {
                jmri.InstanceManager.store(systemMemo.getProgrammerManager(), GlobalProgrammerManager.class);
            }
            /* the "raw" Command Station only works on systems that support
             Ops Mode Programming */
            systemMemo.setCommandStation(systemMemo.getXNetTrafficController().getCommandStation());
            jmri.InstanceManager.store(systemMemo.getCommandStation(), jmri.CommandStation.class);
            systemMemo.setConsistManager(new jmri.jmrix.lenz.XNetConsistManager(systemMemo));
            systemMemo.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager(systemMemo));
            jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
            systemMemo.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo));
            jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
            systemMemo.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager(systemMemo));
            jmri.InstanceManager.setSensorManager(systemMemo.getSensorManager());
        } else if (CSSoftwareVersion < 3.0) {
            log.error("Command Station does not support XpressNet Version 3 Command Set");
        } else {
            /* First, we load things that should work on all systems */
            jmri.InstanceManager.store(systemMemo.getPowerManager(), jmri.PowerManager.class);
            jmri.InstanceManager.setThrottleManager(systemMemo.getThrottleManager());
            /* Next we check the command station type, and add the
             apropriate managers */
            if (CSType == 0x02) {
                if (log.isDebugEnabled()) {
                    log.debug("Command Station is Compact/Commander/Other");
                }
                systemMemo.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager(systemMemo));
                jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
                systemMemo.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo));
                jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
                systemMemo.setConsistManager(new jmri.jmrix.lenz.XNetConsistManager(systemMemo));
            } else if (CSType == 0x01) {
                if (log.isDebugEnabled()) {
                    log.debug("Command Station is LH200");
                }
            } else if (CSType == 0x00) {
                if (log.isDebugEnabled()) {
                    log.debug("Command Station is LZ100/LZV100");
                }
                systemMemo.setProgrammerManager(new XNetProgrammerManager(new XNetProgrammer(systemMemo.getXNetTrafficController()), systemMemo));
                if (systemMemo.getProgrammerManager().isAddressedModePossible()) {
                    jmri.InstanceManager.store(systemMemo.getProgrammerManager(), jmri.AddressedProgrammerManager.class);
                }
                if (systemMemo.getProgrammerManager().isGlobalProgrammerAvailable()) {
                    jmri.InstanceManager.store(systemMemo.getProgrammerManager(), GlobalProgrammerManager.class);
                }
                /* the "raw" Command Station only works on systems that support
                 Ops Mode Programming */
                systemMemo.setCommandStation(systemMemo.getXNetTrafficController().getCommandStation());
                jmri.InstanceManager.store(systemMemo.getCommandStation(), jmri.CommandStation.class);
                systemMemo.getXNetTrafficController()
                        .getCommandStation()
                        .setTrafficController(systemMemo.getXNetTrafficController());
                systemMemo.setConsistManager(new jmri.jmrix.lenz.XNetConsistManager(systemMemo));
                systemMemo.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager(systemMemo));
                jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
                systemMemo.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo));
                jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
                systemMemo.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager(systemMemo));
                jmri.InstanceManager.setSensorManager(systemMemo.getSensorManager());
            } else if (CSType == 0x04) {
                if (log.isDebugEnabled()) {
                    log.debug("Command Station is LokMaus II");
                }
                systemMemo.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager(systemMemo));
                jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
                systemMemo.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo));
                jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
                systemMemo.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager(systemMemo));
                jmri.InstanceManager.setSensorManager(systemMemo.getSensorManager());
                systemMemo.setProgrammerManager(new XNetProgrammerManager(new XNetProgrammer(systemMemo.getXNetTrafficController()), systemMemo));
                if (systemMemo.getProgrammerManager().isAddressedModePossible()) {
                    jmri.InstanceManager.store(systemMemo.getProgrammerManager(), jmri.AddressedProgrammerManager.class);
                }
                if (systemMemo.getProgrammerManager().isGlobalProgrammerAvailable()) {
                    jmri.InstanceManager.store(systemMemo.getProgrammerManager(), GlobalProgrammerManager.class);
                }
                systemMemo.setCommandStation(systemMemo.getXNetTrafficController().getCommandStation());
                jmri.InstanceManager.store(systemMemo.getCommandStation(), jmri.CommandStation.class);
                // LokMaus does not support XpressNET consist commands. Let's the default consist manager be loaded.
            } else if (CSType == 0x10 ) {
                if (log.isDebugEnabled()) {
                    log.debug("Command Station is multiMaus");
                }
                systemMemo.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager(systemMemo));
                jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
                systemMemo.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo));
                jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
                systemMemo.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager(systemMemo));
                jmri.InstanceManager.setSensorManager(systemMemo.getSensorManager());
                systemMemo.setProgrammerManager(new XNetProgrammerManager(new XNetProgrammer(systemMemo.getXNetTrafficController()), systemMemo));
                if (systemMemo.getProgrammerManager().isAddressedModePossible()) {
                    jmri.InstanceManager.store(systemMemo.getProgrammerManager(), jmri.AddressedProgrammerManager.class);
                }
                if (systemMemo.getProgrammerManager().isGlobalProgrammerAvailable()) {
                    jmri.InstanceManager.store(systemMemo.getProgrammerManager(), GlobalProgrammerManager.class);
                }
                systemMemo.setCommandStation(systemMemo.getXNetTrafficController().getCommandStation());
                jmri.InstanceManager.store(systemMemo.getCommandStation(), jmri.CommandStation.class);
                // multMaus does not support XpressNET consist commands. Let's the default consist manager be loaded.
            } else {
                /* If we still don't  know what we have, load everything */
                if (log.isDebugEnabled()) {
                    log.debug("Command Station is Unknown type");
                }
                systemMemo.setProgrammerManager(new XNetProgrammerManager(new XNetProgrammer(systemMemo.getXNetTrafficController()), systemMemo));
                if (systemMemo.getProgrammerManager().isAddressedModePossible()) {
                    jmri.InstanceManager.store(systemMemo.getProgrammerManager(), jmri.AddressedProgrammerManager.class);
                }
                if (systemMemo.getProgrammerManager().isGlobalProgrammerAvailable()) {
                    jmri.InstanceManager.store(systemMemo.getProgrammerManager(), GlobalProgrammerManager.class);
                }
                /* the "raw" Command Station only works on systems that support
                 Ops Mode Programming */
                systemMemo.setCommandStation(systemMemo.getXNetTrafficController().getCommandStation());
                jmri.InstanceManager.store(systemMemo.getCommandStation(), jmri.CommandStation.class);
                systemMemo.setConsistManager(new jmri.jmrix.lenz.XNetConsistManager(systemMemo));
                systemMemo.setTurnoutManager(new jmri.jmrix.lenz.XNetTurnoutManager(systemMemo));
                jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
                systemMemo.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo));
                jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
                systemMemo.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager(systemMemo));
                jmri.InstanceManager.setSensorManager(systemMemo.getSensorManager());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("XpressNet Initialization Complete");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(XNetInitializationManager.class);

}
