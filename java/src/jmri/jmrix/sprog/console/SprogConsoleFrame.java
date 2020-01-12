package jmri.jmrix.sprog.console;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import jmri.jmrix.sprog.SprogConstants;
import jmri.jmrix.sprog.SprogListener;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogReply;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.SprogTrafficController;
import jmri.jmrix.sprog.update.SprogType;
import jmri.jmrix.sprog.update.SprogVersion;
import jmri.jmrix.sprog.update.SprogVersionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for Sprog Console
 *
 * Updated Jan 2010 by Andrew Berridge - fixed errors caused by trying to send
 * some commands while slot manager is active
 * 
 * Updated April 2016 by Andrew Crosland - remove the checks on slot manager
 * status, implement a timeout and look for the correct replies which may be
 * delayed by replies for slot manager.
 *
 * Refactored, I18N
 *
 * @author	Andrew Crosland Copyright (C) 2008, 2016
 */
public class SprogConsoleFrame extends jmri.jmrix.AbstractMonFrame implements SprogListener, SprogVersionListener {

    private SprogSystemConnectionMemo _memo = null;
    // member declarations
    protected javax.swing.JLabel cmdLabel = new javax.swing.JLabel();
    protected javax.swing.JLabel currentLabel = new javax.swing.JLabel();
    protected javax.swing.JButton sendButton = new javax.swing.JButton();
    protected javax.swing.JButton saveButton = new javax.swing.JButton();
    protected javax.swing.JTextField cmdTextField = new javax.swing.JTextField(12);
    protected javax.swing.JTextField currentTextField = new javax.swing.JTextField(12);

    protected JCheckBox ztcCheckBox = new JCheckBox();
    protected JCheckBox blueCheckBox = new JCheckBox();
    protected JCheckBox unlockCheckBox = new JCheckBox();

    protected ButtonGroup speedGroup = new ButtonGroup();
    protected javax.swing.JLabel speedLabel = new javax.swing.JLabel();
    protected JRadioButton speed14Button = new JRadioButton(Bundle.getMessage("ButtonXStep", 14)); // i18n using shared sprogBundle
    protected JRadioButton speed28Button = new JRadioButton(Bundle.getMessage("ButtonXStep", 28));
    protected JRadioButton speed128Button = new JRadioButton(Bundle.getMessage("ButtonXStep", 128));

    protected int modeWord;
    protected int currentLimit = SprogConstants.DEFAULT_I;

    // members for handling the SPROG interface
    SprogTrafficController tc = null;
    String replyString;
    String tmpString = null;
    State state = State.IDLE;

    SprogVersion sv;

    enum State {

        IDLE,
        CURRENTQUERYSENT, // awaiting reply to "I"
        MODEQUERYSENT,    // awaiting reply to "M"
        CURRENTSENT,      // awaiting reply to "I xxx"
        MODESENT,         // awaiting reply to "M xxx"
        WRITESENT   	  // awaiting reply to "W"
    }

    public SprogConsoleFrame(SprogSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String title() {
        return Bundle.getMessage("SprogConsoleTitle");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void init() {
        // connect to TrafficController
        tc = _memo.getSprogTrafficController();
        tc.addSprogListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
	if(tc!=null) {
           tc.removeSprogListener(this);
	}
        super.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC")
    // Ignore unsynchronized access to state
    @Override
    public void initComponents() {
        //SprogMessage msg;
        super.initComponents();

        // Add a nice border to super class
        super.jScrollPane1.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("CommandHistoryTitle")));

        // Let user press return to enter message
        entryField.addActionListener((java.awt.event.ActionEvent e) -> {
            enterButtonActionPerformed(e);
        });

        /*
         * Command panel
         */
        JPanel cmdPane1 = new JPanel();
        cmdPane1.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("SendCommandTitle")));
        cmdPane1.setLayout(new FlowLayout());

        cmdLabel.setText(Bundle.getMessage("CommandLabel"));
        cmdLabel.setVisible(true);

        sendButton.setText(Bundle.getMessage("ButtonSend"));
        sendButton.setVisible(true);
        sendButton.setToolTipText(Bundle.getMessage("SendPacketTooltip"));

