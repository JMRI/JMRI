package jmri.jmrix.cmri.serial.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user configuration of CMRI serial nodes
 *
 * @author Bob Jacobsen Copyright (C) 2004
 * @author Dave Duchamp Copyright (C) 2004
 */
@Deprecated
public class NodeConfigFrame extends jmri.util.JmriJFrame {

    protected JTextField nodeAddrField = new JTextField(3);
    protected JLabel nodeAddrStatic = new JLabel("000"); // NOI18N
    protected JComboBox<String> nodeTypeBox;
    protected JTextField receiveDelayField = new JTextField(3);
    protected JTextField pulseWidthField = new JTextField(4);
    protected JComboBox<String> cardSizeBox;
    protected JLabel cardSizeText = new JLabel("   " + Bundle.getMessage("LabelCardSize")); // NOI18N

    protected JButton addButton = new JButton(Bundle.getMessage("ButtonAdd")); // NOI18N
    protected JButton editButton = new JButton(Bundle.getMessage("ButtonEdit")); // NOI18N
    protected JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete")); // NOI18N
    protected JButton doneButton = new JButton(Bundle.getMessage("ButtonDone")); // NOI18N
    protected JButton updateButton = new JButton(Bundle.getMessage("ButtonUpdate")); // NOI18N
    protected JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel")); // NOI18N

    protected JLabel statusText1 = new JLabel();
    protected JLabel statusText2 = new JLabel();
    protected JLabel statusText3 = new JLabel();

    protected JPanel panel2 = new JPanel();
    protected JPanel panel2a = new JPanel();

    protected boolean changedNode = false;  // true if a node was changed, deleted, or added
    protected boolean editMode = false;     // true if in edit mode

    protected transient SerialNode curNode = null;    // Serial Node being editted
    protected int nodeAddress = 0;          // Node address
    protected int nodeType = SerialNode.SMINI; // Node type
    protected int bitsPerCard = 24;         // number of bits per card
    protected int receiveDelay = 0;         // transmission delay
    protected int pulseWidth = 500;   // pulse width for turnout control (milliseconds)
    protected int num2LSearchLights = 0;    // number of 2-lead oscillating searchlights

    protected int numCards = 0;             //set by consistency check routine

    protected boolean errorInStatus1 = false;
    protected boolean errorInStatus2 = false;
    protected String stdStatus1 = Bundle.getMessage("NotesStd1", Bundle.getMessage("ButtonAdd")); // NOI18N
    protected String stdStatus2 = Bundle.getMessage("NotesStd2", Bundle.getMessage("ButtonEdit")); // NOI18N
    protected String stdStatus3 = Bundle.getMessage("NotesStd3", Bundle.getMessage("ButtonDelete")); // NOI18N
    protected String editStatus1 = Bundle.getMessage("NotesEdit1"); // NOI18N
    protected String editStatus2 = Bundle.getMessage("NotesEdit2", Bundle.getMessage("ButtonUpdate")); // NOI18N
    protected String editStatus3 = Bundle.getMessage("NotesEdit3", Bundle.getMessage("ButtonCancel")); // NOI18N

    private final transient CMRISystemConnectionMemo _memo;

    public NodeConfigFrame(@Nonnull CMRISystemConnectionMemo memo) {
        super();
        _memo = memo;

        // Clear information arrays
        for (int i = 0; i < 64; i++) {
            cardType[i] = Bundle.getMessage("CardTypeNone"); // NOI18N
        }
        for (int i = 0; i < 48; i++) {
            searchlightBits[i] = false;
            firstSearchlight[i] = false;
        }
        addHelpMenu("package.jmri.jmrix.cmri.serial.nodeconfig.NodeConfigFrame", true); // NOI18N
    }

