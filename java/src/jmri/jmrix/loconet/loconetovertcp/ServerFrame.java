package jmri.jmrix.loconet.loconetovertcp;

import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.util.JmriJFrame;

/**
 * Frame displaying and programming a LocoNet clock monitor.
 * <P>
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
public class ServerFrame extends JmriJFrame implements LnTcpServerListener {

    JLabel portNumber;
    JLabel portNumberLabel = new JLabel("  Port Number: ");
    JLabel serverStatus = new JLabel("Server Status:         ");
    JLabel clientStatus = new JLabel("   Client Count:  ");

    JButton startButton = new JButton("Start Server");
    JButton stopButton = new JButton("Stop Server");

    private ServerFrame() {
        super(Bundle.getMessage("ServerAction"));

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        portNumber = new JLabel();

        portNumber.setFocusable(false);

        // add GUI items
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(portNumberLabel);
        panel.add(portNumber);
        getContentPane().add(panel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(startButton);
        panel.add(stopButton);
        getContentPane().add(panel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(serverStatus);
        panel.add(clientStatus);
        getContentPane().add(panel);

        startButton.addActionListener((ActionEvent a) -> {
            LnTcpServer.getDefault().enable();
        });

        stopButton.addActionListener((ActionEvent a) -> {
            LnTcpServer.getDefault().disable();
        });

        pack();
    }

    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        LnTcpServer.getDefault().setStateListner(null);
        dispose();
        super.windowClosing(e);
        InstanceManager.deregister(this, ServerFrame.class);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     *
     * @return the default server frame instance
     * @deprecated since 4.7.5; use {@link #getDefault() } instead
     */
    @Deprecated
    static public ServerFrame getInstance() {
        return getDefault();
    }

    /**
     * Get the default server frame instance.
     *
     * @return the default server frame instance, creating it if needed
     */
    static public synchronized ServerFrame getDefault() {
        return InstanceManager.getOptionalDefault(ServerFrame.class).orElseGet(() -> {
            ServerFrame self = new ServerFrame();
            LnTcpServer server = LnTcpServer.getDefault();
            server.setStateListner(self);
            server.updateServerStateListener();
            server.updateClientStateListener();
            return InstanceManager.setDefault(ServerFrame.class, self);
        });
    }

    private void updateServerStatus() {
        LnTcpServer server = LnTcpServer.getDefault();
        if (portNumber != null) {
            portNumber.setText(Integer.toString(server.getPortNumber()));
            portNumberLabel.setEnabled(!server.isEnabled());
        }
        startButton.setEnabled(!server.isEnabled());
        stopButton.setEnabled(server.isEnabled());
        serverStatus.setText("Server Status: " + (server.isEnabled() ? "Enabled" : "Disabled"));
    }

    private void updateClientStatus() {
        clientStatus.setText("   Client Count: " + Integer.toString(LnTcpServer.getDefault().getClientCount()));
    }

    @Override
    public void notifyServerStateChanged(LnTcpServer s) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            updateServerStatus();
        });
    }

    @Override
    public void notifyClientStateChanged(LnTcpServer s) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            updateClientStatus();
        });
    }

}
