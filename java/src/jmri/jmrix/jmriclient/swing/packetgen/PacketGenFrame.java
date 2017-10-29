package jmri.jmrix.jmriclient.swing.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import jmri.jmrix.jmriclient.JMRIClientMessage;
import jmri.jmrix.jmriclient.JMRIClientReply;
import jmri.jmrix.jmriclient.JMRIClientTrafficController;

/**
 * Description: Frame for user input of JMRIClient messages
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class PacketGenFrame extends jmri.util.JmriJFrame implements jmri.jmrix.jmriclient.JMRIClientListener {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    public PacketGenFrame() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        // the following code sets the frame's initial state

        jLabel1.setText("Command:");
        jLabel1.setVisible(true);

        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Send packet");

        packetTextField.setText("");
        packetTextField.setToolTipText("Enter command as ASCII string");
        packetTextField.setMaximumSize(
                new Dimension(packetTextField.getMaximumSize().width,
                        packetTextField.getPreferredSize().height
                )
        );

        setTitle("Send JMRI Client command");
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
        JMRIClientMessage m = new JMRIClientMessage(packetTextField.getText().length() + 1);
        for (int i = 0; i < packetTextField.getText().length(); i++) {
            m.setElement(i, packetTextField.getText().charAt(i));
        }

        m.setElement(packetTextField.getText().length(), '\n');
        tc.sendJMRIClientMessage(m, this);
    }

    /**
     * {@inheritDoc}
     * Ignore messages.
     */
    @Override
    public void message(JMRIClientMessage m) {
    }

    /**
     * {@inheritDoc}
     * Ignore replies.
     */
    @Override
    public void reply(JMRIClientReply r) {
    }

    // connect to the TrafficController
    public void connect(JMRIClientTrafficController t) {
        tc = t;
    }

    // private data
    private JMRIClientTrafficController tc = null;

}
