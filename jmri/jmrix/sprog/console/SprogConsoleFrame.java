// SprogConsoleFrame.java

package jmri.jmrix.sprog.console;

import java.awt.*;
import javax.swing.*;

import jmri.jmrix.sprog.SprogTrafficController;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogReply;
import jmri.jmrix.sprog.SprogListener;
import jmri.jmrix.sprog.SprogConstants;

/**
 * Frame for Sprog Console
 * 
 * @author			Andrew Crosland   Copyright (C) 2008
 * @version			$Revision: 1.1 $
 */
public class SprogConsoleFrame extends jmri.jmrix.AbstractMonFrame implements SprogListener {
    
    // member declarations
    protected javax.swing.JLabel cmdLabel = new javax.swing.JLabel();
    protected javax.swing.JLabel currentLabel = new javax.swing.JLabel();
    protected javax.swing.JButton sendButton = new javax.swing.JButton();
    protected javax.swing.JButton saveButton = new javax.swing.JButton();
    protected javax.swing.JTextField cmdTextField = new javax.swing.JTextField(12);
    protected javax.swing.JTextField currentTextField = new javax.swing.JTextField(12);

    protected JCheckBox ztcCheckBox = new JCheckBox();
    protected JCheckBox blueCheckBox = new JCheckBox();
    protected JCheckBox unlockCheckBox = new JCheckBox();
    
    protected ButtonGroup speedGroup = new ButtonGroup();
    protected JRadioButton speed14Button = new JRadioButton("14 step");
    protected JRadioButton speed28Button = new JRadioButton("28 step");
    protected JRadioButton speed128Button = new JRadioButton("128 step");
    
    protected int modeWord;
    protected int currentLimit = SprogConstants.DEFAULT_I;
    
    // members for handling the SPROG interface
    SprogMessage msg;
    SprogTrafficController tc = null;
    String replyString;
    int sprogMajorVersion;
    int sprogMinorVersion;
    String sprogType = null;
    String sprogUSB = null;
    String tmpString = null;
    boolean isSprogII = false;
    int state = 0;
    static final int IDLE = 0;
    static final int CRSENT = 1;                // awaiting reply to " "
    static final int QUERYSENT = 2;             // awaiting reply to "?"
    static final int CURRENTQUERYSENT = 3;      // awaiting reply to "I"
    static final int MODEQUERYSENT = 4;         // awaiting reply to "M"
    static final int CURRENTSENT = 5;           // awaiting reply to "I xxx"
    static final int MODESENT = 6;              // awaiting reply to "M xxx"
    static final int WRITESENT = 7;             // awaiting reply to "W"

    public SprogConsoleFrame() {
        super();
    }
    
    protected String title() { return "Sprog Console"; }
    
    protected void init() {
        // connect to TrafficController
        tc = SprogTrafficController.instance();
        tc.addSprogListener(this);
    }
    
    public void dispose() {
        SprogTrafficController.instance().removeSprogListener(this);
    }
    
