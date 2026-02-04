package jmri.jmrix.loconet.swing.lnsv1prog;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.beantable.EnablingCheckboxRenderer;
import jmri.jmrix.loconet.*;
import jmri.jmrix.loconet.lnsvf1.Lnsv1Device;
import jmri.jmrix.loconet.lnsvf1.Lnsv1MessageContents;
import jmri.swing.JTablePersistenceManager;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableRowSorter;
import java.awt.*;

/**
 * Frame for discovery and display of LocoNet LNSVf1 boards, e.g. LocoIO.
 * Derived from lncvprog. Verified with HDL and GCA hardware.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax.
 * <p>
 * Buttons in table row allow to add roster entry for device, and switch to the
 * DecoderPro ops mode programmer.
 *
 * @author Egbert Broerse Copyright (C) 2021, 2022, 2025
 */
public class Lnsv1ProgPane extends jmri.jmrix.loconet.swing.LnPanel implements LocoNetListener {

    private LocoNetSystemConnectionMemo memo;
    protected JButton probeAllButton = new JButton();
    protected JButton setAllAddressButton = new JButton();
    protected JButton readButton = new JButton(Bundle.getMessage("ButtonRead"));
    protected JButton writeButton = new JButton(Bundle.getMessage("ButtonWrite"));
    protected JTextField addressField = new JTextField(4);
    protected JTextField subAddressField = new JTextField(4);
    protected JTextField svField = new JTextField(4);
    protected JTextField valueField = new JTextField(4);
    protected JCheckBox rawCheckBox = new JCheckBox(Bundle.getMessage("ButtonShowRaw"));
    protected JCheckBox decimalCheckBox = new JCheckBox(Bundle.getMessage("ButtonShowDecimal"));
    protected JTable moduleTable = null;
    protected Lnsv1ProgTableModel moduleTableModel = null;
    public static final int ROW_HEIGHT = (new JButton("X").getPreferredSize().height)*9/10;

