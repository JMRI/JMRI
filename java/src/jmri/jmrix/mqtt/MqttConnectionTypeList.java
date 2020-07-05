package jmri.jmrix.mqtt;

import jmri.jmrix.ConnectionTypeList;
import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Lionel Jeanson
 */
@ServiceProvider(service = ConnectionTypeList.class)
@API(status = EXPERIMENTAL)
public class MqttConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String GENMAN = "MQTT";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.mqtt.MqttConnectionConfig"
        };    
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{GENMAN};
    }
    
}
