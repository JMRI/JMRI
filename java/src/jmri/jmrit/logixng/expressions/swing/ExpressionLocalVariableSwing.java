package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionLocalVariable;
import jmri.jmrit.logixng.expressions.ExpressionLocalVariable.CompareTo;
import jmri.jmrit.logixng.expressions.ExpressionLocalVariable.VariableOperation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionLocalVariable object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionLocalVariableSwing extends AbstractDigitalExpressionSwing {

    private JTextField _localVariableTextField;
    private JComboBox<VariableOperation> _variableOperationComboBox;
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
//    private JLabel _panelRowOrColumnLabel;
    private JComboBox<TableRowOrColumn> _tableRowOrColumnComboBox;
    private JComboBox<String> _rowOrColumnNameComboBox;
    
    
    private void enableDisableCompareTo() {
        VariableOperation vo = _variableOperationComboBox.getItemAt(
                        _variableOperationComboBox.getSelectedIndex());
        boolean enable = vo.hasExtraValue();
        _tabbedPaneCompareTo.setEnabled(enable);
        ((JPanel)_tabbedPaneCompareTo.getSelectedComponent())
                .getComponent(0).setEnabled(enable);
        
        boolean regEx = (vo == VariableOperation.MatchRegex)
                || (vo == VariableOperation.NotMatchRegex);
        _tabbedPane.setEnabledAt(0, !regEx);
        _tabbedPane.setEnabledAt(1, regEx);
        _tabbedPane.setSelectedIndex(regEx ? 1 : 0);
    }
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionLocalVariable expression = (ExpressionLocalVariable)object;
        
        panel = new JPanel();
        
        _localVariableTextField = new JTextField(30);
        
        JPanel operationAndCasePanel = new JPanel();
        operationAndCasePanel.setLayout(new BoxLayout(operationAndCasePanel, BoxLayout.Y_AXIS));
        
        _variableOperationComboBox = new JComboBox<>();
        for (VariableOperation e : VariableOperation.values()) {
            _variableOperationComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_variableOperationComboBox);
        operationAndCasePanel.add(_variableOperationComboBox);
        
        _variableOperationComboBox.addActionListener((e) -> { enableDisableCompareTo(); });
        
        _caseInsensitiveCheckBox = new JCheckBox(Bundle.getMessage("ExpressionLocalVariable_CaseInsensitive"));
        operationAndCasePanel.add(_caseInsensitiveCheckBox);
        
        _tabbedPane = new JTabbedPane();
        
        _tabbedPaneCompareTo = new JTabbedPane();
        _tabbedPane.addTab("", _tabbedPaneCompareTo);
        
        _compareToConstant = new JPanel();
        _compareToMemory = new JPanel();
        _compareToLocalVariable = new JPanel();
        _compareToTable = new JPanel();
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
        
        _compareToTableBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(NamedTableManager.class), null);
        
