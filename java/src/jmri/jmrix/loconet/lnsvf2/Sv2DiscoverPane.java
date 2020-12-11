package jmri.jmrix.loconet.lnsvf2;

import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.HashMap;

/**
 * Frame for discovery and display of LocoNet SV2 boards.
 * Derived from xbee node config.
 *
 * @author Bob Jacobsen Copyright (C) 2004
 * @author Dave Duchamp Copyright (C) 2004
 * @author Paul Bender Copyright (C) 2013
 * @author Egbert Broerse Copyright (C) 2020
 */
public class Sv2DiscoverPane extends jmri.jmrix.loconet.swing.LnPanel implements LocoNetListener {

    private LocoNetSystemConnectionMemo memo;
    protected JButton discoverButton = new JButton(Bundle.getMessage("ButtonDiscoverAll"));
    protected JButton identifyByTypeButton = new JButton(Bundle.getMessage("ButtonDiscoverByType"));
    protected JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
    protected JTextField typeField = new JTextField(4);
    protected JTable moduleTable = null;
    protected javax.swing.table.TableModel moduleTableModel = null;

    protected JPanel tablePanel = null;
    protected JLabel statusText1 = new JLabel();
    protected JLabel statusText2 = new JLabel();
    protected JLabel statusText3 = new JLabel();
    protected JLabel TypeFieldLabel = new JLabel(Bundle.getMessage("LabelType"));
    protected JTextArea result = new JTextArea(6,30);
    protected String reply;

    protected JPanel panel2 = new JPanel();
    protected JPanel panel2a = new JPanel();
    private HashMap<Integer, Sv2Module> modules = null;
    private boolean discoveryRunning = false;

    /**
     * Constructor method
     */
    public Sv2DiscoverPane() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.lnsvf2.Sv2DiscoverPane"; // NOI18N
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("MenuItemDiscoverSv2");
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
        moduleTableModel = new Sv2ModulesTableModel(this);
        moduleTable = new JTable(moduleTableModel);
        moduleTable.setRowSelectionAllowed(false);
        moduleTable.setPreferredScrollableViewportSize(new java.awt.Dimension(300, 350));
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
        panel11.add(new JLabel(Bundle.getMessage("ModulesFoundLabel")));

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
        Border panel3Titled = BorderFactory.createTitledBorder(panel3Border, "xxx");
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
        discoverButton.setToolTipText(Bundle.getMessage("TipDiscoverButton"));
        discoverButton.addActionListener(e -> discoverButtonActionPerformed(false));
        discoverButton.setEnabled(!discoveryRunning);
        panel4.add(discoverButton);

        panel4.add(TypeFieldLabel);
        // entry field (decimal)
        panel4.add(typeField);

        identifyByTypeButton.setToolTipText(Bundle.getMessage("TipIdentifyByTypeButton"));
        identifyByTypeButton.addActionListener(e -> discoverButtonActionPerformed(true));
        identifyByTypeButton.setEnabled(!discoveryRunning);
        panel4.add(identifyByTypeButton);

        doneButton.addActionListener(e -> doneButtonActionPerformed());
        panel4.add(doneButton);
        return panel4;
    }

    /**
     * Handle Discover button.
     */
    public void discoverButtonActionPerformed(boolean typed) {
        // provide user feedback
        statusText1.setText(Bundle.getMessage("FeedBackDiscover"));
        if (discoveryRunning) {
           log.debug("Discovery process already running");
           discoverButton.setEnabled(false);
           return;
        }
        discoveryRunning = true;
        discoverButton.setEnabled(false);
        // add listener
        memo.getLnTrafficController().addLocoNetListener(~0, this);
        // send DiscoveryMessage on LocoNet
        if (!typed) {
            memo.getLnTrafficController().sendLocoNetMessage(LnSv2MessageContents.createSvDiscoverQueryMessage());
        } else if (typeField.getText() != null) {
            try {
                int type = Integer.parseInt(typeField.getText());
                memo.getLnTrafficController().sendLocoNetMessage(LnSv2MessageContents.createSv2Message(
                        1, LnSv2MessageContents.Sv2Command.SV2_IDENTIFY_DEVICES_BY_TYPE.cmd, type,
                        0, 0, 0, 0, 0));
            } catch (NumberFormatException e) {
                log.error("invalid entry must be number");
            }
        }
        // stop and inform user
        statusText1.setText(Bundle.getMessage("FeedBackEnterType"));
        discoveryRunning = false;
        discoverButton.setEnabled(true);
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
        reply += formatted + raw + "\n";
        // got a LocoNet message, see if it's a Discovery response
        if (LnSv2MessageContents.extractMessageType(l) == LnSv2MessageContents.Sv2Command.SV2_DISCOVER_DEVICE_REPORT) {
            // it's a Discovery message, decode contents
            // get SV2 message from a LocoNet packet:
            LnSv2MessageContents contents = new LnSv2MessageContents(l);
            int section1 = contents.getSv2ManufacturerID();
            int section2 = contents.getSv2DeveloperID();
            int section3 = contents.getSv2ProductID();
            int section4 = contents.getSv2SerialNum();

            reply += "LNSV2 manuf:" + section1 + " devel: " + section2 + " product:" + section3 + " serial:" + section4 + " address: ?";
            // store replies
            modules.put(counter++, new Sv2Module(new int[]{section1, section2, section3, section4, 0}));

            // TODO query to get module address
            // for each module, ask address
            //        for (module : modules.entrySet()) {
            //            LocoNetMessage q = new createSv2DeviceDiscoveryReply();
            //            //int section4 = contents.getSv2Address();
            //        }
        }

        discoveryFinished(null);
    }

    /*
     * Discovery finished callback.
     */
    public void discoveryFinished(String error){
        if (error != null) {
             log.error("Node discovery processed finished with error: {}", error);
             statusText1.setText(Bundle.getMessage("FeedBackDiscoverFail"));
        } else {
            log.debug("Node discovery process completed successfully.");
            statusText1.setText(Bundle.getMessage("FeedBackDiscoverSuccess", (modules == null ? 0 : modules.size())));
            // reload the node list.
            result.setText(reply);
        }

        memo.getLnTrafficController().removeLocoNetListener(~0, this);
        discoveryRunning = false;
        discoverButton.setEnabled(true);
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

    protected Sv2Module getModule(int i) {
        if (i <= modules.size()) {
            return modules.get(i);
        } else {
            return null;
        }
    }

    private int counter = 0;

    /**
     * Store elements received on LNSV2 QueryAll reply message.
     */
    static class Sv2Module {
        private final int manufacturer;
        private final int developer;
        private final int type;
        private final int serialNum;
        private int address;

        Sv2Module(int[] response) {
            manufacturer = response[0];
            developer = response[1];
            type = response[2];
            serialNum = response[3];
            address = response[4];
        }

        void setAddress(int addr) {
            address = addr;
        }
        int getAddress() {
            return address;
        }
        int getManufacturer() {
            return manufacturer;
        }
        int getDeveloper() {
            return developer;
        }
        int getType() {
            return type;
        }
        int getSerialNum() {
            return serialNum;
        }

    }

    private final static Logger log = LoggerFactory.getLogger(Sv2DiscoverPane.class);

}
