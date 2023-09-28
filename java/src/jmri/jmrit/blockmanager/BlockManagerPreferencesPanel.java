package jmri.jmrit.blockmanager;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.*;

import org.openide.util.lookup.ServiceProvider;

import jmri.InstanceManager;
import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;

/**
 * Preferences panel for BlockManager
 *
 * @author Daniel Bergqvist Copyright 2023
 */
@ServiceProvider(service = PreferencesPanel.class)
public class BlockManagerPreferencesPanel extends JPanel implements PreferencesPanel {

    private final DefaultBlockManagerPreferences preferences;

    JCheckBox _startLogixNGOnLoadCheckBox;
    JCheckBox _installDebuggerCheckBox;
    JCheckBox _showSystemUserNamesCheckBox;
    JCheckBox _treeEditorHighlightRow;
    JCheckBox _showSystemNameInException;


    public BlockManagerPreferencesPanel() {
        BlockManagerPreferences prefs = InstanceManager.getDefault(BlockManagerPreferences.class);
        if (!(prefs instanceof DefaultBlockManagerPreferences)) {
            throw new RuntimeException("BlockManagerPreferences is not of type DefaultBlockManagerPreferences");
        }
        preferences = (DefaultBlockManagerPreferences)prefs;
        initGUI();
    }

    private void initGUI() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(new JTitledSeparator(Bundle.getMessage("TitleStartupSettingsPanel")));
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
        preferences.setShowSystemUserNames(_showSystemUserNamesCheckBox.isSelected());
        preferences.setTreeEditorHighlightRow(_treeEditorHighlightRow.isSelected());
        preferences.setShowSystemNameInException(_showSystemNameInException.isSelected());
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

        _showSystemNameInException = new JCheckBox(Bundle.getMessage("LabelShowSystemNameInException"));
        _showSystemNameInException.setToolTipText(Bundle.getMessage("ToolTipShowSystemNameInException"));

        JPanel gridPanel = new JPanel(new GridLayout(0, 1));

        gridPanel.add(_startLogixNGOnLoadCheckBox);
        gridPanel.add(_installDebuggerCheckBox);
        gridPanel.add(_showSystemUserNamesCheckBox);
        gridPanel.add(_treeEditorHighlightRow);
        gridPanel.add(_showSystemNameInException);

        _startLogixNGOnLoadCheckBox.setSelected(preferences.getStartLogixNGOnStartup());
        _installDebuggerCheckBox.setSelected(preferences.getInstallDebugger());
        _showSystemUserNamesCheckBox.setSelected(preferences.getShowSystemUserNames());
        _treeEditorHighlightRow.setSelected(preferences.getTreeEditorHighlightRow());
        _showSystemNameInException.setSelected(preferences.getShowSystemNameInException());

        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
        panel.add(gridPanel);

        return panel;
    }

    @Override
    public String getPreferencesItem() {
        return "BLOCKMANAGER"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("MenuBlockManager"); // NOI18N
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
