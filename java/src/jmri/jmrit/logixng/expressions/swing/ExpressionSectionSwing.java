package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSection;
import jmri.jmrit.logixng.expressions.ExpressionSection.SectionState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionSection object with a Swing JPanel.
 *
 * @author Dave Sand         Copyright 2023
 */
public class ExpressionSectionSwing extends AbstractDigitalExpressionSwing {

    private LogixNG_SelectNamedBeanSwing<Section> _selectNamedBeanSwing;
    private LogixNG_SelectEnumSwing<SectionState> _selectSectionStateSwing;

    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionSection expression = (ExpressionSection)object;

        panel = new JPanel();

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(SectionManager.class), getJDialog(), this);

        _selectSectionStateSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        JPanel tabbedPaneNamedBean;
        JPanel tabbedPaneSectionState;

        if (expression != null) {
            tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(expression.getSelectNamedBean());
            tabbedPaneSectionState = _selectSectionStateSwing.createPanel(expression.getSelectEnum(), SectionState.values());
        } else {
            tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
            tabbedPaneSectionState = _selectSectionStateSwing.createPanel(null, SectionState.values());
        }

        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_is_IsNot_ComboBox);

        _selectSectionStateSwing.addAddressingListener(e -> { setDataPanelState(); });
        _selectSectionStateSwing.addEnumListener(e -> { setDataPanelState(); });

        setDataPanelState();

        if (expression != null) {
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());
        }

        JComponent[] components = new JComponent[]{
            tabbedPaneNamedBean,
            _is_IsNot_ComboBox,
            tabbedPaneSectionState};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionSection_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setDataPanelState() {
//         boolean newState = (_selectSectionStateSwing.getAddressing() != NamedBeanAddressing.Direct)
//                 || (_selectSectionStateSwing.getEnum() == BlockState.ValueMatches);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        ExpressionSection expression = new ExpressionSection("IQDE1", null);
        _selectNamedBeanSwing.validate(expression.getSelectNamedBean(), errorMessages);
        _selectSectionStateSwing.validate(expression.getSelectEnum(), errorMessages);
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
        ExpressionSection expression = new ExpressionSection(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionSection)) {
            throw new IllegalArgumentException("object must be an ExpressionBlock but is a: "+object.getClass().getName());
        }

        ExpressionSection expression = (ExpressionSection) object;

        _selectNamedBeanSwing.updateObject(expression.getSelectNamedBean());
        _selectSectionStateSwing.updateObject(expression.getSelectEnum());

        expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Section_Short");
    }

    @Override
    public void dispose() {
        _selectNamedBeanSwing.dispose();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSectionSwing.class);
}
