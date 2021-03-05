package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ExecuteDelayed;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.TimerUnit;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExecuteDelayed object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class ExecuteDelayedSwing extends AbstractDigitalActionSwing {

    private final JLabel _unitLabel = new JLabel(Bundle.getMessage("ExecuteDelayedSwing_Unit"));
    private JComboBox<TimerUnit> _unitComboBox;
    private JCheckBox _resetIfAlreadyStarted;
    
    private JTabbedPane _tabbedPaneDelay;
    private JFormattedTextField _timerDelay;
    private JPanel _panelDelayDirect;
    private JPanel _panelDelayReference;
    private JPanel _panelDelayLocalVariable;
    private JPanel _panelDelayFormula;
    private JTextField _delayReferenceTextField;
    private JTextField _delayLocalVariableTextField;
    private JTextField _delayFormulaTextField;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ExecuteDelayed)) {
            throw new IllegalArgumentException("object must be an ExecuteDelayed but is a: "+object.getClass().getName());
        }
        
        ExecuteDelayed action = (ExecuteDelayed)object;
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        _tabbedPaneDelay = new JTabbedPane();
        _panelDelayDirect = new javax.swing.JPanel();
        _panelDelayReference = new javax.swing.JPanel();
        _panelDelayLocalVariable = new javax.swing.JPanel();
        _panelDelayFormula = new javax.swing.JPanel();
        
        _tabbedPaneDelay.addTab(NamedBeanAddressing.Direct.toString(), _panelDelayDirect);
        _tabbedPaneDelay.addTab(NamedBeanAddressing.Reference.toString(), _panelDelayReference);
        _tabbedPaneDelay.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelDelayLocalVariable);
        _tabbedPaneDelay.addTab(NamedBeanAddressing.Formula.toString(), _panelDelayFormula);
        
        _timerDelay = new JFormattedTextField("0");
        _timerDelay.setColumns(7);
        
        _panelDelayDirect.add(_timerDelay);
        
        _delayReferenceTextField = new JTextField();
        _delayReferenceTextField.setColumns(30);
        _panelDelayReference.add(_delayReferenceTextField);
        
        _delayLocalVariableTextField = new JTextField();
        _delayLocalVariableTextField.setColumns(30);
        _panelDelayLocalVariable.add(_delayLocalVariableTextField);
        
        _delayFormulaTextField = new JTextField();
        _delayFormulaTextField.setColumns(30);
        _panelDelayFormula.add(_delayFormulaTextField);
        
        
        if (action != null) {
            switch (action.getDelayAddressing()) {
                case Direct: _tabbedPaneDelay.setSelectedComponent(_panelDelayDirect); break;
                case Reference: _tabbedPaneDelay.setSelectedComponent(_panelDelayReference); break;
                case LocalVariable: _tabbedPaneDelay.setSelectedComponent(_panelDelayLocalVariable); break;
                case Formula: _tabbedPaneDelay.setSelectedComponent(_panelDelayFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getDelayAddressing().name());
            }
            _timerDelay.setText(Integer.toString(action.getDelay()));
            _delayReferenceTextField.setText(action.getDelayReference());
            _delayLocalVariableTextField.setText(action.getDelayLocalVariable());
            _delayFormulaTextField.setText(action.getDelayFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _tabbedPaneDelay};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExecuteDelayed_Components"), components);
        
        JPanel delayPanel = new JPanel();
        for (JComponent c : componentList) delayPanel.add(c);
        
        panel.add(delayPanel);
        
        
        JPanel unitPanel = new JPanel();
        unitPanel.add(_unitLabel);
        
        _unitComboBox = new JComboBox<>();
        for (TimerUnit u : TimerUnit.values()) _unitComboBox.addItem(u);
        JComboBoxUtil.setupComboBoxMaxRows(_unitComboBox);
        if (action != null) _unitComboBox.setSelectedItem(action.getUnit());
        unitPanel.add(_unitComboBox);
        
        panel.add(unitPanel);
        
        _resetIfAlreadyStarted = new JCheckBox(Bundle.getMessage("ExecuteDelayedSwing_ResetIfAlreadyStarted"));
        if (action != null) _resetIfAlreadyStarted.setSelected(action.getResetIfAlreadyStarted());
        panel.add(_resetIfAlreadyStarted);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ExecuteDelayed action = new ExecuteDelayed("IQDA1", null);
        
        try {
            if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayReference) {
                action.setDelayReference(_delayReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            action.setDelayFormula(_delayFormulaTextField.getText());
            if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayDirect) {
                action.setDelayAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayReference) {
                action.setDelayAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayLocalVariable) {
                action.setDelayAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayFormula) {
                action.setDelayAddressing(NamedBeanAddressing.Formula);
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
        ExecuteDelayed action = new ExecuteDelayed(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof ExecuteDelayed)) {
            throw new IllegalArgumentException("object must be an ExecuteDelayed but is a: "+object.getClass().getName());
        }
        
        ExecuteDelayed action = (ExecuteDelayed)object;
        
        action.setUnit(_unitComboBox.getItemAt(_unitComboBox.getSelectedIndex()));
        action.setResetIfAlreadyStarted(_resetIfAlreadyStarted.isSelected());
        
        
        
        try {
            if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayDirect) {
                action.setDelayAddressing(NamedBeanAddressing.Direct);
                action.setDelay(Integer.parseInt(_timerDelay.getText()));
            } else if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayReference) {
                action.setDelayAddressing(NamedBeanAddressing.Reference);
                action.setDelayReference(_delayReferenceTextField.getText());
            } else if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayLocalVariable) {
                action.setDelayAddressing(NamedBeanAddressing.LocalVariable);
                action.setDelayLocalVariable(_delayLocalVariableTextField.getText());
            } else if (_tabbedPaneDelay.getSelectedComponent() == _panelDelayFormula) {
                action.setDelayAddressing(NamedBeanAddressing.Formula);
                action.setDelayFormula(_delayFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneDelay has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ExecuteDelayed_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}
