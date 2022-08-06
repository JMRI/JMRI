package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionOBlock;
import jmri.jmrit.logixng.actions.ActionOBlock.DirectOperation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.BeanSelectPanel;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;

/**
 * Configures an ActionOBlock object with a Swing JPanel.
 *
 * @author Daniel Bergqvist  Copyright 2021
 * @author Dave Sand         Copyright 2021
 */
public class ActionOBlockSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectNamedBeanSwing<OBlock> _selectNamedBeanSwing;

    private LogixNG_SelectEnumSwing<DirectOperation> _selectOperationSwing;

    private JTabbedPane _tabbedPaneData;
    private JPanel _panelDataDirect;
    private JPanel _panelDataReference;
    private JPanel _panelDataLocalVariable;
    private JPanel _panelDataFormula;
    private JTextField _oblockDataDirectTextField;
    private JTextField _oblockDataReferenceTextField;
    private JTextField _oblockDataLocalVariableTextField;
    private JTextField _oblockDataFormulaTextField;
    private BeanSelectPanel<Memory> _panelMemoryBean;
    private JPanel _memoryPanel;
    private JPanel _valuePanel;


    public ActionOBlockSwing() {
    }

    public ActionOBlockSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionOBlock action = (ActionOBlock)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(OBlockManager.class), getJDialog(), this);

        _selectOperationSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        panel = new JPanel();
        _memoryPanel = new JPanel();
        _valuePanel = new JPanel();

        JPanel _tabbedPaneNamedBean;
        JPanel _tabbedPaneOperation;

        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
            _tabbedPaneOperation = _selectOperationSwing.createPanel(action.getSelectEnum(), DirectOperation.values());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
            _tabbedPaneOperation = _selectOperationSwing.createPanel(null, DirectOperation.values());
        }

        _tabbedPaneData = new JTabbedPane();
        _panelDataDirect = new javax.swing.JPanel();
        _panelDataDirect.setLayout(new BoxLayout(_panelDataDirect, BoxLayout.Y_AXIS));
        _panelDataReference = new javax.swing.JPanel();
        _panelDataLocalVariable = new javax.swing.JPanel();
        _panelDataFormula = new javax.swing.JPanel();

        _tabbedPaneData.addTab(NamedBeanAddressing.Direct.toString(), _panelDataDirect);
        _tabbedPaneData.addTab(NamedBeanAddressing.Reference.toString(), _panelDataReference);
        _tabbedPaneData.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelDataLocalVariable);
        _tabbedPaneData.addTab(NamedBeanAddressing.Formula.toString(), _panelDataFormula);

        _oblockDataDirectTextField = new JTextField();
        _oblockDataDirectTextField.setColumns(30);
        _valuePanel.add(_oblockDataDirectTextField);
        _panelDataDirect.add(_valuePanel);

        _panelMemoryBean = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _memoryPanel.add(_panelMemoryBean);
        _panelDataDirect.add(_panelMemoryBean);

        _oblockDataReferenceTextField = new JTextField();
        _oblockDataReferenceTextField.setColumns(30);
        _panelDataReference.add(_oblockDataReferenceTextField);

        _oblockDataLocalVariableTextField = new JTextField();
        _oblockDataLocalVariableTextField.setColumns(30);
        _panelDataLocalVariable.add(_oblockDataLocalVariableTextField);

        _oblockDataFormulaTextField = new JTextField();
        _oblockDataFormulaTextField.setColumns(30);
        _panelDataFormula.add(_oblockDataFormulaTextField);

