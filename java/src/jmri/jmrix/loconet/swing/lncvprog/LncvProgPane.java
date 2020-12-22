package jmri.jmrix.loconet.swing.lncvprog;

import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.uhlenbrock.LncvMessageContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.HashMap;

/**
 * Frame for discovery and display of LocoNet LNCV boards.
 * Derived from xbee node config.
 *
 * @author Bob Jacobsen Copyright (C) 2004
 * @author Dave Duchamp Copyright (C) 2004
 * @author Paul Bender Copyright (C) 2013
 * @author Egbert Broerse Copyright (C) 2020
 */
public class LncvProgPane extends jmri.jmrix.loconet.swing.LnPanel implements LocoNetListener {

    private LocoNetSystemConnectionMemo memo;
    protected JButton allProgButton = new JButton();
    protected JButton modProgButton = new JButton();
    protected JButton readButton = new JButton(Bundle.getMessage("ButtonRead"));
    protected JButton writeButton = new JButton(Bundle.getMessage("ButtonWrite"));
    protected JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
    protected JTextField articleField = new JTextField(4);
    protected JTextField addressField = new JTextField(4);
    protected JTextField cvField = new JTextField(4);
    protected JTextField valueField = new JTextField(4);
    protected JTable moduleTable = null;
    protected javax.swing.table.TableModel moduleTableModel = null;

