package jmri.jmrit.logixng.tools.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import jmri.*;
import jmri.jmrit.beantable.BeanTableDataModel;
import jmri.jmrit.beantable.BeanTableFrame;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.util.JmriJFrame;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Editor for LogixNG
 *
 * @author Dave Duchamp Copyright (C) 2007  (ConditionalListEdit)
 * @author Pete Cressman Copyright (C) 2009, 2010, 2011  (ConditionalListEdit)
 * @author Matthew Harris copyright (c) 2009  (ConditionalListEdit)
 * @author Dave Sand copyright (c) 2017  (ConditionalListEdit)
 * @author Daniel Bergqvist (c) 2019
 * @author Dave Sand (c) 2021
 */
public final class LogixNGEditor implements AbstractLogixNGEditor<LogixNG> {

    BeanTableFrame<LogixNG> beanTableFrame;
    BeanTableDataModel<LogixNG> beanTableDataModel;

    LogixNG_Manager _logixNG_Manager = null;
    LogixNG _curLogixNG = null;

    ConditionalNGEditor _treeEdit = null;
    ConditionalNGDebugger _debugger = null;

    int _numConditionalNGs = 0;
    boolean _inEditMode = false;

    boolean _showReminder = false;
    boolean _suppressReminder = false;
    boolean _suppressIndirectRef = false;

    private final JCheckBox _autoSystemName = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));   // NOI18N
    private final JLabel _sysNameLabel = new JLabel(Bundle.getMessage("SystemName") + ":");  // NOI18N
    private final JLabel _userNameLabel = new JLabel(Bundle.getMessage("UserName") + ":");   // NOI18N
    private final String systemNameAuto = this.getClass().getName() + ".AutoSystemName";         // NOI18N
    private final JTextField _systemName = new JTextField(20);
    private final JTextField _addUserName = new JTextField(20);


    /**
     * Create a new ConditionalNG List View editor.
     *
     * @param f the bean table frame
     * @param m the bean table model
     * @param sName name of the LogixNG being edited
     */
    public LogixNGEditor(BeanTableFrame<LogixNG> f, BeanTableDataModel<LogixNG> m, String sName) {
        this.beanTableFrame = f;
        this.beanTableDataModel = m;
        _logixNG_Manager = InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class);
        _curLogixNG = _logixNG_Manager.getBySystemName(sName);
        makeEditLogixNGWindow();
    }

    // ------------ LogixNG Variables ------------
    JmriJFrame _editLogixNGFrame = null;
    JTextField editUserName = new JTextField(20);
    JLabel status = new JLabel(" ");

    // ------------ ConditionalNG Variables ------------
    private ConditionalNGTableModel _conditionalNGTableModel = null;
    private JCheckBox _showStartupThreadsCheckBox = null;
    private ConditionalNG _curConditionalNG = null;
    int _conditionalRowNumber = 0;
    boolean _inReorderMode = false;
    boolean _inActReorder = false;
    boolean _inVarReorder = false;
    int _nextInOrder = 0;

    // ------------ Select LogixNG/ConditionalNG Variables ------------
    JPanel _selectLogixNGPanel = null;
    JPanel _selectConditionalNGPanel = null;
