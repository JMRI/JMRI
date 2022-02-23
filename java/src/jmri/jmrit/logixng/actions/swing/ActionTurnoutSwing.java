package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionTurnout;
import jmri.jmrit.logixng.actions.ActionTurnout.TurnoutState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionTurnoutSwing extends AbstractDigitalActionSwing {

    private final LogixNG_SelectNamedBeanSwing<Turnout> _selectNamedBeanSwing =
            new LogixNG_SelectNamedBeanSwing<>(InstanceManager.getDefault(TurnoutManager.class));

    private final LogixNG_SelectEnumSwing<TurnoutState> _selectEnumSwing =
            new LogixNG_SelectEnumSwing<>();


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionTurnout action = (ActionTurnout)object;

        panel = new JPanel();

        JPanel _tabbedPaneTurnout;
        JPanel _tabbedPaneTurnoutState;

        if (action != null) {
            _tabbedPaneTurnout = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
            _tabbedPaneTurnoutState = _selectEnumSwing.createPanel(action.getSelectEnum(), TurnoutState.values());
        } else {
            _tabbedPaneTurnout = _selectNamedBeanSwing.createPanel(null);
            _tabbedPaneTurnoutState = _selectEnumSwing.createPanel(null, TurnoutState.values());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneTurnout,
            _tabbedPaneTurnoutState};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionTurnout_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionTurnout action = new ActionTurnout("IQDA1", null);

        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        _selectEnumSwing.validate(action.getSelectEnum(), errorMessages);

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionTurnout action = new ActionTurnout(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionTurnout)) {
            throw new IllegalArgumentException("object must be an ActionTurnout but is a: "+object.getClass().getName());
        }
        ActionTurnout action = (ActionTurnout)object;
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());
        _selectEnumSwing.updateObject(action.getSelectEnum());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Turnout_Short");
    }

    @Override
    public void dispose() {
        _selectNamedBeanSwing.dispose();
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTurnoutSwing.class);

}
