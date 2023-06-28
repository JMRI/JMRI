package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionLight;
import jmri.jmrit.logixng.actions.ActionLight.LightState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;

/**
 * Configures an ActionLight object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionLightSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectNamedBeanSwing<Light> _selectNamedBeanSwing;

    private LogixNG_SelectEnumSwing<LightState> _selectEnumSwing;

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

        _selectEnumSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        panel = new JPanel();

        JPanel _tabbedPaneNamedBean;
        JPanel _tabbedPaneEnum;

        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
            _tabbedPaneEnum = _selectEnumSwing.createPanel(action.getSelectEnum(), LightState.values());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
            _tabbedPaneEnum = _selectEnumSwing.createPanel(null, LightState.values());
        }

        _selectEnumSwing.addEnumListener(e -> { setDataPanelState(); });

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
            _tabbedPaneEnum,
            _tabbedPaneData};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionLight_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void setDataPanelState() {
        boolean newState = _selectEnumSwing.isEnumSelectedOrIndirectAddressing(LightState.Intensity) ||
                _selectEnumSwing.isEnumSelectedOrIndirectAddressing(LightState.Interval);
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

        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        _selectEnumSwing.validate(action.getSelectEnum(), errorMessages);

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

            if (_selectEnumSwing.isEnumSelectedOrIndirectAddressing(LightState.Intensity)) {
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

            if (_selectEnumSwing.isEnumSelectedOrIndirectAddressing(LightState.Interval)) {
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
        _selectEnumSwing.updateObject(action.getSelectEnum());

        try {
            // Right section
            if (_tabbedPaneData.getSelectedComponent() == _panelDataDirect) {
                action.setDataAddressing(NamedBeanAddressing.Direct);
                // Handle optional data field
                if (_selectEnumSwing.isEnumSelectedOrIndirectAddressing(LightState.Intensity) ||
                        _selectEnumSwing.isEnumSelectedOrIndirectAddressing(LightState.Interval)) {
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
        _selectNamedBeanSwing.dispose();
        _selectEnumSwing.dispose();
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLightSwing.class);

}
