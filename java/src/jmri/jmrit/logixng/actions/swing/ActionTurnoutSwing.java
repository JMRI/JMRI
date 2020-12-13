package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionTurnout;
import jmri.jmrit.logixng.actions.ActionTurnout.TurnoutState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectCreatePanel;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 */
public class ActionTurnoutSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPaneTurnout;
    private BeanSelectCreatePanel<Turnout> turnoutBeanPanel;
    private JPanel _panelTurnoutDirect;
    private JPanel _panelTurnoutReference;
    private JPanel _panelTurnoutLocalVariable;
    private JPanel _panelTurnoutFormula;
    private JTextField _turnoutReferenceTextField;
    private JTextField _turnoutLocalVariableTextField;
    private JTextField _turnoutFormulaTextField;
    
    private JTabbedPane _tabbedPaneTurnoutState;
//    private BeanSelectCreatePanel<Turnout> turnoutStateBeanPanel;
    private JComboBox<TurnoutState> stateComboBox;
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
        
        _tabbedPaneTurnout = new JTabbedPane();
        _panelTurnoutDirect = new javax.swing.JPanel();
        _panelTurnoutReference = new javax.swing.JPanel();
        _panelTurnoutLocalVariable = new javax.swing.JPanel();
        _panelTurnoutFormula = new javax.swing.JPanel();
        
        _tabbedPaneTurnout.addTab(NamedBeanAddressing.Direct.toString(), _panelTurnoutDirect);
        _tabbedPaneTurnout.addTab(NamedBeanAddressing.Reference.toString(), _panelTurnoutReference);
        _tabbedPaneTurnout.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelTurnoutLocalVariable);
        _tabbedPaneTurnout.addTab(NamedBeanAddressing.Formula.toString(), _panelTurnoutFormula);
        
        turnoutBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(TurnoutManager.class), null);
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
        
        
        _tabbedPaneTurnoutState = new JTabbedPane();
        _panelTurnoutStateDirect = new javax.swing.JPanel();
        _panelTurnoutStateReference = new javax.swing.JPanel();
        _panelTurnoutStateLocalVariable = new javax.swing.JPanel();
        _panelTurnoutStateFormula = new javax.swing.JPanel();
        
        _tabbedPaneTurnoutState.addTab(NamedBeanAddressing.Direct.toString(), _panelTurnoutStateDirect);
        _tabbedPaneTurnoutState.addTab(NamedBeanAddressing.Reference.toString(), _panelTurnoutStateReference);
        _tabbedPaneTurnoutState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelTurnoutStateLocalVariable);
        _tabbedPaneTurnoutState.addTab(NamedBeanAddressing.Formula.toString(), _panelTurnoutStateFormula);
        
        stateComboBox = new JComboBox<>();
        for (TurnoutState e : TurnoutState.values()) {
            stateComboBox.addItem(e);
        }
        
