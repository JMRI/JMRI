package jmri.jmrit.beantable;

import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.TableColumn;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.NamedBean.BadSystemNameException;
import jmri.NamedBean.BadUserNameException;
import jmri.UserPreferencesManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.tools.swing.AbstractLogixNGEditor;
import jmri.jmrit.logixng.tools.swing.DeleteBean;
import jmri.jmrit.logixng.tools.swing.LogixNGBrowseWindow;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriJOptionPane;

/**
 * Swing action to create and register a LogixNG Table.
 * <p>
 Also contains the panes to create, edit, and delete a LogixNG.
 <p>
 * Most of the text used in this GUI is in BeanTableBundle.properties, accessed
 * via Bundle.getMessage().
 *
 * @author Dave Duchamp Copyright (C) 2007 (LogixTableAction)
 * @author Pete Cressman Copyright (C) 2009, 2010, 2011 (LogixTableAction)
 * @author Matthew Harris copyright (c) 2009 (LogixTableAction)
 * @author Dave Sand copyright (c) 2017 (LogixTableAction)
 * @author Daniel Bergqvist copyright (c) 2019 (AbstractLogixNGTableEditor)
 * @author Dave Sand copyright (c) 2021 (AbstractLogixNGTableEditor)
 *
 * @param <E> the type of NamedBean supported by this model
 */
public abstract class AbstractLogixNGTableAction<E extends NamedBean> extends AbstractTableAction<E> {


    private static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.logixng.LogixNGBundle");
    private static final ResourceBundle rbx2 = ResourceBundle.getBundle("jmri.jmrit.logixng.tools.swing.LogixNGSwingBundle");

    DeleteBean<E> deleteBean = new DeleteBean<>(getManager());

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

    protected abstract AbstractLogixNGEditor<E> getEditor(BeanTableDataModel<E> m, String sName);

    protected boolean isEditSupported() {
        return true;
    }

    @Nonnull
    @Override
    protected abstract Manager<E> getManager();

    protected abstract void enableAll(boolean enable);

    protected abstract void setEnabled(E bean, boolean enable);

    protected abstract boolean isEnabled(E bean);

    protected abstract E createBean(String userName);

    protected abstract E createBean(String systemName, String userName);

    protected abstract void deleteBean(E bean);

    protected boolean browseMonoSpace() { return false; }

    protected abstract String getBeanText(E bean, Base.PrintTreeSettings printTreeSettings);

    protected abstract String getBrowserTitle();

    protected abstract String getAddTitleKey();

    protected abstract String getCreateButtonHintKey();

    protected abstract void getListenerRefsIncludingChildren(E t, List<String> list);

    protected abstract boolean hasChildren(E t);

    // ------------ Methods for LogixNG Table Window ------------

