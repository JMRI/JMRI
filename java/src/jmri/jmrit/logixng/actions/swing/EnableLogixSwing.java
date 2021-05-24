package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Logix;
import jmri.LogixManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.EnableLogix;
import jmri.jmrit.logixng.actions.EnableLogix.Operation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an EnableLogix object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class EnableLogixSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneLogix;
    private BeanSelectPanel<Logix> logixBeanPanel;
    private JPanel _panelLogixDirect;
    private JPanel _panelLogixReference;
    private JPanel _panelLogixLocalVariable;
    private JPanel _panelLogixFormula;
    private JTextField _logixReferenceTextField;
    private JTextField _logixLocalVariableTextField;
    private JTextField _logixFormulaTextField;
    
    private JTabbedPane _tabbedPaneOperation;
    private JComboBox<Operation> _stateComboBox;
    private JPanel _panelOperationDirect;
    private JPanel _panelOperationReference;
    private JPanel _panelOperationLocalVariable;
    private JPanel _panelOperationFormula;
    private JTextField _logixLockReferenceTextField;
    private JTextField _logixLockLocalVariableTextField;
    private JTextField _logixLockFormulaTextField;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        EnableLogix action = (EnableLogix)object;
        
        panel = new JPanel();
        
        _tabbedPaneLogix = new JTabbedPane();
        _panelLogixDirect = new javax.swing.JPanel();
        _panelLogixReference = new javax.swing.JPanel();
        _panelLogixLocalVariable = new javax.swing.JPanel();
        _panelLogixFormula = new javax.swing.JPanel();
        
        _tabbedPaneLogix.addTab(NamedBeanAddressing.Direct.toString(), _panelLogixDirect);
        _tabbedPaneLogix.addTab(NamedBeanAddressing.Reference.toString(), _panelLogixReference);
        _tabbedPaneLogix.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLogixLocalVariable);
        _tabbedPaneLogix.addTab(NamedBeanAddressing.Formula.toString(), _panelLogixFormula);
        
        logixBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(LogixManager.class), null);
        _panelLogixDirect.add(logixBeanPanel);
        
        _logixReferenceTextField = new JTextField();
        _logixReferenceTextField.setColumns(30);
        _panelLogixReference.add(_logixReferenceTextField);
        
        _logixLocalVariableTextField = new JTextField();
        _logixLocalVariableTextField.setColumns(30);
        _panelLogixLocalVariable.add(_logixLocalVariableTextField);
        
        _logixFormulaTextField = new JTextField();
        _logixFormulaTextField.setColumns(30);
        _panelLogixFormula.add(_logixFormulaTextField);
        
        
        _tabbedPaneOperation = new JTabbedPane();
        _panelOperationDirect = new javax.swing.JPanel();
        _panelOperationReference = new javax.swing.JPanel();
        _panelOperationLocalVariable = new javax.swing.JPanel();
        _panelOperationFormula = new javax.swing.JPanel();
        
        _tabbedPaneOperation.addTab(NamedBeanAddressing.Direct.toString(), _panelOperationDirect);
        _tabbedPaneOperation.addTab(NamedBeanAddressing.Reference.toString(), _panelOperationReference);
        _tabbedPaneOperation.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelOperationLocalVariable);
        _tabbedPaneOperation.addTab(NamedBeanAddressing.Formula.toString(), _panelOperationFormula);
        
        _stateComboBox = new JComboBox<>();
        for (Operation e : Operation.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);
        
        _panelOperationDirect.add(_stateComboBox);
        
        _logixLockReferenceTextField = new JTextField();
        _logixLockReferenceTextField.setColumns(30);
        _panelOperationReference.add(_logixLockReferenceTextField);
        
        _logixLockLocalVariableTextField = new JTextField();
        _logixLockLocalVariableTextField.setColumns(30);
        _panelOperationLocalVariable.add(_logixLockLocalVariableTextField);
        
        _logixLockFormulaTextField = new JTextField();
        _logixLockFormulaTextField.setColumns(30);
        _panelOperationFormula.add(_logixLockFormulaTextField);
        
        
        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneLogix.setSelectedComponent(_panelLogixDirect); break;
                case Reference: _tabbedPaneLogix.setSelectedComponent(_panelLogixReference); break;
                case LocalVariable: _tabbedPaneLogix.setSelectedComponent(_panelLogixLocalVariable); break;
                case Formula: _tabbedPaneLogix.setSelectedComponent(_panelLogixFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getLogix() != null) {
                logixBeanPanel.setDefaultNamedBean(action.getLogix().getBean());
            }
            _logixReferenceTextField.setText(action.getReference());
            _logixLocalVariableTextField.setText(action.getLocalVariable());
            _logixFormulaTextField.setText(action.getFormula());
            
            switch (action.getOperationAddressing()) {
                case Direct: _tabbedPaneOperation.setSelectedComponent(_panelOperationDirect); break;
                case Reference: _tabbedPaneOperation.setSelectedComponent(_panelOperationReference); break;
                case LocalVariable: _tabbedPaneOperation.setSelectedComponent(_panelOperationLocalVariable); break;
                case Formula: _tabbedPaneOperation.setSelectedComponent(_panelOperationFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            _stateComboBox.setSelectedItem(action.getOperationDirect());
            _logixLockReferenceTextField.setText(action.getOperationReference());
            _logixLockLocalVariableTextField.setText(action.getOperationLocalVariable());
            _logixLockFormulaTextField.setText(action.getLockFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _tabbedPaneLogix,
            _tabbedPaneOperation};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("EnableLogix_Components"), components);
        
        for (JComponent c : componentList) panel.add(c);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        EnableLogix action = new EnableLogix("IQDA1", null);
        
        try {
            if (_tabbedPaneLogix.getSelectedComponent() == _panelLogixReference) {
                action.setReference(_logixReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationReference(_logixLockReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            action.setFormula(_logixFormulaTextField.getText());
            if (_tabbedPaneLogix.getSelectedComponent() == _panelLogixDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneLogix.getSelectedComponent() == _panelLogixReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneLogix.getSelectedComponent() == _panelLogixLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneLogix.getSelectedComponent() == _panelLogixFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        EnableLogix action = new EnableLogix(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof EnableLogix)) {
            throw new IllegalArgumentException("object must be an EnableLogix but is a: "+object.getClass().getName());
        }
        EnableLogix action = (EnableLogix)object;
        if (_tabbedPaneLogix.getSelectedComponent() == _panelLogixDirect) {
            Logix logix = logixBeanPanel.getNamedBean();
            if (logix != null) {
                NamedBeanHandle<Logix> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(logix.getDisplayName(), logix);
                action.setLogix(handle);
            } else {
                action.removeLogix();
            }
        } else {
            action.removeLogix();
        }
        try {
            if (_tabbedPaneLogix.getSelectedComponent() == _panelLogixDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneLogix.getSelectedComponent() == _panelLogixReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_logixReferenceTextField.getText());
            } else if (_tabbedPaneLogix.getSelectedComponent() == _panelLogixLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_logixLocalVariableTextField.getText());
            } else if (_tabbedPaneLogix.getSelectedComponent() == _panelLogixFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_logixFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneLogix has unknown selection");
            }
            
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationDirect) {
                action.setOperationAddressing(NamedBeanAddressing.Direct);
                action.setOperationDirect(_stateComboBox.getItemAt(_stateComboBox.getSelectedIndex()));
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference(_logixLockReferenceTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationLocalVariable) {
                action.setOperationAddressing(NamedBeanAddressing.LocalVariable);
                action.setOperationLocalVariable(_logixLockLocalVariableTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationFormula) {
                action.setOperationAddressing(NamedBeanAddressing.Formula);
                action.setOperationFormula(_logixLockFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneLogix has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("EnableLogix_Short");
    }
    
    @Override
    public void dispose() {
        if (logixBeanPanel != null) {
            logixBeanPanel.dispose();
        }
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EnableLogixSwing.class);
    
}
