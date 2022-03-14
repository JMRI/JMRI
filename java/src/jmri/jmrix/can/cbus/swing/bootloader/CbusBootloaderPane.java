package jmri.jmrix.can.cbus.swing.bootloader;

import static javax.swing.SwingUtilities.getWindowAncestor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusSend;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.util.FileUtil;
import jmri.util.ThreadingUtil;
import jmri.util.TimerUtil;
import jmri.util.swing.BusyDialog;
import jmri.util.swing.TextAreaFIFO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootloader client for uploading CBUS node firmware.
 *
 * @author Andrew Crosland Copyright (C) 2020
 */
public class CbusBootloaderPane extends jmri.jmrix.can.swing.CanPanel
        implements CanListener {

    // EEPROM base address for older PIC18 devices
    protected static final int EE_START_2580 = 0xf00000;
    // EEPROM base address for for PIC 18F26K83 and related family members
    protected static final int EE_START_26K83 = 0x310000;
    // EEPROM base address for for PIC 18F27Q84 and related family members
    protected static final int EE_START_27Q84 = 0x380000;

    private TrafficController tc;
    private CbusSend send;
    private CbusPreferences preferences;

    private JRadioButtonMenuItem slowWrite;
    private JRadioButtonMenuItem fastWrite;
    protected JTextField nodeNumberField = new JTextField(6);
    protected JCheckBox configCheckBox = new JCheckBox();
    protected JCheckBox eepromCheckBox = new JCheckBox();
    protected JButton programButton;
    protected JButton openFileChooserButton;
    protected JButton readNodeParamsButton;
    private final TextAreaFIFO bootConsole;
    private static final int MAX_LINES = 5000;
    private final JFrame topFrame = (JFrame) getWindowAncestor(this);

    // to find and remember the hex file
    final javax.swing.JFileChooser hexFileChooser =
            new JFileChooser(FileUtil.getUserFilesPath());
    // File to hold name of hex file
    transient HexFile hexFile = null;

    CbusParameters hardwareParams = null;
    CbusParameters fileParams = null;

    boolean hexForBootloader = false;

    int nodeNumber;
    int nextParam;

    private int eeStart;

    BusyDialog busyDialog;

    /**
     * Bootloader state machine states
     */
    protected enum BootState {
        IDLE,
        START_BOOT,
        CHECK_BOOT_MODE,
        INIT_PROG_SENT,
        PROG_DATA,
        PROG_PAUSE,
        PROG_CHECK_SENT,
        INIT_CONFIG_SENT,
        CONFIG_DATA,
        CONFIG_CHECK_SENT,
        INIT_EEPROM_SENT,
        EEPROM_DATA,
        EEPROM_CHECK_SENT
    }
    protected BootState bootState = BootState.IDLE;
    protected int bootAddress;
    protected int checksum;
    protected int dataFramesSent;
    protected boolean writeInFlight = false;
    protected int dataTimeout;


    public CbusBootloaderPane() {
        super();
        bootConsole = new TextAreaFIFO(MAX_LINES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);

        // connect to the CanInterface
        tc = memo.getTrafficController();
        addTc(tc);

        send = new CbusSend(memo, bootConsole);

        preferences = jmri.InstanceManager.getDefault(jmri.jmrix.can.cbus.CbusPreferences.class);

        init();
    }


    /**
     * Not sure this comment really applies here asa init() does not use the tc
     * Don't use initComponent() as memo doesn't yet exist when that gets called.
     * Instead, call init() function from initComponents(memo)
     */
    public void init() {
        bootConsole.setEditable(false);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Node number selector
        JPanel nnPane = new JPanel();
        nnPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("BootNodeNumber")));
        nnPane.add(nodeNumberField);

        nodeNumberField.setText("");
        nodeNumberField.setToolTipText(Bundle.getMessage("BootNodeNumberTT"));
        nodeNumberField.setMaximumSize(nodeNumberField.getPreferredSize());
        // Reset the buttons and clear parameters when a new node is selected
        nodeNumberField.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        resetButtons();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        resetButtons();
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        resetButtons();
                    }

                    public void resetButtons() {
                        openFileChooserButton.setEnabled(false);
                        programButton.setEnabled(false);
                        hardwareParams = null;
                    }
                }
        );
        nnPane.add(nodeNumberField);

        // Memory options
        configCheckBox.setText(Bundle.getMessage("BootWriteConfigWords"));
        configCheckBox.setVisible(true);
        eepromCheckBox.setEnabled(true);
        eepromCheckBox.setSelected(false);
        configCheckBox.setToolTipText(Bundle.getMessage("BootWriteConfigWordsTT"));

        eepromCheckBox.setText(Bundle.getMessage("BootWriteEeprom"));
        eepromCheckBox.setVisible(true);
        eepromCheckBox.setEnabled(true);
        eepromCheckBox.setSelected(false);
        eepromCheckBox.setToolTipText(Bundle.getMessage("BootWriteEepromTT"));

        JPanel memoryPane = new JPanel();
        memoryPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("BootMemoryOptions")));
        memoryPane.setLayout(new BoxLayout(memoryPane, BoxLayout.X_AXIS));
        memoryPane.add(configCheckBox);
        memoryPane.add(eepromCheckBox);

        JPanel selectPane = new JPanel();
        selectPane.setLayout(new BoxLayout(selectPane, BoxLayout.X_AXIS));
        selectPane.add(nnPane);
        selectPane.add(memoryPane);

        // Create buttons
        readNodeParamsButton = new JButton(Bundle.getMessage("BootReadNodeParams"));
        readNodeParamsButton.setVisible(true);
        readNodeParamsButton.setEnabled(true);
        readNodeParamsButton.setToolTipText(Bundle.getMessage("BootReadNodeParamsTT"));
        readNodeParamsButton.addActionListener((java.awt.event.ActionEvent e) -> {
            readNodeParamsButtonActionPerformed(e);
        });

        openFileChooserButton = new JButton(Bundle.getMessage("BootChooseFile"));
        openFileChooserButton.setVisible(true);
        openFileChooserButton.setEnabled(false);
        openFileChooserButton.setToolTipText(Bundle.getMessage("BootChooseFileTT"));
        openFileChooserButton.addActionListener((java.awt.event.ActionEvent e) -> {
            openFileChooserButtonActionPerformed(e);
        });

        programButton = new JButton(Bundle.getMessage("BootStartProgramming"));
        programButton.setVisible(true);
        programButton.setEnabled(false);
        programButton.setToolTipText(Bundle.getMessage("BootStartProgrammingTT"));
        programButton.addActionListener((java.awt.event.ActionEvent e) -> {
            programButtonActionPerformed(e);
        });

        // add pane to hold buttons
        JPanel buttonPane = new JPanel();
        buttonPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), ""));
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
        buttonPane.add(readNodeParamsButton);
        buttonPane.add(openFileChooserButton);
        buttonPane.add(programButton);

        JPanel topPane = new JPanel();
        topPane.setLayout(new BoxLayout(topPane, BoxLayout.Y_AXIS));
        topPane.add(selectPane);
        topPane.add(buttonPane);

        // Scroll pane for feedback area
        JScrollPane feedbackScroll = new JScrollPane(bootConsole);
        feedbackScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("BootConsole")));
        feedbackScroll.setPreferredSize(new Dimension(400, 200));

        // Now add to a border layout so that scroll pane will absorb space
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BorderLayout());
        pane1.add(topPane, BorderLayout.PAGE_START);
        pane1.add(feedbackScroll, BorderLayout.CENTER);

        add(pane1);

        setVisible(true);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return prependConnToString(Bundle.getMessage("MenuItemBootloader"));
    }


    /**
     * Set Menu Options, e.g., which checkboxes, etc., should be checked
     */
    private void setMenuOptions(){
        slowWrite.setSelected(false);
        fastWrite.setSelected(false);

        switch (preferences.getBootWriteDelay()) {
            case 10:
                fastWrite.setSelected(true);
                break;
            case 50:
                slowWrite.setSelected(true);
                break;
            default:
                break;
        }
    }


    /**
     * Creates a Menu List.
     *
     * {@inheritDoc}
     */
    @Override
    public List<JMenu> getMenus() {
        List<JMenu> menuList = new ArrayList<>();

        JMenu optionsMenu = new JMenu(Bundle.getMessage("Options"));

        JMenu writeSpeedMenu = new JMenu(Bundle.getMessage("BootWriteSpeed"));
        ButtonGroup backgroundFetchGroup = new ButtonGroup();

        slowWrite = new JRadioButtonMenuItem(Bundle.getMessage("Slow"));
        fastWrite = new JRadioButtonMenuItem(Bundle.getMessage("Fast"));

        backgroundFetchGroup.add(slowWrite);
        backgroundFetchGroup.add(fastWrite);

        writeSpeedMenu.add(slowWrite);
        writeSpeedMenu.add(fastWrite);

        optionsMenu.add(writeSpeedMenu);

        menuList.add(optionsMenu);

        // saved preferences go through the cbus table model so they can be actioned immediately
        // they'll be also saved by the table, not here.

         // values need to match setMenuOptions()
        ActionListener writeSpeedListener = ae -> {
            if (slowWrite.isSelected()) {
                preferences.setBootWriteDelay(CbusNode.BOOT_PROG_TIMEOUT_SLOW);
            }
            else if (fastWrite.isSelected()) {
                preferences.setBootWriteDelay(CbusNode.BOOT_PROG_TIMEOUT_FAST);
            }
        };
        slowWrite.addActionListener(writeSpeedListener);
        slowWrite.addActionListener(writeSpeedListener);

        setMenuOptions();

        return menuList;
    }


    /**
     * Get the delay to be inserted between bootloader data writes
     *
     * @return Delay in ms
     */
    int getWriteDelay() {
        if (slowWrite.isSelected()) {
            return CbusNode.BOOT_PROG_TIMEOUT_SLOW;
        }
        return CbusNode.BOOT_PROG_TIMEOUT_FAST;
    }


    /**
     * Kick off the reading of parameters from the node, starting with parameter
     * 0, the number of parameters
     *
     * @param e
     */
    private void readNodeParamsButtonActionPerformed(java.awt.event.ActionEvent e) {
        try {
            nodeNumber = Integer.parseInt(nodeNumberField.getText());
        } catch (NumberFormatException e1) {
            addToLog(Bundle.getMessage("BootInvalidNode"));
            log.error("Invalid node number {}", nodeNumberField.getText());
            return;
        }
        // Read the parameters from the chosen node
        addToLog(Bundle.getMessage("BootReadingParams"));
        hardwareParams = new CbusParameters();
        nextParam = 0;
        busyDialog = new BusyDialog(topFrame, Bundle.getMessage("BootReadingParams"), false);
        busyDialog.start();
        requestParam(nextParam);
    }


    /**
     * Let the user choose the hex file and check that it is suitable for the
     * selected node.
     *
     * @param e
     */
    private void openFileChooserButtonActionPerformed(java.awt.event.ActionEvent e) {
        // start at current file, show dialog
        int retVal = hexFileChooser.showOpenDialog(this);

        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            hexFile = new HexFile(hexFileChooser.getSelectedFile().getPath(), eeStart);
            log.debug("hex file chosen: {}", hexFile.getName());
            addToLog(MessageFormat.format(Bundle.getMessage("BootFileChosen"), hexFile.getName()));
            try {
                hexFile.openRd();
                hexFile.read();
                fileParams = new CbusParameters().validate(hexFile, hardwareParams);
                if (fileParams.areValid()) {
                    addToLog(MessageFormat.format(Bundle.getMessage("BootHexFileFoundParameters"), fileParams.toString()));
                    addToLog(MessageFormat.format(Bundle.getMessage("BootHexFileParametersMatch"), hardwareParams.toString()));
                    programButton.setEnabled(true);
                } else {
                    addToLog(Bundle.getMessage("BootHexFileParametersMismatch"));
                }
                if (hardwareParams.getLoadAddress() == 0) {
                    // Special case of rewriting the bootloader for Pi-SPROG One
                    addToLog(Bundle.getMessage("BootBoot"));
                    hexForBootloader = true;
                    programButton.setEnabled(true);
                }
            } catch (IOException ex) {
                log.error("Error opening hex file");
                addToLog(Bundle.getMessage("BootHexFileOpenFailed"));
            }
        }
    }


    /**
     * Send BOOTM OPC to put module in boot mode
     *
     * @param e
     */
    private void programButtonActionPerformed(java.awt.event.ActionEvent e) {
        if (hasActiveTimers()){
            return;
        }
        openFileChooserButton.setEnabled(false);
        programButton.setEnabled(false);
        busyDialog = new BusyDialog(topFrame, Bundle.getMessage("BootLoading"), false);
        busyDialog.start();
        setStartBootTimeout();
        bootState = BootState.START_BOOT;
        CanMessage m = CbusMessage.getBootEntry(nodeNumber, 0);
        tc.sendCanMessage(m, null);
    }


    /**
     * Process some outgoing CAN frames
     * <p>
     * The CBUS bootloader is "fire and forget", there is no positive
     * acknowledgement. We have to wait an indeterminate time and assume the
     * write was successful.
     * <p>
     * A PIC based node will halt execution for 2ms whilst FLASH operations
     * (erase and/or write) complete, during which time I/O will not be serviced.
     * This is probably OK with CAN transport, assuming the ECAN continues to
     * accept frames. With serial (UART) transport, as used by Pi-SPROG, the
     * timing is much more critical as a single missed character will corrupt
     * the node firmware.
     * <p>
     * Furthermore, on some platforms, e.g., Raspberry Pi, there can be
     * considerable delays between the call to the traffic controller
     * sendMessage() method and the message being sent by the transmit thread.
     * This may be due to Flash file system operations and could be affected by
     * the speed of the SD card. Once the message leaves the transmit thread, we
     * are at the mercy of the underlying OS, where there can be further delays.
     * <p>
     * We could set an overlong timeout, but that would slow down the bootloading
     * process in all cases.
     * <p>
     * To improve things somewhat we wait until the message has definitely
     * reached the TC transmit thread, by looking for bootloader data write
     * messages here. Testing indicates this is a marked improvement with no
     * failures observed.
     *
     * @param m CanMessage
     */
    @Override
    public void message(CanMessage m) {
        if ((bootState == BootState.PROG_DATA)
                || (bootState == BootState.CONFIG_DATA)
                || (bootState == BootState.EEPROM_DATA)) {
            if (m.isExtended() ) {
                if (CbusMessage.isBootWriteData(m)) {
                    log.debug("Boot data write message {}", m);
                    writeInFlight = false;
                    setDataTimeout(dataTimeout);
                }
            }
        }
    }


    /**
     * Processes incoming CAN replies
     * <p>
     * The bootloader is only interested in standard parameter responses and
     * extended bootloader responses.
     *
     * {@inheritDoc}
     */
    @Override
    public void reply(CanReply r) {

        if ( r.isRtr() ) {
            return;
        }

        if (!r.isExtended() ) {
            log.debug("Standard Reply {}", r);

            handleStandardReply(r);
        } else {
            log.debug("Extended Reply {} in state {}", r, bootState);
            // Extended messages are only used by the bootloader

            handleExtendedReply(r);
        }
    }


    /**
     * Handle standard ID CAN replies
     *
     * @param r Can reply
     */
    private void handleStandardReply(CanReply r) {
        int opc = CbusMessage.getOpcode(r);
        int nn = (r.getElement(1) * 256 ) + r.getElement(2);
        if (nn != nodeNumber) {
            log.debug("NN {} Not for me {}", nn, nodeNumber);
            return;
        }

        if ( opc == CbusConstants.CBUS_PARAN) { // response from node
            clearAllParamTimeout();

            hardwareParams.setParam(r.getElement(3), r.getElement(4));
            if (++nextParam < (hardwareParams.getParam(0) + 1)) {
                // Read next
                requestParam(nextParam);
            } else {
                // Done reading
                hardwareParams.setValid(true);
                addToLog(MessageFormat.format(Bundle.getMessage("BootNodeParametersFinished"), hardwareParams.toString()));
                busyDialog.finish();
                busyDialog = null;
                // Set EEPROM base address depending on module device type
                if (hardwareParams.getParam(9) == 20) {
                    log.debug("Using EEPROM base address for PIC 18F25-26K83");
                    eeStart = EE_START_26K83;
                } else if ((hardwareParams.getParam(9) == 21) || (hardwareParams.getParam(9) == 22)) {
                    log.debug("Using EEPROM base address for PIC 18F27-47-57Q84");
                    eeStart = EE_START_27Q84;
                } else {
                    // Other PIC 18
                    log.debug("Using EEPROM base address for Generic PIC18");
                    eeStart = EE_START_2580;
                }
                openFileChooserButton.setEnabled(true);
            }
        } else {
            // ignoring OPC
        }
    }


    /**
     * Handle extended ID CAN replies
     * <p>
     * Handle the reply in the bootloader state machine.
     *
     * @param r Can reply
     */
    private void handleExtendedReply(CanReply r) {
        // A boot error message indicates a checksum error
        if (CbusMessage.isBootError(r)) {
            clearCheckTimeout();
            // Checksum verify failed
            log.error("Node {} checksum failed", nodeNumber);
            addToLog(MessageFormat.format(Bundle.getMessage("BootChecksumFailed"), nodeNumber));
            endProgramming();
            return;
        }

        switch (bootState) {
            default:
                break;

            case CHECK_BOOT_MODE:
                clearCheckBootTimeout();
                if (CbusMessage.isBootConfirm(r)) {
                    // The node is in boot mode so we can start programming
                    startProgramming(hardwareParams.getLoadAddress(), BootState.INIT_PROG_SENT);
                }
                break;

            case PROG_CHECK_SENT:
            case CONFIG_CHECK_SENT:
            case EEPROM_CHECK_SENT:
                // Expecting reply to checksum verification, move on to next memory region
                if (CbusMessage.isBootOK(r)) {
                    nextRegion();
                }
                break;
        }
    }


    /**
     * Send data to the hardware and keep a running checksum
     *
     * @param address load address
     * @param d       byte array of data being written
     * @param timeout timeout for write operation
     */
    protected void sendData(int address, byte [] d, int timeout) {
        updateChecksum(d);
        bootAddress += 8;
        dataFramesSent++;
        writeInFlight = true;
        dataTimeout = timeout;
        CanMessage m = CbusMessage.getBootWriteData(d, 0);
        log.debug("Write frame {} at address {} {}", dataFramesSent, Integer.toHexString(address), m);
        addToLog(MessageFormat.format(Bundle.getMessage("BootAddress"), Integer.toHexString(address)));
        tc.sendCanMessage(m, null);
    }


    /**
     * Write the next data frame for the bootloader
     * <p>
     * CONFIG and EEPROM require a longer timeout as the node bootloader writes
     * them one byte at a time.
     *
     * @return true if there was data to write
     */
    protected boolean writeNextData() {
        byte [] d;

        log.debug("writeNextData()");
        if ((bootAddress == 0x7f8) && (hexForBootloader == true)) {
            log.debug("Pause for bootloader reset");
            // Pause at end of bootloader code to allow time for node to reset
            bootAddress = 0x800;
            checksum = 0;
            bootState = BootState.PROG_PAUSE;
            setPauseTimeout();
            return true;
        } else if (bootAddress < hexFile.getProgEnd()) {
            d = hexFile.getData(bootAddress, 8);
            sendData(bootAddress, d, getWriteDelay());
            return true;
        } else if ((bootAddress >= HexFile.CONFIG_START) && (bootAddress < hexFile.getConfigEnd())) {
            d = hexFile.getConfig(bootAddress - HexFile.CONFIG_START, 8);
            sendData(bootAddress, d, CbusNode.BOOT_CONFIG_TIMEOUT_TIME);
            return true;
        } else if ((bootAddress >= eeStart) && (bootAddress < hexFile.getEeEnd())) {
            d = hexFile.getEeprom(bootAddress - eeStart, 8);
            sendData(bootAddress, d, CbusNode.BOOT_CONFIG_TIMEOUT_TIME);
            return true;
        }

        log.debug("No more data to send {}", Integer.toHexString(bootAddress));
        return false;
    }


    private void nextRegion() {
        clearCheckTimeout();
        log.debug("Node {} checksum OK", nodeNumber);
        addToLog(MessageFormat.format(Bundle.getMessage("BootChecksumOK"), nodeNumber));
        // Move onto next memory region
        if ((bootState == BootState.PROG_CHECK_SENT) && configCheckBox.isSelected()) {
            // Move onto config words
            log.debug("Next region: Config words");
            startProgramming(0x300000, BootState.INIT_CONFIG_SENT);
        } else if ((bootState == BootState.PROG_CHECK_SENT) && eepromCheckBox.isSelected()
                || (bootState == BootState.CONFIG_CHECK_SENT) && eepromCheckBox.isSelected()) {
            // Move onto EEPROM
            log.debug("Next region: EEPROM");
            startProgramming(eeStart, BootState.INIT_EEPROM_SENT);
        } else {
            // Done writing
            log.debug("Next region: Done");
            sendReset();
        }
    }


    /**
     * Setup to start programming
     *
     * @param address Start address
     */
    private void startProgramming(int address, BootState state) {
        bootAddress = address;
        checksum = 0;
        dataFramesSent = 0;
        bootState = state;
        log.debug("Start Programming at address {}", Integer.toHexString(bootAddress));
        addToLog(MessageFormat.format(Bundle.getMessage("BootStartAddress"), Integer.toHexString(bootAddress)));
        setInitTimeout();
        CanMessage m = CbusMessage.getBootInitialise(bootAddress, 0);
        tc.sendCanMessage(m, null);
    }


    /**
     * Send bootloader reset frame to put the node back into operating mode.
     *
     * There will be no reply to this.
     */
    protected void sendReset() {
        endProgramming();
        CanMessage m = CbusMessage.getBootReset(0);
        log.debug("Done. Resetting node...");
        addToLog(Bundle.getMessage("BootFinished"));
        tc.sendCanMessage(m, null);
    }


    /**
     * Tidy up after programming success or failure
     */
    private void endProgramming() {
        if (busyDialog != null) {
            busyDialog.finish();
            busyDialog = null;
        }
        openFileChooserButton.setEnabled(true);
        programButton.setEnabled(true);
        bootState = BootState.IDLE;
    }


    /**
     * Add array of bytes to checksum
     *
     * @param d the array of bytes
     */
    protected void updateChecksum(byte [] d) {
        for (int i = 0; i < d.length; i++) {
            // bytes are signed so Cast to int and take the 8 LSBs
            checksum += d[i] & 0xFF;
        }
    }


    /**
     * Request a single Parameter from a Physical Node
     * <p>
     * Will not send the request if there are existing active timers.
     * Starts Parameter timeout
     *
     * @param param Parameter Index Number, Index 0 is total parameters
     */
    public void requestParam(int param){
        if (hasActiveTimers()){
            return;
        }
        setAllParamTimeout(param);
        send.rQNPN(nodeNumber, param);
    }


    /**
     * See if any timers are running, ie waiting for a response from a physical Node.
     *
     * @return true if timers are running else false
     */
    protected boolean hasActiveTimers() {
        return allParamTask != null
            || startBootTask != null
            || checkBootTask != null
            || pauseTask != null
            || programTask != null
            || initTask != null
            || dataTask != null
            || checkTask != null
            || configTask != null
            || eeTask != null;
    }


    private TimerTask allParamTask;
    private TimerTask startBootTask;
    private TimerTask checkBootTask;
    private TimerTask pauseTask;
    private TimerTask programTask;
    private TimerTask initTask;
    private TimerTask dataTask;
    private TimerTask checkTask;
    private TimerTask configTask;
    private TimerTask eeTask;


    /**
     * Stop timer for a single parameter fetch
     */
    private void clearAllParamTimeout() {
        if (allParamTask != null) {
            allParamTask.cancel();
            allParamTask = null;
        }
    }


    /**
     * Start timer for a Parameter request
     * If 10 timeouts are counted, aborts loop, sets 8 parameters to 0
     * and node events array to 0
     */
    private void setAllParamTimeout(int index) {
        clearAllParamTimeout(); // resets if timer already running
        allParamTask = new TimerTask() {
            @Override
            public void run() {
                allParamTask = null;
                if (busyDialog != null) {
                    busyDialog.finish();
                    busyDialog = null;
                    hardwareParams.setValid(true);
                    log.error("Failed to read module parameters from node {}", nodeNumber);
                    addToLog(MessageFormat.format(Bundle.getMessage("BootNodeParametersFailed"), nodeNumber));
                }
            }
        };
        TimerUtil.schedule(allParamTask, CbusNode.SINGLE_MESSAGE_TIMEOUT_TIME);
    }


    /**
     * Stop timer for boot mode request
     */
    private void clearStartBootTimeout() {
        if (startBootTask != null) {
            startBootTask.cancel();
            startBootTask = null;
        }
    }


    /**
     * Start timer for boot mode request
     * <p>
     * We don't get a response, so timeout is expected and we kick off a check
     * for boot mode
     */
    private void setStartBootTimeout() {
        clearStartBootTimeout(); // resets if timer already running
        startBootTask = new TimerTask() {
            @Override
            public void run() {
                startBootTask = null;
                setCheckBootTimeout();
                bootState = BootState.CHECK_BOOT_MODE;
                CanMessage m = CbusMessage.getBootTest(0);
                tc.sendCanMessage(m, null);
            }
        };
        TimerUtil.schedule(startBootTask, CbusNode.BOOT_ENTRY_TIMEOOUT_TIME);
    }


    /**
     * Stop timer for boot mode check
     */
    private void clearCheckBootTimeout() {
        if (checkBootTask != null) {
            checkBootTask.cancel();
            checkBootTask = null;
        }
    }


    /**
     * Start timer for boot mode check
     */
    private void setCheckBootTimeout() {
        clearCheckBootTimeout(); // resets if timer already running
        checkBootTask = new TimerTask() {
            @Override
            public void run() {
                checkBootTask = null;
                log.error("Timeout checking for boot mode");
                addToLog(Bundle.getMessage("BootTimeout"));
                endProgramming();
            }
        };
        TimerUtil.schedule(checkBootTask, CbusNode.BOOT_SINGLE_MESSAGE_TIMEOUT_TIME);
    }


    /**
     * Stop timer for initialisation
     */
    private void clearInitTimeout() {
        if (initTask != null) {
            initTask.cancel();
            initTask = null;
        }
    }


    /**
     * Start timer for initialisation
     * <p>
     * No reply so timeout is expected. Start sending data.
     */
    private void setInitTimeout() {
        log.debug("setInitTimeout()");
        clearInitTimeout(); // resets if timer already running
        initTask = new TimerTask() {
            @Override
            public void run() {
                initTask = null;
                if (bootState == BootState.INIT_PROG_SENT) {
                    bootState = BootState.PROG_DATA;
                    log.debug("Bootstate is PROG_DATA");
                } else if (bootState == BootState.INIT_CONFIG_SENT) {
                    bootState = BootState.CONFIG_DATA;
                    log.debug("Bootstate is CONFIG_DATA");
                } else {
                    bootState = BootState.EEPROM_DATA;
                    log.debug("Bootstate is EEPROM_DATA");
                }
                writeNextData();
            }
        };
        TimerUtil.schedule(initTask, CbusNode.BOOT_SINGLE_MESSAGE_TIMEOUT_TIME);
    }


    /**
     * Stop timer for bootloader reset pause
     */
    private void clearPauseTimeout() {
        if (pauseTask != null) {
            pauseTask.cancel();
            pauseTask = null;
        }
    }


    /**
     * Start timer for bootloader reset pause
     * <p>
     * No reply so timeout is expected. Initialise to new address for application.
     */
    private void setPauseTimeout() {
        clearPauseTimeout(); // resets if timer already running
        pauseTask = new TimerTask() {
            @Override
            public void run() {
                pauseTask = null;
                hexForBootloader = false;
                bootState = BootState.INIT_PROG_SENT;
                log.debug("Start writing at address {}", Integer.toHexString(bootAddress));
                addToLog(MessageFormat.format(Bundle.getMessage("BootStartAddress"), Integer.toHexString(bootAddress)));
                setInitTimeout();
                CanMessage m = CbusMessage.getBootInitialise(bootAddress, 0);
                tc.sendCanMessage(m, null);
                writeNextData();
            }
        };
        TimerUtil.schedule(pauseTask, CbusNode.BOOT_PAUSE_TIMEOUT_TIME);
    }


    /**
     * Stop timer for data writes
     */
    private void clearDataTimeout() {
        if (dataTask != null) {
            dataTask.cancel();
            dataTask = null;
        }
    }


    /**
     * Start timer for data writes
     * <p>
     * No reply so timeout is expected. Send more data.
     */
    private void setDataTimeout(int timeout) {
        clearDataTimeout(); // resets if timer already running
        dataTask = new TimerTask() {
            @Override
            public void run() {
                dataTask = null;
                if (!writeNextData()) {
                    // No data to send so send checksum
                    if (bootState == BootState.PROG_DATA) {
                        bootState = BootState.PROG_CHECK_SENT;
                    } else if (bootState == BootState.CONFIG_DATA) {
                        bootState = BootState.CONFIG_CHECK_SENT;
                    } else {
                        bootState = BootState.EEPROM_CHECK_SENT;
                    }
                    addToLog(Bundle.getMessage("BootVerifyChecksum"));
                    log.debug("Sending checksum {} as 2s complement {}", checksum, 0 - checksum);
                    setCheckTimeout();
                    CanMessage m = CbusMessage.getBootCheck(0 - checksum, 0);
                    tc.sendCanMessage(m, null);
                }
            }
        };
        TimerUtil.schedule(dataTask, timeout);
    }


    /**
     * Stop timer for checksum verification
     */
    private void clearCheckTimeout() {
        if (checkTask != null) {
            checkTask.cancel();
            checkTask = null;
        }
    }


    /**
     * Start timer for checksum verification
     */
    private void setCheckTimeout() {
        clearCheckTimeout(); // resets if timer already running
        checkTask = new TimerTask() {
            @Override
            public void run() {
                checkTask = null;
                endProgramming();
                log.error("Timeout verifying checksum");
                addToLog(Bundle.getMessage("BootCheckTimeout"));
            }
        };
        TimerUtil.schedule(checkTask, CbusNode.BOOT_SINGLE_MESSAGE_TIMEOUT_TIME);
    }


    /**
     * Add to boot loader Log
     *
     * @param boottext String console message
     */
    public void addToLog(String boottext){
        ThreadingUtil.runOnGUI( ()->{
            bootConsole.append("\n"+boottext);
        });
    }


    /**
     * disconnect from the CBUS
     */
    @Override
    public void dispose() {
        if (hexFile != null) {
            hexFile.dispose();
        }
        // stop timers if running

        bootConsole.dispose();
        tc.removeCanListener(this);
    }


    /**
     * Nested class to create one of these using old-style defaults.
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemBootloader"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    CbusBootloaderPane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }


    private final static Logger log = LoggerFactory.getLogger(CbusBootloaderPane.class);

}
