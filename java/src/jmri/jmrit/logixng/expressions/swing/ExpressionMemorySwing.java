package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionMemory;
import jmri.jmrit.logixng.expressions.ExpressionMemory.CompareTo;
import jmri.jmrit.logixng.expressions.ExpressionMemory.MemoryOperation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.util.swing.BeanSelectCreatePanel;

/**
 * Configures an ExpressionMemory object with a Swing JPanel.
 */
public class ExpressionMemorySwing extends AbstractDigitalExpressionSwing {

    private BeanSelectCreatePanel<Memory> _memoryBeanPanel;
    private JComboBox<MemoryOperation> _memoryOperationComboBox;
    
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
        
        _memoryBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        
        _memoryOperationComboBox = new JComboBox<>();
        for (MemoryOperation e : MemoryOperation.values()) {
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
                _memoryBeanPanel.setDefaultNamedBean(expression.getMemory().getBean());
            }
//            _compareToComboBox.setSelectedItem(expression.getCompareTo());
            _memoryOperationComboBox.setSelectedItem(expression.getMemoryOperation());
/*            
            switch (expression.getCompareTo()) {
                case Value: _tabbedPaneCompareTo.setSelectedComponent(_compareToConstant); break;
                case Memory: _tabbedPaneCompareTo.setSelectedComponent(_compareToMemory); break;
                case LocalVariable: _tabbedPaneCompareTo.setSelectedComponent(_compareToLocalVariable); break;
                case RegEx: break;      // This case is handled by enableDisableCompareTo();
//                case RegEx: _tabbedPaneCompareTo.setSelectedComponent(_panelLightFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getCompareTo().name());
            }
*/            
            if (expression.getMemory() != null) {
                _memoryBeanPanel.setDefaultNamedBean(expression.getMemory().getBean());
            }
            _compareToConstantTextField.setText(expression.getConstantValue());
            _tabbedPaneCompareTo.setSelectedComponent(_compareToMemory);
            _compareToConstantTextField.setText(expression.getConstantValue());
            _compareToRegExTextField.setText(expression.getRegEx());
        }
        
        JComponent[] components = new JComponent[]{
            _memoryBeanPanel,
            _memoryOperationComboBox,
            _tabbedPane
//            _tabbedPaneCompareTo
        };
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionMemory_Components"), components);
        
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
        try {
            if (!_memoryBeanPanel.isEmpty()) {
                Memory memory = _memoryBeanPanel.getNamedBean();
                if (memory != null) {
                    NamedBeanHandle<Memory> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(memory.getDisplayName(), memory);
                    expression.setMemory(handle);
                }
            }
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for memory", ex);
        }
//        expression.setCompareTo(_compareToComboBox.getItemAt(_compareToComboBox.getSelectedIndex()));
        expression.setMemoryOperation(_memoryOperationComboBox.getItemAt(_memoryOperationComboBox.getSelectedIndex()));
        
        
        try {
            if (!_compareToMemoryBeanPanel.isEmpty()
                    && (_tabbedPane.getSelectedComponent() == _tabbedPaneBeanPanel)
                    && (_tabbedPaneCompareTo.getSelectedComponent() == _compareToMemory)) {
                Memory otherMemory = _compareToMemoryBeanPanel.getNamedBean();
                if (otherMemory != null) {
                    NamedBeanHandle<Memory> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
                    expression.setOtherMemory(handle);
                }
            }
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for memory", ex);
        }
        
        expression.setConstantValue(_compareToConstantTextField.getText());
        expression.setOtherLocalVaiable(_compareToLocalVariableTextField.getText());
        expression.setRegEx(_compareToRegExTextField.getText());
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Memory_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionMemorySwing.class);
    
}
