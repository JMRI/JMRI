package jmri.jmrix.mqtt.configurexml;

import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import jmri.jmrix.mqtt.MqttAdapter;
import jmri.jmrix.mqtt.MqttConnectionConfig;

/**
 *
 * @author lionel
 */
public class MqttConnectionConfigXml  extends AbstractNetworkConnectionConfigXml {

    public MqttConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        adapter = new MqttAdapter();
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((MqttConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new MqttConnectionConfig(adapter));
    }
   
}
