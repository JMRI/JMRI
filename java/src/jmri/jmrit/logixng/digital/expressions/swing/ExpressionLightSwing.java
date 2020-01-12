package jmri.jmrit.logixng.digital.expressions.swing;

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
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.digital.expressions.ExpressionLight;
import jmri.jmrit.logixng.digital.expressions.ExpressionLight.LightState;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.util.swing.BeanSelectCreatePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures an ExpressionLight object with a Swing JPanel.
 */
public class ExpressionLightSwing extends AbstractExpressionSwing {

    private BeanSelectCreatePanel<Light> lightBeanPanel;
    private JComboBox<Is_IsNot_Enum> is_IsNot_ComboBox;
    private JComboBox<LightState> stateComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ExpressionLight)) {
            throw new IllegalArgumentException("object must be an ExpressionLight but is a: "+object.getClass().getName());
        }
        ExpressionLight expression = (ExpressionLight)object;
        
        panel = new JPanel();
        lightBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(LightManager.class), null);
        
        is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            is_IsNot_ComboBox.addItem(e);
        }
        
        stateComboBox = new JComboBox<>();
        for (LightState e : LightState.values()) {
            stateComboBox.addItem(e);
        }
        
        if (expression != null) {
            if (expression.getLight() != null) {
                lightBeanPanel.setDefaultNamedBean(expression.getLight().getBean());
            }
            is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());
            stateComboBox.setSelectedItem(expression.getLightState());
        }
        
        panel.add(new JLabel(Bundle.getMessage("BeanNameLight")));
        panel.add(lightBeanPanel);
        panel.add(is_IsNot_ComboBox);
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
        ExpressionLight expression = new ExpressionLight(systemName, userName);
        try {
            if (!lightBeanPanel.isEmpty()) {
                Light turnout = lightBeanPanel.getNamedBean();
                if (turnout != null) {
                    NamedBeanHandle<Light> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(turnout.getDisplayName(), turnout);
                    expression.setLight(handle);
                }
            }
            expression.set_Is_IsNot((Is_IsNot_Enum)is_IsNot_ComboBox.getSelectedItem());
            expression.setLightState((LightState)stateComboBox.getSelectedItem());
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for turnout", ex);
        }
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionLight)) {
            throw new IllegalArgumentException("object must be an ExpressionLight but is a: "+object.getClass().getName());
        }
        ExpressionLight expression = (ExpressionLight)object;
        try {
            Light turnout = lightBeanPanel.getNamedBean();
            if (turnout != null) {
                NamedBeanHandle<Light> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(turnout.getDisplayName(), turnout);
                expression.setLight(handle);
            }
            expression.set_Is_IsNot((Is_IsNot_Enum)is_IsNot_ComboBox.getSelectedItem());
            expression.setLightState((LightState)stateComboBox.getSelectedItem());
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for turnout", ex);
        }
    }
    
    
    /**
     * Create Light object for the expression
     *
     * @param reference Light application description
     * @return The new output as Light object
     */
    protected Light getLightFromPanel(String reference) {
        if (lightBeanPanel == null) {
            return null;
        }
        lightBeanPanel.setReference(reference); // pass turnout application description to be put into turnout Comment
        try {
            return lightBeanPanel.getNamedBean();
        } catch (jmri.JmriException ex) {
            log.warn("skipping creation of turnout not found for " + reference);
            return null;
        }
    }
    
//    private void noLightMessage(String s1, String s2) {
//        log.warn("Could not provide turnout " + s2);
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
    
    
    private final static Logger log = LoggerFactory.getLogger(ExpressionLightSwing.class);
    
}
