package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import com.digi.xbee.api.listeners.IDiscoveryListener;
import com.digi.xbee.api.RemoteXBeeDevice;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import jmri.jmrix.ieee802154.xbee.XBeeNode;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user configuration of XBee nodes Derived from node configuration
 * for c/mri nodes.
 *
 * @author	Bob Jacobsen Copyright (C) 2004
 * @author	Dave Duchamp Copyright (C) 2004
 * @author	Paul Bender Copyright (C) 2013
 */
public class NodeConfigFrame extends jmri.jmrix.ieee802154.swing.nodeconfig.NodeConfigFrame implements IDiscoveryListener {

    private XBeeTrafficController xtc = null;
    protected javax.swing.JButton discoverButton = new javax.swing.JButton(Bundle.getMessage("ButtonDiscover"));
    private JComboBox<String> nodeIdentifierField = new javax.swing.JComboBox<String>();
    protected JTable assignmentTable = null;
    protected TableModel assignmentListModel = null;

    protected JPanel assignmentPanel = null;

    /**
     * Constructor method
     */
    public NodeConfigFrame(XBeeTrafficController tc) {
        super(tc);
        xtc = tc;
    }

    /**
     * Initialize the config window
     */
    public void initComponents() {
        setTitle(Bundle.getMessage("WindowTitle"));
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // Set up node address and node type
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
        JPanel panel11 = new JPanel();
        panel11.setLayout(new FlowLayout());
        panel11.add(new JLabel(Bundle.getMessage("LabelNodeAddress") + " "));
        panel11.add(nodeAddrField);
        nodeAddrField.setToolTipText(Bundle.getMessage("TipNodeAddress"));
        nodeAddrField.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                nodeSelected();
            }
        });
        panel11.add(new JLabel(Bundle.getMessage("LabelNodeAddress64") + " "));
        panel11.add(nodeAddr64Field);
        nodeAddr64Field.setToolTipText(Bundle.getMessage("TipNodeAddress64"));
        nodeAddr64Field.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                nodeAddrField.setSelectedIndex(nodeAddr64Field.getSelectedIndex());
            }
        });
        panel11.add(new JLabel(Bundle.getMessage("LabelNodeIdentifier") + " "));
        panel11.add(nodeIdentifierField);
        nodeIdentifierField.setToolTipText(Bundle.getMessage("TipNodeIdentifier"));

        nodeIdentifierField.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                nodeAddrField.setSelectedIndex(nodeIdentifierField.getSelectedIndex());
            }
        });
        JPanel panel12 = new JPanel();
        panel12.setLayout(new FlowLayout());

        initAddressBoxes();

        panel1.add(panel11);
        panel1.add(panel12);

        contentPane.add(panel1);

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

        // Set up the notes panel
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        JPanel panel31 = new JPanel();
        panel31.setLayout(new FlowLayout());
        statusText1.setText(stdStatus1);
        statusText1.setVisible(true);
        panel31.add(statusText1);
        JPanel panel32 = new JPanel();
        panel32.setLayout(new FlowLayout());
        statusText2.setText(stdStatus2);
        statusText2.setVisible(true);
        panel32.add(statusText2);
        JPanel panel33 = new JPanel();
        panel33.setLayout(new FlowLayout());
        statusText3.setText(stdStatus3);
        statusText3.setVisible(true);
        panel33.add(statusText3);
        panel3.add(panel31);
        panel3.add(panel32);
        panel3.add(panel33);
        Border panel3Border = BorderFactory.createEtchedBorder();
        Border panel3Titled = BorderFactory.createTitledBorder(panel3Border,
                Bundle.getMessage("BoxLabelNotes"));
        panel3.setBorder(panel3Titled);
        contentPane.add(panel3);

        // Set up buttons
        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout());
        addButton.setText(Bundle.getMessage("ButtonAdd"));
        addButton.setVisible(true);
        addButton.setToolTipText(Bundle.getMessage("TipAddButton"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                addButtonActionPerformed();
            }
        });
        panel4.add(addButton);
        discoverButton.setText(Bundle.getMessage("ButtonDiscover"));
        discoverButton.setVisible(true);
        discoverButton.setToolTipText(Bundle.getMessage("TipAddButton"));
        discoverButton.addActionListener(new java.awt.event.ActionListener() {
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
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cancelButtonActionPerformed();
            }
        });
        cancelButton.setVisible(false);
        contentPane.add(panel4);
        // pack for display
        pack();

        // after the components are configured, set ourselves up as a 
        // discovery listener.
        xtc.getXBee().getNetwork().addDiscoveryListener(this);

    }

    /**
     * Method to handle add button
     */
    public void addButtonActionPerformed() {
        try {
           // Check that a node with this address does not exist
           String nodeAddress = readNodeAddress();
           if (nodeAddress.equals("")) {
               return;
           }
           // get a XBeeNode corresponding to this node address if one exists
           curNode = (XBeeNode) xtc.getNodeFromAddress(nodeAddress);
           if (curNode != null) {
               statusText1.setText(Bundle.getMessage("Error1") + nodeAddress
                       + Bundle.getMessage("Error2"));
               statusText1.setVisible(true);
               errorInStatus1 = true;
               resetNotes2();
               return;
           }
           // get node information from window

           // check consistency of node information
           if (!checkConsistency()) {
               return;
           }
           // all ready, create the new node
           curNode = new XBeeNode();
           // configure the new node
           setNodeParameters();

           // reset after succefully adding node
           resetNotes();
           changedNode = true;
           // provide user feedback
           statusText1.setText(Bundle.getMessage("FeedBackAdd") + " " + nodeAddress);
           errorInStatus1 = true;
           initAddressBoxes();
       } catch(IllegalArgumentException iae){
               // we really need to set an error status here.
               // illegal argument exception is generated by 
               // readNodeAddress when neither a 16 or 64 bit 
               // addresses is selected.
               return;
       }
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
    public void editButtonActionPerformed() {
       try {
          // Find XBee Node address
          String nodeAddress = readNodeAddress();
          if (nodeAddress.equals("")) {
              return;
          }
          // get the XBeeNode corresponding to this node address
          curNode = (XBeeNode) xtc.getNodeFromAddress(nodeAddress);
          if (curNode == null) {
              statusText1.setText(Bundle.getMessage("Error4"));
              statusText1.setVisible(true);
              errorInStatus1 = true;
              resetNotes2();
              return;
          }

          // Switch buttons
          editMode = true;
          addButton.setVisible(false);
          editButton.setVisible(false);
          deleteButton.setVisible(false);
          doneButton.setVisible(false);
          updateButton.setVisible(true);
          cancelButton.setVisible(true);
          // Switch to edit notes
          statusText1.setText(editStatus1);
          statusText2.setText(editStatus2);
          statusText3.setText(editStatus3);
       } catch(IllegalArgumentException iae){
               // we really need to set an error status here.
               // illegal argument exception is generated by 
               // readNodeAddress when neither a 16 or 64 bit 
               // addresses is selected.
               return;
       }
    }

    /**
     * Method to handle done button
     */
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
    protected boolean checkConsistency() {
        return true;
    }

    // Initilize the drop down box for the address lists.
    @Override
    protected void initAddressBoxes() {
        super.initAddressBoxes();
        XBeeNode current = null;
        nodeIdentifierField.removeAllItems();
        for (int i = 0; i < xtc.getNumNodes(); i++) {
            current = (XBeeNode) xtc.getNode(i);
            nodeIdentifierField.insertItemAt(current.getIdentifier(), i);
        }
        nodeIdentifierField.insertItemAt("", 0);
    }

    // Update the display when the selected node changes.
    @Override
    protected void nodeSelected() {
        try {
           String nodeAddress = readNodeAddress();
           nodeAddrField.setSelectedIndex(nodeAddrField.getSelectedIndex());
           nodeAddr64Field.setSelectedIndex(nodeAddrField.getSelectedIndex());
           nodeIdentifierField.setSelectedIndex(nodeAddrField.getSelectedIndex());
           if (!(nodeAddress.equals(""))) {
              ((AssignmentTableModel) assignmentListModel).setNode((XBeeNode) xtc.getNodeFromAddress(nodeAddress));
           } else {
              log.error("No Node Selected");
           }
       } catch(IllegalArgumentException iae){
               // we really need to set an error status here.
               // illegal argument exception is generated by 
               // readNodeAddress when neither a 16 or 64 bit 
               // addresses is selected.
               return;
       }
    }

    /**
     * Set up table for displaying bit assignments
     */
    public class AssignmentTableModel extends AbstractTableModel {

        /**
         *
         */
        private static final long serialVersionUID = 2449916200516563370L;

        private XBeeNode curNode = null;

        public static final int BIT_COLUMN = 0;
        public static final int SYSNAME_COLUMN = 1;
        public static final int USERNAME_COLUMN = 2;

        private String[] assignmentTableColumnNames = {Bundle.getMessage("HeadingBit"),
            Bundle.getMessage("HeadingSystemName"),
            Bundle.getMessage("HeadingUserName")};

        private String free = Bundle.getMessage("AssignmentFree");

        public void setNode(XBeeNode node) {
            curNode = node;
            fireTableDataChanged();
        }

        public void initTable(JTable assignmentTable) {
            TableColumnModel assignmentColumnModel = assignmentTable.getColumnModel();
            TableColumn bitColumn = assignmentColumnModel.getColumn(BIT_COLUMN);
            bitColumn.setMinWidth(20);
            bitColumn.setMaxWidth(40);
            bitColumn.setResizable(true);
            TableColumn sysColumn = assignmentColumnModel.getColumn(SYSNAME_COLUMN);
            sysColumn.setMinWidth(75);
            sysColumn.setMaxWidth(100);
            sysColumn.setResizable(true);
            TableColumn userColumn = assignmentColumnModel.getColumn(USERNAME_COLUMN);
            userColumn.setMinWidth(90);
            userColumn.setMaxWidth(450);
            userColumn.setResizable(true);
        }

        public String getColumnName(int c) {
            return assignmentTableColumnNames[c];
        }

        public Class<?> getColumnClass(int c) {
            if (c == BIT_COLUMN) {
                return Integer.class;
            } else {
                return String.class;
            }
        }

        public boolean isCellEditable(int r, int c) {
            return false;
        }

        public int getColumnCount() {
            return 3;
        }

        public int getRowCount() {
            return 8;
        }

        public Object getValueAt(int r, int c) {
            Integer pin = Integer.valueOf(r);
            try {
                switch (c) {
                    case BIT_COLUMN:
                        return pin;
                    case SYSNAME_COLUMN:
                        if (curNode.getPinAssigned(pin)) {
                            return curNode.getPinBean(pin).getSystemName();
                        } else {
                            return free;
                        }
                    case USERNAME_COLUMN:
                        if (curNode.getPinAssigned(pin)) {
                            return curNode.getPinBean(pin).getUserName();
                        } else {
                            return "";
                        }
                    default:
                        return "";
                }
            } catch (java.lang.NullPointerException npe) {
                log.debug("Caught NPE getting pin assignment for pin {}", pin);
                return "";
            }
        }

        public void setValueAt(Object type, int r, int c) {
            // nothing is stored here
        }
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
    public void discoveryError(String error){
        log.error("Error durring node discovery process: {}",error);
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
         initAddressBoxes();
       }
       // removing the listener here is causing a
       // ConcurrentModificaitonException on an ArrayList in the library.
       // xtc.getXBee().getNetwork().removeDiscoveryListener(this);
       discoverButton.setEnabled(true);
    }




    private final static Logger log = LoggerFactory.getLogger(NodeConfigFrame.class.getName());

}
