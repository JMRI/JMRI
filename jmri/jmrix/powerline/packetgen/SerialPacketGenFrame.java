// SerialPacketGenFrame.java

package jmri.jmrix.powerline.packetgen;

import jmri.util.StringUtil;
import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.SerialTrafficController;

import jmri.jmrix.powerline.X10;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * Frame for user input of serial messages
 * @author	Bob Jacobsen   Copyright (C) 2002, 2003, 2006, 2007, 2008
 * @version	$Revision: 1.3 $
 */
public class SerialPacketGenFrame extends jmri.util.JmriJFrame implements jmri.jmrix.powerline.SerialListener {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);
    javax.swing.JCheckBox interlockButton = new javax.swing.JCheckBox("Interlock");

    javax.swing.JButton pollButton = new javax.swing.JButton("Send macro");

    public SerialPacketGenFrame() {
        super();
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

        setTitle("Send powerline device command");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(jLabel1);
        getContentPane().add(packetTextField);
        
        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        p2.add(interlockButton);
        p2.add(sendButton);
        getContentPane().add(p2);

        sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });

        getContentPane().add(new JSeparator(JSeparator.HORIZONTAL));

        // add poll message buttons
        JPanel pane3 = new JPanel();
        pane3.setLayout(new FlowLayout());
        pane3.add(new JLabel("Address:"));
        pane3.add(pollButton);
        getContentPane().add(pane3);

        pollButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    pollButtonActionPerformed(e);
                }
            });
        pollButton.setToolTipText("Send poll request");

        // pack for display
        pack();
    }

    public void pollButtonActionPerformed(java.awt.event.ActionEvent e) {
        SerialMessage msg = SerialMessage.setCM11Time(X10.encode(1)); // housecode A
        SerialTrafficController.instance().sendSerialMessage(msg, this);
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        SerialTrafficController.instance().sendSerialMessage(createPacket(packetTextField.getText()), this);
    }

    SerialMessage createPacket(String s) {
        // gather bytes in result
        byte b[] = StringUtil.bytesFromHexString(s);
        SerialMessage m = new SerialMessage(b.length);
        for (int i=0; i<b.length; i++) m.setElement(i, b[i]);
        m.setInterlocked(interlockButton.isSelected());
        return m;
    }

    public void  message(SerialMessage m) {}  // ignore replies
    public void  reply(SerialReply r) {} // ignore replies
}
