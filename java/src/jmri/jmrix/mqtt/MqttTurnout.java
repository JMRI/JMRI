package jmri.jmrix.mqtt;

import javax.annotation.Nonnull;
import jmri.Turnout;
import jmri.implementation.AbstractTurnout;

/**
 * Implementation of the Turnout interface for MQTT layouts.
 *
 * @author Lionel Jeanson Copyright (c) 2017
 */
public class MqttTurnout extends AbstractTurnout implements MqttEventListener {

    private final MqttAdapter mqttAdapter;
    private final String topic;
    private final static String stateTopicPrefix = "state/";
    private final static String commandTopicPrefix = "command/";

    /**
     * Requires, but does not check, that the system name and topic be consistent
     * @param ma Adapter to reference for connection
     * @param systemName System name of turnout
     * @param topic MQTT topic being used
     */
    MqttTurnout(MqttAdapter ma, String systemName, String topic) {
        super(systemName);
        this.topic = topic;
        mqttAdapter = ma;
        mqttAdapter.subscribe(getStateTopicPrefix() + topic, this);
        _validFeedbackNames = new String[] {"DIRECT", "ONESENSOR", "TWOSENSOR", "DELAYED", "MONITORING", "EXACT"};
        _validFeedbackModes = new int[] {DIRECT, ONESENSOR, TWOSENSOR, DELAYED, MONITORING, EXACT};
        _validFeedbackTypes = DIRECT | ONESENSOR | TWOSENSOR | DELAYED | MONITORING;
    }

    private String getStateTopicPrefix() {
        return (getFeedbackMode() == EXACT ? stateTopicPrefix : "");
    }

    private String getCommandTopicPrefix() {
        return (getFeedbackMode() == EXACT ? commandTopicPrefix : "");
    }

    @Override
    public void setFeedbackMode(int mode) throws IllegalArgumentException {
        mqttAdapter.unsubscribe(getStateTopicPrefix() + topic, this);
        try {
            super.setFeedbackMode(mode);
        } finally {
            mqttAdapter.subscribe(getStateTopicPrefix() + topic, this);
        }
    }

    public void setParser(MqttContentParser<Turnout> parser) {
        this.parser = parser;
    }
    
    MqttContentParser<Turnout> parser = new MqttContentParser<Turnout>() {
        private final static String closedText = "CLOSED";
        private final static String thrownText = "THROWN";
        private final static String unknownText = "UNKNOWN";
        private final static String inconsistentText = "INCONSISTENT";
        private final static String movingText = "MOVING";

        private void setExtraWithCheck(int newMode, String payload) {
            if (getFeedbackMode() == MONITORING || getFeedbackMode() == EXACT) {
                newKnownState(newMode);
            } else {
                log.warn("Received state is not valid in current operating mode: {}", payload);
            }
        }

        @Override
        public void beanFromPayload(@Nonnull Turnout bean, @Nonnull String payload, @Nonnull String topic) {
            switch (payload) {
                case closedText:                
                    newKnownState(CLOSED);
                    break;
                case thrownText:
                    newKnownState(THROWN);
                    break;
                case unknownText:
                    setExtraWithCheck(UNKNOWN, payload);
                    break;
                case movingText:
                case inconsistentText:
                    setExtraWithCheck(INCONSISTENT, payload);
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
        log.debug("Send command to {} Pushbutton BT{} not yet coded", (_pushButtonLockout ? "Lock" : "Unlock"), topic);
    }

    private void sendMessage(String c) {
        mqttAdapter.publish(getCommandTopicPrefix() + this.topic, c.getBytes());
    }

    @Override
    public void notifyMqttMessage(String receivedTopic, String message) {
        if (!receivedTopic.endsWith(topic)) {
            log.error("Got a message whose topic ({}) wasn't for me ({})", receivedTopic, topic);
            return;
        }
        
        parser.beanFromPayload(this, message, receivedTopic);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MqttTurnout.class);

}
