package jmri.jmrix.swing;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Frame for user input of XpressNet messages
 *
 * @author	Bob Jacobsen Copyright (C) 2001,2002
 */
public abstract class AbstractPacketGenFrame extends jmri.util.JmriJFrame {

    // member declarations
    protected JLabel jLabel1 = new JLabel();
    protected JButton sendButton = new JButton();
    protected JComboBox<String> packetTextField = new JComboBox<String>();

    public AbstractPacketGenFrame() {
        super();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        // the following code sets the frame's initial state
        setTitle("Send Packet"); // NOI18N

        JPanel packetPane = new JPanel();
        packetPane.setBorder(BorderFactory.createEtchedBorder());
        jLabel1.setText(Bundle.getMessage("PacketLabel"));
        packetPane.add(jLabel1);

        sendButton.setText(Bundle.getMessage("SendPacketTitle"));
        sendButton.setVisible(true);
        sendButton.setToolTipText(Bundle.getMessage("SendToolTip"));

        packetTextField.setPreferredSize(new JButton("XXXXXXXXXXXXXXXXXXXXXXXXX").getPreferredSize());
        packetTextField.setToolTipText(Bundle.getMessage("EnterHexBytesToolTip"));
        packetTextField.setMaximumRowCount(5); // set the maximum number of items in the history.
        packetTextField.setEditable(true);
        packetPane.add(packetTextField);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(packetPane);
        getContentPane().add(sendButton);
        getContentPane().add(Box.createVerticalGlue());

        sendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
                packetTextField.addItem((String) packetTextField.getSelectedItem());
                packetTextField.setSelectedItem("");
            }
        });

        // pack to cause display
        pack();
    }

    abstract public void sendButtonActionPerformed(java.awt.event.ActionEvent e);

}
