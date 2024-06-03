package jmri.jmrit.logixng.tools.swing;

import jmri.jmrit.logixng.LogixNGPreferences;
import jmri.jmrit.logixng.implementation.DefaultLogixNGPreferences;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.*;

import org.openide.util.lookup.ServiceProvider;

import jmri.InstanceManager;
import jmri.jmrit.logixng.MaleSocket.ErrorHandlingType;
import jmri.jmrit.logixng.actions.IfThenElse;
// import jmri.swing.JTitledSeparator;
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
    JCheckBox _logAllBeforeCheckBox;
    JCheckBox _logAllAfterCheckBox;
    JCheckBox _showSystemUserNamesCheckBox;
    JCheckBox _treeEditorHighlightRow;
    JCheckBox _showSystemNameInException;
    JCheckBox _strictTypingGlobalVariables;
    JCheckBox _strictTypingLocalVariables;
    private JComboBox<ErrorHandlingType> _errorHandlingComboBox;
    private JComboBox<IfThenElse.ExecuteType> _ifThenElseExecuteTypeDefault;


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
//        add(new JTitledSeparator(Bundle.getMessage("TitleStartupSettingsPanel")));
        add(getStartupPanel());
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
        preferences.setLogAllBefore(_logAllBeforeCheckBox.isSelected());
        preferences.setLogAllAfter(_logAllAfterCheckBox.isSelected());
        preferences.setShowSystemUserNames(_showSystemUserNamesCheckBox.isSelected());
        preferences.setTreeEditorHighlightRow(_treeEditorHighlightRow.isSelected());
        preferences.setErrorHandlingType(_errorHandlingComboBox.getItemAt(
                _errorHandlingComboBox.getSelectedIndex()));
        preferences.setShowSystemNameInException(_showSystemNameInException.isSelected());
        preferences.setStrictTypingGlobalVariables(_strictTypingGlobalVariables.isSelected());
        preferences.setStrictTypingLocalVariables(_strictTypingLocalVariables.isSelected());
        preferences.setIfThenElseExecuteTypeDefault(_ifThenElseExecuteTypeDefault.getItemAt(
                _ifThenElseExecuteTypeDefault.getSelectedIndex()));
        return didSet;
    }

    private JPanel getStartupPanel() {
        JPanel panel = new JPanel();

        _startLogixNGOnLoadCheckBox = new JCheckBox(Bundle.getMessage("LabelStartLogixNGOnLoad"));
        _startLogixNGOnLoadCheckBox.setToolTipText(Bundle.getMessage("ToolTipStartLogixNGOnLoad"));

        _installDebuggerCheckBox = new JCheckBox(Bundle.getMessage("LabelInstallDebugger"));
        _installDebuggerCheckBox.setToolTipText(Bundle.getMessage("ToolTipLabelInstallDebugger"));

        _logAllBeforeCheckBox = new JCheckBox(Bundle.getMessage("LabelLogAllBefore"));
        _logAllBeforeCheckBox.setToolTipText(Bundle.getMessage("ToolTipLabelLogAllBefore"));

        _logAllAfterCheckBox = new JCheckBox(Bundle.getMessage("LabelLogAllAfter"));
        _logAllAfterCheckBox.setToolTipText(Bundle.getMessage("ToolTipLabelLogAllAfter"));

        _showSystemUserNamesCheckBox = new JCheckBox(Bundle.getMessage("LabelShowSystemUserNames"));
        _showSystemUserNamesCheckBox.setToolTipText(Bundle.getMessage("ToolTipLabeShowSystemUserNames"));

        _treeEditorHighlightRow = new JCheckBox(Bundle.getMessage("LabelTreeEditorHighlightRow"));
        _treeEditorHighlightRow.setToolTipText(Bundle.getMessage("ToolTipTreeEditorHighlightRow"));

        _showSystemNameInException = new JCheckBox(Bundle.getMessage("LabelShowSystemNameInException"));
        _showSystemNameInException.setToolTipText(Bundle.getMessage("ToolTipShowSystemNameInException"));

        _strictTypingGlobalVariables = new JCheckBox(Bundle.getMessage("LabelStrictTypingGlobalVariables"));
        _strictTypingGlobalVariables.setToolTipText(Bundle.getMessage("ToolTipStrictTypingGlobalVariables"));

        _strictTypingLocalVariables = new JCheckBox(Bundle.getMessage("LabelStrictTypingLocalVariables"));
        _strictTypingLocalVariables.setToolTipText(Bundle.getMessage("ToolTipStrictTypingLocalVariables"));

        JPanel gridPanel = new JPanel(new GridLayout(0, 1));

        gridPanel.add(_startLogixNGOnLoadCheckBox);
        gridPanel.add(_installDebuggerCheckBox);
        gridPanel.add(_logAllBeforeCheckBox);
        gridPanel.add(_logAllAfterCheckBox);
        gridPanel.add(_showSystemUserNamesCheckBox);
        gridPanel.add(_treeEditorHighlightRow);
        gridPanel.add(Box.createVerticalStrut(2));
        gridPanel.add(_strictTypingGlobalVariables);
        gridPanel.add(_strictTypingLocalVariables);
        gridPanel.add(Box.createVerticalStrut(2));
        gridPanel.add(_showSystemNameInException);

        _startLogixNGOnLoadCheckBox.setSelected(preferences.getStartLogixNGOnStartup());
        _installDebuggerCheckBox.setSelected(preferences.getInstallDebugger());
        _logAllBeforeCheckBox.setSelected(preferences.getLogAllBefore());
        _logAllAfterCheckBox.setSelected(preferences.getLogAllAfter());
        _showSystemUserNamesCheckBox.setSelected(preferences.getShowSystemUserNames());
        _treeEditorHighlightRow.setSelected(preferences.getTreeEditorHighlightRow());
        _showSystemNameInException.setSelected(preferences.getShowSystemNameInException());
        _strictTypingGlobalVariables.setSelected(preferences.getStrictTypingGlobalVariables());
        _strictTypingLocalVariables.setSelected(preferences.getStrictTypingLocalVariables());

        gridPanel.add(Box.createVerticalStrut(2));
        gridPanel.add(new JLabel(Bundle.getMessage("LabelDefaultErrorHandling")));
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


        gridPanel.add(Box.createVerticalStrut(2));
        gridPanel.add(new JLabel(Bundle.getMessage("LabelDefaultIfThenElseExecution")));
        _ifThenElseExecuteTypeDefault = new JComboBox<>();
        for (IfThenElse.ExecuteType type : IfThenElse.ExecuteType.values()) {
            _ifThenElseExecuteTypeDefault.addItem(type);
            if (preferences.getIfThenElseExecuteTypeDefault() == type) {
                _ifThenElseExecuteTypeDefault.setSelectedItem(type);
            }
        }
        JComboBoxUtil.setupComboBoxMaxRows(_ifThenElseExecuteTypeDefault);
        gridPanel.add(_ifThenElseExecuteTypeDefault);

        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
        panel.add(gridPanel);

        return panel;
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
