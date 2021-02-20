package jmri.jmrix.mqtt;

import java.io.IOException;
import java.util.*;

import javax.annotation.Nonnull;

import org.apiguardian.api.API;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Communications adapter for Mqtt communications links.
 *
 * @author Lionel Jeanson
 * @author Bob Jacobsen   Copyright (c) 2091, 2029
 */
@API(status=API.Status.MAINTAINED)
public class MqttAdapter extends jmri.jmrix.AbstractNetworkPortController implements MqttCallback {

    private final static String PROTOCOL = "tcp://";
    private final static String DEFAULT_BASETOPIC = Bundle.getMessage("TopicBase");
    
    public boolean retained = true;  // public for script access
    public int      qosflag = 2;     // public for script access
    
    /**
     * Otherwise known as "Channel", this is prepended to the 
     * topic for all JMRI inward and outward communications.
     * Typically set by preferences at startup.  Changing it
     * after startup might have no or bad effect.
     */
    @API(status=API.Status.MAINTAINED)
    public String baseTopic = DEFAULT_BASETOPIC;

    HashMap<String, ArrayList<MqttEventListener>> mqttEventListeners;

    MqttClient mqttClient;

    @API(status=API.Status.INTERNAL)
    public MqttAdapter() {
        super(new MqttSystemConnectionMemo());
        log.debug("Doing ctor...");
        option2Name = "0 MQTTchannel"; // 0 to get it to the front of the list
        
        options.put(option2Name, new Option(Bundle.getMessage("NameTopicBase"), 
                                            new String[]{baseTopic}, Option.Type.TEXT));
                                            
        options.put("10.3", new Option(Bundle.getMessage("NameTopicTurnoutSend"),    
                new String[]{Bundle.getMessage("TopicTurnoutSend")},  Option.Type.TEXT));
        options.put("10.5", new Option(Bundle.getMessage("NameTopicTurnoutRcv"),     
                new String[]{Bundle.getMessage("TopicTurnoutRcv")},  Option.Type.TEXT));
        
        
        options.put("11.3", new Option(Bundle.getMessage("NameTopicSensorSend"),
                                            new String[]{Bundle.getMessage("TopicSensorSend")},   Option.Type.TEXT));
        options.put("11.5", new Option(Bundle.getMessage("NameTopicSensorRcv"),
                                            new String[]{Bundle.getMessage("TopicSensorRcv")},   Option.Type.TEXT));
                                            
        options.put("12.3", new Option(Bundle.getMessage("NameTopicLightSend"),    
                                       new String[]{Bundle.getMessage("TopicLightSend")},  Option.Type.TEXT));
        options.put("12.5", new Option(Bundle.getMessage("NameTopicLightRcv"),     
                                       new String[]{Bundle.getMessage("TopicLightRcv")},  Option.Type.TEXT));

        options.put("13", new Option("Reporter topic :",    new String[]{Bundle.getMessage("TopicReporter")}, Option.Type.TEXT));
        options.put("14", new Option("Signal Head topic :", new String[]{Bundle.getMessage("TopicSignalHead")}, Option.Type.TEXT));
        options.put("15", new Option("Signal Mast topic :", new String[]{Bundle.getMessage("TopicSignalMast")}, Option.Type.TEXT));
        allowConnectionRecovery = true;
    }

    @Override
    @API(status=API.Status.INTERNAL)
    public void configure() {
        log.debug("Doing configure...");
        mqttEventListeners = new HashMap<>();
        getSystemConnectionMemo().setMqttAdapter(this);
        getSystemConnectionMemo().configureManagers();
        mqttClient.setCallback(this);
    }

    @Override
    @API(status=API.Status.INTERNAL)
    public void connect() throws IOException {
        log.debug("Doing connect with MQTTchannel = \"{}\"", getOptionState(option2Name));
        
        
        try {
            if ( getOptionState(option2Name)!= null && ! getOptionState(option2Name).trim().isEmpty()) {
                baseTopic = getOptionState(option2Name);
            }

            // have to make that a valid choice, overriding the original above. This
            // is ugly and temporary.
            if (! DEFAULT_BASETOPIC.equals(baseTopic)) {
                options.put(option2Name, new Option("MQTT channel: ", new String[]{baseTopic, DEFAULT_BASETOPIC}));
            }

            //generate a unique client ID based on the network ID and the system prefix of the MQTT connection.
            String clientID = jmri.util.node.NodeIdentity.networkIdentity() + getSystemPrefix();

            //ensure that only valid characters are included in the client ID
            clientID = clientID.replaceAll("[^A-Za-z0-9]", "");
            //ensure the length of the client ID doesn't exceed the guaranteed acceptable length of 23
            if (clientID.length() > 23) {
                clientID = clientID.substring(clientID.length() - 23);
            }
            String tempdirName = jmri.util.FileUtil.getExternalFilename(jmri.util.FileUtil.PROFILE);
            log.debug("will use {} as temporary directory", tempdirName);
            
            mqttClient = new MqttClient(PROTOCOL + getCurrentPortName(), 
                                        clientID,
                                        new MqttDefaultFilePersistence(tempdirName));
            mqttClient.connect();
        } catch (MqttException ex) {
            throw new IOException("Can't create MQTT client", ex);
        }
    }
    
