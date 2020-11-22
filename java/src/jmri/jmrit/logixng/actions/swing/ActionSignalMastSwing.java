package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionSignalMast;
import jmri.util.swing.BeanSelectCreatePanel;

/**
 * Configures an ActionSignalMast object with a Swing JPanel.
 */
public class ActionSignalMastSwing extends AbstractDigitalActionSwing {

    private BeanSelectCreatePanel<SignalMast> signalMastBeanPanel;
    private JComboBox<ActionSignalMast.OperationType> queryTypeComboBox;
    private JComboBox<String> signalMastAspectComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ActionSignalMast)) {
            throw new IllegalArgumentException("object must be an ActionSignalMast but is a: "+object.getClass().getName());
        }
        ActionSignalMast action = (ActionSignalMast)object;
        
        panel = new JPanel();
        signalMastBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(SignalMastManager.class), null);
        
        signalMastAspectComboBox = new JComboBox<>();
        
        queryTypeComboBox = new JComboBox<>();
        for (ActionSignalMast.OperationType e : ActionSignalMast.OperationType.values()) {
            queryTypeComboBox.addItem(e);
        }
        
        if (action != null) {
            if (action.getSignalMast() != null) {
                signalMastBeanPanel.setDefaultNamedBean(action.getSignalMast().getBean());
            }
            queryTypeComboBox.setSelectedItem(action.getOperationType());
        }
        
        if ((action != null) && (action.getSignalMast() != null)) {
            SignalMast sm = action.getSignalMast().getBean();
            
            for (String aspect : sm.getValidAspects()) {
                signalMastAspectComboBox.addItem(aspect);
                if (aspect.equals(action.getAspect())) signalMastAspectComboBox.setSelectedItem(aspect);
            }
        }
        panel.add(new JLabel(Bundle.getMessage("BeanNameSignalMast")));
        panel.add(signalMastBeanPanel);
        panel.add(queryTypeComboBox);
        panel.add(signalMastAspectComboBox);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionSignalMast action = new ActionSignalMast(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionSignalMast)) {
            throw new IllegalArgumentException("object must be an ActionSignalMast but is a: "+object.getClass().getName());
        }
        ActionSignalMast action = (ActionSignalMast)object;
        if (!signalMastBeanPanel.isEmpty()) {
            try {
                SignalMast signalMast = signalMastBeanPanel.getNamedBean();
                if (signalMast != null) {
                    NamedBeanHandle<SignalMast> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(signalMast.getDisplayName(), signalMast);
                    action.setSignalMast(handle);
                }
            } catch (JmriException ex) {
                log.error("Cannot get NamedBeanHandle for signalMast", ex);
            }
        }
        
        action.setOperationType(queryTypeComboBox.getItemAt(queryTypeComboBox.getSelectedIndex()));
        if (signalMastAspectComboBox.getItemCount() > 0) {
            action.setAspect(signalMastAspectComboBox.getItemAt(signalMastAspectComboBox.getSelectedIndex()));
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("SignalMast_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalMastSwing.class);
    
}
