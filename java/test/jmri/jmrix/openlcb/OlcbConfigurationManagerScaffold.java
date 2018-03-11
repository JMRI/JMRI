package jmri.jmrix.openlcb;

import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
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
            public void initialize(){
            }
        };

        // create JMRI objects
        InstanceManager.setSensorManager(
                getSensorManager());

        InstanceManager.setTurnoutManager(
                getTurnoutManager());

        InstanceManager.setThrottleManager(
                getThrottleManager());

        if (getProgrammerManager().isAddressedModePossible()) {
            InstanceManager.setAddressedProgrammerManager(getProgrammerManager());
        }
        if (getProgrammerManager().isGlobalProgrammerAvailable()) {
            jmri.InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        }
    }

    // private final static Logger log = LoggerFactory.getLogger(OlcbConfigurationManagerScaffold.class);
}
