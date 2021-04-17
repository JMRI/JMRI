package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionBlock;
import jmri.jmrit.logixng.expressions.ExpressionBlock.BlockState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionBlock object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist  Copyright 2021
 * @author Dave Sand         Copyright 2021
 */
public class ExpressionBlockSwing extends AbstractDigitalExpressionSwing {

    private JTabbedPane _tabbedPaneBlock;
    private BeanSelectPanel<Block> _blockBeanPanel;
    private JPanel _panelBlockDirect;
    private JPanel _panelBlockReference;
    private JPanel _panelBlockLocalVariable;
    private JPanel _panelBlockFormula;
    private JTextField _blockReferenceTextField;
    private JTextField _blockLocalVariableTextField;
    private JTextField _blockFormulaTextField;

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

    private JPanel _panelStateCombo;
    private JPanel _panelBlockConstant;
    private JPanel _panelMatchMemory;
    private JTextField _blockConstantTextField;
    private BeanSelectPanel<Memory> _blockMemoryBeanPanel;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionBlock expression = (ExpressionBlock)object;

        panel = new JPanel();

        _tabbedPaneBlock = new JTabbedPane();
        _panelBlockDirect = new javax.swing.JPanel();
        _panelBlockReference = new javax.swing.JPanel();
        _panelBlockLocalVariable = new javax.swing.JPanel();
        _panelBlockFormula = new javax.swing.JPanel();

