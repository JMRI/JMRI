package jmri.jmrix.mqtt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.Light;
import jmri.implementation.AbstractLightTestBase;
import jmri.util.*;

import org.junit.jupiter.api.*;

/**
 * Tests for MqttLight class.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.5
 */
public class MqttLightTest extends AbstractLightTestBase {

    private MqttAdapterScaffold a = null;

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
        assertEquals("track/light/2", a.getLastTopic(),"topic");
        assertEquals("ON", new String(a.getLastPayload()),"payload");

        t.setCommandedState(Light.OFF);

        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==3; }, "publish triggered 2");
        assertEquals("track/light/2", a.getLastTopic(),"topic");
        assertEquals("OFF", new String(a.getLastPayload()),"payload");

    }

    @Test
    public void testParserModes() {
        ((MqttLight)t).notifyMqttMessage("track/light/2/foo", "ON");
        assertEquals( Light.ON, t.getKnownState(), "state");
        ((MqttLight)t).notifyMqttMessage("track/light/2/foo", "OFF");
        assertEquals( Light.OFF, t.getKnownState(), "state");
        ((MqttLight)t).notifyMqttMessage("track/light/2/foo", "UNKNOWN");
        assertEquals( Light.UNKNOWN, t.getKnownState(), "state");
    }

    @Override
    public void checkOnMsgSent() {
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==2; }, "publish triggered");
        assertEquals("track/light/2", a.getLastTopic(),"topic");
        assertEquals("ON", new String(a.getLastPayload()),"payload");
    }

    @Test
    @Override
    public void testCommandOff() {
        // Lights are initialized to OFF, so no order is sent. do this to force sening the off later.
        t.setState(Light.ON);
        super.testCommandOff();
    }

    @Override
    public void checkOffMsgSent() {
        JUnitUtil.waitFor( ()->{ return a.getPublishCount()==3; }, "publish on then off triggered");
        assertEquals("track/light/2", a.getLastTopic(),"topic");
        assertEquals("OFF", new String(a.getLastPayload()),"payload");
    }
}
