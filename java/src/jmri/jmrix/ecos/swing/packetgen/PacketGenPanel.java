// PacketGenFrame.java
package jmri.jmrix.ecos.swing.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import jmri.jmrix.ecos.EcosListener;
import jmri.jmrix.ecos.EcosMessage;
import jmri.jmrix.ecos.EcosReply;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;

/**
 * Frame for user input of Ecos messages
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @author Dan Boudreau Copyright (C) 2007
 * @version $Revision$
 */
public class PacketGenPanel extends jmri.jmrix.ecos.swing.EcosPanel implements EcosListener {

    /**
     *
     */
    private static final long serialVersionUID = -4117052466081375675L;
    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(20);

    public PacketGenPanel() {
        super();
    }

    public void initComponents() throws Exception {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // the following code sets the frame's initial state
        {
            jLabel1.setText("Command: ");
            jLabel1.setVisible(true);

            sendButton.setText("Send");
            sendButton.setVisible(true);
            sendButton.setToolTipText("Send packet");

            packetTextField.setText("");
            packetTextField.setToolTipText("Enter command");
            packetTextField.setMaximumSize(new Dimension(packetTextField
                    .getMaximumSize().width, packetTextField.getPreferredSize().height));

            add(jLabel1);
            add(packetTextField);
            add(sendButton);

            sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });
        }
    }

    public String getHelpTarget() {
        return "package.jmri.jmrix.ecos.swing.packetgen.PacketGenFrame";
    }

    public String getTitle() {
        if (memo != null) {
            return "Send " + memo.getUserName() + " command";
        }
        return "Send ECOS command";
    }

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

    public void message(EcosMessage m) {
    }  // ignore replies

    public void reply(EcosReply r) {
    } // ignore replies

}
