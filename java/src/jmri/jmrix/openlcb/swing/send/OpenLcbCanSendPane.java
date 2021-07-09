package jmri.jmrix.openlcb.swing.send;

import java.awt.FlowLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.CbusAddress;
import jmri.jmrix.openlcb.swing.ClientActions;
import jmri.util.StringUtil;
import jmri.util.javaworld.GridLayout2;
import org.openlcb.Connection;
import org.openlcb.DatagramAcknowledgedMessage;
import org.openlcb.DatagramMessage;
import org.openlcb.EventID;
import org.openlcb.IdentifyConsumersMessage;
import org.openlcb.IdentifyEventsAddressedMessage;
import org.openlcb.IdentifyProducersMessage;
import org.openlcb.Message;
import org.openlcb.MimicNodeStore;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.ProducerConsumerEventReportMessage;
import org.openlcb.VerifyNodeIDNumberMessage;
import org.openlcb.can.AliasMap;
import org.openlcb.implementations.MemoryConfigurationService;
import org.openlcb.swing.NodeSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User interface for sending OpenLCB CAN frames to exercise the system
 * <p>
 * When sending a sequence of operations:
 * <ul>
 * <li>Send the next message and start a timer
 * <li>When the timer trips, repeat if buttons still down.
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2008, 2012
 *
 */
public class OpenLcbCanSendPane extends jmri.jmrix.can.swing.CanPanel implements CanListener {

    // member declarations
    final JLabel jLabel1 = new JLabel();
    final JButton sendButton = new JButton();
    final JTextField packetTextField = new JTextField(12);

    // internal members to hold sequence widgets
    static final int MAXSEQUENCE = 4;
    final JTextField[] mPacketField = new JTextField[MAXSEQUENCE];
    final JCheckBox[] mUseField = new JCheckBox[MAXSEQUENCE];
    final JTextField[] mDelayField = new JTextField[MAXSEQUENCE];
    final JToggleButton mRunButton = new JToggleButton("Go");

    final JTextField srcAliasField = new JTextField(4);
    NodeSelector nodeSelector;
    final JTextField sendEventField = new JTextField("02 03 04 05 06 07 00 01 ");     // NOI18N
    final JTextField datagramContentsField = new JTextField("20 61 00 00 00 00 08");  // NOI18N
    final JTextField configNumberField = new JTextField("40");                        // NOI18N
    final JTextField configAddressField = new JTextField("000000");                   // NOI18N
    final JTextField readDataField = new JTextField(80);
    final JTextField writeDataField = new JTextField(80);
    final JComboBox<String> addrSpace = new JComboBox<>(new String[]{"CDI", "All", "Config", "None"});

    Connection connection;
    AliasMap aliasMap;
    NodeID srcNodeID;
    MemoryConfigurationService mcs;
    MimicNodeStore store;
    OlcbInterface iface;
    ClientActions actions;

