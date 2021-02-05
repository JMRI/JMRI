package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionLocalVariable;
import jmri.jmrit.logixng.expressions.ExpressionLocalVariable.VariableOperation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;

/**
 * Configures an ExpressionLocalVariable object with a Swing JPanel.
 */
public class ExpressionLocalVariableSwing extends AbstractDigitalExpressionSwing {

    private JTextField _variableName;
    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;
    private JComboBox<VariableOperation> _variableOperationComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionLocalVariable expression = (ExpressionLocalVariable)object;
        
        panel = new JPanel();
/*        
        _tabbedPaneLocalVariable = new JTabbedPane();
        _panelLocalVariableDirect = new javax.swing.JPanel();
        _panelLocalVariableReference = new javax.swing.JPanel();
        _panelLocalVariableLocalVariable = new javax.swing.JPanel();
        _panelLocalVariableFormula = new javax.swing.JPanel();
        
        _tabbedPaneLocalVariable.addTab(NamedBeanAddressing.Direct.toString(), _panelLocalVariableDirect);
        _tabbedPaneLocalVariable.addTab(NamedBeanAddressing.Reference.toString(), _panelLocalVariableReference);
        _tabbedPaneLocalVariable.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLocalVariableLocalVariable);
        _tabbedPaneLocalVariable.addTab(NamedBeanAddressing.Formula.toString(), _panelLocalVariableFormula);
        
        _variableName = new JTextField(20);
        _panelLocalVariableDirect.add(_variableName);
        
        _variableReferenceTextField = new JTextField(30);
        _panelLocalVariableReference.add(_variableReferenceTextField);
        
        _variableLocalVariableTextField = new JTextField(30);
        _panelLocalVariableLocalVariable.add(_variableLocalVariableTextField);
        
        _variableFormulaTextField = new JTextField(30);
        _panelLocalVariableFormula.add(_variableFormulaTextField);
        
        
        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        
        
        _tabbedPaneVariableOperation = new JTabbedPane();
        _panelVariableOperationDirect = new javax.swing.JPanel();
        _panelVariableOperationReference = new javax.swing.JPanel();
        _panelVariableOperationLocalVariable = new javax.swing.JPanel();
        _panelVariableOperationFormula = new javax.swing.JPanel();
        
        _tabbedPaneVariableOperation.addTab(NamedBeanAddressing.Direct.toString(), _panelVariableOperationDirect);
        _tabbedPaneVariableOperation.addTab(NamedBeanAddressing.Reference.toString(), _panelVariableOperationReference);
        _tabbedPaneVariableOperation.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelVariableOperationLocalVariable);
        _tabbedPaneVariableOperation.addTab(NamedBeanAddressing.Formula.toString(), _panelVariableOperationFormula);
        
        _variableOperationComboBox = new JComboBox<>();
        for (VariableOperation e : VariableOperation.values()) {
            _variableOperationComboBox.addItem(e);
        }
        
        _panelVariableOperationDirect.add(_variableOperationComboBox);
        
        _variableStateReferenceTextField = new JTextField();
        _variableStateReferenceTextField.setColumns(30);
        _panelVariableOperationReference.add(_variableStateReferenceTextField);
        
        _variableStateLocalVariableTextField = new JTextField();
        _variableStateLocalVariableTextField.setColumns(30);
        _panelVariableOperationLocalVariable.add(_variableStateLocalVariableTextField);
        
        _variableStateFormulaTextField = new JTextField();
        _variableStateFormulaTextField.setColumns(30);
        _panelVariableOperationFormula.add(_variableStateFormulaTextField);
        
        
        if (expression != null) {
            switch (expression.getAddressing()) {
                case Direct: _tabbedPaneLocalVariable.setSelectedComponent(_panelLocalVariableDirect); break;
                case Reference: _tabbedPaneLocalVariable.setSelectedComponent(_panelLocalVariableReference); break;
                case LocalVariable: _tabbedPaneLocalVariable.setSelectedComponent(_panelLocalVariableLocalVariable); break;
                case Formula: _tabbedPaneLocalVariable.setSelectedComponent(_panelLocalVariableFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAddressing().name());
            }
            if (expression.getLocalVariable() != null) {
                _variableName.setDefaultNamedBean(expression.getLocalVariable().getBean());
            }
            _variableReferenceTextField.setText(expression.getReference());
            _variableLocalVariableTextField.setText(expression.getLocalVariable());
            _variableFormulaTextField.setText(expression.getFormula());
            
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());
            
            switch (expression.getStateAddressing()) {
                case Direct: _tabbedPaneVariableOperation.setSelectedComponent(_panelVariableOperationDirect); break;
                case Reference: _tabbedPaneVariableOperation.setSelectedComponent(_panelVariableOperationReference); break;
                case LocalVariable: _tabbedPaneVariableOperation.setSelectedComponent(_panelVariableOperationLocalVariable); break;
                case Formula: _tabbedPaneVariableOperation.setSelectedComponent(_panelVariableOperationFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAddressing().name());
            }
            _variableOperationComboBox.setSelectedItem(expression.getBeanState());
            _variableStateReferenceTextField.setText(expression.getStateReference());
            _variableStateLocalVariableTextField.setText(expression.getStateLocalVariable());
            _variableStateFormulaTextField.setText(expression.getStateFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _tabbedPaneLocalVariable,
            _is_IsNot_ComboBox,
            _tabbedPaneVariableOperation};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionLocalVariable_Components"), components);
        
        for (JComponent c : componentList) panel.add(c);
*/        
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionLocalVariable expression = new ExpressionLocalVariable("IQDE1", null);
/*        
        try {
            if (_tabbedPaneLocalVariable.getSelectedComponent() == _panelLocalVariableReference) {
                expression.setReference(_variableReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            if (_tabbedPaneVariableOperation.getSelectedComponent() == _panelVariableOperationReference) {
                expression.setStateReference(_variableStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            expression.setFormula(_variableFormulaTextField.getText());
            if (_tabbedPaneLocalVariable.getSelectedComponent() == _panelLocalVariableDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneLocalVariable.getSelectedComponent() == _panelLocalVariableReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneLocalVariable.getSelectedComponent() == _panelLocalVariableLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneLocalVariable.getSelectedComponent() == _panelLocalVariableFormula) {
                expression.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }
*/        
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
/*        
        if (! (object instanceof ExpressionLocalVariable)) {
            throw new IllegalArgumentException("object must be an ExpressionLocalVariable but is a: "+object.getClass().getName());
        }
        ExpressionLocalVariable expression = (ExpressionLocalVariable)object;
        try {
            if (!_variableName.isEmpty() && (_tabbedPaneLocalVariable.getSelectedComponent() == _panelLocalVariableDirect)) {
                LocalVariable variable = _variableName.getNamedBean();
                if (variable != null) {
                    NamedBeanHandle<LocalVariable> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(variable.getDisplayName(), variable);
                    expression.setLocalVariable(handle);
                }
            }
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for variable", ex);
        }
        try {
            if (_tabbedPaneLocalVariable.getSelectedComponent() == _panelLocalVariableDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneLocalVariable.getSelectedComponent() == _panelLocalVariableReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
                expression.setReference(_variableReferenceTextField.getText());
            } else if (_tabbedPaneLocalVariable.getSelectedComponent() == _panelLocalVariableLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
                expression.setLocalVariable(_variableLocalVariableTextField.getText());
            } else if (_tabbedPaneLocalVariable.getSelectedComponent() == _panelLocalVariableFormula) {
                expression.setAddressing(NamedBeanAddressing.Formula);
                expression.setFormula(_variableFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneLocalVariable has unknown selection");
            }
            
            expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());
            
            if (_tabbedPaneVariableOperation.getSelectedComponent() == _panelVariableOperationDirect) {
                expression.setStateAddressing(NamedBeanAddressing.Direct);
                expression.setBeanState((VariableOperation)_variableOperationComboBox.getSelectedItem());
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _panelVariableOperationReference) {
                expression.setStateAddressing(NamedBeanAddressing.Reference);
                expression.setStateReference(_variableStateReferenceTextField.getText());
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _panelVariableOperationLocalVariable) {
                expression.setStateAddressing(NamedBeanAddressing.LocalVariable);
                expression.setStateLocalVariable(_variableStateLocalVariableTextField.getText());
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _panelVariableOperationFormula) {
                expression.setStateAddressing(NamedBeanAddressing.Formula);
                expression.setStateFormula(_variableStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneVariableOperation has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
*/        
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Variable_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrueSwing.class);
    
}
