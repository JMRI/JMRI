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
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionBlock object with a Swing JPanel.
 *
 * @author Daniel Bergqvist  Copyright 2021
 * @author Dave Sand         Copyright 2021
 */
public class ExpressionBlockSwing extends AbstractDigitalExpressionSwing {

    private LogixNG_SelectNamedBeanSwing<Block> _selectNamedBeanSwing;

    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;

    private JTabbedPane _tabbedPaneBlockState;
    private JComboBox<BlockState> _stateComboBox;
    private JPanel _panelBlockStateDirect;
    private JPanel _panelBlockStateReference;
    private JPanel _panelBlockStateLocalVariable;
    private JPanel _panelBlockStateFormula;
    private JTextField _blockStateReferenceTextField;
    private JTextField _blockStateLocalVariableTextField;
    private JTextField _blockStateFormulaTextField;

    private JTabbedPane _tabbedPaneBlockData;
    private JPanel _panelBlockDataDirect;
    private JPanel _panelBlockDataReference;
    private JPanel _panelBlockDataLocalVariable;
    private JPanel _panelBlockDataFormula;
    private JTextField _blockDataDirectTextField;
    private JTextField _blockDataReferenceTextField;
    private JTextField _blockDataLocalVariableTextField;
    private JTextField _blockDataFormulaTextField;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionBlock expression = (ExpressionBlock)object;

