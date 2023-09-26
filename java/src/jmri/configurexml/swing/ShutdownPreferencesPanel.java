package jmri.configurexml.swing;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.*;

import org.openide.util.lookup.ServiceProvider;

import jmri.InstanceManager;
import jmri.swing.PreferencesPanel;
import jmri.configurexml.ShutdownPreferences;
import jmri.configurexml.ShutdownPreferences.DialogDisplayOptions;

/**
 * Preferences panel for the shutdown options used by StoreAndCompare.
 *
 * @author Dave Sand Copyright 2022
 */
@ServiceProvider(service = PreferencesPanel.class)
public class ShutdownPreferencesPanel extends JPanel implements PreferencesPanel {

    private final ShutdownPreferences _preferences;

    private JCheckBox _enableCheckBox;
    private JComboBox<DialogDisplayOptions> _displayActionComboBox;
    private JCheckBox _timebase;
    private JCheckBox _sensorIconColor;

    public ShutdownPreferencesPanel() {
        _preferences = InstanceManager.getDefault(ShutdownPreferences.class);
        initGUI();
    }

    private void initGUI() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(getPanel());
    }

    /**
     * set the local prefs to match the GUI Local prefs are independent from the
     * singleton instance prefs.
     *
     * @return true if set, false if values are unacceptable.
     */
    private boolean setValues() {
        boolean didSet = true;
        _preferences.setEnableStoreCheck(_enableCheckBox.isSelected());
        _preferences.setIgnoreTimebase(_timebase.isSelected());
        _preferences.setIgnoreSensorColors(_sensorIconColor.isSelected());
        _preferences.setDisplayDialog((DialogDisplayOptions) _displayActionComboBox.getSelectedItem());
        return didSet;
    }

    private JPanel getPanel() {
        _enableCheckBox = new JCheckBox(Bundle.getMessage("LabelEnableCheckbox"));
        _enableCheckBox.setSelected(_preferences.isStoreCheckEnabled());

        _timebase = new JCheckBox(Bundle.getMessage("IgnoreTimebase"));
        _timebase.setSelected(_preferences.isIgnoreTimebaseEnabled());

        _sensorIconColor = new JCheckBox(Bundle.getMessage("IgnoreSensorColor"));
        _sensorIconColor.setSelected(_preferences.isIgnoreSensorColorsEnabled());

        _displayActionComboBox = new JComboBox<>();
        for (DialogDisplayOptions opt : DialogDisplayOptions.values()) {
            _displayActionComboBox.addItem(opt);
            if (opt == _preferences.getDisplayDialog()) {
                _displayActionComboBox.setSelectedItem(opt);
            }
        }

        JPanel gridPanel = new JPanel(new GridLayout(0, 1));
        gridPanel.add(_enableCheckBox);
        gridPanel.add(_displayActionComboBox);
        gridPanel.add(new JLabel());
        gridPanel.add(new jmri.swing.JTitledSeparator(Bundle.getMessage("IgnoreSeparator")));
        gridPanel.add(_timebase);
        gridPanel.add(_sensorIconColor);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
        panel.add(gridPanel);

        return panel;
    }

    @Override
    public String getPreferencesItem() {
        return "SHUTDOWN"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("ShutdownMenu"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return getPreferencesItemText();
    }

    @Override
    public String getLabelKey() {
        return Bundle.getMessage("ShutdownPrefLabel");
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
        return null;
    }

    @Override
    public void savePreferences() {
        if (setValues()) {
            _preferences.save();
        }
    }

    @Override
    public boolean isDirty() {
        return _preferences.isDirty();
    }

    @Override
    public boolean isRestartRequired() {
        return _preferences.isRestartRequired();
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }

    @Override
    public int getSortOrder() {
        return 450; // Place between "Start Up" and "Display"
    }
}
