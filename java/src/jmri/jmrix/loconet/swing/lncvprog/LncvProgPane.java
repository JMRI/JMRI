package jmri.jmrix.loconet.swing.lncvprog;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrix.loconet.*;
import jmri.jmrix.loconet.uhlenbrock.LncvDevice;
import jmri.jmrix.loconet.uhlenbrock.LncvMessageContents;
import jmri.swing.JTablePersistenceManager;
import jmri.util.ThreadingUtil;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Frame for discovery and display of LocoNet LNCV boards.
 * Derived from xbee node config. Verified with Digikeijs DR5033 hardware.
 *
 * Some of the message formats used in this class are Copyright Uhlenbrock.de
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Uhlenbrock.
 *
 * TODO add buttons in table rows to switch to DecoderPro ops mode programmer?
 * @author Egbert Broerse Copyright (C) 2020
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
    protected JCheckBox rawCheckBox = new JCheckBox(Bundle.getMessage("ButtonShowRaw"));
    protected JTable moduleTable = null;
    protected LncvProgTableModel moduleTableModel = null;

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

    //private HashMap<Integer, LncvDevice> modules;
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

    @Override
    public synchronized void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);
        this.memo = memo;
        lncvdm = memo.getLncvDevicesManager();
        log.debug("lncvdm created {}", (lncvdm == null));
        //modules = new HashMap<>(0);
        // connect to the LnTrafficController
        if (memo.getLnTrafficController() == null) {
            log.error("No traffic controller is available");
        } else {
            // add listener
            memo.getLnTrafficController().addLocoNetListener(~0, this);
        }
//    }
//
//    /**
//     * Initialize the config window
//     */
//    @Override
//    public void initComponents() {
        pm = InstanceManager.getDefault(UserPreferencesManager.class);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // buttons at top, like SE8c pane
        add(initButtonPanel());
        add(initStatusPanel());
        // Set up the LNCV modules table
