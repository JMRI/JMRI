package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.Timeout;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.TimerUnit;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectIntegerSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;

/**
 * Configures an Timeout object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class TimeoutSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectIntegerSwing _selectDelaySwing;
    private LogixNG_SelectEnumSwing<TimerUnit> _selectTimerUnitSwing;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof Timeout)) {
            throw new IllegalArgumentException("object must be an Timeout but is a: "+object.getClass().getName());
        }

        Timeout action = (Timeout)object;

        _selectDelaySwing = new LogixNG_SelectIntegerSwing(getJDialog(), this);
        _selectTimerUnitSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        panel = new JPanel();

        JPanel _tabbedPaneDelay;
        JPanel _tabbedPaneTimerUnit;

        if (action != null) {
            _tabbedPaneDelay = _selectDelaySwing.createPanel(action.getSelectDelay());
            _tabbedPaneTimerUnit = _selectTimerUnitSwing.createPanel(action.getSelectTimerUnit(), TimerUnit.values());
        } else {
            _tabbedPaneDelay = _selectDelaySwing.createPanel(null);
            _tabbedPaneTimerUnit = _selectTimerUnitSwing.createPanel(null, TimerUnit.values());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneDelay,
            _tabbedPaneTimerUnit};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("Timeout_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        Timeout action = new Timeout("IQDA1", null);

        _selectDelaySwing.validate(action.getSelectDelay(), errorMessages);
        _selectTimerUnitSwing.validate(action.getSelectTimerUnit(), errorMessages);

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        Timeout action = new Timeout(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof Timeout)) {
            throw new IllegalArgumentException("object must be an Timeout but is a: "+object.getClass().getName());
        }
        Timeout action = (Timeout)object;
        _selectDelaySwing.updateObject(action.getSelectDelay());
        _selectTimerUnitSwing.updateObject(action.getSelectTimerUnit());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Timeout_Short");
    }

    @Override
    public void dispose() {
        _selectDelaySwing.dispose();
        _selectTimerUnitSwing.dispose();
    }

}
