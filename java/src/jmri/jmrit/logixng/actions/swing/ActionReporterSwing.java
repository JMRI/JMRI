package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionReporter;
import jmri.jmrit.logixng.actions.ActionReporter.ReporterValue;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionReporter object with a Swing JPanel.
 *
 * @author Daniel Bergqvist  Copyright 2021
 * @author Dave Sand         Copyright 2021
 */
public class ActionReporterSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneBean;
    private JPanel _panelBeanDirect;
    private JPanel _panelBeanReference;
    private JPanel _panelBeanLocalVariable;
    private JPanel _panelBeanFormula;

    private BeanSelectPanel<Reporter> _beanSelectPanel;
    private JTextField _beanReferenceTextField;
    private JTextField _beanLocalVariableTextField;
    private JTextField _beanFormulaTextField;


    private JPanel _panelReporterValue;
    private JComboBox<ReporterValue> _reporterValueComboBox;


    private JTabbedPane _tabbedPaneData;
    private JPanel _panelDataDirect;
    private JPanel _panelDataReference;
    private JPanel _panelDataLocalVariable;
    private JPanel _panelDataFormula;

    private BeanSelectPanel<Memory> _memorySelectPanel;
    private JTextField _dataReferenceTextField;
    private JTextField _dataLocalVariableTextField;
    private JTextField _dataFormulaTextField;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionReporter action = (ActionReporter)object;

        panel = new JPanel();

        // Left section
        _panelReporterValue = new javax.swing.JPanel();
        _reporterValueComboBox = new JComboBox<>();
        for (ReporterValue e : ReporterValue.values()) {
            _reporterValueComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_reporterValueComboBox);
        _panelReporterValue.add(_reporterValueComboBox);

        // Center section
        _tabbedPaneBean = new JTabbedPane();
        _panelBeanDirect = new javax.swing.JPanel();
        _panelBeanReference = new javax.swing.JPanel();
        _panelBeanLocalVariable = new javax.swing.JPanel();
        _panelBeanFormula = new javax.swing.JPanel();

        _tabbedPaneBean.addTab(NamedBeanAddressing.Direct.toString(), _panelBeanDirect);
        _tabbedPaneBean.addTab(NamedBeanAddressing.Reference.toString(), _panelBeanReference);
        _tabbedPaneBean.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelBeanLocalVariable);
        _tabbedPaneBean.addTab(NamedBeanAddressing.Formula.toString(), _panelBeanFormula);

        _beanSelectPanel = new BeanSelectPanel<>(InstanceManager.getDefault(ReporterManager.class), null);
        _panelBeanDirect.add(_beanSelectPanel);

        _beanReferenceTextField = new JTextField();
        _beanReferenceTextField.setColumns(30);
        _panelBeanReference.add(_beanReferenceTextField);

        _beanLocalVariableTextField = new JTextField();
        _beanLocalVariableTextField.setColumns(30);
        _panelBeanLocalVariable.add(_beanLocalVariableTextField);

        _beanFormulaTextField = new JTextField();
        _beanFormulaTextField.setColumns(30);
        _panelBeanFormula.add(_beanFormulaTextField);

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

        _memorySelectPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _panelDataDirect.add(_memorySelectPanel);

        _dataReferenceTextField = new JTextField();
        _dataReferenceTextField.setColumns(30);
        _panelDataReference.add(_dataReferenceTextField);

        _dataLocalVariableTextField = new JTextField();
        _dataLocalVariableTextField.setColumns(30);
        _panelDataLocalVariable.add(_dataLocalVariableTextField);

        _dataFormulaTextField = new JTextField();
        _dataFormulaTextField.setColumns(30);
        _panelDataFormula.add(_dataFormulaTextField);


        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneBean.setSelectedComponent(_panelBeanDirect); break;
                case Reference: _tabbedPaneBean.setSelectedComponent(_panelBeanReference); break;
                case LocalVariable: _tabbedPaneBean.setSelectedComponent(_panelBeanLocalVariable); break;
                case Formula: _tabbedPaneBean.setSelectedComponent(_panelBeanFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getReporter() != null) {
                _beanSelectPanel.setDefaultNamedBean(action.getReporter().getBean());
            }
            _beanReferenceTextField.setText(action.getReference());
            _beanLocalVariableTextField.setText(action.getLocalVariable());
            _beanFormulaTextField.setText(action.getFormula());

            _reporterValueComboBox.setSelectedItem(action.getReporterValue());

            switch (action.getDataAddressing()) {
                case Direct: _tabbedPaneData.setSelectedComponent(_panelDataDirect); break;
                case Reference: _tabbedPaneData.setSelectedComponent(_panelDataReference); break;
                case LocalVariable: _tabbedPaneData.setSelectedComponent(_panelDataLocalVariable); break;
                case Formula: _tabbedPaneData.setSelectedComponent(_panelDataFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getDataAddressing().name());
            }
            if (action.getMemory() != null) {
                _memorySelectPanel.setDefaultNamedBean(action.getMemory().getBean());
            }
            _dataReferenceTextField.setText(action.getDataReference());
            _dataLocalVariableTextField.setText(action.getDataLocalVariable());
            _dataFormulaTextField.setText(action.getDataFormula());
        }

        JComponent[] components = new JComponent[]{
            _panelReporterValue,
            _tabbedPaneBean,
            _tabbedPaneData};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionReporter_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        validateBeanSection(errorMessages);
        validateDataSection(errorMessages);
        return errorMessages.isEmpty();
    }

    private void validateBeanSection(List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionReporter action = new ActionReporter("IQDA1", null);

        try {
            if (_tabbedPaneBean.getSelectedComponent() == _panelBeanReference) {
                action.setReference(_beanReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return;
        }

        try {
            action.setFormula(_beanFormulaTextField.getText());
            if (_tabbedPaneBean.getSelectedComponent() == _panelBeanDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneBean.getSelectedComponent() == _panelBeanReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneBean.getSelectedComponent() == _panelBeanLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneBean.getSelectedComponent() == _panelBeanFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }

        if (_tabbedPaneBean.getSelectedComponent() == _panelBeanDirect) {
            if (_beanSelectPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionReporter_ErrorReporter"));
            }
        }
    }

    private void validateDataSection(List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionReporter action = new ActionReporter("IQDA3", null);

        try {
            if (_tabbedPaneData.getSelectedComponent() == _panelDataReference) {
                action.setDataReference(_dataReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return;
        }

        try {
            action.setDataFormula(_dataFormulaTextField.getText());
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
            if (_memorySelectPanel.getNamedBean() == null) {
                errorMessages.add(Bundle.getMessage("ActionReporter_ErrorMemory"));
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionReporter action = new ActionReporter(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionReporter)) {
            throw new IllegalArgumentException("object must be an ActionReporter but is a: "+object.getClass().getName());
        }
        ActionReporter action = (ActionReporter) object;

        if (_tabbedPaneBean.getSelectedComponent() == _panelBeanDirect) {
            Reporter reporter = _beanSelectPanel.getNamedBean();
            if (reporter != null) {
                NamedBeanHandle<Reporter> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(reporter.getDisplayName(), reporter);
                action.setReporter(handle);
            } else {
                action.removeReporter();
            }
        } else {
            action.removeReporter();
        }

        try {
            // Left section
            action.setReporterValue(_reporterValueComboBox.getItemAt(_reporterValueComboBox.getSelectedIndex()));

            // Center section
            if (_tabbedPaneBean.getSelectedComponent() == _panelBeanDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneBean.getSelectedComponent() == _panelBeanReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_beanReferenceTextField.getText());
            } else if (_tabbedPaneBean.getSelectedComponent() == _panelBeanLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_beanLocalVariableTextField.getText());
            } else if (_tabbedPaneBean.getSelectedComponent() == _panelBeanFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_beanFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneBean has unknown selection");
            }

            // Right section
            action.removeMemory();
            if (_tabbedPaneData.getSelectedComponent() == _panelDataDirect) {
                action.setDataAddressing(NamedBeanAddressing.Direct);
                Memory memory = _memorySelectPanel.getNamedBean();
                if (memory != null) {
                    NamedBeanHandle<Memory> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(memory.getDisplayName(), memory);
                    action.setMemory(handle);
                }
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataReference) {
                action.setDataAddressing(NamedBeanAddressing.Reference);
                action.setDataReference(_dataReferenceTextField.getText());
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataLocalVariable) {
                action.setDataAddressing(NamedBeanAddressing.LocalVariable);
                action.setDataLocalVariable(_dataLocalVariableTextField.getText());
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataFormula) {
                action.setDataAddressing(NamedBeanAddressing.Formula);
                action.setDataFormula(_dataFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneBean has unknown selection");
            }

        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionReporter_Short");
    }

    @Override
    public void dispose() {
        if (_beanSelectPanel != null) {
            _beanSelectPanel.dispose();
        }
        if (_memorySelectPanel != null) {
            _memorySelectPanel.dispose();
        }
    }


//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionReporterSwing.class);

}
