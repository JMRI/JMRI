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
import jmri.jmrit.logixng.util.swing.LogixNG_SelectTableSwing;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.BeanSelectPanel;

/**
 * Configures an ActionMemory object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionMemorySwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectTableSwing selectTableSwing;

    private LogixNG_SelectNamedBeanSwing<Memory> _selectNamedBeanSwing;

    private JTabbedPane _tabbedPaneMemoryOperation;
    private BeanSelectPanel<Memory> _copyMemoryBeanPanel;
    private JPanel _setToNull;
    private JPanel _setToConstant;
    private JPanel _copyMemory;
    private JPanel _copyTableCell;
    private JPanel _copyVariable;
    private JPanel _calculateFormula;
    private JTextField _setToConstantTextField;
    private JTextField _copyLocalVariableTextField;
    private JTextField _calculateFormulaTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionMemory action = (ActionMemory)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(MemoryManager.class), getJDialog(), this);

        selectTableSwing = new LogixNG_SelectTableSwing(getJDialog(), this);

        panel = new JPanel();

        JPanel _tabbedPaneNamedBean;
        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }

        _tabbedPaneMemoryOperation = new JTabbedPane();

        _setToNull = new JPanel();
        _setToConstant = new JPanel();
        _copyMemory = new JPanel();
        if (action != null) {
            _copyTableCell = selectTableSwing.createPanel(action.getSelectTable());
        } else {
            _copyTableCell = selectTableSwing.createPanel(null);
        }
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

        _calculateFormulaTextField = new JTextField(30);
        _calculateFormula.add(_calculateFormulaTextField);


        if (action != null) {
            if (action.getSelectOtherMemoryNamedBean().getNamedBean() != null) {
                _copyMemoryBeanPanel.setDefaultNamedBean(action.getSelectOtherMemoryNamedBean().getNamedBean().getBean());
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
            _copyLocalVariableTextField.setText(action.getOtherLocalVariable());
            _calculateFormulaTextField.setText(action.getOtherFormula());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _tabbedPaneMemoryOperation
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionMemory_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionMemory action = new ActionMemory("IQDA2", null);

        try {
            action.setMemoryOperation(MemoryOperation.CalculateFormula);
            action.setOtherFormula(_calculateFormulaTextField.getText());
        } catch (ParserException e) {
            errorMessages.add(e.getMessage());
        }

        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        validateDataSection(action, errorMessages);

        return errorMessages.isEmpty();
    }

    public void validateDataSection(@Nonnull ActionMemory action, @Nonnull List<String> errorMessages) {
        // If using the Memory tab, validate the memory variable selection.
        if (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyMemory) {
            if (_copyMemoryBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionMemory_CopyErrorMemory"));
            }
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

        selectTableSwing.validate(action.getSelectTable(), errorMessages);
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
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());

        if (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyMemory) {
            Memory otherMemory = _copyMemoryBeanPanel.getNamedBean();
            if (otherMemory != null) {
                NamedBeanHandle<Memory> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
                action.getSelectOtherMemoryNamedBean().setNamedBean(handle);
            } else {
                action.getSelectOtherMemoryNamedBean().removeNamedBean();
            }
        } else {
            action.getSelectOtherMemoryNamedBean().removeNamedBean();
        }

        try {
            if (_tabbedPaneMemoryOperation.getSelectedComponent() == _setToNull) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.SetToNull);
            } else if (_tabbedPaneMemoryOperation.getSelectedComponent() == _setToConstant) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.SetToString);
                action.setOtherConstantValue(_setToConstantTextField.getText());
            } else if (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyMemory) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.CopyMemoryToMemory);
            } else if (_tabbedPaneMemoryOperation.getSelectedComponent() == _copyTableCell) {
                action.setMemoryOperation(ActionMemory.MemoryOperation.CopyTableCellToMemory);
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

        selectTableSwing.updateObject(action.getSelectTable());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionMemory_Short");
    }

    /** {@inheritDoc} */
    @Override
    public boolean canClose() {
        return selectTableSwing.canClose();
    }

    @Override
    public void dispose() {
        selectTableSwing.dispose();
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionMemorySwing.class);

}
