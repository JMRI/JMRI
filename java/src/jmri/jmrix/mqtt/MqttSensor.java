package jmri.jmrix.mqtt;

import javax.annotation.*;
import jmri.*;
import jmri.implementation.AbstractSensor;

/**
 * Implementation of the Sensor interface for MQTT layouts.
 *
 * @author Lionel Jeanson Copyright (c) 2017, 2019
 * @author Bob Jacobsen   Copyright (c) 2020
 */
public class MqttSensor extends AbstractSensor implements MqttEventListener {

    private final MqttAdapter mqttAdapter;
    private final String sendTopic;
    private final String rcvTopic;

    /**
     * Requires, but does not check, that the system name and topic be consistent
     * @param ma Adapter to specific connection
     * @param systemName System Name for this Sensor
     * @param sendTopic Topic string to be used when sending from JMRI
     * @param rcvTopic Topic string to be used when receiving by JMRI
     */
    MqttSensor(MqttAdapter ma, String systemName, String sendTopic, String rcvTopic) {
        super(systemName);
        this.sendTopic = sendTopic;
        this.rcvTopic  = rcvTopic;
        mqttAdapter = ma;
        mqttAdapter.subscribe(rcvTopic, this);  // only receive receive topic, not send one
    }

    public void setParser(MqttContentParser<Sensor> parser) {
        this.parser = parser;
    }
    
    MqttContentParser<Sensor> parser = new MqttContentParser<Sensor>() {
        private final static String inactiveText = "INACTIVE";
        private final static String activeText = "ACTIVE";
        @Override
        public void beanFromPayload(@Nonnull Sensor bean, @Nonnull String payload, @Nonnull String topic) {
            switch (payload) {
                case inactiveText:                
                    setOwnState(! _inverted ? INACTIVE : ACTIVE);
                    break;
                case activeText:
                    setOwnState(! _inverted ? ACTIVE : INACTIVE);
                    break;
                default:
                    log.warn("Unknown state : {}", payload);
                    break;
            }
        }
        
        @Override
        public @Nonnull String payloadFromBean(@Nonnull Sensor bean, int newState){
            // sort out states
            if ((newState & Sensor.INACTIVE) != 0 ^ getInverted()) {
                // first look for the double case, which we can't handle
                if ((newState & Sensor.ACTIVE ) != 0 ^ getInverted()) {
                    // this is the disaster case!
                    log.error("Cannot command both INACTIVE and ACTIVE: {}", newState);
                    throw new IllegalArgumentException("Cannot command both INACTIVE and ACTIVE: "+newState);
                } else {
                    // send a INACTIVE command
                    return inactiveText;
                }
            } else {
                // send a ACIVE command
                return activeText;
            }
        }
    };
    

    // Sensors do support inversion
    @Override
    public boolean canInvert() {
        return true;
    }

    /**
     * The request is just swallowed
     */
    @Override
    public void requestUpdateFromLayout() {}

    // Handle a request to change state by sending MQTT message
    @Override
    public void setKnownState(int s) {
        // sort out states
        String payload = parser.payloadFromBean(this, s);
        log.debug("payload: {}", payload);
        // send appropriate command
        sendMessage(payload);
        
        // and do internal operations
        setOwnState(s);
    }

    private void sendMessage(String c) {
        mqttAdapter.publish(sendTopic, c);
    }

    @Override
    public void notifyMqttMessage(String receivedTopic, String message) {
        if (! ( receivedTopic.endsWith(rcvTopic) || receivedTopic.endsWith(sendTopic) ) ) {
            log.error("Got a message whose topic ({}) wasn't for me ({})", receivedTopic, rcvTopic);
            return;
        }
        
        parser.beanFromPayload(this, message, receivedTopic);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MqttSensor.class);

}
