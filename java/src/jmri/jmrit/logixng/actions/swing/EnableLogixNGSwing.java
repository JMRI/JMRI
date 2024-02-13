package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.EnableLogixNG;
import jmri.jmrit.logixng.actions.EnableLogixNG.Operation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;

/**
 * Configures an EnableLogixNG object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public class EnableLogixNGSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectNamedBeanSwing<LogixNG> _selectNamedBeanSwing;
    private LogixNG_SelectEnumSwing<Operation> _selectOperationSwing;


    public EnableLogixNGSwing() {
    }

    public EnableLogixNGSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        EnableLogixNG action = (EnableLogixNG)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(LogixNG_Manager.class), getJDialog(), this);

        _selectOperationSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel innerPanel = new JPanel();

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
                Bundle.getMessage("EnableLogixNG_Components"), components);

        for (JComponent c : componentList) innerPanel.add(c);

        panel.add(innerPanel);

        JPanel infoPanel1 = new JPanel();
        infoPanel1.add(new JLabel(Bundle.getMessage("EnableLogixNG_Info1")));
        panel.add(infoPanel1);

        JPanel infoPanel2 = new JPanel();
        infoPanel2.add(new JLabel(Bundle.getMessage("EnableLogixNG_Info2")));
        panel.add(infoPanel2);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        EnableLogixNG action = new EnableLogixNG("IQDA1", null);

        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        _selectOperationSwing.validate(action.getSelectEnum(), errorMessages);

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        EnableLogixNG action = new EnableLogixNG(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof EnableLogixNG)) {
            throw new IllegalArgumentException("object must be an EnableLogixNG but is a: "+object.getClass().getName());
        }
        EnableLogixNG action = (EnableLogixNG)object;
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());
        _selectOperationSwing.updateObject(action.getSelectEnum());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("EnableLogixNG_Short");
    }

    @Override
    public void dispose() {
        _selectNamedBeanSwing.dispose();
        _selectOperationSwing.dispose();
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EnableLogixSwing.class);

}
