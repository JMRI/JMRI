package jmri.configurexml.swing;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.*;

import org.openide.util.lookup.ServiceProvider;

import jmri.InstanceManager;
import jmri.swing.PreferencesPanel;
import jmri.configurexml.LoadAndStorePreferences;

/**
 * Preferences panel for Load and store tables and panel files.
 *
 * @author Dave Sand         Copyright 2022
 * @author Daniel Bergqvist  Copyright 2025
 */
@ServiceProvider(service = PreferencesPanel.class)
public class LoadAndStorePreferencesPanel extends JPanel implements PreferencesPanel {

    private final LoadAndStorePreferences _preferences;

    private JCheckBox _excludeFileHistoryCheckBox;
    private JCheckBox _excludeMemoryIMCURRENTTIME_CheckBox;
    private JCheckBox _excludeJmriVersionCheckBox;
    private JCheckBox _excludeTimebaseCheckBox;
    private JCheckBox _excludeFontExtensionsCheckBox;

    public LoadAndStorePreferencesPanel() {
        _preferences = InstanceManager.getDefault(LoadAndStorePreferences.class);
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
        _preferences.setExcludeFileHistory(_excludeFileHistoryCheckBox.isSelected());
        _preferences.setExcludeMemoryIMCURRENTTIME(_excludeMemoryIMCURRENTTIME_CheckBox.isSelected());
        _preferences.setExcludeJmriVersion(_excludeJmriVersionCheckBox.isSelected());
        _preferences.setExcludeTimebase(_excludeTimebaseCheckBox.isSelected());
        _preferences.setExcludeFontExtensions(_excludeFontExtensionsCheckBox.isSelected());
        return didSet;
    }

    private JPanel getPanel() {
        JLabel infoLabel = new JLabel(Bundle.getMessage("LabelExcludeInfo"));

        _excludeFileHistoryCheckBox = new JCheckBox(Bundle.getMessage("LabelExcludeFileHistory"));
        _excludeFileHistoryCheckBox.setSelected(_preferences.isExcludeFileHistory());

        _excludeMemoryIMCURRENTTIME_CheckBox = new JCheckBox(Bundle.getMessage("ExcludeMemoryIMCURRENTTIME"));
        _excludeMemoryIMCURRENTTIME_CheckBox.setSelected(_preferences.isExcludeMemoryIMCURRENTTIME());

        _excludeJmriVersionCheckBox = new JCheckBox(Bundle.getMessage("ExcludeJmriVersion"));
        _excludeJmriVersionCheckBox.setSelected(_preferences.isExcludeJmriVersion());

        _excludeTimebaseCheckBox = new JCheckBox(Bundle.getMessage("ExcludeTimebase"));
        _excludeTimebaseCheckBox.setSelected(_preferences.isExcludeTimebase());

        _excludeFontExtensionsCheckBox = new JCheckBox(Bundle.getMessage("ExcludeFontExtensions"));
        _excludeFontExtensionsCheckBox.setSelected(_preferences.isExcludeFontExtensions());

        JPanel gridPanel = new JPanel(new GridLayout(0, 1));
//        gridPanel.add(new JLabel());
        gridPanel.add(infoLabel);
        gridPanel.add(_excludeMemoryIMCURRENTTIME_CheckBox);
        gridPanel.add(_excludeJmriVersionCheckBox);
        gridPanel.add(_excludeFileHistoryCheckBox);
        gridPanel.add(_excludeTimebaseCheckBox);
        gridPanel.add(_excludeFontExtensionsCheckBox);
//        gridPanel.add(new jmri.swing.JTitledSeparator(Bundle.getMessage("IgnoreSeparator")));

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
        panel.add(gridPanel);

        return panel;
    }

    @Override
    public String getPreferencesItem() {
        return "LOAD_AND_STORE"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("LoadAndStoreMenu"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return getPreferencesItemText();
    }

    @Override
    public String getLabelKey() {
        return Bundle.getMessage("LoadAndStorePrefLabel");
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
