package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.InstanceManager;
import jmri.JmriException;
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
    private JTextField _oblockOperReferenceTextField;
    private JTextField _oblockOperLocalVariableTextField;
    private JTextField _oblockOperFormulaTextField;

    private JTabbedPane _tabbedPaneData;
    private JPanel _panelDataDirect;
    private JPanel _panelDataReference;
    private JPanel _panelDataLocalVariable;
    private JPanel _panelDataFormula;
    private JTextField _oblockDataDirectTextField;
    private JTextField _oblockDataReferenceTextField;
    private JTextField _oblockDataLocalVariableTextField;
    private JTextField _oblockDataFormulaTextField;

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

        _stateComboBox = new JComboBox<>();
        for (DirectOperation e : DirectOperation.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);
        _stateComboBox.addActionListener((java.awt.event.ActionEvent e) -> {
            setDataPanelState();
        });
        _panelOperationDirect.add(_stateComboBox);

        _oblockOperReferenceTextField = new JTextField();
        _oblockOperReferenceTextField.setColumns(30);
        _panelOperationReference.add(_oblockOperReferenceTextField);

        _oblockOperLocalVariableTextField = new JTextField();
        _oblockOperLocalVariableTextField.setColumns(30);
        _panelOperationLocalVariable.add(_oblockOperLocalVariableTextField);

        _oblockOperFormulaTextField = new JTextField();
        _oblockOperFormulaTextField.setColumns(30);
        _panelOperationFormula.add(_oblockOperFormulaTextField);


        _tabbedPaneData = new JTabbedPane();
        _panelDataDirect = new javax.swing.JPanel();
        _panelDataReference = new javax.swing.JPanel();
        _panelDataLocalVariable = new javax.swing.JPanel();
        _panelDataFormula = new javax.swing.JPanel();

        _tabbedPaneData.addTab(NamedBeanAddressing.Direct.toString(), _panelDataDirect);
        _tabbedPaneData.addTab(NamedBeanAddressing.Reference.toString(), _panelDataReference);
        _tabbedPaneData.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelDataLocalVariable);
        _tabbedPaneData.addTab(NamedBeanAddressing.Formula.toString(), _panelDataFormula);

        _oblockDataDirectTextField = new JTextField();
        _oblockDataDirectTextField.setColumns(30);
        _panelDataDirect.add(_oblockDataDirectTextField);

        _oblockDataReferenceTextField = new JTextField();
        _oblockDataReferenceTextField.setColumns(30);
        _panelDataReference.add(_oblockDataReferenceTextField);

        _oblockDataLocalVariableTextField = new JTextField();
        _oblockDataLocalVariableTextField.setColumns(30);
        _panelDataLocalVariable.add(_oblockDataLocalVariableTextField);

        _oblockDataFormulaTextField = new JTextField();
        _oblockDataFormulaTextField.setColumns(30);
        _panelDataFormula.add(_oblockDataFormulaTextField);

        setDataPanelState();

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
            _oblockOperReferenceTextField.setText(action.getOperationReference());
            _oblockOperLocalVariableTextField.setText(action.getOperationLocalVariable());
            _oblockOperFormulaTextField.setText(action.getOperationFormula());

            switch (action.getDataAddressing()) {
                case Direct: _tabbedPaneData.setSelectedComponent(_panelDataDirect); break;
                case Reference: _tabbedPaneData.setSelectedComponent(_panelDataReference); break;
                case LocalVariable: _tabbedPaneData.setSelectedComponent(_panelDataLocalVariable); break;
                case Formula: _tabbedPaneData.setSelectedComponent(_panelDataFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getDataAddressing().name());
            }
            _oblockDataReferenceTextField.setText(action.getDataReference());
            _oblockDataLocalVariableTextField.setText(action.getDataLocalVariable());
            _oblockDataFormulaTextField.setText(action.getDataFormula());

            _oblockDataDirectTextField.setText(action.getOBlockValue());
            setDataPanelState();
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneOBlock,
            _tabbedPaneOperation,
            _tabbedPaneData};


        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionOBlock_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setDataPanelState() {
        boolean newState = _stateComboBox.getSelectedItem() == DirectOperation.SetValue;
        _tabbedPaneData.setEnabled(newState);
        _oblockDataDirectTextField.setEnabled(newState);
        _oblockDataReferenceTextField.setEnabled(newState);
        _oblockDataLocalVariableTextField.setEnabled(newState);
        _oblockDataFormulaTextField.setEnabled(newState);
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
        ActionOBlock action = new ActionOBlock("IQDA1", null);

        try {
            if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockReference) {
                action.setReference(_oblockReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return;
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

        if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockDirect) {
            if (_oblockBeanPanel == null || _oblockBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionOBlock_ErrorBlock"));
            }
        }
    }

    private void validateOperationSection(List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionOBlock action = new ActionOBlock("IQDA2", null);

        try {
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationReference(_oblockOperReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return;
        }

        try {
            action.setOperationFormula(_oblockOperFormulaTextField.getText());
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
        ActionOBlock action = new ActionOBlock("IQDA3", null);

        try {
            if (_tabbedPaneData.getSelectedComponent() == _panelDataReference) {
                action.setDataReference(_oblockDataReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return;
        }

        try {
            action.setDataFormula(_oblockDataFormulaTextField.getText());
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
                if (_oblockDataDirectTextField.getText().isEmpty()) {
                    errorMessages.add(Bundle.getMessage("ActionOBlock_ErrorValue"));
                }
            }
        }
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
            throw new IllegalArgumentException("object must be an ActionOBlock but is a: "+object.getClass().getName());
        }
        ActionOBlock action = (ActionOBlock) object;

        if (_tabbedPaneOBlock.getSelectedComponent() == _panelOBlockDirect) {
            OBlock oblock = _oblockBeanPanel.getNamedBean();
            if (oblock != null) {
                NamedBeanHandle<OBlock> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(oblock.getDisplayName(), oblock);
                action.setOBlock(handle);
            } else {
                action.removeOBlock();
            }
        } else {
            action.removeOBlock();
        }

        try {
            // Left section
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

            // Center section
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationDirect) {
                action.setOperationAddressing(NamedBeanAddressing.Direct);
                action.setOperationDirect(_stateComboBox.getItemAt(_stateComboBox.getSelectedIndex()));
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference(_oblockOperReferenceTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationLocalVariable) {
                action.setOperationAddressing(NamedBeanAddressing.LocalVariable);
                action.setOperationLocalVariable(_oblockOperLocalVariableTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationFormula) {
                action.setOperationAddressing(NamedBeanAddressing.Formula);
                action.setOperationFormula(_oblockOperFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneOBlock has unknown selection");
            }

            // Right section
            if (_tabbedPaneData.getSelectedComponent() == _panelDataDirect) {
                action.setDataAddressing(NamedBeanAddressing.Direct);
                // Handle optional data field
                if (action.getOperationDirect() == DirectOperation.SetValue) {
                    action.setOBlockValue(_oblockDataDirectTextField.getText());
                }
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataReference) {
                action.setDataAddressing(NamedBeanAddressing.Reference);
                action.setDataReference(_oblockDataReferenceTextField.getText());
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataLocalVariable) {
                action.setDataAddressing(NamedBeanAddressing.LocalVariable);
                action.setDataLocalVariable(_oblockDataLocalVariableTextField.getText());
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataFormula) {
                action.setDataAddressing(NamedBeanAddressing.Formula);
                action.setDataFormula(_oblockDataFormulaTextField.getText());
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
