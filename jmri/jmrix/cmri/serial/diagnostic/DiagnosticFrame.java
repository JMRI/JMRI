// DiagnosticFrame.java

package jmri.jmrix.cmri.serial.diagnostic;

import jmri.util.StringUtil;
import jmri.jmrix.cmri.serial.SerialMessage;
import jmri.jmrix.cmri.serial.SerialReply;
import jmri.jmrix.cmri.serial.SerialTrafficController;

import java.awt.*;

import javax.swing.border.Border;
import javax.swing.*;

/**
 * Frame for running CMRI diagnostics
 * @author	Dave Duchamp   Copyright (C) 2004
 * @version	$Revision: 1.1 $
 */
public class DiagnosticFrame extends javax.swing.JFrame implements jmri.jmrix.cmri.serial.SerialListener {

    // member declarations
   boolean outTest = true;
   boolean wrapTest = false;     
     
    javax.swing.ButtonGroup testGroup = new javax.swing.ButtonGroup();
    javax.swing.JRadioButton outputButton = new javax.swing.JRadioButton("Output Test   ",true);
    javax.swing.JRadioButton wrapButton = new javax.swing.JRadioButton("Wraparound Test",false);

    javax.swing.JTextField uaAddrField = new javax.swing.JTextField(3);
    javax.swing.JTextField outCard = new javax.swing.JTextField(3);
    javax.swing.JTextField inCard = new javax.swing.JTextField(3);
    javax.swing.JTextField obsDelay = new javax.swing.JTextField(5);
    javax.swing.JTextField filterDelay = new javax.swing.JTextField(5);

    javax.swing.JButton runButton = new javax.swing.JButton("Run");
    javax.swing.JButton stopButton = new javax.swing.JButton("Stop");
    javax.swing.JButton continueButton = new javax.swing.JButton("Continue");
    
    javax.swing.JLabel statusText1 = new javax.swing.JLabel();
    javax.swing.JLabel statusText2 = new javax.swing.JLabel();

    public DiagnosticFrame() {
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
        panel21.add(outCard);
        outCard.setToolTipText("Enter output card number, numbering from 0.");
        outCard.setText("0");
        JPanel panel22 = new JPanel();
        panel22.setLayout(new FlowLayout());
        panel22.add(new JLabel("Output Test Only - Observation Delay:"));
        panel22.add(obsDelay);
        obsDelay.setToolTipText("Enter delay between changes of output led's.");
        obsDelay.setText("500");        
        JPanel panel23 = new JPanel();
        panel23.setLayout(new FlowLayout());
        panel23.add(new JLabel("Wraparound Test Only - In Card:"));
        panel23.add(inCard);
        inCard.setToolTipText("Enter input card number, numbering from 0.");
        inCard.setText("2");
        panel23.add(new JLabel("   Filtering Delay:"));
        panel23.add(filterDelay);
        filterDelay.setToolTipText("Enter delay if input card has filtering, else 0.");
        filterDelay.setText("0");        
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
     * Method to handle continue button in Diagnostic Frame
     */        
    public void runButtonActionPerformed(java.awt.event.ActionEvent e) {
        // Read the user entered data, and report any errors
        if ( !readSetupData() ) {
            // Here if errors were noted
            return;
        }
        
//        SerialTrafficController.instance().sendSerialMessage(createPacket(packetTextField.getText()), this);
    }
    
    /**
     * Local method to read data in Diagnostic Frame
     *    Returns 'true' if no errors are found
     *    Returns 'false' if errors are found
     *    If errors are found, the errors are noted in 
     *      the status panel of the Diagnostic Frame
     */        
    protected boolean readSetupData() {
        // determine test type
        outTest = outputButton.isSelected();
        wrapTest = wrapButton.isSelected();
        
// debugging
        if (outTest) {
            statusText1.setText("Output Test");
            statusText1.setVisible(true);
        }
        else if (wrapTest) {
            statusText1.setText("Wraparound Test");
            statusText1.setVisible(true);
        }
// end debugging            
        return (true);
    }

    /**
     * Method to handle continue button in Diagnostic Frame
     */        
    public void continueButtonActionPerformed(java.awt.event.ActionEvent e) {
        statusText1.setText("Continue button");
        statusText1.setVisible(true);
//        SerialTrafficController.instance().sendSerialMessage(createPacket(packetTextField.getText()), this);
    }

    /**
     * Method to handle stop button in Diagnostic Frame
     */
    public void stopButtonActionPerformed(java.awt.event.ActionEvent e) {
//        SerialTrafficController.instance().sendSerialMessage(createPacket(packetTextField.getText()), this);
    }

    SerialMessage createPacket(String s) {
        // gather bytes in result
        byte b[] = StringUtil.bytesFromHexString(s);
        if (b.length == 0) return null;  // no such thing as a zero-length message
        SerialMessage m = new SerialMessage(b.length);
        for (int i=0; i<b.length; i++) m.setElement(i, b[i]);
        return m;
    }

    public void  message(SerialMessage m) {}  // ignore replies
    public void  reply(SerialReply r) {} // ignore replies

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
        setVisible(false);
        dispose();
    }
}
