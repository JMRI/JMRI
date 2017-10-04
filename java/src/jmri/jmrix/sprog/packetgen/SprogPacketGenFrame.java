package jmri.jmrix.sprog.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;

/**
 * Frame for user input of Sprog messages.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2010
 */
public class SprogPacketGenFrame extends jmri.util.JmriJFrame {

    private SprogSystemConnectionMemo _memo = null;
    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    public SprogPacketGenFrame(SprogSystemConnectionMemo memo) {
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
        sendButton.setToolTipText(Bundle.getMessage("SendPacketTooltip"));

        packetTextField.setText("");
        packetTextField.setToolTipText(Bundle.getMessage("SendCommandFieldTooltip"));
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
        SprogMessage m = new SprogMessage(packetTextField.getText());
        _memo.getSprogTrafficController().sendSprogMessage(m);
    }

}
