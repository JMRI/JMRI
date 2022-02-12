package jmri.jmrit.logixng.actions.swing;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionMemory;
import jmri.jmrit.logixng.actions.ActionMemory.MemoryOperation;
import jmri.jmrit.logixng.swing.LogixNG_DataDialog;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;

/**
 * Configures an ActionMemory object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionMemorySwing extends AbstractDigitalActionSwing {

    private final LogixNG_DataDialog _logixNG_DataDialog = new LogixNG_DataDialog(this);
    private WindowFocusListener _focusListener;

    private JTabbedPane _tabbedPaneMemory;
    private BeanSelectPanel<Memory> _memoryBeanPanel;
    private BeanSelectPanel<NamedTable> _copyTableBeanPanel;
    private JPanel _panelMemoryDirect;
    private JPanel _panelMemoryReference;
    private JPanel _panelMemoryLocalVariable;
    private JPanel _panelMemoryFormula;
    private JTextField _memoryReferenceTextField;
    private JTextField _memoryLocalVariableTextField;
    private JTextField _memoryFormulaTextField;

    private JTabbedPane _tabbedPaneMemoryOperation;
    private BeanSelectPanel<Memory> _copyMemoryBeanPanel;
    private JPanel _setToNull;
    private JPanel _setToConstant;
    private JPanel _copyMemory;
    private JPanel _copyTableCell;
    private JPanel _copyVariable;
    private JPanel _calculateFormula;
    private JTextField _setToConstantTextField;
//    private JTextField _copyTableCellTextField;
    private JTextField _copyLocalVariableTextField;
    private JTextField _calculateFormulaTextField;
    private JButton _editTableNameButton;
    private JButton _editRowNameButton;
    private JButton _editColumnNameButton;
    private JLabel _tableNameLabel;
    private JLabel _rowNameLabel;
    private JLabel _columnNameLabel;

    private NamedBeanAddressing _tableNameAddressing = NamedBeanAddressing.Direct;
    private JTextField _tableNameReferenceTextField;
    private JTextField _tableNameLocalVariableTextField;
    private JTextField _tableNameFormulaTextField;

    private NamedBeanAddressing _tableRowAddressing = NamedBeanAddressing.Direct;
    private JComboBox<String> _tableRowNameComboBox;
    private JTextField _tableRowNameTextField;
    private JTextField _tableRowReferenceTextField;
    private JTextField _tableRowLocalVariableTextField;
    private JTextField _tableRowFormulaTextField;

    private NamedBeanAddressing _tableColumnAddressing = NamedBeanAddressing.Direct;
    private JComboBox<String> _tableColumnNameComboBox;
    private JTextField _tableColumnNameTextField;
    private JTextField _tableColumnReferenceTextField;
    private JTextField _tableColumnLocalVariableTextField;
    private JTextField _tableColumnFormulaTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionMemory action = (ActionMemory)object;

        panel = new JPanel();

        _tabbedPaneMemory = new JTabbedPane();
        _panelMemoryDirect = new javax.swing.JPanel();
        _panelMemoryReference = new javax.swing.JPanel();
        _panelMemoryLocalVariable = new javax.swing.JPanel();
        _panelMemoryFormula = new javax.swing.JPanel();

        _tabbedPaneMemory.addTab(NamedBeanAddressing.Direct.toString(), _panelMemoryDirect);
        _tabbedPaneMemory.addTab(NamedBeanAddressing.Reference.toString(), _panelMemoryReference);
        _tabbedPaneMemory.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelMemoryLocalVariable);
        _tabbedPaneMemory.addTab(NamedBeanAddressing.Formula.toString(), _panelMemoryFormula);

        _memoryBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _panelMemoryDirect.add(_memoryBeanPanel);

        _memoryReferenceTextField = new JTextField();
        _memoryReferenceTextField.setColumns(30);
        _panelMemoryReference.add(_memoryReferenceTextField);

        _memoryLocalVariableTextField = new JTextField();
        _memoryLocalVariableTextField.setColumns(30);
        _panelMemoryLocalVariable.add(_memoryLocalVariableTextField);

        _memoryFormulaTextField = new JTextField();
        _memoryFormulaTextField.setColumns(30);
        _panelMemoryFormula.add(_memoryFormulaTextField);

        _tabbedPaneMemoryOperation = new JTabbedPane();

        _setToNull = new JPanel();
        _setToConstant = new JPanel();
        _copyMemory = new JPanel();
        _copyTableCell = new JPanel();
        _copyTableCell.setLayout(new java.awt.GridBagLayout());
        _copyVariable = new JPanel();
        _calculateFormula = new JPanel();

        _tabbedPaneMemoryOperation.addTab(MemoryOperation.SetToNull.toString(), _setToNull);
        _tabbedPaneMemoryOperation.addTab(MemoryOperation.SetToString.toString(), _setToConstant);
        _tabbedPaneMemoryOperation.addTab(MemoryOperation.CopyMemoryToMemory.toString(), _copyMemory);
        _tabbedPaneMemoryOperation.addTab(MemoryOperation.CopyTableCellToMemory.toString(), _copyTableCell);
        _tabbedPaneMemoryOperation.addTab(MemoryOperation.CopyVariableToMemory.toString(), _copyVariable);
        _tabbedPaneMemoryOperation.addTab(MemoryOperation.CalculateFormula.toString(), _calculateFormula);

        _setToNull.add(new JLabel("Null"));     // No I18N

        _setToConstantTextField = new JTextField(30);
        _setToConstant.add(_setToConstantTextField);

        _copyMemoryBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _copyMemory.add(_copyMemoryBeanPanel);

        _copyLocalVariableTextField = new JTextField(30);
        _copyVariable.add(_copyLocalVariableTextField);

//        _copyTableCellTextField = new JTextField(30);
//        _copyTableCell.add(_copyTableCellTextField);


        _copyTableBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(NamedTableManager.class), null);

        _tableNameLabel = new JLabel();
        _rowNameLabel = new JLabel();
        _columnNameLabel = new JLabel();

        _tableNameAddressing = NamedBeanAddressing.Direct;
        _copyTableBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(NamedTableManager.class), null);
        _copyTableBeanPanel.getBeanCombo().addActionListener((evt) -> {
            setupRowOrColumnNameComboBox(action);
        });

        _tableRowNameComboBox = new JComboBox<>();
        _tableRowNameTextField = new JTextField(30);
        _tableColumnNameComboBox = new JComboBox<>();
        _tableColumnNameTextField = new JTextField(30);

        _editTableNameButton = new JButton(Bundle.getMessage("ActionMemory_Edit"));     // NOI18N
        _editRowNameButton = new JButton(Bundle.getMessage("ActionMemory_Edit"));       // NOI18N
        _editColumnNameButton = new JButton(Bundle.getMessage("ActionMemory_Edit"));    // NOI18N

        _tableNameReferenceTextField = new JTextField(30);
        _tableNameLocalVariableTextField = new JTextField(30);
        _tableNameFormulaTextField = new JTextField(30);

        _tableRowReferenceTextField = new JTextField(30);
        _tableRowLocalVariableTextField = new JTextField(30);
        _tableRowFormulaTextField = new JTextField(30);

        _tableColumnReferenceTextField = new JTextField(30);
        _tableColumnLocalVariableTextField = new JTextField(30);
        _tableColumnFormulaTextField = new JTextField(30);

        _editTableNameButton.addActionListener((evt) -> {
            _logixNG_DataDialog.showDialog(
                    _tableNameAddressing,
                    _copyTableBeanPanel,
                    _tableNameReferenceTextField,
                    _tableNameLocalVariableTextField,
                    _tableNameFormulaTextField,
                    this::editTableNameFinished);
        });
        _editRowNameButton.addActionListener((evt) -> {
            if (_tableNameAddressing == NamedBeanAddressing.Direct) {
                _logixNG_DataDialog.showDialog(
                        _tableRowAddressing,
                        _tableRowNameComboBox,
                        _tableRowReferenceTextField,
                        _tableRowLocalVariableTextField,
                        _tableRowFormulaTextField,
                        this::editTableRowFinished);
            } else {
                _logixNG_DataDialog.showDialog(
                        _tableRowAddressing,
                        _tableRowNameTextField,
                        _tableRowReferenceTextField,
                        _tableRowLocalVariableTextField,
                        _tableRowFormulaTextField,
                        this::editTableRowFinished);
            }
        });
        _editColumnNameButton.addActionListener((evt) -> {
            if (_tableNameAddressing == NamedBeanAddressing.Direct) {
                _logixNG_DataDialog.showDialog(
                        _tableColumnAddressing,
                        _tableColumnNameComboBox,
                        _tableColumnReferenceTextField,
                        _tableColumnLocalVariableTextField,
                        _tableColumnFormulaTextField,
                        this::editTableColumnFinished);
            } else {
                _logixNG_DataDialog.showDialog(
                        _tableColumnAddressing,
                        _tableColumnNameTextField,
                        _tableColumnReferenceTextField,
                        _tableColumnLocalVariableTextField,
                        _tableColumnFormulaTextField,
                        this::editTableColumnFinished);
            }
        });

        java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.EAST;
        _copyTableCell.add(_editTableNameButton, constraints);
        constraints.gridy = 1;
        _copyTableCell.add(_editRowNameButton, constraints);
        constraints.gridy = 2;
        _copyTableCell.add(_editColumnNameButton, constraints);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.WEST;
        _copyTableCell.add(_tableNameLabel, constraints);
        constraints.gridy = 1;
        _copyTableCell.add(_rowNameLabel, constraints);
        constraints.gridy = 2;
        _copyTableCell.add(_columnNameLabel, constraints);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.WEST;
        _copyTableCell.add(new JLabel("  "), constraints);
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.WEST;
        _copyTableCell.add(_editTableNameButton, constraints);
        constraints.gridy = 1;
        _copyTableCell.add(_editRowNameButton, constraints);
        constraints.gridy = 2;
        _copyTableCell.add(_editColumnNameButton, constraints);

        _calculateFormulaTextField = new JTextField(30);
        _calculateFormula.add(_calculateFormulaTextField);


        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneMemory.setSelectedComponent(_panelMemoryDirect); break;
                case Reference: _tabbedPaneMemory.setSelectedComponent(_panelMemoryReference); break;
                case LocalVariable: _tabbedPaneMemory.setSelectedComponent(_panelMemoryLocalVariable); break;
                case Formula: _tabbedPaneMemory.setSelectedComponent(_panelMemoryFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getMemory() != null) {
                _memoryBeanPanel.setDefaultNamedBean(action.getMemory().getBean());
            }
            _memoryReferenceTextField.setText(action.getReference());
            _memoryLocalVariableTextField.setText(action.getLocalVariable());
            _memoryFormulaTextField.setText(action.getFormula());

            if (action.getOtherMemory() != null) {
                _copyMemoryBeanPanel.setDefaultNamedBean(action.getOtherMemory().getBean());
            }
            switch (action.getMemoryOperation()) {
                case SetToNull: _tabbedPaneMemoryOperation.setSelectedComponent(_setToNull); break;
                case SetToString: _tabbedPaneMemoryOperation.setSelectedComponent(_setToConstant); break;
                case CopyMemoryToMemory: _tabbedPaneMemoryOperation.setSelectedComponent(_copyMemory); break;
                case CopyTableCellToMemory: _tabbedPaneMemoryOperation.setSelectedComponent(_copyTableCell); break;
                case CopyVariableToMemory: _tabbedPaneMemoryOperation.setSelectedComponent(_copyVariable); break;
                case CalculateFormula: _tabbedPaneMemoryOperation.setSelectedComponent(_calculateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getMemoryOperation().name());
            }
            _setToConstantTextField.setText(action.getConstantValue());
//DANIEL            _copyTableCellTextField.setText(ActionMemory.convertTableReference(action.getOtherTableCell(), false));
            _copyLocalVariableTextField.setText(action.getOtherLocalVariable());
            _calculateFormulaTextField.setText(action.getOtherFormula());


            _tableNameAddressing = action.getTableNameAddressing();

            switch (_tableNameAddressing) {
                case Direct:
                    if (action.getTable() != null) {
                        _copyTableBeanPanel.setDefaultNamedBean(action.getTable().getBean());
                    }
                    break;
                case Reference: _tableNameReferenceTextField.setText(action.getTableNameReference()); break;
                case LocalVariable: _tableNameLocalVariableTextField.setText(action.getTableNameLocalVariable()); break;
                case Formula: _tableNameFormulaTextField.setText(action.getTableNameFormula()); break;
                default: throw new IllegalArgumentException("invalid _tableNameAddressing: " + _tableNameAddressing.name());    // NOI18N
            }

            _tableRowAddressing = action.getTableRowAddressing();
            switch (_tableRowAddressing) {
                case Direct:
                    if (_tableNameAddressing == NamedBeanAddressing.Direct) {
                        _tableRowNameComboBox.setSelectedItem(action.getTableRowName());
                    } else {
                        _tableRowNameTextField.setText(action.getTableRowName());
                    }
                    break;
                case Reference: _tableRowReferenceTextField.setText(action.getTableRowReference()); break;
                case LocalVariable: _tableRowLocalVariableTextField.setText(action.getTableRowLocalVariable()); break;
                case Formula: _tableRowFormulaTextField.setText(action.getTableRowFormula()); break;
                default: throw new IllegalArgumentException("invalid _tableRowAddressing: " + _tableRowAddressing.name());  // NOI18N
            }

            _tableColumnAddressing = action.getTableColumnAddressing();
            switch (_tableColumnAddressing) {
                case Direct:
                    if (_tableNameAddressing == NamedBeanAddressing.Direct) {
                        _tableColumnNameComboBox.setSelectedItem(action.getTableColumnName());
                    } else {
                        _tableColumnNameTextField.setText(action.getTableColumnName());
                    }
                    break;
                case Reference: _tableColumnReferenceTextField.setText(action.getTableColumnReference()); break;
                case LocalVariable: _tableColumnLocalVariableTextField.setText(action.getTableColumnLocalVariable()); break;
                case Formula: _tableColumnFormulaTextField.setText(action.getTableColumnFormula()); break;
                default: throw new IllegalArgumentException("invalid _tableColumnAddressing: " + _tableColumnAddressing.name());    // NOI18N
            }
        }

        // These lines must be after the _copyTableBeanPanel has set the default bean
        boolean enable =
                (_tableNameAddressing != NamedBeanAddressing.Direct)
                || (_copyTableBeanPanel.getNamedBean() != null);
        _editRowNameButton.setEnabled(enable);
        _editColumnNameButton.setEnabled(enable);

        _tableNameLabel.setText(getTableNameDescription());
        _rowNameLabel.setText(getTableRowDescription());
        _columnNameLabel.setText(getTableColumnDescription());

        JComponent[] components = new JComponent[]{
            _tabbedPaneMemory,
            _tabbedPaneMemoryOperation
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionMemory_Components"), components);

        for (JComponent c : componentList) panel.add(c);

        _focusListener = new WindowFocusListener(){
            @Override
            public void windowGainedFocus(WindowEvent e) {
                _logixNG_DataDialog.checkOpenDialog();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                // Do nothing
            }
        };
        getJDialog().addWindowFocusListener(_focusListener);
    }

    private String getTableNameDescription() {
        String namedBean;
        switch (_tableNameAddressing) {
            case Direct:
                String tableName;
                if (_copyTableBeanPanel.getNamedBean() != null) {
                    tableName = _copyTableBeanPanel.getNamedBean().getDisplayName();
                } else {
                    tableName = Bundle.getMessage("BeanNotSelected");   // NOI18N
                }
                namedBean = Bundle.getMessage("AddressByDirect", tableName);    // NOI18N
                break;

            case Reference:
                namedBean = Bundle.getMessage("AddressByReference", _tableNameReferenceTextField.getText());    // NOI18N
                break;

            case LocalVariable:
                namedBean = Bundle.getMessage("AddressByLocalVariable", _tableNameLocalVariableTextField.getText());    // NOI18N
                break;

            case Formula:
                namedBean = Bundle.getMessage("AddressByFormula", _tableNameFormulaTextField.getText());    // NOI18N
                break;

            default:
                throw new IllegalArgumentException("invalid _tableNameAddressing: " + _tableNameAddressing.name()); // NOI18N
        }
        return Bundle.getMessage("ActionMemory_Table", namedBean);  // NOI18N
    }

    private String getTableRowDescription() {
        String row;
        switch (_tableRowAddressing) {
            case Direct:
                String rowName =
                        _tableNameAddressing == NamedBeanAddressing.Direct
                        ? _tableRowNameComboBox.getItemAt(_tableRowNameComboBox.getSelectedIndex())
                        : _tableRowNameTextField.getText();
                row = Bundle.getMessage("AddressByDirect", rowName);   // NOI18N
                break;

            case Reference:
                row = Bundle.getMessage("AddressByReference", _tableRowReferenceTextField.getText());   // NOI18N
                break;

            case LocalVariable:
                row = Bundle.getMessage("AddressByLocalVariable", _tableRowLocalVariableTextField.getText());   // NOI18N
                break;

            case Formula:
                row = Bundle.getMessage("AddressByFormula", _tableRowFormulaTextField.getText());   // NOI18N
                break;

            default:
                throw new IllegalArgumentException("invalid _tableRowAddressing: " + _tableRowAddressing.name());   // NOI18N
        }
        return Bundle.getMessage("ActionMemory_RowName", row);  // NOI18N
    }

    private String getTableColumnDescription() {
        String column;
        switch (_tableColumnAddressing) {
            case Direct:
                String columnName =
                        _tableNameAddressing == NamedBeanAddressing.Direct
                        ? _tableColumnNameComboBox.getItemAt(_tableColumnNameComboBox.getSelectedIndex())
                        : _tableColumnNameTextField.getText();
                column = Bundle.getMessage("AddressByDirect", columnName); // NOI18N
                break;

            case Reference:
                column = Bundle.getMessage("AddressByReference", _tableColumnReferenceTextField.getText()); // NOI18N
                break;

            case LocalVariable:
                column = Bundle.getMessage("AddressByLocalVariable", _tableColumnLocalVariableTextField.getText()); // NOI18N
                break;

            case Formula:
                column = Bundle.getMessage("AddressByFormula", _tableColumnFormulaTextField.getText()); // NOI18N
                break;

            default:
                throw new IllegalArgumentException("invalid _tableRowAddressing: " + _tableColumnAddressing.name());   // NOI18N
        }
        return Bundle.getMessage("ActionMemory_ColumnName", column);    // NOI18N
    }

    private void editTableNameFinished() {
        boolean enable =
                (_tableNameAddressing != NamedBeanAddressing.Direct)
                || (_copyTableBeanPanel.getNamedBean() != null);
        _editRowNameButton.setEnabled(enable);
        _editColumnNameButton.setEnabled(enable);
        _tableNameAddressing = _logixNG_DataDialog.getAddressing();
        _tableNameLabel.setText(getTableNameDescription());
    }

    private void editTableRowFinished() {
        _tableRowAddressing = _logixNG_DataDialog.getAddressing();
        _rowNameLabel.setText(getTableRowDescription());
    }

    private void editTableColumnFinished() {
        _tableColumnAddressing = _logixNG_DataDialog.getAddressing();
        _columnNameLabel.setText(getTableColumnDescription());
    }

    private void setupRowOrColumnNameComboBox(ActionMemory action) {
        String rowName = action != null ? action.getTableRowName() : null;
        String columnName = action != null ? action.getTableColumnName() : null;

        _tableRowNameComboBox.removeAllItems();
        _tableColumnNameComboBox.removeAllItems();

        NamedTable table = _copyTableBeanPanel.getNamedBean();
        if (table != null) {
            for (int row=0; row <= table.numRows(); row++) {
                // If the header is null or empty, treat the row as a comment
                Object header = table.getCell(row, 0);
                if ((header != null) && (!header.toString().isEmpty())) {
                    _tableRowNameComboBox.addItem(header.toString());
                }
            }
            for (int column=0; column <= table.numColumns(); column++) {
                // If the header is null or empty, treat the row as a comment
                Object header = table.getCell(0, column);
                if ((header != null) && (!header.toString().isEmpty())) {
                    _tableColumnNameComboBox.addItem(header.toString());
                }
            }
            _tableRowNameComboBox.setSelectedItem(rowName);
            _tableColumnNameComboBox.setSelectedItem(columnName);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        ActionMemory action = new ActionMemory("IQDA1", null);

        validateMemorySection(errorMessages);
        validateDataSection(errorMessages);

        return errorMessages.isEmpty();
    }

    private void validateMemorySection(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionMemory action = new ActionMemory("IQDA1", null);

        // If using the Direct tab, validate the memory variable selection.
        if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryDirect) {
            if (_memoryBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionMemory_ErrorMemory"));
            }
        }

        // If using the Reference tab, validate the reference content via setReference.
        try {
            if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryReference) {
                action.setReference(_memoryReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
        }

        // Validate formula parsing via setFormula and tab selections.
        try {
            action.setFormula(_memoryFormulaTextField.getText());
            if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }
    }

    public void validateDataSection(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionMemory action = new ActionMemory("IQDA2", null);

        // If using the Memory tab, validate the memory variable selection.
        if (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyMemory) {
            if (_copyMemoryBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionMemory_CopyErrorMemory"));
            }
        }

        // Validate formula parsing via setFormula and tab selection.
        try {
            action.setOtherFormula(_calculateFormulaTextField.getText());
            if (_tabbedPaneMemoryOperation.getSelectedComponent() == _calculateFormula) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.CalculateFormula);
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }

        try {
            switch (_tableNameAddressing) {
                case Direct: action.setTable(_copyTableBeanPanel.getNamedBean()); break;
                case Reference: action.setTableNameReference(_tableNameReferenceTextField.getText()); break;
                case LocalVariable: action.setTableNameLocalVariable(_tableNameLocalVariableTextField.getText()); break;
                case Formula: action.setTableNameFormula(_tableNameFormulaTextField.getText()); break;
                default: throw new IllegalArgumentException("invalid _tableNameAddressing: " + _tableNameAddressing.name());
            }

            String rowName =
                    _tableNameAddressing == NamedBeanAddressing.Direct
                    ? _tableRowNameComboBox.getItemAt(_tableRowNameComboBox.getSelectedIndex())
                    : _tableRowNameTextField.getText();
            switch (_tableRowAddressing) {
                case Direct: action.setTableRowName(rowName); break;
                case Reference: action.setTableRowReference(_tableRowReferenceTextField.getText()); break;
                case LocalVariable: action.setTableRowLocalVariable(_tableRowLocalVariableTextField.getText()); break;
                case Formula: action.setTableRowFormula(_tableRowFormulaTextField.getText()); break;
                default: throw new IllegalArgumentException("invalid _tableRowAddressing: " + _tableRowAddressing.name());
            }

            String columnName =
                    _tableNameAddressing == NamedBeanAddressing.Direct
                    ? _tableColumnNameComboBox.getItemAt(_tableColumnNameComboBox.getSelectedIndex())
                    : _tableRowNameTextField.getText();
            switch (_tableColumnAddressing) {
                case Direct: action.setTableColumnName(columnName); break;
                case Reference: action.setTableColumnReference(_tableColumnReferenceTextField.getText()); break;
                case LocalVariable: action.setTableColumnLocalVariable(_tableColumnLocalVariableTextField.getText()); break;
                case Formula: action.setTableColumnFormula(_tableColumnFormulaTextField.getText()); break;
                default: throw new IllegalArgumentException("invalid _tableColumnAddressing: " + _tableColumnAddressing.name());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add("Invalid value: " + e.getMessage());
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionMemory action = new ActionMemory(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionMemory)) {
            throw new IllegalArgumentException("object must be an ActionMemory but is a: "+object.getClass().getName());
        }
        ActionMemory action = (ActionMemory)object;

        Memory memory = _memoryBeanPanel.getNamedBean();
        if (memory != null) {
            NamedBeanHandle<Memory> handle
                    = InstanceManager.getDefault(NamedBeanHandleManager.class)
                            .getNamedBeanHandle(memory.getDisplayName(), memory);
            action.setMemory(handle);
        } else {
            action.removeMemory();
        }

        if (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyMemory) {
            Memory otherMemory = _copyMemoryBeanPanel.getNamedBean();
            if (otherMemory != null) {
                NamedBeanHandle<Memory> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
                action.setOtherMemory(handle);
            } else {
                action.removeOtherMemory();
            }
        } else {
            action.removeOtherMemory();
        }

        try {
            if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_memoryReferenceTextField.getText());
            } else if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_memoryLocalVariableTextField.getText());
            } else if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_memoryFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneMemory has unknown selection");
            }

            if (_tabbedPaneMemoryOperation.getSelectedComponent() == _setToNull) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.SetToNull);
            } else if (_tabbedPaneMemoryOperation.getSelectedComponent() == _setToConstant) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.SetToString);
                action.setOtherConstantValue(_setToConstantTextField.getText());
            } else if (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyMemory) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.CopyMemoryToMemory);
            } else if (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyTableCell) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.CopyTableCellToMemory);
            } else if (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyVariable) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.CopyVariableToMemory);
                action.setOtherLocalVariable(_copyLocalVariableTextField.getText());
            } else if (_tabbedPaneMemoryOperation.getSelectedComponent() == _calculateFormula) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.CalculateFormula);
                action.setOtherFormula(_calculateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneMemoryOperation has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }


        try {
            action.setTableNameAddressing(_tableNameAddressing);
            switch (_tableNameAddressing) {
                case Direct:
                    NamedTable table = _copyTableBeanPanel.getNamedBean();
                    if (table != null) action.setTable(table);
                    else action.removeTable();
                    break;
                case Reference: action.setTableNameReference(_tableNameReferenceTextField.getText()); break;
                case LocalVariable: action.setTableNameLocalVariable(_tableNameLocalVariableTextField.getText()); break;
                case Formula: action.setTableNameFormula(_tableNameFormulaTextField.getText()); break;
                default: throw new IllegalArgumentException("invalid _tableNameAddressing: " + _tableNameAddressing.name());
            }

            action.setTableRowAddressing(_tableRowAddressing);
            String rowName =
                    _tableNameAddressing == NamedBeanAddressing.Direct
                    ? _tableRowNameComboBox.getItemAt(_tableRowNameComboBox.getSelectedIndex())
                    : _tableRowNameTextField.getText();
            switch (_tableRowAddressing) {
                case Direct: action.setTableRowName(rowName); break;
                case Reference: action.setTableRowReference(_tableRowReferenceTextField.getText()); break;
                case LocalVariable: action.setTableRowLocalVariable(_tableRowLocalVariableTextField.getText()); break;
                case Formula: action.setTableRowFormula(_tableRowFormulaTextField.getText()); break;
                default: throw new IllegalArgumentException("invalid _tableRowAddressing: " + _tableRowAddressing.name());
            }

            action.setTableColumnAddressing(_tableColumnAddressing);
            String columnName =
                    _tableNameAddressing == NamedBeanAddressing.Direct
                    ? _tableColumnNameComboBox.getItemAt(_tableColumnNameComboBox.getSelectedIndex())
                    : _tableRowNameTextField.getText();
            switch (_tableColumnAddressing) {
                case Direct: action.setTableColumnName(columnName); break;
                case Reference: action.setTableColumnReference(_tableColumnReferenceTextField.getText()); break;
                case LocalVariable: action.setTableColumnLocalVariable(_tableColumnLocalVariableTextField.getText()); break;
                case Formula: action.setTableColumnFormula(_tableColumnFormulaTextField.getText()); break;
                default: throw new IllegalArgumentException("invalid _tableColumnAddressing: " + _tableColumnAddressing.name());
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionMemory_Short");
    }

    /** {@inheritDoc} */
    @Override
    public boolean canClose() {
        if (_logixNG_DataDialog.checkOpenDialog()) {
            JOptionPane.showMessageDialog(getJDialog(),
                    Bundle.getMessage("Error_InEditMode"), // NOI18N
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    public void dispose() {
        _logixNG_DataDialog.dispose();
        getJDialog().removeWindowFocusListener(_focusListener);
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionMemorySwing.class);

}
