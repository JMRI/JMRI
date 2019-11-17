package jmri.jmrit.conditional;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.Audio;
import jmri.Conditional;
import jmri.Conditional.Operator;
import jmri.Conditional.State;
import jmri.ConditionalAction;
import jmri.ConditionalVariable;
import jmri.InstanceManager;
import jmri.Light;
import jmri.Logix;
import jmri.NamedBean;
import jmri.Route;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.Turnout;
import jmri.implementation.DefaultConditionalAction;
import jmri.jmrit.beantable.LRouteTableAction;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.sensorgroup.SensorGroupFrame;
import jmri.util.FileUtil;
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
    JmriJFrame _editLogixFrame = null;
    JTextField editUserName = new JTextField(20);
    JLabel status = new JLabel(" ");

    // ------------ Conditional Variables ------------
    ConditionalTableModel conditionalTableModel = null;
    Conditional _curConditional = null;
    int _conditionalRowNumber = 0;
    boolean _inReorderMode = false;
    boolean _inActReorder = false;
    boolean _inVarReorder = false;
    int _nextInOrder = 0;

    // ------------ Select Logix/Conditional Variables ------------
    JPanel _selectLogixPanel = null;
    JPanel _selectConditionalPanel = null;
    JComboBox<String> _selectLogixBox = new JComboBox<>();
    JComboBox<String> _selectConditionalBox = new JComboBox<>();
    TreeMap<String, String> _selectLogixMap = new TreeMap<>();
    ArrayList<String> _selectConditionalList = new ArrayList<>();

    // ------------ Edit Conditional Variables ------------
    boolean _inEditConditionalMode = false;
    JmriJFrame _editConditionalFrame = null;
    JTextField _conditionalUserName = new JTextField(22);
    JRadioButton _triggerOnChangeButton;

    ActionTableModel _actionTableModel = null;
    VariableTableModel _variableTableModel = null;
    JComboBox<Conditional.AntecedentOperator> _operatorBox;
    JComboBox<String> _andOperatorBox;
    JComboBox<String> _notOperatorBox;
    JTextField _antecedentField;
    JPanel _antecedentPanel;
    Conditional.AntecedentOperator _logicType = Conditional.AntecedentOperator.ALL_AND;
    String _antecedent = null;
    boolean _newItem = false; // marks a new Action or Variable object was added

    // ------------ Components of Edit Variable panes ------------
    JmriJFrame _editVariableFrame = null;
    JComboBox<Conditional.ItemType> _variableItemBox;
    JComboBox<Conditional.Type> _variableStateBox;
    JTextField _variableNameField;
    JComboBox<String> _variableCompareOpBox;
    JComboBox<String> _variableSignalBox;
    JComboBox<Conditional.Type> _variableCompareTypeBox;
    JTextField _variableData1Field;
    JTextField _variableData2Field;
    JButton _reorderVarButton;
    JPanel _variableNamePanel;
    JPanel _variableStatePanel;
    JPanel _variableComparePanel;
    JPanel _variableSignalPanel;
    JPanel _variableData1Panel;
    JPanel _variableData2Panel;
    JPanel _variableComboNamePanel;

    // ------------ Components of Edit Action panes ------------
    JmriJFrame _editActionFrame = null;
    JComboBox<Conditional.ItemType> _actionItemBox;
    JComboBox<Conditional.Action> _actionTypeBox;
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
    JPanel _actionComboNamePanel;

    JButton _actionSetButton;
    JPanel _setPanel;
    JPanel _textPanel;

    // ------------ Current Variable Information ------------
    List<ConditionalVariable> _variableList;
    ConditionalVariable _curVariable;
    int _curVariableRowNumber;
    Conditional.ItemType _curVariableItem = Conditional.ItemType.NONE;
    TreeSet<String> _oldTargetNames = new TreeSet<>();

    // ------------ Current Action Information ------------
    List<ConditionalAction> _actionList;
    ConditionalAction _curAction;
    int _curActionRowNumber;
    Conditional.ItemType _curActionItem = Conditional.ItemType.NONE;

    static final int STRUT = 10;

    // ------------ Methods for Edit Logix Pane ------------

    /**
     * Create and/or initialize the Edit Logix pane.
     */
    void makeEditLogixWindow() {
        editUserName.setText(_curLogix.getUserName());
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
            panel2.add(editUserName);
            editUserName.setToolTipText(Bundle.getMessage("LogixUserNameHint2"));  // NOI18N
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
            panel41.add(status);
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
                if (_inEditMode) {
                    donePressed(null);
                } else {
                    finishDone();
                }
            }
        });
        _editLogixFrame.pack();
        _editLogixFrame.setVisible(true);
    }

    public void bringToFront() {
        if (_editLogixFrame != null) {
            _editLogixFrame.setVisible(true);
        }
    }

    /**
     * Display reminder to save.
     */
    @Override
    void showSaveReminder() {
        /*if (_showReminder && !_suppressReminder) {
         JOptionPane.showMessageDialog(editLogixFrame,
         Bundle.getMessage("Reminder1"),
         Bundle.getMessage("ReminderTitle"),
         JOptionPane.INFORMATION_MESSAGE);
         }*/
        if (_showReminder) {
            if (InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class) != null) {
                InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showInfoMessage(Bundle.getMessage("ReminderTitle"), // NOI18N
                                Bundle.getMessage("ReminderSaveString", // NOI18N
                                        Bundle.getMessage("MenuItemLogixTable")), // NOI18N
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
        status.setText(Bundle.getMessage("ReorderMessage"));  // NOI18N
        conditionalTableModel.fireTableDataChanged();
    }

    /**
     * Respond to the First/Next (Delete) Button in the Edit Logix window.
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
                        log.error("Invalid conditional system name when calculating - " // NOI18N
                                + cName);
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
        String uName = editUserName.getText().trim();
        if (!(uName.equals(_curLogix.getUserName()))) {
            // user name has changed - check if already in use
            if (uName.length() > 0) {
                Logix p = _logixManager.getByUserName(uName);
                if (p != null) {
                    // Logix with this user name already exists
                    log.error("Failure to update Logix with Duplicate User Name: " // NOI18N
                            + uName);
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
            log.error("Failure to create Conditional with System Name: " // NOI18N
                    + cName);
            return;
        }
        // add to Logix at the end of the calculate order
        _curLogix.addConditional(cName, -1);
        conditionalTableModel.fireTableRowsInserted(_numConditionals, _numConditionals);
        _conditionalRowNumber = _numConditionals;
        _numConditionals++;
        _showReminder = true;
        // clear action items
        _actionList = new ArrayList<>();
        _variableList = new ArrayList<>();
        _oldTargetNames.clear();
        makeEditConditionalWindow();
    }

    // ------------ Methods for Edit Conditional Pane ------------

    /**
     * Respond to Edit Button in the Conditional table of the Edit Logix Window.
     *
     * @param rx index (row number) of Conditional to be edited
     */
    void editConditionalPressed(int rx) {
        if (_inEditConditionalMode) {
            // Already editing a Conditional, ask for completion of that edit
            JOptionPane.showMessageDialog(_editConditionalFrame,
                    Bundle.getMessage("Error34", _curConditional.getSystemName()),
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        // get Conditional to edit
        _curConditional = _conditionalManager.getBySystemName(_curLogix.getConditionalByNumberOrder(rx));
        if (_curConditional == null) {
            log.error("Attempted edit of non-existant conditional.");  // NOI18N
            return;
        }
        _variableList = _curConditional.getCopyOfStateVariables();
        _conditionalRowNumber = rx;
        loadReferenceNames(_variableList, _oldTargetNames);
        // get action variables
        _actionList = _curConditional.getCopyOfActions();
        makeEditConditionalWindow();
    }

    /**
     * Check if edit of a conditional is in progress.
     *
     * @return true if this is the case, after showing dialog to user
     */
    boolean checkEditConditional() {
        if (_inEditConditionalMode) {
            // Already editing a Conditional, ask for completion of that edit
            JOptionPane.showMessageDialog(_editConditionalFrame,
                    Bundle.getMessage("Error35", _curConditional.getSystemName()), // NOI18N
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return true;
        }
        return false;
    }

    @Override
    boolean checkConditionalUserName(String uName, Logix logix) {
        if ((uName != null) && (!(uName.equals("")))) {
            Conditional p = _conditionalManager.getByUserName(logix, uName);
            if (p != null) {
                // Conditional with this user name already exists
                log.error("Failure to update Conditional with Duplicate User Name: " // NOI18N
                        + uName);
                JOptionPane.showMessageDialog(_editConditionalFrame,
                        Bundle.getMessage("Error10"),    // NOI18N
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
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
        _conditionalUserName.setText(_curConditional.getUserName());
        if (_editConditionalFrame == null) {
            _editConditionalFrame = new JmriJFrame(Bundle.getMessage("TitleEditConditional"), false, false);  // NOI18N
            _editConditionalFrame.addHelpMenu(
                    "package.jmri.jmrit.conditional.ConditionalListEditor", true);  // NOI18N
            Container contentPane = _editConditionalFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            panel1.add(new JLabel(Bundle.getMessage("ColumnSystemName") + ":"));  // NOI18N
            panel1.add(new JLabel(_curConditional.getSystemName()));
            contentPane.add(panel1);
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            panel2.add(new JLabel(Bundle.getMessage("ColumnUserName") + ":"));  // NOI18N
            panel2.add(_conditionalUserName);
            _conditionalUserName.setToolTipText(Bundle.getMessage("ConditionalUserNameHint"));  // NOI18N
            contentPane.add(panel2);

            // add Logical Expression Section
            JPanel logicPanel = new JPanel();
            logicPanel.setLayout(new BoxLayout(logicPanel, BoxLayout.Y_AXIS));

            // add Antecedent Expression Panel - ONLY appears for MIXED operator statements
            _antecedent = _curConditional.getAntecedentExpression();
            _logicType = _curConditional.getLogicType();
            _antecedentField = new JTextField(65);
            _antecedentField.setFont(new Font("SansSerif", Font.BOLD, 14));  // NOI18N
            _antecedentPanel = makeEditPanel(_antecedentField, "LabelAntecedent", "LabelAntecedentHint");  // NOI18N

            JButton helpButton = new JButton(Bundle.getMessage("MenuHelp"));  // NOI18N
            _antecedentPanel.add(helpButton);
            helpButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    helpPressed(e);
                }
            });
            _antecedentPanel.add(helpButton);
            _antecedentPanel.setVisible(_logicType == Conditional.AntecedentOperator.MIXED);
            logicPanel.add(_antecedentPanel);

            // add state variable table title
            JPanel varTitle = new JPanel();
            varTitle.setLayout(new FlowLayout());
            varTitle.add(new JLabel(Bundle.getMessage("StateVariableTableTitle")));  // NOI18N
            logicPanel.add(varTitle);
            // set up state variables table
            // initialize and populate Combo boxes for table of state variables
            _notOperatorBox = new JComboBox<String>();
            _notOperatorBox.addItem(" ");
            _notOperatorBox.addItem(Bundle.getMessage("LogicNOT"));  // NOI18N

            _andOperatorBox = new JComboBox<String>();
            _andOperatorBox.addItem(Bundle.getMessage("LogicAND"));  // NOI18N
            _andOperatorBox.addItem(Bundle.getMessage("LogicOR"));   // NOI18N
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
            JButton testButton = new JButton("XXXXXX");  // NOI18N
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
            JButton addVariableButton = new JButton(Bundle.getMessage("AddVariableButton"));  // NOI18N
            panel42.add(addVariableButton);
            addVariableButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addVariablePressed(e);
                }
            });
            addVariableButton.setToolTipText(Bundle.getMessage("AddVariableButtonHint"));  // NOI18N

            JButton checkVariableButton = new JButton(Bundle.getMessage("CheckVariableButton"));  // NOI18N
            panel42.add(checkVariableButton);
            checkVariableButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    checkVariablePressed(e);
                }
            });
            checkVariableButton.setToolTipText(Bundle.getMessage("CheckVariableButtonHint"));  // NOI18N

            //  - Reorder variable button
            _reorderVarButton = new JButton(Bundle.getMessage("ReorderButton"));  // NOI18N
            panel42.add(_reorderVarButton);
            _reorderVarButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    reorderVariablePressed(e);
                }
            });
            _reorderVarButton.setToolTipText(Bundle.getMessage("ReorderButtonHint"));  // NOI18N
            _reorderVarButton.setEnabled(!(_logicType == Conditional.AntecedentOperator.MIXED));
            logicPanel.add(panel42);

            // logic type area
            _operatorBox = new JComboBox<>();
            for (Conditional.AntecedentOperator operator : Conditional.AntecedentOperator.values()) {
                _operatorBox.addItem(operator);
            }
            JPanel typePanel = makeEditPanel(_operatorBox, "LabelLogicType", "TypeLogicHint");  // NOI18N
            _operatorBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    logicTypeChanged(e);
                }
            });
            _operatorBox.setSelectedItem(_logicType);
            logicPanel.add(typePanel);
            logicPanel.add(Box.createHorizontalStrut(STRUT));

            Border logicPanelBorder = BorderFactory.createEtchedBorder();
            Border logicPanelTitled = BorderFactory.createTitledBorder(
                    logicPanelBorder, Bundle.getMessage("TitleLogicalExpression") + " ");  // NOI18N
            logicPanel.setBorder(logicPanelTitled);
            contentPane.add(logicPanel);
            // End of Logic Expression Section

            JPanel triggerPanel = new JPanel();
            triggerPanel.setLayout(new BoxLayout(triggerPanel, BoxLayout.Y_AXIS));
            ButtonGroup tGroup = new ButtonGroup();
            _triggerOnChangeButton = new JRadioButton(Bundle.getMessage("triggerOnChange"));  // NOI18N
            _triggerOnChangeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    _actionTableModel.fireTableDataChanged();
                }
            });
            tGroup.add(_triggerOnChangeButton);
            triggerPanel.add(_triggerOnChangeButton);
            JRadioButton triggerOnAny = new JRadioButton(Bundle.getMessage("triggerOnAny"));  // NOI18N
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
            actTitle.add(new JLabel(Bundle.getMessage("ActionTableTitle")));  // NOI18N
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
            JButton addActionButton = new JButton(Bundle.getMessage("addActionButton"));  // NOI18N
            panel43.add(addActionButton);
            addActionButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addActionPressed(e);
                }
            });

            addActionButton.setToolTipText(Bundle.getMessage("addActionButtonHint"));  // NOI18N
            conseqentPanel.add(panel43);

            //  - Reorder action button
            JButton reorderButton = new JButton(Bundle.getMessage("ReorderButton"));  // NOI18N
            panel43.add(reorderButton);
            reorderButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    reorderActionPressed(e);
                }
            });
            reorderButton.setToolTipText(Bundle.getMessage("ReorderButtonHint"));  // NOI18N
            conseqentPanel.add(panel43);

            Border conseqentPanelBorder = BorderFactory.createEtchedBorder();
            Border conseqentPanelTitled = BorderFactory.createTitledBorder(
                    conseqentPanelBorder, Bundle.getMessage("TitleAction"));  // NOI18N
            conseqentPanel.setBorder(conseqentPanelTitled);
            contentPane.add(conseqentPanel);
            // End of Action Consequents Section

            // Bottom Buttons - Update Conditional
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            JButton updateConditional = new JButton(Bundle.getMessage("ButtonUpdate"));  // NOI18N
            panel5.add(updateConditional);
            updateConditional.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateConditionalPressed(e);
                }
            });
            updateConditional.setToolTipText(Bundle.getMessage("UpdateConditionalButtonHint"));  // NOI18N
            // Cancel
            JButton cancelConditional = new JButton(Bundle.getMessage("ButtonCancel"));  // NOI18N
            panel5.add(cancelConditional);
            cancelConditional.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelConditionalPressed(e);
                }
            });
            cancelConditional.setToolTipText(Bundle.getMessage("CancelConditionalButtonHint"));  // NOI18N
            // Delete Conditional
            JButton deleteConditional = new JButton(Bundle.getMessage("ButtonDelete"));  // NOI18N
            panel5.add(deleteConditional);
            deleteConditional.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteConditionalPressed(null);
                }
            });
            deleteConditional.setToolTipText(Bundle.getMessage("DeleteConditionalButtonHint"));  // NOI18N

            contentPane.add(panel5);
        }
        // setup window closing listener
        _editConditionalFrame.addWindowListener(
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
        _editConditionalFrame.pack();
        _editConditionalFrame.setVisible(true);
        _inEditConditionalMode = true;
        checkVariablePressed(null); // update variables to their current states
        _antecedentField.setText(translateAntecedent(_antecedent, false));

    }

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
            JOptionPane.showMessageDialog(_editConditionalFrame,
                    Bundle.getMessage("Error49"),
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        _showReminder = true;
        _curVariableItem = Conditional.ItemType.NONE;
        ConditionalVariable variable = new ConditionalVariable();
        _variableList.add(variable);
        _newItem = true;
        int size = _variableList.size();
        // default of operator for postion 0 (row 1) is Conditional.OPERATOR_NONE
        if (size > 1) {
            if (_logicType == Conditional.AntecedentOperator.ALL_OR) {
                variable.setOpern(Conditional.Operator.OR);
            } else {
                variable.setOpern(Conditional.Operator.AND);
            }
        }
        size--;
        _variableTableModel.fireTableRowsInserted(size, size);
        makeEditVariableWindow(size);
        appendToAntecedent();
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
     * Respond to the Reorder Variable Button in the Edit Conditional window.
     *
     * @param e The event heard
     */
    void reorderVariablePressed(ActionEvent e) {
        if (alreadyEditingActionOrVariable()) {
            return;
        }
        _showReminder = true;
        // Check if reorder is reasonable
        if (_variableList.size() <= 1) {
            JOptionPane.showMessageDialog(_editLogixFrame,
                    Bundle.getMessage("Error51"),
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        _nextInOrder = 0;
        _inVarReorder = true;
        _variableTableModel.fireTableDataChanged();
    }

    /**
     * Respond to the First/Next (Delete) Button in the Edit Conditional window.
     *
     * @param row index of the row to put as next in line (instead of the one
     *            that was supposed to be next)
     */
    void swapVariables(int row) {
        ConditionalVariable temp = _variableList.get(row);
        for (int i = row; i > _nextInOrder; i--) {
            _variableList.set(i, _variableList.get(i - 1));
        }

        // Adjust operator type
        Operator oper;
        if (_nextInOrder == 0) {
            oper = Conditional.Operator.NONE;
        } else {
            oper = (_logicType == Conditional.AntecedentOperator.ALL_AND)
                    ? Conditional.Operator.AND
                    : Conditional.Operator.OR;
        }

        temp.setOpern(oper);
        _variableList.set(_nextInOrder, temp);
        _nextInOrder++;
        if (_nextInOrder >= _variableList.size()) {
            _inVarReorder = false;
        }
        _variableTableModel.fireTableDataChanged();
    }

    /**
     * Respond to the Negation column in the Edit Conditional window.
     *
     * @param row  index of the Conditional to change the setting on
     * @param oper NOT (i18n) as negation of condition
     */
    void variableNegationChanged(int row, String oper) {
        ConditionalVariable variable = _variableList.get(row);
        boolean state = variable.isNegated();
        if (oper == null) {
            variable.setNegation(false);
        } else {
            variable.setNegation(oper.equals(Bundle.getMessage("LogicNOT")));  // NOI18N
        }
        if (variable.isNegated() != state) {
            makeAntecedent();
        }
    }

    /**
     * Respond to the Operator column in the Edit Conditional window.
     *
     * @param row  index of the Conditional to change the setting on
     * @param oper AND or OR (i18n) as operand on the list of conditions
     */
    void variableOperatorChanged(int row, String oper) {
        ConditionalVariable variable = _variableList.get(row);
        Operator oldOper = variable.getOpern();
        if (row > 0) {
            if (oper.equals(Bundle.getMessage("LogicOR"))) {  // NOI18N
                variable.setOpern(Conditional.Operator.OR);
            } else {
                variable.setOpern(Conditional.Operator.AND);
            }
        } else {
            variable.setOpern(Conditional.Operator.NONE);
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
        _curActionItem = Conditional.ItemType.NONE;
        _actionList.add(new DefaultConditionalAction());
        _newItem = true;
        _actionTableModel.fireTableRowsInserted(_actionList.size(),
                _actionList.size());
        makeEditActionWindow(_actionList.size() - 1);
    }

    /**
     * Respond to the Reorder Action Button in the Edit Conditional window.
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
            JOptionPane.showMessageDialog(_editLogixFrame,
                    Bundle.getMessage("Error46"),
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        _nextInOrder = 0;
        _inActReorder = true;
        _actionTableModel.fireTableDataChanged();
    }

    /**
     * Respond to the First/Next (Delete) Button in the Edit Conditional window.
     *
     * @param row index of the row to put as next in line (instead of the one
     *            that was supposed to be next)
     */
    void swapActions(int row) {
        ConditionalAction temp = _actionList.get(row);
        for (int i = row; i > _nextInOrder; i--) {
            _actionList.set(i, _actionList.get(i - 1));
        }
        _actionList.set(_nextInOrder, temp);
        _nextInOrder++;
        if (_nextInOrder >= _actionList.size()) {
            _inActReorder = false;
        }
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
                if (_variableList.get(i).getType() == Conditional.Type.NONE) {
                    _variableList.remove(i);
                    _variableTableModel.fireTableRowsDeleted(i, i);
                }
            }
        }
        for (int i = 0; i < _actionList.size(); i++) {
            if (_actionList.get(i).getType() == Conditional.Action.NONE) {
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
            JOptionPane.showMessageDialog(_editConditionalFrame,
                    Bundle.getMessage("Warn8", SensorGroupFrame.logixUserName, SensorGroupFrame.logixSysName)
                    + Bundle.getMessage("Warn11", _curConditional.getUserName(), _curConditional.getSystemName()),
                    Bundle.getMessage("WarningTitle"), // NOI18N
                    JOptionPane.WARNING_MESSAGE);
            cancelConditionalPressed(null);
            return;
        }
        // Check if the User Name has been changed
        String uName = _conditionalUserName.getText();
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
            JOptionPane.showMessageDialog(_editLogixFrame,
                    Bundle.getMessage("Warn5", _curConditional.getUserName(), _curConditional.getSystemName()),
                    Bundle.getMessage("WarningTitle"), // NOI18N
                    JOptionPane.WARNING_MESSAGE);
        }

        if (!validateAntecedent()) {
            return;
        }
        // complete update
        _antecedent = translateAntecedent(_antecedentField.getText(), true);
        _curConditional.setStateVariables(_variableList);
        _curConditional.setAction(_actionList);
        _curConditional.setLogicType(_logicType, _antecedent);
        _curConditional.setTriggerOnChange(_triggerOnChangeButton.isSelected());
        TreeSet<String> newTargetNames = new TreeSet<String>();
        loadReferenceNames(_variableList, newTargetNames);
        updateWhereUsed(_oldTargetNames, newTargetNames, _curConditional.getSystemName());
        cancelConditionalPressed(null);
    }

    /**
     * Respond to the Cancel button in the Edit Conditional frame.
     * <p>
     * Does the cleanup from deleteConditionalPressed, updateConditionalPressed
     * and _editConditionalFrame window closer.
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
            log.debug("NumberFormatException on activation of Logix ", nfe);  // NOI18N
            JOptionPane.showMessageDialog(_editLogixFrame,
                    Bundle.getMessage("Error4") + nfe.toString() + Bundle.getMessage("Error7"), // NOI18N
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);  // NOI18N
        }
        // when user uses the escape key and returns to editing, interaction with
        // window closing event create strange environment
        _inEditConditionalMode = false;
        if (_editConditionalFrame != null) {
            _editConditionalFrame.setVisible(false);
            _editConditionalFrame.dispose();
            _editConditionalFrame = null;
        }
        if (_editLogixFrame != null) {
            _editLogixFrame.setVisible(true);
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
        _numConditionals--;
        String[] msgs = _curLogix.deleteConditional(sName);
        if (msgs != null) {
            JOptionPane.showMessageDialog(_editLogixFrame,
                    Bundle.getMessage("Error11", (Object[]) msgs), // NOI18N
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);  // NOI18N
        }

        // complete deletion
        cancelConditionalPressed(null);
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
     * Respond to a change of Conditional Type in the Edit Conditional pane by
     * showing/hiding the _antecedentPanel when Mixed is selected.
     *
     * @param e The event heard
     * @return false if there is no change in operator
     */
    boolean logicTypeChanged(ActionEvent e) {
        Conditional.AntecedentOperator type =
                _operatorBox.getItemAt(_operatorBox.getSelectedIndex());
        if (type == _logicType) {
            return false;
        }
        makeAntecedent();

        if (type == Conditional.AntecedentOperator.MIXED) {
            _antecedentPanel.setVisible(true);
            _reorderVarButton.setEnabled(false);
        } else {
            Operator oper = (type == Conditional.AntecedentOperator.ALL_AND)
                    ? Conditional.Operator.AND
                    : Conditional.Operator.OR;
            for (int i = 1; i < _variableList.size(); i++) {
                _variableList.get(i).setOpern(oper);
            }
            _antecedentPanel.setVisible(false);
            _reorderVarButton.setEnabled(true);
        }

        _logicType = type;
        _variableTableModel.fireTableDataChanged();
        _editConditionalFrame.repaint();
        return true;
    }

    /**
     * Respond to Help button press in the Edit Conditional pane.
     *
     * @param e The event heard
     */
    void helpPressed(ActionEvent e) {
        JOptionPane.showMessageDialog(_editConditionalFrame,
                new String[]{
                    Bundle.getMessage("ConditionalHelpText1"), // NOI18N
                    Bundle.getMessage("ConditionalHelpText2"), // NOI18N
                    Bundle.getMessage("ConditionalHelpText3"), // NOI18N
                    Bundle.getMessage("ConditionalHelpText4"), // NOI18N
                    Bundle.getMessage("ConditionalHelpText5"), // NOI18N
                    Bundle.getMessage("ConditionalHelpText6"), // NOI18N
                    Bundle.getMessage("ConditionalHelpText7")  // NOI18N
                },
                Bundle.getMessage("MenuHelp"),
                JOptionPane.INFORMATION_MESSAGE);  // NOI18N
    }

    /**
     * Build the antecedent statement.
     */
    void makeAntecedent() {
        _antecedent = makeAntecedent(_variableList);
        _antecedentField.setText(translateAntecedent(_antecedent, false));
        _showReminder = true;
    }

    /**
     * Add a R# to the antecedent statement.
     */
    void appendToAntecedent() {
        _antecedent = appendToAntecedent(_logicType, _variableList.size(), _antecedent);
        _antecedentField.setText(translateAntecedent(_antecedent, false));
        // store in conditional
        _curConditional.setLogicType(_logicType, _antecedent);
    }

    /**
     * Check the antecedent and logic type.
     *
     * @return false if antecedent can't be validated
     */
    boolean validateAntecedent() {
        return validateAntecedent(_logicType, _antecedentField.getText(), _variableList, _curConditional);
    }

    // ============ Shared Variable and Action Methods ============

    /**
     * Check if an editing session is going on.
     * <p>
     * If it is, display a message to user and bring current editing pane to
     * front.
     *
     * @return true if an _editActionFrame or _editVariableFrame exists
     */
    boolean alreadyEditingActionOrVariable() {
        if (_editActionFrame != null) {
            // Already editing an Action, ask for completion of that edit
            JOptionPane.showMessageDialog(_editActionFrame,
                    Bundle.getMessage("Error48"),
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            _editActionFrame.setVisible(true);
            return true;
        }
        if (_editVariableFrame != null) {
            // Already editing a state variable, ask for completion of that edit
            JOptionPane.showMessageDialog(_editVariableFrame,
                    Bundle.getMessage("Error47"),
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            _editVariableFrame.setVisible(true);
            return true;
        }
        if (_selectionMode == SelectionMode.USEMULTI) {
            openPickListTable();
        }
        return false;
    }

    /**
     * Fetch valid localized appearances for a given Signal Head.
     * <p>
     * Warn if head is not found.
     *
     * @param box            the comboBox on the setup pane to fill
     * @param signalHeadName user or system name of the Signal Head
     */
    void loadJComboBoxWithHeadAppearances(JComboBox<String> box, String signalHeadName) {
        box.removeAllItems();
        log.debug("loadJComboBoxWithSignalHeadAppearances called with name: {}", signalHeadName);  // NOI18N
        SignalHead h = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHeadName);
        if (h == null) {
            box.addItem(Bundle.getMessage("PromptLoadHeadName"));  // NOI18N
        } else {
            String[] v = h.getValidStateNames();
            for (int i = 0; i < v.length; i++) {
                box.addItem(v[i]);
            }
            box.setSelectedItem(h.getAppearanceName());
        }
    }

    /**
     * Fetch valid aspects for a given Signal Mast.
     * <p>
     * Warn if mast is not found.
     *
     * @param box      the comboBox on the setup pane to fill
     * @param mastName user or system name of the Signal Mast
     */
    void loadJComboBoxWithMastAspects(JComboBox<String> box, String mastName) {
        box.removeAllItems();
        log.debug("loadJComboBoxWithMastAspects called with name: {}", mastName);  // NOI18N
        SignalMast m = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(mastName);
        if (m == null) {
            box.addItem(Bundle.getMessage("PromptLoadMastName"));  // NOI18N
        } else {
            java.util.Vector<String> v = m.getValidAspects();
            for (int i = 0; i < v.size(); i++) {
                box.addItem(v.get(i));
            }
            box.setSelectedItem(m.getAspect());
        }
    }

    // ------------ Build sub-panels ------------

    /**
     * Create Variable and Action editing pane top part.
     *
     * @param frame  JFrame to add to
     * @param title  property key for border title
     * @param width  fixed dimension to use
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
        Border panelTitled = BorderFactory.createTitledBorder(panelBorder, Bundle.getMessage(title));
        topPanel.setBorder(panelTitled);
        topPanel.add(Box.createRigidArea(new Dimension(width, 0)));
        topPanel.add(Box.createVerticalGlue());
        return topPanel;
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
     * Create Variable and Action editing pane bottom part.
     * <p>
     * Called from {@link #makeEditVariableWindow(int)}
     *
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

        JButton cancelAction = new JButton(Bundle.getMessage("ButtonCancel"));  // NOI18N
        panel3.add(cancelAction);
        panel3.add(Box.createHorizontalStrut(STRUT));
        cancelAction.addActionListener(cancelListener);
        cancelAction.setToolTipText(Bundle.getMessage("CancelButtonHint"));  // NOI18N

        JButton updateAction = new JButton(Bundle.getMessage("ButtonUpdate"));  // NOI18N
        panel3.add(updateAction);
        panel3.add(Box.createHorizontalStrut(STRUT));
        updateAction.addActionListener(updateListener);
        updateAction.setToolTipText(Bundle.getMessage("UpdateButtonHint"));  // NOI18N

        JButton deleteAction = new JButton(Bundle.getMessage("ButtonDelete"));  // NOI18N
        panel3.add(deleteAction);
        deleteAction.addActionListener(deleteListener);
        deleteAction.setToolTipText(Bundle.getMessage("DeleteButtonHint"));  // NOI18N
        return panel3;
    }

    // ============ Edit Variable Window and Methods ============

    /**
     * Create and/or initialize the Edit a Variable pane.
     * <p>
     * Note: you can get here via the New Variable button (addVariablePressed)
     * or via an Edit button in the Variable table of the EditConditional
     * window.
     *
     * @param row index of item to be edited in _variableList
     */
    void makeEditVariableWindow(int row) {
        if (alreadyEditingActionOrVariable()) {
            return;
        }
        log.debug("makeEditVariableWindow: row = {}", row);
        _curVariableRowNumber = row;
        _curVariable = _variableList.get(row);
        _editVariableFrame = new JmriJFrame(Bundle.getMessage("TitleEditVariable"), true, true);  // NOI18N
        JPanel topPanel = makeTopPanel(_editVariableFrame, "TitleAntecedentPhrase", 500, 160);  // NOI18N

        Box panel1 = Box.createHorizontalBox();
        panel1.add(Box.createHorizontalGlue());
        panel1.add(Box.createHorizontalStrut(STRUT));

        // Item Type
        _variableItemBox = new JComboBox<>();
        for (Conditional.ItemType itemType : Conditional.ItemType.getStateVarList()) {
            _variableItemBox.addItem(itemType);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_variableItemBox);
        panel1.add(makeEditPanel(_variableItemBox, "LabelVariableType", "VariableTypeHint"));  // NOI18N
        panel1.add(Box.createHorizontalStrut(STRUT));

        // Item Name
        _variableNameField = new JTextField(30);
        _variableNamePanel = makeEditPanel(_variableNameField, "LabelItemName", null);  // NOI18N
        _variableNamePanel.setMaximumSize(
                new Dimension(50, _variableNamePanel.getPreferredSize().height));
        _variableNamePanel.setVisible(false);
        panel1.add(_variableNamePanel);
        panel1.add(Box.createHorizontalStrut(STRUT));

        // Arbitrary name combo box to facilitate the panel construction
        if (_selectionMode == SelectionMode.USECOMBO) {
            _comboNameBox = createNameBox(Conditional.ItemType.SENSOR);
            _variableComboNamePanel = makeEditPanel(_comboNameBox, "LabelItemName", null);  // NOI18N
            _variableComboNamePanel.setVisible(false);
            panel1.add(_variableComboNamePanel);
            panel1.add(Box.createHorizontalStrut(STRUT));
        }

        // Combo box section for selecting conditional reference
        //   First box selects the Logix, the second selects the conditional within the logix
        _selectLogixBox.addItem("XXXXXXXXXXXXXXXXXXXXX");  // NOI18N
        _selectConditionalBox.addItem("XXXXXXXXXXXXXXXXXXXXX");  // NOI18N
        _selectLogixPanel = makeEditPanel(_selectLogixBox, "SelectLogix", null);  // NOI18N
        _selectConditionalPanel = makeEditPanel(_selectConditionalBox, "SelectConditional", null);  // NOI18N
        _selectLogixPanel.setVisible(false);
        _selectConditionalPanel.setVisible(false);
        panel1.add(_selectLogixPanel);
        panel1.add(_selectConditionalPanel);
        panel1.add(Box.createHorizontalStrut(STRUT));

        // State Box
        _variableStateBox = new JComboBox<>();
        _variableStateBox.addItem(Conditional.Type.XXXXXXX);
        _variableStatePanel = makeEditPanel(_variableStateBox, "LabelVariableState", "VariableStateHint");  // NOI18N
        _variableStatePanel.setVisible(false);
        panel1.add(_variableStatePanel);
        panel1.add(Box.createHorizontalStrut(STRUT));

        // Aspects
        _variableSignalBox = new JComboBox<String>();
        _variableSignalBox.addItem("XXXXXXXXX");  // NOI18N
        _variableSignalPanel = makeEditPanel(_variableSignalBox, "LabelVariableAspect", "VariableAspectHint");  // NOI18N
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
        _variableComparePanel.add(makeEditPanel(_variableCompareOpBox, "LabelCompareOp", "CompareHintMemory"));  // NOI18N
        _variableComparePanel.add(Box.createHorizontalStrut(STRUT));

        // Compare type
        _variableCompareTypeBox = new JComboBox<>();
        for (Conditional.Type t : Conditional.Type.getMemoryItems()) {
            _variableCompareTypeBox.addItem(t);
        }
        _variableComparePanel.add(makeEditPanel(_variableCompareTypeBox, "LabelCompareType", "CompareTypeHint"));  // NOI18N
        _variableComparePanel.setVisible(false);
        _variableCompareTypeBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                compareTypeChanged(_variableCompareTypeBox.getSelectedIndex());
                _editVariableFrame.pack();
            }
        });
        panel1.add(_variableComparePanel);
        panel1.add(Box.createHorizontalStrut(STRUT));

        // Data 1
        _variableData1Field = new JTextField(30);
        _variableData1Panel = makeEditPanel(_variableData1Field, "LabelStartTime", "DataHintTime");  // NOI18N
        _variableData1Panel.setMaximumSize(
                new Dimension(45, _variableData1Panel.getPreferredSize().height));
        _variableData1Panel.setVisible(false);
        panel1.add(_variableData1Panel);
        panel1.add(Box.createHorizontalStrut(STRUT));

        // Data 2
        _variableData2Field = new JTextField(30);
        _variableData2Panel = makeEditPanel(_variableData2Field, "LabelEndTime", "DataHintTime");  // NOI18N
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
        _variableItemBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Conditional.ItemType newVariableItem = _variableItemBox.getItemAt(_variableItemBox.getSelectedIndex());
                if (log.isDebugEnabled()) {
                    log.debug("_variableItemBox Listener: new = {}, curr = {}, row = {}",  // NOI18N
                            newVariableItem, _curVariableItem, _curVariableRowNumber);
                }
                if (newVariableItem != _curVariableItem) {
                    if (_curVariableRowNumber >= 0) {
                        _curVariable = new ConditionalVariable();
                        if (_curVariableRowNumber > 0) {
                            if (_logicType == Conditional.AntecedentOperator.ALL_OR) {
                                _curVariable.setOpern(Conditional.Operator.OR);
                            } else {
                                _curVariable.setOpern(Conditional.Operator.AND);
                            }
                        }
                        _variableList.set(_curVariableRowNumber, _curVariable);
                    }
                    _curVariableItem = newVariableItem;
                }
                variableItemChanged(newVariableItem);
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
        _curVariableItem = _curVariable.getType().getItemType();
        initializeStateVariables();
        _editVariableFrame.pack();
        _editVariableFrame.setVisible(true);
    }

    // ------------ Main Variable methods ------------

    /**
     * Set display to show current state variable (_curVariable) parameters.
     */
    void initializeStateVariables() {
        Conditional.Type testType = _curVariable.getType();
        Conditional.ItemType itemType = testType.getItemType();
        if (log.isDebugEnabled()) {
            log.debug("initializeStateVariables: itemType = {}, testType = {}", itemType, testType);  // NOI18N
        }
        if (testType == Conditional.Type.NONE) {
            return;
        }
        // set item - _variableItemBox Listener will call variableItemChanged
        _variableItemBox.setSelectedItem(itemType);
        switch (itemType) {
            case SENSOR:
            case TURNOUT:
            case LIGHT:
            case CONDITIONAL:
            case WARRANT:
                _variableStateBox.setSelectedItem(testType);
                _variableNameField.setText(_curVariable.getName());
                break;

            case SIGNALHEAD:
                _variableStateBox.setSelectedItem(testType);
                _variableNameField.setText(_curVariable.getName());
                if (Conditional.Type.isSignalHeadApperance(testType)) {
                    _variableStateBox.setSelectedItem(Conditional.Type.SIGNAL_HEAD_APPEARANCE_EQUALS);
                    loadJComboBoxWithHeadAppearances(_variableSignalBox, _curVariable.getName());
                    _variableSignalBox.setSelectedItem(_curVariable.getType());
                    _variableSignalPanel.setVisible(true);
                }
                break;

            case SIGNALMAST:
                // set display to show current state variable (curVariable) parameters
                _variableStateBox.setSelectedItem(testType);
                _variableNameField.setText(_curVariable.getName());
                if (testType == Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS) {
                    loadJComboBoxWithMastAspects(_variableSignalBox, _curVariable.getName());
                    _variableSignalBox.setSelectedItem(_curVariable.getDataString());
                    _variableSignalPanel.setVisible(true);
                }
                break;

            case MEMORY:
                _variableCompareTypeBox.setSelectedIndex(
                        Conditional.Type.getIndexInList(Conditional.Type.getMemoryItems(), testType));
                _variableNameField.setText(_curVariable.getName());
                int num1 = _curVariable.getNum1() - 1;
                if (num1 == -1) {  // former code was only equals
                    num1 = ConditionalVariable.EQUAL - 1;
                }
                _variableCompareOpBox.setSelectedIndex(num1);
                _variableData1Field.setText(_curVariable.getDataString());
                break;

            case CLOCK:
                int time = _curVariable.getNum1();
                _variableData1Field.setText(formatTime(time / 60, time - ((time / 60) * 60)));
                time = _curVariable.getNum2();
                _variableData2Field.setText(formatTime(time / 60, time - ((time / 60) * 60)));
                _variableNameField.setText("");
                break;

            case OBLOCK:
                _variableNameField.setText(_curVariable.getName());
                //_variableStateBox.removeAllItems();
                for (Conditional.Type type : Conditional.Type.getOBlockItems()) {
                    _variableStateBox.addItem(type);
                    if (type.toString().equals(OBlock.getLocalStatusName(_curVariable.getDataString()))) {
                        _variableStateBox.setSelectedItem(type);
                    }
                }
//                Iterator<String> names = OBlock.getLocalStatusNames();
//                while (names.hasNext()) {
//                    _variableStateBox.addItem(names.next());
//                }
//                _variableStateBox.setSelectedItem(OBlock.getLocalStatusName(_curVariable.getDataString()));
                _variableStateBox.setVisible(true);
                break;

            case ENTRYEXIT:
                _variableNameField.setText(_curVariable.getBean().getUserName());
                _variableStateBox.setSelectedItem(testType);
                _variableStateBox.setVisible(true);
                break;

            default:
                break;
        }
        _editVariableFrame.pack();
        _editVariableFrame.transferFocusBackward();
    }

    /**
     * Respond to change in variable item chosen in the State Variable Table in
     * the Edit Conditional pane.
     * <p>
     * Also used to set up for Edit of a Conditional with state variables.
     *
     * @param itemType value representing the newly selected Conditional type,
     *                 i.e. ITEM_TYPE_SENSOR
     */
    private void variableItemChanged(Conditional.ItemType itemType) {
        Conditional.Type testType = _curVariable.getType();
        if (log.isDebugEnabled()) {
            log.debug("variableItemChanged: itemType = {}, testType = {}", itemType, testType);  // NOI18N
        }
        _variableNamePanel.setVisible(false);
        _variableStatePanel.setVisible(false);
        _variableComparePanel.setVisible(false);
        _variableSignalPanel.setVisible(false);
        _variableData1Panel.setVisible(false);
        _variableData2Panel.setVisible(false);
        _selectLogixPanel.setVisible(false);
        _selectConditionalPanel.setVisible(false);
        _variableStateBox.removeAllItems();
        _variableNameField.removeActionListener(variableSignalHeadNameListener);
        _variableNameField.removeActionListener(variableSignalMastNameListener);
        _variableStateBox.removeActionListener(variableSignalTestStateListener);
        _selectLogixBox.removeActionListener(selectLogixBoxListener);
        _selectConditionalBox.removeActionListener(selectConditionalBoxListener);

        if (_selectionMode == SelectionMode.USECOMBO) {
            _variableComboNamePanel.setVisible(false);
        } else if (_selectionMode == SelectionMode.USESINGLE) {
            createSinglePanelPickList(itemType, new PickSingleListener(_variableNameField, itemType), false);
        } else {
            // Default and USEMULTI
            setPickListTab(itemType, false);
        }

        switch (itemType) {
            case NONE:
                return;
            case SENSOR:
                _variableNamePanel.setToolTipText(Bundle.getMessage("NameHintSensor"));  // NOI18N
                for (Conditional.Type type : Conditional.Type.getSensorItems()) {
                    _variableStateBox.addItem(type);
                }
                _variableStatePanel.setVisible(true);
                _variableNamePanel.setVisible(true);
                setVariableNameBox(itemType);
                break;

            case TURNOUT:
                _variableNamePanel.setToolTipText(Bundle.getMessage("NameHintTurnout"));  // NOI18N
                for (Conditional.Type type : Conditional.Type.getTurnoutItems()) {
                    _variableStateBox.addItem(type);
                }
                _variableNamePanel.setVisible(true);
                _variableStatePanel.setVisible(true);
                setVariableNameBox(itemType);
                break;

            case LIGHT:
                _variableNamePanel.setToolTipText(Bundle.getMessage("NameHintLight"));  // NOI18N
                for (Conditional.Type type : Conditional.Type.getLightItems()) {
                    _variableStateBox.addItem(type);
                }
                _variableStatePanel.setVisible(true);
                _variableNamePanel.setVisible(true);
                setVariableNameBox(itemType);
                break;

            case SIGNALHEAD:
                _variableNameField.addActionListener(variableSignalHeadNameListener);
                _variableStateBox.addActionListener(variableSignalTestStateListener);
                loadJComboBoxWithHeadAppearances(_variableSignalBox, _variableNameField.getText().trim());

                for (Conditional.Type type : Conditional.Type.getSignalHeadStateMachineItems()) {
                    _variableStateBox.addItem(type);
                }
                _variableNamePanel.setToolTipText(Bundle.getMessage("NameHintSignal"));  // NOI18N
                _variableNamePanel.setVisible(true);
                _variableStatePanel.setVisible(true);
                if (testType == Conditional.Type.SIGNAL_HEAD_APPEARANCE_EQUALS) {
                    _variableSignalPanel.setVisible(true);
                } else {
                    _variableSignalPanel.setVisible(false);
                }
                setVariableNameBox(itemType);
                break;

            case SIGNALMAST:
                _variableNameField.addActionListener(variableSignalMastNameListener);
                _variableStateBox.addActionListener(variableSignalTestStateListener);
                loadJComboBoxWithMastAspects(_variableSignalBox, _variableNameField.getText().trim());

                for (Conditional.Type type : Conditional.Type.getSignalMastItems()) {
                    _variableStateBox.addItem(type);
                }
                _variableNamePanel.setToolTipText(Bundle.getMessage("NameHintSignalMast"));  // NOI18N
                _variableNamePanel.setVisible(true);
                _variableStatePanel.setVisible(true);
                if (testType == Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS) {
                    _variableSignalPanel.setVisible(true);
                } else {
                    _variableSignalPanel.setVisible(false);
                }
                setVariableNameBox(itemType);
                break;

            case MEMORY:
                JPanel p = (JPanel) _variableData1Panel.getComponent(0);
                JLabel l = (JLabel) p.getComponent(0);
                if ((testType == Conditional.Type.MEMORY_COMPARE)
                        || (testType == Conditional.Type.MEMORY_COMPARE_INSENSITIVE)) {
                    l.setText(Bundle.getMessage("LabelMemoryValue"));  // NOI18N
                    _variableData1Panel.setToolTipText(Bundle.getMessage("DataHintMemory"));  // NOI18N
                } else {
                    l.setText(Bundle.getMessage("LabelLiteralValue"));  // NOI18N
                    _variableData1Panel.setToolTipText(Bundle.getMessage("DataHintValue"));  // NOI18N
                }
                _variableNamePanel.setToolTipText(Bundle.getMessage("NameHintMemory"));  // NOI18N
                _variableNamePanel.setVisible(true);
                _variableData1Panel.setToolTipText(Bundle.getMessage("DataHintMemory"));  // NOI18N
                _variableData1Panel.setVisible(true);
                _variableComparePanel.setVisible(true);
                setVariableNameBox(itemType);
                break;

            case CONDITIONAL:
                _variableNamePanel.setToolTipText(Bundle.getMessage("NameHintConditional"));  // NOI18N
                _selectLogixBox.addActionListener(selectLogixBoxListener);
                _selectConditionalBox.addActionListener(selectConditionalBoxListener);
                for (Conditional.Type type : Conditional.Type.getConditionalItems()) {
                    _variableStateBox.addItem(type);
                }
                // Load the Logix and Conditional combo boxes
                loadSelectLogixBox();
                _selectLogixPanel.setPreferredSize(_selectLogixBox.getPreferredSize());
                _selectConditionalPanel.setPreferredSize(_selectConditionalBox.getPreferredSize());
                _selectLogixPanel.setVisible(true);
                _selectConditionalPanel.setVisible(true);
                _variableStatePanel.setVisible(true);
                break;

            case WARRANT:
                _variableNamePanel.setToolTipText(Bundle.getMessage("NameHintWarrant"));  // NOI18N
                for (Conditional.Type type : Conditional.Type.getWarrantItems()) {
                    _variableStateBox.addItem(type);
                }
                _variableNamePanel.setVisible(true);
                _variableStatePanel.setVisible(true);
                setVariableNameBox(itemType);
                break;

            case CLOCK:
                p = (JPanel) _variableData1Panel.getComponent(0);
                l = (JLabel) p.getComponent(0);
                l.setText(Bundle.getMessage("LabelStartTime"));  // NOI18N
                _variableData1Panel.setToolTipText(Bundle.getMessage("DataHintTime"));  // NOI18N
                _variableData1Panel.setVisible(true);
                _variableData2Panel.setVisible(true);
                break;

            case OBLOCK:
                _variableNamePanel.setToolTipText(Bundle.getMessage("NameHintOBlock"));  // NOI18N
                _variableNamePanel.setVisible(true);
                _variableStateBox.removeAllItems();
                for (Conditional.Type type : Conditional.Type.getOBlockItems()) {
                    _variableStateBox.addItem(type);
                }
//                Iterator<String> names = OBlock.getLocalStatusNames();
//                while (names.hasNext()) {
//                    _variableStateBox.addItem(names.next());
//                }
                _variableStatePanel.setVisible(true);
                setVariableNameBox(itemType);
                break;

            case ENTRYEXIT:
                _variableNamePanel.setToolTipText(Bundle.getMessage("NameHintEntryExit"));  // NOI18N
                _variableNameField.setText(_curVariable.getName());
                for (Conditional.Type type : Conditional.Type.getEntryExitItems()) {
                    _variableStateBox.addItem(type);
                }
                _variableStatePanel.setVisible(true);
                _variableNamePanel.setVisible(true);
                setVariableNameBox(itemType);
                break;

            default:
                break;
        }
        _variableStateBox.setMaximumSize(_variableStateBox.getPreferredSize());
    }

    /**
     * Update the name combo box selection based on the current contents of the
     * name field.
     *
     * @since 4.7.3
     * @param itemType The type of name box to be created.
     */
    void setVariableNameBox(Conditional.ItemType itemType) {
        if (_selectionMode != SelectionMode.USECOMBO) {
            return;
        }
        _comboNameBox = createNameBox(itemType);
        if (_comboNameBox == null) {
            return;
        }
        _comboNameBox.addActionListener(new NameBoxListener(_variableNameField));
        _comboNameBox.setSelectedItemByName(_curVariable.getName());
        _variableComboNamePanel.remove(1);
        _variableComboNamePanel.add(_comboNameBox, null, 1);
        _variableNamePanel.setVisible(false);
        _variableComboNamePanel.setVisible(true);
    }

    // ------------ Variable detail methods ------------

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
        if (_editConditionalFrame != null) {
            _editConditionalFrame.setVisible(true);
        }
    }

    /**
     * Respond to Cancel action button and window closer of the Edit Variable
     * pane.
     * <p>
     * Also does cleanup of Update and Delete Variable buttons.
     */
    void cancelEditVariablePressed() {
        if (_newItem) {
            deleteVariablePressed(_curVariableRowNumber);
        } else {
            cleanUpVariable();
        }
        if (_editConditionalFrame != null) {
            _editConditionalFrame.setVisible(true);
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
            JOptionPane.showMessageDialog(_editConditionalFrame,
                    Bundle.getMessage("Warn3"),
                    Bundle.getMessage("WarningTitle"), // NOI18N
                    JOptionPane.WARNING_MESSAGE);
        }
        // move remaining state variables if needed
        _variableList.remove(row);
        _variableTableModel.fireTableRowsDeleted(row, row);
        makeAntecedent();
        cleanUpVariable();
        if (_editConditionalFrame != null) {
            _editConditionalFrame.setVisible(true);
        }
        _showReminder = true;
    }

    /**
     * Check if Memory type in a Conditional was changed by the user.
     * <p>
     * Update GUI if it has. Called from {@link #makeEditVariableWindow(int)}
     *
     * @param selection index of the currently selected type in the
     *                  _variableCompareTypeBox
     */
    private void compareTypeChanged(int selection) {
        JPanel p = (JPanel) _variableData1Panel.getComponent(0);
        JLabel l = (JLabel) p.getComponent(0);
        Conditional.Type testType = Conditional.Type.getMemoryItems().get(selection);
        if ((testType == Conditional.Type.MEMORY_COMPARE)
                || (testType == Conditional.Type.MEMORY_COMPARE_INSENSITIVE)) {
            l.setText(Bundle.getMessage("LabelMemoryValue"));  // NOI18N
            _variableData1Panel.setToolTipText(Bundle.getMessage("DataHintMemory"));  // NOI18N
        } else {
            l.setText(Bundle.getMessage("LabelLiteralValue"));  // NOI18N
            _variableData1Panel.setToolTipText(Bundle.getMessage("DataHintValue"));  // NOI18N
        }
    }

    /**
     * Load the Logix selection box. Set the selection to the current Logix.
     *
     * @since 4.7.4
     */
    void loadSelectLogixBox() {
        // Get the current Logix name for selecting the current combo box row
        String cdlName = _curVariable.getName();
        String lgxName;
        if (cdlName.length() == 0 || (_curVariable.getType() != Conditional.Type.CONDITIONAL_TRUE
                && _curVariable.getType() != Conditional.Type.CONDITIONAL_FALSE)) {
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
        loadSelectConditionalBox(lgxName);
    }

    /**
     * Load the Conditional selection box. The first row is a prompt.
     *
     * @since 4.7.4
     * @param logixName The Logix system name for selecting the owned
     *                  Conditionals
     */
    void loadSelectConditionalBox(String logixName) {
        // Get the current Conditional name for selecting the current combo box row
        String cdlName = _curVariable.getName();

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

    // ------------ Variable update processes ------------

    /**
     * Validate Variable data from Edit Variable Window, and transfer it to
     * current action object as appropriate.
     * <p>
     * Messages are sent to the user for any errors found. This routine returns
     * false immediately after finding the first error, even if there might be
     * more errors.
     *
     * @return true if all data checks out OK, otherwise false
     */
    boolean validateVariable() {
        String name = _variableNameField.getText().trim();
        _variableNameField.setText(name);
        _curVariable.setDataString("");
        _curVariable.setNum1(0);
        _curVariable.setNum2(0);
        Conditional.ItemType itemType = _variableItemBox.getItemAt(_variableItemBox.getSelectedIndex());
        Conditional.Type testType = Conditional.Type.NONE;
        switch (itemType) {
            case SENSOR:
            case TURNOUT:
            case LIGHT:
            case SIGNALHEAD:
            case SIGNALMAST:
            case CONDITIONAL:
            case WARRANT:
            case ENTRYEXIT:
                testType = _variableStateBox.getItemAt(_variableStateBox.getSelectedIndex());
                break;
            case MEMORY:
                testType = _variableCompareTypeBox.getItemAt(_variableCompareTypeBox.getSelectedIndex());
                break;
            case CLOCK:
                testType = Conditional.Type.FAST_CLOCK_RANGE;
                break;
            case OBLOCK:
                testType = Conditional.Type.BLOCK_STATUS_EQUALS;
                break;
            default:
                JOptionPane.showMessageDialog(_editConditionalFrame,
                        Bundle.getMessage("ErrorVariableType"),
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return false;
        }
        _curVariable.setType(testType);
        log.debug("validateVariable: itemType= {}, testType= {}", itemType, testType);  // NOI18N
        switch (itemType) {
            case SENSOR:
                name = validateSensorReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case TURNOUT:
                name = validateTurnoutReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case CONDITIONAL:
                name = validateConditionalReference(name);
                if (name == null) {
                    return false;
                }
                _curVariable.setName(name);
                Conditional c = _conditionalManager.getBySystemName(name);
                if (c == null) {
                    return false;
                }
                String uName = c.getUserName();
                if (uName == null || uName.isEmpty()) {
                    _curVariable.setGuiName(c.getSystemName());
                } else {
                    _curVariable.setGuiName(uName);
                }
                break;
            case LIGHT:
                name = validateLightReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case MEMORY:
                name = validateMemoryReference(name);
                if (name == null) {
                    return false;
                }
                String name2 = _variableData1Field.getText();
                if ((testType == Conditional.Type.MEMORY_COMPARE)
                        || (testType == Conditional.Type.MEMORY_COMPARE_INSENSITIVE)) {
                    name2 = validateMemoryReference(name2);
                    if (name2 == null) {
                        return false;
                    }
                }
                _curVariable.setDataString(name2);
                _curVariable.setNum1(_variableCompareOpBox.getSelectedIndex() + 1);
                break;
            case CLOCK:
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
                name = "Clock";  // NOI18N
                break;
            case SIGNALHEAD:
                name = validateSignalHeadReference(name);
                if (name == null) {
                    return false;
                }
                if (testType == Conditional.Type.SIGNAL_HEAD_APPEARANCE_EQUALS) {
                    String appStr = (String) _variableSignalBox.getSelectedItem();
                    Conditional.Type type = ConditionalVariable.stringToVariableTest(appStr);
                    if (type == Conditional.Type.ERROR) {
                        JOptionPane.showMessageDialog(_editConditionalFrame,
                                Bundle.getMessage("ErrorAppearance"),
                                Bundle.getMessage("ErrorTitle"), // NOI18N
                                JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    _curVariable.setType(type);
                    _curVariable.setDataString(appStr);
                    log.debug("SignalHead \"{}\" of type '{}' _variableSignalBox.getSelectedItem()= {}",
                            name, testType, _variableSignalBox.getSelectedItem()); // NOI18N
                }
                break;
            case SIGNALMAST:
                name = validateSignalMastReference(name);
                if (name == null) {
                    return false;
                }
                if (testType == Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS) {
                    if (_variableSignalBox.getSelectedIndex() < 0) {
                        JOptionPane.showMessageDialog(_editConditionalFrame,
                                Bundle.getMessage("ErrorAspect"),
                                Bundle.getMessage("ErrorTitle"), // NOI18N
                                JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    // save the selected aspect for comparison
                    _curVariable.setDataString((String) _variableSignalBox.getSelectedItem());
                    //                _curVariable.setType(ConditionalVariable.stringToVariableTest(appStr));
                }
                break;
            case WARRANT:
                name = validateWarrantReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case OBLOCK:
                name = validateOBlockReference(name);
                if (name == null) {
                    return false;
                }
                String str = _variableStateBox.getSelectedItem().toString();
                _curVariable.setDataString(OBlock.getSystemStatusName(str));
                log.debug("OBlock \"{}\" of type '{}' _variableStateBox.getSelectedItem()= {}",
                        name, testType, _variableStateBox.getSelectedItem()); // NOI18N
                break;
            case ENTRYEXIT:
                name = validateEntryExitReference(name);
                if (name == null) {
                    return false;
                }
                break;
            default:
                JOptionPane.showMessageDialog(_editConditionalFrame,
                        Bundle.getMessage("ErrorVariableType"),
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                return false;
        }
        _curVariable.setName(name);
        boolean result = _curVariable.evaluate();
        log.debug("State Variable \"{}\" of type '{}' state= {} type= {}",  // NOI18N
                name, testType.getTestTypeString(), result, _curVariable.getType());
        if (_curVariable.getType() == Conditional.Type.NONE) {
            JOptionPane.showMessageDialog(_editConditionalFrame,
                    Bundle.getMessage("ErrorVariableState"),
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return (true);
    }

    // ------------ Variable detail listeners ------------

    transient ActionListener variableSignalTestStateListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.debug("variableSignalTestStateListener fires; _variableItemBox.getSelectedIndex()= \"{}\" _variableStateBox.getSelectedIndex()= \"{}\"", // NOI18N
                    _variableItemBox.getSelectedIndex(), _variableStateBox.getSelectedIndex());

            Conditional.ItemType itemType = _variableItemBox.getItemAt(_variableItemBox.getSelectedIndex());

            if (_variableStateBox.getSelectedIndex() == 1) {
                if (itemType == Conditional.ItemType.SIGNALHEAD) {
                    loadJComboBoxWithHeadAppearances(_variableSignalBox, _variableNameField.getText().trim());
                    _variableSignalPanel.setVisible(true);
                } else if (itemType == Conditional.ItemType.SIGNALMAST) {
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
            // fired when signal head name changes, but only
            // while in signal head mode
            log.debug("variableSignalHeadNameListener fires; _variableNameField: {}", _variableNameField.getText().trim());
            loadJComboBoxWithHeadAppearances(_variableSignalBox, _variableNameField.getText().trim());
        }
    };

    transient ActionListener variableSignalMastNameListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // fired when signal mast name changes, but only
            // while in signal mast mode
            log.debug("variableSignalMastNameListener fires; _variableNameField: {}", _variableNameField.getText().trim());  // NOI18N
            loadJComboBoxWithMastAspects(_variableSignalBox, _variableNameField.getText().trim());
        }
    };

    transient ActionListener selectLogixBoxListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String lgxItem = (String) _selectLogixBox.getSelectedItem();
            if (lgxItem != null) {
                String lgxName = _selectLogixMap.get(lgxItem);
                if (lgxName != null) {
                    loadSelectConditionalBox(lgxName);
                }
            }
        }
    };

    transient ActionListener selectConditionalBoxListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int cdlIndex = _selectConditionalBox.getSelectedIndex();
            if (cdlIndex > 0 && cdlIndex < _selectConditionalList.size()) {
                String cdlName = _selectConditionalList.get(cdlIndex);
                _variableNameField.setText(cdlName);
            }
        }
    };

    // ============ Edit Action Window and Methods ============

    /**
     * Create and/or initialize the Edit Action window.
     * <p>
     * Note: you can get here via the New Action button (addActionPressed) or
     * via an Edit button in the Action table of the EditConditional window.
     *
     * @param row index in the table of the Action to be edited
     */
    void makeEditActionWindow(int row) {
        if (alreadyEditingActionOrVariable()) {
            return;
        }
        log.debug("makeEditActionWindow: row = {}", row);
        _curActionRowNumber = row;
        _curAction = _actionList.get(row);
        _editActionFrame = new JmriJFrame(Bundle.getMessage("TitleEditAction"), true, true);  // NOI18N
        JPanel topPanel = makeTopPanel(_editActionFrame, "TitleConsequentPhrase", 600, 160);  // NOI18N

        Box panel1 = Box.createHorizontalBox();
        panel1.add(Box.createHorizontalGlue());

        _actionItemBox = new JComboBox<>();
        for (Conditional.ItemType itemType : Conditional.ItemType.values()) {
            _actionItemBox.addItem(itemType);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_actionItemBox);
        panel1.add(makeEditPanel(_actionItemBox, "LabelActionItem", "ActionItemHint"));  // NOI18N
        panel1.add(Box.createHorizontalStrut(STRUT));

        _actionNameField = new JTextField(30);
        _namePanel = makeEditPanel(_actionNameField, "LabelItemName", null);  // NOI18N
        _namePanel.setMaximumSize(
                new Dimension(50, _namePanel.getPreferredSize().height));
        _namePanel.setVisible(false);
        panel1.add(_namePanel);
        panel1.add(Box.createHorizontalStrut(STRUT));

        // Arbitrary name combo box to facilitate the panel construction
        if (_selectionMode == SelectionMode.USECOMBO) {
            _comboNameBox = createNameBox(Conditional.ItemType.SENSOR);
            _actionComboNamePanel = makeEditPanel(_comboNameBox, "LabelItemName", null);  // NOI18N
            _actionComboNamePanel.setVisible(false);
            panel1.add(_actionComboNamePanel);
            panel1.add(Box.createHorizontalStrut(STRUT));
        }

        _actionTypeBox = new JComboBox<>();
        _actionTypeBox.addItem(Conditional.Action.NONE);
        _actionTypePanel = makeEditPanel(_actionTypeBox, "LabelActionType", "ActionTypeHint");  // NOI18N
        _actionTypePanel.setVisible(false);
        panel1.add(_actionTypePanel);
        panel1.add(Box.createHorizontalStrut(STRUT));

        _actionBox = new JComboBox<String>();
        _actionBox.addItem("");
        _actionPanel = makeEditPanel(_actionBox, "LabelActionType", "ActionTypeHint");  // NOI18N
        _actionPanel.setVisible(false);
        panel1.add(_actionPanel);
        panel1.add(Box.createHorizontalStrut(STRUT));

        _shortActionString = new JTextField(15);
        _shortTextPanel = makeEditPanel(_shortActionString, "LabelActionText", null);  // NOI18N
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
        _optionPanel = makeEditPanel(_actionOptionBox, "LabelActionOption", "ActionOptionHint");  // NOI18N
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
        p.add(new JLabel(Bundle.getMessage("LabelActionFile")));  // NOI18N
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
        _actionSetButton.setToolTipText(Bundle.getMessage("FileButtonHint"));  // NOI18N
        _setPanel.add(Box.createVerticalGlue());
        _setPanel.setVisible(false);
        panel2.add(_setPanel);
        panel2.add(Box.createHorizontalStrut(5));

        _longActionString = new JTextField(50);
        _textPanel = makeEditPanel(_longActionString, "LabelActionText", null);  // NOI18N
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
        // note - this listener cannot be added until all items are entered into _actionItemBox
        _actionItemBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Conditional.ItemType newActionItem = _actionItemBox.getItemAt(_actionItemBox.getSelectedIndex());
                log.debug("_actionItemBox Listener: new = {}, curr = {}, row = {}",  // NOI18N
                        newActionItem, _curActionItem, _curActionRowNumber);
                if (newActionItem != _curActionItem) {
                    if (_curActionRowNumber >= 0) {
                        _curAction = new DefaultConditionalAction();
                        _actionList.set(_curActionRowNumber, _curAction);
                    }
                    _curActionItem = newActionItem;
                }
                actionItemChanged(newActionItem);
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
        _curActionItem = _curAction.getType().getItemType();
        initializeActionVariables();
        _editActionFrame.setVisible(true);
        _editActionFrame.pack();
    }

    // ------------ Main Action methods ------------

    /**
     * Set display to show current action (curAction) parameters.
     */
    void initializeActionVariables() {
        Conditional.Action actionType = _curAction.getType();
        Conditional.ItemType itemType = actionType.getItemType();
        if (log.isDebugEnabled()) {
            log.debug("initializeActionVariables: itemType = {}, actionType = {}", itemType, actionType);  // NOI18N
        }
        if (actionType == Conditional.Action.NONE) {
            return;
        }
        _actionItemBox.setSelectedItem(itemType);
        _actionNameField.setText(_curAction.getDeviceName());
        switch (itemType) {
            case SENSOR:
                _actionTypeBox.setSelectedItem(actionType);
                if ((actionType == Conditional.Action.RESET_DELAYED_SENSOR)
                        || (actionType == Conditional.Action.DELAYED_SENSOR)) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                if (actionType == Conditional.Action.SET_SENSOR
                        || actionType == Conditional.Action.DELAYED_SENSOR
                        || actionType == Conditional.Action.RESET_DELAYED_SENSOR) {
                    if (_curAction.getActionData() == Sensor.ACTIVE) {
                        _actionBox.setSelectedIndex(0);
                    } else if (_curAction.getActionData() == Sensor.INACTIVE) {
                        _actionBox.setSelectedIndex(1);
                    } else if (_curAction.getActionData() == Route.TOGGLE) {
                        _actionBox.setSelectedIndex(2);
                    }
                }
                break;

            case TURNOUT:
                _actionTypeBox.setSelectedItem(actionType);
                if ((actionType == Conditional.Action.RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.Action.DELAYED_TURNOUT)) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                if ((actionType == Conditional.Action.SET_TURNOUT)
                        || (actionType == Conditional.Action.RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.Action.DELAYED_TURNOUT)) {
                    if (_curAction.getActionData() == Turnout.CLOSED) {
                        _actionBox.setSelectedIndex(0);
                    } else if (_curAction.getActionData() == Turnout.THROWN) {
                        _actionBox.setSelectedIndex(1);
                    } else if (_curAction.getActionData() == Route.TOGGLE) {
                        _actionBox.setSelectedIndex(2);
                    }
                } else if (actionType == Conditional.Action.LOCK_TURNOUT) {
                    if (_curAction.getActionData() == Turnout.UNLOCKED) {
                        _actionBox.setSelectedIndex(0);
                    } else if (_curAction.getActionData() == Turnout.LOCKED) {
                        _actionBox.setSelectedIndex(1);
                    } else if (_curAction.getActionData() == Route.TOGGLE) {
                        _actionBox.setSelectedIndex(2);
                    }
                }
                break;

            case LIGHT:
                _actionTypeBox.setSelectedItem(actionType);
                if (actionType == Conditional.Action.SET_LIGHT) {
                    if (_curAction.getActionData() == Light.ON) {
                        _actionBox.setSelectedIndex(0);
                    } else if (_curAction.getActionData() == Light.OFF) {
                        _actionBox.setSelectedIndex(1);
                    } else if (_curAction.getActionData() == Route.TOGGLE) {
                        _actionBox.setSelectedIndex(2);
                    }
                } else if ((actionType == Conditional.Action.SET_LIGHT_INTENSITY)
                        || (actionType == Conditional.Action.SET_LIGHT_TRANSITION_TIME)) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                break;

            case CLOCK:
                _actionTypeBox.setSelectedItem(actionType);
                if (actionType == Conditional.Action.SET_FAST_CLOCK_TIME) {
                    int time = _curAction.getActionData();
                    _longActionString.setText(formatTime(time / 60, time - ((time / 60) * 60)));
                    _actionNameField.setText("");
                }
                break;

            case MEMORY:
                _actionTypeBox.setSelectedItem(actionType);
                _shortActionString.setText(_curAction.getActionString());
                break;


            case WARRANT:
                _actionTypeBox.setSelectedItem(actionType);
                if (actionType == Conditional.Action.CONTROL_TRAIN) {
                    if (_curAction.getActionData() == Warrant.HALT) {
                        _actionBox.setSelectedIndex(0);
                    } else if (_curAction.getActionData() == Warrant.RESUME) {
                        _actionBox.setSelectedIndex(1);
                    } else if (_curAction.getActionData() == Warrant.ABORT) {
                        _actionBox.setSelectedIndex(2);
                    }
                } else if (actionType == Conditional.Action.SET_TRAIN_ID
                        || actionType == Conditional.Action.SET_TRAIN_NAME
                        || actionType == Conditional.Action.THROTTLE_FACTOR) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                break;

            case OBLOCK:
                _actionTypeBox.setSelectedItem(actionType);
                if (actionType == Conditional.Action.SET_BLOCK_VALUE) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                break;

            case ENTRYEXIT:
                _actionTypeBox.setSelectedItem(actionType);
                _actionNameField.setText(_curAction.getBean().getUserName());
                break;

            case AUDIO:
                _actionTypeBox.setSelectedItem(actionType);
                if (actionType == Conditional.Action.PLAY_SOUND) {
                    _longActionString.setText(_curAction.getActionString());
                } else if (actionType == Conditional.Action.CONTROL_AUDIO) {
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
                            log.warn("Unexpected _curAction.getActionData() of {}", _curAction.getActionData());  // NOI18N
                            break;
                    }
                }
                break;

            case SCRIPT:
                _actionTypeBox.setSelectedItem(actionType);
                if (actionType == Conditional.Action.RUN_SCRIPT) {
                    _longActionString.setText(_curAction.getActionString());
                } else if (actionType == Conditional.Action.JYTHON_COMMAND) {
                    _shortActionString.setText(_curAction.getActionString());
                }
                break;

            case SIGNALHEAD:
            case SIGNALMAST:
            case LOGIX:
            case OTHER: // ACTION_TRIGGER_ROUTE
                _actionTypeBox.setSelectedItem(actionType);
                break;

            default:
                log.error("Unhandled type: {}", itemType);  // NOI18N
                break;
        }
        _actionOptionBox.setSelectedIndex(_curAction.getOption() - 1);
        _editActionFrame.pack();
        _editActionFrame.transferFocusBackward();
    }

    /**
     * Respond to a change in an Action Type comboBox on the Edit Conditional
     * Action pane.
     * <p>
     * Set components visible for the selected type.
     *
     * @param type index of the newly selected Action type
     */
    void actionItemChanged(Conditional.ItemType type) {
        Conditional.Action actionType = _curAction.getType();
        if (log.isDebugEnabled()) {
            log.debug("actionItemChanged: itemType = {}, actionType = {}", type, actionType);  // NOI18N
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
        Conditional.ItemType itemType = actionType.getItemType();
        if (type == Conditional.ItemType.NONE && itemType == Conditional.ItemType.NONE) {
            return;
        }
        _actionTypePanel.setVisible(true);
        _actionTypeBox.removeAllItems();
        _actionBox.removeAllItems();
        if (type != Conditional.ItemType.NONE) {  // actionItem listener choice overrides current item
            itemType = type;
        }
        if (itemType != actionType.getItemType()) {
            actionType = Conditional.Action.NONE;    // chosen item type does not support action type
        }
        if (actionType != Conditional.Action.NONE) {
            _optionPanel.setVisible(true);    // item type compatible with action type
        }
        _actionTypeBox.addItem(Conditional.Action.NONE);
        _actionNameField.removeActionListener(actionSignalHeadNameListener);
        _actionNameField.removeActionListener(actionSignalMastNameListener);

        if (_selectionMode == SelectionMode.USECOMBO) {
            _actionComboNamePanel.setVisible(false);
        } else if (_selectionMode == SelectionMode.USESINGLE) {
            createSinglePanelPickList(itemType, new PickSingleListener(_actionNameField, itemType), true);
        } else {
            // Default and USEMULTI
            setPickListTab(itemType, true);
        }

        switch (itemType) {
            case TURNOUT:
                for (Conditional.Action action : Conditional.Action.getTurnoutItems()) {
                    _actionTypeBox.addItem(action);
                }
                if ((actionType == Conditional.Action.RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.Action.DELAYED_TURNOUT)) {
                    JPanel p = (JPanel) _shortTextPanel.getComponent(0);
                    JLabel l = (JLabel) p.getComponent(0);
                    l.setText(Bundle.getMessage("LabelDelayTime"));  // NOI18N
                    _shortTextPanel.setToolTipText(Bundle.getMessage("DataHintDelayedTurnout"));  // NOI18N
                    _shortTextPanel.setVisible(true);
                }
                JPanel panel = (JPanel) _actionPanel.getComponent(0);
                JLabel label = (JLabel) panel.getComponent(0);
                if ((actionType == Conditional.Action.SET_TURNOUT)
                        || (actionType == Conditional.Action.RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.Action.DELAYED_TURNOUT)) {
                    label.setText(Bundle.getMessage("LabelActionTurnout"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("TurnoutStateClosed"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("TurnoutStateThrown"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("Toggle"));  // NOI18N
                    _actionPanel.setToolTipText(Bundle.getMessage("TurnoutSetHint"));  // NOI18N
                    _actionPanel.setVisible(true);
                } else if (actionType == Conditional.Action.LOCK_TURNOUT) {
                    label.setText(Bundle.getMessage("LabelActionLock"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("TurnoutUnlock"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("TurnoutLock"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("Toggle"));  // NOI18N
                    _actionPanel.setToolTipText(Bundle.getMessage("LockSetHint"));  // NOI18N
                    _actionPanel.setVisible(true);
                }
                _namePanel.setToolTipText(Bundle.getMessage("NameHintTurnout"));  // NOI18N
                _namePanel.setVisible(true);
                setActionNameBox(itemType);
                break;

            case SENSOR:
                for (Conditional.Action action : Conditional.Action.getSensorItems()) {
                    _actionTypeBox.addItem(action);
                }
                if ((actionType == Conditional.Action.RESET_DELAYED_SENSOR)
                        || (actionType == Conditional.Action.DELAYED_SENSOR)) {
                    JPanel p = (JPanel) _shortTextPanel.getComponent(0);
                    JLabel l = (JLabel) p.getComponent(0);
                    l.setText(Bundle.getMessage("LabelDelayTime"));  // NOI18N
                    _shortTextPanel.setToolTipText(Bundle.getMessage("DataHintDelayedTurnout"));  // NOI18N
                    _shortTextPanel.setVisible(true);
                }
                if ((actionType == Conditional.Action.SET_SENSOR)
                        || (actionType == Conditional.Action.RESET_DELAYED_SENSOR)
                        || (actionType == Conditional.Action.DELAYED_SENSOR)) {
                    JPanel p = (JPanel) _actionPanel.getComponent(0);
                    JLabel l = (JLabel) p.getComponent(0);
                    l.setText(Bundle.getMessage("LabelActionSensor"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("SensorStateActive"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("SensorStateInactive"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("Toggle"));  // NOI18N
                    _actionPanel.setToolTipText(Bundle.getMessage("SensorSetHint"));  // NOI18N
                    _actionPanel.setVisible(true);
                }
                _namePanel.setToolTipText(Bundle.getMessage("NameHintSensor"));  // NOI18N
                _namePanel.setVisible(true);
                setActionNameBox(itemType);
                break;

            case SIGNALHEAD:
                _actionNameField.addActionListener(actionSignalHeadNameListener);

                for (Conditional.Action action : Conditional.Action.getSignalHeadItems()) {
                    _actionTypeBox.addItem(action);
                }
                if (actionType == Conditional.Action.SET_SIGNAL_APPEARANCE) {
                    JPanel p = (JPanel) _actionPanel.getComponent(0);
                    JLabel l = (JLabel) p.getComponent(0);
                    l.setText(Bundle.getMessage("LabelActionSignal"));  // NOI18N

                    loadJComboBoxWithHeadAppearances(_actionBox, _actionNameField.getText().trim());

                    _actionPanel.setToolTipText(Bundle.getMessage("SignalSetHint"));  // NOI18N
                    _actionPanel.setVisible(true);
                }
                _namePanel.setToolTipText(Bundle.getMessage("NameHintSignal"));  // NOI18N
                _namePanel.setVisible(true);
                setActionNameBox(itemType);
                break;

            case SIGNALMAST:
                _actionNameField.addActionListener(actionSignalMastNameListener);

                for (Conditional.Action action : Conditional.Action.getSignalMastItems()) {
                    _actionTypeBox.addItem(action);
                }
                if (actionType == Conditional.Action.SET_SIGNALMAST_ASPECT) {
                    JPanel p = (JPanel) _actionPanel.getComponent(0);
                    JLabel l = (JLabel) p.getComponent(0);
                    l.setText(Bundle.getMessage("LabelSignalAspect"));  // NOI18N

                    loadJComboBoxWithMastAspects(_actionBox, _actionNameField.getText().trim());

                    _actionPanel.setToolTipText(Bundle.getMessage("SignalMastSetHint"));  // NOI18N
                    _actionPanel.setVisible(true);
                }
                _namePanel.setToolTipText(Bundle.getMessage("NameHintSignalMast"));  // NOI18N
                _namePanel.setVisible(true);
                setActionNameBox(itemType);
                break;

            case LIGHT:
                for (Conditional.Action action : Conditional.Action.getLightItems()) {
                    _actionTypeBox.addItem(action);
                }
                if (actionType == Conditional.Action.SET_LIGHT_INTENSITY) {
                    JPanel p = (JPanel) _shortTextPanel.getComponent(0);
                    JLabel l = (JLabel) p.getComponent(0);
                    l.setText(Bundle.getMessage("LabelLightIntensity"));  // NOI18N
                    _shortTextPanel.setToolTipText(Bundle.getMessage("DataHintLightIntensity"));  // NOI18N
                    _shortTextPanel.setVisible(true);
                } else if (actionType == Conditional.Action.SET_LIGHT_TRANSITION_TIME) {
                    JPanel p = (JPanel) _shortTextPanel.getComponent(0);
                    JLabel l = (JLabel) p.getComponent(0);
                    l.setText(Bundle.getMessage("LabelTransitionTime"));  // NOI18N
                    _shortTextPanel.setToolTipText(Bundle.getMessage("DataHintLightTransitionTime"));  // NOI18N
                    _shortTextPanel.setVisible(true);
                } else if (actionType == Conditional.Action.SET_LIGHT) {
                    JPanel p = (JPanel) _actionPanel.getComponent(0);
                    JLabel l = (JLabel) p.getComponent(0);
                    l.setText(Bundle.getMessage("LabelActionLight"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("LightOn"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("LightOff"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("Toggle"));
                    _actionPanel.setToolTipText(Bundle.getMessage("LightSetHint"));  // NOI18N
                    _actionPanel.setVisible(true);
                }
                _namePanel.setToolTipText(Bundle.getMessage("NameHintLight"));  // NOI18N
                _namePanel.setVisible(true);
                setActionNameBox(itemType);
                break;

            case MEMORY:
                for (Conditional.Action action : Conditional.Action.getMemoryItems()) {
                    _actionTypeBox.addItem(action);
                }
                JPanel p = (JPanel) _shortTextPanel.getComponent(0);
                JLabel l = (JLabel) p.getComponent(0);
                if (actionType == Conditional.Action.COPY_MEMORY) {
                    _shortTextPanel.setToolTipText(Bundle.getMessage("DataHintToMemory"));  // NOI18N
                    l.setText(Bundle.getMessage("LabelMemoryLocation"));  // NOI18N
                } else {
                    _shortTextPanel.setToolTipText(Bundle.getMessage("DataHintMemory"));  // NOI18N
                    l.setText(Bundle.getMessage("LabelValue"));
                }
                _shortTextPanel.setVisible(true);
                _namePanel.setToolTipText(Bundle.getMessage("NameHintMemory"));  // NOI18N
                _namePanel.setVisible(true);
                setActionNameBox(itemType);
                break;

            case CLOCK:
                for (Conditional.Action action : Conditional.Action.getClockItems()) {
                    _actionTypeBox.addItem(action);
                }
                if (actionType == Conditional.Action.SET_FAST_CLOCK_TIME) {
                    p = (JPanel) _textPanel.getComponent(0);
                    l = (JLabel) p.getComponent(0);
                    l.setText(Bundle.getMessage("LabelSetTime"));  // NOI18N
                    _textPanel.setToolTipText(Bundle.getMessage("DataHintTime"));  // NOI18N
                    _textPanel.setVisible(true);
                }
                break;

            case LOGIX:
                for (Conditional.Action action : Conditional.Action.getLogixItems()) {
                    _actionTypeBox.addItem(action);
                }
                _namePanel.setToolTipText(Bundle.getMessage("NameHintLogix"));  // NOI18N
                _namePanel.setVisible(true);
                setActionNameBox(itemType);
                break;

            case WARRANT:
                for (Conditional.Action action : Conditional.Action.getWarrantItems()) {
                    _actionTypeBox.addItem(action);
                }
                _namePanel.setToolTipText(Bundle.getMessage("NameHintWarrant"));  // NOI18N
                _namePanel.setVisible(true);
                if (actionType == Conditional.Action.CONTROL_TRAIN) {
                    p = (JPanel) _actionPanel.getComponent(0);
                    l = (JLabel) p.getComponent(0);
                    _actionBox.addItem(Bundle.getMessage("WarrantHalt"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("WarrantResume"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("WarrantAbort"));  // NOI18N
                    l.setText(Bundle.getMessage("LabelControlTrain"));  // NOI18N
                    _actionPanel.setVisible(true);
                } else if (actionType == Conditional.Action.SET_TRAIN_ID
                        || actionType == Conditional.Action.SET_TRAIN_NAME
                        || actionType == Conditional.Action.THROTTLE_FACTOR) {
                    p = (JPanel) _shortTextPanel.getComponent(0);
                    l = (JLabel) p.getComponent(0);
                    if (actionType == Conditional.Action.SET_TRAIN_ID) {
                        _shortTextPanel.setToolTipText(Bundle.getMessage("DataHintTrainId"));  // NOI18N
                        l.setText(Bundle.getMessage("LabelTrainId"));
                    } else if (actionType == Conditional.Action.SET_TRAIN_NAME) {
                        _shortTextPanel.setToolTipText(Bundle.getMessage("DataHintTrainName"));  // NOI18N
                        l.setText(Bundle.getMessage("LabelTrainName"));
                    } else { // must be Conditional.ACTION_THROTTLE_FACTOR, so treat as such
                        _shortTextPanel.setToolTipText(Bundle.getMessage("DataHintThrottleFactor"));  // NOI18N
                        l.setText(Bundle.getMessage("LabelThrottleFactor"));  // NOI18N
                    }
                    _shortTextPanel.setVisible(true);
                }
                setActionNameBox(itemType);
                break;

            case OBLOCK:
                for (Conditional.Action action : Conditional.Action.getOBlockItems()) {
                    _actionTypeBox.addItem(action);
                }
                _namePanel.setToolTipText(Bundle.getMessage("NameHintOBlock"));  // NOI18N
                _namePanel.setVisible(true);
                if (actionType == Conditional.Action.SET_BLOCK_VALUE) {
                    p = (JPanel) _shortTextPanel.getComponent(0);
                    l = (JLabel) p.getComponent(0);
                    _shortTextPanel.setToolTipText(Bundle.getMessage("DataHintBlockValue"));  // NOI18N
                    l.setText(Bundle.getMessage("LabelBlockValue"));  // NOI18N
                    _shortTextPanel.setVisible(true);
                }
                setActionNameBox(itemType);
                break;

            case ENTRYEXIT:
                for (Conditional.Action action : Conditional.Action.getEntryExitItems()) {
                    _actionTypeBox.addItem(action);
                }
                _namePanel.setToolTipText(Bundle.getMessage("NameHintEntryExit"));  // NOI18N
                _namePanel.setVisible(true);
                setActionNameBox(itemType);
                break;

            case AUDIO:
                for (Conditional.Action action : Conditional.Action.getAudioItems()) {
                    _actionTypeBox.addItem(action);
                }
                if (actionType == Conditional.Action.PLAY_SOUND) {
                    p = (JPanel) _textPanel.getComponent(0);
                    l = (JLabel) p.getComponent(0);
                    l.setText(Bundle.getMessage("LabelSetFile"));  // NOI18N
                    _textPanel.setToolTipText(Bundle.getMessage("SetHintSound"));  // NOI18N
                    _textPanel.setVisible(true);
                    _setPanel.setVisible(true);
                } else if (actionType == Conditional.Action.CONTROL_AUDIO) {
                    p = (JPanel) _actionPanel.getComponent(0);
                    l = (JLabel) p.getComponent(0);
                    l.setText(Bundle.getMessage("LabelActionAudio"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourcePlay"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourceStop"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourcePlayToggle"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourcePause"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourceResume"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourcePauseToggle"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourceRewind"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourceFadeIn"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioSourceFadeOut"));  // NOI18N
                    _actionBox.addItem(Bundle.getMessage("AudioResetPosition"));  // NOI18N
                    _actionPanel.setToolTipText(Bundle.getMessage("SetHintAudio"));  // NOI18N
                    _actionPanel.setVisible(true);
                    _namePanel.setToolTipText(Bundle.getMessage("NameHintAudio"));  // NOI18N
                    _namePanel.setVisible(true);
                }
                break;

            case SCRIPT:
                for (Conditional.Action action : Conditional.Action.getScriptItems()) {
                    _actionTypeBox.addItem(action);
                }
                if (actionType == Conditional.Action.RUN_SCRIPT) {
                    p = (JPanel) _textPanel.getComponent(0);
                    l = (JLabel) p.getComponent(0);
                    l.setText(Bundle.getMessage("LabelSetFile"));  // NOI18N
                    _textPanel.setToolTipText(Bundle.getMessage("SetHintScript"));  // NOI18N
                    _textPanel.setVisible(true);
                    _setPanel.setVisible(true);
                } else if (actionType == Conditional.Action.JYTHON_COMMAND) {
                    p = (JPanel) _shortTextPanel.getComponent(0);
                    l = (JLabel) p.getComponent(0);
                    l.setText(Bundle.getMessage("LabelScriptCommand"));  // NOI18N
                    _shortTextPanel.setToolTipText(Bundle.getMessage("SetHintJythonCmd"));  // NOI18N
                    _shortTextPanel.setVisible(true);
                }
                break;

            case OTHER:
                for (Conditional.Action action : Conditional.Action.getOtherItems()) {
                    _actionTypeBox.addItem(action);
                }
                if (actionType == Conditional.Action.TRIGGER_ROUTE) {
                    _namePanel.setToolTipText(Bundle.getMessage("NameHintRoute"));  // NOI18N
                    _namePanel.setVisible(true);
                    setActionNameBox(itemType);
                }
                break;
            default:
                break;
        }
        _actionTypeBox.setMaximumSize(_actionTypeBox.getPreferredSize());
        _actionBox.setMaximumSize(_actionBox.getPreferredSize());
        _actionTypeListener.setItemType(itemType);
        _actionTypeBox.addActionListener(_actionTypeListener);
        _editActionFrame.pack(); // TODO fit all components
    }

    /**
     * Update the name combo box selection based on the current contents of the
     * name field. Called by {@link #actionItemChanged(Conditional.ItemType)}.
     *
     * @since 4.7.3
     * @param itemType The type of name box to be created.
     */
    void setActionNameBox(Conditional.ItemType itemType) {
        if (_selectionMode != SelectionMode.USECOMBO) {
            return;
        }
        _comboNameBox = createNameBox(itemType);
        if (_comboNameBox == null) {
            return;
        }
        _comboNameBox.setSelectedItemByName(_curAction.getDeviceName());
        _comboNameBox.addActionListener(new NameBoxListener(_actionNameField));
        _actionComboNamePanel.remove(1);
        _actionComboNamePanel.add(_comboNameBox, null, 1);
        _namePanel.setVisible(false);
        _actionComboNamePanel.setVisible(true);
    }

    // ------------ Action detail methods ------------

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
        if (_editConditionalFrame != null) {
            _editConditionalFrame.setVisible(true);
        }
    }

    /**
     * Respond to Cancel action button and window closer of the Edit Action
     * window.
     * <p>
     * Also does cleanup of Update and Delete buttons.
     */
    void cancelEditActionPressed() {
        if (_newItem) {
            deleteActionPressed(_curActionRowNumber);
        } else {
            cleanUpAction();
        }
        if (_editConditionalFrame != null) {
            _editConditionalFrame.setVisible(true);
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
     * Respond to Delete action button in the Edit Action window.
     */
    void deleteActionPressed() {
        deleteActionPressed(_curActionRowNumber);
    }

    /**
     * Respond to Delete action button in an action row of the Edit Conditional
     * pane.
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
        if (_editConditionalFrame != null) {
            _editConditionalFrame.setVisible(true);
        }
        _showReminder = true;
    }

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
        Conditional.Action actionType = action.getType();
        if (actionType == Conditional.Action.PLAY_SOUND) {
            if (sndFileChooser == null) {
                sndFileChooser = new JFileChooser(System.getProperty("user.dir") // NOI18N
                        + java.io.File.separator + "resources" // NOI18N
                        + java.io.File.separator + "sounds");  // NOI18N
                jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("wav sound files");  // NOI18N
                filt.addExtension("wav");  // NOI18N
                sndFileChooser.setFileFilter(filt);
            }
            currentChooser = sndFileChooser;
        } else if (actionType == Conditional.Action.RUN_SCRIPT) {
            if (scriptFileChooser == null) {
                scriptFileChooser = new JFileChooser(FileUtil.getScriptsPath());
                jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("Python script files");  // NOI18N
                filt.addExtension("py");  // NOI18N
                scriptFileChooser.setFileFilter(filt);
            }
            currentChooser = scriptFileChooser;
        } else {
            log.warn("Unexpected actionType[" + actionType.name() + "] = " + actionType.toString());  // NOI18N
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
                    log.error("exception setting file location: " + ex);  // NOI18N
                }
                _longActionString.setText("");
            }
        }
    }

    /*.*
     * Convert user setting in Conditional Action configuration pane to integer
     * for processing.
     *
     * @param itemType            value for current item type
     * @param actionTypeSelection index of selected item in configuration
     *                            comboBox
     * @return integer representing the selected action
     */
/*
    static Conditional.Action getActionTypeFromBox(Conditional.ItemType itemType, int actionTypeSelection) {
//        if (itemType < 0 || actionTypeSelection < 0) {
//            return Conditional.Action.NONE;
//        }
        if (actionTypeSelection < 0) {
            return Conditional.Action.NONE;
        }
        switch (itemType) {
            case SENSOR:
                return Conditional.Action.getSensorItems().get(actionTypeSelection);
            case TURNOUT:
                return Conditional.Action.getTurnoutItems().get(actionTypeSelection);
            case LIGHT:
                return Conditional.Action.getLightItems().get(actionTypeSelection);
            case SIGNALHEAD:
                return Conditional.Action.getSignalHeadItems().get(actionTypeSelection);
            case SIGNALMAST:
                return Conditional.Action.getSignalMastItems().get(actionTypeSelection);
            case MEMORY:
                return Conditional.Action.getMemoryItems().get(actionTypeSelection);
            case LOGIX:
                return Conditional.Action.getLogixItems().get(actionTypeSelection);
            case WARRANT:
                return Conditional.Action.getWarrantItems().get(actionTypeSelection);
            case OBLOCK:
                return Conditional.Action.getOBlockItems().get(actionTypeSelection);
            case CLOCK:
                return Conditional.Action.getClockItems().get(actionTypeSelection);
            case AUDIO:
                return Conditional.Action.getAudioItems().get(actionTypeSelection);
            case SCRIPT:
                return Conditional.Action.getScriptItems().get(actionTypeSelection);
            case OTHER:
                return Conditional.Action.getOtherItems().get(actionTypeSelection);
            case ENTRYEXIT:
                return Conditional.Action.getEntryExitItems().get(actionTypeSelection);
            default:
                // fall through
                break;
        }
        return Conditional.Action.NONE;
    }
*/
    // ------------ Action update processes ------------

    /**
     * Validate Action data from Edit Action Window, and transfer it to current
     * action object as appropriate.
     * <p>
     * Messages are sent to the user for any errors found. This routine returns
     * false immediately after finding an error, even if there might be more
     * errors.
     *
     * @return true if all data checks out OK, otherwise false
     */
    boolean validateAction() {
        Conditional.ItemType itemType = _actionItemBox.getItemAt(_actionItemBox.getSelectedIndex());
        Conditional.Action actionType = Conditional.Action.NONE;
        Conditional.Action selection = _actionTypeBox.getItemAt(_actionTypeBox.getSelectedIndex());
        if (selection == Conditional.Action.NONE) {
            JOptionPane.showMessageDialog(_editConditionalFrame,
                    Bundle.getMessage("makeSelection"), // NOI18N
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.WARNING_MESSAGE);  // NOI18N
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
            case SENSOR:
                if (!referenceByMemory) {
                    name = validateSensorReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                if ((actionType == Conditional.Action.RESET_DELAYED_SENSOR)
                        || (actionType == Conditional.Action.DELAYED_SENSOR)) {
                    if (!validateTimeReference(actionType, actionString)) {
                        return (false);
                    }
                    _curAction.setActionString(actionString);
                }
                if ((actionType == Conditional.Action.SET_SENSOR)
                        || (actionType == Conditional.Action.RESET_DELAYED_SENSOR)
                        || (actionType == Conditional.Action.DELAYED_SENSOR)) {
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
            case TURNOUT:
                if (!referenceByMemory) {
                    name = validateTurnoutReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                if ((actionType == Conditional.Action.RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.Action.DELAYED_TURNOUT)) {
                    if (!validateTimeReference(actionType, actionString)) {
                        return (false);
                    }
                    _curAction.setActionString(actionString);
                }
                if ((actionType == Conditional.Action.SET_TURNOUT)
                        || (actionType == Conditional.Action.RESET_DELAYED_TURNOUT)
                        || (actionType == Conditional.Action.DELAYED_TURNOUT)) {
                    if (_actionBox.getSelectedIndex() == 0) {
                        _curAction.setActionData(Turnout.CLOSED);
                    } else if (_actionBox.getSelectedIndex() == 1) {
                        _curAction.setActionData(Turnout.THROWN);
                    } else {
                        _curAction.setActionData(Route.TOGGLE);
                    }
                } else if (actionType == Conditional.Action.LOCK_TURNOUT) {
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
            case LIGHT:
                if (!referenceByMemory) {
                    name = validateLightReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                if (actionType == Conditional.Action.SET_LIGHT_INTENSITY) {
                    Light lgtx = getLight(name);
                    // check if light user name was entered
                    if (lgtx == null) {
                        return false;
                    }
                    if (!lgtx.isIntensityVariable()) {
                        JOptionPane.showMessageDialog(_editConditionalFrame,
                                Bundle.getMessage("Error45", name), // NOI18N
                                Bundle.getMessage("ErrorTitle"),
                                JOptionPane.ERROR_MESSAGE);  // NOI18N
                        return (false);
                    }
                    if (!validateIntensityReference(actionType, actionString)) {
                        return (false);
                    }
                    _curAction.setActionString(actionString);
                } else if (actionType == Conditional.Action.SET_LIGHT_TRANSITION_TIME) {
                    Light lgtx = getLight(name);
                    // check if light user name was entered
                    if (lgtx == null) {
                        return false;
                    }
                    if (!lgtx.isTransitionAvailable()) {
                        JOptionPane.showMessageDialog(_editConditionalFrame,
                                Bundle.getMessage("Error40", name), // NOI18N
                                Bundle.getMessage("ErrorTitle"),
                                JOptionPane.ERROR_MESSAGE);  // NOI18N
                        return (false);
                    }
                    if (!validateTimeReference(actionType, actionString)) {
                        return (false);
                    }
                    _curAction.setActionString(actionString);
                } else if (actionType == Conditional.Action.SET_LIGHT) {
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
            case SIGNALHEAD:
                if (!referenceByMemory) {
                    name = validateSignalHeadReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                if (actionType == Conditional.Action.SET_SIGNAL_APPEARANCE) {
                    String appStr = (String) _actionBox.getSelectedItem();
                    _curAction.setActionData(DefaultConditionalAction.stringToActionData(appStr));
                    _curAction.setActionString(appStr);
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case SIGNALMAST:
                if (!referenceByMemory) {
                    name = validateSignalMastReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                if (actionType == Conditional.Action.SET_SIGNALMAST_ASPECT) {
                    _curAction.setActionString((String) _actionBox.getSelectedItem());
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case MEMORY:
                if (referenceByMemory) {
                    JOptionPane.showMessageDialog(_editActionFrame,
                            Bundle.getMessage("Warn6"),
                            Bundle.getMessage("WarningTitle"), // NOI18N
                            JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                name = validateMemoryReference(name);
                if (name == null) {
                    return false;
                }
                actionType = selection;
                if (actionType == Conditional.Action.COPY_MEMORY) {
                    actionString = validateMemoryReference(actionString);
                    if (actionString == null) {
                        return false;
                    }
                }
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                _curAction.setActionString(actionString);
                break;
            case LOGIX:
                if (!referenceByMemory) {
                    name = validateLogixReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case WARRANT:
                if (!referenceByMemory) {
                    name = validateWarrantReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                if (actionType == Conditional.Action.CONTROL_TRAIN) {
                    if (_actionBox.getSelectedIndex() == 0) {
                        _curAction.setActionData(Warrant.HALT);
                    } else if (_actionBox.getSelectedIndex() == 1) {
                        _curAction.setActionData(Warrant.RESUME);
                    } else {
                        _curAction.setActionData(Warrant.ABORT);
                    }
                } else if (actionType == Conditional.Action.SET_TRAIN_ID
                        || actionType == Conditional.Action.SET_TRAIN_NAME
                        || actionType == Conditional.Action.THROTTLE_FACTOR) {
                    _curAction.setActionString(actionString);
                }
                break;
            case OBLOCK:
                if (!referenceByMemory) {
                    name = validateOBlockReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                if (actionType == Conditional.Action.SET_BLOCK_VALUE) {
                    _curAction.setActionString(actionString);
                }
                break;
            case ENTRYEXIT:
                if (!referenceByMemory) {
                    name = validateEntryExitReference(name);
                    if (name == null) {
                        return false;
                    }
                }
                actionType = selection;
                _actionNameField.setText(name);
                _curAction.setDeviceName(name);
                break;
            case CLOCK:
                actionType = selection;
                if (actionType == Conditional.Action.SET_FAST_CLOCK_TIME) {
                    int time = parseTime(_longActionString.getText().trim());
                    if (time < 0) {
                        return (false);
                    }
                    _curAction.setActionData(time);
                }
                break;
            case AUDIO:
                actionType = selection;
                if (actionType == Conditional.Action.PLAY_SOUND) {
                    _curAction.setActionString(_longActionString.getText().trim());
                } else if (actionType == Conditional.Action.CONTROL_AUDIO) {
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
                            log.warn("Unexpected _actionBox.getSelectedIndex() of {}", _actionBox.getSelectedIndex());  // NOI18N
                            break;
                    }
                }
                break;
            case SCRIPT:
                actionType = selection;
                if (actionType == Conditional.Action.RUN_SCRIPT) {
                    _curAction.setActionString(_longActionString.getText().trim());
                } else if (actionType == Conditional.Action.JYTHON_COMMAND) {
                    _curAction.setActionString(_shortActionString.getText().trim());
                }
                break;
            case OTHER:
                actionType = selection;
                if (actionType == Conditional.Action.TRIGGER_ROUTE) {
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
        if (actionType != Conditional.Action.NONE) {
            _curAction.setOption(_actionOptionBox.getSelectedIndex() + 1);
        } else {
            _curAction.setOption(0);
        }
        _editActionFrame.pack();
        return (true);
    }

    // ------------ Action detail listeners ------------
    /**
     * Listener for _actionTypeBox.
     */
    class ActionTypeListener implements ActionListener {

        Conditional.ItemType _itemType;

        @Override
        public void actionPerformed(ActionEvent e) {
            Conditional.ItemType select1 = _actionItemBox.getItemAt(_actionItemBox.getSelectedIndex());
            Conditional.Action select2 = _actionTypeBox.getItemAt(_actionTypeBox.getSelectedIndex());
            log.debug("ActionTypeListener: actionItemType= {}, _itemType= {}, action= {}",
                    select1, _itemType, select2);  // NOI18N
            if (select1 != _itemType) {
                log.debug("ActionTypeListener actionItem selection ({}) != expected actionItem ({})",
                        select1, _itemType);  // NOI18N
            }
            if (_curAction != null) {
                if (select1 != Conditional.ItemType.NONE && _itemType == select1) {
                    _curAction.setType(select2);
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

        public void setItemType(Conditional.ItemType type) {
            _itemType = type;
        }
    }
    ActionTypeListener _actionTypeListener = new ActionTypeListener();

    transient ActionListener actionSignalHeadNameListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // fired when signal head name changes, but only
            // while in signal head mode
            log.debug("actionSignalHeadNameListener fires; _actionNameField: {}", _actionNameField.getText().trim());  // NOI18N
            loadJComboBoxWithHeadAppearances(_actionBox, _actionNameField.getText().trim());
        }
    };

    transient ActionListener actionSignalMastNameListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // fired when signal mast name changes, but only
            // while in signal mast mode
            log.debug("actionSignalMastNameListener fires; _actionNameField: {}", _actionNameField.getText().trim());  // NOI18N
            loadJComboBoxWithMastAspects(_actionBox, _actionNameField.getText().trim());
        }
    };

    // ------------ Table Models ------------

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
                    cdl.setUserName(uName);
                    fireTableRowsUpdated(row, row);

                    // Update any conditional references
                    ArrayList<String> refList = InstanceManager.getDefault(jmri.ConditionalManager.class).getWhereUsed(sName);
                    if (refList != null) {
                        for (String ref : refList) {
                            Conditional cRef = _conditionalManager.getBySystemName(ref);
                            List<ConditionalVariable> varList = cRef.getCopyOfStateVariables();
                            for (ConditionalVariable var : varList) {
                                // Find the affected conditional variable
                                if (var.getName().equals(sName)) {
                                    if (uName.length() > 0) {
                                        var.setGuiName(uName);
                                    } else {
                                        var.setGuiName(sName);
                                    }
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
                case DELETE_COLUMN:
                    return JButton.class;
                default:
                    // fall through
                    break;
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
                case DESCRIPTION_COLUMN:
                case STATE_COLUMN:
                    return (false);
                case AND_COLUMN:
                    return (_logicType == Conditional.AntecedentOperator.MIXED);
                case NOT_COLUMN:
                case TRIGGERS_COLUMN:
                    return (true);
                case EDIT_COLUMN:
                    if (_inVarReorder) {
                        return false;
                    }
                    return (true);
                case DELETE_COLUMN:
                    if (_inVarReorder && r < _nextInOrder) {
                        return false;
                    }
                    return true;
                default:
                    // fall through
                    break;
            }
            return false;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case ROWNUM_COLUMN:
                    return (Bundle.getMessage("ColumnLabelRow"));  // NOI18N
                case AND_COLUMN:
                    return (Bundle.getMessage("ColumnLabelOperator"));  // NOI18N
                case NOT_COLUMN:
                    return (Bundle.getMessage("ColumnLabelNot"));  // NOI18N
                case DESCRIPTION_COLUMN:
                    return (Bundle.getMessage("ColumnLabelDescription"));  // NOI18N
                case STATE_COLUMN:
                    return (Bundle.getMessage("ColumnState"));  // NOI18N
                case TRIGGERS_COLUMN:
                    return (Bundle.getMessage("ColumnLabelTriggersCalculation"));  // NOI18N
                case EDIT_COLUMN:
                    return "";
                case DELETE_COLUMN:
                    return "";
                default:
                    // fall through
                    break;
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
        public Object getValueAt(int row, int col) {
            if (row >= _variableList.size()) {
                return null;
            }
            ConditionalVariable variable = _variableList.get(row);
            switch (col) {
                case ROWNUM_COLUMN:
                    return ("R" + (row + 1)); //NOI18N
                case AND_COLUMN:
                    if (row == 0) { //removed: || _logicType == Conditional.MIXED
                        return "";
                    }
                    return variable.getOpernString(); // also display Operand selection when set to Mixed
                case NOT_COLUMN:
                    if (variable.isNegated()) {
                        return Bundle.getMessage("LogicNOT");  // NOI18N
                    }
                    break;
                case DESCRIPTION_COLUMN:
                    return variable.toString();
                case STATE_COLUMN:
                    switch (variable.getState()) {
                        case Conditional.TRUE:
                            return Bundle.getMessage("True");  // NOI18N
                        case Conditional.FALSE:
                            return Bundle.getMessage("False");  // NOI18N
                        case NamedBean.UNKNOWN:
                            return Bundle.getMessage("BeanStateUnknown");  // NOI18N
                        default:
                            log.warn("Unhandled state type: {}", variable.getState());  // NOI18N
                            break;
                    }
                    break;
                case TRIGGERS_COLUMN:
                    return variable.doTriggerActions();
                case EDIT_COLUMN:
                    return Bundle.getMessage("ButtonEdit");  // NOI18N
                case DELETE_COLUMN:
                    if (!_inVarReorder) {
                        return Bundle.getMessage("ButtonDelete");  // NOI18N
                    } else if (_nextInOrder == 0) {
                        return Bundle.getMessage("ButtonFirst");  // NOI18N
                    } else if (_nextInOrder <= row) {
                        return Bundle.getMessage("ButtonNext");  // NOI18N
                    }
                    return Integer.toString(row + 1);
                default:
                    break;
            }
            return null;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (row >= _variableList.size()) {
                return;
            }
            ConditionalVariable variable = _variableList.get(row);
            switch (col) {
                case AND_COLUMN:
                    variableOperatorChanged(row, (String) value);
                    break;
                case NOT_COLUMN:
                    variableNegationChanged(row, (String) value);
                    break;
                case STATE_COLUMN:
                    String state = ((String) value);
                    if (state.equals(Bundle.getMessage("True").toUpperCase().trim())) {  // NOI18N
                        variable.setState(State.TRUE.getIntValue());
                    } else if (state.equals(Bundle.getMessage("False").toUpperCase().trim())) {  // NOI18N
                        variable.setState(State.FALSE.getIntValue());
                    } else {
                        variable.setState(State.UNKNOWN.getIntValue());
                    }
                    break;
                case TRIGGERS_COLUMN:
                    variable.setTriggerActions(!variable.doTriggerActions());
                    break;
                case EDIT_COLUMN:
                    if (LRouteTableAction.LOGIX_INITIALIZER.equals(_curLogix.getSystemName())) {
                        JOptionPane.showMessageDialog(_editConditionalFrame,
                                Bundle.getMessage("Error49"),
                                Bundle.getMessage("ErrorTitle"), // NOI18N
                                JOptionPane.ERROR_MESSAGE);
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
                    WindowMaker t = new WindowMaker(row);
                    javax.swing.SwingUtilities.invokeLater(t);
                    break;
                case DELETE_COLUMN:
                    if (_inVarReorder) {
                        swapVariables(row);
                    } else {
                        deleteVariablePressed(row);
                    }
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
            if (_inActReorder && (c == EDIT_COLUMN || r < _nextInOrder)) {
                return false;
            }
            return true;
        }

        @Override
        public String getColumnName(int col) {
            if (col == DESCRIPTION_COLUMN) {
                return Bundle.getMessage("LabelActionDescription");  // NOI18N
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
                    return Bundle.getMessage("ButtonEdit");  // NOI18N
                case DELETE_COLUMN:
                    if (!_inActReorder) {
                        return Bundle.getMessage("ButtonDelete");  // NOI18N
                    } else if (_nextInOrder == 0) {
                        return Bundle.getMessage("ButtonFirst");  // NOI18N
                    } else if (_nextInOrder <= row) {
                        return Bundle.getMessage("ButtonNext");  // NOI18N
                    }
                    return Integer.toString(row + 1);
                default:
                    // fall through
                    break;
            }
            return null;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == EDIT_COLUMN) {
                // Use separate Runnable so window is created on top
                class WindowMaker implements Runnable {

                    private int _row;

                    WindowMaker(int r) {
                        _row = r;
                    }

                    @Override
                    public void run() {
                        makeEditActionWindow(_row);
                    }
                }
                WindowMaker t = new WindowMaker(row);
                javax.swing.SwingUtilities.invokeLater(t);
            } else if (col == DELETE_COLUMN) {
                if (_inActReorder) {
                    swapActions(row);
                } else {
                    deleteActionPressed(row);
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
