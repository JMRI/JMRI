// SerialPacketGenFrame.java

package jmri.jmrix.cmri.serial.packetgen;

import jmri.util.StringUtil;
import jmri.jmrix.cmri.serial.SerialMessage;
import jmri.jmrix.cmri.serial.SerialReply;
import jmri.jmrix.cmri.serial.SerialTrafficController;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * Frame for user input of CMRI serial messages
 * @author	Bob Jacobsen   Copyright (C) 2002, 2003
 * @version	$Revision: 1.5 $
 */
public class SerialPacketGenFrame extends javax.swing.JFrame implements jmri.jmrix.cmri.serial.SerialListener {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    javax.swing.JButton pollButton = new javax.swing.JButton("Send poll");
    javax.swing.JTextField uaAddrField = new javax.swing.JTextField(5);

    public SerialPacketGenFrame() {
    }

    public void initComponents() throws Exception {
        // the following code sets the frame's initial state

        jLabel1.setText("Command:");
        jLabel1.setVisible(true);

        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Send packet");

        packetTextField.setText("");
        packetTextField.setToolTipText("Enter command as hexadecimal bytes separated by a space");
        packetTextField.setMaximumSize(
                                       new Dimension(packetTextField.getMaximumSize().width,
                                                     packetTextField.getPreferredSize().height
                                                     )
                                       );

        setTitle("Send CMRI serial command");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(jLabel1);
        getContentPane().add(packetTextField);
        getContentPane().add(sendButton);


        sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });

        getContentPane().add(new JSeparator(JSeparator.HORIZONTAL));

        // add poll message buttons
        JPanel pane3 = new JPanel();
        pane3.setLayout(new FlowLayout());
        pane3.add(new JLabel("UA:"));
        pane3.add(uaAddrField);
        pane3.add(pollButton);
        uaAddrField.setText("0");
        uaAddrField.setToolTipText("Enter node address (decimal integer)");
        getContentPane().add(pane3);

        pollButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    pollButtonActionPerformed(e);
                }
            });
        pollButton.setToolTipText("Send poll request");

        addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    thisWindowClosing(e);
                }
            });

        // pack for display
        pack();
    }

    public void pollButtonActionPerformed(java.awt.event.ActionEvent e) {
        SerialMessage msg = SerialMessage.getPoll(Integer.valueOf(uaAddrField.getText()).intValue());
        SerialTrafficController.instance().sendSerialMessage(msg, this);
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        SerialTrafficController.instance().sendSerialMessage(createPacket(packetTextField.getText()), this);
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
