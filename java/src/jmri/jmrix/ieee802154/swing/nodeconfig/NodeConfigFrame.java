// NodeConfigFrame.java

package jmri.jmrix.ieee802154.swing.nodeconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;

import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.Border;

import jmri.jmrix.ieee802154.IEEE802154TrafficController;
import jmri.jmrix.ieee802154.IEEE802154Node;

/**
 * Frame for user configuration of XBee nodes
 * Derived from node configuration for c/mri nodes.
 * @author	Bob Jacobsen   Copyright (C) 2004
 * @author	Dave Duchamp   Copyright (C) 2004
 * @author	Paul Bender Copyright (C) 2013
 * @version	$Revision$
 */
public class NodeConfigFrame extends jmri.util.JmriJFrame {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.ieee802154.swing.nodeconfig.NodeConfigBundle");

    protected javax.swing.JComboBox nodeAddrField = new javax.swing.JComboBox();
    protected javax.swing.JComboBox nodeAddr64Field = new javax.swing.JComboBox();
    protected javax.swing.JButton addButton = new javax.swing.JButton(rb.getString("ButtonAdd"));
    protected javax.swing.JButton editButton = new javax.swing.JButton(rb.getString("ButtonEdit"));
    protected javax.swing.JButton deleteButton = new javax.swing.JButton(rb.getString("ButtonDelete"));
    protected javax.swing.JButton doneButton = new javax.swing.JButton(rb.getString("ButtonDone"));
    protected javax.swing.JButton updateButton = new javax.swing.JButton(rb.getString("ButtonUpdate"));
    protected javax.swing.JButton cancelButton = new javax.swing.JButton(rb.getString("ButtonCancel"));
		
    protected javax.swing.JLabel statusText1 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusText2 = new javax.swing.JLabel();
    protected javax.swing.JLabel statusText3 = new javax.swing.JLabel();		

    protected javax.swing.JPanel panel2 = new JPanel();
    protected javax.swing.JPanel panel2a = new JPanel();

    protected boolean changedNode = false;  // true if a node was changed, deleted, or added
    protected boolean editMode = false;     // true if in edit mode
    
    protected IEEE802154Node curNode = null;    // IEEE802154 Node being editted
    protected int nodeAddress = 0;          // 16 bit Node address
    protected String address64 = "";          // 64 bit Node address

    protected boolean errorInStatus1 = false;
    protected boolean errorInStatus2 = false;
    protected String stdStatus1 = rb.getString("NotesStd1");
    protected String stdStatus2 = rb.getString("NotesStd2");
    protected String stdStatus3 = rb.getString("NotesStd3");
    protected String editStatus1 = rb.getString("NotesEdit1");
    protected String editStatus2 = rb.getString("NotesEdit2");
    protected String editStatus3 = rb.getString("NotesEdit3");
	
    private IEEE802154TrafficController itc=null;
	
    /**
     * Constructor method
     */
    public NodeConfigFrame(IEEE802154TrafficController tc) {
    	super();
        addHelpMenu("package.jmri.jmrix.cmri.serial.nodeconfig.NodeConfigFrame", true);
        itc=tc;
    }

