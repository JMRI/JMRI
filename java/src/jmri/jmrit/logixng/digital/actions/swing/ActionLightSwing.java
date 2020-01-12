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
import jmri.Light;
import jmri.LightManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.digital.actions.ActionLight;
import jmri.jmrit.logixng.digital.actions.ActionLight.LightState;
import jmri.util.swing.BeanSelectCreatePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures an ActionLight object with a Swing JPanel.
 */
public class ActionLightSwing extends AbstractActionSwing {

    private BeanSelectCreatePanel<Light> lightBeanPanel;
    private JComboBox<LightState> stateComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionLight action = (ActionLight)object;
        
        panel = new JPanel();
        lightBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(LightManager.class), null);
        
        stateComboBox = new JComboBox<>();
        for (LightState e : LightState.values()) {
            stateComboBox.addItem(e);
        }
        
        if (action != null) {
            if (action.getLight() != null) {
                lightBeanPanel.setDefaultNamedBean(action.getLight().getBean());
            }
            stateComboBox.setSelectedItem(action.getLightState());
        }
        
        panel.add(new JLabel(Bundle.getMessage("BeanNameLight")));
        panel.add(lightBeanPanel);
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
        ActionLight action = new ActionLight(systemName, userName);
        try {
            if (!lightBeanPanel.isEmpty()) {
                Light light = lightBeanPanel.getNamedBean();
                if (light != null) {
                    NamedBeanHandle<Light> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(light.getDisplayName(), light);
                    action.setLight(handle);
                }
            }
            action.setLightState((LightState)stateComboBox.getSelectedItem());
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for light", ex);
        }
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionLight)) {
            throw new IllegalArgumentException("object must be an ActionLight but is a: "+object.getClass().getName());
        }
        ActionLight action = (ActionLight)object;
        try {
            Light light = lightBeanPanel.getNamedBean();
            if (light != null) {
                NamedBeanHandle<Light> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(light.getDisplayName(), light);
                action.setLight(handle);
            }
            action.setLightState((LightState)stateComboBox.getSelectedItem());
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for light", ex);
        }
    }
    
    
    /**
     * Create Light object for the action
     *
     * @param reference Light application description
     * @return The new output as Light object
     */
    protected Light getLightFromPanel(String reference) {
        if (lightBeanPanel == null) {
            return null;
        }
        lightBeanPanel.setReference(reference); // pass light application description to be put into light Comment
        try {
            return lightBeanPanel.getNamedBean();
        } catch (jmri.JmriException ex) {
            log.warn("skipping creation of light not found for " + reference);
            return null;
        }
    }
    
//    private void noLightMessage(String s1, String s2) {
//        log.warn("Could not provide light " + s2);
//        String msg = Bundle.getMessage("WarningNoLight", new Object[]{s1, s2});
//        JOptionPane.showMessageDialog(editFrame, msg,
//                Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
//    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Light_Short");
    }
    
    @Override
    public void dispose() {
        if (lightBeanPanel != null) {
            lightBeanPanel.dispose();
        }
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(ActionLightSwing.class);
    
}
