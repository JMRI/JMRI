package jmri.jmrix.mqtt;

import java.util.HashSet;

import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.IdTag;

import jmri.implementation.AbstractIdTagReporter;

/**
 * Provide a Reporter implementation for MQTT communications
 *
 * @author Bob Jacobsen Copyright (C) 2023
 */

class MqttReporter extends AbstractIdTagReporter implements MqttEventListener {

    /**
     * Requires, but does not check, that the system name and topic be consistent
     * @param ma Adapter to specific connection
     * @param systemName System Name for this Sensor
     * @param rcvTopic Topic string to be used when receiving by JMRI
     */
    MqttReporter(MqttAdapter ma, String systemName, String rcvTopic) {
        super(systemName);
        this.rcvTopic = rcvTopic;
        mqttAdapter = ma;
        mqttAdapter.subscribe(rcvTopic, this);
    }

    private final MqttAdapter mqttAdapter;
    private final String rcvTopic;

    @Override
    public void notifyMqttMessage(String receivedTopic, String message) {
        if (! receivedTopic.endsWith(rcvTopic) ) {
            log.error("Got a message whose topic ({}) wasn't for me ({})", receivedTopic, rcvTopic);
            return;
        }

        log.debug("start parse of {}", message);
        // parse content
        String[] terms = message.split(" ", 2);
        if ( terms.length < 1) {
            log.warn("No loco ID present in ({})", message);
            return;
        }

        String loco = terms[0];
        String content  = "";
        if (terms.length > 1 ) {
            content = terms[1];
        }

        // from java/src/jmri/jmrix/loconet/LnReporter.java
        IdTag idTag = InstanceManager.getDefault(IdTagManager.class).provideIdTag(loco);
        idTag.setProperty("content", content);
        notify(idTag);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MqttReporter.class);

}
