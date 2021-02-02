package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionConditional;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectCreatePanel;

/**
 * Configures an ExpressionTurnout object with a Swing JPanel.
 */
public class ExpressionConditionalSwing extends AbstractDigitalExpressionSwing {

    private JTabbedPane _tabbedPaneConditional;
    private BeanSelectCreatePanel<Conditional> oblockBeanPanel;
    private JPanel _panelConditionalDirect;
    private JPanel _panelConditionalReference;
    private JPanel _panelConditionalLocalVariable;
    private JPanel _panelConditionalFormula;
    private JTextField _oblockReferenceTextField;
    private JTextField _oblockLocalVariableTextField;
    private JTextField _oblockFormulaTextField;
    
    private JComboBox<Is_IsNot_Enum> is_IsNot_ComboBox;
    
    private JTabbedPane _tabbedPaneConditionalState;
    private JComboBox<ExpressionConditional.ConditionalState> _stateComboBox;
    private JPanel _panelConditionalStateDirect;
    private JPanel _panelConditionalStateReference;
    private JPanel _panelConditionalStateLocalVariable;
    private JPanel _panelConditionalStateFormula;
    private JTextField _oblockStateReferenceTextField;
    private JTextField _oblockStateLocalVariableTextField;
    private JTextField _oblockStateFormulaTextField;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionConditional expression = (ExpressionConditional)object;
        
        panel = new JPanel();
        
        _tabbedPaneConditional = new JTabbedPane();
        _panelConditionalDirect = new javax.swing.JPanel();
        _panelConditionalReference = new javax.swing.JPanel();
        _panelConditionalLocalVariable = new javax.swing.JPanel();
        _panelConditionalFormula = new javax.swing.JPanel();
        
