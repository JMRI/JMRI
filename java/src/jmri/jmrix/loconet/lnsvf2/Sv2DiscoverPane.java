package jmri.jmrix.loconet.lnsvf2;

import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.swing.LnPanelInterface;
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
public class Sv2DiscoverPane extends jmri.jmrix.loconet.swing.LnPanel implements LocoNetListener, LnPanelInterface {

    private LocoNetSystemConnectionMemo memo;
    protected javax.swing.JButton discoverButton = new javax.swing.JButton(Bundle.getMessage("ButtonDiscover"));
    //protected javax.swing.JButton doneButton = new javax.swing.JButton(Bundle.getMessage("ButtonDone"));

    protected JTable assignmentTable = null;
    protected javax.swing.table.TableModel assignmentListModel = null;

    protected JPanel assignmentPanel = null;
    protected javax.swing.JLabel statusText1 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusText2 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusText3 = new javax.swing.JLabel();
    protected JTextField result = new JTextField();
    protected String reply;

    protected javax.swing.JPanel panel2 = new JPanel();
    protected javax.swing.JPanel panel2a = new JPanel();
    private HashMap<Integer, Sv2Module> modules;
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
        String uName = "";
        if (memo != null) {
            uName = memo.getUserName();
            if (!"LocoNet".equals(uName)) { // NOI18N
                uName = uName + ": ";
            } else {
                uName = "";
            }
        }
        return uName + Bundle.getMessage("MenuItemDiscoverSv2");
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

        // Set up the pin assignment table
        assignmentPanel = new JPanel();
        assignmentPanel.setLayout(new BoxLayout(assignmentPanel, BoxLayout.Y_AXIS));
        assignmentListModel = new Sv2ModulesTableModel();
        assignmentTable = new JTable(assignmentListModel);
        assignmentTable.setRowSelectionAllowed(false);
        assignmentTable.setPreferredScrollableViewportSize(new java.awt.Dimension(300, 350));
        JScrollPane assignmentScrollPane = new JScrollPane(assignmentTable);
        assignmentPanel.add(assignmentScrollPane, BorderLayout.CENTER);

        add(assignmentPanel);

        add(initNotesPanel());
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
        panel11.add(new JLabel("Modules Found"));//Bundle.getMessage("LabelNodeSelection") + " "));

        result.setSize(300, 400);
        panel11.add(result);
        panel1.add(panel11);
        return panel1;
    }

    /*
     * Initialize the notes panel.
     */
    protected JPanel initNotesPanel(){
        // Set up the notes panel
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        JPanel panel31 = new JPanel();
        panel31.setLayout(new FlowLayout());
        statusText1.setText("help?");
        statusText1.setVisible(true);
        panel31.add(statusText1);
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
    protected JPanel initButtonPanel(){

        // Set up buttons
        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout());
        discoverButton.setText(Bundle.getMessage("ButtonDiscover"));
        discoverButton.setVisible(true);
        //discoverButton.setToolTipText(Bundle.getMessage("TipAddButton"));
        discoverButton.addActionListener(e -> discoverButtonActionPerformed());
        discoverButton.setEnabled(!discoveryRunning);
        panel4.add(discoverButton);

        return panel4;
    }

    /**
     * Handle Discover button
     */
    public void discoverButtonActionPerformed() {
        if (discoveryRunning) {
           log.debug("Discovery process already running");
           discoverButton.setEnabled(false);
           statusText1.setText(Bundle.getMessage("FeedBackDiscover"));
           return;
        }
        discoveryRunning = true;
        discoverButton.setEnabled(false);
        // add listener
        memo.getLnTrafficController().addLocoNetListener(~0, this);
        // send DiscoveryMessage on LocoNet
        memo.getLnTrafficController().sendLocoNetMessage(LnSv2MessageContents.createSvDiscoverQueryMessage());
        // provide user feedback
        statusText1.setText(Bundle.getMessage("FeedBackDiscover"));
    }

    /**
     * Handle Done button
     */
    public void doneButtonActionPerformed() {
        setVisible(false);
        if (memo != null && memo.getLnTrafficController() != null) {
            // disconnect from the LnTrafficController if still there
            memo.getLnTrafficController().removeLocoNetListener(~0, this); // normally detached after Discovery message arrived
        }
        dispose();
    }

    @Override
    public synchronized void message(LocoNetMessage l) { // receive a LocoNet message and log it
        // send the raw data, to display if requested
        String raw = l.toString();
        // format the message text, expect it to provide consistent \n after each line
        String formatted = l.toMonitorString(memo.getSystemPrefix());

        // copy the formatted data
        reply += formatted + "\n" + raw;
        // got a LocoNet message, see if Discovery respo
        // nse
        if (LnSv2MessageContents.extractMessageType(l) == LnSv2MessageContents.Sv2Command.SV2_DISCOVER_DEVICE_REPORT) {
            // it's a Discovery message, decode contents
            // get SV2 message from a LocoNet packet:
            LnSv2MessageContents contents = new LnSv2MessageContents(l);
            int section1 = contents.getSv2ManufacturerID();
            int section2 = contents.getSv2DeveloperID();
            int section3 = contents.getSv2ProductID();
            //int section4 = contents.getSv2Address();


            reply = "LNSV2 manuf:" + section1 + " devel: " + section2 + " product:" + section3 + " address: ?";//+ section4;

            modules.put(counter++, new Sv2Module(new int[]{section1, section2, section3, 0})); // store replies
        }
        // TODO query to get module address
        // for each module, ask address
//        for (module : modules.entrySet()) {
//            LocoNetMessage q = new createSv2DeviceDiscoveryReply();
//
//        }

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

        memo.getLnTrafficController().removeLocoNetListener(~0, this); // TODO detach after Discovery completed
        discoveryRunning = false;
        discoverButton.setEnabled(true);
    }

    @Override
    public void dispose() {
        if (memo != null && memo.getLnTrafficController() != null) {
            // disconnect from the LnTrafficController
            memo.getLnTrafficController().removeLocoNetListener(~0, this); // TODO detach after Discovery completed
        }
        // and unwind swing
        super.dispose();
    }

    int counter = 0;

    class Sv2Module {
        private final int manufacturer;
        private final int type;
        private final int serialNum;
        private final int address;

        Sv2Module(int[] response) {
            manufacturer = response[0];
            type = response[1];
            serialNum = response[2];
            address = response[3];
        }

    }

    private final static Logger log = LoggerFactory.getLogger(Sv2DiscoverPane.class);

}
