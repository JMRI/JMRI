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
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionTurnout object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionConditionalSwing extends AbstractDigitalExpressionSwing {

    private LogixNG_SelectNamedBeanSwing<Conditional> _selectNamedBeanSwing;

    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;

    private JTabbedPane _tabbedPaneConditionalState;
    private JComboBox<ExpressionConditional.ConditionalState> _stateComboBox;
    private JPanel _panelConditionalStateDirect;
    private JPanel _panelConditionalStateReference;
    private JPanel _panelConditionalStateLocalVariable;
    private JPanel _panelConditionalStateFormula;
    private JTextField _conditionalStateReferenceTextField;
    private JTextField _conditionalStateLocalVariableTextField;
    private JTextField _conditionalStateFormulaTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionConditional expression = (ExpressionConditional)object;

        panel = new JPanel();

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(ConditionalManager.class), getJDialog(), this);

        JPanel _tabbedPaneNamedBean;
        if (expression != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(expression.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }


        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_is_IsNot_ComboBox);


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
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);

        _panelConditionalStateDirect.add(_stateComboBox);

        _conditionalStateReferenceTextField = new JTextField();
        _conditionalStateReferenceTextField.setColumns(30);
        _panelConditionalStateReference.add(_conditionalStateReferenceTextField);

        _conditionalStateLocalVariableTextField = new JTextField();
        _conditionalStateLocalVariableTextField.setColumns(30);
        _panelConditionalStateLocalVariable.add(_conditionalStateLocalVariableTextField);

        _conditionalStateFormulaTextField = new JTextField();
        _conditionalStateFormulaTextField.setColumns(30);
        _panelConditionalStateFormula.add(_conditionalStateFormulaTextField);


        if (expression != null) {
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());

            switch (expression.getStateAddressing()) {
                case Direct: _tabbedPaneConditionalState.setSelectedComponent(_panelConditionalStateDirect); break;
                case Reference: _tabbedPaneConditionalState.setSelectedComponent(_panelConditionalStateReference); break;
                case LocalVariable: _tabbedPaneConditionalState.setSelectedComponent(_panelConditionalStateLocalVariable); break;
                case Formula: _tabbedPaneConditionalState.setSelectedComponent(_panelConditionalStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getStateAddressing().name());
            }
            _stateComboBox.setSelectedItem(expression.getConditionalState());
            _conditionalStateReferenceTextField.setText(expression.getStateReference());
            _conditionalStateLocalVariableTextField.setText(expression.getStateLocalVariable());
            _conditionalStateFormulaTextField.setText(expression.getStateFormula());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _is_IsNot_ComboBox,
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

        _selectNamedBeanSwing.validate(expression.getSelectNamedBean(), errorMessages);

        try {
            if (_tabbedPaneConditionalState.getSelectedComponent() == _panelConditionalStateReference) {
                expression.setStateReference(_conditionalStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
        }

        return errorMessages.isEmpty();
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

        _selectNamedBeanSwing.updateObject(expression.getSelectNamedBean());

        try {
            expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());

            if (_tabbedPaneConditionalState.getSelectedComponent() == _panelConditionalStateDirect) {
                expression.setStateAddressing(NamedBeanAddressing.Direct);
                expression.setConditionalState((ExpressionConditional.ConditionalState)_stateComboBox.getSelectedItem());
            } else if (_tabbedPaneConditionalState.getSelectedComponent() == _panelConditionalStateReference) {
                expression.setStateAddressing(NamedBeanAddressing.Reference);
                expression.setStateReference(_conditionalStateReferenceTextField.getText());
            } else if (_tabbedPaneConditionalState.getSelectedComponent() == _panelConditionalStateLocalVariable) {
                expression.setStateAddressing(NamedBeanAddressing.LocalVariable);
                expression.setStateLocalVariable(_conditionalStateLocalVariableTextField.getText());
            } else if (_tabbedPaneConditionalState.getSelectedComponent() == _panelConditionalStateFormula) {
                expression.setStateAddressing(NamedBeanAddressing.Formula);
                expression.setStateFormula(_conditionalStateFormulaTextField.getText());
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


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionConditionalSwing.class);

}
