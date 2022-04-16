package jmri.jmrix.dccpp;

import jmri.GlobalProgrammerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs initialization for DCC++. It
 * adds the appropriate Managers via the Initialization Manager.
 *
 * @author Paul Bender Copyright (C) 2003-2010
 * @author Giorgio Terdina Copyright (C) 2007
 * @author Mark Underwood Copyright (C) 2015
 * @author Harald Barth Copyright (C) 2019
 *
 * Based on XNetInitializationManager by Paul Bender and Giorgio Terdina
 */
public class DCCppInitializationManager {

    protected DCCppSystemConnectionMemo systemMemo = null;

    DCCppPredefinedMeters predefinedMeters;
    
    public DCCppInitializationManager(DCCppSystemConnectionMemo memo) {

        systemMemo = memo;

        log.debug("Starting DCC++ Initialization Process");

        DCCppCommandStation cs = systemMemo.getDCCppTrafficController().getCommandStation();
        jmri.InstanceManager.setThrottleManager(systemMemo.getThrottleManager());
        systemMemo.setProgrammerManager(new DCCppProgrammerManager(new DCCppProgrammer(systemMemo.getDCCppTrafficController()), systemMemo));
        if (systemMemo.getProgrammerManager().isAddressedModePossible()) {
            jmri.InstanceManager.store(systemMemo.getProgrammerManager(), jmri.AddressedProgrammerManager.class);
        }
        if (systemMemo.getProgrammerManager().isGlobalProgrammerAvailable()) {
            jmri.InstanceManager.store(systemMemo.getProgrammerManager(), GlobalProgrammerManager.class);
        }
        systemMemo.setCommandStation(systemMemo.getDCCppTrafficController().getCommandStation());
        jmri.InstanceManager.store(systemMemo.getCommandStation(), jmri.CommandStation.class);
        systemMemo.setTurnoutManager(new jmri.jmrix.dccpp.DCCppTurnoutManager(systemMemo));
        jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
        systemMemo.setLightManager(new jmri.jmrix.dccpp.DCCppLightManager(systemMemo));
        jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
        systemMemo.setSensorManager(new jmri.jmrix.dccpp.DCCppSensorManager(systemMemo));
        jmri.InstanceManager.setSensorManager(systemMemo.getSensorManager());
        jmri.InstanceManager.store(systemMemo.getPowerManager(), jmri.PowerManager.class);
        log.debug("PowerManager: {}", jmri.InstanceManager.getDefault(jmri.PowerManager.class));
        predefinedMeters = new DCCppPredefinedMeters(systemMemo);

        systemMemo.register();

        String base_station = "Unknown";
        String code_build   = "Unknown";
        String version      = "Unknown";
        if (cs != null) {
            base_station    = cs.getStationType();
            code_build      = cs.getBuild();
            version         = cs.getVersion();
        }
        
        log.info("DCC++ Initialization Complete with station type '{}', version '{}' and build '{}'", base_station, version, code_build);
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppInitializationManager.class);

}
