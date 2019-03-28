package jmri.jmrix.mqtt;

import javax.annotation.Nonnull;
import jmri.NamedBean;

/**
 * Interface defining a content parser, which translates to and from the MQTT payload
 * content.
 * 
 * @author Bob Jacobsen
 */
public interface MqttContentParser<T extends NamedBean> {
    /**
     * Load a bean's state from a received MQTT payload.
     * @param bean The particular item receiving the payload
     * @param payload The entire string received via MQTT
     * @throws IllegalArgumentException if the payload is unparsable.
     */
    public void beanFromPayload(@Nonnull T bean, @Nonnull String payload, @Nonnull String topic);
    
    /**
     * Create the payload for a particular state transformation on 
     * a particular bean.
     * @param bean The particular item sending the payload
     * @param newState The value to be sent to the layout; this is not yet present in the bean
     * @return String payload to transfer via MQTT.
     */
    public @Nonnull String payloadFromBean(@Nonnull T bean, int newState);
     
}
