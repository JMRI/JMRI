package jmri.jmrix.secsi.nodeconfig;

import java.awt.Container;
import java.awt.FlowLayout;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import jmri.jmrix.secsi.SerialNode;
import jmri.jmrix.secsi.SerialSensorManager;
import jmri.jmrix.secsi.SerialTrafficController;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user configuration of serial nodes
 *
 * @author	Bob Jacobsen Copyright (C) 2004, 2007, 2008
 * @author	Dave Duchamp Copyright (C) 2004, 2006
 */
public class NodeConfigFrame extends jmri.util.JmriJFrame {

    private SecsiSystemConnectionMemo memo;

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.secsi.nodeconfig.NodeConfigBundle");

    protected javax.swing.JTextField nodeAddrField = new javax.swing.JTextField(3);
    protected javax.swing.JLabel nodeAddrStatic = new javax.swing.JLabel("000");
    protected javax.swing.JComboBox<String> nodeTypeBox;

    protected javax.swing.JButton addButton = new javax.swing.JButton(rb.getString("ButtonAdd"));
    protected javax.swing.JButton editButton = new javax.swing.JButton(rb.getString("ButtonEdit"));
    protected javax.swing.JButton deleteButton = new javax.swing.JButton(rb.getString("ButtonDelete"));
    protected javax.swing.JButton doneButton = new javax.swing.JButton(rb.getString("ButtonDone"));
    protected javax.swing.JButton updateButton = new javax.swing.JButton(rb.getString("ButtonUpdate"));
    protected javax.swing.JButton cancelButton = new javax.swing.JButton(rb.getString("ButtonCancel"));

    protected javax.swing.JLabel statusText1 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusText2 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusText3 = new javax.swing.JLabel();

    protected boolean changedNode = false;  // true if a node was changed, deleted, or added
    protected boolean editMode = false;     // true if in edit mode

    protected SerialNode curNode = null;    // Serial Node being editted
    protected int nodeAddress = 0;          // Node address
    protected int nodeType = SerialNode.DAUGHTER; // Node type

    protected boolean errorInStatus1 = false;
    protected boolean errorInStatus2 = false;
    protected String stdStatus1 = rb.getString("NotesStd1");
    protected String stdStatus2 = rb.getString("NotesStd2");
    protected String stdStatus3 = rb.getString("NotesStd3");
    protected String editStatus1 = rb.getString("NotesEdit1");
    protected String editStatus2 = rb.getString("NotesEdit2");
    protected String editStatus3 = rb.getString("NotesEdit3");

    /**
     * Constructor method
     */
    public NodeConfigFrame(SecsiSystemConnectionMemo _memo) {
        super();
        memo = _memo;
    }

