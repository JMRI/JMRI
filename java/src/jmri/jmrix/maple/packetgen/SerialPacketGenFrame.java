package jmri.jmrix.maple.packetgen;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jmri.jmrix.maple.InputBits;
import jmri.jmrix.maple.SerialMessage;
import jmri.jmrix.maple.SerialReply;
import jmri.jmrix.maple.MapleSystemConnectionMemo;
import jmri.util.StringUtil;

/**
 * Frame for user input of serial messages.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2003
 */
public class SerialPacketGenFrame extends jmri.util.JmriJFrame implements jmri.jmrix.maple.SerialListener {

    private MapleSystemConnectionMemo _memo = null;

    // member declarations
    JLabel jLabel1 = new JLabel();
    JButton sendButton = new JButton();
    JTextField packetTextField = new JTextField(12);

    JButton pollButton = new JButton(Bundle.getMessage("LabelPoll")); // I18N using jmrix.Bundle
    protected JSpinner nodeAddrSpinner;

    public SerialPacketGenFrame(MapleSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        // the following code sets the frame's initial state

        jLabel1.setText(Bundle.getMessage("CommandLabel"));
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

        setTitle(Bundle.getMessage("SendXCommandTitle", "Maple"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(jLabel1);
        getContentPane().add(packetTextField);
        getContentPane().add(sendButton);

        sendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });

        getContentPane().add(new JSeparator(JSeparator.HORIZONTAL));

        // add poll message buttons
        JPanel pane3 = new JPanel();
        pane3.setLayout(new FlowLayout());
        pane3.add(new JLabel(Bundle.getMessage("LabelNodeAddress")));
        nodeAddrSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        nodeAddrSpinner.setToolTipText(Bundle.getMessage("TooltipNodeAddress"));
        pane3.add(nodeAddrSpinner);
        pane3.add(pollButton);
        getContentPane().add(pane3);

        pollButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                pollButtonActionPerformed(e);
            }
        });
        pollButton.setToolTipText(Bundle.getMessage("PollToolTipMulti"));

        // pack for display
        pack();
    }

    public void pollButtonActionPerformed(java.awt.event.ActionEvent e) {
        int endAddr = InputBits.getNumInputBits();
        if (endAddr > 99) {
            endAddr = 99;
        }
        SerialMessage msg = SerialMessage.getPoll((Integer) nodeAddrSpinner.getValue(), 1, endAddr);
        _memo.getTrafficController().sendSerialMessage(msg, this);
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        _memo.getTrafficController().sendSerialMessage(createPacket(packetTextField.getText()), this);
    }

    SerialMessage createPacket(String s) {
        // gather bytes in result
        byte b[] = StringUtil.bytesFromHexString(s);
        if (b.length == 0) {
            return null;  // no such thing as a zero-length message
        }
        SerialMessage m = new SerialMessage(b.length);
        for (int i = 0; i < b.length; i++) {
            m.setElement(i, b[i]);
        }
        return m;
    }

    /** 
     * {@inheritDoc}
     * Ignore messages.
     */
    @Override
    public void message(SerialMessage m) {
    }

    /** 
     * {@inheritDoc}
     * Ignore replies.
     */
    @Override
    public void reply(SerialReply r) {
    }

}
