// NcePacketGenFrame.java

package jmri.jmrix.nce.packetgen;

import jmri.util.*;
import jmri.jmrix.nce.*;

import java.awt.*;

import javax.swing.*;

/**
 * Frame for user input of Nce messages
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version $Revision: 1.2 $
 */
public class NcePacketGenFrame extends javax.swing.JFrame implements jmri.jmrix.nce.NceListener {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    public NcePacketGenFrame() {
    }

    public void initComponents() throws Exception {
        // the following code sets the frame's initial state

        jLabel1.setText("Command:");
        jLabel1.setVisible(true);

        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Send packet");

        packetTextField.setText("");
        packetTextField.setToolTipText("Enter command as ASCII string (hex not yet available)");
        packetTextField.setMaximumSize(
                                       new Dimension(packetTextField.getMaximumSize().width,
                                                     packetTextField.getPreferredSize().height
                                                     )
                                       );

        setTitle("Send NCE command");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(jLabel1);
        getContentPane().add(packetTextField);
        getContentPane().add(sendButton);


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

        // pack for display
        pack();
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        NceMessage m = new NceMessage(packetTextField.getText().length());
        for (int i=0; i<packetTextField.getText().length(); i++)
            m.setElement(i, packetTextField.getText().charAt(i));

        NceTrafficController.instance().sendNceMessage(m, this);
        //NceTrafficController.instance().sendNceMessage(createPacket(packetTextField.getText()), this);
    }

    public void  message(NceMessage m) {}  // ignore replies
    public void  reply(NceReply r) {} // ignore replies

    NceMessage createPacket(String s) {
        // gather bytes in result
        byte b[] = StringUtil.bytesFromHexString(s);
        if (b.length == 0) return null;  // no such thing as a zero-length message
        NceMessage m = new NceMessage(b.length);
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
    }
}
