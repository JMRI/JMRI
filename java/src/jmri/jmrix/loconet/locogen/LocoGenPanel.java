// LocoGenPanel.java
package jmri.jmrix.loconet.locogen;

import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import jmri.jmrix.loconet.LocoNetBundle;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User interface for sending LocoNet messages to exercise the system
 * <P>
 * When sending a sequence of operations:
 * <UL>
 * <LI>Send the next message
 * <LI>Wait until you hear the echo, then start a timer
 * <LI>When the timer trips, repeat if buttons still down.
 * </UL>
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2010
 * @version	$Revision$
 */
public class LocoGenPanel extends jmri.jmrix.loconet.swing.LnPanel
        implements LocoNetListener {

    /**
     *
     */
    private static final long serialVersionUID = -8721664131869665655L;
    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    public LocoGenPanel() {
        super();
    }

    // internal members to hold sequence widgets
    static final int MAXSEQUENCE = 4;
    JTextField mPacketField[] = new JTextField[MAXSEQUENCE];
    JCheckBox mUseField[] = new JCheckBox[MAXSEQUENCE];
    JTextField mDelayField[] = new JTextField[MAXSEQUENCE];
    JToggleButton mRunButton = new JToggleButton("Go");

    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.locogen.LocoGenFrame";
    }

    public String getTitle() {
        return getTitle(LocoNetBundle.bundle().getString("MenuItemSendPacket"));
    }

    public void initComponents() throws Exception {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // handle single-packet part
        add(new JLabel("Send one packet:"));
        {
            JPanel pane1 = new JPanel();
            pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));

            jLabel1.setText("Packet:");
            jLabel1.setVisible(true);

            sendButton.setText("Send");
            sendButton.setVisible(true);
            sendButton.setToolTipText("Send packet");

            packetTextField.setToolTipText("Enter packet as hex pairs, e.g. 82 7D; checksum should be present but is recalculated");

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
        add(new JLabel("Send sequence of packets:"));
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

    public void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);

        memo.getLnTrafficController().addLocoNetListener(~0, this);
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        memo.getLnTrafficController().sendLocoNetMessage(createPacket(packetTextField.getText()));
    }

    // control sequence operation
    int mNextSequenceElement = 0;
    LocoNetMessage mNextEcho = null;
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
     * Process the incoming message to look for the needed echo
     *
     * @param m
     */
    public void message(LocoNetMessage m) {
        log.debug("message");
        // are we running?
        if (!mRunButton.isSelected()) {
            return;
        }
        // yes, is this what we're looking for
        if (!(mNextEcho.equals(m))) {
            return;
        }
        // yes, we got it, do the next
        startSequenceDelay();
    }

    /**
     * Echo has been heard, start delay for next packet
     */
    void startSequenceDelay() {
        log.debug("startSequenceDelay");
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
        log.debug("sendNextItem");
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
            LocoNetMessage m = createPacket(mPacketField[mNextSequenceElement].getText());
            // send it
            mNextEcho = m;
            memo.getLnTrafficController().sendLocoNetMessage(m);
        } else {
            // ask for the next one
            mNextSequenceElement++;
            sendNextItem();
        }
    }

    /**
     * Create a well-formed LocoNet packet from a String
     *
     * @param s
     * @return The packet, with contents filled-in
     */
    LocoNetMessage createPacket(String s) {
        // gather bytes in result
        byte b[] = StringUtil.bytesFromHexString(s);
        if (b.length == 0) {
            return null;  // no such thing as a zero-length message
        }
        LocoNetMessage m = new LocoNetMessage(b.length);
        for (int i = 0; i < b.length; i++) {
            m.setElement(i, b[i]);
        }
        return m;
    }

    /**
     * When the window closes, stop any sequences running
     */
    public void dispose() {
        mRunButton.setSelected(false);
        super.dispose();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LocoGenPanel.class.getName());
}
