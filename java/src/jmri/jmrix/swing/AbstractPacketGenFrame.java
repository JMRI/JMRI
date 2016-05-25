// AbstractPacketGenFrame.java
package jmri.jmrix.swing;

import javax.swing.Box;
import javax.swing.BoxLayout;

/**
 * Frame for user input of XpressNet messages
 *
 * @author	Bob Jacobsen Copyright (C) 2001,2002
 * @version	$Revision$
 */
public abstract class AbstractPacketGenFrame extends jmri.util.JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = 5082190885030718992L;
    // member declarations
    protected javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    protected javax.swing.JButton sendButton = new javax.swing.JButton();
    protected javax.swing.JComboBox<String> packetTextField = new javax.swing.JComboBox<String>();

    public AbstractPacketGenFrame() {
        super();
    }

    public void initComponents() throws Exception {
        // the following code sets the frame's initial state

        jLabel1.setText("Packet:");
        jLabel1.setVisible(true);

        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Send packet");

        packetTextField.setToolTipText("Enter packet as hex pairs, e.g. 82 7D");
        packetTextField.setMaximumRowCount(5); // set the maximum number of items in the history.
        packetTextField.setEditable(true);
        setTitle("Send Packet");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(jLabel1);
        getContentPane().add(packetTextField);
        getContentPane().add(sendButton);
        getContentPane().add(Box.createVerticalGlue());

        sendButton.addActionListener(new java.awt.event.ActionListener() {
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
