package jmri.jmrit.conditional;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;

import jmri.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.conditional.Bundle;
import jmri.jmrit.sensorgroup.SensorGroupFrame;
import jmri.util.JmriJFrame;
import jmri.util.swing.JComboBoxUtil;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * The traditional list based conditional editor based on the original editor
 * included in LogixTableAction.
 * <p>
 * Conditionals now have two policies to trigger execution of their action
 * lists:
 * <ol>
 *   <li>the previous policy - Trigger on change of state only
 *   <li>the new default - Trigger on any enabled state calculation
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
 * Add a Browse Option to the Logix Select Menu. This will display a window that
 * creates a formatted list of the contents of the selected Logix with each
 * Conditional, Variable and Action. The code is courtesy of Chuck Catania and
 * is used with his permission. Apr 2, 2017 - Dave Sand
 * <p>
 * Compare with the other Conditional Edit tool {@link ConditionalTreeEdit}
 *
 * @author Dave Duchamp Copyright (C) 2007
 * @author Pete Cressman Copyright (C) 2009, 2010, 2011
 * @author Matthew Harris copyright (c) 2009
 * @author Dave Sand copyright (c) 2017
 */
public class ConditionalListEdit extends ConditionalEditBase {

    /**
     * Create a new Conditional List View editor.
     *
     * @param sName name of the Logix being edited
     */
    public ConditionalListEdit(String sName) {
        super(sName);
        makeEditLogixWindow();
    }

    public ConditionalListEdit() {
    }

    // ------------ Logix Variables ------------
    JTextField _editUserName;
    JLabel _status;

    // ------------ Conditional Variables ------------
    ConditionalTableModel conditionalTableModel = null;
    Conditional _curConditional = null;
    int _conditionalRowNumber = 0;
    boolean _inReorderMode = false;
    int _nextInOrder = 0;

    ConditionalEditFrame _editConditionalFrame = null;

    JComboBox<String> _selectLogixBox = new JComboBox<>();
    JComboBox<String> _selectConditionalBox = new JComboBox<>();
    TreeMap<String, String> _selectLogixMap = new TreeMap<>();
    ArrayList<String> _selectConditionalList = new ArrayList<>();

    static final int STRUT = 10;

    // ------------ Methods for Edit Logix Pane ------------

