package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionBlock;
import jmri.jmrit.logixng.actions.ActionBlock.BlockOperation;
import jmri.jmrit.logixng.actions.ActionBlock.DirectOperation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectCreatePanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an Block object with a Swing JPanel.
 *
 * @author Dave Sand Copyright 2021
 */
public class ActionBlockSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneBlock;
    private BeanSelectCreatePanel<Block> blockBeanPanel;
    private JPanel _panelBlockDirect;
    private JPanel _panelBlockReference;
    private JPanel _panelBlockLocalVariable;
    private JPanel _panelBlockFormula;
    private JTextField _blockReferenceTextField;
    private JTextField _blockLocalVariableTextField;
    private JTextField _blockFormulaTextField;

    private JTabbedPane _tabbedPaneOperation;
    private JComboBox<DirectOperation> _stateComboBox;
    private JPanel _panelOperationDirect;
    private JPanel _panelOperationReference;
    private JPanel _panelOperationLocalVariable;
    private JPanel _panelOperationFormula;
    private JTextField _blockLockReferenceTextField;
    private JTextField _blockLockLocalVariableTextField;
    private JTextField _blockLockFormulaTextField;

    private JPanel _panelBlockConstant;
    private JPanel _panelCopyFromMemory;
    private JPanel _panelCopyToMemory;
    private JTextField _blockConstantTextField;
    private BeanSelectCreatePanel<Memory> _blockFromMemoryBeanPanel;
    private BeanSelectCreatePanel<Memory> _blockToMemoryBeanPanel;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionBlock action = (ActionBlock)object;

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

        blockBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(BlockManager.class), null);
        _panelBlockDirect.add(blockBeanPanel);

        _blockReferenceTextField = new JTextField();
        _blockReferenceTextField.setColumns(30);
        _panelBlockReference.add(_blockReferenceTextField);

        _blockLocalVariableTextField = new JTextField();
        _blockLocalVariableTextField.setColumns(30);
        _panelBlockLocalVariable.add(_blockLocalVariableTextField);

        _blockFormulaTextField = new JTextField();
        _blockFormulaTextField.setColumns(30);
        _panelBlockFormula.add(_blockFormulaTextField);


        _tabbedPaneOperation = new JTabbedPane();
        _panelOperationDirect = new javax.swing.JPanel();

        _panelBlockConstant = new JPanel();
        _panelCopyFromMemory = new JPanel();
        _panelCopyToMemory = new JPanel();


        _panelOperationReference = new javax.swing.JPanel();
        _panelOperationLocalVariable = new javax.swing.JPanel();
        _panelOperationFormula = new javax.swing.JPanel();

        _tabbedPaneOperation.addTab(NamedBeanAddressing.Direct.toString(), _panelOperationDirect);

        _tabbedPaneOperation.addTab(BlockOperation.SetToConstant.toString(), _panelBlockConstant);
        _tabbedPaneOperation.addTab(BlockOperation.CopyFromMemory.toString(), _panelCopyFromMemory);
        _tabbedPaneOperation.addTab(BlockOperation.CopyToMemory.toString(), _panelCopyToMemory);

        _tabbedPaneOperation.addTab(NamedBeanAddressing.Reference.toString(), _panelOperationReference);
        _tabbedPaneOperation.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelOperationLocalVariable);
        _tabbedPaneOperation.addTab(NamedBeanAddressing.Formula.toString(), _panelOperationFormula);

        _stateComboBox = new JComboBox<>();
        for (DirectOperation e : DirectOperation.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);

        _panelOperationDirect.add(_stateComboBox);

        _blockConstantTextField = new JTextField();
        _blockConstantTextField.setColumns(30);
        _panelBlockConstant.add(_blockConstantTextField);

        _blockFromMemoryBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _panelCopyFromMemory.add(_blockFromMemoryBeanPanel);

        _blockToMemoryBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _panelCopyToMemory.add(_blockToMemoryBeanPanel);

        _blockLockReferenceTextField = new JTextField();
        _blockLockReferenceTextField.setColumns(30);
        _panelOperationReference.add(_blockLockReferenceTextField);

        _blockLockLocalVariableTextField = new JTextField();
        _blockLockLocalVariableTextField.setColumns(30);
        _panelOperationLocalVariable.add(_blockLockLocalVariableTextField);

        _blockLockFormulaTextField = new JTextField();
        _blockLockFormulaTextField.setColumns(30);
        _panelOperationFormula.add(_blockLockFormulaTextField);


        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneBlock.setSelectedComponent(_panelBlockDirect); break;
                case Reference: _tabbedPaneBlock.setSelectedComponent(_panelBlockReference); break;
                case LocalVariable: _tabbedPaneBlock.setSelectedComponent(_panelBlockLocalVariable); break;
                case Formula: _tabbedPaneBlock.setSelectedComponent(_panelBlockFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getBlock() != null) {
                blockBeanPanel.setDefaultNamedBean(action.getBlock().getBean());
            }
            _blockReferenceTextField.setText(action.getReference());
            _blockLocalVariableTextField.setText(action.getLocalVariable());
            _blockFormulaTextField.setText(action.getFormula());

            switch (action.getOperationAddressing()) {
                case Direct: _tabbedPaneOperation.setSelectedComponent(_panelOperationDirect); break;
                case Reference: _tabbedPaneOperation.setSelectedComponent(_panelOperationReference); break;
                case LocalVariable: _tabbedPaneOperation.setSelectedComponent(_panelOperationLocalVariable); break;
                case Formula: _tabbedPaneOperation.setSelectedComponent(_panelOperationFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            _stateComboBox.setSelectedItem(action.getOperationDirect());
            _blockLockReferenceTextField.setText(action.getOperationReference());
            _blockLockLocalVariableTextField.setText(action.getOperationLocalVariable());
            _blockLockFormulaTextField.setText(action.getLockFormula());

            if (action.getBlockOperation() != null) {
                switch (action.getBlockOperation()) {
                    case SetToConstant: _tabbedPaneOperation.setSelectedComponent(_panelBlockConstant); break;
                    case CopyFromMemory: _tabbedPaneOperation.setSelectedComponent(_panelCopyFromMemory); break;
                    case CopyToMemory: _tabbedPaneOperation.setSelectedComponent(_panelCopyToMemory); break;
                    default: throw new IllegalArgumentException("invalid block operation: " + action.getBlockOperation());
                }
            }
            _blockConstantTextField.setText(action.getBlockConstant());
            if (action.getBlockMemory() != null) {
                _blockFromMemoryBeanPanel.setDefaultNamedBean(action.getBlockMemory().getBean());
                _blockToMemoryBeanPanel.setDefaultNamedBean(action.getBlockMemory().getBean());
            }
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneBlock,
            _tabbedPaneOperation};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionBlock_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionBlock action = new ActionBlock("IQDA1", null);

        try {
            if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockReference) {
                action.setReference(_blockReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationReference(_blockLockReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            action.setFormula(_blockFormulaTextField.getText());
            if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionBlock action = new ActionBlock(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionBlock)) {
            throw new IllegalArgumentException("object must be an TriggerBLock but is a: "+object.getClass().getName());
        }
        ActionBlock action = (ActionBlock) object;

        try {
            if (!blockBeanPanel.isEmpty() && (_tabbedPaneBlock.getSelectedComponent() == _panelBlockDirect)) {
                Block block = blockBeanPanel.getNamedBean();
                if (block != null) {
                    NamedBeanHandle<Block> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(block.getDisplayName(), block);
                    action.setBlock(handle);
                }
            }
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for block", ex);
        }

        try {
            // Left panel
            if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_blockReferenceTextField.getText());
            } else if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_blockLocalVariableTextField.getText());
            } else if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_blockFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneBlock has unknown selection");
            }

            // Right panel
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationDirect) {
                action.setOperationAddressing(NamedBeanAddressing.Direct);
                action.setOperationDirect(_stateComboBox.getItemAt(_stateComboBox.getSelectedIndex()));
                action.setBlockOperation(null);     // Ignore custom tabs
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelBlockConstant) {
                action.setBlockOperation(ActionBlock.BlockOperation.SetToConstant);
                action.setBlockConstant(_blockConstantTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelCopyFromMemory) {
                action.setBlockOperation(ActionBlock.BlockOperation.CopyFromMemory);
                try {
                    if (!_blockFromMemoryBeanPanel.isEmpty()) {
                        Memory memory = _blockFromMemoryBeanPanel.getNamedBean();
                        if (memory != null) {
                            NamedBeanHandle<Memory> handle
                                    = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                            .getNamedBeanHandle(memory.getDisplayName(), memory);
                            action.setBlockMemory(handle);
                        }
                    }
                } catch (JmriException ex) {
                    log.error("Cannot get NamedBeanHandle for memory", ex);
                }
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelCopyToMemory) {
                action.setBlockOperation(ActionBlock.BlockOperation.CopyToMemory);
                try {
                    if (!_blockToMemoryBeanPanel.isEmpty()) {
                        Memory memory = _blockToMemoryBeanPanel.getNamedBean();
                        if (memory != null) {
                            NamedBeanHandle<Memory> handle
                                    = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                            .getNamedBeanHandle(memory.getDisplayName(), memory);
                            action.setBlockMemory(handle);
                        }
                    }
                } catch (JmriException ex) {
                    log.error("Cannot get NamedBeanHandle for memory", ex);
                }
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference(_blockLockReferenceTextField.getText());
                action.setBlockOperation(null);
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationLocalVariable) {
                action.setOperationAddressing(NamedBeanAddressing.LocalVariable);
                action.setOperationLocalVariable(_blockLockLocalVariableTextField.getText());
                action.setBlockOperation(null);
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationFormula) {
                action.setOperationAddressing(NamedBeanAddressing.Formula);
                action.setOperationFormula(_blockLockFormulaTextField.getText());
                action.setBlockOperation(null);
            } else {
                throw new IllegalArgumentException("_tabbedPaneBlock has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionBlock_Short");
    }

    @Override
    public void dispose() {
        if (blockBeanPanel != null) {
            blockBeanPanel.dispose();
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionBlockSwing.class);

}
