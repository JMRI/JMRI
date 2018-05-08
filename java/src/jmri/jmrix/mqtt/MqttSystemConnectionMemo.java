package jmri.jmrix.mqtt;

import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.jmrix.SystemConnectionMemo;

/**
 *
 * @author lionel
 */
public class MqttSystemConnectionMemo extends SystemConnectionMemo {

    private MqttAdapter mqttAdapter;

    public MqttSystemConnectionMemo() {
        super("M", "MQTT");
        register();
        InstanceManager.store(this, MqttSystemConnectionMemo.class);
    }
    
    public void configureManagers() {
//        setPowerManager(new jmri.jmrix.jmriclient.JMRIClientPowerManager(this));
//        jmri.InstanceManager.store(getPowerManager(), jmri.PowerManager.class);
        jmri.InstanceManager.setTurnoutManager(getTurnoutManager());
//        jmri.InstanceManager.setSensorManager(getSensorManager());
//        jmri.InstanceManager.setLightManager(getLightManager());
//        jmri.InstanceManager.setReporterManager(getReporterManager());
    }    
    
    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        }
        return false; // nothing, by default
    }    
   
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        return null; // nothing, by default
    }
    
   protected MqttTurnoutManager turnoutManager;

    public MqttTurnoutManager getTurnoutManager() {
        if (getDisabled()) {
            return null;
        }
        if (turnoutManager == null) {
            turnoutManager = new MqttTurnoutManager(mqttAdapter, getSystemPrefix());
        }
        return turnoutManager;
    }

    void setMqttAdapter(MqttAdapter ma) {
        mqttAdapter = ma;
    }

    
}
