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
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.ActionTurnout;
import jmri.jmrit.logixng.actions.ActionTurnout.TurnoutState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.util.swing.BeanSelectCreatePanel;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 */
public class ActionTurnoutSwing extends AbstractDigitalActionSwing {

    private BeanSelectCreatePanel<Turnout> turnoutBeanPanel;
    private JComboBox<TurnoutState> stateComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionTurnout action = (ActionTurnout)object;
        
        panel = new JPanel();
        
        stateComboBox = new JComboBox<>();
        for (TurnoutState e : TurnoutState.values()) {
            stateComboBox.addItem(e);
        }
        
        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel panelTurnout = new javax.swing.JPanel();
        JPanel panelReference = new javax.swing.JPanel();
        JPanel panelFormula = new javax.swing.JPanel();
        
        tabbedPane.addTab("Turnout", panelTurnout); // NOI1aa8N
        tabbedPane.addTab("Reference", panelReference); // NOIaa18N
        tabbedPane.addTab("Formula", panelFormula); // NOI1aa8N
        
        turnoutBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(TurnoutManager.class), null);
        panelTurnout.add(turnoutBeanPanel);
        
        JTextField referenceTextField = new JTextField();
        referenceTextField.setColumns(30);
        panelReference.add(referenceTextField);
        
        JTextField formulaTextField = new JTextField();
        formulaTextField.setColumns(30);
        panelFormula.add(formulaTextField);
        
        if (action != null) {
            if (action.getTurnout() != null) {
                turnoutBeanPanel.setDefaultNamedBean(action.getTurnout().getBean());
            }
            stateComboBox.setSelectedItem(action.getTurnoutState());
        }
        
        JComponent[] components = new JComponent[]{
            tabbedPane,
            stateComboBox};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(Bundle.getMessage("SetTurnout"), components);
        
        for (JComponent c : componentList) panel.add(c);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        if (1==0) {
            errorMessages.add("An error");
            return false;
        }
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionTurnout action = new ActionTurnout(systemName, userName);
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
            Turnout turnout = turnoutBeanPanel.getNamedBean();
            if (turnout != null) {
                NamedBeanHandle<Turnout> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(turnout.getDisplayName(), turnout);
                action.setTurnout(handle);
            }
            action.setTurnoutState((TurnoutState)stateComboBox.getSelectedItem());
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for turnout", ex);
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
