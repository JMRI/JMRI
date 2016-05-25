// AcelaPacketGenFrame.java
package jmri.jmrix.acela.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import jmri.jmrix.acela.AcelaMessage;
import jmri.jmrix.acela.AcelaReply;
import jmri.jmrix.acela.AcelaTrafficController;

/**
 *
 * Description:	Frame for user input of Acela messages
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 *
 * @author	Bob Coleman, Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaPacketGenFrame extends jmri.util.JmriJFrame implements jmri.jmrix.acela.AcelaListener {

    /**
     *
     */
    private static final long serialVersionUID = 1748920880800009266L;
    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    public AcelaPacketGenFrame() {
    }

    public void initComponents() throws Exception {
        // the following code sets the frame's initial state

        jLabel1.setText("Command:");
        jLabel1.setVisible(true);

        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Send packet");

        packetTextField.setText("");
        packetTextField.setToolTipText("Enter command as HEX string (i.e. 0d 00 6F 15) with spaces");
        packetTextField.setMaximumSize(
                new Dimension(packetTextField.getMaximumSize().width,
                        packetTextField.getPreferredSize().height));

        setTitle("Send Acela command");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(jLabel1);
        getContentPane().add(packetTextField);
        getContentPane().add(sendButton);

        sendButton.addActionListener(
                new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendButtonActionPerformed(e);
                    }
                }
        );

        // pack for display
        pack();
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        int sendMessageLength;

        if (((packetTextField.getText().length() + 1) % 3) == 0) {
            sendMessageLength = (packetTextField.getText().length() + 1) / 3;
        } else {
            sendMessageLength = 0;
        }

        if (sendMessageLength > 0) {
            AcelaMessage m = new AcelaMessage(sendMessageLength);
            m.setBinary(true);

            int texti = 0;
            int messi = 0;
            while (texti < packetTextField.getText().length()) {
                int firstChar = 0;
                firstChar = packetTextField.getText().charAt(texti);
                if ((firstChar >= '0') && (firstChar <= '9')) {  // Assumes 0 to 9 are sequential in character set
                    firstChar = firstChar - '0';
                } else {
                    if ((firstChar >= 'a') && (firstChar <= 'f')) {  // Assumes a to f are sequential in character set
                        firstChar = firstChar - 'a' + 10;
                    } else {
                        if ((firstChar >= 'A') && (firstChar <= 'F')) {  // Assumes A to F are sequential in character set
                            firstChar = firstChar - 'A' + 10;
                        } else {
                            firstChar = 0;
                        }
                    }
                }
                texti = texti + 1;
                int secondChar = 0;
                secondChar = packetTextField.getText().charAt(texti);
                if ((secondChar >= '0') && (secondChar <= '9')) {  // Assumes 0 to 9 are sequential in character set
                    secondChar = secondChar - '0';
                } else {
                    if ((secondChar >= 'a') && (secondChar <= 'f')) {  // Assumes a to f are sequential in character set 
                        secondChar = secondChar - 'a' + 10;
                    } else {
                        if ((secondChar >= 'A') && (secondChar <= 'F')) {  // Assumes A to F are sequential in character set
                            secondChar = secondChar - 'A' + 10;
                        } else {
                            secondChar = 0;
                        }
                    }
                }
                texti = texti + 1;
                byte theNewByte = 0x00;
                theNewByte = (byte) ((firstChar * 16) + secondChar);
                m.setElement(messi, theNewByte);
                messi = messi + 1;  // need some error checking here
                texti = texti + 1;  // Skip space -- need more error checking here
            }

            AcelaTrafficController.instance().sendAcelaMessage(m, this);
        }
    }

    public void message(AcelaMessage m) {
    }  // ignore replies

    public void reply(AcelaReply r) {
    } // ignore replies
}

/* @(#)AcelaPacketGenFrame.java */
