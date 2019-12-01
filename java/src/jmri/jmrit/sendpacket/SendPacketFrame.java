package jmri.jmrit.sendpacket;

import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User interface for sending DCC packets.
 * <p>
 * This was originally made from jmrix.loconet.logogen, but note that the logic
 * is somewhat different here. The LocoNet version waited for the sent (LocoNet)
 * packet to be echo'd, while this starts the timeout immediately.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class SendPacketFrame extends jmri.util.JmriJFrame {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    public SendPacketFrame() {
        super();
    }

    // internal members to hold sequence widgets
    static final int MAXSEQUENCE = 4;
    JTextField mPacketField[] = new JTextField[MAXSEQUENCE];
    JCheckBox mUseField[] = new JCheckBox[MAXSEQUENCE];
    JSpinner mDelaySpinner[] = new JSpinner[MAXSEQUENCE];
    JToggleButton mRunButton = new JToggleButton(Bundle.getMessage("ButtonStart"));

    @Override
    public void initComponents() {

        setTitle(Bundle.getMessage("SendPacketTitle"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // handle single-packet part
        getContentPane().add(new JLabel(Bundle.getMessage("SendOneLabel")));
        {
            JPanel pane1 = new JPanel();
            pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));

            jLabel1.setText(Bundle.getMessage("PacketLabel"));
            jLabel1.setVisible(true);

            sendButton.setText(Bundle.getMessage("ButtonSend"));
            sendButton.setVisible(true);
            sendButton.setToolTipText(Bundle.getMessage("SendToolTip"));

            packetTextField.setToolTipText(Bundle.getMessage("EnterHexBytesToolTip"));

            pane1.add(jLabel1);
            pane1.add(packetTextField);
            pane1.add(sendButton);
            pane1.add(Box.createVerticalGlue());

            sendButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });

            getContentPane().add(pane1);
        }

        getContentPane().add(new JSeparator());

        // Configure the sequence
        getContentPane().add(new JLabel(Bundle.getMessage("SendSequenceLabel")));
        JPanel pane2 = new JPanel();
        pane2.setLayout(new GridLayout(MAXSEQUENCE + 2, 4));
        pane2.add(new JLabel(""));
        pane2.add(new JLabel(Bundle.getMessage("ButtonSend")));
        pane2.add(new JLabel(Bundle.getMessage("Packet")));
        pane2.add(new JLabel(Bundle.getMessage("WaitMsec")));
        for (int i = 0; i < MAXSEQUENCE; i++) {
            pane2.add(new JLabel(Integer.toString(i + 1)));
            mUseField[i] = new JCheckBox();
            mPacketField[i] = new JTextField(10);
            mDelaySpinner[i] = new JSpinner(new SpinnerNumberModel(100, 0, 1000, 1));
            pane2.add(mUseField[i]);
            pane2.add(mPacketField[i]);
            pane2.add(mDelaySpinner[i]);
        }
        pane2.add(mRunButton); // starts a new row in layout
        getContentPane().add(pane2);

        mRunButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                runButtonActionPerformed(e);
            }
        });

        // get the CommandStation reference
        cs = InstanceManager.getNullableDefault(CommandStation.class);
        if (cs == null) {
            log.error("No CommandStation object available");
        }

        // add help menu
        addHelpMenu("package.jmri.jmrit.sendpacket.SendPacketFrame", true);

        // pack to cause display
        pack();
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        cs.sendPacket(createPacket(packetTextField.getText()), 1);
    }

    // control sequence operation
    int mNextSequenceElement = 0;
    byte[] mNextEcho = null;
    javax.swing.Timer timer = null;

    /**
     * Internal routine to handle timer starts {@literal &} restarts.
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
     * Run button pressed down, start the sequence operation.
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
     * Echo has been heard, start delay for next packet.
     */
    void startSequenceDelay() {
        // at the start, mNextSequenceElement contains index we're
        // working on
        int delay = 500;   // default delay if non specified, or format bad
        try {
            delay = (Integer) mDelaySpinner[mNextSequenceElement].getValue();
        } catch (NumberFormatException e) {
        }

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
            byte[] m = createPacket(mPacketField[mNextSequenceElement].getText());
            // send it
            mNextEcho = m;
            if (m != null) {
                cs.sendPacket(m, 1);
            } else {
                log.warn("Message invalid: " + mPacketField[mNextSequenceElement].getText());
            }
            // and queue the rest of the sequence if we're continuing
            if (mRunButton.isSelected()) {
                startSequenceDelay();
            }
        } else {
            // ask for the next one
            mNextSequenceElement++;
            sendNextItem();
        }
    }

    /**
     * Create a well-formed DCC packet from a String.
     *
     * @return the packet, with contents filled-in
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
        justification = "API defined by CommmandStation interface")
    byte[] createPacket(String s) {
        // gather bytes in result
        byte b[] = StringUtil.bytesFromHexString(s);
        if (b.length == 0) {
            return null;  // no such thing as a zero-length message
        }
        return b;
    }

    /**
     * When the window closes, stop any sequences running.
     */
    @Override
    public void dispose() {
        mRunButton.setSelected(false);
        super.dispose();
    }

    // private data
    private CommandStation cs = null;

    private final static Logger log = LoggerFactory.getLogger(SendPacketAction.class);

}
