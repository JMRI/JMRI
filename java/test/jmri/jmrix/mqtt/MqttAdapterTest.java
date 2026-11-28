package jmri.jmrix.mqtt;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.util.JUnitAppender;
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
        a.mqttEventListeners = new ConcurrentHashMap<>();
        
        CopyOnWriteArrayList<MqttEventListener> mels = new CopyOnWriteArrayList<>();
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
        a.mqttEventListeners = new ConcurrentHashMap<>();

        CopyOnWriteArrayList<MqttEventListener> mels = new CopyOnWriteArrayList<>();
        mels.add(new TestListener());
        a.mqttEventListeners.put("/trains/#", mels);
        
        a.messageArrived("/trains/foo", new MqttMessage());
        
        Assert.assertEquals("/trains/foo", lastTopic );
        Assert.assertEquals("", lastMessage);
    }

    @Test
    // a message nobody listens to is logged, not thrown, since a throw
    // from messageArrived makes Paho drop the connection
    public void messageArrivedNoSubscriberTest() throws Exception {
        MqttAdapter a = new MqttAdapter();
        a.mqttEventListeners = new ConcurrentHashMap<>();

        a.messageArrived("/trains/foo", new MqttMessage());

        JUnitAppender.assertErrorMessage("No one subscribed to /trains/foo");
        Assert.assertNull(lastTopic);
    }

    @Test
    // retained messages arrive on the Paho thread while the table load
    // is still adding topics; messageArrived must not fail meanwhile
    public void messageArrivedWhileTopicsAddedTest() throws Exception {
        MqttAdapter a = new MqttAdapter();
        a.mqttEventListeners = new ConcurrentHashMap<>();
        CopyOnWriteArrayList<MqttEventListener> mels = new CopyOnWriteArrayList<>();
        mels.add(new TestListener());
        a.mqttEventListeners.put("/trains/foo", mels);

        AtomicBoolean done = new AtomicBoolean(false);
        Thread adder = new Thread(() -> {
            for (int i = 0; i < 20000; i++) {
                a.mqttEventListeners.put("/trains/t" + i, new CopyOnWriteArrayList<>());
            }
            done.set(true);
        }, "MqttAdapterTest adder");
        adder.start();
        int delivered = 0;
        while (!done.get()) {
            a.messageArrived("/trains/foo", new MqttMessage());
            delivered++;
        }
        adder.join();

        Assert.assertTrue("messages delivered during the adds", delivered > 0);
        Assert.assertEquals("/trains/foo", lastTopic);
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
