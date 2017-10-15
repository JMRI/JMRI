package jmri.jmrix.mrc.swing.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import jmri.jmrix.mrc.MrcMessage;
import jmri.jmrix.mrc.MrcSystemConnectionMemo;
import jmri.jmrix.mrc.MrcTrafficController;
import jmri.util.StringUtil;

/**
 * Frame for user input of Mrc messages
 *
 * @author Ken Cameron Copyright (C) 2010 derived from:
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Dan Boudreau Copyright (C) 2007
 */
public class MrcPacketGenPanel extends jmri.jmrix.mrc.swing.MrcPanel {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(20);

    private MrcTrafficController tc = null;

    public MrcPacketGenPanel() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initContext(Object context) {
        if (context instanceof MrcSystemConnectionMemo) {
            initComponents((MrcSystemConnectionMemo) context);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.mrc.swing.packetgen.MrcPacketGenPanel"; //NOI18N
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        StringBuilder x = new StringBuilder();
        if (memo != null) {
            x.append(memo.getUserName());
        } else {
            x.append("MRC_");//IN18N
        }
        x.append(": ");
        x.append(Bundle.getMessage("Title"));//NOI18N
        return x.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(MrcSystemConnectionMemo m) {
        this.memo = m;
        this.tc = m.getMrcTrafficController();

        // the following code sets the frame's initial state
        jLabel1.setText(Bundle.getMessage("MrcPacketGenLabelCommand"));//NOI18N
        jLabel1.setVisible(true);

        sendButton.setText(Bundle.getMessage("MrcPacketGenButtonSend"));//NOI18N
        sendButton.setVisible(true);
        sendButton.setToolTipText(Bundle.getMessage("MrcPacketGenTipSend"));//NOI18N

        packetTextField.setText("");
        packetTextField.setToolTipText(Bundle.getMessage("MrcPacketGenTipText")); //NOI18N
        packetTextField.setMaximumSize(new Dimension(packetTextField
                .getMaximumSize().width, packetTextField.getPreferredSize().height));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(jLabel1);
        add(packetTextField);
        add(sendButton);

        sendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });

    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {

        MrcMessage m = new MrcMessage(packetTextField.getText().length());
        for (int i = 0; i < packetTextField.getText().length(); i++) {
            m.setElement(i, packetTextField.getText().charAt(i));
        }

        tc.sendMrcMessage(m);
    }

    MrcMessage createPacket(String s) {
        // gather bytes in result
        byte b[];
        try {
            b = StringUtil.bytesFromHexString(s);
        } catch (NumberFormatException e) {
            return null;
        }
        if (b.length == 0) {
            return null; // no such thing as a zero-length message
        }
        MrcMessage m = new MrcMessage(b.length);
        for (int i = 0; i < b.length; i++) {
            m.setElement(i, b[i]);
        }
        return m;
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.mrc.swing.MrcNamedPaneAction {
        public Default() {
            super("Open MRC Send Binary Command",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    MrcPacketGenPanel.class.getName(),
                    jmri.InstanceManager.getDefault(MrcSystemConnectionMemo.class));//IN18N
        }
    }
}