        panel = new JPanel();

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(BlockManager.class), getJDialog(), this);

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


        _tabbedPaneBlockState = new JTabbedPane();
        _panelBlockStateDirect = new javax.swing.JPanel();
        _panelBlockStateReference = new javax.swing.JPanel();
        _panelBlockStateLocalVariable = new javax.swing.JPanel();
        _panelBlockStateFormula = new javax.swing.JPanel();

        _tabbedPaneBlockState.addTab(NamedBeanAddressing.Direct.toString(), _panelBlockStateDirect);
        _tabbedPaneBlockState.addTab(NamedBeanAddressing.Reference.toString(), _panelBlockStateReference);
        _tabbedPaneBlockState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelBlockStateLocalVariable);
        _tabbedPaneBlockState.addTab(NamedBeanAddressing.Formula.toString(), _panelBlockStateFormula);

        _stateComboBox = new JComboBox<>();
        for (BlockState e : BlockState.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);

        _stateComboBox.addActionListener((java.awt.event.ActionEvent e) -> {
            setDataPanelState();
        });
        _panelBlockStateDirect.add(_stateComboBox);

        _blockStateReferenceTextField = new JTextField();
        _blockStateReferenceTextField.setColumns(30);
        _panelBlockStateReference.add(_blockStateReferenceTextField);

        _blockStateLocalVariableTextField = new JTextField();
        _blockStateLocalVariableTextField.setColumns(30);
        _panelBlockStateLocalVariable.add(_blockStateLocalVariableTextField);

        _blockStateFormulaTextField = new JTextField();
        _blockStateFormulaTextField.setColumns(30);
        _panelBlockStateFormula.add(_blockStateFormulaTextField);


        _tabbedPaneBlockData = new JTabbedPane();
        _panelBlockDataDirect = new javax.swing.JPanel();
        _panelBlockDataReference = new javax.swing.JPanel();
        _panelBlockDataLocalVariable = new javax.swing.JPanel();
        _panelBlockDataFormula = new javax.swing.JPanel();

        _tabbedPaneBlockData.addTab(NamedBeanAddressing.Direct.toString(), _panelBlockDataDirect);
        _tabbedPaneBlockData.addTab(NamedBeanAddressing.Reference.toString(), _panelBlockDataReference);
        _tabbedPaneBlockData.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelBlockDataLocalVariable);
        _tabbedPaneBlockData.addTab(NamedBeanAddressing.Formula.toString(), _panelBlockDataFormula);

        _blockDataDirectTextField = new JTextField();
        _blockDataDirectTextField.setColumns(30);
        _panelBlockDataDirect.add(_blockDataDirectTextField);

        _blockDataReferenceTextField = new JTextField();
        _blockDataReferenceTextField.setColumns(30);
        _panelBlockDataReference.add(_blockDataReferenceTextField);

        _blockDataLocalVariableTextField = new JTextField();
        _blockDataLocalVariableTextField.setColumns(30);
        _panelBlockDataLocalVariable.add(_blockDataLocalVariableTextField);

        _blockDataFormulaTextField = new JTextField();
        _blockDataFormulaTextField.setColumns(30);
        _panelBlockDataFormula.add(_blockDataFormulaTextField);


        setDataPanelState();

        if (expression != null) {
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());

            switch (expression.getStateAddressing()) {
                case Direct: _tabbedPaneBlockState.setSelectedComponent(_panelBlockStateDirect); break;
                case Reference: _tabbedPaneBlockState.setSelectedComponent(_panelBlockStateReference); break;
                case LocalVariable: _tabbedPaneBlockState.setSelectedComponent(_panelBlockStateLocalVariable); break;
                case Formula: _tabbedPaneBlockState.setSelectedComponent(_panelBlockStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getStateAddressing().name());
            }
            _stateComboBox.setSelectedItem(expression.getBeanState());
            _blockStateReferenceTextField.setText(expression.getStateReference());
            _blockStateLocalVariableTextField.setText(expression.getStateLocalVariable());
            _blockStateFormulaTextField.setText(expression.getStateFormula());

            switch (expression.getDataAddressing()) {
                case Direct: _tabbedPaneBlockData.setSelectedComponent(_panelBlockDataDirect); break;
                case Reference: _tabbedPaneBlockData.setSelectedComponent(_panelBlockDataReference); break;
                case LocalVariable: _tabbedPaneBlockData.setSelectedComponent(_panelBlockDataLocalVariable); break;
                case Formula: _tabbedPaneBlockData.setSelectedComponent(_panelBlockDataFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getDataAddressing().name());
            }
            _blockDataDirectTextField.setText(expression.getBlockValue());
            _blockDataReferenceTextField.setText(expression.getDataReference());
            _blockDataLocalVariableTextField.setText(expression.getDataLocalVariable());
            _blockDataFormulaTextField.setText(expression.getDataFormula());

            setDataPanelState();
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _is_IsNot_ComboBox,
            _tabbedPaneBlockState,
            _tabbedPaneBlockData};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionBlock_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setDataPanelState() {
        boolean newState = _stateComboBox.getSelectedItem() == BlockState.ValueMatches;
        _tabbedPaneBlockData.setEnabled(newState);
        _blockDataDirectTextField.setEnabled(newState);
        _blockDataReferenceTextField.setEnabled(newState);
        _blockDataLocalVariableTextField.setEnabled(newState);
        _blockDataFormulaTextField.setEnabled(newState);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        ExpressionBlock expression = new ExpressionBlock("IQDE1", null);
        _selectNamedBeanSwing.validate(expression.getSelectNamedBean(), errorMessages);
        validateStateSection(errorMessages);
        validateDataSection(errorMessages);
        return errorMessages.isEmpty();
    }

    private void validateStateSection(List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionBlock expression = new ExpressionBlock("IQDE2", null);

        try {
            if (_tabbedPaneBlockState.getSelectedComponent() == _panelBlockStateReference) {
                expression.setStateReference(_blockStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return;
        }

        try {
            expression.setStateFormula(_blockStateFormulaTextField.getText());
            if (_tabbedPaneBlockState.getSelectedComponent() == _panelBlockStateDirect) {
                expression.setStateAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneBlockState.getSelectedComponent() == _panelBlockStateReference) {
                expression.setStateAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneBlockState.getSelectedComponent() == _panelBlockStateLocalVariable) {
                expression.setStateAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneBlockState.getSelectedComponent() == _panelBlockStateFormula) {
                expression.setStateAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPaneBlockState has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }
    }

    private void validateDataSection(List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionBlock expression = new ExpressionBlock("IQDE3", null);

        try {
            if (_tabbedPaneBlockData.getSelectedComponent() == _panelBlockDataReference) {
                expression.setDataReference(_blockDataReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return;
        }

        try {
            expression.setDataFormula(_blockDataFormulaTextField.getText());
            if (_tabbedPaneBlockData.getSelectedComponent() == _panelBlockDataDirect) {
                expression.setDataAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneBlockData.getSelectedComponent() == _panelBlockDataReference) {
                expression.setDataAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneBlockData.getSelectedComponent() == _panelBlockDataLocalVariable) {
                expression.setDataAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneBlockData.getSelectedComponent() == _panelBlockDataFormula) {
                expression.setDataAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPaneBlockData has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }

        if (_tabbedPaneBlockData.getSelectedComponent() == _panelBlockDataDirect) {
            BlockState state = _stateComboBox.getItemAt(_stateComboBox.getSelectedIndex());
            if (state == BlockState.ValueMatches) {
                if (_blockDataDirectTextField.getText().isEmpty()) {
                    errorMessages.add(Bundle.getMessage("Block_ErrorValue"));
                }
            }
        }
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

        try {
            expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());

            if (_tabbedPaneBlockState.getSelectedComponent() == _panelBlockStateDirect) {
                expression.setStateAddressing(NamedBeanAddressing.Direct);
                expression.setBeanState((BlockState)_stateComboBox.getSelectedItem());
            } else if (_tabbedPaneBlockState.getSelectedComponent() == _panelBlockStateReference) {
                expression.setStateAddressing(NamedBeanAddressing.Reference);
                expression.setStateReference(_blockStateReferenceTextField.getText());
            } else if (_tabbedPaneBlockState.getSelectedComponent() == _panelBlockStateLocalVariable) {
                expression.setStateAddressing(NamedBeanAddressing.LocalVariable);
                expression.setStateLocalVariable(_blockStateLocalVariableTextField.getText());
            } else if (_tabbedPaneBlockState.getSelectedComponent() == _panelBlockStateFormula) {
                expression.setStateAddressing(NamedBeanAddressing.Formula);
                expression.setStateFormula(_blockStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneBlockState has unknown selection");
            }

            if (_tabbedPaneBlockData.getSelectedComponent() == _panelBlockDataDirect) {
                expression.setDataAddressing(NamedBeanAddressing.Direct);
                // Handle optional data field
                if (expression.getBeanState() == BlockState.ValueMatches) {
                    expression.setBlockValue(_blockDataDirectTextField.getText());
                }
            } else if (_tabbedPaneBlockData.getSelectedComponent() == _panelBlockDataReference) {
                expression.setDataAddressing(NamedBeanAddressing.Reference);
                expression.setDataReference(_blockDataReferenceTextField.getText());
            } else if (_tabbedPaneBlockData.getSelectedComponent() == _panelBlockDataLocalVariable) {
                expression.setDataAddressing(NamedBeanAddressing.LocalVariable);
                expression.setDataLocalVariable(_blockDataLocalVariableTextField.getText());
            } else if (_tabbedPaneBlockData.getSelectedComponent() == _panelBlockDataFormula) {
                expression.setDataAddressing(NamedBeanAddressing.Formula);
                expression.setDataFormula(_blockDataFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneBlockData has unknown selection");
            }

        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Block_Short");
    }

    @Override
    public void dispose() {
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionBlockSwing.class);
}
