package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionLocalVariable;
import jmri.jmrit.logixng.actions.ActionLocalVariable.VariableOperation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;

/**
 * Configures an ActionLocalVariable object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionLocalVariableSwing extends AbstractDigitalActionSwing {

    private JTextField _localVariableTextField;

    private JTabbedPane _tabbedPaneVariableOperation;
    private BeanSelectPanel<Memory> _copyMemoryBeanPanel;
    private JPanel _setToNull;
    private JPanel _setToConstant;
    private JPanel _copyMemory;
    private JPanel _copyVariable;
    private JPanel _calculateFormula;
    private JPanel _copyTableCell;
    private JTextField _setToConstantTextField;
    private JTextField _copyLocalVariableTextField;
    private JTextField _calculateFormulaTextField;
    private JTextField _copyTableCellTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionLocalVariable action = (ActionLocalVariable)object;

        panel = new JPanel();

        _localVariableTextField = new JTextField(20);

        _tabbedPaneVariableOperation = new JTabbedPane();

        _setToNull = new JPanel();
        _setToConstant = new JPanel();
        _copyMemory = new JPanel();
        _copyTableCell = new JPanel();
        _copyVariable = new JPanel();
        _calculateFormula = new JPanel();

        _tabbedPaneVariableOperation.addTab(VariableOperation.SetToNull.toString(), _setToNull);
        _tabbedPaneVariableOperation.addTab(VariableOperation.SetToString.toString(), _setToConstant);
        _tabbedPaneVariableOperation.addTab(VariableOperation.CopyMemoryToVariable.toString(), _copyMemory);
        _tabbedPaneVariableOperation.addTab(VariableOperation.CopyTableCellToVariable.toString(), _copyTableCell);
        _tabbedPaneVariableOperation.addTab(VariableOperation.CopyVariableToVariable.toString(), _copyVariable);
        _tabbedPaneVariableOperation.addTab(VariableOperation.CalculateFormula.toString(), _calculateFormula);

        _setToNull.add(new JLabel("Null"));     // No I18N

        _setToConstantTextField = new JTextField(30);
        _setToConstant.add(_setToConstantTextField);

        _copyMemoryBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _copyMemory.add(_copyMemoryBeanPanel);

        _copyTableCellTextField = new JTextField(30);
        _copyTableCell.add(_copyTableCellTextField);

        _copyLocalVariableTextField = new JTextField(30);
        _copyVariable.add(_copyLocalVariableTextField);

        _calculateFormulaTextField = new JTextField(30);
        _calculateFormula.add(_calculateFormulaTextField);


        if (action != null) {
            if (action.getLocalVariable() != null) {
                _localVariableTextField.setText(action.getLocalVariable());
            }
            if (action.getMemory() != null) {
                _copyMemoryBeanPanel.setDefaultNamedBean(action.getMemory().getBean());
            }
            switch (action.getVariableOperation()) {
                case SetToNull: _tabbedPaneVariableOperation.setSelectedComponent(_setToNull); break;
                case SetToString: _tabbedPaneVariableOperation.setSelectedComponent(_setToConstant); break;
                case CopyMemoryToVariable: _tabbedPaneVariableOperation.setSelectedComponent(_copyMemory); break;
                case CopyTableCellToVariable: _tabbedPaneVariableOperation.setSelectedComponent(_copyTableCell); break;
                case CopyVariableToVariable: _tabbedPaneVariableOperation.setSelectedComponent(_copyVariable); break;
                case CalculateFormula: _tabbedPaneVariableOperation.setSelectedComponent(_calculateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getVariableOperation().name());
            }
            _setToConstantTextField.setText(action.getConstantValue());
            _copyTableCellTextField.setText(ActionLocalVariable.convertTableReference(action.getOtherTableCell(), false));
            _copyLocalVariableTextField.setText(action.getOtherLocalVariable());
            _calculateFormulaTextField.setText(action.getFormula());
        }

        JComponent[] components = new JComponent[]{
            _localVariableTextField,
            _tabbedPaneVariableOperation
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionLocalVariable_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        ActionLocalVariable action = new ActionLocalVariable("IQDA1", null);

         // If using the Memory tab, validate the memory variable selection.
        if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyMemory) {
            if (_copyMemoryBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionLocalVariable_CopyErrorMemory"));
            }
        }

        // If using the Table tab, validate the table reference content via setOtherTableCell.
        try {
            if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyTableCell) {
                action.setOtherTableCell(ActionLocalVariable.convertTableReference(_copyTableCellTextField.getText(), true));
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
        }

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionLocalVariable action = new ActionLocalVariable(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionLocalVariable)) {
            throw new IllegalArgumentException("object must be an ActionLocalVariable but is a: "+object.getClass().getName());
        }
        ActionLocalVariable action = (ActionLocalVariable)object;

        action.setLocalVariable(_localVariableTextField.getText());


        if (!_copyMemoryBeanPanel.isEmpty()
                && (_tabbedPaneVariableOperation.getSelectedComponent() == _copyMemory)) {
            Memory otherMemory = _copyMemoryBeanPanel.getNamedBean();
            if (otherMemory != null) {
                NamedBeanHandle<Memory> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
                action.setMemory(handle);
            }
        }

        try {
            if (_tabbedPaneVariableOperation.getSelectedComponent() == _setToNull) {
                action.setVariableOperation(VariableOperation.SetToNull);
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _setToConstant) {
                action.setVariableOperation(VariableOperation.SetToString);
                action.setConstantValue(_setToConstantTextField.getText());
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyMemory) {
                action.setVariableOperation(VariableOperation.CopyMemoryToVariable);
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyTableCell) {
                action.setVariableOperation(VariableOperation.CopyTableCellToVariable);
                action.setOtherTableCell(ActionLocalVariable.convertTableReference(_copyTableCellTextField.getText(), true));
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _copyVariable) {
                action.setVariableOperation(VariableOperation.CopyVariableToVariable);
                action.setOtherLocalVariable(_copyLocalVariableTextField.getText());
            } else if (_tabbedPaneVariableOperation.getSelectedComponent() == _calculateFormula) {
                action.setVariableOperation(VariableOperation.CalculateFormula);
                action.setFormula(_calculateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneVariableOperation has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionLocalVariable_Short");
    }

    @Override
    public void dispose() {
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLocalVariableSwing.class);

}
