package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionBlock;
import jmri.jmrit.logixng.expressions.ExpressionBlock.BlockState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectStringSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionBlock object with a Swing JPanel.
 *
 * @author Daniel Bergqvist  Copyright 2021
 * @author Dave Sand         Copyright 2021
 */
public class ExpressionBlockSwing extends AbstractDigitalExpressionSwing {

    private LogixNG_SelectNamedBeanSwing<Block> _selectNamedBeanSwing;
    private LogixNG_SelectEnumSwing<BlockState> _selectBlockStateSwing;
    private LogixNG_SelectStringSwing _selectBlockValueSwing;

    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionBlock expression = (ExpressionBlock)object;

        panel = new JPanel();

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(BlockManager.class), getJDialog(), this);

        _selectBlockStateSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        _selectBlockValueSwing = new LogixNG_SelectStringSwing(getJDialog(), this);

        JPanel tabbedPaneNamedBean;
        JPanel tabbedPaneBlockState;
        JPanel tabbedPaneBlockValue;

        if (expression != null) {
            tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(expression.getSelectNamedBean());
            tabbedPaneBlockState = _selectBlockStateSwing.createPanel(expression.getSelectEnum(), BlockState.values());
            tabbedPaneBlockValue = _selectBlockValueSwing.createPanel(expression.getSelectBlockValue());
        } else {
            tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
            tabbedPaneBlockState = _selectBlockStateSwing.createPanel(null, BlockState.values());
            tabbedPaneBlockValue = _selectBlockValueSwing.createPanel(null);
        }

        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_is_IsNot_ComboBox);

        _selectBlockStateSwing.addAddressingListener(e -> { setDataPanelState(); });
        _selectBlockStateSwing.addEnumListener(e -> { setDataPanelState(); });

        setDataPanelState();

        if (expression != null) {
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());
        }

        JComponent[] components = new JComponent[]{
            tabbedPaneNamedBean,
            _is_IsNot_ComboBox,
            tabbedPaneBlockState,
            tabbedPaneBlockValue};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionBlock_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setDataPanelState() {
        boolean newState = (_selectBlockStateSwing.getAddressing() != NamedBeanAddressing.Direct)
                || (_selectBlockStateSwing.getEnum() == BlockState.ValueMatches);
        _selectBlockValueSwing.setEnabled(newState);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        ExpressionBlock expression = new ExpressionBlock("IQDE1", null);
        _selectNamedBeanSwing.validate(expression.getSelectNamedBean(), errorMessages);
        _selectBlockStateSwing.validate(expression.getSelectEnum(), errorMessages);
        _selectBlockValueSwing.validate(expression.getSelectBlockValue(), errorMessages);
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
        ExpressionBlock expression = new ExpressionBlock(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionBlock)) {
            throw new IllegalArgumentException("object must be an ExpressionBlock but is a: "+object.getClass().getName());
        }

        ExpressionBlock expression = (ExpressionBlock) object;

        _selectNamedBeanSwing.updateObject(expression.getSelectNamedBean());
        _selectBlockStateSwing.updateObject(expression.getSelectEnum());
        _selectBlockValueSwing.updateObject(expression.getSelectBlockValue());

        expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Block_Short");
    }

    @Override
    public void dispose() {
        _selectNamedBeanSwing.dispose();
        _selectBlockValueSwing.dispose();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionBlockSwing.class);
}
