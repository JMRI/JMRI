package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.AnalogActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.AnalogActionMemory;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;

/**
 * Configures an ActionMemory object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class AnalogActionMemorySwing extends AbstractAnalogActionSwing {

    private LogixNG_SelectNamedBeanSwing<Memory> _selectNamedBeanSwing;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        AnalogActionMemory action = (AnalogActionMemory)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(MemoryManager.class), getJDialog(), this);

        panel = new JPanel();
        JPanel _tabbedPaneNamedBean;
        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }

        panel.add(new JLabel(Bundle.getMessage("BeanNameMemory")));
        panel.add(_tabbedPaneNamedBean);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        AnalogActionMemory action = new AnalogActionMemory("IQAA1", null);
        updateObject(action);
        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        AnalogActionMemory action = new AnalogActionMemory(systemName, userName);
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());
        return InstanceManager.getDefault(AnalogActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof AnalogActionMemory)) {
            throw new IllegalArgumentException("object must be an ActionMemory but is a: "+object.getClass().getName());
        }
        AnalogActionMemory action = (AnalogActionMemory)object;
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("AnalogActionMemory_Short");
    }

    @Override
    public void dispose() {
        // Do nothing
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogActionMemorySwing.class);

}
