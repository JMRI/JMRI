package jmri.jmrix.dccpp.dccppovertcp;

import java.awt.event.ActionEvent;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoInitialize;
import jmri.implementation.AbstractInstanceInitializer;
import org.openide.util.lookup.ServiceProvider;

/**
 * Frame displaying and programming a DCCpp clock monitor.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2004
 * @author Alex Shepherd Copyright (C) 2006
 * @author Mark Underwood Copyright (C) 2015
 */
public class ServerFrame extends jmri.util.JmriJFrame implements ServerListner, InstanceManagerAutoInitialize {

    private ServerFrame() {
        super("DCCppOverTcp Server");

        super.getContentPane().setLayout(new BoxLayout(super.getContentPane(), BoxLayout.Y_AXIS));

        portNumber = new JSpinner();
        portNumberModel = new SpinnerNumberModel(65535, 1, 65535, 1);
        portNumber.setModel(portNumberModel);

        portNumber.setFocusable(false);

        // add GUI items
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(autoStartCheckBox);
        panel.add(portNumberLabel);
        panel.add(portNumber);
        super.getContentPane().add(panel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(startButton);
        panel.add(stopButton);
        panel.add(saveButton);
        super.getContentPane().add(panel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(serverStatus);
        panel.add(clientStatus);
        super.getContentPane().add(panel);

        startButton.addActionListener((ActionEvent a) -> {
            InstanceManager.getDefault(Server.class).enable();
        });

        stopButton.addActionListener((ActionEvent a) -> {
            InstanceManager.getDefault(Server.class).disable();
        });

        saveButton.addActionListener((ActionEvent a) -> {
            InstanceManager.getDefault(Server.class).setAutoStart(autoStartCheckBox.isSelected());
            InstanceManager.getDefault(Server.class).setPortNumber(((Integer) portNumber.getValue()));
            InstanceManager.getDefault(Server.class).saveSettings();
        });

        autoStartCheckBox.addActionListener((ActionEvent a) -> {
            saveButton.setEnabled(true);
        });

        if (portNumber != null) {
            portNumber.addChangeListener((ChangeEvent e) -> {
                saveButton.setEnabled(true);
            });
        }

        super.pack();
    }

    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        InstanceManager.getDefault(Server.class).setStateListner(null);
        InstanceManager.deregister(this, ServerFrame.class);
        dispose();
        super.windowClosing(e);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     *
     * @return the managed instance
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    static public synchronized ServerFrame getInstance() {
        return InstanceManager.getDefault(ServerFrame.class);
    }

    private void updateServerStatus() {
        Server server = InstanceManager.getDefault(Server.class);
        autoStartCheckBox.setSelected(server.getAutoStart());
        autoStartCheckBox.setEnabled(!server.isEnabled());
        if (portNumber != null) {
            portNumber.setValue(server.getPortNumber());
            portNumber.setEnabled(!server.isEnabled());
            portNumberLabel.setEnabled(!server.isEnabled());
        }
        startButton.setEnabled(!server.isEnabled());
        stopButton.setEnabled(server.isEnabled());
        saveButton.setEnabled(server.isSettingChanged());
        serverStatus.setText("Server Status: " + (server.isEnabled() ? "Enabled" : "Disabled")); // TODO I18N, also below
    }

    private void updateClientStatus() {
        clientStatus.setText("   Client Count: " + Integer.toString(InstanceManager.getDefault(Server.class).getClientCount()));
    }

    @Override
    public void notifyServerStateChanged(Server s) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            updateServerStatus();
        });
    }

    @Override
    public void notifyClientStateChanged(Server s) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            updateClientStatus();
        });
    }

    JSpinner portNumber;
    SpinnerNumberModel portNumberModel;
    JLabel portNumberLabel = new JLabel("  Port Number: ");
    JLabel serverStatus = new JLabel("Server Status:         ");
    JLabel clientStatus = new JLabel("   Client Count:  ");

    JCheckBox autoStartCheckBox = new JCheckBox(
            "Start Server at Application Startup");
    JButton startButton = new JButton("Start Server");
    JButton stopButton = new JButton("Stop Server");
    JButton saveButton = new JButton("Save Settings");

    @Override
    public void initialize() {
        Server server = InstanceManager.getDefault(Server.class);
        server.setStateListner(this);
        server.updateServerStateListener();
        server.updateClinetStateListener();
    }

    @ServiceProvider(service = InstanceInitializer.class)
    public static class Initializer extends AbstractInstanceInitializer {

        @Override
        public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
            if (type.equals(ServerFrame.class)) {
                return new ServerFrame();
            }
            return super.getDefault(type);
        }

        @Override
        public Set<Class<?>> getInitalizes() {
            Set<Class<?>> set = super.getInitalizes();
            set.add(ServerFrame.class);
            return set;
        }
    }
}
