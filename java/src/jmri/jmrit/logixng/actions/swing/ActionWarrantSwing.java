package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionWarrant;
import jmri.jmrit.logixng.actions.ActionWarrant.DirectOperation;
import jmri.jmrit.logixng.actions.ActionWarrant.ControlAutoTrain;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionWarrant object with a Swing JPanel.
 *
 * @author Daniel Bergqvist  Copyright 2021
 * @author Dave Sand         Copyright 2021
 */
public class ActionWarrantSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectNamedBeanSwing<Warrant> _selectNamedBeanSwing;

    private LogixNG_SelectEnumSwing<DirectOperation> _selectOperationSwing;

    private JTabbedPane _tabbedPaneData;
    private JPanel _panelDataDirect;
    private JPanel _panelDataReference;
    private JPanel _panelDataLocalVariable;
    private JPanel _panelDataFormula;
    private JTextField _warrantDataReferenceTextField;
    private JTextField _warrantDataLocalVariableTextField;
    private JTextField _warrantDataFormulaTextField;

    private JPanel _panelControlTrainCombo;
    private JTextField _trainIdNameTextField;
    private JComboBox<ControlAutoTrain> _controlTrainComboBox;


    public ActionWarrantSwing() {
    }

    public ActionWarrantSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionWarrant action = (ActionWarrant)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(WarrantManager.class), getJDialog(), this);

        _selectOperationSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        panel = new JPanel();

        // Left and center section
        JPanel _tabbedPaneNamedBean;
        JPanel _tabbedPaneOperation;

        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
            _tabbedPaneOperation = _selectOperationSwing.createPanel(action.getSelectEnum(), DirectOperation.values());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
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

        _trainIdNameTextField = new JTextField();
        _trainIdNameTextField.setColumns(30);
        _panelDataDirect.add(_trainIdNameTextField);

        _controlTrainComboBox = new JComboBox<>();
        for (ControlAutoTrain e : ControlAutoTrain.values()) {
            _controlTrainComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_controlTrainComboBox);
        _panelControlTrainCombo = new JPanel();
        _panelControlTrainCombo.add(_controlTrainComboBox);
        _panelDataDirect.add(_panelControlTrainCombo);

        _warrantDataReferenceTextField = new JTextField();
        _warrantDataReferenceTextField.setColumns(30);
        _panelDataReference.add(_warrantDataReferenceTextField);

        _warrantDataLocalVariableTextField = new JTextField();
        _warrantDataLocalVariableTextField.setColumns(30);
        _panelDataLocalVariable.add(_warrantDataLocalVariableTextField);

        _warrantDataFormulaTextField = new JTextField();
        _warrantDataFormulaTextField.setColumns(30);
        _panelDataFormula.add(_warrantDataFormulaTextField);

        setDataPanelState();


        if (action != null) {
            switch (action.getDataAddressing()) {
                case Direct: _tabbedPaneData.setSelectedComponent(_panelDataDirect); break;
                case Reference: _tabbedPaneData.setSelectedComponent(_panelDataReference); break;
                case LocalVariable: _tabbedPaneData.setSelectedComponent(_panelDataLocalVariable); break;
                case Formula: _tabbedPaneData.setSelectedComponent(_panelDataFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getDataAddressing().name());
            }
            _warrantDataReferenceTextField.setText(action.getDataReference());
            _warrantDataLocalVariableTextField.setText(action.getDataLocalVariable());
            _warrantDataFormulaTextField.setText(action.getDataFormula());

            _trainIdNameTextField.setText(action.getTrainIdName());
            _controlTrainComboBox.setSelectedItem(action.getControlAutoTrain());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _tabbedPaneOperation,
            _tabbedPaneData};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionWarrant_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setDataPanelState() {
        boolean newState =
                _selectOperationSwing.isEnumSelectedOrIndirectAddressing(DirectOperation.SetTrainId) ||
                _selectOperationSwing.isEnumSelectedOrIndirectAddressing(DirectOperation.SetTrainName) ||
                _selectOperationSwing.isEnumSelectedOrIndirectAddressing(DirectOperation.ControlAutoTrain);
        _tabbedPaneData.setEnabled(newState);
        _warrantDataReferenceTextField.setEnabled(newState);
        _warrantDataLocalVariableTextField.setEnabled(newState);
        _warrantDataFormulaTextField.setEnabled(newState);

        _controlTrainComboBox.setEnabled(newState);
        _trainIdNameTextField.setEnabled(newState);

        if (_selectOperationSwing.isEnumSelectedOrIndirectAddressing(
                DirectOperation.ControlAutoTrain)) {
            _controlTrainComboBox.setVisible(true);
            _trainIdNameTextField.setVisible(false);
        } else {
            _controlTrainComboBox.setVisible(false);
            _trainIdNameTextField.setVisible(true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        ActionWarrant action = new ActionWarrant("IQDA1", null);
        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        _selectOperationSwing.validate(action.getSelectEnum(), errorMessages);
        validateDataSection(errorMessages);
        return errorMessages.isEmpty();
    }

    private void validateDataSection(List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionWarrant action = new ActionWarrant("IQDA3", null);

        try {
            if (_tabbedPaneData.getSelectedComponent() == _panelDataReference) {
                action.setDataReference(_warrantDataReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return;
        }

        try {
            action.setDataFormula(_warrantDataFormulaTextField.getText());
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
            if (_selectOperationSwing.isEnumSelectedOrIndirectAddressing(DirectOperation.SetTrainId)
                    || _selectOperationSwing.isEnumSelectedOrIndirectAddressing(DirectOperation.SetTrainName)) {
                if (_trainIdNameTextField.getText().isEmpty()) {
                    errorMessages.add(Bundle.getMessage("ActionWarrant_ErrorValue"));
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionWarrant action = new ActionWarrant(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionWarrant)) {
            throw new IllegalArgumentException("object must be an ActionWarrant but is a: "+object.getClass().getName());
        }
        ActionWarrant action = (ActionWarrant) object;
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());
        _selectOperationSwing.updateObject(action.getSelectEnum());

        try {
            // Right section
            if (_tabbedPaneData.getSelectedComponent() == _panelDataDirect) {
                action.setDataAddressing(NamedBeanAddressing.Direct);
                // Handle optional data field
                if (_selectOperationSwing.isEnumSelectedOrIndirectAddressing(DirectOperation.SetTrainId)
                        || _selectOperationSwing.isEnumSelectedOrIndirectAddressing(DirectOperation.SetTrainName)) {
                    action.setTrainIdName(_trainIdNameTextField.getText());
                } else if (_selectOperationSwing.isEnumSelectedOrIndirectAddressing(DirectOperation.ControlAutoTrain)) {
                    action.setControlAutoTrain((ControlAutoTrain) _controlTrainComboBox.getSelectedItem());
                }
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataReference) {
                action.setDataAddressing(NamedBeanAddressing.Reference);
                action.setDataReference(_warrantDataReferenceTextField.getText());
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataLocalVariable) {
                action.setDataAddressing(NamedBeanAddressing.LocalVariable);
                action.setDataLocalVariable(_warrantDataLocalVariableTextField.getText());
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataFormula) {
                action.setDataAddressing(NamedBeanAddressing.Formula);
                action.setDataFormula(_warrantDataFormulaTextField.getText());
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
        return Bundle.getMessage("ActionWarrant_Short");
    }

    @Override
    public void dispose() {
        _selectNamedBeanSwing.dispose();
        _selectOperationSwing.dispose();
    }


//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionWarrantSwing.class);

}
