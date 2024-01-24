package jmri.jmrix.mqtt;

import java.util.Comparator;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.util.NamedBeanComparator;

/**
 *
 * @author Lionel Jeanson
 * @author Dean Cording (c) 2023
 */
public class MqttSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    private MqttAdapter mqttAdapter;

    public MqttSystemConnectionMemo() {
        super("M", "MQTT");
        InstanceManager.store(MqttSystemConnectionMemo.this, MqttSystemConnectionMemo.class);
    }

    @Override
    public void configureManagers() {

        InstanceManager.setTurnoutManager(getTurnoutManager());
        InstanceManager.setSensorManager(getSensorManager());
        InstanceManager.setLightManager(getLightManager());
        InstanceManager.setReporterManager(getReporterManager());
        InstanceManager.setThrottleManager(getThrottleManager());
        InstanceManager.store(getPowerManager(), PowerManager.class);
        InstanceManager.store(getConsistManager(), ConsistManager.class);

        // prefix for MqttSignalMasts
        MqttSignalMast.setSendTopicPrefix(getMqttAdapter().getOptionState("15"));

        register();
    }

    @Override
    public void dispose() {
        InstanceManager.deregister(this, MqttSystemConnectionMemo.class);
        super.dispose();
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
        return (MqttTurnoutManager) classObjectMap.computeIfAbsent(TurnoutManager.class,(Class<?> c) -> {
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
        return (MqttSensorManager) classObjectMap.computeIfAbsent(SensorManager.class,(Class<?> c) -> {
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
        return (MqttLightManager) classObjectMap.computeIfAbsent(LightManager.class,(Class<?> c) -> {
                    MqttLightManager t = new MqttLightManager(this);
                    t.setSendTopicPrefix(getMqttAdapter().getOptionState("12.3"));
                    t.setRcvTopicPrefix(getMqttAdapter().getOptionState("12.5"));
                    return t;
                });
    }

    public MqttReporterManager getReporterManager() {
        if (getDisabled()) {
            return null;
        }
        return (MqttReporterManager) classObjectMap.computeIfAbsent(ReporterManager.class,(Class<?> c) -> {
                    MqttReporterManager t = new MqttReporterManager(this);
                    t.setRcvTopicPrefix(getMqttAdapter().getOptionState("13"));
                    return t;
                });
    }

    public MqttThrottleManager getThrottleManager() {
        if (getDisabled()) {
            return null;
        }
        return (MqttThrottleManager) classObjectMap.computeIfAbsent(ThrottleManager.class,(Class<?> c) -> {
                    MqttThrottleManager t = new MqttThrottleManager(this);
                    t.setSendThrottleTopic(getMqttAdapter().getOptionState("16.3"));
                    t.setRcvThrottleTopic(getMqttAdapter().getOptionState("16.5"));
                    t.setSendDirectionTopic(getMqttAdapter().getOptionState("17.3"));
                    t.setRcvDirectionTopic(getMqttAdapter().getOptionState("17.5"));
                    t.setSendFunctionTopic(getMqttAdapter().getOptionState("18.3"));
                    t.setRcvFunctionTopic(getMqttAdapter().getOptionState("18.5"));
                    return t;
                });
    }

    public MqttPowerManager getPowerManager() {
        if (getDisabled()) {
            return null;
        }
        return (MqttPowerManager) classObjectMap.computeIfAbsent(PowerManager.class,(Class<?> c) -> {
                    MqttPowerManager t = new MqttPowerManager(this);
                    t.setSendTopic(getMqttAdapter().getOptionState("20.3"));
                    t.setRcvTopic(getMqttAdapter().getOptionState("20.5"));
                    return t;
                });
    }

    @Override
    public MqttConsistManager getConsistManager() {
        if (getDisabled()) {
            return null;
        }
        return (MqttConsistManager) classObjectMap.computeIfAbsent(ConsistManager.class,(Class<?> c) -> {
                    MqttConsistManager t = new MqttConsistManager(this);
                    t.setSendTopic(getMqttAdapter().getOptionState("19.3"));
                    return t;
                });
    }

    public void setPowerManager(@Nonnull PowerManager p) {
        store(p,PowerManager.class);
    }

    @Override
    public void setConsistManager(@Nonnull ConsistManager c) {
        store(c,ConsistManager.class);
    }

    void setMqttAdapter(MqttAdapter ma) {
        mqttAdapter = ma;
    }

    public MqttAdapter getMqttAdapter() {
        return mqttAdapter;
    }
}
