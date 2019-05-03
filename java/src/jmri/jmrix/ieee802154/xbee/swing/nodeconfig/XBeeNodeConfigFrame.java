package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.listeners.IDiscoveryListener;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import jmri.jmrix.ieee802154.xbee.XBeeNode;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user configuration of XBee nodes Derived from node configuration
 * for c/mri nodes.
 *
 * @author Bob Jacobsen Copyright (C) 2004
 * @author Dave Duchamp Copyright (C) 2004
 * @author Paul Bender Copyright (C) 2013
 */
public class XBeeNodeConfigFrame extends jmri.jmrix.ieee802154.swing.nodeconfig.NodeConfigFrame implements IDiscoveryListener {

    private XBeeTrafficController xtc = null;
    protected javax.swing.JButton discoverButton = new javax.swing.JButton(Bundle.getMessage("ButtonDiscover"));
    private JComboBox<XBeeNode> nodeField = new javax.swing.JComboBox<XBeeNode>();
    protected JTable assignmentTable = null;
    protected javax.swing.table.TableModel assignmentListModel = null;

    protected JPanel assignmentPanel = null;

    /**
     * Constructor method
     * @param tc traffic controller for node
     */
    public XBeeNodeConfigFrame(XBeeTrafficController tc) {
        super(tc);
        xtc = tc;
    }

    /**
     * Initialize the config window
     */
    @Override
    public void initComponents() {
        setTitle(Bundle.getMessage("WindowTitle"));
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        contentPane.add(initAddressPanel());

        // Set up the pin assignment table
        assignmentPanel = new JPanel();
        assignmentPanel.setLayout(new BoxLayout(assignmentPanel, BoxLayout.Y_AXIS));
        assignmentListModel = new AssignmentTableModel();
        assignmentTable = new JTable(assignmentListModel);
        assignmentTable.setRowSelectionAllowed(false);
        assignmentTable.setPreferredScrollableViewportSize(new java.awt.Dimension(300, 350));
        JScrollPane assignmentScrollPane = new JScrollPane(assignmentTable);
        assignmentPanel.add(assignmentScrollPane, BorderLayout.CENTER);

        contentPane.add(assignmentPanel);

        contentPane.add(initNotesPanel());
        contentPane.add(initButtonPanel());

        // pack for display
        pack();

        // after the components are configured, set ourselves up as a 
        // discovery listener.
        xtc.getXBee().getNetwork().addDiscoveryListener(this);

    }

