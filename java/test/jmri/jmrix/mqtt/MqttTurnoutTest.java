package jmri.jmrix.mqtt;

import javax.annotation.Nonnull;

import jmri.Turnout;
import jmri.implementation.AbstractTurnoutTestBase;
import jmri.util.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for MqttTurnout class.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.5
 */
public class MqttTurnoutTest extends AbstractTurnoutTestBase {

    MqttAdapterScaffold a = null;

    @Override
    public int numListeners() {
        return 0;
    }

    @Test
    public void testParserUpdate() {
        MqttContentParser<Turnout> parser = new MqttContentParser<Turnout>() {
            private final static String CLOSED_TEXT = "BAR";
            private final static String THROWN_TEXT = "FOO";
            @Override
            public void beanFromPayload(@Nonnull Turnout bean, @Nonnull String payload, @Nonnull String topic) {
                switch (payload) {
                    case CLOSED_TEXT:                
                        ((MqttTurnout)t).newKnownState(Turnout.CLOSED);
                        break;
                    case THROWN_TEXT:
                        ((MqttTurnout)t).newKnownState(Turnout.THROWN);
                        break;
                    default:
                        break;
                }
            }
        
            @Override
            public @Nonnull String payloadFromBean(@Nonnull Turnout bean, int newState){
                // sort out states
                if ((newState & Turnout.CLOSED) != 0) {
                    // first look for the double case, which we can't handle
                    if ((newState & Turnout.THROWN ) != 0) {
                        // this is the disaster case!
                        throw new IllegalArgumentException("Cannot command both CLOSED and THROWN: "+newState);
                    } else {
                        // send a CLOSED command
                        return CLOSED_TEXT;
                    }
                } else {
                    // send a THROWN command
                    return THROWN_TEXT;
                }
            }
        };

        ((MqttTurnout)t).setParser(parser);
        
        t.setCommandedState(Turnout.THROWN);
        Assertions.assertEquals(2, a.getPublishCount(),"2 message sent");
        Assertions.assertEquals("track/turnout/2", a.getLastTopic(),"topic");
        Assertions.assertEquals("FOO", new String(a.getLastPayload()),"payload");

        t.setCommandedState(Turnout.CLOSED);
        Assertions.assertEquals(3, a.getPublishCount(),"3 messages sent");
        Assertions.assertEquals("track/turnout/2", a.getLastTopic(),"topic");
        Assertions.assertEquals("BAR", new String(a.getLastPayload()),"payload");
    }

    @Test
    public void testParserModes() {

        t.setFeedbackMode(Turnout.DIRECT);

        ((MqttTurnout)t).notifyMqttMessage("track/turnout/2", "CLOSED");
        Assert.assertEquals("state", Turnout.CLOSED, t.getKnownState());
        ((MqttTurnout)t).notifyMqttMessage("track/turnout/2", "THROWN");
        Assert.assertEquals("state", Turnout.THROWN, t.getKnownState());
        ((MqttTurnout)t).notifyMqttMessage("track/turnout/2", "UNKNOWN");
        Assert.assertEquals("state", Turnout.UNKNOWN, t.getKnownState());

        t.setFeedbackMode(Turnout.MONITORING);

        ((MqttTurnout)t).notifyMqttMessage("track/turnout/2", "CLOSED");
        Assert.assertEquals("state", Turnout.CLOSED, t.getKnownState());
        ((MqttTurnout)t).notifyMqttMessage("track/turnout/2", "THROWN");
        Assert.assertEquals("state", Turnout.THROWN, t.getKnownState());
        ((MqttTurnout)t).notifyMqttMessage("track/turnout/2", "UNKNOWN");
        Assert.assertEquals("state", Turnout.UNKNOWN, t.getKnownState());

        t.setFeedbackMode(Turnout.ONESENSOR);
        ((MqttTurnout)t).notifyMqttMessage("track/turnout/2", "CLOSED");
        Assert.assertEquals("state", Turnout.UNKNOWN, t.getKnownState());

        t.setFeedbackMode(Turnout.TWOSENSOR);
        ((MqttTurnout)t).notifyMqttMessage("track/turnout/2", "CLOSED");
        Assert.assertEquals("state", Turnout.UNKNOWN, t.getKnownState());

    }

    @Override
    public void checkThrownMsgSent() {
        Assertions.assertEquals("track/turnout/2", a.getLastTopic(),"topic");
        Assertions.assertEquals("THROWN", new String(a.getLastPayload()),"payload");
    }

    @Override
    public void checkClosedMsgSent() {
        Assertions.assertEquals("track/turnout/2", a.getLastTopic(),"topic");
        Assertions.assertEquals("CLOSED", new String(a.getLastPayload()),"payload");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        // prepare an interface
        a = new MqttAdapterScaffold(true);
        t = new MqttTurnout(a, "MT2", "track/turnout/2", "track/turnout/2/foo");
    }

    @AfterEach
    @Override
    public void tearDown() {
        t.dispose();
        a.dispose();
        JUnitUtil.tearDown();
    }

}
