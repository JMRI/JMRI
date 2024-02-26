package jmri.jmrix.mqtt;

import jmri.Light;
import jmri.implementation.AbstractVariableLight;

import javax.annotation.Nonnull;

/**
 * MQTT implementation of the Light interface.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2020, 2023
 * @author Paul Bender Copyright (C) 2010
 * @author Fredrik Elestedt  Copyright (C) 2020
 */
public class MqttLight extends AbstractVariableLight implements MqttEventListener {
    private final MqttAdapter mqttAdapter;
    private final String sendTopic;
    private final String rcvTopic;

    static public String intensityText = "INTENSITY ";  // public for script access

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
        private static final String onText = "ON";
        private static final String offText = "OFF";

        int stateFromString(String payload) {
            if (payload.startsWith(intensityText)) return -1; // means don't change state
            switch (payload) {
                case onText: return ON;
                case offText: return OFF;
                default: return UNKNOWN;
            }
        }

        @Override
        public void beanFromPayload(@Nonnull Light bean, @Nonnull String payload, @Nonnull String topic) {
            log.debug("beanFromPayload {} {} {}", bean, payload, topic);
            int state = stateFromString(payload);

            if (state == -1) {
                // don't change anything
                log.trace("  no changes");
                return;
            }
            boolean couldBeSendMessage = topic.endsWith(sendTopic);
            boolean couldBeRcvMessage = topic.endsWith(rcvTopic);

            if (couldBeSendMessage) {
                log.trace("   setCommandedState {}", state);
                setCommandedState(state);
            } else if (couldBeRcvMessage) {
                setState(state);
                log.trace("   setState {}", state);
            } else {
                log.warn("{} failure to decode topic {} {}", getDisplayName(), topic, payload);
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
                    log.error("Light {} has a state which is not supported {}", getDisplayName(), newState);
                    break;
            }
            return toReturn;
        }
    };

    // For AbstractVariableLight
    @Override
    protected int getNumberOfSteps() {
        return 20;
    }

    // For AbstractVariableLight
    @Override
    protected void sendIntensity(double intensity) {
        sendMessage(intensityText+intensity);
    }

    // For AbstractVariableLight
    @Override
    protected void sendOnOffCommand(int newState) {
        switch (newState) {
        case ON:
            sendMessage(true);
            break;
        case OFF:
            sendMessage(false);
            break;
        default:
            log.error("Unexpected state to sendOnOff: {}", newState);
        }
    }

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
                log.error("Cannot command {} to both ON and OFF {}", getDisplayName(), newState);
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
        log.debug("sent {}", c);
    }

    @Override
    public void setState(int newState) {
        log.debug("setState {} was {}", newState, mState);

        if (newState != ON && newState != OFF && newState != UNKNOWN) {
            throw new IllegalArgumentException("cannot set state value " + newState);
        }

        // do the state change in the hardware
        doNewState(mState, newState);
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
            log.error("{} got a message whose topic ({}) wasn't for me ({})", getDisplayName(), receivedTopic, rcvTopic);
            return;
        }
        log.debug("notifyMqttMessage with {}", message);

        // parser doesn't support intensity, so first handle that here
        if (message.startsWith(intensityText)) {
            var stringValue = message.substring(intensityText.length());
            try {
                double intensity = Double.parseDouble(stringValue);
                log.debug("setting received intensity with {}", intensity);
                setObservedAnalogValue(intensity);
            } catch (NumberFormatException e) {
                log.warn("could not parse input {}", receivedTopic, e);
            }
        }

        // handle on/off
        parser.beanFromPayload(this, message, receivedTopic);
    }

    @Override
    public void dispose() {
        mqttAdapter.unsubscribe(rcvTopic,this);
        super.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MqttLight.class);
}
