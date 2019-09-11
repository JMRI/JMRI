package jmri.jmrix.roco.z21;

import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.jmrix.lenz.XNetInitializationManager;
import jmri.jmrix.lenz.XNetLightManager;
import jmri.jmrix.lenz.XNetSensorManager;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs Command Station dependant initialization for the Roco
 * z21/Z21 XpressNet implementation. It adds the appropriate Managers via the
 * Initialization Manager based on the Command Station Type.
 *
 * @author	Paul Bender Copyright (C) 2015
 */
public class Z21XNetInitializationManager extends XNetInitializationManager {

    public Z21XNetInitializationManager(XNetSystemConnectionMemo memo) {
        super(memo);
    }

    @Override
    protected void init() {
        if (log.isDebugEnabled()) {
            log.debug("Init called");
        }
        InstanceManager.store(systemMemo.getPowerManager(), jmri.PowerManager.class);
        InstanceManager.setThrottleManager(systemMemo.getThrottleManager());
        systemMemo.setProgrammerManager(new Z21XNetProgrammerManager(new Z21XNetProgrammer(systemMemo.getXNetTrafficController()), systemMemo));
        if (systemMemo.getProgrammerManager().isAddressedModePossible()) {
            jmri.InstanceManager.store(systemMemo.getProgrammerManager(), jmri.AddressedProgrammerManager.class);
        }
        if (systemMemo.getProgrammerManager().isGlobalProgrammerAvailable()) {
            jmri.InstanceManager.store(systemMemo.getProgrammerManager(), GlobalProgrammerManager.class);
        }
        systemMemo.setTurnoutManager(new Z21XNetTurnoutManager(systemMemo));
        InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
        systemMemo.setLightManager(new XNetLightManager(systemMemo));
        InstanceManager.setLightManager(systemMemo.getLightManager());
        systemMemo.setSensorManager(new XNetSensorManager(systemMemo));
        InstanceManager.setSensorManager(systemMemo.getSensorManager());

        if (log.isDebugEnabled()) {
            log.debug("XpressNet Initialization Complete");
        }
    }

    private static final Logger log = LoggerFactory.getLogger(Z21XNetInitializationManager.class);

}
