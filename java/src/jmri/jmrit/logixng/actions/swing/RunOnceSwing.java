package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.RunOnce;

/**
 * Configures an RunOnce object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class RunOnceSwing extends AbstractDigitalActionSwing {

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof RunOnce)) {
            throw new IllegalArgumentException("object must be an RunOnce but is a: "+object.getClass().getName());
        }

        panel = new JPanel();
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        RunOnce action = new RunOnce(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof RunOnce)) {
            throw new IllegalArgumentException("object must be an RunOnce but is a: "+object.getClass().getName());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("RunOnce_Short");
    }

    @Override
    public void dispose() {
    }

}
