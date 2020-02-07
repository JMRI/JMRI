package jmri.jmrix.can.cbus.swing.bootloader;

import static javax.swing.SwingUtilities.getWindowAncestor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.MessageFormat;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
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

    private TrafficController tc;
    private CbusSend send;

    protected JTextField nodeNumberField = new JTextField(6);
    protected JCheckBox configCheckBox = new JCheckBox();
    protected JCheckBox eepromCheckBox = new JCheckBox();
    protected JButton programButton;
    protected JButton openFileChooserButton;
    protected JButton readNodeParamsButton;
    private final TextAreaFIFO bootConsole;
    private static final int MAX_LINES = 5000;
    private final double splitRatio = 0.75;
    private final JFrame topFrame = (JFrame) getWindowAncestor(this);
    
    // to find and remember the hex file
    final javax.swing.JFileChooser hexFileChooser =
            new JFileChooser(FileUtil.getUserFilesPath());
    // File to hold name of hex file
    transient HexFile hexFile = null;
    
    CbusParameters hardwareParams = null;
    CbusParameters fileParams = null;
    
    int nodeNumber;
    int nextParam;

    BusyDialog busy_dialog;
    
    protected enum BootState {
        IDLE,
        START_BOOT,
        CHECK_BOOT_MODE,
        INIT_PROG_SENT,
        PROG_DATA,
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
        
        init();
    }

    
    /*
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
        // TODO: Might want to allow same file to be used on multiple nodes?
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
     * Kick off the reading of parameters from the node
     * 
     * Start by reading parameter 0, the number of parameters
     * 
     * @param e 
     */
    private void readNodeParamsButtonActionPerformed(java.awt.event.ActionEvent e) {
        try {
            nodeNumber = Integer.parseInt(nodeNumberField.getText());
        } catch (NumberFormatException e1) {
            addToLog(Bundle.getMessage("BootInvalidNode"));
            log.error("Invalid node number {}", e1);
            return;
        }
        // Read the parameters from the chosen node
        addToLog(Bundle.getMessage("BootReadingParams"));
        hardwareParams = new CbusParameters();
        nextParam = 0;
        busy_dialog = new BusyDialog(topFrame, Bundle.getMessage("BootReadingParams"), false);
        busy_dialog.start();
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
            hexFile = new HexFile(hexFileChooser.getSelectedFile().getPath());
            log.debug("hex file chosen: {}", hexFile.getName());
            addToLog(MessageFormat.format(Bundle.getMessage("BootFileChosen"), hexFile.getName()));
            if (hexFile.openRd()) {
                hexFile.read();
                fileParams = new CbusParameters().validate(hexFile, hardwareParams);
                if (fileParams.areValid()) {
                    addToLog(MessageFormat.format(Bundle.getMessage("BootHexFileFoundParameters"), fileParams.toString()));
                    addToLog(MessageFormat.format(Bundle.getMessage("BootHexFileParametersMatch"), hardwareParams.toString()));
                    programButton.setEnabled(true);
                } else {
                    addToLog(Bundle.getMessage("BootHexFileParametersMismatch"));
                }
            } else {
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
        setStartBootTimeout();
        bootState = BootState.START_BOOT;
        CanMessage m = CbusMessage.getBootEntry(nodeNumber, 0);
        tc.sendCanMessage(m, null);  
    }

    
    @Override
    public void message(CanMessage m) {
//        log.debug("Message {}", m);
    }

    
    /**
     * Processes all incoming and certain outgoing CAN Frames
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
                    busy_dialog.finish();
                    busy_dialog = null;
                    openFileChooserButton.setEnabled(true);
                }
            } else {
                // ignoring OPC
            }
        } else {
            log.debug("Extended Reply {} in state {}", r, bootState);
            // Extended messages are only used by the bootloader
            
            switch (bootState) {
                case IDLE:
                case START_BOOT:
                case INIT_PROG_SENT:
                case PROG_DATA:
                case INIT_CONFIG_SENT:
                case CONFIG_DATA:
                case INIT_EEPROM_SENT:
                case EEPROM_DATA:
                    break;
                    
                case CHECK_BOOT_MODE:
                    clearCheckBootTimeout();
                    if (CbusMessage.isBootConfirm(r)) {
                        // The node is in boot mode so we can start programming
                        bootState = BootState.INIT_PROG_SENT;
                        bootAddress = hardwareParams.getLoadAddress();
                        checksum = 0;
                        log.debug("In boot mode, start writing at adress {}", bootAddress);
                        addToLog(MessageFormat.format(Bundle.getMessage("BootStartAddress"), bootAddress));
                        setInitTimeout();
                        CanMessage m = CbusMessage.getBootInitialise(bootAddress, 0);
                        tc.sendCanMessage(m, null);
                    }
                    break;
                    
                case PROG_CHECK_SENT:
                    if (CbusMessage.isBootOK(r)) {
                        clearCheckTimeout();
                        // Move onto config words or eeprom
                        if (configCheckBox.isSelected()) {
                            // Move onto config words
                            bootAddress = 0x300000;
                            bootState = BootState.INIT_CONFIG_SENT;
                            checksum = 0;
                            addToLog(MessageFormat.format(Bundle.getMessage("BootConfigAddress"), bootAddress));
                            setInitTimeout();
                            CanMessage m = CbusMessage.getBootInitialise(bootAddress, 0);
                            tc.sendCanMessage(m, null);
                        } else if (eepromCheckBox.isSelected()) {
                            // Move onto eeprom
                            bootAddress = 0xF00000;
                            bootState = BootState.INIT_EEPROM_SENT;
                            checksum = 0;
                            addToLog(MessageFormat.format(Bundle.getMessage("BootEepromAddress"), bootAddress));
                            setInitTimeout();
                            CanMessage m = CbusMessage.getBootInitialise(bootAddress, 0);
                            tc.sendCanMessage(m, null);
                        } else {
                            // Done writing
                            sendReset();
                        }
                    } else if (CbusMessage.isBootError(r)) {
                        clearCheckTimeout();
                        // Checksum verify failed
                        log.error("Node {} checksum failed", nodeNumber);
                        addToLog(MessageFormat.format(Bundle.getMessage("BootChecksumFailed"), nodeNumber));
                        bootState = BootState.IDLE;
                    }
                    break;
                    
                case CONFIG_CHECK_SENT:
                    if (CbusMessage.isBootOK(r)) {
                        clearCheckTimeout();
                        // Move onto eeprom words
                        if (eepromCheckBox.isSelected()) {
                            bootAddress = 0xF00000;
                            bootState = BootState.INIT_EEPROM_SENT;
                            checksum = 0;
                            addToLog(MessageFormat.format(Bundle.getMessage("BootEepromAddress"), bootAddress));
                            setInitTimeout();
                            CanMessage m = CbusMessage.getBootInitialise(bootAddress, 0);
                            tc.sendCanMessage(m, null);
                        } else {
                            // Done writing
                            sendReset();
                        }
                    } else if (CbusMessage.isBootError(r)) {
                        clearCheckTimeout();
                        // Checksum verify failed
                        log.error("Node {} checksum failed", nodeNumber);
                        addToLog(MessageFormat.format(Bundle.getMessage("BootChecksumFailed"), nodeNumber));
                        bootState = BootState.IDLE;
                    }
                    break;
                    
                case EEPROM_CHECK_SENT:
                    if (CbusMessage.isBootOK(r)) {
                        clearCheckTimeout();
                        // Done writing
                        sendReset();
                    } else if (CbusMessage.isBootError(r)) {
                        clearCheckTimeout();
                        // Checksum verify failed
                        log.error("Node {} checksum failed", nodeNumber);
                        addToLog(MessageFormat.format(Bundle.getMessage("BootChecksumFailed"), nodeNumber));
                        bootState = BootState.IDLE;
                    }
                    break;
                    
            }
        }
    }
    
    
    /**
     * Write the next data frame for the bootloader
     * <p>
     * Keeps a running checksum.
     * 
     * @return 
     */
    protected boolean writeNextData() {
        // getBootWriteData() expects an array of length 8
        byte [] d = new byte[8];
        
        int address = bootAddress;
        
        int timeout = CbusNode.BOOT_PROG_TIMEOUT_TIME;
        boolean dataToSend = false;
        
        if (address < hexFile.getProgEnd()) {
            d = hexFile.getData(address, 8);
            dataToSend = true;
        } else {
            // CONFIG and EEPROM require a longer timeout as bootloader writes
            // them one byte at a time
            timeout = CbusNode.BOOT_CONFIG_TIMEOUT_TIME;
            if ((address >= HexFile.CONFIG_START) && (address < hexFile.getConfigEnd())) {
                d = hexFile.getConfig(address - HexFile.CONFIG_START, 8);
                dataToSend = true;
            } else if ((address >= HexFile.EE_START) && (address < hexFile.getEeEnd())) {
                d = hexFile.getEeprom(address - HexFile.EE_START, 8);
                dataToSend = true;
            }
        }
        
        if (dataToSend) {
            updateChecksum(d);
            bootAddress += 8;
            setDataTimeout(timeout);
            CanMessage m = CbusMessage.getBootWriteData(d, 0);
            log.debug("Write at address {} {}", address, m);
            addToLog(MessageFormat.format(Bundle.getMessage("BootAddress"), address));
            tc.sendCanMessage(m, null);
        } else {
            log.debug("No more data to send");
        }
        return dataToSend;
    }
    
    
    /**
     * Send bootloader reset frame to put the node back into operating mode.
     * 
     * There will be no reply to this.
     * 
     * TODO: Reread parameters and check version number is correct
     */
    protected void sendReset() {
        bootState = BootState.IDLE;
        CanMessage m = CbusMessage.getBootReset(0);
        log.debug("Done. Resetting node...");
        addToLog(Bundle.getMessage("BootFinished"));
        tc.sendCanMessage(m, null);
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
    
    /*
     * Copied and modified from CbusNode.java
     */
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
                if (busy_dialog != null) {
                    busy_dialog.finish();
                    busy_dialog = null;
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
                bootState = BootState.IDLE;
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
        clearInitTimeout(); // resets if timer already running
        initTask = new TimerTask() {
            @Override
            public void run() {
                initTask = null;
                if (bootState == BootState.INIT_PROG_SENT) {
                    bootState = BootState.PROG_DATA;
                } else if (bootState == BootState.INIT_CONFIG_SENT) {
                    bootState = BootState.CONFIG_DATA;
                } else {
                    bootState = BootState.EEPROM_DATA;
                }
                writeNextData();
            }
        };
        TimerUtil.schedule(initTask, CbusNode.BOOT_PROG_TIMEOUT_TIME);
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
                bootState = BootState.IDLE;
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
