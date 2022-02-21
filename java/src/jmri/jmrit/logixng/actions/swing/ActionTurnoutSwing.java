package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionTurnout;
import jmri.jmrit.logixng.actions.ActionTurnout.TurnoutState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionTurnoutSwing extends AbstractDigitalActionSwing {

    private final LogixNG_SelectNamedBeanSwing<Turnout> _selectNamedBeanSwing =
            new LogixNG_SelectNamedBeanSwing<>(InstanceManager.getDefault(TurnoutManager.class));

    private JTabbedPane _tabbedPaneTurnoutState;
    private JComboBox<TurnoutState> _stateComboBox;
    private JPanel _panelTurnoutStateDirect;
    private JPanel _panelTurnoutStateReference;
    private JPanel _panelTurnoutStateLocalVariable;
    private JPanel _panelTurnoutStateFormula;
    private JTextField _turnoutStateReferenceTextField;
    private JTextField _turnoutStateLocalVariableTextField;
    private JTextField _turnoutStateFormulaTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionTurnout action = (ActionTurnout)object;

        panel = new JPanel();

        _tabbedPaneTurnoutState = new JTabbedPane();
        _panelTurnoutStateDirect = new javax.swing.JPanel();
        _panelTurnoutStateReference = new javax.swing.JPanel();
        _panelTurnoutStateLocalVariable = new javax.swing.JPanel();
        _panelTurnoutStateFormula = new javax.swing.JPanel();

        _tabbedPaneTurnoutState.addTab(NamedBeanAddressing.Direct.toString(), _panelTurnoutStateDirect);
        _tabbedPaneTurnoutState.addTab(NamedBeanAddressing.Reference.toString(), _panelTurnoutStateReference);
        _tabbedPaneTurnoutState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelTurnoutStateLocalVariable);
        _tabbedPaneTurnoutState.addTab(NamedBeanAddressing.Formula.toString(), _panelTurnoutStateFormula);

        _stateComboBox = new JComboBox<>();
        for (TurnoutState e : TurnoutState.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);

        _panelTurnoutStateDirect.add(_stateComboBox);

        _turnoutStateReferenceTextField = new JTextField();
        _turnoutStateReferenceTextField.setColumns(30);
        _panelTurnoutStateReference.add(_turnoutStateReferenceTextField);

        _turnoutStateLocalVariableTextField = new JTextField();
        _turnoutStateLocalVariableTextField.setColumns(30);
        _panelTurnoutStateLocalVariable.add(_turnoutStateLocalVariableTextField);

        _turnoutStateFormulaTextField = new JTextField();
        _turnoutStateFormulaTextField.setColumns(30);
        _panelTurnoutStateFormula.add(_turnoutStateFormulaTextField);


        JPanel _tabbedPaneTurnout;

        if (action != null) {
            _tabbedPaneTurnout = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());

            switch (action.getStateAddressing()) {
                case Direct: _tabbedPaneTurnoutState.setSelectedComponent(_panelTurnoutStateDirect); break;
                case Reference: _tabbedPaneTurnoutState.setSelectedComponent(_panelTurnoutStateReference); break;
                case LocalVariable: _tabbedPaneTurnoutState.setSelectedComponent(_panelTurnoutStateLocalVariable); break;
                case Formula: _tabbedPaneTurnoutState.setSelectedComponent(_panelTurnoutStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getStateAddressing().name());
            }
            _stateComboBox.setSelectedItem(action.getBeanState());
            _turnoutStateReferenceTextField.setText(action.getStateReference());
            _turnoutStateLocalVariableTextField.setText(action.getStateLocalVariable());
            _turnoutStateFormulaTextField.setText(action.getStateFormula());
        } else {
            _tabbedPaneTurnout = _selectNamedBeanSwing.createPanel(null);
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneTurnout,
            _tabbedPaneTurnoutState};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionTurnout_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionTurnout action = new ActionTurnout("IQDA1", null);

        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);

        try {
            if (_tabbedPaneTurnoutState.getSelectedComponent() == _panelTurnoutStateReference) {
                action.setStateReference(_turnoutStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionTurnout action = new ActionTurnout(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionTurnout)) {
            throw new IllegalArgumentException("object must be an ActionTurnout but is a: "+object.getClass().getName());
        }
        ActionTurnout action = (ActionTurnout)object;
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());

        try {
            if (_tabbedPaneTurnoutState.getSelectedComponent() == _panelTurnoutStateDirect) {
                action.setStateAddressing(NamedBeanAddressing.Direct);
                action.setBeanState(_stateComboBox.getItemAt(_stateComboBox.getSelectedIndex()));
            } else if (_tabbedPaneTurnoutState.getSelectedComponent() == _panelTurnoutStateReference) {
                action.setStateAddressing(NamedBeanAddressing.Reference);
                action.setStateReference(_turnoutStateReferenceTextField.getText());
            } else if (_tabbedPaneTurnoutState.getSelectedComponent() == _panelTurnoutStateLocalVariable) {
                action.setStateAddressing(NamedBeanAddressing.LocalVariable);
                action.setStateLocalVariable(_turnoutStateLocalVariableTextField.getText());
            } else if (_tabbedPaneTurnoutState.getSelectedComponent() == _panelTurnoutStateFormula) {
                action.setStateAddressing(NamedBeanAddressing.Formula);
                action.setStateFormula(_turnoutStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneTurnoutState has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Turnout_Short");
    }

    @Override
    public void dispose() {
        _selectNamedBeanSwing.dispose();
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTurnoutSwing.class);

}
