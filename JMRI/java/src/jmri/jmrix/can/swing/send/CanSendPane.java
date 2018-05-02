package jmri.jmrix.can.swing.send;

import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.CbusAddress;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User interface for sending CAN frames to exercise the system
 * <P>
 * When sending a sequence of operations:
 * <UL>
 * <LI>Send the next message and start a timer
 * <LI>When the timer trips, repeat if buttons still down.
 * </UL>
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class CanSendPane extends jmri.jmrix.can.swing.CanPanel implements CanListener {

    // member declarations
    JLabel jLabel1 = new JLabel();
    JButton sendButton = new JButton();
    JTextField packetTextField = new JTextField(12);

    public CanSendPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Handle single-packet part
        JPanel topPane = new JPanel();
        // Add a nice border
        topPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("SendFrameTitle")));
        add(topPane);

        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));

        JPanel entry = new JPanel();
        jLabel1.setText(Bundle.getMessage("FrameLabel"));
        jLabel1.setVisible(true);

        sendButton.setText(Bundle.getMessage("ButtonSend"));
        sendButton.setVisible(true);
        sendButton.setToolTipText(Bundle.getMessage("SendToolTip"));

        entry.add(jLabel1);
        entry.add(packetTextField);
        packetTextField.setToolTipText(Bundle.getMessage("PacketToolTip"));
        pane1.add(entry);

        pane1.add(sendButton);
        pane1.add(Box.createVerticalGlue());

        sendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });
        topPane.add(pane1);

        // Configure the sequence
        JPanel bottomPane = new JPanel();
        // Add a nice border
        bottomPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("SendSeqTitle")));
        bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.Y_AXIS));
        add(bottomPane);
        JPanel pane2 = new JPanel();
        pane2.setLayout(new GridLayout(MAXSEQUENCE + 1, 4));
        pane2.add(new JLabel(""));
        pane2.add(new JLabel(Bundle.getMessage("ButtonSend")));
        pane2.add(new JLabel(Bundle.getMessage("PacketLabel")));
        pane2.add(new JLabel(Bundle.getMessage("WaitLabel")));
        for (int i = 0; i < MAXSEQUENCE; i++) {
            pane2.add(new JLabel(Integer.toString(i + 1)));
            mUseField[i] = new JCheckBox();
            mPacketField[i] = new JTextField(10);
            numberSpinner[i] = new JSpinner(new SpinnerNumberModel(1, 0, 10000, 1));
            pane2.add(mUseField[i]);
            pane2.add(mPacketField[i]);
            mPacketField[i].setToolTipText(Bundle.getMessage("PacketToolTip"));
            pane2.add(numberSpinner[i]);
        }
        bottomPane.add(pane2);
        bottomPane.add(Box.createVerticalGlue()); // starts a new row in layout
        bottomPane.add(mRunButton);
        mRunButton.setToolTipText(Bundle.getMessage("StartToolTip"));

        mRunButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                runButtonActionPerformed(e);
            }
        });
    }

    // internal members to hold sequence widgets
    static final int MAXSEQUENCE = 4;
    JTextField mPacketField[] = new JTextField[MAXSEQUENCE];
    JCheckBox mUseField[] = new JCheckBox[MAXSEQUENCE];
    JSpinner numberSpinner[] =  new JSpinner[MAXSEQUENCE];
    JToggleButton mRunButton = new JToggleButton(Bundle.getMessage("ButtonStart"));

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        tc = memo.getTrafficController();
        tc.addCanListener(this);
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.can.swing.send.CanSendFrame";
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " " + Bundle.getMessage("MenuItemSendFrame"));
        }
        return Bundle.getMessage("MenuItemSendFrame");
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        CanMessage m = createPacket(packetTextField.getText());
        log.debug("sendButtonActionPerformed: " + m);
        tc.sendCanMessage(m, this);
    }

    // control sequence operation
    int mNextSequenceElement = 0;
    javax.swing.Timer timer = null;

    /**
     * Internal routine to handle timer starts {@literal &} restarts
     */
    protected void restartTimer(int delay) {
        if (timer == null) {
            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendNextItem();
                }
            });
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Internal routine to handle a timeout and send next item
     */
    synchronized protected void timeout() {
        sendNextItem();
    }

    /**
     * Run button pressed down, start the sequence operation.
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
            JOptionPane.showMessageDialog(null, Bundle.getMessage("NoSelectionDialog"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        // start the operation
        mNextSequenceElement = 0;
        sendNextItem();
    }

    /**
     * Echo has been heard, start delay for next packet.
     */
    void startSequenceDelay() {
        // at the start, mNextSequenceElement contains index we're
        // working on
        int delay = (Integer) numberSpinner[mNextSequenceElement].getValue();
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
     * Create a well-formed message from a String. String is expected to be space
     * seperated hex bytes or CbusAddress, e.g.: 12 34 56 +n4e1
     *
     * @return The packet, with contents filled-in
     */
    CanMessage createPacket(String s) {
        CanMessage m;
        // Try to convert using CbusAddress class
        CbusAddress a = new CbusAddress(s);
        if (a.check()) {
            m = a.makeMessage(tc.getCanid());
        } else {
            m = new CanMessage(tc.getCanid());
            // check for header
            if (s.charAt(0) == '[') {
                // extended header
                m.setExtended(true);
                int i = s.indexOf(']');
                String h = s.substring(1, i);
                m.setHeader(Integer.parseInt(h, 16));
                s = s.substring(i + 1, s.length());
            } else if (s.charAt(0) == '(') {
                // standard header
                int i = s.indexOf(')');
                String h = s.substring(1, i);
                m.setHeader(Integer.parseInt(h, 16));
                s = s.substring(i + 1, s.length());
            }
            // Try to get hex bytes
            byte b[] = StringUtil.bytesFromHexString(s);
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
    }

    /**
     * Don't pay attention to replies
     */
    @Override
    public void reply(CanReply m) {
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
    private TrafficController tc = null;

    /**
     * Nested class to create one of these using old-style defaults.
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemSendFrame"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    CanSendPane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }
    private final static Logger log = LoggerFactory.getLogger(CanSendPane.class);

}
