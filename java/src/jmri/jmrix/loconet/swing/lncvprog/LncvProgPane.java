package jmri.jmrix.loconet.swing.lncvprog;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableRowSorter;
import java.awt.*;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrix.loconet.*;
import jmri.jmrix.loconet.uhlenbrock.LncvDevice;
import jmri.jmrix.loconet.uhlenbrock.LncvMessageContents;
import jmri.swing.JTablePersistenceManager;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for discovery and display of LocoNet LNCV boards.
 * Derived from xbee node config. Verified with Digikeijs DR5033 hardware.
 *
 * Some of the message formats used in this class are Copyright Uhlenbrock.de
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Uhlenbrock.
 *
 * Buttons in table row allows to add roster entry for device, and switch to the
 * DecoderPro ops mode programmer.
 *
 * @author Egbert Broerse Copyright (C) 2021
 */
public class LncvProgPane extends jmri.jmrix.loconet.swing.LnPanel implements LocoNetListener {

    private LocoNetSystemConnectionMemo memo;
    protected JToggleButton allProgButton = new JToggleButton();
    protected JToggleButton modProgButton = new JToggleButton();
    protected JButton readButton = new JButton(Bundle.getMessage("ButtonRead"));
    protected JButton writeButton = new JButton(Bundle.getMessage("ButtonWrite"));
    protected JTextField articleField = new JTextField(4);
    protected JTextField addressField = new JTextField(4);
    protected JTextField cvField = new JTextField(4);
    protected JTextField valueField = new JTextField(4);
    protected JCheckBox directCheckBox = new JCheckBox(Bundle.getMessage("DirectModeBox"));
    protected JCheckBox rawCheckBox = new JCheckBox(Bundle.getMessage("ButtonShowRaw"));
    protected JTable moduleTable = null;
    protected LncvProgTableModel moduleTableModel = null;
    public static final int ROW_HEIGHT = (new JButton("X").getPreferredSize().height)*9/10;