    @Override
    @API(status=API.Status.MAINTAINED)
    public MqttSystemConnectionMemo getSystemConnectionMemo() {
        return (MqttSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    @API(status=API.Status.MAINTAINED)
    public void subscribe(String topic, MqttEventListener mel) {
        if (mqttEventListeners == null || mqttClient == null) {
            jmri.util.LoggingUtil.warnOnce(log, "Trying to subscribe before connect/configure is done");
            return;
        }
        try {
            String fullTopic = baseTopic + topic;
            if (mqttEventListeners.containsKey(fullTopic)) {
                if (!mqttEventListeners.get(fullTopic).contains(mel)) {
                    mqttEventListeners.get(fullTopic).add(mel);
                }
                return;
            }
            ArrayList<MqttEventListener> mels = new ArrayList<>();
            mels.add(mel);
            mqttEventListeners.put(fullTopic, mels);
            mqttClient.subscribe(fullTopic);
            log.debug("Subscribed : \"{}\"", fullTopic);
        } catch (MqttException ex) {
            log.error("Can't subscribe : ", ex);
        }
    }

    @API(status=API.Status.MAINTAINED)
    public void unsubscribe(String topic, MqttEventListener mel) {
        String fullTopic = baseTopic + topic;
        if (mqttEventListeners == null || mqttClient == null) {
            jmri.util.LoggingUtil.warnOnce(log, "Trying to unsubscribe before connect/configure is done");
            return;
        }
        mqttEventListeners.get(fullTopic).remove(mel);
        if (mqttEventListeners.get(fullTopic).isEmpty()) {
            try {
                mqttClient.unsubscribe(fullTopic);
                mqttEventListeners.remove(fullTopic);
                log.debug("Unsubscribed : \"{}\"", fullTopic);
            } catch (MqttException ex) {
                log.error("Can't unsubscribe : ", ex);
            }
        }
    }

    @API(status=API.Status.MAINTAINED)
    public void unsubscribeall(MqttEventListener mel) {
        mqttEventListeners.keySet().forEach((t) -> {
            unsubscribe(t, mel);
        });
    }

    /**
     * Send a message over the existing link to a broker.
     * @param topic The topic, which follows the channel and precedes the payload in the message
     * @param payload The payload makes up the final part of the message
     */
    @API(status=API.Status.MAINTAINED)
    public void publish(@Nonnull String topic, @Nonnull byte[] payload) {
        try {
            String fullTopic = baseTopic + topic;
            mqttClient.publish(fullTopic, payload, 2, true);
        } catch (MqttException ex) {
            log.error("Can't publish : ", ex);
        }
    }

    /**
     * Send a message over the existing link to a broker.
     * @param topic The topic, which follows the channel and precedes the payload in the message
     * @param payload The payload makes up the final part of the message
     */
    @API(status=API.Status.MAINTAINED)
    public void publish(@Nonnull String topic, @Nonnull String payload) {
        publish(topic, payload.getBytes());
    }

    public MqttClient getMQttClient() {
        return (mqttClient);
    }

    @Override
    @API(status=API.Status.INTERNAL)
    public void connectionLost(Throwable thrwbl) {
        log.warn("Lost MQTT broker connection...");
        if (this.allowConnectionRecovery) {
            log.info("...trying to reconnect");
            try {
                mqttClient.connect();
                mqttClient.setCallback(this);
                for (String t : mqttEventListeners.keySet()) {
                    mqttClient.subscribe(t);
                }
            } catch (MqttException ex) {
                log.error("Unable to reconnect", ex);
            }
            return;
        }
        log.error("Won't reconnect");
    }

    @Override
    @API(status=API.Status.INTERNAL)
    public void messageArrived(String topic, MqttMessage mm) throws Exception {
        log.debug("Message received, topic : {}", topic);
        
        boolean found = false;
        for (Map.Entry<String,ArrayList<MqttEventListener>> e : mqttEventListeners.entrySet()) {
            // does key match received topic, including wildcards?
            if (MqttTopic.isMatched(e.getKey(), topic) ) {
                found = true;
                e.getValue().forEach((mel) -> {
                    mel.notifyMqttMessage(topic, mm.toString());
                });              
            }
        }
        
        if (!found) {
            log.error("No one subscribed to {}", topic);
            throw new Exception("No subscriber for MQTT topic " + topic);
        }
    }

    @Override
    @API(status=API.Status.INTERNAL)
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        log.debug("Message delivered");
    }

    private final static Logger log = LoggerFactory.getLogger(MqttAdapter.class);

}
