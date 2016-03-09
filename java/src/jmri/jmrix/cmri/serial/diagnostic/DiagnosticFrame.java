// DiagnosticFrame.java
package jmri.jmrix.cmri.serial.diagnostic;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.Border;
import jmri.jmrix.cmri.serial.SerialMessage;
import jmri.jmrix.cmri.serial.SerialNode;
import jmri.jmrix.cmri.serial.SerialReply;
import jmri.jmrix.cmri.serial.SerialTrafficController;

/**
 * Frame for running CMRI diagnostics
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @version	$Revision$
 */
public class DiagnosticFrame extends jmri.util.JmriJFrame implements jmri.jmrix.cmri.serial.SerialListener {

    /**
     *
     */
    private static final long serialVersionUID = 6464613055016868125L;
    // member declarations
    protected boolean outTest = true;
    protected boolean wrapTest = false;
    protected boolean isSMINI = false;
    protected boolean isUSIC_SUSIC = true;
// Here add other node types
    protected int numOutputCards = 2;
    protected int numInputCards = 1;
    protected int numCards = 3;
    protected int ua = 0;               // node address
    protected SerialNode node;
    protected int outCardNum = 0;
    protected int obsDelay = 2000;
    protected int inCardNum = 2;
    protected int filterDelay = 0;
    // Test running variables  
    protected boolean testRunning = false;
    protected boolean testSuspended = false;  // true when Wraparound is suspended by error 
    protected byte[] outBytes = new byte[256];
    protected int curOutByte = 0;   // current output byte in output test
    protected int curOutBit = 0;    // current on bit in current output byte in output test
    protected short curOutValue = 0;  // current ofoutput byte in wraparound test
    protected int nOutBytes = 6;    // number of output bytes for all cards of this node
    protected int begOutByte = 0;   // numbering from zero, subscript in outBytes
    protected int endOutByte = 2;
    protected byte[] inBytes = new byte[256];
    protected byte[] wrapBytes = new byte[4];
    protected int nInBytes = 3;    // number of input bytes for all cards of this node
    protected int begInByte = 0;   // numbering from zero, subscript in inBytes

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC", justification = "unsync access only during initialization")
    protected int endInByte = 2;

    protected int numErrors = 0;
    protected int numIterations = 0;
    protected javax.swing.Timer outTimer;
    protected javax.swing.Timer wrapTimer;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC", justification = "unsync access only during initialization")
    protected boolean waitingOnInput = false;

    protected boolean needInputTest = false;
    protected int count = 20;
    int debugCount = 0;
    javax.swing.ButtonGroup testGroup = new javax.swing.ButtonGroup();
    javax.swing.JRadioButton outputButton = new javax.swing.JRadioButton("Output Test   ", true);
    javax.swing.JRadioButton wrapButton = new javax.swing.JRadioButton("Wraparound Test", false);

    javax.swing.JTextField uaAddrField = new javax.swing.JTextField(3);
    javax.swing.JTextField outCardField = new javax.swing.JTextField(3);
    javax.swing.JTextField inCardField = new javax.swing.JTextField(3);
    javax.swing.JTextField obsDelayField = new javax.swing.JTextField(5);
    javax.swing.JTextField filterDelayField = new javax.swing.JTextField(5);

    javax.swing.JButton runButton = new javax.swing.JButton("Run");
    javax.swing.JButton stopButton = new javax.swing.JButton("Stop");
    javax.swing.JButton continueButton = new javax.swing.JButton("Continue");

    javax.swing.JLabel statusText1 = new javax.swing.JLabel();
    javax.swing.JLabel statusText2 = new javax.swing.JLabel();

    DiagnosticFrame curFrame;

    public DiagnosticFrame() {
        super();
        curFrame = this;
    }

