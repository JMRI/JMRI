package jmri.jmrix.mqtt;

import javax.annotation.Nonnull;

import jmri.Turnout;
import jmri.implementation.AbstractTurnoutTestBase;
import jmri.util.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for MqttTurnout class.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.5
 */
public class MqttTurnoutTest extends AbstractTurnoutTestBase {

    MqttAdapter a;
    String saveTopic;
    byte[] savePayload;
    
    @Before
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

        t = new MqttTurnout(a, "MT2", "track/turnout/2");
        JUnitAppender.assertWarnMessage("Trying to subscribe before connect/configure is done");
    }

    @Override
    public int numListeners() {
        // return tcis.numListeners();
        return 0;
    }

    @Test
    public void testParserUpdate() {
        MqttContentParser<Turnout> parser = new MqttContentParser<Turnout>() {
            private final String closedText = "BAR";
            private final String thrownText = "FOO";
            @Override
            public void beanFromPayload(@Nonnull Turnout bean, @Nonnull String payload, @Nonnull String topic) {
                switch (payload) {
                    case closedText:                
                        ((MqttTurnout)t).newKnownState(Turnout.CLOSED);
                        break;
                    case thrownText:
                        ((MqttTurnout)t).newKnownState(Turnout.THROWN);
                        break;
                    default:
                        break;
                }
            }
        
            @Override
            public @Nonnull String payloadFromBean(@Nonnull Turnout bean, int newState) {
                // calls jmri.implementation.AbstractTurnout#stateChangeCheck(int)
                String text = "";
                try {
                    text = (((MqttTurnout)t).stateChangeCheck(newState) ? closedText : thrownText);
                } catch (IllegalArgumentException ex) {
                    //log.error("new state invalid, Turnout not set");
                }
                return text;
            }
        };

        ((MqttTurnout)t).setParser(parser);
        
        t.setCommandedState(Turnout.THROWN);
        
        Assert.assertEquals("topic", "track/turnout/2", saveTopic);
        Assert.assertEquals("topic", "FOO", new String(savePayload));
        
        t.setCommandedState(Turnout.CLOSED);
        
        Assert.assertEquals("topic", "track/turnout/2", saveTopic);
        Assert.assertEquals("topic", "BAR", new String(savePayload));
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertEquals("topic", "track/turnout/2", saveTopic);
        Assert.assertEquals("topic", "THROWN", new String(savePayload));
    }

    @Override
    public void checkClosedMsgSent() {
        Assert.assertEquals("topic", "track/turnout/2", saveTopic);
        Assert.assertEquals("topic", "CLOSED", new String(savePayload));
    }

}
