package jmri.jmrix.powerline.swing.packetgen;

import java.awt.Dimension;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import jmri.jmrix.powerline.SerialListener;
import jmri.jmrix.powerline.SerialMessage;
import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.jmrix.powerline.SerialTrafficController;
import jmri.util.StringUtil;

/**
 * Frame for user input of Powerline messages.
 *
 * @author Ken Cameron Copyright (C) 2010 derived from:
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Dan Boudreau Copyright (C) 2007
 */
public class SerialPacketGenPane extends jmri.jmrix.powerline.swing.PowerlinePanel implements SerialListener {

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(20);
    javax.swing.JCheckBox checkBoxBinCmd = new javax.swing.JCheckBox();
    javax.swing.JTextField replyLenTextField = new javax.swing.JTextField(2);
    javax.swing.JCheckBox interlockButton = new javax.swing.JCheckBox(Bundle.getMessage("InterlockBoxLabel"));

    private SerialTrafficController tc = null;
    private SerialSystemConnectionMemo memo = null;

    public SerialPacketGenPane() {
        super();
    }

    public void init() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initContext(Object context) {
        if (context instanceof SerialSystemConnectionMemo) {
            this.memo = (SerialSystemConnectionMemo) context;
            initComponents();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.powerline.packetgen.PowerlinePacketGenPane";
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
            x.append(Bundle.getMessage("DefaultTag"));
        }
        x.append(": ");
        x.append(Bundle.getMessage("Title"));
        return x.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(SerialSystemConnectionMemo memo) {
        this.memo = memo;
        tc = memo.getTrafficController();
        
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

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(jLabel1);
        add(packetTextField);
        add(interlockButton);
        add(sendButton);

        sendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });

    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        tc.sendSerialMessage(createPacket(packetTextField.getText()), this);
    }

    SerialMessage createPacket(String s) {
        // gather bytes in result
        byte b[] = StringUtil.bytesFromHexString(s);
        SerialMessage m = memo.getTrafficController().getSerialMessage(b.length);
        for (int i = 0; i < b.length; i++) {
            m.setElement(i, b[i]);
        }
        m.setInterlocked(interlockButton.isSelected());
        return m;
    }

    @Override
    public void message(SerialMessage m) {
    }  // ignore replies

    @Override
    public void reply(SerialReply r) {
    } // ignore replies

}