    public void initComponents() throws Exception {
        super.initComponents();

        // Send a blank message to kick off the state machine to get the
        // currrent configuration of the attached SPROG
        msg = new SprogMessage(1);
        msg.setOpCode( (int) ' ');
        nextLine("cmd: \""+msg+"\"\n", "");
        tc.sendSprogMessage(msg, this);
        state = CRSENT;
        startShortTimer();
        
        // Add a nice border to super class
        super.jScrollPane1.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Command History"));
        
        // Let user press return to enter message
        entryField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                enterButtonActionPerformed(e);
            }
        });
        
        /*
         * Command panel
         */
        JPanel cmdPane1 = new JPanel();
        cmdPane1.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Send Command"));
        cmdPane1.setLayout(new FlowLayout());
        
        cmdLabel.setText("Command:");
        cmdLabel.setVisible(true);
        
        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Send packet");
        
        cmdTextField.setText("");
        cmdTextField.setToolTipText("Enter a SPROG command");
        cmdTextField.setMaximumSize(
                new Dimension(cmdTextField.getMaximumSize().width,
                cmdTextField.getPreferredSize().height)
                );
        
        cmdTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });
        
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });
        
        cmdPane1.add(cmdLabel);
        cmdPane1.add(cmdTextField);
        cmdPane1.add(sendButton);
        
        getContentPane().add(cmdPane1);
 
        /*
         * Address Panel
         */
        JPanel speedPanel = new JPanel();
        speedPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Speed Step Mode for SPROG Throttle"));
        speedPanel.add(speed14Button);
        speedPanel.add(speed28Button);
        speedPanel.add(speed128Button);
        speedGroup.add(speed14Button);
        speedGroup.add(speed28Button);
        speedGroup.add(speed128Button);
        speed14Button.setToolTipText("Set 14 speed steps for SPROG throttle");
        speed28Button.setToolTipText("Set 28 speed steps for SPROG throttle");
        speed128Button.setToolTipText("Set 128 speed steps for SPROG throttle");
       
        getContentPane().add(speedPanel);
        
        /*
         * Configuration panel
         */
        JPanel configPanel = new JPanel();
        configPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Configuration"));
        configPanel.setLayout(new FlowLayout());
        
        // *** Which versions support current limit ???
        currentLabel.setText("Current Limit (mA):");
        currentLabel.setVisible(true);
        
        currentTextField.setText("");
        currentTextField.setEnabled(false);
        currentTextField.setToolTipText("Enter new current limit in milliAmps (less than 1000)");
        currentTextField.setMaximumSize(
                new Dimension(currentTextField.getMaximumSize().width,
                currentTextField.getPreferredSize().height
                )
                );
         
        currentTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                validateCurrent();
            }
        });
        
        ztcCheckBox.setText("Set ZTC mode");
        ztcCheckBox.setVisible(true);
        ztcCheckBox.setToolTipText("Use this when programming older ZTC decoders");

        blueCheckBox.setText("Set Blueline mode");
        blueCheckBox.setVisible(true);
        blueCheckBox.setEnabled(false);
        blueCheckBox.setToolTipText("Use this when programming blueline decoders - programming will be slower");

        unlockCheckBox.setText("Unlock firmware");
        unlockCheckBox.setVisible(true);
        unlockCheckBox.setEnabled(false);
        unlockCheckBox.setToolTipText("Use this only if you are about to update the SPROG firmware");

        configPanel.add(currentLabel);
        configPanel.add(currentTextField);
        configPanel.add(ztcCheckBox);
        configPanel.add(blueCheckBox);
        configPanel.add(unlockCheckBox);
        
        getContentPane().add(configPanel);

        /*
         * Status Panel
         */
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Save/Load Configuration"));
        statusPanel.setLayout(new FlowLayout());
        
        saveButton.setText("Save");
        saveButton.setVisible(true);
        saveButton.setToolTipText("Save SPROG configuration (in the SPROG EEPROM)");
        
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                saveButtonActionPerformed(e);
            }
        });
        
        statusPanel.add(saveButton);
       
        getContentPane().add(statusPanel);

        // pack for display
        pack();
}
    
    // Override superclass to append return
    public void enterButtonActionPerformed(java.awt.event.ActionEvent e) {
        nextLine(entryField.getText() + "\n", "");
    }
    
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        SprogMessage m = new SprogMessage(cmdTextField.getText().length());
        for (int i=0; i<cmdTextField.getText().length(); i++)
            m.setElement(i, cmdTextField.getText().charAt(i));
        // Messages sent by us will not be forwarded back so add to display manually
        nextLine("cmd: \""+m.toString()+"\"\n", "");
        SprogTrafficController.instance().sendSprogMessage(m, this);
    }
    
    public void validateCurrent() {
        try {
            currentLimit = Integer.parseInt(currentTextField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid Current Limit Entered\n"
                    + "Please enter a value in the range 200 - 1000",
                    "SPROG Console", JOptionPane.ERROR_MESSAGE);
            currentLimit = 996;
            return;
        }
        if ((currentLimit > 1000) || (currentLimit < 200)) {
            JOptionPane.showMessageDialog(null, "Invalid Current Limit Entered\n"
                    + "Please enter a value in the range 200 - 999",
                    "SPROG Console", JOptionPane.ERROR_MESSAGE);
            currentLimit = 996;
        }
    }
    
    public void saveButtonActionPerformed(java.awt.event.ActionEvent e) {
        // Send Current Limit if possible
        state = CURRENTSENT;
        if (isCurrentLimitPossible()) {
            validateCurrent();
            // Value written is number of ADC steps 0f 4.88mV across 0.47 ohms
            currentLimit = currentLimit*470/4880;
            // Hack for SPROG bug where MSbyte of value must be non-zero
            currentLimit += 256;
            tmpString = String.valueOf(currentLimit);
            msg = new SprogMessage("I "+tmpString);
        } else {
            // Else send blank message to kicj things off
            msg = new SprogMessage(" "+tmpString);
        }
        nextLine("cmd: \""+msg.toString()+"\"\n", "");
        tc.sendSprogMessage(msg, this);
       
        // Further messages will be sent from state machine
    }
    
    public boolean isCurrentLimitPossible() {
        if (isSprogII && ((sprogMajorVersion == 1) && (sprogMinorVersion >= 6))
                          || ((sprogMajorVersion == 2) && (sprogMinorVersion >= 1))) {
            return true;
        } else
            return false;
    }
    
    public boolean isBlueLineSupportPossible() {
        if (isSprogII && ((sprogMajorVersion == 1) && (sprogMinorVersion >= 6))
                          || ((sprogMajorVersion == 2) && (sprogMinorVersion >= 1))) {
            return true;
        } else
            return false;
    }
    
    public boolean isFirmwareUnlockPossible() {
        if (isSprogII && ((sprogMajorVersion == 1) && (sprogMinorVersion >= 6))
                          || ((sprogMajorVersion == 2) && (sprogMinorVersion >= 1))) {
            return true;
        } else
            return false;
    }
    
    public synchronized void message(SprogMessage l) {  // receive a message and log it
        nextLine("cmd: \""+l.toString()+"\"\n", "");
    }
    
    public synchronized void reply(SprogReply l) {  // receive a reply message and log it
        replyString = l.toString();
        nextLine("rep: \""+replyString+"\"\n", "");
        
        // *** Check for error reply
        
        if (state == IDLE) {
            if (log.isDebugEnabled()) {
                log.debug("reply in IDLE state");
            }
            return;
        } else if (state == CRSENT) {
            stopTimer();
            if (log.isDebugEnabled()) {
                log.debug("reply in CRSENT state");
            }
            if ( (replyString.indexOf("P>")) >= 0) {
                state = QUERYSENT;
                msg = new SprogMessage(1);
                msg.setOpCode( (int) '?');
                nextLine("cmd: \""+msg+"\"\n", "");
                tc.sendSprogMessage(msg, this);
            } else {
                JOptionPane.showMessageDialog(null, "SPROG prompt not found",
                        "SPROG Version", JOptionPane.ERROR_MESSAGE);
            }
        } else if (state == QUERYSENT) {
            if (log.isDebugEnabled()) {
                log.debug("reply in QUERYSENT state");
            }
            // see if reply is from a SPROG
            if (replyString.indexOf("SPROG") < 0) {
                JOptionPane.showMessageDialog(null, "Not connected to a SPROG",
                        "SPROG Version", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    sprogMajorVersion = Integer.parseInt(replyString.substring(replyString.indexOf(".") -
                            1, replyString.indexOf(".")));
                    sprogMinorVersion = Integer.parseInt(replyString.substring(replyString.indexOf(".") +
                        1, replyString.indexOf(".") + 2));
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Cannot parse SPROG version",
                            "SPROG Console", JOptionPane.ERROR_MESSAGE);
                    state = IDLE;
                    return;
                }
                if (replyString.indexOf("II") >= 0) {
                    isSprogII = true;
                    sprogType = "SPROG II ";
                } else {
                    sprogType = "SPROG ";
                }
                if (replyString.indexOf("USB") >= 0) {
                    sprogUSB = "USB ";
                }
                // We know what we're connected to
                setTitle(title()+" - Connected to " + sprogType + sprogUSB + "v"
                        + String.valueOf(sprogMajorVersion) + "." 
                        + String.valueOf(sprogMinorVersion));
                
                // Enable blueline & firmware unlock check boxes
                if (isBlueLineSupportPossible()) {
                    blueCheckBox.setEnabled(true);
                }
                if (isFirmwareUnlockPossible()) {
                    unlockCheckBox.setEnabled(true);
                }
                
                // Get Current Limit if available
                if (isCurrentLimitPossible()) {
                    state = CURRENTQUERYSENT;
                    msg = new SprogMessage(1);
                    msg.setOpCode( (int) 'I');
                    nextLine("cmd: \""+msg+"\"\n", "");
                    tc.sendSprogMessage(msg, this);
                } else {
                    // Set default and get the mode word
                    currentLimit = (SprogConstants.DEFAULT_I*4880)/470;
                    currentTextField.setText(String.valueOf(currentLimit));
                    state = MODEQUERYSENT;
                    msg = new SprogMessage(1);
                    msg.setOpCode( (int) 'M');
                    nextLine("cmd: \""+msg+"\"\n", "");
                    tc.sendSprogMessage(msg, this);
                }
            }
        } else if (state == CURRENTQUERYSENT) {
            tmpString = new String(replyString.substring(replyString.indexOf("=") + 
                        1, replyString.indexOf("=") + 4));
            // Value returned is number of ADC steps 0f 4.88mV across 0.47 ohms
            // Convert to milliAmps using integer math
            try {
                currentLimit = (Integer.parseInt(tmpString)*4880)/470;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Malformed Reply for current limit",
                        "SPROG Console", JOptionPane.ERROR_MESSAGE);
                state = IDLE;
                return;
            }
            currentTextField.setText(String.valueOf(currentLimit));
            currentTextField.setEnabled(true);
            // Next get the mode word
            state = MODEQUERYSENT;
            msg = new SprogMessage(1);
            msg.setOpCode( (int) 'M');
            nextLine("cmd: \""+msg+"\"\n", "");
            tc.sendSprogMessage(msg, this);
        } else if (state == MODEQUERYSENT) {
            tmpString = new String(replyString.substring(replyString.indexOf("=") + 
                        2, replyString.indexOf("=") + 6));
            // Value returned is in hex
            try {
                modeWord = Integer.parseInt(tmpString, 16);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Malformed Reply for mode word",
                        "SPROG Console", JOptionPane.ERROR_MESSAGE);
                state = IDLE;
                return;
            }
            state = IDLE;
            // Set Speed step radio buttons, etc., according to mode word
            if ((modeWord & SprogConstants.STEP14_BIT) != 0) {
                speed14Button.setSelected(true);
            } else if ((modeWord & SprogConstants.STEP28_BIT) != 0) {
                speed28Button.setSelected(true);
            } else {
                speed128Button.setSelected(true);
            }
            if ((modeWord & SprogConstants.ZTC_BIT) != 0) {
                ztcCheckBox.setSelected(true);
            }
        } else if (state == CURRENTSENT) {
            // Get new mode word - assume 128 steps
            modeWord = SprogConstants.STEP128_BIT;
            if (speed14Button.isSelected()) {
                modeWord = modeWord & ~SprogConstants.STEP_MASK | SprogConstants.STEP14_BIT;
            } else if (speed28Button.isSelected()) {
                modeWord = modeWord & ~SprogConstants.STEP_MASK | SprogConstants.STEP28_BIT;
            }

            // ZTC mode
            if (ztcCheckBox.isSelected() == true) {
                modeWord = modeWord | SprogConstants.ZTC_BIT;
            }
            
            // Blueline mode
            if (blueCheckBox.isSelected() == true) {
                modeWord = modeWord | SprogConstants.BLUE_BIT;
            }
            
            // firmware unlock
            if (unlockCheckBox.isSelected() == true) {
                modeWord = modeWord | SprogConstants.UNLOCK_BIT;
            }
           
            // Send new mode word
            state = MODESENT;
            msg = new SprogMessage("M "+modeWord);
            nextLine("cmd: \""+msg.toString()+"\"\n", "");
            tc.sendSprogMessage(msg, this);
        } else if (state == MODESENT) {
            // Write to EEPROM
            state = WRITESENT;
            msg = new SprogMessage("W");
            nextLine("cmd: \""+msg.toString()+"\"\n", "");
            tc.sendSprogMessage(msg, this);
        } else if (state == WRITESENT) {
            // All done
            state = IDLE;
        }
    }
    
    /**
     * Internal routine to handle a timeout
     */
    synchronized protected void timeout() {
        JOptionPane.showMessageDialog(null, "Timeout talking to SPROG",
                    "Timeout", JOptionPane.ERROR_MESSAGE);
        state = IDLE;
    }

    protected int SHORT_TIMEOUT=500;
    
    javax.swing.Timer timer = null;
    
    /**
     * Internal routine to start timer to protect the mode-change.
     */
    protected void startShortTimer() {
        restartTimer(SHORT_TIMEOUT);
    }
    
    /**
     * Internal routine to stop timer, as all is well
     */
    protected void stopTimer() {
        if (timer!=null) timer.stop();
    }
    
    /**
     * Internal routine to handle timer starts & restarts
     */
    protected void restartTimer(int delay) {
        if (timer==null) {
            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    timeout();
                }
            });
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        timer.start();
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogConsoleFrame.class.getName());
    
}
