package jmri.jmrix.loconet.loconetovertcp;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.util.JmriJFrame;

/**
 * Frame displaying the status of the a LocoNet over TCP server.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2004
 * @author Alex Shepherd Copyright (C) 2006
 * @author Randall Wood Copyright (C) 2017
 */
public class LnTcpServerFrame extends JmriJFrame {

    private final JLabel portNumberLabel = new JLabel(Bundle.getMessage("PortLabel", 1234));
    private final JLabel statusLabel = new JLabel(Bundle.getMessage("StatusLabel", Bundle.getMessage("Stopped"), 0));

    private final JButton startButton = new JButton(Bundle.getMessage("StartServer"));
    private final JButton stopButton = new JButton(Bundle.getMessage("StopServer"));
    private final LnTcpServer server;
    private final LnTcpServerListener listener;

    /**
     * Create a LocoNet over TCP server status window.
     *
     * @param server the server to monitor
     */
    public LnTcpServerFrame(LnTcpServer server) {
        super(Bundle.getMessage("ServerAction"));
        this.server = server;

        super.getContentPane().setLayout(new BoxLayout(super.getContentPane(), BoxLayout.Y_AXIS));

        // add GUI items
        portNumberLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        super.getContentPane().add(portNumberLabel);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(startButton);
        panel.add(stopButton);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        super.getContentPane().add(panel);

        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        super.getContentPane().add(statusLabel);

        startButton.addActionListener((ActionEvent a) -> {
            server.enable();
        });

        stopButton.addActionListener((ActionEvent a) -> {
            server.disable();
        });

        this.listener = new LnTcpServerListener() {
            @Override
            public void notifyServerStateChanged(LnTcpServer s) {
                if (s.equals(LnTcpServerFrame.this.server)) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        LnTcpServerFrame.this.updateServerStatus(s);
                    });
                }
            }

            @Override
            public void notifyClientStateChanged(LnTcpServer s) {
                if (s.equals(LnTcpServerFrame.this.server)) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        LnTcpServerFrame.this.updateClientStatus(s);
                    });
                }
            }
        };
        server.addStateListener(listener);
        this.updateServerStatus(server);
        super.pack();
    }

    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        this.server.removeStateListener(this.listener);
        dispose();
        super.windowClosing(e);
        InstanceManager.deregister(this, LnTcpServerFrame.class);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * Get the server status window for the default LocoNet over TCP server.
     *
     * @return the default server frame instance, creating it if needed
     */
    static public synchronized LnTcpServerFrame getDefault() {
        return InstanceManager.getOptionalDefault(LnTcpServerFrame.class).orElseGet(() -> {
            return InstanceManager.setDefault(LnTcpServerFrame.class, new LnTcpServerFrame(LnTcpServer.getDefault()));
        });
    }

    private void updateServerStatus(LnTcpServer s) {
        portNumberLabel.setText(Bundle.getMessage("PortLabel", s.getPort()));
        startButton.setEnabled(!s.isEnabled());
        stopButton.setEnabled(s.isEnabled());
        this.updateClientStatus(s);
    }

    private void updateClientStatus(LnTcpServer s) {
        statusLabel.setText(Bundle.getMessage("StatusLabel", (s.isEnabled() ? Bundle.getMessage("Running") : Bundle.getMessage("Stopped")), s.getClientCount()));
    }

}
