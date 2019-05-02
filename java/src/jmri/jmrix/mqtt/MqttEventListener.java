package jmri.jmrix.mqtt;

/**
 *
 * @author Lionel Jeanson
 */
public interface MqttEventListener extends java.util.EventListener {
     public void notifyMqttMessage(String topic, String message);
     
}
