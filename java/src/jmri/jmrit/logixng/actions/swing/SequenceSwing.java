package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.Sequence;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class SequenceSwing extends AbstractDigitalActionSwing {

    private JCheckBox _startImmediately;
    private JCheckBox _runContinuously;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof Sequence)) {
            throw new IllegalArgumentException("object must be an Sequence but is a: "+object.getClass().getName());
        }
        
        Sequence action = (Sequence)object;
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        _startImmediately = new JCheckBox(Bundle.getMessage("SequenceSwing_StartImmediately"));
        _runContinuously = new JCheckBox(Bundle.getMessage("SequenceSwing_RunContinuously"));
        if (action != null) {
            _startImmediately.setSelected(action.getStartImmediately());
            _runContinuously.setSelected(action.getRunContinuously());
        }
        panel.add(_startImmediately);
        panel.add(_runContinuously);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        Sequence action = new Sequence(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof Sequence)) {
            throw new IllegalArgumentException("object must be an Sequence but is a: "+object.getClass().getName());
        }
        
        Sequence action = (Sequence)object;
        action.setStartImmediately(_startImmediately.isSelected());
        action.setRunContinuously(_runContinuously.isSelected());
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Sequence_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}
