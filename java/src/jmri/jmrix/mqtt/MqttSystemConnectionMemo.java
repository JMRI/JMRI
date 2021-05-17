package jmri.jmrix.mqtt;

import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.util.NamedBeanComparator;

/**
 *
 * @author Lionel Jeanson
 */
public class MqttSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    private MqttAdapter mqttAdapter;

    public MqttSystemConnectionMemo() {
        super("M", "MQTT");
        InstanceManager.store(this, MqttSystemConnectionMemo.class);
    }

    @Override
    public void configureManagers() {
//        setPowerManager(new jmri.jmrix.jmriclient.JMRIClientPowerManager(this));
//        jmri.InstanceManager.store(getPowerManager(), jmri.PowerManager.class);

        jmri.InstanceManager.setTurnoutManager(getTurnoutManager());
        jmri.InstanceManager.setSensorManager(getSensorManager());
        jmri.InstanceManager.setLightManager(getLightManager());

//        jmri.InstanceManager.setReporterManager(getReporterManager());

        register();
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    public MqttTurnoutManager getTurnoutManager() {
        if (getDisabled()) {
            return null;
        }
        return (MqttTurnoutManager) classObjectMap.computeIfAbsent(TurnoutManager.class,(Class c) -> {
                    MqttTurnoutManager t = new MqttTurnoutManager(this);
                    t.setSendTopicPrefix(getMqttAdapter().getOptionState("10.3"));
                    t.setRcvTopicPrefix(getMqttAdapter().getOptionState("10.5"));
                    return t;
                });
                
    }

    public MqttSensorManager getSensorManager() {
        if (getDisabled()) {
            return null;
        }
        return (MqttSensorManager) classObjectMap.computeIfAbsent(SensorManager.class,(Class c) -> {
                    MqttSensorManager t = new MqttSensorManager(this);
                    t.setSendTopicPrefix(getMqttAdapter().getOptionState("11.3"));
                    t.setRcvTopicPrefix(getMqttAdapter().getOptionState("11.5"));
                    return t;
                });
    }

    public MqttLightManager getLightManager() {
        if (getDisabled()) {
            return null;
        }
        return (MqttLightManager) classObjectMap.computeIfAbsent(LightManager.class,(Class c) -> {
                    MqttLightManager t = new MqttLightManager(this);
                    t.setSendTopicPrefix(getMqttAdapter().getOptionState("12.3"));
                    t.setRcvTopicPrefix(getMqttAdapter().getOptionState("12.5"));
                    return t;
                });
    }

    void setMqttAdapter(MqttAdapter ma) {
        mqttAdapter = ma;
    }

    public MqttAdapter getMqttAdapter() {
        return mqttAdapter;
    }
}
