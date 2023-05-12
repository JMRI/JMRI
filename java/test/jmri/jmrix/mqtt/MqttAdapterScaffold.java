package jmri.jmrix.mqtt;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import org.junit.jupiter.api.Assertions;

import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;

/**
 * MqttAdapterScaffold serves as a scaffold for testing purposes.
 * It provides a mock MqttClient object.
 * It captures arguments passed to the publish method of the MqttClient.
 * @author Steve Young Copyright(C) 2023
 */
public class MqttAdapterScaffold extends MqttAdapter {

    private MqttClient mockClient;

    // Argument captors for the publish method of MqttClient
    private ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
    private ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
    private ArgumentCaptor<Integer> qosCaptor = ArgumentCaptor.forClass(Integer.class);
    private ArgumentCaptor<Boolean> retainedCaptor = ArgumentCaptor.forClass(Boolean.class);

    /**
     * Create a new Mqtt Adapter Scaffold.
     * @param connectAndConfigure true to call the connect and configure methods 
     * to more fully setup the connection. 
     */
    public MqttAdapterScaffold(boolean connectAndConfigure){
        super();
        if (connectAndConfigure){
            try {
                connect();
                configure();
            }
            catch (IOException ex) {
                Assertions.fail("could not connect to AdapterScaffold", ex);
            }
        }
    }

    private int publishCount = 0;

    @Override
    MqttClient getNewMqttClient(String clientID, String tempdirName) throws MqttException {
        mockClient = mock(MqttClient.class);
        doAnswer(invocation -> {
            publishCount++;
            return null;
        }).when(mockClient).publish(anyString(), any(byte[].class), anyInt(), anyBoolean());
        return mockClient;
    }

    /**
     * Get Number of counts that publish has been called.
     * @return number of times an item has been published, default 0
     */
    public int getPublishCount(){
        return publishCount;
    }

    /**
     * Returns the last Topic passed to the
     * publish(String, byte[], Integer, Boolean)
     * method of the mock MqttClient.
     * Verifies that at least 1 message has been published.
     * @return The last captured topic.
     */
    public String getLastTopic(){
        verifyPublishCapture();
        return topicCaptor.getValue();
    }

    /**
     * Returns the last Payload passed to the
     * publish(String, byte[], Integer, Boolean)
     * method of the mock MqttClient.
     * Verifies that at least 1 message has been published.
     * @return The last captured topic.
     */
    public byte[] getLastPayload() {
        verifyPublishCapture();
        return payloadCaptor.getValue();
    }

    private void verifyPublishCapture() {
        try {
            verify(mockClient, atLeastOnce()).publish(topicCaptor.capture(),
                payloadCaptor.capture(), qosCaptor.capture(), retainedCaptor.capture());
        } catch (MqttException ex) {
            Assertions.fail("Could not verify mockclient Publish", ex);
        }
    }

}
