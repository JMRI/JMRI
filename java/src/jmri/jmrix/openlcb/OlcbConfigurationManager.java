package jmri.jmrix.openlcb;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.time.Clock;
import java.util.List;
import java.util.ResourceBundle;

import jmri.ClockControl;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.profile.ProfileManager;
import jmri.util.ThreadingUtil;

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
import org.openlcb.protocols.TimeProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Does configuration for OpenLCB communications implementations.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class OlcbConfigurationManager extends jmri.jmrix.can.ConfigurationManager {

    // Constants for the protocol options keys. These option keys are used to save configuration
    // in the profile.xml and set on a per-connection basis in the connection preferences.

    // Protocol key for node identification
    public static final String OPT_PROTOCOL_IDENT = "Ident";

    // Option key for Node ID
    public static final String OPT_IDENT_NODEID = "NodeId";
    // Option key for User Name, used for the Simple Node Ident Protocol
    public static final String OPT_IDENT_USERNAME = "UserName";
    // Option key for User Description, used for the Simple Node Ident Protocol
    public static final String OPT_IDENT_DESCRIPTION = "UserDescription";

    // Protocol key for fast clock
    public static final String OPT_PROTOCOL_FASTCLOCK = "FastClock";

    // Option key for fast clock mode
    public static final String OPT_FASTCLOCK_ENABLE = "EnableMode";
    // Option value for setting fast clock to disabled.
    public static final String OPT_FASTCLOCK_ENABLE_OFF = "disabled";
    // Option value for setting fast clock to clock generator/producer/master.
    public static final String OPT_FASTCLOCK_ENABLE_GENERATOR = "generator";
    // Option value for setting fast clock to clock consumer/slave.
    public static final String OPT_FASTCLOCK_ENABLE_CONSUMER = "consumer";

    // Option key for setting the clock identifier.
    public static final String OPT_FASTCLOCK_ID = "ClockId";
    // Option value for using the well-known clock id "default clock"
    public static final String OPT_FASTCLOCK_ID_DEFAULT = "default";
    // Option value for using the well-known clock id "default real-time clock"
    public static final String OPT_FASTCLOCK_ID_DEFAULT_RT = "realtime";
    // Option value for using the well-known clock id "alternate clock 1"
    public static final String OPT_FASTCLOCK_ID_ALT_1 = "alt1";
    // Option value for using the well-known clock id "alternate clock 2"
    public static final String OPT_FASTCLOCK_ID_ALT_2 = "alt2";
    // Option value for using a custom clock ID
    public static final String OPT_FASTCLOCK_ID_CUSTOM = "custom";

    // Option key for setting the clock identifier to a custom value. Must set ClockId==custom in
    // order to be in effect. The custom clock id is in node ID format.
    public static final String OPT_FASTCLOCK_CUSTOM_ID = "ClockCustomId";

    public OlcbConfigurationManager(CanSystemConnectionMemo memo) {
        super(memo);

        InstanceManager.store(cf = new jmri.jmrix.openlcb.swing.OpenLcbComponentFactory(adapterMemo),
                jmri.jmrix.swing.ComponentFactory.class);
        InstanceManager.store(this, OlcbConfigurationManager.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    private void initializeFastClock() {
        boolean isMaster = true;
        String enableOption = adapterMemo.getProtocolOption(OPT_PROTOCOL_FASTCLOCK, OPT_FASTCLOCK_ENABLE);
        if (OPT_FASTCLOCK_ENABLE_GENERATOR.equals(enableOption)) {
            isMaster = true;
        } else if (OPT_FASTCLOCK_ENABLE_CONSUMER.equals(enableOption)) {
            isMaster = false;
        } else {
            // no clock needed.
            return;
        }

        NodeID clockId = null;
        String clockIdSetting = adapterMemo.getProtocolOption(OPT_PROTOCOL_FASTCLOCK, OPT_FASTCLOCK_ID);
        if (OPT_FASTCLOCK_ID_DEFAULT.equals(clockIdSetting)) {
            clockId = TimeProtocol.DEFAULT_CLOCK;
        } else if (OPT_FASTCLOCK_ID_DEFAULT_RT.equals(clockIdSetting)) {
            clockId = TimeProtocol.DEFAULT_RT_CLOCK;
        } else if (OPT_FASTCLOCK_ID_ALT_1.equals(clockIdSetting)) {
            clockId = TimeProtocol.ALT_CLOCK_1;
        } else if (OPT_FASTCLOCK_ID_ALT_2.equals(clockIdSetting)) {
            clockId = TimeProtocol.ALT_CLOCK_2;
        } else if (OPT_FASTCLOCK_ID_CUSTOM.equals(clockIdSetting)) {
            String customId = adapterMemo.getProtocolOption(OPT_PROTOCOL_FASTCLOCK, OPT_FASTCLOCK_CUSTOM_ID);
            if (customId == null || customId.isEmpty()) {
                log.error("OpenLCB clock initialize: User selected custom clock, but did not provide a Custom Clock ID. Using default clock.");
            } else {
                try {
                    clockId = new NodeID(customId);
                } catch (IllegalArgumentException e) {
                    log.error("OpenLCB clock initialize: Custom Clock ID '{}' is in illegal format. Use dotted hex notation like 05.01.01.01.DD.EE", customId);
                }
            }
        }
        if (clockId == null) {
            clockId = TimeProtocol.DEFAULT_CLOCK;
        }
        log.debug("Creating olcb clock with id {} is_master {}", clockId, isMaster);
        clockControl = new OlcbClockControl(getInterface(), clockId, isMaster);
        InstanceManager.setDefault(ClockControl.class, clockControl);
    }

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
        
        InstanceManager.setLightManager(
                getLightManager()
        );

        if (getProgrammerManager().isAddressedModePossible()) {
            InstanceManager.store(getProgrammerManager(), jmri.AddressedProgrammerManager.class);
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

        initializeFastClock();

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
    OlcbClockControl clockControl;

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
        if (type.equals(jmri.LightManager.class)) {
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
        if (type.equals(ClockControl.class)) {
            return clockControl != null;
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
        if (T.equals(jmri.LightManager.class)) {
            return (T) getLightManager();
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
        if (T.equals(ClockControl.class)) {
            return (T) clockControl;
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
            throttleManager = new OlcbThrottleManager(adapterMemo);
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
        if (lightManager != null) {
            InstanceManager.deregister(lightManager, jmri.jmrix.openlcb.OlcbLightManager.class);
        }
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        InstanceManager.deregister(this, OlcbConfigurationManager.class);

        if (clockControl != null) {
            clockControl.dispose();
            InstanceManager.deregister(clockControl, ClockControl.class);
        }
    }

    protected OlcbLightManager lightManager;
    
    public OlcbLightManager getLightManager() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (lightManager == null) {
            lightManager = new OlcbLightManager(adapterMemo);
        }
        return lightManager;
    }

    class SimpleNodeIdentInfoHandler extends MessageDecoder {
        /**
         * Helper function to add a string value to the sequence of bytes to send for SNIP
         * response content.
         *
         * @param value    string to render into byte stream
         * @param contents represents the byte stream that will be sent.
         */
        private void  addStringPart(String value, List<Byte> contents) {
            if (value == null || value.isEmpty()) {
                contents.add((byte)0);
            } else {
                byte[] bb;

                try {
                    bb = value.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    bb = new byte[] {'?'};
                }
                for (byte b : bb) {
                    contents.add(b);
                }
                // terminating null byte.
                contents.add((byte)0);
            }
        }

        SimpleNodeIdentInfoHandler() {
            List<Byte> l = new ArrayList<>(256);
            l.add((byte)4); // version byte
            addStringPart("JMRI", l);
            addStringPart("PanelPro", l);
            String name = ProfileManager.getDefault().getActiveProfileName();
            if (name != null) {
                addStringPart("Profile " + name, l); // hardware version
            } else {
                addStringPart("", l); // hardware version
            }
            addStringPart(jmri.Version.name(), l); // software version
            l.add((byte)2); // version byte
            addStringPart(adapterMemo.getProtocolOption(OPT_PROTOCOL_IDENT, OPT_IDENT_USERNAME), l);
            addStringPart(adapterMemo.getProtocolOption(OPT_PROTOCOL_IDENT, OPT_IDENT_DESCRIPTION), l);
            content = new byte[l.size()];
            for (int i = 0; i < l.size(); ++i) {
                content[i] = l.get(i);
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
        String userOption = adapterMemo.getProtocolOption(OPT_PROTOCOL_IDENT, OPT_IDENT_NODEID);
        if (userOption != null && !userOption.isEmpty()) {
            try {
                nodeID = new NodeID(userOption);
                return;
            } catch (IllegalArgumentException e) {
                log.error("User set node ID protocol option which is in invalid format ({}). Expected dotted hex notation like 02.01.12.FF.EE.DD", userOption);
            }
        }
        List<NodeID> previous = InstanceManager.getList(NodeID.class);
        if (!previous.isEmpty()) {
            nodeID = previous.get(0);
            return;
        }

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
        olcbIf.getInterface().setLoopbackThread((Runnable r)->ThreadingUtil.runOnLayout(r::run));
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
                        // N.B. during JUnit testing, the following call tends to hang
                        // on semaphore acquisition in org.openlcb.can.CanInterface.initialize()
                        // near line 109 in openlcb lib 0.7.22, which leaves
                        // the thread hanging around forever.
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
