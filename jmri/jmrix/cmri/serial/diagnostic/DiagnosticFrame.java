// DiagnosticFrame.java

package jmri.jmrix.cmri.serial.diagnostic;

import jmri.util.StringUtil;
import jmri.jmrix.cmri.serial.SerialMessage;
import jmri.jmrix.cmri.serial.SerialReply;
import jmri.jmrix.cmri.serial.SerialTrafficController;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.border.Border;
import javax.swing.*;

import java.lang.Integer;

/**
 * Frame for running CMRI diagnostics
 * @author	Dave Duchamp   Copyright (C) 2004
 * @version	$Revision: 1.2 $
 */
public class DiagnosticFrame extends javax.swing.JFrame implements jmri.jmrix.cmri.serial.SerialListener {

    // member declarations
    protected boolean outTest = true;
    protected boolean wrapTest = false;
    protected boolean isSMINI = false;
    protected boolean isUSIC_SUSIC = true;
    protected int numOutputCards = 2;
    protected int numInputCards = 1;
    protected int numCards = 3;
    protected int ua = 0;
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
    protected int nOutBytes = 6;    // number of output bytes for all cards of this node
    protected int begOutByte = 0;   // numbering from zero, subscript in outBytes
    protected int endOutByte = 2;
    protected byte[] inBytes = new byte[256];
    protected int nInBytes = 3;    // number of input bytes for all cards of this node
    protected int begInByte = 0;   // numbering from zero, subscript in inBytes
    protected int endInByte = 2;
    protected int numErrors = 0;
    protected int numIterations = 0;    
    protected javax.swing.Timer outTimer;  
    protected javax.swing.Timer wrapTimer;
    protected boolean waitingOnInput = false;  
    protected boolean inputHere = false;
    int debugCount = 0;
    javax.swing.ButtonGroup testGroup = new javax.swing.ButtonGroup();
    javax.swing.JRadioButton outputButton = new javax.swing.JRadioButton("Output Test   ",true);
    javax.swing.JRadioButton wrapButton = new javax.swing.JRadioButton("Wraparound Test",false);

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
        curFrame = this;
    }

    public void initComponents() throws Exception {

        // set the frame's initial state
        setTitle("Run CMRI Diagnostic");
        setSize(500,200);
        Container contentPane = getContentPane();        
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // Set up the test type panel
        JPanel panel1 = new JPanel();
        testGroup.add(outputButton);
        testGroup.add(wrapButton);
        panel1.add(outputButton);
        panel1.add(wrapButton);
        Border panel1Border = BorderFactory.createEtchedBorder();
        Border panel1Titled = BorderFactory.createTitledBorder(panel1Border,"Test Type");
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
        Border panel2Titled = BorderFactory.createTitledBorder(panel2Border,"Test Set Up");
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
                            statusText1.getPreferredSize().height) );
        panel31.add(statusText1);
        JPanel panel32 = new JPanel();
        panel32.setLayout(new FlowLayout());
        statusText2.setText("Select Test Type, enter Test Set Up information, then select Run below.");
        statusText2.setVisible(true);
        statusText2.setMaximumSize(new Dimension(statusText2.getMaximumSize().width,
                            statusText2.getPreferredSize().height) );
        panel32.add(statusText2);
        panel3.add(panel31);
        panel3.add(panel32);
        Border panel3Border = BorderFactory.createEtchedBorder();
        Border panel3Titled = BorderFactory.createTitledBorder(panel3Border,"Status");
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

        // add window listener
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                thisWindowClosing(e);
            }
        });

        // pack for display
        pack();
    }

//    public void pollButtonActionPerformed(java.awt.event.ActionEvent e) {
//        SerialMessage msg = SerialMessage.getPoll(Integer.valueOf(uaAddrField.getText()).intValue());
//        SerialTrafficController.instance().sendSerialMessage(msg, this);
//    }