    /**
     * Create and/or initialize the Edit Logix pane.
     */
    void makeEditLogixWindow() {
        _editUserName = new JTextField(20);
        _editUserName.setText(_curLogix.getUserName());
        // clear conditional table if needed
        if (conditionalTableModel != null) {
            conditionalTableModel.fireTableStructureChanged();
        }
        _inEditMode = true;
        if (_editLogixFrame == null) {
            _editLogixFrame = new JmriJFrame(Bundle.getMessage("TitleEditLogix"), false, false);  // NOI18N
            _editLogixFrame.addHelpMenu(
                    "package.jmri.jmrit.conditional.ConditionalListEditor", true);  // NOI18N
            _editLogixFrame.setLocation(100, 30);
            Container contentPane = _editLogixFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel systemNameLabel = new JLabel(Bundle.getMessage("ColumnSystemName") + ":");  // NOI18N
            panel1.add(systemNameLabel);
            JLabel fixedSystemName = new JLabel(_curLogix.getSystemName());
            panel1.add(fixedSystemName);
            contentPane.add(panel1);
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel userNameLabel = new JLabel(Bundle.getMessage("ColumnUserName") + ":");  // NOI18N
            panel2.add(userNameLabel);
            panel2.add(_editUserName);
            _editUserName.setToolTipText(Bundle.getMessage("LogixUserNameHint2"));  // NOI18N
            contentPane.add(panel2);
            // add table of Conditionals
            JPanel pctSpace = new JPanel();
            pctSpace.setLayout(new FlowLayout());
            pctSpace.add(new JLabel("   "));
            contentPane.add(pctSpace);
            JPanel pTitle = new JPanel();
            pTitle.setLayout(new FlowLayout());
            pTitle.add(new JLabel(Bundle.getMessage("ConditionalTableTitle")));  // NOI18N
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
            JButton testButton = new JButton("XXXXXX");  // NOI18N
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
            _status = new JLabel(" ");
            panel41.add(_status);
            panel4.add(panel41);
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
            // Conditional panel buttons - New Conditional
            JButton newConditionalButton = new JButton(Bundle.getMessage("NewConditionalButton"));  // NOI18N
            panel42.add(newConditionalButton);
            newConditionalButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    newConditionalPressed(e);
                }
            });
            newConditionalButton.setToolTipText(Bundle.getMessage("NewConditionalButtonHint"));  // NOI18N
            // Conditional panel buttons - Reorder
            JButton reorderButton = new JButton(Bundle.getMessage("ReorderButton"));  // NOI18N
            panel42.add(reorderButton);
            reorderButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    reorderPressed(e);
                }
            });
            reorderButton.setToolTipText(Bundle.getMessage("ReorderButtonHint"));  // NOI18N
            // Conditional panel buttons - Calculate
            JButton calculateButton = new JButton(Bundle.getMessage("CalculateButton"));  // NOI18N
            panel42.add(calculateButton);
            calculateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    calculatePressed(e);
                }
            });
            calculateButton.setToolTipText(Bundle.getMessage("CalculateButtonHint"));  // NOI18N
            panel4.add(panel42);
            Border panel4Border = BorderFactory.createEtchedBorder();
            panel4.setBorder(panel4Border);
            contentPane.add(panel4);
            // add buttons at bottom of window
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            // Bottom Buttons - Done Logix
            JButton done = new JButton(Bundle.getMessage("ButtonDone"));  // NOI18N
            panel5.add(done);
            done.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    donePressed(e);
                }
            });
            done.setToolTipText(Bundle.getMessage("DoneButtonHint"));  // NOI18N
            // Delete Logix
            JButton delete = new JButton(Bundle.getMessage("ButtonDelete"));  // NOI18N
            panel5.add(delete);
            delete.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deletePressed(e);
                }
            });
            delete.setToolTipText(Bundle.getMessage("DeleteLogixButtonHint"));  // NOI18N
            contentPane.add(panel5);
        }

        _editLogixFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                donePressed(null);
            }
        });
        _editLogixFrame.pack();
        _editLogixFrame.setVisible(true);
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
            JOptionPane.showMessageDialog(_editLogixFrame,
                    Bundle.getMessage("Warn8", SensorGroupFrame.logixUserName, SensorGroupFrame.logixSysName),
                    Bundle.getMessage("WarningTitle"), // NOI18N
                    JOptionPane.WARNING_MESSAGE);
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
            log.error("Failure to create Conditional with System Name: {}", cName);
            return;
        }
        // add to Logix at the end of the calculate order
        _curLogix.addConditional(cName, -1);
        conditionalTableModel.fireTableRowsInserted(_numConditionals, _numConditionals);
        _conditionalRowNumber = _numConditionals;
        _numConditionals++;
        _showReminder = true;
        // clear action items
        makeEditConditionalWindow();
    }

    /**
     * Respond to Edit Button in the Conditional table of the Edit Logix Window.
     *
     * @param rx index (row number) of Conditional to be edited
     */
    void editConditionalPressed(int rx) {
        if (checkEditConditional()) {
            return;
        }
        // get Conditional to edit
        _curConditional = _conditionalManager.getBySystemName(_curLogix.getConditionalByNumberOrder(rx));
        if (_curConditional == null) {
            log.error("Attempted edit of non-existant conditional.");  // NOI18N
            return;
        }
        _conditionalRowNumber = rx;
        makeEditConditionalWindow();
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
        _status.setText(Bundle.getMessage("ReorderMessage"));  // NOI18N
        conditionalTableModel.fireTableDataChanged();
    }

    /**
     * Responds to the Calculate Button in the Edit Logix window.
     *
     * @param e The event heard
     */
    void calculatePressed(ActionEvent e) {
        if (checkEditConditional()) {
            return;
        }
        // are there Conditionals to calculate?
        if (_numConditionals > 0) {
            // There are conditionals to calculate
            String cName = "";
            Conditional c = null;
            for (int i = 0; i < _numConditionals; i++) {
                cName = _curLogix.getConditionalByNumberOrder(i);
                if (cName != null) {
                    c = _conditionalManager.getBySystemName(cName);
                    if (c == null) {
                        log.error("Invalid conditional system name when calculating - {}", cName);
                    } else {
                        // calculate without taking any action
                        c.calculate(false, null);
                    }
                } else {
                    log.error("null conditional system name when calculating");  // NOI18N
                }
            }
            // force the table to update
            conditionalTableModel.fireTableDataChanged();
        }
    }

    /**
     * Respond to the Done button in the Edit Logix window.
     * <p>
     * Note: We also get here if the Edit Logix window is dismissed, or if the
     * Add button is pressed in the Logic Table with an active Edit Logix
     * window.
     *
     * @param e The event heard
     */
    void donePressed(ActionEvent e) {
        if (checkEditConditional()) {
            return;
        }
        if (_curLogix.getSystemName().equals(SensorGroupFrame.logixSysName)) {
            finishDone();
            return;
        }
        // Check if the User Name has been changed
        String uName = _editUserName.getText().trim();
        if (!(uName.equals(_curLogix.getUserName()))) {
            // user name has changed - check if already in use
            if (uName.length() > 0) {
                Logix p = _logixManager.getByUserName(uName);
                if (p != null) {
                    // Logix with this user name already exists
                    log.error("Failure to update Logix with Duplicate User Name: {}", uName);
                    JOptionPane.showMessageDialog(_editLogixFrame,
                            Bundle.getMessage("Error6"),
                            Bundle.getMessage("ErrorTitle"), // NOI18N
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            // user name is unique, change it
            // user name is unique, change it
            logixData.clear();
            logixData.put("chgUname", uName);  // NOI18N
            fireLogixEvent();
        }
        // complete update and activate Logix
        finishDone();
    }

    void finishDone() {
        showSaveReminder();
        _inEditMode = false;
        if (_editLogixFrame != null) {
            _editLogixFrame.setVisible(false);
            _editLogixFrame.dispose();
            _editLogixFrame = null;
        }
        logixData.clear();
        logixData.put("Finish", _curLogix.getSystemName());   // NOI18N
        fireLogixEvent();
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
        if (!checkConditionalReferences(_curLogix.getSystemName())) {
            return;
        }
        _showReminder = true;
        logixData.clear();
        logixData.put("Delete", _curLogix.getSystemName());   // NOI18N
        fireLogixEvent();
        finishDone();
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
        _numConditionals--;
        String[] msgs = _curLogix.deleteConditional(sName);
        if (msgs != null) {
            JOptionPane.showMessageDialog(_editLogixFrame,
                    Bundle.getMessage("Error11", (Object[]) msgs), // NOI18N
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);  // NOI18N
        }

        // complete deletion
        if (_pickTables != null) {
            _pickTables.dispose();
            _pickTables = null;
        }
        conditionalTableModel.fireTableRowsDeleted(_conditionalRowNumber,
                _conditionalRowNumber);
        if (_numConditionals < 1 && !_suppressReminder) {
            // warning message - last Conditional deleted
            JOptionPane.showMessageDialog(_editLogixFrame,
                    Bundle.getMessage("Warn1"),
                    Bundle.getMessage("WarningTitle"), // NOI18N
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Check if edit of a conditional is in progress.
     *
     * @return true if this is the case, after showing dialog to user
     */
    boolean checkEditConditional() {
        if (_editConditionalFrame != null) {
            if (_editConditionalFrame._dataChanged) {
                // Already editing a Conditional, ask for completion of that edit
                JOptionPane.showMessageDialog(_editConditionalFrame,
                        Bundle.getMessage("Error35", _curConditional.getSystemName()), // NOI18N
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return true;
            } else {
                _editConditionalFrame.cancelConditionalPressed();
            }
        }
        return false;
    }

    // ----------------- utilities for logix and Conditional variables

    /**
     * Load the Logix selection box. Set the selection to the current Logix.
     *
     * @since 4.7.4
     * @param curVariable Current ConditionalVariable
     */
    void loadSelectLogixBox(ConditionalVariable curVariable) {
        // Get the current Logix name for selecting the current combo box row
        String cdlName = curVariable.getName();
        String lgxName;
        if (cdlName.length() == 0 || (curVariable.getType() != Conditional.Type.CONDITIONAL_TRUE
                && curVariable.getType() != Conditional.Type.CONDITIONAL_FALSE)) {
            // Use the current logix name for "add" state variable
            lgxName = _curLogix.getSystemName();
        } else {
            Logix x = _conditionalManager.getParentLogix(cdlName);
            if (x == null) {
                log.error("Unable to find the Logix for {}, using the current Logix", cdlName);  // NOI18N
                lgxName = _curLogix.getSystemName();
            } else {
                lgxName = x.getSystemName();
            }
        }

        _selectLogixBox.removeAllItems();
        _selectLogixMap.clear();

        // Create Logix list sorted by a custom display name
        String itemKey = "";
        for (Logix lgx : _logixManager.getNamedBeanSet()) {
            String sName = lgx.getSystemName();
            if (sName.equals("SYS")) {  // NOI18N
                // Cannot refer to sensor name groups
                continue;
            }
            String uName = lgx.getUserName();
            String itemName = "";
            if (uName == null || uName.length() < 1) {
                itemName = sName;
            } else {
                itemName = uName + " ( " + sName + " )";
            }
            _selectLogixMap.put(itemName, sName);
            if (lgxName.equals(sName)) {
                itemKey = itemName;
            }
        }

        // Load the combo box
        for (String item : _selectLogixMap.keySet()) {
            _selectLogixBox.addItem(item);
        }

        JComboBoxUtil.setupComboBoxMaxRows(_selectLogixBox);
        _selectLogixBox.setSelectedItem(itemKey);
        loadSelectConditionalBox(lgxName, curVariable);
    }

    /**
     * Load the Conditional selection box. The first row is a prompt.
     *
     * @since 4.7.4
     * @param logixName The Logix system name for selecting the owned
     *                  Conditionals
     * @param curVariable Current ConditionalVariable
     */
    void loadSelectConditionalBox(String logixName, ConditionalVariable curVariable) {
        // Get the current Conditional name for selecting the current combo box row
        String cdlName = curVariable.getName();

        _selectConditionalBox.removeAllItems();
        _selectConditionalList.clear();

        // Create the first row
        String itemKey = Bundle.getMessage("SelectFirstRow");  // NOI18N
        _selectConditionalBox.addItem(itemKey);
        _selectConditionalList.add("-None-");  // NOI18N

        Logix x = _logixManager.getBySystemName(logixName);
        if (x == null) {
            log.error("Logix '{}' not found while building the conditional list", logixName);  // NOI18N
            return;
        }
        if (x.getNumConditionals() == 0) {
            return;
        }
        for (String cName : _conditionalManager.getSystemNameListForLogix(x)) {
            Conditional c = _conditionalManager.getConditional(cName);
            if (_curConditional.getSystemName().equals(c.getSystemName())) {
                // Don't add myself to the list
                continue;
            }
            String uName = c.getUserName();
            String itemName = "";
            if (uName == null || uName.length() < 1) {
                itemName = cName;
            } else {
                itemName = uName + " ( " + cName + " )";
            }
            _selectConditionalBox.addItem(itemName);
            _selectConditionalList.add(cName);
            if (cdlName.equals(cName)) {
                itemKey = itemName;
            }
        }
        JComboBoxUtil.setupComboBoxMaxRows(_selectConditionalBox);
        _selectConditionalBox.setSelectedItem(itemKey);
    }
    // ============ Edit Conditional Window and Methods ============

    /**
     * Create and/or initialize the Edit Conditional window.
     * <p>
     * Note: you can get here via the New Conditional button
     * (newConditionalPressed) or via an Edit button in the Conditional table of
     * the Edit Logix window.
     */
    void makeEditConditionalWindow() {
        // deactivate this Logix
        _curLogix.deActivateLogix();
        _editConditionalFrame = new ConditionalEditFrame(Bundle.getMessage("TitleEditConditional"), _curConditional, this);  // NOI18N

        // add Delete Conditional button to bottom panel
        JButton deleteConditional = new JButton(Bundle.getMessage("ButtonDelete"));  // NOI18N
        _editConditionalFrame._bottomPanel.add(deleteConditional);
        deleteConditional.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteConditionalPressed(null);
            }
        });
        deleteConditional.setToolTipText(Bundle.getMessage("DeleteConditionalButtonHint"));  // NOI18N

        _editConditionalFrame.pack();
        _editConditionalFrame.setVisible(true);
        InstanceManager.getDefault(jmri.util.PlaceWindow.class).nextTo(_editLogixFrame, null, _editConditionalFrame);
    }

    PickSingleListener getPickSingleListener(JTextField textField, Conditional.ItemType itemType) {
        return new PickSingleListener(textField, itemType);
    }

    void closeConditionalFrame() {
        conditionalTableModel.fireTableDataChanged();
        try {
            _curLogix.activateLogix();
        } catch (NumberFormatException nfe) {
            log.debug("NumberFormatException on activation of Logix ", nfe);  // NOI18N
            JOptionPane.showMessageDialog(_editLogixFrame,
                    Bundle.getMessage("Error4") + nfe.toString() + Bundle.getMessage("Error7"), // NOI18N
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);  // NOI18N
        }
        // when user uses the escape key and returns to editing, interaction with
        // window closing event create strange environment
        
        if (_editConditionalFrame != null) {
            _editConditionalFrame.dispose();
            _editConditionalFrame = null;
        }
        if (_editLogixFrame != null) {
            _editLogixFrame.setVisible(true);
        }
    }

    boolean checkConditionalUserName(String uName) {
        if ((uName != null) && (!(uName.equals("")))) {
            Conditional p = _conditionalManager.getByUserName(_curLogix, uName);
            if (p != null) {
                // Conditional with this user name already exists
                log.error("Failure to update Conditional with Duplicate User Name: {}", uName);
                JOptionPane.showMessageDialog(_editConditionalFrame,
                        Bundle.getMessage("Error10"),    // NOI18N
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } // else return false;
        return true;
    }

    // ------------ Methods for Edit Conditional Pane ------------

    boolean updateConditional(String uName) {
        // Check if the User Name has been changed
        if (!uName.equals(_curConditional.getUserName())) {
            // user name has changed - check if already in use
            if (!checkConditionalUserName(uName, _curLogix)) {
                return false;
            }
            // user name is unique or blank, change it
            _curConditional.setUserName(uName);
        }
        if (_editConditionalFrame._variableList.size() <= 0 && !_suppressReminder) {
            JOptionPane.showMessageDialog(_editLogixFrame,
                    Bundle.getMessage("Warn5", _curConditional.getUserName(), _curConditional.getSystemName()),
                    Bundle.getMessage("WarningTitle"), // NOI18N
                    JOptionPane.WARNING_MESSAGE);
        }
        // complete update
        _curConditional.setStateVariables(_editConditionalFrame._variableList);
        _curConditional.setAction(_editConditionalFrame._actionList);
        _curConditional.setTriggerOnChange(_editConditionalFrame._triggerOnChangeButton.isSelected());
        _curConditional.setLogicType(_editConditionalFrame._logicType, _editConditionalFrame._antecedent);
        TreeSet<String> newTargetNames = new TreeSet<String>();
        loadReferenceNames(_editConditionalFrame._variableList, newTargetNames);
        updateWhereUsed(_editConditionalFrame._oldTargetNames, newTargetNames, _curConditional.getSystemName());
        return true;        
    }

    /**
     * Respond to the First/Next (Delete/Reorder) Button in the ConditionalTableModel.
     *
     * @param row index of the row to put as next in line (instead of the one
     *            that was supposed to be next)
     */
    void swapConditional(int row) {
        _curLogix.swapConditional(_nextInOrder, row);
        _nextInOrder++;
        if (_nextInOrder >= _numConditionals) {
            _inReorderMode = false;
        }
        //status.setText("");
        conditionalTableModel.fireTableDataChanged();
    }

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
            _numConditionals = _curLogix.getNumConditionals();
            for (int i = 0; i < _numConditionals; i++) {
                // if object has been deleted, it's not here; ignore it
                sNam = _curLogix.getConditionalByNumberOrder(i);
                c = _conditionalManager.getBySystemName(sNam);
                if (c != null) {
                    c.removePropertyChangeListener(this);
                }
            }
            // and add them back in
            for (int i = 0; i < _numConditionals; i++) {
                sNam = _curLogix.getConditionalByNumberOrder(i);
                c = _conditionalManager.getBySystemName(sNam);
                if (c != null) {
                    c.addPropertyChangeListener(this);
                }
            }
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {  // NOI18N
                // a new NamedBean is available in the manager
                updateConditionalListeners();
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
            return (e.getPropertyName().contains("State") ||     // NOI18N
                    e.getPropertyName().contains("Appearance")); // NOI18N
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
            return (_numConditionals);
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            if (!_inReorderMode) {
                return ((c == UNAME_COLUMN) || (c == BUTTON_COLUMN));
            } else if (c == BUTTON_COLUMN) {
                return (r >= _nextInOrder);
            }
            return false;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case SNAME_COLUMN:
                    return Bundle.getMessage("ColumnSystemName");  // NOI18N
                case UNAME_COLUMN:
                    return Bundle.getMessage("ColumnUserName");  // NOI18N
                case BUTTON_COLUMN:
                    return ""; // no label
                case STATE_COLUMN:
                    return Bundle.getMessage("ColumnState");  // NOI18N
                default:
                    return "";
            }
        }

        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES",
                justification = "better to keep cases in column order rather than to combine")
        public int getPreferredWidth(int col) {
            switch (col) {
                case SNAME_COLUMN:
                case BUTTON_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                case UNAME_COLUMN:
                    return new JTextField(17).getPreferredSize().width;
                case STATE_COLUMN:
                    return new JTextField(12).getPreferredSize().width;
                default:
                    return new JTextField(5).getPreferredSize().width;
            }
        }

        @Override
        public Object getValueAt(int row, int col) {
            if ((row > _numConditionals) || (_curLogix == null)) {
                return null;
            }
            switch (col) {
                case BUTTON_COLUMN:
                    if (!_inReorderMode) {
                        return Bundle.getMessage("ButtonEdit");  // NOI18N
                    } else if (_nextInOrder == 0) {
                        return Bundle.getMessage("ButtonFirst");  // NOI18N
                    } else if (_nextInOrder <= row) {
                        return Bundle.getMessage("ButtonNext");  // NOI18N
                    } else {
                        return Integer.toString(row + 1);
                    }
                case SNAME_COLUMN:
                    return _curLogix.getConditionalByNumberOrder(row);
                case UNAME_COLUMN: {
                    //log.debug("ConditionalTableModel: {}", _curLogix.getConditionalByNumberOrder(row));  // NOI18N
                    Conditional c = _conditionalManager.getBySystemName(
                            _curLogix.getConditionalByNumberOrder(row));
                    if (c != null) {
                        return c.getUserName();
                    }
                    return "";
                }
                case STATE_COLUMN:
                    Conditional c = _conditionalManager.getBySystemName(
                            _curLogix.getConditionalByNumberOrder(row));
                    if (c != null) {
                        int curState = c.getState();
                        if (curState == Conditional.TRUE) {
                            return Bundle.getMessage("True");  // NOI18N
                        }
                        if (curState == Conditional.FALSE) {
                            return Bundle.getMessage("False");  // NOI18N
                        }
                    }
                    return Bundle.getMessage("BeanStateUnknown");  // NOI18N
                default:
                    return Bundle.getMessage("BeanStateUnknown");  // NOI18N
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if ((row > _numConditionals) || (_curLogix == null)) {
                return;
            }
            if (col == BUTTON_COLUMN) {
                if (_inReorderMode) {
                    swapConditional(row);
                } else if (_curLogix.getSystemName().equals(SensorGroupFrame.logixSysName)) {
                    JOptionPane.showMessageDialog(_editConditionalFrame,
                            Bundle.getMessage("Warn8", SensorGroupFrame.logixUserName, SensorGroupFrame.logixSysName),
                            Bundle.getMessage("WarningTitle"),
                            JOptionPane.WARNING_MESSAGE);  // NOI18N
                } else {
                    // Use separate Runnable so window is created on top
                    class WindowMaker implements Runnable {

                        private int _row;

                        WindowMaker(int r) {
                            _row = r;
                        }

                        @Override
                        public void run() {
                            editConditionalPressed(_row);
                        }
                    }
                    WindowMaker t = new WindowMaker(row);
                    javax.swing.SwingUtilities.invokeLater(t);
                }
            } else if (col == UNAME_COLUMN) {
                String uName = (String) value;
                Conditional cn = _conditionalManager.getByUserName(_curLogix, uName);
                if (cn == null) {
                    String sName = _curLogix.getConditionalByNumberOrder(row);
                    Conditional cdl = _conditionalManager.getBySystemName(sName);
                    if (cdl==null){
                        log.error("No conditional {} while editing user name",sName);
                        return;
                    }
                    cdl.setUserName(uName);
                    fireTableRowsUpdated(row, row);

                    // Update any conditional references
                    ArrayList<String> refList = InstanceManager.getDefault(jmri.ConditionalManager.class).getWhereUsed(sName);
                    if (refList != null) {
                        for (String ref : refList) {
                            Conditional cRef = _conditionalManager.getBySystemName(ref);
                            if (cRef==null){
                                continue;
                            }
                            List<ConditionalVariable> varList = cRef.getCopyOfStateVariables();
                            for (ConditionalVariable var : varList) {
                                // Find the affected conditional variable
                                if (var.getName().equals(sName)) {
                                    var.setGuiName( (uName.length() > 0) ? uName : sName );
                                }
                            }
                            cRef.setStateVariables(varList);
                        }
                    }
                } else {
                    // Duplicate user name
                    String svName = _curLogix.getConditionalByNumberOrder(row);
                    if (cn != _conditionalManager.getBySystemName(svName)) {
                        messageDuplicateConditionalUserName(cn.getSystemName());
                    }
                }
            }
        }
    }


    @Override
    protected String getClassName() {
        return ConditionalListEdit.class.getName();
    }

    private final static Logger log = LoggerFactory.getLogger(ConditionalListEdit.class);
}
