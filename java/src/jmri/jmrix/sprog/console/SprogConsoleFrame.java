// SprogConsoleFrame.java

package jmri.jmrix.sprog.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;
import javax.swing.*;

import jmri.jmrix.sprog.SprogCommandStation;
import jmri.jmrix.sprog.SprogTrafficController;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogReply;
import jmri.jmrix.sprog.SprogListener;
import jmri.jmrix.sprog.SprogConstants;
import jmri.jmrix.sprog.update.*;

/**
 * Frame for Sprog Console
 * 
 * Updated Jan 2010 by Andrew Berridge - fixed errors caused by trying
 * to send some commands while slot manager is active
 * 
 * Refactored
 * 
 * @author			Andrew Crosland   Copyright (C) 2008
 * @version			$Revision$
 */
public class SprogConsoleFrame extends jmri.jmrix.AbstractMonFrame implements SprogListener, SprogVersionListener {
    
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
    SprogTrafficController tc = null;
    SprogCommandStation sm = null;
    SprogMessage msg;
    String replyString;
    String tmpString = null;
    State state = State.IDLE;

    SprogVersion sv;
    
    enum State { IDLE, 
    	CURRENTQUERYSENT, 	// awaiting reply to "I"
    	MODEQUERYSENT, 		// awaiting reply to "M"
    	CURRENTSENT, 		// awaiting reply to "I xxx"
    	MODESENT, 			// awaiting reply to "M xxx"
    	WRITESENT   		// awaiting reply to "W"
    }
    
    /*static final int IDLE = 0;
    static final int CRSENT = 1;                // awaiting reply to " "
    static final int QUERYSENT = 2;             // awaiting reply to "?"
    static final int CURRENTQUERYSENT = 3;      // awaiting reply to "I"
    static final int MODEQUERYSENT = 4;         // awaiting reply to "M"
    static final int CURRENTSENT = 5;           // awaiting reply to "I xxx"
    static final int MODESENT = 6;              // awaiting reply to "M xxx"
    static final int WRITESENT = 7;             // awaiting reply to "W"
*/
    public SprogConsoleFrame() {
        super();
    }
    
    protected String title() { return "Sprog Console"; }
    
    protected void init() {
        // connect to TrafficController
        tc = SprogTrafficController.instance();
        // connect to SlotManager (it is only used in Command Station mode)
        sm = SprogCommandStation.instance();
        tc.addSprogListener(this);
    }
    
