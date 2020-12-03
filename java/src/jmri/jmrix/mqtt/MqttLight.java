package jmri.jmrix.mqtt;

import jmri.Light;
import jmri.implementation.AbstractLight;

import javax.annotation.Nonnull;

/**
 * MQTT implementation of the Light interface.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2020
 * @author Paul Bender Copyright (C) 2010
 * @author Fredrik Elestedt  Copyright (C) 2020
 */
public class MqttLight extends AbstractLight implements MqttEventListener {
    private final MqttAdapter mqttAdapter;
    private final String sendTopic;
    private final String rcvTopic;

    public MqttLight(MqttAdapter ma, String systemName, String userName, String sendTopic, String rcvTopic) {
        super(systemName, userName);
        this.sendTopic = sendTopic;
        this.rcvTopic = rcvTopic;
        this.mqttAdapter = ma;
        this.mqttAdapter.subscribe(rcvTopic, this);
    }

    public void setParser(MqttContentParser<Light> parser) {
        this.parser = parser;
    }

    MqttContentParser<Light> parser = new MqttContentParser<Light>() {
        private final static String onText = "ON";
        private final static String offText = "OFF";

        int stateFromString(String payload) {
            switch (payload) {
                case onText: return ON;
                case offText: return OFF;
                default: return UNKNOWN;
            }
        }

        @Override
        public void beanFromPayload(@Nonnull Light bean, @Nonnull String payload, @Nonnull String topic) {
            int state = stateFromString(payload);

            boolean couldBeSendMessage = topic.endsWith(sendTopic);
            boolean couldBeRcvMessage = topic.endsWith(rcvTopic);

            if (couldBeSendMessage) {
                setCommandedState(state);
            } else if (couldBeRcvMessage) {
                setState(state);
            } else {
                log.warn("failure to decode topic {} {}", topic, payload);
            }
        }
        
        @Override
        public @Nonnull String payloadFromBean(@Nonnull Light bean, int newState){
            String toReturn = "UNKNOWN";
            switch (getState()) {
                case Light.ON:
                    toReturn = onText;
                    break;
                case Light.OFF:
                    toReturn = offText;
                    break;
                default:
                    log.error("Light has a state which is not supported {}", newState);
                    break;
            }
            return toReturn;
        }
    };

    // Handle a request to change state by sending a formatted packet
    // to the server.
    @Override
    protected void doNewState(int oldState, int newState) {
        log.debug("doNewState with old state {} new state {}", oldState, newState);
        if (oldState == newState) {
            return; //no change, just quit.
        }  // sort out states
        if ((newState & Light.ON) != 0) {
            // first look for the double case, which we can't handle
            if ((newState & Light.OFF) != 0) {
                // this is the disaster case!
                log.error("Cannot command both ON and OFF {}", newState);
                return;
            } else {
                // send a ON command
                sendMessage(true);
            }
        } else {
            // send a OFF command
            sendMessage(false);
        }
    }

    private void sendMessage(boolean on) {
        this.sendMessage(on ? "ON" : "OFF");
    }

    private void sendMessage(String c) {
        jmri.util.ThreadingUtil.runOnLayoutEventually(() -> {
            mqttAdapter.publish(this.sendTopic, c.getBytes());
        });
    }

    @Override
    public void setState(int newState) {
        log.debug("setState {} was {}", newState, mState);
        
        //int oldState = mState;
        if (newState != ON && newState != OFF && newState != UNKNOWN) {
            throw new IllegalArgumentException("cannot set state value " + newState);
        }
        
        // do the state change in the hardware
        doNewState(mState, newState); // old state, new state
        // change value and tell listeners
        notifyStateChange(mState, newState);
    }

    //request a status update from the layout
    @Override
    public void requestUpdateFromLayout() {
    }

    @Override
    public void notifyMqttMessage(String receivedTopic, String message) {
        if (! ( receivedTopic.endsWith(rcvTopic) || receivedTopic.endsWith(sendTopic) ) ) {
            log.error("Got a message whose topic ({}) wasn't for me ({})", receivedTopic, rcvTopic);
            return;
        }        
        parser.beanFromPayload(this, message, receivedTopic);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MqttLight.class);
}