    /**
     * Initialize the config window
     */
    @Override
    public void initComponents() {
        setTitle(Bundle.getMessage("ConfigureNodesTitle"));

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // Set up node address and node type
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
        JPanel panel11 = new JPanel();
        panel11.setLayout(new FlowLayout());
        panel11.add(new JLabel(Bundle.getMessage("LabelNodeAddress") + " ")); // NOI18N
        panel11.add(nodeAddrField);
        nodeAddrField.setToolTipText(Bundle.getMessage("TipNodeAddress")); // NOI18N
        nodeAddrField.setText("0");
        panel11.add(nodeAddrStatic);
        nodeAddrStatic.setVisible(false);
        panel11.add(new JLabel("   " + Bundle.getMessage("LabelNodeType") + " ")); // NOI18N
        nodeTypeBox = new JComboBox<>();
        panel11.add(nodeTypeBox);
        nodeTypeBox.addItem("SMINI"); // NOI18N
        nodeTypeBox.addItem("USIC_SUSIC"); // NOI18N
        // Here add code for other types of nodes
        nodeTypeBox.addActionListener((java.awt.event.ActionEvent event) -> {
            String s = (String) nodeTypeBox.getSelectedItem();
            if (s.equals("SMINI")) { // NOI18N
                panel2.setVisible(false);
                panel2a.setVisible(true);
                cardSizeText.setVisible(false);
                cardSizeBox.setVisible(false);
                nodeType = SerialNode.SMINI;
            } else if (s.equals("USIC_SUSIC")) { // NOI18N
                panel2.setVisible(true);
                panel2a.setVisible(false);
                cardSizeText.setVisible(true);
                cardSizeBox.setVisible(true);
                nodeType = SerialNode.USIC_SUSIC;
            }
            // Here add code for other types of nodes
            // reset notes as appropriate
            resetNotes();
        });
        nodeTypeBox.setToolTipText(Bundle.getMessage("TipNodeType")); // NOI18N
        JPanel panel12 = new JPanel();
        panel12.setLayout(new FlowLayout());
        panel12.add(new JLabel(Bundle.getMessage("LabelDelay") + " ")); // NOI18N
        panel12.add(receiveDelayField);
        receiveDelayField.setToolTipText(Bundle.getMessage("TipDelay")); // NOI18N
        receiveDelayField.setText("0");
        panel12.add(cardSizeText);
        cardSizeBox = new JComboBox<>();
        panel12.add(cardSizeBox);
        cardSizeBox.addItem(Bundle.getMessage("CardSize24")); // NOI18N
        cardSizeBox.addItem(Bundle.getMessage("CardSize32")); // NOI18N
        // here add code for other node types, if required
        cardSizeBox.addActionListener((java.awt.event.ActionEvent event) -> {
            String s = (String) cardSizeBox.getSelectedItem();
            if (s.equals(Bundle.getMessage("CardSize24"))) { // NOI18N
                bitsPerCard = 24;
            } else if (s.equals(Bundle.getMessage("CardSize32"))) { // NOI18N
                bitsPerCard = 32;
            }
            // here add code for other node types, if required
        });
        cardSizeBox.setToolTipText(Bundle.getMessage("TipCardSize")); // NOI18N
        cardSizeText.setVisible(false);
        cardSizeBox.setVisible(false);
        JPanel panel13 = new JPanel();
        panel13.setLayout(new FlowLayout());
        panel13.add(new JLabel(Bundle.getMessage("LabelPulseWidth") + " ")); // NOI18N
        panel13.add(pulseWidthField);
        pulseWidthField.setToolTipText(Bundle.getMessage("TipPulseWidth")); // NOI18N
        pulseWidthField.setText("500");
        panel13.add(new JLabel(Bundle.getMessage("LabelMilliseconds"))); // NOI18N

        panel1.add(panel11);
        panel1.add(panel12);
        panel1.add(panel13);
        contentPane.add(panel1);

        // Set up USIC/SUSIC card type configuration table
        JPanel panel21 = new JPanel();
        panel21.setLayout(new BoxLayout(panel21, BoxLayout.Y_AXIS));
        panel21.add(new JLabel(Bundle.getMessage("HintCardTypePartA"))); // NOI18N
        panel21.add(new JLabel(" " + Bundle.getMessage("HintCardTypePartB"))); // NOI18N
        panel21.add(new JLabel(" " + Bundle.getMessage("HintCardTypePartC"))); // NOI18N
        panel21.add(new JLabel("   ")); // NOI18N
        panel21.add(new JLabel(Bundle.getMessage("HintCardTypePartD"))); // NOI18N
        panel21.add(new JLabel(" " + Bundle.getMessage("HintCardTypePartE"))); // NOI18N
        panel21.add(new JLabel(" " + Bundle.getMessage("HintCardTypePartF"))); // NOI18N
        panel2.add(panel21);
        TableModel cardConfigModel = new CardConfigModel();
        JTable cardConfigTable = new JTable(cardConfigModel);
        cardConfigTable.setRowSelectionAllowed(false);
        cardConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180, 100));

        JComboBox<String> cardTypeCombo = new JComboBox<>();
        cardTypeCombo.addItem(Bundle.getMessage("CardTypeOutput")); // NOI18N
        cardTypeCombo.addItem(Bundle.getMessage("CardTypeInput")); // NOI18N
        cardTypeCombo.addItem(Bundle.getMessage("CardTypeNone")); // NOI18N

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
        panel2.add(cardScrollPane, BorderLayout.CENTER);
        contentPane.add(panel2);
        panel2.setVisible(false);

        // Set up SMINI oscillating 2-lead searchlight configuration table
        JPanel panel2a1 = new JPanel();
        panel2a1.setLayout(new BoxLayout(panel2a1, BoxLayout.Y_AXIS));
        panel2a1.add(new JLabel(Bundle.getMessage("HintSearchlightPartA"))); // NOI18N
        panel2a1.add(new JLabel(" " + Bundle.getMessage("HintSearchlightPartB"))); // NOI18N
        panel2a1.add(new JLabel(" " + Bundle.getMessage("HintSearchlightPartC"))); // NOI18N
        panel2a1.add(new JLabel("   ")); // NOI18N
        panel2a1.add(new JLabel(Bundle.getMessage("HintSearchlightPartD"))); // NOI18N
        panel2a1.add(new JLabel(" " + Bundle.getMessage("HintSearchlightPartE"))); // NOI18N
        panel2a1.add(new JLabel(" " + Bundle.getMessage("HintSearchlightPartF"))); // NOI18N
        panel2a.add(panel2a1);
        TableModel searchlightConfigModel = new SearchlightConfigModel();
        JTable searchlightConfigTable = new JTable(searchlightConfigModel);
        searchlightConfigTable.setRowSelectionAllowed(false);
        searchlightConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(208, 100));
        TableColumnModel searchlightColumnModel = searchlightConfigTable.getColumnModel();
        TableColumn portColumn = searchlightColumnModel.getColumn(SearchlightConfigModel.PORT_COLUMN);
        portColumn.setMinWidth(90);
        portColumn.setMaxWidth(100);
        JScrollPane searchlightScrollPane = new JScrollPane(searchlightConfigTable);
        panel2a.add(searchlightScrollPane, BorderLayout.CENTER);
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
                Bundle.getMessage("BoxLabelNotes"));
        panel3.setBorder(panel3Titled);
        contentPane.add(panel3);

        // Set up buttons
        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout());
        addButton.setText(Bundle.getMessage("ButtonAdd")); // NOI18N
        addButton.setVisible(true);
        addButton.setToolTipText(Bundle.getMessage("TipAddButton")); // NOI18N
        addButton.addActionListener((java.awt.event.ActionEvent e) -> {
            addButtonActionPerformed();
        });
        panel4.add(addButton);
        editButton.setText(Bundle.getMessage("ButtonEdit")); // NOI18N
        editButton.setVisible(true);
        editButton.setToolTipText(Bundle.getMessage("TipEditButton")); // NOI18N
        panel4.add(editButton);
        editButton.addActionListener((java.awt.event.ActionEvent e) -> {
            editButtonActionPerformed();
        });
        panel4.add(deleteButton);
        deleteButton.setText(Bundle.getMessage("ButtonDelete")); // NOI18N
        deleteButton.setVisible(true);
        deleteButton.setToolTipText(Bundle.getMessage("TipDeleteButton")); // NOI18N
        panel4.add(deleteButton);
        deleteButton.addActionListener((java.awt.event.ActionEvent e) -> {
            deleteButtonActionPerformed();
        });
        panel4.add(doneButton);
        doneButton.setText(Bundle.getMessage("ButtonDone")); // NOI18N
        doneButton.setVisible(true);
        doneButton.setToolTipText(Bundle.getMessage("TipDoneButton")); // NOI18N
        panel4.add(doneButton);
        doneButton.addActionListener((java.awt.event.ActionEvent e) -> {
            doneButtonActionPerformed();
        });
        panel4.add(updateButton);
        updateButton.setText(Bundle.getMessage("ButtonUpdate")); // NOI18N
        updateButton.setVisible(true);
        updateButton.setToolTipText(Bundle.getMessage("TipUpdateButton")); // NOI18N
        panel4.add(updateButton);
        updateButton.addActionListener((java.awt.event.ActionEvent e) -> {
            updateButtonActionPerformed();
        });
        updateButton.setVisible(false);
        panel4.add(cancelButton);
        cancelButton.setText(Bundle.getMessage("ButtonCancel")); // NOI18N
        cancelButton.setVisible(true);
        cancelButton.setToolTipText(Bundle.getMessage("TipCancelButton")); // NOI18N
        panel4.add(cancelButton);
        cancelButton.addActionListener((java.awt.event.ActionEvent e) -> {
            cancelButtonActionPerformed();
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
        // error if a SerialNode corresponding to this node address exists
        if (_memo == null || _memo.getTrafficController() == null) {
            // shouldn't happen
            log.error("Not properly set up: _memo {}", _memo, new NullPointerException());
            return;
        }

        curNode = (SerialNode) _memo.getTrafficController().getNodeFromAddress(nodeAddress);
        if (curNode != null) {
            statusText1.setText(Bundle.getMessage("Error1") + Integer.toString(nodeAddress) // NOI18N
                    + Bundle.getMessage("Error2")); // NOI18N
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }

        // get node information from window
        if (!readReceiveDelay()) {
            return;
        }
        if (!readPulseWidth()) {
            return;
        }
        // check consistency of node information
        if (!checkConsistency()) {
            return;
        }
        // all ready, create the new node
        curNode = new SerialNode(nodeAddress, nodeType, _memo.getTrafficController());

        // configure the new node
        setNodeParameters();
        // register any orphan sensors that this node may have
        if (_memo.getSensorManager() != null) {
            _memo.getSensorManager().registerSensorsForNode(curNode);
        }
        // reset after succefully adding node
        resetNotes();
        changedNode = true;
        // provide user feedback
        statusText1.setText(Bundle.getMessage("FeedBackAdd") + " " // NOI18N
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
        curNode = (SerialNode) _memo.getTrafficController().getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(Bundle.getMessage("Error4")); // NOI18N
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
        if (nodeType == SerialNode.SMINI) {
            nodeTypeBox.setSelectedItem("SMINI"); // NOI18N
        } else if (nodeType == SerialNode.USIC_SUSIC) {
            nodeTypeBox.setSelectedItem("USIC_SUSIC"); // NOI18N
        }
        // here add code for other node types
        // Node specific initialization
        if (nodeType == SerialNode.USIC_SUSIC) {
            bitsPerCard = curNode.getNumBitsPerCard();
            if (bitsPerCard == 24) {
                cardSizeBox.setSelectedItem(Bundle.getMessage("CardSize24")); // NOI18N
            }
            if (bitsPerCard == 32) {
                cardSizeBox.setSelectedItem(Bundle.getMessage("CardSize32")); // NOI18N
            }
        } else if (nodeType == SerialNode.SMINI) {
            bitsPerCard = 24;
            cardSizeBox.setSelectedItem(Bundle.getMessage("CardSize24")); // NOI18N
            // set up the searchlight arrays
            num2LSearchLights = 0;
            for (int i = 0; i < 48; i++) {
                if (curNode.isSearchLightBit(i)) {
                    searchlightBits[i] = true;
                    searchlightBits[i + 1] = true;
                    firstSearchlight[i] = true;
                    firstSearchlight[i + 1] = false;
                    num2LSearchLights++;
                    i++;
                } else {
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
        for (int i = 0; i < 64; i++) {
            if (curNode.isOutputCard(i)) {
                cardType[i] = Bundle.getMessage("CardTypeOutput"); // NOI18N
            } else if (curNode.isInputCard(i)) {
                cardType[i] = Bundle.getMessage("CardTypeInput");  // NOI18N
            } else {
                cardType[i] = Bundle.getMessage("CardTypeNone");   // NOI18N
            }
        }
        // ensure that table displays correctly
        panel2.setVisible(false);
        panel2a.setVisible(false);
        if (nodeType == SerialNode.USIC_SUSIC) {
            panel2.setVisible(true);
        } else if (nodeType == SerialNode.SMINI) {
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
        if (nodeAddress < 0) {
            return;
        }
        // get the SerialNode corresponding to this node address
        curNode = (SerialNode) _memo.getTrafficController().getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(Bundle.getMessage("Error4")); // NOI18N
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // confirm deletion with the user
        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
                this, Bundle.getMessage("ConfirmDelete1") + "\n" // NOI18N
                + Bundle.getMessage("ConfirmDelete2"), Bundle.getMessage("ConfirmDeleteTitle"), // NOI18N
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE)) {
            // delete this node
            _memo.getTrafficController().deleteNode(nodeAddress);
            // provide user feedback
            resetNotes();
            statusText1.setText(Bundle.getMessage("FeedBackDelete") + " " // NOI18N
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
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("ReminderNode1") + "\n" + Bundle.getMessage("Reminder2", Bundle.getMessage("ButtonSave")), // NOI18N
                    Bundle.getMessage("ReminderTitle"), // NOI18N
                    JOptionPane.INFORMATION_MESSAGE);
        }
        setVisible(false);
        dispose();
    }

    /**
     * Handle click on the Update button.
     */
    public void updateButtonActionPerformed() {
        // get node information from window
        if (!readReceiveDelay()) {
            return;
        }
        if (!readPulseWidth()) {
            return;
        }
        // check consistency of node information
        if (!checkConsistency()) {
            return;
        }
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
        statusText1.setText(Bundle.getMessage("FeedBackUpdate") + " " // NOI18N
                + Integer.toString(nodeAddress));
        errorInStatus1 = true;
    }

    /**
     * Handle click on the Cancel button.
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
     * Handle a closing window.
     *
     * @param e the triggering event
     */
    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        doneButtonActionPerformed();
        super.windowClosing(e);
    }

    /**
     * Set node parameters. The node must exist, and be in 'curNode' Also, the
     * node type must be set and in 'nodeType'.
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
                for (int j = 0; j < 47; j++) {
                    if (curNode.isSearchLightBit(j)) {
                        if (!firstSearchlight[j]) {
                            // this is the first bit of a deleted searchlight - clear it
                            curNode.clear2LeadSearchLight(j);
                            // skip over the second bit of the cleared searchlight
                            j++;
                        } else {
                            // this is the first bit of a kept searchlight - skip second bit
                            j++;
                        }
                    }
                }
                // Add needed searchlights that are not already configured
                for (int i = 0; i < 47; i++) {
                    if (firstSearchlight[i]) {
                        if (!curNode.isSearchLightBit(i)) {
                            // this is the first bit of an added searchlight
                            curNode.set2LeadSearchLight(i);
                        }
                        numSet++;
                    }
                }
                // consistency check
                if (numSet != num2LSearchLights) {
                    log.error("Inconsistent numbers of 2-lead searchlights. numSet = {}, num2LSearchLights = {}",
                            numSet,
                            num2LSearchLights);
                }
                break;
            case SerialNode.USIC_SUSIC:
                // set number of bits per card
                curNode.setNumBitsPerCard(bitsPerCard);
                // configure the input/output cards
                int numInput = 0;
                int numOutput = 0;
                for (int i = 0; i < 64; i++) {
                    if (null == cardType[i]) {
                        log.error("Unexpected card type - {}", cardType[i]); // NOI18N
                    } else {
                        switch (cardType[i]) {
                            case "No Card": // NOI18N
                                curNode.setCardTypeByAddress(i, SerialNode.NO_CARD);
                                break;
                            case "Input Card": // NOI18N
                                curNode.setCardTypeByAddress(i, SerialNode.INPUT_CARD);
                                numInput++;
                                break;
                            case "Output Card": // NOI18N
                                curNode.setCardTypeByAddress(i, SerialNode.OUTPUT_CARD);
                                numOutput++;
                                break;
                            default:
                                log.error("Unexpected card type - {}", cardType[i]); // NOI18N
                                break;
                        }
                    }
                }
                // consistency check
                if (numCards != (numOutput + numInput)) {
                    log.error("Inconsistent numbers of cards - setNodeParameters."); // NOI18N
                }
                break;
            // here add code for other node types
            default:
                log.error("Unexpected node type in setNodeParameters- {}", nodeType); // NOI18N
                break;
        }
        // Cause reinitialization of this Node to reflect these parameters
        _memo.getTrafficController().initializeSerialNode(curNode);
    }

    /**
     * Reset the Notes error after error display.
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
     * Reset the second line of Notes area.
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
     * Read node address and check for legal range.
     *
     * @return if successful, a node address in the range 0-127 is returned. If
     *         not successful, -1 is returned and an appropriate error message
     *         is placed in statusText1.
     */
    private int readNodeAddress() {
        int addr;
        try {
            addr = Integer.parseInt(nodeAddrField.getText());
            if ((addr < 0) || (addr > 127)) {
                statusText1.setText(Bundle.getMessage("Error6"));
                statusText1.setVisible(true);
                errorInStatus1 = true;
                resetNotes2();
                addr = -1;
            }
        } catch (NumberFormatException e) {
            statusText1.setText(Bundle.getMessage("Error5"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            addr = -1;
        }
        return addr;
    }

    /**
     * Read receive delay from window. If an error is detected, a suitable error
     * message is placed in the Notes area.
     *
     * @return 'true' if successful, 'false' if an error was detected.
     */
    protected boolean readReceiveDelay() {
        // get the transmission delay
        try {
            receiveDelay = Integer.parseInt(receiveDelayField.getText());
        } catch (NumberFormatException e) {
            statusText1.setText(Bundle.getMessage("Error7")); // NOI18N
            statusText1.setVisible(true);
            receiveDelay = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (receiveDelay < 0) {
            statusText1.setText(Bundle.getMessage("Error8")); // NOI18N
            statusText1.setVisible(true);
            receiveDelay = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (receiveDelay > 65535) {
            statusText1.setText(Bundle.getMessage("Error9")); // NOI18N
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
     * Read pulse width from window. If an error is detected, a suitable error
     * message is placed in the Notes area.
     *
     * @return 'true' if successful, 'false' if an error was detected.
     */
    protected boolean readPulseWidth() {
        // get the pulse width
        try {
            pulseWidth = Integer.parseInt(pulseWidthField.getText());
        } catch (NumberFormatException e) {
            statusText1.setText(Bundle.getMessage("Error18")); // NOI18N
            statusText1.setVisible(true);
            pulseWidth = 500;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (pulseWidth < 100) {
            statusText1.setText(Bundle.getMessage("Error16")); // NOI18N
            statusText1.setVisible(true);
            pulseWidth = 100;
            pulseWidthField.setText(Integer.toString(pulseWidth));
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (pulseWidth > 10000) {
            statusText1.setText(Bundle.getMessage("Error17")); // NOI18N
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
     * Check for consistency errors by node type. If an error is detected, a
     * suitable error message is placed in the Notes area.
     *
     * @return 'true' if successful, 'false' if an error was detected.
     */
    protected boolean checkConsistency() {
        switch (nodeType) {
            case SerialNode.SMINI:
                // ensure that number of searchlight bits is consistent
                int numBits = 0;
                for (int i = 0; i < 48; i++) {
                    if (searchlightBits[i]) {
                        numBits++;
                    }
                }
                if ((2 * num2LSearchLights) != numBits) {
                    statusText1.setText(Bundle.getMessage("Error10"));
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
                for (int i = 0; i < 64; i++) {
                    if ((cardType[i].equals(Bundle.getMessage("CardTypeOutput"))) // NOI18N
                            || (cardType[i].equals(Bundle.getMessage("CardTypeInput")))) { // NOI18N
                        if (atNoCard) {
                            // gap error
                            statusText1.setText(Bundle.getMessage("Error11")); // NOI18N
                            statusText1.setVisible(true);
                            statusText2.setText(Bundle.getMessage("Error12")); // NOI18N
                            errorInStatus1 = true;
                            errorInStatus2 = true;
                            return (false);
                        } else {
                            numCards++;
                        }
                    } else if (cardType[i].equals(Bundle.getMessage("CardTypeNone"))) { // NOI18N
                        atNoCard = true;
                    }
                }
                // ensure that at least one card has been defined
                if (numCards <= 0) {
                    // no card error
                    statusText1.setText(Bundle.getMessage("Error13")); // NOI18N
                    statusText2.setText(Bundle.getMessage("Error14")); // NOI18N
                    statusText1.setVisible(true);
                    errorInStatus1 = true;
                    errorInStatus2 = true;
                    return (false);
                }
                // check that card size is 24 or 32 bit
                if ((bitsPerCard != 24) && (bitsPerCard != 32)) {
                    // card size error
                    statusText1.setText(Bundle.getMessage("Error15"));
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
                    if (numCards < (numOutput + numInput)) {
                        if (JOptionPane.NO_OPTION
                                == JOptionPane.showConfirmDialog(this,
                                        Bundle.getMessage("ConfirmUpdate1") + "\n" // NOI18N
                                        + Bundle.getMessage("ConfirmUpdate2") + "\n" // NOI18N
                                        + Bundle.getMessage("ConfirmUpdate3"), // NOI18N
                                        Bundle.getMessage("ConfirmUpdateTitle"), // NOI18N
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.WARNING_MESSAGE)) {
                            // user said don't update - cancel the update
                            return (false);
                        }
                    }
                }
                break;
            // here add code for other types of nodes
            default:
                log.warn("Unexpected node type - {}", nodeType); // NOI18N
                break;
        }
        return true;
    }

    /**
     * Set up table for selecting card type by address for USIC_SUSIC nodes.
     */
    public class CardConfigModel extends AbstractTableModel {

        @Override
        public String getColumnName(int c) {
            return cardConfigColumnNames[c];
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return 64;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (c == 0) {
                return Integer.toString(r);
            } else if (c == 1) {
                return cardType[r];
            }
            return "";
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            if (c == 1) {
                cardType[r] = (String) type;
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return (c == 1);
        }

        public static final int ADDRESS_COLUMN = 0;
        public static final int TYPE_COLUMN = 1;
    }
    private final String[] cardConfigColumnNames = {
        Bundle.getMessage("HeadingCardAddress"),
        Bundle.getMessage("HeadingCardType")
    };
    private final String[] cardType = new String[64];

    /**
     * Set up model for SMINI table for designating oscillating 2-lead
     * searchlights.
     */
    public class SearchlightConfigModel extends AbstractTableModel {

        @Override
        public String getColumnName(int c) {
            return searchlightConfigColumnNames[c];
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c > 0) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        @Override
        public int getColumnCount() {
            return 9;
        }

        @Override
        public int getRowCount() {
            return 6;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (c == 0) {
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
            } else {
                int index = (r * 8) + (c - 1);
                return searchlightBits[index];
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            if (c > 0) {
                int index = (r * 8) + (c - 1);
                if (!((Boolean) type)) {
                    searchlightBits[index] = false;
                    if (firstSearchlight[index]) {
                        searchlightBits[index + 1] = false;
                        firstSearchlight[index] = false;
                    } else {
                        searchlightBits[index - 1] = false;
                        firstSearchlight[index - 1] = false;
                    }
                    num2LSearchLights--;
                } else {
                    if (index < 47) {
                        if (!searchlightBits[index] && !searchlightBits[index + 1]) {
                            searchlightBits[index] = true;
                            searchlightBits[index + 1] = true;
                            firstSearchlight[index] = true;
                            firstSearchlight[index + 1] = false;
                            if (index > 0) {
                                firstSearchlight[index - 1] = false;
                            }
                            num2LSearchLights++;
                        }
                    }
                }
                panel2a.setVisible(false);
                panel2a.setVisible(true);
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return (c != 0);
        }

        public static final int PORT_COLUMN = 0;
    }
    private final String[] searchlightConfigColumnNames = {
        Bundle.getMessage("HeadingPort"), // NOI18N
        "0", "1", "2", "3", "4", "5", "6", "7"
    };
    private final boolean[] searchlightBits = new boolean[48];   // true if this bit is a searchlight bit
    private final boolean[] firstSearchlight = new boolean[48];  // true if first of a pair of searchlight bits

    private final static Logger log = LoggerFactory.getLogger(NodeConfigFrame.class);

}
