// CanSendPane.java
package jmri.jmrix.can.swing.send;

import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
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
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version	$Revision: 17977 $
 */
public class CanSendPane extends jmri.jmrix.can.swing.CanPanel implements CanListener {

    /**
     *
     */
    private static final long serialVersionUID = 6281707873589937794L;
    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    public CanSendPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // handle single-packet part
        add(new JLabel("Send one frame:"));
        {
            JPanel pane1 = new JPanel();
            pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));

            jLabel1.setText("Frame:");
            jLabel1.setVisible(true);

            sendButton.setText("Send");
            sendButton.setVisible(true);
            sendButton.setToolTipText("Send frame");

            packetTextField.setToolTipText("Frame packet as hex pairs, e.g. 82 7D; checksum should be present but is recalculated");

            pane1.add(jLabel1);
            pane1.add(packetTextField);
            pane1.add(sendButton);
            pane1.add(Box.createVerticalGlue());

            sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });

            add(pane1);
        }

        add(new JSeparator());

        // Configure the sequence
        add(new JLabel("Send sequence of frames:"));
        JPanel pane2 = new JPanel();
        pane2.setLayout(new GridLayout(MAXSEQUENCE + 2, 4));
        pane2.add(new JLabel(""));
        pane2.add(new JLabel("Send"));
        pane2.add(new JLabel("packet"));
        pane2.add(new JLabel("wait (msec)"));
        for (int i = 0; i < MAXSEQUENCE; i++) {
            pane2.add(new JLabel(Integer.toString(i + 1)));
            mUseField[i] = new JCheckBox();
            mPacketField[i] = new JTextField(10);
            mDelayField[i] = new JTextField(10);
            mDelayField[i].setText("1");
            pane2.add(mUseField[i]);
            pane2.add(mPacketField[i]);
            pane2.add(mDelayField[i]);
        }
        pane2.add(mRunButton); // starts a new row in layout
        add(pane2);

        mRunButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                runButtonActionPerformed(e);
            }
        });
    }

    // internal members to hold sequence widgets
    static final int MAXSEQUENCE = 4;
    JTextField mPacketField[] = new JTextField[MAXSEQUENCE];
    JCheckBox mUseField[] = new JCheckBox[MAXSEQUENCE];
    JTextField mDelayField[] = new JTextField[MAXSEQUENCE];
    JToggleButton mRunButton = new JToggleButton("Go");

    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        tc = memo.getTrafficController();
        tc.addCanListener(this);
    }

    public String getHelpTarget() {
        return "package.jmri.jmrix.can.swing.send.CanSendFrame";
    }

    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " Send Can Frame");
        }
        return "Send Can Frame";
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
     * Run button pressed down, start the sequence operation
     *
     * @param e
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
     *
     * @param s
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
    public void message(CanMessage m) {
    }

    /**
     * Don't pay attention to replies
     */
    public void reply(CanReply m) {
    }

    /**
     * When the window closes, stop any sequences running
     */
    public void dispose() {
        mRunButton.setSelected(false);
        super.dispose();
    }

    // private data
    private TrafficController tc = null;

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        /**
         *
         */
        private static final long serialVersionUID = 6513091592493774694L;

        public Default() {
            super("Send Can Frame",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    CanSendPane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }
    private final static Logger log = LoggerFactory.getLogger(CanSendPane.class.getName());

}
