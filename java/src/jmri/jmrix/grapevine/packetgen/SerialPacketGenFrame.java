package jmri.jmrix.grapevine.packetgen;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JTextField;
import jmri.jmrix.grapevine.SerialMessage;
import jmri.jmrix.grapevine.SerialReply;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user input of serial messages.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2003, 2006, 2007, 2008, 2018
 */
public class SerialPacketGenFrame extends jmri.util.JmriJFrame implements jmri.jmrix.grapevine.SerialListener {

    private GrapevineSystemConnectionMemo memo = null;

    // member declarations
    javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    javax.swing.JButton sendButton = new javax.swing.JButton();
    JTextField packetTextField = new JTextField(12);

    javax.swing.JButton parityButton = new javax.swing.JButton(Bundle.getMessage("ButtonSetParity"));

    javax.swing.JButton pollButton = new javax.swing.JButton(Bundle.getMessage("ButtonQueryNode"));
    protected JSpinner nodeAddrSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

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

        setTitle(Bundle.getMessage("SendXCommandTitle", Bundle.getMessage("MenuSystem")));
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

        pane3.add(new JLabel(Bundle.getMessage("LabelNodeAddress")));

        pane3.add(nodeAddrSpinner);
        nodeAddrSpinner.setToolTipText(Bundle.getMessage("TooltipNodeAddress"));

        pane3.add(pollButton);
        getContentPane().add(pane3);

        pollButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                pollButtonActionPerformed(e);
            }
        });
        pollButton.setToolTipText(Bundle.getMessage("PollToolTip"));

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.grapevine.packetgen.SerialPacketGenFrame", true);

        // pack for display
        pack();
    }

    public void pollButtonActionPerformed(java.awt.event.ActionEvent e) {
        SerialMessage msg = SerialMessage.getPoll((Integer) nodeAddrSpinner.getValue());
        memo.getTrafficController().sendSerialMessage(msg, this);
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        memo.getTrafficController().sendSerialMessage(createPacket(packetTextField.getText()), this);
    }

    public void parityButtonActionPerformed(java.awt.event.ActionEvent e) {
        SerialMessage m = createPacket(packetTextField.getText());
        if (m == null) {
            return;
        }
        m.setParity();
        packetTextField.setText(m.toString());
    }

    SerialMessage createPacket(String s) {
        // gather bytes in result
        byte b[] = StringUtil.bytesFromHexString(s);
        if (b.length != 4) {
            log.warn("Grapevine createPacket not 4 bytes");
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("ErrorInvalidMessageLength"),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            return null; // no such thing as message with other than 4 bytes
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
    } // ignore replies

    /**
     * {@inheritDoc}
     */
    @Override
    public void reply(SerialReply r) {
    } // ignore replies

    private final static Logger log = LoggerFactory.getLogger(SerialPacketGenAction.class);

}
