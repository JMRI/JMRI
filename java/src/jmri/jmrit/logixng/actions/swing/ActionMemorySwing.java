package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionMemory;
import jmri.jmrit.logixng.actions.ActionMemory.MemoryOperation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;

/**
 * Configures an ActionMemory object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionMemorySwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneMemory;
    private BeanSelectPanel<Memory> _memoryBeanPanel;
    private JPanel _panelMemoryDirect;
    private JPanel _panelMemoryReference;
    private JPanel _panelMemoryLocalVariable;
    private JPanel _panelMemoryFormula;
    private JTextField _memoryReferenceTextField;
    private JTextField _memoryLocalVariableTextField;
    private JTextField _memoryFormulaTextField;

    private JTabbedPane _tabbedPaneMemoryOperation;
    private BeanSelectPanel<Memory> _copyMemoryBeanPanel;
    private JPanel _setToNull;
    private JPanel _setToConstant;
    private JPanel _copyMemory;
    private JPanel _copyTableCell;
    private JPanel _copyVariable;
    private JPanel _calculateFormula;
    private JTextField _setToConstantTextField;
    private JTextField _copyTableCellTextField;
    private JTextField _copyLocalVariableTextField;
    private JTextField _calculateFormulaTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionMemory action = (ActionMemory)object;

        panel = new JPanel();

        _tabbedPaneMemory = new JTabbedPane();
        _panelMemoryDirect = new javax.swing.JPanel();
        _panelMemoryReference = new javax.swing.JPanel();
        _panelMemoryLocalVariable = new javax.swing.JPanel();
        _panelMemoryFormula = new javax.swing.JPanel();

        _tabbedPaneMemory.addTab(NamedBeanAddressing.Direct.toString(), _panelMemoryDirect);
        _tabbedPaneMemory.addTab(NamedBeanAddressing.Reference.toString(), _panelMemoryReference);
        _tabbedPaneMemory.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelMemoryLocalVariable);
        _tabbedPaneMemory.addTab(NamedBeanAddressing.Formula.toString(), _panelMemoryFormula);

        _memoryBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _panelMemoryDirect.add(_memoryBeanPanel);

        _memoryReferenceTextField = new JTextField();
        _memoryReferenceTextField.setColumns(30);
        _panelMemoryReference.add(_memoryReferenceTextField);

        _memoryLocalVariableTextField = new JTextField();
        _memoryLocalVariableTextField.setColumns(30);
        _panelMemoryLocalVariable.add(_memoryLocalVariableTextField);

        _memoryFormulaTextField = new JTextField();
        _memoryFormulaTextField.setColumns(30);
        _panelMemoryFormula.add(_memoryFormulaTextField);

        _tabbedPaneMemoryOperation = new JTabbedPane();

        _setToNull = new JPanel();
        _setToConstant = new JPanel();
        _copyMemory = new JPanel();
        _copyTableCell = new JPanel();
        _copyVariable = new JPanel();
        _calculateFormula = new JPanel();

        _tabbedPaneMemoryOperation.addTab(MemoryOperation.SetToNull.toString(), _setToNull);
        _tabbedPaneMemoryOperation.addTab(MemoryOperation.SetToString.toString(), _setToConstant);
        _tabbedPaneMemoryOperation.addTab(MemoryOperation.CopyMemoryToMemory.toString(), _copyMemory);
        _tabbedPaneMemoryOperation.addTab(MemoryOperation.CopyTableCellToMemory.toString(), _copyTableCell);
        _tabbedPaneMemoryOperation.addTab(MemoryOperation.CopyVariableToMemory.toString(), _copyVariable);
        _tabbedPaneMemoryOperation.addTab(MemoryOperation.CalculateFormula.toString(), _calculateFormula);

        _setToNull.add(new JLabel("Null"));     // No I18N

        _setToConstantTextField = new JTextField(30);
        _setToConstant.add(_setToConstantTextField);

        _copyMemoryBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _copyMemory.add(_copyMemoryBeanPanel);

        _copyLocalVariableTextField = new JTextField(30);
        _copyVariable.add(_copyLocalVariableTextField);

        _copyTableCellTextField = new JTextField(30);
        _copyTableCell.add(_copyTableCellTextField);

        _calculateFormulaTextField = new JTextField(30);
        _calculateFormula.add(_calculateFormulaTextField);


        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneMemory.setSelectedComponent(_panelMemoryDirect); break;
                case Reference: _tabbedPaneMemory.setSelectedComponent(_panelMemoryReference); break;
                case LocalVariable: _tabbedPaneMemory.setSelectedComponent(_panelMemoryLocalVariable); break;
                case Formula: _tabbedPaneMemory.setSelectedComponent(_panelMemoryFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getMemory() != null) {
                _memoryBeanPanel.setDefaultNamedBean(action.getMemory().getBean());
            }
            _memoryReferenceTextField.setText(action.getReference());
            _memoryLocalVariableTextField.setText(action.getLocalVariable());
            _memoryFormulaTextField.setText(action.getFormula());

            if (action.getOtherMemory() != null) {
                _copyMemoryBeanPanel.setDefaultNamedBean(action.getOtherMemory().getBean());
            }
            switch (action.getMemoryOperation()) {
                case SetToNull: _tabbedPaneMemoryOperation.setSelectedComponent(_setToNull); break;
                case SetToString: _tabbedPaneMemoryOperation.setSelectedComponent(_setToConstant); break;
                case CopyMemoryToMemory: _tabbedPaneMemoryOperation.setSelectedComponent(_copyMemory); break;
                case CopyTableCellToMemory: _tabbedPaneMemoryOperation.setSelectedComponent(_copyTableCell); break;
                case CopyVariableToMemory: _tabbedPaneMemoryOperation.setSelectedComponent(_copyVariable); break;
                case CalculateFormula: _tabbedPaneMemoryOperation.setSelectedComponent(_calculateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getMemoryOperation().name());
            }
            _setToConstantTextField.setText(action.getConstantValue());
            _copyTableCellTextField.setText(ActionMemory.convertTableReference(action.getOtherTableCell(), false));
            _copyLocalVariableTextField.setText(action.getOtherLocalVariable());
            _calculateFormulaTextField.setText(action.getOtherFormula());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneMemory,
            _tabbedPaneMemoryOperation
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionMemory_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        validateMemorySection(errorMessages);
        validateDataSection(errorMessages);
        return errorMessages.isEmpty();
    }

    private void validateMemorySection(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionMemory action = new ActionMemory("IQDA1", null);

        // If using the Direct tab, validate the memory variable selection.
        if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryDirect) {
            if (_memoryBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionMemory_ErrorMemory"));
            }
        }

        // If using the Reference tab, validate the reference content via setReference.
        try {
            if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryReference) {
                action.setReference(_memoryReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
        }

        // Validate formula parsing via setFormula and tab selections.
        try {
            action.setFormula(_memoryFormulaTextField.getText());
            if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }
    }

    public void validateDataSection(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionMemory action = new ActionMemory("IQDA2", null);

        // If using the Memory tab, validate the memory variable selection.
        if (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyMemory) {
            if (_copyMemoryBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionMemory_CopyErrorMemory"));
            }
        }

        // If using the Table tab, validate the table reference content via setOtherTableCell.
        try {
            if (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyTableCell) {
                action.setOtherTableCell(ActionMemory.convertTableReference(_copyTableCellTextField.getText(), true));
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return;
        }

        // Validate formula parsing via setFormula and tab selection.
        try {
            action.setOtherFormula(_calculateFormulaTextField.getText());
            if (_tabbedPaneMemoryOperation.getSelectedComponent() == _calculateFormula) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.CalculateFormula);
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionMemory action = new ActionMemory(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionMemory)) {
            throw new IllegalArgumentException("object must be an ActionMemory but is a: "+object.getClass().getName());
        }
        ActionMemory action = (ActionMemory)object;

        Memory memory = _memoryBeanPanel.getNamedBean();
        if (memory != null) {
            NamedBeanHandle<Memory> handle
                    = InstanceManager.getDefault(NamedBeanHandleManager.class)
                            .getNamedBeanHandle(memory.getDisplayName(), memory);
            action.setMemory(handle);
        } else {
            action.removeMemory();
        }

        if (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyMemory) {
            Memory otherMemory = _copyMemoryBeanPanel.getNamedBean();
            if (otherMemory != null) {
                NamedBeanHandle<Memory> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
                action.setOtherMemory(handle);
            } else {
                action.removeOtherMemory();
            }
        } else {
            action.removeOtherMemory();
        }

        try {
            if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_memoryReferenceTextField.getText());
            } else if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_memoryLocalVariableTextField.getText());
            } else if (_tabbedPaneMemory.getSelectedComponent() == _panelMemoryFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_memoryFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneMemory has unknown selection");
            }

            if (_tabbedPaneMemoryOperation.getSelectedComponent() == _setToNull) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.SetToNull);
            } else if (_tabbedPaneMemoryOperation.getSelectedComponent() == _setToConstant) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.SetToString);
                action.setOtherConstantValue(_setToConstantTextField.getText());
            } else if (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyMemory) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.CopyMemoryToMemory);
            } else if (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyTableCell) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.CopyTableCellToMemory);
                action.setOtherTableCell(ActionMemory.convertTableReference(_copyTableCellTextField.getText(), true));
            } else if (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyVariable) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.CopyVariableToMemory);
                action.setOtherLocalVariable(_copyLocalVariableTextField.getText());
            } else if (_tabbedPaneMemoryOperation.getSelectedComponent() == _calculateFormula) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.CalculateFormula);
                action.setOtherFormula(_calculateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneMemoryOperation has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionMemory_Short");
    }

    @Override
    public void dispose() {
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionMemorySwing.class);

}
