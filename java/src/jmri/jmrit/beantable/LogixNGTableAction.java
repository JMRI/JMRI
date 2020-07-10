package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
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
import jmri.InstanceManager;
import jmri.Manager;
import jmri.UserPreferencesManager;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.tools.swing.LogixNGEditor;

/**
 * Swing action to create and register a LogixNG Table.
 * <p>
 Also contains the panes to create, edit, and delete a LogixNG.
 <p>
 * Most of the text used in this GUI is in BeanTableBundle.properties, accessed
 * via Bundle.getMessage().
 * <p>
 * Two additional action and variable name selection methods have been added:
 * <ol>
 *     <li>Single Pick List
 *     <li>Combo Box Selection
 * </ol>
 * The traditional tabbed Pick List with text entry is the default method.
 * The Options menu has been expanded to list the 3 methods.
 * Mar 27, 2017 - Dave Sand
 *
 * @author Dave Duchamp Copyright (C) 2007 (LogixTableAction)
 * @author Pete Cressman Copyright (C) 2009, 2010, 2011 (LogixTableAction)
 * @author Matthew Harris copyright (c) 2009 (LogixTableAction)
 * @author Dave Sand copyright (c) 2017 (LogixTableAction)
 * @author Daniel Bergqvist copyright (c) 2019
 */
public class LogixNGTableAction extends AbstractTableAction<LogixNG> {

    private static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.logixng.LogixNGBundle");

    /**
     * Create a LogixNG_Manager instance.
     *
     * @param s the Action title, not the title of the resulting frame. Perhaps
     *          this should be changed?
     */
    public LogixNGTableAction(String s) {
        super(s);
        // set up managers - no need to use InstanceManager since both managers are
        // Default only (internal). We use InstanceManager to get managers for
        // compatibility with other facilities.
        _logixNG_Manager = InstanceManager.getNullableDefault(LogixNG_Manager.class);
        // disable ourself if there is no LogixNG manager
        if (_logixNG_Manager == null) {
            setEnabled(false);
        }
    }

    /**
     * Create a LogixNG_Manager instance with default title.
     */
    public LogixNGTableAction() {
        this(Bundle.getMessage("TitleLogixNGTable"));
    }

    // ------------ Methods for LogixNG Table Window ------------

