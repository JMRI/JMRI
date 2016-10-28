package jmri.jmrix.maple.nodeconfig;

import java.awt.Container;
import java.awt.FlowLayout;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import jmri.jmrix.maple.InputBits;
import jmri.jmrix.maple.OutputBits;
import jmri.jmrix.maple.SerialNode;
import jmri.jmrix.maple.SerialTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user configuration of Maple panel nodes
 *
 * Note: Currently anything to do with pulse width for pulsing a turnout is
 * commented out. This code from the C/MRI version was not deleted in case it is
 * needed in the future.
 *
 * @author	Bob Jacobsen Copyright (C) 2004, 2008
 * @author	Dave Duchamp Copyright (C) 2004, 2009
 */
public class NodeConfigFrame extends jmri.util.JmriJFrame {
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.maple.nodeconfig.NodeConfigBundle");

    protected javax.swing.JTextField nodeAddrField = new javax.swing.JTextField(3);
    protected javax.swing.JLabel nodeAddrStatic = new javax.swing.JLabel("000");
    protected javax.swing.JTextField pollTimeoutField = new javax.swing.JTextField(3);
    protected javax.swing.JTextField sendDelayField = new javax.swing.JTextField(3);
//    protected javax.swing.JTextField pulseWidthField = new javax.swing.JTextField(4);
    protected javax.swing.JTextField numInputField = new javax.swing.JTextField(4);
    protected javax.swing.JTextField numOutputField = new javax.swing.JTextField(4);

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
    protected int pollTimeoutTime = 2000;   // reply timeout time
    protected int sendDelay = 200;			// delay time after send commands
//	protected int pulseWidth = 500;			// pulse width for turnout control (milliseconds)
    protected int inputBits = 40;			// maximum number of input bits - all nodes
    protected int outputBits = 40;			// maximum number of output bits - all nodes

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
        addHelpMenu("package.jmri.jmrix.maple.nodeconfig.NodeConfigFrame", true);
    }

    /**
     * Initialize the config window
     */
    public void initComponents() {
        setTitle(rb.getString("WindowTitle"));
        inputBits = InputBits.getNumInputBits();
        pollTimeoutTime = InputBits.getTimeoutTime();
        outputBits = OutputBits.getNumOutputBits();
        sendDelay = OutputBits.getSendDelay();

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // Set up node address
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
        JPanel panel11 = new JPanel();
        panel11.setLayout(new FlowLayout());
        panel11.add(new JLabel(rb.getString("LabelNodeAddress") + " "));
        panel11.add(nodeAddrField);
        nodeAddrField.setToolTipText(rb.getString("TipNodeAddress"));
        nodeAddrField.setText("1");
        panel11.add(nodeAddrStatic);
        nodeAddrStatic.setVisible(false);
        JPanel panel12 = new JPanel();
        panel12.setLayout(new FlowLayout());
        panel12.add(new JLabel(rb.getString("LabelPollTimeout") + " "));
        panel12.add(pollTimeoutField);
        panel12.add(new JLabel(rb.getString("LabelMilliseconds")));
        pollTimeoutField.setToolTipText(rb.getString("TipPollTimeout"));
        pollTimeoutField.setText("" + pollTimeoutTime);
        JPanel panel120 = new JPanel();
        panel120.setLayout(new FlowLayout());
        panel120.add(new JLabel(rb.getString("LabelSendDelay") + " "));
        panel120.add(sendDelayField);
        panel120.add(new JLabel(rb.getString("LabelMilliseconds")));
        sendDelayField.setToolTipText(rb.getString("TipSendDelay"));
        sendDelayField.setText("" + sendDelay);
//        JPanel panel13 = new JPanel();
//        panel13.setLayout(new FlowLayout());
//        panel13.add(new JLabel(rb.getString("LabelPulseWidth")+" "));
//        panel13.add(pulseWidthField);
//        pulseWidthField.setToolTipText(rb.getString("TipPulseWidth"));
//        pulseWidthField.setText("500");
//        panel13.add(new JLabel(rb.getString("LabelMilliseconds")));
        JPanel panel14 = new JPanel();
        panel14.setLayout(new FlowLayout());
        panel14.add(new JLabel(rb.getString("LabelNumInputBits") + " "));
        panel14.add(numInputField);
        numInputField.setToolTipText(rb.getString("TipInputBits"));
        numInputField.setText("" + inputBits);
        JPanel panel15 = new JPanel();
        panel15.setLayout(new FlowLayout());
        panel15.add(new JLabel(rb.getString("LabelNumOutputBits") + " "));
        panel15.add(numOutputField);
        numOutputField.setToolTipText(rb.getString("TipOutputBits"));
        numOutputField.setText("" + outputBits);

        panel1.add(panel11);
        panel1.add(panel12);
        panel1.add(panel120);
//		panel1.add(panel13);
        panel1.add(panel14);
        panel1.add(panel15);
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
        if (nodeAddress < 0) {
            return;
        }
        // get a SerialNode corresponding to this node address if one exists
        curNode = (SerialNode) SerialTrafficController.instance().getNodeFromAddress(nodeAddress);
        if (curNode != null) {
            statusText1.setText(rb.getString("Error1") + Integer.toString(nodeAddress)
                    + rb.getString("Error2"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // get node information from window
        if (!readPollTimeout()) {
            return;
        }
        if (!readSendDelay()) {
            return;
        }
//		if ( !readPulseWidth() ) return;
        if (!readNumInputBits()) {
            return;
        }
        if (!readNumOutputBits()) {
            return;
        }
        // all ready, create the new node
        curNode = new SerialNode(nodeAddress, 0);
        // configure the new node
        setNodeParameters();
//        // register any orphan sensors that this node may have
//        SerialSensorManager.instance().registerSensorsForNode(curNode);
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
        if (nodeAddress <= 0) {
            return;
        }
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
//		// set up pulse width
//        pulseWidth = curNode.getPulseWidth();
//        pulseWidthField.setText(Integer.toString(pulseWidth));
        // set up number of input and output bits
        inputBits = InputBits.getNumInputBits();
        numInputField.setText(Integer.toString(inputBits));
        outputBits = OutputBits.getNumOutputBits();
        numOutputField.setText(Integer.toString(outputBits));
        // set up poll timeout and send delay
        pollTimeoutTime = InputBits.getTimeoutTime();
        pollTimeoutField.setText(Integer.toString(pollTimeoutTime));
        sendDelay = OutputBits.getSendDelay();
        sendDelayField.setText(Integer.toString(sendDelay));
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
        curNode = (SerialNode) SerialTrafficController.instance().getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(rb.getString("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // confirm deletion with the user
        if (javax.swing.JOptionPane.OK_OPTION == javax.swing.JOptionPane.showConfirmDialog(
                this, rb.getString("ConfirmDelete1") + " " + nodeAddress + "?",
                rb.getString("ConfirmDeleteTitle"),
                javax.swing.JOptionPane.OK_CANCEL_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE)) {
            // delete this node
            SerialTrafficController.instance().deleteNode(nodeAddress);
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
        // get node information from window
        if (!readPollTimeout()) {
            return;
        }
        if (!readSendDelay()) {
            return;
        }
//		if ( !readPulseWidth() ) return;
        if (!readNumInputBits()) {
            return;
        }
        if (!readNumOutputBits()) {
            return;
        }
        // update node information
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
     * Method to close the window when the close box is clicked
     */
    public void windowClosing(java.awt.event.WindowEvent e) {
        doneButtonActionPerformed();
        super.windowClosing(e);
    }

    /**
     * Method to set node parameters The node must exist, and be in 'curNode'
     * Also, the node type must be set and in 'nodeType'
     */
    void setNodeParameters() {
        // following parameters are common for all node types
        InputBits.setTimeoutTime(pollTimeoutTime);
        OutputBits.setSendDelay(sendDelay);
        InputBits.setNumInputBits(inputBits);
        OutputBits.setNumOutputBits(outputBits);
//        curNode.setPulseWidth(pulseWidth);
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
     * in the range 1-99 is returned. If not successful, -1 is returned and an
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
        if ((addr < 1) || (addr > 99)) {
            statusText1.setText(rb.getString("Error6"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return -1;
        }
        return (addr);
    }

    /**
     * Read receive poll reply timeout time from window Returns 'true' if
     * successful, 'false' if an error was detected. If an error is detected, a
     * suitable error message is placed in the Notes area
     */
    protected boolean readPollTimeout() {
        // get the timeout time
        try {
            pollTimeoutTime = Integer.parseInt(pollTimeoutField.getText());
        } catch (Exception e) {
            statusText1.setText(rb.getString("Error7"));
            statusText1.setVisible(true);
            pollTimeoutTime = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (pollTimeoutTime <= 0) {
            statusText1.setText(rb.getString("Error8"));
            statusText1.setVisible(true);
            pollTimeoutTime = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (pollTimeoutTime > 10000) {
            statusText1.setText(rb.getString("Error9"));
            statusText1.setVisible(true);
            pollTimeoutTime = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        // successful
        return true;
    }

    /**
     * Read send delay time from window Returns 'true' if successful, 'false' if
     * an error was detected. If an error is detected, a suitable error message
     * is placed in the Notes area
     */
    protected boolean readSendDelay() {
        // get the timeout time
        try {
            sendDelay = Integer.parseInt(sendDelayField.getText());
        } catch (Exception e) {
            statusText1.setText(rb.getString("Error19"));
            statusText1.setVisible(true);
            sendDelay = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (sendDelay < 0) {
            statusText1.setText(rb.getString("Error20"));
            statusText1.setVisible(true);
            sendDelay = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (sendDelay > 65535) {
            statusText1.setText(rb.getString("Error21"));
            statusText1.setVisible(true);
            sendDelay = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        // successful
        return true;
    }

    /**
     * Read number of input bits from window Returns 'true' if successful,
     * 'false' if an error was detected. If an error is detected, a suitable
     * error message is placed in the Notes area
     */
    protected boolean readNumInputBits() {
        // get the input bits
        try {
            inputBits = Integer.parseInt(numInputField.getText());
        } catch (Exception e) {
            statusText1.setText(rb.getString("Error10"));
            statusText1.setVisible(true);
            inputBits = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (inputBits <= 0) {
            statusText1.setText(rb.getString("Error11"));
            statusText1.setVisible(true);
            inputBits = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (inputBits > 1000) {
            statusText1.setText(rb.getString("Error12"));
            statusText1.setVisible(true);
            inputBits = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        // successful
        return true;
    }

    /**
     * Read number of output bits from window Returns 'true' if successful,
     * 'false' if an error was detected. If an error is detected, a suitable
     * error message is placed in the Notes area
     */
    protected boolean readNumOutputBits() {
        // get the output bits
        try {
            outputBits = Integer.parseInt(numOutputField.getText());
        } catch (Exception e) {
            statusText1.setText(rb.getString("Error13"));
            statusText1.setVisible(true);
            outputBits = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (outputBits <= 0) {
            statusText1.setText(rb.getString("Error14"));
            statusText1.setVisible(true);
            outputBits = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (outputBits > 8000) {
            statusText1.setText(rb.getString("Error15"));
            statusText1.setVisible(true);
            outputBits = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        // successful
        return true;
    }

//    /**
//     * Read pulse width from window
//     *    Returns 'true' if successful, 'false' if an error was detected.
//     *    If an error is detected, a suitable error message is placed in the
//     *        Notes area
//     */
//    protected boolean readPulseWidth() {
//        // get the pulse width
//        try 
//        {
//            pulseWidth = Integer.parseInt(pulseWidthField.getText());
//        }
//        catch (Exception e)
//        {
//            statusText1.setText(rb.getString("Error18"));
//            statusText1.setVisible(true);
//            pulseWidth = 500;
//            errorInStatus1 = true;
//            resetNotes2();
//            return (false);
//        }
//        if (pulseWidth < 100) {
//            statusText1.setText(rb.getString("Error16"));
//            statusText1.setVisible(true);
//            pulseWidth = 100;
//			pulseWidthField.setText(Integer.toString(pulseWidth));
//            errorInStatus1 = true;
//            resetNotes2();
//            return (false);
//        }
//        if (pulseWidth > 10000) {
//            statusText1.setText(rb.getString("Error17"));
//            statusText1.setVisible(true);
//            pulseWidth = 500;
//            pulseWidthField.setText(Integer.toString(pulseWidth));
//            errorInStatus1 = true;
//            resetNotes2();
//            return (false);
//        }
//        // successful
//        return true;
//    }
    private final static Logger log = LoggerFactory.getLogger(NodeConfigFrame.class.getName());

}
