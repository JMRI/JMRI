package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Light;
import jmri.LightManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionLight;
import jmri.jmrit.logixng.actions.ActionLight.LightState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionLight object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionLightSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectNamedBeanSwing<Light> _selectNamedBeanSwing;

    private JTabbedPane _tabbedPaneLightState;
    private JComboBox<LightState> _stateComboBox;
    private JPanel _panelLightStateDirect;
    private JPanel _panelLightStateReference;
    private JPanel _panelLightStateLocalVariable;
    private JPanel _panelLightStateFormula;
    private JTextField _lightStateReferenceTextField;
    private JTextField _lightStateLocalVariableTextField;
    private JTextField _lightStateFormulaTextField;

    private JTabbedPane _tabbedPaneData;
    private JPanel _panelDataDirect;
    private JPanel _panelDataReference;
    private JPanel _panelDataLocalVariable;
    private JPanel _panelDataFormula;
    private JTextField _lightDataDirectTextField;
    private JTextField _lightDataReferenceTextField;
    private JTextField _lightDataLocalVariableTextField;
    private JTextField _lightDataFormulaTextField;


    public ActionLightSwing() {
    }

    public ActionLightSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionLight action = (ActionLight)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(LightManager.class), getJDialog(), this);

        panel = new JPanel();

        JPanel _tabbedPaneNamedBean;
        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }

        _tabbedPaneLightState = new JTabbedPane();
        _panelLightStateDirect = new javax.swing.JPanel();
        _panelLightStateReference = new javax.swing.JPanel();
        _panelLightStateLocalVariable = new javax.swing.JPanel();
        _panelLightStateFormula = new javax.swing.JPanel();

        _tabbedPaneLightState.addTab(NamedBeanAddressing.Direct.toString(), _panelLightStateDirect);
        _tabbedPaneLightState.addTab(NamedBeanAddressing.Reference.toString(), _panelLightStateReference);
        _tabbedPaneLightState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLightStateLocalVariable);
        _tabbedPaneLightState.addTab(NamedBeanAddressing.Formula.toString(), _panelLightStateFormula);

        _stateComboBox = new JComboBox<>();
        for (LightState e : LightState.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);
        _stateComboBox.addActionListener((java.awt.event.ActionEvent e) -> {
            setDataPanelState();
        });

        _panelLightStateDirect.add(_stateComboBox);

        _lightStateReferenceTextField = new JTextField();
        _lightStateReferenceTextField.setColumns(30);
        _panelLightStateReference.add(_lightStateReferenceTextField);

        _lightStateLocalVariableTextField = new JTextField();
        _lightStateLocalVariableTextField.setColumns(30);
        _panelLightStateLocalVariable.add(_lightStateLocalVariableTextField);

        _lightStateFormulaTextField = new JTextField();
        _lightStateFormulaTextField.setColumns(30);
        _panelLightStateFormula.add(_lightStateFormulaTextField);


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

        _lightDataDirectTextField = new JTextField();
        _lightDataDirectTextField.setColumns(30);
        _panelDataDirect.add(_lightDataDirectTextField);

        _lightDataReferenceTextField = new JTextField();
        _lightDataReferenceTextField.setColumns(30);
        _panelDataReference.add(_lightDataReferenceTextField);

        _lightDataLocalVariableTextField = new JTextField();
        _lightDataLocalVariableTextField.setColumns(30);
        _panelDataLocalVariable.add(_lightDataLocalVariableTextField);

        _lightDataFormulaTextField = new JTextField();
        _lightDataFormulaTextField.setColumns(30);
        _panelDataFormula.add(_lightDataFormulaTextField);

        setDataPanelState();

        if (action != null) {
            switch (action.getStateAddressing()) {
                case Direct: _tabbedPaneLightState.setSelectedComponent(_panelLightStateDirect); break;
                case Reference: _tabbedPaneLightState.setSelectedComponent(_panelLightStateReference); break;
                case LocalVariable: _tabbedPaneLightState.setSelectedComponent(_panelLightStateLocalVariable); break;
                case Formula: _tabbedPaneLightState.setSelectedComponent(_panelLightStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getStateAddressing().name());
            }
            _stateComboBox.setSelectedItem(action.getBeanState());
            _lightStateReferenceTextField.setText(action.getStateReference());
            _lightStateLocalVariableTextField.setText(action.getStateLocalVariable());
            _lightStateFormulaTextField.setText(action.getStateFormula());

            switch (action.getDataAddressing()) {
                case Direct: _tabbedPaneData.setSelectedComponent(_panelDataDirect); break;
                case Reference: _tabbedPaneData.setSelectedComponent(_panelDataReference); break;
                case LocalVariable: _tabbedPaneData.setSelectedComponent(_panelDataLocalVariable); break;
                case Formula: _tabbedPaneData.setSelectedComponent(_panelDataFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getDataAddressing().name());
            }
            _lightDataReferenceTextField.setText(action.getDataReference());
            _lightDataLocalVariableTextField.setText(action.getDataLocalVariable());
            _lightDataFormulaTextField.setText(action.getDataFormula());

            _lightDataDirectTextField.setText(Integer.toString(action.getLightValue()));
            setDataPanelState();
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _tabbedPaneLightState,
            _tabbedPaneData};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionLight_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setDataPanelState() {
        boolean newState = _stateComboBox.getSelectedItem() == LightState.Intensity ||
                _stateComboBox.getSelectedItem() == LightState.Interval;
        _tabbedPaneData.setEnabled(newState);
        _lightDataDirectTextField.setEnabled(newState);
        _lightDataReferenceTextField.setEnabled(newState);
        _lightDataLocalVariableTextField.setEnabled(newState);
        _lightDataFormulaTextField.setEnabled(newState);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionLight action = new ActionLight("IQDA1", null);

        try {
            if (_tabbedPaneLightState.getSelectedComponent() == _panelLightStateReference) {
                action.setStateReference(_lightStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            if (_tabbedPaneData.getSelectedComponent() == _panelDataReference) {
                action.setDataReference(_lightDataReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            action.setDataFormula(_lightDataFormulaTextField.getText());
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
            return false;
        }

        if (_tabbedPaneData.getSelectedComponent() == _panelDataDirect) {
            LightState oper = _stateComboBox.getItemAt(_stateComboBox.getSelectedIndex());

            if (oper == LightState.Intensity) {
                boolean result = true;
                try {
                    int value = Integer.parseInt(_lightDataDirectTextField.getText());
                    if (value < 0 || value > 100) {
                        result = false;
                    }
                } catch (NumberFormatException ex) {
                    result = false;
                }
                if (!result) {
                    errorMessages.add(Bundle.getMessage("Light_Error_Intensity"));
                    return false;
                }
            }

            if (oper == LightState.Interval) {
                boolean result = true;
                try {
                    int value = Integer.parseInt(_lightDataDirectTextField.getText());
                    if (value < 0) {
                        result = false;
                    }
                } catch (NumberFormatException ex) {
                    result = false;
                }
                if (!result) {
                    errorMessages.add(Bundle.getMessage("Light_Error_Interval"));
                    return false;
                }
            }
        }

        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionLight action = new ActionLight(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionLight)) {
            throw new IllegalArgumentException("object must be an ActionLight but is a: "+object.getClass().getName());
        }
        ActionLight action = (ActionLight)object;
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());
        try {
            if (_tabbedPaneLightState.getSelectedComponent() == _panelLightStateDirect) {
                action.setStateAddressing(NamedBeanAddressing.Direct);
                action.setBeanState(_stateComboBox.getItemAt(_stateComboBox.getSelectedIndex()));
            } else if (_tabbedPaneLightState.getSelectedComponent() == _panelLightStateReference) {
                action.setStateAddressing(NamedBeanAddressing.Reference);
                action.setStateReference(_lightStateReferenceTextField.getText());
            } else if (_tabbedPaneLightState.getSelectedComponent() == _panelLightStateLocalVariable) {
                action.setStateAddressing(NamedBeanAddressing.LocalVariable);
                action.setStateLocalVariable(_lightStateLocalVariableTextField.getText());
            } else if (_tabbedPaneLightState.getSelectedComponent() == _panelLightStateFormula) {
                action.setStateAddressing(NamedBeanAddressing.Formula);
                action.setStateFormula(_lightStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneLightState has unknown selection");
            }

            // Right section
            if (_tabbedPaneData.getSelectedComponent() == _panelDataDirect) {
                action.setDataAddressing(NamedBeanAddressing.Direct);
                // Handle optional data field
                if (action.getBeanState() == LightState.Intensity ||
                        action.getBeanState() == LightState.Interval) {
                    int value;
                    try {
                        value = Integer.parseInt(_lightDataDirectTextField.getText());
                    } catch (NumberFormatException ex) {
                        value = 0;
                    }
                    action.setLightValue(value);
                }
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataReference) {
                action.setDataAddressing(NamedBeanAddressing.Reference);
                action.setDataReference(_lightDataReferenceTextField.getText());
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataLocalVariable) {
                action.setDataAddressing(NamedBeanAddressing.LocalVariable);
                action.setDataLocalVariable(_lightDataLocalVariableTextField.getText());
            } else if (_tabbedPaneData.getSelectedComponent() == _panelDataFormula) {
                action.setDataAddressing(NamedBeanAddressing.Formula);
                action.setDataFormula(_lightDataFormulaTextField.getText());
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
        return Bundle.getMessage("Light_Short");
    }

    @Override
    public void dispose() {
        // Do nothing
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLightSwing.class);

}
