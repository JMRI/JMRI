// OlcbConfigurationManager.java
package jmri.jmrix.openlcb;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import org.openlcb.AbstractConnection;
import org.openlcb.Connection;
import org.openlcb.Message;
import org.openlcb.MimicNodeStore;
import org.openlcb.NodeID;
import org.openlcb.SimpleNodeIdentInfoReplyMessage;
import org.openlcb.can.AliasMap;
import org.openlcb.can.MessageBuilder;
import org.openlcb.can.NIDaAlgorithm;
import org.openlcb.can.OpenLcbCanFrame;
import org.openlcb.implementations.DatagramMeteringBuffer;
import org.openlcb.implementations.DatagramService;
import org.openlcb.implementations.MemoryConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Does configuration for OpenLCB communications implementations.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 * @version $Revision: 19643 $
 */
public class OlcbConfigurationManager extends jmri.jmrix.can.ConfigurationManager {

    public OlcbConfigurationManager(CanSystemConnectionMemo memo) {
        super(memo);

        InstanceManager.store(cf = new jmri.jmrix.openlcb.swing.OpenLcbComponentFactory(adapterMemo),
                jmri.jmrix.swing.ComponentFactory.class);
        InstanceManager.store(this, OlcbConfigurationManager.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    public void configureManagers() {

        // create our NodeID
        getOurNodeID();

        // create JMRI objects
        InstanceManager.setSensorManager(
                getSensorManager());

        InstanceManager.setTurnoutManager(
                getTurnoutManager());

        InstanceManager.setThrottleManager(
                getThrottleManager());

        InstanceManager.setProgrammerManager(
                getProgrammerManager());
                

        // do the connections
        tc = adapterMemo.getTrafficController();

        tc.addCanListener(new ReceivedFrameAdapter());

        connection = new TransmittedFrameAdapter();

        // create OpenLCB objects
        aliasMap = new AliasMap();
        messageBuilder = new MessageBuilder(aliasMap);

        nodeStore = new MimicNodeStore(connection, nodeID);

        // start alias acquisition
        new StartUpHandler().start(nodeID);

        // configure configuration service
        dmb = new DatagramMeteringBuffer(connection);
        dcs = new DatagramService(nodeID, dmb);
        mcs = new MemoryConfigurationService(nodeID, dcs);

        // show active
        ActiveFlag.setActive();
    }

    AliasMap aliasMap;
    MessageBuilder messageBuilder;
    MimicNodeStore nodeStore;
    Connection connection;
    TrafficController tc;
    NodeID nodeID;
    DatagramMeteringBuffer dmb;
    DatagramService dcs;
    MemoryConfigurationService mcs;

    /**
     * Tells which managers this provides by class
     */
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
        return false; // nothing, by default
    }

    @SuppressWarnings("unchecked")
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
            return (T) nodeStore;
        }
        if (T.equals(Connection.class)) {
            return (T) connection;
        }
        if (T.equals(MemoryConfigurationService.class)) {
            return (T) mcs;
        }
        if (T.equals(DatagramService.class)) {
            return (T) dcs;
        }
        if (T.equals(NodeID.class)) {
            return (T) nodeID;
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

    void updateSimpleNodeInfo() {
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
        byte[] content = new byte[part1.length + part2.length + part3.length + part4.length];
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

        // store to self!
        nodeStore.put(new SimpleNodeIdentInfoReplyMessage(nodeID, nodeID, content), null);
    }

    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.openlcb.OlcbActionListBundle");
    }

    /**
     * Create a node ID in the JMRI range from one byte of IP address, and 2
     * bytes of PID. That changes each time, which isn't perhaps what's wanted.
     */
    protected void getOurNodeID() {
        long pid = getProcessId(1);
        log.debug("Process ID: " + pid);

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
        log.debug("InetAddress: " + address);
        int b1 = 0;
        if (address != null) {
            b1 = address.getAddress()[0];
        }

        // store new NodeID
        nodeID = new NodeID(new byte[]{2, 1, 18, (byte) (b1 & 0xFF), (byte) ((pid >> 8) & 0xFF), (byte) (pid & 0xFF)});
        log.debug("Node ID: " + nodeID);
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

    /**
     * Receives frames from the TrafficController, and forwards into OpenLCB
     * system objects
     */
    class ReceivedFrameAdapter implements jmri.jmrix.can.CanListener {

        public synchronized void message(CanMessage l) {
            int header = l.getHeader();

            OpenLcbCanFrame frame = new OpenLcbCanFrame(header & 0xFFF);
            frame.setHeader(l.getHeader());
            if (l.getNumDataElements() != 0) {
                byte[] data = new byte[l.getNumDataElements()];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte) l.getElement(i);
                }
                frame.setData(data);
            }

            aliasMap.processFrame(frame);
            if (log.isDebugEnabled()) {
                log.debug("processing received message frame " + frame);
            }
            java.util.List<Message> list = messageBuilder.processFrame(frame);
            processToNetMessages(list);
        }

