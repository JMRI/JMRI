package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionTimer;
import jmri.jmrit.logixng.actions.swing.Bundle;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 */
public class ActionTimerSwing extends AbstractDigitalActionSwing {

    public static final int MAX_NUM_TIMERS = 10;
    
    private JCheckBox _startImmediately;
    private JCheckBox _runContinuously;
    private JTextField _numTimers;
    private JButton _addTimer;
    private JButton _removeTimer;
    private JFormattedTextField[] _timerDelays;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ActionTimer)) {
            throw new IllegalArgumentException("object must be an ActionTimer but is a: "+object.getClass().getName());
        }
        
        ActionTimer action = (ActionTimer)object;
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        _startImmediately = new JCheckBox(Bundle.getMessage("ActionTimerSwing_StartImmediately"));
        _runContinuously = new JCheckBox(Bundle.getMessage("ActionTimerSwing_RunContinuously"));
        panel.add(_startImmediately);
        panel.add(_runContinuously);
        
        JPanel numActionsPanel = new JPanel();
        _numTimers = new JTextField(1);
        _numTimers.setColumns(2);
        _numTimers.setEnabled(false);
        _addTimer = new JButton(Bundle.getMessage("ActionTimerSwing_AddTimer"));
        _removeTimer = new JButton(Bundle.getMessage("ActionTimerSwing_RemoveTimer"));
        numActionsPanel.add(new JLabel(Bundle.getMessage("ActionTimerSwing_NumTimers")));
        numActionsPanel.add(_numTimers);
        numActionsPanel.add(_addTimer);
        numActionsPanel.add(_removeTimer);
        panel.add(numActionsPanel);
        
        JPanel timerDelaysPanel = new JPanel();
        timerDelaysPanel.setLayout(new BoxLayout(timerDelaysPanel, BoxLayout.Y_AXIS));
        timerDelaysPanel.add(new JLabel(Bundle.getMessage("ActionTimerSwing_TimerDelays")));
        JPanel timerDelaysSubPanel = new JPanel();
        _timerDelays = new JFormattedTextField[MAX_NUM_TIMERS];
        for (int i=0; i < MAX_NUM_TIMERS; i++) {
            JPanel delayPanel = new JPanel();
            delayPanel.setLayout(new BoxLayout(delayPanel, BoxLayout.Y_AXIS));
            boolean enabled = false;
            String socketName = "A";
            if ((action != null) && (i < action.getNumActions())) {
                socketName = action.getActionSocket(i).getName();
                enabled = true;
            }
            JLabel label = new JLabel(socketName);
            if (!enabled) label.setEnabled(false);
            delayPanel.add(label);
            _timerDelays[i] = new JFormattedTextField("123");
            _timerDelays[i].setColumns(7);
            if (!enabled) _timerDelays[i].setEnabled(false);
            delayPanel.add(_timerDelays[i]);
            timerDelaysSubPanel.add(delayPanel);
        }
        timerDelaysPanel.add(timerDelaysSubPanel);
        panel.add(timerDelaysPanel);
        
        if (action != null) {
            _numTimers = new JTextField(Integer.toString(action.getNumActions()));
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionTimer action = new ActionTimer(systemName, userName);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Timer_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}
