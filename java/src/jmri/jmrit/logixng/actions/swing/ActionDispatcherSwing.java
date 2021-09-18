package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.dispatcher.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionDispatcher;
import jmri.jmrit.logixng.actions.ActionDispatcher.DirectOperation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.DispatcherTrainInfoManager;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionDispatcher object with a Swing JPanel.
 *
 * @author Daniel Bergqvist  Copyright 2021
 * @author Dave Sand         Copyright 2021
 */
public class ActionDispatcherSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneDispatcher;
    private JPanel _panelDispatcherDirect;
    private JPanel _panelDispatcherReference;
    private JPanel _panelDispatcherLocalVariable;
    private JPanel _panelDispatcherFormula;

    private JComboBox<String> _fileNamesComboBox;
    private JTextField _dispatcherReferenceTextField;
    private JTextField _dispatcherLocalVariableTextField;
    private JTextField _dispatcherFormulaTextField;


    private JTabbedPane _tabbedPaneOperation;
    private JPanel _panelOperationDirect;
    private JPanel _panelOperationReference;
    private JPanel _panelOperationLocalVariable;
    private JPanel _panelOperationFormula;

    private JComboBox<DirectOperation> _stateComboBox;
    private JTextField _dispatcherOperReferenceTextField;
    private JTextField _dispatcherOperLocalVariableTextField;
    private JTextField _dispatcherOperFormulaTextField;


    private JTabbedPane _tabbedPaneData;
    private JPanel _panelDataDirect;
    private JPanel _panelDataReference;
    private JPanel _panelDataLocalVariable;
    private JPanel _panelDataFormula;

    private JSpinner _priority;
    private JCheckBox _resetOption;
    private JCheckBox _terminateOption;
    private JTextField _dispatcherDataReferenceTextField;
    private JTextField _dispatcherDataLocalVariableTextField;
    private JTextField _dispatcherDataFormulaTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionDispatcher action = (ActionDispatcher)object;

        panel = new JPanel();

        // Left section
        _tabbedPaneDispatcher = new JTabbedPane();
        _panelDispatcherDirect = new javax.swing.JPanel();
        _panelDispatcherReference = new javax.swing.JPanel();
        _panelDispatcherLocalVariable = new javax.swing.JPanel();
        _panelDispatcherFormula = new javax.swing.JPanel();

        _tabbedPaneDispatcher.addTab(NamedBeanAddressing.Direct.toString(), _panelDispatcherDirect);
        _tabbedPaneDispatcher.addTab(NamedBeanAddressing.Reference.toString(), _panelDispatcherReference);
        _tabbedPaneDispatcher.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelDispatcherLocalVariable);
        _tabbedPaneDispatcher.addTab(NamedBeanAddressing.Formula.toString(), _panelDispatcherFormula);

        _fileNamesComboBox = new JComboBox<>();
        DispatcherTrainInfoManager.getTrainInfoFileNames().forEach(name -> {
            _fileNamesComboBox.addItem(name);
        });
        JComboBoxUtil.setupComboBoxMaxRows(_fileNamesComboBox);
        _panelDispatcherDirect.add(_fileNamesComboBox);

        _dispatcherReferenceTextField = new JTextField();
        _dispatcherReferenceTextField.setColumns(30);
        _panelDispatcherReference.add(_dispatcherReferenceTextField);

        _dispatcherLocalVariableTextField = new JTextField();
        _dispatcherLocalVariableTextField.setColumns(30);
        _panelDispatcherLocalVariable.add(_dispatcherLocalVariableTextField);

        _dispatcherFormulaTextField = new JTextField();
        _dispatcherFormulaTextField.setColumns(30);
        _panelDispatcherFormula.add(_dispatcherFormulaTextField);


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

        _dispatcherOperReferenceTextField = new JTextField();
        _dispatcherOperReferenceTextField.setColumns(30);
        _panelOperationReference.add(_dispatcherOperReferenceTextField);

        _dispatcherOperLocalVariableTextField = new JTextField();
        _dispatcherOperLocalVariableTextField.setColumns(30);
        _panelOperationLocalVariable.add(_dispatcherOperLocalVariableTextField);

        _dispatcherOperFormulaTextField = new JTextField();
        _dispatcherOperFormulaTextField.setColumns(30);
        _panelOperationFormula.add(_dispatcherOperFormulaTextField);


        // Right section
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

        JPanel dataGroup = new JPanel();
        _priority = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1));
        dataGroup.add(_priority);
        _resetOption = new JCheckBox();
        dataGroup.add(_resetOption);
        _terminateOption = new JCheckBox();
        dataGroup.add(_terminateOption);
        _panelDataDirect.add(dataGroup);

        _dispatcherDataReferenceTextField = new JTextField();
        _dispatcherDataReferenceTextField.setColumns(30);
        _panelDataReference.add(_dispatcherDataReferenceTextField);

        _dispatcherDataLocalVariableTextField = new JTextField();
        _dispatcherDataLocalVariableTextField.setColumns(30);
        _panelDataLocalVariable.add(_dispatcherDataLocalVariableTextField);

        _dispatcherDataFormulaTextField = new JTextField();
        _dispatcherDataFormulaTextField.setColumns(30);
        _panelDataFormula.add(_dispatcherDataFormulaTextField);

        setDataPanelState();


        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneDispatcher.setSelectedComponent(_panelDispatcherDirect); break;
                case Reference: _tabbedPaneDispatcher.setSelectedComponent(_panelDispatcherReference); break;
                case LocalVariable: _tabbedPaneDispatcher.setSelectedComponent(_panelDispatcherLocalVariable); break;
                case Formula: _tabbedPaneDispatcher.setSelectedComponent(_panelDispatcherFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            _fileNamesComboBox.setSelectedItem(action.getTrainInfoFileName());
            _dispatcherReferenceTextField.setText(action.getReference());
            _dispatcherLocalVariableTextField.setText(action.getLocalVariable());
            _dispatcherFormulaTextField.setText(action.getFormula());

            switch (action.getOperationAddressing()) {
                case Direct: _tabbedPaneOperation.setSelectedComponent(_panelOperationDirect); break;
                case Reference: _tabbedPaneOperation.setSelectedComponent(_panelOperationReference); break;
                case LocalVariable: _tabbedPaneOperation.setSelectedComponent(_panelOperationLocalVariable); break;
                case Formula: _tabbedPaneOperation.setSelectedComponent(_panelOperationFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            _stateComboBox.setSelectedItem(action.getOperationDirect());
            setDataPanelState();
            _dispatcherOperReferenceTextField.setText(action.getOperationReference());
            _dispatcherOperLocalVariableTextField.setText(action.getOperationLocalVariable());
            _dispatcherOperFormulaTextField.setText(action.getOperFormula());

            switch (action.getDataAddressing()) {
                case Direct: _tabbedPaneData.setSelectedComponent(_panelDataDirect); break;
                case Reference: _tabbedPaneData.setSelectedComponent(_panelDataReference); break;
                case LocalVariable: _tabbedPaneData.setSelectedComponent(_panelDataLocalVariable); break;
                case Formula: _tabbedPaneData.setSelectedComponent(_panelDataFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getDataAddressing().name());
            }
            _priority.setValue(action.getTrainPriority());
            _resetOption.setSelected(action.getResetOption());
            _terminateOption.setSelected(action.getTerminateOption());
            _dispatcherDataReferenceTextField.setText(action.getDataReference());
            _dispatcherDataLocalVariableTextField.setText(action.getDataLocalVariable());
            _dispatcherDataFormulaTextField.setText(action.getDataFormula());

        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneDispatcher,
            _tabbedPaneOperation,
            _tabbedPaneData};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionDispatcher_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setDataPanelState() {
        _priority.setVisible(false);
        _resetOption.setVisible(false);
        _terminateOption.setVisible(false);

        boolean newState = false;
        DirectOperation oper = _stateComboBox.getItemAt(_stateComboBox.getSelectedIndex());

        if (oper == DirectOperation.TrainPriority) {
            _priority.setVisible(true);
            newState = true;
        }

        if (oper == DirectOperation.ResetWhenDoneOption) {
            _resetOption.setVisible(true);
            newState = true;
        }

        if (oper == DirectOperation.TerminateWhenDoneOption) {
            _terminateOption.setVisible(true);
            newState = true;
        }

        _tabbedPaneData.setEnabled(newState);
        _dispatcherDataReferenceTextField.setEnabled(newState);
        _dispatcherDataLocalVariableTextField.setEnabled(newState);
        _dispatcherDataFormulaTextField.setEnabled(newState);
   }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        validateInfoFileSection(errorMessages);
        validateOperationSection(errorMessages);
        validateDataSection(errorMessages);
        return errorMessages.isEmpty();
    }

    private void validateInfoFileSection(List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionDispatcher action = new ActionDispatcher("IQDA1", null);

        try {
            if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherReference) {
                action.setReference(_dispatcherReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return;
        }

        try {
            action.setFormula(_dispatcherFormulaTextField.getText());
            if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }

        if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherDirect) {
            if (_stateComboBox.getSelectedItem() == null) {
                errorMessages.add(Bundle.getMessage("ActionDispatcher_ErrorFile"));
            }
        }
    }

    private void validateOperationSection(List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionDispatcher action = new ActionDispatcher("IQDA2", null);

        try {
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationReference(_dispatcherOperReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return;
        }

        try {
            action.setOperationFormula(_dispatcherOperFormulaTextField.getText());
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

        if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationDirect) {
            if (_stateComboBox.getSelectedIndex() < 1) {
                errorMessages.add(Bundle.getMessage("ActionDispatcher_ErrorStateAction"));
            }
        }
    }

    private void validateDataSection(List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionDispatcher action = new ActionDispatcher("IQDA3", null);

        try {
            if (_tabbedPaneData.getSelectedComponent() == _panelDataReference) {
                action.setDataReference(_dispatcherDataReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return;
        }

        try {
            action.setDataFormula(_dispatcherDataFormulaTextField.getText());
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

    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionDispatcher action = new ActionDispatcher(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionDispatcher)) {
            throw new IllegalArgumentException("object must be an ActionDispatcher but is a: "+object.getClass().getName());
        }
        ActionDispatcher action = (ActionDispatcher) object;

        try {
            // Left section
            if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
                action.setTrainInfoFileName((String) _fileNamesComboBox.getSelectedItem());
            } else if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_dispatcherReferenceTextField.getText());
            } else if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_dispatcherLocalVariableTextField.getText());
            } else if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_dispatcherFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneDispatcher has unknown selection");
            }

            // Center section
            if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationDirect) {
                action.setOperationAddressing(NamedBeanAddressing.Direct);
                action.setOperationDirect(_stateComboBox.getItemAt(_stateComboBox.getSelectedIndex()));
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationReference) {
                action.setOperationAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference(_dispatcherOperReferenceTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationLocalVariable) {
                action.setOperationAddressing(NamedBeanAddressing.LocalVariable);
                action.setOperationLocalVariable(_dispatcherOperLocalVariableTextField.getText());
            } else if (_tabbedPaneOperation.getSelectedComponent() == _panelOperationFormula) {
                action.setOperationAddressing(NamedBeanAddressing.Formula);
                action.setOperationFormula(_dispatcherOperFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneOperation has unknown selection");
            }

            // Right section
            if (_tabbedPaneData.getSelectedComponent() == _panelDataDirect) {
                action.setDataAddressing(NamedBeanAddressing.Direct);
                action.setTrainPriority((int) _priority.getValue());
                action.setResetOption(_resetOption.isSelected());
                action.setTerminateOption(_terminateOption.isSelected());
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataReference) {
                action.setDataAddressing(NamedBeanAddressing.Reference);
                action.setDataReference(_dispatcherDataReferenceTextField.getText());
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataLocalVariable) {
                action.setDataAddressing(NamedBeanAddressing.LocalVariable);
                action.setDataLocalVariable(_dispatcherDataLocalVariableTextField.getText());
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataFormula) {
                action.setDataAddressing(NamedBeanAddressing.Formula);
                action.setDataFormula(_dispatcherDataFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneData has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionDispatcher_Short");
    }

    @Override
    public void dispose() {
    }


//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionDispatcherSwing.class);

}
