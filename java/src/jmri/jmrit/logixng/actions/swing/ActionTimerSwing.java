package jmri.jmrit.logixng.actions.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionTimer;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 */
public class ActionTimerSwing extends AbstractDigitalActionSwing {

    public static final int MAX_NUM_TIMERS = 10;
    
    private JCheckBox _startImmediately;
    private JCheckBox _runContinuously;
    private JComboBox<ActionTimer.Unit> _unit;
    private JTextField _numTimers;
    private JButton _addTimer;
    private JButton _removeTimer;
    private JTextField[] _timerSocketNames;
    private JFormattedTextField[] _timerDelays;
    private int numActions = 1;
    
    private String getNewSocketName(ActionTimer action) {
        int numExpr = action.getNumExpressions();
        int size = action.getNumExpressions() + MAX_NUM_TIMERS;
        String[] names = new String[size];
        for (int i=0; i < numExpr; i++) {
            names[i] = action.getExpressionSocket(i).getName();
        }
        for (int i=0; i < MAX_NUM_TIMERS; i++) {
            names[numExpr+i] = _timerSocketNames[i].getText();
        }
        return action.getNewSocketName(names);
    }
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ActionTimer)) {
            throw new IllegalArgumentException("object must be an ActionTimer but is a: "+object.getClass().getName());
        }
        
        // Create a temporary action in case we don't have one.
        ActionTimer action = object != null ? (ActionTimer)object : new ActionTimer("IQDA1", null);
        
        numActions = action.getNumActions();
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        _startImmediately = new JCheckBox(Bundle.getMessage("ActionTimerSwing_StartImmediately"));
        _runContinuously = new JCheckBox(Bundle.getMessage("ActionTimerSwing_RunContinuously"));
        
        _unit = new JComboBox<>();
        for (ActionTimer.Unit u : ActionTimer.Unit.values()) _unit.addItem(u);
        _unit.setSelectedItem(action.getUnit());
        
        panel.add(_startImmediately);
        panel.add(_runContinuously);
        panel.add(_unit);
        
        JPanel numActionsPanel = new JPanel();
        _numTimers = new JTextField(Integer.toString(numActions));
        _numTimers.setColumns(2);
        _numTimers.setEnabled(false);
        
        _addTimer = new JButton(Bundle.getMessage("ActionTimerSwing_AddTimer"));
        _addTimer.addActionListener((ActionEvent e) -> {
            numActions++;
            _numTimers.setText(Integer.toString(numActions));
            if (_timerSocketNames[numActions-1].getText().trim().isEmpty()) {
                _timerSocketNames[numActions-1].setText(getNewSocketName(action));
            }
            _timerSocketNames[numActions-1].setEnabled(true);
            _timerDelays[numActions-1].setEnabled(true);
            if (numActions >= MAX_NUM_TIMERS) _addTimer.setEnabled(false);
            _removeTimer.setEnabled(true);
        });
        if (numActions >= MAX_NUM_TIMERS) _addTimer.setEnabled(false);
        
        _removeTimer = new JButton(Bundle.getMessage("ActionTimerSwing_RemoveTimer"));
        _removeTimer.addActionListener((ActionEvent e) -> {
            _timerSocketNames[numActions-1].setEnabled(false);
            _timerDelays[numActions-1].setEnabled(false);
            numActions--;
            _numTimers.setText(Integer.toString(numActions));
            _addTimer.setEnabled(true);
            if ((numActions <= 1)
                    || ((action.getNumActions() >= numActions)
                        && (action.getActionSocket(numActions-1).isConnected()))) {
                _removeTimer.setEnabled(false);
            }
        });
        if ((numActions <= 1) || (action.getActionSocket(numActions-1).isConnected())) {
            _removeTimer.setEnabled(false);
        }
        
        numActionsPanel.add(new JLabel(Bundle.getMessage("ActionTimerSwing_NumTimers")));
        numActionsPanel.add(_numTimers);
        numActionsPanel.add(_addTimer);
        numActionsPanel.add(_removeTimer);
        panel.add(numActionsPanel);
        
        JPanel timerDelaysPanel = new JPanel();
        timerDelaysPanel.setLayout(new BoxLayout(timerDelaysPanel, BoxLayout.Y_AXIS));
        timerDelaysPanel.add(new JLabel(Bundle.getMessage("ActionTimerSwing_TimerDelays")));
        JPanel timerDelaysSubPanel = new JPanel();
        _timerSocketNames = new JTextField[MAX_NUM_TIMERS];
        _timerDelays = new JFormattedTextField[MAX_NUM_TIMERS];
        
        for (int i=0; i < MAX_NUM_TIMERS; i++) {
            JPanel delayPanel = new JPanel();
            delayPanel.setLayout(new BoxLayout(delayPanel, BoxLayout.Y_AXIS));
            _timerSocketNames[i] = new JTextField();
            _timerSocketNames[i].setEnabled(false);
            delayPanel.add(_timerSocketNames[i]);
            _timerDelays[i] = new JFormattedTextField("0");
            _timerDelays[i].setColumns(7);
            _timerDelays[i].setEnabled(false);
            delayPanel.add(_timerDelays[i]);
            timerDelaysSubPanel.add(delayPanel);
            if (i < action.getNumActions()) {
                String socketName = action.getActionSocket(i).getName();
                _timerSocketNames[i].setText(socketName);
                _timerSocketNames[i].setEnabled(true);
                _timerDelays[i].setText(Integer.toString(action.getDelay(i)));
                _timerDelays[i].setEnabled(true);
            }
        }
        timerDelaysPanel.add(timerDelaysSubPanel);
        panel.add(timerDelaysPanel);
        
        _startImmediately.setSelected(action.getStartImmediately());
        _runContinuously.setSelected(action.getRunContinuously());
        _numTimers.setText(Integer.toString(action.getNumActions()));
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
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof ActionTimer)) {
            throw new IllegalArgumentException("object must be an ActionTimer but is a: "+object.getClass().getName());
        }
        
        ActionTimer action = (ActionTimer)object;
        
        action.setStartImmediately(_startImmediately.isSelected());
        action.setRunContinuously(_runContinuously.isSelected());
        action.setUnit(_unit.getItemAt(_unit.getSelectedIndex()));
        action.setNumActions(numActions);
        
        for (int i=0; i < numActions; i++) {
            action.getActionSocket(i).setName(_timerSocketNames[i].getText());
            action.setDelay(i, Integer.parseInt(_timerDelays[i].getText()));
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionTimer_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}
