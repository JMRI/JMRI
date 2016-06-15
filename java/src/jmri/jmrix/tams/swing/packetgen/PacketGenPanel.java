package jmri.jmrix.tams.swing.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import jmri.jmrix.tams.TamsListener;
import jmri.jmrix.tams.TamsMessage;
import jmri.jmrix.tams.TamsReply;
import jmri.jmrix.tams.TamsSystemConnectionMemo;
import jmri.util.StringUtil;

/**
 * Frame for user input of Tams messages Based on work by Bob Jacobsen
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 * @revisor Jan Boen
 */
public class PacketGenPanel extends jmri.jmrix.tams.swing.TamsPanel implements TamsListener {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(20);
    javax.swing.JCheckBox checkBoxBinCmd = new javax.swing.JCheckBox();
    javax.swing.JCheckBox checkBoxReplyType = new javax.swing.JCheckBox();

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

            checkBoxBinCmd.setText("Binary");
            checkBoxBinCmd.setVisible(true);
            checkBoxBinCmd.setToolTipText("Check to enable binary commands");
            checkBoxBinCmd.setSelected(false);

            checkBoxReplyType.setText("Multi Byte Reply");
            checkBoxReplyType.setVisible(true);
            checkBoxReplyType.setToolTipText("Check when you expect binary reply containing MULTIPLE bytes");
            checkBoxReplyType.setSelected(false);

            add(jLabel1);
            add(packetTextField);
            add(checkBoxBinCmd);
            add(checkBoxReplyType);
            add(sendButton);

            sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });
        }
    }

    public String getHelpTarget() {
        return "package.jmri.jmrix.tams.swing.packetgen.PacketGenFrame";
    }

    public String getTitle() {
        return "Send Tams command";
    }

    public void initComponents(TamsSystemConnectionMemo memo) {
        super.initComponents(memo);

        memo.getTrafficController().addTamsListener(this);
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        TamsMessage m;
        if (checkBoxBinCmd.isSelected()) {
            // Binary selected, convert ASCII to hex

            m = createPacket(packetTextField.getText());
            if (m == null) {
                JOptionPane.showMessageDialog(PacketGenPanel.this,
                        "Enter hexadecimal numbers only", "Tams Binary Command",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            m.setBinary(true);
            if (checkBoxReplyType.isSelected()){
            	m.setReplyOneByte(false);
            } else {
            	m.setReplyOneByte(true);
            }
            //Must add logic so the replyType gets properly populated 
        } else {

            m = new TamsMessage(packetTextField.getText().length());
            for (int i = 0; i < packetTextField.getText().length(); i++) {
                m.setElement(i, packetTextField.getText().charAt(i));
            }
        }
        memo.getTrafficController().sendTamsMessage(m, this);

    }

    TamsMessage createPacket(String s) {
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
        TamsMessage m = new TamsMessage(b.length);
        for (int i = 0; i < b.length; i++) {
            m.setElement(i, b[i]);
        }
        return m;
    }

    public void message(TamsMessage m) {
    }  // ignore replies

    public void reply(TamsReply r) {
    } // ignore replies

}
