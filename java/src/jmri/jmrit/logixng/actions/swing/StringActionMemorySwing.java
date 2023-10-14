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
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.StringActionMemory;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;

/**
 * Configures an ActionMemory object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class StringActionMemorySwing extends AbstractStringActionSwing {

    private LogixNG_SelectNamedBeanSwing<Memory> _selectNamedBeanSwing;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        StringActionMemory action = (StringActionMemory)object;

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
        StringActionMemory action = new StringActionMemory("IQSA1", null);
        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        StringActionMemory action = new StringActionMemory(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(StringActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof StringActionMemory)) {
            throw new IllegalArgumentException("object must be an ActionMemory but is a: "+object.getClass().getName());
        }
        StringActionMemory action = (StringActionMemory)object;
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("StringActionMemory_Short");
    }

    @Override
    public void dispose() {
        // Do nothing
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringActionMemorySwing.class);

}
