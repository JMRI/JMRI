package jmri.jmrix.mqtt;

import java.util.ArrayList;
import java.util.HashMap;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import org.eclipse.paho.client.mqttv3.MqttMessage;
/**
 * Tests for MqttAdapter class.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.5
 */
public class MqttAdapterTest {

    @Test
    public void testConstructor() {
        MqttAdapter a = new MqttAdapter();
        Assert.assertNotNull("constructor", a);
    }
    
    String lastTopic;
    String lastMessage;
    
    private class TestListener implements MqttEventListener {
        @Override
        public void notifyMqttMessage(String topic, String message){
            lastTopic = topic;
            lastMessage = message;
        }
    }
    
    @Test
    // tests via internal data structures
    public void messageArrivedTopicTest() throws Exception {
        MqttAdapter a = new MqttAdapter();
        a.mqttEventListeners = new HashMap<>();
        
        ArrayList<MqttEventListener> mels = new ArrayList<>();
        mels.add(new TestListener());
        a.mqttEventListeners.put("/trains/foo", mels);
        
        a.messageArrived("/trains/foo", new MqttMessage());
        
        Assert.assertEquals( "/trains/foo", lastTopic);
        Assert.assertEquals("", lastMessage);
    }

    @Test
    // tests via internal data structures
    public void messageArrivedWildcardTest() throws Exception {
        MqttAdapter a = new MqttAdapter();
        a.mqttEventListeners = new HashMap<>();

        ArrayList<MqttEventListener> mels = new ArrayList<>();
        mels.add(new TestListener());
        a.mqttEventListeners.put("/trains/#", mels);
        
        a.messageArrived("/trains/foo", new MqttMessage());
        
        Assert.assertEquals("/trains/foo", lastTopic );
        Assert.assertEquals("", lastMessage);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        lastTopic = null;
        lastMessage = null;
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
