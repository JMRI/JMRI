package jmri.jmrix.ecos.swing.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import jmri.jmrix.ecos.EcosListener;
import jmri.jmrix.ecos.EcosMessage;
import jmri.jmrix.ecos.EcosReply;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;

/**
 * Frame for user input of ECoS messages
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Dan Boudreau Copyright (C) 2007
 */
public class PacketGenPanel extends jmri.jmrix.ecos.swing.EcosPanel implements EcosListener {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(20);

    public PacketGenPanel() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // the following code sets the frame's initial state
        {
            jLabel1.setText(Bundle.getMessage("CommandLabel"));
            jLabel1.setVisible(true);

            sendButton.setText(Bundle.getMessage("ButtonSend"));
            sendButton.setVisible(true);
            sendButton.setToolTipText(Bundle.getMessage("SendToolTip"));

            packetTextField.setText("");
            packetTextField.setToolTipText(Bundle.getMessage("EnterASCIIToolTip"));
            packetTextField.setMaximumSize(new Dimension(packetTextField
                    .getMaximumSize().width, packetTextField.getPreferredSize().height));

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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.ecos.swing.packetgen.PacketGenFrame";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        if (memo != null) {
            return Bundle.getMessage("SendXCommandTitle", memo.getUserName());
        }
        return Bundle.getMessage("MenuItemSendPacket");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(EcosSystemConnectionMemo memo) {
        super.initComponents(memo);

        memo.getTrafficController().addEcosListener(this);
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {

        EcosMessage m = new EcosMessage(packetTextField.getText().length());
        for (int i = 0; i < packetTextField.getText().length(); i++) {
            m.setElement(i, packetTextField.getText().charAt(i));
        }

        memo.getTrafficController().sendEcosMessage(m, this);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(EcosMessage m) {
    }  // ignore replies

    /**
     * {@inheritDoc}
     */
    @Override
    public void reply(EcosReply r) {
    } // ignore replies

}
