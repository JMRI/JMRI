package jmri.jmrix.dccpp;

import jmri.GlobalProgrammerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs Command Station dependant initialization for DCC++. It
 * adds the appropriate Managers via the Initialization Manager based on the
 * Command Station Type.
 *
 * @author Paul Bender Copyright (C) 2003-2010
 * @author Giorgio Terdina Copyright (C) 2007
 * @author Mark Underwood Copyright (C) 2015
 * @author Harald Barth Copyright (C) 2019
 *
 * Based on XNetInitializationManager by Paul Bender and Giorgio Terdina
 */
public class DCCppInitializationManager extends AbstractDCCppInitializationManager {

    public DCCppInitializationManager(DCCppSystemConnectionMemo memo) {
        super(memo);
    }

    @Override
    protected void init() {
	log.debug("DCCppInitializationManager init() called");

        String base_station = "Unknown";
        String code_build = "Unknown";

        if (systemMemo.getDCCppTrafficController().getCommandStation() != null) {
            base_station = systemMemo.getDCCppTrafficController().getCommandStation().getBaseStationType();
        }
        if (systemMemo.getDCCppTrafficController().getCommandStation() != null) {
            code_build = systemMemo.getDCCppTrafficController().getCommandStation().getCodeBuildDate();
        }

        /* First, we load things that should work on all systems */
        jmri.InstanceManager.store(systemMemo.getPowerManager(), jmri.PowerManager.class);
        log.debug("PowerManager: {}", jmri.InstanceManager.getDefault(jmri.PowerManager.class));

        jmri.InstanceManager.setThrottleManager(systemMemo.getThrottleManager());

        /* Next we check the command station type, and add the
           apropriate managers */

        /* If we still don't  know what we have, load everything */
        log.debug("Command Station is type {} build {}", base_station, code_build);

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
        systemMemo.setMultiMeter(new DCCppMultiMeter(systemMemo));
        jmri.InstanceManager.store(systemMemo.getMultiMeter(), jmri.MultiMeter.class);

        log.debug("DCC++ Initialization Complete");
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppInitializationManager.class);

}
