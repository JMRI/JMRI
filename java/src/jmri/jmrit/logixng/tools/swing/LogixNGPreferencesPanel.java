package jmri.jmrit.logixng.tools.swing;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import jmri.jmrit.logixng.implementation.LogixNGPreferences;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Insets;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import jmri.InstanceManager;
import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;

/**
 * Preferences panel for LogixNG
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
@ServiceProvider(service = PreferencesPanel.class)
public class LogixNGPreferencesPanel extends JPanel implements PreferencesPanel {
    
    private final LogixNGPreferences preferences;
//    private jmri.web.server.WebServerPreferences apreferences;
    
    JCheckBox _startLogixNGOnLoadCheckBox;
    JCheckBox _allowDebugModeCheckBox;
    
    public LogixNGPreferencesPanel() {
        preferences = InstanceManager.getDefault(LogixNGPreferences.class);
        initGUI();
        setGUI();
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

    private void setGUI() {
        _startLogixNGOnLoadCheckBox.setSelected(preferences.getStartLogixNGOnStartup());
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
        return didSet;
    }
    
    private JPanel getStartupPanel() {
        JPanel panel = new JPanel();

        _startLogixNGOnLoadCheckBox = new JCheckBox(Bundle.getMessage("LabelStartLogixNGOnLoad"));
        _startLogixNGOnLoadCheckBox.setToolTipText(Bundle.getMessage("ToolTipStartLogixNGOnLoad"));

        _allowDebugModeCheckBox = new JCheckBox(Bundle.getMessage("LabelAllowDebugMode"));
        _allowDebugModeCheckBox.setToolTipText(Bundle.getMessage("ToolTipLabelAllowDebugMode"));

        JPanel gridPanel = new JPanel(new GridLayout(0, 1));
        
        gridPanel.add(_startLogixNGOnLoadCheckBox);
        gridPanel.add(_allowDebugModeCheckBox);
        
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
