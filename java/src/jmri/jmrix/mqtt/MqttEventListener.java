package jmri.jmrix.mqtt;

/**
 *
 * @author lionel
 */
public interface MqttEventListener extends java.util.EventListener {
     public void notifyMqttMessage(String topic, String message);
     
}
