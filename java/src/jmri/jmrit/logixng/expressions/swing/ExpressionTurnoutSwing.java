package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionTurnout;
import jmri.jmrit.logixng.expressions.ExpressionTurnout.TurnoutState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionTurnout object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionTurnoutSwing extends AbstractDigitalExpressionSwing {

    private JTabbedPane _tabbedPaneTurnout;
    private BeanSelectPanel<Turnout> turnoutBeanPanel;
    private JPanel _panelTurnoutDirect;
    private JPanel _panelTurnoutReference;
    private JPanel _panelTurnoutLocalVariable;
    private JPanel _panelTurnoutFormula;
    private JTextField _turnoutReferenceTextField;
    private JTextField _turnoutLocalVariableTextField;
    private JTextField _turnoutFormulaTextField;
    
    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;
    
    private JTabbedPane _tabbedPaneTurnoutState;
    private JComboBox<TurnoutState> _stateComboBox;
    private JPanel _panelTurnoutStateDirect;
    private JPanel _panelTurnoutStateReference;
    private JPanel _panelTurnoutStateLocalVariable;
    private JPanel _panelTurnoutStateFormula;
    private JTextField _turnoutStateReferenceTextField;
    private JTextField _turnoutStateLocalVariableTextField;
    private JTextField _turnoutStateFormulaTextField;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionTurnout expression = (ExpressionTurnout)object;
        
        panel = new JPanel();
        
        _tabbedPaneTurnout = new JTabbedPane();
        _panelTurnoutDirect = new javax.swing.JPanel();
        _panelTurnoutReference = new javax.swing.JPanel();
        _panelTurnoutLocalVariable = new javax.swing.JPanel();
        _panelTurnoutFormula = new javax.swing.JPanel();
        
        _tabbedPaneTurnout.addTab(NamedBeanAddressing.Direct.toString(), _panelTurnoutDirect);
        _tabbedPaneTurnout.addTab(NamedBeanAddressing.Reference.toString(), _panelTurnoutReference);
        _tabbedPaneTurnout.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelTurnoutLocalVariable);
        _tabbedPaneTurnout.addTab(NamedBeanAddressing.Formula.toString(), _panelTurnoutFormula);
        
        turnoutBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(TurnoutManager.class), null);
        _panelTurnoutDirect.add(turnoutBeanPanel);
        
        _turnoutReferenceTextField = new JTextField();
        _turnoutReferenceTextField.setColumns(30);
        _panelTurnoutReference.add(_turnoutReferenceTextField);
        
        _turnoutLocalVariableTextField = new JTextField();
        _turnoutLocalVariableTextField.setColumns(30);
        _panelTurnoutLocalVariable.add(_turnoutLocalVariableTextField);
        
        _turnoutFormulaTextField = new JTextField();
        _turnoutFormulaTextField.setColumns(30);
        _panelTurnoutFormula.add(_turnoutFormulaTextField);
        
        
        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_is_IsNot_ComboBox);
        
        
        _tabbedPaneTurnoutState = new JTabbedPane();
        _panelTurnoutStateDirect = new javax.swing.JPanel();
        _panelTurnoutStateReference = new javax.swing.JPanel();
        _panelTurnoutStateLocalVariable = new javax.swing.JPanel();
        _panelTurnoutStateFormula = new javax.swing.JPanel();
        
        _tabbedPaneTurnoutState.addTab(NamedBeanAddressing.Direct.toString(), _panelTurnoutStateDirect);
        _tabbedPaneTurnoutState.addTab(NamedBeanAddressing.Reference.toString(), _panelTurnoutStateReference);
        _tabbedPaneTurnoutState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelTurnoutStateLocalVariable);
        _tabbedPaneTurnoutState.addTab(NamedBeanAddressing.Formula.toString(), _panelTurnoutStateFormula);
        
        _stateComboBox = new JComboBox<>();
        for (TurnoutState e : TurnoutState.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);
        
        _panelTurnoutStateDirect.add(_stateComboBox);
        
        _turnoutStateReferenceTextField = new JTextField();
        _turnoutStateReferenceTextField.setColumns(30);
        _panelTurnoutStateReference.add(_turnoutStateReferenceTextField);
        
        _turnoutStateLocalVariableTextField = new JTextField();
        _turnoutStateLocalVariableTextField.setColumns(30);
        _panelTurnoutStateLocalVariable.add(_turnoutStateLocalVariableTextField);
        
        _turnoutStateFormulaTextField = new JTextField();
        _turnoutStateFormulaTextField.setColumns(30);
        _panelTurnoutStateFormula.add(_turnoutStateFormulaTextField);
        
        
        if (expression != null) {
            switch (expression.getAddressing()) {
                case Direct: _tabbedPaneTurnout.setSelectedComponent(_panelTurnoutDirect); break;
                case Reference: _tabbedPaneTurnout.setSelectedComponent(_panelTurnoutReference); break;
                case LocalVariable: _tabbedPaneTurnout.setSelectedComponent(_panelTurnoutLocalVariable); break;
                case Formula: _tabbedPaneTurnout.setSelectedComponent(_panelTurnoutFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAddressing().name());
            }
            if (expression.getTurnout() != null) {
                turnoutBeanPanel.setDefaultNamedBean(expression.getTurnout().getBean());
            }
            _turnoutReferenceTextField.setText(expression.getReference());
            _turnoutLocalVariableTextField.setText(expression.getLocalVariable());
            _turnoutFormulaTextField.setText(expression.getFormula());
            
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());
            
            switch (expression.getStateAddressing()) {
                case Direct: _tabbedPaneTurnoutState.setSelectedComponent(_panelTurnoutStateDirect); break;
                case Reference: _tabbedPaneTurnoutState.setSelectedComponent(_panelTurnoutStateReference); break;
                case LocalVariable: _tabbedPaneTurnoutState.setSelectedComponent(_panelTurnoutStateLocalVariable); break;
                case Formula: _tabbedPaneTurnoutState.setSelectedComponent(_panelTurnoutStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAddressing().name());
            }
            _stateComboBox.setSelectedItem(expression.getBeanState());
            _turnoutStateReferenceTextField.setText(expression.getStateReference());
            _turnoutStateLocalVariableTextField.setText(expression.getStateLocalVariable());
            _turnoutStateFormulaTextField.setText(expression.getStateFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _tabbedPaneTurnout,
            _is_IsNot_ComboBox,
            _tabbedPaneTurnoutState};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionTurnout_Components"), components);
        
        for (JComponent c : componentList) panel.add(c);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionTurnout expression = new ExpressionTurnout("IQDE1", null);
        
        try {
            if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutReference) {
                expression.setReference(_turnoutReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            if (_tabbedPaneTurnoutState.getSelectedComponent() == _panelTurnoutStateReference) {
                expression.setStateReference(_turnoutStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            expression.setFormula(_turnoutFormulaTextField.getText());
            if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutFormula) {
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
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExpressionTurnout expression = new ExpressionTurnout(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionTurnout)) {
            throw new IllegalArgumentException("object must be an ExpressionTurnout but is a: "+object.getClass().getName());
        }
        ExpressionTurnout expression = (ExpressionTurnout)object;
        if (!turnoutBeanPanel.isEmpty() && (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutDirect)) {
            Turnout turnout = turnoutBeanPanel.getNamedBean();
            if (turnout != null) {
                NamedBeanHandle<Turnout> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(turnout.getDisplayName(), turnout);
                expression.setTurnout(handle);
            }
        }
        try {
            if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
                expression.setReference(_turnoutReferenceTextField.getText());
            } else if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
                expression.setLocalVariable(_turnoutLocalVariableTextField.getText());
            } else if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutFormula) {
                expression.setAddressing(NamedBeanAddressing.Formula);
                expression.setFormula(_turnoutFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneTurnout has unknown selection");
            }
            
            expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());
            
            if (_tabbedPaneTurnoutState.getSelectedComponent() == _panelTurnoutStateDirect) {
                expression.setStateAddressing(NamedBeanAddressing.Direct);
                expression.setBeanState((TurnoutState)_stateComboBox.getSelectedItem());
            } else if (_tabbedPaneTurnoutState.getSelectedComponent() == _panelTurnoutStateReference) {
                expression.setStateAddressing(NamedBeanAddressing.Reference);
                expression.setStateReference(_turnoutStateReferenceTextField.getText());
            } else if (_tabbedPaneTurnoutState.getSelectedComponent() == _panelTurnoutStateLocalVariable) {
                expression.setStateAddressing(NamedBeanAddressing.LocalVariable);
                expression.setStateLocalVariable(_turnoutStateLocalVariableTextField.getText());
            } else if (_tabbedPaneTurnoutState.getSelectedComponent() == _panelTurnoutStateFormula) {
                expression.setStateAddressing(NamedBeanAddressing.Formula);
                expression.setStateFormula(_turnoutStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneTurnoutState has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Turnout_Short");
    }
    
    @Override
    public void dispose() {
        if (turnoutBeanPanel != null) {
            turnoutBeanPanel.dispose();
        }
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionTurnoutSwing.class);
    
}
