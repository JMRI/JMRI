package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import javax.annotation.Nonnull;
import javax.swing.*;
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
import jmri.NamedBean;
import jmri.UserPreferencesManager;
import jmri.jmrit.logixng.tools.swing.AbstractLogixNGEditor;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;

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
 * @author Daniel Bergqvist copyright (c) 2019 (AbstractLogixNGTableEditor)
 * 
 * @param <E> the type of NamedBean supported by this model
 */
public abstract class AbstractLogixNGTableAction<E extends NamedBean> extends AbstractTableAction<E> {

    
    private static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.logixng.LogixNGBundle");
    
    JTextArea _textContent;
    
    /**
     * Create a AbstractLogixNGTableAction instance.
     *
     * @param s the Action title, not the title of the resulting frame. Perhaps
     *          this should be changed?
     */
    public AbstractLogixNGTableAction(String s) {
        super(s);
        super.setEnabled(false);
    }

    protected abstract AbstractLogixNGEditor<E> getEditor(BeanTableFrame<E> f, BeanTableDataModel<E> m, String sName);
    
    @Nonnull
    @Override
    protected abstract Manager<E> getManager();
    
    protected abstract void enableAll(boolean enable);
    
    protected abstract void setEnabled(E bean, boolean enable);
    
    protected abstract boolean isEnabled(E bean);
    
    protected abstract E createBean(String userName);
    
    protected abstract E createBean(String systemName, String userName);
    
    protected abstract void deleteBean(E bean);
    
    protected abstract String getBeanText(E bean);
    
    protected JPanel getSettingsPanel() {
        return null;
    }

    // ------------ Methods for LogixNG Table Window ------------