        cmdTextField.setText("");
        cmdTextField.setToolTipText(Bundle.getMessage("EnterSPROGCommandTooltip", Bundle.getMessage("ButtonSend")));
        cmdTextField.setMaximumSize(
                new Dimension(cmdTextField.getMaximumSize().width,
                        cmdTextField.getPreferredSize().height)
        );

        cmdTextField.addActionListener((java.awt.event.ActionEvent e) -> {
            sendButtonActionPerformed(e);
        });

        sendButton.addActionListener((java.awt.event.ActionEvent e) -> {
            sendButtonActionPerformed(e);
        });

        cmdPane1.add(cmdLabel);
        cmdPane1.add(cmdTextField);
        cmdPane1.add(sendButton);

        getContentPane().add(cmdPane1);

        /*
         * Speed Step Panel
         */
        JPanel speedPanel = new JPanel();
        speedPanel.setBorder(BorderFactory.createEtchedBorder());
        speedLabel.setText(Bundle.getMessage("SpeedStepModeLabel"));
        speedPanel.add(speedLabel);
        speedPanel.add(speed14Button);
        speedPanel.add(speed28Button);
        speedPanel.add(speed128Button);
        speedGroup.add(speed14Button);
        speedGroup.add(speed28Button);
        speedGroup.add(speed128Button);
        speed14Button.setToolTipText(Bundle.getMessage("ButtonXStepTooltip", 14));
        speed28Button.setToolTipText(Bundle.getMessage("ButtonXStepTooltip", 28));
        speed128Button.setToolTipText(Bundle.getMessage("ButtonXStepTooltip", 128));

        /*
         * Configuration panel
         */
        JPanel configPanel = new JPanel();
        // *** Which versions support current limit ???
        currentLabel.setText(Bundle.getMessage("CurrentLimitLabel"));
        currentLabel.setVisible(true);

        currentTextField.setText("");
        currentTextField.setEnabled(false);
        currentTextField.setToolTipText(Bundle.getMessage("CurrentLimitFieldTooltip"));
        currentTextField.setMaximumSize(
                new Dimension(currentTextField.getMaximumSize().width,
                        currentTextField.getPreferredSize().height
                )
        );

        ztcCheckBox.setText(Bundle.getMessage("ButtonSetZTCMode"));
        ztcCheckBox.setVisible(true);
        ztcCheckBox.setToolTipText(Bundle.getMessage("ButtonSetZTCModeTooltip"));

        blueCheckBox.setText(Bundle.getMessage("ButtonSetBluelineMode"));
        blueCheckBox.setVisible(true);
        blueCheckBox.setEnabled(false);
        blueCheckBox.setToolTipText(Bundle.getMessage("ButtonSetBluelineModeTooltip"));

        unlockCheckBox.setText(Bundle.getMessage("ButtonUnlockFirmware"));
        unlockCheckBox.setVisible(true);
        unlockCheckBox.setEnabled(false);
        unlockCheckBox.setToolTipText(Bundle.getMessage("ButtonUnlockFirmwareTooltip"));

        configPanel.add(currentLabel);
        configPanel.add(currentTextField);
        configPanel.add(ztcCheckBox);
        configPanel.add(blueCheckBox);
        configPanel.add(unlockCheckBox);

