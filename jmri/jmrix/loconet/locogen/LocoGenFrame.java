// LocoGenFrame.java

package jmri.jmrix.loconet.locogen;

import java.awt.*;

import javax.swing.*;

import jmri.jmrix.loconet.*;

/**
 * User interface for sending LocoNet messages to exercise the system
 * <P>
 * When  sending a sequence of operations:
 * <UL>
 * <LI>Send the next message
 * <LI>Wait until you hear the echo, then start a timer
 * <LI>When the timer trips, repeat if buttons still down.
 * </UL>
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision: 1.5 $
 */
public class LocoGenFrame extends javax.swing.JFrame implements LocoNetListener {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    public LocoGenFrame() {
    }

    // internal members to hold sequence widgets
    static final int MAXSEQUENCE = 4;
    JTextField mPacketField[]   = new JTextField[MAXSEQUENCE];
    JCheckBox  mUseField[]      = new JCheckBox[MAXSEQUENCE];
    JTextField mDelayField[]    = new JTextField[MAXSEQUENCE];
    JToggleButton    mRunButton = new JToggleButton("Go");

    public void initComponents() throws Exception {

        setTitle("Send LocoNet Packet");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // handle single-packet part
        getContentPane().add(new JLabel("Send one packet:"));
        {
            JPanel pane1 = new JPanel();
            pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));
            
            jLabel1.setText("Packet:");
            jLabel1.setVisible(true);
            
            sendButton.setText("Send");
            sendButton.setVisible(true);
            sendButton.setToolTipText("Send packet");
            
            packetTextField.setToolTipText("Enter packet as hex pairs, e.g. 82 7D");
            
            
            pane1.add(jLabel1);
            pane1.add(packetTextField);
            pane1.add(sendButton);
            pane1.add(Box.createVerticalGlue());
            
            sendButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendButtonActionPerformed(e);
                    }
                });
            addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        thisWindowClosing(e);
                    }
                });
            
            getContentPane().add(pane1);
        }
        
        getContentPane().add(new JSeparator());
        
        // Configure the sequence
        getContentPane().add(new JLabel("Send sequence of packets:"));
        JPanel pane2 = new JPanel();
        pane2.setLayout(new GridLayout(MAXSEQUENCE+2, 4));
        pane2.add(new JLabel(""));
        pane2.add(new JLabel("Send"));
        pane2.add(new JLabel("packet"));
        pane2.add(new JLabel("wait"));
        for (int i=0;i<MAXSEQUENCE; i++) {
            pane2.add(new JLabel(Integer.toString(i+1)));
            mUseField[i]=new JCheckBox();
            mPacketField[i]=new JTextField(10);
            mDelayField[i]=new JTextField(10);
            pane2.add(mUseField[i]);
            pane2.add(mPacketField[i]);
            pane2.add(mDelayField[i]);
        }
        pane2.add(mRunButton); // starts a new row in layout
        getContentPane().add(pane2);
        
        mRunButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    runButtonActionPerformed(e);
                }
            });
        
        // pack to cause display
        pack();
    }
    
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        tc.sendLocoNetMessage(createPacket(packetTextField.getText()));
    }
    
    // control sequence operation
    int mNextSequenceElement = 0;
    LocoNetMessage mNextEcho = null;
    javax.swing.Timer timer = null;
    
    /**
     * Internal routine to handle timer starts & restarts
     */
    protected void restartTimer(int delay) {
        if (timer==null) {
            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendNextItem();
                    }
                });
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * Run button pressed down, start the sequence operation
     * @param e
     */
    public void runButtonActionPerformed(java.awt.event.ActionEvent e) {
        if (!mRunButton.isSelected()) return;
        // make sure at least one is checked
        boolean ok = false;
        for (int i=0; i<MAXSEQUENCE; i++) {
            if (mUseField[i].isSelected()) ok = true;
        }
        if (!ok) {
            mRunButton.setSelected(false);
            return;
        }
        // start the operation
        mNextSequenceElement = 0;
        sendNextItem();
    }
    
    /**
     * Process the incoming message to look for the needed echo
     * @param m
     */
    public void message(LocoNetMessage m) {
        // are we running?
        if (!mRunButton.isSelected()) return;
        // yes, is this what we're looking for
        if (! (mNextEcho.equals(m))) return;
        // yes, we got it, do the next
        startSequenceDelay();
    }
    
    /**
     * Echo has been heard, start delay for next packet
     */
    void startSequenceDelay() {
        // at the start, mNextSequenceElement contains index we're
        // working on
        int delay = Integer.parseInt(mDelayField[mNextSequenceElement].getText());
        // increment to next line at completion
        mNextSequenceElement++;
        // start timer
        restartTimer(delay);
    }
    
    /**
     * Send next item; may be used for the first item or
     * when a delay has elapsed.
     */
    void sendNextItem() {
        // check if still running
        if (!mRunButton.isSelected()) return;
        // have we run off the end?
        if (mNextSequenceElement>=MAXSEQUENCE) {
            // past the end, go back
            mNextSequenceElement = 0;
        }
        // is this one enabled?
        if (mUseField[mNextSequenceElement].isSelected()) {
            // make the packet
            LocoNetMessage m = createPacket(mPacketField[mNextSequenceElement].getText());
            // send it
            mNextEcho = m;
            tc.sendLocoNetMessage(m);
        } else {
            // ask for the next one
            mNextSequenceElement++;
            sendNextItem();
        }
    }
    
    /**
     * Create a well-formed LocoNet packet from a String
     * @param s
     * @return The packet, with contents filled-in
     */
    LocoNetMessage createPacket(String s) {
        // gather bytes in result
        int b[] = parseString(s);
        if (b.length == 0) return null;  // no such thing as a zero-length message
        LocoNetMessage m = new LocoNetMessage(b.length);
        for (int i=0; i<b.length; i++) m.setElement(i, b[i]);
        return m;
    }
    
    int[] parseString(String s) {
        String ts = s+"  "; // ensure blanks on end to make scan easier
        int len = 0;
        // scan for length
        for (int i= 0; i< s.length(); i++) {
            if (ts.charAt(i) != ' ')  {
                // need to process char for number. Is this a single digit?
                if (ts.charAt(i+1) != ' ') {
                    // 2 char value
                    i++;
                    len++;
                } else {
                    // 1 char value
                    len++;
                }
            }
        }
        int[] b = new int[len];
        // scan for content
        int saveAt = 0;
        for (int i= 0; i< s.length(); i++) {
            if (ts.charAt(i) != ' ')  {
                // need to process char for number. Is this a single digit?
                if (ts.charAt(i+1) != ' ') {
                    // 2 char value
                    String v = new String(""+ts.charAt(i))+ts.charAt(i+1);
                    b[saveAt] = Integer.valueOf(v,16).intValue();
                    i++;
                    saveAt++;
                } else {
                    // 1 char value
                    String v = new String(""+ts.charAt(i));
                    b[saveAt] = Integer.valueOf(v,16).intValue();
                    saveAt++;
                }
            }
        }
        return b;
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
        setVisible(false);
        dispose();
	// disconnect from LnTrafficController
        tc = null;
    }
    
    // connect to the LnTrafficController
    public void connect(LnTrafficController t) {
        tc = t;
        tc.addLocoNetListener(~0, this);
    }
    
    
    // private data
    private LnTrafficController tc = null;
    
}
