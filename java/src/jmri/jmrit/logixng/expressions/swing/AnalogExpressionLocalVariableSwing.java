package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.*;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.expressions.AnalogExpressionLocalVariable;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectStringSwing;

/**
 * Configures an AnalogExpressionLocalVariable object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class AnalogExpressionLocalVariableSwing extends AbstractAnalogExpressionSwing {

    private LogixNG_SelectStringSwing _selectLocalVariableSwing;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        AnalogExpressionLocalVariable action = (AnalogExpressionLocalVariable)object;

        panel = new JPanel();

        _selectLocalVariableSwing = new LogixNG_SelectStringSwing(getJDialog(), this);

        JPanel _tabbedPaneNamedBean;
        if (action != null) {
            _tabbedPaneNamedBean = _selectLocalVariableSwing.createPanel(action.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectLocalVariableSwing.createPanel(null);
        }

        panel.add(new JLabel(Bundle.getMessage("AnalogExpressionLocalVariable_LocalVariable")));
        panel.add(_tabbedPaneNamedBean);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        AnalogExpressionLocalVariable expression = new AnalogExpressionLocalVariable("IQAE1", null);
        _selectLocalVariableSwing.validate(expression.getSelectNamedBean(), errorMessages);
        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        AnalogExpressionLocalVariable expression = new AnalogExpressionLocalVariable(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof AnalogExpressionLocalVariable)) {
            throw new IllegalArgumentException("object must be an AnalogExpressionLocalVariable but is a: "+object.getClass().getName());
        }
        AnalogExpressionLocalVariable expression = (AnalogExpressionLocalVariable)object;
        _selectLocalVariableSwing.updateObject(expression.getSelectNamedBean());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("AnalogExpressionLocalVariable_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogExpressionLocalVariableSwing.class);

}
