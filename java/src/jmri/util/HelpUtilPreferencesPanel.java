package jmri.util;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.*;

import org.openide.util.lookup.ServiceProvider;

import jmri.InstanceManager;
// import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;

/**
 * Preferences panel for HelpUtil
 *
 * @author Daniel Bergqvist Copyright 2021
 */
@ServiceProvider(service = PreferencesPanel.class)
public class HelpUtilPreferencesPanel extends JPanel implements PreferencesPanel {

    private final HelpUtilPreferences _preferences;

    JRadioButton _openHelpOnFileRadioButton;
    JRadioButton _openHelpOnlineRadioButton;
    JRadioButton _openHelpOnJMRIWebServerRadioButton;
    ButtonGroup _openHelpButtonGroup;

    public HelpUtilPreferencesPanel() {
        _preferences = InstanceManager.getDefault(HelpUtilPreferences.class);
        initGUI();
    }

    private void initGUI() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(getHelpPanel());
    }

    /**
     * set the local prefs to match the GUI Local prefs are independent from the
     * singleton instance prefs.
     *
     * @return true if set, false if values are unacceptable.
     */
    private boolean setValues() {
        boolean didSet = true;
        _preferences.setOpenHelpOnFile(_openHelpOnFileRadioButton.isSelected());
        _preferences.setOpenHelpOnline(_openHelpOnlineRadioButton.isSelected());
        _preferences.setOpenHelpOnJMRIWebServer(_openHelpOnJMRIWebServerRadioButton.isSelected());
        return didSet;
    }

    private JPanel getHelpPanel() {
        JPanel panel = new JPanel();

        _openHelpButtonGroup = new ButtonGroup();

        _openHelpOnFileRadioButton = new JRadioButton(Bundle.getMessage("Help_LabelOpenHelpOnFile"));
        _openHelpOnFileRadioButton.setToolTipText(Bundle.getMessage("Help_ToolTipLabelOpenHelpOnFile"));
        _openHelpButtonGroup.add(_openHelpOnFileRadioButton);

        _openHelpOnlineRadioButton = new JRadioButton(Bundle.getMessage("Help_LabelOpenHelpOnline"));
        _openHelpOnlineRadioButton.setToolTipText(Bundle.getMessage("Help_ToolTipLabelOpenHelpOnline"));
        _openHelpButtonGroup.add(_openHelpOnlineRadioButton);

        _openHelpOnJMRIWebServerRadioButton = new JRadioButton(Bundle.getMessage("Help_LabelOpenHelpOnJMRIWebServer"));
        _openHelpOnJMRIWebServerRadioButton.setToolTipText(Bundle.getMessage("Help_ToolTipLabelOpenHelpOnJMRIWebServer"));
        _openHelpButtonGroup.add(_openHelpOnJMRIWebServerRadioButton);

        JPanel gridPanel = new JPanel(new GridLayout(0, 1));

        gridPanel.add(_openHelpOnFileRadioButton);
        gridPanel.add(_openHelpOnlineRadioButton);
        gridPanel.add(_openHelpOnJMRIWebServerRadioButton);

        _openHelpOnFileRadioButton.setSelected(_preferences.getOpenHelpOnFile());
        _openHelpOnlineRadioButton.setSelected(_preferences.getOpenHelpOnline());
        _openHelpOnJMRIWebServerRadioButton.setSelected(_preferences.getOpenHelpOnJMRIWebServer());

        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
        panel.add(gridPanel);

        return panel;
    }

    @Override
    public String getPreferencesItem() {
        return "HELP"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("MenuHelp"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return getPreferencesItemText();
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

}
