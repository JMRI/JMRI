package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionLightIntensity;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;

/**
 * Configures an ActionLightIntensity object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionLightIntensitySwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneLight;
    private BeanSelectPanel<VariableLight> lightBeanPanel;
    private JPanel _panelLightDirect;
    private JPanel _panelLightReference;
    private JPanel _panelLightLocalVariable;
    private JPanel _panelLightFormula;
    private JTextField _lightReferenceTextField;
    private JTextField _lightLocalVariableTextField;
    private JTextField _lightFormulaTextField;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionLightIntensity action = (ActionLightIntensity)object;
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        _tabbedPaneLight = new JTabbedPane();
        _panelLightDirect = new javax.swing.JPanel();
        _panelLightReference = new javax.swing.JPanel();
        _panelLightLocalVariable = new javax.swing.JPanel();
        _panelLightFormula = new javax.swing.JPanel();
        
        _tabbedPaneLight.addTab(NamedBeanAddressing.Direct.toString(), _panelLightDirect);
        _tabbedPaneLight.addTab(NamedBeanAddressing.Reference.toString(), _panelLightReference);
        _tabbedPaneLight.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLightLocalVariable);
        _tabbedPaneLight.addTab(NamedBeanAddressing.Formula.toString(), _panelLightFormula);
        
        lightBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(VariableLightManager.class), null);
        _panelLightDirect.add(lightBeanPanel);
        
        _lightReferenceTextField = new JTextField();
        _lightReferenceTextField.setColumns(30);
        _panelLightReference.add(_lightReferenceTextField);
        
        _lightLocalVariableTextField = new JTextField();
        _lightLocalVariableTextField.setColumns(30);
        _panelLightLocalVariable.add(_lightLocalVariableTextField);
        
        _lightFormulaTextField = new JTextField();
        _lightFormulaTextField.setColumns(30);
        _panelLightFormula.add(_lightFormulaTextField);
        
        
        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneLight.setSelectedComponent(_panelLightDirect); break;
                case Reference: _tabbedPaneLight.setSelectedComponent(_panelLightReference); break;
                case LocalVariable: _tabbedPaneLight.setSelectedComponent(_panelLightLocalVariable); break;
                case Formula: _tabbedPaneLight.setSelectedComponent(_panelLightFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getLight() != null) {
                lightBeanPanel.setDefaultNamedBean(action.getLight().getBean());
            }
            _lightReferenceTextField.setText(action.getReference());
            _lightLocalVariableTextField.setText(action.getLocalVariable());
            _lightFormulaTextField.setText(action.getFormula());
        }
        
        panel.add(_tabbedPaneLight);
        
        panel.add(javax.swing.Box.createVerticalStrut(10));
        
        JPanel labelPanel = new JPanel();
        labelPanel.add(new JLabel(Bundle.getMessage("ActionLightIntensity_Descr")));
        panel.add(labelPanel);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionLightIntensity action = new ActionLightIntensity("IQDA1", null);
        
        try {
            if (_tabbedPaneLight.getSelectedComponent() == _panelLightReference) {
                action.setReference(_lightReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            action.setFormula(_lightFormulaTextField.getText());
            if (_tabbedPaneLight.getSelectedComponent() == _panelLightDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneLight.getSelectedComponent() == _panelLightReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneLight.getSelectedComponent() == _panelLightLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneLight.getSelectedComponent() == _panelLightFormula) {
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
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionLightIntensity action = new ActionLightIntensity(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionLightIntensity)) {
            throw new IllegalArgumentException("object must be an ActionLightIntensity but is a: "+object.getClass().getName());
        }
        ActionLightIntensity action = (ActionLightIntensity)object;
        if (_tabbedPaneLight.getSelectedComponent() == _panelLightDirect) {
            VariableLight light = lightBeanPanel.getNamedBean();
            if (light != null) {
                NamedBeanHandle<VariableLight> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(light.getDisplayName(), light);
                action.setLight(handle);
            } else {
                action.removeLight();
            }
        } else {
            action.removeLight();
        }
        try {
            if (_tabbedPaneLight.getSelectedComponent() == _panelLightDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneLight.getSelectedComponent() == _panelLightReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_lightReferenceTextField.getText());
            } else if (_tabbedPaneLight.getSelectedComponent() == _panelLightLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_lightLocalVariableTextField.getText());
            } else if (_tabbedPaneLight.getSelectedComponent() == _panelLightFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_lightFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneLight has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionLightIntensity_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}
