package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionTurnoutLock;
import jmri.jmrit.logixng.actions.ActionTurnoutLock.TurnoutLock;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionTurnoutLock object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionTurnoutLockSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectNamedBeanSwing<Turnout> _selectNamedBeanSwing;

    private JTabbedPane _tabbedPaneTurnoutLock;
    private JComboBox<TurnoutLock> _stateComboBox;
    private JPanel _panelTurnoutLockDirect;
    private JPanel _panelTurnoutLockReference;
    private JPanel _panelTurnoutLockLocalVariable;
    private JPanel _panelTurnoutLockFormula;
    private JTextField _turnoutLockReferenceTextField;
    private JTextField _turnoutLockLocalVariableTextField;
    private JTextField _turnoutLockFormulaTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionTurnoutLock action = (ActionTurnoutLock)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(TurnoutManager.class), getJDialog(), this);

        panel = new JPanel();

        JPanel _tabbedPaneNamedBean;
        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }


        _tabbedPaneTurnoutLock = new JTabbedPane();
        _panelTurnoutLockDirect = new javax.swing.JPanel();
        _panelTurnoutLockReference = new javax.swing.JPanel();
        _panelTurnoutLockLocalVariable = new javax.swing.JPanel();
        _panelTurnoutLockFormula = new javax.swing.JPanel();

        _tabbedPaneTurnoutLock.addTab(NamedBeanAddressing.Direct.toString(), _panelTurnoutLockDirect);
        _tabbedPaneTurnoutLock.addTab(NamedBeanAddressing.Reference.toString(), _panelTurnoutLockReference);
        _tabbedPaneTurnoutLock.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelTurnoutLockLocalVariable);
        _tabbedPaneTurnoutLock.addTab(NamedBeanAddressing.Formula.toString(), _panelTurnoutLockFormula);

        _stateComboBox = new JComboBox<>();
        for (TurnoutLock e : TurnoutLock.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);

        _panelTurnoutLockDirect.add(_stateComboBox);

        _turnoutLockReferenceTextField = new JTextField();
        _turnoutLockReferenceTextField.setColumns(30);
        _panelTurnoutLockReference.add(_turnoutLockReferenceTextField);

        _turnoutLockLocalVariableTextField = new JTextField();
        _turnoutLockLocalVariableTextField.setColumns(30);
        _panelTurnoutLockLocalVariable.add(_turnoutLockLocalVariableTextField);

        _turnoutLockFormulaTextField = new JTextField();
        _turnoutLockFormulaTextField.setColumns(30);
        _panelTurnoutLockFormula.add(_turnoutLockFormulaTextField);


        if (action != null) {
            switch (action.getLockAddressing()) {
                case Direct: _tabbedPaneTurnoutLock.setSelectedComponent(_panelTurnoutLockDirect); break;
                case Reference: _tabbedPaneTurnoutLock.setSelectedComponent(_panelTurnoutLockReference); break;
                case LocalVariable: _tabbedPaneTurnoutLock.setSelectedComponent(_panelTurnoutLockLocalVariable); break;
                case Formula: _tabbedPaneTurnoutLock.setSelectedComponent(_panelTurnoutLockFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getLockAddressing().name());
            }
            _stateComboBox.setSelectedItem(action.getTurnoutLock());
            _turnoutLockReferenceTextField.setText(action.getLockReference());
            _turnoutLockLocalVariableTextField.setText(action.getLockLocalVariable());
            _turnoutLockFormulaTextField.setText(action.getLockFormula());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _tabbedPaneTurnoutLock};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionTurnoutLock_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionTurnoutLock action = new ActionTurnoutLock("IQDA1", null);

        try {
            if (_tabbedPaneTurnoutLock.getSelectedComponent() == _panelTurnoutLockReference) {
                action.setLockReference(_turnoutLockReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);
        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionTurnoutLock action = new ActionTurnoutLock(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionTurnoutLock)) {
            throw new IllegalArgumentException("object must be an ActionTurnoutLock but is a: "+object.getClass().getName());
        }
        ActionTurnoutLock action = (ActionTurnoutLock)object;
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());
        try {
            if (_tabbedPaneTurnoutLock.getSelectedComponent() == _panelTurnoutLockDirect) {
                action.setLockAddressing(NamedBeanAddressing.Direct);
                action.setTurnoutLock(_stateComboBox.getItemAt(_stateComboBox.getSelectedIndex()));
            } else if (_tabbedPaneTurnoutLock.getSelectedComponent() == _panelTurnoutLockReference) {
                action.setLockAddressing(NamedBeanAddressing.Reference);
                action.setLockReference(_turnoutLockReferenceTextField.getText());
            } else if (_tabbedPaneTurnoutLock.getSelectedComponent() == _panelTurnoutLockLocalVariable) {
                action.setLockAddressing(NamedBeanAddressing.LocalVariable);
                action.setLockLocalVariable(_turnoutLockLocalVariableTextField.getText());
            } else if (_tabbedPaneTurnoutLock.getSelectedComponent() == _panelTurnoutLockFormula) {
                action.setLockAddressing(NamedBeanAddressing.Formula);
                action.setLockFormula(_turnoutLockFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneTurnoutLock has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("TurnoutLock_Short");
    }

    @Override
    public void dispose() {
        // Do nothing
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTurnoutLockSwing.class);

}
