package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionEntryExit;
import jmri.jmrit.logixng.actions.ActionEntryExit.Operation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;

/**
 * Configures an EntryExit object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionEntryExitSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectNamedBeanSwing<DestinationPoints> _selectNamedBeanSwing;
    private LogixNG_SelectEnumSwing<Operation> _selectOperationSwing;


    public ActionEntryExitSwing() {
    }

    public ActionEntryExitSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionEntryExit action = (ActionEntryExit)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class), getJDialog(), this);

        _selectOperationSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        panel = new JPanel();

        JPanel _tabbedPaneNamedBean;
        JPanel _tabbedPaneOperation;

        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
            _tabbedPaneOperation = _selectOperationSwing.createPanel(action.getSelectEnum(), Operation.values());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
            _tabbedPaneOperation = _selectOperationSwing.createPanel(null, Operation.values());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _tabbedPaneOperation};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionEntryExit_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionEntryExit action = new ActionEntryExit("IQDA1", null);

        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        _selectOperationSwing.validate(action.getSelectEnum(), errorMessages);

        if (_selectNamedBeanSwing.getAddressing() == NamedBeanAddressing.Direct &&
                 _selectOperationSwing.getAddressing() == NamedBeanAddressing.Direct) {
            DestinationPoints dp = _selectNamedBeanSwing.getBean();
            boolean isReverse = _selectOperationSwing.getEnum() == Operation.SetNXPairReversed;
            if (dp != null && isReverse && dp.isUniDirection()) {
                errorMessages.add(Bundle.getMessage("ActionEntryExit_SetReversedError", dp.getDisplayName()));
            }
        }

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionEntryExit action = new ActionEntryExit(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionEntryExit)) {
            throw new IllegalArgumentException("object must be an TriggerEntryExit but is a: "+object.getClass().getName());
        }
        ActionEntryExit action = (ActionEntryExit)object;
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());
        _selectOperationSwing.updateObject(action.getSelectEnum());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionEntryExit_Short");
    }

    @Override
    public void dispose() {
        _selectNamedBeanSwing.dispose();
        _selectOperationSwing.dispose();
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionEntryExitSwing.class);

}
