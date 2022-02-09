package jmri.jmrit.logixng.expressions.swing;

import java.awt.event.*;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionMemory;
import jmri.jmrit.logixng.expressions.ExpressionMemory.CompareTo;
import jmri.jmrit.logixng.expressions.ExpressionMemory.MemoryOperation;
import jmri.jmrit.logixng.swing.LogixNG_DataDialog;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionMemory object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionMemorySwing extends AbstractDigitalExpressionSwing {

    private final LogixNG_DataDialog _logixNG_DataDialog = new LogixNG_DataDialog(this);
    private WindowFocusListener _focusListener;
    private BeanSelectPanel<Memory> _memoryBeanPanel;
    private JComboBox<MemoryOperation> _memoryOperationComboBox;
    private JCheckBox _caseInsensitiveCheckBox;

    private JTabbedPane _tabbedPane;

    private JTabbedPane _tabbedPaneCompareTo;
    private BeanSelectPanel<Memory> _compareToMemoryBeanPanel;
    private BeanSelectPanel<NamedTable> _compareToTableBeanPanel;
    private JPanel _compareToConstant;
    private JPanel _compareToMemory;
    private JPanel _compareToLocalVariable;
    private JPanel _compareToTable;
    private JPanel _compareToRegEx;
    private JTextField _compareToConstantTextField;
    private JTextField _compareToLocalVariableTextField;
    private JTextField _compareToRegExTextField;
    private JButton _editTableNameButton;
    private JButton _editRowNameButton;
    private JButton _editColumnNameButton;
    private JLabel _tableNameLabel;
    private JLabel _rowNameLabel;
    private JLabel _columnNameLabel;

    private NamedBeanAddressing _tableNameAddressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<NamedTable> _tableHandle;
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



    private void enableDisableCompareTo() {
        MemoryOperation mo = _memoryOperationComboBox.getItemAt(
                        _memoryOperationComboBox.getSelectedIndex());
        boolean enable = mo.hasExtraValue();
        _tabbedPaneCompareTo.setEnabled(enable);
        ((JPanel)_tabbedPaneCompareTo.getSelectedComponent())
                .getComponent(0).setEnabled(enable);

        boolean regEx = (mo == MemoryOperation.MatchRegex)
                || (mo == MemoryOperation.NotMatchRegex);
        _tabbedPane.setEnabledAt(0, !regEx);
        _tabbedPane.setEnabledAt(1, regEx);
        _tabbedPane.setSelectedIndex(regEx ? 1 : 0);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionMemory expression = (ExpressionMemory)object;

        panel = new JPanel();

        _memoryBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);

        JPanel operationAndCasePanel = new JPanel();
        operationAndCasePanel.setLayout(new BoxLayout(operationAndCasePanel, BoxLayout.Y_AXIS));

        _memoryOperationComboBox = new JComboBox<>();
        for (MemoryOperation e : MemoryOperation.values()) {
            _memoryOperationComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_memoryOperationComboBox);
        operationAndCasePanel.add(_memoryOperationComboBox);

        _memoryOperationComboBox.addActionListener((e) -> { enableDisableCompareTo(); });

        _caseInsensitiveCheckBox = new JCheckBox(Bundle.getMessage("ExpressionMemory_CaseInsensitive"));
        operationAndCasePanel.add(_caseInsensitiveCheckBox);

        _tabbedPane = new JTabbedPane();

        _tabbedPaneCompareTo = new JTabbedPane();
        _tabbedPane.addTab("", _tabbedPaneCompareTo);

        _compareToConstant = new JPanel();
        _compareToMemory = new JPanel();
        _compareToLocalVariable = new JPanel();
        _compareToTable = new JPanel();
        _compareToTable.setLayout(new java.awt.GridBagLayout());
        _compareToRegEx = new JPanel();

        _tabbedPaneCompareTo.addTab(CompareTo.Value.toString(), _compareToConstant);
        _tabbedPaneCompareTo.addTab(CompareTo.Memory.toString(), _compareToMemory);
        _tabbedPaneCompareTo.addTab(CompareTo.LocalVariable.toString(), _compareToLocalVariable);
        _tabbedPaneCompareTo.addTab(CompareTo.Table.toString(), _compareToTable);

        _tabbedPane.addTab(CompareTo.RegEx.toString(), _compareToRegEx);

        _compareToConstantTextField = new JTextField(30);
        _compareToConstant.add(_compareToConstantTextField);

        _compareToMemoryBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _compareToMemory.add(_compareToMemoryBeanPanel);

        _compareToLocalVariableTextField = new JTextField(30);
        _compareToLocalVariable.add(_compareToLocalVariableTextField);




        _tableNameLabel = new JLabel("Table MyTable");
        _rowNameLabel = new JLabel("Row by reference {IM1}");
        _columnNameLabel = new JLabel("Column by local variable myVar");

        _tableNameAddressing = NamedBeanAddressing.Direct;
        _compareToTableBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(NamedTableManager.class), null);
        _compareToTableBeanPanel.getBeanCombo().addActionListener((evt) -> {
            setupRowOrColumnNameComboBox(expression);
        });

        _tableRowNameComboBox = new JComboBox<>();
        _tableRowNameTextField = new JTextField();
        _tableColumnNameComboBox = new JComboBox<>();
        _tableColumnNameTextField = new JTextField();

        _editTableNameButton = new JButton(Bundle.getMessage("ExpressionMemory_Edit"));
        _editRowNameButton = new JButton(Bundle.getMessage("ExpressionMemory_Edit"));
        _editColumnNameButton = new JButton(Bundle.getMessage("ExpressionMemory_Edit"));

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
                    _compareToTableBeanPanel,
                    _tableNameReferenceTextField,
                    _tableNameLocalVariableTextField,
                    _tableNameFormulaTextField,
                    this::editTableNameFinished);
        });
        _editRowNameButton.addActionListener((evt) -> {
            if (_tableNameAddressing == NamedBeanAddressing.Direct) {
                _logixNG_DataDialog.showDialog(
                        _tableRowNameComboBox,
                        _tableRowReferenceTextField,
                        _tableRowLocalVariableTextField,
                        _tableRowFormulaTextField,
                        this::editTableRowFinished);
            } else {
                _logixNG_DataDialog.showDialog(
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
                        _tableColumnNameComboBox,
                        _tableColumnReferenceTextField,
                        _tableColumnLocalVariableTextField,
                        _tableColumnFormulaTextField,
                        this::editTableColumnFinished);
            } else {
                _logixNG_DataDialog.showDialog(
                        _tableColumnNameTextField,
                        _tableColumnReferenceTextField,
                        _tableColumnLocalVariableTextField,
                        _tableColumnFormulaTextField,
                        this::editTableColumnFinished);
            }
        });

