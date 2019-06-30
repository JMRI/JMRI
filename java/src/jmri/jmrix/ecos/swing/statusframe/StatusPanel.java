package jmri.jmrix.ecos.swing.statusframe;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.JmriException;
import jmri.jmrix.ecos.EcosListener;
import jmri.jmrix.ecos.EcosMessage;
import jmri.jmrix.ecos.EcosReply;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.jmrix.ecos.EcosTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel to show ECoS status
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class StatusPanel extends jmri.jmrix.ecos.swing.EcosPanel implements EcosListener {

    JPanel statusPanel = new JPanel();
    String appString = Bundle.getMessage("ApplicationVersionLabel") + " ";
    String proString = Bundle.getMessage("ProtocolVersionLabel") + " ";
    String hrdString = Bundle.getMessage("HardwareVersionLabel") + " ";
    JLabel appVersion = new JLabel(appString + Bundle.getMessage("StateUnknown"));
    JLabel proVersion = new JLabel(proString + Bundle.getMessage("StateUnknown"));
    JLabel hrdVersion = new JLabel(hrdString + Bundle.getMessage("StateUnknown"));

    JButton sendButton;

    public StatusPanel() {
        super();
    }

    @Override
    public void initComponents(EcosSystemConnectionMemo memo) {
        super.initComponents(memo);
        //memo.getTrafficController().addEcosListener(this);
        tc = memo.getTrafficController();
        // Create GUI
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(appVersion);
        statusPanel.add(proVersion);
        statusPanel.add(hrdVersion);
        add(statusPanel);

        try {
            // connect to the TrafficManager
            tc.addEcosListener(this);

            // ask to be notified
            EcosMessage m = new EcosMessage("request(1, view)");
            tc.sendEcosMessage(m, this);

            // get initial state
            m = new EcosMessage("get(1, info)");
            tc.sendEcosMessage(m, this);
        } catch (NullPointerException npe) {
            log.warn("Could not connect to ECoS connection {}", memo);
        }
        sendButton = new JButton(Bundle.getMessage("ButtonUpdate"));
        sendButton.setVisible(true);
        sendButton.setToolTipText(Bundle.getMessage("UpdateToolTip"));

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
        proVersion.setText(proString + Bundle.getMessage("StateUnknown"));
        hrdVersion.setText(hrdString + Bundle.getMessage("StateUnknown"));
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        tc.removeEcosListener(this);
        tc = null;
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        reset();
        try {
            EcosMessage m = new EcosMessage("get(1, info)");
            tc.sendEcosMessage(m, null);
        } catch (NullPointerException npe) {
            log.warn("Could not connect to ECoS connection {}", memo);
        }
    }

    EcosTrafficController tc;

    // to listen for status changes from Ecos system
    @Override
    public void reply(EcosReply m) {
        // power message?
        String msg = m.toString();
        if (msg.contains("<EVENT 1>") || msg.contains("REPLY get(1,")) {
            if (msg.contains("info")) {
                // probably right, extract info
                int first;
                int last;
                first = msg.indexOf("ProtocolVersion[");
                if (first > 0) {
                    last = msg.indexOf("]", first + 16);
                    proVersion.setText(proString + msg.substring(first + 16, last));
                }
                first = msg.indexOf("ApplicationVersion[");
                if (first > 0) {
                    last = msg.indexOf("]", first + 19);
                    appVersion.setText(appString + msg.substring(first + 19, last));
                }
                first = msg.indexOf("HardwareVersion[");
                if (first > 0) {
                    last = msg.indexOf("]", first + 16);
                    hrdVersion.setText(hrdString + msg.substring(first + 16, last));
                }
            }
        }
    }

    @Override
    public void message(EcosMessage m) {
        // messages are ignored
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            return Bundle.getMessage("XInfoTitle", memo.getUserName());
        }
        return Bundle.getMessage("MenuItemInfo");
    }

    /**
     * Nested class to create one of these using old-style defaults.
     */
    static public class Default extends jmri.jmrix.ecos.swing.EcosNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("MenuItemInfo"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    StatusPanel.class.getName(),
                    jmri.InstanceManager.getDefault(EcosSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(StatusPanel.class);

}
