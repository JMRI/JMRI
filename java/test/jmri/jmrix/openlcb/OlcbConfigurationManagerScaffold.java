package jmri.jmrix.openlcb;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;
import java.util.ResourceBundle;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import org.openlcb.Connection;
import org.openlcb.LoaderClient;
import org.openlcb.MessageDecoder;
import org.openlcb.MimicNodeStore;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.SimpleNodeIdentInfoReplyMessage;
import org.openlcb.SimpleNodeIdentInfoRequestMessage;
import org.openlcb.Version;
import org.openlcb.can.AliasMap;
import org.openlcb.can.CanInterface;
import org.openlcb.can.MessageBuilder;
import org.openlcb.can.OpenLcbCanFrame;
import org.openlcb.implementations.DatagramService;
import org.openlcb.implementations.MemoryConfigurationService;
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

        OlcbInterface iface = getInterface();
        loaderClient = new LoaderClient(iface.getOutputConnection(),
                iface.getMemoryConfigurationService(),
                iface.getDatagramService());
        iface.registerMessageListener(loaderClient);

        iface.registerMessageListener(new SimpleNodeIdentInfoHandler());

        aliasMap = new AliasMap();
        tc.addCanListener(new CanListener() {
            @Override
            public void message(CanMessage m) {
                if (!m.isExtended() || m.isRtr()) {
                    return;
                }
                aliasMap.processFrame(convertFromCan(m));
            }

            @Override
            public void reply(CanReply m) {
                if (!m.isExtended() || m.isRtr()) {
                    return;
                }
                aliasMap.processFrame(convertFromCan(m));
            }
        });
        messageBuilder = new MessageBuilder(aliasMap);
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbConfigurationManagerScaffold.class);
}