//        setDataPanelState();
        _valuePanel.setVisible(false);
        _memoryPanel.setVisible(false);

        if (action != null) {
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
            if (action.getSelectMemoryNamedBean().getNamedBean() != null) {
                _panelMemoryBean.setDefaultNamedBean(action.getSelectMemoryNamedBean().getNamedBean().getBean());
            }

            LogixNG_SelectEnum<DirectOperation> selectEnum = action.getSelectEnum();
            if (selectEnum.getEnum() != null) {
                switch (selectEnum.getEnum()) {
                    case GetBlockWarrant:
                    case GetBlockValue:
                        _panelMemoryBean.setVisible(true);
                        break;
                    case SetValue:
                        _valuePanel.setVisible(true);
                        break;
                    default:
                }
            }
        }

        setDataPanelState();

        _selectOperationSwing.addAddressingListener((evt) -> { setDataPanelState(); });
        _selectOperationSwing.addEnumListener((evt) -> { setDataPanelState(); });

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _tabbedPaneOperation,
            _tabbedPaneData};


        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionOBlock_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setDataPanelState() {
        _valuePanel.setVisible(false);
        _panelMemoryBean.setVisible(false);

        boolean newState = false;

        if (_selectOperationSwing.isEnumSelectedOrIndirectAddressing(DirectOperation.SetValue)) {
            _valuePanel.setVisible(true);
            newState = true;
        } else if (_selectOperationSwing.isEnumSelectedOrIndirectAddressing(DirectOperation.GetBlockWarrant) ||
                _selectOperationSwing.isEnumSelectedOrIndirectAddressing(DirectOperation.GetBlockValue)) {
            _panelMemoryBean.setVisible(true);
            newState = true;
        }

        _tabbedPaneData.setEnabled(newState);
//        _oblockDataDirectTextField.setEnabled(newState);
        _oblockDataReferenceTextField.setEnabled(newState);
        _oblockDataLocalVariableTextField.setEnabled(newState);
        _oblockDataFormulaTextField.setEnabled(newState);
    }


    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        ActionOBlock action = new ActionOBlock("IQDA1", null);
        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        _selectOperationSwing.validate(action.getSelectEnum(), errorMessages);
        validateDataSection(errorMessages);
        return errorMessages.isEmpty();
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
            if (_selectOperationSwing.isEnumSelectedOrIndirectAddressing(DirectOperation.GetBlockWarrant)
                    || _selectOperationSwing.isEnumSelectedOrIndirectAddressing(DirectOperation.GetBlockValue)) {
                if (_panelMemoryBean.isEmpty() || _panelMemoryBean.getNamedBean() == null) {
                    errorMessages.add(Bundle.getMessage("ActionWarrant_ErrorMemory"));
                }
            } else if (_selectOperationSwing.isEnumSelectedOrIndirectAddressing(DirectOperation.SetValue)) {
                if (_oblockDataDirectTextField.getText().isEmpty()) {
                    errorMessages.add(Bundle.getMessage("ActionWarrant_ErrorValue"));
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
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());

        try {
            // Center section
            _selectOperationSwing.updateObject(action.getSelectEnum());

            // Right section
            if (_tabbedPaneData.getSelectedComponent() == _panelDataDirect) {
                action.setDataAddressing(NamedBeanAddressing.Direct);
                // Handle optional data field
                if (_selectOperationSwing.isEnumSelectedOrIndirectAddressing(DirectOperation.SetValue)) {
                    action.setOBlockValue(_oblockDataDirectTextField.getText());
                } else if (_selectOperationSwing.isEnumSelectedOrIndirectAddressing(DirectOperation.GetBlockWarrant)
                        || _selectOperationSwing.isEnumSelectedOrIndirectAddressing(DirectOperation.GetBlockValue)) {
                    Memory memory = _panelMemoryBean.getNamedBean();
                    if (memory != null) {
                        NamedBeanHandle<Memory> handle
                                = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                        .getNamedBeanHandle(memory.getDisplayName(), memory);
                        action.getSelectMemoryNamedBean().setNamedBean(handle);
                    } else {
                        action.getSelectMemoryNamedBean().removeNamedBean();
                    }
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
        _selectNamedBeanSwing.dispose();
        _selectOperationSwing.dispose();
    }


//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionOBlockSwing.class);

}