    /*
     * Initilaize the address panel.
     */
    @Override
    protected JPanel initAddressPanel(){
        // Set up node address and node type
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
        JPanel panel11 = new JPanel();
        panel11.setLayout(new FlowLayout());
        panel11.add(new JLabel(Bundle.getMessage("LabelNodeSelection") + " "));
        panel11.add(nodeField);
        nodeField.setToolTipText(Bundle.getMessage("TipNodeSelection"));
        nodeField.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent e) {
                   nodeSelected();
            }
        });

        initAddressBoxes();

        panel1.add(panel11);
        return panel1;
    }

    /*
     * Initilaize the Button panel.
     */
    @Override
    protected JPanel initButtonPanel(){

        // Set up buttons
        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout());
        addButton.setText(Bundle.getMessage("ButtonAdd"));
        addButton.setVisible(true);
        addButton.setToolTipText(Bundle.getMessage("TipAddButton"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                addButtonActionPerformed();
            }
        });
        panel4.add(addButton);
        discoverButton.setText(Bundle.getMessage("ButtonDiscover"));
        discoverButton.setVisible(true);
        discoverButton.setToolTipText(Bundle.getMessage("TipAddButton"));
        discoverButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                discoverButtonActionPerformed();
            }
        });
        discoverButton.setEnabled(!(xtc.getXBee().getNetwork().isDiscoveryRunning()));
        panel4.add(discoverButton);
        editButton.setText(Bundle.getMessage("ButtonEdit"));
        editButton.setVisible(true);
        editButton.setToolTipText(Bundle.getMessage("TipEditButton"));
        panel4.add(editButton);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                editButtonActionPerformed();
            }
        });
        panel4.add(deleteButton);
        deleteButton.setText(Bundle.getMessage("ButtonDelete"));
        deleteButton.setVisible(true);
        deleteButton.setToolTipText(Bundle.getMessage("TipDeleteButton"));
        panel4.add(deleteButton);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                deleteButtonActionPerformed();
            }
        });
        panel4.add(doneButton);
        doneButton.setText(Bundle.getMessage("ButtonDone"));
        doneButton.setVisible(true);
        doneButton.setToolTipText(Bundle.getMessage("TipDoneButton"));
        panel4.add(doneButton);
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doneButtonActionPerformed();
            }
        });
        panel4.add(updateButton);
        updateButton.setText(Bundle.getMessage("ButtonUpdate"));
        updateButton.setVisible(true);
        updateButton.setToolTipText(Bundle.getMessage("TipUpdateButton"));
        panel4.add(updateButton);
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                updateButtonActionPerformed();
            }
        });
        updateButton.setVisible(false);
        panel4.add(cancelButton);
        cancelButton.setText(Bundle.getMessage("ButtonCancel"));
        cancelButton.setVisible(true);
        cancelButton.setToolTipText(Bundle.getMessage("TipCancelButton"));
        panel4.add(cancelButton);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cancelButtonActionPerformed();
            }
        });
        cancelButton.setVisible(false);
        return panel4;
    }

    /**
     * Method to handle add button
     */
    @Override
    public void addButtonActionPerformed() {
        // create a new Add Frame and display it.
        jmri.util.JmriJFrame addFrame = new XBeeAddNodeFrame(xtc,this);
        try {
           addFrame.initComponents();
        } catch(Exception ex) {
           log.error("Exception initializing Frame: {}",ex.toString());
           return;
        }
        addFrame.setVisible(true);
    }

    /**
     * Method to handle discover button
     */
    public void discoverButtonActionPerformed() {

        if(xtc.getXBee().getNetwork().isDiscoveryRunning()){
           log.debug("Discovery process already running");
           discoverButton.setEnabled(false);
           statusText1.setText(Bundle.getMessage("FeedBackDiscover"));
           return;
        }

        jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo memo = xtc.getAdapterMemo();
        if( memo instanceof XBeeConnectionMemo) {

           XBeeConnectionMemo m = (XBeeConnectionMemo) memo;

           // call the node discovery code in the node manager.
           m.getXBeeNodeManager().startNodeDiscovery();

           discoverButton.setEnabled(false);
        }
        // provide user feedback
        statusText1.setText(Bundle.getMessage("FeedBackDiscover"));
        errorInStatus1 = true;
        resetNotes2();
    }

    /**
     * Method to handle edit button
     */
    @Override
    public void editButtonActionPerformed() {
       // get the XBeeNode corresponding to this node address
       curNode = (XBeeNode) nodeField.getSelectedItem();
       if (curNode == null) {
          statusText1.setText(Bundle.getMessage("Error4"));
          statusText1.setVisible(true);
          errorInStatus1 = true;
          resetNotes2();
          return;
       }

        // create a new Edit Frame and display it.
        jmri.util.JmriJFrame editFrame = new XBeeEditNodeFrame(xtc,(XBeeNode)curNode,this);
        try {
           editFrame.initComponents();
        } catch(Exception ex) {
           log.error("Exception initializing Frame: {}",ex.toString());
           return;
        }
        editFrame.setVisible(true);

    }


