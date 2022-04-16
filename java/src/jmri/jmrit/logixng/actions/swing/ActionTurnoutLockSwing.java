package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionTurnoutLock;
import jmri.jmrit.logixng.actions.ActionTurnoutLock.TurnoutLock;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionTurnoutLock object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionTurnoutLockSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneTurnout;
    private BeanSelectPanel<Turnout> turnoutBeanPanel;
    private JPanel _panelTurnoutDirect;
    private JPanel _panelTurnoutReference;
    private JPanel _panelTurnoutLocalVariable;
    private JPanel _panelTurnoutFormula;
    private JTextField _turnoutReferenceTextField;
    private JTextField _turnoutLocalVariableTextField;
    private JTextField _turnoutFormulaTextField;

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

        panel = new JPanel();

        _tabbedPaneTurnout = new JTabbedPane();
        _panelTurnoutDirect = new javax.swing.JPanel();
        _panelTurnoutReference = new javax.swing.JPanel();
        _panelTurnoutLocalVariable = new javax.swing.JPanel();
        _panelTurnoutFormula = new javax.swing.JPanel();

        _tabbedPaneTurnout.addTab(NamedBeanAddressing.Direct.toString(), _panelTurnoutDirect);
        _tabbedPaneTurnout.addTab(NamedBeanAddressing.Reference.toString(), _panelTurnoutReference);
        _tabbedPaneTurnout.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelTurnoutLocalVariable);
        _tabbedPaneTurnout.addTab(NamedBeanAddressing.Formula.toString(), _panelTurnoutFormula);

        turnoutBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(TurnoutManager.class), null);
        _panelTurnoutDirect.add(turnoutBeanPanel);

        _turnoutReferenceTextField = new JTextField();
        _turnoutReferenceTextField.setColumns(30);
        _panelTurnoutReference.add(_turnoutReferenceTextField);

        _turnoutLocalVariableTextField = new JTextField();
        _turnoutLocalVariableTextField.setColumns(30);
        _panelTurnoutLocalVariable.add(_turnoutLocalVariableTextField);

        _turnoutFormulaTextField = new JTextField();
        _turnoutFormulaTextField.setColumns(30);
        _panelTurnoutFormula.add(_turnoutFormulaTextField);


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
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneTurnout.setSelectedComponent(_panelTurnoutDirect); break;
                case Reference: _tabbedPaneTurnout.setSelectedComponent(_panelTurnoutReference); break;
                case LocalVariable: _tabbedPaneTurnout.setSelectedComponent(_panelTurnoutLocalVariable); break;
                case Formula: _tabbedPaneTurnout.setSelectedComponent(_panelTurnoutFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getTurnout() != null) {
                turnoutBeanPanel.setDefaultNamedBean(action.getTurnout().getBean());
            }
            _turnoutReferenceTextField.setText(action.getReference());
            _turnoutLocalVariableTextField.setText(action.getLocalVariable());
            _turnoutFormulaTextField.setText(action.getFormula());

            switch (action.getLockAddressing()) {
                case Direct: _tabbedPaneTurnoutLock.setSelectedComponent(_panelTurnoutLockDirect); break;
                case Reference: _tabbedPaneTurnoutLock.setSelectedComponent(_panelTurnoutLockReference); break;
                case LocalVariable: _tabbedPaneTurnoutLock.setSelectedComponent(_panelTurnoutLockLocalVariable); break;
                case Formula: _tabbedPaneTurnoutLock.setSelectedComponent(_panelTurnoutLockFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            _stateComboBox.setSelectedItem(action.getTurnoutLock());
            _turnoutLockReferenceTextField.setText(action.getLockReference());
            _turnoutLockLocalVariableTextField.setText(action.getLockLocalVariable());
            _turnoutLockFormulaTextField.setText(action.getLockFormula());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneTurnout,
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
            if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutReference) {
                action.setReference(_turnoutReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            if (_tabbedPaneTurnoutLock.getSelectedComponent() == _panelTurnoutLockReference) {
                action.setLockReference(_turnoutLockReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            action.setFormula(_turnoutFormulaTextField.getText());
            if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
            return false;
        }
        return true;
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
        if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutDirect) {
            Turnout turnout = turnoutBeanPanel.getNamedBean();
            if (turnout != null) {
                NamedBeanHandle<Turnout> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(turnout.getDisplayName(), turnout);
                action.setTurnout(handle);
            } else {
                action.removeTurnout();
            }
        } else {
            action.removeTurnout();
        }
        try {
            if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_turnoutReferenceTextField.getText());
            } else if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_turnoutLocalVariableTextField.getText());
            } else if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_turnoutFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneTurnout has unknown selection");
            }

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
        if (turnoutBeanPanel != null) {
            turnoutBeanPanel.dispose();
        }
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTurnoutLockSwing.class);

}
