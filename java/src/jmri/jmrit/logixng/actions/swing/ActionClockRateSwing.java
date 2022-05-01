package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionClockRate;
import jmri.jmrit.logixng.actions.ActionClockRate.ClockState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectDoubleSwing;

/**
 * Configures an ActionClockRate object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class ActionClockRateSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectEnumSwing<ClockState> _selectEnumSwing;
    private LogixNG_SelectDoubleSwing _selectSpeedSwing;

    private final JLabel labelToWith = new JLabel(Bundle.getMessage("ActionClockRate_LabelTo"));


    public ActionClockRateSwing() {
    }

    public ActionClockRateSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionClockRate action = (ActionClockRate) object;
        if (action == null) {
            // Create a temporary action
            action = new ActionClockRate("IQDA1", null);
        }

        _selectEnumSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);
        _selectSpeedSwing = new LogixNG_SelectDoubleSwing(getJDialog(), this);

        panel = new JPanel();
        JPanel tabbedPaneClockState;
        JPanel tabbedPaneSpeed;

        tabbedPaneClockState = _selectEnumSwing.createPanel(action.getSelectEnum(), ClockState.values());
        tabbedPaneSpeed = _selectSpeedSwing.createPanel(action.getSelectSpeed());

        JComponent[] components = new JComponent[]{
            tabbedPaneClockState,
            labelToWith,
            tabbedPaneSpeed};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionClock_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionClockRate action = new ActionClockRate("IQDA1", null);

        _selectEnumSwing.validate(action.getSelectEnum(), errorMessages);
        _selectSpeedSwing.validate(action.getSelectSpeed(), errorMessages);

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public String getAutoSystemName() {
        return InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionClockRate action = new ActionClockRate(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionClockRate)) {
            throw new IllegalArgumentException("object must be an ActionClockRate but is a: "+object.getClass().getName());
        }
        ActionClockRate action = (ActionClockRate) object;

        _selectEnumSwing.updateObject(action.getSelectEnum());
        _selectSpeedSwing.updateObject(action.getSelectSpeed());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionClockRate_Short");
    }

    @Override
    public void dispose() {
        _selectEnumSwing.dispose();
        _selectSpeedSwing.dispose();
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionClockRateSwing.class);

}
