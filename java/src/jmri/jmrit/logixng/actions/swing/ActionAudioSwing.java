package jmri.jmrit.logixng.actions.swing;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.Audio;
import jmri.AudioManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionAudio;
import jmri.jmrit.logixng.actions.ActionAudio.Operation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;

/**
 * Configures an ActionAudio object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionAudioSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectNamedBeanSwing<Audio> _selectNamedBeanSwing;
    private LogixNG_SelectEnumSwing<Operation> _selectOperationSwing;


    public ActionAudioSwing() {
    }

    public ActionAudioSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionAudio action = (ActionAudio)object;

        Predicate<Audio> filter = (bean) -> { return bean.getSubType() != Audio.BUFFER; };

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(AudioManager.class), getJDialog(), this);

        _selectOperationSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        panel = new JPanel();

        JPanel _tabbedPaneNamedBean;
        JPanel _tabbedPaneOperation;

        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean(), filter);
            _tabbedPaneOperation = _selectOperationSwing.createPanel(action.getSelectEnum(), Operation.values());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null, filter);
            _tabbedPaneOperation = _selectOperationSwing.createPanel(null, Operation.values());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _tabbedPaneOperation};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionAudio_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionAudio action = new ActionAudio("IQDA1", null);
        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        _selectOperationSwing.validate(action.getSelectEnum(), errorMessages);
        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionAudio action = new ActionAudio(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionAudio)) {
            throw new IllegalArgumentException("object must be an ActionAudio but is a: "+object.getClass().getName());
        }
        ActionAudio action = (ActionAudio)object;
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());
        _selectOperationSwing.updateObject(action.getSelectEnum());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionAudio_Short");
    }

    @Override
    public void dispose() {
        _selectNamedBeanSwing.dispose();
        _selectOperationSwing.dispose();
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionAudioSwing.class);

}
