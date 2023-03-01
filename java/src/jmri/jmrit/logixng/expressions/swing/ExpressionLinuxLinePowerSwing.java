package jmri.jmrit.logixng.expressions.swing;

import java.io.IOException;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionLinuxLinePower;
import jmri.jmrit.logixng.expressions.ExpressionLinuxLinePower.NoPowerSuppliesException;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionLinuxLinePower object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright (C) 2023
 */
public class ExpressionLinuxLinePowerSwing extends AbstractDigitalExpressionSwing {

    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;


    public ExpressionLinuxLinePowerSwing() {
    }

    public ExpressionLinuxLinePowerSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionLinuxLinePower expression = (ExpressionLinuxLinePower)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_is_IsNot_ComboBox);

        if (expression != null) {
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());
        }

        JComponent[] components = new JComponent[]{
            _is_IsNot_ComboBox
        };

        JPanel innerPanel = new JPanel();
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionLinuxLinePower_Components"), components);

        for (JComponent c : componentList) innerPanel.add(c);

        panel.add(innerPanel);

        try {
            ExpressionLinuxLinePower tempExpression = new ExpressionLinuxLinePower("IQDE1", null);
            tempExpression.isLinePowerOnline();
        } catch (NoPowerSuppliesException e) {
            JPanel p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("ExpressionLinuxLinePower_NoPowerSuppliesFound")));
            panel.add(p);
        } catch (IOException | JmriException e) {
            JPanel p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("ExpressionLinuxLinePower_Error")));
            panel.add(p);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExpressionLinuxLinePower expression = new ExpressionLinuxLinePower(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionLinuxLinePower)) {
            throw new IllegalArgumentException("object must be an ExpressionLinuxLinePower but is a: "+object.getClass().getName());
        }
        ExpressionLinuxLinePower expression = (ExpressionLinuxLinePower)object;

        expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("LinuxLinePower_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionLinuxLinePowerSwing.class);

}