    public void dispose() {
        SprogTrafficController.instance().removeSprogListener(this);
        super.dispose();
    }
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="IS2_INCONSISTENT_SYNC")
    // Ignore unsynchronized access to state
    public void initComponents() throws Exception {
        //SprogMessage msg;
        super.initComponents();

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
         
//        currentTextField.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent e) {
//                validateCurrent();
//            }
//        });
        
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

        // Now the GUI is all setup we can get the SPROG version
        // Only send this if we have zero active slots in slot manager
        // in command mode

        if (sm.getInUseCount() == 0) {
            SprogVersionQuery.requestVersion(this);
        }
}
    
    /**
     * Define help menu for this window.
     * <p>
     * By default, provides a generic help page
     * that covers general features.  Specific
     * implementations can override this to 
     * show their own help page if desired.
     */
    protected void addHelpMenu() {
    	addHelpMenu("package.jmri.jmrix.sprog.console.SprogConsoleFrame", true);
    }
    
    // Override superclass to append return
    public void enterButtonActionPerformed(java.awt.event.ActionEvent e) {
        nextLine(entryField.getText() + "\n", "");
    }
    
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        SprogMessage m = new SprogMessage(cmdTextField.getText());
        // Messages sent by us will not be forwarded back so add to display manually
        nextLine("cmd: \""+m.toString()+"\"\n", "");
        SprogTrafficController.instance().sendSprogMessage(m, this);
    }
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="IS2_INCONSISTENT_SYNC")
    // validateCurrent() is called from synchronised code
    public void validateCurrent() {
        String currentRange = "200 - 996";
        int validLimit = 996;
        if (sv.sprogType.sprogType > SprogType.SPROGIIv3) {
            currentRange = "200 - 2499";
            validLimit = 2499;
        }
        try {
            currentLimit = Integer.parseInt(currentTextField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid Current Limit Entered\n"
                    + "Please enter a value in the range " + currentRange,
                    "SPROG Console", JOptionPane.ERROR_MESSAGE);
            currentLimit = validLimit;
            return;
        }
        if ((currentLimit > validLimit) || (currentLimit < 200)) {
            JOptionPane.showMessageDialog(null, "Invalid Current Limit Entered\n"
                    + "Please enter a value in the range " + currentRange,
                    "SPROG Console", JOptionPane.ERROR_MESSAGE);
            currentLimit = validLimit;
        }
    }
    
    synchronized public void saveButtonActionPerformed(java.awt.event.ActionEvent e) {
        SprogMessage saveMsg;
        // Send Current Limit if possible
        state = State.CURRENTSENT;
        if (isCurrentLimitPossible()) {
            validateCurrent();
            // Value written is number of ADC steps 0f 4.88mV across 0.47 ohms
            currentLimit = currentLimit*470/4880;
            if (sv.sprogType.sprogType < SprogType.SPROGIIv3) {
                // Hack for SPROG bug where MSbyte of value must be non-zero
                currentLimit += 256;
            }
            tmpString = String.valueOf(currentLimit);
            saveMsg = new SprogMessage("I "+tmpString);
        } else {
            // Else send blank message to kicj things off
            saveMsg = new SprogMessage(" "+tmpString);
        }
        nextLine("cmd: \""+saveMsg.toString()+"\"\n", "");
        tc.sendSprogMessage(saveMsg, this);
       
        // Further messages will be sent from state machine
    }
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="IS2_INCONSISTENT_SYNC")
    // Called from synchronised code
    public boolean isCurrentLimitPossible() {
        if (sv.hasCurrentLimit()) {
            return true;
        } else
            return false;
    }
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="IS2_INCONSISTENT_SYNC")
    // Called from synchronised code
    public boolean isBlueLineSupportPossible() {
        if (sv.hasBlueLine()) {
            return true;
        } else
            return false;
    }
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="IS2_INCONSISTENT_SYNC")
    // Called from synchronised code
    public boolean isFirmwareUnlockPossible() {
        if (sv.hasFirmwareLock()) {
            return true;
        } else
            return false;
    }
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="IS2_INCONSISTENT_SYNC")
    // Called from synchronised code
    public boolean isZTCModePossible() {
        if (sv.hasZTCMode()) {
            return true;
        } else
            return false;
    }
    
    synchronized public void notifyVersion(SprogVersion v) {
        sv = v;
        if (log.isDebugEnabled()) { log.debug("Found: " + sv.toString()); }
        if (sv.sprogType.isSprog() == false) {
            // Didn't recognize a SPROG so check if it is in boot mode already
            JOptionPane.showMessageDialog(null, "SPROG prompt not found",
                    "SPROG Console", JOptionPane.ERROR_MESSAGE);
        } else {
            if (sv.sprogType.sprogType > SprogType.SPROGIIv3) {
                currentTextField.setToolTipText("Enter new current limit in milliAmps (less than 2500)");
            }
            // We know what we're connected to
            setTitle(title() + " - Connected to " + sv.toString());

            // Enable blueline & firmware unlock check boxes
            if (isBlueLineSupportPossible()) {
                if (log.isDebugEnabled()) { log.debug("Enable blueline check box"); }
                blueCheckBox.setEnabled(true);
                if (log.isDebugEnabled()) { log.debug(Boolean.toString(blueCheckBox.isEnabled())); }
            }
            if (isFirmwareUnlockPossible()) {
                if (log.isDebugEnabled()) { log.debug("Enable firmware check box"); }
                unlockCheckBox.setEnabled(true);
                if (log.isDebugEnabled()) { log.debug(Boolean.toString(unlockCheckBox.isEnabled())); }
            }

            ztcCheckBox.setEnabled(isZTCModePossible());
            
            // Get Current Limit if available
            if (isCurrentLimitPossible() && sm.getInUseCount() == 0) {
                state = State.CURRENTQUERYSENT;
                msg = new SprogMessage(1);
                msg.setOpCode('I');
                nextLine("cmd: \"" + msg + "\"\n", "");
                tc.sendSprogMessage(msg, this);
            } else {
                // Set default and get the mode word
                currentLimit = (SprogConstants.DEFAULT_I * 4880) / 470;
                currentTextField.setText(String.valueOf(SprogConstants.DEFAULT_I));

                //Only send this if we have zero active slots in slot manager
                //in command mode           
                if (sm.getInUseCount() == 0) {
                    state = State.MODEQUERYSENT;
                    msg = new SprogMessage(1);
                    msg.setOpCode('M');
                    nextLine("cmd: \"" + msg + "\"\n", "");
                    tc.sendSprogMessage(msg, this);
                } else {
                    state = State.IDLE;
                }
            }
        }
    }

    public synchronized void notifyMessage(SprogMessage l) {  // receive a message and log it
    	nextLine("cmd: \""+l.toString()+"\"\n", "");
    }
    
    public synchronized void notifyReply(SprogReply l) {  // receive a reply message and log it
        SprogMessage msg;
        replyString = l.toString();
        nextLine("rep: \""+replyString+"\"\n", "");
        
        // *** Check for error reply
        
        switch (state) {
            case IDLE:
                if (log.isDebugEnabled()) { log.debug("reply in IDLE state: "+replyString); }
                break;
            case CURRENTQUERYSENT:
                if (log.isDebugEnabled()) { log.debug("reply in CURRENTQUERYSENT state: "+replyString); }
                int valueLength = 4;
                if (sv.sprogType.sprogType >= SprogType.SPROGIIv3) {
                    valueLength = 6;
                }
                tmpString = replyString.substring(replyString.indexOf("=") + 
                            1, replyString.indexOf("=") + valueLength);
                if (log.isDebugEnabled()) { log.debug("Current limit string: "+tmpString); }
                // Value returned is number of ADC steps 0f 4.88mV across 0.47 ohms
                // SPROG 3 is equivalent, using .047R sense with 10x amplifier
                // Convert to milliAmps using integer math
                try {
                    currentLimit = (Integer.parseInt(tmpString)*4880)/470;
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Malformed Reply for current limit",
                            "SPROG Console", JOptionPane.ERROR_MESSAGE);
                    state = State.IDLE;
                    return;
                }
                if (log.isDebugEnabled()) { log.debug("Current limit value: "+currentLimit); }
                currentTextField.setText(String.valueOf(currentLimit));
                currentTextField.setEnabled(true);
                // Next get the mode word
                // Only get this if we have zero active slots in slot manager
                // in command mode
                if (sm.getInUseCount() == 0) {
                    state = State.MODEQUERYSENT;
                    msg = new SprogMessage(1);
                    msg.setOpCode('M');
                    nextLine("cmd: \""+msg+"\"\n", "");
                    tc.sendSprogMessage(msg, this);
                } else {
                    state = State.IDLE;
                }
                break;
            case MODEQUERYSENT:
                if (log.isDebugEnabled()) { log.debug("reply in MODEQUERYSENT state: "+replyString); }
                tmpString = replyString.substring(replyString.indexOf("=") + 
                            2, replyString.indexOf("=") + 6);
                // Value returned is in hex
                try {
                    modeWord = Integer.parseInt(tmpString, 16);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Malformed Reply for mode word",
                            "SPROG Console", JOptionPane.ERROR_MESSAGE);
                    state = State.IDLE;
                    return;
                }
                state = State.IDLE;
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
                if ((modeWord & SprogConstants.BLUE_BIT) != 0) {
                    blueCheckBox.setSelected(true);
                }
                break;
            case CURRENTSENT:
                if (log.isDebugEnabled()) { log.debug("reply in CURRENTSENT state: "+replyString); }
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
                state = State.MODESENT;
                msg = new SprogMessage("M "+modeWord);
                nextLine("cmd: \""+msg.toString()+"\"\n", "");
                tc.sendSprogMessage(msg, this);
                break;
            case MODESENT:
                if (log.isDebugEnabled()) { log.debug("reply in MODESENT state: "+replyString); }
                // Write to EEPROM
                state = State.WRITESENT;
                msg = new SprogMessage("W");
                nextLine("cmd: \""+msg.toString()+"\"\n", "");
                tc.sendSprogMessage(msg, this);
                break;
            case WRITESENT:
                if (log.isDebugEnabled()) { log.debug("reply in WRITESENT state: "+replyString); }
                // All done
                state = State.IDLE;
        }
    }
    
    /**
     * Internal routine to handle a timeout
     */
    synchronized protected void timeout() {
        JOptionPane.showMessageDialog(null, "Timeout talking to SPROG",
                    "Timeout", JOptionPane.ERROR_MESSAGE);
        state = State.IDLE;
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
    
    static Logger log = LoggerFactory.getLogger(SprogConsoleFrame.class.getName());
    
}
