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
    private final String sendTopic;
    private final String rcvTopic;

    /**
     * Requires, but does not check, that the system name and topic be consistent
     * @param ma Adapter to reference for connection
     * @param systemName System name of turnout
     * @param sendTopic MQTT topic to use when sending (full string, including systemName part)
     * @param rcvTopic MQTT topic to use when receiving (full string, including systemName part)
     */
    MqttTurnout(MqttAdapter ma, String systemName, String sendTopic, String rcvTopic) {
        super(systemName);
        this.sendTopic = sendTopic;
        this.rcvTopic  = rcvTopic;
        mqttAdapter = ma;
        mqttAdapter.subscribe(rcvTopic, this);  // only receive receive topic, not send one
        _validFeedbackNames = new String[] {"DIRECT", "ONESENSOR", "TWOSENSOR", "DELAYED", "MONITORING"};
        _validFeedbackModes = new int[] {DIRECT, ONESENSOR, TWOSENSOR, DELAYED, MONITORING};
        _validFeedbackTypes = DIRECT | ONESENSOR | TWOSENSOR | DELAYED | MONITORING;
    }

    public void setParser(MqttContentParser<Turnout> parser) {
        this.parser = parser;
    }
        
    MqttContentParser<Turnout> parser = new MqttContentParser<Turnout>() {
        private final static String closedText = "CLOSED";
        private final static String thrownText = "THROWN";
        private final static String unknownText = "UNKNOWN";
        private final static String inconsistentText = "INCONSISTENT";

        int stateFromString(String payload) {
            switch (payload) {
                case closedText:                
                    return CLOSED;
                case thrownText:
                    return THROWN;
                case unknownText:
                    return UNKNOWN;
                case inconsistentText:
                    return INCONSISTENT;
                default:
                    log.warn("Unknown state : {}, substitute UNKNOWN", payload);
                    return UNKNOWN;
            }
        }
        
        @Override
        public void beanFromPayload(@Nonnull Turnout bean, @Nonnull String payload, @Nonnull String topic) {
            int state = stateFromString(payload);
            
            boolean couldBeSendMessage = topic.endsWith(sendTopic); // not listening for send messages, but can get them anyway
            boolean couldBeRcvMessage = topic.endsWith(rcvTopic);
            
            if (couldBeSendMessage) {
                // always accept as commadn
                newCommandedState(state);
                
                // when needed, do feedback
                if (getFeedbackMode() == DIRECT || getFeedbackMode() == MONITORING) newKnownState(state);
                
                return;
            }
            
            if (couldBeRcvMessage) {

                // if MONITORING, do feedback
                if (getFeedbackMode() == DIRECT || getFeedbackMode() == MONITORING) newKnownState(state);
                
                return;
            }

            // really shouldn't have gotten here
            log.warn("expected failure to decode topic {} {}", topic, payload);
            return;
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

    @Override
    protected void turnoutPushbuttonLockout(boolean _pushButtonLockout) {
        log.warn("Send command to {} Pushbutton in {} not yet coded", (_pushButtonLockout ? "Lock" : "Unlock"), getSystemName());
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MqttTurnout.class);

}
