package jmri.jmrit.logixng.actions.swing;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AbstractDigitalAction;
import jmri.jmrit.logixng.actions.ActionTimer;
import jmri.jmrit.logixng.util.TimerUnit;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionTimerSwing extends AbstractDigitalActionSwing {

    public static final int MAX_NUM_TIMERS = 10;
    
    private JCheckBox _startImmediately;
    private JCheckBox _runContinuously;
    private JComboBox<TimerUnit> _unitComboBox;
    private JTextField _numTimers;
    private JButton _addTimer;
    private JButton _removeTimer;
    private JTextField[] _timerSocketNames;
    private JFormattedTextField[] _timerDelays;
    private int numActions = 1;
    private ActionTimer _tempAction;    // We need an action in validate() to check female socket names.
    
    private String getNewSocketName(ActionTimer action) {
        int size = ActionTimer.NUM_STATIC_EXPRESSIONS + MAX_NUM_TIMERS;
        String[] names = new String[size];
        names[ActionTimer.EXPRESSION_START] = action.getStartExpressionSocket().getName();
        names[ActionTimer.EXPRESSION_STOP] = action.getStopExpressionSocket().getName();
        for (int i=0; i < MAX_NUM_TIMERS; i++) {
            names[ActionTimer.NUM_STATIC_EXPRESSIONS + i] = _timerSocketNames[i].getText();
        }
        return AbstractDigitalAction.getNewSocketName(names);
    }
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ActionTimer)) {
            throw new IllegalArgumentException("object must be an ActionTimer but is a: "+object.getClass().getName());
        }
        
        // Create a temporary action in case we don't have one.
        ActionTimer action = object != null ? (ActionTimer)object : new ActionTimer("IQDA1", null);
        _tempAction = action;
        
        numActions = action.getNumActions();
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        _startImmediately = new JCheckBox(Bundle.getMessage("ActionTimerSwing_StartImmediately"));
        _runContinuously = new JCheckBox(Bundle.getMessage("ActionTimerSwing_RunContinuously"));
        
        _unitComboBox = new JComboBox<>();
        for (TimerUnit u : TimerUnit.values()) _unitComboBox.addItem(u);
        JComboBoxUtil.setupComboBoxMaxRows(_unitComboBox);
        _unitComboBox.setSelectedItem(action.getUnit());
        
        panel.add(_startImmediately);
        panel.add(_runContinuously);
        panel.add(_unitComboBox);
        
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
        boolean hasErrors = false;
        for (int i=0; i < numActions; i++) {
            if (! _tempAction.getActionSocket(0).validateName(_timerSocketNames[i].getText())) {
                errorMessages.add(Bundle.getMessage("InvalidSocketName", _timerSocketNames[i].getText()));
                hasErrors = true;
            }
        }
        return !hasErrors;
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
        action.setUnit(_unitComboBox.getItemAt(_unitComboBox.getSelectedIndex()));
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
