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
    protected JButton progSessionButton = new JButton();
    protected JButton moduleProgButton = new JButton();
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
    protected JLabel statusText2 = new JLabel();
    protected JLabel statusText3 = new JLabel();
    protected JLabel articleFieldLabel = new JLabel(Bundle.getMessage("LabelArticleNum"));
    protected JLabel addressFieldLabel = new JLabel(Bundle.getMessage("LabelModuleAddress"));
    protected JLabel cvFieldLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("HeadingCv")));
    protected JLabel valueFieldLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("HeadingValue")));
    protected JTextArea result = new JTextArea(6,50);
    protected String reply;
    protected int art;
    protected int adr = 1;
    protected int cv = 0;
    protected int val;
    boolean writeConfirmed = false;

    protected JPanel panel2 = new JPanel();
    protected JPanel panel2a = new JPanel();
    private HashMap<Integer, LncvModule> modules = null;
    private boolean progSessionRunning = false;
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
        // connect to the LnTrafficController
        if (memo.getLnTrafficController() == null) {
            log.error("No traffic controller is available");
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
        progSessionButton.setText(progSessionRunning ? Bundle.getMessage("ButtonLeaveProgSession") : Bundle.getMessage("ButtonStartProgSession"));
        progSessionButton.setToolTipText(Bundle.getMessage("TipProgSessionButton"));
        progSessionButton.addActionListener(e -> sessionButtonActionPerformed());
        panel4.add(progSessionButton);

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

        moduleProgButton.setEnabled(progSessionRunning);
        moduleProgButton.setText(moduleProgRunning ? Bundle.getMessage("ButtonModuleProgStop") : Bundle.getMessage("ButtonModuleProgStart"));
        moduleProgButton.setToolTipText(Bundle.getMessage("TipModuleProgButton"));
        moduleProgButton.addActionListener(e -> modProgButtonActionPerformed());
        panel4.add(moduleProgButton);

        panel4.add(readButton);
        readButton.setEnabled(progSessionRunning);
        readButton.addActionListener(e -> readButtonActionPerformed());

        panel4.add(writeButton);
        writeButton.setEnabled(progSessionRunning);
        writeButton.addActionListener(e -> writeButtonActionPerformed());

        doneButton.addActionListener(e -> doneButtonActionPerformed());
        panel4.add(doneButton);
        return panel4;
    }

    // READCV button
    /**
     * Handle Start/End Module Prog button.
     */
    public void readButtonActionPerformed() {
        if ((articleField.getText() != null) && (addressField.getText() != null)) {
            try {
                art = Integer.parseInt(articleField.getText());
                adr = Integer.parseInt(addressField.getText()); // used as address for reply
                cv = Integer.parseInt(cvField.getText()); // decimal to Hex
                memo.getLnTrafficController().sendLocoNetMessage(LncvMessageContents.createCvReadRequest(art, adr, cv));
            } catch (NumberFormatException e) {
                log.error("invalid entry must be number");
            }
        }
        // stop and inform user
        //statusText1.setText(Bundle.getMessage("FeedBackEnterArticle"));
    }

    // WriteCV button
    /**
     * Handle Start/End Module Prog button.
     */
    public void writeButtonActionPerformed() {
        // TODO assemble LNCV write message
        if (articleField.getText() != null && cvField.getText() != null && valueField.getText() != null) {
            try {
                art = Integer.parseInt(articleField.getText());
                cv = Integer.parseInt(cvField.getText()); // decimal to Hex
                val = Integer.parseInt(valueField.getText());
                memo.getLnTrafficController().sendLocoNetMessage(LncvMessageContents.createCvWriteRequest(art, cv, val));
            } catch (NumberFormatException e) {
                log.error("invalid entry must be number");
            }
        }
        // stop and inform user
        //statusText1.setText(Bundle.getMessage("FeedBackWritten"));

        // wait for LACK reply
        writeConfirmed = false;
    }

    /**
     * Handle Start/End Session button.
     */
    public void sessionButtonActionPerformed() {
        // provide user feedback
        statusText1.setText(Bundle.getMessage("FeedBackSessionOpen"));
        moduleProgButton.setEnabled(!progSessionRunning);
        if (progSessionRunning) {
            if (!moduleProgRunning) { // stop session (check that moduleProgRun is closed first)
                progSessionRunning = false;
                log.debug("Session already running, closing");
                progSessionButton.setText(Bundle.getMessage("ButtonStartProgSession"));
                // remove listener
                memo.getLnTrafficController().removeLocoNetListener(~0, this);
                // send LncvProgSessionLeave command on LocoNet
                memo.getLnTrafficController().sendLocoNetMessage(LncvMessageContents.createProgSessionEndCommand());
            } else {
                statusText1.setText(Bundle.getMessage("FeedbackStopModProg", Bundle.getMessage("ButtonModuleProgStop")));
            }
            return;
        }
        progSessionRunning = true;
        progSessionButton.setText(Bundle.getMessage("ButtonLeaveProgSession"));
        // add listener
        memo.getLnTrafficController().addLocoNetListener(~0, this);
        // send LncvProgSessionStart command on LocoNet
        LocoNetMessage m = LncvMessageContents.createProgSessionStartCommand();
        log.debug("message sent, cmd = {}", m.getElement(5));
        memo.getLnTrafficController().sendLocoNetMessage(m);
        // stop and inform user
        statusText1.setText(Bundle.getMessage("FeedBackEnterType"));
    }

    // MODULEPROG button
    /**
     * Handle Start/End Module Prog button.
     */
    public void modProgButtonActionPerformed() {
        // provide user feedback
        statusText1.setText(Bundle.getMessage("FeedBackModProgOpen", adr));
        readButton.setEnabled(!moduleProgRunning);
        writeButton.setEnabled(!moduleProgRunning);
        if (moduleProgRunning) { // stop prog
            try {
                art = Integer.parseInt(articleField.getText());
                adr = Integer.parseInt(addressField.getText());
                memo.getLnTrafficController().sendLocoNetMessage(LncvMessageContents.createModProgEndRequest(art, adr));
                moduleProgRunning = false;
                moduleProgButton.setText(Bundle.getMessage("ButtonModuleProgStart"));
            } catch (NumberFormatException e) {
                log.error("invalid entry must be number");
            }
            return;
        }
        moduleProgRunning = true;
        moduleProgButton.setText(Bundle.getMessage("ButtonModuleProgStop"));
        if ((articleField.getText() != null) && (addressField.getText() != null)) {
            try {
                art = Integer.parseInt(articleField.getText());
                adr = Integer.parseInt(addressField.getText());
                memo.getLnTrafficController().sendLocoNetMessage(LncvMessageContents.createModProgStartRequest(art, adr));
            } catch (NumberFormatException e) {
                log.error("invalid entry must be number");
            }
        }
        // stop and inform user
        statusText1.setText(Bundle.getMessage("FeedBackEnterArticle"));
        moduleProgRunning = false;
        //progSessionButton.setEnabled(false);
    }

    /**
     * Handle Done button.
     */
    public void doneButtonActionPerformed() {
        dispose();
    }

    @Override
    public synchronized void message(LocoNetMessage l) { // receive a LocoNet message and log it
        // send the raw data, to display if requested
        String raw = l.toString();
        // format the message text, expect it to provide consistent \n after each line
        String formatted = l.toMonitorString(memo.getSystemPrefix());
        // copy the formatted data
        reply += formatted + " " + raw + "\n";
        // got a LocoNet message, see if it's a Discovery response
        if (LncvMessageContents.extractMessageType(l) == LncvMessageContents.LncvCommand.LNCV_READ_REPLY) {
            writeConfirmed = true;
            // feedback
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
                mod.setAddress(adr); // trust last used address, to be sure, check against Article number
                tempMod = adr;
            }
            modules.put(counter++, mod);
            reply += "LNCV art:" + section1 + " address:" + tempMod + " cv:" + section2 + " value:" + section3 + "\n";

            // TODO query to get module address
            // for each module, ask address
            //        for (module : modules.entrySet()) {
            //            LocoNetMessage q = new createSv2DeviceDiscoveryReply();
            //            //int section4 = contents.getSv2Address();
            //        }
        }

        if (reply != null) {
            sessionFinished(null);
        }
    }

    /*
     * Session callback.
     */
    public void sessionFinished(String error){
        if (error != null) {
             log.error("Node discovery processed finished with error: {}", error);
             statusText1.setText(Bundle.getMessage("FeedBackDiscoverFail"));
        } else {
            log.debug("Node discovery process completed successfully.");
            statusText1.setText(Bundle.getMessage("FeedBackDiscoverSuccess", (modules == null ? 0 : modules.size())));
            // reload the node list.
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
        if (i <= modules.size()) {
            return modules.get(i);
        } else {
            return null;
        }
    }

    private int counter = 0;

    /**
     * Store elements received on LNCV reply message.
     */
    static class LncvModule {
        private final int article;
        private int address = 0; // Module address in reply
        private final int cvNum;
        private final int cvValue;

        LncvModule(int[] response) {
            article = response[0];
            //address = response[1];
            cvNum = response[1];
            cvValue = response[2];
            log.debug("Added Module {}", article);
        }

        void setAddress(int addr) {
            address = addr;
        }
        int getAddress() {
            return address;
        }
        int getArticle() {
            return article;
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
