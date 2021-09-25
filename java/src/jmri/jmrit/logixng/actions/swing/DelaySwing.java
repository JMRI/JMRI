package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.Delay;
import jmri.jmrit.logixng.actions.Delay.TimeUnit;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;

/**
 * Configures an Delay object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class DelaySwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneTime;
    private JTextField _timeTextField;
    private JPanel _panelTimeDirect;
    private JPanel _panelTimeReference;
    private JPanel _panelTimeLocalVariable;
    private JPanel _panelTimeFormula;
    private JTextField _timeReferenceTextField;
    private JTextField _timeLocalVariableTextField;
    private JTextField _timeFormulaTextField;

    private JTabbedPane _tabbedPaneTimeUnit;
    private JComboBox<TimeUnit> _timeUnitComboBox;
    private JPanel _panelTimeUnitDirect;
    private JPanel _panelTimeUnitReference;
    private JPanel _panelTimeUnitLocalVariable;
    private JPanel _panelTimeUnitFormula;
    private JTextField _timeUnitReferenceTextField;
    private JTextField _timeUnitLocalVariableTextField;
    private JTextField _timeUnitFormulaTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        Delay action = (Delay)object;

        panel = new JPanel();

        _tabbedPaneTime = new JTabbedPane();
        _panelTimeDirect = new javax.swing.JPanel();
        _panelTimeReference = new javax.swing.JPanel();
        _panelTimeLocalVariable = new javax.swing.JPanel();
        _panelTimeFormula = new javax.swing.JPanel();

        _tabbedPaneTime.addTab(NamedBeanAddressing.Direct.toString(), _panelTimeDirect);
        _tabbedPaneTime.addTab(NamedBeanAddressing.Reference.toString(), _panelTimeReference);
        _tabbedPaneTime.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelTimeLocalVariable);
        _tabbedPaneTime.addTab(NamedBeanAddressing.Formula.toString(), _panelTimeFormula);

        _timeTextField = new JTextField("0");
        _timeTextField.setColumns(10);
        _panelTimeDirect.add(_timeTextField);

        _timeReferenceTextField = new JTextField();
        _timeReferenceTextField.setColumns(30);
        _panelTimeReference.add(_timeReferenceTextField);

        _timeLocalVariableTextField = new JTextField();
        _timeLocalVariableTextField.setColumns(30);
        _panelTimeLocalVariable.add(_timeLocalVariableTextField);

        _timeFormulaTextField = new JTextField();
        _timeFormulaTextField.setColumns(30);
        _panelTimeFormula.add(_timeFormulaTextField);


        _tabbedPaneTimeUnit = new JTabbedPane();
        _panelTimeUnitDirect = new javax.swing.JPanel();
        _panelTimeUnitReference = new javax.swing.JPanel();
        _panelTimeUnitLocalVariable = new javax.swing.JPanel();
        _panelTimeUnitFormula = new javax.swing.JPanel();

        _tabbedPaneTimeUnit.addTab(NamedBeanAddressing.Direct.toString(), _panelTimeUnitDirect);
        _tabbedPaneTimeUnit.addTab(NamedBeanAddressing.Reference.toString(), _panelTimeUnitReference);
        _tabbedPaneTimeUnit.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelTimeUnitLocalVariable);
        _tabbedPaneTimeUnit.addTab(NamedBeanAddressing.Formula.toString(), _panelTimeUnitFormula);

        _timeUnitComboBox = new JComboBox<>();
        for (TimeUnit e : TimeUnit.values()) {
            _timeUnitComboBox.addItem(e);
        }

        _panelTimeUnitDirect.add(_timeUnitComboBox);

        _timeUnitReferenceTextField = new JTextField();
        _timeUnitReferenceTextField.setColumns(30);
        _panelTimeUnitReference.add(_timeUnitReferenceTextField);

        _timeUnitLocalVariableTextField = new JTextField();
        _timeUnitLocalVariableTextField.setColumns(30);
        _panelTimeUnitLocalVariable.add(_timeUnitLocalVariableTextField);

        _timeUnitFormulaTextField = new JTextField();
        _timeUnitFormulaTextField.setColumns(30);
        _panelTimeUnitFormula.add(_timeUnitFormulaTextField);


        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneTime.setSelectedComponent(_panelTimeDirect); break;
                case Reference: _tabbedPaneTime.setSelectedComponent(_panelTimeReference); break;
                case LocalVariable: _tabbedPaneTime.setSelectedComponent(_panelTimeLocalVariable); break;
                case Formula: _tabbedPaneTime.setSelectedComponent(_panelTimeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            _timeTextField.setText(Integer.toString(action.getTime()));
            _timeReferenceTextField.setText(action.getReference());
            _timeLocalVariableTextField.setText(action.getLocalVariable());
            _timeFormulaTextField.setText(action.getFormula());

            switch (action.getTimeUnitAddressing()) {
                case Direct: _tabbedPaneTimeUnit.setSelectedComponent(_panelTimeUnitDirect); break;
                case Reference: _tabbedPaneTimeUnit.setSelectedComponent(_panelTimeUnitReference); break;
                case LocalVariable: _tabbedPaneTimeUnit.setSelectedComponent(_panelTimeUnitLocalVariable); break;
                case Formula: _tabbedPaneTimeUnit.setSelectedComponent(_panelTimeUnitFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            _timeUnitComboBox.setSelectedItem(action.getTimeUnit());
            _timeUnitReferenceTextField.setText(action.getTimeUnitReference());
            _timeUnitLocalVariableTextField.setText(action.getTimeUnitLocalVariable());
            _timeUnitFormulaTextField.setText(action.getTimeUnitFormula());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneTime,
            _tabbedPaneTimeUnit};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("Delay_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        Delay action = new Delay("IQDA1", null);

        try {
            if (_tabbedPaneTime.getSelectedComponent() == _panelTimeDirect) {
                Integer.parseInt(_timeTextField.getText());
            }
        } catch (NumberFormatException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            if (_tabbedPaneTime.getSelectedComponent() == _panelTimeReference) {
                action.setReference(_timeReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            if (_tabbedPaneTimeUnit.getSelectedComponent() == _panelTimeUnitReference) {
                action.setTimeUnitReference(_timeUnitReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            action.setFormula(_timeFormulaTextField.getText());
            if (_tabbedPaneTime.getSelectedComponent() == _panelTimeDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneTime.getSelectedComponent() == _panelTimeReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneTime.getSelectedComponent() == _panelTimeLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneTime.getSelectedComponent() == _panelTimeFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        Delay action = new Delay(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof Delay)) {
            throw new IllegalArgumentException("object must be an Delay but is a: "+object.getClass().getName());
        }
        Delay action = (Delay)object;
        if (_tabbedPaneTime.getSelectedComponent() == _panelTimeDirect) {
            action.setTime(Integer.parseInt(_timeTextField.getText()));
            if (_timeTextField != null) {
                try {
                    action.setTime(Integer.parseInt(_timeTextField.getText()));
                } catch (NumberFormatException e) {
                    action.setTime(0);
                }
            } else {
                action.setTime(0);
            }
        } else {
            action.setTime(0);
        }
        try {
            if (_tabbedPaneTime.getSelectedComponent() == _panelTimeDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneTime.getSelectedComponent() == _panelTimeReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_timeReferenceTextField.getText());
            } else if (_tabbedPaneTime.getSelectedComponent() == _panelTimeLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_timeLocalVariableTextField.getText());
            } else if (_tabbedPaneTime.getSelectedComponent() == _panelTimeFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_timeFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneTime has unknown selection");
            }

            if (_tabbedPaneTimeUnit.getSelectedComponent() == _panelTimeUnitDirect) {
                action.setTimeUnitAddressing(NamedBeanAddressing.Direct);
                action.setTimeUnit(_timeUnitComboBox.getItemAt(_timeUnitComboBox.getSelectedIndex()));
            } else if (_tabbedPaneTimeUnit.getSelectedComponent() == _panelTimeUnitReference) {
                action.setTimeUnitAddressing(NamedBeanAddressing.Reference);
                action.setTimeUnitReference(_timeUnitReferenceTextField.getText());
            } else if (_tabbedPaneTimeUnit.getSelectedComponent() == _panelTimeUnitLocalVariable) {
                action.setTimeUnitAddressing(NamedBeanAddressing.LocalVariable);
                action.setTimeUnitLocalVariable(_timeUnitLocalVariableTextField.getText());
            } else if (_tabbedPaneTimeUnit.getSelectedComponent() == _panelTimeUnitFormula) {
                action.setTimeUnitAddressing(NamedBeanAddressing.Formula);
                action.setTimeUnitFormula(_timeUnitFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneTimeUnit has unknown selection");
            }

        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Delay_Short");
    }

    @Override
    public void dispose() {
        // Do nothing
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DelaySwing.class);

}
