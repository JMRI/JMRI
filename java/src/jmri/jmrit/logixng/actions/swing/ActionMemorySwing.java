package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.ActionMemory;
import jmri.jmrit.logixng.actions.ActionMemory.MemoryOperation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectCreatePanel;

/**
 * Configures an ActionMemory object with a Swing JPanel.
 */
public class ActionMemorySwing extends AbstractDigitalActionSwing {

    private BeanSelectCreatePanel<Memory> _memoryBeanPanel;
    
    private JTabbedPane _tabbedPaneMemoryOperation;
    private BeanSelectCreatePanel<Memory> _copyMemoryBeanPanel;
    private JPanel _setToNull;
    private JPanel _setToConstant;
    private JPanel _copyMemory;
    private JPanel _copyVariable;
    private JPanel _calculateFormula;
    private JTextField _setToConstantTextField;
    private JTextField _copyLocalVariableTextField;
    private JTextField _calculateFormulaTextField;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionMemory action = (ActionMemory)object;
        
        panel = new JPanel();
        
        _memoryBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        
        _tabbedPaneMemoryOperation = new JTabbedPane();
        
        _setToNull = new JPanel();
        _setToConstant = new JPanel();
        _copyMemory = new JPanel();
        _copyVariable = new JPanel();
        _calculateFormula = new JPanel();
        
        _tabbedPaneMemoryOperation.addTab(MemoryOperation.SetToNull.toString(), _setToNull);
        _tabbedPaneMemoryOperation.addTab(MemoryOperation.SetToString.toString(), _setToConstant);
        _tabbedPaneMemoryOperation.addTab(MemoryOperation.CopyMemoryToMemory.toString(), _copyMemory);
        _tabbedPaneMemoryOperation.addTab(MemoryOperation.CopyVariableToMemory.toString(), _copyVariable);
        _tabbedPaneMemoryOperation.addTab(MemoryOperation.CalculateFormula.toString(), _calculateFormula);
        
        _setToNull.add(new JLabel("Null"));     // No I18N
        
        _setToConstantTextField = new JTextField(30);
        _setToConstant.add(_setToConstantTextField);
        
        _copyMemoryBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _copyMemory.add(_copyMemoryBeanPanel);
        
        _copyLocalVariableTextField = new JTextField(30);
        _copyVariable.add(_copyLocalVariableTextField);
        
        _calculateFormulaTextField = new JTextField(30);
        _calculateFormula.add(_calculateFormulaTextField);
        
        
        if (action != null) {
            if (action.getMemory() != null) {
                _memoryBeanPanel.setDefaultNamedBean(action.getMemory().getBean());
            }
            if (action.getOtherMemory() != null) {
                _copyMemoryBeanPanel.setDefaultNamedBean(action.getOtherMemory().getBean());
            }
            switch (action.getMemoryOperation()) {
                case SetToNull: _tabbedPaneMemoryOperation.setSelectedComponent(_setToNull); break;
                case SetToString: _tabbedPaneMemoryOperation.setSelectedComponent(_setToConstant); break;
                case CopyMemoryToMemory: _tabbedPaneMemoryOperation.setSelectedComponent(_copyMemory); break;
                case CopyVariableToMemory: _tabbedPaneMemoryOperation.setSelectedComponent(_copyVariable); break;
                case CalculateFormula: _tabbedPaneMemoryOperation.setSelectedComponent(_calculateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getMemoryOperation().name());
            }
            _setToConstantTextField.setText(action.getConstantValue());
            _copyLocalVariableTextField.setText(action.getLocalVariable());
            _calculateFormulaTextField.setText(action.getFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _memoryBeanPanel,
            _tabbedPaneMemoryOperation
        };
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionMemory_Components"), components);
        
        for (JComponent c : componentList) panel.add(c);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
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
        
        try {
            if (!_memoryBeanPanel.isEmpty()) {
                Memory memory = _memoryBeanPanel.getNamedBean();
                if (memory != null) {
                    NamedBeanHandle<Memory> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(memory.getDisplayName(), memory);
                    action.setMemory(handle);
                }
            }
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for memory", ex);     // No I18N
        }
        
        try {
            if (!_copyMemoryBeanPanel.isEmpty()
                    && (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyMemory)) {
                Memory otherMemory = _copyMemoryBeanPanel.getNamedBean();
                if (otherMemory != null) {
                    NamedBeanHandle<Memory> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
                    action.setOtherMemory(handle);
                }
            }
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for memory", ex);     // No I18N
        }
        
        try {
            if (_tabbedPaneMemoryOperation.getSelectedComponent() == _setToNull) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.SetToNull);
            } else if (_tabbedPaneMemoryOperation.getSelectedComponent() == _setToConstant) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.SetToString);
                action.setConstantValue(_setToConstantTextField.getText());
            } else if (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyMemory) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.CopyMemoryToMemory);
            } else if (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyVariable) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.CopyVariableToMemory);
                action.setLocalVariable(_copyLocalVariableTextField.getText());
            } else if (_tabbedPaneMemoryOperation.getSelectedComponent() == _calculateFormula) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.CalculateFormula);
                action.setFormula(_calculateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneMemoryOperation has unknown selection");
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
    
    @Override
    public void dispose() {
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionMemorySwing.class);
    
}
