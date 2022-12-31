package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionBlock;
import jmri.jmrit.logixng.actions.ActionBlock.DirectOperation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.*;


/**
 * Configures an ActionBlock object with a Swing JPanel.
 *
 * @author Daniel Bergqvist  Copyright 2021
 * @author Dave Sand         Copyright 2021
 */
public class ActionBlockSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectNamedBeanSwing<Block> _selectNamedBeanSwing;
    private LogixNG_SelectEnumSwing<DirectOperation> _selectOperationSwing;
    private LogixNG_SelectStringSwing _selectBlockValueSwing;


    public ActionBlockSwing() {
    }

    public ActionBlockSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionBlock action = (ActionBlock)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(BlockManager.class), getJDialog(), this);

        _selectOperationSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        _selectBlockValueSwing = new LogixNG_SelectStringSwing(getJDialog(), this);

       panel = new JPanel();

        JPanel tabbedPaneNamedBean;
        JPanel tabbedPaneOperation;
        JPanel tabbedPaneBlockValue;

        if (action != null) {
            tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
            tabbedPaneOperation = _selectOperationSwing.createPanel(action.getSelectEnum(), DirectOperation.values());
            tabbedPaneBlockValue = _selectBlockValueSwing.createPanel(action.getSelectBlockValue());
        } else {
            tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
            tabbedPaneOperation = _selectOperationSwing.createPanel(null, DirectOperation.values());
            tabbedPaneBlockValue = _selectBlockValueSwing.createPanel(null);
        }

        _selectOperationSwing.addAddressingListener((evt) -> { setDataPanelState(); });
        _selectOperationSwing.addEnumListener((evt) -> { setDataPanelState(); });

        setDataPanelState();

        JComponent[] components = new JComponent[]{
            tabbedPaneNamedBean,
            tabbedPaneOperation,
            tabbedPaneBlockValue};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionBlock_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setDataPanelState() {
        boolean newState = _selectOperationSwing
                .isEnumSelectedOrIndirectAddressing(DirectOperation.SetValue);
        _selectBlockValueSwing.setEnabled(newState);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {

        ActionBlock action = new ActionBlock("IQDA2", null);
        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        _selectOperationSwing.validate(action.getSelectEnum(), errorMessages);
        _selectBlockValueSwing.validate(action.getSelectBlockValue(), errorMessages);

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionBlock action = new ActionBlock(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionBlock)) {
            throw new IllegalArgumentException("object must be an ActionBlock but is a: "+object.getClass().getName());
        }
        ActionBlock action = (ActionBlock) object;

        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());
        _selectOperationSwing.updateObject(action.getSelectEnum());
        _selectBlockValueSwing.updateObject(action.getSelectBlockValue());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionBlock_Short");
    }

    @Override
    public void dispose() {
        _selectNamedBeanSwing.dispose();
        _selectOperationSwing.dispose();
        _selectBlockValueSwing.dispose();
    }


//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionBlockSwing.class);

}
