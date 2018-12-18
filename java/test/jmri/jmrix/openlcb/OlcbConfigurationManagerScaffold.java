package jmri.jmrix.openlcb;

import jmri.*;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.util.ThreadingUtil;

import org.openlcb.can.CanInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Does test configuration for OpenLCB communications implementations.
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class OlcbConfigurationManagerScaffold extends jmri.jmrix.openlcb.OlcbConfigurationManager {

    public OlcbConfigurationManagerScaffold(CanSystemConnectionMemo memo) {
        super(memo);
    }

    @Override
    public void configureManagers() {

        // create our NodeID
        getOurNodeID();

        // do the connections
        tc = adapterMemo.getTrafficController();

        olcbCanInterface = new CanInterface(nodeID, frame -> tc.sendCanMessage(convertToCan(frame),null)){
            @Override
            public void initialize() {
                // Purposefully do not call the super implementation here in order to avoid
                // running the alias allocation state machine.
                initialized = true;
            }
        };
        olcbCanInterface.getInterface().setLoopbackThread((Runnable r)-> ThreadingUtil.runOnLayout(r::run));

        // create JMRI objects
        InstanceManager.setSensorManager(
                getSensorManager());

        InstanceManager.setTurnoutManager(
                getTurnoutManager());

        InstanceManager.setThrottleManager(
                getThrottleManager());

        if (getProgrammerManager().isAddressedModePossible()) {
            InstanceManager.store(getProgrammerManager(), AddressedProgrammerManager.class);
        }
        if (getProgrammerManager().isGlobalProgrammerAvailable()) {
            jmri.InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        }
    }

    // private final static Logger log = LoggerFactory.getLogger(OlcbConfigurationManagerScaffold.class);
}
