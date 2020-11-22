package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionSignalHead;
import jmri.util.swing.BeanSelectCreatePanel;

/**
 * Configures an ActionSignalHead object with a Swing JPanel.
 */
public class ActionSignalHeadSwing extends AbstractDigitalActionSwing {

    private BeanSelectCreatePanel<SignalHead> signalHeadBeanPanel;
    private JComboBox<ActionSignalHead.OperationType> queryTypeComboBox;
    private JComboBox<SignalHeadState> signalHeadStateComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ActionSignalHead)) {
            throw new IllegalArgumentException("object must be an ActionSignalHead but is a: "+object.getClass().getName());
        }
        ActionSignalHead action = (ActionSignalHead)object;
        
        panel = new JPanel();
        signalHeadBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(SignalHeadManager.class), null);
        
        signalHeadStateComboBox = new JComboBox<>();
        
        queryTypeComboBox = new JComboBox<>();
        for (ActionSignalHead.OperationType e : ActionSignalHead.OperationType.values()) {
            queryTypeComboBox.addItem(e);
        }
        
        if (action != null) {
            if (action.getSignalHead() != null) {
                signalHeadBeanPanel.setDefaultNamedBean(action.getSignalHead().getBean());
            }
            queryTypeComboBox.setSelectedItem(action.getOperationType());
        }
        
        if ((action != null) && (action.getSignalHead() != null)) {
            SignalHead sh = action.getSignalHead().getBean();
            
            int[] states = sh.getValidStates();
            for (int s : states) {
                SignalHeadState shs = new SignalHeadState();
                shs._state = s;
                shs._name = sh.getAppearanceName(s);
                signalHeadStateComboBox.addItem(shs);
                if (action.getAppearance() == s) signalHeadStateComboBox.setSelectedItem(shs);
            }
        }
        panel.add(new JLabel(Bundle.getMessage("BeanNameSignalHead")));
        panel.add(signalHeadBeanPanel);
        panel.add(queryTypeComboBox);
        panel.add(signalHeadStateComboBox);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionSignalHead action = new ActionSignalHead(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionSignalHead)) {
            throw new IllegalArgumentException("object must be an ActionSignalHead but is a: "+object.getClass().getName());
        }
        ActionSignalHead action = (ActionSignalHead)object;
        if (!signalHeadBeanPanel.isEmpty()) {
            try {
                SignalHead signalHead = signalHeadBeanPanel.getNamedBean();
                if (signalHead != null) {
                    NamedBeanHandle<SignalHead> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(signalHead.getDisplayName(), signalHead);
                    action.setSignalHead(handle);
                }
            } catch (JmriException ex) {
                log.error("Cannot get NamedBeanHandle for signalHead", ex);
            }
        }
        
        action.setOperationType(queryTypeComboBox.getItemAt(queryTypeComboBox.getSelectedIndex()));
        if (signalHeadStateComboBox.getItemCount() > 0) {
            action.setAppearance(signalHeadStateComboBox.getItemAt(signalHeadStateComboBox.getSelectedIndex())._state);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("SignalHead_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
    private static class SignalHeadState {
        
        private int _state;
        private String _name;
        
        @Override
        public String toString() {
            return _name;
        }
        
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalHeadSwing.class);
    
}
