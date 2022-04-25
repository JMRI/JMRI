package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionTurnoutLock;
import jmri.jmrit.logixng.actions.ActionTurnoutLock.TurnoutLock;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;

/**
 * Configures an ActionTurnoutLock object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionTurnoutLockSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectNamedBeanSwing<Turnout> _selectNamedBeanSwing;
    private LogixNG_SelectEnumSwing<TurnoutLock> _selectEnumSwing;


    public ActionTurnoutLockSwing() {
    }

    public ActionTurnoutLockSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionTurnoutLock action = (ActionTurnoutLock)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(TurnoutManager.class), getJDialog(), this);

        _selectEnumSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        panel = new JPanel();

        JPanel _tabbedPaneNamedBean;
        JPanel _tabbedPaneEnum;

        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
            _tabbedPaneEnum = _selectEnumSwing.createPanel(action.getSelectEnum(), TurnoutLock.values());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
            _tabbedPaneEnum = _selectEnumSwing.createPanel(null, TurnoutLock.values());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _tabbedPaneEnum};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionTurnoutLock_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionTurnoutLock action = new ActionTurnoutLock("IQDA1", null);

        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        _selectEnumSwing.validate(action.getSelectEnum(), errorMessages);

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionTurnoutLock action = new ActionTurnoutLock(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionTurnoutLock)) {
            throw new IllegalArgumentException("object must be an ActionTurnoutLock but is a: "+object.getClass().getName());
        }
        ActionTurnoutLock action = (ActionTurnoutLock)object;
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());
        _selectEnumSwing.updateObject(action.getSelectEnum());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("TurnoutLock_Short");
    }

    @Override
    public void dispose() {
        _selectNamedBeanSwing.dispose();
        _selectEnumSwing.dispose();
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTurnoutLockSwing.class);

}
