package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
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
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;
import jmri.Conditional;
import jmri.ConditionalAction;
import jmri.ConditionalManager;
import jmri.ConditionalVariable;
import jmri.InstanceManager;
import jmri.Logix;
import jmri.LogixManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.UserPreferencesManager;
import jmri.jmrit.conditional.ConditionalEditBase;
import jmri.jmrit.conditional.ConditionalListEdit;
import jmri.jmrit.conditional.ConditionalTreeEdit;
import jmri.jmrit.sensorgroup.SensorGroupFrame;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a Logix Table.
 * <p>
 * Also contains the panes to create, edit, and delete a Logix. Conditional
 * editing has been moved to ConditionalListView or CondtionalTreeView.
 * <p>
 * Most of the text used in this GUI is in BeanTableBundle.properties, accessed
 * via Bundle.getMessage().
 * 201803 Moved all keys from LogixTableBundle.properties to
 * BeanTableBundle.properties to simplify i18n.
 * <p>
 * Conditionals now have two policies to trigger execution of their action lists:
 * <ol>
 *     <li>the previous policy - Trigger on change of state only
 *     <li>the new default - Trigger on any enabled state calculation
 * </ol>
 * Jan 15, 2011 - Pete Cressman
 * <p>
 * Two additional action and variable name selection methods have been added:
 * <ol>
 *     <li>Single Pick List
 *     <li>Combo Box Selection
 * </ol>
 * The traditional tabbed Pick List with text entry is the default method.
 * The Options menu has been expanded to list the 3 methods.
 * Mar 27, 2017 - Dave Sand
 * <p>
 * Add a Browse Option to the Logix Select Menu This will display a window that
 * creates a formatted list of the contents of the selected Logix with each
 * Conditional, Variable and Action. The code is courtesy of Chuck Catania and
 * is used with his permission. Apr 2, 2017 - Dave Sand
 *
 * @author Dave Duchamp Copyright (C) 2007
 * @author Pete Cressman Copyright (C) 2009, 2010, 2011
 * @author Matthew Harris copyright (c) 2009
 * @author Dave Sand copyright (c) 2017
 */
public class LogixTableAction extends AbstractTableAction<Logix> {

    /**
     * Create a LogixManager instance.
     *
     * @param s the Action title, not the title of the resulting frame. Perhaps
     *          this should be changed?
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
     * Create a LogixManager instance with default title.
     */
    public LogixTableAction() {
        this(Bundle.getMessage("TitleLogixTable"));
    }

    // ------------ Methods for Logix Table Window ------------

