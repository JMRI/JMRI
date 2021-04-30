package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionOBlock;
import jmri.jmrit.logixng.actions.ActionOBlock.DirectOperation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionOBlock object with a Swing JPanel.
 *
 * @author Daniel Bergqvist  Copyright 2021
 * @author Dave Sand         Copyright 2021
 */
public class ActionOBlockSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneOBlock;
    private BeanSelectPanel<OBlock> _oblockBeanPanel;
    private JPanel _panelOBlockDirect;
    private JPanel _panelOBlockReference;
    private JPanel _panelOBlockLocalVariable;
    private JPanel _panelOBlockFormula;
    private JTextField _oblockReferenceTextField;
    private JTextField _oblockLocalVariableTextField;
    private JTextField _oblockFormulaTextField;

    private JTabbedPane _tabbedPaneOperation;
    private JComboBox<DirectOperation> _stateComboBox;
    private JPanel _panelOperationDirect;
    private JPanel _panelOperationReference;
    private JPanel _panelOperationLocalVariable;
    private JPanel _panelOperationFormula;
    private JTextField _oblockLockReferenceTextField;
    private JTextField _oblockLockLocalVariableTextField;
    private JTextField _oblockLockFormulaTextField;

    private JPanel _panelStateCombo;
    private JPanel _panelOBlockConstant;
    private JPanel _panelCopyMemory;
    private JTextField _oblockConstantTextField;
    private BeanSelectPanel<Memory> _oblockMemoryBeanPanel;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionOBlock action = (ActionOBlock)object;

        panel = new JPanel();

        _tabbedPaneOBlock = new JTabbedPane();
        _panelOBlockDirect = new javax.swing.JPanel();
        _panelOBlockReference = new javax.swing.JPanel();
        _panelOBlockLocalVariable = new javax.swing.JPanel();
        _panelOBlockFormula = new javax.swing.JPanel();

        _tabbedPaneOBlock.addTab(NamedBeanAddressing.Direct.toString(), _panelOBlockDirect);
        _tabbedPaneOBlock.addTab(NamedBeanAddressing.Reference.toString(), _panelOBlockReference);
        _tabbedPaneOBlock.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelOBlockLocalVariable);
        _tabbedPaneOBlock.addTab(NamedBeanAddressing.Formula.toString(), _panelOBlockFormula);

        _oblockBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(OBlockManager.class), null);
        _panelOBlockDirect.add(_oblockBeanPanel);

        _oblockReferenceTextField = new JTextField();
        _oblockReferenceTextField.setColumns(30);
        _panelOBlockReference.add(_oblockReferenceTextField);

        _oblockLocalVariableTextField = new JTextField();
        _oblockLocalVariableTextField.setColumns(30);
        _panelOBlockLocalVariable.add(_oblockLocalVariableTextField);

        _oblockFormulaTextField = new JTextField();
        _oblockFormulaTextField.setColumns(30);
        _panelOBlockFormula.add(_oblockFormulaTextField);

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
        _panelOBlockConstant = new JPanel();
        _panelCopyMemory = new JPanel();


        _panelOBlockConstant.setVisible(false);
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

        _oblockConstantTextField = new JTextField();
        _oblockConstantTextField.setColumns(25);
        _panelOBlockConstant.add(_oblockConstantTextField);

        _oblockMemoryBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _panelCopyMemory.add(_oblockMemoryBeanPanel);

        directContainer.add(_panelStateCombo);
        directContainer.add(_panelOBlockConstant);
        directContainer.add(_panelCopyMemory);

        _panelOperationDirect.add(directSpacer);
        _panelOperationDirect.add(directContainer);
        // direct container done

        _oblockLockReferenceTextField = new JTextField();
        _oblockLockReferenceTextField.setColumns(30);
        _panelOperationReference.add(_oblockLockReferenceTextField);

        _oblockLockLocalVariableTextField = new JTextField();
        _oblockLockLocalVariableTextField.setColumns(30);
        _panelOperationLocalVariable.add(_oblockLockLocalVariableTextField);

        _oblockLockFormulaTextField = new JTextField();
        _oblockLockFormulaTextField.setColumns(30);
        _panelOperationFormula.add(_oblockLockFormulaTextField);

        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneOBlock.setSelectedComponent(_panelOBlockDirect); break;
                case Reference: _tabbedPaneOBlock.setSelectedComponent(_panelOBlockReference); break;
                case LocalVariable: _tabbedPaneOBlock.setSelectedComponent(_panelOBlockLocalVariable); break;
                case Formula: _tabbedPaneOBlock.setSelectedComponent(_panelOBlockFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getOBlock() != null) {
                _oblockBeanPanel.setDefaultNamedBean(action.getOBlock().getBean());
            }
            _oblockReferenceTextField.setText(action.getReference());
            _oblockLocalVariableTextField.setText(action.getLocalVariable());
            _oblockFormulaTextField.setText(action.getFormula());

            switch (action.getOperationAddressing()) {
                case Direct: _tabbedPaneOperation.setSelectedComponent(_panelOperationDirect); break;
                case Reference: _tabbedPaneOperation.setSelectedComponent(_panelOperationReference); break;
                case LocalVariable: _tabbedPaneOperation.setSelectedComponent(_panelOperationLocalVariable); break;
                case Formula: _tabbedPaneOperation.setSelectedComponent(_panelOperationFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            _stateComboBox.setSelectedItem(action.getOperationDirect());
            setPanelDetailVisibility();
            _oblockLockReferenceTextField.setText(action.getOperationReference());
            _oblockLockLocalVariableTextField.setText(action.getOperationLocalVariable());
            _oblockLockFormulaTextField.setText(action.getLockFormula());

            _oblockConstantTextField.setText(action.getOBlockConstant());
            if (action.getOBlockMemory() != null) {
                _oblockMemoryBeanPanel.setDefaultNamedBean(action.getOBlockMemory().getBean());
            }
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneOBlock,
            _tabbedPaneOperation};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionOBlock_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setPanelDetailVisibility() {
        _panelOBlockConstant.setVisible(false);
        _panelCopyMemory.setVisible(false);
        if (_stateComboBox.getSelectedItem() == DirectOperation.SetValue) {
            _panelOBlockConstant.setVisible(true);
        } else if (_stateComboBox.getSelectedItem() == DirectOperation.CopyFromMemory
                || _stateComboBox.getSelectedItem() == DirectOperation.CopyToMemory) {
            _panelCopyMemory.setVisible(true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionOBlock action = new ActionOBlock("IQDA1", null);

        try {
            if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockReference) {
                action.setReference(_oblockReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationReference(_oblockLockReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            action.setFormula(_oblockFormulaTextField.getText());
            if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }

        if (_oblockBeanPanel == null || _oblockBeanPanel.getNamedBean() == null) {
            errorMessages.add(Bundle.getMessage("ActionOBlock_ErrorOBlock"));
        }

        DirectOperation oper = _stateComboBox.getItemAt(_stateComboBox.getSelectedIndex());
        if (oper == DirectOperation.None) {
            errorMessages.add(Bundle.getMessage("ActionOBlock_ErrorOperation"));
        } else if (oper == DirectOperation.SetValue) {
            if (_oblockConstantTextField.getText().isEmpty()) {
                errorMessages.add(Bundle.getMessage("ActionOBlock_ErrorConstant"));
            }
        } else if (oper == DirectOperation.CopyFromMemory || oper == DirectOperation.CopyToMemory) {
            if (_oblockMemoryBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionOBlock_ErrorMemory"));
            }
        }

        if (!errorMessages.isEmpty()) return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionOBlock action = new ActionOBlock(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionOBlock)) {
            throw new IllegalArgumentException("object must be an ActionOBLock but is a: "+object.getClass().getName());
        }
        ActionOBlock action = (ActionOBlock) object;

        if (_oblockBeanPanel != null && !_oblockBeanPanel.isEmpty() && (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockDirect)) {
            OBlock oblock = _oblockBeanPanel.getNamedBean();
            if (oblock != null) {
                NamedBeanHandle<OBlock> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(oblock.getDisplayName(), oblock);
                action.setOBlock(handle);
            }
        }

        try {
            // Left panel
            if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_oblockReferenceTextField.getText());
            } else if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_oblockLocalVariableTextField.getText());
            } else if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_oblockFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneOBlock has unknown selection");
            }

            // Right panel
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationDirect) {
                action.setOperationAddressing(NamedBeanAddressing.Direct);
                action.setOperationDirect(_stateComboBox.getItemAt(_stateComboBox.getSelectedIndex()));

                // Handle optional data fields
                if (action.getOperationDirect() == DirectOperation.SetValue) {
                    action.setOBlockConstant(_oblockConstantTextField.getText());
                } else if (action.getOperationDirect() == DirectOperation.CopyFromMemory
                        || action.getOperationDirect() == DirectOperation.CopyToMemory) {
                    Memory memory = _oblockMemoryBeanPanel.getNamedBean();
                    if (memory != null) {
                        NamedBeanHandle<Memory> handle
                                = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                        .getNamedBeanHandle(memory.getDisplayName(), memory);
                        action.setOBlockMemory(handle);
                    }
                }

            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference(_oblockLockReferenceTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationLocalVariable) {
                action.setOperationAddressing(NamedBeanAddressing.LocalVariable);
                action.setOperationLocalVariable(_oblockLockLocalVariableTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationFormula) {
                action.setOperationAddressing(NamedBeanAddressing.Formula);
                action.setOperationFormula(_oblockLockFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneOBlock has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionOBlock_Short");
    }

    @Override
    public void dispose() {
        if (_oblockBeanPanel != null) {
            _oblockBeanPanel.dispose();
        }
    }


//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionOBlockSwing.class);

}
