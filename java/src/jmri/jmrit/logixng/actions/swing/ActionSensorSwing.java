package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionSensor;
import jmri.jmrit.logixng.actions.ActionSensor.SensorState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionSensor object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionSensorSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneSensor;
    private BeanSelectPanel<Sensor> sensorBeanPanel;
    private JPanel _panelSensorDirect;
    private JPanel _panelSensorReference;
    private JPanel _panelSensorLocalVariable;
    private JPanel _panelSensorFormula;
    private JTextField _sensorReferenceTextField;
    private JTextField _sensorLocalVariableTextField;
    private JTextField _sensorFormulaTextField;
    
    private JTabbedPane _tabbedPaneSensorState;
    private JComboBox<SensorState> _stateComboBox;
    private JPanel _panelSensorStateDirect;
    private JPanel _panelSensorStateReference;
    private JPanel _panelSensorStateLocalVariable;
    private JPanel _panelSensorStateFormula;
    private JTextField _sensorStateReferenceTextField;
    private JTextField _sensorStateLocalVariableTextField;
    private JTextField _sensorStateFormulaTextField;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionSensor action = (ActionSensor)object;
        
        panel = new JPanel();
        
        _tabbedPaneSensor = new JTabbedPane();
        _panelSensorDirect = new javax.swing.JPanel();
        _panelSensorReference = new javax.swing.JPanel();
        _panelSensorLocalVariable = new javax.swing.JPanel();
        _panelSensorFormula = new javax.swing.JPanel();
        
        _tabbedPaneSensor.addTab(NamedBeanAddressing.Direct.toString(), _panelSensorDirect);
        _tabbedPaneSensor.addTab(NamedBeanAddressing.Reference.toString(), _panelSensorReference);
        _tabbedPaneSensor.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelSensorLocalVariable);
        _tabbedPaneSensor.addTab(NamedBeanAddressing.Formula.toString(), _panelSensorFormula);
        
        sensorBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(SensorManager.class), null);
        _panelSensorDirect.add(sensorBeanPanel);
        
        _sensorReferenceTextField = new JTextField();
        _sensorReferenceTextField.setColumns(30);
        _panelSensorReference.add(_sensorReferenceTextField);
        
        _sensorLocalVariableTextField = new JTextField();
        _sensorLocalVariableTextField.setColumns(30);
        _panelSensorLocalVariable.add(_sensorLocalVariableTextField);
        
        _sensorFormulaTextField = new JTextField();
        _sensorFormulaTextField.setColumns(30);
        _panelSensorFormula.add(_sensorFormulaTextField);
        
        
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
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneSensor.setSelectedComponent(_panelSensorDirect); break;
                case Reference: _tabbedPaneSensor.setSelectedComponent(_panelSensorReference); break;
                case LocalVariable: _tabbedPaneSensor.setSelectedComponent(_panelSensorLocalVariable); break;
                case Formula: _tabbedPaneSensor.setSelectedComponent(_panelSensorFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getSensor() != null) {
                sensorBeanPanel.setDefaultNamedBean(action.getSensor().getBean());
            }
            _sensorReferenceTextField.setText(action.getReference());
            _sensorLocalVariableTextField.setText(action.getLocalVariable());
            _sensorFormulaTextField.setText(action.getFormula());
            
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
            _tabbedPaneSensor,
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
            if (_tabbedPaneSensor.getSelectedComponent() == _panelSensorReference) {
                action.setReference(_sensorReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            if (_tabbedPaneSensorState.getSelectedComponent() == _panelSensorStateReference) {
                action.setStateReference(_sensorStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            action.setFormula(_sensorFormulaTextField.getText());
            if (_tabbedPaneSensor.getSelectedComponent() == _panelSensorDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneSensor.getSelectedComponent() == _panelSensorReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneSensor.getSelectedComponent() == _panelSensorLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneSensor.getSelectedComponent() == _panelSensorFormula) {
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
        if (_tabbedPaneSensor.getSelectedComponent() == _panelSensorDirect) {
            Sensor sensor = sensorBeanPanel.getNamedBean();
            if (sensor != null) {
                NamedBeanHandle<Sensor> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(sensor.getDisplayName(), sensor);
                action.setSensor(handle);
            } else {
                action.removeSensor();
            }
        } else {
            action.removeSensor();
        }
        try {
            if (_tabbedPaneSensor.getSelectedComponent() == _panelSensorDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneSensor.getSelectedComponent() == _panelSensorReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_sensorReferenceTextField.getText());
            } else if (_tabbedPaneSensor.getSelectedComponent() == _panelSensorLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_sensorLocalVariableTextField.getText());
            } else if (_tabbedPaneSensor.getSelectedComponent() == _panelSensorFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_sensorFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneSensor has unknown selection");
            }
            
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
        if (sensorBeanPanel != null) {
            sensorBeanPanel.dispose();
        }
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSensorSwing.class);
    
}
