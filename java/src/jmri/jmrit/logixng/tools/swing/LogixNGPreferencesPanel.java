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
import jmri.jmrit.logixng.PluginManager;
import jmri.jmrit.logixng.PluginManager.ClassDefinition;
import jmri.jmrit.logixng.PluginManager.ClassType;
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
        add(new JTitledSeparator(Bundle.getMessage("TitlePluginClassesPanel")));
        add(getPluginClassesPanel());
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

    private static class ClassTableModel extends AbstractTableModel {
        
        List<ClassDefinition> classList = new ArrayList<>();

        @Override
        public int getRowCount() {
            return classList.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0: return classList.get(rowIndex).getEnabled();
                case 1: return classList.get(rowIndex).getType().name();
                case 2: return classList.get(rowIndex).getName();
                default: return null;
            }
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0: return Boolean.class;
                case 1: return String.class;
                case 2: return String.class;
                default: return null;
            }
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            ClassType type = classList.get(rowIndex).getType();
            return (columnIndex == 0)
                    && ((type == ClassType.EXPRESSION) || (type == ClassType.ACTION));
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            classList.get(rowIndex).setEnabled((Boolean) aValue);
        }
    }
    
//    private void printClassPath() {
//        ClassLoader cl = ClassLoader.getSystemClassLoader();
//        URL[] urls = ((URLClassLoader)cl).getURLs();
//        for(URL url: urls){
//            System.out.println(url.getFile());
//        }
//    }
    
    private JPanel getPluginClassesPanel() {
        // For testing only
//        printClassPath();
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        ClassTableModel jarTableModel1 = new ClassTableModel();
        jarTableModel1.classList.add(new ClassDefinition(false, ClassType.EXPRESSION, "Test"));
        jarTableModel1.classList.add(new ClassDefinition(false, ClassType.ACTION_NOT_PLUGIN, "TestAA"));
        jarTableModel1.classList.add(new ClassDefinition(true, ClassType.ACTION, "Test bla vla"));
        jarTableModel1.classList.add(new ClassDefinition(false, ClassType.EXPRESSION, "Test"));
        jarTableModel1.classList.add(new ClassDefinition(true, ClassType.OTHER, "Test test test"));
        jarTableModel1.classList.add(new ClassDefinition(false, ClassType.EXPRESSION_NOT_PLUGIN, "Test"));
        jarTableModel1.classList.add(new ClassDefinition(false, ClassType.EXPRESSION, "Test"));
        JTable jarTable1 = new JTable(jarTableModel1);
        tabbedPane.addTab("JAR file 1", jarTable1);
        
        ClassTableModel jarTableModel2 = new ClassTableModel();
        JTable jarTable2 = new JTable(jarTableModel2);
        tabbedPane.addTab("JAR file 2", jarTable2);
        
        panel.add(tabbedPane);
        
        JPanel addRemoveButtonsPanel = new JPanel();
        addRemoveButtonsPanel.setLayout(
                new BoxLayout(addRemoveButtonsPanel, BoxLayout.X_AXIS));
        JButton addJarFile = new JButton(Bundle.getMessage("LabelButtonAddJarFile"));
        JButton removeJarFile = new JButton(Bundle.getMessage("LabelButtonRemoveJarFile"));
        addRemoveButtonsPanel.add(addJarFile);
        addRemoveButtonsPanel.add(removeJarFile);
        panel.add(addRemoveButtonsPanel);
        
        return panel;
/*        
        //Lay out the label and scroll pane from top to bottom.
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel(labelText);
        ...
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0, 5)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(setButton);
        
        //Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(listPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);
*/
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
