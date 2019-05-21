package jmri.jmrix.easydcc.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import jmri.jmrix.easydcc.EasyDccMessage;
import jmri.jmrix.easydcc.EasyDccReply;
import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;

/**
 * Frame for user input of EasyDCC messages.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class EasyDccPacketGenFrame extends jmri.util.JmriJFrame implements jmri.jmrix.easydcc.EasyDccListener {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);
    private EasyDccSystemConnectionMemo _memo = null;

    public EasyDccPacketGenFrame(EasyDccSystemConnectionMemo memo) {
        super();
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
        sendButton.setToolTipText(Bundle.getMessage("SendToolTip"));

        packetTextField.setText("");
        packetTextField.setToolTipText(Bundle.getMessage("EnterASCIIToolTip"));
        packetTextField.setMaximumSize(
                new Dimension(packetTextField.getMaximumSize().width,
                        packetTextField.getPreferredSize().height
                )
        );

        setTitle(Bundle.getMessage("SendCommandTitle"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(jLabel1);
        getContentPane().add(packetTextField);
        getContentPane().add(sendButton);

        sendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });

        // pack for display
        pack();
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        EasyDccMessage m = new EasyDccMessage(packetTextField.getText().length());
        for (int i = 0; i < packetTextField.getText().length(); i++) {
            m.setElement(i, packetTextField.getText().charAt(i));
        }

        _memo.getTrafficController().sendEasyDccMessage(m, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(EasyDccMessage m) {
    }  // ignore replies

    /**
     * {@inheritDoc}
     */
    @Override
    public void reply(EasyDccReply r) {
    } // ignore replies

}
