package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ForEachWithDelay;
import jmri.jmrit.logixng.actions.CommonManager;
import jmri.jmrit.logixng.actions.ForEachWithDelay.UserSpecifiedSource;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectStringSwing;
import jmri.util.TimerUnit;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ForEachWithDelay object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public class ForEachWithDelaySwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectStringSwing _selectVariableSwing;
    private LogixNG_SelectNamedBeanSwing<Memory> _selectMemorySwing;

    private JTabbedPane _commonOrUserSpecifiedPane;
    private JPanel _commonPanel;
    private JComboBox<CommonManager> _commonManagersComboBox;

    private JTabbedPane _tabbedPaneUserSpecifiedSource;
    JPanel _tabbedPaneMemoryBean;
    JPanel _tabbedPaneVariable;
    private JPanel _calculateFormula;
    private JTextField _calculateFormulaTextField;

    private JTextField _localVariable;

    private JFormattedTextField _timerDelay;
    private JComboBox<TimerUnit> _unitComboBox;
    private JCheckBox _resetIfAlreadyStarted;
    private JCheckBox _useIndividualTimers;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ForEachWithDelay)) {
            throw new IllegalArgumentException("object must be an ForEachWithDelay but is a: "+object.getClass().getName());
        }

        ForEachWithDelay action = (ForEachWithDelay)object;

        _selectVariableSwing = new LogixNG_SelectStringSwing(getJDialog(), this);

        _selectMemorySwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(MemoryManager.class), getJDialog(), this);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        if (action != null) {
            _tabbedPaneMemoryBean = _selectMemorySwing.createPanel(action.getSelectMemoryNamedBean());
            _tabbedPaneVariable = _selectVariableSwing.createPanel(action.getSelectVariable());
        } else {
            _tabbedPaneMemoryBean = _selectMemorySwing.createPanel(null);
            _tabbedPaneVariable = _selectVariableSwing.createPanel(null);
        }

        _commonOrUserSpecifiedPane = new JTabbedPane();

        _commonPanel = new JPanel();
        _commonOrUserSpecifiedPane.addTab(Bundle.getMessage("ForEachWithDelaySwing_Common"), _commonPanel);
        _commonManagersComboBox = new JComboBox<>();
        for (CommonManager commonManager : CommonManager.values()) {
            _commonManagersComboBox.addItem(commonManager);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_commonManagersComboBox);

        _commonPanel.add(_commonManagersComboBox);


        _tabbedPaneUserSpecifiedSource = new JTabbedPane();
        _commonOrUserSpecifiedPane.addTab(Bundle.getMessage("ForEachWithDelaySwing_UserSpecified"), _tabbedPaneUserSpecifiedSource);

        _calculateFormula = new JPanel();

        _tabbedPaneUserSpecifiedSource.addTab(UserSpecifiedSource.Memory.toString(), _tabbedPaneMemoryBean);
        _tabbedPaneUserSpecifiedSource.addTab(UserSpecifiedSource.Variable.toString(), _tabbedPaneVariable);
        _tabbedPaneUserSpecifiedSource.addTab(UserSpecifiedSource.Formula.toString(), _calculateFormula);

        _calculateFormulaTextField = new JTextField(30);
        _calculateFormula.add(_calculateFormulaTextField);

        JPanel localVariablePanel = new JPanel();
        localVariablePanel.add(new JLabel(Bundle.getMessage("ForEachWithDelaySwing_LocalVariable")));
        _localVariable = new JTextField(20);
        localVariablePanel.add(_localVariable);
        panel.add(localVariablePanel);

        panel.add(_commonOrUserSpecifiedPane);
        panel.add(localVariablePanel);

        JPanel panelDelay = new javax.swing.JPanel();
        panelDelay.add(new JLabel(Bundle.getMessage("ForEachWithDelaySwing_Time")));
        _timerDelay = new JFormattedTextField("0");
        _timerDelay.setColumns(7);
        panelDelay.add(_timerDelay);
        panel.add(panelDelay);

        JPanel unitPanel = new JPanel();
        unitPanel.add(new JLabel(Bundle.getMessage("ForEachWithDelaySwing_Unit")));
        panel.add(unitPanel);

        _unitComboBox = new JComboBox<>();
        for (TimerUnit u : TimerUnit.values()) _unitComboBox.addItem(u);
        JComboBoxUtil.setupComboBoxMaxRows(_unitComboBox);
        if (action != null) _unitComboBox.setSelectedItem(action.getUnit());
        unitPanel.add(_unitComboBox);
        panel.add(unitPanel);

        _resetIfAlreadyStarted = new JCheckBox(Bundle.getMessage("ForEachWithDelaySwing_ResetIfAlreadyStarted"));
        panel.add(_resetIfAlreadyStarted);

        _useIndividualTimers = new JCheckBox(Bundle.getMessage("ForEachWithDelaySwing_UseIndividualTimers"));
        _useIndividualTimers.addActionListener((evt)->{
            if (_useIndividualTimers.isSelected()) {
                _resetIfAlreadyStarted.setEnabled(false);
                _resetIfAlreadyStarted.setSelected(false);
            } else {
                _resetIfAlreadyStarted.setEnabled(true);
            }
        });
        panel.add(_useIndividualTimers);


        if (action != null) {
            if (!action.isUseCommonSource()) {
                _commonOrUserSpecifiedPane.setSelectedComponent(_tabbedPaneUserSpecifiedSource);
            }
            _commonManagersComboBox.setSelectedItem(action.getCommonManager());

            switch (action.getUserSpecifiedSource()) {
                case Memory: _tabbedPaneUserSpecifiedSource.setSelectedComponent(_tabbedPaneMemoryBean); break;
                case Variable: _tabbedPaneUserSpecifiedSource.setSelectedComponent(_tabbedPaneVariable); break;
                case Formula: _tabbedPaneUserSpecifiedSource.setSelectedComponent(_calculateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getUserSpecifiedSource().name());
            }
            _calculateFormulaTextField.setText(action.getFormula());
            _timerDelay.setText(Integer.toString(action.getDelay()));
            _localVariable.setText(action.getLocalVariableName());
            _resetIfAlreadyStarted.setSelected(action.getResetIfAlreadyStarted());
            _useIndividualTimers.setSelected(action.getUseIndividualTimers());
            _resetIfAlreadyStarted.setEnabled(!action.getUseIndividualTimers());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ForEachWithDelay action = new ForEachWithDelay("IQDA1", null);

        if (_commonOrUserSpecifiedPane.getSelectedComponent() == _tabbedPaneUserSpecifiedSource) {
            // If using the Memory tab, validate the memory variable selection.
            if (_tabbedPaneUserSpecifiedSource.getSelectedComponent() == _tabbedPaneMemoryBean) {
                _selectMemorySwing.validate(action.getSelectMemoryNamedBean(), errorMessages);
            }

            // If using the Variable tab, validate the memory variable selection.
            if (_tabbedPaneUserSpecifiedSource.getSelectedComponent() == _tabbedPaneVariable) {
                _selectVariableSwing.validate(action.getSelectVariable(), errorMessages);
            }

            if (_tabbedPaneUserSpecifiedSource.getSelectedComponent() == _calculateFormula) {
                try {
                    action.setUserSpecifiedSource(UserSpecifiedSource.Formula);
                    action.setFormula(_calculateFormulaTextField.getText());
                } catch (ParserException e) {
                    errorMessages.add(e.getMessage());
                }
            }
        }

        try {
            action.setDelay(Integer.parseInt(_timerDelay.getText()));
        } catch (NumberFormatException e) {
            errorMessages.add(e.getLocalizedMessage());
        }

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ForEachWithDelay action = new ForEachWithDelay(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof ForEachWithDelay)) {
            throw new IllegalArgumentException("object must be an ForEachWithDelay but is a: "+object.getClass().getName());
        }

        ForEachWithDelay action = (ForEachWithDelay)object;

        try {
            if (_commonOrUserSpecifiedPane.getSelectedComponent() == _commonPanel) {

                action.setUseCommonSource(true);
                action.setCommonManager(_commonManagersComboBox.getItemAt(
                        _commonManagersComboBox.getSelectedIndex()));

            } else if (_commonOrUserSpecifiedPane.getSelectedComponent() == _tabbedPaneUserSpecifiedSource) {

                action.setUseCommonSource(false);

                try {
                    if (_tabbedPaneUserSpecifiedSource.getSelectedComponent() == _tabbedPaneMemoryBean) {
                        action.setUserSpecifiedSource(UserSpecifiedSource.Memory);
                        _selectMemorySwing.updateObject(action.getSelectMemoryNamedBean());
                    } else if (_tabbedPaneUserSpecifiedSource.getSelectedComponent() == _tabbedPaneVariable) {
                        action.setUserSpecifiedSource(UserSpecifiedSource.Variable);
                        _selectVariableSwing.updateObject(action.getSelectVariable());
                    } else if (_tabbedPaneUserSpecifiedSource.getSelectedComponent() == _calculateFormula) {
                        action.setUserSpecifiedSource(UserSpecifiedSource.Formula);
                        action.setFormula(_calculateFormulaTextField.getText());
                    } else {
                        throw new IllegalArgumentException("_tabbedPaneUserSpecifiedSource has unknown selection");
                    }
                } catch (ParserException e) {
                    throw new RuntimeException("ParserException: "+e.getMessage(), e);
                }

            } else {
                throw new IllegalArgumentException("_tabbedPaneUserSpecifiedSource has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }

        action.setLocalVariableName(_localVariable.getText());

        action.setDelay(Integer.parseInt(_timerDelay.getText()));
        action.setUnit(_unitComboBox.getItemAt(_unitComboBox.getSelectedIndex()));
        action.setResetIfAlreadyStarted(_resetIfAlreadyStarted.isSelected());
        action.setUseIndividualTimers(_useIndividualTimers.isSelected());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ForEachWithDelay_Short");
    }

    @Override
    public void dispose() {
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ForEachSwing.class);

}
