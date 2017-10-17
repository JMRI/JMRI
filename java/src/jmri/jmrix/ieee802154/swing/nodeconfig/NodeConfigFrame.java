package jmri.jmrix.ieee802154.swing.nodeconfig;

import java.awt.Container;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import jmri.jmrix.ieee802154.IEEE802154Node;
import jmri.jmrix.ieee802154.IEEE802154TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user configuration of IEEE 802.15.4 nodes. 
 * Derived from node configuration for c/mri nodes.
 *
 * @author Bob Jacobsen Copyright (C) 2004
 * @author Dave Duchamp Copyright (C) 2004
 * @author Paul Bender Copyright (C) 2013
 */
public class NodeConfigFrame extends jmri.util.JmriJFrame {

    protected javax.swing.JComboBox<String> nodeAddrField = new javax.swing.JComboBox<String>();
    protected javax.swing.JComboBox<String> nodeAddr64Field = new javax.swing.JComboBox<String>();
    protected javax.swing.JButton addButton = new javax.swing.JButton(Bundle.getMessage("ButtonAdd"));
    protected javax.swing.JButton editButton = new javax.swing.JButton(Bundle.getMessage("ButtonEdit"));
    protected javax.swing.JButton deleteButton = new javax.swing.JButton(Bundle.getMessage("ButtonDelete"));
    protected javax.swing.JButton doneButton = new javax.swing.JButton(Bundle.getMessage("ButtonDone"));
    protected javax.swing.JButton updateButton = new javax.swing.JButton(Bundle.getMessage("ButtonUpdate"));
    protected javax.swing.JButton cancelButton = new javax.swing.JButton(Bundle.getMessage("ButtonCancel"));

    protected javax.swing.JLabel statusText1 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusText2 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusText3 = new javax.swing.JLabel();

    protected javax.swing.JPanel panel2 = new JPanel();
    protected javax.swing.JPanel panel2a = new JPanel();

    protected boolean changedNode = false;  // true if a node was changed, deleted, or added
    protected boolean editMode = false;     // true if in edit mode

    protected IEEE802154Node curNode = null;    // IEEE802154 Node being editted

    protected boolean errorInStatus1 = false;
    protected boolean errorInStatus2 = false;
    protected String stdStatus1 = Bundle.getMessage("NotesStd1");
    protected String stdStatus2 = Bundle.getMessage("NotesStd2");
    protected String stdStatus3 = Bundle.getMessage("NotesStd3");
    protected String editStatus1 = Bundle.getMessage("NotesEdit1");
    protected String editStatus2 = Bundle.getMessage("NotesEdit2");
    protected String editStatus3 = Bundle.getMessage("NotesEdit3");

    private IEEE802154TrafficController itc = null;

    /**
     * Constructor method
     * @param tc connector for node
     */
    public NodeConfigFrame(IEEE802154TrafficController tc) {
        super();
        addHelpMenu("package.jmri.jmrix.ieee802154.swing.nodeconfig.NodeConfigFrame", true);
        itc = tc;
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
        contentPane.add(initNotesPanel());
        contentPane.add(initButtonPanel());


        // pack for display
        pack();
    }


