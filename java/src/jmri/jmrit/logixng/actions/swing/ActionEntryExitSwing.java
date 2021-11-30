package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionEntryExit;
import jmri.jmrit.logixng.actions.ActionEntryExit.Operation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an EntryExit object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionEntryExitSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneEntryExit;
    private BeanSelectPanel<DestinationPoints> entryExitBeanPanel;
    private JPanel _panelEntryExitDirect;
    private JPanel _panelEntryExitReference;
    private JPanel _panelEntryExitLocalVariable;
    private JPanel _panelEntryExitFormula;
    private JTextField _entryExitReferenceTextField;
    private JTextField _entryExitLocalVariableTextField;
    private JTextField _entryExitFormulaTextField;
    
    private JTabbedPane _tabbedPaneOperation;
    private JComboBox<Operation> _stateComboBox;
    private JPanel _panelOperationDirect;
    private JPanel _panelOperationReference;
    private JPanel _panelOperationLocalVariable;
    private JPanel _panelOperationFormula;
    private JTextField _entryExitLockReferenceTextField;
    private JTextField _entryExitLockLocalVariableTextField;
    private JTextField _entryExitLockFormulaTextField;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionEntryExit action = (ActionEntryExit)object;
        
        panel = new JPanel();
        
        _tabbedPaneEntryExit = new JTabbedPane();
        _panelEntryExitDirect = new javax.swing.JPanel();
        _panelEntryExitReference = new javax.swing.JPanel();
        _panelEntryExitLocalVariable = new javax.swing.JPanel();
        _panelEntryExitFormula = new javax.swing.JPanel();
        
        _tabbedPaneEntryExit.addTab(NamedBeanAddressing.Direct.toString(), _panelEntryExitDirect);
        _tabbedPaneEntryExit.addTab(NamedBeanAddressing.Reference.toString(), _panelEntryExitReference);
        _tabbedPaneEntryExit.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelEntryExitLocalVariable);
        _tabbedPaneEntryExit.addTab(NamedBeanAddressing.Formula.toString(), _panelEntryExitFormula);
        
        entryExitBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class), null);
        _panelEntryExitDirect.add(entryExitBeanPanel);
        
        _entryExitReferenceTextField = new JTextField();
        _entryExitReferenceTextField.setColumns(30);
        _panelEntryExitReference.add(_entryExitReferenceTextField);
        
        _entryExitLocalVariableTextField = new JTextField();
        _entryExitLocalVariableTextField.setColumns(30);
        _panelEntryExitLocalVariable.add(_entryExitLocalVariableTextField);
        
        _entryExitFormulaTextField = new JTextField();
        _entryExitFormulaTextField.setColumns(30);
        _panelEntryExitFormula.add(_entryExitFormulaTextField);
        
        
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
        
        _entryExitLockReferenceTextField = new JTextField();
        _entryExitLockReferenceTextField.setColumns(30);
        _panelOperationReference.add(_entryExitLockReferenceTextField);
        
        _entryExitLockLocalVariableTextField = new JTextField();
        _entryExitLockLocalVariableTextField.setColumns(30);
        _panelOperationLocalVariable.add(_entryExitLockLocalVariableTextField);
        
        _entryExitLockFormulaTextField = new JTextField();
        _entryExitLockFormulaTextField.setColumns(30);
        _panelOperationFormula.add(_entryExitLockFormulaTextField);
        
        
        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneEntryExit.setSelectedComponent(_panelEntryExitDirect); break;
                case Reference: _tabbedPaneEntryExit.setSelectedComponent(_panelEntryExitReference); break;
                case LocalVariable: _tabbedPaneEntryExit.setSelectedComponent(_panelEntryExitLocalVariable); break;
                case Formula: _tabbedPaneEntryExit.setSelectedComponent(_panelEntryExitFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getDestinationPoints() != null) {
                entryExitBeanPanel.setDefaultNamedBean(action.getDestinationPoints().getBean());
            }
            _entryExitReferenceTextField.setText(action.getReference());
            _entryExitLocalVariableTextField.setText(action.getLocalVariable());
            _entryExitFormulaTextField.setText(action.getFormula());
            
            switch (action.getOperationAddressing()) {
                case Direct: _tabbedPaneOperation.setSelectedComponent(_panelOperationDirect); break;
                case Reference: _tabbedPaneOperation.setSelectedComponent(_panelOperationReference); break;
                case LocalVariable: _tabbedPaneOperation.setSelectedComponent(_panelOperationLocalVariable); break;
                case Formula: _tabbedPaneOperation.setSelectedComponent(_panelOperationFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            _stateComboBox.setSelectedItem(action.getOperationDirect());
            _entryExitLockReferenceTextField.setText(action.getOperationReference());
            _entryExitLockLocalVariableTextField.setText(action.getOperationLocalVariable());
            _entryExitLockFormulaTextField.setText(action.getLockFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _tabbedPaneEntryExit,
            _tabbedPaneOperation};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionEntryExit_Components"), components);
        
        for (JComponent c : componentList) panel.add(c);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionEntryExit action = new ActionEntryExit("IQDA1", null);
        
        try {
            if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitReference) {
                action.setReference(_entryExitReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationReference(_entryExitLockReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            action.setFormula(_entryExitFormulaTextField.getText());
            if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitFormula) {
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
        ActionEntryExit action = new ActionEntryExit(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionEntryExit)) {
            throw new IllegalArgumentException("object must be an TriggerEntryExit but is a: "+object.getClass().getName());
        }
        ActionEntryExit action = (ActionEntryExit)object;
        if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitDirect) {
            DestinationPoints entryExit = entryExitBeanPanel.getNamedBean();
            if (entryExit != null) {
                NamedBeanHandle<DestinationPoints> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(entryExit.getDisplayName(), entryExit);
                action.setDestinationPoints(handle);
            } else {
                action.removeDestinationPoints();
            }
        } else {
            action.removeDestinationPoints();
        }
        try {
            if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_entryExitReferenceTextField.getText());
            } else if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_entryExitLocalVariableTextField.getText());
            } else if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_entryExitFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneEntryExit has unknown selection");
            }
            
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationDirect) {
                action.setOperationAddressing(NamedBeanAddressing.Direct);
                action.setOperationDirect(_stateComboBox.getItemAt(_stateComboBox.getSelectedIndex()));
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference(_entryExitLockReferenceTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationLocalVariable) {
                action.setOperationAddressing(NamedBeanAddressing.LocalVariable);
                action.setOperationLocalVariable(_entryExitLockLocalVariableTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationFormula) {
                action.setOperationAddressing(NamedBeanAddressing.Formula);
                action.setOperationFormula(_entryExitLockFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneEntryExit has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionEntryExit_Short");
    }
    
    @Override
    public void dispose() {
        if (entryExitBeanPanel != null) {
            entryExitBeanPanel.dispose();
        }
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionEntryExitSwing.class);
    
}
