package jmri.jmrix.ztc.ztc611;

import jmri.GlobalProgrammerManager;
import jmri.jmrix.lenz.AbstractXNetInitializationManager;
import jmri.jmrix.lenz.XNetProgrammer;
import jmri.jmrix.lenz.XNetProgrammerManager;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs Command Station dependant initialization for the ZTC
 * ZTC611. It adds the appropriate Managers via the Instance Manager based
 * on the Command Station Type.
 *
 * @author Paul Bender Copyright (C) 2003, 2008
 */
public class ZTC611XNetInitializationManager extends AbstractXNetInitializationManager {

    public ZTC611XNetInitializationManager(XNetSystemConnectionMemo memo) {
        super(memo);
    }

    @Override
    protected void init() {
        log.debug("Init called");
        log.debug("Command Station is ZTC ZTC611 (manually identified).");

        /* First, we load things that should work on all systems */

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
        systemMemo.setTurnoutManager(new ZTC611XNetTurnoutManager(systemMemo));
        jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
        systemMemo.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo));
        jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
        systemMemo.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager(systemMemo));
        jmri.InstanceManager.setSensorManager(systemMemo.getSensorManager());

        log.debug("XpressNet Initialization Complete");
    }

    private final static Logger log = LoggerFactory.getLogger(ZTC611XNetInitializationManager.class);

}
