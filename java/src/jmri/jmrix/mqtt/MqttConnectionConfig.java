
package jmri.jmrix.mqtt;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Lionel Jeanson
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(final JPanel details) {
        super.loadDetails(details);
        
        // the following is a very brittle work-around until we 
        // move to a SystemConnectionMemo architecture.  It sets the
        // combobox for the topic preference to editable, so that it
        // can be changed to an arbitrary string before being stored.
        ((JComboBox) options.get(adapter.getOptions()[0]).getComponent()).setEditable(true);
    }
    
    @Override
    protected void checkOptionValueValidity(String i, JComboBox<String> opt) {
        // it's OK, even it it doesn't match a pre-load
    }
    
    // private final static Logger log = LoggerFactory.getLogger(MqttConnectionConfig.class);

}