    /**
     * Create the JTable DataModel, along with the changes (overrides of
     * BeanTableDataModel) for the specific case of a LogixNG table.
     */
    @Override
    protected void createModel() {
        m = new TableModel();
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
    public void setMenuBar(BeanTableFrame<E> f) {
        JMenu menu = new JMenu(Bundle.getMessage("MenuOptions"));  // NOI18N
        menu.setMnemonic(KeyEvent.VK_O);
        JMenuBar menuBar = f.getJMenuBar();
        int pos = menuBar.getMenuCount() - 1;  // count the number of menus to insert the TableMenus before 'Window' and 'Help'
        int offset = 1;
        log.debug("setMenuBar number of menu items = {}", pos);  // NOI18N
        for (int i = 0; i <= pos; i++) {
            if (menuBar.getComponent(i) instanceof JMenu) {
                if (((JMenu) menuBar.getComponent(i)).getText().equals(Bundle.getMessage("MenuHelp"))) {  // NOI18N
                    offset = -1;  // correct for use as part of ListedTableAction where the Help Menu is not yet present
                }
            }
        }

        // Do not include this menu for Module or Table tables
        if (this instanceof LogixNGTableAction) {
            JMenuItem r = new JMenuItem(Bundle.getMessage("EnableAllLogixNGs"));  // NOI18N
            r.addActionListener((ActionEvent e) -> {
                enableAll(true);
            });
            menu.add(r);

            r = new JMenuItem(Bundle.getMessage("DisableAllLogixNGs"));  // NOI18N
            r.addActionListener((ActionEvent e) -> {
                enableAll(false);
            });
            menu.add(r);

            menuBar.add(menu, pos + offset);
            offset++;
        }

        menu = new JMenu(Bundle.getMessage("MenuTools"));  // NOI18N
        menu.setMnemonic(KeyEvent.VK_T);

        JMenuItem item = new JMenuItem(rbx2.getString("MenuOpenClipboard"));  // NOI18N
        item.addActionListener((ActionEvent e) -> {
            jmri.jmrit.logixng.tools.swing.TreeEditor.openClipboard();
        });
        menu.add(item);

        item = new JMenuItem(Bundle.getMessage("OpenPickListTables"));  // NOI18N
        item.addActionListener((ActionEvent e) -> {
            openPickListTable();
        });
        menu.add(item);

        menuBar.add(menu, pos + offset);  // add this menu to the right of the previous
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

    protected AbstractLogixNGEditor<E> _editor = null;

    boolean _showReminder = false;
    private boolean _checkEnabled = jmri.InstanceManager.getDefault(jmri.configurexml.ShutdownPreferences.class).isStoreCheckEnabled();
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
    String systemNameAuto = this.getClassName() + ".AutoSystemName";       // NOI18N
    JButton create;

    // Edit E Variables
    private boolean _inEditMode = false;
    private boolean _inCopyMode = false;

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
            String titleKey = getAddTitleKey();
            String buttonHintKey = getCreateButtonHintKey();
            JPanel panel5 = makeAddFrame(titleKey, "Add");  // NOI18N
            // Create bean
            create = new JButton(Bundle.getMessage("ButtonCreate"));  // NOI18N
            panel5.add(create);
            create.addActionListener(this::createPressed);
            create.setToolTipText(Bundle.getMessage(buttonHintKey));  // NOI18N
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
     * Note: Also get there if the user closes the Add bean window.
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
//                JmriJOptionPane.showMessageDialog(null, "Copy is not implemented yet.", "Error", JmriJOptionPane.ERROR_MESSAGE);

                JPanel panel5 = makeAddFrame("TitleCopyLogixNG", "Copy");    // NOI18N
                // Create bean
                JButton create = new JButton(Bundle.getMessage("ButtonCopy"));  // NOI18N
                panel5.add(create);
                create.addActionListener((ActionEvent e) -> {
                    copyBeanPressed(e);
                });
                addLogixNGFrame.pack();
                addLogixNGFrame.setVisible(true);
                _autoSystemName.setSelected(false);
                InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
                    _autoSystemName.setSelected(prefMgr.getCheckboxPreferenceState(systemNameAuto, true));
                });

