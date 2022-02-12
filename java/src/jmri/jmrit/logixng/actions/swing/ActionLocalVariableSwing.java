package jmri.jmrit.logixng.actions.swing;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionLocalVariable;
import jmri.jmrit.logixng.actions.ActionLocalVariable.VariableOperation;
import jmri.jmrit.logixng.swing.LogixNG_DataDialog;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;

/**
 * Configures an ActionLocalVariable object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionLocalVariableSwing extends AbstractDigitalActionSwing {

    private final LogixNG_DataDialog _logixNG_DataDialog = new LogixNG_DataDialog(this);
    private WindowFocusListener _focusListener;

    private JTextField _localVariableTextField;

    private JTabbedPane _tabbedPaneVariableOperation;
    private BeanSelectPanel<Memory> _copyMemoryBeanPanel;
    private BeanSelectPanel<NamedTable> _copyTableBeanPanel;
    private JCheckBox _listenOnMemory;
    private BeanSelectPanel<Block> _copyBlockBeanPanel;
    private JCheckBox _listenOnBlock;
    private BeanSelectPanel<Reporter> _copyReporterBeanPanel;
    private JCheckBox _listenOnReporter;
    private JPanel _setToNull;
    private JPanel _setToConstant;
    private JPanel _copyMemory;
    private JPanel _copyBlock;
    private JPanel _copyReporter;
    private JPanel _copyVariable;
    private JPanel _calculateFormula;
    private JPanel _copyTableCell;
    private JTextField _setToConstantTextField;
    private JTextField _copyLocalVariableTextField;
    private JTextField _calculateFormulaTextField;
//    private JTextField _copyTableCellTextField;
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
        ActionLocalVariable action = (ActionLocalVariable)object;

        panel = new JPanel();

        _localVariableTextField = new JTextField(20);

        _tabbedPaneVariableOperation = new JTabbedPane();

        _setToNull = new JPanel();
        _setToConstant = new JPanel();
        _copyMemory = new JPanel();
        _copyBlock = new JPanel();
        _copyReporter = new JPanel();
        _copyTableCell = new JPanel();
        _copyTableCell.setLayout(new java.awt.GridBagLayout());
        _copyVariable = new JPanel();
        _calculateFormula = new JPanel();

        _tabbedPaneVariableOperation.addTab(VariableOperation.SetToNull.toString(), _setToNull);
        _tabbedPaneVariableOperation.addTab(VariableOperation.SetToString.toString(), _setToConstant);
        _tabbedPaneVariableOperation.addTab(VariableOperation.CopyMemoryToVariable.toString(), _copyMemory);
        _tabbedPaneVariableOperation.addTab(VariableOperation.CopyBlockToVariable.toString(), _copyBlock);
        _tabbedPaneVariableOperation.addTab(VariableOperation.CopyReporterToVariable.toString(), _copyReporter);
        _tabbedPaneVariableOperation.addTab(VariableOperation.CopyVariableToVariable.toString(), _copyVariable);
        _tabbedPaneVariableOperation.addTab(VariableOperation.CopyTableCellToVariable.toString(), _copyTableCell);
        _tabbedPaneVariableOperation.addTab(VariableOperation.CalculateFormula.toString(), _calculateFormula);

        _setToNull.add(new JLabel("Null"));     // No I18N

        _setToConstantTextField = new JTextField(30);
        _setToConstant.add(_setToConstantTextField);

        _copyMemoryBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _listenOnMemory = new JCheckBox(Bundle.getMessage("ActionLocalVariable_ListenOnMemory"));
        _copyMemory.add(_copyMemoryBeanPanel);
        _copyMemory.add(_listenOnMemory);

        _copyBlockBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(BlockManager.class), null);
        _listenOnBlock = new JCheckBox(Bundle.getMessage("ActionLocalVariable_ListenOnBlock"));
        _copyBlock.add(_copyBlockBeanPanel);
        _copyBlock.add(_listenOnBlock);

        _copyReporterBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(ReporterManager.class), null);
        _listenOnReporter = new JCheckBox(Bundle.getMessage("ActionLocalVariable_ListenOnReporter"));
        _copyReporter.add(_copyReporterBeanPanel);
        _copyReporter.add(_listenOnReporter);

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

        _editTableNameButton = new JButton(Bundle.getMessage("ActionLocalVariable_Edit"));     // NOI18N
        _editRowNameButton = new JButton(Bundle.getMessage("ActionLocalVariable_Edit"));       // NOI18N
        _editColumnNameButton = new JButton(Bundle.getMessage("ActionLocalVariable_Edit"));    // NOI18N

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

        _copyLocalVariableTextField = new JTextField(30);
        _copyVariable.add(_copyLocalVariableTextField);

        _calculateFormulaTextField = new JTextField(30);
        _calculateFormula.add(_calculateFormulaTextField);


        if (action != null) {
            if (action.getLocalVariable() != null) {
                _localVariableTextField.setText(action.getLocalVariable());
            }
            if (action.getMemory() != null) {
                _copyMemoryBeanPanel.setDefaultNamedBean(action.getMemory().getBean());
            }
            if (action.getBlock() != null) {
                _copyBlockBeanPanel.setDefaultNamedBean(action.getBlock().getBean());
            }
            if (action.getReporter() != null) {
                _copyReporterBeanPanel.setDefaultNamedBean(action.getReporter().getBean());
            }
            switch (action.getVariableOperation()) {
                case SetToNull: _tabbedPaneVariableOperation.setSelectedComponent(_setToNull); break;
                case SetToString: _tabbedPaneVariableOperation.setSelectedComponent(_setToConstant); break;
                case CopyMemoryToVariable: _tabbedPaneVariableOperation.setSelectedComponent(_copyMemory); break;
                case CopyTableCellToVariable: _tabbedPaneVariableOperation.setSelectedComponent(_copyTableCell); break;
                case CopyBlockToVariable: _tabbedPaneVariableOperation.setSelectedComponent(_copyBlock); break;
                case CopyReporterToVariable: _tabbedPaneVariableOperation.setSelectedComponent(_copyReporter); break;
                case CopyVariableToVariable: _tabbedPaneVariableOperation.setSelectedComponent(_copyVariable); break;
                case CalculateFormula: _tabbedPaneVariableOperation.setSelectedComponent(_calculateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getVariableOperation().name());
            }
            _setToConstantTextField.setText(action.getConstantValue());
            _copyLocalVariableTextField.setText(action.getOtherLocalVariable());
            _calculateFormulaTextField.setText(action.getFormula());

            _listenOnMemory.setSelected(action.getListenToMemory());
            _listenOnBlock.setSelected(action.getListenToBlock());
            _listenOnReporter.setSelected(action.getListenToReporter());


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
            _localVariableTextField,
            _tabbedPaneVariableOperation
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionLocalVariable_Components"), components);

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
        return Bundle.getMessage("ActionLocalVariable_Table", namedBean);  // NOI18N
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
        return Bundle.getMessage("ActionLocalVariable_RowName", row);  // NOI18N
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
        return Bundle.getMessage("ActionLocalVariable_ColumnName", column);    // NOI18N
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

    private void setupRowOrColumnNameComboBox(ActionLocalVariable action) {
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
        ActionLocalVariable action = new ActionLocalVariable("IQDA1", null);

         // If using the Memory tab, validate the memory variable selection.
        if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyMemory) {
            if (_copyMemoryBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionLocalVariable_CopyErrorMemory"));
            }
        }

         // If using the Block tab, validate the block variable selection.
        if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyBlock) {
            if (_copyBlockBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionLocalVariable_CopyErrorBlock"));
            }
        }

         // If using the Reporter tab, validate the reporter variable selection.
        if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyReporter) {
            if (_copyReporterBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionLocalVariable_CopyErrorReporter"));
            }
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

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionLocalVariable action = new ActionLocalVariable(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionLocalVariable)) {
            throw new IllegalArgumentException("object must be an ActionLocalVariable but is a: "+object.getClass().getName());
        }
        ActionLocalVariable action = (ActionLocalVariable)object;

        action.setLocalVariable(_localVariableTextField.getText());


        if (!_copyMemoryBeanPanel.isEmpty()
                && (_tabbedPaneVariableOperation.getSelectedComponent() == _copyMemory)) {
            Memory memory = _copyMemoryBeanPanel.getNamedBean();
            if (memory != null) {
                NamedBeanHandle<Memory> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(memory.getDisplayName(), memory);
                action.setMemory(handle);
            }
        }

        if (!_copyBlockBeanPanel.isEmpty()
                && (_tabbedPaneVariableOperation.getSelectedComponent() == _copyBlock)) {
            Block block = _copyBlockBeanPanel.getNamedBean();
            if (block != null) {
                NamedBeanHandle<Block> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(block.getDisplayName(), block);
                action.setBlock(handle);
            }
        }

        if (!_copyReporterBeanPanel.isEmpty()
                && (_tabbedPaneVariableOperation.getSelectedComponent() == _copyReporter)) {
            Reporter reporter = _copyReporterBeanPanel.getNamedBean();
            if (reporter != null) {
                NamedBeanHandle<Reporter> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(reporter.getDisplayName(), reporter);
                action.setReporter(handle);
            }
        }

        try {
            if (_tabbedPaneVariableOperation.getSelectedComponent() == _setToNull) {
                action.setVariableOperation(VariableOperation.SetToNull);
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _setToConstant) {
                action.setVariableOperation(VariableOperation.SetToString);
                action.setConstantValue(_setToConstantTextField.getText());
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyMemory) {
                action.setVariableOperation(VariableOperation.CopyMemoryToVariable);
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyBlock) {
                action.setVariableOperation(VariableOperation.CopyBlockToVariable);
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyReporter) {
                action.setVariableOperation(VariableOperation.CopyReporterToVariable);
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyTableCell) {
                action.setVariableOperation(VariableOperation.CopyTableCellToVariable);
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyVariable) {
                action.setVariableOperation(VariableOperation.CopyVariableToVariable);
                action.setOtherLocalVariable(_copyLocalVariableTextField.getText());
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _calculateFormula) {
                action.setVariableOperation(VariableOperation.CalculateFormula);
                action.setFormula(_calculateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneVariableOperation has unknown selection");
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

        action.setListenToMemory(_listenOnMemory.isSelected());
        action.setListenToBlock(_listenOnBlock.isSelected());
        action.setListenToReporter(_listenOnReporter.isSelected());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionLocalVariable_Short");
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

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLocalVariableSwing.class);

}