    public OpenLcbCanSendPane() {
        // most of the action is in initComponents
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        iface = memo.get(OlcbInterface.class);
        actions = new ClientActions(iface, memo);
        tc = memo.getTrafficController();
        tc.addCanListener(this);
        connection = memo.get(org.openlcb.Connection.class);
        srcNodeID = memo.get(org.openlcb.NodeID.class);
        aliasMap = memo.get(org.openlcb.can.AliasMap.class);

        // register request for notification
        Connection.ConnectionListener cl = new Connection.ConnectionListener() {
            @Override
            public void connectionActive(Connection c) {
                log.debug("connection active");
                // load the alias field
                srcAliasField.setText(Integer.toHexString(aliasMap.getAlias(srcNodeID)));
            }
        };
        connection.registerStartNotification(cl);

        mcs = memo.get(MemoryConfigurationService.class);
        store = memo.get(MimicNodeStore.class);
        nodeSelector = new NodeSelector(store);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // handle single-packet part
        add(getSendSinglePacketJPanel());

        add(new JSeparator());

        // Configure the sequence
        add(new JLabel("Send sequence of frames:"));
        JPanel pane2 = new JPanel();
        pane2.setLayout(new GridLayout2(MAXSEQUENCE + 2, 4));
        pane2.add(new JLabel(""));
        pane2.add(new JLabel("Send"));
        pane2.add(new JLabel("packet"));
        pane2.add(new JLabel("wait (msec)"));
        for (int i = 0; i < MAXSEQUENCE; i++) {
            pane2.add(new JLabel(Integer.toString(i + 1)));
            mUseField[i] = new JCheckBox();
            mPacketField[i] = new JTextField(10);
            mDelayField[i] = new JTextField(10);
            pane2.add(mUseField[i]);
            pane2.add(mPacketField[i]);
            pane2.add(mDelayField[i]);
        }
        pane2.add(mRunButton); // starts a new row in layout
        add(pane2);

        mRunButton.addActionListener(this::runButtonActionPerformed);

        // special packet forms
        add(new JSeparator());
        add(new JLabel("Send special frame:"));

        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        pane2.add(new JLabel("Src Node alias:"));
        pane2.add(srcAliasField);
        add(pane2);

        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        add(pane2);
        JButton b;
        b = new JButton("Send CIM");
        b.addActionListener(this::sendCimPerformed);

        pane2.add(b);

        // send OpenLCB messages
        add(new JSeparator());
        add(addLineLabel("Send OpenLCB global message:"));

        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        add(pane2);
        b = new JButton("Send Verify Nodes Global");
        b.addActionListener(this::sendVerifyNodeGlobal);
        pane2.add(b);
        b = new JButton("Send Verify Node Global with NodeID");
        b.addActionListener(this::sendVerifyNodeGlobalID);
        pane2.add(b);

        add(new JSeparator());
        add(addLineLabel("Send OpenLCB event message:"));
        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        add(pane2);
        b = new JButton("Send Request Consumers");
        b.addActionListener(this::sendReqConsumers);
        pane2.add(b);
        b = new JButton("Send Request Producers");
        b.addActionListener(this::sendReqProducers);
        pane2.add(b);
        b = new JButton("Send Event Produced");
        b.addActionListener(this::sendEventPerformed);
        pane2.add(b);
        pane2.add(new JLabel("Event ID (8 bytes):"));
        pane2.add(sendEventField);

        add(new JSeparator());
        add(addLineLabel("Send OpenLCB addressed message to:", nodeSelector));
        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        add(pane2);
        b = new JButton("Send Request Events");
        b.addActionListener(this::sendRequestEvents);
        pane2.add(b);

        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        add(pane2);
        b = new JButton("Send Datagram");
        b.addActionListener(this::sendDatagramPerformed);
        pane2.add(b);
        pane2.add(new JLabel("Contents: "));
        pane2.add(datagramContentsField);
        b = new JButton("Send Datagram Reply");
        b.addActionListener(this::sendDatagramReply);
        pane2.add(b);

        // send OpenLCB Configuration message
        add(new JSeparator());
        add(addLineLabel("Send OpenLCB Configuration Command:"));

        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        add(pane2);
        pane2.add(new JLabel("Memory Address: "));
        pane2.add(configAddressField);
        pane2.add(new JLabel("Address Space: "));
        pane2.add(addrSpace);
        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        add(pane2);
        pane2.add(new JLabel("Byte Count: "));
        pane2.add(configNumberField);
        b = new JButton("Read");
        b.addActionListener(this::readPerformed);
        pane2.add(b);
        pane2.add(new JLabel("Data: "));
        pane2.add(readDataField);

        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        add(pane2);
        b = new JButton("Write");
        b.addActionListener(this::writePerformed);
        pane2.add(b);
        pane2.add(new JLabel("Data: "));
        writeDataField.setText("00 00");   // NOI18N
        pane2.add(writeDataField);

        b = new JButton("Open CDI Config Tool");
        add(b);
        b.addActionListener(e -> openCdiPane());

    }