                _inCopyMode = false;
            }
        };
        log.debug("copyPressed started for {}", sName);  // NOI18N
        javax.swing.SwingUtilities.invokeLater(t);
        _inCopyMode = true;
        _logixNGSysName = sName;
    }

    String _logixNGSysName;

    protected void copyBean(@Nonnull E sourceBean, @Nonnull E targetBean) {
        throw new UnsupportedOperationException("Not implemented");
    }

    protected boolean isCopyBeanSupported() {
        return false;
    }

    protected boolean isExecuteSupported() {
        return false;
    }

    protected void execute(@Nonnull E bean) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Copy the bean as configured in the Copy set up pane.
     *
     * @param e the event heard
     */
    private void copyBeanPressed(ActionEvent e) {

        String uName = _addUserName.getText().trim();
        if (uName.length() == 0) {
            uName = null;
        }
        E targetBean;
        if (_autoSystemName.isSelected()) {
            if (!checkLogixNGUserName(uName)) {
                return;
            }
            targetBean = createBean(uName);
        } else {
            if (!checkLogixNGSysName()) {
                return;
            }
            String sName = _systemName.getText().trim();
            // check if a bean with this name already exists
            boolean createLogix = true;
            targetBean = getManager().getBySystemName(sName);
            if (targetBean != null) {
                int result = JmriJOptionPane.showConfirmDialog(f,
                        Bundle.getMessage("ConfirmLogixDuplicate", sName, _logixNGSysName), // NOI18N
                        Bundle.getMessage("QuestionTitle"), JmriJOptionPane.YES_NO_OPTION,    // NOI18N
                        JmriJOptionPane.QUESTION_MESSAGE);
                if (JmriJOptionPane.NO_OPTION == result) {
                    return;
                }
                createLogix = false;
                String userName = targetBean.getUserName();
                if (userName != null && userName.length() > 0) {
                    _addUserName.setText(userName);
                    uName = userName;
                }
            } else if (!checkLogixNGUserName(uName)) {
                return;
            }
            if (createLogix) {
                // Create the new LogixNG
                targetBean = createBean(sName, uName);
                if (targetBean == null) {
                    // should never get here unless there is an assignment conflict
                    log.error("Failure to create LogixNG with System Name: {}", sName);  // NOI18N
                    return;
                }
            } else if (targetBean == null) {
                log.error("Error targetLogix is null!");  // NOI18N
                return;
            } else {
                targetBean.setUserName(uName);
            }
        }
        E sourceBean = getManager().getBySystemName(_logixNGSysName);
        if (sourceBean != null) copyBean(sourceBean, targetBean);
        else log.error("Error targetLogix is null!");  // NOI18N
        cancelAddPressed(null);
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
                JmriJOptionPane.showMessageDialog(addLogixNGFrame,
                        Bundle.getMessage("LogixNGError3"), Bundle.getMessage("ErrorTitle"), // NOI18N
                        JmriJOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    /**
     * Check validity of various LogixNG system names.
     * <p>
     * Fixes name if it doesn't start with the appropriate prefix or the $ for alpha suffixes
     *
     * @return false if the name fails the NameValidity check
     */
    boolean checkLogixNGSysName() {
        if (_autoSystemName.isSelected()) {
            return true;
        }

        var sName = _systemName.getText().trim();
        var prefix = getManager().getSubSystemNamePrefix();

        if (!sName.isEmpty() && !sName.startsWith(prefix)) {
            var isNumber = sName.matches("^\\d+$");
            var hasDollar = sName.startsWith("$");

            var newName = new StringBuilder(prefix);
            if (!isNumber && !hasDollar) {
                newName.append("$");
            }
            newName.append(sName);
            sName = newName.toString();
        }

        if (getManager().validSystemNameFormat(sName) != jmri.Manager.NameValidity.VALID) {
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("LogixNGError8", sName), Bundle.getMessage("ErrorTitle"), // NOI18N
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
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
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("LogixNGError32", _curNamedBean.getSystemName()),
                    Bundle.getMessage("ErrorTitle"),
                    JmriJOptionPane.ERROR_MESSAGE);
            if (_editor != null) {
                _editor.bringToFront();
            }
            return false;
        }

        if (_inCopyMode) {
            // Already editing a bean, ask for completion of that edit
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("LogixNGError31", _logixNGSysName),
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (sName != null) {
            // check if a bean with this name exists
            E x = getManager().getBySystemName(sName);
            if (x == null) {
                // bean does not exist, so cannot be edited
                log.error("No bean with system name: {}", sName);
                JmriJOptionPane.showMessageDialog(null,
                        Bundle.getMessage("LogixNGError5"),
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JmriJOptionPane.ERROR_MESSAGE);
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
            try {
                _curNamedBean = createBean(uName);
            } catch (BadSystemNameException | BadUserNameException ex) {
                JmriJOptionPane.showMessageDialog(addLogixNGFrame, ex.getLocalizedMessage(),
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JmriJOptionPane.ERROR_MESSAGE);
                return;
            }
            if (_curNamedBean == null) {
                log.error("Failure to create bean with System Name: {}", "none");  // NOI18N
                return;
            }
            sName = _curNamedBean.getSystemName();
        } else {
            if (!checkLogixNGSysName()) {
                return;
            }
            // Get validated system name
            sName = _systemName.getText();
            // check if a bean with this name already exists
            E x;
            try {
                x = getManager().getBySystemName(sName);
            } catch (Exception ex) {
                // user input no good
                handleCreateException(sName);
                return;  // without creating
            }
            if (x != null) {
                // bean already exists
                JmriJOptionPane.showMessageDialog(addLogixNGFrame, Bundle.getMessage("LogixNGError1"),
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JmriJOptionPane.ERROR_MESSAGE);
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
        JmriJOptionPane.showMessageDialog(addLogixNGFrame,
                Bundle.getMessage("ErrorLogixAddFailed", sysName), // NOI18N
                Bundle.getMessage("ErrorTitle"), // NOI18N
                JmriJOptionPane.ERROR_MESSAGE);
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
        _editor = getEditor(m, sName);

        if (_editor == null) return;    // Editor not implemented yet for LogixNG Tables

        _inEditMode = true;

        _editor.addEditorEventListener((data) -> {
            String lgxName = sName;
            data.forEach((key, value) -> {
                if (key.equals("Finish")) {                  // NOI18N
                    _editor = null;
                    _inEditMode = false;
                    f.setVisible(true);
                } else if (key.equals("Delete")) {           // NOI18N
                    _inEditMode = false;
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

    /**
     * Display reminder to save.
     */
    void showSaveReminder() {
        if (_showReminder && !_checkEnabled) {
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

        if (x == null) return;  // This should never happen

        deleteBean.delete(x, hasChildren(x), (t)->{deleteBean(t);},
                (t,list)->{getListenerRefsIncludingChildren(t,list);},
                getClassName());
    }

    /**
     * Respond to the Execute combo selection bean window execute request.
     *
     * @param sName system name of bean to be deleted
     */
    void executePressed(String sName) {
        final E x = getManager().getBySystemName(sName);

        if (x == null) return;  // This should never happen

        execute(x);
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleLogixNGTable");        // NOI18N
    }

    @Override
    protected String getClassName() {
        // The class that is returned must have a default constructor,
        // a constructor with no parameters.
        return jmri.jmrit.logixng.LogixNG_UserPreferences.class.getName();
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

        if (_curNamedBean == null) {
            // This should never happen but SpotBugs complains about it.
            throw new RuntimeException("_curNamedBean is null");
        }

        String title = Bundle.getMessage("BrowserLogixNG") + " " + _curNamedBean.getSystemName() + "    " // NOI18N
                + _curNamedBean.getUserName() + "    "
                + (isEnabled(_curNamedBean)
                        ? Bundle.getMessage("BrowserEnabled") // NOI18N
                        : Bundle.getMessage("BrowserDisabled"));  // NOI18N

        LogixNGBrowseWindow browseWindow =
                new LogixNGBrowseWindow(Bundle.getMessage("LogixNG_Browse_Title"));
        browseWindow.getPrintTreeSettings();
        boolean showSettingsPanel = this instanceof LogixNGTableAction || this instanceof LogixNGModuleTableAction;
        browseWindow.makeBrowserWindow(browseMonoSpace(), showSettingsPanel, title, _curNamedBean.getSystemName(),
                (printTreeSettings) -> {
                        return AbstractLogixNGTableAction.this.getBeanText(_curNamedBean, printTreeSettings);
                });
    }



    protected class TableModel extends BeanTableDataModel<E> {

        // overlay the state column with the edit column
        static public final int ENABLECOL = VALUECOL;
        static public final int EDITCOL = DELETECOL;
        protected String enabledString = Bundle.getMessage("ColumnHeadEnabled");  // NOI18N

        @Override
        public String getColumnName(int col) {
            if (col == EDITCOL) {
                return Bundle.getMessage("ColumnHeadMenu");     // This makes it easier to test the table
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

                } else if (Bundle.getMessage("LogixNG_ButtonExecute").equals(value)) {  // NOI18N
                    executePressed(sName);
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
        }

        @Override
        public E getBySystemName(String name) {
            return AbstractLogixNGTableAction.this.getManager().getBySystemName(name);
        }

        @Override
        public E getByUserName(String name) {
            return AbstractLogixNGTableAction.this.getManager().getByUserName(name);
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
            if (!(getManager() instanceof jmri.jmrit.logixng.LogixNG_Manager)) {
                table.getColumnModel().getColumn(2).setMinWidth(0);
                table.getColumnModel().getColumn(2).setMaxWidth(0);
            }
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
            if (isEditSupported()) editCombo.addItem(Bundle.getMessage("ButtonEdit"));  // NOI18N
            editCombo.addItem(Bundle.getMessage("BrowserButton"));  // NOI18N
            if (isCopyBeanSupported()) editCombo.addItem(Bundle.getMessage("ButtonCopy"));  // NOI18N
            editCombo.addItem(Bundle.getMessage("ButtonDelete"));  // NOI18N
            if (isExecuteSupported()) editCombo.addItem(Bundle.getMessage("LogixNG_ButtonExecute"));  // NOI18N
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
//                 return Bundle.getMessage("BeanNameLogix");  // NOI18N
            return rbx.getString("BeanNameLogixNG");  // NOI18N
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractLogixNGTableAction.class);

}
