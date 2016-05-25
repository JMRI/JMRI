// SerialPacketGenFrame.java
package jmri.jmrix.tmcc.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JSeparator;
import jmri.jmrix.tmcc.SerialMessage;
import jmri.jmrix.tmcc.SerialReply;
import jmri.jmrix.tmcc.SerialTrafficController;
import jmri.util.StringUtil;

/**
 * Frame for user input of serial messages
 *
 * @author	Bob Jacobsen Copyright (C) 2002, 2003, 2006
 * @version	$Revision$
 */
public class SerialPacketGenFrame extends jmri.util.JmriJFrame implements jmri.jmrix.tmcc.SerialListener {

    /**
     *
     */
    private static final long serialVersionUID = 6372326464393162756L;
    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

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

        setTitle("Send TMCC command");
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

        // pack for display
        pack();
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        SerialTrafficController.instance().sendSerialMessage(createPacket(packetTextField.getText()), this);
    }

    SerialMessage createPacket(String s) {
        // gather bytes in result
        byte b[] = StringUtil.bytesFromHexString(s);
        if (b.length != 3) {
            return null;  // no such thing as message of other than 3 bytes
        }
        SerialMessage m = new SerialMessage();
        for (int i = 0; i < b.length; i++) {
            m.setElement(i, b[i]);
        }
        return m;
    }

    public void message(SerialMessage m) {
    }  // ignore replies

    public void reply(SerialReply r) {
    } // ignore replies
}
