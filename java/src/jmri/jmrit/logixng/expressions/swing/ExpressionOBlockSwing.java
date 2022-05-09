package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionOBlock;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionOBlock object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionOBlockSwing extends AbstractDigitalExpressionSwing {

    private LogixNG_SelectNamedBeanSwing<OBlock> _selectNamedBeanSwing;

    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;

    private JTabbedPane _tabbedPaneOBlockState;
    private JComboBox<OBlock.OBlockStatus> _stateComboBox;
    private JPanel _panelOBlockStateDirect;
    private JPanel _panelOBlockStateReference;
    private JPanel _panelOBlockStateLocalVariable;
    private JPanel _panelOBlockStateFormula;
    private JTextField _oblockStateReferenceTextField;
    private JTextField _oblockStateLocalVariableTextField;
    private JTextField _oblockStateFormulaTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionOBlock expression = (ExpressionOBlock)object;

        panel = new JPanel();

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(OBlockManager.class), getJDialog(), this);

        JPanel _tabbedPaneNamedBean;
        if (expression != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(expression.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }


        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_is_IsNot_ComboBox);


        _tabbedPaneOBlockState = new JTabbedPane();
        _panelOBlockStateDirect = new javax.swing.JPanel();
        _panelOBlockStateReference = new javax.swing.JPanel();
        _panelOBlockStateLocalVariable = new javax.swing.JPanel();
        _panelOBlockStateFormula = new javax.swing.JPanel();

        _tabbedPaneOBlockState.addTab(NamedBeanAddressing.Direct.toString(), _panelOBlockStateDirect);
        _tabbedPaneOBlockState.addTab(NamedBeanAddressing.Reference.toString(), _panelOBlockStateReference);
        _tabbedPaneOBlockState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelOBlockStateLocalVariable);
        _tabbedPaneOBlockState.addTab(NamedBeanAddressing.Formula.toString(), _panelOBlockStateFormula);

        _stateComboBox = new JComboBox<>();
        for (OBlock.OBlockStatus e : OBlock.OBlockStatus.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);

        _panelOBlockStateDirect.add(_stateComboBox);

        _oblockStateReferenceTextField = new JTextField();
        _oblockStateReferenceTextField.setColumns(30);
        _panelOBlockStateReference.add(_oblockStateReferenceTextField);

        _oblockStateLocalVariableTextField = new JTextField();
        _oblockStateLocalVariableTextField.setColumns(30);
        _panelOBlockStateLocalVariable.add(_oblockStateLocalVariableTextField);

        _oblockStateFormulaTextField = new JTextField();
        _oblockStateFormulaTextField.setColumns(30);
        _panelOBlockStateFormula.add(_oblockStateFormulaTextField);


        if (expression != null) {
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());

            switch (expression.getStateAddressing()) {
                case Direct: _tabbedPaneOBlockState.setSelectedComponent(_panelOBlockStateDirect); break;
                case Reference: _tabbedPaneOBlockState.setSelectedComponent(_panelOBlockStateReference); break;
                case LocalVariable: _tabbedPaneOBlockState.setSelectedComponent(_panelOBlockStateLocalVariable); break;
                case Formula: _tabbedPaneOBlockState.setSelectedComponent(_panelOBlockStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getStateAddressing().name());
            }
            _stateComboBox.setSelectedItem(expression.getBeanState());
            _oblockStateReferenceTextField.setText(expression.getStateReference());
            _oblockStateLocalVariableTextField.setText(expression.getStateLocalVariable());
            _oblockStateFormulaTextField.setText(expression.getStateFormula());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _is_IsNot_ComboBox,
            _tabbedPaneOBlockState};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionOBlock_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionOBlock expression = new ExpressionOBlock("IQDE1", null);

        _selectNamedBeanSwing.validate(expression.getSelectNamedBean(), errorMessages);

        try {
            if (_tabbedPaneOBlockState.getSelectedComponent() == _panelOBlockStateReference) {
                expression.setStateReference(_oblockStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
        }

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
        ExpressionOBlock expression = new ExpressionOBlock(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionOBlock)) {
            throw new IllegalArgumentException("object must be an ExpressionOBlock but is a: "+object.getClass().getName());
        }
        ExpressionOBlock expression = (ExpressionOBlock)object;

        _selectNamedBeanSwing.updateObject(expression.getSelectNamedBean());

        try {
            expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());

            if (_tabbedPaneOBlockState.getSelectedComponent() == _panelOBlockStateDirect) {
                expression.setStateAddressing(NamedBeanAddressing.Direct);
                expression.setBeanState((OBlock.OBlockStatus)_stateComboBox.getSelectedItem());
            } else if (_tabbedPaneOBlockState.getSelectedComponent() == _panelOBlockStateReference) {
                expression.setStateAddressing(NamedBeanAddressing.Reference);
                expression.setStateReference(_oblockStateReferenceTextField.getText());
            } else if (_tabbedPaneOBlockState.getSelectedComponent() == _panelOBlockStateLocalVariable) {
                expression.setStateAddressing(NamedBeanAddressing.LocalVariable);
                expression.setStateLocalVariable(_oblockStateLocalVariableTextField.getText());
            } else if (_tabbedPaneOBlockState.getSelectedComponent() == _panelOBlockStateFormula) {
                expression.setStateAddressing(NamedBeanAddressing.Formula);
                expression.setStateFormula(_oblockStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneOBlockState has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("OBlock_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionOBlockSwing.class);

}