    protected JPanel tablePanel = null;
    protected JLabel statusText1 = new JLabel();
    protected JLabel statusText2 = new JLabel();
    protected JLabel sepFieldLabel = new JLabel("/", JLabel.RIGHT);
    protected JLabel addressFieldLabel = new JLabel(Bundle.getMessage("LabelModuleAddress", JLabel.RIGHT));
    protected JLabel svFieldLabel = new JLabel(Bundle.getMessage("LabelSv"), JLabel.RIGHT);
    protected JLabel valueFieldLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("HeadingValue")), JLabel.RIGHT);
    protected JTextArea result = new JTextArea(6,50);
    protected String reply = "";
    protected int addr;
    protected int subAddr;
    protected int sv = 0;
    protected int val;
    boolean writeConfirmed = false;
    private final String rawDataCheck = this.getClass().getName() + ".RawData"; // NOI18N
    private final String decimalDataCheck = this.getClass().getName() + ".DecimalData"; // NOI18N
    private UserPreferencesManager pm;
    private transient TableRowSorter<Lnsv1ProgTableModel> sorter;
    private Lnsv1DevicesManager lnsv1dm;

    /**
     * Constructor method
     */
    public Lnsv1ProgPane() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.swing.lnsv1prog.Lnsv1ProgPane"; // NOI18N
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("MenuItemLnsv1Prog");
    }

    /**
     * Initialize the config window
     */
    @Override
    public void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        // buttons at top, like SE8c pane
        add(initButtonPanel()); // requires presence of memo.
        add(initStatusPanel()); // positioned after ButtonPanel so to keep it simple also delayed
        // creation of table must wait for memo + tc to be available, see initComponents(memo) next
    }

    @Override
    public synchronized void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);
        this.memo = memo;
        lnsv1dm = memo.getLnsv1DevicesManager();
        pm = InstanceManager.getDefault(UserPreferencesManager.class);
        // connect to the LnTrafficController
        if (memo.getLnTrafficController() == null) {
            log.error("No traffic controller is available");
        } else {
            // add listener
            memo.getLnTrafficController().addLocoNetListener(~0, this);
        }

        // create the data model and its table
        moduleTableModel = new Lnsv1ProgTableModel(this, memo);
        moduleTable = new JTable(moduleTableModel);
        moduleTable.setRowSelectionAllowed(false);
        moduleTable.setPreferredScrollableViewportSize(new Dimension(300, 200));
        moduleTable.setRowHeight(ROW_HEIGHT);
        moduleTable.setDefaultEditor(JButton.class, new ButtonEditor(new JButton()));
        moduleTable.setDefaultRenderer(JButton.class, new ButtonRenderer());
        moduleTable.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());
        moduleTable.setRowSelectionAllowed(true);
        moduleTable.getSelectionModel().addListSelectionListener(event -> {
            synchronized (this) {
                if (moduleTable.getSelectedRow() > -1 && moduleTable.getSelectedRow() < moduleTable.getRowCount()) {
                    // copy composite board address, svNuma and value from selected row
                    copyEntrytoFields((int) moduleTable.getValueAt(moduleTable.getSelectedRow(), Lnsv1ProgTableModel.MODADDR_COLUMN));
                    setCvFields((int) moduleTable.getValueAt(moduleTable.getSelectedRow(), Lnsv1ProgTableModel.CV_COLUMN),
                            (int) moduleTable.getValueAt(moduleTable.getSelectedRow(), Lnsv1ProgTableModel.VALUE_COLUMN));
                }
            }
        });
        // establish row sorting for the table
        sorter = new TableRowSorter<>(moduleTableModel);
        moduleTable.setRowSorter(sorter);
         // establish table physical characteristics persistence
        moduleTable.setName("LNSV1 Device Management"); // NOI18N
        // Reset and then persist the table's ui state
        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent((tpm) -> {
            synchronized (this) {
                tpm.resetState(moduleTable);
                tpm.persist(moduleTable, true);
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(moduleTable);
        tablePanel = new JPanel();
        Border resultBorder = BorderFactory.createEtchedBorder();
        Border resultTitled = BorderFactory.createTitledBorder(resultBorder, Bundle.getMessage("Lnsv1TableTitle"));
        tablePanel.setBorder(resultTitled);
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.add(tableScrollPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePanel, getMonitorPanel());
        splitPane.setOneTouchExpandable(true);
        splitPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        add(splitPane);

        rawCheckBox.setSelected(pm.getSimplePreferenceState(rawDataCheck));
        decimalCheckBox.setSelected(pm.getSimplePreferenceState(decimalDataCheck));

        // Probe when ready
        probeAllButtonActionPerformed();
    }

    /*
     * Initialize the LNSV1 Monitor panel.
     */
    protected JPanel getMonitorPanel() {
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));

        JPanel panel31 = new JPanel();
        panel31.setLayout(new BoxLayout(panel31, BoxLayout.Y_AXIS));
        JScrollPane resultScrollPane = new JScrollPane(result);
        panel31.add(resultScrollPane);

        JPanel panel31b = new JPanel();
        panel31b.setLayout(new BoxLayout(panel31b, BoxLayout.X_AXIS));
        panel31b.add(rawCheckBox);
        rawCheckBox.setVisible(true);
        rawCheckBox.setToolTipText(Bundle.getMessage("TooltipShowRaw"));
        panel31b.add(decimalCheckBox);
        decimalCheckBox.setVisible(true);
        decimalCheckBox.setToolTipText(Bundle.getMessage("TooltipShowDecimal"));
        panel31.add(panel31b);

        panel3.add(panel31);
        Border panel3Border = BorderFactory.createEtchedBorder();
        Border panel3Titled = BorderFactory.createTitledBorder(panel3Border, Bundle.getMessage("Lnsv1MonitorTitle"));
        panel3.setBorder(panel3Titled);
        return panel3;
    }

    /*
     * Initialize the Button panel. Requires presence of memo to send and receive.
     */
    protected JPanel initButtonPanel() {
        // Set up buttons and entry fields
        JPanel panel4 = new JPanel();
        panel4.setLayout(new BoxLayout(panel4, BoxLayout.X_AXIS));
        panel4.add(Box.createHorizontalGlue()); // this will expand/contract

        JPanel panel41 = new JPanel();
        panel41.setLayout(new BoxLayout(panel41, BoxLayout.PAGE_AXIS));
        probeAllButton.setText(Bundle.getMessage("ButtonProbe"));
        probeAllButton.setToolTipText(Bundle.getMessage("TipProbeAllButton"));
        probeAllButton.addActionListener(e -> probeAllButtonActionPerformed());
        panel41.add(probeAllButton);

        setAllAddressButton.setText(Bundle.getMessage("ButtonSetModuleAddress"));
        setAllAddressButton.setToolTipText(Bundle.getMessage("TipSetModuleAddrButton"));
        setAllAddressButton.addActionListener(e -> setAllAddressButtonActionPerformed());
        panel41.add(setAllAddressButton);
        panel4.add(panel41);

        JPanel panel42 = new JPanel();
        panel42.setLayout(new BoxLayout(panel42, BoxLayout.PAGE_AXIS));

        JPanel panel421 = new JPanel(); // default FlowLayout
        panel421.add(addressFieldLabel);
        // entry field (decimal)
        JPanel panel4211 = new JPanel(); // keep entry fields together
        addressField.setToolTipText(Bundle.getMessage("TipModuleAddrEntry"));
        panel4211.add(addressField); // entry field (decimal) for Module Low Address
        panel4211.add(sepFieldLabel); // holds the slash between base and subaddress
        subAddressField.setToolTipText(Bundle.getMessage("TipModuleSubaddrEntry"));
        panel4211.add(subAddressField); // entry field (decimal) for Module Subaddress
        panel421.add(panel4211);
        panel42.add(panel421);
        panel4.add(panel42);

        JPanel panel43 = new JPanel(); // CV num + value
        Border panel43Border = BorderFactory.createEtchedBorder();
        panel43.setBorder(panel43Border);
        panel43.setLayout(new BoxLayout(panel43, BoxLayout.LINE_AXIS));

        JPanel panel431 = new JPanel(); // labels
        panel431.setLayout(new BoxLayout(panel431, BoxLayout.PAGE_AXIS));
        svFieldLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        svFieldLabel.setMinimumSize(new Dimension(60, new JTextField("X").getHeight() + 5));
        panel431.add(svFieldLabel);

        valueFieldLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        valueFieldLabel.setMinimumSize(new Dimension(60, new JTextField("X").getHeight() + 5));
        panel431.add(valueFieldLabel);
        panel43.add(panel431);

        JPanel panel432 = new JPanel(); // entry fields
        panel432.setMaximumSize(new Dimension(50, 50));
        panel432.setPreferredSize(new Dimension(50, 50));
        panel432.setMinimumSize(new Dimension(50, 50));
        panel432.setLayout(new BoxLayout(panel432, BoxLayout.PAGE_AXIS));
        panel432.add(svField); // entry field (decimal) for SV number to read/write
        panel432.add(valueField); // entry field (decimal) for CV value
        panel43.add(panel432);

        JPanel panel433 = new JPanel(); // read/write buttons
        panel433.setLayout(new BoxLayout(panel433, BoxLayout.PAGE_AXIS));
        panel433.add(readButton);
        readButton.setEnabled(true);
        readButton.addActionListener(e -> readButtonActionPerformed());

        panel433.add(writeButton);
        writeButton.setEnabled(false); // disabled button, to write we point to Roster in button tooltip
        writeButton.addActionListener(e -> writeButtonActionPerformed());
        writeButton.setToolTipText(Bundle.getMessage("ButtonWriteInactiveTip"));
        panel43.add(panel433);
        panel4.add(panel43);

        panel4.add(Box.createHorizontalGlue()); // this will expand/contract
        panel4.setAlignmentX(Component.CENTER_ALIGNMENT);

        return panel4;
    }

    /*
     * Initialize the Status panel.
     */
    protected JPanel initStatusPanel() {
        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.PAGE_AXIS));

        statusText1.setText("   ");
        statusText1.setHorizontalAlignment(JLabel.CENTER);
        panel2.add(statusText1);

        statusText2.setText("   ");
        statusText2.setHorizontalAlignment(JLabel.CENTER);
        panel2.add(statusText2);

        panel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        return panel2;
    }

    /**
     * PROBE button.
     */
    public void probeAllButtonActionPerformed() {
        // send probeAll command onto LocoNet
        statusText1.setText(Bundle.getMessage("FeedBackProbing"));
        probeAllButton.setText(Bundle.getMessage("ButtonProbing"));
        LocoNetMessage m = Lnsv1MessageContents.createBroadcastProbeAll();
        memo.getLnTrafficController().sendLocoNetMessage(m);
        // wait for replies
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // retain if needed later
            return; // interrupt kills the thread
        }
        statusText1.setText(Bundle.getMessage("FeedBackProbingStop"));
        probeAllButton.setText(Bundle.getMessage("ButtonProbe"));
    }

    // MODULE_SET_ADDRESS button
    /**
     * Write SV1 and (optionally) SV2.
     */
    public void setAllAddressButtonActionPerformed() {
        addressField.setBackground(Color.WHITE);
        subAddressField.setBackground(Color.WHITE);
        if (addressField.getText().isEmpty() || subAddressField.getText().isEmpty()) {
            statusText1.setText(Bundle.getMessage("FeedBackEnterHiLoAddress"));
            if (addressField.getText().isEmpty()) {
                addressField.setBackground(Color.RED);
            } else {
                subAddressField.setBackground(Color.RED);
            }
            setAllAddressButton.setSelected(false);
            return;
        }
        // show dialog to protect unwanted ALL messages
        Object[] dialogBoxButtonOptions = {
                Bundle.getMessage("ButtonProceed"),
                Bundle.getMessage("ButtonCancel")};
        int userReply = JmriJOptionPane.showOptionDialog(this.getParent(),
                Bundle.getMessage("DialogAllLnsv1Warning"),
                Bundle.getMessage("WarningTitle"),
                JmriJOptionPane.DEFAULT_OPTION, JmriJOptionPane.QUESTION_MESSAGE,
                null, dialogBoxButtonOptions, dialogBoxButtonOptions[1]);
        if (userReply != 0 ) { // not array position 0 ButtonProceed
            return;
        }
        if ((!addressField.getText().isEmpty()) && (!subAddressField.getText().isEmpty())) {
            try {
                addr = inDomain(addressField.getText(), 1, 127); // goes in DST_L as module low address
                subAddr = inDomain(subAddressField.getText(), 1,127); // goes in d5 as module high address
                // check & warn for reserved LocoBuffer 0x50/0d80 address
                if (addr == 0x50) {
                    locoBufferReservedAddress();
                    return;
                }
                setAllAddressButton.setEnabled(false);
                statusText1.setText(Bundle.getMessage("FeedBackModAddrStart", addr, subAddr));
                addressField.setEditable(false); // lock addressL & H fields to prevent accidentally changing it
                subAddressField.setEditable(false);
                LocoNetMessage[] messageArray = Lnsv1MessageContents.createBroadcastSetAddress(addr, subAddr);
                // send Lnsv1 broadcast write address command(s) onto LocoNet
                for (LocoNetMessage m : messageArray) {
                    if (m != null) {
                        memo.getLnTrafficController().sendLocoNetMessage(m);
                    }
                }
                // wait a second for replies
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // retain if needed later
                    return; // interrupt kills the thread
                }
                setAllAddressButton.setEnabled(true);
                addressField.setEditable(true); // unlock addressL fields
                subAddressField.setEditable(true);
            } catch (NumberFormatException e) {
                statusText1.setText(Bundle.getMessage("FeedBackEnterHiLoAddress"));
                log.error("invalid entry, both must be numbers");
            }
        }
    }

    // READ_SV button
    /**
     * Handle Read CV button, assemble LNSVf1 read message. Requires presence of memo.
     */
    public void readButtonActionPerformed() {
        addressField.setBackground(Color.WHITE);
        subAddressField.setBackground(Color.WHITE);
        svField.setBackground(Color.WHITE);
        if (addressField.getText().isEmpty() || subAddressField.getText().isEmpty()) {
            statusText1.setText(Bundle.getMessage("FeedBackEnterHiLoAddress"));
            if (addressField.getText().isEmpty()) {
                addressField.setBackground(Color.RED);
            } else {
                subAddressField.setBackground(Color.RED);
            }
            setAllAddressButton.setSelected(false);
            return;
        }
        if (svField.getText().isEmpty()) {
            svField.setBackground(Color.RED);
            statusText1.setText(Bundle.getMessage("FeedBackEnterSv"));
            return;
        }
        try {
            addr = inDomain(addressField.getText(), 1,127); // goes in DST_L, used as module base address
            subAddr = inDomain(subAddressField.getText(), 1,127); // goes in D5, used as module subaddress
            // check & warn for reserved LocoBuffer 0x50/0d80 address
            if (addr == 0x50) {
                locoBufferReservedAddress();
                return;
            }
            sv = inDomain(svField.getText(), 0,127); // decimal entry
            log.debug("ReadButtonPressed adrL={}, sub={}, sv={}", addr, subAddr, sv);
            LocoNetMessage m = Lnsv1MessageContents.createSv1ReadRequest(addr, subAddr, sv);
            memo.getLnTrafficController().sendLocoNetMessage(m);
        } catch (NumberFormatException e) {
            statusText1.setText(Bundle.getMessage("FeedBackEnterNumbers"));
            log.error("invalid entry, must be numbers");
        }
        // stop and inform user
        statusText1.setText(Bundle.getMessage("FeedBackRead", "LNSV1"));
    }

    // WriteCV button
    /**
     * Handle Write button click, assemble LNSVf1 write message. Requires presence of memo.
     */
    public void writeButtonActionPerformed() {
        if (addressField.getText() != null && subAddressField.getText() != null
                && (svField.getText() != null) && (valueField.getText() != null)) {
            try {
                addr = inDomain(addressField.getText(), 1,0x7F); // goes in DST_L as module low address
                subAddr = inDomain(subAddressField.getText(), 1,0x7F); // goes in d5 as module high address
                // check & warn for reserved LocoBuffer 0x50/0d80 address
                if (addr == 0x50) {
                    locoBufferReservedAddress();
                    return;
                }
                sv = inDomain(svField.getText(), 1,0x7F); // decimal entry
                val = inDomain(valueField.getText(), 0,0x7F); // decimal entry
                if (sv == 100 || sv == 80) {
                    // reserved general module address, warn in status and abort
                    statusText1.setText(Bundle.getMessage("FeedBackValidAddressRange"));
                    valueField.setBackground(Color.RED);
                    return;
                }
                writeConfirmed = false;
                LocoNetMessage m = Lnsv1MessageContents.createSv1WriteRequest(addr, subAddr, sv, val);
                memo.getLnTrafficController().sendLocoNetMessage(m);
                valueField.setBackground(Color.ORANGE);
            } catch (NumberFormatException e) {
                statusText1.setText(Bundle.getMessage("FeedBackEnterNumbers"));
                log.error("invalid entry, must be numbers");
            }
        } else {
            statusText1.setText(Bundle.getMessage("FeedBackEnterHiLoAddress"));
            return;
        }
        // stop and inform user
        statusText1.setText(Bundle.getMessage("FeedBackWrite", "LNSV1"));
    }

    private int inDomain(String entry, int min, int max) {
        int n = -1;
        try {
            n = Integer.parseInt(entry);
        } catch (NumberFormatException e) {
            log.error("invalid entry, must be number");
        }
        if ((min <= n) && (n <= max)) {
            return n;
        } else {
            statusText1.setText(Bundle.getMessage("FeedBackInputOutsideRange"));
            return 0;
        }
    }

    public void copyEntrytoFields(int adr) {
        addressField.setText((adr & 0x7F) + "");
        subAddressField.setText((((adr >> 8) & 0x7F) + 1) + "");
    }

    /**
     * Show dialog to warn that address 0x50 is reserved and invalid entry in LNSV1 pane
     */
    private void locoBufferReservedAddress() {
        Object[] dialogBoxButtonOptions = {
                Bundle.getMessage("ButtonOK")};
        JmriJOptionPane.showOptionDialog(this.getParent(),
                Bundle.getMessage("DialogWarnLbReserved"),
                Bundle.getMessage("WarningTitle"),
                JmriJOptionPane.DEFAULT_OPTION, JmriJOptionPane.WARNING_MESSAGE,
                null, dialogBoxButtonOptions, dialogBoxButtonOptions[0]);
    }

    /**
     * {@inheritDoc}
     * See jmri.jmrix.loconet.lnsvf1.Lnsv1MessageContents.toString().
     * Compare to {@link LnOpsModeProgrammer#message(LocoNetMessage)}.
     * We pick up the SV + value add some details to status line (LnMonitor shows on our pane too).
     * @param m a message received and analysed for LNSVf1 characteristics
     */
    @Override
    public synchronized void message(LocoNetMessage m) { // receive a LocoNet message and log it to the monitor
        // got a LocoNet message, see if it's an LNSV1 response
        if (Lnsv1MessageContents.isSupportedSv1Message(m)) {
            // raw data, to display
            String raw = (rawCheckBox.isSelected() ? ("[" + m + "] ") : "");
            // format the message text, expect it to provide consistent \n after each line
            String formatted = m.toMonitorString(memo.getSystemPrefix());
            // copy the formatted data
            reply += raw + formatted;
        } else {
            log.debug("Rejected by isSupportedSv1Message");
            return;
        }
        // use dec checkbox state to choose either LnMonitor by loconet.messageinterp
        //  using Integer.toHexString(i) and/or String.format("0x%02X", i))
        boolean addDec = decimalCheckBox.isSelected();
        Lnsv1MessageContents contents = new Lnsv1MessageContents(m);
        // Use Programmer to (read and) write individual SV's - best done via Ports tab sheet

        if (Lnsv1MessageContents.extractMessageType(m) == Lnsv1MessageContents.Sv1Command.SV1_WRITE) {
            // it's an LNSV1 WriteReply message, decode contents:
            log.debug("SV1_WRITE decode contents");
            if (contents.getSrcL() == 0x50) {
                if (contents.getDstL() == 0x00) {
                    log.debug("Write all from LocoBuffer/PC");
                    if (addDec) {
                        reply += Bundle.getMessage("SV1_WRITE_ALL_INTERPRETED_DEC",
                                contents.getSvNum(),
                                contents.getSv1D4()); // NOI18N
                    }
                } else { // write request from LocoBuffer
                    log.debug("Write request from LocoBuffer/PC");
                    if (addDec) {
                        reply += Bundle.getMessage("SV1_WRITE_INTERPRETED_DEC",
                                contents.getSrcL(),
                                contents.getSubAddress(),
                                contents.getSvNum(),
                                contents.getSv1D4()); // NOI18N
                    }
                }
            } else {
                // Write Reply from LocoIO
                log.debug("Write Reply from LocoIO");
                if (addDec) {
                    reply += Bundle.getMessage("SV1_WRITE_REPLY_INTERPRETED_DEC",
                            contents.getSrcL(),
                            contents.getSubAddress(),
                            contents.getSvNum(),
                            contents.getSv1D8()); // NOI18N
                }
            }
        }
        if (Lnsv1MessageContents.extractMessageType(m) == Lnsv1MessageContents.Sv1Command.SV1_READ) {
            // it's an LNSV1 ReadReply message, decode contents:
            log.debug("SV1_READ decode contents");
            if (contents.getSrcL() == 0x50) {
                log.debug("Read request from LocoBuffer/PC"); // nothing else to do
                if (addDec) {
                    reply += Bundle.getMessage("SV1_READ_INTERPRETED_DEC",
                            contents.getDstL(),
                            contents.getSubAddress(),
                            contents.getSvNum()); // NOI18N
                }
            } else {
                // Read Reply from LocoIO
                log.debug("Read Reply from LocoIO");
                if (addDec) {
                    reply += Bundle.getMessage("SV1_READ_REPLY_INTERPRETED_DEC",
                            contents.getSrcL(),
                            contents.getSubAddress(),
                            contents.getSvNum(),
                            contents.getSv1D6(),
                            contents.getSv1D7(),
                            contents.getSv1D8()); // NOI18N
                }
                // storing a Module in the list using the (first) write reply is handled by loconet.Lnsv1DevicesManager
                // store in Value field if module address matches
                if (("" + contents.getSrcL()).equals(addressField.getText()) &&
                        ("" + contents.getSubAddress()).equals(subAddressField.getText()) &&
                        ("" + contents.getSvNum()).equals(svField.getText())) {
                    valueField.setText("" + contents.getSvValue());
                }
                // store the (last read) cvNum and cvValue to the Lnsv1Device
                Lnsv1Device dev = memo.getLnsv1DevicesManager().getDevice(addr, subAddr);
                if (dev != null) {
                    dev.setCvNum(contents.getSvNum());
                    dev.setCvValue(contents.getSv1D4());
                }
                memo.getLnsv1DevicesManager().firePropertyChange("DeviceListChanged", true, false);
            }
        }

        if (reply != null) { // we fool allProgFinished (copied from LNSV2 class)
            allProgFinished(null);
        }
    }

    /**
     * AllProg Session callback.
     *
     * @param error feedback from Finish process
     */
    public void allProgFinished(String error) {
        if (error != null) {
             log.error("LNSV1 process finished with error: {}", error);
             statusText2.setText(Bundle.getMessage("FeedBackDiscoverFail"));
        } else {
            synchronized (this) {
                if (lnsv1dm.getDeviceCount() == 1) {
                    statusText2.setText(Bundle.getMessage("FeedBackDiscoverSuccessOne"));
                } else {
                    statusText2.setText(Bundle.getMessage("FeedBackDiscoverSuccess", lnsv1dm.getDeviceCount()));
                }
                result.setText(reply);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (memo != null && memo.getLnTrafficController() != null) {
            // disconnect from the LnTrafficController, normally attached/detached after Discovery completed
            memo.getLnTrafficController().removeLocoNetListener(~0, this);
        }
        // and unwind swing
        if (pm != null) {
            pm.setSimplePreferenceState(rawDataCheck, rawCheckBox.isSelected());
            pm.setSimplePreferenceState(decimalDataCheck, decimalCheckBox.isSelected());
        }
        super.setVisible(false);

        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent((tpm) -> {
            synchronized (this) {
                tpm.stopPersisting(moduleTable);
            }
        });

        super.dispose();
    }

    // Testing methods

    protected synchronized String getMonitorContents(){
            return reply;
    }

    protected void setCvFields(int cvNum, int cvVal) {
        svField.setText(""+cvNum);
        if (cvVal > -1) {
            valueField.setText("" + cvVal);
        } else {
            valueField.setText("");
        }
    }

    protected synchronized Lnsv1Device getModule(int i) {
        if (lnsv1dm == null) {
            lnsv1dm = memo.getLnsv1DevicesManager();
        }
        //log.debug("lncvdm.getDeviceCount()={}", lnsv1dm.getDeviceCount());
        if (i > -1 && i < lnsv1dm.getDeviceCount()) {
            return lnsv1dm.getDeviceList().getDevice(i);
        } else {
            log.debug("getModule({}) failed", i);
            return null;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Lnsv1ProgPane.class);

}
