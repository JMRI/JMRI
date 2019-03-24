package jmri.jmrix.mqtt;

import javax.annotation.Nonnull;
import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Turnout interface for MQTT layouts.
 *
 * @author Lionel Jeanson Copyright (c) 2017
 */
public class MqttTurnout extends AbstractTurnout implements MqttEventListener {

    private final MqttAdapter mqttAdapter;
    private final String mysubTopic;
    private final int _number;   // turnout number

    public void setParser(MqttContentParser<Turnout> parser) {
        this.parser = parser;
    }
    
    MqttContentParser<Turnout> parser = new MqttContentParser<Turnout>() {
        private final static String closedText = "CLOSED";
        private final static String thrownText = "THROWN";
        @Override
        public void beanFromPayload(@Nonnull Turnout bean, @Nonnull String payload, @Nonnull String topic) {
            switch (payload) {
                case closedText:                
                    newKnownState(CLOSED);
                    break;
                case thrownText:
                    newKnownState(THROWN);
                    break;
                default:
                    log.warn("Unknown state : {}", payload);
                    break;
            }
        }
        
        @Override
        public @Nonnull String payloadFromBean(@Nonnull Turnout bean, int newState){
            // sort out states
            if ((newState & Turnout.CLOSED) != 0 ^ getInverted()) {
                // first look for the double case, which we can't handle
                if ((newState & Turnout.THROWN ) != 0 ^ getInverted()) {
                    // this is the disaster case!
                    log.error("Cannot command both CLOSED and THROWN: {}", newState);
                    throw new IllegalArgumentException("Cannot command both CLOSED and THROWN: "+newState);
                } else {
                    // send a CLOSED command
                    return closedText;
                }
            } else {
                // send a THROWN command
                return thrownText;
            }
        }
    };
    
    
    MqttTurnout(MqttAdapter ma, int number) {
        super("MT" + number);
        _number = number;
        mqttAdapter = ma;
        mysubTopic = "track/turnout/" + _number;
        mqttAdapter.subscribe(mysubTopic, this);
    }

    public int getNumber() {
        return _number;
    }

    // Turnouts do support inversion
    @Override
    public boolean canInvert() {
        return true;
    }

    // Handle a request to change state by sending a formatted DCC packet
    @Override
    protected void forwardCommandChangeToLayout(int s) {
        // sort out states
        String payload = parser.payloadFromBean(this, s);

        // send appropriate command
        sendMessage(payload);
    }

    @Override
    protected void turnoutPushbuttonLockout(boolean _pushButtonLockout) {
        log.debug("Send command to {} Pushbutton BT{}", (_pushButtonLockout ? "Lock" : "Unlock"), _number);
    }

    private void sendMessage(String c) {
        mqttAdapter.publish(mysubTopic, c.getBytes());
    }

    @Override
    public void notifyMqttMessage(String topic, String message) {
        if (!topic.endsWith(mysubTopic)) {
            log.error("Got a message whose topic ({}) wasn't for me ({})", topic, mysubTopic);
            return;
        }
        
        parser.beanFromPayload(this, message, topic);
    }

    private final static Logger log = LoggerFactory.getLogger(MqttTurnout.class);

}