    protected JPanel tablePanel = null;
    protected JLabel statusText1 = new JLabel();
    protected JLabel articleFieldLabel = new JLabel(Bundle.getMessage("LabelArticleNum"));
    protected JLabel addressFieldLabel = new JLabel(Bundle.getMessage("LabelModuleAddress"));
    protected JLabel cvFieldLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("HeadingCv")));
    protected JLabel valueFieldLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("HeadingValue")));
    protected JTextArea result = new JTextArea(6,50);
    protected String reply = "";
    protected int art;
    protected int adr = 1;
    protected int cv = 0;
    protected int val;
    boolean writeConfirmed = false;

    private HashMap<Integer, LncvModule> modules;
    private boolean allProgRunning = false;
    private boolean moduleProgRunning = false;

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
        modules = new HashMap<Integer, LncvModule>(0);
        // connect to the LnTrafficController
        if (memo.getLnTrafficController() == null) {
            log.error("No traffic controller is available");
        } else {
            // add listener
            memo.getLnTrafficController().addLocoNetListener(~0, this);
        }
    }

    /**
     * Initialize the config window
     */
    @Override
    public void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(initAddressPanel());

        // Set up the SV2 modules table
        tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        moduleTableModel = new LncvProgTableModel(this);
        moduleTable = new JTable(moduleTableModel);
        moduleTable.setRowSelectionAllowed(false);
        moduleTable.setPreferredScrollableViewportSize(new Dimension(300, 350));
        JScrollPane tableScrollPane = new JScrollPane(moduleTable);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        add(tablePanel);

        add(initNotesPanel());

        statusText1.setText("help?");
        statusText1.setVisible(true);
        add(statusText1);

        add(initButtonPanel());
    }

    /*
     * Initialize the address panel.
     */
    protected JPanel initAddressPanel(){
        // Set up module address and node type
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
        JPanel panel11 = new JPanel();
        panel11.setLayout(new FlowLayout());
        panel11.add(new JLabel(Bundle.getMessage("LncvTableTitle")));

        panel1.add(panel11);
        return panel1;
    }

    /*
     * Initialize the notes panel.
     */
    protected JPanel initNotesPanel() {
        // Set up the notes panel
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        JPanel panel31 = new JPanel();
        panel31.setLayout(new FlowLayout());

        JScrollPane resultScrollPane = new JScrollPane(result);
        panel31.add(resultScrollPane);

        panel3.add(panel31);
        Border panel3Border = BorderFactory.createEtchedBorder();
        Border panel3Titled = BorderFactory.createTitledBorder(panel3Border, "Monitor");
                //Bundle.getMessage("BoxLabelNotes"));
        panel3.setBorder(panel3Titled);
        return panel3;
    }

    /*
     * Initialize the Button panel.
     */
    protected JPanel initButtonPanel() {
        // Set up buttons
        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout());

        modProgButton.setText(moduleProgRunning ?
                Bundle.getMessage("ButtonStopModProg") : Bundle.getMessage("ButtonStartModProg"));
        modProgButton.setToolTipText(Bundle.getMessage("TipModuleProgButton"));
        modProgButton.addActionListener(e -> modProgButtonActionPerformed());
        panel4.add(modProgButton);

        panel4.add(articleFieldLabel);
        // entry field (decimal)
        articleField.setToolTipText(Bundle.getMessage("TipModuleArticleField"));
        panel4.add(articleField);

        panel4.add(addressFieldLabel);
        // entry field (decimal) for Module Address
        addressField.setText("1");
        panel4.add(addressField);

        panel4.add(cvFieldLabel);
        // entry field (decimal) for CV number to read/write
        //cvField.setToolTipText(Bundle.getMessage("TipModuleCvField"));
        cvField.setText("0");
        panel4.add(cvField);

        panel4.add(valueFieldLabel);
        // entry field (decimal) for CV value
        //valueField.setToolTipText(Bundle.getMessage("TipModuleValueField"));
        valueField.setText("1");
        panel4.add(valueField);

        allProgButton.setText(allProgRunning ?
                Bundle.getMessage("ButtonStopAllProg") : Bundle.getMessage("ButtonStartAllProg"));
        allProgButton.setToolTipText(Bundle.getMessage("TipAllProgButton"));
        allProgButton.addActionListener(e -> allProgButtonActionPerformed());
        panel4.add(allProgButton);

        panel4.add(readButton);
        readButton.setEnabled(false);
        readButton.addActionListener(e -> readButtonActionPerformed());

        panel4.add(writeButton);
        writeButton.setEnabled(false);
        writeButton.addActionListener(e -> writeButtonActionPerformed());

        doneButton.addActionListener(e -> doneButtonActionPerformed());
        panel4.add(doneButton);
        return panel4;
    }

    /**
     * GENERALPROG button.
     */
    public void allProgButtonActionPerformed() {
        if (moduleProgRunning) {
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
            memo.getLnTrafficController().sendLocoNetMessage(LncvMessageContents.createAllProgEndRequest());
            statusText1.setText(Bundle.getMessage("FeedBackStopAllProg"));
            allProgButton.setText(Bundle.getMessage("ButtonStartAllProg"));
            allProgRunning = false;
            // remove listener last to see message sent out
            //memo.getLnTrafficController().removeLocoNetListener(~0, this);
            return;
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
        // add listener
        //memo.getLnTrafficController().addLocoNetListener(~0, this);
        // send LncvProgSessionStart command on LocoNet
        LocoNetMessage m = LncvMessageContents.createAllProgStartRequest();
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
            return;
        }
        // provide user feedback
        articleField.setBackground(Color.WHITE); // reset
        readButton.setEnabled(!moduleProgRunning);
        writeButton.setEnabled(!moduleProgRunning);
        if (moduleProgRunning) { // stop prog
            try {
                art = inDomain(articleField.getText(), 9999);
                adr = inDomain(addressField.getText(), 65535); // used as module address
                memo.getLnTrafficController().sendLocoNetMessage(LncvMessageContents.createModProgEndRequest(art, adr));
                statusText1.setText(Bundle.getMessage("FeedBackModProgClosed"));
                modProgButton.setText(Bundle.getMessage("ButtonStartModProg"));
                moduleProgRunning = false;
                // remove listener last to see message sent out, no reply expected
                //memo.getLnTrafficController().removeLocoNetListener(~0, this);
            } catch (NumberFormatException e) {
                statusText1.setText(Bundle.getMessage("FeedBackEnterArticle"));
            }
            return;
        }
        // add listener
        //memo.getLnTrafficController().addLocoNetListener(~0, this);
        if ((articleField.getText() != null) && (addressField.getText() != null)) {
            try {
                art = inDomain(articleField.getText(), 9999);
                adr = inDomain(addressField.getText(), 65535); // go in d5-d6 as module address
                memo.getLnTrafficController().sendLocoNetMessage(LncvMessageContents.createModProgStartRequest(art, adr));
                statusText1.setText(Bundle.getMessage("FeedBackModProgOpen", adr));
                modProgButton.setText(Bundle.getMessage("ButtonStopModProg"));
                moduleProgRunning = true;
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
        if (moduleProgRunning) {
            sArt = articleField.getText();
            articleField.setBackground(Color.WHITE); // reset
        }
        if ((sArt != null) && (addressField.getText() != null) && (cvField.getText() != null)) {
            try {
                art = inDomain(sArt, 9999);
                adr = inDomain(addressField.getText(), 9999); // used as address for reply
                cv = inDomain(cvField.getText(), 65535); // decimal entry
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
        if (moduleProgRunning) {
            sArt = articleField.getText();
        }
        if ((sArt != null) && (cvField.getText() != null) && (valueField.getText() != null)) {
            articleField.setBackground(Color.WHITE);
            try {
                writeConfirmed = false;
                art = inDomain(sArt, 9999);
                cv = inDomain(cvField.getText(), 9999); // decimal entry
                val = inDomain(valueField.getText(), 65535); // decimal entry
                memo.getLnTrafficController().sendLocoNetMessage(LncvMessageContents.createCvWriteRequest(art, cv, val));
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

    /**
     * Handle Done button.
     */
    public void doneButtonActionPerformed() {
        dispose();
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

    @Override
    public synchronized void message(LocoNetMessage l) { // receive a LocoNet message and log it
        if (LncvMessageContents.isSupportedLncvMessage(l)) {
            // raw data, to display
            String raw = l.toString();
            // format the message text, expect it to provide consistent \n after each line
            String formatted = l.toMonitorString(memo.getSystemPrefix());
            // copy the formatted data
            reply += formatted + " " + raw + "\n";
        }
        // got a LocoNet message, see if it's a LNCV response
        if (l.getElement(1) == 0x6D && l.getElement(2) == 0x7f) { // LACK
            // elem 1 = OPC (matches 0xED), elem 2 =
            writeConfirmed = true;
            // feedback
            reply += "(LNCV) WRITE confirmed.\n";
            //jmri.jmrix.loconet.messageinterp.Bundle.getMessage("LN_MSG_LONG_ACK_OPC_IMM_ACCEPT");
        }
        if (LncvMessageContents.extractMessageType(l) == LncvMessageContents.LncvCommand.LNCV_READ_REPLY) {
            // it's a LNCV ReadReply message, decode contents
            // get LNCV message contents from a LocoNet packet:
            LncvMessageContents contents = new LncvMessageContents(l);
            int section1 = contents.getLncvArticleNum();
            int section2 = contents.getCvNum();
            int section3 = contents.getCvValue();

            // store replies
            LncvModule mod = new LncvModule(new int[]{section1, section2, section3});
            int tempMod = 0;
            if (section1 == art) {
                mod.setAddress(adr); // trust last used address, to be sure, check against Article (hardware class) number
                tempMod = adr;
            }
            String foundMod = "LNCV art:" + section1 + " address:" + tempMod + " cv:" + section2 + " value:" + section3 + "\n";
            reply += foundMod;
            modules.put(counter++, mod);
            log.debug("LNCV Added Module {}: {}", counter, foundMod);
            moduleTable.revalidate();
            // query to get module address/all CV's/switch to DecoderPro tabs?
            // for each module, ask address
            //        for (module : modules.entrySet()) {
            //            LocoNetMessage q = new createSv2DeviceDiscoveryReply();
            //            //int section4 = contents.getSv2Address();
            //        }
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
             statusText1.setText(Bundle.getMessage("FeedBackDiscoverFail"));
        } else {
            log.debug("LNCV process completed successfully.");
            statusText1.setText(Bundle.getMessage("FeedBackDiscoverSuccess",
                    (modules == null ? 0 : modules.size())));
            // reload the CV? list
            result.setText(reply);
        }
    }

    @Override
    public void dispose() {
        if (memo != null && memo.getLnTrafficController() != null) {
            // disconnect from the LnTrafficController, normally attached/detached after Discovery completed
            memo.getLnTrafficController().removeLocoNetListener(~0, this);
        }
        // and unwind swing
        super.dispose();
    }

    protected LncvModule getModule(int i) {
        if ((modules != null) && (i <= modules.size())) {
            return modules.get(i);
        } else {
            log.debug("getModule({}) failed", i);
            return null;
        }
    }

    private int counter = 0;

    /**
     * Store elements received on LNCV reply message.
     */
    static class LncvModule {
        private final int classNum;
        private int address = 0; // Module address in reply
        private final int cvNum;
        private final int cvValue;

        LncvModule(int[] response) {
            classNum = response[0];
            //address = response[1];
            cvNum = response[1];
            cvValue = response[2];
            log.debug("Added Module {}", classNum);
        }

        void setAddress(int addr) {
            address = addr;
        }
        int getAddress() {
            return address;
        }
        int getClassNum() {
            return classNum;
        }
        int getCvNum() {
            return cvNum;
        }
        int getCvValue() {
            return cvValue;
        }

    }

    private final static Logger log = LoggerFactory.getLogger(LncvProgPane.class);

}