//        _panelRowOrColumnLabel = new JLabel(Bundle.getMessage("TableForEachSwing_RowName"));

        java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.EAST;
        _compareToTable.add(_editTableNameButton, constraints);
//        _compareToTable.add(new JLabel(Bundle.getMessage("ExpressionMemory_Table")), constraints);
        constraints.gridy = 1;
        _compareToTable.add(_editRowNameButton, constraints);
//        _compareToTable.add(new JLabel(Bundle.getMessage("ExpressionMemory_RowName")), constraints);
        constraints.gridy = 2;
        _compareToTable.add(_editColumnNameButton, constraints);
//        _compareToTable.add(new JLabel(Bundle.getMessage("ExpressionMemory_ColumnName")), constraints);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.WEST;
//        _compareToTable.add(_compareToTableBeanPanel, constraints);
        _compareToTable.add(_tableNameLabel, constraints);
        constraints.gridy = 1;
//        _compareToTable.add(_rowNameComboBox, constraints);
        _compareToTable.add(_rowNameLabel, constraints);
        constraints.gridy = 2;
//        _compareToTable.add(_columnNameComboBox, constraints);
        _compareToTable.add(_columnNameLabel, constraints);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.WEST;
        _compareToTable.add(new JLabel("  "), constraints);
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.WEST;
//        _compareToTable.add(_compareToTableBeanPanel, constraints);
        _compareToTable.add(_editTableNameButton, constraints);
        constraints.gridy = 1;
//        _compareToTable.add(_rowNameComboBox, constraints);
        _compareToTable.add(_editRowNameButton, constraints);
        constraints.gridy = 2;