//    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
//        SerialTrafficController.instance().sendSerialMessage(createPacket(packetTextField.getText()), this);
//    }

    /**
     * Method to handle run button in Diagnostic Frame
     */        
    public void runButtonActionPerformed(java.awt.event.ActionEvent e) {
        // Ignore button if test is already running
        if (!testRunning) {
            // Read the user entered data, and report any errors
            if ( readSetupData() ) {
                if (outTest) {
                    // Initialize output test
                    if (initializeOutputTest()) {
                        // Run output test
                        runOutputTest();
                    }
                }
                else if (wrapTest) {
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
     * Local method to read data in Diagnostic Frame, get node data, and test for consistency
     *    Returns 'true' if no errors are found
     *    Returns 'false' if errors are found
     *    If errors are found, the errors are noted in 
     *      the status panel of the Diagnostic Frame
     */        
    protected boolean readSetupData() {
        // determine test type
        outTest = outputButton.isSelected();
        wrapTest = wrapButton.isSelected();
        // read setup data - Node(UA) field
        try 
        {
            ua = Integer.parseInt(uaAddrField.getText());
        }
        catch (Exception e)
        {
            statusText1.setText("Error - Bad character in Node(UA) field, please try again.");
            statusText1.setVisible(true);
            return (false);
        }
        if ( (ua < 0) || (ua > 127) ) {
            statusText1.setText("Error - Node(UA) is not between 0 and 127, please try again.");
            statusText1.setVisible(true);
            return (false);
        }
        // determine if node is SMINI
// here insert code to find the node and the node type and
//    initialize numInputCards, numOutputCards, and numCards

        // read setup data - Out Card field
        try 
        {
            outCardNum = Integer.parseInt(outCardField.getText());
        }
        catch (Exception e)
        {
            statusText1.setText("Error - Bad character in Out Card field, please try again.");
            statusText1.setVisible(true);
            return (false);
        }
        if ( isUSIC_SUSIC ) {
            if ( (outCardNum < 0) || (outCardNum >= numCards) ) {
                statusText1.setText("Error - Out Card is not between 0 and "+Integer.toString(numCards-1)+
                                                                        ", please try again.");
                statusText1.setVisible(true);
                return (false);
            }
// Here insert test to make sure outCardNum refers to an output card
        }
        if (isSMINI && ( (outCardNum < 0) || (outCardNum > 1) ) ) {
            statusText1.setText("Error - Out Card is not 0 or 1, please try again.");
            statusText1.setVisible(true);
            return (false);
        }

        if (outTest) {
            // read setup data - Observation Delay field
            try 
            {
                obsDelay = Integer.parseInt(obsDelayField.getText());
            }
            catch (Exception e)
            {
                statusText1.setText("Error - Bad character in Observation Delay field, please try again.");
                statusText1.setVisible(true);
                return (false);
            }
        }

        if (wrapTest) {
            // read setup data - In Card field
            try 
            {
                inCardNum = Integer.parseInt(inCardField.getText());
            }
            catch (Exception e)
            {
                statusText1.setText("Error - Bad character in In Card field, please try again.");
                statusText1.setVisible(true);
                return (false);
            }
            if (isUSIC_SUSIC) {
                if ( (inCardNum < 0) || (inCardNum >= numCards) )  {
                    statusText1.setText("Error - In Card is not between 0 and "+
                                            Integer.toString(numCards-1)+", please try again.");
                    statusText1.setVisible(true);
                    return (false);
                }
// Here add test if inCardNum is a defined input card for node
            }
            if (isSMINI && (inCardNum != 2) ) {
                statusText1.setText("Error - In Card not 2 for SMINI, assumed 2 and continued.");
                statusText1.setVisible(true);
                inCardNum = 2;
            }

            // read setup data - Filtering Delay field
            try 
            {
                filterDelay = Integer.parseInt(filterDelayField.getText());
            }
            catch (Exception e)
            {
                statusText1.setText("Error - Bad character in Filtering Delay field, please try again.");
                statusText1.setVisible(true);
                return (false);
            }
        }
        
// debugging
        if (outTest) {
            statusText1.setText("Output: UA = "+Integer.toString(ua)+", Out Card = "+
                    Integer.toString(outCardNum)+", Obs Delay = "+Integer.toString(obsDelay));
            statusText1.setVisible(true);
        }
        else if (wrapTest) {
            statusText1.setText("Wraparound: UA = "+Integer.toString(ua)+", Out Card = "+
                    Integer.toString(outCardNum)+", In Card = "+Integer.toString(inCardNum)+
                    ", Filtering Delay = "+Integer.toString(filterDelay));
            statusText1.setVisible(true);
        }
        try {
            wait (2000);
        }
        catch (Exception e) {
            // ignore exception
        }
// end debugging            
        return (true);
    }

    /**
     * Method to handle continue button in Diagnostic Frame
     */        
    public void continueButtonActionPerformed(java.awt.event.ActionEvent e) {
        if (testRunning && testSuspended) {
            testSuspended = false;
        }statusText1.setText("Continue button");
    }

    /**
     * Method to handle Stop button in Diagnostic Frame
     */
    public void stopButtonActionPerformed(java.awt.event.ActionEvent e) {
        // Ignore button push if test is not running, else change flag
        if (testRunning) {
            if (outTest) {
                stopOutputTest();
            }
            else if (wrapTest) {
                stopWraparoundTest();
            }
            testRunning = false;
        }
   }
    
    /**
     * Local Method to initialize an Output Test
     *    Returns 'true' if successfully initialized
     *    Returns 'false' if errors are found
     *    If errors are found, the errors are noted in 
     *      the status panel of the Diagnostic Frame
     */
    protected boolean initializeOutputTest() {
        // clear all output bytes for this node
        for (int i=0;i<nOutBytes;i++) {
            outBytes[i] = 0;
        }
        // check the entered delay--if too short an overrun could occur
        //     where the computer program is ahead of buffered serial output
        if (obsDelay<2000) obsDelay = 2000;
        // determine the beginning and ending output bytes for this output card
    
        // Set up beginning LED on position
        curOutByte = begOutByte;
        curOutBit = 0;
        // Send initialization message                
//        SerialTrafficController.instance().sendSerialMessage(createInitPacket(),curFrame);
        try {
            // Wait for initialization to complete
            wait (1000);    
        }
        catch (Exception e) {
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
    protected void runOutputTest() {
        // Set up timer to update output pattern periodically
        outTimer = new Timer(obsDelay,new ActionListener() {
            public void actionPerformed(ActionEvent evnt) 
            {
                if (testRunning && outTest) {
                    short[] outBitPattern = {0x01,0x02,0x04,0x08,0x10,0x20,0x40,0x80};
                    String[] portID = {"A","B","C","D"};
                    // set new pattern
                    outBytes[curOutByte] = (byte)outBitPattern[curOutBit];                    
                    // send new pattern
                    SerialTrafficController.instance().sendSerialMessage(
                                                        createOutPacket(),curFrame);
                    // update status panel to show bit that is on
                    statusText1.setText("Port "+portID[curOutByte-begOutByte]+" Bit "+
                            Integer.toString(curOutBit)+
                                    " is on - Compare LED's with the pattern below");
                    statusText1.setVisible(true);
                    String st = "";
                    for (int i = begOutByte;i<=endOutByte;i++) {
                        st = st + "  ";
                        for (int j = 0;j<8;j++) {
                            if ( (i==curOutByte) && (j==curOutBit) )
                                st = st + "X ";
                            else
                                st = st + "O ";
                        }
                    }                        
                    statusText2.setText(st);
                    statusText2.setVisible(true);                        
                    // update bit pattern for next entry
                    curOutBit ++;
                    if (curOutBit>7) {
                        // Move to the next byte
                        curOutBit = 0;
                        outBytes[curOutByte] = 0;
                        curOutByte ++;
                        if (curOutByte>endOutByte) {
                            // Pattern complete, recycle to first byte
                            curOutByte = begOutByte;
                            numIterations ++;
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
            statusText1.setText("Output Test stopped after "+
                                    Integer.toString(numIterations)+" Cycles");
            statusText1.setVisible(true);
            statusText2.setText("  ");
            statusText2.setVisible(true);
        }
    }
    
    /**
     * Local Method to initialize a Wraparound Test
     *    Returns 'true' if successfully initialized
     *    Returns 'false' if errors are found
     *    If errors are found, the errors are noted in 
     *      the status panel of the Diagnostic Frame
     */
    protected boolean initializeWraparoundTest() {
        // Set up beginning
        
        // Clear error count
        numErrors = 0;
        // Send initialization message                
//        SerialTrafficController.instance().sendSerialMessage(createPacket(packetTextField.getText()), this);
        // Initialization was successful
        testRunning = true;
        return (true);
    }
    
    /**
     * Local Method to run a Wraparound Test
     */
    protected void runWraparoundTest() {
        // Display Status Message
        statusText1.setText("Running Wraparound Test");
        statusText1.setVisible(true);
        
        // Set up timer to update output pattern periodically
        wrapTimer = new Timer(2000,new ActionListener() {
            public void actionPerformed(ActionEvent evnt) 
            {
                if (testRunning && !testSuspended) {
                    if (waitingOnInput) {
                        // poll for next input
                        
                        if (inputHere) {
                            waitingOnInput = false;
                            inputHere = false;
                            // Compare actual and expected
                            
                            // Send message and suspend if error
                        }
                    }
                    
                    if (!waitingOnInput) {
                        // ready to send next test pattern
                        
                        // increment pattern
// debugging
                        debugCount ++;
                        // fake an error every 200 counts
                        if ( (debugCount%200) == 0) {
                            testSuspended = true;
                            numErrors ++;                            
                        }
// end debugging
                    }
                        
                    // send new output pattern
                    
// debugging
                    // update status panel
                    statusText2.setText(Integer.toString(debugCount));
                    statusText2.setVisible(true); 
// end debugging
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
            statusText1.setText("Wraparound Test Stopped, "+Integer.toString(numErrors)+
                                                                " Errors Found");
            statusText1.setVisible(true);
            statusText2.setText("  ");
            statusText2.setVisible(true);
        }
    }
    
    /**
     * Local Method to create an Transmit packet (SerialMessage)
     */
    SerialMessage createOutPacket() {
        // Count the number of DLE's to be inserted
        int nDLE = 0;
        for (int i=0; i<nOutBytes; i++) {
            if ( (outBytes[i]==2) || (outBytes[i]==3) ||(outBytes[i]==16) ) 
                nDLE ++;
        }
        // Create a Serial message and add initial bytes
        SerialMessage m = new SerialMessage(nOutBytes + nDLE + 2);
        m.setElement(0,ua+65);  // node address
        m.setElement(1,84);     // 'T'
        // Add output bytes
        int k = 2;
        for (int i=0; i<nOutBytes; i++) {
            // perform C/MRI required DLE processing
            if ( (outBytes[i]==2) || (outBytes[i]==3) ||(outBytes[i]==16) ) {
                m.setElement(k,16);  // DLE
                k ++;
            }
            // add output byte
            m.setElement(k, outBytes[i]);
            k ++;
        }
        return m;
    }

    /**
     * Message notification method to implement SerialListener interface
     */
    public void  message(SerialMessage m) {}  // Ignore for now

    /**
     * Reply notification method to implement SerialListener interface
     */
    public void  reply(SerialReply r) {
        if (r.isRcv()) {
            // This is a receive message
            if (ua == r.getUA()) {
                // This is for the node being tested, get input
                
                // Set waiting over
                waitingOnInput = false;
            }
        }
    }

    private boolean mShown = false;

    public void addNotify() {
        super.addNotify();

        if (mShown)
            return;

        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }

        mShown = true;
    }

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        if (testRunning) {
            if (outTest) {
                stopOutputTest();
            }
            else if (wrapTest) {
                stopWraparoundTest();
            }
        }
        setVisible(false);
        dispose();
    }
}
