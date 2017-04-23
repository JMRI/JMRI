package jmri.jmrit.beantable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jmri.Audio;
import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.ConditionalManager;
import jmri.ConditionalVariable;
import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.Logix;
import jmri.LogixManager;
import jmri.Manager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.Route;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.UserPreferencesManager;
import jmri.implementation.DefaultConditional;
import jmri.implementation.DefaultConditionalAction;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.picker.PickSinglePanel;
import jmri.jmrit.sensorgroup.SensorGroupFrame;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriBeanComboBox;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a Logix Table.
 * <P>
 * Also contains the panes to create, edit, and delete a Logix. Also contains
 * the pane to define and edit a Conditional.
 * <P>
 * Most of the text used in this GUI is in LogixTableBundle.properties, accessed
 * via rbx, and the remainder of the text is in BeanTableBundle.properties,
 * accessed via the Bundle.getMessage() method.
 * <p>
 * Methods and Members for 'state variables' and 'actions' removed to become
 * their own objects - 'ConditionalVariable' and 'ConditionalAction' in jmri
 * package. Two more types of logic for a Conditional to use in its antecedent
 * have been added to the original 'AND'ing all statevariables - 'OR' (i.e. all
 * OR's) and 'MIXED' (i.e. general boolean statement with any mixture of boolean
 * operations). The 'OR's an 'AND's types are unambiguous and do not require
 * parentheses. The 'Mixed' type uses a TextField for the user to insert
 * parenthees. Jan 22, 2009 - Pete Cressman
 * <p>
 * Conditionals now have two policies to trigger execution of their action
 * lists:<br>
 * 1. the previous policy - Trigger on change of state only <br>
 * 2. the new default - Trigger on any enabled state calculation
 * Jan 15, 2011 - Pete Cressman
 * <p>
 * Two additional action and variable name selection methods have been added:
 * 1) Single Pick List
 * 2) Combo Box Selection
 * The traditional tabbed Pick List with text entry is the default method.
 * The Options menu has been expanded to list the 3 methods.
 * Mar 27, 2017 - Dave Sand
 * <p>
 * Add a Browse Option to the Logix Select Menu
 * This will display a window that creates a formatted list of the contents of the
 * seletcted Logix with each Conditional, Variable and Action.
 * The code is courtesy of Chuck Catania and is used with his permission.
 * Apr 2, 2017 - Dave Sand
 * <p>
 * @author Dave Duchamp Copyright (C) 2007
 * @author Pete Cressman Copyright (C) 2009, 2010, 2011
 * @author Matthew Harris copyright (c) 2009
 * @author Dave Sand copyright (c) 2017
 */
public class LogixTableAction extends AbstractTableAction {

    /**
     * Constructor to create a LogixManager instance.
     *
     * @param s the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     */
    public LogixTableAction(String s) {
        super(s);
        // set up managers - no need to use InstanceManager since both managers are
        // Default only (internal). We use InstanceManager to get managers for
        // compatibility with other facilities.
        _logixManager = InstanceManager.getNullableDefault(jmri.LogixManager.class);
        _conditionalManager = InstanceManager.getNullableDefault(jmri.ConditionalManager.class);
        // disable ourself if there is no Logix manager or no Conditional manager available
        if ((_logixManager == null) || (_conditionalManager == null)) {
            setEnabled(false);
        }
    }

    /**
     * Constructor to create a LogixManager instance with default title.
     */
    public LogixTableAction() {
        this(Bundle.getMessage("TitleLogixTable"));
    }

    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.beantable.LogixTableBundle");

    // *********** Methods for Logix Table Window ********************

    /**
     * Create the JTable DataModel, along with the changes (overrides of
     * BeanTableDataModel) for the specific case of a Logix table.
     * <p>
     * Note: Table Models for the Conditional table in the Edit Logix window,
     * and the State Variable table in the Edit Conditional window are at
     * the end of this module.
     */
    @Override
    protected void createModel() {
        m = new BeanTableDataModel() {
            // overlay the state column with the edit column
            static public final int ENABLECOL = VALUECOL;
            static public final int EDITCOL = DELETECOL;
            protected String enabledString = Bundle.getMessage("ColumnHeadEnabled");

            @Override
            public String getColumnName(int col) {
                if (col == EDITCOL) {
                    return ""; // no heading on "Edit"
                }
                if (col == ENABLECOL) {
                    return enabledString;
                }
                return super.getColumnName(col);
            }

            @Override
            public Class<?> getColumnClass(int col) {
                if (col == EDITCOL) {
                    return String.class;
                }
                if (col == ENABLECOL) {
                    return Boolean.class;
                }
                return super.getColumnClass(col);
            }

            @Override
            public int getPreferredWidth(int col) {
                // override default value for SystemName and UserName columns
                if (col == SYSNAMECOL) {
                    return new JTextField(12).getPreferredSize().width;
                }
                if (col == USERNAMECOL) {
                    return new JTextField(17).getPreferredSize().width;
                }
                if (col == EDITCOL) {
                    return new JTextField(12).getPreferredSize().width;
                }
                if (col == ENABLECOL) {
                    return new JTextField(5).getPreferredSize().width;
                }
                return super.getPreferredWidth(col);
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                if (col == EDITCOL) {
                    return true;
                }
                if (col == ENABLECOL) {
                    return true;
                }
                return super.isCellEditable(row, col);
            }

            @Override
            public Object getValueAt(int row, int col) {
                if (col == EDITCOL) {
                    return Bundle.getMessage("ButtonSelect");
                } else if (col == ENABLECOL) {
                    Logix logix = (Logix) getBySystemName((String) getValueAt(row, SYSNAMECOL));
                    if (logix == null) {
                        return null;
                    }
                    return Boolean.valueOf(logix.getEnabled());
                } else {
                    return super.getValueAt(row, col);
                }
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col == EDITCOL) {
                    // set up to edit
                    String sName = (String) getValueAt(row, SYSNAMECOL);
                    if (Bundle.getMessage("ButtonEdit").equals(value)) {
                        editPressed(sName);
                        
                    } else if (rbx.getString("BrowserButton").equals(value)) {
                      conditionalRowNumber = row;
                      browserPressed(sName);
                      
                    } else if (Bundle.getMessage("ButtonCopy").equals(value)) {
                        copyPressed(sName);
                        
                    } else if (Bundle.getMessage("ButtonDelete").equals(value)) {
                        deletePressed(sName);
                    }
                } else if (col == ENABLECOL) {
                    // alternate
                    Logix x = (Logix) getBySystemName((String) getValueAt(row,
                            SYSNAMECOL));
                    boolean v = x.getEnabled();
                    x.setEnabled(!v);
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            /**
             * Delete the bean after all the checking has been done.
             * <p>
             * Deactivates the Logix and remove it's conditionals.
             *
             * @param bean of the Logix to delete
             */
            @Override
            void doDelete(NamedBean bean) {
                Logix l = (Logix) bean;
                l.deActivateLogix();
                // delete the Logix and all its Conditionals
                _logixManager.deleteLogix(l);
            }

            @Override
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals(enabledString)) {
                    return true;
                }
                return super.matchPropertyName(e);
            }

            @Override
            public Manager getManager() {
                return InstanceManager.getDefault(jmri.LogixManager.class);
            }

            @Override
            public NamedBean getBySystemName(String name) {
                return InstanceManager.getDefault(jmri.LogixManager.class).getBySystemName(
                        name);
            }

            @Override
            public NamedBean getByUserName(String name) {
                return InstanceManager.getDefault(jmri.LogixManager.class).getByUserName(
                        name);
            }

            @Override
            protected String getMasterClassName() {
                return getClassName();
            }

            @Override
            public void configureTable(JTable table) {
                table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());
                table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
                table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
                super.configureTable(table);
            }

            /**
             * Replace delete button with comboBox to edit/delete/copy/select Logix.
             *
             * @param table name of the Logix JTable holding the column
             */
            @Override
            protected void configDeleteColumn(JTable table) {
                JComboBox<String> editCombo = new JComboBox<String>();
                editCombo.addItem(Bundle.getMessage("ButtonSelect"));
                editCombo.addItem(Bundle.getMessage("ButtonEdit"));
				editCombo.addItem(rbx.getString("BrowserButton"));
                editCombo.addItem(Bundle.getMessage("ButtonCopy"));
                editCombo.addItem(Bundle.getMessage("ButtonDelete"));
                TableColumn col = table.getColumnModel().getColumn(BeanTableDataModel.DELETECOL);
                col.setCellEditor(new DefaultCellEditor(editCombo));
            }

            // Not needed - here for interface compatibility
            @Override
            public void clickOn(NamedBean t) {
            }

            @Override
            public String getValue(String s) {
                return "";
            }