    /**
     * Initialize the config window
     */
    @Override
    public void initComponents() {
        setTitle(rb.getString("WindowTitle"));

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // Set up node address and node type
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));

        // panel11 is the node address and type
        JPanel panel11 = new JPanel();
        panel11.setLayout(new FlowLayout());
        panel11.add(new JLabel(rb.getString("LabelNodeAddress") + " "));
        panel11.add(nodeAddrField);
        nodeAddrField.setToolTipText(rb.getString("TipNodeAddress"));
        nodeAddrField.setText("0");
        panel11.add(nodeAddrStatic);
        nodeAddrStatic.setVisible(false);
        panel11.add(new JLabel("   " + rb.getString("LabelNodeType") + " "));
        nodeTypeBox = new JComboBox<String>(SerialNode.getBoardNames());
        panel11.add(nodeTypeBox);
        nodeTypeBox.setToolTipText(rb.getString("TipNodeType"));
        contentPane.add(panel11);

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
                rb.getString("BoxLabelNotes"));
        panel3.setBorder(panel3Titled);
        contentPane.add(panel3);

        // Set up buttons
        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout());
        addButton.setText(rb.getString("ButtonAdd"));
        addButton.setVisible(true);
        addButton.setToolTipText(rb.getString("TipAddButton"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                addButtonActionPerformed();
            }
        });
        panel4.add(addButton);
        editButton.setText(rb.getString("ButtonEdit"));
        editButton.setVisible(true);
        editButton.setToolTipText(rb.getString("TipEditButton"));
        panel4.add(editButton);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                editButtonActionPerformed();
            }
        });
        panel4.add(deleteButton);
        deleteButton.setText(rb.getString("ButtonDelete"));
        deleteButton.setVisible(true);
        deleteButton.setToolTipText(rb.getString("TipDeleteButton"));
        panel4.add(deleteButton);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                deleteButtonActionPerformed();
            }
        });
        panel4.add(doneButton);
        doneButton.setText(rb.getString("ButtonDone"));
        doneButton.setVisible(true);
        doneButton.setToolTipText(rb.getString("TipDoneButton"));
        panel4.add(doneButton);
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doneButtonActionPerformed();
            }
        });
        panel4.add(updateButton);
        updateButton.setText(rb.getString("ButtonUpdate"));
        updateButton.setVisible(true);
        updateButton.setToolTipText(rb.getString("TipUpdateButton"));
        panel4.add(updateButton);
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                updateButtonActionPerformed();
            }
        });
        updateButton.setVisible(false);
        panel4.add(cancelButton);
        cancelButton.setText(rb.getString("ButtonCancel"));
        cancelButton.setVisible(true);
        cancelButton.setToolTipText(rb.getString("TipCancelButton"));
        panel4.add(cancelButton);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cancelButtonActionPerformed();
            }
        });
        cancelButton.setVisible(false);
        contentPane.add(panel4);

        // pack for display
        pack();
    }

    /**
     * Method to handle add button
     */
    public void addButtonActionPerformed() {
        // Check that a node with this address does not exist
        int nodeAddress = readNodeAddress();
        if (nodeAddress < 0) {
            return;
        }
        // get a SerialNode corresponding to this node address if one exists
        curNode = (SerialNode) memo.getTrafficController().getNodeFromAddress(nodeAddress);
        if (curNode != null) {
            statusText1.setText(rb.getString("Error1") + Integer.toString(nodeAddress)
                    + rb.getString("Error2"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        nodeType = nodeTypeBox.getSelectedIndex();

        // all ready, create the new node
        curNode = new SerialNode(nodeAddress, nodeType,memo.getTrafficController());
        if (curNode == null) {
            statusText1.setText(rb.getString("Error3"));
            statusText1.setVisible(true);
            log.error("Error creating Serial Node, constructor returned null");
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // configure the new node
        setNodeParameters();
        // register any orphan sensors that this node may have
        ((SerialSensorManager)memo.getSensorManager()).registerSensorsForNode(curNode);
        // reset after succefully adding node
        resetNotes();
        changedNode = true;
        // provide user feedback
        statusText1.setText(rb.getString("FeedBackAdd") + " "
                + Integer.toString(nodeAddress));
        errorInStatus1 = true;
    }

    /**
     * Method to handle edit button
     */
    public void editButtonActionPerformed() {
        // Find Serial Node address
        nodeAddress = readNodeAddress();
        if (nodeAddress < 0) {
            return;
        }
        // get the SerialNode corresponding to this node address
        curNode = (SerialNode) memo.getTrafficController().getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(rb.getString("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // Set up static node address
        nodeAddrStatic.setText(Integer.toString(nodeAddress));
        nodeAddrField.setVisible(false);
        nodeAddrStatic.setVisible(true);
        // get information for this node and set up combo box
        nodeType = curNode.getNodeType();
        nodeTypeBox.setSelectedIndex(nodeType);
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
    }

    /**
     * Method to handle delete button
     */
    public void deleteButtonActionPerformed() {
        // Find Serial Node address
        int nodeAddress = readNodeAddress();
        if (nodeAddress < 0) {
            return;
        }
        // get the SerialNode corresponding to this node address
        curNode = (SerialNode) memo.getTrafficController().getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(rb.getString("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // confirm deletion with the user
        if (javax.swing.JOptionPane.OK_OPTION == javax.swing.JOptionPane.showConfirmDialog(
                this, rb.getString("ConfirmDelete1") + "\n"
                + rb.getString("ConfirmDelete2"), rb.getString("ConfirmDeleteTitle"),
                javax.swing.JOptionPane.OK_CANCEL_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE)) {
            // delete this node
            memo.getTrafficController().deleteNode(nodeAddress);
            // provide user feedback
            resetNotes();
            statusText1.setText(rb.getString("FeedBackDelete") + " "
                    + Integer.toString(nodeAddress));
            errorInStatus1 = true;
            changedNode = true;
        } else {
            // reset as needed
            resetNotes();
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
            nodeAddrField.setVisible(true);
            nodeAddrStatic.setVisible(false);
        }
        if (changedNode) {
            // Remind user to Save new configuration
            javax.swing.JOptionPane.showMessageDialog(this,
                    rb.getString("Reminder1") + "\n" + rb.getString("Reminder2"),
                    rb.getString("ReminderTitle"),
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
        setVisible(false);
        dispose();
    }

    /**
     * Method to handle update button
     */
    public void updateButtonActionPerformed() {
        // update node information
        nodeType = nodeTypeBox.getSelectedIndex();
        log.debug("update performed: was " + curNode.getNodeType() + " request " + nodeType);
        if (curNode.getNodeType() != nodeType) {
            // node type has changed
            curNode.setNodeType(nodeType);
        }
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
        // make node address editable again	
        nodeAddrField.setVisible(true);
        nodeAddrStatic.setVisible(false);
        // refresh notes panel
        statusText2.setText(stdStatus2);
        statusText3.setText(stdStatus3);
        // provide user feedback
        statusText1.setText(rb.getString("FeedBackUpdate") + " "
                + Integer.toString(nodeAddress));
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
        // make node address editable again	
        nodeAddrField.setVisible(true);
        nodeAddrStatic.setVisible(false);
        // refresh notes panel
        statusText1.setText(stdStatus1);
        statusText2.setText(stdStatus2);
        statusText3.setText(stdStatus3);
    }

    /**
     * Do the done action if the window is closed early.
     */
    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        doneButtonActionPerformed();
    }

    /**
     * Method to set node parameters The node must exist, and be in 'curNode'
     * Also, the node type must be set and in 'nodeType'
     */
    void setNodeParameters() {
        // set curNode type
        curNode.setNodeType(nodeType);
        // Cause reinitialization of this Node to reflect these parameters
        memo.getTrafficController().initializeSerialNode(curNode);
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
     * Read node address and check for legal range If successful, a node address
     * in the range 0-255 is returned. If not successful, -1 is returned and an
     * appropriate error message is placed in statusText1.
     */
    private int readNodeAddress() {
        int addr = -1;
        try {
            addr = Integer.parseInt(nodeAddrField.getText());
        } catch (Exception e) {
            statusText1.setText(rb.getString("Error5"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return -1;
        }
        if ((addr < 0) || (addr > 255)) {
            statusText1.setText(rb.getString("Error6"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return -1;
        }
        return (addr);
    }

    private final static Logger log = LoggerFactory.getLogger(NodeConfigFrame.class);

}