    public void initComponents() throws Exception {

        // set the frame's initial state
        setTitle("Run CMRI Diagnostic");
        setSize(500, 200);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // Set up the test type panel
        JPanel panel1 = new JPanel();
        testGroup.add(outputButton);
        testGroup.add(wrapButton);
        panel1.add(outputButton);
        panel1.add(wrapButton);
        Border panel1Border = BorderFactory.createEtchedBorder();
        Border panel1Titled = BorderFactory.createTitledBorder(panel1Border, "Test Type");
        panel1.setBorder(panel1Titled);
        contentPane.add(panel1);

        // Set up the test setup panel
        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
        JPanel panel21 = new JPanel();
        panel21.setLayout(new FlowLayout());
        panel21.add(new JLabel("Node(UA):"));
        panel21.add(uaAddrField);
        uaAddrField.setToolTipText("Enter node address, numbering from 0.");
        uaAddrField.setText("0");
        panel21.add(new JLabel("  Out Card:"));
        panel21.add(outCardField);
        outCardField.setToolTipText("Enter output card number, numbering from 0.");
        outCardField.setText("0");
        JPanel panel22 = new JPanel();
        panel22.setLayout(new FlowLayout());
        panel22.add(new JLabel("Output Test Only - Observation Delay:"));
        panel22.add(obsDelayField);
        obsDelayField.setToolTipText("Enter delay (milliseconds) between changes of output led's.");
        obsDelayField.setText("2000");
        JPanel panel23 = new JPanel();
        panel23.setLayout(new FlowLayout());
        panel23.add(new JLabel("Wraparound Test Only - In Card:"));
        panel23.add(inCardField);
        inCardField.setToolTipText("Enter input card number, numbering from 0.");
        inCardField.setText("2");
        panel23.add(new JLabel("   Filtering Delay:"));
        panel23.add(filterDelayField);
        filterDelayField.setToolTipText("Enter delay (milliseconds) if input card has filtering, else 0.");
        filterDelayField.setText("0");
        panel2.add(panel21);
        panel2.add(panel22);
        panel2.add(panel23);
        Border panel2Border = BorderFactory.createEtchedBorder();
        Border panel2Titled = BorderFactory.createTitledBorder(panel2Border, "Test Set Up");
        panel2.setBorder(panel2Titled);
        contentPane.add(panel2);

        // Set up the status panel
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        JPanel panel31 = new JPanel();
        panel31.setLayout(new FlowLayout());
        statusText1.setText("Please ensure test hardware is installed.");
        statusText1.setVisible(true);
        statusText1.setMaximumSize(new Dimension(statusText1.getMaximumSize().width,
                statusText1.getPreferredSize().height));
        panel31.add(statusText1);
        JPanel panel32 = new JPanel();
        panel32.setLayout(new FlowLayout());
        statusText2.setText("Select Test Type, enter Test Set Up information, then select Run below.");
        statusText2.setVisible(true);
        statusText2.setMaximumSize(new Dimension(statusText2.getMaximumSize().width,
                statusText2.getPreferredSize().height));
        panel32.add(statusText2);
        panel3.add(panel31);
        panel3.add(panel32);
        Border panel3Border = BorderFactory.createEtchedBorder();
        Border panel3Titled = BorderFactory.createTitledBorder(panel3Border, "Status");
        panel3.setBorder(panel3Titled);
        contentPane.add(panel3);

        // Set up Continue, Stop, Run buttons
        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout());
        continueButton.setText("Continue");
        continueButton.setVisible(true);
        continueButton.setToolTipText("Continue Current Test");
        continueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                continueButtonActionPerformed(e);
            }
        });
        panel4.add(continueButton);
        stopButton.setText("Stop");
        stopButton.setVisible(true);
        stopButton.setToolTipText("Stop Test");
        panel4.add(stopButton);
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                stopButtonActionPerformed(e);
            }
        });
        runButton.setText("Run");
        runButton.setVisible(true);
        runButton.setToolTipText("Run Test");
        panel4.add(runButton);
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                runButtonActionPerformed(e);
            }
        });
        contentPane.add(panel4);

        addHelpMenu("package.jmri.jmrix.cmri.serial.diagnostic.DiagnosticFrame", true);

        // pack for display
        pack();
    }

    /**
     * Method to handle run button in Diagnostic Frame
     */
    public void runButtonActionPerformed(java.awt.event.ActionEvent e) {
        // Ignore button if test is already running
        if (!testRunning) {
            // Read the user entered data, and report any errors
            if (readSetupData()) {
                if (outTest) {
                    // Initialize output test
                    if (initializeOutputTest()) {
                        // Run output test
                        runOutputTest();
                    }
                } else if (wrapTest) {
                    // Initialize wraparound test
                    if (initializeWraparoundTest()) {
                        // Run wraparound test
                        runWraparoundTest();
                    }
                }
            }
        }
    }

    /**
     * Local method to read data in Diagnostic Frame, get node data, and test
     * for consistency Returns 'true' if no errors are found Returns 'false' if
     * errors are found If errors are found, the errors are noted in the status
     * panel of the Diagnostic Frame
     */
    protected boolean readSetupData() {
        // determine test type
        outTest = outputButton.isSelected();
        wrapTest = wrapButton.isSelected();
        // read setup data - Node(UA) field
        try {
            ua = Integer.parseInt(uaAddrField.getText());
        } catch (Exception e) {
            statusText1.setText("Error - Bad character in Node(UA) field, please try again.");
            statusText1.setVisible(true);
            return (false);
        }
        if ((ua < 0) || (ua > 127)) {
            statusText1.setText("Error - Node(UA) is not between 0 and 127, please try again.");
            statusText1.setVisible(true);
            return (false);
        }
        // get the SerialNode corresponding to this node address
        node = (SerialNode) SerialTrafficController.instance().getNodeFromAddress(ua);
        if (node == null) {
            statusText1.setText("Error - Unknown address in Node(UA) field, please try again.");
            statusText1.setVisible(true);
            return (false);
        }
        // determine if node is SMINI, USIC_SUSIC, or 
        int type = node.getNodeType();
        isSMINI = (type == SerialNode.SMINI);
        isUSIC_SUSIC = (type == SerialNode.USIC_SUSIC);
// Here insert code for other type nodes
        // initialize numInputCards, numOutputCards, and numCards
        numOutputCards = node.numOutputCards();
        numInputCards = node.numInputCards();
        numCards = numOutputCards + numInputCards;

        // read setup data - Out Card field
        try {
            outCardNum = Integer.parseInt(outCardField.getText());
        } catch (Exception e) {
            statusText1.setText("Error - Bad character in Out Card field, please try again.");
            statusText1.setVisible(true);
            return (false);
        }
        // Check for consistency with Node definition
        if (isUSIC_SUSIC) {
            if ((outCardNum < 0) || (outCardNum >= numCards)) {
                statusText1.setText("Error - Out Card is not between 0 and " + Integer.toString(numCards - 1)
                        + ", please try again.");
                statusText1.setVisible(true);
                return (false);
            }
            if (!node.isOutputCard(outCardNum)) {
                statusText1.setText("Error - Out Card is not an Output Card in your Node definition, "
                        + "please try again.");
                statusText1.setVisible(true);
                return (false);
            }
        }
        if (isSMINI && ((outCardNum < 0) || (outCardNum > 1))) {
            statusText1.setText("Error - Out Card is not 0 or 1, please try again.");
            statusText1.setVisible(true);
            return (false);
        }

        if (outTest) {
            // read setup data - Observation Delay field
            try {
                obsDelay = Integer.parseInt(obsDelayField.getText());
            } catch (Exception e) {
                statusText1.setText("Error - Bad character in Observation Delay field, please try again.");
                statusText1.setVisible(true);
                return (false);
            }
        }

        if (wrapTest) {
            // read setup data - In Card field
            try {
                inCardNum = Integer.parseInt(inCardField.getText());
            } catch (Exception e) {
                statusText1.setText("Error - Bad character in In Card field, please try again.");
                statusText1.setVisible(true);
                return (false);
            }
            // Check for consistency with Node definition
            if (isUSIC_SUSIC) {
                if ((inCardNum < 0) || (inCardNum >= numCards)) {
                    statusText1.setText("Error - In Card is not between 0 and "
                            + Integer.toString(numCards - 1) + ", please try again.");
                    statusText1.setVisible(true);
                    return (false);
                }
                if (!node.isInputCard(inCardNum)) {
                    statusText1.setText("Error - In Card is not an Input Card in your Node definition, "
                            + "please try again.");
                    statusText1.setVisible(true);
                    return (false);
                }
            }
            if (isSMINI && (inCardNum != 2)) {
                statusText1.setText("Error - In Card not 2 for SMINI, please try again.");
                statusText1.setVisible(true);
                return (false);
            }

            // read setup data - Filtering Delay field
            try {
                filterDelay = Integer.parseInt(filterDelayField.getText());
            } catch (Exception e) {
                statusText1.setText("Error - Bad character in Filtering Delay field, please try again.");
                statusText1.setVisible(true);
                return (false);
            }
        }

        // complete initialization of output card
        int portsPerCard = (node.getNumBitsPerCard()) / 8;
        begOutByte = (node.getOutputCardIndex(outCardNum)) * portsPerCard;
        endOutByte = begOutByte + portsPerCard - 1;
        nOutBytes = numOutputCards * portsPerCard;
        // if wraparound test, complete initialization of the input card
        if (wrapTest) {
            begInByte = (node.getInputCardIndex(inCardNum)) * portsPerCard;
            endInByte = begInByte + portsPerCard - 1;
            nInBytes = numInputCards * portsPerCard;
        }
        return (true);
    }

    /**
     * Method to handle continue button in Diagnostic Frame
     */
    public void continueButtonActionPerformed(java.awt.event.ActionEvent e) {
        if (testRunning && testSuspended) {
            testSuspended = false;
            if (wrapTest) {
                statusText1.setText("Running Wraparound Test");
                statusText1.setVisible(true);
            }
        }
    }

    /**
     * Method to handle Stop button in Diagnostic Frame
     */
    public void stopButtonActionPerformed(java.awt.event.ActionEvent e) {
        // Ignore button push if test is not running, else change flag
        if (testRunning) {
            if (outTest) {
                stopOutputTest();
            } else if (wrapTest) {
                stopWraparoundTest();
            }
            testRunning = false;
        }
    }

    /**
     * Local Method to initialize an Output Test Returns 'true' if successfully
     * initialized Returns 'false' if errors are found If errors are found, the
     * errors are noted in the status panel of the Diagnostic Frame
     */
    protected boolean initializeOutputTest() {
        // clear all output bytes for this node
        for (int i = 0; i < nOutBytes; i++) {
            outBytes[i] = 0;
        }
        // check the entered delay--if too short an overrun could occur
        //     where the computer program is ahead of buffered serial output
        if (obsDelay < 400) {
            obsDelay = 400;
        }
        // Set up beginning LED on position
        curOutByte = begOutByte;
        curOutBit = 0;
        // Send initialization message                
        SerialTrafficController.instance().sendSerialMessage((SerialMessage) node.createInitPacket(), curFrame);
        try {
            // Wait for initialization to complete
            wait(1000);
        } catch (Exception e) {
            // Ignore exception and continue
        }
        // Initialization was successful
        numIterations = 0;
        testRunning = true;
        return (true);
    }

    /**
     * Local Method to run an Output Test
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION")
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    protected void runOutputTest() {
        // Set up timer to update output pattern periodically
        outTimer = new Timer(obsDelay, new ActionListener() {
            public void actionPerformed(ActionEvent evnt) {
                if (testRunning && outTest) {
                    short[] outBitPattern = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80};
                    String[] portID = {"A", "B", "C", "D"};
                    // set new pattern
                    outBytes[curOutByte] = (byte) outBitPattern[curOutBit];
                    // send new pattern
                    SerialMessage m = createOutPacket();
                    m.setTimeout(50);
                    SerialTrafficController.instance().sendSerialMessage(m, curFrame);
                    // update status panel to show bit that is on
                    statusText1.setText("Port " + portID[curOutByte - begOutByte] + " Bit "
                            + Integer.toString(curOutBit)
                            + " is on - Compare LED's with the pattern below");
                    statusText1.setVisible(true);
                    StringBuilder st = new StringBuilder();
                    for (int i = begOutByte; i <= endOutByte; i++) {
                        st.append("  ");
                        for (int j = 0; j < 8; j++) {
                            if ((i == curOutByte) && (j == curOutBit)) {
                                st.append("X ");
                            } else {
                                st.append("O ");
                            }
                        }
                    }
                    statusText2.setText(new String(st));
                    statusText2.setVisible(true);
                    // update bit pattern for next entry
                    curOutBit++;
                    if (curOutBit > 7) {
                        // Move to the next byte
                        curOutBit = 0;
                        outBytes[curOutByte] = 0;
                        curOutByte++;
                        if (curOutByte > endOutByte) {
                            // Pattern complete, recycle to first byte
                            curOutByte = begOutByte;
                            numIterations++;
                        }
                    }
                }
            }
        });

        // start timer        
        outTimer.start();
    }

    /**
     * Local Method to stop an Output Test
     */
    protected void stopOutputTest() {
        if (testRunning && outTest) {
            // Stop the timer
            outTimer.stop();
            // Update the status
            statusText1.setText("Output Test stopped after "
                    + Integer.toString(numIterations) + " Cycles");
            statusText1.setVisible(true);
            statusText2.setText("  ");
            statusText2.setVisible(true);
        }
    }

    /**
     * Local Method to initialize a Wraparound Test Returns 'true' if
     * successfully initialized Returns 'false' if errors are found If errors
     * are found, the errors are noted in the status panel of the Diagnostic
     * Frame
     */
    protected boolean initializeWraparoundTest() {
        // clear all output bytes for this node
        for (int i = 0; i < nOutBytes; i++) {
            outBytes[i] = 0;
        }
        // Set up beginning output values
        curOutByte = begOutByte;
        curOutValue = 0;

        // Send initialization message                
        SerialTrafficController.instance().sendSerialMessage((SerialMessage) node.createInitPacket(), curFrame);
        try {
            // Wait for initialization to complete
            wait(1000);
        } catch (Exception e) {
            // Ignore exception and continue
        }

        // Clear error count
        numErrors = 0;
        numIterations = 0;
        // Initialize running flags 
        testRunning = true;
        testSuspended = false;
        waitingOnInput = false;
        needInputTest = false;
        count = 50;
        return (true);
    }

    /**
     * Local Method to run a Wraparound Test
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION")
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    protected void runWraparoundTest() {
        // Display Status Message
        statusText1.setText("Running Wraparound Test");
        statusText1.setVisible(true);

        // Set up timer to update output pattern periodically
        wrapTimer = new Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent evnt) {
                if (testRunning && !testSuspended) {
                    if (waitingOnInput) {
                        count--;
                        if (count == 0) {
                            statusText2.setText("Time Out Error - no response after 5 seconds.");
                            statusText2.setVisible(true);
                        }
                    } else {
                        // compare input with previous output if needed
                        if (needInputTest) {
                            needInputTest = false;
                            boolean comparisonError = false;
                            // compare input and output bytes
                            int j = 0;
                            for (int i = begInByte; i <= endInByte; i++, j++) {
                                if (inBytes[i] != wrapBytes[j]) {
                                    comparisonError = true;
                                }
                            }
                            if (comparisonError) {
                                // report error and suspend test
                                statusText1.setText("Test Suspended for Error - Stop or Continue?");
                                statusText1.setVisible(true);
                                StringBuilder st = new StringBuilder("Compare Error - Out Bytes (hex):");
                                for (int i = begOutByte; i <= endOutByte; i++) {
                                    st.append(" ");
                                    st.append(Integer.toHexString((outBytes[i]) & 0x000000ff));
                                }
                                st.append("    In Bytes (hex):");
                                for (int i = begInByte; i <= endInByte; i++) {
                                    st.append(" ");
                                    st.append(Integer.toHexString((inBytes[i]) & 0x000000ff));
                                }
                                statusText2.setText(new String(st));
                                statusText2.setVisible(true);
                                numErrors++;
                                testSuspended = true;
                                return;
                            }
                        }

                        // send next output pattern
                        outBytes[curOutByte] = (byte) curOutValue;
                        if (isSMINI) {
                            // If SMINI, send same pattern to both output cards
                            if (curOutByte > 2) {
                                outBytes[curOutByte - 3] = (byte) curOutValue;
                            } else {
                                outBytes[curOutByte + 3] = (byte) curOutValue;
                            }
                        }
                        SerialMessage m = createOutPacket();
                        // wait for signal to settle down if filter delay
                        m.setTimeout(50 + filterDelay);
                        SerialTrafficController.instance().sendSerialMessage(m, curFrame);

                        // update Status area
                        short[] outBitPattern = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80};
                        String[] portID = {"A", "B", "C", "D"};
                        StringBuilder st = new StringBuilder("Port: ");
                        st.append(portID[curOutByte - begOutByte]);
                        st.append(",  Pattern: ");
                        for (int j = 0; j < 8; j++) {
                            if ((curOutValue & outBitPattern[j]) != 0) {
                                st.append("X ");
                            } else {
                                st.append("O ");
                            }
                        }
                        statusText2.setText(new String(st));
                        statusText2.setVisible(true);

                        // set up for testing input returned
                        int k = 0;
                        for (int i = begOutByte; i <= endOutByte; i++, k++) {
                            wrapBytes[k] = outBytes[i];
                        }
                        waitingOnInput = true;
                        needInputTest = true;
                        count = 50;
                        // send poll
                        SerialTrafficController.instance().sendSerialMessage(
                                SerialMessage.getPoll(ua), curFrame);

                        // update output pattern for next entry
                        curOutValue++;
                        if (curOutValue > 255) {
                            // Move to the next byte
                            curOutValue = 0;
                            outBytes[curOutByte] = 0;
                            if (isSMINI) {
                                // If SMINI, clear ports of both output cards
                                if (curOutByte > 2) {
                                    outBytes[curOutByte - 3] = 0;
                                } else {
                                    outBytes[curOutByte + 3] = 0;
                                }
                            }
                            curOutByte++;
                            if (curOutByte > endOutByte) {
                                // Pattern complete, recycle to first port (byte)
                                curOutByte = begOutByte;
                                numIterations++;
                            }
                        }
                    }
                }
            }
        });

        // start timer        
        wrapTimer.start();
    }

    /**
     * Local Method to stop a Wraparound Test
     */
    protected void stopWraparoundTest() {
        if (testRunning && wrapTest) {
            // Stop the timer
            wrapTimer.stop();
            // Update the status
            statusText1.setText("Wraparound Test Stopped, " + Integer.toString(numErrors)
                    + " Errors Found");
            statusText1.setVisible(true);
            statusText2.setText(Integer.toString(numIterations) + " Cycles Completed");
            statusText2.setVisible(true);
        }
    }

    /**
     * Local Method to create an Transmit packet (SerialMessage)
     */
    SerialMessage createOutPacket() {
        // Count the number of DLE's to be inserted
        int nDLE = 0;
        for (int i = 0; i < nOutBytes; i++) {
            if ((outBytes[i] == 2) || (outBytes[i] == 3) || (outBytes[i] == 16)) {
                nDLE++;
            }
        }
        // Create a Serial message and add initial bytes
        SerialMessage m = new SerialMessage(nOutBytes + nDLE + 2);
        m.setElement(0, ua + 65);  // node address
        m.setElement(1, 84);     // 'T'
        // Add output bytes
        int k = 2;
        for (int i = 0; i < nOutBytes; i++) {
            // perform C/MRI required DLE processing
            if ((outBytes[i] == 2) || (outBytes[i] == 3) || (outBytes[i] == 16)) {
                m.setElement(k, 16);  // DLE
                k++;
            }
            // add output byte
            m.setElement(k, outBytes[i]);
            k++;
        }
        return m;
    }

    /**
     * Message notification method to implement SerialListener interface
     */
    public void message(SerialMessage m) {
    }  // Ignore for now 

    /**
     * Reply notification method to implement SerialListener interface
     */
    public synchronized void reply(SerialReply l) {
        // Test if waiting on this input
        if (waitingOnInput && (l.isRcv()) && (ua == l.getUA())) {
            // This is a receive message for the node being tested
            for (int i = begInByte; i <= endInByte; i++) {
                // get data bytes, skipping over node address and 'R'
                inBytes[i] = (byte) l.getElement(i + 2);
            }
            waitingOnInput = false;
        }
    }

    /**
     * Stop operation when window closing
     */
    public void windowClosing(java.awt.event.WindowEvent e) {
        if (testRunning) {
            if (outTest) {
                stopOutputTest();
            } else if (wrapTest) {
                stopWraparoundTest();
            }
        }
        super.windowClosing(e);
    }
}
