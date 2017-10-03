package jmri.jmrix.ztc.ztc611;

import jmri.GlobalProgrammerManager;
import jmri.jmrix.lenz.AbstractXNetInitializationManager;
import jmri.jmrix.lenz.XNetProgrammer;
import jmri.jmrix.lenz.XNetProgrammerManager;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs Command Station dependant initilization for The ZTC
 * ZTC611. It adds the appropriate Managers via the Instance Manager based
 * on the Command Station Type.
 *
 * @author Paul Bender Copyright (C) 2003,2008
  */
public class ZTC611XNetInitializationManager extends AbstractXNetInitializationManager {

    public ZTC611XNetInitializationManager(XNetSystemConnectionMemo memo) {
        super(memo);
    }

    @Override
    protected void init() {
        if (log.isDebugEnabled()) {
            log.debug("Init called");
        }

        if (log.isDebugEnabled()) {
            log.debug("Command Station is ZTC ZTC611 (manually identified).");
        }

        /* First, we load things that should work on all systems */

        jmri.InstanceManager.store(systemMemo.getPowerManager(), jmri.PowerManager.class);
        jmri.InstanceManager.setThrottleManager(systemMemo.getThrottleManager());
        systemMemo.setProgrammerManager(new XNetProgrammerManager(new XNetProgrammer(systemMemo.getXNetTrafficController()), systemMemo));
        if (systemMemo.getProgrammerManager().isAddressedModePossible()) {
            jmri.InstanceManager.setAddressedProgrammerManager(systemMemo.getProgrammerManager());
        }
        if (systemMemo.getProgrammerManager().isGlobalProgrammerAvailable()) {
            jmri.InstanceManager.store(systemMemo.getProgrammerManager(), GlobalProgrammerManager.class);
        }
        /* the "raw" Command Station only works on systems that support
        Ops Mode Programming */
        systemMemo.setCommandStation(systemMemo.getXNetTrafficController().getCommandStation());
        jmri.InstanceManager.setCommandStation(systemMemo.getCommandStation());
        /* the consist manager has to be set up AFTER the programmer, to
        prevent the default consist manager from being loaded on top of it */
        systemMemo.setConsistManager(new jmri.jmrix.lenz.XNetConsistManager(systemMemo));
        jmri.InstanceManager.setConsistManager(systemMemo.getConsistManager());
        systemMemo.setTurnoutManager(new ZTC611XNetTurnoutManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
        jmri.InstanceManager.setTurnoutManager(systemMemo.getTurnoutManager());
        systemMemo.setLightManager(new jmri.jmrix.lenz.XNetLightManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
        jmri.InstanceManager.setLightManager(systemMemo.getLightManager());
        systemMemo.setSensorManager(new jmri.jmrix.lenz.XNetSensorManager(systemMemo.getXNetTrafficController(), systemMemo.getSystemPrefix()));
        jmri.InstanceManager.setSensorManager(systemMemo.getSensorManager());

        if (log.isDebugEnabled()) {
            log.debug("XpressNet Initialization Complete");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ZTC611XNetInitializationManager.class);

}
