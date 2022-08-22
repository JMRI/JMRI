package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.expressions.Or;
import jmri.jmrit.logixng.expressions.Or.Type;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionTurnout object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class OrSwing extends AbstractDigitalExpressionSwing {

    private JComboBox<Type> _typeComboBox;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof Or)) {
            throw new IllegalArgumentException("object must be an Or but is a: "+object.getClass().getName());
        }

        Or action = (Or)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        _typeComboBox = new JComboBox<>();
        for (Type type : Type.values()) _typeComboBox.addItem(type);
        JComboBoxUtil.setupComboBoxMaxRows(_typeComboBox);
        if (action != null) _typeComboBox.setSelectedItem(action.getType());

        JPanel typePanel = new JPanel();
        typePanel.add(_typeComboBox);
        panel.add(typePanel);

        JPanel labelPanel = new JPanel();
        labelPanel.add(new JLabel(Bundle.getMessage("Or_Info")));
        panel.add(labelPanel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        Or expression = new Or(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof Or)) {
            throw new IllegalArgumentException("object must be an Or but is a: "+object.getClass().getName());
        }

        Or expression = (Or)object;

        expression.setType(_typeComboBox.getItemAt(_typeComboBox.getSelectedIndex()));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Or_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrSwing.class);

}
