package jmri.jmrix.grapevine.packetgen;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import jmri.jmrix.grapevine.SerialMessage;
import jmri.jmrix.grapevine.SerialReply;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.util.StringUtil;

/**
 * Frame for user input of serial messages
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2003, 2006, 2007, 2008
 */
public class SerialPacketGenFrame extends jmri.util.JmriJFrame implements jmri.jmrix.grapevine.SerialListener {

    private GrapevineSystemConnectionMemo memo = null;

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    javax.swing.JTextField packetTextField = new javax.swing.JTextField(12);

    javax.swing.JButton parityButton = new javax.swing.JButton("Set Parity");

    javax.swing.JButton pollButton = new javax.swing.JButton("Query Node");
    javax.swing.JTextField uaAddrField = new javax.swing.JTextField(5);

    public SerialPacketGenFrame(GrapevineSystemConnectionMemo _memo) {
        super();
        memo = _memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        // the following code sets the frame's initial state

        jLabel1.setText("Command:");
        jLabel1.setVisible(true);

        sendButton.setText("Send");
        sendButton.setVisible(true);
        sendButton.setToolTipText("Send packet");

        packetTextField.setText("");
        packetTextField.setToolTipText("Enter command as hexadecimal bytes separated by a space");
        packetTextField.setMaximumSize(
                new Dimension(packetTextField.getMaximumSize().width,
                        packetTextField.getPreferredSize().height
                )
        );

        setTitle("Send Grapevine serial command");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(jLabel1);
        getContentPane().add(packetTextField);
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.add(parityButton);
        p1.add(sendButton);
        getContentPane().add(p1);

        sendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });

        parityButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                parityButtonActionPerformed(e);
            }
        });

        getContentPane().add(new JSeparator(JSeparator.HORIZONTAL));

        // add poll message buttons
        JPanel pane3 = new JPanel();
        pane3.setLayout(new FlowLayout());
        pane3.add(new JLabel("Address:"));
        pane3.add(uaAddrField);
        pane3.add(pollButton);
        uaAddrField.setText("0");
        uaAddrField.setToolTipText("Enter node address (decimal integer)");
        getContentPane().add(pane3);

        pollButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                pollButtonActionPerformed(e);
            }
        });
        pollButton.setToolTipText("Send poll request");

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.grapevine.packetgen.SerialPacketGenFrame", true);

        // pack for display
        pack();
    }

    public void pollButtonActionPerformed(java.awt.event.ActionEvent e) {
        SerialMessage msg = SerialMessage.getPoll(Integer.valueOf(uaAddrField.getText()).intValue());
        memo.getTrafficController().sendSerialMessage(msg, this);
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        memo.getTrafficController().sendSerialMessage(createPacket(packetTextField.getText()), this);
    }

    public void parityButtonActionPerformed(java.awt.event.ActionEvent e) {
        SerialMessage m = createPacket(packetTextField.getText());
        m.setParity();
        packetTextField.setText(m.toString());
    }

    SerialMessage createPacket(String s) {
        // gather bytes in result
        byte b[] = StringUtil.bytesFromHexString(s);
        if (b.length != 4) {
            return null;  // no such thing as message with other than 4 bytes
        }
        SerialMessage m = new SerialMessage();
        for (int i = 0; i < b.length; i++) {
            m.setElement(i, b[i]);
        }
        return m;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(SerialMessage m) {
    }  // ignore replies

    /**
     * {@inheritDoc}
     */
    @Override
    public void reply(SerialReply r) {
    } // ignore replies
}
