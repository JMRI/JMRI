package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionTransit;
import jmri.jmrit.logixng.expressions.ExpressionTransit.TransitState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionTransit object with a Swing JPanel.
 *
 * @author Dave Sand         Copyright 2023
 */
public class ExpressionTransitSwing extends AbstractDigitalExpressionSwing {

    private LogixNG_SelectNamedBeanSwing<Transit> _selectNamedBeanSwing;
    private LogixNG_SelectEnumSwing<TransitState> _selectTransitStateSwing;

    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionTransit expression = (ExpressionTransit)object;

        panel = new JPanel();

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(TransitManager.class), getJDialog(), this);

        _selectTransitStateSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        JPanel tabbedPaneNamedBean;
        JPanel tabbedPaneTransitState;

        if (expression != null) {
            tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(expression.getSelectNamedBean());
            tabbedPaneTransitState = _selectTransitStateSwing.createPanel(expression.getSelectEnum(), TransitState.values());
        } else {
            tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
            tabbedPaneTransitState = _selectTransitStateSwing.createPanel(null, TransitState.values());
        }

        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_is_IsNot_ComboBox);

        _selectTransitStateSwing.addAddressingListener(e -> { setDataPanelState(); });
        _selectTransitStateSwing.addEnumListener(e -> { setDataPanelState(); });

        setDataPanelState();

        if (expression != null) {
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());
        }

        JComponent[] components = new JComponent[]{
            tabbedPaneNamedBean,
            _is_IsNot_ComboBox,
            tabbedPaneTransitState};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionTransit_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setDataPanelState() {
//         boolean newState = (_selectTransitStateSwing.getAddressing() != NamedBeanAddressing.Direct)
//                 || (_selectTransitStateSwing.getEnum() == BlockState.ValueMatches);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        ExpressionTransit expression = new ExpressionTransit("IQDE1", null);
        _selectNamedBeanSwing.validate(expression.getSelectNamedBean(), errorMessages);
        _selectTransitStateSwing.validate(expression.getSelectEnum(), errorMessages);
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
        ExpressionTransit expression = new ExpressionTransit(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionTransit)) {
            throw new IllegalArgumentException("object must be an ExpressionBlock but is a: "+object.getClass().getName());
        }

        ExpressionTransit expression = (ExpressionTransit) object;

        _selectNamedBeanSwing.updateObject(expression.getSelectNamedBean());
        _selectTransitStateSwing.updateObject(expression.getSelectEnum());

        expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Transit_Short");
    }

    @Override
    public void dispose() {
        _selectNamedBeanSwing.dispose();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionTransitSwing.class);
}
