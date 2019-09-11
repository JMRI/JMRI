package jmri.jmrit.logixng.digital.actions.configureswing;

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
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.util.swing.BeanSelectCreatePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures an ActionSensor object with a Swing JPanel.
 */
public class ActionSensorSwing implements SwingConfiguratorInterface {

    private JPanel panel;
    private BeanSelectCreatePanel<Sensor> sensorBeanPanel;
    private JComboBox<SensorState> stateComboBox;
    
    
    /** {@inheritDoc} */
    @Override
    public JPanel getConfigPanel() throws IllegalArgumentException {
        createPanel(null);
        return panel;
    }
    
    /** {@inheritDoc} */
    @Override
    public JPanel getConfigPanel(@Nonnull Base object) throws IllegalArgumentException {
        createPanel(object);
        return panel;
    }
    
    private void createPanel(Base object) {
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
    public boolean validate(@Nonnull StringBuilder errorMessage) {
        if (1==0) {
            errorMessage.append("An error");
            return false;
        }
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @Nonnull String userName) {
        ActionSensor action = new ActionSensor(systemName, userName);
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