        public synchronized void reply(CanReply l) {
            int header = l.getHeader();

            OpenLcbCanFrame frame = new OpenLcbCanFrame(header & 0xFFF);
            frame.setHeader(l.getHeader());
            if (l.getNumDataElements() < 0) {
                log.error("Unexpected negative length in " + l);
            }
            if (l.getNumDataElements() > 0) {
                byte[] data = new byte[l.getNumDataElements()];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte) l.getElement(i);
                }
                frame.setData(data);
            }

            aliasMap.processFrame(frame);
            if (log.isDebugEnabled()) {
                log.debug("processing received reply frame " + frame);
            }
            java.util.List<Message> list = messageBuilder.processFrame(frame);
            processFromNetMessages(list);
        }
    }

    void processToNetMessages(java.util.List<Message> list) {
        if (list != null) {
            for (Message m : list) {
                log.debug("distribute message (to net): " + m);
                nodeStore.put(m, null);
            }
        }
    }

    void processFromNetMessages(java.util.List<Message> list) {
        if (list != null) {
            for (Message m : list) {
                log.debug("distribute message (fr net): " + m);
                nodeStore.put(m, null);
                dmb.connectionForRepliesFromDownstream().put(m, null);
                dcs.put(m, null);
            }
        }
    }

    boolean initialized = false;
    ArrayList<Connection.ConnectionListener> pendingList = new ArrayList<Connection.ConnectionListener>();

    jmri.jmrix.can.CanMessage convertToCan(OpenLcbCanFrame f) {
        jmri.jmrix.can.CanMessage fout = new jmri.jmrix.can.CanMessage(f.getData(), f.getHeader());
        fout.setExtended(true);
        return fout;
    }

    class TransmittedFrameAdapter extends AbstractConnection {

        public void put(org.openlcb.Message m, org.openlcb.Connection c) {
            if (log.isDebugEnabled()) {
                log.debug("transmitting message " + m);
            }

            List<OpenLcbCanFrame> list = messageBuilder.processMessage(m);
            for (OpenLcbCanFrame f : list) {
                if (log.isDebugEnabled()) {
                    log.debug("    as frame " + f);
                }
                // convert to proper CAN type and send
                tc.sendCanMessage(convertToCan(f), null);
            }
        }

        public void registerStartNotification(ConnectionListener c) {
            if (initialized) {
                super.registerStartNotification(c);
            } else {
                pendingList.add(c);
            }
        }
    }

    /**
     * State machine to handle startup
     */
    class StartUpHandler {

        NIDaAlgorithm nidaa;
        NodeID n;
        javax.swing.Timer timer;

        static final int START_DELAY = 2500; 
        
        void start(NodeID n) {
            this.n = n;
            log.debug("StartUpHandler starts up");
            // wait geological time for adapter startup
            javax.swing.Action doNextStep = new javax.swing.AbstractAction() {
                /**
                 *
                 */
                private static final long serialVersionUID = -5412454245885537585L;

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    run();
                }
            };

            timer = new javax.swing.Timer(START_DELAY, doNextStep);
            timer.start();
        }

        void run() {
            // Start acquiring our alias
            nidaa = new NIDaAlgorithm(n);
            OpenLcbCanFrame f;
            while ((f = nidaa.nextFrame()) != null) {
                tc.sendCanMessage(convertToCan(f), null);
            }

            // and switch to repeating faster
            // timer.setDelay(200);
            // done, kill timer and handle rest of startup
            timer.stop();

            // map our nodeID
            log.debug("mapping own alias {} to own NodeID {}", (int) nidaa.getNIDa(), n);
            aliasMap.insert((int) nidaa.getNIDa(), n);

            // insert our protocol info
            updateSimpleNodeInfo();

            // Request everybody else's info
            connection.put(new org.openlcb.VerifyNodeIDNumberMessage(n), null);

            // and wake up all
            initialized = true;
            for (Connection.ConnectionListener c : pendingList) {
                c.connectionActive(connection);
            }
        }

    }

    private final static Logger log = LoggerFactory.getLogger(OlcbConfigurationManager.class.getName());
}

/* @(#)OlcbConfigurationManager.java */
