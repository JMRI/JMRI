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
    
    JRadioButton _openHelpOnlineRadioButton;
    JRadioButton _openHelpOnFileRadioButton;
    JRadioButton _openHelpOnJMRIWebServerRadioButton;
    ButtonGroup _openHelpBbuttonGroup;
    
    public HelpUtilPreferencesPanel() {
        _preferences = InstanceManager.getDefault(HelpUtilPreferences.class);
        initGUI();
    }

    private void initGUI() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
//        add(new JTitledSeparator(Bundle.getMessage("TitleStartupSettingsPanel")));
        add(getHelpPanel());
//        add(new JTitledSeparator(Bundle.getMessage("TitleTimeDiagramColorsPanel")));
//        add(getTimeDiagramColorsPanel());
//        add(new JTitledSeparator(Bundle.getMessage("TitleNetworkPanel")));
//        add(networkPanel());
//        add(new JTitledSeparator(Bundle.getMessage("TitleControllersPanel")));
//        add(allowedControllers());
        
//        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
//        add(new JTitledSeparator(Bundle.getMessage("TitleRailroadNamePreferences")));
//        add(getLogixNGPanel());
    }

    /**
     * set the local prefs to match the GUI Local prefs are independent from the
     * singleton instance prefs.
     *
     * @return true if set, false if values are unacceptable.
     */
    private boolean setValues() {
        boolean didSet = true;
        _preferences.setOpenHelpOnline(_openHelpOnlineRadioButton.isSelected());
        _preferences.setOpenHelpOnFile(_openHelpOnFileRadioButton.isSelected());
        _preferences.setOpenHelpOnJMRIWebServer(_openHelpOnJMRIWebServerRadioButton.isSelected());
        return didSet;
    }
    
    private JPanel getHelpPanel() {
        JPanel panel = new JPanel();

        _openHelpBbuttonGroup = new ButtonGroup();

        _openHelpOnlineRadioButton = new JRadioButton(Bundle.getMessage("Help_LabelOpenHelpOnline"));
        _openHelpOnlineRadioButton.setToolTipText(Bundle.getMessage("Help_ToolTipLabelOpenHelpOnline"));
        _openHelpBbuttonGroup.add(_openHelpOnlineRadioButton);

        _openHelpOnFileRadioButton = new JRadioButton(Bundle.getMessage("Help_LabelOpenHelpOnFile"));
        _openHelpOnFileRadioButton.setToolTipText(Bundle.getMessage("Help_ToolTipLabelOpenHelpOnFile"));
        _openHelpBbuttonGroup.add(_openHelpOnFileRadioButton);

        _openHelpOnJMRIWebServerRadioButton = new JRadioButton(Bundle.getMessage("Help_LabelOpenHelpOnJMRIWebServer"));
        _openHelpOnJMRIWebServerRadioButton.setToolTipText(Bundle.getMessage("Help_ToolTipLabelOpenHelpOnJMRIWebServer"));
        _openHelpBbuttonGroup.add(_openHelpOnJMRIWebServerRadioButton);

        JPanel gridPanel = new JPanel(new GridLayout(0, 1));
        
        gridPanel.add(_openHelpOnlineRadioButton);
        gridPanel.add(_openHelpOnFileRadioButton);
        gridPanel.add(_openHelpOnJMRIWebServerRadioButton);
        
        _openHelpOnlineRadioButton.setSelected(_preferences.getOpenHelpOnline());
        _openHelpOnFileRadioButton.setSelected(_preferences.getOpenHelpOnFile());
        _openHelpOnJMRIWebServerRadioButton.setSelected(_preferences.getOpenHelpOnJMRIWebServer());
        
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
        panel.add(gridPanel);

        return panel;
    }

    private JPanel getTimeDiagramColorsPanel() {
        return new JPanel();
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