    private JPanel getSendSinglePacketJPanel() {
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));

        jLabel1.setText("Single Frame:  (Raw input format is [123] 12 34 56) ");
        jLabel1.setVisible(true);

        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Send frame");

        packetTextField.setToolTipText("Frame as hex pairs, e.g. 82 7D; standard header in (), extended in []");

        pane1.add(jLabel1);
        pane1.add(packetTextField);
        pane1.add(sendButton);
        pane1.add(Box.createVerticalGlue());

        sendButton.addActionListener(this::sendButtonActionPerformed);
        return pane1;
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.openlcb.swing.send.OpenLcbCanSendFrame";  // NOI18N
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " Send Can Frame");
        }
        return "Send CAN Frames and OpenLCB Messages";
    }

    JComponent addLineLabel(String text) {
        return addLineLabel(text, null);
    }

    JComponent addLineLabel(String text, JComponent c) {
        JLabel lab = new JLabel(text);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        if (c != null) {
            p.add(lab);
            p.add(c);
        } else {
            p.add(lab);
        }
        p.add(Box.createHorizontalGlue());
        return p;
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        String input = packetTextField.getText();
        // TODO check input + feedback on error. Too easy to cause NPE
        CanMessage m = createPacket(input);
        log.debug("sendButtonActionPerformed: {}",m);
        tc.sendCanMessage(m, this);
    }

    public void sendCimPerformed(java.awt.event.ActionEvent e) {
        String data = "[10700" + srcAliasField.getText() + "]";  // NOI18N
        log.debug("|{}|",data);
        CanMessage m = createPacket(data);
        log.debug("sendCimPerformed");
        tc.sendCanMessage(m, this);
    }

    NodeID destNodeID() {
        return nodeSelector.getSelectedItem();
    }

    EventID eventID() {
        return new EventID(jmri.util.StringUtil.bytesFromHexString(sendEventField.getText()
                .replace(".", " ")));
    }

    public void sendVerifyNodeGlobal(java.awt.event.ActionEvent e) {
        Message m = new VerifyNodeIDNumberMessage(srcNodeID);
        connection.put(m, null);
    }

    public void sendVerifyNodeGlobalID(java.awt.event.ActionEvent e) {
        Message m = new VerifyNodeIDNumberMessage(srcNodeID, destNodeID());
        connection.put(m, null);
    }

    public void sendRequestEvents(java.awt.event.ActionEvent e) {
        Message m = new IdentifyEventsAddressedMessage(srcNodeID, destNodeID());
        connection.put(m, null);
    }

    public void sendEventPerformed(java.awt.event.ActionEvent e) {
        Message m = new ProducerConsumerEventReportMessage(srcNodeID, eventID());
        connection.put(m, null);
    }

    public void sendReqConsumers(java.awt.event.ActionEvent e) {
        Message m = new IdentifyConsumersMessage(srcNodeID, eventID());
        connection.put(m, null);
    }

    public void sendReqProducers(java.awt.event.ActionEvent e) {
        Message m = new IdentifyProducersMessage(srcNodeID, eventID());
        connection.put(m, null);
    }

    public void sendDatagramPerformed(java.awt.event.ActionEvent e) {
        Message m = new DatagramMessage(srcNodeID, destNodeID(),
                jmri.util.StringUtil.bytesFromHexString(datagramContentsField.getText()));
        connection.put(m, null);
    }

    public void sendDatagramReply(java.awt.event.ActionEvent e) {
        Message m = new DatagramAcknowledgedMessage(srcNodeID, destNodeID());
        connection.put(m, null);
    }

    public void readPerformed(java.awt.event.ActionEvent e) {
        int space = 0xFF - addrSpace.getSelectedIndex();
        long addr = Integer.parseInt(configAddressField.getText(), 16);
        int length = Integer.parseInt(configNumberField.getText());
        mcs.requestRead(destNodeID(), space, addr,
                length, new MemoryConfigurationService.McsReadHandler() {
                    @Override
                    public void handleReadData(NodeID dest, int space, long address, byte[] data) {
                        log.debug("Read data received {} bytes",data.length);
                        readDataField.setText(jmri.util.StringUtil.hexStringFromBytes(data));
                    }

                    @Override
                    public void handleFailure(int errorCode) {
                        log.warn("OpenLCB read failed: 0x{}", Integer.toHexString
                                (errorCode));
                    }
                });
    }

    public void writePerformed(java.awt.event.ActionEvent e) {
        int space = 0xFF - addrSpace.getSelectedIndex();
        long addr = Integer.parseInt(configAddressField.getText(), 16);
        byte[] content = jmri.util.StringUtil.bytesFromHexString(writeDataField.getText());
        mcs.requestWrite(destNodeID(), space, addr, content, new MemoryConfigurationService.McsWriteHandler() {
            @Override
            public void handleSuccess() {
                // no action required on success
            }

            @Override
            public void handleFailure(int errorCode) {
                log.warn("OpenLCB write failed:  0x{}", Integer.toHexString
                        (errorCode));
            }
        });
    }

    public void openCdiPane() {
        actions.openCdiWindow(destNodeID(), destNodeID().toString());
    }

    // control sequence operation
    int mNextSequenceElement = 0;
    javax.swing.Timer timer = null;

    /**
     * Internal routine to handle timer starts and restarts
     * @param delay milliseconds to delay
     */
    protected void restartTimer(int delay) {
        if (timer == null) {
            timer = new javax.swing.Timer(delay, e -> sendNextItem());
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Internal routine to handle a timeout and send next item
     */
    protected synchronized void timeout() {
        sendNextItem();
    }

    /**
     * Run button pressed down, start the sequence operation
     * @param e event from GUI
     *
     */
    public void runButtonActionPerformed(java.awt.event.ActionEvent e) {
        if (!mRunButton.isSelected()) {
            return;
        }
        // make sure at least one is checked
        boolean ok = false;
        for (int i = 0; i < MAXSEQUENCE; i++) {
            if (mUseField[i].isSelected()) {
                ok = true;
            }
        }
        if (!ok) {
            mRunButton.setSelected(false);
            return;
        }
        // start the operation
        mNextSequenceElement = 0;
        sendNextItem();
    }

    /**
     * Echo has been heard, start delay for next packet
     */
    void startSequenceDelay() {
        // at the start, mNextSequenceElement contains index we're
        // working on
        int delay = Integer.parseInt(mDelayField[mNextSequenceElement].getText());
        // increment to next line at completion
        mNextSequenceElement++;
        // start timer
        restartTimer(delay);
    }

    /**
     * Send next item; may be used for the first item or when a delay has
     * elapsed.
     */
    void sendNextItem() {
        // check if still running
        if (!mRunButton.isSelected()) {
            return;
        }
        // have we run off the end?
        if (mNextSequenceElement >= MAXSEQUENCE) {
            // past the end, go back
            mNextSequenceElement = 0;
        }
        // is this one enabled?
        if (mUseField[mNextSequenceElement].isSelected()) {
            // make the packet
            CanMessage m = createPacket(mPacketField[mNextSequenceElement].getText());
            // send it
            tc.sendCanMessage(m, this);
            startSequenceDelay();
        } else {
            // ask for the next one
            mNextSequenceElement++;
            sendNextItem();
        }
    }

    /**
     * Create a well-formed message from a String String is expected to be space
     * seperated hex bytes or CbusAddress, e.g.: 12 34 56 +n4e1
     * @param s string of spaced hex byte codes
     * @return The packet, with contents filled-in
     */
    CanMessage createPacket(String s) {
        CanMessage m;
        // Try to convert using CbusAddress class to reuse a little code
        CbusAddress a = new CbusAddress(s);
        if (a.check()) {
            m = a.makeMessage(tc.getCanid());
        } else {
            m = new CanMessage(tc.getCanid());
            // check for header
            if (s.charAt(0) == '[') {           // NOI18N
                // extended header
                m.setExtended(true);
                int i = s.indexOf(']');       // NOI18N
                String h = s.substring(1, i);
                m.setHeader(Integer.parseInt(h, 16));
                s = s.substring(i + 1);
            } else if (s.charAt(0) == '(') {  // NOI18N
                // standard header
                int i = s.indexOf(')');       // NOI18N
                String h = s.substring(1, i);
                m.setHeader(Integer.parseInt(h, 16));
                s = s.substring(i + 1);
            }
            // Try to get hex bytes
            byte[] b = StringUtil.bytesFromHexString(s);
            m.setNumDataElements(b.length);
            // Use &0xff to ensure signed bytes are stored as unsigned ints
            for (int i = 0; i < b.length; i++) {
                m.setElement(i, b[i] & 0xff);
            }
        }
        return m;
    }

    /**
     * Don't pay attention to messages
     */
    @Override
    public void message(CanMessage m) {
        // ignore outgoing messages
    }

    /**
     * Don't pay attention to replies
     */
    @Override
    public void reply(CanReply m) {
        // ignore incoming replies
    }

    /**
     * When the window closes, stop any sequences running
     */
    @Override
    public void dispose() {
        mRunButton.setSelected(false);
        super.dispose();
    }

    // private data
    private TrafficController tc = null; //was CanInterface
    private final static Logger log = LoggerFactory.getLogger(OpenLcbCanSendPane.class);

}
