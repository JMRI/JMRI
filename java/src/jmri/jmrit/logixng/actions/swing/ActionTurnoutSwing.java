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
    private JComboBox<TurnoutState> stateComboBox;
    private JPanel _panelTurnoutDirect;
    private JPanel _panelTurnoutReference;
    private JPanel _panelTurnoutLocalVariable;
    private JPanel _panelTurnoutFormula;
    private JTextField _turnoutReferenceTextField;
    private JTextField _turnoutLocalVariableTextField;
    private JTextField _turnoutFormulaTextField;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionTurnout action = (ActionTurnout)object;
        
        panel = new JPanel();
        
        stateComboBox = new JComboBox<>();
        for (TurnoutState e : TurnoutState.values()) {
            stateComboBox.addItem(e);
        }
        
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
            stateComboBox.setSelectedItem(action.getTurnoutState());
            _turnoutReferenceTextField.setText(action.getReference());
            _turnoutLocalVariableTextField.setText(action.getLocalVariable());
            _turnoutFormulaTextField.setText(action.getFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _tabbedPaneTurnout,
            stateComboBox};
        
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
            action.setTurnoutState((TurnoutState)stateComboBox.getSelectedItem());
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
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
            action.setReference(_turnoutReferenceTextField.getText());
            action.setLocalVariable(_turnoutLocalVariableTextField.getText());
            action.setFormula(_turnoutFormulaTextField.getText());
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