//        turnoutStateBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(TurnoutManager.class), null);
        _panelTurnoutStateDirect.add(stateComboBox);
        
        _turnoutStateReferenceTextField = new JTextField();
        _turnoutStateReferenceTextField.setColumns(30);
        _panelTurnoutStateReference.add(_turnoutStateReferenceTextField);
        
        _turnoutStateLocalVariableTextField = new JTextField();
        _turnoutStateLocalVariableTextField.setColumns(30);
        _panelTurnoutStateLocalVariable.add(_turnoutStateLocalVariableTextField);
        
        _turnoutStateFormulaTextField = new JTextField();
        _turnoutStateFormulaTextField.setColumns(30);
        _panelTurnoutStateFormula.add(_turnoutStateFormulaTextField);
        
        
        if (action != null) {
            switch (action.getTurnoutAddressing()) {
                case Direct: _tabbedPaneTurnout.setSelectedComponent(_panelTurnoutDirect); break;
                case Reference: _tabbedPaneTurnout.setSelectedComponent(_panelTurnoutReference); break;
                case LocalVariable: _tabbedPaneTurnout.setSelectedComponent(_panelTurnoutLocalVariable); break;
                case Formula: _tabbedPaneTurnout.setSelectedComponent(_panelTurnoutFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getTurnoutAddressing().name());
            }
            if (action.getTurnout() != null) {
                turnoutBeanPanel.setDefaultNamedBean(action.getTurnout().getBean());
            }
            _turnoutReferenceTextField.setText(action.getReference());
            _turnoutLocalVariableTextField.setText(action.getLocalVariable());
            _turnoutFormulaTextField.setText(action.getFormula());
            
            switch (action.getStateAddressing()) {
                case Direct: _tabbedPaneTurnoutState.setSelectedComponent(_panelTurnoutStateDirect); break;
                case Reference: _tabbedPaneTurnoutState.setSelectedComponent(_panelTurnoutStateReference); break;
                case LocalVariable: _tabbedPaneTurnoutState.setSelectedComponent(_panelTurnoutStateLocalVariable); break;
                case Formula: _tabbedPaneTurnoutState.setSelectedComponent(_panelTurnoutStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getTurnoutAddressing().name());
            }
            stateComboBox.setSelectedItem(action.getState());
            _turnoutStateReferenceTextField.setText(action.getStateReference());
            _turnoutStateLocalVariableTextField.setText(action.getStateLocalVariable());
            _turnoutStateFormulaTextField.setText(action.getStateFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _tabbedPaneTurnout,
            _tabbedPaneTurnoutState};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(Bundle.getMessage("SetTurnout"), components);
        
        for (JComponent c : componentList) panel.add(c);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionTurnout action = new ActionTurnout("IQDA1", null);
        
        try {
            if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutReference) {
                action.setReference(_turnoutReferenceTextField.getText());
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
        try {
            if (!turnoutBeanPanel.isEmpty()) {
                Turnout turnout = turnoutBeanPanel.getNamedBean();
                if (turnout != null) {
                    NamedBeanHandle<Turnout> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(turnout.getDisplayName(), turnout);
                    action.setTurnout(handle);
                }
            }
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for turnout", ex);
        }
        try {
            if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneTurnout.getSelectedComponent() == _panelTurnoutFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPaneTurnout has unknown selection");
            }
            action.setReference(_turnoutReferenceTextField.getText());
            action.setLocalVariable(_turnoutLocalVariableTextField.getText());
            action.setFormula(_turnoutFormulaTextField.getText());
            
            if (_tabbedPaneTurnoutState.getSelectedComponent() == _panelTurnoutStateDirect) {
                action.setStateAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneTurnoutState.getSelectedComponent() == _panelTurnoutStateReference) {
                action.setStateAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneTurnoutState.getSelectedComponent() == _panelTurnoutStateLocalVariable) {
                action.setStateAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneTurnoutState.getSelectedComponent() == _panelTurnoutStateFormula) {
                action.setStateAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPaneTurnoutState has unknown selection");
            }
            action.setBeanState((TurnoutState)stateComboBox.getSelectedItem());
            action.setReference(_turnoutStateReferenceTextField.getText());
            action.setLocalVariable(_turnoutStateLocalVariableTextField.getText());
            action.setFormula(_turnoutStateFormulaTextField.getText());
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }
    
    
    /**
     * Create Turnout object for the action
     *
     * @param reference Turnout application description
     * @return The new output as Turnout object
     */
    protected Turnout getTurnoutFromPanel(String reference) {
        if (turnoutBeanPanel == null) {
            return null;
        }
        turnoutBeanPanel.setReference(reference); // pass turnout application description to be put into turnout Comment
        try {
            return turnoutBeanPanel.getNamedBean();
        } catch (jmri.JmriException ex) {
            log.warn("skipping creation of turnout not found for " + reference);
            return null;
        }
    }
    
//    private void noTurnoutMessage(String s1, String s2) {
//        log.warn("Could not provide turnout " + s2);
//        String msg = Bundle.getMessage("WarningNoTurnout", new Object[]{s1, s2});
//        JOptionPane.showMessageDialog(editFrame, msg,
//                Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
//    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Turnout_Short");
    }
    
    @Override
    public void dispose() {
        if (turnoutBeanPanel != null) {
            turnoutBeanPanel.dispose();
        }
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTurnoutSwing.class);
    
}
