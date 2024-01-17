/**
 * MqttPowerManager.java
 *
 * PowerManager implementation for controlling layout power
 *
 * @author Dean Cording Copyright (C) 2023
 *
 */
package jmri.jmrix.mqtt;

import jmri.JmriException;
import jmri.managers.AbstractPowerManager;

import javax.annotation.Nonnull;

public class MqttPowerManager extends AbstractPowerManager<MqttSystemConnectionMemo> implements MqttEventListener {

    private static final String onText = "ON";
    private static final String offText = "OFF";
    private final MqttAdapter mqttAdapter;

    public MqttPowerManager(MqttSystemConnectionMemo memo) {
        super(memo);
        mqttAdapter = memo.getMqttAdapter();

    }

    public void setSendTopic(@Nonnull String sendTopic) {
        this.sendTopic = sendTopic;
    }
    public void setRcvTopic(@Nonnull String rcvTopic) {
        this.mqttAdapter.unsubscribe(rcvTopic, this);
        this.rcvTopic = rcvTopic;
        this.mqttAdapter.subscribe(rcvTopic, this);

        log.info("Subscribed to {}", rcvTopic);

    }

    @Nonnull
    public String sendTopic = "track/power"; // for constructing topic; public for script access
    @Nonnull
    public String rcvTopic = "track/power"; // for constructing topic; public for script access

    @Override
    public boolean implementsIdle() {
        return false;
    }

    @Override
    public void setPower(int v) throws JmriException {
        if (v == ON) {
            // send TRACK_POWER_ON
            sendMessage(onText);
        } else if (v == OFF) {
            // send TRACK_POWER_OFF
            sendMessage(offText);
        } else {
            log.warn("Saw unknown power state : {}", v);
        }
    }



    // to free resources when no longer used
    @Override
    public void dispose() throws JmriException {
            this.mqttAdapter.unsubscribe(rcvTopic, this);
    }


     private void sendMessage(String c) {
        jmri.util.ThreadingUtil.runOnLayoutEventually(() -> {
            mqttAdapter.publish(this.sendTopic, c);
        });
        log.debug("sent {}", c);
    }

    @Override
    public void notifyMqttMessage(String receivedTopic, String message) {
        if (!receivedTopic.endsWith(rcvTopic)) {
            log.error("Got a message whose topic ({}) wasn't for me ({})", receivedTopic, rcvTopic);
            return;
        }
        log.debug("notifyMqttMessage with {}", message);

        // handle on/off
        try {
            switch (message) {
                case onText: super.setPower(ON); break;
                case offText: super.setPower(OFF); break;
                default: log.error("Invalid message to power manager: {}", message); break;
            }
        } catch (JmriException e) {
            log.error("JMRI Exception", e);
        }
    }

    // Initialize logging information
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MqttPowerManager.class);

}