/**
     * Method to handle delete button
     */
    @Override
    public void deleteButtonActionPerformed() {
        // get the XBeeNode corresponding to this node address
        curNode = (XBeeNode) nodeField.getSelectedItem();
        if (curNode == null) {
            statusText1.setText(Bundle.getMessage("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // confirm deletion with the user
        if (javax.swing.JOptionPane.OK_OPTION == javax.swing.JOptionPane.showConfirmDialog(
                this, Bundle.getMessage("ConfirmDelete1") + "\n"
                + Bundle.getMessage("ConfirmDelete2"), Bundle.getMessage("ConfirmDeleteTitle"),
                javax.swing.JOptionPane.OK_CANCEL_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE)) {
            // delete this node
            xtc.deleteNode((XBeeNode) curNode);
            // provide user feedback
            resetNotes();
            statusText1.setText(Bundle.getMessage("FeedBackDelete") + " " + curNode.toString());
            errorInStatus1 = true;
            changedNode = true;
        } else {
            // reset as needed
            resetNotes();
        }
        initAddressBoxes();
    }

    /**
     * Method to handle done button
     */
    @Override
    public void doneButtonActionPerformed() {
        if (editMode) {
            // Reset 
            editMode = false;
            curNode = null;
            // Switch buttons
            addButton.setVisible(true);
            editButton.setVisible(true);
            deleteButton.setVisible(true);
            doneButton.setVisible(true);
            updateButton.setVisible(false);
            cancelButton.setVisible(false);
        }
        if (changedNode) {
            // Remind user to Save new configuration
            javax.swing.JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("Reminder1") + "\n" + Bundle.getMessage("Reminder2"),
                    Bundle.getMessage("ReminderTitle"),
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
        setVisible(false);
        dispose();
    }

    /**
     * Method to handle update button
     */
    @Override
    public void updateButtonActionPerformed() {
        // get node information from window

        // check consistency of node information
        if (!checkConsistency()) {
            return;
        }
        // update node paramaters
        setNodeParameters();
        changedNode = true;
        // Reset Edit Mode
        editMode = false;
        curNode = null;
        // Switch buttons
        addButton.setVisible(true);
        editButton.setVisible(true);
        deleteButton.setVisible(true);
        doneButton.setVisible(true);
        updateButton.setVisible(false);
        cancelButton.setVisible(false);
        // refresh notes panel
        statusText2.setText(stdStatus2);
        statusText3.setText(stdStatus3);
        // provide user feedback
        try {
           statusText1.setText(Bundle.getMessage("FeedBackUpdate") + " " + readNodeAddress());
           } catch(IllegalArgumentException iae){
               // we really need to set an error status here.
               // illegal argument exception is generated by 
               // readNodeAddress when neither a 16 or 64 bit 
               // addresses is selected.
           }
        errorInStatus1 = true;
    }

    /**
     * Method to handle cancel button
     */
    @Override
    public void cancelButtonActionPerformed() {
        // Reset 
        editMode = false;
        curNode = null;
        // Switch buttons
        addButton.setVisible(true);
        editButton.setVisible(true);
        deleteButton.setVisible(true);
        doneButton.setVisible(true);
        updateButton.setVisible(false);
        cancelButton.setVisible(false);
        // refresh notes panel
        statusText1.setText(stdStatus1);
        statusText2.setText(stdStatus2);
        statusText3.setText(stdStatus3);
    }

    /**
     * Method to close the window when the close box is clicked
     */
    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        doneButtonActionPerformed();
        super.windowClosing(e);
    }

    /**
     * Method to set node parameters The node must exist, and be in 'curNode'
     */
    @Override
    protected void setNodeParameters() {
        super.setNodeParameters();
    }

    /**
     * Method to reset the notes error after error display
     */
    private void resetNotes() {
        if (errorInStatus1) {
            if (editMode) {
                statusText1.setText(editStatus1);
            } else {
                statusText1.setText(stdStatus1);
            }
            errorInStatus1 = false;
        }
        resetNotes2();
    }

    /**
     * Reset the second line of Notes area
     */
    private void resetNotes2() {
        if (errorInStatus2) {
            if (editMode) {
                statusText1.setText(editStatus2);
            } else {
                statusText2.setText(stdStatus2);
            }
            errorInStatus2 = false;
        }
    }

    /**
     * Read selected node address.
     * @return The 16 bit node address, if it is not a broadcast address.  
               The 64 bit node address if the 16 bit address is a broadcast address.
     * @throws IllegalArgumentException if no address is selected, or the 16 bit
               address is a broadcast address and no 64 bit address is selected.
     *
     */
    private String readNodeAddress() {
        String addr = "";
        addr = (String) nodeAddrField.getSelectedItem();
        if (addr==null || addr.equals("FF FE ") || addr.equals("FF FF ")) {
            addr = (String) nodeAddr64Field.getSelectedItem();
            if(addr == null)
               throw new IllegalArgumentException("Invalid Address");
        }
        return (addr);
    }

    /**
     * Check for consistency errors by node type Returns 'true' if successful,
     * 'false' if an error was detected. If an error is detected, a suitable
     * error message is placed in the Notes area
     */
    @Override
    protected boolean checkConsistency() {
        return true;
    }

    // Initilize the drop down box for the address lists.
    @Override
    protected void initAddressBoxes() {
        nodeField.removeAllItems();
        for (int i = 0; i < xtc.getNumNodes(); i++) {
            nodeField.insertItemAt((XBeeNode) xtc.getNode(i),i);
        }
        nodeField.insertItemAt(null,0);
    }

   /*
    * package protected method to allow child windows to notify
    * that the list of nodes changed due to an addition/deletion/edit.
    */
   void nodeListChanged(){
       // call initAddressBoxes to update.
       initAddressBoxes();
   }

    // Update the display when the selected node changes.
    @Override
    protected void nodeSelected() {
       log.debug("node {} selected",nodeField.getSelectedItem());
       ((AssignmentTableModel) assignmentListModel).setNode((XBeeNode)nodeField.getSelectedItem());
    }

    // IDiscoveryListener interface methods
   
    /*
     * Device discovered callback.
     */
    @Override
    public void deviceDiscovered(RemoteXBeeDevice discoveredDevice){
        log.debug("New Device discovered {}", discoveredDevice.toString());
    }

    /*
     * Discovery error callback.
     */
    @Override
    public void discoveryError(String error){
        log.error("Error during node discovery process: {}", error);
    }

    /*
     * Discovery finished callback.
     */
    @Override
    public void discoveryFinished(String error){
       if(error != null){
         log.error("Node discovery processed finished with error: {}", error);
         statusText1.setText(Bundle.getMessage("FeedBackDiscoverFail"));
       } else {
         log.debug("Node discovery process completed successfully.");
         statusText1.setText(Bundle.getMessage("FeedBackDiscoverSuccess"));
         // reload the node list.
         initAddressBoxes();
       }
       // removing the listener here is causing a
       // ConcurrentModificaitonException on an ArrayList in the library.
       // xtc.getXBee().getNetwork().removeDiscoveryListener(this);
       discoverButton.setEnabled(true);
    }




    private final static Logger log = LoggerFactory.getLogger(XBeeNodeConfigFrame.class);

}