    /**
     * Create the JTable DataModel, along with the changes (overrides of
     * BeanTableDataModel) for the specific case of a LogixNG table.
     */
    @Override
    protected void createModel() {
        m = new BeanTableDataModel<LogixNG>() {
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
                    LogixNG logixNG = (LogixNG) getValueAt(row, SYSNAMECOL);
                    if (logixNG == null) {
                        return null;
                    }
                    return logixNG.isEnabled();
                } else {
                    return super.getValueAt(row, col);
                }
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col == EDITCOL) {
                    // set up to edit
                    String sName = ((LogixNG) getValueAt(row, SYSNAMECOL)).getSystemName();
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
                    LogixNG x = (LogixNG) getValueAt(row, SYSNAMECOL);
                    boolean v = x.isEnabled();
                    x.setEnabled(!v);
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            /**
             * Delete the bean after all the checking has been done.
             * <p>
             * Deletes the LogixNG.
             *
             * @param bean of the LogixNG to delete
             */
            @Override
            void doDelete(LogixNG bean) {
                bean.setEnabled(false);
                // delete the LogixNG
                _logixNG_Manager.deleteLogixNG(bean);
            }

            @Override
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals(enabledString)) {
                    return true;
                }
                return super.matchPropertyName(e);
            }

            @Override
            public Manager<LogixNG> getManager() {
                return InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class);
            }

            @Override
            public LogixNG getBySystemName(String name) {
                return InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class).getBySystemName(
                        name);
            }

            @Override
            public LogixNG getByUserName(String name) {
                return InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class).getByUserName(
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
             * Replace delete button with comboBox to edit/delete/copy/select LogixNG.
             *
             * @param table name of the LogixNG JTable holding the column
             */
            @Override
            protected void configDeleteColumn(JTable table) {
                JComboBox<String> editCombo = new JComboBox<>();
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
            public void clickOn(LogixNG t) {
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
     * Set title of LogixNG table.
     */
    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleLogixNGTable"));
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
        JRadioButtonMenuItem r = new JRadioButtonMenuItem(Bundle.getMessage("EnableAllLogixNGs"));  // NOI18N
        r.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableAll(true);
            }
        });
        enableButtonGroup.add(r);
        r.setSelected(true);
        menu.add(r);

        r = new JRadioButtonMenuItem(Bundle.getMessage("DisableAllLogixNGs"));  // NOI18N
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
                    log.warn("Invalid LogixNG selection mode value, '{}', returned", currentMode);  // NOI18N
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
     * Get the saved mode selection, default to the tree editor.
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
            _editMode = EditMode.TREEEDIT;
        } else {
            String currentMode = (String) modeName;
            switch (currentMode) {
                case "TREEEDIT":       // NOI18N
                    _editMode = EditMode.TREEEDIT;
                    break;
                default:
                    log.warn("Invalid edit mode value, '{}', returned", currentMode);  // NOI18N
                    _editMode = EditMode.TREEEDIT;
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
     * Open a new Pick List to drag Actions from to form LogixNG.
     */
    private void openPickListTable() {
        if (_pickTables == null) {
            _pickTables = new jmri.jmrit.picker.PickFrame(Bundle.getMessage("TitlePickList"));  // NOI18N
        } else {
            _pickTables.setVisible(true);
        }
        _pickTables.toFront();
    }

    void enableAll(boolean enable) {
        for (LogixNG x : _logixNG_Manager.getNamedBeanSet()) {
            x.setEnabled(enable);
        }
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.LogixNGTable";  // NOI18N
    }

    // ------------ variable definitions ------------

    LogixNG_Manager _logixNG_Manager = null;  // set when LogixNGAction is created

    LogixNGEditor _logixNGEdit = null;
//    ConditionalNGEditor _treeEdit = null;

    boolean _showReminder = false;
    jmri.jmrit.picker.PickFrame _pickTables;

    // Current focus variables
    LogixNG _curLogixNG = null;
    int conditionalRowNumber = 0;

    // Add LogixNG Variables
    JmriJFrame addLogixNGFrame = null;
    JTextField _systemName = new JTextField(20);
    JTextField _addUserName = new JTextField(20);
    JCheckBox _autoSystemName = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));   // NOI18N
    JLabel _sysNameLabel = new JLabel(rbx.getString("BeanNameLogixNG") + " " + Bundle.getMessage("ColumnSystemName") + ":");  // NOI18N
    JLabel _userNameLabel = new JLabel(rbx.getString("BeanNameLogixNG") + " " + Bundle.getMessage("ColumnUserName") + ":");   // NOI18N
    String systemNameAuto = this.getClass().getName() + ".AutoSystemName";      // NOI18N
    JButton create;

    // Edit LogixNG Variables
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
     * LogixNG edit view mode
     */
    public enum EditMode {
        /**
         * Use the tree based mode for editing LogixNG
         */
        TREEEDIT;
    }
    EditMode _editMode;

    // ------------ Methods for Add LogixNG Window ------------

    /**
     * Respond to the Add button in LogixNG table Creates and/or initialize
     * the Add LogixNG pane.
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
        // make an Add LogixNG Frame
        if (addLogixNGFrame == null) {
            JPanel panel5 = makeAddLogixNGFrame("TitleAddLogixNG", "AddLogixNGMessage");  // NOI18N
            // Create LogixNG
            create = new JButton(Bundle.getMessage("ButtonCreate"));  // NOI18N
            panel5.add(create);
            create.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            });
            create.setToolTipText(Bundle.getMessage("LogixNGCreateButtonHint"));  // NOI18N
        }
        addLogixNGFrame.pack();
        addLogixNGFrame.setVisible(true);
        _autoSystemName.setSelected(false);
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
            _autoSystemName.setSelected(prefMgr.getSimplePreferenceState(systemNameAuto));
        });
    }

    /**
     * Create or copy LogixNG frame.
     *
     * @param titleId   property key to fetch as title of the frame (using Bundle)
     * @param messageId part 1 of property key to fetch as user instruction on
     *                  pane, either 1 or 2 is added to form the whole key
     * @return the button JPanel
     */
    JPanel makeAddLogixNGFrame(String titleId, String messageId) {
        addLogixNGFrame = new JmriJFrame(Bundle.getMessage(titleId));
        addLogixNGFrame.addHelpMenu(
                "package.jmri.jmrit.beantable.LogixNGAddEdit", true);     // NOI18N
        addLogixNGFrame.setLocation(50, 30);
        Container contentPane = addLogixNGFrame.getContentPane();
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
        _sysNameLabel.setLabelFor(_systemName);
        c.gridy = 1;
        p.add(_userNameLabel, c);
        _userNameLabel.setLabelFor(_addUserName);
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
        _addUserName.setToolTipText(Bundle.getMessage("LogixNGUserNameHint"));    // NOI18N
        _systemName.setToolTipText(Bundle.getMessage("LogixNGSystemNameHint"));   // NOI18N
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

        addLogixNGFrame.addWindowListener(new java.awt.event.WindowAdapter() {
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
     * Respond to the Cancel button in Add LogixNG window.
     * <p>
 Note: Also get there if the user closes the Add LogixNG window.
     *
     * @param e The event heard
     */
    void cancelAddPressed(ActionEvent e) {
        addLogixNGFrame.setVisible(false);
        addLogixNGFrame.dispose();
        addLogixNGFrame = null;
        _inCopyMode = false;
        if (f != null) {
            f.setVisible(true);
        }
    }

    /**
     * Respond to the Copy LogixNG button in Add LogixNG window.
     * <p>
     * Provides a pane to set new properties of the copy.
     *
     * @param sName system name of LogixNG to be copied
     */
    void copyPressed(String sName) {
        if (!checkFlags(sName)) {
            return;
        }

        Runnable t = new Runnable() {
            @Override
            public void run() {
                JPanel panel5 = makeAddLogixNGFrame("TitleCopyLogixNG", "CopyLogixNGMessage");    // NOI18N
                // Create LogixNG
                JButton create = new JButton(Bundle.getMessage("ButtonCopy"));  // NOI18N
                panel5.add(create);
                create.addActionListener((ActionEvent e) -> {
                    JOptionPane.showMessageDialog(null, "Copy is not implemented yet.", "Error", JOptionPane.ERROR_MESSAGE);
//                    copyLogixNGPressed(e);
                });
                addLogixNGFrame.pack();
                addLogixNGFrame.setVisible(true);
                _autoSystemName.setSelected(false);
                InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
                    _autoSystemName.setSelected(prefMgr.getSimplePreferenceState(systemNameAuto));
                });
            }
        };
        log.debug("copyPressed started for {}", sName);  // NOI18N
        javax.swing.SwingUtilities.invokeLater(t);
        _inCopyMode = true;
        _logixNGSysName = sName;
    }

    String _logixNGSysName;

    /**
     * Copy the LogixNG as configured in the Copy set up pane.
     *
     * @param e the event heard
     */
    void copyLogixNGPressed(ActionEvent e) {
/*        
        String uName = _addUserName.getText().trim();
        if (uName.length() == 0) {
            uName = null;
        }
        LogixNG targetLogixNG;
        if (_autoSystemName.isSelected()) {
            if (!checkLogixNGUserName(uName)) {
                return;
            }
            targetLogixNG = _logixNG_Manager.createLogixNG(uName);
        } else {
            if (!checkLogixNGSysName()) {
                return;
            }
            String sName = _systemName.getText().trim();
            // check if a LogixNG with this name already exists
            boolean createLogix = true;
            targetLogixNG = _logixNG_Manager.getBySystemName(sName);
            if (targetLogixNG != null) {
                int result = JOptionPane.showConfirmDialog(f,
                        Bundle.getMessage("ConfirmLogixDuplicate", sName, _logixNGSysName), // NOI18N
                        Bundle.getMessage("QuestionTitle"), JOptionPane.YES_NO_OPTION,    // NOI18N
                        JOptionPane.QUESTION_MESSAGE);
                if (JOptionPane.NO_OPTION == result) {
                    return;
                }
                createLogix = false;
                String userName = targetLogixNG.getUserName();
                if (userName != null && userName.length() > 0) {
                    _addUserName.setText(userName);
                    uName = userName;
                }
            } else if (!checkLogixNGUserName(uName)) {
                return;
            }
            if (createLogix) {
                // Create the new LogixNG
                targetLogixNG = _logixNG_Manager.createLogixNG(sName, uName);
                if (targetLogixNG == null) {
                    // should never get here unless there is an assignment conflict
                    log.error("Failure to create LogixNG with System Name: {}", sName);  // NOI18N
                    return;
                }
            } else if (targetLogixNG == null) {
                log.error("Error targetLogix is null!");  // NOI18N
                return;
            } else {
                targetLogixNG.setUserName(uName);
            }
        }
        LogixNG srcLogic = _logixNG_Manager.getBySystemName(_logixNGSysName);
        for (int i = 0; i < srcLogic.getNumConditionals(); i++) {
            String cSysName = srcLogic.getConditionalByNumberOrder(i);
            copyConditionalToLogix(cSysName, srcLogic, targetLogixNG);
        }
        cancelAddPressed(null);
*/
    }

    /**
     * Check and warn if a string is already in use as the user name of a LogixNG.
     *
     * @param uName the suggested name
     * @return true if not in use
     */
    boolean checkLogixNGUserName(String uName) {
        // check if a LogixNG with the same user name exists
        if (uName != null && uName.trim().length() > 0) {
            LogixNG x = _logixNG_Manager.getByUserName(uName);
            if (x != null) {
                // LogixNG with this user name already exists
                JOptionPane.showMessageDialog(addLogixNGFrame,
                        Bundle.getMessage("LogixNGError3"), Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    /**
     * Check validity of LogixNG system name.
     * <p>
     * Fixes name if it doesn't start with "IQ".
     *
     * @return false if name has length &lt; 1 after displaying a dialog
     */
    boolean checkLogixNGSysName() {
        String sName = _systemName.getText();
        if ((sName.length() < 1)) {
            // Entered system name is blank or too short
            JOptionPane.showMessageDialog(addLogixNGFrame,
                    Bundle.getMessage("LogixNGError8"), Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if ((sName.length() < 2) || (sName.charAt(0) != 'I')
                || (sName.charAt(1) != 'Q')) {
            // System name does not begin with IQ:, prefix IQ: to it
            String s = sName;
            sName = "IQ" + s;  // NOI18N
        }
        _systemName.setText(sName);
        return true;
    }

    /**
     * Check if another LogixNG editing session is currently open or no system
     * name is provided.
     *
     * @param sName system name of LogixNG to be copied
     * @return true if a new session may be started
     */
    boolean checkFlags(String sName) {
        if (_inEditMode) {
            // Already editing a LogixNG, ask for completion of that edit
            JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("LogixNGError32", _curLogixNG.getSystemName()),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            if (_logixNGEdit != null) {
//                _logixNGEdit.toFront();
                _logixNGEdit.bringToFront();
            }
            return false;
        }

        if (_inCopyMode) {
            // Already editing a LogixNG, ask for completion of that edit
            JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("LogixNGError31", _logixNGSysName),
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (sName != null) {
            // check if a LogixNG with this name exists
            LogixNG x = _logixNG_Manager.getBySystemName(sName);
            if (x == null) {
                // LogixNG does not exist, so cannot be edited
                log.error("No LogixNG with system name: " + sName);
                JOptionPane.showMessageDialog(null,
                        Bundle.getMessage("LogixNGError5"),
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    /**
     * Respond to the Create LogixNG button in Add LogixNG window.
     *
     * @param e The event heard
     */
    void createPressed(ActionEvent e) {
        // possible change
        _showReminder = true;
        String sName = "";
        String uName = _addUserName.getText().trim();
        if (uName.length() == 0) {
            uName = null;
        }
        if (_autoSystemName.isSelected()) {
            if (!checkLogixNGUserName(uName)) {
                return;
            }
            _curLogixNG = _logixNG_Manager.createLogixNG(uName);
            sName = _curLogixNG.getSystemName();
        } else {
            if (!checkLogixNGSysName()) {
                return;
            }
            // Get validated system name
            sName = _systemName.getText();
            // check if a LogixNG with this name already exists
            LogixNG x = null;
            try {
                x = _logixNG_Manager.getBySystemName(sName);
            } catch (Exception ex) {
                // user input no good
                handleCreateException(sName);
                return;  // without creating
            }
            if (x != null) {
                // LogixNG already exists
                JOptionPane.showMessageDialog(addLogixNGFrame, Bundle.getMessage("LogixNGError1"),
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!checkLogixNGUserName(uName)) {
                return;
            }
            // Create the new LogixNG
            _curLogixNG = _logixNG_Manager.createLogixNG(sName, uName);
            if (_curLogixNG == null) {
                // should never get here unless there is an assignment conflict
                log.error("Failure to create LogixNG with System Name: {}", sName);  // NOI18N
                return;
            }
        }
        cancelAddPressed(null);
        // create the Edit LogixNG Window
        editPressed(sName);
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
            prefMgr.setSimplePreferenceState(systemNameAuto, _autoSystemName.isSelected());
        });
    }

    void handleCreateException(String sysName) {
        JOptionPane.showMessageDialog(addLogixNGFrame,
                Bundle.getMessage("ErrorLogixAddFailed", sysName), // NOI18N
                Bundle.getMessage("ErrorTitle"), // NOI18N
                JOptionPane.ERROR_MESSAGE);
    }

    // ------------ Methods for Edit LogixNG Pane ------------

    /**
     * Respond to the Edit button pressed in LogixNG table.
     *
     * @param sName system name of LogixNG to be edited
     */
    void editPressed(String sName) {
        _curLogixNG = _logixNG_Manager.getBySystemName(sName);
        if (!checkFlags(sName)) {
            return;
        }

        // Create a new LogixNG edit view, add the listener.
        if (_editMode == EditMode.TREEEDIT) {
            _logixNGEdit = new LogixNGEditor(f, m, sName);
            _inEditMode = true;
            _logixNGEdit.addLogixNGEventListener(new LogixNGEditor.LogixNGEventListener() {
                @Override
                public void logixNGEventOccurred() {
                    String lgxName = sName;
                    _logixNGEdit.logixData.forEach((key, value) -> {
                        if (key.equals("Finish")) {                  // NOI18N
                            _logixNGEdit = null;
                            _inEditMode = false;
                            _curLogixNG.activateLogixNG();
                            f.setVisible(true);
                        } else if (key.equals("Delete")) {           // NOI18N
                            deletePressed(value);
                        } else if (key.equals("chgUname")) {         // NOI18N
                            LogixNG x = _logixNG_Manager.getBySystemName(lgxName);
                            if (x == null) {
                                log.error("Found no logixNG for name {} when changing user name (2)", lgxName);
                                return;
                            }
                            x.setUserName(value);
                            m.fireTableDataChanged();
                        }
                    });
                }
            });
/*
            ConditionalNG conditionalNG = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName(sName);
            _treeEdit = new ConditionalNGEditor(conditionalNG);
            _treeEdit.initComponents();
            _treeEdit.setVisible(true);
            _inEditMode = true;
            
            _treeEdit.addLogixNGEventListener(new ConditionalNGEditor.LogixNGEventListener() {
                @Override
                public void logixNGEventOccurred() {
                    String lgxName = sName;
                    _treeEdit.logixNGData.forEach((key, value) -> {
                        if (key.equals("Finish")) {                  // NOI18N
                            _treeEdit = null;
                            _inEditMode = false;
                            _curLogixNG.setEnabled(true);
                            f.setVisible(true);
                        } else if (key.equals("Delete")) {           // NOI18N
                            deletePressed(value);
                        } else if (key.equals("chgUname")) {         // NOI18N
                            LogixNG x = _logixNG_Manager.getBySystemName(lgxName);
                            x.setUserName(value);
                            m.fireTableDataChanged();
                        }
                    });
                }
            });
*/
        }
    }

    /**
     * Display reminder to save.
     */
    void showSaveReminder() {
        if (_showReminder) {
            if (InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class) != null) {
                InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showInfoMessage(Bundle.getMessage("ReminderTitle"), Bundle.getMessage("ReminderSaveString", Bundle.getMessage("MenuItemLogixNGTable")), // NOI18N
                                getClassName(),
                                "remindSaveLogix");  // NOI18N
            }
        }
    }

    @Override
    public void setMessagePreferencesDetails() {
        HashMap<Integer, String> options = new HashMap<>(3);
        options.put(0x00, Bundle.getMessage("DeleteAsk"));      // NOI18N
        options.put(0x01, Bundle.getMessage("DeleteNever"));    // NOI18N
        options.put(0x02, Bundle.getMessage("DeleteAlways"));   // NOI18N
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).setMessageItemDetails(getClassName(), "delete", Bundle.getMessage("DeleteLogixNG"), options, 0x00);  // NOI18N
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).setPreferenceItemDetails(getClassName(), "remindSaveLogixNG", Bundle.getMessage("HideSaveReminder"));  // NOI18N
        super.setMessagePreferencesDetails();
    }

    /**
     * Respond to the Delete combo selection LogixNG window delete request.
     *
     * @param sName system name of bean to be deleted
     */
    void deletePressed(String sName) {
        if (!checkFlags(sName)) {
            return;
        }
        
        final LogixNG x = _logixNG_Manager.getBySystemName(sName);
        final jmri.UserPreferencesManager p;
        p = jmri.InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class);
        if (p != null && p.getMultipleChoiceOption(getClassName(), "delete") == 0x02) {     // NOI18N
            if (x != null) {
                _logixNG_Manager.deleteLogixNG(x);
//                deleteSourceWhereUsed();
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
                        _logixNG_Manager.deleteLogixNG(x);
//                        deleteSourceWhereUsed();
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
        return Bundle.getMessage("TitleLogixNGTable");        // NOI18N
    }

    @Override
    protected String getClassName() {
        return LogixNGTableAction.class.getName();
    }

// ------------ Methods for Conditional Browser Window ------------
    /**
     * Respond to the Browse button pressed in LogixNG table.
     *
     * @param sName The selected LogixNG system name
     */
    void browserPressed(String sName) {
        // LogixNG was found, create the window
        _curLogixNG = _logixNG_Manager.getBySystemName(sName);
        makeBrowserWindow();
    }

    /**
     * Create and initialize the conditionalNGs browser window.
     */
    void makeBrowserWindow() {
        JmriJFrame condBrowserFrame = new JmriJFrame(Bundle.getMessage("BrowserTitle"), false, true);   // NOI18N
        condBrowserFrame.addHelpMenu("package.jmri.jmrit.beantable.LogixAddEdit", true);            // NOI18N

        Container contentPane = condBrowserFrame.getContentPane();
        contentPane.setLayout(new BorderLayout());

        // LogixNG header information
        JPanel topPanel = new JPanel();
        String tStr = Bundle.getMessage("BrowserLogixNG") + " " + _curLogixNG.getSystemName() + "    " // NOI18N
                + _curLogixNG.getUserName() + "    "
                + (_curLogixNG.isEnabled()
                        ? Bundle.getMessage("BrowserEnabled") // NOI18N
                        : Bundle.getMessage("BrowserDisabled"));  // NOI18N
        topPanel.add(new JLabel(tStr));
        contentPane.add(topPanel, BorderLayout.NORTH);

        // Build the conditionalNGs listing
        StringWriter writer = new StringWriter();
        _curLogixNG.printTree(new PrintWriter(writer), "    ");
        JTextArea textContent = new JTextArea(writer.toString());
        JScrollPane scrollPane = new JScrollPane(textContent);
        contentPane.add(scrollPane);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        JButton helpBrowse = new JButton(Bundle.getMessage("MenuHelp"));   // NOI18N
        bottomPanel.add(helpBrowse, BorderLayout.WEST);
        helpBrowse.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(condBrowserFrame,
                    Bundle.getMessage("BrowserHelpText"),   // NOI18N
                    Bundle.getMessage("BrowserHelpTitle"),  // NOI18N
                    JOptionPane.INFORMATION_MESSAGE);
        });
        JButton saveBrowse = new JButton(Bundle.getMessage("BrowserSaveButton"));   // NOI18N
        saveBrowse.setToolTipText(Bundle.getMessage("BrowserSaveButtonHint"));      // NOI18N
        bottomPanel.add(saveBrowse, BorderLayout.EAST);
        saveBrowse.addActionListener((ActionEvent e) -> {
            saveBrowserPressed();
        });
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        condBrowserFrame.pack();
        condBrowserFrame.setVisible(true);
    }  // makeBrowserWindow

    JFileChooser userFileChooser = new JFileChooser(FileUtil.getUserFilesPath());

    /**
     * Save the LogixNG browser window content to a text file.
     */
    void saveBrowserPressed() {
        userFileChooser.setApproveButtonText(Bundle.getMessage("BrowserSaveDialogApprove"));  // NOI18N
        userFileChooser.setDialogTitle(Bundle.getMessage("BrowserSaveDialogTitle"));  // NOI18N
        userFileChooser.rescanCurrentDirectory();
        // Default to logixNG system name.txt
        userFileChooser.setSelectedFile(new File(_curLogixNG.getSystemName() + ".txt"));  // NOI18N
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
        String tStr = Bundle.getMessage("BrowserLogixNG") + " " + _curLogixNG.getSystemName() + "    "  // NOI18N
                + _curLogixNG.getUserName() + "    "
                + (_curLogixNG.isEnabled()
                        ? Bundle.getMessage("BrowserEnabled")    // NOI18N
                        : Bundle.getMessage("BrowserDisabled"));  // NOI18N
//        JTextArea textContent = buildConditionalListing();
        JTextArea textContent = new JTextArea();
        try {
            // ADD LogixNG Header inforation first
            FileUtil.appendTextToFile(file, tStr);
            FileUtil.appendTextToFile(file, textContent.getText());
        } catch (IOException e) {
            log.error("Unable to write browser content to '{}', exception: '{}'", file, e);  // NOI18N
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LogixNGTableAction.class);

}