    /** 
     *  Initialize the config window
     */
    public void initComponents() {
        setTitle(rb.getString("WindowTitle"));
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
			
        // Set up node address and node type
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
        JPanel panel11 = new JPanel();
        panel11.setLayout(new FlowLayout());
        panel11.add(new JLabel(rb.getString("LabelNodeAddress")+" "));
        panel11.add(nodeAddrField);
        nodeAddrField.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                nodeSelected();
            }
        });
        nodeAddrField.setToolTipText(rb.getString("TipNodeAddress"));
        panel11.add(new JLabel(rb.getString("LabelNodeAddress64")+" "));
        panel11.add(nodeAddr64Field);
        nodeAddr64Field.setToolTipText(rb.getString("TipNodeAddress64"));

        initAddressBoxes();

        JPanel panel12 = new JPanel();
        panel12.setLayout(new FlowLayout());

        panel1.add(panel11);			
        panel1.add(panel12);			

        contentPane.add(panel1);			

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
        if (nodeAddress < 0) return;
        // get a IEEE802154 Node corresponding to this node address if one exists
        curNode = (IEEE802154Node) itc.getNodeFromAddress(nodeAddress);
        if (curNode != null) {
            statusText1.setText(rb.getString("Error1")+Integer.toString(nodeAddress)+
                        rb.getString("Error2"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // get node information from window

        // check consistency of node information
        if ( !checkConsistency() ) return;
        // all ready, create the new node
        curNode = itc.newNode();
        if (curNode == null) {
            statusText1.setText(rb.getString("Error3"));
            statusText1.setVisible(true);
            log.error("Error creating IEEE802154 Node, constructor returned null");
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // configure the new node
        setNodeParameters();

        // reset after succefully adding node
        resetNotes();
        changedNode = true;
        // provide user feedback
        statusText1.setText(rb.getString("FeedBackAdd")+" "+
                                    Integer.toString(nodeAddress));
        errorInStatus1 = true;
    }

    /**
     * Method to handle edit button 
     */        
    public void editButtonActionPerformed() {
        // Find IEEE802154 Node address
        nodeAddress = readNodeAddress();
        if (nodeAddress < 0) return;
        // get the IEEE802154Node corresponding to this node address
        curNode = (IEEE802154Node) itc.getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(rb.getString("Error4"));
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
    }

    /**
     * Method to handle delete button 
     */        
    public void deleteButtonActionPerformed() {
        // Find IEEE802154 Node address
        int nodeAddress = readNodeAddress();
        if (nodeAddress < 0) return;
        // get the IEEE802154Node corresponding to this node address
        curNode = (IEEE802154Node) itc.getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(rb.getString("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // confirm deletion with the user
        if ( javax.swing.JOptionPane.OK_OPTION == javax.swing.JOptionPane.showConfirmDialog(
                this,rb.getString("ConfirmDelete1")+"\n"+
                    rb.getString("ConfirmDelete2"),rb.getString("ConfirmDeleteTitle"),
                        javax.swing.JOptionPane.OK_CANCEL_OPTION,
                            javax.swing.JOptionPane.WARNING_MESSAGE) ) {
            // delete this node
            itc.deleteNode(nodeAddress);
            // provide user feedback
            resetNotes();
            statusText1.setText(rb.getString("FeedBackDelete")+" "+
                                    Integer.toString(nodeAddress));
            errorInStatus1 = true;
            changedNode = true;
	}
        else {
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
        }
        if (changedNode) {
            // Remind user to Save new configuration
            javax.swing.JOptionPane.showMessageDialog(this,
                    rb.getString("Reminder1")+"\n"+rb.getString("Reminder2"),
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
        // get node information from window
 
       // check consistency of node information
        if ( !checkConsistency() ) return;
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
        statusText1.setText(rb.getString("FeedBackUpdate")+" "+
                                    Integer.toString(nodeAddress));
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
     * Method to set node parameters
     *    The node must exist, and be in 'curNode'
     */
    void setNodeParameters() {
    }
    
    /**
     * Method to reset the notes error after error display
     */
    private void resetNotes() {
        if (errorInStatus1) {
            if (editMode) {
                statusText1.setText(editStatus1);
            }
            else {
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
            }
            else {
                statusText2.setText(stdStatus2);
            }
            errorInStatus2 = false;
        }
    }
    
    /**
     * Read node address and check for legal range
     *     If successful, a node address in the range 0-127 is returned.
     *     If not successful, -1 is returned and an appropriate error
     *          message is placed in statusText1.
     */
    private int readNodeAddress() {
        int addr = -1;
        try 
        {
            addr = Integer.parseInt((String)nodeAddrField.getSelectedItem());
        }
        catch (Exception e)
        {
            statusText1.setText(rb.getString("Error5"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return -1;
        }
        if ( (addr < 0) || (addr > 127) ) {
            statusText1.setText(rb.getString("Error6"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return -1;
        }
        return (addr);
    }
    
    /**
     * Check for consistency errors by node type
     *    Returns 'true' if successful, 'false' if an error was detected.
     *    If an error is detected, a suitable error message is placed in the
     *        Notes area
     */
    protected boolean checkConsistency() {
        return true;
    }

    // Initilize the drop down box for the address lists.
    protected void initAddressBoxes() {
       IEEE802154Node current=null;
       nodeAddrField.removeAllItems();
       nodeAddr64Field.removeAllItems();
       for(int i=0;i<itc.getNumNodes();i++){
           current=(IEEE802154Node) itc.getNode(i);
           nodeAddrField.insertItemAt(current.getNodeAddress(),i);           
           nodeAddr64Field.insertItemAt(jmri.util.StringUtil.hexStringFromBytes(current.getGlobalAddress()),i);           
       }
       nodeAddrField.insertItemAt("",0);
       nodeAddrField.setEditable(true);
       nodeAddr64Field.insertItemAt("",0);
    }

    // Update the display when the selected node changes.
    protected void nodeSelected(){
       nodeAddr64Field.setSelectedIndex(nodeAddrField.getSelectedIndex());
    }



    static Logger log = LoggerFactory.getLogger(NodeConfigFrame.class.getName());

}
