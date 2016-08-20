/**
 * Description:	Frame for user input of Xpa+Modem (dialing) messages
 * <p>
 * @author	Paul Bender Copyright (C) 2004
 */
package jmri.jmrix.xpa.swing.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import jmri.jmrix.xpa.XpaMessage;

public class XpaPacketGenFrame extends jmri.util.JmriJFrame implements jmri.jmrix.xpa.XpaListener {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    jmri.jmrix.xpa.XpaSystemConnectionMemo memo = null;

    public XpaPacketGenFrame(jmri.jmrix.xpa.XpaSystemConnectionMemo m) {
        super();
        memo = m;
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

        setTitle("Send Xpa+Modem command");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(jLabel1);
        getContentPane().add(packetTextField);
        getContentPane().add(sendButton);

        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });

        // pack for display
        pack();
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        XpaMessage m = new XpaMessage(packetTextField.getText().length());
        for (int i = 0; i < packetTextField.getText().length(); i++) {
            m.setElement(i, packetTextField.getText().charAt(i));
        }

        memo.getXpaTrafficController().sendXpaMessage(m, this);
    }

    public void message(XpaMessage m) {
    }  // ignore replies

    public void reply(XpaMessage r) {
    } // ignore replies
}
