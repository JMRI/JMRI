package jmri.jmrix.acela.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.jmrix.acela.AcelaMessage;
import jmri.jmrix.acela.AcelaReply;

/**
 * Frame for user input of Acela messages
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Bob Coleman, Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaPacketGenFrame extends jmri.util.JmriJFrame implements jmri.jmrix.acela.AcelaListener {

    // member declarations
    JLabel jLabel1 = new JLabel();
    JButton sendButton = new JButton();
    JTextField packetTextField = new JTextField(12);

    private jmri.jmrix.acela.AcelaSystemConnectionMemo _memo = null;

    public AcelaPacketGenFrame(jmri.jmrix.acela.AcelaSystemConnectionMemo memo) {
        _memo = memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        // the following code sets the frame's initial state

        jLabel1.setText(Bundle.getMessage("CommandLabel"));
        jLabel1.setVisible(true);

        sendButton.setText(Bundle.getMessage("ButtonSend"));
        sendButton.setVisible(true);
        sendButton.setToolTipText(Bundle.getMessage("MenuItemSendCommand"));

        packetTextField.setText("");
        packetTextField.setToolTipText(Bundle.getMessage("EnterHexBytesToolTip"));
        packetTextField.setMaximumSize(
                new Dimension(packetTextField.getMaximumSize().width,
                        packetTextField.getPreferredSize().height));

        setTitle(Bundle.getMessage("AcelaSendCommandTitle"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(jLabel1);
        getContentPane().add(packetTextField);
        getContentPane().add(sendButton);

        sendButton.addActionListener(new java.awt.event.ActionListener() {

            /**
             * {@inheritDoc}
             */
            @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendButtonActionPerformed(e);
                    }
                }
        );

        addHelpMenu("package.jmri.jmrix.acela.packetgen.AcelaPacketGenFrame", true); // NOI18N

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

            _memo.getTrafficController().sendAcelaMessage(m, this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(AcelaMessage m) {
    }  // ignore replies

    /**
     * {@inheritDoc}
     */
    @Override
    public void reply(AcelaReply r) {
    } // ignore replies

}
