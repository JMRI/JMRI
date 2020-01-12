package jmri.jmrix.marklin.swing.packetgen;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrix.marklin.MarklinListener;
import jmri.jmrix.marklin.MarklinMessage;
import jmri.jmrix.marklin.MarklinReply;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user input of Marklin messages
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Dan Boudreau Copyright (C) 2007
 */
public class PacketGenPanel extends jmri.jmrix.marklin.swing.MarklinPanel implements MarklinListener {

    // member declarations
    JLabel entryLabel = new JLabel();
    JLabel replyLabel = new JLabel();
    JButton sendButton = new JButton();
    JTextField packetTextField = new JTextField(20);
    JTextField packetReplyField = new JTextField(20);

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

        JPanel entrybox = new JPanel();
        entryLabel.setText(Bundle.getMessage("CommandLabel"));
        entryLabel.setVisible(true);

        replyLabel.setText(Bundle.getMessage("ReplyLabel"));
        replyLabel.setVisible(true);

        sendButton.setText(Bundle.getMessage("ButtonSend"));
        sendButton.setVisible(true);
        sendButton.setToolTipText(Bundle.getMessage("SendToolTip"));

        packetTextField.setText("");
        packetTextField.setToolTipText(Bundle.getMessage("EnterHexToolTip"));
        packetTextField.setMaximumSize(new Dimension(packetTextField
                .getMaximumSize().width, packetTextField.getPreferredSize().height));

        entrybox.setLayout(new GridLayout(2, 2));
        entrybox.add(entryLabel);
        entrybox.add(packetTextField);
        entrybox.add(replyLabel);

        JPanel buttonbox = new JPanel();
        FlowLayout buttonLayout = new FlowLayout(FlowLayout.TRAILING);
        buttonbox.setLayout(buttonLayout);
        buttonbox.add(sendButton);
        entrybox.add(buttonbox);
        //packetReplyField.setEditable(false); // keep field editable to allow user to select and copy the reply
        add(entrybox);
        add(packetReplyField);
        add(Box.createVerticalGlue());

        sendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.marklin.swing.packetgen.PacketGenFrame";
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return Bundle.getMessage("SendCommandTitle");
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initComponents(MarklinSystemConnectionMemo memo) {
        super.initComponents(memo);
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        if (packetTextField.getText() != null || !packetTextField.getText().equals("")) {
            String text = packetTextField.getText();
            if (text.length() == 0) {
                return; // no work
            }
            log.info("Entry[{}]", text);
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
                log.error("Only hex commands are supported");
                JOptionPane.showMessageDialog(null, Bundle.getMessage("HexOnlyDialog"),
                        Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    /** 
     * {@inheritDoc}
     * Ignore messages
     */
    @Override
    public void message(MarklinMessage m) {
    }

    /** 
     * {@inheritDoc}
     * Ignore replies
     */
    @Override
    public void reply(MarklinReply r) {
        packetReplyField.setText(r.toString());
    }

    private final static Logger log = LoggerFactory.getLogger(PacketGenPanel.class);
}
