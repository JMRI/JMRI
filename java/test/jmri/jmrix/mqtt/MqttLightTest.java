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

    MqttAdapterScaffold a = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        // prepare an interface
        a = new MqttAdapterScaffold(true);
        t = new MqttLight(a, "ML2", "", "track/light/2", "track/light/2/foo");
    }

    @AfterEach
    public void tearDown() {
        t.dispose();
        a.dispose();
        JUnitUtil.tearDown();
    }

    @Override
    public int numListeners() {
        return 0;
    }

    @Test
    public void testParserUpdate() {

        t.setCommandedState(Light.ON);

        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==2; }, "publish triggered");
        Assertions.assertEquals("track/light/2", a.getLastTopic(),"topic");
        Assertions.assertEquals("ON", new String(a.getLastPayload()),"payload");

        t.setCommandedState(Light.OFF);

        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==3; }, "publish triggered 2");
        Assertions.assertEquals("track/light/2", a.getLastTopic(),"topic");
        Assertions.assertEquals("OFF", new String(a.getLastPayload()),"payload");

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
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==2; }, "publish triggered");
        Assertions.assertEquals("track/light/2", a.getLastTopic(),"topic");
        Assertions.assertEquals("ON", new String(a.getLastPayload()),"payload");
    }

    @Override
    public void testCommandOff() {
        // Lights are initialized to OFF, so no order is sent. do this to force sening the off later.
        t.setState(Light.ON);
        super.testCommandOff();
    }

    @Override
    public void checkOffMsgSent() {
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==3; }, "publish on then off triggered");
        Assertions.assertEquals("track/light/2", a.getLastTopic(),"topic");
        Assertions.assertEquals("OFF", new String(a.getLastPayload()),"payload");
    }
}