//        _panelRowOrColumnLabel = new JLabel(Bundle.getMessage("TableForEachSwing_RowName"));
        
        _tableRowOrColumnComboBox = new JComboBox<>();
        for (TableRowOrColumn item : TableRowOrColumn.values()) {
            _tableRowOrColumnComboBox.addItem(item);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_tableRowOrColumnComboBox);
        _tableRowOrColumnComboBox.addActionListener((evt) -> {
            setupRowOrColumnNameComboBox(expression != null ? expression.getRowOrColumnName() : null);
            
//            if (_tableRowOrColumnComboBox.getItemAt(_tableRowOrColumnComboBox.getSelectedIndex()) == TableRowOrColumn.Row) {
//                _panelRowOrColumnLabel.setText(Bundle.getMessage("TableForEachSwing_RowName"));
//            } else {
//                _panelRowOrColumnLabel.setText(Bundle.getMessage("TableForEachSwing_ColumnName"));
//            }
        });
        _rowOrColumnNameComboBox = new JComboBox<>();
        _compareToTable.add(_compareToTableBeanPanel);
        _compareToTable.add(_tableRowOrColumnComboBox);
        _compareToTable.add(_rowOrColumnNameComboBox);
        
        _compareToRegExTextField = new JTextField(30);
        _compareToRegEx.add(_compareToRegExTextField);
        
        
        if (expression != null) {
            if (expression.getLocalVariable() != null) {
                _localVariableTextField.setText(expression.getLocalVariable());
            }
            if (expression.getMemory() != null) {
                _compareToMemoryBeanPanel.setDefaultNamedBean(expression.getMemory().getBean());
            }
            switch (expression.getCompareTo()) {
                case RegEx:
                case Value: _tabbedPaneCompareTo.setSelectedComponent(_compareToConstant); break;
                case Memory: _tabbedPaneCompareTo.setSelectedComponent(_compareToMemory); break;
                case LocalVariable: _tabbedPaneCompareTo.setSelectedComponent(_compareToLocalVariable); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getCompareTo().name());
            }
            _variableOperationComboBox.setSelectedItem(expression.getVariableOperation());
            _caseInsensitiveCheckBox.setSelected(expression.getCaseInsensitive());
            _compareToConstantTextField.setText(expression.getConstantValue());
            _compareToLocalVariableTextField.setText(expression.getOtherLocalVariable());
            _compareToRegExTextField.setText(expression.getRegEx());
        }
        
        JComponent[] components = new JComponent[]{
            _localVariableTextField,
            operationAndCasePanel,
            _tabbedPane
        };
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionLocalVariable_Components"), components);
        
        for (JComponent c : componentList) panel.add(c);
        
        enableDisableCompareTo();
    }
    
    private void setupRowOrColumnNameComboBox(String rowOrColumnName) {
        _rowOrColumnNameComboBox.removeAllItems();
        NamedTable table = _compareToTableBeanPanel.getNamedBean();
        if (table != null) {
            if (_tableRowOrColumnComboBox.getItemAt(_tableRowOrColumnComboBox.getSelectedIndex()) == TableRowOrColumn.Column) {
                for (int column=0; column <= table.numColumns(); column++) {
                    // If the header is null or empty, treat the row as a comment
                    Object header = table.getCell(0, column);
                    if ((header != null) && (!header.toString().isEmpty())) {
                        _rowOrColumnNameComboBox.addItem(header.toString());
                    }
                }
            } else {
                for (int row=0; row <= table.numRows(); row++) {
                    // If the header is null or empty, treat the row as a comment
                    Object header = table.getCell(row, 0);
                    if ((header != null) && (!header.toString().isEmpty())) {
                        _rowOrColumnNameComboBox.addItem(header.toString());
                    }
                }
            }
            _rowOrColumnNameComboBox.setSelectedItem(rowOrColumnName);
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
        ExpressionLocalVariable expression = new ExpressionLocalVariable(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionLocalVariable)) {
            throw new IllegalArgumentException("object must be an ExpressionLocalVariable but is a: "+object.getClass().getName());
        }
        ExpressionLocalVariable expression = (ExpressionLocalVariable)object;
        
        expression.setLocalVariable(_localVariableTextField.getText());
        expression.setVariableOperation(_variableOperationComboBox.getItemAt(_variableOperationComboBox.getSelectedIndex()));
        expression.setCaseInsensitive(_caseInsensitiveCheckBox.isSelected());
        
        
        if (!_compareToMemoryBeanPanel.isEmpty()
                && (_tabbedPane.getSelectedComponent() == _tabbedPaneCompareTo)
                && (_tabbedPaneCompareTo.getSelectedComponent() == _compareToMemory)) {
            Memory otherMemory = _compareToMemoryBeanPanel.getNamedBean();
            if (otherMemory != null) {
                NamedBeanHandle<Memory> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
                expression.setMemory(handle);
            } else {
                expression.removeMemory();
            }
        } else {
            expression.removeMemory();
        }
        
        if (_tabbedPane.getSelectedComponent() == _tabbedPaneCompareTo) {
            if (_tabbedPaneCompareTo.getSelectedComponent() == _compareToConstant) {
                expression.setCompareTo(CompareTo.Value);
                expression.setConstantValue(_compareToConstantTextField.getText());
            } else if (_tabbedPaneCompareTo.getSelectedComponent() == _compareToMemory) {
                expression.setCompareTo(CompareTo.Memory);
            } else if (_tabbedPaneCompareTo.getSelectedComponent() == _compareToLocalVariable) {
                expression.setCompareTo(CompareTo.LocalVariable);
                expression.setOtherLocalVariable(_compareToLocalVariableTextField.getText());
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
        return Bundle.getMessage("LocalVariable_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionLocalVariableSwing.class);
    
}
