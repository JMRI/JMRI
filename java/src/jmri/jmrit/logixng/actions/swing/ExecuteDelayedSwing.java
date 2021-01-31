package jmri.jmrit.logixng.actions.swing;

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

    private JFormattedTextField _timerDelay;
    private JComboBox<TimerUnit> _unit;
    private JCheckBox _resetIfAlreadyStarted;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ExecuteDelayed)) {
            throw new IllegalArgumentException("object must be an ExecuteDelayed but is a: "+object.getClass().getName());
        }
        
        ExecuteDelayed action = (ExecuteDelayed)object;
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        _unit = new JComboBox<>();
        for (TimerUnit u : TimerUnit.values()) _unit.addItem(u);
        if (action != null) _unit.setSelectedItem(action.getUnit());
        
        panel.add(_unit);
        
        JPanel timerDelaysPanel = new JPanel();
        timerDelaysPanel.setLayout(new BoxLayout(timerDelaysPanel, BoxLayout.Y_AXIS));
        timerDelaysPanel.add(new JLabel(Bundle.getMessage("ExecuteDelayedSwing_TimerDelay")));
        
        _timerDelay = new JFormattedTextField("0");
        _timerDelay.setColumns(7);
        if (action != null) _timerDelay.setText(Integer.toString(action.getDelay()));
        timerDelaysPanel.add(_timerDelay);
        panel.add(timerDelaysPanel);
        
        JPanel resetIfAlreadyStartedPanel = new JPanel();
        resetIfAlreadyStartedPanel.setLayout(new BoxLayout(resetIfAlreadyStartedPanel, BoxLayout.Y_AXIS));
        resetIfAlreadyStartedPanel.add(new JLabel(Bundle.getMessage("ExecuteDelayedSwing_ResetIfAlreadyStarted")));
        
        _resetIfAlreadyStarted = new JCheckBox();
        if (action != null) _resetIfAlreadyStarted.setSelected(action.getResetIfAlreadyStarted());
        resetIfAlreadyStartedPanel.add(_resetIfAlreadyStarted);
        panel.add(resetIfAlreadyStartedPanel);
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