        /*
         * Status Panel
         */
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("ConfigurationTitle")));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));

        saveButton.setText(Bundle.getMessage("ButtonApply"));
        saveButton.setVisible(true);
        saveButton.setToolTipText(Bundle.getMessage("ButtonApplyTooltip"));

        saveButton.addActionListener((java.awt.event.ActionEvent e) -> {
            saveButtonActionPerformed(e);
        });

        statusPanel.add(speedPanel);
        statusPanel.add(configPanel);
        statusPanel.add(saveButton);

        getContentPane().add(statusPanel);

        // pack for display
        pack();
        cmdPane1.setMaximumSize(statusPanel.getSize());
        statusPanel.setMaximumSize(statusPanel.getSize());
        pack();

        // Now the GUI is all setup we can get the SPROG version
        _memo.getSprogVersionQuery().requestVersion(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setHelp() {
        addHelpMenu("package.jmri.jmrix.sprog.console.SprogConsoleFrame", true);
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        SprogMessage m = new SprogMessage(cmdTextField.getText());
        // Messages sent by us will not be forwarded back so add to display manually
        nextLine("cmd: \"" + m.toString(_memo.getSprogTrafficController().isSIIBootMode()) + "\"\n", "");
        tc.sendSprogMessage(m, this);
    }

    /**
     * Validate the current limit value entered by the user, depending on the
     * SPROG version.
     */
    @SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC")
    // validateCurrent() is called from synchronised code
    public void validateCurrent() {
        String currentRange = "200 - 996";
        int validLimit = 996;
        if (_memo.getSprogVersion().sprogType.sprogType > SprogType.SPROGIIv3) {
            currentRange = "200 - 2499";
            validLimit = 2499;
        }
        try {
            currentLimit = Integer.parseInt(currentTextField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("CurrentLimitDialogString", currentRange),
                    Bundle.getMessage("SprogConsoleTitle"), JOptionPane.ERROR_MESSAGE);
            currentLimit = validLimit;
            return;
        }
        if ((currentLimit > validLimit) || (currentLimit < 200)) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("CurrentLimitDialogString", currentRange),
                    Bundle.getMessage("SprogConsoleTitle"), JOptionPane.ERROR_MESSAGE);
            currentLimit = validLimit;
        }
    }

    synchronized public void saveButtonActionPerformed(java.awt.event.ActionEvent e) {
        SprogMessage saveMsg;
        int currentLimitForHardware;
        // Send Current Limit if possible
        state = State.CURRENTSENT;
        if (isCurrentLimitPossible()) {
            validateCurrent();
            // Value written is scaled from mA to hardware units
            currentLimitForHardware = (int) (currentLimit * (1/sv.sprogType.getCurrentMultiplier()));
            if (sv.sprogType.sprogType < SprogType.SPROGIIv3) {
                // Hack for SPROG bug where MSbyte of value must be non-zero
                currentLimitForHardware += 256;
            }
            tmpString = String.valueOf(currentLimitForHardware);
            saveMsg = new SprogMessage("I " + tmpString);
        } else {
            // Else send blank message to kick things off
            saveMsg = new SprogMessage(" " + tmpString);
        }
        nextLine("cmd: \"" + saveMsg.toString(_memo.getSprogTrafficController().isSIIBootMode()) + "\"\n", "");
        tc.sendSprogMessage(saveMsg, this);

        // Further messages will be sent from state machine
    }

    @SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC")
    // Called from synchronised code
    public boolean isCurrentLimitPossible() {
        return sv.hasCurrentLimit();
    }

    @SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC")
    // Called from synchronised code
    public boolean isBlueLineSupportPossible() {
        return sv.hasBlueLine();
    }

    @SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC")
    // Called from synchronised code
    public boolean isFirmwareUnlockPossible() {
        return sv.hasFirmwareLock();
    }

    @SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC")
    // Called from synchronised code
    public boolean isZTCModePossible() {
        return sv.hasZTCMode();
    }

    /**
     * Handle a SprogVersion notification.
     * <p>
     * Decode the SPROG version and populate the console gui appropriately with 
     * the features applicable to the version.
     * 
     * @param v The SprogVersion being handled
     */
    @Override
    synchronized public void notifyVersion(SprogVersion v) {
        SprogMessage msg;
        sv = v;
        // Save it for others
        _memo.setSprogVersion(v);
        if (log.isDebugEnabled()) {
            log.debug("Found: {}", sv.toString());
        }
        if (sv.sprogType.isSprog() == false) {
            // Didn't recognize a SPROG so check if it is in boot mode already
            JOptionPane.showMessageDialog(null, Bundle.getMessage("TypeNoSprogPromptFound"),
                    Bundle.getMessage("SprogConsoleTitle"), JOptionPane.ERROR_MESSAGE);
        } else {
            if ((sv.sprogType.sprogType > SprogType.SPROGIIv3) &&(sv.sprogType.sprogType < SprogType.NANO)) {
                currentTextField.setToolTipText(Bundle.getMessage("CurrentLimitFieldTooltip2500"));
            }
            // We know what we're connected to
            setTitle(title() + " - Connected to " + sv.toString());

            // Enable blueline & firmware unlock check boxes
            if (isBlueLineSupportPossible()) {
                if (log.isDebugEnabled()) {
                    log.debug("Enable blueline check box");
                }
                blueCheckBox.setEnabled(true);
                if (log.isDebugEnabled()) {
                    log.debug(Boolean.toString(blueCheckBox.isEnabled()));
                }
            }
            if (isFirmwareUnlockPossible()) {
                if (log.isDebugEnabled()) {
                    log.debug("Enable firmware check box");
                }
                unlockCheckBox.setEnabled(true);
                if (log.isDebugEnabled()) {
                    log.debug(Boolean.toString(unlockCheckBox.isEnabled()));
                }
            }

            ztcCheckBox.setEnabled(isZTCModePossible());

            // Get Current Limit if available
            if (isCurrentLimitPossible()) {
                state = State.CURRENTQUERYSENT;
                msg = new SprogMessage(1);
                msg.setOpCode('I');
                nextLine("cmd: \"" + msg + "\"\n", "");
                tc.sendSprogMessage(msg, this);
                startTimer();
            } else {
                // Set default and get the mode word
                currentLimit = (int) (SprogConstants.DEFAULT_I * sv.sprogType.getCurrentMultiplier());
                currentTextField.setText(String.valueOf(SprogConstants.DEFAULT_I));
                //currentField.setValue(Integer.valueOf(SprogConstants.DEFAULT_I)); // TODO use JSpinner so int
                state = State.MODEQUERYSENT;
                msg = new SprogMessage(1);
                msg.setOpCode('M');
                nextLine("cmd: \"" + msg + "\"\n", "");
                tc.sendSprogMessage(msg, this);
                startTimer();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void notifyMessage(SprogMessage l) { // receive a message and log it
        nextLine("cmd: \"" + l.toString(_memo.getSprogTrafficController().isSIIBootMode()) + "\"\n", "");
    }

    /**
     * Handle a SprogReply in a console specific way.
     * <p>
     * Parse replies from the SPROG using a state machine to determine what we are
     * expecting in response to commands sent to the SPROG. Extract data to populate
     * various fields in the gui.
     * 
     * @param l The SprogReply to be parsed
     */
    @Override
    public synchronized void notifyReply(SprogReply l) { // receive a reply message and log it
        SprogMessage msg;
        int currentLimitFromHardware;
        replyString = l.toString();
        nextLine("rep: \"" + replyString + "\"\n", "");

        // *** Check for error reply
        switch (state) {
            case IDLE:
                log.debug("reply in IDLE state: " + replyString);
                break;
            case CURRENTQUERYSENT:
                // Look for an "I=" reply
                log.debug("reply in CURRENTQUERYSENT state: " + replyString);
                if (replyString.contains("I=")) {
                    stopTimer();
                    int valueLength = 4;
                    if (sv.sprogType.sprogType >= SprogType.SPROGIIv3) {
                        valueLength = 6;
                    }
                    tmpString = replyString.substring(replyString.indexOf("=")
                            + 1, replyString.indexOf("=") + valueLength);
                    log.debug("Current limit string: " + tmpString);
                    try {
                        currentLimitFromHardware = Integer.parseInt(tmpString);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorFrameDialogLimit"),
                                Bundle.getMessage("SprogConsoleTitle"), JOptionPane.ERROR_MESSAGE);
                        state = State.IDLE;
                        return;
                    }
                    // Value written is scaled from hardware units to mA
                    currentLimit = (int)(currentLimitFromHardware * sv.sprogType.getCurrentMultiplier());
                    log.debug("Current limit scale factor: " + sv.sprogType.getCurrentMultiplier());
                    log.debug("Current limit from hardware: " + currentLimitFromHardware + " scaled to: " + currentLimit + "mA");
                    currentTextField.setText(String.valueOf(currentLimit));
                    currentTextField.setEnabled(true);

                    // Next get the mode word
                    state = State.MODEQUERYSENT;
                    msg = new SprogMessage(1);
                    msg.setOpCode('M');
                    nextLine("cmd: \"" + msg + "\"\n", "");
                    tc.sendSprogMessage(msg, this);
                    startTimer();
                }
                break;
            case MODEQUERYSENT:
                log.debug("reply in MODEQUERYSENT state: " + replyString);
                if (replyString.contains("M=")) {
                    stopTimer();
                    tmpString = replyString.substring(replyString.indexOf("=")
                            + 2, replyString.indexOf("=") + 6);
                    // Value returned is in hex
                    try {
                        modeWord = Integer.parseInt(tmpString, 16);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorFrameDialogWord"),
                                Bundle.getMessage("SprogConsoleTitle"), JOptionPane.ERROR_MESSAGE);
                        state = State.IDLE;
                        return;
                    }
                    state = State.IDLE;
                    // Set Speed step radio buttons, etc., according to mode word
                    if ((modeWord & SprogConstants.STEP14_BIT) != 0) {
                        speed14Button.setSelected(true);
                    } else if ((modeWord & SprogConstants.STEP28_BIT) != 0) {
                        speed28Button.setSelected(true);
                    } else {
                        speed128Button.setSelected(true);
                    }
                    if ((modeWord & SprogConstants.ZTC_BIT) != 0) {
                        ztcCheckBox.setSelected(true);
                    }
                    if ((modeWord & SprogConstants.BLUE_BIT) != 0) {
                        blueCheckBox.setSelected(true);
                    }
                }
                break;
            case CURRENTSENT:
                // Any reply will do here
                log.debug("reply in CURRENTSENT state: " + replyString);
                // Get new mode word - assume 128 steps
                modeWord = SprogConstants.STEP128_BIT;
                if (speed14Button.isSelected()) {
                    modeWord = modeWord & ~SprogConstants.STEP_MASK | SprogConstants.STEP14_BIT;
                } else if (speed28Button.isSelected()) {
                    modeWord = modeWord & ~SprogConstants.STEP_MASK | SprogConstants.STEP28_BIT;
                }

                // ZTC mode
                if (ztcCheckBox.isSelected() == true) {
                    modeWord = modeWord | SprogConstants.ZTC_BIT;
                }

                // Blueline mode
                if (blueCheckBox.isSelected() == true) {
                    modeWord = modeWord | SprogConstants.BLUE_BIT;
                }

                // firmware unlock
                if (unlockCheckBox.isSelected() == true) {
                    modeWord = modeWord | SprogConstants.UNLOCK_BIT;
                }

                // Send new mode word
                state = State.MODESENT;
                msg = new SprogMessage("M " + modeWord);
                nextLine("cmd: \"" + msg.toString(_memo.getSprogTrafficController().isSIIBootMode()) + "\"\n", "");
                tc.sendSprogMessage(msg, this);
                break;
            case MODESENT:
                // Any reply will do here
                log.debug("reply in MODESENT state: " + replyString);
                // Write to EEPROM
                state = State.WRITESENT;
                msg = new SprogMessage("W");
                nextLine("cmd: \"" + msg.toString(_memo.getSprogTrafficController().isSIIBootMode()) + "\"\n", "");
                tc.sendSprogMessage(msg, this);
                break;
            case WRITESENT:
                // Any reply will do here
                log.debug("reply in WRITESENT state: " + replyString);
                // All done
                state = State.IDLE;
                break;
            default:
                log.warn("Unhandled state: {}", state);
                break;
        }
    }

    /**
     * Internal routine to handle a timeout.
     */
    synchronized protected void timeout() {
        JOptionPane.showMessageDialog(null, Bundle.getMessage("TypeTimeoutTalkingToSPROG"),
                Bundle.getMessage("Timeout"), JOptionPane.ERROR_MESSAGE);
        state = State.IDLE;
    }

    protected int TIMEOUT = 1000;

    javax.swing.Timer timer = null;

    /**
     * Internal routine to start timer to protect the mode-change.
     */
    protected void startTimer() {
        restartTimer(TIMEOUT);
    }

    /**
     * Internal routine to stop timer, as all is well.
     */
    protected void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    /**
     * Internal routine to handle timer starts {@literal &} restarts.
     *
     * @param delay milliseconds to delay
     */
    protected void restartTimer(int delay) {
        if (timer == null) {
            timer = new javax.swing.Timer(delay, (java.awt.event.ActionEvent e) -> {
                timeout();
            });
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        timer.start();
    }

    private final static Logger log = LoggerFactory.getLogger(SprogConsoleFrame.class);

}
