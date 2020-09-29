package jmri.jmrit.conditional;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.SortedSet;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;

import jmri.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.conditional.ConditionalEditBase.SelectionMode;
import jmri.jmrit.entryexit.EntryExitPairs;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.WarrantManager;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Extracted from ConditionalEditList.
 * Allows ConditionalEditList to open alternate frame
 * for copying Conditionals.
 * 
 * @author Pete Cressman Copyright (C) 2020
 */
public class ConditionalCopyFrame extends ConditionalFrame {

    CopyTableModel _actionTableModel = null;
    CopyTableModel _variableTableModel = null;
    JTextField _antecedentField;
    JPanel _antecedentPanel;

    Conditional.ItemType _saveType = Conditional.ItemType.NONE;
    static final int STRUT = 10;

    // ------------------------------------------------------------------
    
    ConditionalCopyFrame(String title, Conditional conditional, ConditionalList parent) {
        super(title, conditional, parent);
        makeConditionalFrame(conditional);
    }

    void makeConditionalFrame(Conditional conditional) {
        addHelpMenu(
                "package.jmri.jmrit.conditional.ConditionalCopy", true);  // NOI18N
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(makeTopPanel(conditional));

        // add Logical Expression Section
        JPanel logicPanel = new JPanel();
        logicPanel.setLayout(new BoxLayout(logicPanel, BoxLayout.Y_AXIS));

        // add Antecedent Expression Panel - ONLY appears for MIXED operator statements
        _antecedentField = new JTextField(65);
        _antecedentField.setText(ConditionalEditBase.translateAntecedent(_antecedent, false));
        _antecedentField.setFont(new Font("SansSerif", Font.BOLD, 14));  // NOI18N
        _antecedentPanel = makeEditPanel(_antecedentField, "LabelAntecedent", "LabelAntecedentHint");  // NOI18N
        _antecedentPanel.setVisible(_logicType == Conditional.AntecedentOperator.MIXED);
        logicPanel.add(_antecedentPanel);

        // add state variable table title
        JPanel varTitle = new JPanel();
        varTitle.setLayout(new FlowLayout());
        varTitle.add(new JLabel(Bundle.getMessage("StateVariableTableTitle")));  // NOI18N
        logicPanel.add(varTitle);
        // initialize table of state variables
        _variableTableModel = new VariableCopyTableModel(false, _parent._selectionMode != SelectionMode.USESINGLE);
        JTable variableTable = new JTable(_variableTableModel);
        variableTable.setRowSelectionAllowed(false);
        int rowHeight = variableTable.getRowHeight();

        TableColumnModel variableColumnModel = variableTable.getColumnModel();

        TableColumn rowColumn = variableColumnModel.getColumn(VariableCopyTableModel.ROWNUM_COLUMN);
        rowColumn.setResizable(false);
        rowColumn.setMaxWidth(new JTextField(3).getPreferredSize().width);

        TableColumn nameColumn = variableColumnModel.getColumn(VariableCopyTableModel.NAME_COLUMN);
        nameColumn.setResizable(false);
        if (_parent._selectionMode != SelectionMode.USESINGLE) {
            nameColumn.setCellEditor(new NameCellEditor(new JComboBox<String>()));
        } else {
            nameColumn.setCellEditor(new NameCellEditor(new JTextField()));            
        }
        nameColumn.setMinWidth(40);
        nameColumn.setResizable(true);

        TableColumn descColumn = variableColumnModel.getColumn(VariableCopyTableModel.DESCRIPTION_COLUMN);
        descColumn.setMinWidth(300);
        descColumn.setResizable(true);

        // add a scroll pane
        JScrollPane variableTableScrollPane = new JScrollPane(variableTable);
        Dimension dim = variableTable.getPreferredSize();
        dim.height = 7 * rowHeight;
        variableTableScrollPane.getViewport().setPreferredSize(dim);

        logicPanel.add(variableTableScrollPane);


        Border logicPanelBorder = BorderFactory.createEtchedBorder();
        Border logicPanelTitled = BorderFactory.createTitledBorder(
                logicPanelBorder, Bundle.getMessage("TitleLogicalExpression") + " ");  // NOI18N
        logicPanel.setBorder(logicPanelTitled);
        contentPane.add(logicPanel);
        // End of Logic Expression Section

        // add Action Consequents Section
        JPanel conseqentPanel = new JPanel();
        conseqentPanel.setLayout(new BoxLayout(conseqentPanel, BoxLayout.Y_AXIS));

        JPanel actTitle = new JPanel();
        actTitle.setLayout(new FlowLayout());
        actTitle.add(new JLabel(Bundle.getMessage("ActionTableTitle")));  // NOI18N
        conseqentPanel.add(actTitle);

        // set up action consequents table
        _actionTableModel = new ActionCopyTableModel(true, _parent._selectionMode != SelectionMode.USESINGLE);
        JTable actionTable = new JTable(_actionTableModel);
        actionTable.setRowSelectionAllowed(false);
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        actionTable.setDefaultRenderer(JButton.class, buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        actionTable.setDefaultEditor(JButton.class, buttonEditor);
        JButton testButton = new JButton("XXXXXX");  // NOI18N
        actionTable.setRowHeight(testButton.getPreferredSize().height);
        TableColumnModel actionColumnModel = actionTable.getColumnModel();

        nameColumn = actionColumnModel.getColumn(ActionCopyTableModel.NAME_COLUMN);
        nameColumn.setResizable(false);
        if (_parent._selectionMode != SelectionMode.USESINGLE) {
            nameColumn.setCellEditor(new NameCellEditor(new JComboBox<String>()));
        } else {
            nameColumn.setCellEditor(new NameCellEditor(new JTextField()));            
        }
        nameColumn.setMinWidth(40);
        nameColumn.setResizable(true);

        descColumn = actionColumnModel.getColumn(ActionCopyTableModel.DESCRIPTION_COLUMN);
        descColumn.setMinWidth(300);
        descColumn.setResizable(true);

        TableColumn deleteColumn = actionColumnModel.getColumn(ActionCopyTableModel.DELETE_COLUMN);
        // ButtonRenderer and TableCellEditor already set
        deleteColumn.setMinWidth(testButton.getPreferredSize().width);
        deleteColumn.setMaxWidth(testButton.getPreferredSize().width);
        deleteColumn.setResizable(false);
        
        // add a scroll pane
        JScrollPane actionTableScrollPane = new JScrollPane(actionTable);
        dim = actionTableScrollPane.getPreferredSize();
        dim.height = 7 * rowHeight;
        actionTableScrollPane.getViewport().setPreferredSize(dim);
        conseqentPanel.add(actionTableScrollPane);


        Border conseqentPanelBorder = BorderFactory.createEtchedBorder();
        Border conseqentPanelTitled = BorderFactory.createTitledBorder(
                conseqentPanelBorder, Bundle.getMessage("TitleAction"));  // NOI18N
        conseqentPanel.setBorder(conseqentPanelTitled);
        contentPane.add(conseqentPanel);
        // End of Action Consequents Section

        contentPane.add(_parent.makeBottomPanel());
        
        // setup window closing listener
        this.addWindowListener(
                new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancelConditionalPressed();
            }
        });
        // initialize state variable table
        _variableTableModel.fireTableDataChanged();
        // initialize action variables
        _actionTableModel.fireTableDataChanged();
    }   // end makeConditionalFrame

    class NameCellEditor extends DefaultCellEditor {

        NameCellEditor(JComboBox<String> comboBox) {
            super(comboBox);
            log.debug("New JComboBox<String> NameCellEditor");
        }

        NameCellEditor(JTextField textField) {
            super(textField);
            log.debug("New JTextField NameCellEditor");
        }

        @SuppressWarnings("unchecked") // getComponent call requires an unchecked cast
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            CopyTableModel model = (CopyTableModel) table.getModel();
            if (log.isDebugEnabled()) {
                log.debug("getTableCellEditorComponent: row= {}, column= {} selected = {} isComboTable= {}",
                        row, column, isSelected, model._isComboTable);
            }
            Conditional.ItemType itemType;
            String name;
            if (model.isActionTable()) {
                ConditionalAction action = _actionList.get(row);
                itemType = action.getType().getItemType();
                name = action.getDeviceName();
            } else {
                ConditionalVariable variable = _variableList.get(row);
                itemType = variable.getType().getItemType();
                name = variable.getName();
            }
            if (model._isComboTable) {
                SortedSet<NamedBean> namedBeans = (SortedSet<NamedBean>)getItemNamedBeamns(itemType);
                JComboBox<String> comboBox = (JComboBox<String>)getComponent();
                comboBox.removeAllItems();
                for (NamedBean b : namedBeans) {
                    comboBox.addItem(b.getDisplayName());
                }
            } else {
                if (_saveType != itemType) {
                    _parent.closeSinglePanelPickList();
                    _parent.createSinglePanelPickList(itemType, null, true);
                    _saveType = itemType;
                }
                JTextField field = (JTextField)getComponent();
                field.setText(name);
            }
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
    }

    SortedSet<?> getItemNamedBeamns(Conditional.ItemType itemType) {
        switch (itemType) {
            case SENSOR:      // 1
                return InstanceManager.getDefault(SensorManager.class).getNamedBeanSet();
            case TURNOUT:     // 2
                return InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet();
            case LIGHT:       // 3
                return InstanceManager.getDefault(LightManager.class).getNamedBeanSet();
            case SIGNALHEAD:  // 4
                return InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet();
            case SIGNALMAST:  // 5
                return InstanceManager.getDefault(SignalMastManager.class).getNamedBeanSet();
            case MEMORY:      // 6
                return InstanceManager.getDefault(MemoryManager.class).getNamedBeanSet();
            case LOGIX:       // 7
                return InstanceManager.getDefault(LogixManager.class).getNamedBeanSet();
            case WARRANT:     // 8
                return InstanceManager.getDefault(WarrantManager.class).getNamedBeanSet();
            case OBLOCK:      // 10
                return InstanceManager.getDefault(OBlockManager.class).getNamedBeanSet();
            case ENTRYEXIT:   // 11
                return InstanceManager.getDefault(EntryExitPairs.class).getNamedBeanSet();
            case OTHER:   // 14
                return InstanceManager.getDefault(jmri.RouteManager.class).getNamedBeanSet();
            default:
                return new java.util.TreeSet<String>();             // Skip any other items.
        }
    }
    /**
     * Respond to the Cancel button in the Edit Conditional frame.
     * <p>
     * Does the cleanup from updateConditionalPressed
     * and _editConditionalFrame window closer.
     */
    @Override
    void cancelConditionalPressed() {
        super.cancelConditionalPressed();
    }

    /**
     * Validate Variable name change.
     * <p>
     * Messages are sent to the user for any errors found. This routine returns
     * false immediately after finding the first error, even if there might be
     * more errors.
     *
     * @param name name of the ConditionalVariable
     * @param variable ConditionalVariable to validate
     * @return true if all data checks out OK, otherwise false
     */
    boolean validateVariable(String name, ConditionalVariable variable) {
        Conditional.ItemType itemType = variable.getType().getItemType();

        if (!checkIsAction(name, itemType) ) {
            return false;
        }
        if (!isValidType(itemType, name)) {
            return false;
        }
        return (true);
    }
    /**
     * Validate Action item name change.
     * <p>
     * Messages are sent to the user for any errors found. This routine returns
     * false immediately after finding an error, even if there might be more
     * errors.
     *
     * @param name name of the action
     * @param action ConditionalAction to validate
     * @return true if all data checks out OK, otherwise false
     */
    boolean validateAction(String name, ConditionalAction action) {
        if (!checkReferenceByMemory(name)) {
            return false;
        }
        Conditional.ItemType itemType = action.getType().getItemType();
        if (_referenceByMemory) {
            if (itemType == Conditional.ItemType.MEMORY) {
                JOptionPane.showMessageDialog(this,
                        Bundle.getMessage("Warn6"),
                        Bundle.getMessage("WarningTitle"), // NOI18N
                        JOptionPane.WARNING_MESSAGE);
                return false;                
            }
        } else {
            if (!checkIsVariable(name, itemType) ) {
                return false;
            }
            if (!isValidType(itemType, name)) {
                return false;
            }
        }
        return (true);
    }

    boolean isValidType( Conditional.ItemType itemType, String name) {
        switch (itemType) {
            case SENSOR:
                name = _parent.validateSensorReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case TURNOUT:
                name = _parent.validateTurnoutReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case CONDITIONAL:
                name = _parent.validateConditionalReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case LIGHT:
                name = _parent.validateLightReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case SIGNALHEAD:
                name = _parent.validateSignalHeadReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case SIGNALMAST:
                name = _parent.validateSignalMastReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case MEMORY:
                name = _parent.validateMemoryReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case LOGIX:
                name = _parent.validateLogixReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case WARRANT:
                name = _parent.validateWarrantReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case OBLOCK:
                name = _parent.validateOBlockReference(name);
                if (name == null) {
                    return false;
                }
                break;
            case ENTRYEXIT:
                name = _parent.validateEntryExitReference(name);
                if (name == null) {
                    return false;
                }
                break;
            default:
                break;
        }
        return true;
    }

    //------------------- Table Models ----------------------

    abstract class CopyTableModel extends AbstractTableModel{
        
        boolean _isActionTable;
        boolean _isComboTable;

        CopyTableModel(boolean isAction, boolean isCombo) {
            _isActionTable = isAction;
            _isComboTable = isCombo;
            log.debug("CopyTableModel: isAction= {}, _isComboTable= {}", isAction, _isComboTable);
        }

        boolean isActionTable() {
            return _isActionTable;
        }

        boolean isComboTable() {
            return _isComboTable;
        }
    }

    class VariableCopyTableModel extends CopyTableModel{
        
        static final int ROWNUM_COLUMN = 0;
        static final int NAME_COLUMN = 1;
        static final int DESCRIPTION_COLUMN = 2;

        VariableCopyTableModel(boolean isAction, boolean isCombo) {
            super(isAction, isCombo);
        }

        @Override
        public Class<?> getColumnClass(int c) {
            switch (c) {
                case NAME_COLUMN:
                    if (_isComboTable) {
                        return JComboBox.class;
                    } else {
                        return JTextField.class;
                    }
                case DESCRIPTION_COLUMN:
                    return String.class;
                default:
                    // fall through
                    break;
            }
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public boolean isCellEditable(int r, int col) {
            if (col == NAME_COLUMN) {
                return true;
            }
            return false;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case ROWNUM_COLUMN:
                    return (Bundle.getMessage("ColumnLabelRow"));  // NOI18N
                case NAME_COLUMN:
                    return (Bundle.getMessage("ColumnLabelName"));  // NOI18N
                case DESCRIPTION_COLUMN:
                    return (Bundle.getMessage("ColumnLabelDescription"));  // NOI18N
                default:
                    break;
            }
            return "";
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case ROWNUM_COLUMN:
                    return 10;
                case NAME_COLUMN:
                    return 200;
                case DESCRIPTION_COLUMN:
                    return 600;
                default:
                    break;
            }
            return 10;
        }

        @Override
        public int getRowCount() {
            return _variableList.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (row >= getRowCount()) {
                return null;
            }
            switch (col) {
                case ROWNUM_COLUMN:
                    return ("R" + (row + 1)); // NOI18N
                case NAME_COLUMN:
                    return _variableList.get(row).getName();  // NOI18N
               case DESCRIPTION_COLUMN:
                    return _variableList.get(row).toString();
                default:
                    break;
            }
            return null;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (row >= getRowCount()) {
                return;
            }
            if (col == NAME_COLUMN) {
                String name = (String)value;
                ConditionalVariable variable = _variableList.get(row);
                if (validateVariable(name, variable)) {
                    variable.setName(name);
                    this.fireTableRowsDeleted(row, row);
                }
            }
        }        
    }

    class ActionCopyTableModel extends CopyTableModel {
        
        static final int NAME_COLUMN = 0;
        static final int DESCRIPTION_COLUMN = 1;
        static final int DELETE_COLUMN = 2;

        boolean _isActionTable;
        boolean _isComboTable;

        ActionCopyTableModel(boolean isAction, boolean isCombo) {
            super(isAction, isCombo);
        }

        @Override
        public Class<?> getColumnClass(int c) {
            switch (c) {
                case NAME_COLUMN:
                    if (_isComboTable) {
                        return JComboBox.class;
                    } else {
                        return JTextField.class;
                    }
                case DESCRIPTION_COLUMN:
                    return String.class;
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
            return 3;
        }

        @Override
        public boolean isCellEditable(int r, int col) {
            if (col == DESCRIPTION_COLUMN) {
                return false;
            }
            return true;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case NAME_COLUMN:
                    return (Bundle.getMessage("ColumnLabelName"));  // NOI18N
                case DESCRIPTION_COLUMN:
                    return (Bundle.getMessage("ColumnLabelDescription"));  // NOI18N
                case DELETE_COLUMN:
                    return "";
                default:
                    break;
            }
            return "";
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case NAME_COLUMN:
                    return 200;
                case DESCRIPTION_COLUMN:
                    return 600;
                default:
                    break;
            }
            return 10;
        }

        @Override
        public int getRowCount() {
            return _actionList.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (row >= getRowCount()) {
                return null;
            }
            ConditionalAction action = _actionList.get(row);
            switch (col) {
                case NAME_COLUMN:
                    return action.getDeviceName();
               case DESCRIPTION_COLUMN:
                   return action.description(_parent._curConditional.getTriggerOnChange());
               case DELETE_COLUMN:
                   return Bundle.getMessage("ButtonDelete");  // NOI18N
                default:
                    break;
            }
            return null;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (row >= getRowCount()) {
                return;
            }
            if (col == NAME_COLUMN) {
                ConditionalAction action = _actionList.get(row);
                String name = (String)value;
                if (validateAction(name, action)) {
                    action.setDeviceName(name);
                    this.fireTableRowsDeleted(row, row);
                }
            } else if (col == DELETE_COLUMN) {
                _actionList.remove(row);
                this.fireTableRowsDeleted(row, row);
            }
        }        
    }

    private final static Logger log = LoggerFactory.getLogger(ConditionalCopyFrame.class);
}
