package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.expressions.ExpressionSensor.SensorState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionSensor object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionSensorSwing extends AbstractDigitalExpressionSwing {

    private LogixNG_SelectNamedBeanSwing<Sensor> _selectNamedBeanSwing;
    private LogixNG_SelectEnumSwing<SensorState> _selectEnumSwing;

    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;


    public ExpressionSensorSwing() {
    }

    public ExpressionSensorSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionSensor expression = (ExpressionSensor)object;

        panel = new JPanel();

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(SensorManager.class), getJDialog(), this);

        _selectEnumSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        JPanel _tabbedPaneNamedBean;
        JPanel _tabbedPaneEnum;

        if (expression != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(expression.getSelectNamedBean());
            _tabbedPaneEnum = _selectEnumSwing.createPanel(expression.getSelectEnum(), SensorState.values());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
            _tabbedPaneEnum = _selectEnumSwing.createPanel(null, SensorState.values());
        }


        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_is_IsNot_ComboBox);


        if (expression != null) {
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _is_IsNot_ComboBox,
            _tabbedPaneEnum};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionSensor_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionSensor expression = new ExpressionSensor("IQDE1", null);

        _selectNamedBeanSwing.validate(expression.getSelectNamedBean(), errorMessages);
        _selectEnumSwing.validate(expression.getSelectEnum(), errorMessages);

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public String getAutoSystemName() {
        return InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExpressionSensor expression = new ExpressionSensor(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionSensor)) {
            throw new IllegalArgumentException("object must be an ExpressionSensor but is a: "+object.getClass().getName());
        }
        ExpressionSensor expression = (ExpressionSensor)object;

        _selectNamedBeanSwing.updateObject(expression.getSelectNamedBean());
        _selectEnumSwing.updateObject(expression.getSelectEnum());

        expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Sensor_Short");
    }

    @Override
    public void dispose() {
        _selectNamedBeanSwing.dispose();
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSensorSwing.class);

}
