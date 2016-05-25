// PacketGenFrame.java
package jmri.jmrix.dcc4pc.swing.packetgen;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import jmri.jmrix.dcc4pc.Dcc4PcListener;
import jmri.jmrix.dcc4pc.Dcc4PcMessage;
import jmri.jmrix.dcc4pc.Dcc4PcReply;
import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;

/**
 * Frame for user input of Dcc4Pc messages
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2008
 * @author Dan Boudreau Copyright (C) 2007
 * @version $Revision: 17977 $
 */
public class PacketGenPanel extends jmri.jmrix.dcc4pc.swing.Dcc4PcPanel implements Dcc4PcListener {

    /**
     *
     */
    private static final long serialVersionUID = -3220732618500304699L;
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
        return "package.jmri.jmrix.dcc4pc.swing.packetgen.PacketGenFrame";
    }

    public String getTitle() {
        return "Send DCC4PC command";
    }

    public void initComponents(Dcc4PcSystemConnectionMemo memo) {
        super.initComponents(memo);

        //memo.getTrafficController().addDcc4PcListener(this);
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {

        String text = packetTextField.getText();
        if (text.startsWith("0x")) {
            hexStringToByteArray(text);
            return;
        }
        Dcc4PcMessage m = new Dcc4PcMessage(text);
        memo.getDcc4PcTrafficController().sendDcc4PcMessage(m, null);

        /*Dcc4PcMessage m = new Dcc4PcMessage(packetTextField.getText().length());
         for (int i = 0; i < packetTextField.getText().length(); i++)
         m.setElement(i, packetTextField.getText().charAt(i));

         memo.getDcc4PcTrafficController().sendDcc4PcMessage(m, this);*/
    }

    public void hexStringToByteArray(String s) {
        s = s.substring(2);
        System.out.println(s);
        int len = s.length();
        byte[] data = new byte[len / 2];
        int loc = 0;
        Dcc4PcMessage m = new Dcc4PcMessage((len / 2));
        for (int i = 0; i < data.length; i++) {
            int val = (byte) ((Character.digit(s.charAt(loc), 16) << 4)
                    + Character.digit(s.charAt(loc + 1), 16));
            System.out.println("i " + i + " " + val);
            m.setElement(i, val);
            loc = loc + 2;
        }
        //Dcc4PcMessage m = new Dcc4PcMessage(len);

        memo.getDcc4PcTrafficController().sendDcc4PcMessage(m, null);
    }

    public void handleTimeout(Dcc4PcMessage m) {
    }

    public void message(Dcc4PcMessage m) {
    }  // ignore replies

    public void reply(Dcc4PcReply r) {
    } // ignore replies

    public void processingData() {
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.dcc4pc.swing.Dcc4PcNamedPaneAction {

        /**
         *
         */
        private static final long serialVersionUID = -82246637685914738L;

        public Default() {
            super("Dcc4PC Command Monitor",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    PacketGenPanel.class.getName(),
                    jmri.InstanceManager.getDefault(Dcc4PcSystemConnectionMemo.class));
        }
    }

}
