// NodeConfigFrame.java

package jmri.jmrix.cmri.serial.nodeconfig;

import org.apache.log4j.Logger;
import java.awt.*;

import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.Border;

import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.jmrix.cmri.serial.SerialNode;
import jmri.jmrix.cmri.serial.SerialSensorManager;

/**
 * Frame for user configuration of CMRI serial nodes
 * @author	Bob Jacobsen   Copyright (C) 2004
 * @author	Dave Duchamp   Copyright (C) 2004
 * @version	$Revision$
 */
public class NodeConfigFrame extends jmri.util.JmriJFrame {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.cmri.serial.nodeconfig.NodeConfigBundle");

    protected javax.swing.JTextField nodeAddrField = new javax.swing.JTextField(3);
    protected javax.swing.JLabel nodeAddrStatic = new javax.swing.JLabel("000");
    protected javax.swing.JComboBox nodeTypeBox; 
    protected javax.swing.JTextField receiveDelayField = new javax.swing.JTextField(3);
    protected javax.swing.JTextField pulseWidthField = new javax.swing.JTextField(4);
    protected javax.swing.JComboBox cardSizeBox; 
    protected javax.swing.JLabel cardSizeText = new javax.swing.JLabel("   "+rb.getString("LabelCardSize"));
    
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
    
    protected SerialNode curNode = null;    // Serial Node being editted
    protected int nodeAddress = 0;          // Node address
    protected int nodeType = SerialNode.SMINI; // Node type
    protected int bitsPerCard = 24;         // number of bits per card
    protected int receiveDelay = 0;         // transmission delay
	protected int pulseWidth = 500;			// pulse width for turnout control (milliseconds)
    protected int num2LSearchLights = 0;    // number of 2-lead oscillating searchlights

