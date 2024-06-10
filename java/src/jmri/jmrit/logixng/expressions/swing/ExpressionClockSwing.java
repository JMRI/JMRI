package jmri.jmrit.logixng.expressions.swing;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionClock;
import jmri.jmrit.logixng.expressions.ExpressionClock.Type;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionClock object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class ExpressionClockSwing extends AbstractDigitalExpressionSwing {

    private JComboBox<Type> _stateComboBox;
    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;
    private JTextField _beginTextField;
    private JTextField _endTextField;

    private int _beginMinutes;
    private int _endMinutes;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionClock expression = (ExpressionClock) object;
        panel = new JPanel();

        _stateComboBox = new JComboBox<>();
        for (Type e : Type.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);

        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_is_IsNot_ComboBox);

        _beginTextField = new JTextField(4);
        _beginTextField.setText(ExpressionClock.formatTime(0));


        _endTextField = new JTextField(4);
        _endTextField.setText(ExpressionClock.formatTime(0));

        if (expression != null) {
            _stateComboBox.setSelectedItem(expression.getType());
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());
            _beginTextField.setText(ExpressionClock.formatTime(expression.getBeginTime()));
            _endTextField.setText(ExpressionClock.formatTime(expression.getEndTime()));
        }

        JComponent[] components = new JComponent[]{
            _stateComboBox,
            _is_IsNot_ComboBox,
            _beginTextField,
            _endTextField
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionClock_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        _beginMinutes = validateTime(errorMessages, _beginTextField);
        _endMinutes = validateTime(errorMessages, _endTextField);
        return errorMessages.isEmpty();
    }

    private int validateTime(List<String> errorMessages, JTextField timeField) {
        int minutes = 0;
        try {
            LocalTime newHHMM = LocalTime.parse(timeField.getText().trim(), DateTimeFormatter.ofPattern("H:mm"));

            minutes = newHHMM.getHour() * 60 + newHHMM.getMinute();
            if (minutes < 0 || minutes > 1439) {
                errorMessages.add(Bundle.getMessage("Clock_RangeError"));
            }
        } catch (java.time.format.DateTimeParseException ex) {
            errorMessages.add(Bundle.getMessage("Clock_ParseError", ex.getParsedString()));
        }
        return minutes;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExpressionClock expression = new ExpressionClock(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionClock)) {
            throw new IllegalArgumentException("object must be an ExpressionClock but is a: "+object.getClass().getName());
        }
        ExpressionClock expression = (ExpressionClock) object;
        expression.setType(_stateComboBox.getItemAt(_stateComboBox.getSelectedIndex()));
        expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());
        expression.setRange(_beginMinutes, _endMinutes);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Clock_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrueSwing.class);

}
