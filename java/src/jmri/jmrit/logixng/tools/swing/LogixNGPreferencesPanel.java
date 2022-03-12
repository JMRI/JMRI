package jmri.jmrit.logixng.tools.swing;

import jmri.jmrit.logixng.LogixNGPreferences;
import jmri.jmrit.logixng.implementation.DefaultLogixNGPreferences;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.*;

import org.openide.util.lookup.ServiceProvider;

import jmri.InstanceManager;
import jmri.jmrit.logixng.MaleSocket.ErrorHandlingType;
import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Preferences panel for LogixNG
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
@ServiceProvider(service = PreferencesPanel.class)
public class LogixNGPreferencesPanel extends JPanel implements PreferencesPanel {
    
    private final DefaultLogixNGPreferences preferences;
    
    JCheckBox _startLogixNGOnLoadCheckBox;
    JCheckBox _installDebuggerCheckBox;
    JCheckBox _showSystemUserNamesCheckBox;
    JCheckBox _treeEditorHighlightRow;
    private JComboBox<ErrorHandlingType> _errorHandlingComboBox;
    
    
    public LogixNGPreferencesPanel() {
        LogixNGPreferences prefs = InstanceManager.getDefault(LogixNGPreferences.class);
        if (!(prefs instanceof DefaultLogixNGPreferences)) {
            throw new RuntimeException("LogixNGPreferences is not of type DefaultLogixNGPreferences");
        }
        preferences = (DefaultLogixNGPreferences)prefs;
        initGUI();
    }

    private void initGUI() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(new JTitledSeparator(Bundle.getMessage("TitleStartupSettingsPanel")));
        add(getStartupPanel());
        add(new JTitledSeparator(Bundle.getMessage("TitleTimeDiagramColorsPanel")));
        add(getTimeDiagramColorsPanel());
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
        preferences.setStartLogixNGOnStartup(_startLogixNGOnLoadCheckBox.isSelected());
        preferences.setInstallDebugger(_installDebuggerCheckBox.isSelected());
        preferences.setShowSystemUserNames(_showSystemUserNamesCheckBox.isSelected());
        preferences.setTreeEditorHighlightRow(_treeEditorHighlightRow.isSelected());
        preferences.setErrorHandlingType(_errorHandlingComboBox.getItemAt(_errorHandlingComboBox.getSelectedIndex()));
        return didSet;
    }
    
    private JPanel getStartupPanel() {
        JPanel panel = new JPanel();

        _startLogixNGOnLoadCheckBox = new JCheckBox(Bundle.getMessage("LabelStartLogixNGOnLoad"));
        _startLogixNGOnLoadCheckBox.setToolTipText(Bundle.getMessage("ToolTipStartLogixNGOnLoad"));

        _installDebuggerCheckBox = new JCheckBox(Bundle.getMessage("LabelInstallDebugger"));
        _installDebuggerCheckBox.setToolTipText(Bundle.getMessage("ToolTipLabelInstallDebugger"));

        _showSystemUserNamesCheckBox = new JCheckBox(Bundle.getMessage("LabelShowSystemUserNames"));
        _showSystemUserNamesCheckBox.setToolTipText(Bundle.getMessage("ToolTipLabeShowSystemUserNames"));

        _treeEditorHighlightRow = new JCheckBox(Bundle.getMessage("LabelTreeEditorHighlightRow"));
        _treeEditorHighlightRow.setToolTipText(Bundle.getMessage("ToolTipTreeEditorHighlightRow"));

        JPanel gridPanel = new JPanel(new GridLayout(0, 1));
        
        gridPanel.add(_startLogixNGOnLoadCheckBox);
        gridPanel.add(_installDebuggerCheckBox);
        gridPanel.add(_showSystemUserNamesCheckBox);
        gridPanel.add(_treeEditorHighlightRow);
        
        _startLogixNGOnLoadCheckBox.setSelected(preferences.getStartLogixNGOnStartup());
        _installDebuggerCheckBox.setSelected(preferences.getInstallDebugger());
        _showSystemUserNamesCheckBox.setSelected(preferences.getShowSystemUserNames());
        _treeEditorHighlightRow.setSelected(preferences.getTreeEditorHighlightRow());
        
        _errorHandlingComboBox = new JComboBox<>();
        for (ErrorHandlingType type : ErrorHandlingType.values()) {
            // ErrorHandlingType.Default cannot be used as default
            if (type != ErrorHandlingType.Default) {
                _errorHandlingComboBox.addItem(type);
                if (preferences.getErrorHandlingType() == type) {
                    _errorHandlingComboBox.setSelectedItem(type);
                }
            }
        }
        JComboBoxUtil.setupComboBoxMaxRows(_errorHandlingComboBox);
        gridPanel.add(_errorHandlingComboBox);
        
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
        panel.add(gridPanel);

        return panel;
    }

    private JPanel getTimeDiagramColorsPanel() {
        return new JPanel();
    }
    
    @Override
    public String getPreferencesItem() {
        return "LOGIXNG"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("MenuLogixNG"); // NOI18N
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
            preferences.save();
        }
    }

    @Override
    public boolean isDirty() {
        return preferences.isDirty();
    }

    @Override
    public boolean isRestartRequired() {
        return preferences.isRestartRequired();
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }

}
