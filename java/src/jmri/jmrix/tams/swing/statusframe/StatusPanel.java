package jmri.jmrix.tams.swing.statusframe;

import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import jmri.JmriException;
import jmri.jmrix.tams.TamsListener;
import jmri.jmrix.tams.TamsMessage;
import jmri.jmrix.tams.TamsReply;
import jmri.jmrix.tams.TamsSystemConnectionMemo;
import jmri.jmrix.tams.TamsTrafficController;

/**
 * Panel to show TAMS status.
 *
 * Based on work by Bob Jacobsen
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 */
public class StatusPanel extends jmri.jmrix.tams.swing.TamsPanel implements TamsListener {

    String appString = Bundle.getMessage("ApplicationVersionLabel");
    String serString = Bundle.getMessage("SerialNumberLabel");
    JLabel appVersion = new JLabel(appString + Bundle.getMessage("StateUnknown")); // "<unknown>"
    JLabel serVersion = new JLabel(serString + Bundle.getMessage("StateUnknown"));

    JButton sendButton;

    public StatusPanel() {
        super();
    }

    @Override
    public void initComponents(TamsSystemConnectionMemo memo) {
        super.initComponents(memo);
        //memo.getTrafficController().addTamsListener(this);
        tc = memo.getTrafficController();
        // Create GUI
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(appVersion);
        add(serVersion);

        // ask to be notified
        TamsMessage m = new TamsMessage("xV");
        tc.sendTamsMessage(m, this);

        sendButton = new JButton(Bundle.getMessage("ButtonUpdate"));
        sendButton.setVisible(true);
        sendButton.setToolTipText(Bundle.getMessage("ButtonUpdateToolTip"));

        add(sendButton);
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                sendButtonActionPerformed(e);
            }
        });
    }

    void reset() {
        appVersion.setText(appString + Bundle.getMessage("StateUnknown"));
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        tc.removeTamsListener(this);
        tc = null;
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        reset();
        TamsMessage m = new TamsMessage("xV");
        tc.sendTamsMessage(m, this);
    }

    TamsTrafficController tc;

    // to listen for status changes from Tams system
    @Override
    public void reply(TamsReply m) {
        // power message?
        String msg = m.toString();
        String[] version = msg.split("\\r");
        appVersion.setText(appString + version[0]);
        serVersion.setText(serString + version[1]);
    }

    @Override
    public void message(TamsMessage m) {
        // messages are ignored
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.tams.swing.TamsNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemInfo"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    StatusPanel.class.getName(),
                    jmri.InstanceManager.getDefault(TamsSystemConnectionMemo.class));
        }
    }

}