    protected JPanel tablePanel = null;
    protected JLabel statusText1 = new JLabel();
    protected JLabel statusText2 = new JLabel();
    protected JLabel articleFieldLabel = new JLabel(Bundle.getMessage("LabelArticleNum", JLabel.RIGHT));
    protected JLabel addressFieldLabel = new JLabel(Bundle.getMessage("LabelModuleAddress", JLabel.RIGHT));
    protected JLabel cvFieldLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("HeadingCv")), JLabel.RIGHT);
    protected JLabel valueFieldLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("HeadingValue")), JLabel.RIGHT);
    protected JTextArea result = new JTextArea(6,50);
    protected String reply = "";
    protected int art;
    protected int adr = 1;
    protected int cv = 0;
    protected int val;
    boolean writeConfirmed = false;
    private final String rawDataCheck = this.getClass().getName() + ".RawData"; // NOI18N
    private UserPreferencesManager pm;
    private transient TableRowSorter<LncvProgTableModel> sorter;
    private LncvDevicesManager lncvdm;

    private boolean allProgRunning = false;
    private int moduleProgRunning = -1; // stores module address as int during moduleProgramming session, -1 = no session

    /**
     * Constructor method
     */
    public LncvProgPane() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.swing.lncvprog.LncvProgPane"; // NOI18N
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("MenuItemLncvProg");
    }

    /**
     * Initialize the config window
     */
    @Override
    public void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        // buttons at top, like SE8c pane
        add(initButtonPanel()); // requires presence of memo.
        add(initDirectPanel()); // starts hidden, to set bits in Direct Mode only
        add(initStatusPanel()); // positioned after ButtonPanel so to keep it simple also delayed
        // creation of table must wait for memo + tc to be available, see initComponents(memo) next
    }

    @Override
    public synchronized void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);
        this.memo = memo;
        lncvdm = memo.getLncvDevicesManager();
        pm = InstanceManager.getDefault(UserPreferencesManager.class);
        // connect to the LnTrafficController
        if (memo.getLnTrafficController() == null) {
            log.error("No traffic controller is available");
        } else {
            // add listener
            memo.getLnTrafficController().addLocoNetListener(~0, this);
        }

        // create the data model and its table
        moduleTableModel = new LncvProgTableModel(this, memo);
        moduleTable = new JTable(moduleTableModel);
        moduleTable.setRowSelectionAllowed(false);
        moduleTable.setPreferredScrollableViewportSize(new Dimension(300, 200));
        moduleTable.setRowHeight(ROW_HEIGHT);
        moduleTable.setDefaultEditor(JButton.class, new ButtonEditor(new JButton()));
        moduleTable.setDefaultRenderer(JButton.class, new ButtonRenderer());
        moduleTable.setRowSelectionAllowed(true);
        moduleTable.getSelectionModel().addListSelectionListener(event -> {
            synchronized (this) {
                if (moduleTable.getSelectedRow() > -1 && moduleTable.getSelectedRow() < moduleTable.getRowCount()) {
                    // print first column value from selected row
                    copyEntry((int) moduleTable.getValueAt(moduleTable.getSelectedRow(), 1), (int) moduleTable.getValueAt(moduleTable.getSelectedRow(), 2));
                }
            }
        });
        // establish row sorting for the table
        sorter = new TableRowSorter<>(moduleTableModel);
        moduleTable.setRowSorter(sorter);
         // establish table physical characteristics persistence
        moduleTable.setName("LNCV Device Management"); // NOI18N
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
        Border resultTitled = BorderFactory.createTitledBorder(resultBorder, Bundle.getMessage("LncvTableTitle"));
        tablePanel.setBorder(resultTitled);
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        // this does not fill the full width, why?
//        JSplitPane holder = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
//                tablePanel, getMonitorPanel());
//        holder.setMinimumSize(new Dimension(1000, 400));
//        holder.setPreferredSize(new Dimension(1000, 400));
//        holder.setDividerSize(8);
//        holder.setOneTouchExpandable(true);
//        add(holder, BorderLayout.LINE_START);
        add(tablePanel);
        add(getMonitorPanel());
        rawCheckBox.setSelected(pm.getSimplePreferenceState(rawDataCheck));
    }

    /*
     * Initialize the LNCV Monitor panel.
     */
    protected JPanel getMonitorPanel() {
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));

        JPanel panel31 = new JPanel();
        panel31.setLayout(new BoxLayout(panel31, BoxLayout.Y_AXIS));
        JScrollPane resultScrollPane = new JScrollPane(result);
        panel31.add(resultScrollPane);

        panel31.add(rawCheckBox);
        rawCheckBox.setVisible(true);
        rawCheckBox.setToolTipText(Bundle.getMessage("TooltipShowRaw"));
        panel3.add(panel31);
        Border panel3Border = BorderFactory.createEtchedBorder();
        Border panel3Titled = BorderFactory.createTitledBorder(panel3Border, Bundle.getMessage("LncvMonitorTitle"));
        panel3.setBorder(panel3Titled);
        return panel3;
    }

    /*
     * Initialize the Button panel. Requires presence of memo to send and receive.
     */
    protected JPanel initButtonPanel() {
        // Set up buttons and entry fields
        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout());

        JPanel panel41 = new JPanel();
        panel41.setLayout(new BoxLayout(panel41, BoxLayout.PAGE_AXIS));
        allProgButton.setText(allProgRunning ?
                Bundle.getMessage("ButtonStopAllProg") : Bundle.getMessage("ButtonStartAllProg"));
        allProgButton.setToolTipText(Bundle.getMessage("TipAllProgButton"));
        allProgButton.addActionListener(e -> allProgButtonActionPerformed());
        panel41.add(allProgButton);

        modProgButton.setText((moduleProgRunning >= 0) ?
                Bundle.getMessage("ButtonStopModProg") : Bundle.getMessage("ButtonStartModProg"));
        modProgButton.setToolTipText(Bundle.getMessage("TipModuleProgButton"));
        modProgButton.addActionListener(e -> modProgButtonActionPerformed());
        panel41.add(modProgButton);
        panel4.add(panel41);

        JPanel panel42 = new JPanel();
        panel42.setLayout(new BoxLayout(panel42, BoxLayout.PAGE_AXIS));
        JPanel panel421 = new JPanel();
        panel421.add(articleFieldLabel);
        // entry field (decimal)
        articleField.setToolTipText(Bundle.getMessage("TipModuleArticleField"));
        panel421.add(articleField);
        panel42.add(panel421);

        JPanel panel422 = new JPanel();
        panel422.add(addressFieldLabel);
        // entry field (decimal) for Module Address
        addressField.setText("1");
        panel422.add(addressField);
        panel42.add(panel422);
        panel42.add(directCheckBox);
        directCheckBox.addActionListener(e -> directActionPerformed());
        directCheckBox.setToolTipText(Bundle.getMessage("TipDirectMode"));
        panel4.add(panel42);

        JPanel panel43 = new JPanel();
        Border panel43Border = BorderFactory.createEtchedBorder();
        panel43.setBorder(panel43Border);
        panel43.setLayout(new BoxLayout(panel43, BoxLayout.LINE_AXIS));

        JPanel panel431 = new JPanel();
        panel431.setLayout(new BoxLayout(panel431, BoxLayout.PAGE_AXIS));
        JPanel panel4311 = new JPanel();
        panel4311.add(cvFieldLabel);
        // entry field (decimal) for CV number to read/write
        //cvField.setToolTipText(Bundle.getMessage("TipModuleCvField"));
        cvField.setText("0");
        panel4311.add(cvField);
        panel431.add(panel4311);

        JPanel panel4312 = new JPanel();
        panel4312.add(valueFieldLabel);
        // entry field (decimal) for CV value
        //valueField.setToolTipText(Bundle.getMessage("TipModuleValueField"));
        valueField.setText("1");
        panel4312.add(valueField);
        panel431.add(panel4312);
        panel43.add(panel431);

        JPanel panel432 = new JPanel();
        panel432.setLayout(new BoxLayout(panel432, BoxLayout.PAGE_AXIS));
        panel432.add(readButton);
        readButton.setEnabled(false);
        readButton.addActionListener(e -> readButtonActionPerformed());

        panel432.add(writeButton);
        writeButton.setEnabled(false);
        writeButton.addActionListener(e -> writeButtonActionPerformed());
        panel43.add(panel432);
        panel4.add(panel43);

        return panel4;
    }

    /*
     * Initialize the Status panel.
     */
    protected JPanel initStatusPanel() {
        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.PAGE_AXIS));
        JPanel panel21 = new JPanel();
        panel21.setLayout(new FlowLayout());

        statusText1.setText("   ");
        statusText1.setHorizontalAlignment(JLabel.CENTER);
        panel21.add(statusText1);
        panel2.add(panel21);

        statusText2.setText("   ");
        statusText2.setHorizontalAlignment(JLabel.CENTER);
        panel2.add(statusText2);
        return panel2;
    }

    /**
     * GENERALPROG button.
     */
    public void allProgButtonActionPerformed() {
        if (moduleProgRunning >= 0) {
            statusText1.setText(Bundle.getMessage("FeedBackModProgRunning"));
            return;
        }
        if (directCheckBox.isSelected()) {
            statusText1.setText(Bundle.getMessage("FeedBackDirectRunning"));
            return;
        }
        // provide user feedback
        readButton.setEnabled(!allProgRunning);
        writeButton.setEnabled(!allProgRunning);
        log.debug("AllProg pressed, allProgRunning={}", allProgRunning);
        if (allProgRunning) {
            log.debug("Session was running, closing");
            // send LncvAllProgEnd command on LocoNet
            memo.getLnTrafficController().sendLocoNetMessage(LncvMessageContents.createAllProgEndRequest(art));
            statusText1.setText(Bundle.getMessage("FeedBackStopAllProg"));
            allProgButton.setText(Bundle.getMessage("ButtonStartAllProg"));
            articleField.setEditable(true);
            addressField.setEditable(true);
            allProgRunning = false;
            return;
        }
        articleField.setEditable(false);
        addressField.setEditable(false);
        art = -1;
        if (!articleField.getText().equals("")) {
            try {
                art = inDomain(articleField.getText(), 9999);
            } catch (NumberFormatException e) {
                // fine, will do broadcast all
            }
        }
        // show dialog to protect unwanted ALL messages
        Object[] dialogBoxButtonOptions = {
                Bundle.getMessage("ButtonProceed"),
                Bundle.getMessage("ButtonCancel")};
        int userReply = JOptionPane.showOptionDialog(this.getParent(),
                Bundle.getMessage("DialogAllWarning"),
                Bundle.getMessage("WarningTitle"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, dialogBoxButtonOptions, dialogBoxButtonOptions[1]);
        if (userReply != 0) {
            return;
        }
        statusText1.setText(Bundle.getMessage("FeedBackStartAllProg"));
        // send LncvProgSessionStart command on LocoNet
        LocoNetMessage m = LncvMessageContents.createAllProgStartRequest(art);
        memo.getLnTrafficController().sendLocoNetMessage(m);
        // stop and inform user
        statusText1.setText(Bundle.getMessage("FeedBackStartAllProg"));
        allProgButton.setText(Bundle.getMessage("ButtonStopAllProg"));
        allProgRunning = true;
    }

    // MODULEPROG button
    /**
     * Handle Start/End Module Prog button.
     */
    public void modProgButtonActionPerformed() {
        if (allProgRunning) {
            statusText1.setText(Bundle.getMessage("FeedBackAllProgRunning"));
            return;
        }
        if (directCheckBox.isSelected()) {
            statusText1.setText(Bundle.getMessage("FeedBackDirectRunning"));
            return;
        }
        if (articleField.getText().equals("")) {
            statusText1.setText(Bundle.getMessage("FeedBackEnterArticle"));
            articleField.setBackground(Color.RED);
            modProgButton.setSelected(false);
            return;
        }
        if (addressField.getText().equals("")) {
            statusText1.setText(Bundle.getMessage("FeedBackEnterAddress"));
            addressField.setBackground(Color.RED);
            modProgButton.setSelected(false);
            return;
        }
        // provide user feedback
        articleField.setBackground(Color.WHITE); // reset
        readButton.setEnabled(moduleProgRunning < 0);
        writeButton.setEnabled(moduleProgRunning < 0);
        if (moduleProgRunning >= 0) { // stop prog
            try {
                art = inDomain(articleField.getText(), 9999);
                adr = moduleProgRunning; // use module address that was used to start Modprog
                memo.getLnTrafficController().sendLocoNetMessage(LncvMessageContents.createModProgEndRequest(art, adr));
                statusText1.setText(Bundle.getMessage("FeedBackModProgClosed", adr));
                modProgButton.setText(Bundle.getMessage("ButtonStartModProg"));
                moduleProgRunning = -1;
                articleField.setEditable(true);
                addressField.setEditable(true);
            } catch (NumberFormatException e) {
                statusText1.setText(Bundle.getMessage("FeedBackEnterArticle"));
                modProgButton.setSelected(true);
            }
            return;
        }
        if ((!articleField.getText().equals("")) && (!addressField.getText().equals(""))) {
            try {
                art = inDomain(articleField.getText(), 9999);
                adr = inDomain(addressField.getText(), 65535); // goes in d5-d6 as module address
                memo.getLnTrafficController().sendLocoNetMessage(LncvMessageContents.createModProgStartRequest(art, adr));
                statusText1.setText(Bundle.getMessage("FeedBackModProgOpen", adr));
                modProgButton.setText(Bundle.getMessage("ButtonStopModProg"));
                moduleProgRunning = adr; // store address during modProg, so next line is mostly as UI indication:
                articleField.setEditable(false);
                addressField.setEditable(false); // lock address field to prevent accidentally changing it

            } catch (NumberFormatException e) {
                log.error("invalid entry, must be number");
            }
        }
        // stop and inform user
    }

    // READCV button
    /**
     * Handle Read CV button, assemble LNCV read message. Requires presence of memo.
     */
    public void readButtonActionPerformed() {
        String sArt = "65535"; // LncvMessageContents.LNCV_ALL = broadcast
        if (moduleProgRunning >= 0) {
            sArt = articleField.getText();
            articleField.setBackground(Color.WHITE); // reset
        }
        if ((sArt != null) && (addressField.getText() != null) && (cvField.getText() != null)) {
            try {
                art = inDomain(sArt, 9999); // limited according to Uhlenbrock info
                adr = inDomain(addressField.getText(), 65535); // used as address for reply
                cv = inDomain(cvField.getText(), 9999); // decimal entry
                memo.getLnTrafficController().sendLocoNetMessage(LncvMessageContents.createCvReadRequest(art, adr, cv));
            } catch (NumberFormatException e) {
                log.error("invalid entry, must be number");
            }
        } else {
            statusText1.setText(Bundle.getMessage("FeedBackEnterArticle"));
            articleField.setBackground(Color.RED);
            return;
        }
        // stop and inform user
        statusText1.setText(Bundle.getMessage("FeedBackRead"));
    }

    // WriteCV button
    /**
     * Handle Write button click, assemble LNCV write message. Requires presence of memo.
     */
    public void writeButtonActionPerformed() {
        String sArt = "65535"; // LncvMessageContents.LNCV_ALL;
        if (moduleProgRunning >= 0) {
            sArt = articleField.getText();
        }
        if ((sArt != null) && (cvField.getText() != null) && (valueField.getText() != null)) {
            articleField.setBackground(Color.WHITE);
            try {
                art = inDomain(sArt, 9999);
                cv = inDomain(cvField.getText(), 9999); // decimal entry
                val = inDomain(valueField.getText(), 65535); // decimal entry
                if (cv == 0 && (val > 65534 || val < 1)) {
                    // reserved general module address, warn in status and abort
                    statusText1.setText(Bundle.getMessage("FeedBackValidAddressRange"));
                    valueField.setBackground(Color.RED);
                    return;
                }
                writeConfirmed = false;
                memo.getLnTrafficController().sendLocoNetMessage(LncvMessageContents.createCvWriteRequest(art, cv, val));
                valueField.setBackground(Color.ORANGE);
            } catch (NumberFormatException e) {
                log.error("invalid entry, must be number");
            }
        } else {
            statusText1.setText(Bundle.getMessage("FeedBackEnterArticle"));
            articleField.setBackground(Color.RED);
            return;
        }
        // stop and inform user
        statusText1.setText(Bundle.getMessage("FeedBackWrite"));
        // LACK reply will be received separately
        // if (received) {
        //      writeConfirmed = true;
        // }
    }

    private JPanel ledPanel;

    // a row of checkboxes to set LEDs in module on/off
    private JPanel initDirectPanel() {
        ledPanel = new JPanel();
        for (int i = 0; i < 16; i++) {
            JCheckBox ledBox = new JCheckBox(""+i);
            ledPanel.add(ledBox);
        }
        JPanel options = new JPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        JToggleButton buttonAll = new JToggleButton(Bundle.getMessage("AllOn"));
        buttonAll.addActionListener(e -> toggleAll(buttonAll.isSelected()));
        options.add(buttonAll);
        JCheckBox serieTwo = new JCheckBox("LED2");
        serieTwo.addActionListener(e -> renumber(serieTwo.isSelected()));
        options.add(serieTwo); // place to the right of Set button
        ledPanel.add(options);
        JButton buttonSet = new JButton(Bundle.getMessage("ButtonSetDirect"));
        ledPanel.add(buttonSet);
        buttonSet.addActionListener(e -> setDirect(serieTwo.isSelected()));
        ledPanel.setVisible(false); // initially hide ledPanel
        return ledPanel;
    }

    private void toggleAll(boolean on) {
        for (int j = 0; j < 16 ; j++) {
            ((JCheckBox)ledPanel.getComponent(j)).setSelected(on);
        }
    }

    protected void directActionPerformed() {
        if (allProgRunning || moduleProgRunning > -1) {
            directCheckBox.setSelected(false);
            return;
        }
        if (directCheckBox.isSelected()) {
            articleField.setEditable(false);
            articleField.setText("6900"); // fixed article number as per documentation
            articleField.setBackground(Color.WHITE); // reset
            readButton.setEnabled (false);
            ledPanel.setVisible(true);
        } else {
            articleField.setText("");
            articleField.setEditable(true);
            readButton.setEnabled (true);
            ledPanel.setVisible(false);
        }
    }

    /**
     * Renumber the checkbox labels to match LED numbers.
     * @param range2 false for LEDs 0-15, true for LEDs 16-31
     */
    protected void renumber(boolean range2) {
        for (int j = 0; j < 16 ; j++) {
            ((JCheckBox)ledPanel.getComponent(j)).setText(range2 ? ""+(j+16) : ""+j);
        }
    }

    // SetDirect button
    /**
     * Handle SetDirect button, assemble LNCV Direct Set message. Requires presence of memo to send.
     * @param range2 false for LEDs 0-15, true for LEDs 16-31
     */
    protected void setDirect(boolean range2) {
        if (addressField.getText() != null) {
            try {
                adr = inDomain(addressField.getText(), 65535);
                int cv = 0x00;
                // fetch the bits as set on the ledPanel
                for (int j = 0; j < 16 ; j++) {
                    cv += (((JCheckBox)ledPanel.getComponent(j)).isSelected() ? (1 << j) : 0);
                    //log.debug("j={} cv={}", j, cv);
                }
                memo.getLnTrafficController().sendLocoNetMessage(LncvMessageContents.createDirectWriteRequest(adr, cv, range2));
            } catch (NumberFormatException e) {
                log.error("invalid entry, must be number");
            }
        } else {
            statusText1.setText(Bundle.getMessage("FeedBackEnterArticle"));
            addressField.setBackground(Color.RED);
            return;
        }
        // stop and inform user
        statusText1.setText(Bundle.getMessage("FeedBackSetDirect"));
    }

    private int inDomain(String entry, int max) {
        int n = -1;
        try {
            n = Integer.parseInt(entry);
        } catch (NumberFormatException e) {
            log.error("invalid entry, must be number");
        }
        if ((0 <= n) && (n <= max)) {
            return n;
        } else {
            statusText1.setText(Bundle.getMessage("FeedBackInputOutsideRange"));
            return 0;
        }
    }

    public void copyEntry(int art, int mod) {
        if ((moduleProgRunning < 0) && !allProgRunning) { // protect locked fields while programming
            articleField.setText(art + "");
            addressField.setText(mod + "");
        }
    }

    /**
     * {@inheritDoc}
     * Compare to {@link LnOpsModeProgrammer#message(jmri.jmrix.loconet.LocoNetMessage)}
     *
     * @param m a message received and analysed for LNCV characteristics
     */
    @Override
    public synchronized void message(LocoNetMessage m) { // receive a LocoNet message and log it
        // got a LocoNet message, see if it's an LNCV response
        //log.debug("LncvProgPane heard message {}", m.toMonitorString());
        if (LncvMessageContents.isSupportedLncvMessage(m)) {
            // raw data, to display
            String raw = (rawCheckBox.isSelected() ? ("[" + m.toString() + "] ") : "");
            // format the message text, expect it to provide consistent \n after each line
            String formatted = m.toMonitorString(memo.getSystemPrefix());
            // copy the formatted data
            reply += raw + formatted;
        }
        // or LACK write confirmation response from module?
        if ((m.getOpCode() == LnConstants.OPC_LONG_ACK) &&
                (m.getElement(1) == 0x6D)) { // elem 1 = OPC (matches 0xED), elem 2 = ack1
            writeConfirmed = true;
            if (m.getElement(2) == 0x7f) {
                valueField.setBackground(Color.GREEN);
                reply += Bundle.getMessage("LNCV_WRITE_CONFIRMED", moduleProgRunning) + "\n";
            } else if (m.getElement(2) == 1) {
                valueField.setBackground(Color.RED);
                reply += Bundle.getMessage("LNCV_WRITE_CV_NOTSUPPORTED", moduleProgRunning, cv) + "\n";
            } else if (m.getElement(2) == 2) {
                valueField.setBackground(Color.RED);
                reply += Bundle.getMessage("LNCV_WRITE_CV_READONLY", moduleProgRunning, cv) + "\n";
            } else if (m.getElement(2) == 3) {
                valueField.setBackground(Color.RED);
                reply += Bundle.getMessage("LNCV_WRITE_CV_OUTOFBOUNDS", moduleProgRunning, val) + "\n";
            }
        }
        if (LncvMessageContents.extractMessageType(m) == LncvMessageContents.LncvCommand.LNCV_WRITE) {
            reply += Bundle.getMessage("LNCV_WRITE_MOD_MONITOR", (moduleProgRunning == -1 ? "ALL" : moduleProgRunning)) + "\n";
        }
        if (LncvMessageContents.extractMessageType(m) == LncvMessageContents.LncvCommand.LNCV_READ) {
            reply += Bundle.getMessage("LNCV_READ_MOD_MONITOR", (moduleProgRunning == -1 ? "ALL" : moduleProgRunning)) + "\n";
        }
        if (LncvMessageContents.extractMessageType(m) == LncvMessageContents.LncvCommand.LNCV_READ_REPLY) {
            // it's a LNCV ReadReply message, decode contents:
            LncvMessageContents contents = new LncvMessageContents(m);
            int msgArt = contents.getLncvArticleNum();
            int msgAdr = moduleProgRunning;
            int msgCv = contents.getCvNum();
            int msgVal = contents.getCvValue();
            if ((msgCv == 0) || (msgArt == art)) { // trust last used address. to be sure, check against Article (hardware class) number
                msgAdr = msgVal; // if cvNum = 0, this is the LNCV module address
            }
            String foundMod = "(LNCV) " + Bundle.getMessage("LabelArticle") +  art + " "
                    + Bundle.getMessage("LabelAddress") + msgAdr + " "
                    + Bundle.getMessage("LabelCv") + msgCv + " "
                    + Bundle.getMessage("LabelValue")+ msgVal + "\n";
            reply += foundMod;
            // store Module in list using write reply is handled by loconet.LncvDevicesManager

            // enter returned CV in CVnum field
            cvField.setText(msgCv + "");
            cvField.setBackground(Color.WHITE);
            // enter returned value in Value field
            valueField.setText(msgVal + "");
            valueField.setBackground(Color.WHITE);

            LncvDevice dev = memo.getLncvDevicesManager().getDevice(art, adr);
            if (dev != null) {
                dev.setCvNum(msgCv);
                dev.setCvValue(msgVal);
            }
            memo.getLncvDevicesManager().firePropertyChange("DeviceListChanged", true, false);
        }

        if (reply != null) {
            allProgFinished(null);
        }
    }

    /*
     * AllProg Session callback.
     */
    public void allProgFinished(String error) {
        if (error != null) {
             log.error("LNCV process finished with error: {}", error);
             statusText2.setText(Bundle.getMessage("FeedBackDiscoverFail"));
        } else {
            synchronized (this) {
                statusText2.setText(Bundle.getMessage("FeedBackDiscoverSuccess", lncvdm.getDeviceCount()));
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
        }
        if (moduleProgRunning >= 0) {
            modProgButtonActionPerformed();
        }
        if (allProgRunning) {
            allProgButtonActionPerformed();
        }
        super.setVisible(false);

        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent((tpm) -> {
            synchronized (this) {
                tpm.stopPersisting(moduleTable);
            }
        });

        super.dispose();
    }

    /**
     * Testing methods.
     *
     * @return text currently in Article field
     */
    protected String getArticleEntry() {
        if (!articleField.isEditable()) {
            return "locked";
        } else {
            return articleField.getText();
        }
    }

    protected String getAddressEntry() {
        if (!addressField.isEditable()) {
            return "locked";
        } else {
            return addressField.getText();
        }
    }

    protected synchronized String getMonitorContents(){
            return reply;
    }

    protected void setCvFields(int cvNum, int cvVal) {
        cvField.setText(""+cvNum);
        if (cvVal > -1) {
            valueField.setText("" + cvVal);
        } else {
            valueField.setText("");
        }
    }

    protected synchronized LncvDevice getModule(int i) {
        if (lncvdm == null) {
            lncvdm = memo.getLncvDevicesManager();
        }
        log.debug("lncvdm.getDeviceCount()={}", lncvdm.getDeviceCount());
        if (i > -1 && i < lncvdm.getDeviceCount()) {
            return lncvdm.getDeviceList().getDevice(i);
        } else {
            log.debug("getModule({}) failed", i);
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LncvProgPane.class);

}
