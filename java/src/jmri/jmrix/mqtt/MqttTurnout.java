package jmri.jmrix.mqtt;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MQTT implementation of the Turnout interface.
 * <p>
 * Description: extend jmri.AbstractTurnout for MQTT layouts
 *
 * @author Lionel Jeanson Copyright: Copyright (c) 2017
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
                LOG.error("Cannot command both CLOSED and THROWN " + s);
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("Send command to " + (_pushButtonLockout ? "Lock" : "Unlock") + " Pushbutton BT" + _number);
        }
    }

    private void sendMessage(String c) {
        mqttAdapter.publish(mysubTopic, c.getBytes());
    }

    @Override
    public void notifyMqttMessage(String topic, String message) {
        if (!topic.endsWith(mysubTopic)) {
            LOG.error("Got a message whose topic (" + topic + ") wasn't for me (" + mysubTopic + ")");
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
                LOG.warn("Unknow state : " + message + " (topic : " + topic + ")");
                break;
        }
    }

    private final static Logger LOG = LoggerFactory.getLogger(MqttTurnout.class);
}
