package jmri.jmrit.logixng.actions.swing;

import java.awt.GridBagConstraints;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ExecuteDelayed;
import jmri.jmrit.logixng.util.TimerUnit;

/**
 * Configures an ExecuteDelayed object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class ExecuteDelayedSwing extends AbstractDigitalActionSwing {

    private final JLabel _unitLabel = new JLabel(Bundle.getMessage("ExecuteDelayedSwing_Unit"));
    private JComboBox<TimerUnit> _unit;
    private final JLabel _timerDelayLabel = new JLabel(Bundle.getMessage("ExecuteDelayedSwing_TimerDelay"));
    private JFormattedTextField _timerDelay;
    private final JLabel _resetIfAlreadyStartedLabel = new JLabel(Bundle.getMessage("ExecuteDelayedSwing_ResetIfAlreadyStarted"));
    private JCheckBox _resetIfAlreadyStarted;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ExecuteDelayed)) {
            throw new IllegalArgumentException("object must be an ExecuteDelayed but is a: "+object.getClass().getName());
        }
        
        ExecuteDelayed action = (ExecuteDelayed)object;
        
        panel = new JPanel();
        panel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        panel.add(_unitLabel, c);
        
        c.gridx = 1;
        _unit = new JComboBox<>();
        for (TimerUnit u : TimerUnit.values()) _unit.addItem(u);
        if (action != null) _unit.setSelectedItem(action.getUnit());
        panel.add(_unit, c);
        
        c.gridx = 0;
        c.gridy = 1;
        panel.add(_timerDelayLabel, c);
        
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        _timerDelay = new JFormattedTextField("0");
        _timerDelay.setColumns(7);
        if (action != null) _timerDelay.setText(Integer.toString(action.getDelay()));
        _resetIfAlreadyStartedLabel.setLabelFor(_timerDelay);
        panel.add(_timerDelay, c);
        
        c.gridx = 0;
        c.gridy = 2;
        panel.add(_resetIfAlreadyStartedLabel, c);
        
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        _resetIfAlreadyStarted = new JCheckBox();
        if (action != null) _resetIfAlreadyStarted.setSelected(action.getResetIfAlreadyStarted());
        _resetIfAlreadyStartedLabel.setLabelFor(_resetIfAlreadyStarted);
        panel.add(_resetIfAlreadyStarted, c);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
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
        
        action.setDelay(Integer.parseInt(_timerDelay.getText()));
        action.setUnit(_unit.getItemAt(_unit.getSelectedIndex()));
        action.setResetIfAlreadyStarted(_resetIfAlreadyStarted.isSelected());
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
