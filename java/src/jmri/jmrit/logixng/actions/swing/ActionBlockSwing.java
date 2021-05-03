package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.JmriException;
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
    private BeanSelectPanel<Block> _blockBeanPanel;
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
    private JTextField _blockOperReferenceTextField;
    private JTextField _blockOperLocalVariableTextField;
    private JTextField _blockOperFormulaTextField;

    private JTabbedPane _tabbedPaneData;
    private JPanel _panelDataDirect;
    private JPanel _panelDataReference;
    private JPanel _panelDataLocalVariable;
    private JPanel _panelDataFormula;
    private JTextField _blockDataDirectTextField;
    private JTextField _blockDataReferenceTextField;
    private JTextField _blockDataLocalVariableTextField;
    private JTextField _blockDataFormulaTextField;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionBlock action = (ActionBlock)object;

        panel = new JPanel();
        // Left section
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

//         _blockBeanPanel.getBeanCombo().addActionListener((java.awt.event.ActionEvent e) -> {
//             log.info("sample bean selection changed: {}", e);
//         });

        _blockReferenceTextField = new JTextField();
        _blockReferenceTextField.setColumns(30);
        _panelBlockReference.add(_blockReferenceTextField);

        _blockLocalVariableTextField = new JTextField();
        _blockLocalVariableTextField.setColumns(30);
        _panelBlockLocalVariable.add(_blockLocalVariableTextField);

        _blockFormulaTextField = new JTextField();
        _blockFormulaTextField.setColumns(30);
        _panelBlockFormula.add(_blockFormulaTextField);

        // Center section
        _tabbedPaneOperation = new JTabbedPane();
        _panelOperationDirect = new javax.swing.JPanel();
        _panelOperationReference = new javax.swing.JPanel();
        _panelOperationLocalVariable = new javax.swing.JPanel();
        _panelOperationFormula = new javax.swing.JPanel();

        _tabbedPaneOperation.addTab(NamedBeanAddressing.Direct.toString(), _panelOperationDirect);
        _tabbedPaneOperation.addTab(NamedBeanAddressing.Reference.toString(), _panelOperationReference);
        _tabbedPaneOperation.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelOperationLocalVariable);
        _tabbedPaneOperation.addTab(NamedBeanAddressing.Formula.toString(), _panelOperationFormula);

        _stateComboBox = new JComboBox<>();
        for (DirectOperation e : DirectOperation.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);
        _stateComboBox.addActionListener((java.awt.event.ActionEvent e) -> {
            setDataPanelState();
        });
        _panelOperationDirect.add(_stateComboBox);

        _blockOperReferenceTextField = new JTextField();
        _blockOperReferenceTextField.setColumns(30);
        _panelOperationReference.add(_blockOperReferenceTextField);

        _blockOperLocalVariableTextField = new JTextField();
        _blockOperLocalVariableTextField.setColumns(30);
        _panelOperationLocalVariable.add(_blockOperLocalVariableTextField);

        _blockOperFormulaTextField = new JTextField();
        _blockOperFormulaTextField.setColumns(30);
        _panelOperationFormula.add(_blockOperFormulaTextField);

        // Right section
        _tabbedPaneData = new JTabbedPane();
        _panelDataDirect = new javax.swing.JPanel();
        _panelDataReference = new javax.swing.JPanel();
        _panelDataLocalVariable = new javax.swing.JPanel();
        _panelDataFormula = new javax.swing.JPanel();

        _tabbedPaneData.addTab(NamedBeanAddressing.Direct.toString(), _panelDataDirect);
        _tabbedPaneData.addTab(NamedBeanAddressing.Reference.toString(), _panelDataReference);
        _tabbedPaneData.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelDataLocalVariable);
        _tabbedPaneData.addTab(NamedBeanAddressing.Formula.toString(), _panelDataFormula);

        _blockDataDirectTextField = new JTextField();
        _blockDataDirectTextField.setColumns(30);
        _panelDataDirect.add(_blockDataDirectTextField);

        _blockDataReferenceTextField = new JTextField();
        _blockDataReferenceTextField.setColumns(30);
        _panelDataReference.add(_blockDataReferenceTextField);

        _blockDataLocalVariableTextField = new JTextField();
        _blockDataLocalVariableTextField.setColumns(30);
        _panelDataLocalVariable.add(_blockDataLocalVariableTextField);

        _blockDataFormulaTextField = new JTextField();
        _blockDataFormulaTextField.setColumns(30);
        _panelDataFormula.add(_blockDataFormulaTextField);

        setDataPanelState();

        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneBlock.setSelectedComponent(_panelBlockDirect); break;
                case Reference: _tabbedPaneBlock.setSelectedComponent(_panelBlockReference); break;
                case LocalVariable: _tabbedPaneBlock.setSelectedComponent(_panelBlockLocalVariable); break;
                case Formula: _tabbedPaneBlock.setSelectedComponent(_panelBlockFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getBlock() != null) {
                _blockBeanPanel.setDefaultNamedBean(action.getBlock().getBean());
            }
            _blockReferenceTextField.setText(action.getReference());
            _blockLocalVariableTextField.setText(action.getLocalVariable());
            _blockFormulaTextField.setText(action.getFormula());

            switch (action.getOperationAddressing()) {
                case Direct: _tabbedPaneOperation.setSelectedComponent(_panelOperationDirect); break;
                case Reference: _tabbedPaneOperation.setSelectedComponent(_panelOperationReference); break;
                case LocalVariable: _tabbedPaneOperation.setSelectedComponent(_panelOperationLocalVariable); break;
                case Formula: _tabbedPaneOperation.setSelectedComponent(_panelOperationFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getOperationAddressing().name());
            }
            _stateComboBox.setSelectedItem(action.getOperationDirect());
            _blockOperReferenceTextField.setText(action.getOperationReference());
            _blockOperLocalVariableTextField.setText(action.getOperationLocalVariable());
            _blockOperFormulaTextField.setText(action.getOperationFormula());

            switch (action.getDataAddressing()) {
                case Direct: _tabbedPaneData.setSelectedComponent(_panelDataDirect); break;
                case Reference: _tabbedPaneData.setSelectedComponent(_panelDataReference); break;
                case LocalVariable: _tabbedPaneData.setSelectedComponent(_panelDataLocalVariable); break;
                case Formula: _tabbedPaneData.setSelectedComponent(_panelDataFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getDataAddressing().name());
            }
            _blockDataReferenceTextField.setText(action.getDataReference());
            _blockDataLocalVariableTextField.setText(action.getDataLocalVariable());
            _blockDataFormulaTextField.setText(action.getDataFormula());

            _blockDataDirectTextField.setText(action.getBlockValue());
            setDataPanelState();
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneBlock,
            _tabbedPaneOperation,
            _tabbedPaneData};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionBlock_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setDataPanelState() {
        boolean newState = _stateComboBox.getSelectedItem() == DirectOperation.SetValue;
        _tabbedPaneData.setEnabled(newState);
        _blockDataDirectTextField.setEnabled(newState);
        _blockDataReferenceTextField.setEnabled(newState);
        _blockDataLocalVariableTextField.setEnabled(newState);
        _blockDataFormulaTextField.setEnabled(newState);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        validateBlockSection(errorMessages);
        validateOperationSection(errorMessages);
        validateDataSection(errorMessages);
        return errorMessages.isEmpty();
    }

    private void validateBlockSection(List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionBlock action = new ActionBlock("IQDA1", null);

        try {
            if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockReference) {
                action.setReference(_blockReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return;
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

        if (_tabbedPaneBlock.getSelectedComponent() == _panelBlockDirect) {
            if (_blockBeanPanel == null || _blockBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionBlock_ErrorBlock"));
            }
        }
    }

    private void validateOperationSection(List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionBlock action = new ActionBlock("IQDA2", null);

        try {
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationReference(_blockOperReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return;
        }

        try {
            action.setOperationFormula(_blockOperFormulaTextField.getText());
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationDirect) {
                action.setOperationAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationLocalVariable) {
                action.setOperationAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationFormula) {
                action.setOperationAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }
    }

    private void validateDataSection(List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionBlock action = new ActionBlock("IQDA3", null);

        try {
            if (_tabbedPaneData.getSelectedComponent() == _panelDataReference) {
                action.setDataReference(_blockDataReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return;
        }

        try {
            action.setDataFormula(_blockDataFormulaTextField.getText());
            if (_tabbedPaneData.getSelectedComponent() == _panelDataDirect) {
                action.setDataAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataReference) {
                action.setDataAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataLocalVariable) {
                action.setDataAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataFormula) {
                action.setDataAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }

        if (_tabbedPaneData.getSelectedComponent() == _panelDataDirect) {
            DirectOperation oper = _stateComboBox.getItemAt(_stateComboBox.getSelectedIndex());
            if (oper == DirectOperation.SetValue) {
                if (_blockDataDirectTextField.getText().isEmpty()) {
                    errorMessages.add(Bundle.getMessage("ActionBlock_ErrorValue"));
                }
            }
        }
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
            throw new IllegalArgumentException("object must be an ActionBlock but is a: "+object.getClass().getName());
        }
        ActionBlock action = (ActionBlock) object;

        if (_blockBeanPanel != null && !_blockBeanPanel.isEmpty() && (_tabbedPaneBlock.getSelectedComponent() == _panelBlockDirect)) {
            Block block = _blockBeanPanel.getNamedBean();
            if (block != null) {
                NamedBeanHandle<Block> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(block.getDisplayName(), block);
                action.setBlock(handle);
            }
        }

        try {
            // Left section
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

            // Center section
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationDirect) {
                action.setOperationAddressing(NamedBeanAddressing.Direct);
                action.setOperationDirect(_stateComboBox.getItemAt(_stateComboBox.getSelectedIndex()));
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference(_blockOperReferenceTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationLocalVariable) {
                action.setOperationAddressing(NamedBeanAddressing.LocalVariable);
                action.setOperationLocalVariable(_blockOperLocalVariableTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationFormula) {
                action.setOperationAddressing(NamedBeanAddressing.Formula);
                action.setOperationFormula(_blockOperFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneBlock has unknown selection");
            }

            // Right section
            if (_tabbedPaneData.getSelectedComponent() == _panelDataDirect) {
                action.setDataAddressing(NamedBeanAddressing.Direct);
                // Handle optional data field
                if (action.getOperationDirect() == DirectOperation.SetValue) {
                    action.setBlockValue(_blockDataDirectTextField.getText());
                }
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataReference) {
                action.setDataAddressing(NamedBeanAddressing.Reference);
                action.setDataReference(_blockDataReferenceTextField.getText());
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataLocalVariable) {
                action.setDataAddressing(NamedBeanAddressing.LocalVariable);
                action.setDataLocalVariable(_blockDataLocalVariableTextField.getText());
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataFormula) {
                action.setDataAddressing(NamedBeanAddressing.Formula);
                action.setDataFormula(_blockDataFormulaTextField.getText());
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
        if (_blockBeanPanel != null) {
            _blockBeanPanel.dispose();
        }
    }


//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionBlockSwing.class);

}