//        _compareToTable.add(_columnNameComboBox, constraints);
        _compareToTable.add(_editColumnNameButton, constraints);

        _compareToRegExTextField = new JTextField(30);
        _compareToRegEx.add(_compareToRegExTextField);


        if (expression != null) {
            if (expression.getMemory() != null) {
                _memoryBeanPanel.setDefaultNamedBean(expression.getMemory().getBean());
            }
            if (expression.getOtherMemory() != null) {
                _compareToMemoryBeanPanel.setDefaultNamedBean(expression.getOtherMemory().getBean());
            }
            _tableNameAddressing = expression.getTableNameAddressing();
            if (expression.getTable() != null) {
                _compareToTableBeanPanel.setDefaultNamedBean(expression.getTable().getBean());
            }
            switch (expression.getCompareTo()) {
                case RegEx:
                case Value: _tabbedPaneCompareTo.setSelectedComponent(_compareToConstant); break;
                case Memory: _tabbedPaneCompareTo.setSelectedComponent(_compareToMemory); break;
                case Table: _tabbedPaneCompareTo.setSelectedComponent(_compareToTable); break;
                case LocalVariable: _tabbedPaneCompareTo.setSelectedComponent(_compareToLocalVariable); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getCompareTo().name());
            }
            _memoryOperationComboBox.setSelectedItem(expression.getMemoryOperation());
            _caseInsensitiveCheckBox.setSelected(expression.getCaseInsensitive());
            _compareToConstantTextField.setText(expression.getConstantValue());
            _compareToLocalVariableTextField.setText(expression.getLocalVariable());
            _compareToRegExTextField.setText(expression.getRegEx());
            _tableRowNameComboBox.setSelectedItem(expression.getTableRowName());
            _tableColumnNameComboBox.setSelectedItem(expression.getTableColumnName());
        }

        // These lines must be after the _compareToTableBeanPanel has set the default bean
        boolean enable = _compareToTableBeanPanel.getNamedBean() != null;
        _editRowNameButton.setEnabled(enable);
        _editColumnNameButton.setEnabled(enable);

        JComponent[] components = new JComponent[]{
            _memoryBeanPanel,
            operationAndCasePanel,
            _tabbedPane
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionMemory_Components"), components);

        for (JComponent c : componentList) panel.add(c);

        enableDisableCompareTo();

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

    private void editTableNameFinished() {
        boolean enable = _compareToTableBeanPanel.getNamedBean() != null;
        _editRowNameButton.setEnabled(enable);
        _editColumnNameButton.setEnabled(enable);
    }

    private void editTableRowFinished() {

    }

    private void editTableColumnFinished() {

    }

    private void setupRowOrColumnNameComboBox(ExpressionMemory expression) {
        String rowName = expression != null ? expression.getTableRowName() : null;
        String columnName = expression != null ? expression.getTableColumnName() : null;

        _tableRowNameComboBox.removeAllItems();
        _tableColumnNameComboBox.removeAllItems();

        NamedTable table = _compareToTableBeanPanel.getNamedBean();
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
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExpressionMemory expression = new ExpressionMemory(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionMemory)) {
            throw new IllegalArgumentException("object must be an ExpressionMemory but is a: "+object.getClass().getName());
        }
        ExpressionMemory expression = (ExpressionMemory)object;
        Memory memory = _memoryBeanPanel.getNamedBean();
        if (memory != null) {
            NamedBeanHandle<Memory> handle
                    = InstanceManager.getDefault(NamedBeanHandleManager.class)
                            .getNamedBeanHandle(memory.getDisplayName(), memory);
            expression.setMemory(handle);
        } else {
            expression.removeMemory();
        }
        expression.setMemoryOperation(_memoryOperationComboBox.getItemAt(_memoryOperationComboBox.getSelectedIndex()));
        expression.setCaseInsensitive(_caseInsensitiveCheckBox.isSelected());


        if (!_compareToMemoryBeanPanel.isEmpty()
                && (_tabbedPane.getSelectedComponent() == _tabbedPaneCompareTo)
                && (_tabbedPaneCompareTo.getSelectedComponent() == _compareToMemory)) {
            Memory otherMemory = _compareToMemoryBeanPanel.getNamedBean();
            if (otherMemory != null) {
                NamedBeanHandle<Memory> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
                expression.setOtherMemory(handle);
            } else {
                expression.removeOtherMemory();
            }
        } else {
            expression.removeOtherMemory();
        }

        if (!_compareToTableBeanPanel.isEmpty()
                && (_tabbedPane.getSelectedComponent() == _tabbedPaneCompareTo)
                && (_tabbedPaneCompareTo.getSelectedComponent() == _compareToTable)) {
            NamedTable table = _compareToTableBeanPanel.getNamedBean();
            if (table != null) {
                NamedBeanHandle<NamedTable> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(table.getDisplayName(), table);
                expression.setTable(handle);
            } else {
                expression.removeTable();
            }
        } else {
            expression.removeTable();
        }
        if (_tableRowNameComboBox.getSelectedIndex() != -1) {
            expression.setTableRowName(_tableRowNameComboBox.getItemAt(_tableRowNameComboBox.getSelectedIndex()));
        } else {
            expression.setTableRowName("");
        }
        if (_tableColumnNameComboBox.getSelectedIndex() != -1) {
            expression.setTableColumnName(_tableColumnNameComboBox.getItemAt(_tableColumnNameComboBox.getSelectedIndex()));
        } else {
            expression.setTableColumnName("");
        }

        if (_tabbedPane.getSelectedComponent() == _tabbedPaneCompareTo) {
            if (_tabbedPaneCompareTo.getSelectedComponent() == _compareToConstant) {
                expression.setCompareTo(CompareTo.Value);
                expression.setConstantValue(_compareToConstantTextField.getText());
            } else if (_tabbedPaneCompareTo.getSelectedComponent() == _compareToMemory) {
                expression.setCompareTo(CompareTo.Memory);
            } else if (_tabbedPaneCompareTo.getSelectedComponent() == _compareToTable) {
                expression.setCompareTo(CompareTo.Table);
            } else if (_tabbedPaneCompareTo.getSelectedComponent() == _compareToLocalVariable) {
                expression.setCompareTo(CompareTo.LocalVariable);
                expression.setLocalVariable(_compareToLocalVariableTextField.getText());
//            } else if (_tabbedPaneCompareTo.getSelectedComponent() == _compareToRegEx) {
//                expression.setCompareTo(CompareTo.RegEx);
//                expression.setRegEx(_compareToRegExTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneLight has unknown selection");
            }
        } else {
            expression.setCompareTo(CompareTo.RegEx);
            expression.setRegEx(_compareToRegExTextField.getText());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Memory_Short");
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


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionMemorySwing.class);

}
