package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.StringIO;
import jmri.StringIOManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.StringActionStringIO;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;

/**
 * Configures an StringActionStringIO object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class StringActionStringIOSwing extends AbstractStringActionSwing {

    private LogixNG_SelectNamedBeanSwing<StringIO> _selectNamedBeanSwing;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        StringActionStringIO action = (StringActionStringIO)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(StringIOManager.class), getJDialog(), this);

        panel = new JPanel();

        JPanel _tabbedPaneNamedBean;
        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }

        panel.add(new JLabel(Bundle.getMessage("BeanNameStringIO")));
        panel.add(_tabbedPaneNamedBean);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        StringActionStringIO action = new StringActionStringIO("IQSA1", null);
        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        StringActionStringIO action = new StringActionStringIO(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(StringActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof StringActionStringIO)) {
            throw new IllegalArgumentException("object must be an StringActionStringIO but is a: "+object.getClass().getName());
        }
        StringActionStringIO action = (StringActionStringIO)object;
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("StringActionStringIO_Short");
    }

    @Override
    public void dispose() {
        // Do nothing
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringActionStringIOSwing.class);

}
