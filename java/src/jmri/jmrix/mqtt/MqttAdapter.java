package jmri.jmrix.mqtt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Lionel Jeanson
 */
public class MqttAdapter extends jmri.jmrix.AbstractNetworkPortController implements MqttCallback {

    private final static String PROTOCOL = "tcp://";
    private final static String CLID = "JMRI";
    private final static String DEFAULT_BASETOPIC = "/trains/";
    
    public String baseTopic = DEFAULT_BASETOPIC;

    HashMap<String, ArrayList<MqttEventListener>> mqttEventListeners;

    MqttClient mqttClient;

    public MqttAdapter() {
        super(new MqttSystemConnectionMemo());
        log.debug("Doing ctor...");
        option2Name = "MQTTchannel";
        options.put(option2Name, new Option("MQTT channel :", new String[]{baseTopic}));
        allowConnectionRecovery = true;
    }

    @Override
    public void configure() {
        log.debug("Doing configure...");
        mqttEventListeners = new HashMap<>();
        getSystemConnectionMemo().setMqttAdapter(this);
        getSystemConnectionMemo().configureManagers();
        mqttClient.setCallback(this);
    }

    @Override
    public void connect() throws IOException {
        log.debug("Doing connect with MQTTchannel = \"{}\"", getOptionState("MQTTchannel"));
        
        
        try {
            if (! getOptionState("MQTTchannel").trim().isEmpty()) {
                baseTopic = getOptionState("MQTTchannel");
            }

            // have to make that a valid choice, overriding the original above. This
            // is ugly and temporary.
            if (! DEFAULT_BASETOPIC.equals(baseTopic)) {
                options.put(option2Name, new Option("MQTT channel :", new String[]{baseTopic, DEFAULT_BASETOPIC}));
            }

            String clientID = CLID + "-" + this.getUserName();
            mqttClient = new MqttClient(PROTOCOL + getCurrentPortName(), clientID);
            mqttClient.connect();
        } catch (MqttException ex) {
            throw new IOException("Can't create MQTT client", ex);
        }
    }
    
    @Override
    public MqttSystemConnectionMemo getSystemConnectionMemo() {
        return (MqttSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    public void subscribe(String topic, MqttEventListener mel) {
        if (mqttEventListeners == null || mqttClient == null) {
            jmri.util.Log4JUtil.warnOnce(log, "Trying to subscribe before connect/configure is done");
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
        } catch (MqttException ex) {
            log.error("Can't subscribe : ", ex);
        }
    }

    public void unsubscribe(String topic, MqttEventListener mel) {
        String fullTopic = baseTopic + topic;
        mqttEventListeners.get(fullTopic).remove(mel);
        if (mqttEventListeners.get(fullTopic).isEmpty()) {
            try {
                mqttClient.unsubscribe(fullTopic);
                mqttEventListeners.remove(fullTopic);
            } catch (MqttException ex) {
                log.error("Can't unsubscribe : ", ex);
            }
        }
    }

    public void unsubscribeall(MqttEventListener mel) {
        mqttEventListeners.keySet().forEach((t) -> {
            unsubscribe(t, mel);
        });
    }

    public void publish(String topic, byte[] payload) {
        try {
            String fullTopic = baseTopic + topic;
            mqttClient.publish(fullTopic, payload, 2, true);
        } catch (MqttException ex) {
            log.error("Can't publish : ", ex);
        }
    }

    public MqttClient getMQttClient() {
        return (mqttClient);
    }

    @Override
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
    public void messageArrived(String topic, MqttMessage mm) throws Exception {
        log.debug("Message received, topic : {}", topic);
        if (!mqttEventListeners.containsKey(topic)) {
            log.error("No one subscribed to {}", topic);
            throw new Exception("No subscriber for MQTT topic " + topic);
        }
        mqttEventListeners.get(topic).forEach((mel) -> {
            mel.notifyMqttMessage(topic, mm.toString());
        });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        log.debug("Message delivered");
    }

    private final static Logger log = LoggerFactory.getLogger(MqttAdapter.class);

}