//    private JComboBox<String> _selectLogixNGComboBox = new JComboBox<>();
//    private JComboBox<String> _selectConditionalNGComboBox = new JComboBox<>();
    TreeMap<String, String> _selectLogixNGMap = new TreeMap<>();
    ArrayList<String> _selectConditionalNGList = new ArrayList<>();

    // ------------ Edit ConditionalNG Variables ------------
    boolean _inEditConditionalNGMode = false;
    JmriJFrame _editConditionalNGFrame = null;
    JRadioButton _triggerOnChangeButton;

    // ------------ Methods for Edit LogixNG Pane ------------

    /**
     * Create and/or initialize the Edit LogixNG pane.
     */
    void makeEditLogixNGWindow() {
        editUserName.setText(_curLogixNG.getUserName());
        // clear conditional table if needed
        if (_conditionalNGTableModel != null) {
            _conditionalNGTableModel.fireTableStructureChanged();
        }
        _inEditMode = true;
        if (_editLogixNGFrame == null) {
            if (_curLogixNG.getUserName() != null) {
                _editLogixNGFrame = new JmriJFrame(
                        Bundle.getMessage("TitleEditLogixNG2",
                                _curLogixNG.getSystemName(),   // NOI18N
                                _curLogixNG.getUserName()),    // NOI18N
                        false,
                        false);
            } else {
                _editLogixNGFrame = new JmriJFrame(
                        Bundle.getMessage("TitleEditLogixNG", _curLogixNG.getSystemName()),  // NOI18N
                        false,
                        false);
            }
            _editLogixNGFrame.addHelpMenu(
                    "package.jmri.jmrit.logixng.LogixNGTableEditor", true);  // NOI18N
            _editLogixNGFrame.setLocation(100, 30);
            Container contentPane = _editLogixNGFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel systemNameLabel = new JLabel(Bundle.getMessage("ColumnSystemName") + ":");  // NOI18N
            panel1.add(systemNameLabel);
            JLabel fixedSystemName = new JLabel(_curLogixNG.getSystemName());
            panel1.add(fixedSystemName);
            contentPane.add(panel1);
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel userNameLabel = new JLabel(Bundle.getMessage("ColumnUserName") + ":");  // NOI18N
            panel2.add(userNameLabel);
            panel2.add(editUserName);
            editUserName.setToolTipText(Bundle.getMessage("LogixNGUserNameHint2"));  // NOI18N
            contentPane.add(panel2);
            // add table of ConditionalNGs
            JPanel pctSpace = new JPanel();
            pctSpace.setLayout(new FlowLayout());
            pctSpace.add(new JLabel("   "));
            contentPane.add(pctSpace);
            JPanel pTitle = new JPanel();
            pTitle.setLayout(new FlowLayout());
            pTitle.add(new JLabel(Bundle.getMessage("ConditionalNGTableTitle")));  // NOI18N
            contentPane.add(pTitle);
            // initialize table of conditionals
            _conditionalNGTableModel = new ConditionalNGTableModel();
            JTable conditionalTable = new JTable(_conditionalNGTableModel);
            conditionalTable.setRowSelectionAllowed(false);
            TableColumnModel conditionalColumnModel = conditionalTable
                    .getColumnModel();
            TableColumn sNameColumn = conditionalColumnModel
                    .getColumn(ConditionalNGTableModel.SNAME_COLUMN);
            sNameColumn.setResizable(true);
            sNameColumn.setMinWidth(100);
            sNameColumn.setPreferredWidth(130);
            TableColumn uNameColumn = conditionalColumnModel
                    .getColumn(ConditionalNGTableModel.UNAME_COLUMN);
            uNameColumn.setResizable(true);
            uNameColumn.setMinWidth(210);
            uNameColumn.setPreferredWidth(260);
            TableColumn threadColumn = conditionalColumnModel
                    .getColumn(ConditionalNGTableModel.THREAD_COLUMN);
            threadColumn.setResizable(true);
            threadColumn.setMinWidth(210);
            threadColumn.setPreferredWidth(260);
            TableColumn buttonColumn = conditionalColumnModel
                    .getColumn(ConditionalNGTableModel.BUTTON_COLUMN);
            TableColumn buttonDeleteColumn = conditionalColumnModel
                    .getColumn(ConditionalNGTableModel.BUTTON_DELETE_COLUMN);
            TableColumn buttonEditThreadsColumn = conditionalColumnModel
                    .getColumn(ConditionalNGTableModel.BUTTON_EDIT_THREADS_COLUMN);

            // install button renderer and editor
            ButtonRenderer buttonRenderer = new ButtonRenderer();
            conditionalTable.setDefaultRenderer(JButton.class, buttonRenderer);
            TableCellEditor buttonEditor = new ButtonEditor(new JButton());
            conditionalTable.setDefaultEditor(JButton.class, buttonEditor);
            JButton testButton = new JButton("XXXXXX");  // NOI18N
            JButton testButton2 = new JButton("XXXXXXXXXX");  // NOI18N
            conditionalTable.setRowHeight(testButton.getPreferredSize().height);
            buttonColumn.setMinWidth(testButton.getPreferredSize().width);
            buttonColumn.setMaxWidth(testButton.getPreferredSize().width);
            buttonColumn.setResizable(false);
            buttonDeleteColumn.setMinWidth(testButton.getPreferredSize().width);
            buttonDeleteColumn.setMaxWidth(testButton.getPreferredSize().width);
            buttonDeleteColumn.setResizable(false);
            buttonEditThreadsColumn.setMinWidth(testButton2.getPreferredSize().width);
            buttonEditThreadsColumn.setMaxWidth(testButton2.getPreferredSize().width);
            buttonEditThreadsColumn.setResizable(false);

            JScrollPane conditionalTableScrollPane = new JScrollPane(conditionalTable);
            Dimension dim = conditionalTable.getPreferredSize();
            dim.height = 450;
            conditionalTableScrollPane.getViewport().setPreferredSize(dim);
            contentPane.add(conditionalTableScrollPane);

            _showStartupThreadsCheckBox = new JCheckBox(Bundle.getMessage("ShowStartupThreadCheckBox"));
            contentPane.add(_showStartupThreadsCheckBox);
            _showStartupThreadsCheckBox.addActionListener((evt) -> {
                _conditionalNGTableModel.setShowStartupThreads(
                        _showStartupThreadsCheckBox.isSelected());
            });

            // add message area between table and buttons
            JPanel panel4 = new JPanel();
            panel4.setLayout(new BoxLayout(panel4, BoxLayout.Y_AXIS));
            JPanel panel41 = new JPanel();
            panel41.setLayout(new FlowLayout());
            panel41.add(status);
            panel4.add(panel41);
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
            // ConditionalNG panel buttons - New ConditionalNG
            JButton newConditionalNGButton = new JButton(Bundle.getMessage("NewConditionalNGButton"));  // NOI18N
            panel42.add(newConditionalNGButton);
            newConditionalNGButton.addActionListener((e) -> {
                newConditionalNGPressed(e);
            });
            newConditionalNGButton.setToolTipText(Bundle.getMessage("NewConditionalNGButtonHint"));  // NOI18N
            // ConditionalNG panel buttons - Reorder
            JButton reorderButton = new JButton(Bundle.getMessage("ReorderButton"));  // NOI18N
            panel42.add(reorderButton);
            reorderButton.addActionListener((e) -> {
                reorderPressed(e);
            });
            reorderButton.setToolTipText(Bundle.getMessage("ReorderButtonHint"));  // NOI18N
            // ConditionalNG panel buttons - Calculate
            JButton executeButton = new JButton(Bundle.getMessage("ExecuteButton"));  // NOI18N
            panel42.add(executeButton);
            executeButton.addActionListener((e) -> {
                executePressed(e);
            });
            executeButton.setToolTipText(Bundle.getMessage("ExecuteButtonHint"));  // NOI18N
            panel4.add(panel42);
            Border panel4Border = BorderFactory.createEtchedBorder();
            panel4.setBorder(panel4Border);
            contentPane.add(panel4);
            // add buttons at bottom of window
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            // Bottom Buttons - Done LogixNG
            JButton done = new JButton(Bundle.getMessage("ButtonDone"));  // NOI18N
            panel5.add(done);
            done.addActionListener((e) -> {
                donePressed(e);
            });
            done.setToolTipText(Bundle.getMessage("DoneButtonHint"));  // NOI18N
            // Delete LogixNG
            JButton delete = new JButton(Bundle.getMessage("ButtonDelete"));  // NOI18N
            panel5.add(delete);
            delete.addActionListener((e) -> {
                deletePressed();
            });
            delete.setToolTipText(Bundle.getMessage("DeleteLogixNGButtonHint"));  // NOI18N
            contentPane.add(panel5);
        }

        _editLogixNGFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (_inEditMode) {
                    donePressed(null);
                } else {
                    finishDone();
                }
            }
        });
        _editLogixNGFrame.pack();
        _editLogixNGFrame.setVisible(true);
    }

    @Override
    public void bringToFront() {
        if (_editLogixNGFrame != null) {
            _editLogixNGFrame.setVisible(true);
        }
    }

    /**
     * Display reminder to save.
     */
    void showSaveReminder() {
        if (_showReminder) {
            if (InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class) != null) {
                InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showInfoMessage(Bundle.getMessage("ReminderTitle"), // NOI18N
                                Bundle.getMessage("ReminderSaveString", // NOI18N
                                        Bundle.getMessage("MenuItemLogixNGTable")), // NOI18N
                                getClassName(),
                                "remindSaveLogixNG"); // NOI18N
            }
        }
    }

    /**
     * Respond to the Reorder Button in the Edit LogixNG pane.
     *
     * @param e The event heard
     */
    void reorderPressed(ActionEvent e) {
        if (checkEditConditionalNG()) {
            return;
        }
        // Check if reorder is reasonable
        _showReminder = true;
        _nextInOrder = 0;
        _inReorderMode = true;
        status.setText(Bundle.getMessage("ReorderMessage"));  // NOI18N
        _conditionalNGTableModel.fireTableDataChanged();
    }

    /**
     * Respond to the First/Next (Delete) Button in the Edit LogixNG window.
     *
     * @param row index of the row to put as next in line (instead of the one
     *            that was supposed to be next)
     */
    void swapConditionalNG(int row) {
        _curLogixNG.swapConditionalNG(_nextInOrder, row);
        _nextInOrder++;
        if (_nextInOrder >= _numConditionalNGs) {
            _inReorderMode = false;
        }
        //status.setText("");
        _conditionalNGTableModel.fireTableDataChanged();
    }

    /**
     * Responds to the Execute Button in the Edit LogixNG window.
     *
     * @param e The event heard
     */
    void executePressed(ActionEvent e) {
        if (checkEditConditionalNG()) {
            return;
        }
        // are there ConditionalNGs to execute?
        if (_numConditionalNGs > 0) {
            // There are conditionals to calculate
            for (int i = 0; i < _numConditionalNGs; i++) {
                ConditionalNG c = _curLogixNG.getConditionalNG(i);
                if (c == null) {
                    log.error("Invalid conditional system name when executing"); // NOI18N
                } else {
                    c.execute();
                }
            }
            // force the table to update
//            conditionalNGTableModel.fireTableDataChanged();
        }
    }

    /**
     * Respond to the Done button in the Edit LogixNG window.
     * <p>
     * Note: We also get here if the Edit LogixNG window is dismissed, or if the
     * Add button is pressed in the Logic Table with an active Edit LogixNG
     * window.
     *
     * @param e The event heard
     */
    void donePressed(ActionEvent e) {
        if (checkEditConditionalNG()) {
            return;
        }
        // Check if the User Name has been changed
        String uName = editUserName.getText().trim();
        if (!(uName.equals(_curLogixNG.getUserName()))) {
            // user name has changed - check if already in use
            if (uName.length() > 0) {
                LogixNG p = _logixNG_Manager.getByUserName(uName);
                if (p != null) {
                    // LogixNG with this user name already exists
                    log.error("Failure to update LogixNG with Duplicate User Name: " // NOI18N
                            + uName);
                    JOptionPane.showMessageDialog(_editLogixNGFrame,
                            Bundle.getMessage("Error6"),
                            Bundle.getMessage("ErrorTitle"), // NOI18N
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            // user name is unique, change it
            // user name is unique, change it
            logixNG_Data.clear();
            logixNG_Data.put("chgUname", uName);  // NOI18N
            fireEditorEvent();
        }
        // complete update and activate LogixNG
        finishDone();
    }

    void finishDone() {
        showSaveReminder();
        _inEditMode = false;
        if (_editLogixNGFrame != null) {
            _editLogixNGFrame.setVisible(false);
            _editLogixNGFrame.dispose();
            _editLogixNGFrame = null;
        }
        logixNG_Data.clear();
        logixNG_Data.put("Finish", _curLogixNG.getSystemName());   // NOI18N
        fireEditorEvent();
    }

    /**
     * Respond to the Delete button in the Edit LogixNG window.
     */
    void deletePressed() {
        if (checkEditConditionalNG()) {
            return;
        }

        _showReminder = true;
        logixNG_Data.clear();
        logixNG_Data.put("Delete", _curLogixNG.getSystemName());   // NOI18N
        fireEditorEvent();
        finishDone();
    }

    /**
     * Respond to the New ConditionalNG Button in Edit LogixNG Window.
     *
     * @param e The event heard
     */
    void newConditionalNGPressed(ActionEvent e) {
        if (checkEditConditionalNG()) {
            return;
        }

        // make an Add Item Frame
        if (showAddLogixNGFrame()) {
            if (_systemName.getText().isEmpty() && _autoSystemName.isSelected()) {
                _systemName.setText(InstanceManager.getDefault(ConditionalNG_Manager.class).getAutoSystemName());
            }

            // Create ConditionalNG
            _curConditionalNG =
                    InstanceManager.getDefault(ConditionalNG_Manager.class)
                            .createConditionalNG(_curLogixNG, _systemName.getText(), _addUserName.getText());

            if (_curConditionalNG == null) {
                // should never get here unless there is an assignment conflict
                log.error("Failure to create ConditionalNG"); // NOI18N
                return;
            }
            // add to LogixNG at the end of the calculate order
            _conditionalNGTableModel.fireTableRowsInserted(_numConditionalNGs, _numConditionalNGs);
            _conditionalRowNumber = _numConditionalNGs;
            _numConditionalNGs++;
            _showReminder = true;
            makeEditConditionalNGWindow();
        }
    }

    /**
     * Create or edit action/expression dialog.
     */
    private boolean showAddLogixNGFrame() {

        AtomicBoolean result = new AtomicBoolean(false);

        JDialog dialog  = new JDialog(
                _editLogixNGFrame,
                Bundle.getMessage("AddConditionalNGDialogTitle"),
                true);
//        frame.addHelpMenu(
//                "package.jmri.jmrit.logixng.tools.swing.ConditionalNGAddEdit", true);     // NOI18N
        Container contentPanel = dialog.getContentPane();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JPanel p;
        p = new JPanel();
//        p.setLayout(new FlowLayout());
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

        _systemName.setText("");
        _systemName.setEnabled(true);
        _addUserName.setText("");

        _addUserName.setToolTipText(Bundle.getMessage("UserNameHint"));    // NOI18N
//        _addUserName.setToolTipText("LogixNGUserNameHint");    // NOI18N
        _systemName.setToolTipText(Bundle.getMessage("LogixNGSystemNameHint"));   // NOI18N
//        _systemName.setToolTipText("LogixNGSystemNameHint");   // NOI18N
        contentPanel.add(p);
        // set up message
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        JPanel panel31 = new JPanel();
//        panel31.setLayout(new FlowLayout());
        JPanel panel32 = new JPanel();
        JLabel message1 = new JLabel(Bundle.getMessage("AddMessage1"));  // NOI18N
        panel31.add(message1);
        JLabel message2 = new JLabel(Bundle.getMessage("AddMessage2"));  // NOI18N
        panel32.add(message2);

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());

        // Get panel for the item
        panel3.add(panel31);
        panel3.add(panel32);
        contentPanel.add(panel3);

        // Cancel
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        panel5.add(cancel);
        cancel.addActionListener((ActionEvent e) -> {
            dialog.dispose();
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        cancel.setToolTipText("CancelLogixButtonHint");      // NOI18N

        JButton create = new JButton(Bundle.getMessage("ButtonCreate"));  // NOI18N
        create.addActionListener((ActionEvent e2) -> {
            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
                prefMgr.setCheckboxPreferenceState(systemNameAuto, _autoSystemName.isSelected());
            });
            result.set(true);
            dialog.dispose();
        });
        create.setToolTipText(Bundle.getMessage("CreateButtonHint"));  // NOI18N

        panel5.add(create);

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                dialog.dispose();
            }
        });

        contentPanel.add(panel5);

        _autoSystemName.addItemListener((ItemEvent e) -> {
            autoSystemName();
        });
