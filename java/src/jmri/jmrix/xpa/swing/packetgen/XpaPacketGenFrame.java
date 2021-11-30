package jmri.jmrix.xpa.swing.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import jmri.jmrix.xpa.XpaMessage;

/**
 * Frame for user input of Xpa+Modem (dialing) messages.
 *
 * @author Paul Bender Copyright (C) 2004
 */
public class XpaPacketGenFrame extends jmri.util.JmriJFrame implements jmri.jmrix.xpa.XpaListener {

    // member declarations
    final javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    final javax.swing.JButton sendButton = new javax.swing.JButton();
    final javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    final jmri.jmrix.xpa.XpaSystemConnectionMemo memo;

    public XpaPacketGenFrame(jmri.jmrix.xpa.XpaSystemConnectionMemo m) {
        super();
        memo = m;
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

        sendButton.addActionListener(this::sendButtonActionPerformed);

        // pack for display
        pack();
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        String input = packetTextField.getText();
        // TODO check input + feedback on error. Too easy to cause NPE
        XpaMessage m = new XpaMessage(input.length());
        for (int i = 0; i < input.length(); i++) {
            m.setElement(i, input.charAt(i));
        }

        memo.getXpaTrafficController().sendXpaMessage(m, this);
    }

    @Override
    public void message(XpaMessage m) {
    }  // ignore replies

    @Override
    public void reply(XpaMessage r) {
    } // ignore replies

}
