package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.expressions.ExpressionSensor.SensorState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionSensor object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionSensorSwing extends AbstractDigitalExpressionSwing {

    private LogixNG_SelectNamedBeanSwing<Sensor> _selectNamedBeanSwing;

    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;

    private JTabbedPane _tabbedPaneSensorState;
    private JComboBox<SensorState> _stateComboBox;
    private JPanel _panelSensorStateDirect;
    private JPanel _panelSensorStateReference;
    private JPanel _panelSensorStateLocalVariable;
    private JPanel _panelSensorStateFormula;
    private JTextField _sensorStateReferenceTextField;
    private JTextField _sensorStateLocalVariableTextField;
    private JTextField _sensorStateFormulaTextField;


    public ExpressionSensorSwing() {
    }

    public ExpressionSensorSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionSensor expression = (ExpressionSensor)object;

        panel = new JPanel();

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(SensorManager.class), getJDialog(), this);

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


        _tabbedPaneSensorState = new JTabbedPane();
        _panelSensorStateDirect = new javax.swing.JPanel();
        _panelSensorStateReference = new javax.swing.JPanel();
        _panelSensorStateLocalVariable = new javax.swing.JPanel();
        _panelSensorStateFormula = new javax.swing.JPanel();

        _tabbedPaneSensorState.addTab(NamedBeanAddressing.Direct.toString(), _panelSensorStateDirect);
        _tabbedPaneSensorState.addTab(NamedBeanAddressing.Reference.toString(), _panelSensorStateReference);
        _tabbedPaneSensorState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelSensorStateLocalVariable);
        _tabbedPaneSensorState.addTab(NamedBeanAddressing.Formula.toString(), _panelSensorStateFormula);

        _stateComboBox = new JComboBox<>();
        for (SensorState e : SensorState.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);

        _panelSensorStateDirect.add(_stateComboBox);

        _sensorStateReferenceTextField = new JTextField();
        _sensorStateReferenceTextField.setColumns(30);
        _panelSensorStateReference.add(_sensorStateReferenceTextField);

        _sensorStateLocalVariableTextField = new JTextField();
        _sensorStateLocalVariableTextField.setColumns(30);
        _panelSensorStateLocalVariable.add(_sensorStateLocalVariableTextField);

        _sensorStateFormulaTextField = new JTextField();
        _sensorStateFormulaTextField.setColumns(30);
        _panelSensorStateFormula.add(_sensorStateFormulaTextField);


        if (expression != null) {
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());

            switch (expression.getStateAddressing()) {
                case Direct: _tabbedPaneSensorState.setSelectedComponent(_panelSensorStateDirect); break;
                case Reference: _tabbedPaneSensorState.setSelectedComponent(_panelSensorStateReference); break;
                case LocalVariable: _tabbedPaneSensorState.setSelectedComponent(_panelSensorStateLocalVariable); break;
                case Formula: _tabbedPaneSensorState.setSelectedComponent(_panelSensorStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getStateAddressing().name());
            }
            _stateComboBox.setSelectedItem(expression.getBeanState());
            _sensorStateReferenceTextField.setText(expression.getStateReference());
            _sensorStateLocalVariableTextField.setText(expression.getStateLocalVariable());
            _sensorStateFormulaTextField.setText(expression.getStateFormula());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _is_IsNot_ComboBox,
            _tabbedPaneSensorState};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionSensor_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionSensor expression = new ExpressionSensor("IQDE1", null);

        _selectNamedBeanSwing.validate(expression.getSelectNamedBean(), errorMessages);

        try {
            if (_tabbedPaneSensorState.getSelectedComponent() == _panelSensorStateReference) {
                expression.setStateReference(_sensorStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
        }

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public String getAutoSystemName() {
        return InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExpressionSensor expression = new ExpressionSensor(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionSensor)) {
            throw new IllegalArgumentException("object must be an ExpressionSensor but is a: "+object.getClass().getName());
        }
        ExpressionSensor expression = (ExpressionSensor)object;

        _selectNamedBeanSwing.updateObject(expression.getSelectNamedBean());

        try {
            expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());

            if (_tabbedPaneSensorState.getSelectedComponent() == _panelSensorStateDirect) {
                expression.setStateAddressing(NamedBeanAddressing.Direct);
                expression.setBeanState((SensorState)_stateComboBox.getSelectedItem());
            } else if (_tabbedPaneSensorState.getSelectedComponent() == _panelSensorStateReference) {
                expression.setStateAddressing(NamedBeanAddressing.Reference);
                expression.setStateReference(_sensorStateReferenceTextField.getText());
            } else if (_tabbedPaneSensorState.getSelectedComponent() == _panelSensorStateLocalVariable) {
                expression.setStateAddressing(NamedBeanAddressing.LocalVariable);
                expression.setStateLocalVariable(_sensorStateLocalVariableTextField.getText());
            } else if (_tabbedPaneSensorState.getSelectedComponent() == _panelSensorStateFormula) {
                expression.setStateAddressing(NamedBeanAddressing.Formula);
                expression.setStateFormula(_sensorStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneSensorState has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Sensor_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSensorSwing.class);

}
