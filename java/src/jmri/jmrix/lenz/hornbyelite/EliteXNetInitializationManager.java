package jmri.jmrix.lenz.hornbyelite;

import jmri.GlobalProgrammerManager;
import jmri.jmrix.lenz.AbstractXNetInitializationManager;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs Command Station dependant initialization for the Hornby
 * Elite. It adds the appropriate Managers via the Initialization Manager based
 * on the Command Station Type.
 *
 * @author Paul Bender Copyright (C) 2003,2008
 */
public class EliteXNetInitializationManager extends AbstractXNetInitializationManager {

    public EliteXNetInitializationManager(XNetSystemConnectionMemo memo) {
        super(memo);
    }

    @Override
    protected void init() {
        log.debug("Init called");

        /* First, we load things that should work on all systems */
        jmri.InstanceManager.store(systemMemo.getPowerManager(), jmri.PowerManager.class);
        systemMemo.setThrottleManager(new jmri.jmrix.lenz.hornbyelite.EliteXNetThrottleManager(systemMemo));
        jmri.InstanceManager.setThrottleManager(systemMemo.getThrottleManager());

        if (log.isDebugEnabled()) {
            log.debug("Command Station is Hornby Elite (manually identified).");
        }
        systemMemo.setTurnoutManager(new jmri.jmrix.lenz.hornbyelite.EliteXNetTurnoutManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
        jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());

        systemMemo.setCommandStation(systemMemo.getXNetTrafficController().getCommandStation());
        jmri.InstanceManager.setCommandStation(systemMemo.getCommandStation());

        systemMemo.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
        jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
        systemMemo.setProgrammerManager(new jmri.jmrix.lenz.XNetProgrammerManager(new jmri.jmrix.lenz.hornbyelite.EliteXNetProgrammer(systemMemo.getXNetTrafficController()), systemMemo));
        if (systemMemo.getProgrammerManager().isAddressedModePossible()) {
            jmri.InstanceManager.setAddressedProgrammerManager(systemMemo.getProgrammerManager());
        }
        if (systemMemo.getProgrammerManager().isGlobalProgrammerAvailable()) {
            jmri.InstanceManager.store(systemMemo.getProgrammerManager(), GlobalProgrammerManager.class);
        }

        log.debug("XpressNet Initialization Complete");
    }

    private final static Logger log = LoggerFactory.getLogger(EliteXNetInitializationManager.class);

}