    /*
     * Initilaize the address panel.
     */
    protected JPanel initAddressPanel(){
        // Set up node address and node type
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
        JPanel panel11 = new JPanel();
        panel11.setLayout(new FlowLayout());
        panel11.add(new JLabel(Bundle.getMessage("LabelNodeAddress") + " "));
        panel11.add(nodeAddrField);
        nodeAddrField.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                nodeSelected();
            }
        });
        nodeAddrField.setToolTipText(Bundle.getMessage("TipNodeAddress"));
        panel11.add(new JLabel(Bundle.getMessage("LabelNodeAddress64") + " "));
        panel11.add(nodeAddr64Field);
        nodeAddr64Field.setToolTipText(Bundle.getMessage("TipNodeAddress64"));
        nodeAddr64Field.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                nodeAddrField.setSelectedIndex(nodeAddr64Field.getSelectedIndex());
            }
        });

        initAddressBoxes();
        panel1.add(panel11);
        return panel1;
    }

    
    /*
     * Initilaize the notes panel.
     */
    protected JPanel initNotesPanel(){
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
        return panel3;
    }

    /*
     * Initilaize the Button panel.
     */
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
    public void addButtonActionPerformed() {
        // create a new Add Frame and display it.
        jmri.util.JmriJFrame addFrame = new AddNodeFrame(itc);
        try {
           addFrame.initComponents();
        } catch(Exception ex) {
           log.error("Exception initializing Frame: {}",ex.toString());
           return;
        }
        addFrame.setVisible(true);
    }

    /**
     * Method to handle edit button
     */
    public void editButtonActionPerformed() {
        // Find IEEE802154 Node address
        String nodeAddress = readNodeAddress();
        if (nodeAddress.equals("")) {
            return;
        }
        // get the IEEE802154Node corresponding to this node address
        curNode = (IEEE802154Node) itc.getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(Bundle.getMessage("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }

        // create a new Edit Frame and display it.
        jmri.util.JmriJFrame editFrame = new EditNodeFrame(itc,curNode);
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
    public void deleteButtonActionPerformed() {
        // Find IEEE802154 Node address
        String nodeAddress = readNodeAddress();
        if (nodeAddress.equals("")) {
            return;
        }
        // get the IEEE802154Node corresponding to this node address
        curNode = (IEEE802154Node) itc.getNodeFromAddress(nodeAddress);
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
            itc.deleteNode(nodeAddress);
            // provide user feedback
            resetNotes();
            statusText1.setText(Bundle.getMessage("FeedBackDelete") + " " + nodeAddress);
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
        statusText1.setText(Bundle.getMessage("FeedBackUpdate") + " " + readNodeAddress());
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
    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        doneButtonActionPerformed();
        super.windowClosing(e);
    }

    /**
     * Method to set node parameters The node must exist, and be in 'curNode'
     */
    protected void setNodeParameters() {
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
     * Read node address from the nodeAddressField or nodeAddr64Field 
     * as appropriate and return as a string.  
     *
     * @return String containing the short (two byte) address of the node.
     *         if the two byte node address is either "FF FF" or "FF FE",
     *         returns the long (64 bit) address.
     */
    private String readNodeAddress() {
        String addr = "";
        addr = (String) nodeAddrField.getSelectedItem();
        if (addr.equals("FF FF ") || addr.equals("FF FE ")) {
            addr = (String) nodeAddr64Field.getSelectedItem();
        }
        return (addr);
    }

    /**
     * Check for consistency errors by node type Returns 'true' if successful,
     * 'false' if an error was detected. If an error is detected, a suitable
     * error message is placed in the Notes area
     * @return always true
     */
    protected boolean checkConsistency() {
        return true;
    }

    // Initilize the drop down box for the address lists.
    protected void initAddressBoxes() {
        IEEE802154Node current = null;
        nodeAddrField.removeAllItems();
        nodeAddr64Field.removeAllItems();
        for (int i = 0; i < itc.getNumNodes(); i++) {
            current = (IEEE802154Node) itc.getNode(i);
            nodeAddrField.insertItemAt(jmri.util.StringUtil.hexStringFromBytes(current.getUserAddress()), i);
            nodeAddr64Field.insertItemAt(jmri.util.StringUtil.hexStringFromBytes(current.getGlobalAddress()), i);
        }
        nodeAddrField.insertItemAt("", 0);
        nodeAddrField.setEditable(true);
        nodeAddr64Field.insertItemAt("", 0);
    }

    // Update the display when the selected node changes.
    protected void nodeSelected() {
        nodeAddr64Field.setSelectedIndex(nodeAddrField.getSelectedIndex());
    }

    private final static Logger log = LoggerFactory.getLogger(NodeConfigFrame.class);

}
