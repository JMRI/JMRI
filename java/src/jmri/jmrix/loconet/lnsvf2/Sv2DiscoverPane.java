package jmri.jmrix.loconet.lnsvf2;

import jmri.InstanceManager;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.swing.LnPanelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

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
    protected javax.swing.JButton doneButton = new javax.swing.JButton(Bundle.getMessage("ButtonDone"));

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
    private boolean discoveryRunning = false;

    /**
     * Constructor method
     */
    public Sv2DiscoverPane() {
        //addHelpMenu("package.jmri.jmrix.loconet.lnsvf2.Sv2DiscoverPane", true);
        memo = InstanceManager.getDefault(LocoNetSystemConnectionMemo.class);
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
    public void dispose() {
        if (memo != null && memo.getLnTrafficController() != null) {
            // disconnect from the LnTrafficController
            memo.getLnTrafficController().removeLocoNetListener(~0, this); // TODO detach after Discovery completed
        }
        // and unwind swing
        super.dispose();
    }

    @Override
    public synchronized void initComponents(LocoNetSystemConnectionMemo memo) {
        this.memo = memo;
        // connect to the LnTrafficController
        if (memo.getLnTrafficController() == null) {
            log.error("No traffic controller is available");
            return;
        }
        memo.getLnTrafficController().addLocoNetListener(~0, this); // TODO add when Discovery clicked
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
        statusText1.setText("");
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

//        panel4.add(doneButton);
//        doneButton.setText(Bundle.getMessage("ButtonDone"));
//        doneButton.setVisible(true);
//        //doneButton.setToolTipText(Bundle.getMessage("TipDoneButton"));
//        panel4.add(doneButton);
//        doneButton.addActionListener(new java.awt.event.ActionListener() {
//            @Override
//            public void actionPerformed(java.awt.event.ActionEvent e) {
//                doneButtonActionPerformed();
//            }
//        });
        return panel4;
    }

    /**
     * Handle Discover button
     */
    public void discoverButtonActionPerformed() {

        if (discoveryRunning) {
           log.debug("Discovery process already running");
           discoverButton.setEnabled(false);
           //statusText1.setText(Bundle.getMessage("FeedBackDiscover"));
           return;
        }
        discoveryRunning = true;
        discoverButton.setEnabled(false);
        // provide user feedback
        //statusText1.setText(Bundle.getMessage("FeedBackDiscover"));
    }

    /**
     * Handle Done button
     */
    public void doneButtonActionPerformed() {
        setVisible(false);
        if (memo != null && memo.getLnTrafficController() != null) {
            // disconnect from the LnTrafficController
            memo.getLnTrafficController().removeLocoNetListener(~0, this); // TODO detach after Discovery completed
        }
        dispose();
    }

    @Override
    public synchronized void message(LocoNetMessage l) { // receive a LocoNet message and log it
        // send the raw data, to display if requested
        String raw = l.toString();
        // format the message text, expect it to provide consistent \n after each line
        String formatted = l.toMonitorString(memo.getSystemPrefix());

        // display the formatted data in the monitor pane
        reply += formatted + "\n" + raw;

        // include LocoNet monitoring in session.log if TRACE enabled
        if (log.isTraceEnabled()) log.trace(formatted.substring(0, formatted.length() - 1));  // remove trailing newline
    }


    /*
     * Discovery finished callback.
     */
    public void discoveryFinished(String error){
       if(error != null){
         log.error("Node discovery processed finished with error: {}", error);
         statusText1.setText(Bundle.getMessage("FeedBackDiscoverFail"));
       } else {
         log.debug("Node discovery process completed successfully.");
         statusText1.setText(Bundle.getMessage("FeedBackDiscoverSuccess"));
         // reload the node list.
         result.setText(reply);
       }
       discoverButton.setEnabled(true);
    }

    class Sv2Module {
        private final String manufacturer;
        private final String type;
        private final String serialNum;
        private final String address;

        Sv2Module(String[] response) {
            manufacturer = response[0];
            type = response[1];
            serialNum = response[2];
            address = response[3];
        }

    }

    private final static Logger log = LoggerFactory.getLogger(Sv2DiscoverFrame.class);

}
