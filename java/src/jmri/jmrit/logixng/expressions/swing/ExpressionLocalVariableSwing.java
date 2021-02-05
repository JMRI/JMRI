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
import jmri.util.swing.BeanSelectCreatePanel;

/**
 * Configures an ExpressionLocalVariable object with a Swing JPanel.
 */
public class ExpressionLocalVariableSwing extends AbstractDigitalExpressionSwing {

    private JTextField _localVariableTextField;
    private JComboBox<VariableOperation> _memoryOperationComboBox;
    
    private JTabbedPane _tabbedPane;
    private JPanel _tabbedPaneBeanPanel;
    
    private JTabbedPane _tabbedPaneCompareTo;
    private BeanSelectCreatePanel<Memory> _compareToMemoryBeanPanel;
    private JPanel _compareToConstant;
    private JPanel _compareToMemory;
    private JPanel _compareToLocalVariable;
    private JPanel _compareToRegEx;
    private JTextField _compareToConstantTextField;
    private JTextField _compareToLocalVariableTextField;
    private JTextField _compareToRegExTextField;
    
    
    private void enableDisableCompareTo() {
        VariableOperation vo = _memoryOperationComboBox.getItemAt(
                        _memoryOperationComboBox.getSelectedIndex());
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
        
        _memoryOperationComboBox = new JComboBox<>();
        for (VariableOperation e : VariableOperation.values()) {
            _memoryOperationComboBox.addItem(e);
        }
        
        _memoryOperationComboBox.addActionListener((e) -> { enableDisableCompareTo(); });
        
        _tabbedPane = new JTabbedPane();
        _tabbedPaneBeanPanel = new JPanel();
        
        _tabbedPaneCompareTo = new JTabbedPane();
        _tabbedPane.addTab("", _tabbedPaneCompareTo);
        
        _compareToConstant = new JPanel();
        _compareToMemory = new JPanel();
        _compareToLocalVariable = new JPanel();
        _compareToRegEx = new JPanel();
        
        _tabbedPaneCompareTo.addTab(CompareTo.Value.toString(), _compareToConstant);
        _tabbedPaneCompareTo.addTab(CompareTo.Memory.toString(), _compareToMemory);
        _tabbedPaneCompareTo.addTab(CompareTo.LocalVariable.toString(), _compareToLocalVariable);
        
        _tabbedPane.addTab(CompareTo.RegEx.toString(), _compareToRegEx);
        
        _compareToConstantTextField = new JTextField(30);
        _compareToConstant.add(_compareToConstantTextField);
        
        _compareToMemoryBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _compareToMemory.add(_compareToMemoryBeanPanel);
        
        _compareToLocalVariableTextField = new JTextField(30);
        _compareToLocalVariable.add(_compareToLocalVariableTextField);
        
        _compareToRegExTextField = new JTextField(30);
        _compareToRegEx.add(_compareToRegExTextField);
        
        
        if (expression != null) {
            if (expression.getMemory() != null) {
                _localVariableTextField.setText(expression.getVariableName());
            }
            _memoryOperationComboBox.setSelectedItem(expression.getVariableOperation());
            _compareToConstantTextField.setText(expression.getConstantValue());
            _tabbedPaneCompareTo.setSelectedComponent(_compareToMemory);
            _compareToConstantTextField.setText(expression.getConstantValue());
            _compareToRegExTextField.setText(expression.getRegEx());
        }
        
        JComponent[] components = new JComponent[]{
            _localVariableTextField,
            _memoryOperationComboBox,
            _tabbedPane
        };
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionLocalVariable_Components"), components);
        
        for (JComponent c : componentList) panel.add(c);
        
        enableDisableCompareTo();
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
        
        expression.setVariable(_localVariableTextField.getText());
        expression.setVariableOperation(_memoryOperationComboBox.getItemAt(_memoryOperationComboBox.getSelectedIndex()));
        
        
        try {
            if (!_compareToMemoryBeanPanel.isEmpty()
                    && (_tabbedPane.getSelectedComponent() == _tabbedPaneBeanPanel)
                    && (_tabbedPaneCompareTo.getSelectedComponent() == _compareToMemory)) {
                Memory otherMemory = _compareToMemoryBeanPanel.getNamedBean();
                if (otherMemory != null) {
                    NamedBeanHandle<Memory> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
                    expression.setMemory(handle);
                }
            }
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for memory", ex);
        }
        
        expression.setConstantValue(_compareToConstantTextField.getText());
        expression.setOtherLocalVariable(_compareToLocalVariableTextField.getText());
        expression.setRegEx(_compareToRegExTextField.getText());
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Variable_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionLocalVariableSwing.class);
    
}
