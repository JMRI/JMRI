package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSensorEdge;
import jmri.jmrit.logixng.expressions.ExpressionSensorEdge.SensorState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;

/**
 * Configures an ExpressionSensorEdge object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ExpressionSensorEdgeSwing extends AbstractDigitalExpressionSwing {

    private LogixNG_SelectNamedBeanSwing<Sensor> _selectNamedBeanSwing;
    private LogixNG_SelectEnumSwing<SensorState> _selectEnumFromStateSwing;
    private LogixNG_SelectEnumSwing<SensorState> _selectEnumToStateSwing;
    private JCheckBox _onlyTrueOnceCheckBox;


    public ExpressionSensorEdgeSwing() {
    }

    public ExpressionSensorEdgeSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionSensorEdge expression = (ExpressionSensorEdge)object;

        if (expression == null) {
            // Create a temporary expression since only direct addressing is allowed
            expression = new ExpressionSensorEdge("IQDE1", null);
        }

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel innerPanel = new JPanel();

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(SensorManager.class), getJDialog(), this);

        _selectEnumFromStateSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);
        _selectEnumToStateSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);
        _onlyTrueOnceCheckBox = new JCheckBox(Bundle.getMessage("ExpressionSensorEdge_OnlyTrueOnce"));

        JPanel _tabbedPaneNamedBean;
        JPanel _tabbedPaneEnumFromState;
        JPanel _tabbedPaneEnumToState;

        _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(expression.getSelectNamedBean());
        _tabbedPaneEnumFromState = _selectEnumFromStateSwing.createPanel(expression.getSelectEnumFromState(), SensorState.values());
        _tabbedPaneEnumToState = _selectEnumToStateSwing.createPanel(expression.getSelectEnumToState(), SensorState.values());
        _onlyTrueOnceCheckBox.setSelected(expression.getOnlyTrueOnce());


        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _tabbedPaneEnumFromState,
            _tabbedPaneEnumToState};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionSensorEdge_Components"), components);

        for (JComponent c : componentList) innerPanel.add(c);

        panel.add(innerPanel);
        panel.add(_onlyTrueOnceCheckBox);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionSensorEdge expression = new ExpressionSensorEdge("IQDE1", null);

        _selectNamedBeanSwing.validate(expression.getSelectNamedBean(), errorMessages);
        _selectEnumFromStateSwing.validate(expression.getSelectEnumFromState(), errorMessages);
        _selectEnumToStateSwing.validate(expression.getSelectEnumToState(), errorMessages);

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
        ExpressionSensorEdge expression = new ExpressionSensorEdge(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionSensorEdge)) {
            throw new IllegalArgumentException("object must be an ExpressionSensorEdge but is a: "+object.getClass().getName());
        }
        ExpressionSensorEdge expression = (ExpressionSensorEdge)object;

        _selectNamedBeanSwing.updateObject(expression.getSelectNamedBean());
        _selectEnumFromStateSwing.updateObject(expression.getSelectEnumFromState());
        _selectEnumToStateSwing.updateObject(expression.getSelectEnumToState());
        expression.setOnlyTrueOnce(_onlyTrueOnceCheckBox.isSelected());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("SensorEdge_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSensorEdgeSwing.class);

}
