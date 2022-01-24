package jmri.jmrix.mqtt;

import jmri.Light;
import jmri.implementation.AbstractLightTestBase;
import jmri.util.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for MqttLight class.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.5
 */
public class MqttLightTest extends AbstractLightTestBase {

    MqttAdapter a;
    String saveTopic;
    byte[] savePayload;

    @BeforeEach
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        // prepare an interface
        saveTopic = null;
        savePayload = null;
        a = new MqttAdapter(){
                @Override
                public void publish(String topic, byte[] payload) {
                    saveTopic = topic;
                    savePayload = payload;
                }
            };

        t = new MqttLight(a, "ML2", "", "track/light/2", "track/light/2/foo");
        JUnitAppender.assertWarnMessage("Trying to subscribe before connect/configure is done");
    }

    @AfterEach
    public void tearDown() {
       jmri.util.JUnitUtil.tearDown();
     }

    @Override
    public int numListeners() {
        // return tcis.numListeners();
        return 0;
    }

    @Test
    public void testParserUpdate() {

        t.setCommandedState(Light.ON);

        JUnitUtil.waitFor( ()->{
            return "track/light/2".equals(saveTopic);
        }, "topic check");
        Assert.assertEquals("topic", "ON", new String(savePayload));

        saveTopic = null;
        savePayload = null;

        t.setCommandedState(Light.OFF);

        JUnitUtil.waitFor( ()->{
            return "track/light/2".equals(saveTopic);
        }, "topic check");
        Assert.assertEquals("topic", "OFF", new String(savePayload));

    }

    @Test
    public void testParserModes() {
        ((MqttLight)t).notifyMqttMessage("track/light/2/foo", "ON");
        Assert.assertEquals("state", Light.ON, t.getKnownState());
        ((MqttLight)t).notifyMqttMessage("track/light/2/foo", "OFF");
        Assert.assertEquals("state", Light.OFF, t.getKnownState());
        ((MqttLight)t).notifyMqttMessage("track/light/2/foo", "UNKNOWN");
        Assert.assertEquals("state", Light.UNKNOWN, t.getKnownState());
    }

    @Override
    public void checkOnMsgSent() {
        JUnitUtil.waitFor( ()->{
            return "track/light/2".equals(saveTopic);
        }, "topic check");
        Assert.assertEquals("topic", "track/light/2", saveTopic);
        Assert.assertEquals("topic", "ON", new String(savePayload));
    }

    @Override
    public void testCommandOff() {
        // Lights are initialized to OFF, so no order is sent. do this to force sening the off later.
        t.setState(Light.ON);
        super.testCommandOff();
    }

    @Override
    public void checkOffMsgSent() {
        Assert.assertEquals("topic", "track/light/2", saveTopic);
        Assert.assertEquals("topic", "OFF", new String(savePayload));
    }
}
