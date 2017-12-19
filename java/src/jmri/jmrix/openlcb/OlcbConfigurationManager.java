package jmri.jmrix.openlcb;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
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
 * Does configuration for OpenLCB communications implementations.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class OlcbConfigurationManager extends jmri.jmrix.can.ConfigurationManager {

    public OlcbConfigurationManager(CanSystemConnectionMemo memo) {
        super(memo);

        InstanceManager.store(cf = new jmri.jmrix.openlcb.swing.OpenLcbComponentFactory(adapterMemo),
                jmri.jmrix.swing.ComponentFactory.class);
        InstanceManager.store(this, OlcbConfigurationManager.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    @Override
    public void configureManagers() {

        // create our NodeID
        getOurNodeID();

        // do the connections
        tc = adapterMemo.getTrafficController();

        olcbCanInterface = createOlcbCanInterface(nodeID, tc);

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

        // start alias acquisition
        new StartUpHandler().start();

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

    CanInterface olcbCanInterface;
    TrafficController tc;
    NodeID nodeID;
    LoaderClient loaderClient;

    OlcbInterface getInterface() {
        return olcbCanInterface.getInterface();
    }

    // These components are internal implementation details of the OpenLCB library
    // and should not be exposed here.
    @Deprecated
    AliasMap aliasMap;
    @Deprecated
    MessageBuilder messageBuilder;

    /**
     * Check if a type of manager is provided by this manager.
     *
     * @param type the class of manager to check
     * @return true if the type of manager is provided; false otherwise
     */
    @Override
    public boolean provides(Class<?> type) {
        if (adapterMemo.getDisabled()) {
            return false;
        }
        if (type.equals(jmri.ThrottleManager.class)) {
            return true;
        }
        if (type.equals(jmri.SensorManager.class)) {
            return true;
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        }
        if (type.equals(AliasMap.class)) {
            return true;
        }
        if (type.equals(MessageBuilder.class)) {
            return true;
        }
        if (type.equals(MimicNodeStore.class)) {
            return true;
        }
        if (type.equals(Connection.class)) {
            return true;
        }
        if (type.equals(MemoryConfigurationService.class)) {
            return true;
        }
        if (type.equals(DatagramService.class)) {
            return true;
        }
        if (type.equals(NodeID.class)) {
            return true;
        }
        if (type.equals(OlcbInterface.class)) {
            return true;
        }
        if (type.equals(CanInterface.class)) {
            return true;
        }
        return false; // nothing, by default
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (T.equals(jmri.ThrottleManager.class)) {
            return (T) getThrottleManager();
        }
        if (T.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        if (T.equals(AliasMap.class)) {
            return (T) aliasMap;
        }
        if (T.equals(MessageBuilder.class)) {
            return (T) messageBuilder;
        }
        if (T.equals(MimicNodeStore.class)) {
            return (T) getInterface().getNodeStore();
        }
        if (T.equals(Connection.class)) {
            return (T) getInterface().getOutputConnection();
        }
        if (T.equals(MemoryConfigurationService.class)) {
            return (T) getInterface().getMemoryConfigurationService();
        }
        if (T.equals(DatagramService.class)) {
            return (T) getInterface().getDatagramService();
        }
        if (T.equals(LoaderClient.class)) {
            return (T) loaderClient;
        }
        if (T.equals(NodeID.class)) {
            return (T) nodeID;
        }
        if (T.equals(OlcbInterface.class)) {
            return (T) getInterface();
        }
        if (T.equals(CanInterface.class)) {
            return (T) olcbCanInterface;
        }
        return null; // nothing, by default
    }

    protected OlcbProgrammerManager programmerManager;

    public OlcbProgrammerManager getProgrammerManager() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (programmerManager == null) {
            programmerManager = new OlcbProgrammerManager(new OlcbProgrammer());
        }
        return programmerManager;
    }

    protected OlcbThrottleManager throttleManager;

    public OlcbThrottleManager getThrottleManager() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (throttleManager == null) {
            throttleManager = new OlcbThrottleManager(adapterMemo, this);
        }
        return throttleManager;
    }

    protected OlcbTurnoutManager turnoutManager;

    public OlcbTurnoutManager getTurnoutManager() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (turnoutManager == null) {
            turnoutManager = new OlcbTurnoutManager(adapterMemo);
        }
        return turnoutManager;
    }

    protected OlcbSensorManager sensorManager;

    public OlcbSensorManager getSensorManager() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (sensorManager == null) {
            sensorManager = new OlcbSensorManager(adapterMemo);
        }
        return sensorManager;
    }

    @Override
    public void dispose() {
        if (turnoutManager != null) {
            InstanceManager.deregister(turnoutManager, jmri.jmrix.openlcb.OlcbTurnoutManager.class);
        }
        if (sensorManager != null) {
            InstanceManager.deregister(sensorManager, jmri.jmrix.openlcb.OlcbSensorManager.class);
        }
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        InstanceManager.deregister(this, OlcbConfigurationManager.class);
    }

    class SimpleNodeIdentInfoHandler extends MessageDecoder {

        SimpleNodeIdentInfoHandler() {
            byte[] part1 = new byte[]{1, 'J', 'M', 'R', 'I', 0, 'P', 'a', 'n', 'e', 'l', 'P', 'r', 'o', 0}; // NOI18N
            byte[] part2 = new byte[]{0};
            byte[] part3;
            try {
                part3 = jmri.Version.name().getBytes("UTF-8");  // OpenLCB is UTF-8           // NOI18N
            } catch (java.io.UnsupportedEncodingException e) {
                log.error("Cannot proceed if UTF-8 not supported?");
                part3 = new byte[]{'?'};                                                      // NOI18N
            }
            byte[] part4 = new byte[]{0, ' ', 0, ' ', 0};                                         // NOI18N
            content = new byte[part1.length + part2.length + part3.length + part4.length];
            int i = 0;
            for (int j = 0; j < part1.length; j++) {
                content[i++] = part1[j];
            }
            for (int j = 0; j < part2.length; j++) {
                content[i++] = part2[j];
            }
            for (int j = 0; j < part3.length; j++) {
                content[i++] = part3[j];
            }
            for (int j = 0; j < part4.length; j++) {
                content[i++] = part4[j];
            }
        }
        private final byte[] content;

        @Override
        public void handleSimpleNodeIdentInfoRequest(SimpleNodeIdentInfoRequestMessage msg,
                Connection sender) {
            if (msg.getDestNodeID().equals(nodeID)) {
                // Sending a SNIP reply to the bus crashes the library up to 0.7.7.
                if (msg.getSourceNodeID().equals(nodeID) || Version.libVersionAtLeast(0, 7, 8)) {
                    getInterface().getOutputConnection().put(new SimpleNodeIdentInfoReplyMessage(nodeID, msg.getSourceNodeID(), content), this);
                }
            }
        }
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.openlcb.OlcbActionListBundle");
    }

    /**
     * Create a node ID in the JMRI range from one byte of IP address, and 2
     * bytes of PID. That changes each time, which isn't perhaps what's wanted.
     */
    protected void getOurNodeID() {
        long pid = getProcessId(1);
        log.debug("Process ID: {}", pid);

        // get first network interface internet address
        // almost certainly the wrong approach, isn't likely to
        // find real IP address for coms, but it gets some entropy.
        InetAddress address = null;
        try {
            NetworkInterface n = NetworkInterface.getNetworkInterfaces().nextElement();
            if (n != null) {
                address = n.getInetAddresses().nextElement();
            }
        } catch (SocketException e) {
            log.warn("Can't get IP address to make NodeID", e);
        }
        log.debug("InetAddress: {}", address);
        int b1 = 0;
        if (address != null) {
            b1 = address.getAddress()[0];
        }

        // store new NodeID
        nodeID = new NodeID(new byte[]{2, 1, 18, (byte) (b1 & 0xFF), (byte) ((pid >> 8) & 0xFF), (byte) (pid & 0xFF)});
        log.debug("Node ID: {}", nodeID);
    }

    protected long getProcessId(final long fallback) {
        // Note: may fail in some JVM implementations
        // therefore fallback has to be provided

        // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
        final String jvmName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');

        if (index < 1) {
            // part before '@' empty (index = 0) / '@' not found (index = -1)
            return fallback;
        }

        try {
            return Long.parseLong(jvmName.substring(0, index));
        } catch (NumberFormatException e) {
            // ignore
        }
        return fallback;
    }

    public static CanInterface createOlcbCanInterface(NodeID nodeID, TrafficController tc) {
        final CanInterface olcbIf = new CanInterface(nodeID, frame -> tc.sendCanMessage(convertToCan(frame), null));
        tc.addCanListener(new CanListener() {
            @Override
            public void message(CanMessage m) {
                // ignored -- loopback is handled by the olcbInterface.
            }

            @Override
            public void reply(CanReply m) {
                if (!m.isExtended() || m.isRtr()) {
                    return;
                }
                olcbIf.frameInput().send(convertFromCan(m));
            }
        });
        return olcbIf;
    }

    static jmri.jmrix.can.CanMessage convertToCan(org.openlcb.can.CanFrame f) {
        jmri.jmrix.can.CanMessage fout = new jmri.jmrix.can.CanMessage(f.getData(), f.getHeader());
        fout.setExtended(true);
        return fout;
    }

    static OpenLcbCanFrame convertFromCan(jmri.jmrix.can.CanFrame message) {
        OpenLcbCanFrame fin = new OpenLcbCanFrame(0);
        fin.setHeader(message.getHeader());
        if (message.getNumDataElements() == 0) {
            return fin;
        }
        byte[] data = new byte[message.getNumDataElements()];
        for (int i = 0; i < data.length; ++i) {
            data[i] = (byte) (message.getElement(i) & 0xff);
        }
        fin.setData(data);
        return fin;
    }

    /**
     * State machine to handle startup
     */
    class StartUpHandler {

        javax.swing.Timer timer;

        static final int START_DELAY = 2500;

        void start() {
            log.debug("StartUpHandler starts up");
            // wait geological time for adapter startup
            timer = new javax.swing.Timer(START_DELAY, new javax.swing.AbstractAction() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    new Thread(() -> {
                        olcbCanInterface.initialize();
                    }, "olcbCanInterface.initialize").start();
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbConfigurationManager.class);
}
