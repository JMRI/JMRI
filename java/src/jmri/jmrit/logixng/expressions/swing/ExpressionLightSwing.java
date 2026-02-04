package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionLight;
import jmri.jmrit.logixng.expressions.ExpressionLight.LightState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionLight object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionLightSwing extends AbstractDigitalExpressionSwing {

    private LogixNG_SelectNamedBeanSwing<Light> _selectNamedBeanSwing;

    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;

    private JTabbedPane _tabbedPaneLightState;
    private JComboBox<LightState> _stateComboBox;
    private JPanel _panelLightStateDirect;
    private JPanel _panelLightStateReference;
    private JPanel _panelLightStateLocalVariable;
    private JPanel _panelLightStateFormula;
    private JTextField _lightStateReferenceTextField;
    private JTextField _lightStateLocalVariableTextField;
    private JTextField _lightStateFormulaTextField;


    public ExpressionLightSwing() {
    }

    public ExpressionLightSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionLight expression = (ExpressionLight)object;

        panel = new JPanel();

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(LightManager.class), getJDialog(), this);

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


        _tabbedPaneLightState = new JTabbedPane();
        _panelLightStateDirect = new javax.swing.JPanel();
        _panelLightStateReference = new javax.swing.JPanel();
        _panelLightStateLocalVariable = new javax.swing.JPanel();
        _panelLightStateFormula = new javax.swing.JPanel();

        _tabbedPaneLightState.addTab(NamedBeanAddressing.Direct.toString(), _panelLightStateDirect);
        _tabbedPaneLightState.addTab(NamedBeanAddressing.Reference.toString(), _panelLightStateReference);
        _tabbedPaneLightState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLightStateLocalVariable);
        _tabbedPaneLightState.addTab(NamedBeanAddressing.Formula.toString(), _panelLightStateFormula);

        _stateComboBox = new JComboBox<>();
        for (LightState e : LightState.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);

        _panelLightStateDirect.add(_stateComboBox);

        _lightStateReferenceTextField = new JTextField();
        _lightStateReferenceTextField.setColumns(30);
        _panelLightStateReference.add(_lightStateReferenceTextField);

        _lightStateLocalVariableTextField = new JTextField();
        _lightStateLocalVariableTextField.setColumns(30);
        _panelLightStateLocalVariable.add(_lightStateLocalVariableTextField);

        _lightStateFormulaTextField = new JTextField();
        _lightStateFormulaTextField.setColumns(30);
        _panelLightStateFormula.add(_lightStateFormulaTextField);


        if (expression != null) {
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());

            switch (expression.getStateAddressing()) {
                case Direct: _tabbedPaneLightState.setSelectedComponent(_panelLightStateDirect); break;
                case Reference: _tabbedPaneLightState.setSelectedComponent(_panelLightStateReference); break;
                case LocalVariable: _tabbedPaneLightState.setSelectedComponent(_panelLightStateLocalVariable); break;
                case Formula: _tabbedPaneLightState.setSelectedComponent(_panelLightStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getStateAddressing().name());
            }
            _stateComboBox.setSelectedItem(expression.getBeanState());
            _lightStateReferenceTextField.setText(expression.getStateReference());
            _lightStateLocalVariableTextField.setText(expression.getStateLocalVariable());
            _lightStateFormulaTextField.setText(expression.getStateFormula());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _is_IsNot_ComboBox,
            _tabbedPaneLightState};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionLight_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionLight expression = new ExpressionLight("IQDE1", null);

        _selectNamedBeanSwing.validate(expression.getSelectNamedBean(), errorMessages);

        try {
            if (_tabbedPaneLightState.getSelectedComponent() == _panelLightStateReference) {
                expression.setStateReference(_lightStateReferenceTextField.getText());
            } else if (_tabbedPaneLightState.getSelectedComponent() == _panelLightStateFormula) {
                expression.setStateAddressing(NamedBeanAddressing.Formula);
                expression.setStateFormula(_lightStateFormulaTextField.getText());
            }
        } catch (IllegalArgumentException | ParserException e) {
            errorMessages.add(e.getMessage());
        }

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExpressionLight expression = new ExpressionLight(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionLight)) {
            throw new IllegalArgumentException("object must be an ExpressionLight but is a: "+object.getClass().getName());
        }
        ExpressionLight expression = (ExpressionLight)object;

        _selectNamedBeanSwing.updateObject(expression.getSelectNamedBean());

        try {
            expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());

            if (_tabbedPaneLightState.getSelectedComponent() == _panelLightStateDirect) {
                expression.setStateAddressing(NamedBeanAddressing.Direct);
                expression.setBeanState((LightState)_stateComboBox.getSelectedItem());
            } else if (_tabbedPaneLightState.getSelectedComponent() == _panelLightStateReference) {
                expression.setStateAddressing(NamedBeanAddressing.Reference);
                expression.setStateReference(_lightStateReferenceTextField.getText());
            } else if (_tabbedPaneLightState.getSelectedComponent() == _panelLightStateLocalVariable) {
                expression.setStateAddressing(NamedBeanAddressing.LocalVariable);
                expression.setStateLocalVariable(_lightStateLocalVariableTextField.getText());
            } else if (_tabbedPaneLightState.getSelectedComponent() == _panelLightStateFormula) {
                expression.setStateAddressing(NamedBeanAddressing.Formula);
                expression.setStateFormula(_lightStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneLightState has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Light_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionLightSwing.class);

}
