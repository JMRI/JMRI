package jmri.jmrix.lenz.swing.lz100;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetListener;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal Frame displaying the LZ100 configuration utility
 *
 * This is a configuration utility for the LZ100. It allows the user to set the
 * statup mode (automatic or manual) and to reset the command station.
 *
 * @author Paul Bender Copyright (C) 2005-2010
 */
public class LZ100InternalFrame extends javax.swing.JInternalFrame implements XNetListener {

    private boolean autoMode = false; // holds Auto/Manual Startup Mode.

    private int resetMode = 0; // holds the reset mode;
    static final private int IDLE = 0;
    static final private int ONSENT = 1;
    static final private int OFFSENT = 2;

    private int sendCount = 0; // count the number of times the on/off 
    // sequence for F4 has been sent during a reset

    protected XNetTrafficController tc = null;

    public LZ100InternalFrame(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {

        tc = memo.getXNetTrafficController();

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        setTitle(Bundle.getMessage("CommandConfigTitle"));

        JPanel pane4 = new JPanel();
        pane4.add(new JLabel(Bundle.getMessage("LZ100StartMode")));

        isAutoMode.setVisible(true);
        // isAutoMode
        isAutoMode.setToolTipText(Bundle.getMessage("LZ100AutoModeToolTip"));
        pane4.add(isAutoMode);

        isManualMode.setVisible(true);
        // isManualMode
        isManualMode.setToolTipText(Bundle.getMessage("LZ100ManualModeToolTip"));
        pane4.add(isManualMode);

        // amModeGetButton
        amModeGetButton.setToolTipText(Bundle.getMessage("LZ100GetAMModeToolTip"));
        pane4.add(amModeGetButton);

        // amModeSetButton
        amModeSetButton.setToolTipText(Bundle.getMessage("LZ100SetAMModeToolTip"));
        pane4.add(amModeSetButton);
        getContentPane().add(pane4);

        JPanel pane3 = new JPanel();
        pane3.add(new JLabel(Bundle.getMessage("LZ100OptionLabel")));

        // resetCSButton
        resetCSButton.setToolTipText(Bundle.getMessage("LZ100ResetToolTip"));
        pane3.add(resetCSButton);
        getContentPane().add(pane3);

        // add status
        status.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        status.setVisible(true);
        getContentPane().add(status);

        // and prep for display
        pack();

        // install reset Command Station button handler
        resetCSButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                // ask user confirmation
                if (javax.swing.JOptionPane.OK_OPTION == javax.swing.JOptionPane.showConfirmDialog(
                        null, Bundle.getMessage("LZ100ConfirmResetDialog"),
                                Bundle.getMessage("QuestionTitle"),
                        javax.swing.JOptionPane.OK_CANCEL_OPTION,
                        javax.swing.JOptionPane.WARNING_MESSAGE)) {
                    // indeed send reset commands
                    resetLZ100CS();
                }
            }
        }
        );

        // install Auto/Manual mode retreive button handler.
        amModeGetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                amModeGet();

            }
        }
        );

        // install Auto/Manual mode Save button handler.
        amModeSetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                amModeSave();

            }
        }
        );

        // install Auto mode button handler.
        isAutoMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                autoModeAction();
            }
        }
        );

        // install Manual  mode button handler.
        isManualMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                manualModeAction();
            }
        }
        );

        // configure internal frame options
        setClosable(false);     // don't let the user close this frame
        setResizable(false);    // don't let the user resize this frame
        setIconifiable(false);  // don't let the user minimize this frame
        setMaximizable(false);  // don't let the user maximize this frame

        // make the internal frame visible
        this.setVisible(true);

        // Check for XpressNet Connection, add listener if present,
        //  warn if not.
        if (tc != null) {
            tc.addXNetListener(~0, this);
        } else {
            log.warn("No XpressNet connection, so panel won't function");
        }
    }

    boolean read = false;

    JLabel status = new JLabel(" ");
    JButton resetCSButton = new JButton(Bundle.getMessage("LZ100Reset"));
    JRadioButton isAutoMode = new JRadioButton(Bundle.getMessage("Automatic"));
    JRadioButton isManualMode = new JRadioButton(Bundle.getMessage("Manual"));
    JToggleButton amModeGetButton = new JToggleButton(Bundle.getMessage("LZ100GetAMMode"));
    JToggleButton amModeSetButton = new JToggleButton(Bundle.getMessage("LZ100SetAMMode"));

    /**
     * Listen for responses from the LZ100.
     */
    @Override
    synchronized public void message(XNetReply l) {
        if (l.isOkMessage()) {
            /* this was an "OK" message
             We're only paying attention to it if we're 
             resetting the command station 
             */
            if (status.getText().equals(Bundle.getMessage("LZ100StatusSetMode"))) {
                status.setText(Bundle.getMessage("StatusOK"));
            }
            if (resetMode == OFFSENT) {
                XNetMessage msgon = XNetMessage.getFunctionGroup1OpsMsg(0, false, false, false, false, true);
                sendCount--;
                resetMode = ONSENT;
                tc.sendXNetMessage(msgon, this);
            } else if (resetMode == ONSENT) {
                XNetMessage msgoff = XNetMessage.getFunctionGroup1OpsMsg(0, false, false, false, false, false);
                if (sendCount >= 0) {
                    resetMode = OFFSENT;
                } else {
                    resetMode = IDLE;
                    resetCSButton.setEnabled(true);
                    status.setText(Bundle.getMessage("LZ100ResetFinished"));
                }
                tc.sendXNetMessage(msgoff, this);
            }
        } else if (l.getElement(0) == XNetConstants.CS_REQUEST_RESPONSE
                && l.getElement(1) == XNetConstants.CS_STATUS_RESPONSE) {
            int statusByte = l.getElement(2);
            if ((statusByte & 0x04) == 0x04) {
                isAutoMode.setSelected(true);
                isManualMode.setSelected(false);
                autoMode = true;
                status.setText(Bundle.getMessage("StatusOK"));
            } else {
                isAutoMode.setSelected(false);
                isManualMode.setSelected(true);
                autoMode = false;
                status.setText(Bundle.getMessage("StatusOK"));
            }
        }
    }

    /**
     * Listen for the messages to the LI100/LI101.
     */
    @Override
    synchronized public void message(XNetMessage l) {
    }

    /**
     * Handle a timeout notification.
     */
    @Override
    public void notifyTimeout(XNetMessage msg) {
        log.debug("Notified of timeout on message {}", msg.toString());
    }

    /**
     * Reset the command station to factory defaults.
     */
    synchronized void resetLZ100CS() {
        resetCSButton.setEnabled(false);
        status.setText(Bundle.getMessage("LZ100StatusReset"));
        // the Command station is reset by sending F4 25 times for address 00
        XNetMessage msgon = XNetMessage.getFunctionGroup1OpsMsg(0, false, false, false, true, false);
        resetMode = ONSENT;
        sendCount = 25;

        tc.sendXNetMessage(msgon, this);
    }

    /**
     * Get the current automatic/manual mode.
     */
    synchronized void amModeGet() {
        XNetMessage msg = XNetMessage.getCSStatusRequestMessage();
        tc.sendXNetMessage(msg, this);
        amModeGetButton.setSelected(false);
        status.setText(Bundle.getMessage("LZ100StatusRetrieveMode"));
    }

    /**
     * Set the current automatic/manual mode.
     */
    synchronized void amModeSave() {
        if (autoMode) {
            log.debug("Auto Mode True");
        } else {
            log.debug("Auto Mode False");
        }
        XNetMessage msg = XNetMessage.getCSAutoStartMessage(autoMode);
        tc.sendXNetMessage(msg, this);
        amModeSetButton.setSelected(false);
        status.setText(Bundle.getMessage("LZ100StatusSetMode"));
    }

    /**
     * Toggle Auto Power-up Mode.
     */
    synchronized void autoModeAction() {
        log.debug("Auto Mode Action Called");
        isAutoMode.setSelected(true);
        isManualMode.setSelected(false);
        autoMode = true;
    }

    /**
     * Toggle Manual Power-up Mode.
     */
    synchronized void manualModeAction() {
        log.debug("Manual Mode Action Called");
        isAutoMode.setSelected(false);
        isManualMode.setSelected(true);
        autoMode = false;
    }

    @Override
    public void dispose() {
        // take apart the JFrame
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(LZ100Frame.class);

}
