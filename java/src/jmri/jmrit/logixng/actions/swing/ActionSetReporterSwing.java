package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionSetReporter;
import jmri.jmrit.logixng.actions.ActionSetReporter.ReporterOperation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectTableSwing;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.BeanSelectPanel;

/**
 * Configures an ActionSetReporter object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public class ActionSetReporterSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectTableSwing selectTableSwing;

    private LogixNG_SelectNamedBeanSwing<Reporter> _selectNamedBeanSwing;

    private JTabbedPane _tabbedPaneReporterOperation;
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
    private JCheckBox _provideAnIdTag;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionSetReporter action = (ActionSetReporter)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(ReporterManager.class), getJDialog(), this);

        selectTableSwing = new LogixNG_SelectTableSwing(getJDialog(), this);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel innerPanel = new JPanel();

        JPanel _tabbedPaneNamedBean;
        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }

        _tabbedPaneReporterOperation = new JTabbedPane();

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

        _tabbedPaneReporterOperation.addTab(ReporterOperation.SetToNull.toString(), _setToNull);
        _tabbedPaneReporterOperation.addTab(ReporterOperation.SetToString.toString(), _setToConstant);
        _tabbedPaneReporterOperation.addTab(ReporterOperation.CopyMemoryToReporter.toString(), _copyMemory);
        _tabbedPaneReporterOperation.addTab(ReporterOperation.CopyTableCellToReporter.toString(), _copyTableCell);
        _tabbedPaneReporterOperation.addTab(ReporterOperation.CopyVariableToReporter.toString(), _copyVariable);
        _tabbedPaneReporterOperation.addTab(ReporterOperation.CalculateFormula.toString(), _calculateFormula);

        _setToNull.add(new JLabel("Null"));     // No I18N

        _setToConstantTextField = new JTextField(30);
        _setToConstant.add(_setToConstantTextField);

        _copyMemoryBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _copyMemory.add(_copyMemoryBeanPanel);

        _copyLocalVariableTextField = new JTextField(30);
        _copyVariable.add(_copyLocalVariableTextField);

        _calculateFormulaTextField = new JTextField(30);
        _calculateFormula.add(_calculateFormulaTextField);

        _provideAnIdTag = new JCheckBox(Bundle.getMessage("ActionSetReporter_ProvideAnIdTag"));


        if (action != null) {
            if (action.getSelectOtherMemoryNamedBean().getNamedBean() != null) {
                _copyMemoryBeanPanel.setDefaultNamedBean(action.getSelectOtherMemoryNamedBean().getNamedBean().getBean());
            }
            switch (action.getReporterOperation()) {
                case SetToNull: _tabbedPaneReporterOperation.setSelectedComponent(_setToNull); break;
                case SetToString: _tabbedPaneReporterOperation.setSelectedComponent(_setToConstant); break;
                case CopyMemoryToReporter: _tabbedPaneReporterOperation.setSelectedComponent(_copyMemory); break;
                case CopyTableCellToReporter: _tabbedPaneReporterOperation.setSelectedComponent(_copyTableCell); break;
                case CopyVariableToReporter: _tabbedPaneReporterOperation.setSelectedComponent(_copyVariable); break;
                case CalculateFormula: _tabbedPaneReporterOperation.setSelectedComponent(_calculateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getReporterOperation().name());
            }
            _setToConstantTextField.setText(action.getConstantValue());
            _copyLocalVariableTextField.setText(action.getOtherLocalVariable());
            _calculateFormulaTextField.setText(action.getOtherFormula());
            _provideAnIdTag.setSelected(action.isProvideAnIdTag());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _tabbedPaneReporterOperation
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionSetReporter_Components"), components);

        for (JComponent c : componentList) innerPanel.add(c);

        panel.add(innerPanel);
        panel.add(_provideAnIdTag);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionSetReporter action = new ActionSetReporter("IQDA2", null);

        try {
            action.setMemoryOperation(ReporterOperation.CalculateFormula);
            action.setOtherFormula(_calculateFormulaTextField.getText());
        } catch (ParserException e) {
            errorMessages.add(e.getMessage());
        }

        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        validateDataSection(action, errorMessages);

        return errorMessages.isEmpty();
    }

    public void validateDataSection(@Nonnull ActionSetReporter action, @Nonnull List<String> errorMessages) {
        // If using the Memory tab, validate the memory variable selection.
        if (_tabbedPaneReporterOperation.getSelectedComponent() == _copyMemory) {
            if (_copyMemoryBeanPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionSetReporter_CopyErrorMemory"));
            }
        }

        // Validate formula parsing via setFormula and tab selection.
        try {
            action.setOtherFormula(_calculateFormulaTextField.getText());
            if (_tabbedPaneReporterOperation.getSelectedComponent() == _calculateFormula) {
                action.setMemoryOperation(ActionSetReporter.ReporterOperation.CalculateFormula);
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }

        selectTableSwing.validate(action.getSelectTable(), errorMessages);
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionSetReporter action = new ActionSetReporter(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionSetReporter)) {
            throw new IllegalArgumentException("object must be an ActionSetReporter but is a: "+object.getClass().getName());
        }
        ActionSetReporter action = (ActionSetReporter)object;
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());

        if (_tabbedPaneReporterOperation.getSelectedComponent() == _copyMemory) {
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
            if (_tabbedPaneReporterOperation.getSelectedComponent() == _setToNull) {
                action.setMemoryOperation(ActionSetReporter.ReporterOperation.SetToNull);
            } else if (_tabbedPaneReporterOperation.getSelectedComponent() == _setToConstant) {
                action.setMemoryOperation(ActionSetReporter.ReporterOperation.SetToString);
                action.setOtherConstantValue(_setToConstantTextField.getText());
            } else if (_tabbedPaneReporterOperation.getSelectedComponent() == _copyMemory) {
                action.setMemoryOperation(ActionSetReporter.ReporterOperation.CopyMemoryToReporter);
            } else if (_tabbedPaneReporterOperation.getSelectedComponent() == _copyTableCell) {
                action.setMemoryOperation(ActionSetReporter.ReporterOperation.CopyTableCellToReporter);
            } else if (_tabbedPaneReporterOperation.getSelectedComponent() == _copyVariable) {
                action.setMemoryOperation(ActionSetReporter.ReporterOperation.CopyVariableToReporter);
                action.setOtherLocalVariable(_copyLocalVariableTextField.getText());
            } else if (_tabbedPaneReporterOperation.getSelectedComponent() == _calculateFormula) {
                action.setMemoryOperation(ActionSetReporter.ReporterOperation.CalculateFormula);
                action.setOtherFormula(_calculateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneMemoryOperation has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }

        selectTableSwing.updateObject(action.getSelectTable());

        action.setProvideAnIdTag(_provideAnIdTag.isSelected());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionSetReporter_Short");
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

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSetReporterSwing.class);

}
