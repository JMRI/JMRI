package jmri.jmrix.powerline.swing.packetgen;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.SerialTrafficController;
import jmri.util.StringUtil;

/**
 * Frame for user input of serial messages.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2003, 2006, 2007, 2008 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SerialPacketGenFrame extends jmri.util.JmriJFrame implements jmri.jmrix.powerline.SerialListener {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);
    javax.swing.JCheckBox interlockButton = new javax.swing.JCheckBox(Bundle.getMessage("InterlockBoxLabel"));

    public SerialPacketGenFrame(SerialTrafficController tc) {
        super();
        this.tc = tc;
    }
    SerialTrafficController tc = null;

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        // the following code sets the frame's initial state

        jLabel1.setText(Bundle.getMessage("CommandLabel")); // I18N using Bundle.getMessage("key") with keys already available in JmrixBundle
        jLabel1.setVisible(true);

        sendButton.setText(Bundle.getMessage("ButtonSend"));
        sendButton.setVisible(true);
        sendButton.setToolTipText(Bundle.getMessage("TooltipSendPacket"));

        packetTextField.setText("");
        packetTextField.setToolTipText(Bundle.getMessage("EnterHexToolTip"));
        packetTextField.setMaximumSize(
                new Dimension(packetTextField.getMaximumSize().width,
                        packetTextField.getPreferredSize().height
                )
        );

        setTitle(Bundle.getMessage("SendPacketTitle"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(jLabel1);
        getContentPane().add(packetTextField);

        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        p2.add(interlockButton);
        p2.add(sendButton);
        getContentPane().add(p2);

        sendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });

        getContentPane().add(new JSeparator(JSeparator.HORIZONTAL));

        // pack for display
        pack();
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        tc.sendSerialMessage(createPacket(packetTextField.getText()), this);
    }

    SerialMessage createPacket(String s) {
        // gather bytes in result
        byte b[] = StringUtil.bytesFromHexString(s);
        SerialMessage m = tc.getSerialMessage(b.length);
        for (int i = 0; i < b.length; i++) {
            m.setElement(i, b[i]);
        }
        m.setInterlocked(interlockButton.isSelected());
        return m;
    }

    /** 
     * {@inheritDoc}
     * Ignores messages.
     */
    @Override
    public void message(SerialMessage m) {
    }

    /** 
     * {@inheritDoc}
     * Ignores replies.
     */
    @Override
    public void reply(SerialReply r) {
    }

}
