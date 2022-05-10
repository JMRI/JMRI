package jmri.jmrit.logixng.actions.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionDispatcher;
import jmri.jmrit.logixng.actions.ActionDispatcher.DirectOperation;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.DispatcherActiveTrainManager;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
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

    private LogixNG_SelectEnumSwing<DirectOperation> _selectOperationSwing;

    private JTabbedPane _tabbedPaneData;
    private JPanel _panelDataDirect;
    private JPanel _panelDataReference;
    private JPanel _panelDataLocalVariable;
    private JPanel _panelDataFormula;

    JLabel _priorityLabel;
    JLabel _resetLabel;
    JLabel _terminateLabel;
    private JSpinner _priority;
    private JCheckBox _resetOption;
    private JCheckBox _terminateOption;
    private JTextField _dispatcherDataReferenceTextField;
    private JTextField _dispatcherDataLocalVariableTextField;
    private JTextField _dispatcherDataFormulaTextField;


    public ActionDispatcherSwing() {
    }

    public ActionDispatcherSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionDispatcher action = (ActionDispatcher)object;

        _selectOperationSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

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
        InstanceManager.getDefault(DispatcherActiveTrainManager.class).getTrainInfoFileNames().forEach(name -> {
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
        JPanel _tabbedPaneOperation;

        if (action != null) {
            _tabbedPaneOperation = _selectOperationSwing.createPanel(action.getSelectEnum(), DirectOperation.values());
        } else {
            _tabbedPaneOperation = _selectOperationSwing.createPanel(null, DirectOperation.values());
        }


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


        _priorityLabel = new JLabel(Bundle.getMessage("ActionDispatcher_Priority"));
        _priority = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1));
        _resetLabel = new JLabel(Bundle.getMessage("ActionDispatcher_Reset"));
        _resetOption = new JCheckBox();
        _terminateLabel = new JLabel(Bundle.getMessage("ActionDispatcher_Terminate"));
        _terminateOption = new JCheckBox();

        JPanel dataGroup = new JPanel();
        dataGroup.setLayout(new GridBagLayout());
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.gridwidth = 1;
        constraint.gridheight = 1;
        constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.anchor = GridBagConstraints.EAST;
        dataGroup.add(_priorityLabel, constraint);
        _priorityLabel.setLabelFor(_priority);
        constraint.gridy = 1;
        dataGroup.add(_resetLabel, constraint);
        _resetLabel.setLabelFor(_resetOption);
        constraint.gridy = 2;
        dataGroup.add(_terminateLabel, constraint);
        _terminateLabel.setLabelFor(_terminateOption);
        constraint.gridx = 1;
        constraint.gridy = 0;
        constraint.anchor = GridBagConstraints.WEST;
        dataGroup.add(_priority, constraint);
        constraint.gridy = 1;
        dataGroup.add(_resetOption, constraint);
        constraint.gridy = 2;
        dataGroup.add(_terminateOption, constraint);

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

        setDataPanelState();

        _selectOperationSwing.addAddressingListener((evt) -> { setDataPanelState(); });
        _selectOperationSwing.addEnumListener((evt) -> { setDataPanelState(); });

        JComponent[] components = new JComponent[]{
            _tabbedPaneDispatcher,
            _tabbedPaneOperation,
            _tabbedPaneData};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionDispatcher_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setDataPanelState() {

        _priorityLabel.setVisible(false);
        _priority.setVisible(false);
        _resetLabel.setVisible(false);
        _resetOption.setVisible(false);
        _terminateLabel.setVisible(false);
        _terminateOption.setVisible(false);

        boolean newState = false;

        if (_selectOperationSwing.isEnumSelectedOrIndirectAddressing(
                DirectOperation.TrainPriority)) {
            _priorityLabel.setVisible(true);
            _priority.setVisible(true);
            newState = true;
        }

        if (_selectOperationSwing.isEnumSelectedOrIndirectAddressing(
                DirectOperation.ResetWhenDoneOption)) {
            _resetLabel.setVisible(true);
            _resetOption.setVisible(true);
            newState = true;
        }

        if (_selectOperationSwing.isEnumSelectedOrIndirectAddressing(
                DirectOperation.TerminateWhenDoneOption)) {
            _terminateLabel.setVisible(true);
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
        // Create a temporary action to test formula
        ActionDispatcher action = new ActionDispatcher("IQDA2", null);

        validateInfoFileSection(errorMessages);
        _selectOperationSwing.validate(action.getSelectEnum(), errorMessages);

        if ((_selectOperationSwing.getAddressing() == NamedBeanAddressing.Direct)
                && (_selectOperationSwing.getEnum() == DirectOperation.None)) {
            errorMessages.add(Bundle.getMessage("ActionDispatcher_ErrorDispatcherActionNotSelected"));
        }
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

//        if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherDirect) {
//            if (_stateComboBox.getSelectedItem() == null) {
//                errorMessages.add(Bundle.getMessage("ActionDispatcher_ErrorFile"));
//            }
//        }
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
            _selectOperationSwing.updateObject(action.getSelectEnum());

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
    public void setDefaultValues() {
        _selectOperationSwing.setEnum(DirectOperation.LoadTrainFromFile);
    }

    @Override
    public void dispose() {
        _selectOperationSwing.dispose();
    }


//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionDispatcherSwing.class);

}