        _tabbedPaneBlock.addTab(NamedBeanAddressing.Direct.toString(), _panelBlockDirect);
        _tabbedPaneBlock.addTab(NamedBeanAddressing.Reference.toString(), _panelBlockReference);
        _tabbedPaneBlock.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelBlockLocalVariable);
        _tabbedPaneBlock.addTab(NamedBeanAddressing.Formula.toString(), _panelBlockFormula);

        _blockBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(BlockManager.class), null);
        _panelBlockDirect.add(_blockBeanPanel);

        _blockReferenceTextField = new JTextField();
        _blockReferenceTextField.setColumns(30);
        _panelBlockReference.add(_blockReferenceTextField);

        _blockLocalVariableTextField = new JTextField();
        _blockLocalVariableTextField.setColumns(30);
        _panelBlockLocalVariable.add(_blockLocalVariableTextField);

        _blockFormulaTextField = new JTextField();
        _blockFormulaTextField.setColumns(30);
        _panelBlockFormula.add(_blockFormulaTextField);


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

        // Create an empty strut panel that sits to the left of the container
        JPanel directSpacer = new JPanel();
        directSpacer.setLayout(new BoxLayout(directSpacer, BoxLayout.Y_AXIS));
        directSpacer.add(Box.createVerticalStrut(100));

        // The container holds 3 panels, two of which are based on selections
        JPanel directContainer = new JPanel();
        directContainer.setLayout(new BoxLayout(directContainer, BoxLayout.Y_AXIS));

        _panelStateCombo = new JPanel();
        _panelBlockConstant = new JPanel();
        _panelMatchMemory = new JPanel();

        _panelBlockConstant.setVisible(false);
        _panelMatchMemory.setVisible(false);

        // Populate the container sub-panels
        _stateComboBox = new JComboBox<>();
        for (BlockState e : BlockState.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);

        _stateComboBox.addActionListener((java.awt.event.ActionEvent e) -> {
            setPanelDetailVisibility();
        });
        _panelStateCombo.add(_stateComboBox);

        _blockConstantTextField = new JTextField();
        _blockConstantTextField.setColumns(25);
        _panelBlockConstant.add(_blockConstantTextField);

        _blockMemoryBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _panelMatchMemory.add(_blockMemoryBeanPanel);

        directContainer.add(_panelStateCombo);
        directContainer.add(_panelBlockConstant);
        directContainer.add(_panelMatchMemory);

        _panelBlockStateDirect.add(directSpacer);
        _panelBlockStateDirect.add(directContainer);
        // direct container done

        _blockStateReferenceTextField = new JTextField();
        _blockStateReferenceTextField.setColumns(30);
        _panelBlockStateReference.add(_blockStateReferenceTextField);

        _blockStateLocalVariableTextField = new JTextField();
        _blockStateLocalVariableTextField.setColumns(30);
        _panelBlockStateLocalVariable.add(_blockStateLocalVariableTextField);

        _blockStateFormulaTextField = new JTextField();
        _blockStateFormulaTextField.setColumns(30);
        _panelBlockStateFormula.add(_blockStateFormulaTextField);


        if (expression != null) {
            switch (expression.getAddressing()) {
                case Direct: _tabbedPaneBlock.setSelectedComponent(_panelBlockDirect); break;
                case Reference: _tabbedPaneBlock.setSelectedComponent(_panelBlockReference); break;
                case LocalVariable: _tabbedPaneBlock.setSelectedComponent(_panelBlockLocalVariable); break;
                case Formula: _tabbedPaneBlock.setSelectedComponent(_panelBlockFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAddressing().name());
            }
            if (expression.getBlock() != null) {
                _blockBeanPanel.setDefaultNamedBean(expression.getBlock().getBean());
            }
            _blockReferenceTextField.setText(expression.getReference());
            _blockLocalVariableTextField.setText(expression.getLocalVariable());
            _blockFormulaTextField.setText(expression.getFormula());

            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());

            switch (expression.getStateAddressing()) {
                case Direct: _tabbedPaneBlockState.setSelectedComponent(_panelBlockStateDirect); break;
                case Reference: _tabbedPaneBlockState.setSelectedComponent(_panelBlockStateReference); break;
                case LocalVariable: _tabbedPaneBlockState.setSelectedComponent(_panelBlockStateLocalVariable); break;
                case Formula: _tabbedPaneBlockState.setSelectedComponent(_panelBlockStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAddressing().name());
            }
            _stateComboBox.setSelectedItem(expression.getBeanState());
            _blockStateReferenceTextField.setText(expression.getStateReference());
            _blockStateLocalVariableTextField.setText(expression.getStateLocalVariable());
            _blockStateFormulaTextField.setText(expression.getStateFormula());

            _blockConstantTextField.setText(expression.getBlockConstant());
            if (expression.getBlockMemory() != null) {
                _blockMemoryBeanPanel.setDefaultNamedBean(expression.getBlockMemory().getBean());
            }
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneBlock,
            _is_IsNot_ComboBox,
            _tabbedPaneBlockState};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionBlock_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setPanelDetailVisibility() {
        _panelBlockConstant.setVisible(false);
        _panelMatchMemory.setVisible(false);
        if (_stateComboBox.getSelectedItem() == BlockState.ValueMatches) {
            _panelBlockConstant.setVisible(true);
        } else if (_stateComboBox.getSelectedItem() == BlockState.MemoryMatches) {
            _panelMatchMemory.setVisible(true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionBlock expression = new ExpressionBlock("IQDE1", null);

        try {
            if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockReference) {
                expression.setReference(_blockReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            if (_tabbedPaneBlockState.getSelectedComponent() == _panelBlockStateReference) {
                expression.setStateReference(_blockStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            expression.setFormula(_blockFormulaTextField.getText());
            if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockFormula) {
                expression.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }

        if (_blockBeanPanel == null || _blockBeanPanel.getNamedBean() == null) {
            errorMessages.add(Bundle.getMessage("Block_ErrorBlock"));
        }

        BlockState oper = _stateComboBox.getItemAt(_stateComboBox.getSelectedIndex());
        if (oper == BlockState.ValueMatches) {
            if (_blockConstantTextField.getText().isEmpty()) {
                errorMessages.add(Bundle.getMessage("Block_ErrorConstant"));
            }
        } else if (oper == BlockState.MemoryMatches) {
            if (_blockMemoryBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("Block_ErrorMemory"));
            }
        }

        if (!errorMessages.isEmpty()) return false;
        return true;
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
            if (_blockBeanPanel != null && !_blockBeanPanel.isEmpty() && (_tabbedPaneBlock.getSelectedComponent() == _panelBlockDirect)) {
                Block block = _blockBeanPanel.getNamedBean();
                if (block != null) {
                    NamedBeanHandle<Block> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(block.getDisplayName(), block);
                    expression.setBlock(handle);
                }
            }
        try {
            if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
                expression.setReference(_blockReferenceTextField.getText());
            } else if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
                expression.setLocalVariable(_blockLocalVariableTextField.getText());
            } else if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockFormula) {
                expression.setAddressing(NamedBeanAddressing.Formula);
                expression.setFormula(_blockFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneBlock has unknown selection");
            }

            expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());

            if (_tabbedPaneBlockState.getSelectedComponent() == _panelBlockStateDirect) {
                expression.setStateAddressing(NamedBeanAddressing.Direct);
                expression.setBeanState((BlockState)_stateComboBox.getSelectedItem());

                // Handle optional data fields
                if (expression.getBeanState() == BlockState.ValueMatches) {
                    expression.setBlockConstant(_blockConstantTextField.getText());
                } else if (expression.getBeanState() == BlockState.MemoryMatches) {
                    Memory memory = _blockMemoryBeanPanel.getNamedBean();
                    if (memory != null) {
                        NamedBeanHandle<Memory> handle
                                = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                        .getNamedBeanHandle(memory.getDisplayName(), memory);
                        expression.setBlockMemory(handle);
                    }
                }

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
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }


    /**
     * Create Block object for the expression
     *
     * @param reference Block application description
     * @return The new output as Block object
     */
    protected Block getBlockFromPanel(String reference) {
        if (_blockBeanPanel == null) {
            return null;
        }
        return _blockBeanPanel.getNamedBean();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Block_Short");
    }

    @Override
    public void dispose() {
        if (_blockBeanPanel != null) {
            _blockBeanPanel.dispose();
        }
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionBlockSwing.class);
}
