package jmri.jmrix.can.cbus.swing.bootloader;

import static javax.swing.SwingUtilities.getWindowAncestor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

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
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

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
 * <p>
 * Update March 2022 A new CBUS bootloader protocol supports two new features:
 * - Reading back device ID
 * - Reading back bootloader ID 
 * - Positive acknowledgement (or error) for write command
 * - Possibility fro alternative checksum algorithms.
 * <p>
 * The module may buffer write commands in RAM, sending an immediate ACK and
 * only writing when a FLASH page worth of data is received, which will result
 * in a delayed ACK.
 * <p>
 * A new command, that will be ignored by the old bootloader, is used to request
 * the bootloader ID. If no reply is received after a suitable timeout
 * then the original protocol will be used.
 * 
 * The old protocol is only supported for older PIC18 K8x devices.
 * 
 * Modules based on any other devices are expected to support the new protocol.
 *
 * @author Andrew Crosland Copyright (C) 2020 Updates for new bootloader
 * protocol
 * @author Andrew Crosland Copyright (C) 2022
 */
public class CbusBootloaderPane extends jmri.jmrix.can.swing.CanPanel
        implements CanListener {

    private TrafficController tc;
    private CbusSend send;
    private CbusPreferences preferences;

    private final JRadioButtonMenuItem slowWrite;
    private final JRadioButtonMenuItem fastWrite;
    protected JTextField nodeNumberField = new JTextField(6);
    protected JCheckBox configCheckBox = new JCheckBox();
    protected JCheckBox eepromCheckBox = new JCheckBox();
    protected JCheckBox moduleCheckBox = new JCheckBox();
    protected JButton programButton;
    protected JButton openFileChooserButton;
    protected JButton readNodeParamsButton;
    private final TextAreaFIFO bootConsole;
    private static final int MAX_LINES = 5000;
    private final JFrame topFrame = (JFrame) getWindowAncestor(this);

    // to find and remember the hex file
    final javax.swing.JFileChooser hexFileChooser =
            new jmri.util.swing.JmriJFileChooser(FileUtil.getUserFilesPath());
    // File to hold name of hex file
    transient HexFile hexFile = null;

    CbusParameters hardwareParams = null;
    CbusParameters fileParams = null;

    boolean hexForBootloader = false;
    
    int nodeNumber;
    int nextParam;
    
    protected HexRecord currentRecord;
    protected int recordIndex = 0;
    protected boolean recordDone = false;

    // Set Program memory upper limit for PIC18
    // Only needed for old AN274 based bootloader, which had no acknowledge. Used
    // to determine when to use a longer timeout for EEPROM and CONFIG.
    // New modules should use the CBUS bootloader.
    private static final int CONFIG_START = 0x200000;

    BusyDialog busyDialog;

    /**
     * Bootloader protocol
     */
    protected enum BootProtocol {
        UNKNOWN,
        AN247,
        CBUS_2_0
    }
    protected BootProtocol bootProtocol = BootProtocol.UNKNOWN;
    
    /**
     * Bootloader checksum calculation
     */
    protected enum BootChecksum {
        CHECK_2S_COMPLEMENT,
        CHECK_CRC16
    }
    protected BootChecksum bootChecksum = BootChecksum.CHECK_2S_COMPLEMENT;
    
    /**
     * Bootloader state machine states
     */
    protected enum BootState {
        IDLE,
        GET_PARAMS,
        START_BOOT,
        CHECK_BOOT_MODE,
        WAIT_BOOT_DEVID,
        WAIT_BOOT_ID,
        ENABLES_SENT,
        INIT_SENT,
        PROG_DATA,
        PROG_PAUSE,
        CHECK_SENT,
        NOP_SENT
    }
    protected BootState bootState = BootState.IDLE;
    
    /**
     * Bootloader status values
     */
    protected enum BootStatus {
        NONE,
        PARAMETER_TIMEOUT,
        INIT_OUT_OF_RANGE,
        DATA_ERROR,
        DATA_OUT_OF_RANGE,
        ADDRESS_OUT_OF_RANGE,
        CHECKSUM_FAILED,
        ADDRESS_NOT_FOUND,
        COMPLETE,
        BOOT_TIMEOUT,
        ACK_TIMEOUT,
        CHECKSUM_TIMEOUT,
        PROTOCOL_ERROR
    }
    
    protected int bootAddress;
    protected int checksum;
    protected int dataFramesSent;
    protected int dataTimeout;


    public CbusBootloaderPane() {
        super();
        bootConsole = new TextAreaFIFO(MAX_LINES);
        slowWrite = new JRadioButtonMenuItem(Bundle.getMessage("Slow"));
        fastWrite = new JRadioButtonMenuItem(Bundle.getMessage("Fast"));
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

        preferences = memo.get(jmri.jmrix.can.cbus.CbusPreferences.class);

        init();
        setMenuOptions();
    }


    /**
     * Not sure this comment really applies here as init() does not use the tc
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
        configCheckBox.setEnabled(true);
        configCheckBox.setSelected(false);
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

        // Module sanity check
        moduleCheckBox.setText(Bundle.getMessage("BootIgnoreParams"));
        moduleCheckBox.setVisible(true);
        moduleCheckBox.setEnabled(true);
        moduleCheckBox.setSelected(false);
        moduleCheckBox.setToolTipText(Bundle.getMessage("BootIgnoreParamsTT"));
        
        JPanel modulePane = new JPanel();
        modulePane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("BootModuleOptions")));
        modulePane.setLayout(new BoxLayout(modulePane, BoxLayout.X_AXIS));
        modulePane.add(moduleCheckBox);

        JPanel selectPane = new JPanel();
        selectPane.setLayout(new BoxLayout(selectPane, BoxLayout.X_AXIS));
        selectPane.add(nnPane);
        selectPane.add(modulePane);
        selectPane.add(memoryPane);

        // Create buttons
        readNodeParamsButton = new JButton(Bundle.getMessage("BootReadNodeParams"));
        readNodeParamsButton.setVisible(true);
        readNodeParamsButton.setEnabled(true);
        readNodeParamsButton.setToolTipText(Bundle.getMessage("BootReadNodeParamsTT"));
        readNodeParamsButton.addActionListener((java.awt.event.ActionEvent e) -> {
            readNodeParamsButtonActionPerformed(e);
        });

        FileFilter filter = new FileNameExtensionFilter("Hex file", new String[] {"hex"});
        hexFileChooser.setFileFilter(filter);
        hexFileChooser.addChoosableFileFilter(filter);
        
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

        

        return menuList;
    }


    /**
     * Get the delay to be inserted between bootloader data writes.
     * 
     * For AN247, that has no handshaking can be slow or fast and then extended 
     * for slow writes to EEPROM and CONFIG.
     * 
     * Only a single long timeout is used for CBUS protocol, which has full
     * handshaking
     *
     * @return Delay in ms
     */
    int getWriteDelay() {
        int delay = CbusNode.BOOT_PROG_TIMEOUT_FAST;
        
        if (bootProtocol == BootProtocol.AN247) {
            if (slowWrite.isSelected()) {
                delay = CbusNode.BOOT_PROG_TIMEOUT_SLOW;
            }
            if (bootAddress >= CONFIG_START) {
                delay *= 8;
            }
        } else {
            delay = CbusNode.BOOT_LONG_TIMEOUT_TIME;
        }
        
        return delay;
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
            hexFile = new CbusPicHexFile(hexFileChooser.getSelectedFile().getPath());
            log.debug("hex file chosen: {}", hexFile.getName());
            addToLog(MessageFormat.format(Bundle.getMessage("BootFileChosen"), hexFile.getName()));
            try {
                hexFile.openRd();
                hexFile.read();
            } catch (IOException ex) {
                log.error("Error opening hex file");
                addToLog(Bundle.getMessage("BootHexFileOpenFailed"));
                return;
            }
            
            fileParams = hexFile.getParams();
            if (!moduleCheckBox.isSelected()) {
                if (fileParams.validate(fileParams, hardwareParams)) {
                    addToLog(MessageFormat.format(Bundle.getMessage("BootHexFileFoundParameters"), fileParams.toString()));
                    addToLog(Bundle.getMessage("BootHexFileParametersMatch"));
                    programButton.setEnabled(true);
                } else {
                    addToLog(Bundle.getMessage("BootHexFileParametersMismatch"));
                }
            } else {
                addToLog(Bundle.getMessage("BootHexFileIgnoringParameters"));
                programButton.setEnabled(true);
            }
            if ((hardwareParams.areValid()) && (hardwareParams.getLoadAddress() == 0)) {
                // Special case of rewriting the bootloader for Pi-SPROG One
                addToLog(Bundle.getMessage("BootBoot"));
                hexForBootloader = true;
                programButton.setEnabled(true);
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
     * The CBUS bootloader was originally "fire and forget", with no positive
     * acknowledgement. We had to wait an indeterminate time and assume the
     * write was successful.
     * <p>
     * A PIC based node will halt execution for some time ((10+ ms with newer Q
     * series devices) whilst FLASH operations (erase and/or write) complete,
     * during which time I/O will not be serviced. This is probably OK with CAN 
     * transport, assuming the ECAN continues to accept frames. With serial
     * (UART) transport, as used by Pi-SPROG, the timing is much more critical 
     * as a single missed character will corrupt the node firmware.
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
     * This is unnecessary, and not used, for the new protocol which has a
     * positive acknowledge mechanism.
     * 
     * @param m CanMessage
     */
    @Override
    public void message(CanMessage m) {
        if (bootProtocol == BootProtocol.AN247) {
            if ((bootState == BootState.PROG_DATA)) {
                if (m.isExtended() ) {
                    if (CbusMessage.isBootWriteData(m)) {
                        log.debug("Boot data write message {}", m);
                        setDataTimeout(dataTimeout);
                    }
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
//            log.debug("Extended Reply {} in state {}", r, bootState);
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
        if (bootState != BootState.GET_PARAMS) {
            log.debug("Reply not for me");
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
                openFileChooserButton.setEnabled(true);
                bootState = BootState.IDLE;
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
        switch (bootState) {
            default:
                break;

            case CHECK_BOOT_MODE:
                clearCheckBootTimeout();
                if (CbusMessage.isBootConfirm(r)) {
                    // The node is in boot mode so we can look for the device ID
                    requestDevId();
                }
                break;

            case WAIT_BOOT_DEVID:
                clearDevIdTimeout();
                if (CbusMessage.isBootDevId(r)) {
                    // We had a response to the Device ID request so we can proceed with the new protocol
                    showDevId(r);
                    bootProtocol = BootProtocol.CBUS_2_0;
                    requestBootId();
                } else {
                    protocolError();
                }
                break;
                
            case WAIT_BOOT_ID:
                clearBootIdTimeout();
                if (CbusMessage.isBootId(r)) {
                    // We had a response to the bootloader ID request so send the write enables
                    showBootId(r);
                    sendBootEnables();
                } else {
                    protocolError();
                }
                break;
                
            case ENABLES_SENT:
                clearAckTimeout();
                if (CbusMessage.isBootOK(r)) {
                    // We had a response to the enables so start programming.
                    initialise();
                } else {
                    protocolError();
                }
                break;
                        
            case INIT_SENT:
                clearAckTimeout();
                if (CbusMessage.isBootOK(r)) {
                    // We had a response to the initislise so start programming.
                    writeNextData();
                } else if (CbusMessage.isBootOutOfRange(r)) {
                    log.error("INIT Address out of range");
                    endProgramming(BootStatus.INIT_OUT_OF_RANGE);
                } else {
                    protocolError();
                }
                break;
                        
            case PROG_DATA:
                clearAckTimeout();
                if (CbusMessage.isBootDataOK(r)) {
                    // Acknowledge received for CBUS protocol
                    writeNextData();
                } else if (CbusMessage.isBootError(r)){
                    log.error("Data Error");
                    endProgramming(BootStatus.DATA_ERROR);
                } else if (CbusMessage.isBootDataOutOfRange(r)) {
                    log.error("Data Address out of range");
                    endProgramming(BootStatus.DATA_OUT_OF_RANGE);
                } else {
                    protocolError();
                }
                break;
                
            case NOP_SENT:
                clearAckTimeout();
                if (CbusMessage.isBootOK(r)) {
                    // Acknowledge received for NOP
                    bootState = BootState.PROG_DATA;
                    writeNextData();
                } else if (CbusMessage.isBootOutOfRange(r)) {
                    log.error("NOP Address out of range");
                    endProgramming(BootStatus.ADDRESS_OUT_OF_RANGE);
                } else {
                    protocolError();
                }
                break;
                
            case CHECK_SENT:
                clearCheckTimeout();
                if (CbusMessage.isBootOK(r)) {
                    sendReset();
                } else if (CbusMessage.isBootError(r)) {
                    // Checksum verify failed
                    log.error("Node {} checksum failed", nodeNumber);
                    endProgramming(BootStatus.CHECKSUM_FAILED);
                } else {
                    protocolError();
                }
                break;
        }
    }

    
    /**
     * Show the device ID
     * 
     * Manufacturere and device from cbusdefs.h, device ID from the device
     * 
     * @param r device ID reply
     */
    void showDevId(CanReply r) {
        log.debug("Found device ID Manu: {} Dev: {} Device ID: {}",
                r.getElement(1),
                r.getElement(2),
                (r.getElement(3)<<24) + (r.getElement(4)<<16) + (r.getElement(5)<<8) + r.getElement(4));
        addToLog(MessageFormat.format(Bundle.getMessage("DevIdCbus"),
                r.getElement(1),
                r.getElement(2),
                (r.getElement(3)<<24) + (r.getElement(4)<<16) + (r.getElement(5)<<8) + r.getElement(4)));
    }
    
   
    /**
     * Show the bootloader ID
     * 
     * Major/Minor version number, checksum algorithm error report capability
     * 
     * @param r Bootloader ID reply
     */
    void showBootId(CanReply r) {
        log.debug("Found bootloader Major: {} Minor: {} Algo: {} Reports: {}",
                r.getElement(1),
                r.getElement(2),
                r.getElement(3),
                r.getElement(4));
        addToLog(MessageFormat.format(Bundle.getMessage("BootIdCbus"),
                r.getElement(1),
                r.getElement(2),
                r.getElement(3),
                r.getElement(4)));
    }
    
    
    /**
     * Send the memory region write enable bit mask for CBUS bootloader protocol
     */
    void sendBootEnables() {
        int enables = 1;    // Prog mem always enabled
        
        if (eepromCheckBox.isSelected()) {
            enables |= 2;
        }
        if (configCheckBox.isSelected()) {
            enables |= 4;
        }
        
        bootState = BootState.ENABLES_SENT;
        setAckTimeout();
        CanMessage m = CbusMessage.getBootEnables(enables, 0);
        log.debug("Send boot enables {}", enables);
        addToLog(MessageFormat.format(Bundle.getMessage("BootEnables"), enables));
        tc.sendCanMessage(m, null);
    }
    
    
    /**
     * Protocol Error
     */
    void protocolError() {
        log.error("Bootloader Protocol Error in state {}", bootState.toString());
        addToLog(MessageFormat.format(Bundle.getMessage("BootProtocol"), bootState.toString()));
        endProgramming(BootStatus.PROTOCOL_ERROR);
    }
    
    
    /**
     * Is Programming Needed
     * 
     * Check if any data bytes actually need programming
     * 
     * @param d data bytes to check
     * @return false if all bytes are 0xFF, else true
     */
    boolean isProgrammingNeeded(byte [] d) {
        for (int i = 0; i < d.length; i++) {
            if (d[i] != (byte)0xFF) {
                return true;
            }
        }
        return false;
    }

    
    protected void logFrame(CanMessage m) {
        log.debug("Write frame {} at address {} {}", dataFramesSent, Integer.toHexString(bootAddress), m);
        if ((bootAddress & 0xFF) == 0) {
            addToLog(MessageFormat.format(Bundle.getMessage("BootAddress"), Integer.toHexString(bootAddress)));
        } else {
            bootConsole.append(".");
        }        
    }
    
    
    /**
     * Check if data is filtered (e.g., EEPROM selection unticked)
     * 
     * Used only for AN247
     * 
     * @param address of data record
     * @return true if data is filtered and should not be written
     */
    protected boolean dataIsFiltered(int address) {
        if ((address >= 0x300000) && (address < 0x310000) && (!configCheckBox.isSelected())) {
            // PIC18 Config space at 0x200000 is filtered
            return true;
        } else if ((address >= 0x310000) && (!eepromCheckBox.isSelected())) {
            // PIC18 EEPROM space at 0x300000, 0x310000 or 0x380000 is filtered
            return true;
        }
        return false;
    }
    
    
    /**
     * Send data to the hardware and keep a running checksum
     *
     * @param timeout timeout for write operation
     */
    protected void sendData(int timeout) {

        byte [] d = getDataFromRecord();
        dataFramesSent++;
        
        CanMessage m = CbusMessage.getBootWriteData(d, 0);
        if (bootProtocol == BootProtocol.CBUS_2_0) {
            setAckTimeout();
            updateChecksum(d);
            logFrame(m);       
            tc.sendCanMessage(m, null);
        } else {
            // For AN247 protocol, we need to filter data
            if (!dataIsFiltered(bootAddress)) {
                // Timeout will be set when we see the outgoing message
                dataTimeout = timeout;
                updateChecksum(d);
                logFrame(m);       
                tc.sendCanMessage(m, null);
            } else {
                // No data to send, set short timeout to trigger next data
                setDataTimeout(10);
            }
        }
        bootAddress += d.length;
    }

    
    /**
     * Extract data from the current hex record
     * 
     * Returns 8 byte array or whatever is left in the record if less than 8 bytes.
     * 
     * Sets recordDone flag if record is exhausted.
     * 
     * @return data array
     */
    private byte [] getDataFromRecord() {
        byte [] d;
        
        if (currentRecord.len - recordIndex >= 8) {
            d = new byte[8];
            if (currentRecord.len - recordIndex == 8) {
                recordDone = true;
            }
        } else {
            d = new byte[currentRecord.len - recordIndex];
            recordDone = true;
        }
        for (int i = 0; i < d.length; i++) {
            d[i] = currentRecord.getData(recordIndex++);
        }
        return d;
    }

    
    /**
     * Write next data for AN247 protocol
     */
    void writeNextDataAn247() {
//        log.debug("writeNextDataAn247()");
        
        if ((bootAddress == 0x7f8) && (hexForBootloader == true)) {
            log.debug("Pause for bootloader reset");
            // Special case for Pi-SPROG One, pause at end of bootloader code to allow time for node to reset
            bootAddress = 0x800;
            checksum = 0;
            bootState = BootState.PROG_PAUSE;
            setPauseTimeout();
        } else {
            // If the address has skipped we need to send a new address to the bootloader
            // There's no ACK so send data immediately afterwards
            if ((currentRecord.address + recordIndex) != bootAddress) {
                bootAddress = currentRecord.address;
                // Send NOP to adjust the address, no reply to this from AN247
                log.debug("Start writing at new address {}", Integer.toHexString(bootAddress));
                addToLog(MessageFormat.format(Bundle.getMessage("BootNewAddress"), Integer.toHexString(bootAddress)));
                CanMessage m = CbusMessage.getBootNop(bootAddress, 0);
                tc.sendCanMessage(m, null);
            }
            if ((bootAddress < CONFIG_START) && (currentRecord.len%8 != 0)) {
                // AN247 bootloader always writes 8 bytes to FLASH so we need to pad the packet and adjust the length
                int pad = 8 - currentRecord.len%8;
                for (int i = 0; i < pad; i++) {
                    currentRecord.data[currentRecord.len + pad] = (byte)0xFF;
                }
                currentRecord.len += pad;
            }
            sendData(getWriteDelay());
        }
    }


    /**
     * Write next data for CBUS protocol
     */
    void writeNextDataCbus() {
//        log.debug("writeNextDataCbus()");

        // If the address has skipped we need to send a new address to the bootloader
        if ((currentRecord.address + recordIndex) != bootAddress) {
            bootAddress = currentRecord.address;
            // Send NOP to adjust the address 
            log.debug("Start writing at new address {}", Integer.toHexString(bootAddress));
            addToLog(MessageFormat.format(Bundle.getMessage("BootNewAddress"), Integer.toHexString(bootAddress)));
            bootState = BootState.NOP_SENT;
            setAckTimeout();
            CanMessage m = CbusMessage.getBootNop(bootAddress, 0);
            tc.sendCanMessage(m, null);
        } else {
            // Extract the data, send it and update bootAddress for next packet
            sendData(getWriteDelay());
        }
    }


    /**
     * Write the next data frame for the bootloader
     */
    void writeNextData() {
        if (recordDone) {
            // Current record is exhausted, Get next ONE
            recordDone = false;
            recordIndex = 0;
            currentRecord = hexFile.getNextRecord();
            if (currentRecord.type == HexRecord.END) {
                // No more data to send so send checksum
                bootState = BootState.CHECK_SENT;
                addToLog(Bundle.getMessage("BootVerifyChecksum"));
                log.debug("Sending checksum {} as 2s complement {}", checksum, 0 - checksum);
                setCheckTimeout();
                CanMessage m = CbusMessage.getBootCheck(0 - checksum, 0);
                tc.sendCanMessage(m, null);
                return;
            }
        }
        
        bootState = BootState.PROG_DATA;
        if (bootProtocol == BootProtocol.AN247) {
            writeNextDataAn247();
        } else {
            writeNextDataCbus();
        }
    }


    /**
     * Initialise programming
     * 
     * We normally start at the address from the module parameters, or from the 
     * hex file, otherwise start at the beginning of the hex file. 
     */
    private void initialise() {
        Optional<HexRecord> hexRecord;
        
        if (hardwareParams.areValid()) {
            bootAddress = hardwareParams.getLoadAddress();
        } else if (fileParams.areValid()) {
            bootAddress = fileParams.getLoadAddress();
        } else {
            bootAddress = hexFile.getProgStart();
        }
        
        recordDone = false;
        recordIndex = 0;
        
        hexRecord = hexFile.getRecordForAddress(bootAddress);
        if (hexRecord.isPresent()) {
            currentRecord = hexRecord.get();
        } else {
            log.error("Did not find hex record for load address {}", "0x"+Integer.toHexString(bootAddress));
            endProgramming(BootStatus.ADDRESS_NOT_FOUND);
        }
        checksum = 0;
        dataFramesSent = 0;
        log.debug("Initialise at address {}", "0x"+Integer.toHexString(bootAddress));
        addToLog(MessageFormat.format(Bundle.getMessage("BootStartAddress"), Integer.toHexString(bootAddress)));
        // Initialise the bootloader, only CBUS protocol will ACK this
        if (bootProtocol == BootProtocol.CBUS_2_0) {
            setAckTimeout();
        }
        CanMessage m = CbusMessage.getBootInitialise(bootAddress, 0);
        bootState = BootState.INIT_SENT;
        tc.sendCanMessage(m, null);
        if (bootProtocol == BootProtocol.AN247) {
            // No wait for ACK so start sending data
            writeNextData();
        }
    }

    
    protected void requestDevId() {
        CanMessage m = CbusMessage.getBootDevId(0);
        log.debug("Requesting bootloader device ID...");
        addToLog(Bundle.getMessage("ReqDevId"));
        bootState = BootState.WAIT_BOOT_DEVID;
        setDevIdTimeout();
        tc.sendCanMessage(m, null);
    }

    
    protected void requestBootId() {
        CanMessage m = CbusMessage.getBootId(0);
        log.debug("Requesting bootloader ID...");
        addToLog(Bundle.getMessage("ReqBootId"));
        bootState = BootState.WAIT_BOOT_ID;
        setBootIdTimeout();
        tc.sendCanMessage(m, null);
    }


    /**
     * Send bootloader reset frame to put the node back into operating mode.
     *
     * There will be no reply to this.
     */
    protected void sendReset() {
        CanMessage m = CbusMessage.getBootReset(0);
        log.debug("Done. Resetting node...");
        addToLog(Bundle.getMessage("BootFinished"));
        tc.sendCanMessage(m, null);
        endProgramming(BootStatus.COMPLETE);
    }


    /**
     * Tidy up after programming success or failure
     */
    private void endProgramming(BootStatus status) {
        log.debug("Boot status is {}", status.toString());
        addToLog(MessageFormat.format(Bundle.getMessage("BootStatus"), status.toString()));
        if (busyDialog != null) {
            busyDialog.finish();
            busyDialog = null;
        }
        openFileChooserButton.setEnabled(true);
        programButton.setEnabled(false);
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
        bootState = BootState.GET_PARAMS;
        setAllParamTimeout();
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
            || devIdTask != null
            || bootIdTask != null
            || pauseTask != null
            || dataTask != null
            || ackTask != null
            || checkTask != null;
    }


    private TimerTask allParamTask;
    private TimerTask startBootTask;
    private TimerTask checkBootTask;
    private TimerTask pauseTask;
    private TimerTask dataTask;
    private TimerTask ackTask;
    private TimerTask devIdTask;
    private TimerTask bootIdTask;
    private TimerTask checkTask;


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
     * 
     * On timeout, attempt to find module already in boot mode.
     */
    private void setAllParamTimeout() {
        clearAllParamTimeout(); // resets if timer already running
        allParamTask = new TimerTask() {
            @Override
            public void run() {
                allParamTask = null;
                if (busyDialog != null) {
                    busyDialog.finish();
                    busyDialog = null;
                    log.debug("Failed to read module parameters from node {}", nodeNumber);
                    hardwareParams.setValid(false);
                    moduleCheckBox.setSelected(true);
                    openFileChooserButton.setEnabled(true);
                    endProgramming(BootStatus.PARAMETER_TIMEOUT);
                }
            }
        };
        TimerUtil.schedule(allParamTask, CbusNode.SINGLE_MESSAGE_TIMEOUT_TIME);
    }


    /**
     * Stop timer for boot mode entry request
     */
    private void clearStartBootTimeout() {
        if (startBootTask != null) {
            startBootTask.cancel();
            startBootTask = null;
        }
    }


    /**
     * Start timer for boot mode entry request
     * <p>
     * We don't get a response, so timeout is expected, assume module is in boot
     * mode and start check for boot mode
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
        TimerUtil.schedule(startBootTask, CbusNode.BOOT_LONG_TIMEOUT_TIME);
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
                endProgramming(BootStatus.BOOT_TIMEOUT);
            }
        };
        TimerUtil.schedule(checkBootTask, CbusNode.BOOT_LONG_TIMEOUT_TIME);
    }


    /**
     * Stop timer for bootloader device ID request
     */
    private void clearDevIdTimeout() {
        if (devIdTask != null) {
            devIdTask.cancel();
            devIdTask = null;
        }
    }


    /**
     * Start timer for bootloader device ID request
     * <p>
     * If we don't get a response we start programming with the old AN247 protocol.
     */
    private void setDevIdTimeout() {
        clearDevIdTimeout(); // resets if timer already running
        devIdTask = new TimerTask() {
            @Override
            public void run() {
                devIdTask = null;
                bootProtocol = BootProtocol.AN247;
                log.debug("Found AN247 bootloader");
                addToLog(Bundle.getMessage("BootIdAn247"));
                initialise();
            }
        };
        TimerUtil.schedule(devIdTask, CbusNode.BOOT_LONG_TIMEOUT_TIME);
    }


    /**
     * Stop timer for bootloader ID request
     */
    private void clearBootIdTimeout() {
        if (bootIdTask != null) {
            bootIdTask.cancel();
            bootIdTask = null;
        }
    }


    /**
     * Start timer for bootloader ID request
     * <p>
     */
    private void setBootIdTimeout() {
        clearBootIdTimeout(); // resets if timer already running
        bootIdTask = new TimerTask() {
            @Override
            public void run() {
                bootIdTask = null;
                protocolError();
            }
        };
        TimerUtil.schedule(bootIdTask, CbusNode.BOOT_LONG_TIMEOUT_TIME);
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
     * Special case for Pi-SPROG One AN247 protocol only
     * <p>
     * No reply so timeout is expected. Initialise to new address for application.
     * The init is now sent from writeNextData for AN247.
     */
    private void setPauseTimeout() {
        clearPauseTimeout(); // resets if timer already running
        pauseTask = new TimerTask() {
            @Override
            public void run() {
                pauseTask = null;
                hexForBootloader = false;
                log.debug("Start writing at address {}", Integer.toHexString(bootAddress));
                addToLog(MessageFormat.format(Bundle.getMessage("BootStartAddress"), Integer.toHexString(bootAddress)));
                bootState = BootState.PROG_DATA;
                writeNextData();
            }
        };
        TimerUtil.schedule(pauseTask, CbusNode.BOOT_LONG_TIMEOUT_TIME);
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
     * 
     * Only used for AN247 prototocl
     * <p>
     * No reply so timeout is expected. Send more data.
     */
    private void setDataTimeout(int timeout) {
        clearDataTimeout(); // resets if timer already running
        dataTask = new TimerTask() {
            @Override
            public void run() {
                dataTask = null;
                writeNextData();
            }
        };
        TimerUtil.schedule(dataTask, timeout);
    }


    /**
     * Stop timer for ACK timeout
     */
    private void clearAckTimeout() {
        if (ackTask != null) {
            ackTask.cancel();
            ackTask = null;
        }
    }


    /**
     * Start timer for ACK timeout
     * <p>
     * Error condition if no ACK received
     */
    private void setAckTimeout() {
        clearAckTimeout(); // resets if timer already running
        ackTask = new TimerTask() {
            @Override
            public void run() {
                ackTask = null;
                endProgramming(BootStatus.ACK_TIMEOUT);
                bootAddress -= 8;
                log.error("Timeout waiting for data write ACK at address {}", Integer.toHexString(bootAddress));
            }
        };
        TimerUtil.schedule(ackTask, CbusNode.BOOT_LONG_TIMEOUT_TIME);
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
                endProgramming(BootStatus.CHECKSUM_TIMEOUT);
                log.error("Timeout verifying checksum");
            }
        };
        TimerUtil.schedule(checkTask, CbusNode.BOOT_LONG_TIMEOUT_TIME);
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
