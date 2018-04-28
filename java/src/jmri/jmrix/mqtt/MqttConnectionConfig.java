
package jmri.jmrix.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lionel
 */
public class MqttConnectionConfig extends jmri.jmrix.AbstractNetworkConnectionConfig {

    public MqttConnectionConfig(jmri.jmrix.NetworkPortAdapter p) {
        super(p);
    }

    public MqttConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return("MQTT Connection");
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new MqttAdapter();
            adapter.setPort(1883);
        }
    }

    @Override
    public String getInfo() {
        return("MQTT");
    }

    @Override
    public String getManufacturer() {
        return(MqttConnectionTypeList.GENMAN);
    }

    // private final static Logger log = LoggerFactory.getLogger(MqttConnectionConfig.class);    
}