    /**
     * Create the JTable DataModel, along with the changes (overrides of
     * BeanTableDataModel) for the specific case of a LogixNG table.
     */
    @Override
    protected void createModel() {
        m = new BeanTableDataModel<E>() {
            // overlay the state column with the edit column
            static public final int ENABLECOL = VALUECOL;
            static public final int EDITCOL = DELETECOL;
            protected String enabledString = Bundle.getMessage("ColumnHeadEnabled");  // NOI18N

            @Override
            public String getColumnName(int col) {
                if (col == EDITCOL) {
                    return Bundle.getMessage("ColumnHeadMenu");     // This makes it easier to test the table
//                    return "";  // no heading on "Edit"
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

            @SuppressWarnings("unchecked")  // Unchecked cast from Object to E
            @Override
            public Object getValueAt(int row, int col) {
                if (col == EDITCOL) {
                    return Bundle.getMessage("ButtonSelect");  // NOI18N
                } else if (col == ENABLECOL) {
                    E x = (E) getValueAt(row, SYSNAMECOL);
                    if (x == null) {
                        return null;
                    }
                    return isEnabled(x);
                } else {
                    return super.getValueAt(row, col);
                }
            }

            @SuppressWarnings("unchecked")  // Unchecked cast from Object to E
            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col == EDITCOL) {
                    // set up to edit
                    String sName = ((NamedBean) getValueAt(row, SYSNAMECOL)).getSystemName();
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
                    E x = (E) getValueAt(row, SYSNAMECOL);
                    boolean v = isEnabled(x);
                    setEnabled(x, !v);
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            /**
             * Delete the bean after all the checking has been done.
             * <p>
             * Deletes the NamedBean.
             *
             * @param bean of the NamedBean to delete
             */
            @Override
            protected void doDelete(E bean) {
                // delete the LogixNG
                AbstractLogixNGTableAction.this.deleteBean(bean);
            }

            @Override
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals(enabledString)) {
                    return true;
                }
                return super.matchPropertyName(e);
            }

            @Override
            public Manager<E> getManager() {
                return AbstractLogixNGTableAction.this.getManager();
//                return InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class);
            }

            @Override
            public E getBySystemName(String name) {
                return AbstractLogixNGTableAction.this.getManager().getBySystemName(name);
//                return InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class).getBySystemName(
//                        name);
            }

            @Override
            public E getByUserName(String name) {
                return AbstractLogixNGTableAction.this.getManager().getByUserName(name);
//                return InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class).getByUserName(
//                        name);
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
             * Replace delete button with comboBox to edit/delete/copy/select NamedBean.
             *
             * @param table name of the NamedBean JTable holding the column
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
            public void clickOn(NamedBean t) {
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
     * Set title of NamedBean table.
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
        r.addActionListener((ActionEvent e) -> {
            enableAll(true);
        });
        enableButtonGroup.add(r);
        r.setSelected(true);
        menu.add(r);

        r = new JRadioButtonMenuItem(Bundle.getMessage("DisableAllLogixNGs"));  // NOI18N
        r.addActionListener((ActionEvent e) -> {
            enableAll(false);
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
        item.addActionListener((ActionEvent e) -> {
            openPickListTable();
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
                    log.warn("Invalid NamedBean selection mode value, '{}', returned", currentMode);  // NOI18N
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
     * Open a new Pick List to drag Actions from to form NamedBean.
     */
    private void openPickListTable() {
        if (_pickTables == null) {
            _pickTables = new jmri.jmrit.picker.PickFrame(Bundle.getMessage("TitlePickList"));  // NOI18N
        } else {
            _pickTables.setVisible(true);
        }
        _pickTables.toFront();
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.LogixNGTable";  // NOI18N
    }

    // ------------ variable definitions ------------

//    LogixNG_Manager _logixNG_Manager = null;  // set when LogixNGAction is created

    protected AbstractLogixNGEditor<E> _editor = null;
//    ConditionalNGEditor _treeEdit = null;

    boolean _showReminder = false;
    jmri.jmrit.picker.PickFrame _pickTables;

    // Current focus variables
    protected E _curNamedBean = null;
    int conditionalRowNumber = 0;

    // Add E Variables
    JmriJFrame addLogixNGFrame = null;
    JTextField _systemName = new JTextField(20);
    JTextField _addUserName = new JTextField(20);
    JCheckBox _autoSystemName = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));   // NOI18N
    JLabel _sysNameLabel = new JLabel(rbx.getString("BeanNameLogixNG") + " " + Bundle.getMessage("ColumnSystemName") + ":");  // NOI18N
    JLabel _userNameLabel = new JLabel(rbx.getString("BeanNameLogixNG") + " " + Bundle.getMessage("ColumnUserName") + ":");   // NOI18N
    String systemNameAuto = this.getClass().getName() + ".AutoSystemName";       // NOI18N
    JButton create;

    // Edit E Variables
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
     * Edit view mode
     */
    public enum EditMode {
        /**
         * Use the tree based mode for editing bean
         */
        TREEEDIT;
    }
    EditMode _editMode;

    // ------------ Methods for Add bean Window ------------

    /**
     * Respond to the Add button in bean table Creates and/or initialize
     * the Add bean pane.
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
        // make an Add bean Frame
        if (addLogixNGFrame == null) {
            JPanel panel5 = makeAddFrame("TitleAddLogixNG", "Add");  // NOI18N
            // Create bean
            create = new JButton(Bundle.getMessage("ButtonCreate"));  // NOI18N
            panel5.add(create);
            create.addActionListener(this::createPressed);
            create.setToolTipText(Bundle.getMessage("LogixNGCreateButtonHint"));  // NOI18N
        }
        addLogixNGFrame.pack();
        addLogixNGFrame.setVisible(true);
        _autoSystemName.setSelected(false);
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
            _autoSystemName.setSelected(prefMgr.getCheckboxPreferenceState(systemNameAuto, true));
        });
    }

    protected abstract JPanel makeAddFrame(String titleId, String startMessageId);

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
     * Respond to the Cancel button in Add bean window.
     * <p>
 Note: Also get there if the user closes the Add bean window.
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
     * Respond to the Copy bean button in Add bean window.
     * <p>
     * Provides a pane to set new properties of the copy.
     *
     * @param sName system name of bean to be copied
     */
    void copyPressed(String sName) {
        if (!checkFlags(sName)) {
            return;
        }

        Runnable t = new Runnable() {
            @Override
            public void run() {
                log.error("Copy LogixNG is not implemented yet");
                
                // This may or may not work. It's not tested yet.
                // Disable for now.
                if (1==0) {
                    JPanel panel5 = makeAddFrame("TitleCopyLogixNG", "Copy");    // NOI18N
                    // Create bean
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
                        _autoSystemName.setSelected(prefMgr.getCheckboxPreferenceState(systemNameAuto, true));
                    });
                }
            }
        };
        log.debug("copyPressed started for {}", sName);  // NOI18N
        javax.swing.SwingUtilities.invokeLater(t);
        _inCopyMode = true;
        _logixNGSysName = sName;
    }

    String _logixNGSysName;

    /**
     * Copy the bean as configured in the Copy set up pane.
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
        // check if a bean with the same user name exists
        if (uName != null && uName.trim().length() > 0) {
            E x = getManager().getByUserName(uName);
            if (x != null) {
                // A bean with this user name already exists
                JOptionPane.showMessageDialog(addLogixNGFrame,
                        Bundle.getMessage("LogixNGError3"), Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    /**
     * Check validity of bean system name.
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
     * Check if another bean editing session is currently open or no system
     * name is provided.
     *
     * @param sName system name of bean to be copied
     * @return true if a new session may be started
     */
    boolean checkFlags(String sName) {
        if (_inEditMode) {
            // Already editing a bean, ask for completion of that edit
            JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("LogixNGError32", _curNamedBean.getSystemName()),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            if (_editor != null) {
//                _logixNGEdit.toFront();
                _editor.bringToFront();
            }
            return false;
        }

        if (_inCopyMode) {
            // Already editing a bean, ask for completion of that edit
            JOptionPane.showMessageDialog(null,
                    Bundle.getMessage("LogixNGError31", _logixNGSysName),
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (sName != null) {
            // check if a bean with this name exists
            E x = getManager().getBySystemName(sName);
            if (x == null) {
                // bean does not exist, so cannot be edited
                log.error("No bean with system name: " + sName);
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
     * Respond to the Create bean button in Add bean window.
     *
     * @param e The event heard
     */
    void createPressed(ActionEvent e) {
        // possible change
        _showReminder = true;
        String sName;
        String uName = _addUserName.getText().trim();
        if (uName.length() == 0) {
            uName = null;
        }
        if (_autoSystemName.isSelected()) {
            if (!checkLogixNGUserName(uName)) {
                return;
            }
            _curNamedBean = createBean(uName);
            sName = _curNamedBean.getSystemName();
        } else {
            if (!checkLogixNGSysName()) {
                return;
            }
            // Get validated system name
            sName = _systemName.getText();
            // check if a bean with this name already exists
            E x = null;
            try {
                x = getManager().getBySystemName(sName);
            } catch (Exception ex) {
                // user input no good
                handleCreateException(sName);
                return;  // without creating
            }
            if (x != null) {
                // bean already exists
                JOptionPane.showMessageDialog(addLogixNGFrame, Bundle.getMessage("LogixNGError1"),
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!checkLogixNGUserName(uName)) {
                return;
            }
            // Create the new bean
            _curNamedBean = createBean(sName, uName);
            if (_curNamedBean == null) {
                // should never get here unless there is an assignment conflict
                log.error("Failure to create bean with System Name: {}", sName);  // NOI18N
                return;
            }
        }
        cancelAddPressed(null);
        // create the Edit bean Window
        editPressed(sName);
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
            prefMgr.setCheckboxPreferenceState(systemNameAuto, _autoSystemName.isSelected());
        });
    }

    void handleCreateException(String sysName) {
        JOptionPane.showMessageDialog(addLogixNGFrame,
                Bundle.getMessage("ErrorLogixAddFailed", sysName), // NOI18N
                Bundle.getMessage("ErrorTitle"), // NOI18N
                JOptionPane.ERROR_MESSAGE);
    }

    // ------------ Methods for Edit bean Pane ------------

    /**
     * Respond to the Edit button pressed in LogixNG table.
     *
     * @param sName system name of LogixNG to be edited
     */
    void editPressed(String sName) {
        _curNamedBean = getManager().getBySystemName(sName);
        if (!checkFlags(sName)) {
            return;
        }

        // Create a new bean edit view, add the listener.
        if (_editMode == EditMode.TREEEDIT) {
            _editor = getEditor(f, m, sName);
            
            if (_editor == null) return;    // Editor not implemented yet for LogixNG Tables
            
            _inEditMode = true;
            
            _editor.addEditorEventListener((data) -> {
                String lgxName = sName;
                data.forEach((key, value) -> {
                    if (key.equals("Finish")) {                  // NOI18N
                        _editor = null;
                        _inEditMode = false;
                        
//                        _curLogixNG.setEnabled(true);
//                        _curLogixNG.getConditionalNG(0).setEnabled(true);
//                        log.warn("zxc Enable LogixNG: {}", _curLogixNG.getSystemName());
//                        _curLogixNG.activateLogixNG();
                        f.setVisible(true);
                    } else if (key.equals("Delete")) {           // NOI18N
                        deletePressed(value);
                    } else if (key.equals("chgUname")) {         // NOI18N
                        E x = getManager().getBySystemName(lgxName);
                        if (x == null) {
                            log.error("Found no logixNG for name {} when changing user name (2)", lgxName);
                            return;
                        }
                        x.setUserName(value);
                        m.fireTableDataChanged();
                    }
                });
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
     * Respond to the Delete combo selection bean window delete request.
     *
     * @param sName system name of bean to be deleted
     */
    void deletePressed(String sName) {
        if (!checkFlags(sName)) {
            return;
        }
        
        final E x = getManager().getBySystemName(sName);
        final jmri.UserPreferencesManager p;
        p = jmri.InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class);
        if (p != null && p.getMultipleChoiceOption(getClassName(), "delete") == 0x02) {     // NOI18N
            if (x != null) {
                deleteBean(x);
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

            noButton.addActionListener((ActionEvent e) -> {
                //there is no point in remebering this the user will never be
                //able to delete a bean!
                /*if(remember.isSelected()){
                setDisplayDeleteMsg(0x01);
                }*/
                dialog.dispose();
            });

            yesButton.addActionListener((ActionEvent e) -> {
                if (p != null && remember.isSelected()) {
                    p.setMultipleChoiceOption(getClassName(), "delete", 0x02);  // NOI18N
                }
                if (x != null) {
                    deleteBean(x);
//                        deleteSourceWhereUsed();
                }
                dialog.dispose();
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
        return AbstractLogixNGTableAction.class.getName();
    }

// ------------ Methods for Conditional Browser Window ------------
    /**
     * Respond to the Browse button pressed in bean table.
     *
     * @param sName The selected bean system name
     */
    void browserPressed(String sName) {
        // bean was found, create the window
        _curNamedBean = getManager().getBySystemName(sName);
        makeBrowserWindow();
    }

    /**
     * Update text in the browser window.
     */
    void updateBrowserText() {
        if (_textContent != null) {
            _textContent.setText(getBeanText(_curNamedBean));
        }
    }
    
    /**
     * Create and initialize the conditionalNGs browser window.
     */
    void makeBrowserWindow() {
        JmriJFrame condBrowserFrame = new JmriJFrame(Bundle.getMessage("BrowserTitle"), false, true);   // NOI18N
        condBrowserFrame.addHelpMenu("package.jmri.jmrit.beantable.LogixAddEdit", true);            // NOI18N
        
        condBrowserFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                _textContent = null;
            }
        });
        
        Container contentPane = condBrowserFrame.getContentPane();
        contentPane.setLayout(new BorderLayout());

        // bean header information
        JPanel topPanel = new JPanel();
        String tStr = Bundle.getMessage("BrowserLogixNG") + " " + _curNamedBean.getSystemName() + "    " // NOI18N
                + _curNamedBean.getUserName() + "    "
                + (isEnabled(_curNamedBean)
                        ? Bundle.getMessage("BrowserEnabled") // NOI18N
                        : Bundle.getMessage("BrowserDisabled"));  // NOI18N
        topPanel.add(new JLabel(tStr));
        contentPane.add(topPanel, BorderLayout.NORTH);

        // Build the conditionalNGs listing
        _textContent = new JTextArea(this.getBeanText(_curNamedBean));
        JScrollPane scrollPane = new JScrollPane(_textContent);
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
        
        JPanel settingsPanel = getSettingsPanel();
        if (settingsPanel != null) bottomPanel.add(settingsPanel, BorderLayout.CENTER);
        
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
     * Save the bean browser window content to a text file.
     */
    void saveBrowserPressed() {
        userFileChooser.setApproveButtonText(Bundle.getMessage("BrowserSaveDialogApprove"));  // NOI18N
        userFileChooser.setDialogTitle(Bundle.getMessage("BrowserSaveDialogTitle"));  // NOI18N
        userFileChooser.rescanCurrentDirectory();
        // Default to logixNG system name.txt
        userFileChooser.setSelectedFile(new File(_curNamedBean.getSystemName() + ".txt"));  // NOI18N
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
        String tStr = Bundle.getMessage("BrowserLogixNG") + " " + _curNamedBean.getSystemName() + "    "  // NOI18N
                + _curNamedBean.getUserName() + "    "
                + (isEnabled(_curNamedBean)
                        ? Bundle.getMessage("BrowserEnabled")    // NOI18N
                        : Bundle.getMessage("BrowserDisabled"));  // NOI18N
//        JTextArea textContent = buildConditionalListing();
        JTextArea textContent = new JTextArea();
        try {
            // Add bean Header inforation first
            FileUtil.appendTextToFile(file, tStr);
            FileUtil.appendTextToFile(file, textContent.getText());
        } catch (IOException e) {
            log.error("Unable to write browser content to '{}', exception: '{}'", file, e);  // NOI18N
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractLogixNGTableAction.class);

}
