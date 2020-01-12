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
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.digital.actions.ActionSensor;
import jmri.jmrit.logixng.digital.actions.ActionSensor.SensorState;
import jmri.util.swing.BeanSelectCreatePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures an ActionSensor object with a Swing JPanel.
 */
public class ActionSensorSwing extends AbstractActionSwing {

    private BeanSelectCreatePanel<Sensor> sensorBeanPanel;
    private JComboBox<SensorState> stateComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionSensor action = (ActionSensor)object;
        
        panel = new JPanel();
        sensorBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(SensorManager.class), null);
        
        stateComboBox = new JComboBox<>();
        for (SensorState e : SensorState.values()) {
            stateComboBox.addItem(e);
        }
        
        if (action != null) {
            if (action.getSensor() != null) {
                sensorBeanPanel.setDefaultNamedBean(action.getSensor().getBean());
            }
            stateComboBox.setSelectedItem(action.getSensorState());
        }
        
        panel.add(new JLabel(Bundle.getMessage("BeanNameSensor")));
        panel.add(sensorBeanPanel);
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
    public String getAutoSystemName() {
        return InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName();
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionSensor action = new ActionSensor(systemName, userName);
        try {
            if (!sensorBeanPanel.isEmpty()) {
                Sensor sensor = sensorBeanPanel.getNamedBean();
                if (sensor != null) {
                    NamedBeanHandle<Sensor> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(sensor.getDisplayName(), sensor);
                    action.setSensor(handle);
                }
            }
            action.setSensorState((SensorState)stateComboBox.getSelectedItem());
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for sensor", ex);
        }
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionSensor)) {
            throw new IllegalArgumentException("object must be an ActionSensor but is a: "+object.getClass().getName());
        }
        ActionSensor action = (ActionSensor)object;
        try {
            Sensor sensor = sensorBeanPanel.getNamedBean();
            if (sensor != null) {
                NamedBeanHandle<Sensor> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(sensor.getDisplayName(), sensor);
                action.setSensor(handle);
            }
            action.setSensorState((SensorState)stateComboBox.getSelectedItem());
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for sensor", ex);
        }
    }
    
    
    /**
     * Create Sensor object for the action
     *
     * @param reference Sensor application description
     * @return The new output as Sensor object
     */
    protected Sensor getSensorFromPanel(String reference) {
        if (sensorBeanPanel == null) {
            return null;
        }
        sensorBeanPanel.setReference(reference); // pass sensor application description to be put into sensor Comment
        try {
            return sensorBeanPanel.getNamedBean();
        } catch (jmri.JmriException ex) {
            log.warn("skipping creation of sensor not found for " + reference);
            return null;
        }
    }
    
//    private void noSensorMessage(String s1, String s2) {
//        log.warn("Could not provide sensor " + s2);
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
    
    
    private final static Logger log = LoggerFactory.getLogger(ActionSensorSwing.class);
    
}
