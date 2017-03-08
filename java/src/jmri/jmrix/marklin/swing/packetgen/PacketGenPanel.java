package jmri.jmrix.marklin.swing.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import jmri.jmrix.marklin.MarklinListener;
import jmri.jmrix.marklin.MarklinMessage;
import jmri.jmrix.marklin.MarklinReply;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user input of Marklin messages
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @author Dan Boudreau Copyright (C) 2007
 */
public class PacketGenPanel extends jmri.jmrix.marklin.swing.MarklinPanel implements MarklinListener {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(20);
    javax.swing.JTextField packetReplyField = new javax.swing.JTextField(20);

    public PacketGenPanel() {
        super();
    }

    @Override
    public void initComponents() throws Exception {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // the following code sets the frame's initial state
        {
            jLabel1.setText("Command: ");
            jLabel1.setVisible(true);

            jLabel2.setText("Reply: ");
            jLabel2.setVisible(true);

            sendButton.setText("Send");
            sendButton.setVisible(true);
            sendButton.setToolTipText("Send packet");

            packetTextField.setText("");
            packetTextField.setToolTipText("Enter command");
            packetTextField.setMaximumSize(new Dimension(packetTextField
                    .getMaximumSize().width, packetTextField.getPreferredSize().height));

            add(jLabel1);
            add(packetTextField);
            add(jLabel2);
            add(packetReplyField);
            add(sendButton);

            sendButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });
        }
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.marklin.swing.packetgen.PacketGenFrame";
    }

    @Override
    public String getTitle() {
        return "Send CS2 command";
    }

    @Override
    public void initComponents(MarklinSystemConnectionMemo memo) {
        super.initComponents(memo);
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        if (packetTextField.getText() != null || !packetTextField.getText().equals("")) {
            String text = packetTextField.getText();
            if (text.startsWith("0x")) { //We want to send a hex message

                text = text.replaceAll("\\s", "");
                text = text.substring(2);
                String[] arr = text.split(",");
                byte[] msgArray = new byte[arr.length];
                int pos = 0;
                for (String s : arr) {
                    msgArray[pos++] = (byte) (Integer.parseInt(s, 16) & 0xFF);
                }

                MarklinMessage m = new MarklinMessage(msgArray);
                memo.getTrafficController().sendMarklinMessage(m, this);
            } else {
                log.error("Binary commands are only supported");
            }
        }

    }

    @Override
    public void message(MarklinMessage m) {
    }  // ignore replies

    @Override
    public void reply(MarklinReply r) {
        packetReplyField.setText(r.toHexString());
    } // ignore replies
    private final static Logger log = LoggerFactory.getLogger(PacketGenPanel.class.getName());
}