//        addLogixNGFrame.setLocationRelativeTo(component);
        dialog.pack();
        dialog.setLocationRelativeTo(null);

        _autoSystemName.setSelected(true);
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
            _autoSystemName.setSelected(prefMgr.getCheckboxPreferenceState(systemNameAuto, true));
        });

        dialog.setVisible(true);

        return result.get();
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

    // ============ Edit Conditional Window and Methods ============

    /**
     * Create and/or initialize the Edit Conditional window.
     * <p>
     * Note: you can get here via the New Conditional button
     * (newConditionalPressed) or via an Edit button in the Conditional table of
     * the Edit Logix window.
     */
    void makeEditConditionalNGWindow() {
        // Create a new LogixNG edit view, add the listener.
        _treeEdit = new ConditionalNGEditor(_curConditionalNG);
        _treeEdit.initComponents();
        _treeEdit.setVisible(true);
        _inEditConditionalNGMode = true;
        _editConditionalNGFrame = _treeEdit;
        _editConditionalNGFrame.addHelpMenu(
                "package.jmri.jmrit.logixng.ConditionalNGEditor", true);  // NOI18N

        final LogixNGEditor logixNGEditor = this;
        _treeEdit.addLogixNGEventListener(new LogixNGEventListenerImpl(logixNGEditor));
    }

    /**
     * Create and/or initialize the Edit Conditional window.
     * <p>
     * Note: you can get here via the New Conditional button
     * (newConditionalPressed) or via an Edit button in the Conditional table of
     * the Edit Logix window.
     */
    void makeDebugConditionalNGWindow() {
        // Create a new LogixNG edit view, add the listener.
        _debugger = new ConditionalNGDebugger(_curConditionalNG);
        _debugger.initComponents();
        _debugger.setVisible(true);
        _inEditConditionalNGMode = true;
        _editConditionalNGFrame = _debugger;

        final LogixNGEditor logixNGEditor = this;
        _debugger.addLogixNGEventListener(new LogixNG_DebuggerEventListenerImpl(logixNGEditor));
    }

    // ------------ Methods for Edit ConditionalNG Pane ------------

    /**
     * Respond to Edit Button in the ConditionalNG table of the Edit LogixNG Window.
     *
     * @param rx index (row number) of ConditionalNG to be edited
     */
    void editConditionalNGPressed(int rx) {
        if (checkEditConditionalNG()) {
            return;
        }
        // get ConditionalNG to edit
        _curConditionalNG = _curLogixNG.getConditionalNG(rx);
        if (_curConditionalNG == null) {
            log.error("Attempted edit of non-existant conditional.");  // NOI18N
            return;
        }
        _conditionalRowNumber = rx;
        // get action variables
        makeEditConditionalNGWindow();
    }

    /**
     * Respond to Edit Button in the ConditionalNG table of the Edit LogixNG Window.
     *
     * @param rx index (row number) of ConditionalNG to be edited
     */
    void debugConditionalNGPressed(int rx) {
        if (checkEditConditionalNG()) {
            return;
        }
        // get ConditionalNG to edit
        _curConditionalNG = _curLogixNG.getConditionalNG(rx);
        if (_curConditionalNG == null) {
            log.error("Attempted edit of non-existant conditional.");  // NOI18N
            return;
        }
        _conditionalRowNumber = rx;
        // get action variables
        makeDebugConditionalNGWindow();
    }

    /**
     * Check if edit of a conditional is in progress.
     *
     * @return true if this is the case, after showing dialog to user
     */
    private boolean checkEditConditionalNG() {
        if (_inEditConditionalNGMode) {
            // Already editing a ConditionalNG, ask for completion of that edit
            JOptionPane.showMessageDialog(_editConditionalNGFrame,
                    Bundle.getMessage("Error_ConditionalNGInEditMode", _curConditionalNG.getSystemName()), // NOI18N
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            _editConditionalNGFrame.setVisible(true);
            return true;
        }
        return false;
    }

    boolean checkConditionalNGUserName(String uName, LogixNG logixNG) {
        if ((uName != null) && (!(uName.equals("")))) {
            for (int i=0; i < logixNG.getNumConditionalNGs(); i++) {
                ConditionalNG p = logixNG.getConditionalNG(i);
                if (uName.equals(p.getUserName())) {
                    // ConditionalNG with this user name already exists
                    log.error("Failure to update ConditionalNG with Duplicate User Name: " // NOI18N
                            + uName);
                    JOptionPane.showMessageDialog(_editConditionalNGFrame,
                            Bundle.getMessage("Error10"),    // NOI18N
                            Bundle.getMessage("ErrorTitle"), // NOI18N
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        } // else return false;
        return true;
    }

    /**
     * Check form of ConditionalNG systemName.
     *
     * @param sName system name of bean to be checked
     * @return false if sName is empty string or null
     */
    boolean checkConditionalNGSystemName(String sName) {
        if ((sName != null) && (!(sName.equals("")))) {
            ConditionalNG p = _curLogixNG.getConditionalNG(sName);
            if (p != null) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    // ------------ Table Models ------------

    /**
     * Table model for ConditionalNGs in the Edit LogixNG pane.
     */
    public final class ConditionalNGTableModel extends AbstractTableModel
            implements PropertyChangeListener {

        public static final int SNAME_COLUMN = 0;
        public static final int UNAME_COLUMN = SNAME_COLUMN + 1;
        public static final int THREAD_COLUMN = UNAME_COLUMN + 1;
        public static final int BUTTON_COLUMN = THREAD_COLUMN + 1;
        public static final int BUTTON_DEBUG_COLUMN = BUTTON_COLUMN + 1;
        public static final int BUTTON_DELETE_COLUMN = BUTTON_DEBUG_COLUMN + 1;
        public static final int BUTTON_EDIT_THREADS_COLUMN = BUTTON_DELETE_COLUMN + 1;
        public static final int NUM_COLUMNS = BUTTON_EDIT_THREADS_COLUMN + 1;

        private boolean _showStartupThreads;


        public ConditionalNGTableModel() {
            super();
            updateConditionalNGListeners();
        }

        synchronized void updateConditionalNGListeners() {
            // first, remove listeners from the individual objects
            ConditionalNG c;
            _numConditionalNGs = _curLogixNG.getNumConditionalNGs();
            for (int i = 0; i < _numConditionalNGs; i++) {
                // if object has been deleted, it's not here; ignore it
                c = _curLogixNG.getConditionalNG(i);
                if (c != null) {
                    c.removePropertyChangeListener(this);
                }
            }
            // and add them back in
            for (int i = 0; i < _numConditionalNGs; i++) {
                c = _curLogixNG.getConditionalNG(i);
                if (c != null) {
                    c.addPropertyChangeListener(this);
                }
            }
        }

        public void setShowStartupThreads(boolean showStartupThreads) {
            _showStartupThreads = showStartupThreads;
            fireTableRowsUpdated(0, _curLogixNG.getNumConditionalNGs()-1);
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {  // NOI18N
                // a new NamedBean is available in the manager
                updateConditionalNGListeners();
                fireTableDataChanged();
            } else if (matchPropertyName(e)) {
                // a value changed.
                fireTableDataChanged();
            }
        }

        /**
         * Check if this property event is announcing a change this table should
         * display.
         * <p>
         * Note that events will come both from the NamedBeans and from the
         * manager.
         *
         * @param e the event heard
         * @return true if a change in State or Appearance was heard
         */
        boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
            return (e.getPropertyName().contains("UserName") ||      // NOI18N
                    e.getPropertyName().contains("Thread"));  // NOI18N
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if ((c == BUTTON_COLUMN)
                    || (c == BUTTON_DEBUG_COLUMN)
                    || (c == BUTTON_DELETE_COLUMN)
                    || (c == BUTTON_EDIT_THREADS_COLUMN)) {
                return JButton.class;
            }
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return NUM_COLUMNS;
        }

        @Override
        public int getRowCount() {
            return (_numConditionalNGs);
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            if (!_inReorderMode) {
                return ((c == UNAME_COLUMN)
                        || (c == BUTTON_COLUMN)
                        || ((c == BUTTON_DEBUG_COLUMN) && InstanceManager.getDefault(LogixNGPreferences.class).getInstallDebugger())
                        || (c == BUTTON_DELETE_COLUMN)
                        || (c == BUTTON_EDIT_THREADS_COLUMN));
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
                    return Bundle.getMessage("ColumnSystemName");  // NOI18N
                case UNAME_COLUMN:
                    return Bundle.getMessage("ColumnUserName");  // NOI18N
                case THREAD_COLUMN:
                    return Bundle.getMessage("ConditionalNG_Table_ColumnThreadName");  // NOI18N
                case BUTTON_COLUMN:
                    return ""; // no label
                case BUTTON_DEBUG_COLUMN:
                    return ""; // no label
                case BUTTON_DELETE_COLUMN:
                    return ""; // no label
                case BUTTON_EDIT_THREADS_COLUMN:
                    return ""; // no label
                default:
                    throw new IllegalArgumentException("Unknown column");
            }
        }

        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES",
                justification = "better to keep cases in column order rather than to combine")
        public int getPreferredWidth(int col) {
            switch (col) {
                case SNAME_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                case UNAME_COLUMN:
                    return new JTextField(17).getPreferredSize().width;
                case THREAD_COLUMN:
                    return new JTextField(10).getPreferredSize().width;
                case BUTTON_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                case BUTTON_DEBUG_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                case BUTTON_DELETE_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                case BUTTON_EDIT_THREADS_COLUMN:
                    return new JTextField(12).getPreferredSize().width;
                default:
                    throw new IllegalArgumentException("Unknown column");
            }
        }

        @Override
        public Object getValueAt(int r, int col) {
            int rx = r;
            if ((rx > _numConditionalNGs) || (_curLogixNG == null)) {
                return null;
            }
            switch (col) {
                case BUTTON_COLUMN:
                    if (!_inReorderMode) {
                        return Bundle.getMessage("ButtonEdit");  // NOI18N
                    } else if (_nextInOrder == 0) {
                        return Bundle.getMessage("ButtonFirst");  // NOI18N
                    } else if (_nextInOrder <= r) {
                        return Bundle.getMessage("ButtonNext");  // NOI18N
                    } else {
                        return Integer.toString(rx + 1);
                    }
                case BUTTON_DEBUG_COLUMN:
                    return Bundle.getMessage("ConditionalNG_Table_ButtonDebug");  // NOI18N
                case BUTTON_DELETE_COLUMN:
                    return Bundle.getMessage("ButtonDelete");  // NOI18N
                case BUTTON_EDIT_THREADS_COLUMN:
                    return Bundle.getMessage("ConditionalNG_Table_ButtonEditThreads");  // NOI18N
                case SNAME_COLUMN:
                    return _curLogixNG.getConditionalNG(rx);
                case UNAME_COLUMN: {
                    //log.debug("ConditionalNGTableModel: {}", _curLogixNG.getConditionalNGByNumberOrder(rx));  // NOI18N
                    ConditionalNG c = _curLogixNG.getConditionalNG(rx);
                    if (c != null) {
                        return c.getUserName();
                    }
                    return "";
                }
                case THREAD_COLUMN:
                    if (_showStartupThreads) {
                        return LogixNG_Thread.getThread(
                                _curLogixNG.getConditionalNG(r).getStartupThreadId())
                                .getThreadName();
                    } else {
                        return _curLogixNG.getConditionalNG(r).getCurrentThread().getThreadName();
                    }
                default:
                    throw new IllegalArgumentException("Unknown column");
            }
        }

        private void buttomColumnClicked(int row, int col) {
            if (_inReorderMode) {
                swapConditionalNG(row);
            } else {
                // Use separate Runnable so window is created on top
                class WindowMaker implements Runnable {

                    int row;

                    WindowMaker(int r) {
                        row = r;
                    }

                    @Override
                    public void run() {
                        editConditionalNGPressed(row);
                    }
                }
                WindowMaker t = new WindowMaker(row);
                javax.swing.SwingUtilities.invokeLater(t);
            }
        }

        private void buttomDebugClicked(int row, int col) {
            if (_inReorderMode) {
                swapConditionalNG(row);
            } else {
                // Use separate Runnable so window is created on top
                class WindowMaker implements Runnable {

                    int row;

                    WindowMaker(int r) {
                        row = r;
                    }

                    @Override
                    public void run() {
                        debugConditionalNGPressed(row);
                    }
                }
                WindowMaker t = new WindowMaker(row);
                javax.swing.SwingUtilities.invokeLater(t);
            }
        }

        private void deleteConditionalNG(int row) {
            DeleteBeanWorker worker = new DeleteBeanWorker(_curLogixNG.getConditionalNG(row), row);
            worker.execute();
        }

        private void changeUserName(Object value, int row) {
            String uName = (String) value;
            ConditionalNG cn = _curLogixNG.getConditionalNGByUserName(uName);
            if (cn == null) {
                ConditionalNG cdl = _curLogixNG.getConditionalNG(row);
                cdl.setUserName(uName.trim()); // N11N
                fireTableRowsUpdated(row, row);
            } else {
                // Duplicate user name
                if (cn != _curLogixNG.getConditionalNG(row)) {
                    messageDuplicateConditionalNGUserName(cn.getSystemName());
                }
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if ((row > _numConditionalNGs) || (_curLogixNG == null)) {
                return;
            }
            switch (col) {
                case BUTTON_COLUMN:
                    buttomColumnClicked(row, col);
                    break;
                case BUTTON_DEBUG_COLUMN:
                    buttomDebugClicked(row, col);
                    break;
                case BUTTON_DELETE_COLUMN:
                    deleteConditionalNG(row);
                    break;
                case BUTTON_EDIT_THREADS_COLUMN:
                    EditThreadsDialog dialog = new EditThreadsDialog(_curLogixNG.getConditionalNG(row));
                    dialog.showDialog();
                    break;
                case SNAME_COLUMN:
                    throw new IllegalArgumentException("System name cannot be changed");
                case UNAME_COLUMN: {
                    changeUserName(value, row);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown column");
            }
        }
    }

    /**
     * Send a duplicate Conditional user name message for Edit Logix pane.
     *
     * @param svName proposed name that duplicates an existing name
     */
    void messageDuplicateConditionalNGUserName(String svName) {
        JOptionPane.showMessageDialog(null,
                Bundle.getMessage("Error30", svName),
                Bundle.getMessage("ErrorTitle"), // NOI18N
                JOptionPane.ERROR_MESSAGE);
    }

    private String getClassName() {
        // The class that is returned must have a default constructor,
        // a constructor with no parameters.
        return jmri.jmrit.logixng.LogixNG_UserPreferences.class.getName();
    }


    // ------------ LogixNG Notifications ------------
    // The ConditionalNG views support some direct changes to the parent logix.
    // This custom event is used to notify the parent LogixNG that changes are requested.
    // When the event occurs, the parent LogixNG can retrieve the necessary information
    // to carry out the actions.
    //
    // 1) Notify the calling LogixNG that the LogixNG user name has been changed.
    // 2) Notify the calling LogixNG that the conditional view is closing
    // 3) Notify the calling LogixNG that it is to be deleted
    /**
     * Create a custom listener event.
     */
    public interface LogixNGEventListener extends EventListener {

        void logixNGEventOccurred();
    }

    /**
     * Maintain a list of listeners -- normally only one.
     */
    List<EditorEventListener> listenerList = new ArrayList<>();

    /**
     * This contains a list of commands to be processed by the listener
     * recipient.
     */
    private HashMap<String, String> logixNG_Data = new HashMap<>();

    /**
     * Add a listener.
     *
     * @param listener The recipient
     */
    @Override
    public void addEditorEventListener(EditorEventListener listener) {
        listenerList.add(listener);
    }

    /**
     * Remove a listener -- not used.
     *
     * @param listener The recipient
     */
    @Override
    public void removeEditorEventListener(EditorEventListener listener) {
        listenerList.remove(listener);
    }

    /**
     * Notify the listeners to check for new data.
     */
    private void fireEditorEvent() {
        for (EditorEventListener l : listenerList) {
            l.editorEventOccurred(logixNG_Data);
        }
    }


    private class LogixNGEventListenerImpl implements ConditionalNGEditor.ConditionalNGEventListener {

        private final LogixNGEditor _logixNGEditor;

        public LogixNGEventListenerImpl(LogixNGEditor logixNGEditor) {
            this._logixNGEditor = logixNGEditor;
        }

        @Override
        public void conditionalNGEventOccurred() {
            String lgxName = _curLogixNG.getSystemName();
            _treeEdit.logixNGData.forEach((key, value) -> {
                if (key.equals("Finish")) {                  // NOI18N
                    _treeEdit = null;
                    _inEditConditionalNGMode = false;
                    _logixNGEditor.bringToFront();
                } else if (key.equals("Delete")) {           // NOI18N
                    deletePressed();
                } else if (key.equals("chgUname")) {         // NOI18N
                    LogixNG x = _logixNG_Manager.getBySystemName(lgxName);
                    if (x == null) {
                        log.error("Found no logixNG for name {} when changing user name (2)", lgxName);
                        return;
                    }
                    x.setUserName(value);
                    beanTableDataModel.fireTableDataChanged();
                }
            });
        }
    }


    private class LogixNG_DebuggerEventListenerImpl
            implements ConditionalNGDebugger.ConditionalNGEventListener {

        private final LogixNGEditor _logixNGEditor;

        public LogixNG_DebuggerEventListenerImpl(LogixNGEditor logixNGEditor) {
            this._logixNGEditor = logixNGEditor;
        }

        @Override
        public void conditionalNGEventOccurred() {
            String lgxName = _curLogixNG.getSystemName();
            _debugger.logixNGData.forEach((key, value) -> {
                if (key.equals("Finish")) {                  // NOI18N
                    _debugger = null;
                    _inEditConditionalNGMode = false;
                    _logixNGEditor.bringToFront();
                } else if (key.equals("Delete")) {           // NOI18N
                    deletePressed();
                } else if (key.equals("chgUname")) {         // NOI18N
                    LogixNG x = _logixNG_Manager.getBySystemName(lgxName);
                    if (x == null) {
                        log.error("Found no logixNG for name {} when changing user name (2)", lgxName);
                        return;
                    }
                    x.setUserName(value);
                    beanTableDataModel.fireTableDataChanged();
                }
            });
        }
    }


    // This class is copied from BeanTableDataModel
    private class DeleteBeanWorker extends SwingWorker<Void, Void> {

        private final ConditionalNG _conditionalNG;
        private final int _row;
        boolean _hasDeleted = false;

        public DeleteBeanWorker(ConditionalNG conditionalNG, int row) {
            _conditionalNG = conditionalNG;
            _row = row;
        }

        public int getDisplayDeleteMsg() {
            return InstanceManager.getDefault(UserPreferencesManager.class).getMultipleChoiceOption(TreeEditor.class.getName(), "deleteInUse");
        }

        public void setDisplayDeleteMsg(int boo) {
            InstanceManager.getDefault(UserPreferencesManager.class).setMultipleChoiceOption(TreeEditor.class.getName(), "deleteInUse", boo);
        }

        public void doDelete() {
            try {
                InstanceManager.getDefault(ConditionalNG_Manager.class).deleteBean(_conditionalNG, "DoDelete");  // NOI18N
                _conditionalNGTableModel.fireTableRowsDeleted(_row, _row);
                _numConditionalNGs--;
                _showReminder = true;
                _hasDeleted = true;
            } catch (PropertyVetoException e) {
                //At this stage the DoDelete shouldn't fail, as we have already done a can delete, which would trigger a veto
                log.error(e.getMessage());
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Void doInBackground() {
            _conditionalNG.getFemaleSocket().unregisterListeners();

            StringBuilder message = new StringBuilder();
            try {
                InstanceManager.getDefault(ConditionalNG_Manager.class).deleteBean(_conditionalNG, "CanDelete");  // NOI18N
            } catch (PropertyVetoException e) {
                if (e.getPropertyChangeEvent().getPropertyName().equals("DoNotDelete")) { // NOI18N
                    log.warn(e.getMessage());
                    message.append(Bundle.getMessage("VetoDeleteBean", _conditionalNG.getBeanType(), _conditionalNG.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME), e.getMessage()));
                    JOptionPane.showMessageDialog(null, message.toString(),
                            Bundle.getMessage("WarningTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                message.append(e.getMessage());
            }
            List<String> listenerRefs = new ArrayList<>();
            _conditionalNG.getListenerRefsIncludingChildren(listenerRefs);
            int listenerRefsCount = listenerRefs.size();
            log.debug("Delete with {}", listenerRefsCount);
            if (getDisplayDeleteMsg() == 0x02 && message.toString().isEmpty()) {
                doDelete();
            } else {
                final JDialog dialog = new JDialog();
                dialog.setTitle(Bundle.getMessage("WarningTitle"));
                dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                JPanel container = new JPanel();
                container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

                if (listenerRefsCount > 0) { // warn of listeners attached before delete
                    String prompt = _conditionalNG.getFemaleSocket().isConnected()
                            ? "DeleteWithChildrenPrompt" : "DeletePrompt";
                    JLabel question = new JLabel(Bundle.getMessage(prompt, _conditionalNG.getDisplayName(NamedBean.DisplayOptions.USERNAME_SYSTEMNAME)));
                    question.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.add(question);

                    ArrayList<String> listeners = new ArrayList<>();
                    for (String listenerRef : listenerRefs) {
                        if (!listeners.contains(listenerRef)) {
                            listeners.add(listenerRef);
                        }
                    }

                    message.append("<br>");
                    message.append(Bundle.getMessage("ReminderInUse", listenerRefsCount));
                    message.append("<ul>");
                    for (String listener : listeners) {
                        message.append("<li>");
                        message.append(listener);
                        message.append("</li>");
                    }
                    message.append("</ul>");

                    JEditorPane pane = new JEditorPane();
                    pane.setContentType("text/html");
                    pane.setText("<html>" + message.toString() + "</html>");
                    pane.setEditable(false);
                    JScrollPane jScrollPane = new JScrollPane(pane);
                    container.add(jScrollPane);
                } else {
                    String prompt = _conditionalNG.getFemaleSocket().isConnected()
                            ? "DeleteWithChildrenPrompt" : "DeletePrompt";
                    String msg = MessageFormat.format(
                            Bundle.getMessage(prompt), _conditionalNG.getSystemName());
                    JLabel question = new JLabel(msg);
                    question.setAlignmentX(Component.CENTER_ALIGNMENT);
                    container.add(question);
                }

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

                noButton.addActionListener((ActionEvent e) -> {
                    //there is no point in remembering this the user will never be
                    //able to delete a bean!
                    dialog.dispose();
                });

                yesButton.addActionListener((ActionEvent e) -> {
                    if (remember.isSelected()) {
                        setDisplayDeleteMsg(0x02);
                    }
                    doDelete();
                    dialog.dispose();
                });
                container.add(remember);
                container.setAlignmentX(Component.CENTER_ALIGNMENT);
                container.setAlignmentY(Component.CENTER_ALIGNMENT);
                dialog.getContentPane().add(container);
                dialog.pack();
                
                dialog.getRootPane().setDefaultButton(noButton);
                noButton.requestFocusInWindow(); // set default keyboard focus, after pack() before setVisible(true)
                dialog.getRootPane().registerKeyboardAction(e -> { // escape to exit
                        dialog.setVisible(false);
                        dialog.dispose(); }, 
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

                dialog.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - dialog.getWidth() / 2, (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - dialog.getHeight() / 2);
                dialog.setModal(true);
                dialog.setVisible(true);
            }
            if (!_hasDeleted && _conditionalNG.getFemaleSocket().isActive()) _conditionalNG.getFemaleSocket().registerListeners();
            return null;
        }

        /**
         * {@inheritDoc} Minimal implementation to catch and log errors
         */
        @Override
        protected void done() {
            try {
                get();  // called to get errors
            } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                log.error("Exception while deleting bean", e);
            }
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNGEditor.class);

}
