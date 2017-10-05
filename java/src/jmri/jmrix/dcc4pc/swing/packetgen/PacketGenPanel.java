package jmri.jmrix.dcc4pc.swing.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.jmrix.dcc4pc.Dcc4PcListener;
import jmri.jmrix.dcc4pc.Dcc4PcMessage;
import jmri.jmrix.dcc4pc.Dcc4PcReply;
import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import jmri.jmrix.dcc4pc.swing.Dcc4PcPanelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user input of Dcc4Pc messages
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Dan Boudreau Copyright (C) 2007
 * @author Kevin Dickerson Copyright (C) 2015
 */
public class PacketGenPanel extends jmri.jmrix.dcc4pc.swing.Dcc4PcPanel implements Dcc4PcListener, Dcc4PcPanelInterface {

    // member declarations
    JLabel jLabel1 = new JLabel();
    JButton sendButton = new JButton();
    JTextField packetTextField = new JTextField(20);
    JCheckBox childBoardBox = new JCheckBox("Child Board");
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
            jLabel1.setText("Command: ");
            jLabel1.setVisible(true);

            sendButton.setText("Send");
            sendButton.setVisible(true);
            sendButton.setToolTipText("Send packet");

            packetTextField.setText("");
            packetTextField.setToolTipText("Enter command");
            packetTextField.setMaximumSize(new Dimension(packetTextField
                    .getMaximumSize().width, packetTextField.getPreferredSize().height));

            childBoardBox.setSelected(false);
                    
            add(jLabel1);
            add(packetTextField);
            add(childBoardBox);
            add(sendButton);

            sendButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    sendButtonActionPerformed(e);
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.dcc4pc.swing.packetgen.PacketGenFrame";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "Send DCC4PC command";
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {

        String text = packetTextField.getText();
        if (text.startsWith("0x")) {
            hexStringToByteArray(text);
            return;
        }
        Dcc4PcMessage m = new Dcc4PcMessage(text);
        m.setForChildBoard(childBoardBox.isSelected());
        memo.getDcc4PcTrafficController().sendDcc4PcMessage(m, null);
    }

    public void hexStringToByteArray(String s) {
        s = s.substring(2);
        int len = s.length();
        byte[] data = new byte[len / 2];
        int loc = 0;
        Dcc4PcMessage m = new Dcc4PcMessage((len / 2));
        for (int i = 0; i < data.length; i++) {
            int val = (byte) ((Character.digit(s.charAt(loc), 16) << 4)
                    + Character.digit(s.charAt(loc + 1), 16));
            m.setElement(i, val);
            loc = loc + 2;
        }
        m.setForChildBoard(childBoardBox.isSelected());
        if(memo.getDcc4PcTrafficController()!=null){
            memo.getDcc4PcTrafficController().sendDcc4PcMessage(m, null);
        } else {
            log.error("no Traffic Controller Found");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleTimeout(Dcc4PcMessage m) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(Dcc4PcMessage m) {
    }  // ignore replies

    /**
     * {@inheritDoc}
     */
    @Override
    public void reply(Dcc4PcReply r) {
    } // ignore replies
    
    private final static Logger log = LoggerFactory.getLogger(PacketGenPanel.class);
    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.dcc4pc.swing.Dcc4PcNamedPaneAction {

        public Default() {
            super("Dcc4PC Command Monitor",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    PacketGenPanel.class.getName(),
                    jmri.InstanceManager.getDefault(Dcc4PcSystemConnectionMemo.class));
        }
    }

}