            @Override
            protected String getBeanType() {
                return Bundle.getMessage("BeanNameLogix");
            }
        };
    }

    /**
     * Set title of Logix table.
     */
    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleLogixTable"));
    }

    /**
     * Insert 2 table specific menus.
     * <p>
     * Accounts for the Window and Help menus, which are already added to the menu bar
     * as part of the creation of the JFrame, by adding the new menus 2 places earlier
     * unless the table is part of the ListedTableFrame, which adds the Help menu later on.
     * @param f the JFrame of this table
     */
    @Override
    public void setMenuBar(BeanTableFrame f) {
        loadSelectionMode();

        JMenu menu = new JMenu(Bundle.getMessage("MenuOptions"));
        menu.setMnemonic(KeyEvent.VK_O);
        javax.swing.JMenuBar menuBar = f.getJMenuBar();
        int pos = menuBar.getMenuCount() - 1; // count the number of menus to insert the TableMenus before 'Window' and 'Help'
        int offset = 1;
        log.debug("setMenuBar number of menu items = " + pos);
        for (int i = 0; i <= pos; i++) {
            if (menuBar.getComponent(i) instanceof JMenu) {
                if (((JMenu) menuBar.getComponent(i)).getText().equals(Bundle.getMessage("MenuHelp"))) {
                    offset = -1; // correct for use as part of ListedTableAction where the Help Menu is not yet present
                }
            }
        }

        ButtonGroup enableButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(rbx.getString("EnableAll"));
        r.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableAll(true);
            }
        });
        enableButtonGroup.add(r);
        r.setSelected(true);
        menu.add(r);
        
        r = new JRadioButtonMenuItem(rbx.getString("DisableAll"));
        r.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableAll(false);
            }
        });
        enableButtonGroup.add(r);
        menu.add(r);

        menu.addSeparator();

        ButtonGroup modeButtonGroup = new ButtonGroup();
        r = new JRadioButtonMenuItem(rbx.getString("UseMultiPick"));
        r.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setSelectionMode(SelectionMode.USEMULTI);
            }
        });
        modeButtonGroup.add(r);
        menu.add(r);
        r.setSelected(_selectionMode == SelectionMode.USEMULTI);

        r = new JRadioButtonMenuItem(rbx.getString("UseSinglePick"));
        r.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setSelectionMode(SelectionMode.USESINGLE);
            }
        });
        modeButtonGroup.add(r);
        menu.add(r);
        r.setSelected(_selectionMode == SelectionMode.USESINGLE);

        r = new JRadioButtonMenuItem(rbx.getString("UseComboNameBoxes"));
        r.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setSelectionMode(SelectionMode.USECOMBO);
            }
        });
        modeButtonGroup.add(r);
        menu.add(r);
        r.setSelected(_selectionMode == SelectionMode.USECOMBO);

        menuBar.add(menu, pos + offset);

        menu = new JMenu(Bundle.getMessage("MenuTools"));
        menu.setMnemonic(KeyEvent.VK_T);

        JMenuItem item = new JMenuItem(rbx.getString("OpenPickListTables"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OpenPickListTable();
            }
        });
        menu.add(item);

        item = new JMenuItem(rbx.getString("FindOrphans"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findOrphansPressed(e);
            }
        });
        menu.add(item);

        item = new JMenuItem(rbx.getString("EmptyConditionals"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findEmptyPressed(e);
            }
        });
        menu.add(item);

        item = new JMenuItem(rbx.getString("CrossReference"));
        item.addActionListener(new ActionListener() {
            BeanTableFrame parent;

            @Override
            public void actionPerformed(ActionEvent e) {
                new RefDialog(parent);
            }

            ActionListener init(BeanTableFrame f) {
                parent = f;
                return this;
            }
        }.init(f));
        menu.add(item);
        menuBar.add(menu, pos + offset + 1); // add this menu to the right of the previous
    }

    /**
     * Get the saved mode selection, default to the tranditional tabbed pick list.
     * <p>
     * During the menu build process, the corresponding menu item is set to selected.
     * @since 4.7.3
     */
    void loadSelectionMode() {
        Object modeName = InstanceManager.getDefault(jmri.UserPreferencesManager.class).getProperty(getClassName(), "Selection Mode");
        if (modeName == null) {
            _selectionMode = SelectionMode.USEMULTI;
        } else {
            String currentMode = (String) modeName;
            switch (currentMode) {
                case "USEMULTI":
                    _selectionMode = SelectionMode.USEMULTI;
                    break;
                case "USESINGLE":
                    _selectionMode = SelectionMode.USESINGLE;
                    break;
                case "USECOMBO":
                    _selectionMode = SelectionMode.USECOMBO;
                    break;
                default:
                    log.warn("Invalid Logix conditional selection mode value, '{}', returned", currentMode);
                    _selectionMode = SelectionMode.USEMULTI;
            }
        }        
    }

    /**
     * Save the mode selection.  Called by menu item change events.
     * @since 4.7.3
     * @param newMode The SelectionMode enum constant 
     */
    void setSelectionMode(SelectionMode newMode) {
        _selectionMode = newMode;
        InstanceManager.getDefault(jmri.UserPreferencesManager.class).setProperty(getClassName(), "Selection Mode", newMode.toString());
    }

    /**
     * Open a new Pick List to drag Actions from to form Logix Conditionals.
     */
    void OpenPickListTable() {
        if (_pickTables == null) {
            _pickTables = new jmri.jmrit.picker.PickFrame(rbx.getString("TitlePickList"));
        } else {
            _pickTables.setVisible(true);
        }
        _pickTables.toFront();
    }

    /**
     * Find empty Conditional entries, called from menu.
     *
     * @see Maintenance#findEmptyPressed(Frame)
     * @param e the event heard
     */
    void findEmptyPressed(ActionEvent e) {
        Maintenance.findEmptyPressed(f);
    }

    /**
     * Find orphaned entries, called from menu.
     *
     * @see Maintenance#findOrphansPressed(Frame)
     * @param e the event heard
     */
    void findOrphansPressed(ActionEvent e) {
        Maintenance.findOrphansPressed(f);
    }

    class RefDialog extends JDialog {

        JTextField _devNameField;
        java.awt.Frame _parent;

        RefDialog(java.awt.Frame frame) {
            super(frame, rbx.getString("CrossReference"), true);
            _parent = frame;
            JPanel extraPanel = new JPanel();
            extraPanel.setLayout(new BoxLayout(extraPanel, BoxLayout.Y_AXIS));
            _devNameField = new JTextField(30);
            JPanel panel = makeEditPanel(_devNameField, "ElementName", "ElementNameHint"); //NOI18N
            JButton referenceButton = new JButton(rbx.getString("ReferenceButton"));
            panel.add(referenceButton);
            referenceButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deviceReportPressed(e);
                }
            });
            panel.add(referenceButton);
            extraPanel.add(panel);
            setContentPane(extraPanel);
            pack();
            // setLocationRelativeTo((java.awt.Component)_pos);
            setVisible(true);
        }

        void deviceReportPressed(ActionEvent e) {
            Maintenance.deviceReportPressed(_devNameField.getText(), _parent);
            dispose();
        }
    }

    void enableAll(boolean enable) {
        List<String> sysNameList = _logixManager.getSystemNameList();
        for (int i = 0; i < sysNameList.size(); i++) {
            Logix x = _logixManager.getBySystemName(sysNameList.get(i));
            x.setEnabled(enable);
        }
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.LogixTable";
    }

    // *********** variable definitions ********************

    // Multi use variables
    ConditionalManager _conditionalManager = null; // set when LogixAction is created

    LogixManager _logixManager = null; // set when LogixAction is created
    boolean _showReminder = false;
    boolean _suppressReminder = false;
    boolean _suppressIndirectRef = false;
    jmri.jmrit.picker.PickFrame _pickTables;

    // Current focus variables
    Logix _curLogix = null;
    int numConditionals = 0;
    int conditionalRowNumber = 0;
    Conditional _curConditional = null;

    // Add Logix Variables
    JmriJFrame addLogixFrame = null;
    JTextField _systemName = new JTextField(20); // N11N
    JTextField _addUserName = new JTextField(20); // N11N
    JCheckBox _autoSystemName = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));
    JLabel _sysNameLabel = new JLabel(Bundle.getMessage("BeanNameLogix") + " " + Bundle.getMessage("ColumnSystemName") + ":");
    JLabel _userNameLabel = new JLabel(Bundle.getMessage("BeanNameLogix") + " " + Bundle.getMessage("ColumnUserName") + ":");
    String systemNameAuto = this.getClass().getName() + ".AutoSystemName";
    JButton create;

    // Edit Logix Variables
    JmriJFrame editLogixFrame = null;
    boolean inEditMode = false;
    boolean inCopyMode = false;
    boolean _inReorderMode = false;
    int _nextInOrder = 0;
    JTextField editUserName = new JTextField(20);
    ConditionalTableModel conditionalTableModel = null;
    JLabel status = new JLabel(" ");

    // Edit Conditional Variables
    boolean inEditConditionalMode = false;
    JmriJFrame editConditionalFrame = null;
    JTextField conditionalUserName = new JTextField(22);
    private JRadioButton _triggerOnChangeButton;

    private ActionTableModel _actionTableModel = null;
    private VariableTableModel _variableTableModel = null;
    private JComboBox<String> _operatorBox;
    private JComboBox<String> _andOperatorBox;
    private JComboBox<String> _notOperatorBox;
    private JTextField _antecedentField;
    private JPanel _antecedentPanel;
    private int _logicType = Conditional.ALL_AND;
    private String _antecedent = null;
    private boolean _newItem = false; // marks a new Action or Variable object was added

    // Conditional Browser Variables
    JmriJFrame condBrowserFrame = null;
    JTextArea condText = null;

    /**
     * Input selection names
     * @since 4.7.3
     */ 
    public enum SelectionMode {
        /** Use the traditional text field, with the tabbed Pick List available for drag-n-drop */
        USEMULTI,
        /** Use the traditional text field, but with a single Pick List that responds with a click */
        USESINGLE,
        /** Use combo boxes to select names instead of a text field. */
        USECOMBO;
    }
    SelectionMode _selectionMode;
    
    // Single pick list name selection variables
    PickSinglePanel _pickSingle = null;     // used to build the JFrame, content copied from the table type pick object.
    JFrame _pickSingleFrame = null;
    JTable _pickTable = null;               // Current pick table
    PickSingleListener _pickListener = null;

    /**
     * Listen for Pick Single table click events.
     * <p>
     * When a table row is selected, the user/system name is copied to the Action or Variable name field.
     * @since 4.7.3
     */
    class PickSingleListener implements ListSelectionListener {
        int saveItemType = -1;          // Current table type
        boolean dragNdrop = false;      // Not currently used

        @Override
        public void valueChanged(ListSelectionEvent e) {
            // Drag and drop can be controlled programmtically.
            if (dragNdrop) {
                return;
            }

            // Determine the current sub-panel type: Variable or Action, ignore item state changes if neither is active
            String chkMode = "None";
            if (_editVariableFrame == null) {
                if (_editActionFrame == null) {
                    return;         // There is no active variable or action input frame
                } else {
                    chkMode = "Action";
                }
            } else {
                chkMode = "Variable";
            }

            int selectedRow = _pickTable.getSelectedRow();
            if (selectedRow >= 0) {
                int selectedCol = _pickTable.getSelectedColumn();
                String newName = (String) _pickTable.getValueAt(selectedRow, selectedCol);

                if (log.isDebugEnabled()) {
                    log.debug("Pick single panel row event: row = '{}', column = '{}', selected name = '{}'", selectedRow, selectedCol, newName);
                }

                // Set the appropriate name field
                if (chkMode.equals("Action")) {
                    _actionNameField.setText(newName);
                } else if (chkMode.equals("Variable")) {
                    _variableNameField.setText(newName);
                }
            }
        }
        
        public int getItemType() {
            return saveItemType;
        }

        public void setItemType(int itemType) {
            saveItemType = itemType;
        }

        public void setDragDrop(boolean dNd) {
            dragNdrop = dNd;
        }
    }

    // Combo boxes for name selection and their JPanel variables
    // These are shared by the conditional and action selection processs
    JmriBeanComboBox _sensorNameBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SensorManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    JmriBeanComboBox _turnoutNameBox = new JmriBeanComboBox(
            InstanceManager.getDefault(TurnoutManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    JmriBeanComboBox _lightNameBox = new JmriBeanComboBox(
            InstanceManager.getDefault(LightManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    JmriBeanComboBox _sigheadNameBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalHeadManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    JmriBeanComboBox _sigmastNameBox = new JmriBeanComboBox(
            InstanceManager.getDefault(SignalMastManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    JmriBeanComboBox _memoryNameBox = new JmriBeanComboBox(
            InstanceManager.getDefault(MemoryManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    JmriBeanComboBox _warrantNameBox = new JmriBeanComboBox(
            InstanceManager.getDefault(WarrantManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    JmriBeanComboBox _oblockNameBox = new JmriBeanComboBox(
            InstanceManager.getDefault(OBlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    JmriBeanComboBox _conditionalNameBox = new JmriBeanComboBox(
            InstanceManager.getDefault(ConditionalManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    JmriBeanComboBox _logixNameBox = new JmriBeanComboBox(
            InstanceManager.getDefault(LogixManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
//    JmriBeanComboBox _entryexitNameBox = new JmriBeanComboBox(
//            InstanceManager.getDefault(EntryExitManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);

    JPanel _actionComboNamePanel;
    JPanel _variableComboNamePanel;
    boolean _nameBoxListenersDone = false;

    /**
     * Listen for name combo box selection events.
     * <p>
     * When a combo box row is selected, the user/system name is copied to the Action or Variable name field.
     * @since 4.7.3
     */
    class NameBoxListener implements ItemListener {

        String _nameType;               // sensor, turnout, memory, etc.
        boolean _programEvent = false;  // When true, program code created the event.  This way we don't update the field that just supplied the new value.

        @Override
        public void itemStateChanged(ItemEvent e) {
            // Determine the current sub-panel type: Variable or Action, ignore item state changes if neither is active
            String chkMode = "None";
            if (_editVariableFrame == null) {
                if (_editActionFrame == null) {
                    return;             // There is no active variable or action input frame
                } else {
                    chkMode = "Action";
                }
            } else {
                chkMode = "Variable";
            }

            // Get the combo box, the display name and the new state
            int newState = e.getStateChange();
            Object src = e.getSource();
            if (!(src instanceof JmriBeanComboBox)) {
                return;
            }
            JmriBeanComboBox srcBox = (JmriBeanComboBox) src;
            String newName = srcBox.getSelectedDisplayName();

            if (newState == ItemEvent.SELECTED && !_programEvent) {
                if (log.isDebugEnabled()) {
                    log.debug("Name ComboBox Item Event: type = '{}', mode = '{}', name = '{}'", _nameType, chkMode, newName);
                }

                // Set the appropriate name field
                if (chkMode.equals("Action")) {
                    _actionNameField.setText(newName);
                } else if (chkMode.equals("Variable")) {
                    _variableNameField.setText(newName);
                }
            }
            _programEvent = false;
        }

        public void setNameType(String type) {
            _nameType = type;
        }

        public void setProgramEvent() {
            _programEvent = true;
        }
    }

    NameBoxListener _sensorNameListener;
    NameBoxListener _turnoutNameListener;
    NameBoxListener _lightNameListener;
    NameBoxListener _sigheadNameListener;
    NameBoxListener _sigmastNameListener;
    NameBoxListener _memoryNameListener;

    NameBoxListener _warrantNameListener;
    NameBoxListener _oblockNameListener;
    NameBoxListener _conditionalNameListener;
    NameBoxListener _logixNameListener;
    NameBoxListener _entryexitNameListener;

// warrent
// oblock
// entry exit (var only)

    /**
     * Components of Edit Variable panes
     */
    JmriJFrame _editVariableFrame = null;
    JComboBox<String> _variableTypeBox;
    JTextField _variableNameField;
    JComboBox<String> _variableStateBox;
    JComboBox<String> _variableCompareOpBox;
    JComboBox<String> _variableSignalBox;
    JComboBox<String> _variableCompareTypeBox;
    JTextField _variableData1Field;
    JTextField _variableData2Field;
    JPanel _variableNamePanel;
    JPanel _variableStatePanel;
    JPanel _variableComparePanel;
    JPanel _variableSignalPanel;
    JPanel _variableData1Panel;
    JPanel _variableData2Panel;

    /**
     * Conponents of Edit Action panes
     */
    JmriJFrame _editActionFrame = null;
    JComboBox<String> _actionItemTypeBox;
    JComboBox<String> _actionTypeBox;
    JComboBox<String> _actionBox;
    JTextField _actionNameField;
    JTextField _longActionString;
    JTextField _shortActionString;
    JComboBox<String> _actionOptionBox;
    JPanel _actionPanel;
    JPanel _actionTypePanel;
    JPanel _namePanel;
    JPanel _shortTextPanel;
    JPanel _optionPanel;

    JButton _actionSetButton;
    JPanel _setPanel;
    JPanel _textPanel;

    /**
     * Listener for _actionTypeBox.
     */
    class ActionTypeListener implements ActionListener {

        int _itemType;

        @Override
        public void actionPerformed(ActionEvent e) {
            int select1 = _actionItemTypeBox.getSelectedIndex();
            int select2 = _actionTypeBox.getSelectedIndex() - 1;
            if (log.isDebugEnabled()) {
                log.debug("ActionTypeListener: actionItemType= " + select1 + ", _itemType= "
                        + _itemType + ", action= " + select2);
            }
            if (select1 != _itemType) {
                if (log.isDebugEnabled()) {
                    log.error("ActionTypeListener actionItem selection (" + select1
                            + ") != expected actionItem (" + _itemType + ")");
                }
            }
            if (_curAction != null) {
                if (select1 > 0 && _itemType == select1) {
                    _curAction.setType(getActionTypeFromBox(_itemType, select2));
                    if (select1 == _itemType) {
                        String text = _actionNameField.getText();
                        if (text != null && text.length() > 0) {
                            _curAction.setDeviceName(text);
                        }
                    }
                    actionItemChanged(_itemType);
                    initializeActionVariables();
                }
            }
        }

        public void setItemType(int type) {
            _itemType = type;
        }
    }
    ActionTypeListener _actionTypeListener = new ActionTypeListener();

    // Current Variable Information
    private ArrayList<ConditionalVariable> _variableList;
    private ConditionalVariable _curVariable;
    private int _curVariableRowNumber;

    // Current Action Information
    private ArrayList<ConditionalAction> _actionList;
    private ConditionalAction _curAction;
    private int _curActionRowNumber;

    static final int STRUT = 10;

    // *********** Methods for Add Logix Window ********************

    /**
     * Respond to the Add button in Logix table Creates and/or initializes the
     * Add Logix pane.
     *
     * @param e The event heard
     */
    @Override
    protected void addPressed(ActionEvent e) {
        // possible change
        if (!checkFlags(null)) {
            return;
        }
        _showReminder = true;
        // make an Add Logix Frame
        if (addLogixFrame == null) {
            JPanel panel5 = makeAddLogixFrame("TitleAddLogix", "AddLogixMessage");
            // Create Logix
            create = new JButton(Bundle.getMessage("ButtonCreate"));
            panel5.add(create);
            create.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            });
            create.setToolTipText(rbx.getString("LogixCreateButtonHint"));
        }
        addLogixFrame.pack();
        addLogixFrame.setVisible(true);
        _autoSystemName.setSelected(false);
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
            _autoSystemName.setSelected(prefMgr.getSimplePreferenceState(systemNameAuto));
        });
    }

    /**
     * Create or copy Logix frame.
     *
     * @param titleId property key to fetch as title of the frame
     * @param messageId part 1 of property key to fetch as user instruction on pane,
     *                  either 1 or 2 is added to form the whole key
     * @return the button JPanel
     */
    JPanel makeAddLogixFrame(String titleId, String messageId) {
        addLogixFrame = new JmriJFrame(rbx.getString(titleId));
        addLogixFrame.addHelpMenu(
                "package.jmri.jmrit.beantable.LogixAddEdit", true);
        addLogixFrame.setLocation(50, 30);
        Container contentPane = addLogixFrame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        JPanel p;
        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        p.add(_sysNameLabel, c);
        c.gridy = 1;
        p.add(_userNameLabel, c);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        p.add(_systemName, c);
        c.gridy = 1;
        p.add(_addUserName, c);
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        c.gridy = 0;
        p.add(_autoSystemName, c);
        _addUserName.setToolTipText(rbx.getString("LogixUserNameHint"));
        _systemName.setToolTipText(rbx.getString("LogixSystemNameHint"));
        contentPane.add(p);
        // set up message
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        JPanel panel31 = new JPanel();
        panel31.setLayout(new FlowLayout());
        JLabel message1 = new JLabel(rbx.getString(messageId + "1"));
        panel31.add(message1);
        JPanel panel32 = new JPanel();
        JLabel message2 = new JLabel(rbx.getString(messageId + "2"));
        panel32.add(message2);
        panel3.add(panel31);
        panel3.add(panel32);
        contentPane.add(panel3);

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        // Cancel
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));
        panel5.add(cancel);
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelAddPressed(e);
            }
        });
        cancel.setToolTipText(rbx.getString("CancelLogixButtonHint"));

        addLogixFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancelAddPressed(null);
            }
        });
        contentPane.add(panel5);

        _autoSystemName.addItemListener(
                new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        autoSystemName();
                    }
                });
        return panel5;
    }

    /**
     * Enable/disable fields for data entry when user selects to have system name
     * automatically generated.
     */
    void autoSystemName() {
        if (_autoSystemName.isSelected()) {
            _systemName.setEnabled(false);
            _sysNameLabel.setEnabled(false);
        } else {
            _systemName.setEnabled(true);
            _sysNameLabel.setEnabled(true);
        }
    }

    /**
     * Respond to the Cancel button in Add Logix window.
     * <p>
     * Note: Also get there if the user closes the Add Logix window.
     * @param e The event heard
     */
    void cancelAddPressed(ActionEvent e) {
        addLogixFrame.setVisible(false);
        addLogixFrame.dispose();
        addLogixFrame = null;
        inCopyMode = false;
        if (f != null) {
            f.setVisible(true);
        }
    }

    /**
     * Respond to the Copy Logix button in Add Logix window.
     * <p>
     * Provides a pane to set new properties of the copy.
     *
     * @param sName system name of Logix to be copied
     */
    void copyPressed(String sName) {
        if (!checkFlags(sName)) {
            return;
        }
        Runnable t = new Runnable() {
            @Override
            public void run() {
                JPanel panel5 = makeAddLogixFrame("TitleCopyLogix", "CopyLogixMessage");
                // Create Logix
                JButton create = new JButton(Bundle.getMessage("ButtonCopy"));
                panel5.add(create);
                create.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        copyLogixPressed(e);
                    }
                });
                addLogixFrame.pack();
                addLogixFrame.setVisible(true);
                _autoSystemName.setSelected(false);
                InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
                    _autoSystemName.setSelected(prefMgr.getSimplePreferenceState(systemNameAuto));
                });
            }
        };
        if (log.isDebugEnabled()) {
            log.debug("copyPressed started for " + sName);
        }
        javax.swing.SwingUtilities.invokeLater(t);
        inCopyMode = true;
        _logixSysName = sName;
    }

    String _logixSysName;

    /**
     * Copy the Logix as configured in the Copy set up pane.
     *
     * @param e the event heard
     */
    void copyLogixPressed(ActionEvent e) {
        String uName = _addUserName.getText().trim(); // N11N
        if (uName.length() == 0) {
            uName = null;
        }
        Logix targetLogix;
        if (_autoSystemName.isSelected()) {
            if (!checkLogixUserName(uName)) {
                return;
            }
            targetLogix = _logixManager.createNewLogix(uName);
        } else {
            if (!checkLogixSysName()) {
                return;
            }
            String sName = _systemName.getText().trim(); // N11N
            // check if a Logix with this name already exists
            boolean createLogix = true;
            targetLogix = _logixManager.getBySystemName(sName);
            if (targetLogix != null) {
                int result = JOptionPane.showConfirmDialog(f, java.text.MessageFormat.format(
                        rbx.getString("ConfirmLogixDuplicate"),
                        new Object[]{sName, _logixSysName}),
                        rbx.getString("ConfirmTitle"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (JOptionPane.NO_OPTION == result) {
                    return;
                }
                createLogix = false;
                String userName = targetLogix.getUserName();
                if (userName.length() > 0) {
                    _addUserName.setText(userName);
                    uName = userName;
                }
            } else if (!checkLogixUserName(uName)) {
                return;
            }
            if (createLogix) {
                // Create the new Logix
                targetLogix = _logixManager.createNewLogix(sName, uName);
                if (targetLogix == null) {
                    // should never get here unless there is an assignment conflict
                    log.error("Failure to create Logix with System Name: " + sName);
                    return;
                }
            } else if (targetLogix == null) {
                log.error("Error targetLogix is null!");
                return;
            } else {
                targetLogix.setUserName(uName);
            }
        }
        Logix srcLogic = _logixManager.getBySystemName(_logixSysName);
        for (int i = 0; i < srcLogic.getNumConditionals(); i++) {
            String cSysName = srcLogic.getConditionalByNumberOrder(i);
            copyConditionalToLogix(cSysName, srcLogic, targetLogix);
        }
        cancelAddPressed(null);
    }

    /**
     * Copy a given Conditional from one Logix to another.
     *
     * @param cSysName system name of the Conditional
     * @param srcLogix original Logix containing the Conditional
     * @param targetLogix target Logix to copy to
     */
    void copyConditionalToLogix(String cSysName, Logix srcLogix, Logix targetLogix) {
        Conditional cOld = _conditionalManager.getBySystemName(cSysName);
        if (cOld == null) {
            log.error("Failure to find Conditional with System Name: " + cSysName);
            return;
        }
        String cOldSysName = cOld.getSystemName();
        String cOldUserName = cOld.getUserName();

        // make system name for new conditional
        int num = targetLogix.getNumConditionals() + 1;
        String cNewSysName = targetLogix.getSystemName() + "C" + Integer.toString(num);
        // add to Logix at the end of the calculate order
        String cNewUserName = java.text.MessageFormat.format(rbx.getString("CopyOf"), cOldUserName);
        if (cOldUserName.length() == 0) {
            cNewUserName += "C" + Integer.toString(num);
        }
        do {
            cNewUserName = JOptionPane.showInputDialog(f, java.text.MessageFormat.format(
                    rbx.getString("NameConditionalCopy"), new Object[]{
                        cOldUserName, cOldSysName, _logixSysName,
                        targetLogix.getUserName(), targetLogix.getSystemName()}),
                    cNewUserName);
            if (cNewUserName == null || cNewUserName.length() == 0) {
                return;
            }
        } while (!checkConditionalUserName(cNewUserName, targetLogix));

        while (!checkConditionalSystemName(cNewSysName)) {
            cNewSysName = targetLogix.getSystemName() + "C" + ++num;
        }

        Conditional cNew = _conditionalManager.createNewConditional(cNewSysName, cNewUserName);
        if (cNew == null) {
            // should never get here unless there is an assignment conflict
            log.error("Failure to create Conditional with System Name: \""
                    + cNewSysName + "\" and User Name: \"" + cNewUserName + "\"");
            return;
        }
        cNew.setLogicType(cOld.getLogicType(), cOld.getAntecedentExpression());
        cNew.setStateVariables(cOld.getCopyOfStateVariables());
        cNew.setAction(cOld.getCopyOfActions());
        targetLogix.addConditional(cNewSysName, -1);
    }

    /**
     * Check and warn if a string is already in use as the user name of a Logix.
     *
     * @param uName the suggested name
     * @return true if not in use
     */
    boolean checkLogixUserName(String uName) {
        // check if a Logix with the same user name exists
        if (uName != null && uName.trim().length() > 0) {
            Logix x = _logixManager.getByUserName(uName);
            if (x != null) {
                // Logix with this user name already exists
                javax.swing.JOptionPane.showMessageDialog(addLogixFrame,
                        rbx.getString("Error3"), Bundle.getMessage("ErrorTitle"),
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    /**
     * Check validity of Logix system name.
     * <p>
     * Fixes name if it doesn't start with "IX".
     * </p>
     * @return false if name has length &lt; 1 after displaying a dialog
     */
    boolean checkLogixSysName() {
        String sName = _systemName.getText().trim(); // N11N
        if ((sName.length() < 1)) {
            // Entered system name is blank or too short
            javax.swing.JOptionPane.showMessageDialog(addLogixFrame,
                    rbx.getString("Error8"), Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if ((sName.length() < 2) || (sName.charAt(0) != 'I')
                || (sName.charAt(1) != 'X')) {
            // System name does not begin with IX, prefix IX to it
            String s = sName;
            sName = "IX" + s;
        }
        _systemName.setText(sName);
        return true;
    }

    /**
     * Check if another Logix editing session is currently open
     * or no system name is provided.
     *
     * @param sName system name of Logix to be copied
     * @return true if a new session may be started
     */
    boolean checkFlags(String sName) {
        if (inEditMode) {
            // Already editing a Logix, ask for completion of that edit
            javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
                    java.text.MessageFormat.format(rbx.getString("Error32"),
                            new Object[]{_curLogix.getSystemName()}), Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (inCopyMode) {
            // Already editing a Logix, ask for completion of that edit
            javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
                    java.text.MessageFormat.format(rbx.getString("Error31"),
                            new Object[]{_logixSysName}), Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (sName != null) {
            // check if a Logix with this name exists
            Logix x = _logixManager.getBySystemName(sName);
            if (x == null) {
                // Logix does not exist, so cannot be edited
                log.error("No Logix with system name: " + sName);
                javax.swing.JOptionPane.showMessageDialog(editLogixFrame, rbx
                        .getString("Error5"), Bundle.getMessage("ErrorTitle"),
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                if (editLogixFrame != null) {
                    editLogixFrame.setVisible(false);
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Respond to the Create Logix button in Add Logix window.
     *
     * @param e The event heard
     */
    void createPressed(ActionEvent e) {
        // possible change
        _showReminder = true;
        String uName = _addUserName.getText().trim(); // N11N
        if (uName.length() == 0) {
            uName = null;
        }
        String sName = _systemName.getText().trim(); // N11N
        if (_autoSystemName.isSelected()) {
            if (!checkLogixUserName(uName)) {
                return;
            }
            _curLogix = _logixManager.createNewLogix(uName);
        } else {
            if (!checkLogixSysName()) {
                return;
            }
            // check if a Logix with this name already exists
            Logix x = null;
            try {
                x = _logixManager.getBySystemName(sName);
            } catch (Exception ex) {
                // user input no good
                handleCreateException(sName);
                return; // without creating       
            }
            if (x != null) {
                // Logix already exists
                javax.swing.JOptionPane.showMessageDialog(addLogixFrame, rbx
                        .getString("Error1"), Bundle.getMessage("ErrorTitle"),
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!checkLogixUserName(uName)) {
                return;
            }
            // Create the new Logix
            _curLogix = _logixManager.createNewLogix(sName, uName);
            if (_curLogix == null) {
                // should never get here unless there is an assignment conflict
                log.error("Failure to create Logix with System Name: " + sName);
                return;
            }
        }
        numConditionals = 0;
        cancelAddPressed(null);
        // create the Edit Logix Window
        makeEditLogixWindow();
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
            prefMgr.setSimplePreferenceState(systemNameAuto, _autoSystemName.isSelected());
        });
    }

    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(addLogixFrame,
                java.text.MessageFormat.format(
                        Bundle.getMessage("ErrorLogixAddFailed"),
                        new Object[]{sysName}),
                Bundle.getMessage("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    // *********** Methods for Edit Logix Pane ********************

    /**
     * Respond to the Edit button pressed in Logix table.
     *
     * @param sName system name of Logix to be edited
     */
    void editPressed(String sName) {
        if (!checkFlags(sName)) {
            return;
        }
        // Logix was found, initialize for edit
        _curLogix = _logixManager.getBySystemName(sName);
        numConditionals = _curLogix.getNumConditionals();
        // create the Edit Logix Window
        // Use separate operation so window is created on top
        Runnable t = new Runnable() {
            @Override
            public void run() {
                makeEditLogixWindow();
            }
        };
        if (log.isDebugEnabled()) {
            log.debug("editPressed Runnable started for " + sName);
        }
        javax.swing.SwingUtilities.invokeLater(t);
    }

    /**
     * Create and/or initialize the Edit Logix pane.
     */
    void makeEditLogixWindow() {
        setNameBoxListeners();      // Setup the name box components.
        //if (log.isDebugEnabled()) log.debug("makeEditLogixWindow ");
        editUserName.setText(_curLogix.getUserName());
        // clear conditional table if needed
        if (conditionalTableModel != null) {
            conditionalTableModel.fireTableStructureChanged();
        }
        inEditMode = true;
        if (editLogixFrame == null) {
            editLogixFrame = new JmriJFrame(rbx.getString("TitleEditLogix"), false, false);
            editLogixFrame.addHelpMenu(
                    "package.jmri.jmrit.beantable.LogixAddEdit", true);
            editLogixFrame.setLocation(100, 30);
            Container contentPane = editLogixFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel systemNameLabel = new JLabel(Bundle.getMessage("ColumnSystemName") + ":");
            panel1.add(systemNameLabel);
            JLabel fixedSystemName = new JLabel(_curLogix.getSystemName());
            panel1.add(fixedSystemName);
            contentPane.add(panel1);
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel userNameLabel = new JLabel(Bundle.getMessage("ColumnUserName") + ":");
            panel2.add(userNameLabel);
            panel2.add(editUserName);
            editUserName.setToolTipText(rbx.getString("LogixUserNameHint2"));
            contentPane.add(panel2);
            // add table of Conditionals
            JPanel pctSpace = new JPanel();
            pctSpace.setLayout(new FlowLayout());
            pctSpace.add(new JLabel("   "));
            contentPane.add(pctSpace);
            JPanel pTitle = new JPanel();
            pTitle.setLayout(new FlowLayout());
            pTitle.add(new JLabel(rbx.getString("ConditionalTableTitle")));
            contentPane.add(pTitle);
            // initialize table of conditionals
            conditionalTableModel = new ConditionalTableModel();
            JTable conditionalTable = new JTable(conditionalTableModel);
            conditionalTable.setRowSelectionAllowed(false);
            TableColumnModel conditionalColumnModel = conditionalTable
                    .getColumnModel();
            TableColumn sNameColumn = conditionalColumnModel
                    .getColumn(ConditionalTableModel.SNAME_COLUMN);
            sNameColumn.setResizable(true);
            sNameColumn.setMinWidth(100);
            sNameColumn.setPreferredWidth(130);
            TableColumn uNameColumn = conditionalColumnModel
                    .getColumn(ConditionalTableModel.UNAME_COLUMN);
            uNameColumn.setResizable(true);
            uNameColumn.setMinWidth(210);
            uNameColumn.setPreferredWidth(260);
            TableColumn stateColumn = conditionalColumnModel
                    .getColumn(ConditionalTableModel.STATE_COLUMN);
            stateColumn.setResizable(true);
            stateColumn.setMinWidth(50);
            stateColumn.setMaxWidth(100);
            TableColumn buttonColumn = conditionalColumnModel
                    .getColumn(ConditionalTableModel.BUTTON_COLUMN);

            // install button renderer and editor
            ButtonRenderer buttonRenderer = new ButtonRenderer();
            conditionalTable.setDefaultRenderer(JButton.class, buttonRenderer);
            TableCellEditor buttonEditor = new ButtonEditor(new JButton());
            conditionalTable.setDefaultEditor(JButton.class, buttonEditor);
            JButton testButton = new JButton("XXXXXX");
            conditionalTable.setRowHeight(testButton.getPreferredSize().height);
            buttonColumn.setMinWidth(testButton.getPreferredSize().width);
            buttonColumn.setMaxWidth(testButton.getPreferredSize().width);
            buttonColumn.setResizable(false);

            JScrollPane conditionalTableScrollPane = new JScrollPane(conditionalTable);
            Dimension dim = conditionalTable.getPreferredSize();
            dim.height = 450;
            conditionalTableScrollPane.getViewport().setPreferredSize(dim);
            contentPane.add(conditionalTableScrollPane);

            // add message area between table and buttons
            JPanel panel4 = new JPanel();
            panel4.setLayout(new BoxLayout(panel4, BoxLayout.Y_AXIS));
            JPanel panel41 = new JPanel();
            panel41.setLayout(new FlowLayout());
            panel41.add(status);
            panel4.add(panel41);
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
            // Conditional panel buttons - New Conditional
            JButton newConditionalButton = new JButton(rbx.getString("NewConditionalButton"));
            panel42.add(newConditionalButton);
            newConditionalButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    newConditionalPressed(e);
                }
            });
            newConditionalButton.setToolTipText(rbx.getString("NewConditionalButtonHint"));
            // Conditional panel buttons - Reorder
            JButton reorderButton = new JButton(rbx.getString("ReorderButton"));
            panel42.add(reorderButton);
            reorderButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    reorderPressed(e);
                }
            });
            reorderButton.setToolTipText(rbx.getString("ReorderButtonHint"));
            // Conditional panel buttons - Calculate
            JButton calculateButton = new JButton(rbx.getString("CalculateButton"));
            panel42.add(calculateButton);
            calculateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    calculatePressed(e);
                }
            });
            calculateButton.setToolTipText(rbx.getString("CalculateButtonHint"));
            panel4.add(panel42);
            Border panel4Border = BorderFactory.createEtchedBorder();
            panel4.setBorder(panel4Border);
            contentPane.add(panel4);
            // add buttons at bottom of window
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            // Bottom Buttons - Done Logix
            JButton done = new JButton(Bundle.getMessage("ButtonDone"));
            panel5.add(done);
            done.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    donePressed(e);
                }
            });
            done.setToolTipText(rbx.getString("DoneButtonHint"));
            // Delete Logix
            JButton delete = new JButton(Bundle.getMessage("ButtonDelete"));
            panel5.add(delete);
            delete.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deletePressed(e);
                }
            });
            delete.setToolTipText(rbx.getString("DeleteLogixButtonHint"));
            contentPane.add(panel5);
        }

        editLogixFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (inEditMode) {
                    donePressed(null);
                } else {
                    finishDone();
                }
            }
        });
        editLogixFrame.pack();
        editLogixFrame.setVisible(true);
    }

    /**
     * Display reminder to save.
     */
    void showSaveReminder() {
        /*if (_showReminder && !_suppressReminder) {
         javax.swing.JOptionPane.showMessageDialog(editLogixFrame, rbx
         .getString("Reminder1"),
         rbx.getString("ReminderTitle"),
         javax.swing.JOptionPane.INFORMATION_MESSAGE);
         }*/
        if (_showReminder) {
            if (InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class) != null) {
                InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showInfoMessage(Bundle.getMessage("ReminderTitle"), Bundle.getMessage("ReminderSaveString", Bundle.getMessage("MenuItemLogixTable")),
                                getClassName(),
                                "remindSaveLogix"); // NOI18N
            }
        }
    }

    /**
     * Respond to the Reorder Button in the Edit Logix pane.
     *
     * @param e The event heard
     */
    void reorderPressed(ActionEvent e) {
        if (checkEditConditional()) {
            return;
        }
        // Check if reorder is reasonable
        _showReminder = true;
        _nextInOrder = 0;
        _inReorderMode = true;
        status.setText(rbx.getString("ReorderMessage"));
        conditionalTableModel.fireTableDataChanged();
    }

    /**
     * Respond to the First/Next (Delete) Button in the Edit Logix window.
     *
     * @param row index of the row to put as next in line (instead of the one that was supposed to be next)
     */
    void swapConditional(int row) {
        _curLogix.swapConditional(_nextInOrder, row);
        _nextInOrder++;
        if (_nextInOrder >= numConditionals) {
            _inReorderMode = false;
        }
        //status.setText("");
        conditionalTableModel.fireTableDataChanged();
    }

    /**
     * Responds to the Calculate Button in the Edit Logix window
     *
     * @param e The event heard
     */
    void calculatePressed(ActionEvent e) {
        if (checkEditConditional()) {
            return;
        }
        // are there Conditionals to calculate?
        if (numConditionals > 0) {
            // There are conditionals to calculate
            String cName = "";
            Conditional c = null;
            for (int i = 0; i < numConditionals; i++) {
                cName = _curLogix.getConditionalByNumberOrder(i);
                if (cName != null) {
                    c = _conditionalManager.getBySystemName(cName);
                    if (c == null) {
                        log.error("Invalid conditional system name when calculating - "
                                + cName);
                    } else {
                        // calculate without taking any action
                        c.calculate(false, null);
                    }
                } else {
                    log.error("null conditional system name when calculating");
                }
            }
            // force the table to update
            conditionalTableModel.fireTableDataChanged();
        }
    }

    /**
     * Respond to the Done button in the Edit Logix window.
     * <p>
     * Note: We also get here
     * if the Edit Logix window is dismissed, or if the Add button is pressed in
     * the Logic Table with an active Edit Logix window.
     *
     * @param e The event heard
     */
    void donePressed(ActionEvent e) {
        if (_curLogix == null) {
            log.error("null pointer to _curLogix in donePressed method");
            finishDone();
            return;
        }
        if (checkEditConditional()) {
            return;
        }
        if (_curLogix.getSystemName().equals(SensorGroupFrame.logixSysName)) {
            finishDone();
            return;
        }
        // Check if the User Name has been changed
        String uName = editUserName.getText().trim(); // N11N
        if (!(uName.equals(_curLogix.getUserName()))) {
            // user name has changed - check if already in use
            if (uName.length() > 0) {
                Logix p = _logixManager.getByUserName(uName);
                if (p != null) {
                    // Logix with this user name already exists
                    log.error("Failure to update Logix with Duplicate User Name: "
                            + uName);
                    javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
                            rbx.getString("Error6"), Bundle.getMessage("ErrorTitle"),
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            // user name is unique, change it
            _curLogix.setUserName(uName);
            m.fireTableDataChanged();
        }
        // complete update and activate Logix
        finishDone();
    }


    void finishDone() {
        showSaveReminder();
        inEditMode = false;
        if (editLogixFrame != null) {
            editLogixFrame.setVisible(false);
            editLogixFrame.dispose();
            editLogixFrame = null;
        }
        // bring Logix Table to front
        if (f != null) {
            f.setVisible(true);
        }
    }

    @Override
    public void setMessagePreferencesDetails() {
        HashMap<Integer, String> options = new HashMap< Integer, String>(3);
        options.put(0x00, Bundle.getMessage("DeleteAsk"));
        options.put(0x01, Bundle.getMessage("DeleteNever"));
        options.put(0x02, Bundle.getMessage("DeleteAlways"));
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).messageItemDetails(getClassName(), "delete", rbx.getString("DeleteLogix"), options, 0x00);
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).preferenceItemDetails(getClassName(), "remindSaveLogix", rbx.getString("SuppressWithDisable"));
        super.setMessagePreferencesDetails();
    }

    /**
     * Respond to the Delete combo selection Logix window.
     *
     * @param sName system name of bean to be deleted
     */
    void deletePressed(String sName) {
        if (!checkFlags(sName)) {
            return;
        }
        final Logix x = _logixManager.getBySystemName(sName);
        final jmri.UserPreferencesManager p;
        p = jmri.InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class);
        if (p != null && p.getMultipleChoiceOption(getClassName(), "delete") == 0x02) {
            if (x != null) {
                _logixManager.deleteLogix(x);
            }
        } else {
            final JDialog dialog = new JDialog();
            String msg;
            dialog.setTitle(rbx.getString("ConfirmTitle"));
            dialog.setLocationRelativeTo(null);
            dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
            JPanel container = new JPanel();
            container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            msg = java.text.MessageFormat.format(
                    rbx.getString("ConfirmLogixDelete"), sName);
            JLabel question = new JLabel(msg);
            question.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.add(question);

            final JCheckBox remember = new JCheckBox(Bundle.getMessage("MessageRememberSetting"));
            remember.setFont(remember.getFont().deriveFont(10f));
            remember.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton yesButton = new JButton(Bundle.getMessage("ButtonYes"));
            JButton noButton = new JButton(Bundle.getMessage("ButtonNo"));
            JPanel button = new JPanel();
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.add(yesButton);
            button.add(noButton);
            container.add(button);

            noButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //there is no point in remebering this the user will never be
                    //able to delete a bean!
                    /*if(remember.isSelected()){
                     setDisplayDeleteMsg(0x01);
                     }*/
                    dialog.dispose();
                }
            });

            yesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (p != null && remember.isSelected()) {
                        p.setMultipleChoiceOption(getClassName(), "delete", 0x02);
                    }
                    if (x != null) {
                        _logixManager.deleteLogix(x);
                    }
                    dialog.dispose();
                }
            });
            container.add(remember);
            container.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.setAlignmentY(Component.CENTER_ALIGNMENT);
            dialog.getContentPane().add(container);
            dialog.pack();
            dialog.setModal(true);
            dialog.setVisible(true);
        }

        /*if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(f, java.text.MessageFormat.format(
         rbx.getString("ConfirmLogixDelete"), sName),
         rbx.getString("ConfirmTitle"), JOptionPane.YES_NO_OPTION,
         JOptionPane.QUESTION_MESSAGE) )
         {
         Logix x = _logixManager.getBySystemName(sName);
         if (x != null) {
         _logixManager.deleteLogix(x);
         }
         }*/
        f.setVisible(true);
    }

    /**
     * Respond to the Delete button in the Edit Logix window.
     *
     * @param e The event heard
     */
    void deletePressed(ActionEvent e) {
        if (checkEditConditional()) {
            return;
        }
        _showReminder = true;
        Logix x = _curLogix;
        // delete this Logix
        _logixManager.deleteLogix(x);
        _curLogix = null;
        finishDone();
    }

    /**
     * Respond to the New Conditional Button in Edit Logix Window.
     *
     * @param e The event heard
     */
    void newConditionalPressed(ActionEvent e) {
        if (checkEditConditional()) {
            return;
        }
        if (_curLogix.getSystemName().equals(SensorGroupFrame.logixSysName)) {
            javax.swing.JOptionPane.showMessageDialog(
                    editLogixFrame, java.text.MessageFormat.format(rbx.getString("Warn8"),
                            new Object[]{SensorGroupFrame.logixUserName, SensorGroupFrame.logixSysName}),
                    Bundle.getMessage("WarningTitle"),
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        // make system name for new conditional
        int num = _curLogix.getNumConditionals() + 1;
        _curConditional = null;
        String cName = null;
        while (_curConditional == null) {
            cName = _curLogix.getSystemName() + "C" + Integer.toString(num);
            _curConditional = _conditionalManager.createNewConditional(cName, "");
            num++;
            if (num == 1000) {
                break;
            }
        }
        if (_curConditional == null) {
            // should never get here unless there is an assignment conflict
            log.error("Failure to create Conditional with System Name: "
                    + cName);
            return;
        }
        // add to Logix at the end of the calculate order
        _curLogix.addConditional(cName, -1);
        conditionalTableModel.fireTableRowsInserted(numConditionals, numConditionals);
        conditionalRowNumber = numConditionals;
        numConditionals++;
        _showReminder = true;
        // clear action items
        _actionList = new ArrayList<ConditionalAction>();
        _variableList = new ArrayList<ConditionalVariable>();
        makeEditConditionalWindow();
    }

    /**
     * Respond to Edit Button in the Conditional table of the Edit Logix Window.
     *
     * @param rx index (row number) of Conditional te be edited
     */
    void editConditionalPressed(int rx) {
        if (inEditConditionalMode) {
            // Already editing a Conditional, ask for completion of that edit
            javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                    java.text.MessageFormat.format(rbx.getString("Error34"),
                            new Object[]{_curConditional.getSystemName()}),
                    Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        // get Conditional to edit
        _curConditional = _conditionalManager.getBySystemName(_curLogix.getConditionalByNumberOrder(rx));
        if (_curConditional == null) {
            log.error("Attempted edit of non-existant conditional.");
            return;
        }
        _variableList = _curConditional.getCopyOfStateVariables();
        conditionalRowNumber = rx;
        // get action variables
        _actionList = _curConditional.getCopyOfActions();
        makeEditConditionalWindow();
    }  /* editConditionalPressed */


    /**
     * Check if edit of a conditional is in progress.
     *
     * @return true if this is the case, after showing dialog to user
     */
    boolean checkEditConditional() {
        if (inEditConditionalMode) {
            // Already editing a Conditional, ask for completion of that edit
            javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                    java.text.MessageFormat.format(rbx.getString("Error35"),
                            new Object[]{_curConditional.getSystemName()}),
                    Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return true;
        }
        return false;
    }

    boolean checkConditionalUserName(String uName, Logix logix) {
        if ((uName != null) && (!(uName.equals("")))) {
            Conditional p = _conditionalManager.getByUserName(logix, uName);
            if (p != null) {
                // Conditional with this user name already exists
                log.error("Failure to update Conditional with Duplicate User Name: "
                        + uName);
                javax.swing.JOptionPane.showMessageDialog(
                        editConditionalFrame, rbx.getString("Error10"),
                        Bundle.getMessage("ErrorTitle"),
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } // else return false;
        return true;
    }

    /**
     * Check form of Conditional systemName.
     *
     * @param sName system name of bean to be checked
     * @return false if sName is empty string or null
     */
    boolean checkConditionalSystemName(String sName) {
        if ((sName != null) && (!(sName.equals("")))) {
            Conditional p = _conditionalManager.getBySystemName(sName);
            if (p != null) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    // ********************* Edit Conditional Window and Methods *******************

    /**
     * Create and/or initialize the Edit Conditional window.
     * <p>
     * Note: you can get here via the New Conditional button (newConditionalPressed)
     * or via an Edit button in the Conditional table of the Edit Logix window.
     */
    void makeEditConditionalWindow() {
        // deactivate this Logix
        _curLogix.deActivateLogix();
        conditionalUserName.setText(_curConditional.getUserName());
        if (editConditionalFrame == null) {
            editConditionalFrame = new JmriJFrame(rbx.getString("TitleEditConditional"), false, false);
            editConditionalFrame.addHelpMenu(
                    "package.jmri.jmrit.beantable.ConditionalAddEdit", true);
            Container contentPane = editConditionalFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            panel1.add(new JLabel(Bundle.getMessage("ColumnSystemName") + ":"));
            panel1.add(new JLabel(_curConditional.getSystemName()));
            contentPane.add(panel1);
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            panel2.add(new JLabel(Bundle.getMessage("ColumnUserName") + ":"));
            panel2.add(conditionalUserName);
            conditionalUserName.setToolTipText(rbx.getString("ConditionalUserNameHint"));
            contentPane.add(panel2);

            // add Logical Expression Section
            JPanel logicPanel = new JPanel();
            logicPanel.setLayout(new BoxLayout(logicPanel, BoxLayout.Y_AXIS));

            // add Antecedent Expression Panel -ONLY appears for MIXED operator statements
            _antecedent = _curConditional.getAntecedentExpression();
            _logicType = _curConditional.getLogicType();
            _antecedentField = new JTextField(65);
            _antecedentField.setFont(new Font("SansSerif", Font.BOLD, 14));
            _antecedentField.setText(_antecedent);
            _antecedentPanel = makeEditPanel(_antecedentField, "LabelAntecedent", "LabelAntecedentHint");

            JButton helpButton = new JButton(Bundle.getMessage("MenuHelp"));
            _antecedentPanel.add(helpButton);
            helpButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    helpPressed(e);
                }
            });
            _antecedentPanel.add(helpButton);
            _antecedentPanel.setVisible(_logicType == Conditional.MIXED);
            logicPanel.add(_antecedentPanel);

            // add state variable table title
            JPanel varTitle = new JPanel();
            varTitle.setLayout(new FlowLayout());
            varTitle.add(new JLabel(rbx.getString("StateVariableTableTitle")));
            logicPanel.add(varTitle);
            // set up state variables table
            // initialize and populate Combo boxes for table of state variables
            _notOperatorBox = new JComboBox<String>();
            _notOperatorBox.addItem(" ");
            _notOperatorBox.addItem(Bundle.getMessage("LogicNOT"));

            _andOperatorBox = new JComboBox<String>();
            _andOperatorBox.addItem(Bundle.getMessage("LogicAND"));
            _andOperatorBox.addItem(Bundle.getMessage("LogicOR"));
            // initialize table of state variables
            _variableTableModel = new VariableTableModel();
            JTable variableTable = new JTable(_variableTableModel);
            variableTable.setRowHeight(_notOperatorBox.getPreferredSize().height);
            variableTable.setRowSelectionAllowed(false);
            int rowHeight = variableTable.getRowHeight();

            TableColumnModel variableColumnModel = variableTable.getColumnModel();

            TableColumn rowColumn = variableColumnModel.getColumn(VariableTableModel.ROWNUM_COLUMN);
            rowColumn.setResizable(false);
            rowColumn.setMaxWidth(new JTextField(3).getPreferredSize().width);

            TableColumn andColumn = variableColumnModel.getColumn(VariableTableModel.AND_COLUMN);
            andColumn.setResizable(false);
            andColumn.setCellEditor(new DefaultCellEditor(_andOperatorBox));
            andColumn.setMaxWidth(_andOperatorBox.getPreferredSize().width - 5);

            TableColumn notColumn = variableColumnModel.getColumn(VariableTableModel.NOT_COLUMN);
            notColumn.setCellEditor(new DefaultCellEditor(_notOperatorBox));
            notColumn.setMaxWidth(_notOperatorBox.getPreferredSize().width - 5);
            notColumn.setResizable(false);

            TableColumn descColumn = variableColumnModel.getColumn(VariableTableModel.DESCRIPTION_COLUMN);
            descColumn.setPreferredWidth(300);
            descColumn.setMinWidth(200);
            descColumn.setResizable(true);

            TableColumn stateColumn = variableColumnModel.getColumn(VariableTableModel.STATE_COLUMN);
            stateColumn.setResizable(true);
            stateColumn.setMinWidth(50);
            stateColumn.setMaxWidth(80);

            TableColumn triggerColumn = variableColumnModel.getColumn(VariableTableModel.TRIGGERS_COLUMN);
            triggerColumn.setResizable(true);
            triggerColumn.setMinWidth(30);
            triggerColumn.setMaxWidth(80);

            TableColumn editColumn = variableColumnModel.getColumn(VariableTableModel.EDIT_COLUMN);
            ButtonRenderer buttonRenderer = new ButtonRenderer();
            variableTable.setDefaultRenderer(JButton.class, buttonRenderer);
            TableCellEditor buttonEditor = new ButtonEditor(new JButton());
            variableTable.setDefaultEditor(JButton.class, buttonEditor);
            JButton testButton = new JButton("XXXXXX");
            variableTable.setRowHeight(testButton.getPreferredSize().height);
            editColumn.setMinWidth(testButton.getPreferredSize().width);
            editColumn.setMaxWidth(testButton.getPreferredSize().width);
            editColumn.setResizable(false);

            TableColumn deleteColumn = variableColumnModel.getColumn(VariableTableModel.DELETE_COLUMN);
            // ButtonRenderer and TableCellEditor already set
            deleteColumn.setMinWidth(testButton.getPreferredSize().width);
            deleteColumn.setMaxWidth(testButton.getPreferredSize().width);
            deleteColumn.setResizable(false);
            // add a scroll pane
            JScrollPane variableTableScrollPane = new JScrollPane(variableTable);
            Dimension dim = variableTable.getPreferredSize();
            dim.height = 7 * rowHeight;
            variableTableScrollPane.getViewport().setPreferredSize(dim);

            logicPanel.add(variableTableScrollPane);

            // set up state variable buttons and logic
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
            //  Add State Variable
            JButton addVariableButton = new JButton(rbx.getString("AddVariableButton"));
            panel42.add(addVariableButton);
            addVariableButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addVariablePressed(e);
                }
            });
            addVariableButton.setToolTipText(rbx.getString("AddVariableButtonHint"));

            JButton checkVariableButton = new JButton(rbx.getString("CheckVariableButton"));
            panel42.add(checkVariableButton);
            checkVariableButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    checkVariablePressed(e);
                }
            });
            checkVariableButton.setToolTipText(rbx.getString("CheckVariableButtonHint"));
            logicPanel.add(panel42);

            // logic type area
            _operatorBox = new JComboBox<String>(new String[]{
                    Bundle.getMessage("LogicAND"),
                    Bundle.getMessage("LogicOR"),
                    Bundle.getMessage("LogicMixed")});
            JPanel typePanel = makeEditPanel(_operatorBox, "LabelLogicType", "TypeLogicHint");
            _operatorBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    logicTypeChanged(e);
                }
            });
            _operatorBox.setSelectedIndex(_logicType - 1);
            logicPanel.add(typePanel);
            logicPanel.add(Box.createHorizontalStrut(STRUT));

            Border logicPanelBorder = BorderFactory.createEtchedBorder();
            Border logicPanelTitled = BorderFactory.createTitledBorder(
                    logicPanelBorder, rbx.getString("TitleLogicalExpression") + " ");
            logicPanel.setBorder(logicPanelTitled);
            contentPane.add(logicPanel);
            // End of Logic Expression Section

            JPanel triggerPanel = new JPanel();
            triggerPanel.setLayout(new BoxLayout(triggerPanel, BoxLayout.Y_AXIS));
            ButtonGroup tGroup = new ButtonGroup();
            _triggerOnChangeButton = new JRadioButton(rbx.getString("triggerOnChange"));
            _triggerOnChangeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    _actionTableModel.fireTableDataChanged();
                }
            });
            tGroup.add(_triggerOnChangeButton);
            triggerPanel.add(_triggerOnChangeButton);
            JRadioButton triggerOnAny = new JRadioButton(rbx.getString("triggerOnAny"));
            triggerOnAny.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    _actionTableModel.fireTableDataChanged();
                }
            });
            tGroup.add(triggerOnAny);
            triggerPanel.add(triggerOnAny);
            triggerOnAny.setSelected(true);
            JPanel trigPanel = new JPanel();
            trigPanel.add(triggerPanel);
            contentPane.add(trigPanel);
            _triggerOnChangeButton.setSelected(_curConditional.getTriggerOnChange());

            // add Action Consequents Section
            JPanel conseqentPanel = new JPanel();
            conseqentPanel.setLayout(new BoxLayout(conseqentPanel, BoxLayout.Y_AXIS));

            JPanel actTitle = new JPanel();
            actTitle.setLayout(new FlowLayout());
            actTitle.add(new JLabel(rbx.getString("ActionTableTitle")));
            conseqentPanel.add(actTitle);

            // set up action consequents table
            _actionTableModel = new ActionTableModel();
            JTable actionTable = new JTable(_actionTableModel);
            actionTable.setRowSelectionAllowed(false);
            actionTable.setRowHeight(testButton.getPreferredSize().height);
            JPanel actionPanel = new JPanel();
            actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
            JPanel actionTitle = new JPanel();
            actionTitle.setLayout(new FlowLayout());
            conseqentPanel.add(actionPanel);

            TableColumnModel actionColumnModel = actionTable.getColumnModel();

            TableColumn descriptionColumn = actionColumnModel.getColumn(
                    ActionTableModel.DESCRIPTION_COLUMN);
            descriptionColumn.setResizable(true);
            descriptionColumn.setPreferredWidth(600);
            descriptionColumn.setMinWidth(300);
            //descriptionColumn.setMaxWidth(760);

            TableColumn actionEditColumn = actionColumnModel.getColumn(ActionTableModel.EDIT_COLUMN);
            // ButtonRenderer already exists
            actionTable.setDefaultRenderer(JButton.class, buttonRenderer);
            TableCellEditor editButEditor = new ButtonEditor(new JButton());
            actionTable.setDefaultEditor(JButton.class, editButEditor);
            actionEditColumn.setMinWidth(testButton.getPreferredSize().width);
            actionEditColumn.setMaxWidth(testButton.getPreferredSize().width);
            actionEditColumn.setResizable(false);

            TableColumn actionDeleteColumn = actionColumnModel.getColumn(ActionTableModel.DELETE_COLUMN);
            // ButtonRenderer and TableCellEditor already set
            actionDeleteColumn.setMinWidth(testButton.getPreferredSize().width);
            actionDeleteColumn.setMaxWidth(testButton.getPreferredSize().width);
            actionDeleteColumn.setResizable(false);
            // add a scroll pane
            JScrollPane actionTableScrollPane = new JScrollPane(actionTable);
            dim = actionTableScrollPane.getPreferredSize();
            dim.height = 7 * rowHeight;
            actionTableScrollPane.getViewport().setPreferredSize(dim);
            conseqentPanel.add(actionTableScrollPane);

            // add action buttons to Action Section
            JPanel panel43 = new JPanel();
            panel43.setLayout(new FlowLayout());
            JButton addActionButton = new JButton(rbx.getString("addActionButton"));
            panel43.add(addActionButton);
            addActionButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addActionPressed(e);
                }
            });

            addActionButton.setToolTipText(rbx.getString("addActionButtonHint"));
            conseqentPanel.add(panel43);
            //  - Reorder action button
            JButton reorderButton = new JButton(rbx.getString("ReorderButton"));
            panel43.add(reorderButton);
            reorderButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    reorderActionPressed(e);
                }
            });
            reorderButton.setToolTipText(rbx.getString("ReorderButtonHint"));
            conseqentPanel.add(panel43);

            Border conseqentPanelBorder = BorderFactory.createEtchedBorder();
            Border conseqentPanelTitled = BorderFactory.createTitledBorder(
                    conseqentPanelBorder, rbx.getString("TitleAction"));
            conseqentPanel.setBorder(conseqentPanelTitled);
            contentPane.add(conseqentPanel);
            // End of Action Consequents Section

            // Bottom Buttons - Update Conditional
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            JButton updateConditional = new JButton(rbx.getString("UpdateConditionalButton"));
            panel5.add(updateConditional);
            updateConditional.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateConditionalPressed(e);
                }
            });
            updateConditional.setToolTipText(rbx.getString("UpdateConditionalButtonHint"));
            // Cancel
            JButton cancelConditional = new JButton(Bundle.getMessage("ButtonCancel"));
            panel5.add(cancelConditional);
            cancelConditional.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelConditionalPressed(e);
                }
            });
            cancelConditional.setToolTipText(rbx.getString("CancelConditionalButtonHint"));
            // Delete Conditional
            JButton deleteConditional = new JButton(Bundle.getMessage("ButtonDelete"));
            panel5.add(deleteConditional);
            deleteConditional.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteConditionalPressed(null);
                }
            });
            deleteConditional.setToolTipText(rbx.getString("DeleteConditionalButtonHint"));

            contentPane.add(panel5);
        }
        // setup window closing listener
        editConditionalFrame.addWindowListener(
                new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        cancelConditionalPressed(null);
                    }
                });
        // initialize state variable table
        _variableTableModel.fireTableDataChanged();
        // initialize action variables
        _actionTableModel.fireTableDataChanged();
        editConditionalFrame.pack();
        editConditionalFrame.setVisible(true);
        inEditConditionalMode = true;
        checkVariablePressed(null);     // update variables to their current states
    }   /* makeEditConditionalWindow */


    /**
     * Respond to the Add State Variable Button in the Edit Conditional window.
     *
     * @param e The event heard
     */
    void addVariablePressed(ActionEvent e) {
        if (alreadyEditingActionOrVariable()) {
            return;
        }
        if (LRouteTableAction.LOGIX_INITIALIZER.equals(_curLogix.getSystemName())) {
            javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                    rbx.getString("Error49"), Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        _showReminder = true;
        ConditionalVariable variable = new ConditionalVariable();
        _variableList.add(variable);
        _newItem = true;
        int size = _variableList.size();
        // default of operator for postion 0 (row 1) is Conditional.OPERATOR_NONE
        if (size > 1) {
            if (_logicType == Conditional.ALL_OR) {
                variable.setOpern(Conditional.OPERATOR_OR);
            } else {
                variable.setOpern(Conditional.OPERATOR_AND);
            }
        }
        size--;
        _variableTableModel.fireTableRowsInserted(size, size);
        makeEditVariableWindow(size);
        appendToAntecedent(variable);
    }

    /**
     * Respond to the Check State Variable Button in the Edit Conditional
     * window.
     *
     * @param e the event heard
     */
    void checkVariablePressed(ActionEvent e) {
        for (int i = 0; i < _variableList.size(); i++) {
            _variableList.get(i).evaluate();
        }
        _variableTableModel.fireTableDataChanged();
    }

    /**
     * Respond to the Negation column in the Edit Conditional window.
     *
     * @param row index of the Conditional to change the setting on
     * @param oper NOT (i18n) as negation of condition
     */
    void variableNegationChanged(int row, String oper) {
        ConditionalVariable variable = _variableList.get(row);
        boolean state = variable.isNegated();
        if (oper == null) {
            variable.setNegation(false);
        } else {
            variable.setNegation(oper.equals(Bundle.getMessage("LogicNOT")));
        }
        if (variable.isNegated() != state) {
            makeAntecedent();
        }
    }

    /**
     * Respond to the Operator column in the Edit Conditional window.
     *
     * @param row index of the Conditional to change the setting on
     * @param oper AND or OR (i18n) as operand on the list of conditions
     */
    void variableOperatorChanged(int row, String oper) {
        ConditionalVariable variable = _variableList.get(row);
        int oldOper = variable.getOpern();
        if (row > 0) {
            if (oper.equals(Bundle.getMessage("LogicOR"))) {
                variable.setOpern(Conditional.OPERATOR_OR);
            } else {
                variable.setOpern(Conditional.OPERATOR_AND);
            }
        } else {
            variable.setOpern(Conditional.OPERATOR_NONE);
        }
        if (variable.getOpern() != oldOper) {
            makeAntecedent();
        }
    }

    /**
     * Respond to Add action button in the EditConditional window.
     *
     * @param e The event heard
     */
    void addActionPressed(ActionEvent e) {
        if (alreadyEditingActionOrVariable()) {
            return;
        }
        _showReminder = true;
        _actionList.add(new DefaultConditionalAction());
        _newItem = true;
        _actionTableModel.fireTableRowsInserted(_actionList.size(),
                _actionList.size());
        makeEditActionWindow(_actionList.size() - 1);
    }

    /**
     * Respond to the Reorder Button in the Edit Conditional window.
     *
     * @param e The event heard
     */
    void reorderActionPressed(ActionEvent e) {
        if (alreadyEditingActionOrVariable()) {
            return;
        }
        _showReminder = true;
        // Check if reorder is reasonable
        if (_actionList.size() <= 1) {
            javax.swing.JOptionPane.showMessageDialog(editLogixFrame, rbx
                    .getString("Error46"), Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        _nextInOrder = 0;
        _inReorderMode = true;
        //status.setText(rbx.getString("ReorderMessage"));
        _actionTableModel.fireTableDataChanged();
    }

    /**
     * Respond to the First/Next (Delete) Button in the Edit Conditional window.
     *
     * @param row index of the row to put as next in line (instead of the one that was supposed to be next)
     */
    void swapActions(int row) {
        ConditionalAction temp = _actionList.get(row);
        for (int i = row; i > _nextInOrder; i--) {
            _actionList.set(i, _actionList.get(i - 1));
        }
        _actionList.set(_nextInOrder, temp);
        _nextInOrder++;
        if (_nextInOrder >= _actionList.size()) {
            _inReorderMode = false;
        }
        //status.setText("");
        _actionTableModel.fireTableDataChanged();
    }

    /**
     * Respond to the Update Conditional Button in the Edit Conditional window.
     *
     * @param e The event heard
     */
    void updateConditionalPressed(ActionEvent e) {
        if (alreadyEditingActionOrVariable()) {
            return;
        }
        // clean up empty variable and actions
        if (!LRouteTableAction.LOGIX_INITIALIZER.equals(_curLogix.getSystemName())) {
            for (int i = 0; i < _variableList.size(); i++) {
                if (_variableList.get(i).getType() == Conditional.TYPE_NONE) {
                    _variableList.remove(i);
                    _variableTableModel.fireTableRowsDeleted(i, i);
                }
            }
        }
        for (int i = 0; i < _actionList.size(); i++) {
            if (_actionList.get(i).getType() == Conditional.ACTION_NONE) {
                _actionList.remove(i);
                _actionTableModel.fireTableRowsDeleted(i, i);
            }
        }

        if (_variableList.size() <= 0 && _actionList.size() <= 0) {
            deleteConditionalPressed(null);
            return;
        }
        /*  if (_curConditional==null) {
         return;
         } */
        if (_curLogix.getSystemName().equals(SensorGroupFrame.logixSysName)) {
            javax.swing.JOptionPane.showMessageDialog(
                    editConditionalFrame, java.text.MessageFormat.format(rbx.getString("Warn8"),
                            new Object[]{SensorGroupFrame.logixUserName, SensorGroupFrame.logixSysName})
                    + java.text.MessageFormat.format(rbx.getString("Warn11"),
                            new Object[]{_curConditional.getUserName(), _curConditional.getSystemName()}),
                    Bundle.getMessage("WarningTitle"),
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            cancelConditionalPressed(null);
            return;
        }
        // Check if the User Name has been changed
        String uName = conditionalUserName.getText().trim(); // N11N
        if (!uName.equals(_curConditional.getUserName())) {
            // user name has changed - check if already in use
            if (!checkConditionalUserName(uName, _curLogix)) {
                return;
            }
            // user name is unique or blank, change it
            _curConditional.setUserName(uName);
            conditionalTableModel.fireTableDataChanged();
        }
        if (_variableList.size() <= 0 && !_suppressReminder) {
            javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
                    java.text.MessageFormat.format(rbx.getString("Warn5"),
                            new Object[]{_curConditional.getUserName(), _curConditional.getSystemName()}),
                    Bundle.getMessage("WarningTitle"),
                    javax.swing.JOptionPane.WARNING_MESSAGE);
        }

        if (!validateAntecedent()) {
            return;
        }
        // complete update
        _curConditional.setStateVariables(_variableList);
        _curConditional.setAction(_actionList);
        _curConditional.setLogicType(_logicType, _antecedent);
        _curConditional.setTriggerOnChange(_triggerOnChangeButton.isSelected());
        cancelConditionalPressed(null);
    }

    /**
     * Respond to the Cancel button in the Edit Conditional frame.
     * <p>
     * Does the cleanup from deleteConditionalPressed, updateConditionalPressed
     * and editConditionalFrame window closer.
     *
     * @param e The event heard
     */
    void cancelConditionalPressed(ActionEvent e) {
        if (_pickTables != null) {
            _pickTables.dispose();
            _pickTables = null;
        }
        if (_editActionFrame != null) {
            cleanUpAction();
        }
        if (_editVariableFrame != null) {
            cleanUpVariable();
        }
        try {
            _curLogix.activateLogix();
        } catch (NumberFormatException nfe) {
            if (log.isDebugEnabled()) {
                log.error("NumberFormatException on activation of Logix " + nfe);
            }
            //nfe.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
                    rbx.getString("Error4") + nfe.toString() + rbx.getString("Error7"),
                    Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        // when user uses the escape key and returns to editing, interaction with 
        // window closing event create strange environment
        inEditConditionalMode = false;
        if (editConditionalFrame != null) {
            //editConditionalFrame.setVisible(false);
            editConditionalFrame.dispose();
            editConditionalFrame = null;
        }
        if (editLogixFrame != null) {
            editLogixFrame.setVisible(true);
        }
    }

    /**
     * Respond to the Delete Conditional Button in the Edit Conditional window.
     *
     * @param sName system name of Conditional to be deleted
     */
    void deleteConditionalPressed(String sName) {
        if (_curConditional == null) {
            return;
        }
        // delete this Conditional - this is done by the parent Logix
        if (sName == null) {
            sName = _curConditional.getSystemName();
        }

        _showReminder = true;
        _curConditional = null;
        numConditionals--;
        String[] msgs = _curLogix.deleteConditional(sName);
        if (msgs != null) {
            javax.swing.JOptionPane.showMessageDialog(editLogixFrame,
                    java.text.MessageFormat.format(rbx.getString("Error11"), (Object[]) msgs),
                    Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        // complete deletion
        cancelConditionalPressed(null);
        conditionalTableModel.fireTableRowsDeleted(conditionalRowNumber,
                conditionalRowNumber);
        if (numConditionals < 1 && !_suppressReminder) {
            // warning message - last Conditional deleted
            javax.swing.JOptionPane.showMessageDialog(editLogixFrame, rbx
                    .getString("Warn1"), Bundle.getMessage("WarningTitle"),
                    javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Respond to a change of Conditional Type in the Edit Conditional pane
     * by showing/hiding the _antecedentPanel when Mixed is selected.
     *
     * @param e The event heard
     * @return false if there is no change in operator
     */
    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    boolean logicTypeChanged(ActionEvent e) {
        int type = _operatorBox.getSelectedIndex() + 1;
        if (type == _logicType) {
            return false;
        }
        makeAntecedent();
        int oper = Conditional.OPERATOR_OR;
        switch (type) {
            case Conditional.ALL_AND:
                oper = Conditional.OPERATOR_AND;
            // fall through intended here
            case Conditional.ALL_OR:
                for (int i = 1; i < _variableList.size(); i++) {
                    _variableList.get(i).setOpern(oper);
                }
                _antecedentPanel.setVisible(false);
                break;
            case Conditional.MIXED:
                _antecedentPanel.setVisible(true);
                break;
            default:
                break;
        }
        _logicType = type;
        _variableTableModel.fireTableDataChanged();
        editConditionalFrame.repaint();
        return true;
    }

    /**
     * Respond to Help button press in the Edit Conditional pane.
     *
     * @param e The event heard
     */
    void helpPressed(ActionEvent e) {
        javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                new String[]{
                    rbx.getString("LogicHelpText1"),
                    rbx.getString("LogicHelpText2"),
                    rbx.getString("LogicHelpText3"),
                    rbx.getString("LogicHelpText4"),
                    rbx.getString("LogicHelpText5"),
                    rbx.getString("LogicHelpText6"),
                    rbx.getString("LogicHelpText7")
                },
                Bundle.getMessage("MenuHelp"), javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Build the antecedent statement.
     */
    void makeAntecedent() {
        String str = "";
        if (_variableList.size() != 0) {
            String not = Bundle.getMessage("LogicNOT").toLowerCase();
            String row = "R"; //NOI18N
            String and = " " + Bundle.getMessage("LogicAND").toLowerCase() + " ";
            String or = " " + Bundle.getMessage("LogicOR").toLowerCase() + " ";
            if (_variableList.get(0).isNegated()) {
                str = not + " ";
            }
            str = str + row + "1";
            for (int i = 1; i < _variableList.size(); i++) {
                ConditionalVariable variable = _variableList.get(i);
                switch (variable.getOpern()) {
                    case Conditional.OPERATOR_AND:
                        str = str + and;
                        break;
                    case Conditional.OPERATOR_OR:
                        str = str + or;
                        break;
                    default:
                        break;
                }
                if (variable.isNegated()) {
                    str = str + not + " ";
                }
                str = str + row + (i + 1);
                if (i > 0 && i + 1 < _variableList.size()) {
                    str = "(" + str + ")";
                }
            }
        }
        _antecedent = str;
        _antecedentField.setText(_antecedent);
        _showReminder = true;
    }

    /**
     * Add a part to the antecedent statement.
     *
     * @param variable the current Conditional Variable, ignored in method
     */
    void appendToAntecedent(ConditionalVariable variable) {
        if (_variableList.size() > 1) {
            if (_logicType == Conditional.OPERATOR_OR) {
                _antecedent = _antecedent + " " + Bundle.getMessage("LogicOR").toLowerCase() + " ";
            } else {
                _antecedent = _antecedent + " " + Bundle.getMessage("LogicAND").toLowerCase() + " ";
            }
        }
        _antecedent = _antecedent + "R" + _variableList.size(); //NOI18N
        _antecedentField.setText(_antecedent);
    }

    /**
     * Check the antecedent and logic type.
     *
     * @return false if antecedent can't be validated
     */
    boolean validateAntecedent() {
        if (_logicType != Conditional.MIXED || LRouteTableAction.LOGIX_INITIALIZER.equals(_curLogix.getSystemName())) {
            return true;
        }
        _antecedent = _antecedentField.getText();
        if (_antecedent == null || _antecedent.trim().length() == 0) {
            makeAntecedent();
        }
        String message = _curConditional.validateAntecedent(_antecedent, _variableList);
        if (message != null) {
            javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                    message + rbx.getString("ParseError8"), Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    // *********************** Methods for Edit Variable Pane *******************

    /**
     * Check if an editing session on (another) Logix is going on.
     * <p>
     * If it is, display a message to user and bring current editing pane to front.
     *
     * @return true if an _editActionFrame or _editVariableFrame exists
     */
    boolean alreadyEditingActionOrVariable() {
        if (_selectionMode == SelectionMode.USEMULTI) {
            OpenPickListTable();
        }
        if (_editActionFrame != null) {
            // Already editing an Action, ask for completion of that edit
            javax.swing.JOptionPane.showMessageDialog(_editActionFrame,
                    rbx.getString("Error48"), Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            _editActionFrame.setVisible(true);
            return true;
        }
        if (_editVariableFrame != null) {
            // Already editing a state variable, ask for completion of that edit
            javax.swing.JOptionPane.showMessageDialog(_editVariableFrame,
                    rbx.getString("Error47"), Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            _editVariableFrame.setVisible(true);
            return true;
        }
        return false;
    }

    /**
     * Create and/or initialize the Edit a Variable pane.
     * <p>
     * Note: you can get here via the New Variable button (addVariablePressed)
     * or via an Edit button in the Variable table of the EditConditional window.
     *
     * @param row index of item to be edited in _variableList
     */
    void makeEditVariableWindow(int row) {
        if (alreadyEditingActionOrVariable()) {
            return;
        }
        _curVariableRowNumber = row;
        _curVariable = _variableList.get(row);
        _editVariableFrame = new JmriJFrame(rbx.getString("TitleEditVariable"), true, true);
        //        _editVariableFrame.setLocation(10, 100);
        JPanel topPanel = makeTopPanel(_editVariableFrame, "TitleAntecedentPhrase", 500, 160);

        Box panel1 = Box.createHorizontalBox();
        panel1.add(Box.createHorizontalGlue());
        panel1.add(Box.createHorizontalStrut(STRUT));
        // Item Type
        _variableTypeBox = new JComboBox<String>();
        for (int i = 0; i <= Conditional.ITEM_TYPE_LAST_STATE_VAR; i++) {
            _variableTypeBox.addItem(ConditionalVariable.getItemTypeString(i));
        }
        panel1.add(makeEditPanel(_variableTypeBox, "LabelVariableType", "VariableTypeHint"));
        panel1.add(Box.createHorizontalStrut(STRUT));
        // Item Name
        _variableNameField = new JTextField(30);
        _variableNamePanel = makeEditPanel(_variableNameField, "LabelItemName", null);
        _variableNamePanel.setMaximumSize(
                new Dimension(50, _variableNamePanel.getPreferredSize().height));
        _variableNamePanel.setVisible(false);
        panel1.add(_variableNamePanel);
        panel1.add(Box.createHorizontalStrut(STRUT));

        // Arbitrary name combo box to facilitate the panel construction
        _variableComboNamePanel = makeEditPanel(_sensorNameBox, "LabelItemName", null);
        _variableComboNamePanel.setVisible(false);
        panel1.add(_variableComboNamePanel);
        panel1.add(Box.createHorizontalStrut(STRUT));

        // State Box
        _variableStateBox = new JComboBox<String>();
        _variableStateBox.addItem("XXXXXXX");
        _variableStatePanel = makeEditPanel(_variableStateBox, "LabelVariableState", "VariableStateHint");
        _variableStatePanel.setVisible(false);
        panel1.add(_variableStatePanel);
        panel1.add(Box.createHorizontalStrut(STRUT));
        // Aspects
        _variableSignalBox = new JComboBox<String>();
        _variableSignalBox.addItem("XXXXXXXXX");
        _variableSignalPanel = makeEditPanel(_variableSignalBox, "LabelVariableAspect", "VariableAspectHint");
        _variableSignalPanel.setVisible(false);
        panel1.add(_variableSignalPanel);
        panel1.add(Box.createHorizontalStrut(STRUT));
        // Compare operator
        _variableComparePanel = new JPanel();
        _variableComparePanel.setLayout(new BoxLayout(_variableComparePanel, BoxLayout.X_AXIS));
        _variableCompareOpBox = new JComboBox<String>();
        for (int i = 1; i <= ConditionalVariable.NUM_COMPARE_OPERATIONS; i++) {
            _variableCompareOpBox.addItem(ConditionalVariable.getCompareOperationString(i));
        }
        _variableComparePanel.add(makeEditPanel(_variableCompareOpBox, "LabelCompareOp", "CompareHintMemory"));
        _variableComparePanel.add(Box.createHorizontalStrut(STRUT));
        // Compare type
        _variableCompareTypeBox = new JComboBox<String>();
        for (int i = 0; i < Conditional.ITEM_TO_MEMORY_TEST.length; i++) {
            _variableCompareTypeBox.addItem(ConditionalVariable.describeState(Conditional.ITEM_TO_MEMORY_TEST[i]));
        }
        _variableComparePanel.add(makeEditPanel(_variableCompareTypeBox, "LabelCompareType", "CompareTypeHint"));
        _variableComparePanel.setVisible(false);
        _variableCompareTypeBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                compareTypeChanged(_variableCompareTypeBox.getSelectedIndex());
                _editVariableFrame.pack();
            }
        });
        panel1.add(_variableComparePanel);
        panel1.add(Box.createHorizontalStrut(STRUT));
        // Data 1
        _variableData1Field = new JTextField(30);
        _variableData1Panel = makeEditPanel(_variableData1Field, "LabelStartTime", "DataHintTime");
        _variableData1Panel.setMaximumSize(
                new Dimension(45, _variableData1Panel.getPreferredSize().height));
        _variableData1Panel.setVisible(false);
        panel1.add(_variableData1Panel);
        panel1.add(Box.createHorizontalStrut(STRUT));
        // Data 2
        _variableData2Field = new JTextField(30);
        _variableData2Panel = makeEditPanel(_variableData2Field, "LabelEndTime", "DataHintTime");
        _variableData2Panel.setMaximumSize(
                new Dimension(45, _variableData2Panel.getPreferredSize().height));
        _variableData2Panel.setVisible(false);
        panel1.add(_variableData2Panel);
        panel1.add(Box.createHorizontalStrut(STRUT));
        panel1.add(Box.createHorizontalGlue());
        topPanel.add(panel1);

        ActionListener updateListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateVariablePressed();
            }
        };
        ActionListener cancelListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelEditVariablePressed();
            }
        };
        ActionListener deleteListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteVariablePressed();
            }
        };
        JPanel panel = makeButtonPanel(updateListener, cancelListener, deleteListener);
        topPanel.add(panel);
        topPanel.add(Box.createVerticalGlue());

        Container contentPane = _editVariableFrame.getContentPane();
        contentPane.add(topPanel);
        // note - this listener cannot be added before other action items
        // have been created
        _variableTypeBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                variableTypeChanged(_variableTypeBox.getSelectedIndex());
                _editVariableFrame.pack();
            }
        });
        // setup window closing listener
        _editVariableFrame.addWindowListener(
                new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        cancelEditVariablePressed();
                    }
                });
        initializeStateVariables();
        _editVariableFrame.pack();
        _editVariableFrame.setVisible(true);
    }

    // *************************** Edit Action Window and methods **********************

    /**
     * Create and/or initialize the Edit Action window.
     * <p>
     * Note: you can get here
     * via the New Action button (addActionPressed) or via an Edit button in the
     * Action table of the EditConditional window.
     *
     * @param row index in the table of the Action to be edited
     */
    void makeEditActionWindow(int row) {
        if (alreadyEditingActionOrVariable()) {
            return;
        }
        _curActionRowNumber = row;
        _curAction = _actionList.get(row);
        _editActionFrame = new JmriJFrame(rbx.getString("TitleEditAction"), true, true);
        //        _editActionFrame.setLocation(10, 300);
        JPanel topPanel = makeTopPanel(_editActionFrame, "TitleConsequentPhrase", 600, 160);

        Box panel1 = Box.createHorizontalBox();
        panel1.add(Box.createHorizontalGlue());

        _actionItemTypeBox = new JComboBox<String>();
        for (int i = 0; i <= Conditional.ITEM_TYPE_LAST_ACTION; i++) {
            _actionItemTypeBox.addItem(DefaultConditionalAction.getItemTypeString(i));
        }
        panel1.add(makeEditPanel(_actionItemTypeBox, "LabelActionItem", "ActionItemHint"));
        panel1.add(Box.createHorizontalStrut(STRUT));

        _actionNameField = new JTextField(30);
        _namePanel = makeEditPanel(_actionNameField, "LabelItemName", null);
        _namePanel.setMaximumSize(
                new Dimension(50, _namePanel.getPreferredSize().height));
        _namePanel.setVisible(false);
        panel1.add(_namePanel);
        panel1.add(Box.createHorizontalStrut(STRUT));

        // Arbitrary name combo box to facilitate the panel construction
        _actionComboNamePanel = makeEditPanel(_sensorNameBox, "LabelItemName", null);
        _actionComboNamePanel.setVisible(false);
        panel1.add(_actionComboNamePanel);
        panel1.add(Box.createHorizontalStrut(STRUT));

        _actionTypeBox = new JComboBox<String>();
        _actionTypeBox.addItem("");
        _actionTypePanel = makeEditPanel(_actionTypeBox, "LabelActionType", "ActionTypeHint");
        _actionTypePanel.setVisible(false);
        panel1.add(_actionTypePanel);
        panel1.add(Box.createHorizontalStrut(STRUT));

        _actionBox = new JComboBox<String>();
        _actionBox.addItem("");
        _actionPanel = makeEditPanel(_actionBox, "LabelActionType", "ActionTypeHint");
        _actionPanel.setVisible(false);
        panel1.add(_actionPanel);
        panel1.add(Box.createHorizontalStrut(STRUT));

        _shortActionString = new JTextField(15);
        _shortTextPanel = makeEditPanel(_shortActionString, "LabelActionText", null);
        _shortTextPanel.setMaximumSize(
                new Dimension(25, _shortTextPanel.getPreferredSize().height));
        _shortTextPanel.add(Box.createVerticalGlue());
        _shortTextPanel.setVisible(false);
        panel1.add(_shortTextPanel);
        panel1.add(Box.createHorizontalStrut(STRUT));

        _actionOptionBox = new JComboBox<String>();
        for (int i = 1; i <= Conditional.NUM_ACTION_OPTIONS; i++) {
            _actionOptionBox.addItem(DefaultConditionalAction.getOptionString(i, _triggerOnChangeButton.isSelected()));
        }
        _optionPanel = makeEditPanel(_actionOptionBox, "LabelActionOption", "ActionOptionHint");
        _optionPanel.setVisible(false);
        panel1.add(_optionPanel);
        panel1.add(Box.createHorizontalStrut(STRUT));

        panel1.add(Box.createHorizontalGlue());
        topPanel.add(panel1);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(Box.createVerticalGlue());

        Box panel2 = Box.createHorizontalBox();
        panel2.add(Box.createHorizontalGlue());

        _setPanel = new JPanel();
        _setPanel.setLayout(new BoxLayout(_setPanel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.add(new JLabel(rbx.getString("LabelActionFile")));
        _setPanel.add(p);
        _actionSetButton = new JButton("..."); // "File" replaced by ...
        _actionSetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateAction();
                setFileLocation(e);
            }
        });
        _actionSetButton.setMaximumSize(_actionSetButton.getPreferredSize());
        _setPanel.add(_actionSetButton);
        _actionSetButton.setToolTipText(rbx.getString("FileButtonHint"));
        _setPanel.add(Box.createVerticalGlue());
        _setPanel.setVisible(false);
        panel2.add(_setPanel);
        panel2.add(Box.createHorizontalStrut(5));

        _longActionString = new JTextField(50);
        _textPanel = makeEditPanel(_longActionString, "LabelActionText", null);
        _textPanel.setMaximumSize(
                new Dimension(80, _textPanel.getPreferredSize().height));
        _textPanel.add(Box.createVerticalGlue());
        _textPanel.setVisible(false);
        panel2.add(_textPanel);
        panel2.add(Box.createHorizontalGlue());
        topPanel.add(panel2);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(Box.createVerticalGlue());

        ActionListener updateListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateActionPressed();
            }
        };
        ActionListener cancelListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelEditActionPressed();
            }
        };
        ActionListener deleteListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteActionPressed();
            }
        };
        JPanel panel = makeButtonPanel(updateListener, cancelListener, deleteListener);
        topPanel.add(panel);
        topPanel.add(Box.createVerticalGlue());

        Container contentPane = _editActionFrame.getContentPane();
        contentPane.add(topPanel);
        // note - this listener cannot be added until all items are entered into _actionItemTypeBox 
        _actionItemTypeBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int select = _actionItemTypeBox.getSelectedIndex();
                if (log.isDebugEnabled()) {
                    log.debug("_actionItemTypeBoxListener: select= " + select);
                }
                actionItemChanged(select);
                _editActionFrame.pack();
            }
        });
        // setup window closing listener
        _editActionFrame.addWindowListener(
                new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        cancelEditActionPressed();
                    }
                });
        actionItemChanged(Conditional.TYPE_NONE);
        initializeActionVariables();
        _editActionFrame.setVisible(true);
        _editActionFrame.pack();
    }

    // ***** Methods shared by Edit Variable and Edit Action Windows *********

    /**
     * Create Variable and Action editing pane top part.
     *
     * @param frame JFrame to add to
     * @param title property key for border title
     * @param width fixed dimension to use
     * @param height fixed dimension to use
     * @return JPanel containing interface
     */
    JPanel makeTopPanel(JFrame frame, String title, int width, int height) {
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
        contentPane.add(Box.createRigidArea(new Dimension(0, height)));
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        Border panelBorder = BorderFactory.createEtchedBorder();
        Border panelTitled = BorderFactory.createTitledBorder(panelBorder, rbx.getString(title));
        topPanel.setBorder(panelTitled);
        topPanel.add(Box.createRigidArea(new Dimension(width, 0)));
        topPanel.add(Box.createVerticalGlue());
        return topPanel;
    }

    /**
     * Create Variable and Action editing pane center part.
     *
     * @param comp Field or comboBox to include on sub pane
     * @param label property key for label
     * @param hint property key for tooltip for this sub pane
     * @return JPanel containing interface
     */
    JPanel makeEditPanel(JComponent comp, String label, String hint) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.add(new JLabel(rbx.getString(label)));
        panel.add(p);
        if (hint != null) {
            panel.setToolTipText(rbx.getString(hint));
        }
        comp.setMaximumSize(comp.getPreferredSize());  // override for text fields
        panel.add(comp);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    /**
     * Create Variable and Action editing pane bottom part.
     * <p>
     * Called from {@link #makeEditVariableWindow(int)}
     * @param updateListener listener for Update pressed
     * @param cancelListener listener for Cancel pressed
     * @param deleteListener listener for Delete pressed
     * @return JPanel containing Update etc. buttons
     */
    JPanel makeButtonPanel(ActionListener updateListener,
            ActionListener cancelListener,
            ActionListener deleteListener) {
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS));

        JButton cancelAction = new JButton(Bundle.getMessage("ButtonCancel"));
        panel3.add(cancelAction);
        panel3.add(Box.createHorizontalStrut(STRUT));
        cancelAction.addActionListener(cancelListener);
        cancelAction.setToolTipText(rbx.getString("CancelButtonHint"));

        JButton updateAction = new JButton(Bundle.getMessage("ButtonUpdate"));
        panel3.add(updateAction);
        panel3.add(Box.createHorizontalStrut(STRUT));
        updateAction.addActionListener(updateListener);
        updateAction.setToolTipText(rbx.getString("UpdateButtonHint"));

        JButton deleteAction = new JButton(Bundle.getMessage("ButtonDelete"));
        panel3.add(deleteAction);
        deleteAction.addActionListener(deleteListener);
        deleteAction.setToolTipText(rbx.getString("DeleteButtonHint"));
        return panel3;
    }

    /**
     * Close a single panel picklist JFrame and related items.
     * @since 4.7.3
     */
    void closeSinglePanelPickList() {
        if (_pickSingleFrame != null) {
            _pickSingleFrame.setVisible(false);
            _pickSingleFrame.dispose();
            _pickSingleFrame = null;
            _pickListener = null;
            _pickTable = null;
            _pickSingle = null;
        }
    }
    
    /**
     * Create a single panel picklist JFrame for choosing action and variable names.
     * <p>
     * Called from {@link #actionItemChanged} and {@link #variableTypeChanged}
     * @since 4.7.3
     * @param itemType The selected variable or action type
     * @param isVariable True if called by variableTypeChanged
     */
    void createSinglePanelPickList(int itemType, boolean isVariable) {
        if (_pickListener != null) {
            int saveType = _pickListener.getItemType();
            if (saveType != itemType) {
                // The type has changed, need to start over
                closeSinglePanelPickList();
            } else {
                // The pick list has already been created
                return;
            }
        }

        switch (itemType) {
            case Conditional.ITEM_TYPE_SENSOR:      // 1
                _pickSingle = new PickSinglePanel(PickListModel.sensorPickModelInstance());
                break;
            case Conditional.ITEM_TYPE_TURNOUT:     // 2
                _pickSingle = new PickSinglePanel(PickListModel.turnoutPickModelInstance());
                break;
            case Conditional.ITEM_TYPE_LIGHT:       // 3
                _pickSingle = new PickSinglePanel(PickListModel.lightPickModelInstance());
                break;
            case Conditional.ITEM_TYPE_SIGNALHEAD:  // 4
                _pickSingle = new PickSinglePanel(PickListModel.signalHeadPickModelInstance());
                break;
            case Conditional.ITEM_TYPE_SIGNALMAST:  // 5
                _pickSingle = new PickSinglePanel(PickListModel.signalMastPickModelInstance());
                break;
            case Conditional.ITEM_TYPE_MEMORY:      // 6
                _pickSingle = new PickSinglePanel(PickListModel.memoryPickModelInstance());
                break;
            case Conditional.ITEM_TYPE_WARRANT:     // 8
                _pickSingle = new PickSinglePanel(PickListModel.warrantPickModelInstance());
                break;
            case Conditional.ITEM_TYPE_OBLOCK:      // 10
                _pickSingle = new PickSinglePanel(PickListModel.oBlockPickModelInstance());
                break;
            case Conditional.ITEM_TYPE_ENTRYEXIT:   // 11
                _pickSingle = new PickSinglePanel(PickListModel.entryExitPickModelInstance());
                break;
//            case Conditional.ITEM_TYPE_BLOCK:     // Future ??
//                _pickSingle = new PickSinglePanel(PickListModel.blockPickModelInstance());
//                break;
            default:
                if (itemType == Conditional.ITEM_TYPE_CONDITIONAL && isVariable) {
                    _pickSingle = new PickSinglePanel(PickListModel.conditionalPickModelInstance());
                    break;      // Conditional (type 7) Variable, no pick list for Action
                }
                return;             // Skip any other items.
        }

        // Create the JFrame
        _pickSingleFrame = new JmriJFrame(rbx.getString("SinglePickFrame"));
        _pickSingleFrame.setContentPane(_pickSingle);
        _pickSingleFrame.pack();
        _pickSingleFrame.setVisible(true);
        _pickSingleFrame.toFront();

        // Set the table selection listener
        _pickListener = new PickSingleListener();
        _pickTable = _pickSingle.getTable();
        _pickTable.getSelectionModel().addListSelectionListener(_pickListener);
        
        // Save the current item type
        _pickListener.setItemType(itemType);
    }

    /**
     * For each name combo box, enable first item blank and add the item change listener
     * @since 4.7.3
     */
    void setNameBoxListeners() {
        if (_nameBoxListenersDone) {
            return;
        }

        _sensorNameBox.setFirstItemBlank(true);
        _turnoutNameBox.setFirstItemBlank(true);
        _lightNameBox.setFirstItemBlank(true);
        _sigheadNameBox.setFirstItemBlank(true);
        _sigmastNameBox.setFirstItemBlank(true);
        _memoryNameBox.setFirstItemBlank(true);
        _warrantNameBox.setFirstItemBlank(true);
        _oblockNameBox.setFirstItemBlank(true);
        _conditionalNameBox.setFirstItemBlank(true);
        _logixNameBox.setFirstItemBlank(true);
//        _entryexitNameBox.setFirstItemBlank(true);

        _sensorNameListener = new NameBoxListener();
        _sensorNameListener.setNameType("sensor");
        _sensorNameBox.addItemListener(_sensorNameListener);

        _turnoutNameListener = new NameBoxListener();
        _turnoutNameListener.setNameType("turnout");
        _turnoutNameBox.addItemListener(_turnoutNameListener);

        _lightNameListener = new NameBoxListener();
        _lightNameListener.setNameType("light");
        _lightNameBox.addItemListener(_lightNameListener);

        _sigheadNameListener = new NameBoxListener();
        _sigheadNameListener.setNameType("signalhead");
        _sigheadNameBox.addItemListener(_sigheadNameListener);

        _sigmastNameListener = new NameBoxListener();
        _sigmastNameListener.setNameType("signalmast");
        _sigmastNameBox.addItemListener(_sigmastNameListener);

        _memoryNameListener = new NameBoxListener();
        _memoryNameListener.setNameType("memory");
        _memoryNameBox.addItemListener(_memoryNameListener);

        _warrantNameListener = new NameBoxListener();
        _warrantNameListener.setNameType("warrant");
        _warrantNameBox.addItemListener(_warrantNameListener);

        _oblockNameListener = new NameBoxListener();
        _oblockNameListener.setNameType("oblock");
        _oblockNameBox.addItemListener(_oblockNameListener);

        _conditionalNameListener = new NameBoxListener();
        _conditionalNameListener.setNameType("conditional");
        _conditionalNameBox.addItemListener(_conditionalNameListener);

        _logixNameListener = new NameBoxListener();
        _logixNameListener.setNameType("logix");
        _logixNameBox.addItemListener(_logixNameListener);

//        _entryexitNameListener = new NameBoxListener();
//        _entryexitNameListener.setNameType("entryexit");
//        _entryexitNameBox.addItemListener(_entryexitNameListener);
        
        _nameBoxListenersDone = true;

    }

    // *********** Responses for Edit Action and Edit Variable Buttons **********

    /**
     * Respond to Update Action button in the Edit Action pane.
     */
    void updateActionPressed() {
        if (!validateAction()) {
            _editActionFrame.toFront();
            return;
        }
        _actionTableModel.fireTableRowsUpdated(_curActionRowNumber, _curActionRowNumber);
        cleanUpAction();
        if (editConditionalFrame != null) {
            editConditionalFrame.setVisible(true);
        }
    }

    /**
     * Respond to Update Variable button in the Edit Action pane.
     */
    void updateVariablePressed() {
        if (!validateVariable()) {
            _editVariableFrame.toFront();
            return;
        }
        _variableTableModel.fireTableRowsUpdated(_curVariableRowNumber, _curVariableRowNumber);
        cleanUpVariable();
        if (editConditionalFrame != null) {
            editConditionalFrame.setVisible(true);
        }
    }

    /**
     * Respond to Cancel action button and window closer of the
     * Edit Action window.
     * <p>
     * Also does cleanup of Update and Delete buttons.
     */
    void cancelEditActionPressed() {
        if (_newItem) {
            deleteActionPressed(_curActionRowNumber);
        } else {
            cleanUpAction();
        }
        if (editConditionalFrame != null) {
            editConditionalFrame.setVisible(true);
        }
    }

    /**
     * Clean up Update and Delete Action buttons.
     */
    void cleanUpAction() {
        _newItem = false;
        if (_editActionFrame != null) {
            _actionTypeBox.removeActionListener(_actionTypeListener);
            _editActionFrame.setVisible(false);
            _editActionFrame.dispose();
            _editActionFrame = null;
            closeSinglePanelPickList();
        }
        _curActionRowNumber = -1;
    }

    /**
     * Respond to Cancel action button and window closer of the
     * Edit Variable pane.
     * <p>Also does cleanup of Update and Delete Variable buttons.
     */
    void cancelEditVariablePressed() {
        if (_newItem) {
            deleteVariablePressed(_curVariableRowNumber);
        } else {
            cleanUpVariable();
        }
        if (editConditionalFrame != null) {
            editConditionalFrame.setVisible(true);
        }
    }

    /**
     * Clean up Update and Delete Variable buttons.
     */
    void cleanUpVariable() {
        _newItem = false;
        if (_editVariableFrame != null) {
            _editVariableFrame.setVisible(false);
            _editVariableFrame.dispose();
            _editVariableFrame = null;
            closeSinglePanelPickList();
        }
        _curVariableRowNumber = -1;
    }

    /**
     * Respond to Delete action button in the Edit Action window.
     */
    void deleteActionPressed() {
        deleteActionPressed(_curActionRowNumber);
    }

    /**
     * Respond to Delete action button in an action row of the
     * Edit Conditional pane.
     *
     * @param row index in table of action to be deleted
     */
    void deleteActionPressed(int row) {
        if (row != _curActionRowNumber && alreadyEditingActionOrVariable()) {
            return;
        }
        _actionList.remove(row);
        _actionTableModel.fireTableRowsDeleted(row, row);
        cleanUpAction();
        if (editConditionalFrame != null) {
            editConditionalFrame.setVisible(true);
        }
        _showReminder = true;
    }

    /**
     * Respond to Delete action button in the Edit Variable window.
     */
    void deleteVariablePressed() {
        deleteVariablePressed(_curVariableRowNumber);
    }

    /**
     * Respond to the Delete Button in the State Variable Table of the Edit
     * Conditional window.
     *
     * @param row index in table of variable to be deleted
     */
    void deleteVariablePressed(int row) {
        if (row != _curVariableRowNumber && alreadyEditingActionOrVariable()) {
            return;
        }
        if (_variableList.size() < 1 && !_suppressReminder) {
            // warning message - last State Variable deleted
            javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                    rbx.getString("Warn3"), Bundle.getMessage("WarningTitle"),
                    javax.swing.JOptionPane.WARNING_MESSAGE);
        }
        // move remaining state variables if needed
        _variableList.remove(row);
        _variableTableModel.fireTableRowsDeleted(row, row);
        makeAntecedent();
        cleanUpVariable();
        if (editConditionalFrame != null) {
            editConditionalFrame.setVisible(true);
        }
        _showReminder = true;
    }

    /**
     * Set display to show current state variable (curVariable) parameters.
     */
    void initializeStateVariables() {
        int testType = _curVariable.getType();
        if (log.isDebugEnabled()) {
            log.debug("initializeStateVariables: testType= " + testType);
        }
        if (testType == Conditional.TYPE_NONE) {
            return;
        }
        int itemType = Conditional.TEST_TO_ITEM[testType];
        if (log.isDebugEnabled()) {
            log.debug("initializeStateVariables: itemType= " + itemType + ", testType= " + testType);
        }
        // set type after call to variableTypeChanged - addItemListener action will call variableTypeChanged
        _variableTypeBox.setSelectedIndex(itemType);
        //variableTypeChanged(itemType);
        switch (itemType) {
            case Conditional.ITEM_TYPE_SENSOR:
                _variableStateBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_SENSOR_TEST, testType));
                _variableNameField.setText(_curVariable.getName());
                break;
            case Conditional.ITEM_TYPE_TURNOUT:
                _variableStateBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_TURNOUT_TEST, testType));
                _variableNameField.setText(_curVariable.getName());
                break;
            case Conditional.ITEM_TYPE_LIGHT:
                _variableStateBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_LIGHT_TEST, testType));
                _variableNameField.setText(_curVariable.getName());
                break;

            case Conditional.ITEM_TYPE_SIGNALHEAD:
                _variableStateBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_SIGNAL_HEAD_TEST, testType));
                _variableNameField.setText(_curVariable.getName());
                if ((Conditional.TYPE_SIGNAL_HEAD_RED <= testType && testType <= Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN)
                        || Conditional.TYPE_SIGNAL_HEAD_LUNAR == testType
                        || Conditional.TYPE_SIGNAL_HEAD_FLASHLUNAR == testType) {
                    _variableStateBox.setSelectedItem( // index 1 = TYPE_SIGNAL_HEAD_APPEARANCE_EQUALS
                            ConditionalVariable.describeState(Conditional.ITEM_TO_SIGNAL_HEAD_TEST[1]));
                    loadJComboBoxWithHeadAppearances(_variableSignalBox, _curVariable.getName());
                    _variableSignalBox.setSelectedItem(
                            ConditionalVariable.describeState(_curVariable.getType()));
                    _variableSignalPanel.setVisible(true);
                }
                break;

            case Conditional.ITEM_TYPE_SIGNALMAST:
                // set display to show current state variable (curVariable) parameters
                _variableStateBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_SIGNAL_MAST_TEST, testType));
                _variableNameField.setText(_curVariable.getName());
                if (testType == Conditional.TYPE_SIGNAL_MAST_ASPECT_EQUALS) {
                    loadJComboBoxWithMastAspects(_variableSignalBox, _curVariable.getName());
                    _variableSignalBox.setSelectedItem(_curVariable.getDataString());
                    _variableSignalPanel.setVisible(true);
                }
                break;

            case Conditional.ITEM_TYPE_MEMORY:
                _variableCompareTypeBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_MEMORY_TEST, testType));
                _variableNameField.setText(_curVariable.getName());
                int num1 = _curVariable.getNum1() - 1;
                if (num1 == -1) {  // former code was only equals
                    num1 = ConditionalVariable.EQUAL - 1;
                }
                _variableCompareOpBox.setSelectedIndex(num1);
                _variableData1Field.setText(_curVariable.getDataString());
                break;
            case Conditional.ITEM_TYPE_CONDITIONAL:
                _variableStateBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_CONDITIONAL_TEST, testType));
                _variableNameField.setText(_curVariable.getName());
                break;
            case Conditional.ITEM_TYPE_WARRANT:
                _variableStateBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_WARRANT_TEST, testType));
                _variableNameField.setText(_curVariable.getName());
                break;
            case Conditional.ITEM_TYPE_CLOCK:
                int time = _curVariable.getNum1();
                _variableData1Field.setText(formatTime(time / 60, time - ((time / 60) * 60)));
                time = _curVariable.getNum2();
                _variableData2Field.setText(formatTime(time / 60, time - ((time / 60) * 60)));
                _variableNameField.setText("");
                break;

            case Conditional.ITEM_TYPE_OBLOCK:
                _variableNameField.setText(_curVariable.getName());
                //_variableStateBox.removeAllItems();
                Iterator<String> names = OBlock.getLocalStatusNames();
                while (names.hasNext()) {
                    _variableStateBox.addItem(names.next());
                }
                _variableStateBox.setSelectedItem(OBlock.getLocalStatusName(_curVariable.getDataString()));
                _variableStateBox.setVisible(true);
                break;
            case Conditional.ITEM_TYPE_ENTRYEXIT:
                _variableNameField.setText(_curVariable.getBean().getUserName());
                _variableStateBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_ENTRYEXIT_TEST, testType));
                _variableStateBox.setVisible(true);
                break;
            default:
                break;
        }
        _editVariableFrame.pack();
        _editVariableFrame.transferFocusBackward();
    }

    /*
     String getConditionalUserName(String name) {
     Conditional c = _conditionalManager.getBySystemName(name);
     if (c != null) {
     return c.getUserName();
     }
     return name;
     }

     /**
     * Set display to show current action (curAction) parameters.
     */
    void initializeActionVariables() {
        int actionType = _curAction.getType();
        int itemType = Conditional.ACTION_TO_ITEM[actionType];
        if (log.isDebugEnabled()) {
            log.debug("initializeActionVariables: itemType= " + itemType + ", actionType= " + actionType);
        }
        if (actionType == Conditional.ACTION_NONE) {
            return;
        }
        _actionItemTypeBox.setSelectedIndex(itemType);
        _actionNameField.setText(_curAction.getDeviceName());
        switch (itemType) {
            case Conditional.ITEM_TYPE_SENSOR:
                _actionTypeBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_SENSOR_ACTION, actionType) + 1);
                if ((actionType == Conditional.ACTION_RESET_DELAYED_SENSOR)
                        || (actionType == Conditional.ACTION_DELAYED_SENSOR)) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                if (actionType == Conditional.ACTION_SET_SENSOR
                        || actionType == Conditional.ACTION_DELAYED_SENSOR
                        || actionType == Conditional.ACTION_RESET_DELAYED_SENSOR) {
                    if (_curAction.getActionData() == Sensor.ACTIVE) {
                        _actionBox.setSelectedIndex(0);
                    } else if (_curAction.getActionData() == Sensor.INACTIVE) {
                        _actionBox.setSelectedIndex(1);
                    } else if (_curAction.getActionData() == Route.TOGGLE) {
                        _actionBox.setSelectedIndex(2);
                    }
                }
                break;
            case Conditional.ITEM_TYPE_TURNOUT:
                _actionTypeBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_TURNOUT_ACTION, actionType) + 1);
                if ((actionType == Conditional.ACTION_RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.ACTION_DELAYED_TURNOUT)) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                if ((actionType == Conditional.ACTION_SET_TURNOUT)
                        || (actionType == Conditional.ACTION_RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.ACTION_DELAYED_TURNOUT)) {
                    if (_curAction.getActionData() == Turnout.CLOSED) {
                        _actionBox.setSelectedIndex(0);
                    } else if (_curAction.getActionData() == Turnout.THROWN) {
                        _actionBox.setSelectedIndex(1);
                    } else if (_curAction.getActionData() == Route.TOGGLE) {
                        _actionBox.setSelectedIndex(2);
                    }
                } else if (actionType == Conditional.ACTION_LOCK_TURNOUT) {
                    if (_curAction.getActionData() == Turnout.UNLOCKED) {
                        _actionBox.setSelectedIndex(0);
                    } else if (_curAction.getActionData() == Turnout.LOCKED) {
                        _actionBox.setSelectedIndex(1);
                    } else if (_curAction.getActionData() == Route.TOGGLE) {
                        _actionBox.setSelectedIndex(2);
                    }
                }
                break;
            case Conditional.ITEM_TYPE_LIGHT:
                _actionTypeBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_LIGHT_ACTION, actionType) + 1);
                if (actionType == Conditional.ACTION_SET_LIGHT) {
                    if (_curAction.getActionData() == Light.ON) {
                        _actionBox.setSelectedIndex(0);
                    } else if (_curAction.getActionData() == Light.OFF) {
                        _actionBox.setSelectedIndex(1);
                    } else if (_curAction.getActionData() == Route.TOGGLE) {
                        _actionBox.setSelectedIndex(2);
                    }
                } else if ((actionType == Conditional.ACTION_SET_LIGHT_INTENSITY)
                        || (actionType == Conditional.ACTION_SET_LIGHT_TRANSITION_TIME)) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                break;
            case Conditional.ITEM_TYPE_SIGNALHEAD:
                /*
                 _actionTypeBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                 Conditional.ITEM_TO_SIGNAL_HEAD_ACTION, actionType)+1);
                 if (actionType==Conditional.ACTION_SET_SIGNAL_APPEARANCE) {
                 _actionBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                 AbstractSignalHead.getDefaultValidStates(), _curAction.getActionData()));
                 }
                 */
                _actionTypeBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_SIGNAL_HEAD_ACTION, actionType) + 1);
                if (actionType == Conditional.ACTION_SET_SIGNAL_APPEARANCE) {
                    loadJComboBoxWithHeadAppearances(_actionBox, _actionNameField.getText().trim());
                }
                break;
            case Conditional.ITEM_TYPE_SIGNALMAST:
                _actionTypeBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_SIGNAL_MAST_ACTION, actionType) + 1);
                break;
            case Conditional.ITEM_TYPE_CLOCK:
                _actionTypeBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_CLOCK_ACTION, actionType) + 1);
                if (actionType == Conditional.ACTION_SET_FAST_CLOCK_TIME) {
                    int time = _curAction.getActionData();
                    _longActionString.setText(formatTime(time / 60, time - ((time / 60) * 60)));
                    _actionNameField.setText("");
                }
                break;
            case Conditional.ITEM_TYPE_MEMORY:
                _actionTypeBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_MEMORY_ACTION, actionType) + 1);
                _shortActionString.setText(_curAction.getActionString());
                break;
            case Conditional.ITEM_TYPE_LOGIX:
                _actionTypeBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_LOGIX_ACTION, actionType) + 1);
                break;
            case Conditional.ITEM_TYPE_WARRANT:
                _actionTypeBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_WARRANT_ACTION, actionType) + 1);
                if (actionType == Conditional.ACTION_CONTROL_TRAIN) {
                    if (_curAction.getActionData() == Warrant.HALT) {
                        _actionBox.setSelectedIndex(0);
                    } else if (_curAction.getActionData() == Warrant.RESUME) {
                        _actionBox.setSelectedIndex(1);
                    } else if (_curAction.getActionData() == Warrant.ABORT) {
                        _actionBox.setSelectedIndex(2);
                    }
                } else if (actionType == Conditional.ACTION_SET_TRAIN_ID
                        || actionType == Conditional.ACTION_SET_TRAIN_NAME
                        || actionType == Conditional.ACTION_THROTTLE_FACTOR) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                break;
            case Conditional.ITEM_TYPE_OBLOCK:
                _actionTypeBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_OBLOCK_ACTION, actionType) + 1);
                if (actionType == Conditional.ACTION_SET_BLOCK_VALUE) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                break;
            case Conditional.ITEM_TYPE_AUDIO:
                _actionTypeBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_AUDIO_ACTION, actionType) + 1);
                if (actionType == Conditional.ACTION_PLAY_SOUND) {
                    _longActionString.setText(_curAction.getActionString());
                } else if (actionType == Conditional.ACTION_CONTROL_AUDIO) {
                    switch (_curAction.getActionData()) {
                        case Audio.CMD_PLAY:
                            _actionBox.setSelectedIndex(0);
                            break;
                        case Audio.CMD_STOP:
                            _actionBox.setSelectedIndex(1);
                            break;
                        case Audio.CMD_PLAY_TOGGLE:
                            _actionBox.setSelectedIndex(2);
                            break;
                        case Audio.CMD_PAUSE:
                            _actionBox.setSelectedIndex(3);
                            break;
                        case Audio.CMD_RESUME:
                            _actionBox.setSelectedIndex(4);
                            break;
                        case Audio.CMD_PAUSE_TOGGLE:
                            _actionBox.setSelectedIndex(5);
                            break;
                        case Audio.CMD_REWIND:
                            _actionBox.setSelectedIndex(6);
                            break;
                        case Audio.CMD_FADE_IN:
                            _actionBox.setSelectedIndex(7);
                            break;
                        case Audio.CMD_FADE_OUT:
                            _actionBox.setSelectedIndex(8);
                            break;
                        case Audio.CMD_RESET_POSITION:
                            _actionBox.setSelectedIndex(9);
                            break;
                        default:
                            log.warn("Unexpected _curAction.getActionData() of {}", _curAction.getActionData());
                            break;
                    }
                }
                break;
            case Conditional.ITEM_TYPE_SCRIPT:
                _actionTypeBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_SCRIPT_ACTION, actionType) + 1);
                if (actionType == Conditional.ACTION_RUN_SCRIPT) {
                    _longActionString.setText(_curAction.getActionString());
                } else if (actionType == Conditional.ACTION_JYTHON_COMMAND) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                break;
            case Conditional.ITEM_TYPE_OTHER:
                _actionTypeBox.setSelectedIndex(DefaultConditional.getIndexInTable(
                        Conditional.ITEM_TO_OTHER_ACTION, actionType) + 1);
                // ACTION_TRIGGER_ROUTE
                break;
        }
        _actionOptionBox.setSelectedIndex(_curAction.getOption() - 1);
        _editActionFrame.pack();
        _editActionFrame.transferFocusBackward();
    }   /* initializeActionVariables */


    JFileChooser sndFileChooser = null;
    JFileChooser scriptFileChooser = null;
    JFileChooser defaultFileChooser = null;

    /**
     * Respond to the [...] button in the Edit Action window action section.
     * <p>
     * Ask user to select an audio or python script file on disk.
     *
     * @param e the event heard
     */
    void setFileLocation(ActionEvent e) {
        ConditionalAction action = _actionList.get(_curActionRowNumber);
        JFileChooser currentChooser;
        int actionType = action.getType();
        if (actionType == Conditional.ACTION_PLAY_SOUND) {
            if (sndFileChooser == null) {
                sndFileChooser = new JFileChooser(System.getProperty("user.dir")
                        + java.io.File.separator + "resources"
                        + java.io.File.separator + "sounds");
                jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("wav sound files");
                filt.addExtension("wav");
                sndFileChooser.setFileFilter(filt);
            }
            currentChooser = sndFileChooser;
        } else if (actionType == Conditional.ACTION_RUN_SCRIPT) {
            if (scriptFileChooser == null) {
                scriptFileChooser = new JFileChooser(FileUtil.getScriptsPath());
                jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("Python script files");
                filt.addExtension("py");
                scriptFileChooser.setFileFilter(filt);
            }
            currentChooser = scriptFileChooser;
        } else {
            log.warn("Unexpected actionType[" + actionType + "] = " + DefaultConditionalAction.getActionTypeString(actionType));
            if (defaultFileChooser == null) {
                defaultFileChooser = new JFileChooser(FileUtil.getUserFilesPath());
                defaultFileChooser.setFileFilter(new jmri.util.NoArchiveFileFilter());
            }
            currentChooser = defaultFileChooser;
        }

        currentChooser.rescanCurrentDirectory();
        int retVal = currentChooser.showOpenDialog(null);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            // set selected file location in data string
            try {
                _longActionString.setText(FileUtil.getPortableFilename(currentChooser.getSelectedFile().getCanonicalPath()));
            } catch (java.io.IOException ex) {
                if (log.isDebugEnabled()) {
                    log.error("exception setting file location: " + ex);
                }
                _longActionString.setText("");
            }
        }
    }

    /**
     * Respond to a change in an Action Type comboBox on the Edit Conditional Action pane.
     * <p>
     * Set components visible for the selected type.
     *
     * @param type index of the newly selected Action type
     */
    void actionItemChanged(int type) {
        int actionType = _curAction.getType();
        if (log.isDebugEnabled()) {
            log.debug("actionItemChanged: itemType= " + type + ", actionType= " + actionType);
        }
        _actionTypeBox.removeActionListener(_actionTypeListener);
        _actionTypePanel.setVisible(false);
        _setPanel.setVisible(false);
        _shortTextPanel.setVisible(false);
        _shortActionString.setText("");
        _textPanel.setVisible(false);
        _longActionString.setText("");
        _namePanel.setVisible(false);
        _actionPanel.setVisible(false);
        _optionPanel.setVisible(false);
        _actionComboNamePanel.setVisible(false);
        int itemType = Conditional.ACTION_TO_ITEM[actionType];
        if (type == Conditional.TYPE_NONE && itemType == Conditional.TYPE_NONE) {
            return;
        }
        _actionTypePanel.setVisible(true);
        _actionTypeBox.removeAllItems();
        _actionBox.removeAllItems();
        if (type != Conditional.TYPE_NONE) {  // actionItem listener choice overrides current item 
            itemType = type;
        }
        if (itemType != Conditional.ACTION_TO_ITEM[actionType]) {
            actionType = Conditional.ACTION_NONE;    // chosen item type does not support action type       
        }
        if (actionType != Conditional.ACTION_NONE) {
            _optionPanel.setVisible(true);    // item type compatible with action type
        }
        _actionTypeBox.addItem("");
        _actionNameField.removeActionListener(actionSignalHeadNameListener);
        _actionNameField.removeActionListener(actionSignalMastNameListener);

        if (_selectionMode == SelectionMode.USESINGLE) {
            createSinglePanelPickList(itemType, false);
        }

        switch (itemType) {
            case Conditional.ITEM_TYPE_TURNOUT:
                for (int i = 0; i < Conditional.ITEM_TO_TURNOUT_ACTION.length; i++) {
                    _actionTypeBox.addItem(
                            DefaultConditionalAction.getActionTypeString(Conditional.ITEM_TO_TURNOUT_ACTION[i]));
                }
                if ((actionType == Conditional.ACTION_RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.ACTION_DELAYED_TURNOUT)) {
                    JPanel p = (JPanel) _shortTextPanel.getComponent(0);
                    JLabel l = (JLabel) p.getComponent(0);
                    l.setText(rbx.getString("LabelDelayTime"));
                    _shortTextPanel.setToolTipText(rbx.getString("DataHintDelayedTurnout"));
                    _shortTextPanel.setVisible(true);
                }
                JPanel panel = (JPanel) _actionPanel.getComponent(0);
                JLabel label = (JLabel) panel.getComponent(0);
                if ((actionType == Conditional.ACTION_SET_TURNOUT)
                        || (actionType == Conditional.ACTION_RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.ACTION_DELAYED_TURNOUT)) {
                    label.setText(rbx.getString("LabelActionTurnout"));
                    _actionBox.addItem(Bundle.getMessage("TurnoutStateClosed"));
                    _actionBox.addItem(Bundle.getMessage("TurnoutStateThrown"));
                    _actionBox.addItem(Bundle.getMessage("Toggle"));
                    _actionPanel.setToolTipText(rbx.getString("TurnoutSetHint"));
                    _actionPanel.setVisible(true);
                } else if (actionType == Conditional.ACTION_LOCK_TURNOUT) {
                    label.setText(rbx.getString("LabelActionLock"));
                    _actionBox.addItem(rbx.getString("TurnoutUnlock"));
                    _actionBox.addItem(rbx.getString("TurnoutLock"));
                    _actionBox.addItem(Bundle.getMessage("Toggle"));
                    _actionPanel.setToolTipText(rbx.getString("LockSetHint"));
                    _actionPanel.setVisible(true);
                }
                _namePanel.setToolTipText(rbx.getString("NameHintTurnout"));
                _namePanel.setVisible(true);

                // Set and swap name boxes
                setActionNameBox(_turnoutNameBox, _turnoutNameListener);
                break;
            case Conditional.ITEM_TYPE_SENSOR:
                for (int i = 0; i < Conditional.ITEM_TO_SENSOR_ACTION.length; i++) {
                    _actionTypeBox.addItem(
                            DefaultConditionalAction.getActionTypeString(Conditional.ITEM_TO_SENSOR_ACTION[i]));
                }
                if ((actionType == Conditional.ACTION_RESET_DELAYED_SENSOR)
                        || (actionType == Conditional.ACTION_DELAYED_SENSOR)) {
                    JPanel p = (JPanel) _shortTextPanel.getComponent(0);
                    JLabel l = (JLabel) p.getComponent(0);
                    l.setText(rbx.getString("LabelDelayTime"));
                    _shortTextPanel.setToolTipText(rbx.getString("DataHintDelayedTurnout"));
                    _shortTextPanel.setVisible(true);
                }
                if ((actionType == Conditional.ACTION_SET_SENSOR)
                        || (actionType == Conditional.ACTION_RESET_DELAYED_SENSOR)
                        || (actionType == Conditional.ACTION_DELAYED_SENSOR)) {
                    JPanel p = (JPanel) _actionPanel.getComponent(0);
                    JLabel l = (JLabel) p.getComponent(0);
                    l.setText(rbx.getString("LabelActionSensor"));
                    _actionBox.addItem(Bundle.getMessage("SensorStateActive"));
                    _actionBox.addItem(Bundle.getMessage("SensorStateInactive"));
                    _actionBox.addItem(Bundle.getMessage("Toggle"));
                    _actionPanel.setToolTipText(rbx.getString("SensorSetHint"));
                    _actionPanel.setVisible(true);
                }
                _namePanel.setToolTipText(rbx.getString("NameHintSensor"));
                _namePanel.setVisible(true);

                // Set and swap name boxes
                setActionNameBox(_sensorNameBox, _sensorNameListener);
                break;
            case Conditional.ITEM_TYPE_SIGNALHEAD:
                _actionNameField.addActionListener(actionSignalHeadNameListener);

                for (int i = 0; i < Conditional.ITEM_TO_SIGNAL_HEAD_ACTION.length; i++) {
                    _actionTypeBox.addItem(
                            DefaultConditionalAction.getActionTypeString(Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[i]));
                }
                if (actionType == Conditional.ACTION_SET_SIGNAL_APPEARANCE) {
                    JPanel p = (JPanel) _actionPanel.getComponent(0);
                    JLabel l = (JLabel) p.getComponent(0);
                    l.setText(rbx.getString("LabelActionSignal"));

                    loadJComboBoxWithHeadAppearances(_actionBox, _actionNameField.getText().trim());

                    _actionPanel.setToolTipText(rbx.getString("SignalSetHint"));
                    _actionPanel.setVisible(true);
                }
                _namePanel.setToolTipText(rbx.getString("NameHintSignal"));
                _namePanel.setVisible(true);

                // Set and swap name boxes
                setActionNameBox(_sigheadNameBox, _sigheadNameListener);
                break;
            case Conditional.ITEM_TYPE_SIGNALMAST:
                _actionNameField.addActionListener(actionSignalMastNameListener);

                for (int i = 0; i < Conditional.ITEM_TO_SIGNAL_MAST_ACTION.length; i++) {
                    _actionTypeBox.addItem(
                            DefaultConditionalAction.getActionTypeString(Conditional.ITEM_TO_SIGNAL_MAST_ACTION[i]));
                }
                if (actionType == Conditional.ACTION_SET_SIGNALMAST_ASPECT) {
                    JPanel p = (JPanel) _actionPanel.getComponent(0);
                    JLabel l = (JLabel) p.getComponent(0);
                    l.setText(rbx.getString("LabelSignalAspect"));

                    loadJComboBoxWithMastAspects(_actionBox, _actionNameField.getText().trim());

                    _actionPanel.setToolTipText(rbx.getString("SignalMastSetHint"));
                    _actionPanel.setVisible(true);
                }
                _namePanel.setToolTipText(rbx.getString("NameHintSignalMast"));
                _namePanel.setVisible(true);

                // Set and swap name boxes
                setActionNameBox(_sigmastNameBox, _sigmastNameListener);
                break;
            case Conditional.ITEM_TYPE_LIGHT:
                for (int i = 0; i < Conditional.ITEM_TO_LIGHT_ACTION.length; i++) {
                    _actionTypeBox.addItem(
                            DefaultConditionalAction.getActionTypeString(Conditional.ITEM_TO_LIGHT_ACTION[i]));
                }
                if (actionType == Conditional.ACTION_SET_LIGHT_INTENSITY) {
                    JPanel p = (JPanel) _shortTextPanel.getComponent(0);
                    JLabel l = (JLabel) p.getComponent(0);
                    l.setText(rbx.getString("LabelLightIntensity"));
                    _shortTextPanel.setToolTipText(rbx.getString("DataHintLightIntensity"));
                    _shortTextPanel.setVisible(true);
                } else if (actionType == Conditional.ACTION_SET_LIGHT_TRANSITION_TIME) {
                    JPanel p = (JPanel) _shortTextPanel.getComponent(0);
                    JLabel l = (JLabel) p.getComponent(0);
                    l.setText(rbx.getString("LabelTransitionTime"));
                    _shortTextPanel.setToolTipText(rbx.getString("DataHintLightTransitionTime"));
                    _shortTextPanel.setVisible(true);
                } else if (actionType == Conditional.ACTION_SET_LIGHT) {
                    JPanel p = (JPanel) _actionPanel.getComponent(0);
                    JLabel l = (JLabel) p.getComponent(0);
                    l.setText(rbx.getString("LabelActionLight"));
                    _actionBox.addItem(rbx.getString("LightOn"));
                    _actionBox.addItem(rbx.getString("LightOff"));
                    _actionBox.addItem(Bundle.getMessage("Toggle"));
                    _actionPanel.setToolTipText(rbx.getString("LightSetHint"));
                    _actionPanel.setVisible(true);
                }
                _namePanel.setToolTipText(rbx.getString("NameHintLight"));
                _namePanel.setVisible(true);

                // Set and swap name boxes
                setActionNameBox(_lightNameBox, _lightNameListener);
                break;
            case Conditional.ITEM_TYPE_MEMORY:
                for (int i = 0; i < Conditional.ITEM_TO_MEMORY_ACTION.length; i++) {
                    _actionTypeBox.addItem(
                            DefaultConditionalAction.getActionTypeString(Conditional.ITEM_TO_MEMORY_ACTION[i]));
                }
                JPanel p = (JPanel) _shortTextPanel.getComponent(0);
                JLabel l = (JLabel) p.getComponent(0);
                if (actionType == Conditional.ACTION_COPY_MEMORY) {
                    _shortTextPanel.setToolTipText(rbx.getString("DataHintToMemory"));
                    l.setText(rbx.getString("LabelMemoryLocation"));
                } else {
                    _shortTextPanel.setToolTipText(rbx.getString("DataHintMemory"));
                    l.setText(rbx.getString("LabelValue"));
                }
                _shortTextPanel.setVisible(true);
                _namePanel.setToolTipText(rbx.getString("NameHintMemory"));
                _namePanel.setVisible(true);

                // Set and swap name boxes
                setActionNameBox(_memoryNameBox, _memoryNameListener);
                break;
            case Conditional.ITEM_TYPE_CLOCK:
                for (int i = 0; i < Conditional.ITEM_TO_CLOCK_ACTION.length; i++) {
                    _actionTypeBox.addItem(
                            DefaultConditionalAction.getActionTypeString(Conditional.ITEM_TO_CLOCK_ACTION[i]));
                }
                if (actionType == Conditional.ACTION_SET_FAST_CLOCK_TIME) {
                    p = (JPanel) _textPanel.getComponent(0);
                    l = (JLabel) p.getComponent(0);
                    l.setText(rbx.getString("LabelSetTime"));
                    _textPanel.setToolTipText(rbx.getString("DataHintTime"));
                    _textPanel.setVisible(true);
                }
                break;
            case Conditional.ITEM_TYPE_LOGIX:
                for (int i = 0; i < Conditional.ITEM_TO_LOGIX_ACTION.length; i++) {
                    _actionTypeBox.addItem(
                            DefaultConditionalAction.getActionTypeString(Conditional.ITEM_TO_LOGIX_ACTION[i]));
                }
                _namePanel.setToolTipText(rbx.getString("NameHintLogix"));
                _namePanel.setVisible(true);

                // Set and swap name boxes
                setActionNameBox(_logixNameBox, _logixNameListener);
                break;
            case Conditional.ITEM_TYPE_WARRANT:
                for (int i = 0; i < Conditional.ITEM_TO_WARRANT_ACTION.length; i++) {
                    _actionTypeBox.addItem(
                            DefaultConditionalAction.getActionTypeString(Conditional.ITEM_TO_WARRANT_ACTION[i]));
                }
                _namePanel.setToolTipText(rbx.getString("NameHintWarrant"));
                _namePanel.setVisible(true);
                if (actionType == Conditional.ACTION_CONTROL_TRAIN) {
                    p = (JPanel) _actionPanel.getComponent(0);
                    l = (JLabel) p.getComponent(0);
                    _actionBox.addItem(rbx.getString("WarrantHalt"));
                    _actionBox.addItem(rbx.getString("WarrantResume"));
                    _actionBox.addItem(rbx.getString("WarrantAbort"));
                    l.setText(rbx.getString("LabelControlTrain"));
                    _actionPanel.setVisible(true);
                } else if (actionType == Conditional.ACTION_SET_TRAIN_ID
                        || actionType == Conditional.ACTION_SET_TRAIN_NAME
                        || actionType == Conditional.ACTION_THROTTLE_FACTOR) {
                    p = (JPanel) _shortTextPanel.getComponent(0);
                    l = (JLabel) p.getComponent(0);
                    if (actionType == Conditional.ACTION_SET_TRAIN_ID) {
                        _shortTextPanel.setToolTipText(rbx.getString("DataHintTrainId"));
                        l.setText(rbx.getString("LabelTrainId"));
                    } else if (actionType == Conditional.ACTION_SET_TRAIN_NAME) {
                        _shortTextPanel.setToolTipText(rbx.getString("DataHintTrainName"));
                        l.setText(rbx.getString("LabelTrainName"));
                    } else { // must be Conditional.ACTION_THROTTLE_FACTOR, so treat as such
                        _shortTextPanel.setToolTipText(rbx.getString("DataHintThrottleFactor"));
                        l.setText(rbx.getString("LabelThrottleFactor"));
                    }
                    _shortTextPanel.setVisible(true);
                }

                // Set and swap name boxes
                setActionNameBox(_warrantNameBox, _warrantNameListener);
                break;
            case Conditional.ITEM_TYPE_OBLOCK:
                for (int i = 0; i < Conditional.ITEM_TO_OBLOCK_ACTION.length; i++) {
                    _actionTypeBox.addItem(
                            DefaultConditionalAction.getActionTypeString(Conditional.ITEM_TO_OBLOCK_ACTION[i]));
                }
                _namePanel.setToolTipText(rbx.getString("NameHintOBlock"));
                _namePanel.setVisible(true);
                if (actionType == Conditional.ACTION_SET_BLOCK_VALUE) {
                    p = (JPanel) _shortTextPanel.getComponent(0);
                    l = (JLabel) p.getComponent(0);
                    _shortTextPanel.setToolTipText(rbx.getString("DataHintBlockValue"));
                    l.setText(rbx.getString("LabelBlockValue"));
                    _shortTextPanel.setVisible(true);
                }

                // Set and swap name boxes
                setActionNameBox(_oblockNameBox, _oblockNameListener);
                break;
            case Conditional.ITEM_TYPE_AUDIO:
                for (int i = 0; i < Conditional.ITEM_TO_AUDIO_ACTION.length; i++) {
                    _actionTypeBox.addItem(
                            DefaultConditionalAction.getActionTypeString(Conditional.ITEM_TO_AUDIO_ACTION[i]));
                }
                if (actionType == Conditional.ACTION_PLAY_SOUND) {
                    p = (JPanel) _textPanel.getComponent(0);
                    l = (JLabel) p.getComponent(0);
                    l.setText(rbx.getString("LabelSetFile"));
                    _textPanel.setToolTipText(rbx.getString("SetHintSound"));
                    _textPanel.setVisible(true);
                    _setPanel.setVisible(true);
                } else if (actionType == Conditional.ACTION_CONTROL_AUDIO) {
                    p = (JPanel) _actionPanel.getComponent(0);
                    l = (JLabel) p.getComponent(0);
                    l.setText(rbx.getString("LabelActionAudio"));
                    _actionBox.addItem(rbx.getString("AudioSourcePlay"));
                    _actionBox.addItem(rbx.getString("AudioSourceStop"));
                    _actionBox.addItem(rbx.getString("AudioSourcePlayToggle"));
                    _actionBox.addItem(rbx.getString("AudioSourcePause"));
                    _actionBox.addItem(rbx.getString("AudioSourceResume"));
                    _actionBox.addItem(rbx.getString("AudioSourcePauseToggle"));
                    _actionBox.addItem(rbx.getString("AudioSourceRewind"));
                    _actionBox.addItem(rbx.getString("AudioSourceFadeIn"));
                    _actionBox.addItem(rbx.getString("AudioSourceFadeOut"));
                    _actionBox.addItem(rbx.getString("AudioResetPosition"));
                    _actionPanel.setToolTipText(rbx.getString("SetHintAudio"));
                    _actionPanel.setVisible(true);
                    _namePanel.setToolTipText(rbx.getString("NameHintAudio"));
                    _namePanel.setVisible(true);
                }
                break;
            case Conditional.ITEM_TYPE_SCRIPT:
                for (int i = 0; i < Conditional.ITEM_TO_SCRIPT_ACTION.length; i++) {
                    _actionTypeBox.addItem(
                            DefaultConditionalAction.getActionTypeString(Conditional.ITEM_TO_SCRIPT_ACTION[i]));
                }
                if (actionType == Conditional.ACTION_RUN_SCRIPT) {
                    p = (JPanel) _textPanel.getComponent(0);
                    l = (JLabel) p.getComponent(0);
                    l.setText(rbx.getString("LabelSetFile"));
                    _textPanel.setToolTipText(rbx.getString("SetHintScript"));
                    _textPanel.setVisible(true);
                    _setPanel.setVisible(true);
                } else if (actionType == Conditional.ACTION_JYTHON_COMMAND) {
                    p = (JPanel) _shortTextPanel.getComponent(0);
                    l = (JLabel) p.getComponent(0);
                    l.setText(rbx.getString("LabelScriptCommand"));
                    _shortTextPanel.setToolTipText(rbx.getString("SetHintJythonCmd"));
                    _shortTextPanel.setVisible(true);
                }
                break;
            case Conditional.ITEM_TYPE_OTHER:
                for (int i = 0; i < Conditional.ITEM_TO_OTHER_ACTION.length; i++) {
                    _actionTypeBox.addItem(
                            DefaultConditionalAction.getActionTypeString(Conditional.ITEM_TO_OTHER_ACTION[i]));
                }
                if (actionType == Conditional.ACTION_TRIGGER_ROUTE) {
                    _namePanel.setToolTipText(rbx.getString("NameHintRoute"));
                    _namePanel.setVisible(true);
                }
                break;
            default:
                break;
        }
        _actionTypeBox.setMaximumSize(_actionTypeBox.getPreferredSize());
        _actionBox.setMaximumSize(_actionBox.getPreferredSize());
        _actionTypeListener.setItemType(itemType);
        _actionTypeBox.addActionListener(_actionTypeListener);
        if (log.isDebugEnabled()) {
            log.debug("Exit actionItemChanged size: " + _editActionFrame.getWidth()
                    + " X " + _editActionFrame.getHeight());
        }
    }

    /**
     * Update the name combo box selection based on the current contents of the name field.
     * Swap the name panel contents and set visible if needed
     * Called by actionItemChanged
     *
     * @since 4.7.3
     * @param nameBox The name box to be updated.
     * @param nameBoxListener The name box listener:  setProgramEvent() to disable the redundant field update since this is a result of a field update.
     */
    void setActionNameBox (JmriBeanComboBox nameBox, NameBoxListener nameBoxListener) {
        if (_selectionMode != SelectionMode.USECOMBO) {
            return;
        }
        
        // Select the current entry
        String dispName = nameBox.getSelectedDisplayName();
        String newName = _curAction.getDeviceName();
        if (newName != null && !(newName.equals(dispName))) {
            nameBoxListener.setProgramEvent();
            nameBox.setSelectedBeanByName(newName);
        }

        // Swap the name boxes
        _actionComboNamePanel.remove(1);
        _actionComboNamePanel.add(nameBox, null, 1);
        _namePanel.setVisible(false);
        _actionComboNamePanel.setVisible(true);
    }

    /**
     * Check if Memory type in a Conditional was changed by the user.
     * <p>
     * Update GUI if it has. Called from {@link #makeEditVariableWindow(int)}
     *
     * @param selection index of the currently selected type in the _variableCompareTypeBox
     */
    private void compareTypeChanged(int selection) {
        JPanel p = (JPanel) _variableData1Panel.getComponent(0);
        JLabel l = (JLabel) p.getComponent(0);
        int testType = Conditional.ITEM_TO_MEMORY_TEST[selection];
        if ((testType == Conditional.TYPE_MEMORY_COMPARE)
                || (testType == Conditional.TYPE_MEMORY_COMPARE_INSENSITIVE)) {
            l.setText(rbx.getString("LabelMemoryValue"));
            _variableData1Panel.setToolTipText(rbx.getString("DataHintMemory"));
        } else {
            l.setText(rbx.getString("LabelLiteralValue"));
            _variableData1Panel.setToolTipText(rbx.getString("DataHintValue"));
        }
    }

    transient ActionListener variableSignalTestStateListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.debug("variableSignalTestStateListener fires; _variableTypeBox.getSelectedIndex()= "
                    + _variableTypeBox.getSelectedIndex()
                    + "\" _variableStateBox.getSelectedIndex()= \"" + _variableStateBox.getSelectedIndex() + "\"");

            int itemType = _variableTypeBox.getSelectedIndex();
            
            if (_variableStateBox.getSelectedIndex() == 1) {
                if (itemType == Conditional.ITEM_TYPE_SIGNALHEAD) {
                    loadJComboBoxWithHeadAppearances(_variableSignalBox, _variableNameField.getText().trim());
                    _variableSignalPanel.setVisible(true);
                } else if (itemType == Conditional.ITEM_TYPE_SIGNALMAST) {
                    loadJComboBoxWithMastAspects(_variableSignalBox, _variableNameField.getText().trim());
                    _variableSignalPanel.setVisible(true);
                } else {
                    _variableSignalPanel.setVisible(false);
                }
            } else {
                _variableSignalPanel.setVisible(false);
            }
            
            _variableSignalBox.setMaximumSize(_variableSignalBox.getPreferredSize());
            if (_editVariableFrame != null) {
                _editVariableFrame.pack();
            }
        }
    };

    transient ActionListener variableSignalHeadNameListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // fired when signal mast name changes, but only
            // while in signal mast mode
            log.debug("variableSignalHeadNameListener fires; _variableNameField : " + _variableNameField.getText().trim());
            loadJComboBoxWithHeadAppearances(_variableSignalBox, _variableNameField.getText().trim());
        }
    };

    transient ActionListener actionSignalHeadNameListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // fired when signal mast name changes, but only
            // while in signal mast mode
            log.debug("actionSignalHeadNameListener fires; _actionNameField : " + _actionNameField.getText().trim());
            loadJComboBoxWithHeadAppearances(_actionBox, _actionNameField.getText().trim());
        }
    };

    /**
     * Fetch valid appearances for a given Signal Head.
     * <p>
     * Warn if head is not found.
     *
     * @param box the comboBox on the setup pane to fill
     * @param signalHeadName user or system name of the Signal Head
     */
    void loadJComboBoxWithHeadAppearances(JComboBox<String> box, String signalHeadName) {
        box.removeAllItems();
        log.debug("loadJComboBoxWithSignalHeadAppearances called with name: " + signalHeadName);
        SignalHead h = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHeadName);
        if (h == null) {
            box.addItem(rbx.getString("PromptLoadHeadName"));
        } else {
            String[] v = h.getValidStateNames();
            for (int i = 0; i < v.length; i++) {
                box.addItem(v[i]);
            }
            box.setSelectedItem(h.getAppearanceName());
        }
    }

    transient ActionListener variableSignalMastNameListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // fired when signal mast name changes, but only
            // while in signal mast mode
            log.debug("variableSignalMastNameListener fires; _variableNameField : " + _variableNameField.getText().trim());
            loadJComboBoxWithMastAspects(_variableSignalBox, _variableNameField.getText().trim());
        }
    };

    transient ActionListener actionSignalMastNameListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // fired when signal mast name changes, but only
            // while in signal mast mode
            log.debug("actionSignalMastNameListener fires; _actionNameField : " + _actionNameField.getText().trim());
            loadJComboBoxWithMastAspects(_actionBox, _actionNameField.getText().trim());
        }
    };

    /**
     * Fetch valid aspects for a given Signal Mast.
     * <p>
     * Warn if mast is not found.
     *
     * @param box the comboBox on the setup pane to fill
     * @param mastName user or system name of the Signal Mast
     */
    void loadJComboBoxWithMastAspects(JComboBox<String> box, String mastName) {
        box.removeAllItems();
        log.debug("loadJComboBoxWithMastAspects called with name: " + mastName);
        SignalMast m = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(mastName);
        if (m == null) {
            box.addItem(rbx.getString("PromptLoadMastName"));
        } else {
            java.util.Vector<String> v = m.getValidAspects();
            for (int i = 0; i < v.size(); i++) {
                box.addItem(v.get(i));
            }
            box.setSelectedItem(m.getAspect());
        }
    }

    /**
     * Respond to change in variable type chosen in the State Variable Table
     * in the Edit Conditional pane.
     * <p>
     * Also used to set up for Edit of a Conditional with state variables.
     *
     * @param itemType value representing the newly selected Conditional type, i.e. ITEM_TYPE_SENSOR
     */
    private void variableTypeChanged(int itemType) {
        int testType = _curVariable.getType();
        if (log.isDebugEnabled()) {
            log.debug("variableTypeChanged: itemType= " + itemType + ", testType= " + testType);
        }
        _variableNamePanel.setVisible(false);
        _variableStatePanel.setVisible(false);
        _variableComparePanel.setVisible(false);
        _variableSignalPanel.setVisible(false);
        _variableData1Panel.setVisible(false);
        _variableData2Panel.setVisible(false);
        _variableComboNamePanel.setVisible(false);
        _variableStateBox.removeAllItems();
        _variableNameField.removeActionListener(variableSignalHeadNameListener);
        _variableNameField.removeActionListener(variableSignalMastNameListener);
        _variableStateBox.removeActionListener(variableSignalTestStateListener);

        if (_selectionMode == SelectionMode.USESINGLE) {
            createSinglePanelPickList(itemType, true);
        }

        switch (itemType) {
            case Conditional.TYPE_NONE:
                return;
            case Conditional.ITEM_TYPE_SENSOR:
                _variableNamePanel.setToolTipText(rbx.getString("NameHintSensor"));
                for (int i = 0; i < Conditional.ITEM_TO_SENSOR_TEST.length; i++) {
                    _variableStateBox.addItem(
                            ConditionalVariable.describeState(Conditional.ITEM_TO_SENSOR_TEST[i]));
                }
                _variableStatePanel.setVisible(true);
                _variableNamePanel.setVisible(true);

                // Set and swap name boxes
                setVariableNameBox(_sensorNameBox, _sensorNameListener);
                break;
            case Conditional.ITEM_TYPE_TURNOUT:
                _variableNamePanel.setToolTipText(rbx.getString("NameHintTurnout"));
                for (int i = 0; i < Conditional.ITEM_TO_LIGHT_TEST.length; i++) {
                    _variableStateBox.addItem(
                            ConditionalVariable.describeState(Conditional.ITEM_TO_TURNOUT_TEST[i]));
                }
                _variableNamePanel.setVisible(true);
                _variableStatePanel.setVisible(true);

                // Set and swap name boxes
                setVariableNameBox(_turnoutNameBox, _turnoutNameListener);
                break;
            case Conditional.ITEM_TYPE_LIGHT:
                _variableNamePanel.setToolTipText(rbx.getString("NameHintLight"));
                for (int i = 0; i < Conditional.ITEM_TO_LIGHT_TEST.length; i++) {
                    _variableStateBox.addItem(
                            ConditionalVariable.describeState(Conditional.ITEM_TO_LIGHT_TEST[i]));
                }
                _variableStatePanel.setVisible(true);
                _variableNamePanel.setVisible(true);

                // Set and swap name boxes
                setVariableNameBox(_lightNameBox, _lightNameListener);
                break;
            case Conditional.ITEM_TYPE_SIGNALHEAD:
                _variableNameField.addActionListener(variableSignalHeadNameListener);
                _variableStateBox.addActionListener(variableSignalTestStateListener);
                loadJComboBoxWithHeadAppearances(_variableSignalBox, _variableNameField.getText().trim());

                for (int i = 0; i < Conditional.ITEM_TO_SIGNAL_HEAD_TEST.length; i++) {
                    _variableStateBox.addItem(
                            ConditionalVariable.describeState(Conditional.ITEM_TO_SIGNAL_HEAD_TEST[i]));
                }
                _variableNamePanel.setToolTipText(rbx.getString("NameHintSignal"));
                _variableNamePanel.setVisible(true);
                _variableStatePanel.setVisible(true);
                if (testType == Conditional.TYPE_SIGNAL_HEAD_APPEARANCE_EQUALS) {
                    _variableSignalPanel.setVisible(true);
                } else {
                    _variableSignalPanel.setVisible(false);
                }

                // Set and swap name boxes
                setVariableNameBox(_sigheadNameBox, _sigheadNameListener);
                break;
            case Conditional.ITEM_TYPE_SIGNALMAST:
                _variableNameField.addActionListener(variableSignalMastNameListener);
                _variableStateBox.addActionListener(variableSignalTestStateListener);
                loadJComboBoxWithMastAspects(_variableSignalBox, _variableNameField.getText().trim());

                for (int i = 0; i < Conditional.ITEM_TO_SIGNAL_MAST_TEST.length; i++) {
                    _variableStateBox.addItem(
                            ConditionalVariable.describeState(Conditional.ITEM_TO_SIGNAL_MAST_TEST[i]));
                }
                _variableNamePanel.setToolTipText(rbx.getString("NameHintSignalMast"));
                _variableNamePanel.setVisible(true);
                _variableStatePanel.setVisible(true);
                if (testType == Conditional.TYPE_SIGNAL_MAST_ASPECT_EQUALS) {
                    _variableSignalPanel.setVisible(true);
                } else {
                    _variableSignalPanel.setVisible(false);
                }

                // Set and swap name boxes
                setVariableNameBox(_sigmastNameBox, _sigmastNameListener);
                break;
            case Conditional.ITEM_TYPE_MEMORY:
                JPanel p = (JPanel) _variableData1Panel.getComponent(0);
                JLabel l = (JLabel) p.getComponent(0);
                if ((testType == Conditional.TYPE_MEMORY_COMPARE)
                        || (testType == Conditional.TYPE_MEMORY_COMPARE_INSENSITIVE)) {
                    l.setText(rbx.getString("LabelMemoryValue"));
                    _variableData1Panel.setToolTipText(rbx.getString("DataHintMemory"));
                } else {
                    l.setText(rbx.getString("LabelLiteralValue"));
                    _variableData1Panel.setToolTipText(rbx.getString("DataHintValue"));
                }
                _variableNamePanel.setToolTipText(rbx.getString("NameHintMemory"));
                _variableNamePanel.setVisible(true);
                _variableData1Panel.setToolTipText(rbx.getString("DataHintMemory"));
                _variableData1Panel.setVisible(true);
                _variableComparePanel.setVisible(true);

                // Set and swap name boxes
                setVariableNameBox(_memoryNameBox, _memoryNameListener);
                break;
            case Conditional.ITEM_TYPE_CONDITIONAL:
                _variableNamePanel.setToolTipText(rbx.getString("NameHintConditional"));
                for (int i = 0; i < Conditional.ITEM_TO_CONDITIONAL_TEST.length; i++) {
                    _variableStateBox.addItem(
                            ConditionalVariable.describeState(Conditional.ITEM_TO_CONDITIONAL_TEST[i]));
                }
                _variableNamePanel.setVisible(true);
                _variableStatePanel.setVisible(true);

                // Set and swap name boxes
                setVariableNameBox(_conditionalNameBox, _conditionalNameListener);
                break;
            case Conditional.ITEM_TYPE_WARRANT:
                _variableNamePanel.setToolTipText(rbx.getString("NameHintWarrant"));
                for (int i = 0; i < Conditional.ITEM_TO_WARRANT_TEST.length; i++) {
                    _variableStateBox.addItem(
                            ConditionalVariable.describeState(Conditional.ITEM_TO_WARRANT_TEST[i]));
                }
                _variableNamePanel.setVisible(true);
                _variableStatePanel.setVisible(true);

                // Set and swap name boxes
                setVariableNameBox(_warrantNameBox, _warrantNameListener);
                break;
            case Conditional.ITEM_TYPE_CLOCK:
                p = (JPanel) _variableData1Panel.getComponent(0);
                l = (JLabel) p.getComponent(0);
                l.setText(rbx.getString("LabelStartTime"));
                _variableData1Panel.setToolTipText(rbx.getString("DataHintTime"));
                _variableData1Panel.setVisible(true);
                _variableData2Panel.setVisible(true);
                break;
            case Conditional.ITEM_TYPE_OBLOCK:
                _variableNamePanel.setToolTipText(rbx.getString("NameHintOBlock"));
                _variableNamePanel.setVisible(true);
                _variableStateBox.removeAllItems();
                Iterator<String> names = OBlock.getLocalStatusNames();
                while (names.hasNext()) {
                    _variableStateBox.addItem(names.next());
                }
                _variableStatePanel.setVisible(true);

                // Set and swap name boxes
                setVariableNameBox(_oblockNameBox, _oblockNameListener);
                break;
            case Conditional.ITEM_TYPE_ENTRYEXIT:
                _variableNameField.setText(_curVariable.getName());
                for (int i = 0; i < Conditional.ITEM_TO_ENTRYEXIT_TEST.length; i++) {
                    _variableStateBox.addItem(
                            ConditionalVariable.describeState(Conditional.ITEM_TO_ENTRYEXIT_TEST[i]));
                }
                _variableStatePanel.setVisible(true);
                _variableNamePanel.setVisible(true);
                break;
            default:
                break;
        }
        _variableStateBox.setMaximumSize(_variableStateBox.getPreferredSize());
    }

    /**
     * Update the name combo box selection based on the current contents of the name field.
     * Swap the name panel contents and set visible if needed
     * Called by variableItemChanged
     * @since 4.7.3
     * @param nameBox The name box to be updated.
     * @param nameBoxListener The name box listener:  setProgramEvent() to disable the redundant field update since this is a result of a field update.
     */
    void setVariableNameBox (JmriBeanComboBox nameBox, NameBoxListener nameBoxListener) {
        if (_selectionMode != SelectionMode.USECOMBO) {
            return;
        }
        
        // Select the current entry
        String dispName = nameBox.getSelectedDisplayName();
        String newName = _curVariable.getName();
        if (newName != null && !(newName.equals(dispName))) {
            nameBoxListener.setProgramEvent();
            nameBox.setSelectedBeanByName(newName);
        }

        // Swap the name boxes
        _variableComboNamePanel.remove(1);
        _variableComboNamePanel.add(nameBox, null, 1);
        _variableNamePanel.setVisible(false);
        _variableComboNamePanel.setVisible(true);
    }

    /**
     * Validate Variable data from Edit Variable Window, and transfer it to
     * current action object as appropriate.
     * <p>
     * Messages are sent to the user for any errors found. This routine returns
     * false immediately after finding the first error, even if there might be more
     * errors.
     *
     * @return true if all data checks out OK, otherwise false
     */
    boolean validateVariable() {
        String name = _variableNameField.getText().trim();
        _variableNameField.setText(name);
        _curVariable.setDataString("");
        _curVariable.setNum1(0);
        _curVariable.setNum2(0);
        int itemType = _variableTypeBox.getSelectedIndex();
        int testType = 0;
        switch (itemType) {
            case Conditional.ITEM_TYPE_SENSOR:
                testType = Conditional.ITEM_TO_SENSOR_TEST[_variableStateBox.getSelectedIndex()];
                break;
            case Conditional.ITEM_TYPE_TURNOUT:
                testType = Conditional.ITEM_TO_TURNOUT_TEST[_variableStateBox.getSelectedIndex()];
                break;
            case Conditional.ITEM_TYPE_LIGHT:
                testType = Conditional.ITEM_TO_LIGHT_TEST[_variableStateBox.getSelectedIndex()];
                break;
            case Conditional.ITEM_TYPE_SIGNALHEAD:
                testType = Conditional.ITEM_TO_SIGNAL_HEAD_TEST[_variableStateBox.getSelectedIndex()];
                break;
            case Conditional.ITEM_TYPE_SIGNALMAST:
                testType = Conditional.ITEM_TO_SIGNAL_MAST_TEST[_variableStateBox.getSelectedIndex()];
                break;
            case Conditional.ITEM_TYPE_MEMORY:
                testType = Conditional.ITEM_TO_MEMORY_TEST[_variableCompareTypeBox.getSelectedIndex()];
                break;
            case Conditional.ITEM_TYPE_CONDITIONAL:
                testType = Conditional.ITEM_TO_CONDITIONAL_TEST[_variableStateBox.getSelectedIndex()];
                break;
            case Conditional.ITEM_TYPE_WARRANT:
                testType = Conditional.ITEM_TO_WARRANT_TEST[_variableStateBox.getSelectedIndex()];
                break;
            case Conditional.ITEM_TYPE_CLOCK:
                testType = Conditional.TYPE_FAST_CLOCK_RANGE;
                break;
            case Conditional.ITEM_TYPE_OBLOCK:
                testType = Conditional.TYPE_BLOCK_STATUS_EQUALS;
                break;
            case Conditional.ITEM_TYPE_ENTRYEXIT:
                testType = Conditional.ITEM_TO_ENTRYEXIT_TEST[_variableStateBox.getSelectedIndex()];
                break;
            default:
                javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                        rbx.getString("ErrorVariableType"), Bundle.getMessage("ErrorTitle"),
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return false;
        }
        _curVariable.setType(testType);
        if (log.isDebugEnabled()) {
            log.debug("validateVariable: itemType= " + itemType + ", testType= " + testType);
        }
        switch (itemType) {
            case Conditional.ITEM_TYPE_SENSOR:
                name = validateSensorReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case Conditional.ITEM_TYPE_TURNOUT:
                name = validateTurnoutReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case Conditional.ITEM_TYPE_CONDITIONAL:
                name = validateConditionalReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                break;
            case Conditional.ITEM_TYPE_LIGHT:
                name = validateLightReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case Conditional.ITEM_TYPE_MEMORY:
                name = validateMemoryReference(name);
                if (name == null) {
                    return false;
                }
                String name2 = _variableData1Field.getText();
                if ((testType == Conditional.TYPE_MEMORY_COMPARE)
                        || (testType == Conditional.TYPE_MEMORY_COMPARE_INSENSITIVE)) {
                    name2 = validateMemoryReference(name2);
                    if (name2 == null) {
                        return false;
                    }
                }
                _curVariable.setDataString(name2);
                _curVariable.setNum1(_variableCompareOpBox.getSelectedIndex() + 1);
                break;
            case Conditional.ITEM_TYPE_CLOCK:
                int beginTime = parseTime(_variableData1Field.getText());
                if (beginTime < 0) {
                    // parse error occurred - message has been sent
                    return (false);
                }
                int endTime = parseTime(_variableData2Field.getText());
                if (endTime < 0) {
                    return (false);
                }
                // set beginning and end time (minutes since midnight)
                _curVariable.setNum1(beginTime);
                _curVariable.setNum2(endTime);
                name = "Clock";
                break;
            case Conditional.ITEM_TYPE_SIGNALHEAD:
                name = validateSignalHeadReference(name);
                if (name == null) {
                    return false;
                }
                if (testType == Conditional.TYPE_SIGNAL_HEAD_APPEARANCE_EQUALS) {
                    String appStr = (String) _variableSignalBox.getSelectedItem();
                    int type = ConditionalVariable.stringToVariableTest(appStr);
                    if (type < 0) {
                        javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                                rbx.getString("ErrorAppearance"), Bundle.getMessage("ErrorTitle"),
                                javax.swing.JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    _curVariable.setType(type);
                    _curVariable.setDataString(appStr);
                    if (log.isDebugEnabled()) {
                        log.debug("SignalHead \"" + name + "\"of type '" + testType
                                + "' _variableSignalBox.getSelectedItem()= "
                                + _variableSignalBox.getSelectedItem());
                    }
                }
                break;
            case Conditional.ITEM_TYPE_SIGNALMAST:
                name = validateSignalMastReference(name);
                if (name == null) {
                    return false;
                }
                if (testType == Conditional.TYPE_SIGNAL_MAST_ASPECT_EQUALS) {
                    if (_variableSignalBox.getSelectedIndex() < 0) {
                        javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                                rbx.getString("ErrorAspect"), Bundle.getMessage("ErrorTitle"),
                                javax.swing.JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    // save the selected aspect for comparison
                    _curVariable.setDataString((String) _variableSignalBox.getSelectedItem());
                    //                _curVariable.setType(ConditionalVariable.stringToVariableTest(appStr));
                }
                break;
            case Conditional.ITEM_TYPE_WARRANT:
                name = validateWarrantReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case Conditional.ITEM_TYPE_OBLOCK:
                name = validateOBlockReference(name);
                if (name == null) {
                    return false;
                }
                String str = (String) _variableStateBox.getSelectedItem();
                _curVariable.setDataString(OBlock.getSystemStatusName(str));
                if (log.isDebugEnabled()) {
                    log.debug("OBlock \"" + name + "\"of type '" + testType
                            + "' _variableStateBox.getSelectedItem()= "
                            + _variableStateBox.getSelectedItem());
                }
                break;
            case Conditional.ITEM_TYPE_ENTRYEXIT:
                name = validateEntryExitReference(name);
                if (name == null) {
                    return false;
                }
                break;
            default:
                javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                        rbx.getString("ErrorVariableType"), Bundle.getMessage("ErrorTitle"),
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return false;
        }
        _curVariable.setName(name);
        boolean result = _curVariable.evaluate();
        if (log.isDebugEnabled()) {
            log.debug("State Variable \"" + name + "\"of type '"
                    + ConditionalVariable.getTestTypeString(testType)
                    + "' state= " + result + " type= " + _curVariable.getType());
        }
        if (_curVariable.getType() == Conditional.TYPE_NONE) {
            javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                    rbx.getString("ErrorVariableState"), Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return (true);
    }   /* validateVariable */


    /**
     * Validate Action data from Edit Action Window, and transfer it to
     * current action object as appropriate.
     * <p>
     * Messages are sent to the user for any errors found. This routine returns
     * false immediately after finding an error, even if there might be more
     * errors.
     *
     * @return true if all data checks out OK, otherwise false.
     */
    boolean validateAction() {
        int itemType = _actionItemTypeBox.getSelectedIndex();
        int actionType = Conditional.ACTION_NONE;
        int selection = _actionTypeBox.getSelectedIndex();
        if (selection == 0) {
            javax.swing.JOptionPane.showMessageDialog(
                    editConditionalFrame, rbx.getString("makeSelection"),
                    Bundle.getMessage("WarningTitle"), javax.swing.JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String name = _actionNameField.getText().trim();
        String actionString = _shortActionString.getText().trim();
        _curAction.setActionString("");
        _curAction.setActionData(-1);
        boolean referenceByMemory = false;
        if (name.length() > 0 && name.charAt(0) == '@') {
            String memName = name.substring(1);
            if (!confirmIndirectMemory(memName)) {
                return false;
            }
            memName = validateMemoryReference(memName);
            if (memName == null) {
                return false;
            }
            referenceByMemory = true;
        }
        switch (itemType) {
            case Conditional.ITEM_TYPE_SENSOR:
                if (!referenceByMemory) {
                    name = validateSensorReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = Conditional.ITEM_TO_SENSOR_ACTION[selection - 1];
                if ((actionType == Conditional.ACTION_RESET_DELAYED_SENSOR)
                        || (actionType == Conditional.ACTION_DELAYED_SENSOR)) {
                    if (!validateTimeReference(actionType, actionString)) {
                        return (false);
                    }
                    _curAction.setActionString(actionString);
                }
                if ((actionType == Conditional.ACTION_SET_SENSOR)
                        || (actionType == Conditional.ACTION_RESET_DELAYED_SENSOR)
                        || (actionType == Conditional.ACTION_DELAYED_SENSOR)) {
                    if (_actionBox.getSelectedIndex() == 0) {
                        _curAction.setActionData(Sensor.ACTIVE);
                    } else if (_actionBox.getSelectedIndex() == 1) {
                        _curAction.setActionData(Sensor.INACTIVE);
                    } else {
                        _curAction.setActionData(Route.TOGGLE);
                    }
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case Conditional.ITEM_TYPE_TURNOUT:
                if (!referenceByMemory) {
                    name = validateTurnoutReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = Conditional.ITEM_TO_TURNOUT_ACTION[selection - 1];
                if ((actionType == Conditional.ACTION_RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.ACTION_DELAYED_TURNOUT)) {
                    if (!validateTimeReference(actionType, actionString)) {
                        return (false);
                    }
                    _curAction.setActionString(actionString);
                }
                if ((actionType == Conditional.ACTION_SET_TURNOUT)
                        || (actionType == Conditional.ACTION_RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.ACTION_DELAYED_TURNOUT)) {
                    if (_actionBox.getSelectedIndex() == 0) {
                        _curAction.setActionData(Turnout.CLOSED);
                    } else if (_actionBox.getSelectedIndex() == 1) {
                        _curAction.setActionData(Turnout.THROWN);
                    } else {
                        _curAction.setActionData(Route.TOGGLE);
                    }
                } else if (actionType == Conditional.ACTION_LOCK_TURNOUT) {
                    if (_actionBox.getSelectedIndex() == 0) {
                        _curAction.setActionData(Turnout.UNLOCKED);
                    } else if (_actionBox.getSelectedIndex() == 1) {
                        _curAction.setActionData(Turnout.LOCKED);
                    } else {
                        _curAction.setActionData(Route.TOGGLE);
                    }
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case Conditional.ITEM_TYPE_LIGHT:
                if (!referenceByMemory) {
                    name = validateLightReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = Conditional.ITEM_TO_LIGHT_ACTION[selection - 1];
                if (actionType == Conditional.ACTION_SET_LIGHT_INTENSITY) {
                    Light lgtx = getLight(name);
                    // check if light user name was entered
                    if (lgtx == null) {
                        return false;
                    }
                    if (!lgtx.isIntensityVariable()) {
                        javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                                java.text.MessageFormat.format(
                                        rbx.getString("Error45"), new Object[]{name}),
                                Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);
                        return (false);
                    }
                    if (!validateIntensityReference(actionType, actionString)) {
                        return (false);
                    }
                    _curAction.setActionString(actionString);
                } else if (actionType == Conditional.ACTION_SET_LIGHT_TRANSITION_TIME) {
                    Light lgtx = getLight(name);
                    // check if light user name was entered
                    if (lgtx == null) {
                        return false;
                    }
                    if (!lgtx.isTransitionAvailable()) {
                        javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                                java.text.MessageFormat.format(
                                        rbx.getString("Error40"), new Object[]{name}),
                                Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);
                        return (false);
                    }
                    if (!validateTimeReference(actionType, actionString)) {
                        return (false);
                    }
                    _curAction.setActionString(actionString);
                } else if (actionType == Conditional.ACTION_SET_LIGHT) {
                    if (_actionBox.getSelectedIndex() == 0) {
                        _curAction.setActionData(Light.ON);
                    } else if (_actionBox.getSelectedIndex() == 1) {
                        _curAction.setActionData(Light.OFF);
                    } else {
                        _curAction.setActionData(Route.TOGGLE);
                    }
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case Conditional.ITEM_TYPE_SIGNALHEAD:
                if (!referenceByMemory) {
                    name = validateSignalHeadReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[selection - 1];
                if (actionType == Conditional.ACTION_SET_SIGNAL_APPEARANCE) {
                    String appStr = (String) _actionBox.getSelectedItem();
                    _curAction.setActionData(DefaultConditionalAction.stringToActionData(appStr));
                    _curAction.setActionString(appStr);
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case Conditional.ITEM_TYPE_SIGNALMAST:
                if (!referenceByMemory) {
                    name = validateSignalMastReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = Conditional.ITEM_TO_SIGNAL_MAST_ACTION[selection - 1];
                if (actionType == Conditional.ACTION_SET_SIGNALMAST_ASPECT) {
                    _curAction.setActionString((String) _actionBox.getSelectedItem());
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case Conditional.ITEM_TYPE_MEMORY:
                if (referenceByMemory) {
                    javax.swing.JOptionPane.showMessageDialog(_editActionFrame, rbx.getString("Warn6"), Bundle.getMessage("WarningTitle"),
                            javax.swing.JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                name = validateMemoryReference(name);
                if (name == null) {
                    return false;
                }
                actionType = Conditional.ITEM_TO_MEMORY_ACTION[selection - 1];
                if (actionType == Conditional.ACTION_COPY_MEMORY) {
                    actionString = validateMemoryReference(actionString);
                    if (actionString == null) {
                        return false;
                    }
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                _curAction.setActionString(actionString);
                break;
            case Conditional.ITEM_TYPE_LOGIX:
                if (!referenceByMemory) {
                    name = validateLogixReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = Conditional.ITEM_TO_LOGIX_ACTION[selection - 1];
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case Conditional.ITEM_TYPE_WARRANT:
                if (!referenceByMemory) {
                    name = validateWarrantReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = Conditional.ITEM_TO_WARRANT_ACTION[selection - 1];
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                if (actionType == Conditional.ACTION_CONTROL_TRAIN) {
                    if (_actionBox.getSelectedIndex() == 0) {
                        _curAction.setActionData(Warrant.HALT);
                    } else if (_actionBox.getSelectedIndex() == 1) {
                        _curAction.setActionData(Warrant.RESUME);
                    } else {
                        _curAction.setActionData(Warrant.ABORT);
                    }
                } else if (actionType == Conditional.ACTION_SET_TRAIN_ID
                        || actionType == Conditional.ACTION_SET_TRAIN_NAME
                        || actionType == Conditional.ACTION_THROTTLE_FACTOR) {
                    _curAction.setActionString(actionString);
                }
                break;
            case Conditional.ITEM_TYPE_OBLOCK:
                if (!referenceByMemory) {
                    name = validateOBlockReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = Conditional.ITEM_TO_OBLOCK_ACTION[selection - 1];
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                if (actionType == Conditional.ACTION_SET_BLOCK_VALUE) {
                    _curAction.setActionString(actionString);
                }
                break;
            case Conditional.ITEM_TYPE_CLOCK:
                actionType = Conditional.ITEM_TO_CLOCK_ACTION[selection - 1];
                if (actionType == Conditional.ACTION_SET_FAST_CLOCK_TIME) {
                    int time = parseTime(_longActionString.getText().trim());
                    if (time < 0) {
                        return (false);
                    }
                    _curAction.setActionData(time);
                }
                break;
            case Conditional.ITEM_TYPE_AUDIO:
                actionType = Conditional.ITEM_TO_AUDIO_ACTION[selection - 1];
                if (actionType == Conditional.ACTION_PLAY_SOUND) {
                    _curAction.setActionString(_longActionString.getText().trim());
                } else if (actionType == Conditional.ACTION_CONTROL_AUDIO) {
                    if (!referenceByMemory) {
                        name = validateAudioReference(name);
                        if (name == null) {
                            return false;
                        }
                    }
                    _actionNameField.setText(name);
                    _curAction.setDeviceName(name);
                    switch (_actionBox.getSelectedIndex()) {
                        case 0:
                            _curAction.setActionData(Audio.CMD_PLAY);
                            break;
                        case 1:
                            _curAction.setActionData(Audio.CMD_STOP);
                            break;
                        case 2:
                            _curAction.setActionData(Audio.CMD_PLAY_TOGGLE);
                            break;
                        case 3:
                            _curAction.setActionData(Audio.CMD_PAUSE);
                            break;
                        case 4:
                            _curAction.setActionData(Audio.CMD_RESUME);
                            break;
                        case 5:
                            _curAction.setActionData(Audio.CMD_PAUSE_TOGGLE);
                            break;
                        case 6:
                            _curAction.setActionData(Audio.CMD_REWIND);
                            break;
                        case 7:
                            _curAction.setActionData(Audio.CMD_FADE_IN);
                            break;
                        case 8:
                            _curAction.setActionData(Audio.CMD_FADE_OUT);
                            break;
                        case 9:
                            _curAction.setActionData(Audio.CMD_RESET_POSITION);
                            break;
                        default:
                            log.warn("Unexpected _actionBox.getSelectedIndex() of {}", _actionBox.getSelectedIndex());
                            break;
                    }
                }
                break;
            case Conditional.ITEM_TYPE_SCRIPT:
                actionType = Conditional.ITEM_TO_SCRIPT_ACTION[selection - 1];
                if (actionType == Conditional.ACTION_RUN_SCRIPT) {
                    _curAction.setActionString(_longActionString.getText().trim());
                } else if (actionType == Conditional.ACTION_JYTHON_COMMAND) {
                    _curAction.setActionString(_shortActionString.getText().trim());
                }
                break;
            case Conditional.ITEM_TYPE_OTHER:
                actionType = Conditional.ITEM_TO_OTHER_ACTION[selection - 1];
                if (actionType == Conditional.ACTION_TRIGGER_ROUTE) {
                    if (!referenceByMemory) {
                        name = validateRouteReference(name);
                        if (name == null) {
                            return false;
                        }
                    }
                    _actionNameField.setText(name);
                    _curAction.setDeviceName(name);
                }
                break;
            default:
                break;
        }
        _curAction.setType(actionType);
        if (actionType != Conditional.ACTION_NONE) {
            _curAction.setOption(_actionOptionBox.getSelectedIndex() + 1);
        } else {
            _curAction.setOption(0);
        }
        _editActionFrame.pack();
        return (true);
    }

    /**
     * Convert user setting in Conditional Action configuration pane to integer for processing.
     *
     * @param itemType value for current item type
     * @param actionTypeSelection index of selected item in configuration comboBox
     * @return integer representing the selected action
     */
    static int getActionTypeFromBox(int itemType, int actionTypeSelection) {
        if (itemType < 0 || actionTypeSelection < 0) {
            return Conditional.ACTION_NONE;
        }
        switch (itemType) {
            case Conditional.ITEM_TYPE_SENSOR:
                return Conditional.ITEM_TO_SENSOR_ACTION[actionTypeSelection];
            case Conditional.ITEM_TYPE_TURNOUT:
                return Conditional.ITEM_TO_TURNOUT_ACTION[actionTypeSelection];
            case Conditional.ITEM_TYPE_LIGHT:
                return Conditional.ITEM_TO_LIGHT_ACTION[actionTypeSelection];
            case Conditional.ITEM_TYPE_SIGNALHEAD:
                return Conditional.ITEM_TO_SIGNAL_HEAD_ACTION[actionTypeSelection];
            case Conditional.ITEM_TYPE_SIGNALMAST:
                return Conditional.ITEM_TO_SIGNAL_MAST_ACTION[actionTypeSelection];
            case Conditional.ITEM_TYPE_MEMORY:
                return Conditional.ITEM_TO_MEMORY_ACTION[actionTypeSelection];
            case Conditional.ITEM_TYPE_LOGIX:
                return Conditional.ITEM_TO_LOGIX_ACTION[actionTypeSelection];
            case Conditional.ITEM_TYPE_WARRANT:
                return Conditional.ITEM_TO_WARRANT_ACTION[actionTypeSelection];
            case Conditional.ITEM_TYPE_OBLOCK:
                return Conditional.ITEM_TO_OBLOCK_ACTION[actionTypeSelection];
            case Conditional.ITEM_TYPE_CLOCK:
                return Conditional.ITEM_TO_CLOCK_ACTION[actionTypeSelection];
            case Conditional.ITEM_TYPE_AUDIO:
                return Conditional.ITEM_TO_AUDIO_ACTION[actionTypeSelection];
            case Conditional.ITEM_TYPE_SCRIPT:
                return Conditional.ITEM_TO_SCRIPT_ACTION[actionTypeSelection];
            case Conditional.ITEM_TYPE_OTHER:
                return Conditional.ITEM_TO_OTHER_ACTION[actionTypeSelection];
        }
        return Conditional.ACTION_NONE;
    }

    // *********** Utility Methods ********************

    /**
     * Check if String is an integer or references an integer.
     *
     * @param actionType Conditional action to check for, i.e. ACTION_SET_LIGHT_INTENSITY
     * @param intReference string referencing a decimal for light intensity or the name of a memory
     * @return true if either correct decimal format or a memory with the given name is present
     */
    boolean validateIntensityReference(int actionType, String intReference) {
        if (intReference == null || intReference.trim().length() == 0) {
            displayBadNumberReference(actionType);
            return false;
        }
        try {
            return validateIntensity(Integer.valueOf(intReference).intValue());
        } catch (NumberFormatException e) {
            String intRef = intReference;
            if (intReference.length() > 1 && intReference.charAt(0) == '@') {
                intRef = intRef.substring(1);
            }
            if (!confirmIndirectMemory(intRef)) {
                return false;
            }
            intRef = validateMemoryReference(intRef);
            if (intRef != null) // memory named 'intReference' exists
            {
                Memory m = InstanceManager.memoryManagerInstance().getByUserName(intRef);
                if (m == null) {
                    m = InstanceManager.memoryManagerInstance().getBySystemName(intRef);
                }
                try {
                    validateIntensity(Integer.valueOf((String) m.getValue()).intValue());
                } catch (NumberFormatException ex) {
                    javax.swing.JOptionPane.showMessageDialog(
                            editConditionalFrame, java.text.MessageFormat.format(rbx.getString("Error24"),
                                    intReference), Bundle.getMessage("WarningTitle"), javax.swing.JOptionPane.WARNING_MESSAGE);
                }
                return true;    // above is a warning to set memory correctly
            }
            displayBadNumberReference(actionType);
        }
        return false;
    }
    
    /**
     * Check if text represents an integer is suitable for percentage
     * w/o NumberFormatException.
     *
     * @param time value to use as light intensity percentage
     * @return true if time is an integer in range 0 - 100
     */
    boolean validateIntensity(int time) {
        if (time < 0 || time > 100) {
            javax.swing.JOptionPane.showMessageDialog(
                    editConditionalFrame, java.text.MessageFormat.format(rbx.getString("Error38"),
                            time, rbx.getString("Error42")), Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    /**
     * Check if a string is decimal or references a decimal.
     *
     * @param actionType integer representing the Conditional action type being checked, i.e. ACTION_DELAYED_TURNOUT
     * @param ref entry to check
     * @return true if ref is itself a decimal or user will provide one from a Memory at run time
     */
    boolean validateTimeReference (int actionType, String ref) {
        if (ref == null || ref.trim().length() == 0) {
            displayBadNumberReference(actionType);
            return false;
        }
        try {
            return validateTime(actionType, Float.valueOf(ref).floatValue());
            // return true if ref is decimal within allowed range
        } catch (NumberFormatException e) {
            String memRef = ref;
            if (ref.length() > 1 && ref.charAt(0) == '@') {
                memRef = ref.substring(1);
            }
            if (!confirmIndirectMemory(memRef)) {
                return false;
            }
            memRef = validateMemoryReference(memRef);
            if (memRef != null) // memory named 'intReference' exists
            {
                Memory m = InstanceManager.memoryManagerInstance().getByUserName(memRef);
                if (m == null) {
                    m = InstanceManager.memoryManagerInstance().getBySystemName(memRef);
                }
                try {
                    validateTime(actionType, Float.valueOf((String) m.getValue()).floatValue());
                } catch (NumberFormatException ex) {
                    javax.swing.JOptionPane.showMessageDialog(
                            editConditionalFrame, java.text.MessageFormat.format(rbx.getString("Error24"),
                                    memRef), Bundle.getMessage("WarningTitle"), javax.swing.JOptionPane.WARNING_MESSAGE);
                }
                return true;    // above is a warning to set memory correctly
            }
            displayBadNumberReference(actionType);
        }
        return false;
    }

    /**
     * Range check time entry (assumes seconds).
     *
     * @param actionType integer representing the Conditional action type being checked, i.e. ACTION_DELAYED_TURNOUT
     * @param time value to be checked
     * @return false if time &gt; 3600 (seconds) or too small
     */
    boolean validateTime(int actionType, float time) {
        float maxTime = 3600;     // more than 1 hour
        float minTime = 0.020f;
        if (time < minTime || time > maxTime) {
            String errorNum = " ";
            switch (actionType) {
                case Conditional.ACTION_DELAYED_TURNOUT:
                    errorNum = "Error39";
                    break;
                case Conditional.ACTION_RESET_DELAYED_TURNOUT:
                    errorNum = "Error41";
                    break;
                case Conditional.ACTION_DELAYED_SENSOR:
                    errorNum = "Error23";
                    break;
                case Conditional.ACTION_RESET_DELAYED_SENSOR:
                    errorNum = "Error27";
                    break;
                case Conditional.ACTION_SET_LIGHT_TRANSITION_TIME:
                    errorNum = "Error29";
                    break;
                default:
                    break;
            }
            javax.swing.JOptionPane.showMessageDialog(
                    editConditionalFrame, java.text.MessageFormat.format(rbx.getString("Error38"),
                            time, rbx.getString(errorNum)), Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Display an error message to user when an invalid number is provided in Conditional set up.
     *
     * @param actionType integer representing the Conditional action type being checked, i.e. ACTION_DELAYED_TURNOUT
     */
    void displayBadNumberReference(int actionType) {
        String errorNum = " ";
        switch (actionType) {
            case Conditional.ACTION_DELAYED_TURNOUT:
                errorNum = "Error39";
                break;
            case Conditional.ACTION_RESET_DELAYED_TURNOUT:
                errorNum = "Error41";
                break;
            case Conditional.ACTION_DELAYED_SENSOR:
                errorNum = "Error23";
                break;
            case Conditional.ACTION_RESET_DELAYED_SENSOR:
                errorNum = "Error27";
                break;
            case Conditional.ACTION_SET_LIGHT_INTENSITY:
                javax.swing.JOptionPane.showMessageDialog(
                        editConditionalFrame, rbx.getString("Error43"),
                        Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            case Conditional.ACTION_SET_LIGHT_TRANSITION_TIME:
                errorNum = "Error29";
                break;
            default:
                log.warn("Unexpected action type {} in displayBadNumberReference", actionType);
        }
        javax.swing.JOptionPane.showMessageDialog(
                editConditionalFrame, java.text.MessageFormat.format(rbx.getString("Error9"),
                        rbx.getString(errorNum)), Bundle.getMessage("ErrorTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Check Memory reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Memory, null if not found
     */
    String validateMemoryReference(String name) {
        Memory m = null;
        if (name != null) {
            if (name.length() > 0) {
                m = InstanceManager.memoryManagerInstance().getByUserName(name);
                if (m != null) {
                    return name;
                }
            }
            m = InstanceManager.memoryManagerInstance().getBySystemName(name);
        }
        if (m == null) {
            messageInvalidActionItemName(name, "Memory"); //NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check if user will provide a valid item name in a Memory variable
     *
     * @param memName Memory location to provide item name at run time
     * @return false if user replies No
     */
    boolean confirmIndirectMemory(String memName) {
        if (!_suppressIndirectRef) {
            int response = JOptionPane.showConfirmDialog(_editActionFrame, java.text.MessageFormat.format(
                    rbx.getString("ConfirmIndirectReference"), memName),
                    rbx.getString("ConfirmTitle"), JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.NO_OPTION) {
                return false;
            } else if (response == JOptionPane.CANCEL_OPTION) {
                _suppressIndirectRef = true;
            }
        }
        return true;       
    }

    /**
     * Check Turnout reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Turnout, null if not found
     */
    String validateTurnoutReference(String name) {
        Turnout t = null;
        if (name != null) {
            if (name.length() > 0) {
                t = InstanceManager.turnoutManagerInstance().getByUserName(name);
                if (t != null) {
                    return name;
                }
            }
            t = InstanceManager.turnoutManagerInstance().getBySystemName(name);
        }
        if (t == null) {
            messageInvalidActionItemName(name, "Turnout"); //NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check SignalHead reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding SignalHead, null if not found
     */
    String validateSignalHeadReference(String name) {
        SignalHead h = null;
        if (name != null) {
            if (name.length() > 0) {
                h = InstanceManager.getDefault(jmri.SignalHeadManager.class).getByUserName(name);
                if (h != null) {
                    return name;
                }
            }
            h = InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(name);
        }
        if (h == null) {
            messageInvalidActionItemName(name, "SignalHead"); //NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check SignalMast reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Signal Mast, null if not found
     */
    String validateSignalMastReference(String name) {
        SignalMast h = null;
        if (name != null) {
            if (name.length() > 0) {
                h = InstanceManager.getDefault(jmri.SignalMastManager.class).getByUserName(name);
                if (h != null) {
                    return name;
                }
            }
            try {
                h = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(name);
            } catch (IllegalArgumentException ex) {
                h = null; // tested below
            }
        }
        if (h == null) {
            messageInvalidActionItemName(name, "SignalMast"); //NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check Warrant reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Warrant, null if not found
     */
    String validateWarrantReference(String name) {
        Warrant w = null;
        if (name != null) {
            if (name.length() > 0) {
                w = InstanceManager.getDefault(WarrantManager.class).getByUserName(name);
                if (w != null) {
                    return name;
                }
            }
            w = InstanceManager.getDefault(WarrantManager.class).getBySystemName(name);
        }
        if (w == null) {
            messageInvalidActionItemName(name, "Warrant"); //NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check OBlock reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding OBlock, null if not found
     */
    String validateOBlockReference(String name) {
        OBlock b = null;
        if (name != null) {
            if (name.length() > 0) {
                b = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getByUserName(name);
                if (b != null) {
                    return name;
                }
            }
            b = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getBySystemName(name);
        }
        if (b == null) {
            messageInvalidActionItemName(name, "OBlock"); //NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check Sensor reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Sensor, null if not found
     */
    String validateSensorReference(String name) {
        Sensor s = null;
        if (name != null) {
            if (name.length() > 0) {
                s = InstanceManager.getDefault(jmri.SensorManager.class).getByUserName(name);
                if (s != null) {
                    return name;
                }
            }
            s = InstanceManager.getDefault(jmri.SensorManager.class).getBySystemName(name);
        }
        if (s == null) {
            messageInvalidActionItemName(name, "Sensor"); //NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check Light reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Light, null if not found
     */
    String validateLightReference(String name) {
        Light l = null;
        if (name != null) {
            if (name.length() > 0) {
                l = InstanceManager.lightManagerInstance().getByUserName(name);
                if (l != null) {
                    return name;
                }
            }
            l = InstanceManager.lightManagerInstance().getBySystemName(name);
        }
        if (l == null) {
            messageInvalidActionItemName(name, "Light"); //NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check Conditional reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Conditional, null if not found
     */
    String validateConditionalReference(String name) {
        Conditional c = null;
        if (name != null) {
            if (name.length() > 0) {
                c = _conditionalManager.getByUserName(name);
                if (c != null) {
                    return name;
                }
            }
            c = _conditionalManager.getBySystemName(name);
        }
        if (c == null) {
            messageInvalidActionItemName(name, "Conditional"); //NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check Logix reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Logix, null if not found
     */
    String validateLogixReference(String name) {
        Logix l = null;
        if (name != null) {
            if (name.length() > 0) {
                l = _logixManager.getByUserName(name);
                if (l != null) {
                    return name;
                }
            }
            l = _logixManager.getBySystemName(name);
        }
        if (l == null) {
            messageInvalidActionItemName(name, "Logix"); //NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check Route reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding Route, null if not found
     */
    String validateRouteReference(String name) {
        Route r = null;
        if (name != null) {
            if (name.length() > 0) {
                r = InstanceManager.getDefault(jmri.RouteManager.class).getByUserName(name);
                if (r != null) {
                    return name;
                }
            }
            r = InstanceManager.getDefault(jmri.RouteManager.class).getBySystemName(name);
        }
        if (r == null) {
            messageInvalidActionItemName(name, "Route"); //NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check an Audio reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system or user name of the corresponding AudioManager, null if not found
     */
    String validateAudioReference(String name) {
        Audio a = null;
        if (name != null) {
            if (name.length() > 0) {
                a = InstanceManager.getDefault(jmri.AudioManager.class).getByUserName(name);
                if (a != null) {
                    return name;
                }
            }
            a = InstanceManager.getDefault(jmri.AudioManager.class).getBySystemName(name);
        }
        if (a == null || (a.getSubType() != Audio.SOURCE && a.getSubType() != Audio.LISTENER)) {
            messageInvalidActionItemName(name, "Audio"); //NOI18N
            return null;
        }
        return name;
    }

    /**
     * Check an EntryExit reference of text.
     * <p>
     * Show a message if not found.
     *
     * @param name the name to look for
     * @return the system name of the corresponding EntryExit pair, null if not found
     */
    String validateEntryExitReference(String name) {
        NamedBean nb = null;
        if (name != null) {
            if (name.length() > 0) {
                nb = jmri.InstanceManager.getDefault(jmri.jmrit.signalling.EntryExitPairs.class).getNamedBean(name);
                if (nb != null) {
                    return nb.getSystemName();
                }
            }
        }
        messageInvalidActionItemName(name, "EntryExit"); //NOI18N
        return null;
    }

    /**
     * Get Light instance.
     * <p>
     * Show a message if not found.
     *
     * @param name user or system name of an existing light
     * @return the Light object
     */
    Light getLight(String name) {
        if (name == null) {
            return null;
        }
        Light l = null;
        if (name.length() > 0) {
            l = InstanceManager.lightManagerInstance().getByUserName(name);
            if (l != null) {
                return l;
            }
            l = InstanceManager.lightManagerInstance().getBySystemName(name);
        }
        if (l == null) {
            messageInvalidActionItemName(name, "Light"); //NOI18N
        }
        return l;
    }

    int parseTime(String s) {
        int nHour = 0;
        int nMin = 0;
        boolean error = false;
        int index = s.indexOf(':');
        String hour = null;
        String minute = null;
        try {
            if (index > 0) { // : after start
                hour = s.substring(0, index);
                if (index+1 < s.length()) { // check for : at end
                    minute = s.substring(index + 1);
                } else {
                    minute = "0";
                }
            } else if (index == 0) { // : at start
                hour = "0";
                minute = s.substring(index + 1);
            } else {
                hour = s;
                minute = "0";
            }
        } catch (IndexOutOfBoundsException ioob) {
            error = true;
        }
        if (!error) {
            try {
                nHour = Integer.valueOf(hour);
                if ((nHour < 0) || (nHour > 24)) {
                    error = true;
                }
                nMin = Integer.valueOf(minute);
                if ((nMin < 0) || (nMin > 59)) {
                    error = true;
                }
            } catch (NumberFormatException e) {
                error = true;
            }
        }
        if (error) {
            // if unsuccessful, print error message
            javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                    java.text.MessageFormat.format(rbx.getString("Error26"),
                            new Object[]{s}), Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return (-1);
        }
        // here if successful
        return ((nHour * 60) + nMin);
    }

    /**
     * Format time to hh:mm given integer hour and minute.
     *
     * @param hour value for time hours
     * @param minute value for time minutes
     * @return Formatted time string
     */
    public static String formatTime(int hour, int minute) {
        String s = "";
        String t = Integer.toString(hour);
        if (t.length() == 2) {
            s = t + ":";
        } else if (t.length() == 1) {
            s = "0" + t + ":";
        }
        t = Integer.toString(minute);
        if (t.length() == 2) {
            s = s + t;
        } else if (t.length() == 1) {
            s = s + "0" + t;
        }
        if (s.length() != 5) {
            // input error
            s = "00:00";
        }
        return s;
    }

    // ********************** Error Dialogs *********************************

    /**
     * Send an Invalid Conditional SignalHead state message for Edit Logix pane.
     *
     * @param name proposed appearance description
     * @param appearance to compare to
     */
    void messageInvalidSignalHeadAppearance(String name, String appearance) {
        javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                java.text.MessageFormat.format(rbx.getString("Error21"),
                        new Object[]{name, appearance}), Bundle.getMessage("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Send an Invalid Conditional Action name message for Edit Logix pane.
     *
     * @param name user or system name to look up
     * @param itemType type of Bean to look for
     */
    void messageInvalidActionItemName(String name, String itemType) {
        javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                java.text.MessageFormat.format(rbx.getString("Error22"),
                        new Object[]{name, Bundle.getMessage("BeanName" + itemType)}), Bundle.getMessage("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Send a duplicate Conditional user name message for Edit Logix pane.
     *
     * @param svName proposed name that duplicates an existing name
     */
    void messageDuplicateConditionalUserName(String svName) {
        javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                java.text.MessageFormat.format(rbx.getString("Error30"),
                        new Object[]{svName}), Bundle.getMessage("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    // *********** Special Table Models ********************

    /**
     * Table model for Conditionals in the Edit Logix pane.
     */
    public class ConditionalTableModel extends AbstractTableModel implements
            PropertyChangeListener {

        public static final int SNAME_COLUMN = 0;

        public static final int UNAME_COLUMN = 1;

        public static final int STATE_COLUMN = 2;

        public static final int BUTTON_COLUMN = 3;

        public ConditionalTableModel() {
            super();
            _conditionalManager.addPropertyChangeListener(this);
            updateConditionalListeners();
        }

        synchronized void updateConditionalListeners() {
            // first, remove listeners from the individual objects
            String sNam = "";
            Conditional c = null;
            numConditionals = _curLogix.getNumConditionals();
            for (int i = 0; i < numConditionals; i++) {
                // if object has been deleted, it's not here; ignore it
                sNam = _curLogix.getConditionalByNumberOrder(i);
                c = _conditionalManager.getBySystemName(sNam);
                if (c != null) {
                    c.removePropertyChangeListener(this);
                }
            }
            // and add them back in
            for (int i = 0; i < numConditionals; i++) {
                sNam = _curLogix.getConditionalByNumberOrder(i);
                c = _conditionalManager.getBySystemName(sNam);
                if (c != null) {
                    addPropertyChangeListener(this);
                }
            }
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new NamedBean is available in the manager
                updateConditionalListeners();
                fireTableDataChanged();
            } else if (matchPropertyName(e)) {
                // a value changed.
                fireTableDataChanged();
            }
        }

        /**
         * Check if this property event is announcing a change this table should display.
         * <p>
         * Note that events will come both from the NamedBeans and from the
         * manager.
         *
         * @param e the event heard
         * @return true if a change in State or Appearance was heard
         */
        boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
            return (e.getPropertyName().indexOf("State") >= 0 || e
                    .getPropertyName().indexOf("Appearance") >= 0);
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == BUTTON_COLUMN) {
                return JButton.class;
            }
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public int getRowCount() {
            return (numConditionals);
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            if (!_inReorderMode) {
                return ((c == UNAME_COLUMN) || (c == BUTTON_COLUMN));
            } else if (c == BUTTON_COLUMN) {
                if (r >= _nextInOrder) {
                    return (true);
                }
            }
            return (false);
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case SNAME_COLUMN:
                    return Bundle.getMessage("ColumnSystemName");
                case UNAME_COLUMN:
                    return Bundle.getMessage("ColumnUserName");
                case BUTTON_COLUMN:
                    return ""; // no label
                case STATE_COLUMN:
                    return Bundle.getMessage("ColumnState");
                default:
                    return "";
            }
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case SNAME_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                case UNAME_COLUMN:
                    return new JTextField(17).getPreferredSize().width;
                case BUTTON_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                case STATE_COLUMN:
                    return new JTextField(12).getPreferredSize().width;
                default:
                    return new JTextField(5).getPreferredSize().width;
            }
        }

        @Override
        public Object getValueAt(int r, int col) {
            int rx = r;
            if ((rx > numConditionals) || (_curLogix == null)) {
                return null;
            }
            switch (col) {
                case BUTTON_COLUMN:
                    if (!_inReorderMode) {
                        return Bundle.getMessage("ButtonEdit");
                    } else if (_nextInOrder == 0) {
                        return rbx.getString("ButtonFirst");
                    } else if (_nextInOrder <= r) {
                        return rbx.getString("ButtonNext");
                    } else {
                        return Integer.toString(rx + 1);
                    }
                case SNAME_COLUMN:
                    return _curLogix.getConditionalByNumberOrder(rx);
                case UNAME_COLUMN: {
                    //log.debug("ConditionalTableModel: "+_curLogix.getConditionalByNumberOrder(rx));
                    Conditional c = _conditionalManager.getBySystemName(
                            _curLogix.getConditionalByNumberOrder(rx));
                    if (c != null) {
                        return c.getUserName();
                    }
                    return "";
                }
                case STATE_COLUMN:
                    Conditional c = _conditionalManager.getBySystemName(
                            _curLogix.getConditionalByNumberOrder(rx));
                    if (c != null) {
                        int curState = c.getState();
                        if (curState == Conditional.TRUE) {
                            return rbx.getString("True");
                        }
                        if (curState == Conditional.FALSE) {
                            return rbx.getString("False");
                        }
                    }
                    return Bundle.getMessage("BeanStateUnknown");
                default:
                    return Bundle.getMessage("BeanStateUnknown");
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            int rx = row;
            if ((rx > numConditionals) || (_curLogix == null)) {
                return;
            }
            if (col == BUTTON_COLUMN) {
                if (_inReorderMode) {
                    swapConditional(row);
                } else if (_curLogix.getSystemName().equals(SensorGroupFrame.logixSysName)) {
                    javax.swing.JOptionPane.showMessageDialog(
                            editConditionalFrame, java.text.MessageFormat.format(rbx.getString("Warn8"),
                                    new Object[]{SensorGroupFrame.logixUserName, SensorGroupFrame.logixSysName}),
                            Bundle.getMessage("WarningTitle"), javax.swing.JOptionPane.WARNING_MESSAGE);
                } else {
                    // Use separate Runnable so window is created on top
                    class WindowMaker implements Runnable {

                        int row;

                        WindowMaker(int r) {
                            row = r;
                        }

                        @Override
                        public void run() {
                            editConditionalPressed(row);
                        }
                    }
                    WindowMaker t = new WindowMaker(rx);
                    javax.swing.SwingUtilities.invokeLater(t);
                }
            } else if (col == UNAME_COLUMN) {
                String uName = (String) value;
                Conditional cn = _conditionalManager.getByUserName(_curLogix,
                        uName.trim()); // N11N
                if (cn == null) {
                    _conditionalManager.getBySystemName(
                            _curLogix.getConditionalByNumberOrder(rx))
                            .setUserName(uName.trim()); // N11N
                    fireTableRowsUpdated(rx, rx);
                } else {
                    String svName = _curLogix.getConditionalByNumberOrder(rx);
                    if (cn != _conditionalManager.getBySystemName(svName)) {
                        messageDuplicateConditionalUserName(cn
                                .getSystemName());
                    }
                }
            }
        }
    }

    /**
     * Table model for State Variables in Edit Conditional pane.
     */
    public class VariableTableModel extends AbstractTableModel {

        public static final int ROWNUM_COLUMN = 0;

        public static final int AND_COLUMN = 1;

        public static final int NOT_COLUMN = 2;

        public static final int DESCRIPTION_COLUMN = 3;

        public static final int STATE_COLUMN = 4;

        public static final int TRIGGERS_COLUMN = 5;

        public static final int EDIT_COLUMN = 6;

        public static final int DELETE_COLUMN = 7;

        @Override
        public Class<?> getColumnClass(int c) {
            switch (c) {
                case ROWNUM_COLUMN:
                    return String.class;
                case AND_COLUMN:
                    return JComboBox.class;
                case NOT_COLUMN:
                    return JComboBox.class;
                case DESCRIPTION_COLUMN:
                    return String.class;
                case STATE_COLUMN:
                    return String.class;
                case TRIGGERS_COLUMN:
                    return Boolean.class;
                case EDIT_COLUMN:
                    return JButton.class;
                case DELETE_COLUMN:
                    return JButton.class;
            }
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return 8;
        }

        @Override
        public int getRowCount() {
            return _variableList.size();
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            switch (c) {
                case ROWNUM_COLUMN:
                    return (false);
                case AND_COLUMN:
                    return (_logicType == Conditional.MIXED);
                case NOT_COLUMN:
                    return (true);
                case DESCRIPTION_COLUMN:
                    return (false);
                case STATE_COLUMN:
                    return (false);
                case TRIGGERS_COLUMN:
                    return (true);
                case EDIT_COLUMN:
                    return (true);
                case DELETE_COLUMN:
                    return (true);
            }
            return (false);
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case ROWNUM_COLUMN:
                    return (rbx.getString("ColumnLabelRow"));
                case AND_COLUMN:
                    return (rbx.getString("ColumnLabelOperator"));
                case NOT_COLUMN:
                    return (rbx.getString("ColumnLabelNot"));
                case DESCRIPTION_COLUMN:
                    return (rbx.getString("ColumnLabelDescription"));
                case STATE_COLUMN:
                    return (Bundle.getMessage("ColumnState"));
                case TRIGGERS_COLUMN:
                    return (rbx.getString("ColumnLabelTriggersCalculation"));
                case EDIT_COLUMN:
                    return "";
                case DELETE_COLUMN:
                    return "";
            }
            return "";
        }

        public int getPreferredWidth(int col) {
            if (col == DESCRIPTION_COLUMN) {
                return 500;
            }
            return 10;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (r >= _variableList.size()) {
                return null;
            }
            ConditionalVariable variable = _variableList.get(r);
            switch (c) {
                case ROWNUM_COLUMN:
                    return ("R" + (r + 1)); //NOI18N
                case AND_COLUMN:
                    if (r == 0) { //removed: || _logicType == Conditional.MIXED
                        return "";
                    }
                    return variable.getOpernString(); // also display Operand selection when set to Mixed
                case NOT_COLUMN:
                    if (variable.isNegated()) {
                        return Bundle.getMessage("LogicNOT");
                    }
                    break;
                case DESCRIPTION_COLUMN:
                    return variable.toString();
                case STATE_COLUMN:
                    switch (variable.getState()) {
                        case Conditional.TRUE:
                            return rbx.getString("True");
                        case Conditional.FALSE:
                            return rbx.getString("False");
                        case NamedBean.UNKNOWN:
                            return Bundle.getMessage("BeanStateUnknown");
                    }
                    break;
                case TRIGGERS_COLUMN:
                    return Boolean.valueOf(variable.doTriggerActions());
                case EDIT_COLUMN:
                    return Bundle.getMessage("ButtonEdit");
                case DELETE_COLUMN:
                    return Bundle.getMessage("ButtonDelete");
                default:
                    break;
            }
            return null;
        }

        @Override
        public void setValueAt(Object value, int r, int c) {
            if (r >= _variableList.size()) {
                return;
            }
            ConditionalVariable variable = _variableList.get(r);
            switch (c) {
                case AND_COLUMN:
                    variableOperatorChanged(r, (String) value);
                    break;
                case NOT_COLUMN:
                    variableNegationChanged(r, (String) value);
                    break;
                case STATE_COLUMN:
                    String state = ((String) value);
                    if (state.equals(rbx.getString("True").toUpperCase().trim())) {
                        variable.setState(Conditional.TRUE);
                    } else if (state.equals(rbx.getString("False").toUpperCase().trim())) {
                        variable.setState(Conditional.FALSE);
                    } else {
                        variable.setState(NamedBean.UNKNOWN);
                    }
                    break;
                case TRIGGERS_COLUMN:
                    variable.setTriggerActions(!variable.doTriggerActions());
                    break;
                case EDIT_COLUMN:
                    if (LRouteTableAction.LOGIX_INITIALIZER.equals(_curLogix.getSystemName())) {
                        javax.swing.JOptionPane.showMessageDialog(editConditionalFrame,
                                rbx.getString("Error49"), Bundle.getMessage("ErrorTitle"),
                                javax.swing.JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    // Use separate Runnable so window is created on top
                    class WindowMaker implements Runnable {

                        int row;

                        WindowMaker(int r) {
                            row = r;
                        }

                @Override
                        public void run() {
                            makeEditVariableWindow(row);
                        }
                    }
                    WindowMaker t = new WindowMaker(r);
                    javax.swing.SwingUtilities.invokeLater(t);
                    break;
                case DELETE_COLUMN:
                    deleteVariablePressed(r);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Table model for Actions in Edit Conditional pane.
     */
    public class ActionTableModel extends AbstractTableModel {

        public static final int DESCRIPTION_COLUMN = 0;

        public static final int EDIT_COLUMN = 1;

        public static final int DELETE_COLUMN = 2;

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == EDIT_COLUMN || c == DELETE_COLUMN) {
                return JButton.class;
            }
            return super.getColumnClass(c);
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public int getRowCount() {
            return _actionList.size();
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            if (c == DESCRIPTION_COLUMN) {
                return false;
            }
            if (_inReorderMode && (c == EDIT_COLUMN || r < _nextInOrder)) {
                return false;
            }
            return true;
        }

        @Override
        public String getColumnName(int col) {
            if (col == DESCRIPTION_COLUMN) {
                return rbx.getString("LabelActionDescription");
            }
            return "";
        }

        public int getPreferredWidth(int col) {
            if (col == DESCRIPTION_COLUMN) {
                return 680;
            }
            return 20;
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (row >= _actionList.size()) {
                return null;
            }
            switch (col) {
                case DESCRIPTION_COLUMN:
                    ConditionalAction action = _actionList.get(row);
                    return action.description(_triggerOnChangeButton.isSelected());
                case EDIT_COLUMN:
                    return Bundle.getMessage("ButtonEdit");
                case DELETE_COLUMN:
                    if (!_inReorderMode) {
                        return Bundle.getMessage("ButtonDelete");
                    } else if (_nextInOrder == 0) {
                        return rbx.getString("ButtonFirst");
                    } else if (_nextInOrder <= row) {
                        return rbx.getString("ButtonNext");
                    }
                    return Integer.toString(row + 1);
            }
            return null;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == EDIT_COLUMN) {
                // Use separate Runnable so window is created on top
                class WindowMaker implements Runnable {

                    int row;

                    WindowMaker(int r) {
                        row = r;
                    }

                    @Override
                    public void run() {
                        makeEditActionWindow(row);
                    }
                }
                WindowMaker t = new WindowMaker(row);
                javax.swing.SwingUtilities.invokeLater(t);
            } else if (col == DELETE_COLUMN) {
                if (_inReorderMode) {
                    swapActions(row);
                } else {
                    deleteActionPressed(row);
                }
            }
        }
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleLogixTable");
    }

    @Override
    protected String getClassName() {
        return LogixTableAction.class.getName();
    }

// *********** Methods for Conditional Browser Window ********************

    /**
     * Responds to the Browse button pressed in Logix table
     * @param sName The selected Logix system name
     */
    void browserPressed(String sName) {
        if (!checkFlags(sName)) {
            return;
        }
        
        // Logix was found, create the window
        _curLogix =  _logixManager.getBySystemName(sName);
        makeBrowserWindow();
    }

 	/**
	 * Builds the text representing the current conditionals for the selected
     * Logix statement
	 */
    void buildConditionalListing() {
        String  showSystemName,
        showUserName,
        showCondName,
        condName,
        operand,
        tStr;
        
        ConditionalVariable variable;
        ConditionalAction action;
        
        numConditionals = _curLogix.getNumConditionals();
        showSystemName  = _curLogix.getSystemName();
        showUserName    = _curLogix.getUserName();
        
        condText.setText(null);
        
        for (int rx = 0; rx < numConditionals; rx++) {
            conditionalRowNumber = rx;
            _curConditional = _conditionalManager.getBySystemName(_curLogix.getConditionalByNumberOrder(rx));
            _variableList = _curConditional.getCopyOfStateVariables();
            _logixSysName = _curConditional.getSystemName();
            _actionList = _curConditional.getCopyOfActions();
            
            showCondName = _curConditional.getUserName();
            if (showCondName == null) {
                showCondName = "";
            }
            showSystemName=_curConditional.getSystemName();

            // If no user name for a conditional, create one using C + row number
            if (showCondName.equals("")) {
                showCondName = "C"+(rx+1);
            }
            condText.append("\n   "+showSystemName+"  "+showCondName+"\n");
            if (_curConditional.getLogicType() == Conditional.MIXED) {
               _antecedent = _curConditional.getAntecedentExpression();
                condText.append("   "+rbx.getString("BrowserAntecedent")+" "+_antecedent+"\n");
            }

            for (int i = 0; i < _variableList.size(); i++) {
                variable = _variableList.get(i);
                tStr = "        ";
                tStr = tStr + " R" + (i+1) + (i>9?"":"  ");  // Makes {Rx}bb or {Rxx}b
                condText.append(tStr);
                
                operand = variable.getOpernString();
                if (i==0) { // add the IF to the first conditional
                    condText.append(rbx.getString("BrowserIF")+" "+operand+" ");
                } else {
                    condText.append(" "+operand+" ");
                }
                if (variable.isNegated()) {
                    condText.append(rbx.getString("LogicNOT")+" ");
                }
                
                condText.append(variable.toString()+"\n");
            } // for _variableList

            if (_actionList.size() > 0) {
                condText.append("              "+rbx.getString("BrowserTHEN")+"\n");
                for (int i=0; i<_actionList.size(); i++) {
                    action = _actionList.get(i);
                    condName=action.description(false);
                    condText.append("                 "+condName+"\n");
                }  // for _actionList
            } else {
                condText.append("            "+rbx.getString("BrowserNoAction")+"\n");
            }
        } // for numConditionals
        condText.setCaretPosition(0);

    }  // buildConditionalListing

    /**
     * creates and initializes the conditionals browser window
     */
    void makeBrowserWindow() {
        condBrowserFrame = new JmriJFrame(rbx.getString("BrowserTitle"));
        condBrowserFrame.addHelpMenu("package.jmri.jmrit.beantable.LogixAddEdit", true);

        Container contentPane = condBrowserFrame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // LOGIX header information
        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout());
        String tStr = rbx.getString("BrowserLogix")+" "+_curLogix.getSystemName()+"    "+
                                     _curLogix.getUserName()+"    "+
                           (Boolean.valueOf(_curLogix.getEnabled()) ?
                                rbx.getString("BrowserEnabled") :
                                rbx.getString("BrowserDisabled"));
        panel1.add(new JLabel(tStr));
        contentPane.add(panel1);

        JScrollPane scrollPane = null;
        condText = new javax.swing.JTextArea(50,50);
        condText.setEditable(false);
        condText.setTabSize(4);

        // Build the conditionals listing
        buildConditionalListing();
        scrollPane = new JScrollPane(condText);
        contentPane.add(scrollPane);

        condBrowserFrame.pack();
        condBrowserFrame.setVisible(true);

    }  // makeBrowserWindow

    private final static Logger log = LoggerFactory.getLogger(LogixTableAction.class.getName());
}