//        if (memo == null) {
//            log.error("NO LN MEMO");
//        }

        // create the data model and its table
        moduleTableModel = new LncvProgTableModel(this, memo);
        moduleTable = new JTable(moduleTableModel);
        moduleTable.setRowSelectionAllowed(false);
        moduleTable.getColumnModel().getColumn(LncvProgTableModel.OPENPRGMRBUTTONCOLUMN).setCellEditor(new ButtonEditor(new JButton()));
        moduleTable.getColumnModel().getColumn(LncvProgTableModel.OPENPRGMRBUTTONCOLUMN).setCellRenderer(new ButtonRenderer());
        moduleTable.setPreferredScrollableViewportSize(new Dimension(300, 200));

        // establish row sorting for the table
        sorter = new TableRowSorter<LncvProgTableModel>(moduleTableModel);
        moduleTable.setRowSorter(sorter);
        // establish table physical characteristics persistence
        moduleTable.setName("LNCV Device Management"); // NOI18N
        // Reset and then persist the table's ui state
        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent((tpm) -> {
            tpm.resetState(moduleTable);
            tpm.persist(moduleTable, true);
        });

        JScrollPane tableScrollPane = new JScrollPane(moduleTable);
        tablePanel = new JPanel();
        Border resultBorder = BorderFactory.createEtchedBorder();
        Border resultTitled = BorderFactory.createTitledBorder(resultBorder, Bundle.getMessage("LncvTableTitle"));
        tablePanel.setBorder(resultTitled);
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        add(tablePanel);

        add(initNotesPanel());
        rawCheckBox.setSelected(pm.getSimplePreferenceState(rawDataCheck));
    }

    /*
     * Initialize the Notes panel.
     */
    protected JPanel initNotesPanel() {
        // Set up the notes panel
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));

        JPanel panel31 = new JPanel();
        panel31.setLayout(new BoxLayout(panel31, BoxLayout.Y_AXIS));
        JScrollPane resultScrollPane = new JScrollPane(result);
        panel31.add(resultScrollPane);

        panel31.add(rawCheckBox);
        rawCheckBox.setVisible(true);
        rawCheckBox.setToolTipText(Bundle.getMessage("TooltipShowRaw")); // NOI18N
        panel3.add(panel31);
        Border panel3Border = BorderFactory.createEtchedBorder();
        Border panel3Titled = BorderFactory.createTitledBorder(panel3Border, Bundle.getMessage("LncvMonitorTitle"));
        panel3.setBorder(panel3Titled);
        return panel3;
    }

    /*
     * Initialize the Button panel.
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
        // Set up module address and node type
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
            allProgRunning = false;
            return;
        }
        try {
            art = inDomain(articleField.getText(), 9999);
        } catch (NumberFormatException e) {
            // fine, broadcast all
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
            statusText1.setText(Bundle.getMessage("FeedBackStartAllProg"));
            return;
        }
        if (articleField.getText().equals("")) {
            statusText1.setText(Bundle.getMessage("FeedBackEnterArticle", adr));
            articleField.setBackground(Color.RED);
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
        if ((articleField.getText() != null) && (addressField.getText() != null)) {
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
     * Handle Read CV button.
     */
    public void readButtonActionPerformed() {
        String sArt = "65535"; // LncvMessageContents.LNCV_ALL = broadcast
        if (moduleProgRunning >= 0) {
            sArt = articleField.getText();
            articleField.setBackground(Color.WHITE); // reset
        }
        if ((sArt != null) && (addressField.getText() != null) && (cvField.getText() != null)) {
            try {
                art = inDomain(sArt, 9999);
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
     * Handle Write button click, assemble LNCV write message
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
        statusText1.setText(Bundle.getMessage("FeedBackWritten"));
        // wait for LACK reply
        //
        // if (received) {
        //      writeConfirmed = true;
        // }
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
     * @param l a message received and analysed for LNCV characteristics
     */
    @Override
    public synchronized void message(LocoNetMessage l) { // receive a LocoNet message and log it
        // got a LocoNet message, see if it's a LNCV response
        if (LncvMessageContents.isSupportedLncvMessage(l)) {
            // raw data, to display
            String raw = (rawCheckBox.isSelected() ? l.toString() : "");
            // format the message text, expect it to provide consistent \n after each line
            String formatted = l.toMonitorString(memo.getSystemPrefix());
            // copy the formatted data
            reply += formatted + " " + raw + "\n";
        }
        // or LACK write confirmation response from module?
        if ((l.getOpCode() == LnConstants.OPC_LONG_ACK) &&
                (l.getElement(1) == 0x6D)) { // elem 1 = OPC (matches 0xED), elem 2 = ack1
            writeConfirmed = true;
            if (l.getElement(2) == 0x7f) {
                valueField.setBackground(Color.GREEN);
                reply += Bundle.getMessage("LNCV_WRITE_CONFIRMED", moduleProgRunning) + "\n";
            } else if (l.getElement(2) == 1) {
                valueField.setBackground(Color.RED);
                reply += Bundle.getMessage("LNCV_WRITE_CV_NOTSUPPORTED", moduleProgRunning) + "\n";
            } else if (l.getElement(2) == 2) {
                valueField.setBackground(Color.RED);
                reply += Bundle.getMessage("LNCV_WRITE_CV_READONLY", moduleProgRunning) + "\n";
            } else if (l.getElement(2) == 3) {
                valueField.setBackground(Color.RED);
                reply += Bundle.getMessage("LNCV_WRITE_CV_OUTOFBOUNDS", moduleProgRunning) + "\n";
            }
        }
        if (LncvMessageContents.extractMessageType(l) == LncvMessageContents.LncvCommand.LNCV_WRITE) {
            reply += Bundle.getMessage("LNCV_WRITE_MOD_MONITOR", (moduleProgRunning == -1 ? "ALL" : moduleProgRunning)) + "\n";
        }
        if (LncvMessageContents.extractMessageType(l) == LncvMessageContents.LncvCommand.LNCV_READ) {
            reply += Bundle.getMessage("LNCV_READ_MOD_MONITOR", (moduleProgRunning == -1 ? "ALL" : moduleProgRunning)) + "\n";
        }
        if (LncvMessageContents.extractMessageType(l) == LncvMessageContents.LncvCommand.LNCV_READ_REPLY) {
            // it's a LNCV ReadReply message, decode contents:
            LncvMessageContents contents = new LncvMessageContents(l);
            int msgArt = contents.getLncvArticleNum();
            int msgAdr = moduleProgRunning;
            int msgCv = contents.getCvNum();
            int msgVal = contents.getCvValue();
            if ((msgCv == 0) || (msgArt == art)) { // trust last used address. to be sure, check against Article (hardware class) number
                msgAdr = msgVal; // if cvNum = 0, this is the LNCV module address
            }
            String foundMod = "(LNCV) art:" + art + " address:" + msgAdr + " cv:" + msgCv + " value:" + msgVal + "\n";
            reply += foundMod;
            // store Module in list using write reply, moved to LncvDevicesManager
            //LncvDevice dev = new LncvDevice(msgArt, msgAdr, msgCv, msgVal);
//            boolean inMap = false;
//            for (Map.Entry<Integer, LncvDevice> module : modules.entrySet()) {
//                if ((module.getValue().getClassNum() == art) && (module.getValue().getAddress() == msgAdr)) {
//                    module.getValue().setCvNum(msgCv);
//                    module.getValue().setCvValue(msgVal);
//                    inMap = true;
//                    break;
//                }
//            }
//            if (!inMap) {
//                modules.put(counter++, dev);
//                lncvdm.clearDevicesList();
//                ThreadingUtil.runOnLayoutEventually( ()->{
//                    lncvdm.sendLncvDiscoveryRequest();
//                });
                //log.debug("LNCV Added Module {}: {}", counter, foundMod);
//            }
            //moduleTable.revalidate();

            // enter returned CV in CVnum field
            cvField.setText(msgCv + "");
            cvField.setBackground(Color.WHITE);
            // enter returned value in Value field
            valueField.setText(msgVal + "");
            valueField.setBackground(Color.WHITE);
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
            log.debug("LNCV process completed successfully.");
            statusText2.setText(Bundle.getMessage("FeedBackDiscoverSuccess",
                    lncvdm.getDeviceCount()));
                    //(modules == null ? 0 : modules.size())));
            // reload the CV? list
            result.setText(reply);
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
            tpm.stopPersisting(moduleTable);
        });

        super.dispose();
    }

    protected LncvDevice getModule(int i) {
        if (i <= lncvdm.getDeviceCount()) {
            return lncvdm.getDeviceList().getDevice(i);
        } else {
            log.debug("getModule({}) failed", i);
            return null;
        }
    }

    private int counter = 0;

    /**
     * Get the number of modules (replies) in table.
     * @return number of Modules in modules hashmap
     */
    public int getCount() {
        return counter;
    }

//    /**
//     * Store elements received on LNCV reply message.
//     */
//    static class LncvModule {
//        private final int classNum;
//        private int address = 0; // Module address in reply
//        private int cvNum;
//        private int cvValue;
//
//        LncvModule(int[] response) {
//            classNum = response[0];
//            //address = response[1];
//            cvNum = response[1];
//            cvValue = response[2];
//            log.debug("Added Module {}", classNum);
//        }
//
//        int getAddress() {
//            return address;
//        }
//        void setAddress(int addr) {
//            address = addr;
//        }
//        int getClassNum() {
//            return classNum;
//        }
//        int getCvNum() {
//            return cvNum;
//        }
//        void setCvNum(int num) {
//            cvNum = num;
//        }
//        int getCvValue() {
//            return cvValue;
//        }
//        void setCvValue(int val) {
//            cvValue = val;
//        }
//
//    }

    private final static Logger log = LoggerFactory.getLogger(LncvProgPane.class);

}
