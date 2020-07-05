package jmri.jmrix.mqtt;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 *
 * @author Lionel Jeanson
 */
@API(status = EXPERIMENTAL)
public interface MqttEventListener extends java.util.EventListener {
     public void notifyMqttMessage(String topic, String message);
     
}
