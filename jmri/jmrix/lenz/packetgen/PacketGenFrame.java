// PacketGenFrame.java

package jmri.jmrix.lenz.packetgen;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.util.StringUtil;

import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetTrafficController;

/**
 * Frame for user input of XpressNet messages
 * @author			Bob Jacobsen   Copyright (C) 2001,2002
 * @version			$Revision: 1.4 $
 */
public class PacketGenFrame extends javax.swing.JFrame {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);
    
    public PacketGenFrame() {
    }
    
    public void initComponents() throws Exception {
        // the following code sets the frame's initial state
        
        jLabel1.setText("Packet:");
        jLabel1.setVisible(true);
        
        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Send packet");
        
        packetTextField.setToolTipText("Enter packet as hex pairs, e.g. 82 7D");
        
        setTitle("Send XpressNet Packet");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        getContentPane().add(jLabel1);
        getContentPane().add(packetTextField);
        getContentPane().add(sendButton);
        getContentPane().add(Box.createVerticalGlue());
        
        
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
        
        // pack to cause display
        pack();
    }
    
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        tc.sendXNetMessage(createPacket(packetTextField.getText()), null);
    }
    
    XNetMessage createPacket(String s) {
        // gather bytes in result
        byte b[] = StringUtil.bytesFromHexString(s);
        if (b.length == 0) return null;  // no such thing as a zero-length message
        XNetMessage m = new XNetMessage(b.length);
        for (int i=0; i<b.length; i++) m.setElement(i, b[i]);
        return m;
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
	// disconnect from TrafficController
        tc = null;
    }
    
    // connect to the TrafficController
    public void connect(XNetTrafficController t) {
        tc = t;
    }
    
    
    // private data
    private XNetTrafficController tc = null;
    
}