    /**
     * Create the JTable DataModel, along with the changes (overrides of
     * BeanTableDataModel) for the specific case of a Logix table.
     */
    @Override
    protected void createModel() {
        m = new BeanTableDataModel<Logix>() {
            // overlay the state column with the edit column
            static public final int ENABLECOL = VALUECOL;
            static public final int EDITCOL = DELETECOL;
            protected String enabledString = Bundle.getMessage("ColumnHeadEnabled");  // NOI18N

            @Override
            public String getColumnName(int col) {
                if (col == EDITCOL) {
                    return "";  // no heading on "Edit"
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
                    return Bundle.getMessage("ButtonSelect");  // NOI18N
                } else if (col == ENABLECOL) {
                    Logix logix = (Logix) getValueAt(row, SYSNAMECOL);
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
                    String sName = ((Logix) getValueAt(row, SYSNAMECOL)).getSystemName();
                    if (Bundle.getMessage("ButtonEdit").equals(value)) {  // NOI18N
                        editPressed(sName);

                    } else if (Bundle.getMessage("BrowserButton").equals(value)) {  // NOI18N
                        conditionalRowNumber = row;
                        browserPressed(sName);

                    } else if (Bundle.getMessage("ButtonCopy").equals(value)) {  // NOI18N
                        copyPressed(sName);

                    } else if (Bundle.getMessage("ButtonDelete").equals(value)) {  // NOI18N
                        deletePressed(sName);
                    }
                } else if (col == ENABLECOL) {
                    // alternate
                    Logix x = (Logix) getValueAt(row, SYSNAMECOL);
                    boolean v = x.getEnabled();
                    x.setEnabled(!v);
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            /**
             * Delete the bean after all the checking has been done.
             * <p>
             * Deactivates the Logix and remove its conditionals.
             *
             * @param bean of the Logix to delete
             */
            @Override
            void doDelete(Logix bean) {
                bean.deActivateLogix();
                // delete the Logix and all its Conditionals
                _logixManager.deleteLogix(bean);
            }

            @Override
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals(enabledString)) {
                    return true;
                }
                return super.matchPropertyName(e);
            }

            @Override
            public Manager<Logix> getManager() {
                return InstanceManager.getDefault(jmri.LogixManager.class);
            }

            @Override
            public Logix getBySystemName(String name) {
                return InstanceManager.getDefault(jmri.LogixManager.class).getBySystemName(
                        name);
            }

            @Override
            public Logix getByUserName(String name) {
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
             * Replace delete button with comboBox to edit/delete/copy/select
             * Logix.
             *
             * @param table name of the Logix JTable holding the column
             */
            @Override
            protected void configDeleteColumn(JTable table) {
                JComboBox<String> editCombo = new JComboBox<String>();
                editCombo.addItem(Bundle.getMessage("ButtonSelect"));  // NOI18N
                editCombo.addItem(Bundle.getMessage("ButtonEdit"));  // NOI18N
                editCombo.addItem(Bundle.getMessage("BrowserButton"));  // NOI18N
                editCombo.addItem(Bundle.getMessage("ButtonCopy"));  // NOI18N
                editCombo.addItem(Bundle.getMessage("ButtonDelete"));  // NOI18N
                TableColumn col = table.getColumnModel().getColumn(BeanTableDataModel.DELETECOL);
                col.setCellEditor(new DefaultCellEditor(editCombo));
            }

            // Not needed - here for interface compatibility
            @Override
            public void clickOn(Logix t) {
            }

            @Override
            public String getValue(String s) {
                return "";
            }

            @Override
            protected String getBeanType() {
                return Bundle.getMessage("BeanNameLogix");  // NOI18N
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
     * Accounts for the Window and Help menus, which are already added to the
     * menu bar as part of the creation of the JFrame, by adding the new menus 2
     * places earlier unless the table is part of the ListedTableFrame, which
     * adds the Help menu later on.
     *
     * @param f the JFrame of this table
     */
    @Override
    public void setMenuBar(BeanTableFrame f) {
        loadSelectionMode();
        loadEditorMode();

        JMenu menu = new JMenu(Bundle.getMessage("MenuOptions"));  // NOI18N
        menu.setMnemonic(KeyEvent.VK_O);
        javax.swing.JMenuBar menuBar = f.getJMenuBar();
        int pos = menuBar.getMenuCount() - 1;  // count the number of menus to insert the TableMenus before 'Window' and 'Help'
        int offset = 1;
        log.debug("setMenuBar number of menu items = " + pos);  // NOI18N
        for (int i = 0; i <= pos; i++) {
            if (menuBar.getComponent(i) instanceof JMenu) {
                if (((JMenu) menuBar.getComponent(i)).getText().equals(Bundle.getMessage("MenuHelp"))) {  // NOI18N
                    offset = -1;  // correct for use as part of ListedTableAction where the Help Menu is not yet present
                }
            }
        }

        ButtonGroup enableButtonGroup = new ButtonGroup();
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(Bundle.getMessage("EnableAll"));  // NOI18N
        r.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableAll(true);
            }
        });
        enableButtonGroup.add(r);
        r.setSelected(true);
        menu.add(r);

        r = new JRadioButtonMenuItem(Bundle.getMessage("DisableAll"));  // NOI18N
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
        r = new JRadioButtonMenuItem(Bundle.getMessage("UseMultiPick"));  // NOI18N
        r.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setSelectionMode(SelectionMode.USEMULTI);
            }
        });
        modeButtonGroup.add(r);
        menu.add(r);
        r.setSelected(_selectionMode == SelectionMode.USEMULTI);

        r = new JRadioButtonMenuItem(Bundle.getMessage("UseSinglePick"));  // NOI18N
        r.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setSelectionMode(SelectionMode.USESINGLE);
            }
        });
        modeButtonGroup.add(r);
        menu.add(r);
        r.setSelected(_selectionMode == SelectionMode.USESINGLE);

        r = new JRadioButtonMenuItem(Bundle.getMessage("UseComboNameBoxes"));  // NOI18N
        r.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setSelectionMode(SelectionMode.USECOMBO);
            }
        });
        modeButtonGroup.add(r);
        menu.add(r);
        r.setSelected(_selectionMode == SelectionMode.USECOMBO);

        menu.addSeparator();

        ButtonGroup viewButtonGroup = new ButtonGroup();
        r = new JRadioButtonMenuItem(Bundle.getMessage("ListEdit"));  // NOI18N
        r.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setEditorMode(EditMode.LISTEDIT);
            }
        });
        viewButtonGroup.add(r);
        menu.add(r);
        r.setSelected(_editMode == EditMode.LISTEDIT);

        r = new JRadioButtonMenuItem(Bundle.getMessage("TreeEdit"));  // NOI18N
        r.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setEditorMode(EditMode.TREEEDIT);
            }
        });
        viewButtonGroup.add(r);
        menu.add(r);
        r.setSelected(_editMode == EditMode.TREEEDIT);

        menuBar.add(menu, pos + offset);

        menu = new JMenu(Bundle.getMessage("MenuTools"));  // NOI18N
        menu.setMnemonic(KeyEvent.VK_T);

        JMenuItem item = new JMenuItem(Bundle.getMessage("OpenPickListTables"));  // NOI18N
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPickListTable();
            }
        });
        menu.add(item);

        item = new JMenuItem(Bundle.getMessage("FindOrphans"));  // NOI18N
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findOrphansPressed(e);
            }
        });
        menu.add(item);

        item = new JMenuItem(Bundle.getMessage("EmptyConditionals"));  // NOI18N
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findEmptyPressed(e);
            }
        });
        menu.add(item);

        item = new JMenuItem(Bundle.getMessage("CrossReference"));  // NOI18N
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

        item = new JMenuItem(Bundle.getMessage("DisplayWhereUsed"));  // NOI18N
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                makeWhereUsedWindow();
            }
        });
        menu.add(item);

        menuBar.add(menu, pos + offset + 1);  // add this menu to the right of the previous
    }

    /**
     * Get the saved mode selection, default to the tranditional tabbed pick
     * list.
     * <p>
     * During the menu build process, the corresponding menu item is set to
     * selected.
     *
     * @since 4.7.3
     */
    void loadSelectionMode() {
        Object modeName = InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                getProperty(getClassName(), "Selection Mode");      // NOI18N
        if (modeName == null) {
            _selectionMode = SelectionMode.USEMULTI;
        } else {
            String currentMode = (String) modeName;
            switch (currentMode) {
                case "USEMULTI":        // NOI18N
                    _selectionMode = SelectionMode.USEMULTI;
                    break;
                case "USESINGLE":       // NOI18N
                    _selectionMode = SelectionMode.USESINGLE;
                    break;
                case "USECOMBO":        // NOI18N
                    _selectionMode = SelectionMode.USECOMBO;
                    break;
                default:
                    log.warn("Invalid Logix conditional selection mode value, '{}', returned", currentMode);  // NOI18N
                    _selectionMode = SelectionMode.USEMULTI;
            }
        }
    }

    /**
     * Save the mode selection. Called by menu item change events.
     *
     * @since 4.7.3
     * @param newMode The SelectionMode enum constant
     */
    void setSelectionMode(SelectionMode newMode) {
        _selectionMode = newMode;
        InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                setProperty(getClassName(), "Selection Mode", newMode.toString());  // NOI18N
    }

    /**
     * Get the saved mode selection, default to the tranditional conditional
     * list editor.
     * <p>
     * During the menu build process, the corresponding menu item is set to
     * selected.
     *
     * @since 4.9.x
     */
    void loadEditorMode() {
        Object modeName = InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                getProperty(getClassName(), "Edit Mode");      // NOI18N
        if (modeName == null) {
            _editMode = EditMode.LISTEDIT;
        } else {
            String currentMode = (String) modeName;
            switch (currentMode) {
                case "LISTEDIT":        // NOI18N
                    _editMode = EditMode.LISTEDIT;
                    break;
                case "TREEEDIT":       // NOI18N
                    _editMode = EditMode.TREEEDIT;
                    break;
                default:
                    log.warn("Invalid conditional edit mode value, '{}', returned", currentMode);  // NOI18N
                    _editMode = EditMode.LISTEDIT;
            }
        }
    }

    /**
     * Save the view mode selection. Called by menu item change events.
     *
     * @since 4.9.x
     * @param newMode The ViewMode enum constant
     */
    void setEditorMode(EditMode newMode) {
        _editMode = newMode;
        InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                setProperty(getClassName(), "Edit Mode", newMode.toString());  // NOI18N
    }

    /**
     * Open a new Pick List to drag Actions from to form Logix Conditionals.
     */
    void openPickListTable() {
        if (_pickTables == null) {
            _pickTables = new jmri.jmrit.picker.PickFrame(Bundle.getMessage("TitlePickList"));  // NOI18N
        } else {
            _pickTables.setVisible(true);
        }
        _pickTables.toFront();
    }

    /**
     * Find empty Conditional entries, called from menu.
     *
     * @see Maintenance#findEmptyPressed(java.awt.Frame)
     * @param e the event heard
     */
    void findEmptyPressed(ActionEvent e) {
        Maintenance.findEmptyPressed(f);
    }

    /**
     * Find orphaned entries, called from menu.
     *
     * @see Maintenance#findOrphansPressed(java.awt.Frame)
     * @param e the event heard
     */
    void findOrphansPressed(ActionEvent e) {
        Maintenance.findOrphansPressed(f);
    }

    class RefDialog extends JDialog {

        JTextField _devNameField;
        java.awt.Frame _parent;

        RefDialog(java.awt.Frame frame) {
            super(frame, Bundle.getMessage("CrossReference"), true);  // NOI18N
            _parent = frame;
            JPanel extraPanel = new JPanel();
            extraPanel.setLayout(new BoxLayout(extraPanel, BoxLayout.Y_AXIS));
            _devNameField = new JTextField(30);
            JPanel panel = makeEditPanel(_devNameField, "ElementName", "ElementNameHint");  // NOI18N
            JButton referenceButton = new JButton(Bundle.getMessage("ReferenceButton"));  // NOI18N
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
        for (Logix x : _logixManager.getNamedBeanSet()) {
            x.setEnabled(enable);
        }
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.LogixTable";  // NOI18N
    }

    // ------------ variable definitions ------------

    // Multi use variables
    ConditionalManager _conditionalManager = null;  // set when LogixAction is created
    LogixManager _logixManager = null;  // set when LogixAction is created

    ConditionalListEdit _listEdit = null;
    ConditionalTreeEdit _treeEdit = null;

    boolean _showReminder = false;
    jmri.jmrit.picker.PickFrame _pickTables;

    // Current focus variables
    Logix _curLogix = null;
    int conditionalRowNumber = 0;

    // Add Logix Variables
    JmriJFrame addLogixFrame = null;
    JTextField _systemName = new JTextField(20);
    JTextField _addUserName = new JTextField(20);
    JCheckBox _autoSystemName = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));   // NOI18N
    JLabel _sysNameLabel = new JLabel(Bundle.getMessage("BeanNameLogix") + " " + Bundle.getMessage("ColumnSystemName") + ":");  // NOI18N
    JLabel _userNameLabel = new JLabel(Bundle.getMessage("BeanNameLogix") + " " + Bundle.getMessage("ColumnUserName") + ":");   // NOI18N
    String systemNameAuto = this.getClass().getName() + ".AutoSystemName";      // NOI18N
    JButton create;

    // Edit Logix Variables
    private boolean _inEditMode = false;
    private boolean _inCopyMode = false;

    /**
     * Input selection names.
     *
     * @since 4.7.3
     */
    public enum SelectionMode {
        /**
         * Use the traditional text field, with the tabbed Pick List available
         * for drag-n-drop
         */
        USEMULTI,
        /**
         * Use the traditional text field, but with a single Pick List that
         * responds with a click
         */
        USESINGLE,
        /**
         * Use combo boxes to select names instead of a text field.
         */
        USECOMBO;
    }
    SelectionMode _selectionMode;

    /**
     * Conditional edit view mode
     *
     * @since 4.9.x
     */
    public enum EditMode {
        /**
         * Use the traditional table list mode for editing conditionals
         */
        LISTEDIT,
        /**
         * Use the tree based mode for editing condtiionals
         */
        TREEEDIT;
    }
    EditMode _editMode;

    // Save conditional reference target names before updating
    private TreeSet<String> _saveTargetNames = new TreeSet<String>();
    private HashMap<String, ArrayList<String>> _saveTargetList = new HashMap<>();

    // ------------ Methods for Add Logix Window ------------

    /**
     * Respond to the Add button in Logix table Creates and/or initialize the
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
            JPanel panel5 = makeAddLogixFrame("TitleAddLogix", "AddLogixMessage");  // NOI18N
            // Create Logix
            create = new JButton(Bundle.getMessage("ButtonCreate"));  // NOI18N
            panel5.add(create);
            create.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            });
            create.setToolTipText(Bundle.getMessage("LogixCreateButtonHint"));  // NOI18N
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
     * @param titleId   property key to fetch as title of the frame (using Bundle)
     * @param messageId part 1 of property key to fetch as user instruction on
     *                  pane, either 1 or 2 is added to form the whole key
     * @return the button JPanel
     */
    JPanel makeAddLogixFrame(String titleId, String messageId) {
        addLogixFrame = new JmriJFrame(Bundle.getMessage(titleId));
        addLogixFrame.addHelpMenu(
                "package.jmri.jmrit.beantable.LogixAddEdit", true);     // NOI18N
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
        _addUserName.setToolTipText(Bundle.getMessage("LogixUserNameHint"));    // NOI18N
        _systemName.setToolTipText(Bundle.getMessage("LogixSystemNameHint"));   // NOI18N
        contentPane.add(p);
        // set up message
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        JPanel panel31 = new JPanel();
        panel31.setLayout(new FlowLayout());
        JLabel message1 = new JLabel(Bundle.getMessage(messageId + "1"));  // NOI18N
        panel31.add(message1);
        JPanel panel32 = new JPanel();
        JLabel message2 = new JLabel(Bundle.getMessage(messageId + "2"));  // NOI18N
        panel32.add(message2);
        panel3.add(panel31);
        panel3.add(panel32);
        contentPane.add(panel3);

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        // Cancel
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        panel5.add(cancel);
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelAddPressed(e);
            }
        });
        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N

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
     * Enable/disable fields for data entry when user selects to have system
     * name automatically generated.
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
     *
     * @param e The event heard
     */
    void cancelAddPressed(ActionEvent e) {
        addLogixFrame.setVisible(false);
        addLogixFrame.dispose();
        addLogixFrame = null;
        _inCopyMode = false;
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
                JPanel panel5 = makeAddLogixFrame("TitleCopyLogix", "CopyLogixMessage");    // NOI18N
                // Create Logix
                JButton create = new JButton(Bundle.getMessage("ButtonCopy"));  // NOI18N
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
        log.debug("copyPressed started for {}", sName);  // NOI18N
        javax.swing.SwingUtilities.invokeLater(t);
        _inCopyMode = true;
        _logixSysName = sName;
    }

    String _logixSysName;

    /**
     * Copy the Logix as configured in the Copy set up pane.
     *
     * @param e the event heard
     */
    void copyLogixPressed(ActionEvent e) {
        String uName = _addUserName.getText();
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
            String sName = _systemName.getText();
            // check if a Logix with this name already exists
            boolean createLogix = true;
            targetLogix = _logixManager.getBySystemName(sName);
            if (targetLogix != null) {
                int result = JOptionPane.showConfirmDialog(f,
                        Bundle.getMessage("ConfirmLogixDuplicate", sName, _logixSysName), // NOI18N
                        Bundle.getMessage("QuestionTitle"), JOptionPane.YES_NO_OPTION,    // NOI18N
                        JOptionPane.QUESTION_MESSAGE);
                if (JOptionPane.NO_OPTION == result) {
                    return;
                }
                createLogix = false;
                String userName = targetLogix.getUserName();
                if (userName != null && userName.length() > 0) {
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
                    log.error("Failure to create Logix with System Name: {}", sName);  // NOI18N
                    return;
                }
            } else if (targetLogix == null) {
                log.error("Error targetLogix is null!");  // NOI18N
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
     * @param cSysName    system name of the Conditional
     * @param srcLogix    original Logix containing the Conditional
     * @param targetLogix target Logix to copy to
     */
    void copyConditionalToLogix(String cSysName, Logix srcLogix, Logix targetLogix) {
        Conditional cOld = _conditionalManager.getBySystemName(cSysName);
        if (cOld == null) {
            log.error("Failure to find Conditional with System Name: {}", cSysName);  // NOI18N
            return;
        }
        String cOldSysName = cOld.getSystemName();
        String cOldUserName = cOld.getUserName();

        // make system name for new conditional
        int num = targetLogix.getNumConditionals() + 1;
        String cNewSysName = targetLogix.getSystemName() + "C" + Integer.toString(num);
        // add to Logix at the end of the calculate order
        String cNewUserName = Bundle.getMessage("CopyOf", cOldUserName); // NOI18N
        if (cOldUserName != null && cOldUserName.length() == 0) {
            cNewUserName += "C" + Integer.toString(num);
        }
        do {
            cNewUserName = JOptionPane.showInputDialog(f,
                    Bundle.getMessage("NameConditionalCopy", cOldUserName, cOldSysName, _logixSysName,
                            targetLogix.getUserName(), targetLogix.getSystemName()),
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
            log.error("Failure to create Conditional with System Name: \"{}\" and User Name: \"{}\"", cNewSysName, cNewUserName);  // NOI18N
            return;
        }
        cNew.setLogicType(cOld.getLogicType(), cOld.getAntecedentExpression());
        cNew.setStateVariables(cOld.getCopyOfStateVariables());
        cNew.setAction(cOld.getCopyOfActions());
        targetLogix.addConditional(cNewSysName, -1);

        // Update where used with the copy results
        _saveTargetNames.clear();
        TreeSet<String> newTargetNames = new TreeSet<String>();
        loadReferenceNames(cOld.getCopyOfStateVariables(), newTargetNames);
        updateWhereUsed(newTargetNames, cNewSysName);
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
                JOptionPane.showMessageDialog(addLogixFrame,
                        Bundle.getMessage("LogixError3"), Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    /**
     * Check validity of Logix system name.
     * <p>
     * Fixes name if it doesn't start with "IX".
     *
     * @return false if name has length &lt; 1 after displaying a dialog
     */
    boolean checkLogixSysName() {
        String sName = _systemName.getText();
        if ((sName.length() < 1)) {
            // Entered system name is blank or too short
            JOptionPane.showMessageDialog(addLogixFrame,
                    Bundle.getMessage("LogixError8"), Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if ((sName.length() < 2) || (sName.charAt(0) != 'I')
                || (sName.charAt(1) != 'X')) {
            // System name does not begin with IX, prefix IX to it
            String s = sName;
            sName = "IX" + s;  // NOI18N
        }
        _systemName.setText(sName);
        return true;
    }

    /**
     * Check if another Logix editing session is currently open or no system
     * name is provided.
     *
     * @param sName system name of Logix to be copied
     * @return true if a new session may be started
     */
    boolean checkFlags(String sName) {
        if (_inEditMode) {
            // Already editing a Logix, ask for completion of that edit
            JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("LogixError32", _curLogix.getSystemName()),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            if (_treeEdit != null) {
                _treeEdit.bringToFront();
            } else if (_listEdit != null) {
                _listEdit.bringToFront();
            }
            return false;
        }

        if (_inCopyMode) {
            // Already editing a Logix, ask for completion of that edit
            JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("LogixError31", _logixSysName),
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (sName != null) {
            // check if a Logix with this name exists
            Logix x = _logixManager.getBySystemName(sName);
            if (x == null) {
                // Logix does not exist, so cannot be edited
                log.error("No Logix with system name: " + sName);
                JOptionPane.showMessageDialog(null,
                        Bundle.getMessage("LogixError5"),
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
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
        String sName = "";
        String uName = _addUserName.getText();
        if (uName.length() == 0) {
            uName = null;
        }
        if (_autoSystemName.isSelected()) {
            if (!checkLogixUserName(uName)) {
                return;
            }
            _curLogix = _logixManager.createNewLogix(uName);
            sName = _curLogix.getSystemName();
        } else {
            if (!checkLogixSysName()) {
                return;
            }
            // Get validated system name
            sName = _systemName.getText();
            // check if a Logix with this name already exists
            Logix x = null;
            try {
                x = _logixManager.getBySystemName(sName);
            } catch (Exception ex) {
                // user input no good
                handleCreateException(sName);
                return;  // without creating
            }
            if (x != null) {
                // Logix already exists
                JOptionPane.showMessageDialog(addLogixFrame, Bundle.getMessage("LogixError1"),
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!checkLogixUserName(uName)) {
                return;
            }
            // Create the new Logix
            _curLogix = _logixManager.createNewLogix(sName, uName);
            if (_curLogix == null) {
                // should never get here unless there is an assignment conflict
                log.error("Failure to create Logix with System Name: {}", sName);  // NOI18N
                return;
            }
        }
        cancelAddPressed(null);
        // create the Edit Logix Window
        editPressed(sName);
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
            prefMgr.setSimplePreferenceState(systemNameAuto, _autoSystemName.isSelected());
        });
    }

    void handleCreateException(String sysName) {
        JOptionPane.showMessageDialog(addLogixFrame,
                Bundle.getMessage("ErrorLogixAddFailed", sysName), // NOI18N
                Bundle.getMessage("ErrorTitle"), // NOI18N
                JOptionPane.ERROR_MESSAGE);
    }

    // ------------ Methods for Edit Logix Pane ------------

    /**
     * Respond to the Edit button pressed in Logix table.
     *
     * @param sName system name of Logix to be edited
     */
    void editPressed(String sName) {
        _curLogix = _logixManager.getBySystemName(sName);
        if (!checkFlags(sName)) {
            return;
        }

        if (sName.equals(SensorGroupFrame.logixSysName)) {
            // Sensor group message
            JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("LogixWarn8", SensorGroupFrame.logixUserName, SensorGroupFrame.logixSysName),
                    Bundle.getMessage("WarningTitle"), // NOI18N
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create a new conditional edit view, add the listener.
        if (_editMode == EditMode.TREEEDIT) {
            _treeEdit = new ConditionalTreeEdit(sName);
            _inEditMode = true;
            _treeEdit.addLogixEventListener(new ConditionalEditBase.LogixEventListener() {
                @Override
                public void logixEventOccurred() {
                    String lgxName = sName;
                    _treeEdit.logixData.forEach((key, value) -> {
                        if (key.equals("Finish")) {                  // NOI18N
                            _treeEdit = null;
                            _inEditMode = false;
                            _curLogix.activateLogix();
                            f.setVisible(true);
                        } else if (key.equals("Delete")) {           // NOI18N
                            deletePressed(value);
                        } else if (key.equals("chgUname")) {         // NOI18N
                            Logix x = _logixManager.getBySystemName(lgxName);
                            x.setUserName(value);
                            m.fireTableDataChanged();
                        }
                    });
                }
            });
        } else {
            _listEdit = new ConditionalListEdit(sName);
            _inEditMode = true;
            _listEdit.addLogixEventListener(new ConditionalEditBase.LogixEventListener() {
                @Override
                public void logixEventOccurred() {
                    String lgxName = sName;
                    _listEdit.logixData.forEach((key, value) -> {
                        if (key.equals("Finish")) {                  // NOI18N
                            _listEdit = null;
                            _inEditMode = false;
                            _curLogix.activateLogix();
                            f.setVisible(true);
                        } else if (key.equals("Delete")) {           // NOI18N
                            deletePressed(value);
                        } else if (key.equals("chgUname")) {         // NOI18N
                            Logix x = _logixManager.getBySystemName(lgxName);
                            x.setUserName(value);
                            m.fireTableDataChanged();
                        }
                    });
                }
            });
        }
    }

    /**
     * Display reminder to save.
     */
    void showSaveReminder() {
        if (_showReminder) {
            if (InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class) != null) {
                InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showInfoMessage(Bundle.getMessage("ReminderTitle"), Bundle.getMessage("ReminderSaveString", Bundle.getMessage("MenuItemLogixTable")), // NOI18N
                                getClassName(),
                                "remindSaveLogix");  // NOI18N
            }
        }
    }

    @Override
    public void setMessagePreferencesDetails() {
        HashMap<Integer, String> options = new HashMap< Integer, String>(3);
        options.put(0x00, Bundle.getMessage("DeleteAsk"));      // NOI18N
        options.put(0x01, Bundle.getMessage("DeleteNever"));    // NOI18N
        options.put(0x02, Bundle.getMessage("DeleteAlways"));   // NOI18N
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).setMessageItemDetails(getClassName(), "delete", Bundle.getMessage("DeleteLogix"), options, 0x00);  // NOI18N
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).setPreferenceItemDetails(getClassName(), "remindSaveLogix", Bundle.getMessage("HideSaveReminder"));  // NOI18N
        super.setMessagePreferencesDetails();
    }

    /**
     * Respond to the Delete combo selection Logix window or conditional view
     * delete request.
     *
     * @param sName system name of bean to be deleted
     */
    void deletePressed(String sName) {
        if (!checkConditionalReferences(sName)) {
            return;
        }
        final Logix x = _logixManager.getBySystemName(sName);
        final jmri.UserPreferencesManager p;
        p = jmri.InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class);
        if (p != null && p.getMultipleChoiceOption(getClassName(), "delete") == 0x02) {     // NOI18N
            if (x != null) {
                _logixManager.deleteLogix(x);
                deleteSourceWhereUsed();
            }
        } else {
            final JDialog dialog = new JDialog();
            String msg;
            dialog.setTitle(Bundle.getMessage("QuestionTitle"));     // NOI18N
            dialog.setLocationRelativeTo(null);
            dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
            JPanel container = new JPanel();
            container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            msg = Bundle.getMessage("ConfirmLogixDelete", sName);    // NOI18N
            JLabel question = new JLabel(msg);
            question.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.add(question);

            final JCheckBox remember = new JCheckBox(Bundle.getMessage("MessageRememberSetting"));  // NOI18N
            remember.setFont(remember.getFont().deriveFont(10f));
            remember.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton yesButton = new JButton(Bundle.getMessage("ButtonYes"));    // NOI18N
            JButton noButton = new JButton(Bundle.getMessage("ButtonNo"));      // NOI18N
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
                        p.setMultipleChoiceOption(getClassName(), "delete", 0x02);  // NOI18N
                    }
                    if (x != null) {
                        _logixManager.deleteLogix(x);
                        deleteSourceWhereUsed();
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

        f.setVisible(true);
    }

    /**
     * Build a tree set from conditional references.
     *
     * @since 4.7.4
     * @param varList The ConditionalVariable list that might contain
     *                conditional references
     * @param treeSet A tree set to be built from the varList data
     */
    void loadReferenceNames(List<ConditionalVariable> varList, TreeSet<String> treeSet) {
        treeSet.clear();
        for (ConditionalVariable var : varList) {
            if (var.getType() == Conditional.Type.CONDITIONAL_TRUE
                    || var.getType() == Conditional.Type.CONDITIONAL_FALSE) {
                treeSet.add(var.getName());
            }
        }
    }

    boolean checkConditionalUserName(String uName, Logix logix) {
        if ((uName != null) && (!(uName.equals("")))) {
            Conditional p = _conditionalManager.getByUserName(logix, uName);
            if (p != null) {
                // Conditional with this user name already exists
                log.error("Failure to update Conditional with Duplicate User Name: " // NOI18N
                        + uName);
                JOptionPane.showMessageDialog(
                        null, Bundle.getMessage("LogixError10"), // NOI18N
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
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

    /**
     * Check for conditional references.
     *
     * @since 4.7.4
     * @param logixName The Logix under consideration
     * @return true if no references
     */
    boolean checkConditionalReferences(String logixName) {
        _saveTargetList.clear();
        Logix x = _logixManager.getLogix(logixName);
        int numConditionals = x.getNumConditionals();
        if (numConditionals > 0) {
            for (int i = 0; i < numConditionals; i++) {
                String csName = x.getConditionalByNumberOrder(i);

                // If the conditional is a where used source, retain it for later
                ArrayList<String> targetList = InstanceManager.getDefault(jmri.ConditionalManager.class).getTargetList(csName);
                if (targetList.size() > 0) {
                    _saveTargetList.put(csName, targetList);
                }

                // If the conditional is a where used target, check scope
                ArrayList<String> refList = InstanceManager.getDefault(jmri.ConditionalManager.class).getWhereUsed(csName);
                if (refList != null) {
                    for (String refName : refList) {
                        Logix xRef = _conditionalManager.getParentLogix(refName);
                        String xsName = xRef.getSystemName();
                        if (logixName.equals(xsName)) {
                            // Member of the same Logix
                            continue;
                        }

                        // External references have to be removed before the Logix can be deleted.
                        Conditional c = x.getConditional(csName);
                        Conditional cRef = xRef.getConditional(refName);
                        JOptionPane.showMessageDialog(null,
                                Bundle.getMessage("LogixError11", c.getUserName(), c.getSystemName(), cRef.getUserName(),
                                        cRef.getSystemName(), xRef.getUserName(), xRef.getSystemName()), // NOI18N
                                Bundle.getMessage("ErrorTitle"),
                                JOptionPane.ERROR_MESSAGE);  // NOI18N
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Remove target/source where used entries after a Logix delete.
     *
     * @since 4.7.4
     */
    void deleteSourceWhereUsed() {
        _saveTargetList.forEach((refName, targetList) -> {
            for (String targetName : targetList) {
                InstanceManager.getDefault(jmri.ConditionalManager.class).removeWhereUsed(targetName, refName);
            }
        });
    }

    /**
     * Update the conditional reference where used.
     * <p>
     * The difference between the saved target names and new target names is
     * used to add/remove where used references.
     *
     * @since 4.7.4
     * @param newTargetNames The conditional target names after updating
     * @param refName        The system name for the referencing conditional
     */
    void updateWhereUsed(TreeSet<String> newTargetNames, String refName) {
        TreeSet<String> deleteNames = new TreeSet<>(_saveTargetNames);
        deleteNames.removeAll(newTargetNames);
        for (String deleteName : deleteNames) {
            InstanceManager.getDefault(jmri.ConditionalManager.class).removeWhereUsed(deleteName, refName);
        }

        TreeSet<String> addNames = new TreeSet<>(newTargetNames);
        addNames.removeAll(_saveTargetNames);
        for (String addName : addNames) {
            InstanceManager.getDefault(jmri.ConditionalManager.class).addWhereUsed(addName, refName);
        }
    }

    /**
     * Create Variable and Action editing pane center part.
     *
     * @param comp  Field or comboBox to include on sub pane
     * @param label property key for label
     * @param hint  property key for tooltip for this sub pane
     * @return JPanel containing interface
     */
    JPanel makeEditPanel(JComponent comp, String label, String hint) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.add(new JLabel(Bundle.getMessage(label)));
        panel.add(p);
        if (hint != null) {
            panel.setToolTipText(Bundle.getMessage(hint));
        }
        comp.setMaximumSize(comp.getPreferredSize());  // override for text fields
        panel.add(comp);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    /**
     * Format time to hh:mm given integer hour and minute.
     *
     * @param hour   value for time hours
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

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleLogixTable");        // NOI18N
    }

    @Override
    protected String getClassName() {
        return LogixTableAction.class.getName();
    }

    // ------------ Methods for Conditional References Window ------------
    /**
     * Builds the conditional references window when the Conditional Variable
     * References menu item is selected.
     * <p>
     * This is a stand-alone window that can be closed at any time.
     *
     * @since 4.7.4
     */
    void makeWhereUsedWindow() {

        JmriJFrame referenceListFrame = new JmriJFrame(Bundle.getMessage("LabelRefTitle"), false, true);    // NOI18N
        Container contentPane = referenceListFrame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // build header information
        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel1.add(new JLabel(Bundle.getMessage("LabelRefTarget")));    // NOI18N
        panel1.add(new JLabel(Bundle.getMessage("LabelRefSource")));    // NOI18N
        contentPane.add(panel1);

        // Build the conditional references listing
        JScrollPane scrollPane = null;
        JTextArea textContent = buildWhereUsedListing();
        scrollPane = new JScrollPane(textContent);
        contentPane.add(scrollPane);

        referenceListFrame.pack();
        referenceListFrame.setVisible(true);
    }

    /**
     * Creates a component containing the conditional reference where used list.
     * The source is {@link jmri.ConditionalManager#getWhereUsedMap()}
     *
     * @return a TextArea, empty if reference is not used
     * @since 4.7.4
     */
    JTextArea buildWhereUsedListing() {
        JTextArea condText = new javax.swing.JTextArea();
        condText.setText(null);
        HashMap<String, ArrayList<String>> whereUsed = InstanceManager.getDefault(ConditionalManager.class).getWhereUsedMap();
        SortedSet<String> targets = new TreeSet<>(whereUsed.keySet());
        targets.forEach((target) -> {
            condText.append("\n" + target + "\t" + getWhereUsedName(target) + "  \n");
            ArrayList<String> refNames = whereUsed.get(target);
            refNames.forEach((refName) -> {
                condText.append("\t\t" + refName + "\t" + getWhereUsedName(refName) + "  \n");
            });
        });
        condText.setCaretPosition(0);
        condText.setTabSize(2);
        condText.setEditable(false);
        return condText;
    }

    String getWhereUsedName(String cName) {
        return _conditionalManager.getBySystemName(cName).getUserName();
    }

// ------------ Methods for Conditional Browser Window ------------
    /**
     * Respond to the Browse button pressed in Logix table.
     *
     * @param sName The selected Logix system name
     */
    void browserPressed(String sName) {
        // Logix was found, create the window
        _curLogix = _logixManager.getBySystemName(sName);
        makeBrowserWindow();
    }

    /**
     * Create and initialize the conditionals browser window.
     */
    void makeBrowserWindow() {
        JmriJFrame condBrowserFrame = new JmriJFrame(Bundle.getMessage("BrowserTitle"), false, true);   // NOI18N
        condBrowserFrame.addHelpMenu("package.jmri.jmrit.beantable.LogixAddEdit", true);            // NOI18N

        Container contentPane = condBrowserFrame.getContentPane();
        contentPane.setLayout(new BorderLayout());

        // LOGIX header information
        JPanel topPanel = new JPanel();
        String tStr = Bundle.getMessage("BrowserLogix") + " " + _curLogix.getSystemName() + "    " // NOI18N
                + _curLogix.getUserName() + "    "
                + (Boolean.valueOf(_curLogix.getEnabled())
                        ? Bundle.getMessage("BrowserEnabled") // NOI18N
                        : Bundle.getMessage("BrowserDisabled"));  // NOI18N
        topPanel.add(new JLabel(tStr));
        contentPane.add(topPanel, BorderLayout.NORTH);

        // Build the conditionals listing
        JScrollPane scrollPane = null;
        JTextArea textContent = buildConditionalListing();
        scrollPane = new JScrollPane(textContent);
        contentPane.add(scrollPane);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        JButton helpBrowse = new JButton(Bundle.getMessage("MenuHelp"));   // NOI18N
        bottomPanel.add(helpBrowse, BorderLayout.WEST);
        helpBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(condBrowserFrame,
                        Bundle.getMessage("BrowserHelpText"),   // NOI18N
                        Bundle.getMessage("BrowserHelpTitle"),  // NOI18N
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        JButton saveBrowse = new JButton(Bundle.getMessage("BrowserSaveButton"));   // NOI18N
        saveBrowse.setToolTipText(Bundle.getMessage("BrowserSaveButtonHint"));      // NOI18N
        bottomPanel.add(saveBrowse, BorderLayout.EAST);
        saveBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveBrowserPressed();
            }
        });
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        condBrowserFrame.pack();
        condBrowserFrame.setVisible(true);
    }  // makeBrowserWindow

    JFileChooser userFileChooser = new JFileChooser(FileUtil.getUserFilesPath());

    /**
     * Save the Logix browser window content to a text file.
     */
    void saveBrowserPressed() {
        userFileChooser.setApproveButtonText(Bundle.getMessage("BrowserSaveDialogApprove"));  // NOI18N
        userFileChooser.setDialogTitle(Bundle.getMessage("BrowserSaveDialogTitle"));  // NOI18N
        userFileChooser.rescanCurrentDirectory();
        // Default to logix system name.txt
        userFileChooser.setSelectedFile(new File(_curLogix.getSystemName() + ".txt"));  // NOI18N
        int retVal = userFileChooser.showSaveDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            log.debug("Save browser content stopped, no file selected");  // NOI18N
            return;  // give up if no file selected or cancel pressed
        }
        File file = userFileChooser.getSelectedFile();
        log.debug("Save browser content to '{}'", file);  // NOI18N

        if (file.exists()) {
            Object[] options = {Bundle.getMessage("BrowserSaveDuplicateReplace"),  // NOI18N
                    Bundle.getMessage("BrowserSaveDuplicateAppend"),  // NOI18N
                    Bundle.getMessage("ButtonCancel")};               // NOI18N
            int selectedOption = JOptionPane.showOptionDialog(null,
                    Bundle.getMessage("BrowserSaveDuplicatePrompt", file.getName()), // NOI18N
                    Bundle.getMessage("BrowserSaveDuplicateTitle"),   // NOI18N
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            if (selectedOption == 2 || selectedOption == -1) {
                log.debug("Save browser content stopped, file replace/append cancelled");  // NOI18N
                return;  // Cancel selected or dialog box closed
            }
            if (selectedOption == 0) {
                FileUtil.delete(file);  // Replace selected
            }
        }

        // Create the file content
        String tStr = Bundle.getMessage("BrowserLogix") + " " + _curLogix.getSystemName() + "    "  // NOI18N
                + _curLogix.getUserName() + "    "
                + (Boolean.valueOf(_curLogix.getEnabled())
                        ? Bundle.getMessage("BrowserEnabled")    // NOI18N
                        : Bundle.getMessage("BrowserDisabled"));  // NOI18N
        JTextArea textContent = buildConditionalListing();
        try {
            // ADD Logix Header inforation first
            FileUtil.appendTextToFile(file, tStr);
            FileUtil.appendTextToFile(file, textContent.getText());
        } catch (IOException e) {
            log.error("Unable to write browser content to '{}', exception: '{}'", file, e);  // NOI18N
        }
    }

    /**
     * Builds a Component representing the current conditionals for the selected
     * Logix statement.
     *
     * @return a TextArea listing existing conditionals; will be empty if there
     *         are none
     */
    JTextArea buildConditionalListing() {
        String showSystemName,
                showCondName,
                condName,
                operand,
                tStr;

        List<ConditionalVariable> variableList;
        List<ConditionalAction> actionList;
        ConditionalVariable variable;
        ConditionalAction action;
        String _antecedent = null;

        JTextArea condText = new javax.swing.JTextArea();
        condText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        condText.setText(null);
        int numConditionals = _curLogix.getNumConditionals();
        for (int rx = 0; rx < numConditionals; rx++) {
            conditionalRowNumber = rx;
            Conditional curConditional = _conditionalManager.getBySystemName(_curLogix.getConditionalByNumberOrder(rx));
            variableList = curConditional.getCopyOfStateVariables();
            _logixSysName = curConditional.getSystemName();
            actionList = curConditional.getCopyOfActions();

            showCondName = curConditional.getUserName();
            if (showCondName == null) {
                showCondName = "";
            }
            showSystemName = curConditional.getSystemName();

            // If no user name for a conditional, create one using C + row number
            if (showCondName.equals("")) {
                showCondName = "C" + (rx + 1);
            }
            condText.append("\n  " + showSystemName + "  " + showCondName + "   \n");
            if (curConditional.getLogicType() == Conditional.AntecedentOperator.MIXED) {
                _antecedent = curConditional.getAntecedentExpression();
                String antecedent = ConditionalEditBase.translateAntecedent(_antecedent, false);
                condText.append("   " + Bundle.getMessage("LogixAntecedent") + " " + antecedent + "  \n");   // NOI18N
            }

            for (int i = 0; i < variableList.size(); i++) {
                variable = variableList.get(i);
                String varTrigger = (variable.doTriggerActions())
                        ? "[x]" // NOI18N
                        : "[ ]";
                tStr = "    " + varTrigger + " ";
                tStr = tStr + " R" + (i + 1) + (i > 8 ? " " : "  ");  // Makes {Rx}bb or {Rxx}b
                condText.append(tStr);

                operand = variable.getOpernString();
                if (i == 0) { // add the IF to the first conditional
                    condText.append(Bundle.getMessage("BrowserIF") + " " + operand + " ");    // NOI18N
                } else {
                    condText.append("  " + operand + " ");
                }
                if (variable.isNegated()) {
                    condText.append(Bundle.getMessage("LogicNOT") + " ");     // NOI18N
                }
                condText.append(variable.toString() + "   \n");
            } // for _variableList

            if (actionList.size() > 0) {
                condText.append("             " + Bundle.getMessage("BrowserTHEN") + "   \n");  // NOI18N
                boolean triggerType = curConditional.getTriggerOnChange();
                for (int i = 0; i < actionList.size(); i++) {
                    action = actionList.get(i);
                    condName = action.description(triggerType);
                    condText.append("               " + condName + "   \n");
                }  // for _actionList
            } else {
                condText.append("             " + Bundle.getMessage("BrowserNoAction") + "   \n\n");    // NOI18N
            }
        } // for numConditionals

        condText.setCaretPosition(0);
        condText.setTabSize(4);
        condText.setEditable(false);
        return condText;
    }  // buildConditionalListing

    private final static Logger log = LoggerFactory.getLogger(LogixTableAction.class);

}
