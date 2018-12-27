package jmri.jmrix.mqtt;

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

    private final String closedText = "CLOSED";
    private final String thrownText = "THROWN";

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
        if ((s & Turnout.CLOSED) != 0) {
            // first look for the double case, which we can't handle
            if ((s & Turnout.THROWN) != 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN {}", s);
                return;
            } else {
                // send a CLOSED command
                sendMessage(closedText);
            }
        } else {
            // send a THROWN command
            sendMessage(thrownText);
        }
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
        switch (message) {
            case closedText:                
                newKnownState(CLOSED);
                break;
            case thrownText:
                newKnownState(THROWN);
                break;
            default:
                log.warn("Unknow state : {} (topic : {})", message, topic);
                break;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(MqttTurnout.class);

}
