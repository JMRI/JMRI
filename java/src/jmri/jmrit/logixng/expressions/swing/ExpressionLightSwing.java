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
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionLight object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionLightSwing extends AbstractDigitalExpressionSwing {

    private JTabbedPane _tabbedPaneLight;
    private BeanSelectPanel<Light> _lightBeanPanel;
    private JPanel _panelLightDirect;
    private JPanel _panelLightReference;
    private JPanel _panelLightLocalVariable;
    private JPanel _panelLightFormula;
    private JTextField _lightReferenceTextField;
    private JTextField _lightLocalVariableTextField;
    private JTextField _lightFormulaTextField;
    
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
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionLight expression = (ExpressionLight)object;
        
        panel = new JPanel();
        
        _tabbedPaneLight = new JTabbedPane();
        _panelLightDirect = new javax.swing.JPanel();
        _panelLightReference = new javax.swing.JPanel();
        _panelLightLocalVariable = new javax.swing.JPanel();
        _panelLightFormula = new javax.swing.JPanel();
        
        _tabbedPaneLight.addTab(NamedBeanAddressing.Direct.toString(), _panelLightDirect);
        _tabbedPaneLight.addTab(NamedBeanAddressing.Reference.toString(), _panelLightReference);
        _tabbedPaneLight.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLightLocalVariable);
        _tabbedPaneLight.addTab(NamedBeanAddressing.Formula.toString(), _panelLightFormula);
        
        _lightBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(LightManager.class), null);
        _panelLightDirect.add(_lightBeanPanel);
        
        _lightReferenceTextField = new JTextField();
        _lightReferenceTextField.setColumns(30);
        _panelLightReference.add(_lightReferenceTextField);
        
        _lightLocalVariableTextField = new JTextField();
        _lightLocalVariableTextField.setColumns(30);
        _panelLightLocalVariable.add(_lightLocalVariableTextField);
        
        _lightFormulaTextField = new JTextField();
        _lightFormulaTextField.setColumns(30);
        _panelLightFormula.add(_lightFormulaTextField);
        
        
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
            switch (expression.getAddressing()) {
                case Direct: _tabbedPaneLight.setSelectedComponent(_panelLightDirect); break;
                case Reference: _tabbedPaneLight.setSelectedComponent(_panelLightReference); break;
                case LocalVariable: _tabbedPaneLight.setSelectedComponent(_panelLightLocalVariable); break;
                case Formula: _tabbedPaneLight.setSelectedComponent(_panelLightFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAddressing().name());
            }
            if (expression.getLight() != null) {
                _lightBeanPanel.setDefaultNamedBean(expression.getLight().getBean());
            }
            _lightReferenceTextField.setText(expression.getReference());
            _lightLocalVariableTextField.setText(expression.getLocalVariable());
            _lightFormulaTextField.setText(expression.getFormula());
            
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());
            
            switch (expression.getStateAddressing()) {
                case Direct: _tabbedPaneLightState.setSelectedComponent(_panelLightStateDirect); break;
                case Reference: _tabbedPaneLightState.setSelectedComponent(_panelLightStateReference); break;
                case LocalVariable: _tabbedPaneLightState.setSelectedComponent(_panelLightStateLocalVariable); break;
                case Formula: _tabbedPaneLightState.setSelectedComponent(_panelLightStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAddressing().name());
            }
            _stateComboBox.setSelectedItem(expression.getBeanState());
            _lightStateReferenceTextField.setText(expression.getStateReference());
            _lightStateLocalVariableTextField.setText(expression.getStateLocalVariable());
            _lightStateFormulaTextField.setText(expression.getStateFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _tabbedPaneLight,
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
        
        try {
            if (_tabbedPaneLight.getSelectedComponent() == _panelLightReference) {
                expression.setReference(_lightReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            if (_tabbedPaneLightState.getSelectedComponent() == _panelLightStateReference) {
                expression.setStateReference(_lightStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            expression.setFormula(_lightFormulaTextField.getText());
            if (_tabbedPaneLight.getSelectedComponent() == _panelLightDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneLight.getSelectedComponent() == _panelLightReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneLight.getSelectedComponent() == _panelLightLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneLight.getSelectedComponent() == _panelLightFormula) {
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
        if (_tabbedPaneLight.getSelectedComponent() == _panelLightDirect) {
            Light light = _lightBeanPanel.getNamedBean();
            if (light != null) {
                NamedBeanHandle<Light> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(light.getDisplayName(), light);
                expression.setLight(handle);
            } else {
                expression.removeLight();
            }
        } else {
            expression.removeLight();
        }
        try {
            if (_tabbedPaneLight.getSelectedComponent() == _panelLightDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneLight.getSelectedComponent() == _panelLightReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
                expression.setReference(_lightReferenceTextField.getText());
            } else if (_tabbedPaneLight.getSelectedComponent() == _panelLightLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
                expression.setLocalVariable(_lightLocalVariableTextField.getText());
            } else if (_tabbedPaneLight.getSelectedComponent() == _panelLightFormula) {
                expression.setAddressing(NamedBeanAddressing.Formula);
                expression.setFormula(_lightFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneLight has unknown selection");
            }
            
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
        if (_lightBeanPanel != null) {
            _lightBeanPanel.dispose();
        }
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionLightSwing.class);
    
}
