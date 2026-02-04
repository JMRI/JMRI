package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionPower;
import jmri.jmrit.logixng.actions.ActionPower.PowerState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;

/**
 * Configures an ActionPower object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionPowerSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectEnumSwing<PowerState> _selectEnumSwing;


    public ActionPowerSwing() {
    }

    public ActionPowerSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionPower action = (ActionPower)object;

        _selectEnumSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        panel = new JPanel();

        JPanel _tabbedPaneOperation;

        if (action != null) {
            _tabbedPaneOperation = _selectEnumSwing.createPanel(action.getSelectEnum(), PowerState.values());
        } else {
            _tabbedPaneOperation = _selectEnumSwing.createPanel(null, PowerState.values());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneOperation};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionPower_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionPower action = new ActionPower("IQDA1", null);
        _selectEnumSwing.validate(action.getSelectEnum(), errorMessages);
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
        ActionPower action = new ActionPower(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionPower)) {
            throw new IllegalArgumentException("object must be an ActionPower but is a: "+object.getClass().getName());
        }
        ActionPower action = (ActionPower)object;
        _selectEnumSwing.updateObject(action.getSelectEnum());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Power_Short");
    }

    @Override
    public void dispose() {
        _selectEnumSwing.dispose();
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionPowerSwing.class);

}
