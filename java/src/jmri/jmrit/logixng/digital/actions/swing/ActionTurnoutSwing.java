package jmri.jmrit.logixng.digital.actions.swing;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.digital.actions.ActionTurnout;
import jmri.jmrit.logixng.digital.actions.ActionTurnout.TurnoutState;
import jmri.util.swing.BeanSelectCreatePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 */
public class ActionTurnoutSwing extends AbstractActionSwing {

    private BeanSelectCreatePanel<Turnout> turnoutBeanPanel;
    private JComboBox<TurnoutState> stateComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionTurnout action = (ActionTurnout)object;
        
        panel = new JPanel();
        turnoutBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(TurnoutManager.class), null);
        
        stateComboBox = new JComboBox<>();
        for (TurnoutState e : TurnoutState.values()) {
            stateComboBox.addItem(e);
        }
        
        if (action != null) {
            if (action.getTurnout() != null) {
                turnoutBeanPanel.setDefaultNamedBean(action.getTurnout().getBean());
            }
            stateComboBox.setSelectedItem(action.getTurnoutState());
        }
        
        panel.add(new JLabel(Bundle.getMessage("BeanNameTurnout")));
        panel.add(turnoutBeanPanel);
        panel.add(stateComboBox);
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
    
    
    private final static Logger log = LoggerFactory.getLogger(ActionTurnoutSwing.class);
    
}
