package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionSensor;
import jmri.jmrit.logixng.actions.ActionSensor.SensorState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionSensor object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionSensorSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectNamedBeanSwing<Sensor> _selectNamedBeanSwing;

    private JTabbedPane _tabbedPaneSensorState;
    private JComboBox<SensorState> _stateComboBox;
    private JPanel _panelSensorStateDirect;
    private JPanel _panelSensorStateReference;
    private JPanel _panelSensorStateLocalVariable;
    private JPanel _panelSensorStateFormula;
    private JTextField _sensorStateReferenceTextField;
    private JTextField _sensorStateLocalVariableTextField;
    private JTextField _sensorStateFormulaTextField;


    public ActionSensorSwing() {
    }

    public ActionSensorSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionSensor action = (ActionSensor)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(SensorManager.class), getJDialog(), this);

        panel = new JPanel();

        JPanel _tabbedPaneNamedBean;
        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }


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


        if (action != null) {
            switch (action.getStateAddressing()) {
                case Direct: _tabbedPaneSensorState.setSelectedComponent(_panelSensorStateDirect); break;
                case Reference: _tabbedPaneSensorState.setSelectedComponent(_panelSensorStateReference); break;
                case LocalVariable: _tabbedPaneSensorState.setSelectedComponent(_panelSensorStateLocalVariable); break;
                case Formula: _tabbedPaneSensorState.setSelectedComponent(_panelSensorStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getStateAddressing().name());
            }
            _stateComboBox.setSelectedItem(action.getBeanState());
            _sensorStateReferenceTextField.setText(action.getStateReference());
            _sensorStateLocalVariableTextField.setText(action.getStateLocalVariable());
            _sensorStateFormulaTextField.setText(action.getStateFormula());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _tabbedPaneSensorState};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionSensor_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionSensor action = new ActionSensor("IQDA1", null);

        try {
            if (_tabbedPaneSensorState.getSelectedComponent() == _panelSensorStateReference) {
                action.setStateReference(_sensorStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public String getAutoSystemName() {
        return InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionSensor action = new ActionSensor(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionSensor)) {
            throw new IllegalArgumentException("object must be an ActionSensor but is a: "+object.getClass().getName());
        }
        ActionSensor action = (ActionSensor)object;
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());
        try {
            if (_tabbedPaneSensorState.getSelectedComponent() == _panelSensorStateDirect) {
                action.setStateAddressing(NamedBeanAddressing.Direct);
                action.setBeanState(_stateComboBox.getItemAt(_stateComboBox.getSelectedIndex()));
            } else if (_tabbedPaneSensorState.getSelectedComponent() == _panelSensorStateReference) {
                action.setStateAddressing(NamedBeanAddressing.Reference);
                action.setStateReference(_sensorStateReferenceTextField.getText());
            } else if (_tabbedPaneSensorState.getSelectedComponent() == _panelSensorStateLocalVariable) {
                action.setStateAddressing(NamedBeanAddressing.LocalVariable);
                action.setStateLocalVariable(_sensorStateLocalVariableTextField.getText());
            } else if (_tabbedPaneSensorState.getSelectedComponent() == _panelSensorStateFormula) {
                action.setStateAddressing(NamedBeanAddressing.Formula);
                action.setStateFormula(_sensorStateFormulaTextField.getText());
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
        // Do nothing
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSensorSwing.class);

}
