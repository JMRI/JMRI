package jmri.jmrit.logixng.expressions.swing;

import java.util.List;
import java.util.SortedSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionReporter;
import jmri.jmrit.logixng.expressions.ExpressionReporter.CompareTo;
import jmri.jmrit.logixng.expressions.ExpressionReporter.ReporterOperation;
import jmri.jmrit.logixng.expressions.ExpressionReporter.ReporterValue;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionReporter object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class ExpressionReporterSwing extends AbstractDigitalExpressionSwing {

    private LogixNG_SelectNamedBeanSwing<Reporter> _selectNamedBeanSwing;
    private JComboBox<ReporterValue> _reporterValueComboBox;
    private JComboBox<ReporterOperation> _reporterOperationComboBox;
    private JCheckBox _caseInsensitiveCheckBox;

    private JTabbedPane _tabbedPane;

    private JTabbedPane _tabbedPaneCompareTo;
    private BeanSelectPanel<Memory> _compareToMemoryBeanPanel;
    private JPanel _reporterValuePanel;
    private JPanel _compareToConstant;
    private JPanel _compareToMemory;
    private JPanel _compareToLocalVariable;
    private JPanel _compareToRegEx;
    private JTextField _compareToConstantTextField;
    private JTextField _compareToLocalVariableTextField;
    private JTextField _compareToRegExTextField;


    private void enableDisableCompareTo() {
        ReporterOperation mo = _reporterOperationComboBox.getItemAt(
                        _reporterOperationComboBox.getSelectedIndex());
        boolean enable = mo.hasExtraValue();
        _tabbedPaneCompareTo.setEnabled(enable);
        ((JPanel)_tabbedPaneCompareTo.getSelectedComponent())
                .getComponent(0).setEnabled(enable);

        boolean regEx = (mo == ReporterOperation.MatchRegex)
                || (mo == ReporterOperation.NotMatchRegex);
        _tabbedPane.setEnabledAt(0, !regEx);
        _tabbedPane.setEnabledAt(1, regEx);
        _tabbedPane.setSelectedIndex(regEx ? 1 : 0);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionReporter expression = (ExpressionReporter)object;

        panel = new JPanel();

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(ReporterManager.class), getJDialog(), this);

        JPanel _tabbedPaneNamedBean;

        if (expression != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(expression.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }

        _reporterValuePanel = new JPanel();
        _reporterValueComboBox = new JComboBox<>();
        for (ReporterValue e : ReporterValue.values()) {
            _reporterValueComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_reporterValueComboBox);
        _reporterValuePanel.add(_reporterValueComboBox);

        JPanel operationAndCasePanel = new JPanel();
        operationAndCasePanel.setLayout(new BoxLayout(operationAndCasePanel, BoxLayout.Y_AXIS));

        _reporterOperationComboBox = new JComboBox<>();
        for (ReporterOperation e : ReporterOperation.values()) {
            _reporterOperationComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_reporterOperationComboBox);
        if (expression == null) {
            _reporterOperationComboBox.setSelectedItem(ReporterOperation.Equal);
        }
        operationAndCasePanel.add(_reporterOperationComboBox);

        _reporterOperationComboBox.addActionListener((e) -> { enableDisableCompareTo(); });

        _caseInsensitiveCheckBox = new JCheckBox(Bundle.getMessage("ExpressionReporter_CaseInsensitive"));
        operationAndCasePanel.add(_caseInsensitiveCheckBox);

        _tabbedPane = new JTabbedPane();

        _tabbedPaneCompareTo = new JTabbedPane();
        _tabbedPane.addTab("", _tabbedPaneCompareTo);

        _compareToConstant = new JPanel();
        _compareToMemory = new JPanel();
        _compareToLocalVariable = new JPanel();
        _compareToRegEx = new JPanel();

        _tabbedPaneCompareTo.addTab(CompareTo.Value.toString(), _compareToConstant);
        _tabbedPaneCompareTo.addTab(CompareTo.Memory.toString(), _compareToMemory);
        _tabbedPaneCompareTo.addTab(CompareTo.LocalVariable.toString(), _compareToLocalVariable);

        _tabbedPane.addTab(CompareTo.RegEx.toString(), _compareToRegEx);

        _compareToConstantTextField = new JTextField(30);
        _compareToConstant.add(_compareToConstantTextField);

        _compareToMemoryBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _compareToMemory.add(_compareToMemoryBeanPanel);

        _compareToLocalVariableTextField = new JTextField(30);
        _compareToLocalVariable.add(_compareToLocalVariableTextField);

        _compareToRegExTextField = new JTextField(30);
        _compareToRegEx.add(_compareToRegExTextField);


        if (expression != null) {
            if (expression.getSelectMemoryNamedBean().getNamedBean() != null) {
                _compareToMemoryBeanPanel.setDefaultNamedBean(expression.getSelectMemoryNamedBean().getNamedBean().getBean());
            }
            switch (expression.getCompareTo()) {
                case RegEx:
                case Value: _tabbedPaneCompareTo.setSelectedComponent(_compareToConstant); break;
                case Memory: _tabbedPaneCompareTo.setSelectedComponent(_compareToMemory); break;
                case LocalVariable: _tabbedPaneCompareTo.setSelectedComponent(_compareToLocalVariable); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getCompareTo().name());
            }
            _reporterValueComboBox.setSelectedItem(expression.getReporterValue());
            _reporterOperationComboBox.setSelectedItem(expression.getReporterOperation());
            _caseInsensitiveCheckBox.setSelected(expression.getCaseInsensitive());
            _compareToConstantTextField.setText(expression.getConstantValue());
            _compareToLocalVariableTextField.setText(expression.getLocalVariable());
            _compareToRegExTextField.setText(expression.getRegEx());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _reporterValuePanel,
            operationAndCasePanel,
            _tabbedPane
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionReporter_Components"), components);

        for (JComponent c : componentList) panel.add(c);

        enableDisableCompareTo();
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionReporter expression = new ExpressionReporter("IQDE1", null);

        if (_selectNamedBeanSwing.getAddressing() == NamedBeanAddressing.Direct
                && _selectNamedBeanSwing.getBean() == null) {
            errorMessages.add(Bundle.getMessage("Reporter_No_Reporter"));
        }
        _selectNamedBeanSwing.validate(expression.getSelectNamedBean(), errorMessages);
        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExpressionReporter expression = new ExpressionReporter(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionReporter)) {
            throw new IllegalArgumentException("object must be an ExpressionReporter but is a: "+object.getClass().getName());
        }
        ExpressionReporter expression = (ExpressionReporter)object;
        
        _selectNamedBeanSwing.updateObject(expression.getSelectNamedBean());

        expression.setReporterValue(_reporterValueComboBox.getItemAt(_reporterValueComboBox.getSelectedIndex()));
        expression.setReporterOperation(_reporterOperationComboBox.getItemAt(_reporterOperationComboBox.getSelectedIndex()));
        expression.setCaseInsensitive(_caseInsensitiveCheckBox.isSelected());


        if (!_compareToMemoryBeanPanel.isEmpty()
                && (_tabbedPane.getSelectedComponent() == _tabbedPaneCompareTo)
                && (_tabbedPaneCompareTo.getSelectedComponent() == _compareToMemory)) {
            Memory memory = _compareToMemoryBeanPanel.getNamedBean();
            if (memory != null) {
                NamedBeanHandle<Memory> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(memory.getDisplayName(), memory);
                expression.getSelectMemoryNamedBean().setNamedBean(handle);
            } else {
                expression.getSelectMemoryNamedBean().removeNamedBean();
            }
        } else {
            expression.getSelectMemoryNamedBean().removeNamedBean();
        }

        if (_tabbedPane.getSelectedComponent() == _tabbedPaneCompareTo) {
            if (_tabbedPaneCompareTo.getSelectedComponent() == _compareToConstant) {
                expression.setCompareTo(CompareTo.Value);
                expression.setConstantValue(_compareToConstantTextField.getText());
            } else if (_tabbedPaneCompareTo.getSelectedComponent() == _compareToMemory) {
                expression.setCompareTo(CompareTo.Memory);
            } else if (_tabbedPaneCompareTo.getSelectedComponent() == _compareToLocalVariable) {
                expression.setCompareTo(CompareTo.LocalVariable);
                expression.setLocalVariable(_compareToLocalVariableTextField.getText());
//            } else if (_tabbedPaneCompareTo.getSelectedComponent() == _compareToRegEx) {
//                expression.setCompareTo(CompareTo.RegEx);
//                expression.setRegEx(_compareToRegExTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneLight has unknown selection");
            }
        } else {
            expression.setCompareTo(CompareTo.RegEx);
            expression.setRegEx(_compareToRegExTextField.getText());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Reporter_Short");
    }

    @Override
    public void setDefaultValues() {
        if (_selectNamedBeanSwing.getBean() == null) {
            SortedSet<Reporter> set = InstanceManager.getDefault(ReporterManager.class).getNamedBeanSet();
            if (!set.isEmpty()) {
                Reporter r = set.first();
                _selectNamedBeanSwing.getBeanSelectPanel().setDefaultNamedBean(r);
            } else {
                log.error("Reporter manager has no reporters. Can't set default values");
            }
        }
    }

    @Override
    public void dispose() {
        _selectNamedBeanSwing.dispose();
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionReporterSwing.class);

}
