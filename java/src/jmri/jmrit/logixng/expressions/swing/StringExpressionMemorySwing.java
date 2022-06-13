package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.*;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.StringExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.expressions.StringExpressionMemory;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;

/**
 * Configures an StringExpressionMemory object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class StringExpressionMemorySwing extends AbstractStringExpressionSwing {

    private LogixNG_SelectNamedBeanSwing<Memory> _selectNamedBeanSwing;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        StringExpressionMemory action = (StringExpressionMemory)object;

        panel = new JPanel();

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(MemoryManager.class), getJDialog(), this);

        JPanel _tabbedPaneNamedBean;
        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }

        panel.add(new JLabel(Bundle.getMessage("StringExpressionMemory_Short")));
        panel.add(_tabbedPaneNamedBean);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        StringExpressionMemory expression = new StringExpressionMemory("IQSE1", null);
        _selectNamedBeanSwing.validate(expression.getSelectNamedBean(), errorMessages);
        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        StringExpressionMemory expression = new StringExpressionMemory(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(StringExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof StringExpressionMemory)) {
            throw new IllegalArgumentException("object must be an StringExpressionMemory but is a: "+object.getClass().getName());
        }
        StringExpressionMemory expression = (StringExpressionMemory)object;
        _selectNamedBeanSwing.updateObject(expression.getSelectNamedBean());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("StringExpressionMemory_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringExpressionMemorySwing.class);

}
