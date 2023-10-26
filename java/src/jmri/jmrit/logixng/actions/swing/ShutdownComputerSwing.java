package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.ShutdownComputer;
import jmri.jmrit.logixng.actions.ShutdownComputer.Operation;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ShutdownComputerSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectEnumSwing<Operation> _selectOperationSwing;


    public ShutdownComputerSwing() {
    }

    public ShutdownComputerSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ShutdownComputer action = (ShutdownComputer)object;

        _selectOperationSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        JPanel _tabbedPaneOperation;
        if (action != null) {
            _tabbedPaneOperation = _selectOperationSwing.createPanel(action.getSelectEnum(), Operation.values());
        } else {
            _tabbedPaneOperation = _selectOperationSwing.createPanel(null, Operation.values(), Operation.ShutdownJMRI);
        }

        panel = new JPanel();
        panel.add(_tabbedPaneOperation);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ShutdownComputer action = new ShutdownComputer("IQDA1", null);
        _selectOperationSwing.validate(action.getSelectEnum(), errorMessages);
        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ShutdownComputer action = new ShutdownComputer(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ShutdownComputer)) {
            throw new IllegalArgumentException("object must be an ShutdownComputer but is a: "+object.getClass().getName());
        }
        ShutdownComputer action = (ShutdownComputer)object;
        _selectOperationSwing.updateObject(action.getSelectEnum());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ShutdownComputer_Short");
    }

    @Override
    public void dispose() {
        _selectOperationSwing.dispose();
    }

}