    protected int numCards = 0;             //set by consistency check routine

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
    public NodeConfigFrame() {
    	super();
        // Clear information arrays
        for (int i = 0; i<64 ; i++) {
            cardType[i] = rb.getString("CardTypeNone");
        }
        for (int i = 0; i<48 ; i++) {
            searchlightBits[i] = false;
            firstSearchlight[i] = false;
        }
        addHelpMenu("package.jmri.jmrix.cmri.serial.nodeconfig.NodeConfigFrame", true);
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
        nodeAddrField.setToolTipText(rb.getString("TipNodeAddress"));
        nodeAddrField.setText("0");
        panel11.add(nodeAddrStatic);
        nodeAddrStatic.setVisible(false);
        panel11.add(new JLabel("   "+rb.getString("LabelNodeType")+" "));
        nodeTypeBox = new JComboBox();
        panel11.add(nodeTypeBox);
        nodeTypeBox.addItem("SMINI");
        nodeTypeBox.addItem("USIC_SUSIC");
// Here add code for other types of nodes
        nodeTypeBox.addActionListener(new java.awt.event.ActionListener() 
            {
                public void actionPerformed(java.awt.event.ActionEvent event)
                {
                    String s = (String)nodeTypeBox.getSelectedItem();
                    if (s.equals("SMINI")) {
                        panel2.setVisible(false);
                        panel2a.setVisible(true);
                        cardSizeText.setVisible(false);
                        cardSizeBox.setVisible(false);
                        nodeType = SerialNode.SMINI;
                    }
                    else if (s.equals("USIC_SUSIC")) {
                        panel2.setVisible(true);
                        panel2a.setVisible(false);
                        cardSizeText.setVisible(true);
                        cardSizeBox.setVisible(true);
                        nodeType = SerialNode.USIC_SUSIC;
                    }
// Here add code for other types of nodes
                    // reset notes as appropriate
                    resetNotes();
                }
            });
        nodeTypeBox.setToolTipText(rb.getString("TipNodeType"));
        JPanel panel12 = new JPanel();
        panel12.setLayout(new FlowLayout());
        panel12.add(new JLabel(rb.getString("LabelDelay")+" "));
        panel12.add(receiveDelayField);
        receiveDelayField.setToolTipText(rb.getString("TipDelay"));
        receiveDelayField.setText("0");
        panel12.add(cardSizeText);
        cardSizeBox = new JComboBox();
        panel12.add(cardSizeBox);
        cardSizeBox.addItem(rb.getString("CardSize24"));
        cardSizeBox.addItem(rb.getString("CardSize32"));
// here add code for other node types, if required
        cardSizeBox.addActionListener(new java.awt.event.ActionListener() 
            {
                public void actionPerformed(java.awt.event.ActionEvent event)
                {
                    String s = (String)cardSizeBox.getSelectedItem();
                    if (s.equals(rb.getString("CardSize24"))) {
                        bitsPerCard = 24;
                    }
                    else if (s.equals(rb.getString("CardSize32"))) {
                        bitsPerCard = 32;
                    }
// here add code for other node types, if required
                }
            });
        cardSizeBox.setToolTipText(rb.getString("TipCardSize"));
        cardSizeText.setVisible(false);
        cardSizeBox.setVisible(false);
        JPanel panel13 = new JPanel();
        panel13.setLayout(new FlowLayout());
        panel13.add(new JLabel(rb.getString("LabelPulseWidth")+" "));
        panel13.add(pulseWidthField);
        pulseWidthField.setToolTipText(rb.getString("TipPulseWidth"));
        pulseWidthField.setText("500");
        panel13.add(new JLabel(rb.getString("LabelMilliseconds")));
			
        panel1.add(panel11);
        panel1.add(panel12);
		panel1.add(panel13);
        contentPane.add(panel1);			

        // Set up USIC/SUSIC card type configuration table
        JPanel panel21 = new JPanel();
        panel21.setLayout(new BoxLayout(panel21, BoxLayout.Y_AXIS));
        panel21.add(new JLabel(rb.getString("HintCardTypePartA")));
        panel21.add(new JLabel(" "+rb.getString("HintCardTypePartB")));
        panel21.add(new JLabel(" "+rb.getString("HintCardTypePartC")));
        panel21.add(new JLabel("   "));
        panel21.add(new JLabel(rb.getString("HintCardTypePartD")));
        panel21.add(new JLabel(" "+rb.getString("HintCardTypePartE")));
        panel21.add(new JLabel(" "+rb.getString("HintCardTypePartF")));
        panel2.add(panel21);
        TableModel cardConfigModel = new CardConfigModel();
        JTable cardConfigTable = new JTable(cardConfigModel);
        cardConfigTable.setRowSelectionAllowed(false);
        cardConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180,100));
			
        JComboBox cardTypeCombo = new JComboBox();
        cardTypeCombo.addItem(rb.getString("CardTypeOutput"));
        cardTypeCombo.addItem(rb.getString("CardTypeInput"));
        cardTypeCombo.addItem(rb.getString("CardTypeNone"));
			
        TableColumnModel typeColumnModel = cardConfigTable.getColumnModel();
        TableColumn addressColumn = typeColumnModel.getColumn(CardConfigModel.ADDRESS_COLUMN);
        addressColumn.setMinWidth(70);
        addressColumn.setMaxWidth(80);
        TableColumn cardTypeColumn = typeColumnModel.getColumn(CardConfigModel.TYPE_COLUMN);
        cardTypeColumn.setCellEditor(new DefaultCellEditor(cardTypeCombo));
        cardTypeColumn.setResizable(false);
        cardTypeColumn.setMinWidth(90);
        cardTypeColumn.setMaxWidth(100);
			
        JScrollPane cardScrollPane = new JScrollPane(cardConfigTable);
        panel2.add(cardScrollPane,BorderLayout.CENTER);
        contentPane.add(panel2);
        panel2.setVisible(false);
			
        // Set up SMINI oscillating 2-lead searchlight configuration table
        JPanel panel2a1 = new JPanel();
        panel2a1.setLayout(new BoxLayout(panel2a1, BoxLayout.Y_AXIS));
        panel2a1.add(new JLabel(rb.getString("HintSearchlightPartA")));
        panel2a1.add(new JLabel(" "+rb.getString("HintSearchlightPartB")));
        panel2a1.add(new JLabel(" "+rb.getString("HintSearchlightPartC")));
        panel2a1.add(new JLabel("   "));
        panel2a1.add(new JLabel(rb.getString("HintSearchlightPartD")));
        panel2a1.add(new JLabel(" "+rb.getString("HintSearchlightPartE")));
        panel2a1.add(new JLabel(" "+rb.getString("HintSearchlightPartF")));
        panel2a.add(panel2a1);
        TableModel searchlightConfigModel = new SearchlightConfigModel();
        JTable searchlightConfigTable = new JTable(searchlightConfigModel);
        searchlightConfigTable.setRowSelectionAllowed(false);
        searchlightConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(208,100));
        TableColumnModel searchlightColumnModel = searchlightConfigTable.getColumnModel();
        TableColumn portColumn = searchlightColumnModel.getColumn(SearchlightConfigModel.PORT_COLUMN);
        portColumn.setMinWidth(90);
        portColumn.setMaxWidth(100);
        JScrollPane searchlightScrollPane = new JScrollPane(searchlightConfigTable);
        panel2a.add(searchlightScrollPane,BorderLayout.CENTER);
        contentPane.add(panel2a);			

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
        // get a SerialNode corresponding to this node address if one exists
        curNode = (SerialNode) SerialTrafficController.instance().getNodeFromAddress(nodeAddress);
        if (curNode != null) {
            statusText1.setText(rb.getString("Error1")+Integer.toString(nodeAddress)+
                        rb.getString("Error2"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // get node information from window
        if ( !readReceiveDelay() ) return;
		if ( !readPulseWidth() ) return;
        // check consistency of node information
        if ( !checkConsistency() ) return;
        // all ready, create the new node
        curNode = new SerialNode(nodeAddress,nodeType);
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
        SerialSensorManager.instance().registerSensorsForNode(curNode);
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
        // Find Serial Node address
        nodeAddress = readNodeAddress();
        if (nodeAddress < 0) return;
        // get the SerialNode corresponding to this node address
        curNode = (SerialNode) SerialTrafficController.instance().getNodeFromAddress(nodeAddress);
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
        if (nodeType==SerialNode.SMINI) {
            nodeTypeBox.setSelectedItem("SMINI");
        }
        else if (nodeType==SerialNode.USIC_SUSIC) {
            nodeTypeBox.setSelectedItem("USIC_SUSIC");
        }
// here add code for other node types
        // Node specific initialization
        if (nodeType==SerialNode.USIC_SUSIC) {
            bitsPerCard = curNode.getNumBitsPerCard();
            if (bitsPerCard==24) {
                cardSizeBox.setSelectedItem(rb.getString("CardSize24"));
            }
            if (bitsPerCard==32) {
                cardSizeBox.setSelectedItem(rb.getString("CardSize32"));
            }
        }
        else if (nodeType==SerialNode.SMINI) {
            bitsPerCard = 24;
            cardSizeBox.setSelectedItem(rb.getString("CardSize24"));
            // set up the searchlight arrays
            num2LSearchLights = 0;
            for (int i=0;i<48;i++) {
                if ( curNode.isSearchLightBit(i) ) {
                    searchlightBits[i] = true;
                    searchlightBits[i+1] = true;
                    firstSearchlight[i] = true;
                    firstSearchlight[i+1] = false;
                    num2LSearchLights ++;
                    i ++;
                }
                else {
                    searchlightBits[i] = false;
                    firstSearchlight[i] = false;
                }
            }
        }
        // set up receive delay
        receiveDelay = curNode.getTransmissionDelay();
        receiveDelayField.setText(Integer.toString(receiveDelay));
		// set up pulse width
        pulseWidth = curNode.getPulseWidth();
        pulseWidthField.setText(Integer.toString(pulseWidth));
        // set up card types
        for (int i=0;i<64;i++) {
            if (curNode.isOutputCard(i)) {
                cardType[i] = rb.getString("CardTypeOutput");
            }
            else if (curNode.isInputCard(i)) {
                cardType[i] = rb.getString("CardTypeInput");
            }
            else {
                cardType[i] = rb.getString("CardTypeNone");
            }
        }
        // ensure that table displays correctly
        panel2.setVisible(false);
        panel2a.setVisible(false);
        if (nodeType==SerialNode.USIC_SUSIC) {
            panel2.setVisible(true);
        }
        else if (nodeType==SerialNode.SMINI) {
            panel2a.setVisible(true);
        }
// here insert code for other node types        
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
        if (nodeAddress < 0) return;
        // get the SerialNode corresponding to this node address
        curNode = (SerialNode) SerialTrafficController.instance().getNodeFromAddress(nodeAddress);
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
            SerialTrafficController.instance().deleteNode(nodeAddress);
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
            nodeAddrField.setVisible(true);
            nodeAddrStatic.setVisible(false);     
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
        if ( !readReceiveDelay() ) return;
		if ( !readPulseWidth() ) return;
        // check consistency of node information
        if ( !checkConsistency() ) return;
        // update node information
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
        // make node address editable again	
        nodeAddrField.setVisible(true);
        nodeAddrStatic.setVisible(false);             
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
     *    Also, the node type must be set and in 'nodeType'
     */
    void setNodeParameters() {
        // receive delay is common for all node types
        curNode.setTransmissionDelay(receiveDelay);
        // pulse width is common for all node types
        curNode.setPulseWidth(pulseWidth);
        // continue in a node specific way
        switch (nodeType) {
            case SerialNode.SMINI:
                // Note: most parameters are set by default on creation
                int numSet = 0;
                // Configure 2-lead oscillating searchlights - first clear unneeded searchlights
                for (int j=0;j<47;j++) {
                    if ( curNode.isSearchLightBit(j) ) {
                        if(!firstSearchlight[j]) {
                            // this is the first bit of a deleted searchlight - clear it
                            curNode.clear2LeadSearchLight(j);
                            // skip over the second bit of the cleared searchlight
                            j ++;
                        }
                        else {
                            // this is the first bit of a kept searchlight - skip second bit
                            j ++;
                        }
                    }
                }
                // Add needed searchlights that are not already configured    
                for (int i=0;i<47;i++) {
                    if ( firstSearchlight[i] ) {
                        if ( !curNode.isSearchLightBit(i) ) {
                            // this is the first bit of an added searchlight
                            curNode.set2LeadSearchLight(i);
                        }
                        numSet ++;
                    }
                }
                // consistency check
                if (numSet != num2LSearchLights) {
                    log.error("Inconsistent numbers of 2-lead searchlights. numSet = "+
                            Integer.toString(numSet)+", num2LSearchLights = "+
                            Integer.toString(num2LSearchLights) );
                }
                break;
            case SerialNode.USIC_SUSIC:
                // set number of bits per card
                curNode.setNumBitsPerCard(bitsPerCard);
                // configure the input/output cards
                int numInput = 0;
                int numOutput = 0;
                for (int i=0;i<64;i++) {
                    if ( "No Card".equals(cardType[i]) ) {
                        curNode.setCardTypeByAddress(i,SerialNode.NO_CARD);
                    }
                    else if ( "Input Card".equals(cardType[i]) ) {
                        curNode.setCardTypeByAddress(i,SerialNode.INPUT_CARD);
                        numInput ++;
                    }
                    else if ( "Output Card".equals(cardType[i]) ) {
                        curNode.setCardTypeByAddress(i,SerialNode.OUTPUT_CARD);
                        numOutput ++;
                    }
                    else {
                        log.error("Unexpected card type - "+cardType[i]);
                    }
                }
                // consistency check
                if ( numCards != (numOutput+numInput) ) {
                    log.error("Inconsistent numbers of cards - setNodeParameters.");
                }
                break;
// here add code for other node types
            default:
                log.error("Unexpected node type in setNodeParameters- "+
                                            Integer.toString(nodeType));
                break;
        }
        // Cause reinitialization of this Node to reflect these parameters
        SerialTrafficController.instance().initializeSerialNode(curNode);
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
            addr = Integer.parseInt(nodeAddrField.getText());
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
     * Read receive delay from window
     *    Returns 'true' if successful, 'false' if an error was detected.
     *    If an error is detected, a suitable error message is placed in the
     *        Notes area
     */
    protected boolean readReceiveDelay() {
        // get the transmission delay
        try 
        {
            receiveDelay = Integer.parseInt(receiveDelayField.getText());
        }
        catch (Exception e)
        {
            statusText1.setText(rb.getString("Error7"));
            statusText1.setVisible(true);
            receiveDelay = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (receiveDelay < 0) {
            statusText1.setText(rb.getString("Error8"));
            statusText1.setVisible(true);
            receiveDelay = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (receiveDelay > 65535) {
            statusText1.setText(rb.getString("Error9"));
            statusText1.setVisible(true);
            receiveDelay = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        // successful
        return true;
    }
    
    /**
     * Read pulse width from window
     *    Returns 'true' if successful, 'false' if an error was detected.
     *    If an error is detected, a suitable error message is placed in the
     *        Notes area
     */
    protected boolean readPulseWidth() {
        // get the pulse width
        try 
        {
            pulseWidth = Integer.parseInt(pulseWidthField.getText());
        }
        catch (Exception e)
        {
            statusText1.setText(rb.getString("Error18"));
            statusText1.setVisible(true);
            pulseWidth = 500;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (pulseWidth < 100) {
            statusText1.setText(rb.getString("Error16"));
            statusText1.setVisible(true);
            pulseWidth = 100;
			pulseWidthField.setText(Integer.toString(pulseWidth));
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (pulseWidth > 10000) {
            statusText1.setText(rb.getString("Error17"));
            statusText1.setVisible(true);
            pulseWidth = 500;
			pulseWidthField.setText(Integer.toString(pulseWidth));
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        // successful
        return true;
    }

    /**
     * Check for consistency errors by node type
     *    Returns 'true' if successful, 'false' if an error was detected.
     *    If an error is detected, a suitable error message is placed in the
     *        Notes area
     */
    protected boolean checkConsistency() {
        switch (nodeType) {
            case SerialNode.SMINI:
                // ensure that number of searchlight bits is consistent
                int numBits = 0;
                for (int i = 0;i<48;i++) {
                    if (searchlightBits[i]) numBits ++;
                }
                if ( (2*num2LSearchLights) != numBits ) {
                    statusText1.setText(rb.getString("Error10"));
                    statusText1.setVisible(true);
                    errorInStatus1 = true;
                    resetNotes2();
                    return (false);
                }
                break;
            case SerialNode.USIC_SUSIC:
                // ensure that at least one card is defined
                numCards = 0;
                boolean atNoCard = false;
                for (int i = 0; i<64 ; i++) {
                    if ( (cardType[i].equals(rb.getString("CardTypeOutput"))) || 
                            (cardType[i].equals(rb.getString("CardTypeInput"))) ) {
                        if (atNoCard) {
                            // gap error
                            statusText1.setText(rb.getString("Error11"));
                            statusText1.setVisible(true);
                            statusText2.setText(rb.getString("Error12"));
                            errorInStatus1 = true;
                            errorInStatus2 = true;
                            return (false);
                        }
                        else {
                            numCards ++;
                        }
                    }
                    else if (cardType[i].equals(rb.getString("CardTypeNone"))) {
                        atNoCard = true;
                    }
                }
                // ensure that at least one card has been defined
                if ( numCards <= 0 ) {
                    // no card error
                    statusText1.setText(rb.getString("Error13"));
                    statusText2.setText(rb.getString("Error14"));
                    statusText1.setVisible(true);
                    errorInStatus1 = true;
                    errorInStatus2 = true;
                    return (false);
                }
                // check that card size is 24 or 32 bit
                if ( (bitsPerCard!=24 ) && (bitsPerCard!=32) ) {
                    // card size error
                    statusText1.setText(rb.getString("Error15"));
                    statusText1.setVisible(true);
                    errorInStatus1 = true;
                    resetNotes2();
                    return (false);
                }
                // further checking if in Edit mode
                if (editMode) {
                    // get pre-edit numbers of cards
                    int numOutput = curNode.numOutputCards();
                    int numInput = curNode.numInputCards();
                    // will the number of cards be reduced by this edit?
                    if ( numCards<(numOutput+numInput) ) {
                        if ( javax.swing.JOptionPane.NO_OPTION == 
                                javax.swing.JOptionPane.showConfirmDialog(this,
                                rb.getString("ConfirmUpdate1")+"\n"+
                                rb.getString("ConfirmUpdate2")+"\n"+
                                rb.getString("ConfirmUpdate3"),
                                rb.getString("ConfirmUpdateTitle"),
                                    javax.swing.JOptionPane.YES_NO_OPTION,
                                        javax.swing.JOptionPane.WARNING_MESSAGE) ) {
                            // user said don't update - cancel the update
                            return (false);
                        }
                    }
                }
                break;
// here add code for other types of nodes
            default:
                log.warn("Unexpected node type - "+Integer.toString(nodeType));
                break;
        }
        return true;
    }

    /**
     * Set up table for selecting card type by address for USIC_SUSIC nodes
     */
    public class CardConfigModel extends AbstractTableModel
    {
        public String getColumnName(int c) {return cardConfigColumnNames[c];}
        public Class<?> getColumnClass(int c) {return String.class;}
        public int getColumnCount () {return 2;}
        public int getRowCount () {return 64;}
        public Object getValueAt (int r,int c) {
            if (c==0) {
                return Integer.toString(r);
            }
            else if (c==1) {
                return cardType[r];
            }
            return "";
        }
        public void setValueAt(Object type,int r,int c) {
            if (c==1) {
                cardType[r] = (String)type;
            }
        }
        public boolean isCellEditable(int r,int c) {return (c==1);}
		
        public static final int ADDRESS_COLUMN = 0;
        public static final int TYPE_COLUMN = 1;
    }
    private String[] cardConfigColumnNames = {rb.getString("HeadingCardAddress"),
                                        rb.getString("HeadingCardType")};
    private String[] cardType = new String[64];

    /**
     * Set up model for SMINI table for designating oscillating 2-lead searchlights
     */
    public class SearchlightConfigModel extends AbstractTableModel
    {
        public String getColumnName(int c) {return searchlightConfigColumnNames[c];}
        public Class<?> getColumnClass(int c) {
            if (c > 0) {
                return Boolean.class;
            }
            else {
                return String.class;
            }
        }
        public int getColumnCount () {return 9;}
        public int getRowCount () {return 6;}
        public Object getValueAt (int r,int c) {
            if (c==0) {
                switch (r) {
                    case 0:
                        return ("Card 0 Port A");
                    case 1:
                        return ("Card 0 Port B");
                    case 2:
                        return ("Card 0 Port C");
                    case 3:
                        return ("Card 1 Port A");
                    case 4:
                        return ("Card 1 Port B");
                    case 5:
                        return ("Card 1 Port C");
                    default:
                        return ("");
                }
            }
            else {
                int index = (r*8) + (c-1);
                if (searchlightBits[index]) {
                    return (Boolean.TRUE);
                }
                else {
                    return (Boolean.FALSE);
                }
            }
        }
        public void setValueAt(Object type,int r,int c) {
            if (c > 0) {
                int index = (r*8) + (c-1);
				if (!((Boolean)type).booleanValue()) {
                    searchlightBits[index] = false;
                    if (firstSearchlight[index]) {
                        searchlightBits[index+1] = false;
                        firstSearchlight[index] = false;
                    }
                    else {
                        searchlightBits[index-1] = false;
                        firstSearchlight[index-1] = false;
                    }
                    num2LSearchLights --;
                }
                else {
                    if (index<47) {
                        if (!searchlightBits[index] && !searchlightBits[index+1]) {
                            searchlightBits[index] = true;
                            searchlightBits[index+1] = true;
                            firstSearchlight[index] = true;
                            firstSearchlight[index+1] = false;
                            if (index > 0) {
                                firstSearchlight[index-1] = false;
                            }
                            num2LSearchLights ++;
                        }
                    }
                }
                panel2a.setVisible(false);
                panel2a.setVisible(true);
            }
        }
        public boolean isCellEditable(int r,int c) {return (c!=0);}

        public static final int PORT_COLUMN = 0;
    }
    private String[] searchlightConfigColumnNames = {rb.getString("HeadingPort"),
                                "0","1","2","3","4","5","6","7"};
    private boolean[] searchlightBits = new boolean[48];   // true if this bit is a searchlight bit
    private boolean[] firstSearchlight = new boolean[48];  // true if first of a pair of searchlight bits


    static Logger log = Logger.getLogger(NodeConfigFrame.class.getName());

}
