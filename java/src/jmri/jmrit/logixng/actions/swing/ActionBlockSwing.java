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
import jmri.jmrit.logixng.actions.ActionBlock.DirectOperation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionBlock object with a Swing JPanel.
 *
 * @author Daniel Bergqvist  Copyright 2021
 * @author Dave Sand         Copyright 2021
 */
public class ActionBlockSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneBlock;
    private BeanSelectPanel<Block> blockBeanPanel;
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

    private JPanel _panelStateCombo;
    private JPanel _panelBlockConstant;
    private JPanel _panelCopyMemory;
    private JTextField _blockConstantTextField;
    private BeanSelectPanel<Memory> _blockMemoryBeanPanel;

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

        blockBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(BlockManager.class), null);
        _panelBlockDirect.add(blockBeanPanel);

        blockBeanPanel.getBeanCombo().addActionListener((java.awt.event.ActionEvent e) -> {
//             log.info("sample bean selection changed: {}", e);
        });

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

        _panelOperationReference = new javax.swing.JPanel();
        _panelOperationLocalVariable = new javax.swing.JPanel();
        _panelOperationFormula = new javax.swing.JPanel();

        _tabbedPaneOperation.addTab(NamedBeanAddressing.Direct.toString(), _panelOperationDirect);
        _tabbedPaneOperation.addTab(NamedBeanAddressing.Reference.toString(), _panelOperationReference);
        _tabbedPaneOperation.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelOperationLocalVariable);
        _tabbedPaneOperation.addTab(NamedBeanAddressing.Formula.toString(), _panelOperationFormula);

        // Create an empty strut panel that sits to the left of the container
        JPanel directSpacer = new JPanel();
        directSpacer.setLayout(new BoxLayout(directSpacer, BoxLayout.Y_AXIS));
        directSpacer.add(Box.createVerticalStrut(100));

        // The container holds 3 panels, two of which are based on selections
        JPanel directContainer = new JPanel();
        directContainer.setLayout(new BoxLayout(directContainer, BoxLayout.Y_AXIS));

        _panelStateCombo = new JPanel();
        _panelBlockConstant = new JPanel();
        _panelCopyMemory = new JPanel();


        _panelBlockConstant.setVisible(false);
        _panelCopyMemory.setVisible(false);

        // Populate the container sub-panels
        _stateComboBox = new JComboBox<>();
        for (DirectOperation e : DirectOperation.values()) {
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
        _panelCopyMemory.add(_blockMemoryBeanPanel);

        directContainer.add(_panelStateCombo);
        directContainer.add(_panelBlockConstant);
        directContainer.add(_panelCopyMemory);

        _panelOperationDirect.add(directSpacer);
        _panelOperationDirect.add(directContainer);
        // direct container done

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
            setPanelDetailVisibility();
            _blockLockReferenceTextField.setText(action.getOperationReference());
            _blockLockLocalVariableTextField.setText(action.getOperationLocalVariable());
            _blockLockFormulaTextField.setText(action.getLockFormula());

            _blockConstantTextField.setText(action.getBlockConstant());
            if (action.getBlockMemory() != null) {
                _blockMemoryBeanPanel.setDefaultNamedBean(action.getBlockMemory().getBean());
            }
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneBlock,
            _tabbedPaneOperation};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionBlock_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setPanelDetailVisibility() {
        _panelBlockConstant.setVisible(false);
        _panelCopyMemory.setVisible(false);
        if (_stateComboBox.getSelectedItem() == DirectOperation.SetToConstant) {
            _panelBlockConstant.setVisible(true);
        } else if (_stateComboBox.getSelectedItem() == DirectOperation.CopyFromMemory
                || _stateComboBox.getSelectedItem() == DirectOperation.CopyToMemory) {
            _panelCopyMemory.setVisible(true);
        }
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

        if (blockBeanPanel.getNamedBean() == null) {
            errorMessages.add(Bundle.getMessage("ActionBlock_ErrorBlock"));
        }

        DirectOperation oper = _stateComboBox.getItemAt(_stateComboBox.getSelectedIndex());
        if (oper == DirectOperation.None) {
            errorMessages.add(Bundle.getMessage("ActionBlock_ErrorOperation"));
        } else if (oper == DirectOperation.SetToConstant) {
            if (_blockConstantTextField.getText().isEmpty()) {
                errorMessages.add(Bundle.getMessage("ActionBlock_ErrorConstant"));
            }
        } else if (oper == DirectOperation.CopyFromMemory || oper == DirectOperation.CopyToMemory) {
            if (_blockMemoryBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionBlock_ErrorMemory"));
            }
        }

        if (!errorMessages.isEmpty()) return false;
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
            throw new IllegalArgumentException("object must be an ActionBLock but is a: "+object.getClass().getName());
        }
        ActionBlock action = (ActionBlock) object;

        if (!blockBeanPanel.isEmpty() && (_tabbedPaneBlock.getSelectedComponent() == _panelBlockDirect)) {
            Block block = blockBeanPanel.getNamedBean();
            if (block != null) {
                NamedBeanHandle<Block> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(block.getDisplayName(), block);
                action.setBlock(handle);
            }
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

                // Handle optional data fields
                if (action.getOperationDirect() == DirectOperation.SetToConstant) {
                    action.setBlockConstant(_blockConstantTextField.getText());
                } else if (action.getOperationDirect() == DirectOperation.CopyFromMemory
                        || action.getOperationDirect() == DirectOperation.CopyToMemory) {
                    Memory memory = _blockMemoryBeanPanel.getNamedBean();
                    if (memory != null) {
                        NamedBeanHandle<Memory> handle
                                = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                        .getNamedBeanHandle(memory.getDisplayName(), memory);
                        action.setBlockMemory(handle);
                    }
                }

            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference(_blockLockReferenceTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationLocalVariable) {
                action.setOperationAddressing(NamedBeanAddressing.LocalVariable);
                action.setOperationLocalVariable(_blockLockLocalVariableTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationFormula) {
                action.setOperationAddressing(NamedBeanAddressing.Formula);
                action.setOperationFormula(_blockLockFormulaTextField.getText());
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


//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionBlockSwing.class);

}
