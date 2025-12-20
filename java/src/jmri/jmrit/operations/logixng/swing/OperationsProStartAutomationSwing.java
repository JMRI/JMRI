package jmri.jmrit.operations.logixng.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.swing.AbstractDigitalActionSwing;
import jmri.jmrit.operations.logixng.OperationsProStartAutomation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.LogixNG_SelectComboBox.Item;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectComboBoxSwing;

/**
 * Configures an OperationsPro_StartAutomation object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public class OperationsProStartAutomationSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectComboBoxSwing _selectAutomationsSwing;


    public OperationsProStartAutomationSwing() {
    }

    public OperationsProStartAutomationSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        OperationsProStartAutomation action = (OperationsProStartAutomation)object;
        if (action == null) {
            // Create a temporary action
            action = new OperationsProStartAutomation("IQDA1", null);
        }

        _selectAutomationsSwing = new LogixNG_SelectComboBoxSwing(getJDialog(), this);

        panel = new JPanel();

        JPanel _tabbedPaneEnum;

        var selectAutomations = action.getSelectAutomations();
        Item[] items = selectAutomations.getValues();
        Item item = items.length > 0 ? selectAutomations.getValue() : null;
        _tabbedPaneEnum = _selectAutomationsSwing.createPanel(selectAutomations, item);

        JComponent[] components = new JComponent[]{_tabbedPaneEnum};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("OperationsProStartAutomation_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        OperationsProStartAutomation action = new OperationsProStartAutomation("IQDA1", null);
        _selectAutomationsSwing.validate(action.getSelectAutomations(), errorMessages);
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
        OperationsProStartAutomation action = new OperationsProStartAutomation(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof OperationsProStartAutomation)) {
            throw new IllegalArgumentException("object must be an OperationsPro_StartAutomation but is a: "+object.getClass().getName());
        }
        OperationsProStartAutomation action = (OperationsProStartAutomation)object;
        _selectAutomationsSwing.updateObject(action.getSelectAutomations());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("OperationsProStartAutomation_Short");
    }

    @Override
    public void dispose() {
        _selectAutomationsSwing.dispose();
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OperationsPro_StartAutomationSwing.class);

}
