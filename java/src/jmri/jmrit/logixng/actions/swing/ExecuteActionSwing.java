package jmri.jmrit.logixng.actions.swing;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ExecuteAction;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExecuteAction object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public class ExecuteActionSwing extends AbstractDigitalActionSwing {

    private JComboBox<String> _actionsComboBox;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExecuteAction action = (ExecuteAction)object;

        panel = new JPanel();

        _actionsComboBox = new JComboBox<>();
        _actionsComboBox.addItem("");
        for (MaleDigitalActionSocket bean : InstanceManager.getDefault(DigitalActionManager.class).getNamedBeanSet()) {
            if (bean.getUserName() != null) {
                _actionsComboBox.addItem(bean.getDisplayName());
                if (action != null) {
                    NamedBeanHandle<MaleDigitalActionSocket> handle =
                            action.getSelectNamedBean().getNamedBean();
                    if ((handle != null) && (handle.getName().equals(bean.getDisplayName()))) {
                        _actionsComboBox.setSelectedItem(bean.getDisplayName());
                    }
                }
            }
        }
        JComboBoxUtil.setupComboBoxMaxRows(_actionsComboBox);

        panel.add(_actionsComboBox);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExecuteAction action = new ExecuteAction(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExecuteAction)) {
            throw new IllegalArgumentException("object must be an ExecuteAction but is a: "+object.getClass().getName());
        }

        ExecuteAction action = (ExecuteAction)object;

        String expr = _actionsComboBox.getItemAt(_actionsComboBox.getSelectedIndex());
        if (expr.isEmpty()) action.getSelectNamedBean().removeNamedBean();
        else action.getSelectNamedBean().setNamedBean(expr);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ExecuteAction_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecuteActionSwing.class);

}
