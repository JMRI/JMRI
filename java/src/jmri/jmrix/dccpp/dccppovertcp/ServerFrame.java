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
 * Frame displaying and programming a DCCppovertcp server.
 * <p>
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
        panel.add(statusLabel);
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
        updateClientStatus(server);
    }

    private void updateClientStatus(Server s) {
        statusLabel.setText(Bundle.getMessage("StatusLabel", (s.isEnabled() ? Bundle.getMessage("Running") : Bundle.getMessage("Stopped")), s.getClientCount()));
        // combined status and count in 1 field, like LnTcpServer
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
            updateClientStatus(s);
        });
    }

    JSpinner portNumber;
    SpinnerNumberModel portNumberModel;
    JLabel portNumberLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("LabelPort")));
    private final JLabel statusLabel = new JLabel(Bundle.getMessage("StatusLabel", Bundle.getMessage("Stopped"), 0));
    JCheckBox autoStartCheckBox = new JCheckBox(Bundle.getMessage("LabelStartup"));
    JButton startButton = new JButton(Bundle.getMessage("StartServer"));
    JButton stopButton = new JButton(Bundle.getMessage("StopServer"));
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

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
        public <T> Object getDefault(Class<T> type) {
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
