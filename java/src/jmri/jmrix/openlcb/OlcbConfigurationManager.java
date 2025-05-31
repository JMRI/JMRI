package jmri.jmrix.openlcb;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import jmri.ClockControl;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.profile.ProfileManager;
import jmri.util.ThreadingUtil;

import org.openlcb.*;
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

    final jmri.jmrix.swing.ComponentFactory cf;

    private void initializeFastClock() {
        boolean isMaster;
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

        InstanceManager.store(getPowerManager(), jmri.PowerManager.class);

        InstanceManager.setStringIOManager(
                getStringIOManager());

        InstanceManager.setThrottleManager(
                getThrottleManager());

        InstanceManager.setReporterManager(
                getReporterManager());

        InstanceManager.setLightManager(
                getLightManager()
        );

        InstanceManager.setMeterManager(
                getMeterManager()
        );

        InstanceManager.store(getCommandStation(), jmri.CommandStation.class);
        
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
        iface.registerMessageListener(new PipRequestHandler());

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
    OlcbEventNameStore olcbEventNameStore = new OlcbEventNameStore();
    
    OlcbInterface getInterface() {
        return olcbCanInterface.getInterface();
    }

    // internal to OpenLCB library, should not be exposed
    AliasMap aliasMap;
    // internal to OpenLCB library, should not be exposed
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
        if (type.equals(jmri.PowerManager.class)) {
            return true;
        }
        if (type.equals(jmri.ReporterManager.class)) {
            return true;
        }
        if (type.equals(jmri.LightManager.class)) {
            return true;
        }
        if (type.equals(jmri.MeterManager.class)) {
            return true;
        }
        if (type.equals(jmri.StringIOManager.class)) {
            return true;
        }
        if (type.equals(jmri.GlobalProgrammerManager.class)) {
            return true;
        }
        if (type.equals(jmri.AddressedProgrammerManager.class)) {
            return true;
        }
        if (type.equals(jmri.CommandStation.class)) {
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
        if (type.equals(OlcbEventNameStore.class)) {
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
        if (T.equals(jmri.PowerManager.class)) {
            return (T) getPowerManager();
        }
        if (T.equals(jmri.LightManager.class)) {
            return (T) getLightManager();
        }
        if (T.equals(jmri.MeterManager.class)) {
            return (T) getMeterManager();
        }
        if (T.equals(jmri.StringIOManager.class)) {
            return (T) getStringIOManager();
        }
        if (T.equals(jmri.ReporterManager.class)) {
            return (T) getReporterManager();
        }
        if (T.equals(jmri.GlobalProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.AddressedProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.CommandStation.class)) {
            return (T) getCommandStation();
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
        if (T.equals(OlcbEventNameStore.class)) {
            return (T) olcbEventNameStore;
        }
        return null; // nothing, by default
    }

    protected OlcbProgrammerManager programmerManager;

    public OlcbProgrammerManager getProgrammerManager() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (programmerManager == null) {
            programmerManager = new OlcbProgrammerManager(adapterMemo);
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

    protected OlcbPowerManager powerManager;

    public OlcbPowerManager getPowerManager() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (powerManager == null) {
            powerManager = new OlcbPowerManager(adapterMemo);
        }
        return powerManager;
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

    protected OlcbMeterManager meterManager;

    public OlcbMeterManager getMeterManager() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (meterManager == null) {
            meterManager = new OlcbMeterManager(adapterMemo);
        }
        return meterManager;
    }

    protected OlcbStringIOManager stringIOManager;

    public OlcbStringIOManager getStringIOManager() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (stringIOManager == null) {
            stringIOManager = new OlcbStringIOManager(adapterMemo);
        }
        return stringIOManager;
    }

    protected OlcbReporterManager reporterManager;

    public OlcbReporterManager getReporterManager() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (reporterManager == null) {
            reporterManager = new OlcbReporterManager(adapterMemo);
        }
        return reporterManager;
    }

    protected OlcbCommandStation commandStation;

    public OlcbCommandStation getCommandStation() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (commandStation == null) {
            commandStation = new OlcbCommandStation(adapterMemo);
        }
        return commandStation;
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

    class SimpleNodeIdentInfoHandler extends MessageDecoder {
        /**
         * Helper function to add a string value to the sequence of bytes to send for SNIP
         * response content.
         *
         * @param addString  string to render into byte stream
         * @param contents   represents the byte stream that will be sent.
         * @param maxlength  maximum number of characters to include, not counting terminating null
         */
        private void  addStringPart(String addString, List<Byte> contents, int maxlength) {
            if (addString != null && !addString.isEmpty()) {
                String value = addString.substring(0,Math.min(maxlength, addString.length()));
                byte[] bb = value.getBytes(StandardCharsets.UTF_8);
                for (byte b : bb) {
                    contents.add(b);
                }
            }
            // terminating null byte.
            contents.add((byte)0);
        }

        SimpleNodeIdentInfoHandler() {
            List<Byte> l = new ArrayList<>(256);

            l.add((byte)4); // version byte
            addStringPart("JMRI", l, 40);  // mfg field; 40 char limit in Standard, not counting final null
            addStringPart(jmri.Application.getApplicationName(), l, 40);  // model
            String name = ProfileManager.getDefault().getActiveProfileName();
            if (name != null) {
                addStringPart(name, l, 20); // hardware version
            } else {
                addStringPart("", l, 20); // hardware version
            }
            addStringPart(jmri.Version.name(), l, 20); // software version

            l.add((byte)2); // version byte
            addStringPart(adapterMemo.getProtocolOption(OPT_PROTOCOL_IDENT, OPT_IDENT_USERNAME), l, 62);
            addStringPart(adapterMemo.getProtocolOption(OPT_PROTOCOL_IDENT, OPT_IDENT_DESCRIPTION), l, 63);

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

    class PipRequestHandler extends MessageDecoder {

        @Override
        public void handleProtocolIdentificationRequest(ProtocolIdentificationRequestMessage msg, Connection sender) {
            long flags = 0x00041000000000L;  // PC, SNIP protocols
            // only reply if for us
            if (msg.getDestNodeID() == nodeID) {
                getInterface().getOutputConnection().put(new ProtocolIdentificationReplyMessage(nodeID, msg.getSourceNodeID(), flags), this);
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
        try {
            String userOption = adapterMemo.getProtocolOption(OPT_PROTOCOL_IDENT, OPT_IDENT_NODEID);
            if (userOption != null && !userOption.isEmpty()) {
                try {
                    nodeID = new NodeID(userOption);
                    log.trace("getOurNodeID sets known option Node ID: {}", nodeID);
                    return;
                } catch (IllegalArgumentException e) {
                    log.error("User configured a node ID protocol option which is in invalid format ({}). Expected dotted hex notation like 02.01.12.FF.EE.DD", userOption);
                }
            }
            List<NodeID> previous = InstanceManager.getList(NodeID.class);
            if (!previous.isEmpty()) {
                nodeID = previous.get(0);
                log.trace("getOurNodeID sets known instance Node ID: {}", nodeID);
                return;
            }
    
            long pid = getProcessId(1);
            log.trace("Process ID: {}", pid);
    
            // get first network interface internet address
            // almost certainly the wrong approach, isn't likely to
            // find real IP address for coms, but it gets some entropy.
            InetAddress address = null;
            try {
                NetworkInterface n = NetworkInterface.getNetworkInterfaces().nextElement();
                if (n != null) {
                    address = n.getInetAddresses().nextElement();
                }
                log.debug("InetAddress: {}", address);
            } catch (SocketException | java.util.NoSuchElementException e) {
                // SocketException is part of the getNetworkInterfaces specification.
                // java.util.NoSuchElementException seen on some Windows machines
                // for unknown reasons.  We provide a short error message in that case.
                log.warn("Can't get IP address to make NodeID. You should set a NodeID in the Connection preferences.");
            }
            
            int b2 = 0;
            if (address != null) {
                b2 = address.getAddress()[0];
            } else {
                b2 = (byte)(RANDOM.nextInt(255) & 0xFF); // & 0xFF not strictly necessary, but makes SpotBugs happy
                log.trace("Used random value {} for address byte", b2);
            }
            
            // store new NodeID
            nodeID = new NodeID(new byte[]{2, 1, 18, (byte) (b2 & 0xFF), (byte) ((pid >> 8) & 0xFF), (byte) (pid & 0xFF)});
            log.debug("getOurNodeID sets new Node ID: {}", nodeID);
            
        } catch (Exception e) {
            // We catch Exception here, instead of within the NetworkInterface lookup, because
            // we want to know which kind of exceptions we're seeing.  If/when this gets reported,
            // generalize the catch statement above.
            log.error("Unexpected Exception while processing Node ID definition. Please report this to the JMRI developers", e);
            byte b2 = (byte)(RANDOM.nextInt(255) & 0xFF); // & 0xFF not strictly necessary, but makes SpotBugs happy
            byte b1 = (byte)(RANDOM.nextInt(255) & 0xFF);
            byte b0 = (byte)(RANDOM.nextInt(255) & 0xFF);
            nodeID = new NodeID(new byte[]{2, 1, 18, b2, b1, b0});            
            log.debug("Setting random Node ID: {}", nodeID);
        }
    }

    private static final Random RANDOM = new Random();
    
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
                    Thread t = jmri.util.ThreadingUtil.newThread(
                                    () -> {
                                        // N.B. during JUnit testing, the following call tends to hang
                                        // on semaphore acquisition in org.openlcb.can.CanInterface.initialize()
                                        // near line 109 in openlcb lib 0.7.22, which leaves
                                        // the thread hanging around forever.
                                        olcbCanInterface.initialize();
                                    },
                                "olcbCanInterface.initialize");
                    t.start();
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbConfigurationManager.class);
}
