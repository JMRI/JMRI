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
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.digital.expressions.ExpressionSensor;
import jmri.jmrit.logixng.digital.expressions.ExpressionSensor.SensorState;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.util.swing.BeanSelectCreatePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures an ExpressionSensor object with a Swing JPanel.
 */
public class ExpressionSensorSwing extends AbstractExpressionSwing {

    private BeanSelectCreatePanel<Sensor> sensorBeanPanel;
    private JComboBox<Is_IsNot_Enum> is_IsNot_ComboBox;
    private JComboBox<SensorState> stateComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ExpressionSensor)) {
            throw new IllegalArgumentException("object must be an ExpressionSensor but is a: "+object.getClass().getName());
        }
        ExpressionSensor expression = (ExpressionSensor)object;
        
        panel = new JPanel();
        sensorBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(SensorManager.class), null);
        
        is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            is_IsNot_ComboBox.addItem(e);
        }
        
        stateComboBox = new JComboBox<>();
        for (SensorState e : SensorState.values()) {
            stateComboBox.addItem(e);
        }
        
        if (expression != null) {
            if (expression.getSensor() != null) {
                sensorBeanPanel.setDefaultNamedBean(expression.getSensor().getBean());
            }
            is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());
            stateComboBox.setSelectedItem(expression.getSensorState());
        }
        
        panel.add(new JLabel(Bundle.getMessage("BeanNameSensor")));
        panel.add(sensorBeanPanel);
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
        ExpressionSensor expression = new ExpressionSensor(systemName, userName);
        try {
            if (!sensorBeanPanel.isEmpty()) {
                Sensor sensor = sensorBeanPanel.getNamedBean();
                if (sensor != null) {
                    NamedBeanHandle<Sensor> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(sensor.getDisplayName(), sensor);
                    expression.setSensor(handle);
                }
            }
            expression.set_Is_IsNot((Is_IsNot_Enum)is_IsNot_ComboBox.getSelectedItem());
            expression.setSensorState((SensorState)stateComboBox.getSelectedItem());
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for turnout", ex);
        }
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionSensor)) {
            throw new IllegalArgumentException("object must be an ExpressionSensor but is a: "+object.getClass().getName());
        }
        ExpressionSensor expression = (ExpressionSensor)object;
        try {
            Sensor turnout = sensorBeanPanel.getNamedBean();
            if (turnout != null) {
                NamedBeanHandle<Sensor> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(turnout.getDisplayName(), turnout);
                expression.setSensor(handle);
            }
            expression.set_Is_IsNot((Is_IsNot_Enum)is_IsNot_ComboBox.getSelectedItem());
            expression.setSensorState((SensorState)stateComboBox.getSelectedItem());
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for turnout", ex);
        }
    }
    
    
    /**
     * Create Sensor object for the expression
     *
     * @param reference Sensor application description
     * @return The new output as Sensor object
     */
    protected Sensor getSensorFromPanel(String reference) {
        if (sensorBeanPanel == null) {
            return null;
        }
        sensorBeanPanel.setReference(reference); // pass turnout application description to be put into turnout Comment
        try {
            return sensorBeanPanel.getNamedBean();
        } catch (jmri.JmriException ex) {
            log.warn("skipping creation of turnout not found for " + reference);
            return null;
        }
    }
    
//    private void noSensorMessage(String s1, String s2) {
//        log.warn("Could not provide turnout " + s2);
//        String msg = Bundle.getMessage("WarningNoSensor", new Object[]{s1, s2});
//        JOptionPane.showMessageDialog(editFrame, msg,
//                Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
//    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Sensor_Short");
    }
    
    @Override
    public void dispose() {
        if (sensorBeanPanel != null) {
            sensorBeanPanel.dispose();
        }
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(ExpressionSensorSwing.class);
    
}
