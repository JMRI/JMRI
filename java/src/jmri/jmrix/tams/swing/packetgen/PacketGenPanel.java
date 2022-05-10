package jmri.jmrix.tams.swing.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import jmri.jmrix.tams.TamsConstants;
import jmri.jmrix.tams.TamsMessage;
import jmri.util.StringUtil;

/**
 * Frame for user input of Tams messages Based on work by Bob Jacobsen and Kevin Dickerson
 *
 * @author Jan Boen
 */
public class PacketGenPanel extends jmri.jmrix.tams.swing.TamsPanel {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(20);
    javax.swing.JCheckBox checkBoxBinCmd = new javax.swing.JCheckBox();
    javax.swing.JCheckBox checkBoxReplyType = new javax.swing.JCheckBox();

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
            jLabel1.setText("Command: "); // TODO I18N using Bundle.getMessage("key") - many keys available in JmrixBundle
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

            add(jLabel1);
            add(packetTextField);
            add(checkBoxBinCmd);
            add(sendButton);

            sendButton.addActionListener(this::sendButtonActionPerformed);
        }
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.tams.swing.packetgen.PacketGenFrame";
    }

    @Override
    public String getTitle() {
        return "Send Tams command";
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        TamsMessage m;
        String input = packetTextField.getText();
        // TODO check input + feedback on error. Too easy to cause NPE
        if (checkBoxBinCmd.isSelected()) {//Binary TamsMessage to be sent
            m = createPacket(input);
            if (m == null) {
                JOptionPane.showMessageDialog(PacketGenPanel.this,
                        "Enter hexadecimal numbers only", "Tams Binary Command",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            //Set replyType to unknown just in case
            m.setReplyType('M');
            m.setBinary(true);
            //Check which command is issued and replace by predefined TamsMessage
            if (m.getElement(1) == TamsConstants.XPWROFF){
                m = TamsMessage.setXPwrOff();
            }
            if (m.getElement(1) == TamsConstants.XPWRON){
                m = TamsMessage.setXPwrOn();
            }
            /*if (m.getElement(1) == TamsConstants.XEVENT){
                m = TamsMessage.getXStatus();
            }
            if (m.getElement(1) == TamsConstants.XEVTSEN){
                m = TamsMessage.getXEvtSen();
            }
            if (m.getElement(1) == TamsConstants.XEVTLOK){
                m = TamsMessage.getXEvtLok();
            }
            if (m.getElement(1) == TamsConstants.XEVTTRN){
                m = TamsMessage.getXEvtTrn();
            }*/
        } else {//ASCII TamsMessage to be sent
            m = new TamsMessage(input.length());
            for (int i = 0; i < input.length(); i++) {
                m.setElement(i, input.charAt(i));
            }
            //Set replyType to unknown just in case
            m.setReplyType('M');
            m.setBinary(false);
            //Check which command is issued and set correct Reply Type
            if (m.getElement(1) == 'P'){//Programming message
                m.setReplyType('C');
            }
            /*if (m.getElement(1) == 'T'){//Turnout message
                m.setReplyType('T');
            }
            if (m.getElement(1) == 'S'){//Sensor message
                m.setReplyType('S');
            }*/
        }
        if ( memo != null ) {
            memo.getTrafficController().sendTamsMessage(m, null);
        }
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

}
