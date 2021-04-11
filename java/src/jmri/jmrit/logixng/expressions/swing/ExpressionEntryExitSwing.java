package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.entryexit.EntryExitPairs;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionEntryExit;
import jmri.jmrit.logixng.expressions.ExpressionEntryExit.EntryExitState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionEntryExit object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionEntryExitSwing extends AbstractDigitalExpressionSwing {

    private JTabbedPane _tabbedPaneEntryExit;
    private BeanSelectPanel<DestinationPoints> destinationPointsBeanPanel;
    private JPanel _panelEntryExitDirect;
    private JPanel _panelEntryExitReference;
    private JPanel _panelEntryExitLocalVariable;
    private JPanel _panelEntryExitFormula;
    private JTextField _entryExitReferenceTextField;
    private JTextField _entryExitLocalVariableTextField;
    private JTextField _entryExitFormulaTextField;
    
    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;
    
    private JTabbedPane _tabbedPaneEntryExitState;
    private JComboBox<EntryExitState> _stateComboBox;
    private JPanel _panelEntryExitStateDirect;
    private JPanel _panelEntryExitStateReference;
    private JPanel _panelEntryExitStateLocalVariable;
    private JPanel _panelEntryExitStateFormula;
    private JTextField _entryExitStateReferenceTextField;
    private JTextField _entryExitStateLocalVariableTextField;
    private JTextField _entryExitStateFormulaTextField;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionEntryExit expression = (ExpressionEntryExit)object;
        
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
        
        destinationPointsBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(EntryExitPairs.class), null);
        _panelEntryExitDirect.add(destinationPointsBeanPanel);
        
        _entryExitReferenceTextField = new JTextField();
        _entryExitReferenceTextField.setColumns(30);
        _panelEntryExitReference.add(_entryExitReferenceTextField);
        
        _entryExitLocalVariableTextField = new JTextField();
        _entryExitLocalVariableTextField.setColumns(30);
        _panelEntryExitLocalVariable.add(_entryExitLocalVariableTextField);
        
        _entryExitFormulaTextField = new JTextField();
        _entryExitFormulaTextField.setColumns(30);
        _panelEntryExitFormula.add(_entryExitFormulaTextField);
        
        
        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_is_IsNot_ComboBox);
        
        
        _tabbedPaneEntryExitState = new JTabbedPane();
        _panelEntryExitStateDirect = new javax.swing.JPanel();
        _panelEntryExitStateReference = new javax.swing.JPanel();
        _panelEntryExitStateLocalVariable = new javax.swing.JPanel();
        _panelEntryExitStateFormula = new javax.swing.JPanel();
        
        _tabbedPaneEntryExitState.addTab(NamedBeanAddressing.Direct.toString(), _panelEntryExitStateDirect);
        _tabbedPaneEntryExitState.addTab(NamedBeanAddressing.Reference.toString(), _panelEntryExitStateReference);
        _tabbedPaneEntryExitState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelEntryExitStateLocalVariable);
        _tabbedPaneEntryExitState.addTab(NamedBeanAddressing.Formula.toString(), _panelEntryExitStateFormula);
        
        _stateComboBox = new JComboBox<>();
        for (EntryExitState e : EntryExitState.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);
        
        _panelEntryExitStateDirect.add(_stateComboBox);
        
        _entryExitStateReferenceTextField = new JTextField();
        _entryExitStateReferenceTextField.setColumns(30);
        _panelEntryExitStateReference.add(_entryExitStateReferenceTextField);
        
        _entryExitStateLocalVariableTextField = new JTextField();
        _entryExitStateLocalVariableTextField.setColumns(30);
        _panelEntryExitStateLocalVariable.add(_entryExitStateLocalVariableTextField);
        
        _entryExitStateFormulaTextField = new JTextField();
        _entryExitStateFormulaTextField.setColumns(30);
        _panelEntryExitStateFormula.add(_entryExitStateFormulaTextField);
        
        
        if (expression != null) {
            switch (expression.getAddressing()) {
                case Direct: _tabbedPaneEntryExit.setSelectedComponent(_panelEntryExitDirect); break;
                case Reference: _tabbedPaneEntryExit.setSelectedComponent(_panelEntryExitReference); break;
                case LocalVariable: _tabbedPaneEntryExit.setSelectedComponent(_panelEntryExitLocalVariable); break;
                case Formula: _tabbedPaneEntryExit.setSelectedComponent(_panelEntryExitFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAddressing().name());
            }
            if (expression.getEntryExit() != null) {
                destinationPointsBeanPanel.setDefaultNamedBean(expression.getEntryExit().getBean());
            }
            _entryExitReferenceTextField.setText(expression.getReference());
            _entryExitLocalVariableTextField.setText(expression.getLocalVariable());
            _entryExitFormulaTextField.setText(expression.getFormula());
            
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());
            
            switch (expression.getStateAddressing()) {
                case Direct: _tabbedPaneEntryExitState.setSelectedComponent(_panelEntryExitStateDirect); break;
                case Reference: _tabbedPaneEntryExitState.setSelectedComponent(_panelEntryExitStateReference); break;
                case LocalVariable: _tabbedPaneEntryExitState.setSelectedComponent(_panelEntryExitStateLocalVariable); break;
                case Formula: _tabbedPaneEntryExitState.setSelectedComponent(_panelEntryExitStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAddressing().name());
            }
            _stateComboBox.setSelectedItem(expression.getBeanState());
            _entryExitStateReferenceTextField.setText(expression.getStateReference());
            _entryExitStateLocalVariableTextField.setText(expression.getStateLocalVariable());
            _entryExitStateFormulaTextField.setText(expression.getStateFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _tabbedPaneEntryExit,
            _is_IsNot_ComboBox,
            _tabbedPaneEntryExitState};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionEntryExit_Components"), components);
        
        for (JComponent c : componentList) panel.add(c);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionEntryExit expression = new ExpressionEntryExit("IQDE1", null);
        
        try {
            if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitReference) {
                expression.setReference(_entryExitReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            if (_tabbedPaneEntryExitState.getSelectedComponent() == _panelEntryExitStateReference) {
                expression.setStateReference(_entryExitStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            expression.setFormula(_entryExitFormulaTextField.getText());
            if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitFormula) {
                expression.setAddressing(NamedBeanAddressing.Formula);
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
    public String getAutoSystemName() {
        return InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName();
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExpressionEntryExit expression = new ExpressionEntryExit(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionEntryExit)) {
            throw new IllegalArgumentException("object must be an ExpressionEntryExit but is a: "+object.getClass().getName());
        }
        ExpressionEntryExit expression = (ExpressionEntryExit)object;
        if (!destinationPointsBeanPanel.isEmpty() && (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitDirect)) {
            DestinationPoints entryExit = destinationPointsBeanPanel.getNamedBean();
            if (entryExit != null) {
                NamedBeanHandle<DestinationPoints> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(entryExit.getDisplayName(), entryExit);
                expression.setDestinationPoints(handle);
            }
        }
        try {
            if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
                expression.setReference(_entryExitReferenceTextField.getText());
            } else if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
                expression.setLocalVariable(_entryExitLocalVariableTextField.getText());
            } else if (_tabbedPaneEntryExit.getSelectedComponent() == _panelEntryExitFormula) {
                expression.setAddressing(NamedBeanAddressing.Formula);
                expression.setFormula(_entryExitFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneEntryExit has unknown selection");
            }
            
            expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());
            
            if (_tabbedPaneEntryExitState.getSelectedComponent() == _panelEntryExitStateDirect) {
                expression.setStateAddressing(NamedBeanAddressing.Direct);
                expression.setBeanState((EntryExitState)_stateComboBox.getSelectedItem());
            } else if (_tabbedPaneEntryExitState.getSelectedComponent() == _panelEntryExitStateReference) {
                expression.setStateAddressing(NamedBeanAddressing.Reference);
                expression.setStateReference(_entryExitStateReferenceTextField.getText());
            } else if (_tabbedPaneEntryExitState.getSelectedComponent() == _panelEntryExitStateLocalVariable) {
                expression.setStateAddressing(NamedBeanAddressing.LocalVariable);
                expression.setStateLocalVariable(_entryExitStateLocalVariableTextField.getText());
            } else if (_tabbedPaneEntryExitState.getSelectedComponent() == _panelEntryExitStateFormula) {
                expression.setStateAddressing(NamedBeanAddressing.Formula);
                expression.setStateFormula(_entryExitStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneEntryExitState has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("EntryExit_Short");
    }
    
    @Override
    public void dispose() {
        if (destinationPointsBeanPanel != null) {
            destinationPointsBeanPanel.dispose();
        }
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionEntryExitSwing.class);
    
}
