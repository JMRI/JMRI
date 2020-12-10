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

    private JTabbedPane _tabbedPane;
    private BeanSelectCreatePanel<Turnout> turnoutBeanPanel;
    private JComboBox<TurnoutState> stateComboBox;
    private JPanel _panelDirect;
    private JPanel _panelReference;
    private JPanel _panelLocalVariable;
    private JPanel _panelFormula;
    private JTextField _referenceTextField;
    private JTextField _localVariableTextField;
    private JTextField _formulaTextField;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionTurnout action = (ActionTurnout)object;
        
        panel = new JPanel();
        
        stateComboBox = new JComboBox<>();
        for (TurnoutState e : TurnoutState.values()) {
            stateComboBox.addItem(e);
        }
        
        _tabbedPane = new JTabbedPane();
        _panelDirect = new javax.swing.JPanel();
        _panelReference = new javax.swing.JPanel();
        _panelLocalVariable = new javax.swing.JPanel();
        _panelFormula = new javax.swing.JPanel();
        
        _tabbedPane.addTab(NamedBeanAddressing.Direct.toString(), _panelDirect);
        _tabbedPane.addTab(NamedBeanAddressing.Reference.toString(), _panelReference);
        _tabbedPane.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLocalVariable);
        _tabbedPane.addTab(NamedBeanAddressing.Formula.toString(), _panelFormula);
        
        turnoutBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(TurnoutManager.class), null);
        _panelDirect.add(turnoutBeanPanel);
        
        _referenceTextField = new JTextField();
        _referenceTextField.setColumns(30);
        _panelReference.add(_referenceTextField);
        
        _localVariableTextField = new JTextField();
        _localVariableTextField.setColumns(30);
        _panelLocalVariable.add(_localVariableTextField);
        
        _formulaTextField = new JTextField();
        _formulaTextField.setColumns(30);
        _panelFormula.add(_formulaTextField);
        
        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPane.setSelectedComponent(_panelDirect); break;
                case Reference: _tabbedPane.setSelectedComponent(_panelReference); break;
                case LocalVariable: _tabbedPane.setSelectedComponent(_panelLocalVariable); break;
                case Formula: _tabbedPane.setSelectedComponent(_panelFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getTurnout() != null) {
                turnoutBeanPanel.setDefaultNamedBean(action.getTurnout().getBean());
            }
            stateComboBox.setSelectedItem(action.getTurnoutState());
            _referenceTextField.setText(action.getReference());
            _localVariableTextField.setText(action.getLocalVariable());
            _formulaTextField.setText(action.getFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _tabbedPane,
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
            if (_tabbedPane.getSelectedComponent() == _panelReference) {
                action.setReference(_referenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            action.setFormula(_formulaTextField.getText());
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
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
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
            action.setReference(_referenceTextField.getText());
            action.setLocalVariable(_localVariableTextField.getText());
            action.setFormula(_formulaTextField.getText());
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
