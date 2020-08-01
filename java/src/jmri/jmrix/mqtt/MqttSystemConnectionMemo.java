package jmri.jmrix.mqtt;

import java.util.Comparator;
import java.util.ResourceBundle;

import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.TurnoutManager;
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

    public void configureManagers() {
        InstanceManager.setTurnoutManager(getTurnoutManager());
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
        return (MqttTurnoutManager) classObjectMap.computeIfAbsent(TurnoutManager.class,(Class c) -> new MqttTurnoutManager(this));
    }

    void setMqttAdapter(MqttAdapter ma) {
        mqttAdapter = ma;
    }

    MqttAdapter getMqttAdapter() {
        return mqttAdapter;
    }
}
