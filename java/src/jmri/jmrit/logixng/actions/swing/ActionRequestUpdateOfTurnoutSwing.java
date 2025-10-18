package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionRequestUpdateOfTurnout;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;

/**
 * Configures an ActionRequestUpdateOfTurnout object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public class ActionRequestUpdateOfTurnoutSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectNamedBeanSwing<Turnout> _selectNamedBeanSwing;


    public ActionRequestUpdateOfTurnoutSwing() {
    }

    public ActionRequestUpdateOfTurnoutSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionRequestUpdateOfTurnout action = (ActionRequestUpdateOfTurnout)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(TurnoutManager.class), getJDialog(), this);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel innerPanel = new JPanel();

        JPanel _tabbedPaneNamedBean;

        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionRequestUpdateOfTurnout_Components"), components);

        for (JComponent c : componentList) innerPanel.add(c);

        JPanel infoPanel = new JPanel();
        infoPanel.add(new JLabel(Bundle.getMessage("ActionRequestUpdateOfTurnout_Info")));

        panel.add(innerPanel);
        panel.add(infoPanel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionRequestUpdateOfTurnout action = new ActionRequestUpdateOfTurnout("IQDA1", null);

        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);

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
        ActionRequestUpdateOfTurnout action = new ActionRequestUpdateOfTurnout(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionRequestUpdateOfTurnout)) {
            throw new IllegalArgumentException("object must be an ActionRequestUpdateOfTurnout but is a: "+object.getClass().getName());
        }
        ActionRequestUpdateOfTurnout action = (ActionRequestUpdateOfTurnout)object;
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionRequestUpdateOfTurnout_Short");
    }

    @Override
    public void dispose() {
        _selectNamedBeanSwing.dispose();
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionRequestUpdateOfTurnoutSwing.class);

}
