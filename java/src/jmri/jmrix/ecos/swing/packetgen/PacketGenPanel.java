package jmri.jmrix.ecos.swing.packetgen;

import java.awt.Dimension;

import javax.swing.BoxLayout;

import jmri.jmrix.ecos.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user input of ECoS messages
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Dan Boudreau Copyright (C) 2007
 */
public class PacketGenPanel extends jmri.jmrix.ecos.swing.EcosPanel {

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

            sendButton.addActionListener(this::sendButtonActionPerformed);
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

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        String input = packetTextField.getText();
        // TODO check input + feedback on error. Too easy to cause NPE
        EcosMessage m = new EcosMessage(input.length());
        for (int i = 0; i < input.length(); i++) {
            m.setElement(i, input.charAt(i));
        }
        if ( memo == null ) {
            log.error("no System Connection Memo Found when sending {}", m);
            return; 
        }
        EcosTrafficController tc = memo.getTrafficController();
        if (tc ==null ) {
            log.error("no Traffic Controller for Memo {} when sending {}", memo.getUserName(), m);
            return; 
        }
        tc.sendEcosMessage(m, null);
    }

    private final static Logger log = LoggerFactory.getLogger(PacketGenPanel.class);
        
}
