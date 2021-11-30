package jmri.jmrit.logixng.expressions.swing;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.TimeSinceMidnight;
import jmri.jmrit.logixng.expressions.TimeSinceMidnight.Type;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an TimeSinceMidnight object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class TimeSinceMidnightSwing extends AbstractAnalogExpressionSwing {

    private JComboBox<Type> _stateComboBox;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        TimeSinceMidnight expression = (TimeSinceMidnight) object;
        panel = new JPanel();

        _stateComboBox = new JComboBox<>();
        for (Type e : Type.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);

        if (expression != null) {
            _stateComboBox.setSelectedItem(expression.getType());
        }

        JComponent[] components = new JComponent[]{
            _stateComboBox
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("TimeSinceMidnight_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        TimeSinceMidnight expression = new TimeSinceMidnight(systemName, userName);
        expression.setType(TimeSinceMidnight.Type.FastClock);
        return InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof TimeSinceMidnight)) {
            throw new IllegalArgumentException("object must be an TimeSinceMidnight but is a: "+object.getClass().getName());
        }
        TimeSinceMidnight expression = (TimeSinceMidnight) object;
        expression.setType(_stateComboBox.getItemAt(_stateComboBox.getSelectedIndex()));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("TimeSinceMidnight_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrueSwing.class);

}