        _tabbedPaneConditional.addTab(NamedBeanAddressing.Direct.toString(), _panelConditionalDirect);
        _tabbedPaneConditional.addTab(NamedBeanAddressing.Reference.toString(), _panelConditionalReference);
        _tabbedPaneConditional.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelConditionalLocalVariable);
        _tabbedPaneConditional.addTab(NamedBeanAddressing.Formula.toString(), _panelConditionalFormula);
        
        oblockBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(ConditionalManager.class), null);
        _panelConditionalDirect.add(oblockBeanPanel);
        
        _oblockReferenceTextField = new JTextField();
        _oblockReferenceTextField.setColumns(30);
        _panelConditionalReference.add(_oblockReferenceTextField);
        
        _oblockLocalVariableTextField = new JTextField();
        _oblockLocalVariableTextField.setColumns(30);
        _panelConditionalLocalVariable.add(_oblockLocalVariableTextField);
        
        _oblockFormulaTextField = new JTextField();
        _oblockFormulaTextField.setColumns(30);
        _panelConditionalFormula.add(_oblockFormulaTextField);
        
        
        is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            is_IsNot_ComboBox.addItem(e);
        }
        
        
        _tabbedPaneConditionalState = new JTabbedPane();
        _panelConditionalStateDirect = new javax.swing.JPanel();
        _panelConditionalStateReference = new javax.swing.JPanel();
        _panelConditionalStateLocalVariable = new javax.swing.JPanel();
        _panelConditionalStateFormula = new javax.swing.JPanel();
        
        _tabbedPaneConditionalState.addTab(NamedBeanAddressing.Direct.toString(), _panelConditionalStateDirect);
        _tabbedPaneConditionalState.addTab(NamedBeanAddressing.Reference.toString(), _panelConditionalStateReference);
        _tabbedPaneConditionalState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelConditionalStateLocalVariable);
        _tabbedPaneConditionalState.addTab(NamedBeanAddressing.Formula.toString(), _panelConditionalStateFormula);
        
        _stateComboBox = new JComboBox<>();
        for (ExpressionConditional.ConditionalState e : ExpressionConditional.ConditionalState.values()) {
            _stateComboBox.addItem(e);
        }
        
        _panelConditionalStateDirect.add(_stateComboBox);
        
        _oblockStateReferenceTextField = new JTextField();
        _oblockStateReferenceTextField.setColumns(30);
        _panelConditionalStateReference.add(_oblockStateReferenceTextField);
        
        _oblockStateLocalVariableTextField = new JTextField();
        _oblockStateLocalVariableTextField.setColumns(30);
        _panelConditionalStateLocalVariable.add(_oblockStateLocalVariableTextField);
        
        _oblockStateFormulaTextField = new JTextField();
        _oblockStateFormulaTextField.setColumns(30);
        _panelConditionalStateFormula.add(_oblockStateFormulaTextField);
        
        
        if (expression != null) {
            switch (expression.getAddressing()) {
                case Direct: _tabbedPaneConditional.setSelectedComponent(_panelConditionalDirect); break;
                case Reference: _tabbedPaneConditional.setSelectedComponent(_panelConditionalReference); break;
                case LocalVariable: _tabbedPaneConditional.setSelectedComponent(_panelConditionalLocalVariable); break;
                case Formula: _tabbedPaneConditional.setSelectedComponent(_panelConditionalFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAddressing().name());
            }
            if (expression.getConditional() != null) {
                oblockBeanPanel.setDefaultNamedBean(expression.getConditional().getBean());
            }
            _oblockReferenceTextField.setText(expression.getReference());
            _oblockLocalVariableTextField.setText(expression.getLocalVariable());
            _oblockFormulaTextField.setText(expression.getFormula());
            
            is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());
            
            switch (expression.getStateAddressing()) {
                case Direct: _tabbedPaneConditionalState.setSelectedComponent(_panelConditionalStateDirect); break;
                case Reference: _tabbedPaneConditionalState.setSelectedComponent(_panelConditionalStateReference); break;
                case LocalVariable: _tabbedPaneConditionalState.setSelectedComponent(_panelConditionalStateLocalVariable); break;
                case Formula: _tabbedPaneConditionalState.setSelectedComponent(_panelConditionalStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAddressing().name());
            }
            _stateComboBox.setSelectedItem(expression.getConditionalState());
            _oblockStateReferenceTextField.setText(expression.getStateReference());
            _oblockStateLocalVariableTextField.setText(expression.getStateLocalVariable());
            _oblockStateFormulaTextField.setText(expression.getStateFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _tabbedPaneConditional,
            is_IsNot_ComboBox,
            _tabbedPaneConditionalState};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionConditional_Components"), components);
        
        for (JComponent c : componentList) panel.add(c);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionConditional expression = new ExpressionConditional("IQDE1", null);
        
        try {
            if (_tabbedPaneConditional.getSelectedComponent() == _panelConditionalReference) {
                expression.setReference(_oblockReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            if (_tabbedPaneConditionalState.getSelectedComponent() == _panelConditionalStateReference) {
                expression.setStateReference(_oblockStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            expression.setFormula(_oblockFormulaTextField.getText());
            if (_tabbedPaneConditional.getSelectedComponent() == _panelConditionalDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneConditional.getSelectedComponent() == _panelConditionalReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneConditional.getSelectedComponent() == _panelConditionalLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneConditional.getSelectedComponent() == _panelConditionalFormula) {
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
        ExpressionConditional expression = new ExpressionConditional(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionConditional)) {
            throw new IllegalArgumentException("object must be an ExpressionConditional but is a: "+object.getClass().getName());
        }
        ExpressionConditional expression = (ExpressionConditional)object;
        try {
            if (!oblockBeanPanel.isEmpty() && (_tabbedPaneConditional.getSelectedComponent() == _panelConditionalDirect)) {
                Conditional oblock = oblockBeanPanel.getNamedBean();
                if (oblock != null) {
                    NamedBeanHandle<Conditional> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(oblock.getDisplayName(), oblock);
                    expression.setConditional(handle);
                }
            }
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for oblock", ex);
        }
        try {
            if (_tabbedPaneConditional.getSelectedComponent() == _panelConditionalDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneConditional.getSelectedComponent() == _panelConditionalReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
                expression.setReference(_oblockReferenceTextField.getText());
            } else if (_tabbedPaneConditional.getSelectedComponent() == _panelConditionalLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
                expression.setLocalVariable(_oblockLocalVariableTextField.getText());
            } else if (_tabbedPaneConditional.getSelectedComponent() == _panelConditionalFormula) {
                expression.setAddressing(NamedBeanAddressing.Formula);
                expression.setFormula(_oblockFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneConditional has unknown selection");
            }
            
            expression.set_Is_IsNot((Is_IsNot_Enum)is_IsNot_ComboBox.getSelectedItem());
            
            if (_tabbedPaneConditionalState.getSelectedComponent() == _panelConditionalStateDirect) {
                expression.setStateAddressing(NamedBeanAddressing.Direct);
                expression.setConditionalState((ExpressionConditional.ConditionalState)_stateComboBox.getSelectedItem());
            } else if (_tabbedPaneConditionalState.getSelectedComponent() == _panelConditionalStateReference) {
                expression.setStateAddressing(NamedBeanAddressing.Reference);
                expression.setStateReference(_oblockStateReferenceTextField.getText());
            } else if (_tabbedPaneConditionalState.getSelectedComponent() == _panelConditionalStateLocalVariable) {
                expression.setStateAddressing(NamedBeanAddressing.LocalVariable);
                expression.setStateLocalVariable(_oblockStateLocalVariableTextField.getText());
            } else if (_tabbedPaneConditionalState.getSelectedComponent() == _panelConditionalStateFormula) {
                expression.setStateAddressing(NamedBeanAddressing.Formula);
                expression.setStateFormula(_oblockStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneConditionalState has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Conditional_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionConditionalSwing.class);
    
}
