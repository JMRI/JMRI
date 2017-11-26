package jmri.jmris.json;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import jmri.InstanceManager;
import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Randall Wood Copyright (C) 2012, 2015
 */
@ServiceProvider(service = PreferencesPanel.class)
public class JsonServerPreferencesPanel extends JPanel implements PreferencesPanel {

    public static final int MAX_HEARTBEAT_INTERVAL = 3600;
    public static final int MIN_HEARTBEAT_INTERVAL = 1;
    private JSpinner heartbeatIntervalSpinner;
    private JSpinner port;
    private JsonServerPreferences preferences;
    private JFrame parentFrame = null;

    public JsonServerPreferencesPanel() {
        this.preferences = new JsonServerPreferences();
        this.preferences.apply(InstanceManager.getDefault(JsonServerPreferences.class));
        initGUI();
        setGUI();
    }

    public JsonServerPreferencesPanel(JFrame f) {
        this();
        parentFrame = f;
    }

    private void initGUI() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(new JTitledSeparator(Bundle.getMessage("ServerSectionTitle")));
        add(portPanel());
        add(new JTitledSeparator(Bundle.getMessage("JSONSectionTitle")));
        add(heartbeatPanel());
        add(Box.createVerticalGlue());
    }

    private void setGUI() {
        heartbeatIntervalSpinner.setValue(preferences.getHeartbeatInterval() / 1000); // convert from milliseconds to seconds
        port.setValue(preferences.getPort());
    }

    /**
     * set the local prefs to match the GUI Local prefs are independent from the
     * singleton instance prefs.
     *
     * @return true if set, false if values are unacceptable.
     */
    private boolean setValues() {
        boolean didSet = true;
        preferences.setHeartbeatInterval((int) heartbeatIntervalSpinner.getValue() * 1000); // convert to milliseconds from seconds
        preferences.setPort((int) port.getValue());
        return didSet;
    }

    private JPanel heartbeatPanel() {
        JPanel panel = new JPanel();
        heartbeatIntervalSpinner = new JSpinner(new SpinnerNumberModel(15, MIN_HEARTBEAT_INTERVAL, MAX_HEARTBEAT_INTERVAL, 1));
        ((JSpinner.DefaultEditor) heartbeatIntervalSpinner.getEditor()).getTextField().setEditable(true);
        this.heartbeatIntervalSpinner.addChangeListener((ChangeEvent e) -> {
            this.setValues();
        });
        this.heartbeatIntervalSpinner.setToolTipText(Bundle.getMessage("HeartbeatToolTip", MIN_HEARTBEAT_INTERVAL, MAX_HEARTBEAT_INTERVAL));
        panel.add(heartbeatIntervalSpinner);
        JLabel label = new JLabel(Bundle.getMessage("HeartbeatLabel"));
        label.setToolTipText(this.heartbeatIntervalSpinner.getToolTipText());
        panel.add(label);
        return panel;
    }

    private JPanel portPanel() {
        JPanel panel = new JPanel();
        port = new JSpinner(new SpinnerNumberModel(JsonServerPreferences.DEFAULT_PORT, 1, 65535, 1));
        ((JSpinner.DefaultEditor) port.getEditor()).getTextField().setEditable(true);
        port.setEditor(new JSpinner.NumberEditor(port, "#"));
        this.port.addChangeListener((ChangeEvent e) -> {
            this.setValues();
        });
        this.port.setToolTipText(Bundle.getMessage("PortToolTip"));
        panel.add(port);
        JLabel label = new JLabel(Bundle.getMessage("LabelPort"));
        label.setToolTipText(this.port.getToolTipText());
        panel.add(label);
        return panel;
    }

    @Override
    public String getPreferencesItem() {
        return Bundle.getMessage("PreferencesItem");
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("PreferencesItemTitle");
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return Bundle.getMessage("PreferencesTabTitle");
    }

    @Override
    public String getLabelKey() {
        return null;
    }

    @Override
    public JComponent getPreferencesComponent() {
        return this;
    }

    @Override
    public boolean isPersistant() {
        return false;
    }

    @Override
    public String getPreferencesTooltip() {
        return Bundle.getMessage("PreferencesTooltip");
    }

    @Override
    public void savePreferences() {
        if (this.setValues()) {
            InstanceManager.getDefault(JsonServerPreferences.class).apply(this.preferences);
            InstanceManager.getDefault(JsonServerPreferences.class).save();
            if (this.parentFrame != null) {
                this.parentFrame.dispose();
            }
        }
    }

    @Override
    public boolean isDirty() {
        return this.preferences.compareValuesDifferent(InstanceManager.getDefault(JsonServerPreferences.class))
                || InstanceManager.getDefault(JsonServerPreferences.class).isDirty();
    }

    @Override
    public boolean isRestartRequired() {
        return InstanceManager.getDefault(JsonServerPreferences.class).isRestartRequired();
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }
}
