package jmri.jmrix.maple.nodeconfig;

import java.awt.Container;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import jmri.jmrix.maple.InputBits;
import jmri.jmrix.maple.MapleSystemConnectionMemo;
import jmri.jmrix.maple.OutputBits;
import jmri.jmrix.maple.SerialNode;

/**
 * Frame for user configuration of Maple panel nodes.
 * <p>
 * Note: Currently anything to do with pulse width for pulsing a turnout is
 * commented out. This code from the C/MRI version was not deleted in case it is
 * needed in the future.
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2008
 * @author Dave Duchamp Copyright (C) 2004, 2009
 */
public class NodeConfigFrame extends jmri.util.JmriJFrame {

    protected JSpinner nodeAddrSpinner;
    protected JLabel nodeAddrStatic = new JLabel("000");
    protected JTextField pollTimeoutField = new JTextField(3);
    protected JTextField sendDelayField = new JTextField(3);
//    protected JTextField pulseWidthField = new JTextField(4);
    protected JTextField numInputField = new JTextField(4);
    protected JTextField numOutputField = new JTextField(4);

    protected JButton addButton = new JButton(Bundle.getMessage("ButtonAdd"));
    protected JButton editButton = new JButton(Bundle.getMessage("ButtonEdit"));
    protected JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
    protected JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
    protected JButton updateButton = new JButton(Bundle.getMessage("ButtonUpdate"));
    protected JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));

    protected JLabel statusText1 = new JLabel();
    protected JLabel statusText2 = new JLabel();
    protected JLabel statusText3 = new JLabel();

    protected JPanel panel2 = new JPanel();
    protected JPanel panel2a = new JPanel();

    protected boolean changedNode = false;  // true if a node was changed, deleted, or added
    protected boolean editMode = false;     // true if in edit mode

    protected SerialNode curNode = null;    // Serial Node being editted
    protected int nodeAddress = 0;          // Node address
    protected int pollTimeoutTime = 2000;   // reply timeout time
    protected int sendDelay = 200;   // delay time after send commands
    // protected int pulseWidth = 500;   // pulse width for turnout control (milliseconds)
    protected int inputBits = 40;   // maximum number of input bits - all nodes
    protected int outputBits = 40;   // maximum number of output bits - all nodes

    protected boolean errorInStatus1 = false;
    protected boolean errorInStatus2 = false;
    protected String stdStatus1 = Bundle.getMessage("NotesStd1");
    protected String stdStatus2 = Bundle.getMessage("NotesStd2");
    protected String stdStatus3 = Bundle.getMessage("NotesStd3");
    protected String editStatus1 = Bundle.getMessage("NotesEdit1");
    protected String editStatus2 = Bundle.getMessage("NotesEdit2");
    protected String editStatus3 = Bundle.getMessage("NotesEdit3");

    private MapleSystemConnectionMemo _memo = null;

    /**
     * Constructor method
     */
    public NodeConfigFrame(MapleSystemConnectionMemo memo) {
        super();
        _memo = memo;

        addHelpMenu("package.jmri.jmrix.maple.nodeconfig.NodeConfigFrame", true);
    }

    /**
     * Initialize the config window
     */
    @Override
    public void initComponents() {
        setTitle(Bundle.getMessage("WindowTitle"));
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
        panel11.add(new JLabel(Bundle.getMessage("LabelNodeAddress") + " "));
        nodeAddrSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        panel11.add(nodeAddrSpinner);
        nodeAddrSpinner.setToolTipText(Bundle.getMessage("TipNodeAddress"));
        panel11.add(nodeAddrStatic);
        nodeAddrStatic.setVisible(false);
        JPanel panel12 = new JPanel();
        panel12.setLayout(new FlowLayout());
        panel12.add(new JLabel(Bundle.getMessage("LabelPollTimeout") + " "));
        panel12.add(pollTimeoutField);
        panel12.add(new JLabel(Bundle.getMessage("LabelMilliseconds")));
        pollTimeoutField.setToolTipText(Bundle.getMessage("TipPollTimeout"));
        pollTimeoutField.setText("" + pollTimeoutTime);
        JPanel panel120 = new JPanel();
        panel120.setLayout(new FlowLayout());
        panel120.add(new JLabel(Bundle.getMessage("LabelSendDelay") + " "));
        panel120.add(sendDelayField);
        panel120.add(new JLabel(Bundle.getMessage("LabelMilliseconds")));
        sendDelayField.setToolTipText(Bundle.getMessage("TipSendDelay"));
        sendDelayField.setText("" + sendDelay);
//        JPanel panel13 = new JPanel();
//        panel13.setLayout(new FlowLayout());
//        panel13.add(new JLabel(Bundle.getMessage("LabelPulseWidth")+" "));
//        panel13.add(pulseWidthField);
//        pulseWidthField.setToolTipText(Bundle.getMessage("TipPulseWidth"));
//        pulseWidthField.setText("500");
//        panel13.add(new JLabel(Bundle.getMessage("LabelMilliseconds")));
        JPanel panel14 = new JPanel();
        panel14.setLayout(new FlowLayout());
        panel14.add(new JLabel(Bundle.getMessage("LabelNumInputBits") + " "));
        panel14.add(numInputField);
        numInputField.setToolTipText(Bundle.getMessage("TipInputBits"));
        numInputField.setText("" + inputBits);
        JPanel panel15 = new JPanel();
        panel15.setLayout(new FlowLayout());
        panel15.add(new JLabel(Bundle.getMessage("LabelNumOutputBits") + " "));
        panel15.add(numOutputField);
        numOutputField.setToolTipText(Bundle.getMessage("TipOutputBits"));
        numOutputField.setText("" + outputBits);

        panel1.add(panel11);
        panel1.add(panel12);
        panel1.add(panel120);
//  panel1.add(panel13);
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
        contentPane.add(panel4);

        // pack for display
        pack();
    }

    /**
     * Handle Add button.
     */
    public void addButtonActionPerformed() {
        // Check that a node with this address does not exist
        int nodeAddress = readNodeAddress();
        if (nodeAddress < 0) {
            return;
        }
        // get a SerialNode corresponding to this node address if one exists
        curNode = (SerialNode) _memo.getTrafficController().getNodeFromAddress(nodeAddress);
        if (curNode != null) {
            statusText1.setText(Bundle.getMessage("Error1", Integer.toString(nodeAddress)));
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
//  if ( !readPulseWidth() ) return;
        if (!readNumInputBits()) {
            return;
        }
        if (!readNumOutputBits()) {
            return;
        }
        // all ready, create the new node
        curNode = new SerialNode(nodeAddress, 0, _memo.getTrafficController() );
        // configure the new node
        setNodeParameters();
//        // register any orphan sensors that this node may have
//        _memo.getSensorManager().registerSensorsForNode(curNode);
        // reset after succefully adding node
        resetNotes();
        changedNode = true;
        // provide user feedback
        statusText1.setText(Bundle.getMessage("FeedBackAdd") + " "
                + Integer.toString(nodeAddress));
        errorInStatus1 = true;
    }

    /**
     * Handle Edit button.
     */
    public void editButtonActionPerformed() {
        // Find Serial Node address
        nodeAddress = readNodeAddress();
        if (nodeAddress <= 0) {
            return;
        }
        // get the SerialNode corresponding to this node address
        curNode = (SerialNode) _memo.getTrafficController().getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(Bundle.getMessage("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // Set up static node address
        nodeAddrStatic.setText(Integer.toString(nodeAddress));
        nodeAddrSpinner.setVisible(false);
        nodeAddrStatic.setVisible(true);
        // set up pulse width
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
     * Handle Delete button.
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
            statusText1.setText(Bundle.getMessage("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // confirm deletion with the user
        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
                this, Bundle.getMessage("ConfirmDelete1") + " " + nodeAddress + "?",
                Bundle.getMessage("ConfirmDeleteTitle"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE)) {
            // delete this node
            _memo.getTrafficController().deleteNode(nodeAddress);
            // provide user feedback
            resetNotes();
            statusText1.setText(Bundle.getMessage("FeedBackDelete") + " "
                    + Integer.toString(nodeAddress));
            errorInStatus1 = true;
            changedNode = true;
        } else {
            // reset as needed
            resetNotes();
        }
    }

    /**
     * Handle Done button.
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
            nodeAddrSpinner.setVisible(true);
            nodeAddrStatic.setVisible(false);
        }
        if (changedNode) {
            // Remind user to Save new configuration
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("ReminderNode1") + "\n" + Bundle.getMessage("Reminder2"),
                    Bundle.getMessage("ReminderTitle"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
        setVisible(false);
        dispose();
    }

    /**
     * Handle Update button.
     */
    public void updateButtonActionPerformed() {
        // get node information from window
        if (!readPollTimeout()) {
            return;
        }
        if (!readSendDelay()) {
            return;
        }
        //  if ( !readPulseWidth() ) return;
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
        nodeAddrSpinner.setVisible(true);
        nodeAddrStatic.setVisible(false);
        // refresh notes panel
        statusText2.setText(stdStatus2);
        statusText3.setText(stdStatus3);
        // provide user feedback
        statusText1.setText(Bundle.getMessage("FeedBackUpdate") + " "
                + Integer.toString(nodeAddress));
        errorInStatus1 = true;
    }

    /**
     * Handle Cancel button.
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
        nodeAddrSpinner.setVisible(true);
        nodeAddrStatic.setVisible(false);
        // refresh notes panel
        statusText1.setText(stdStatus1);
        statusText2.setText(stdStatus2);
        statusText3.setText(stdStatus3);
    }

    /**
     * Close the window when the close box is clicked.
     */
    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        doneButtonActionPerformed();
        super.windowClosing(e);
    }

    /**
     * Set node parameters. The node must exist, and be in 'curNode'
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
     * Reset the notes error text after error display.
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
     * Range is protected by JSpinner, error dialogs removed.
     *
     * @return a node address in the range 1-99.
     */
    private int readNodeAddress() {
        return (Integer) nodeAddrSpinner.getValue();
    }

    /**
     * Read receive poll reply timeout time from window.
     * If an error is detected, a suitable error message is placed
     * in the Notes area.
     *
     * @return 'true' if successful, 'false' if an error was detected
     */
    protected boolean readPollTimeout() {
        // get the timeout time
        try {
            pollTimeoutTime = Integer.parseInt(pollTimeoutField.getText());
        } catch (Exception e) {
            statusText1.setText(Bundle.getMessage("Error7"));
            statusText1.setVisible(true);
            pollTimeoutTime = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (pollTimeoutTime <= 0) {
            statusText1.setText(Bundle.getMessage("Error8"));
            statusText1.setVisible(true);
            pollTimeoutTime = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (pollTimeoutTime > 10000) {
            statusText1.setText(Bundle.getMessage("Error9"));
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
     * Read send delay time from window.
     * If an error is detected, a suitable error message
     * is placed in the Notes area.
     *
     * @return 'true' if successful, 'false' if an error was detected
     */
    protected boolean readSendDelay() {
        // get the timeout time
        try {
            sendDelay = Integer.parseInt(sendDelayField.getText());
        } catch (Exception e) {
            statusText1.setText(Bundle.getMessage("Error19"));
            statusText1.setVisible(true);
            sendDelay = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (sendDelay < 0) {
            statusText1.setText(Bundle.getMessage("Error20"));
            statusText1.setVisible(true);
            sendDelay = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (sendDelay > 65535) {
            statusText1.setText(Bundle.getMessage("Error21"));
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
     * Read number of input bits from window.
     * If an error is detected, a suitable error message is
     * placed in the Notes area.
     *
     * @return 'true' if successful, 'false' if an error was detected
     */
    protected boolean readNumInputBits() {
        // get the input bits
        try {
            inputBits = Integer.parseInt(numInputField.getText());
        } catch (Exception e) {
            statusText1.setText(Bundle.getMessage("Error10"));
            statusText1.setVisible(true);
            inputBits = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (inputBits <= 0) {
            statusText1.setText(Bundle.getMessage("Error11"));
            statusText1.setVisible(true);
            inputBits = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (inputBits > 1000) {
            statusText1.setText(Bundle.getMessage("Error12"));
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
     * Read number of output bits from window.
     * If an error is detected, a suitable error message is placed
     * in the Notes area.
     *
     * @return 'true' if successful, 'false' if an error was detected
     */
    protected boolean readNumOutputBits() {
        // get the output bits
        try {
            outputBits = Integer.parseInt(numOutputField.getText());
        } catch (Exception e) {
            statusText1.setText(Bundle.getMessage("Error13"));
            statusText1.setVisible(true);
            outputBits = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (outputBits <= 0) {
            statusText1.setText(Bundle.getMessage("Error14"));
            statusText1.setVisible(true);
            outputBits = 0;
            errorInStatus1 = true;
            resetNotes2();
            return (false);
        }
        if (outputBits > 8000) {
            statusText1.setText(Bundle.getMessage("Error15"));
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
//     * Read pulse width from window.
//     * If an error is detected, a suitable error message is placed in the
//     * Notes area.
//     *
//     * @return 'true' if successful, 'false' if an error was detected
//     */
//    protected boolean readPulseWidth() {
//        // get the pulse width
//        try 
//        {
//            pulseWidth = Integer.parseInt(pulseWidthField.getText());
//        }
//        catch (Exception e)
//        {
//            statusText1.setText(Bundle.getMessage("Error18"));
//            statusText1.setVisible(true);
//            pulseWidth = 500;
//            errorInStatus1 = true;
//            resetNotes2();
//            return (false);
//        }
//        if (pulseWidth < 100) {
//            statusText1.setText(Bundle.getMessage("Error16"));
//            statusText1.setVisible(true);
//            pulseWidth = 100;
//   pulseWidthField.setText(Integer.toString(pulseWidth));
//            errorInStatus1 = true;
//            resetNotes2();
//            return (false);
//        }
//        if (pulseWidth > 10000) {
//            statusText1.setText(Bundle.getMessage("Error17"));
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

    // private final static Logger log = LoggerFactory.getLogger(NodeConfigFrame.class);

}
