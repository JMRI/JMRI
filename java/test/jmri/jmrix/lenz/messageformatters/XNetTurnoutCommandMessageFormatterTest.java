package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetMessage;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the XNetTurnoutCommandMessageFormatter class.
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetTurnoutCommandMessageFormatterTest {
    // Test that the handlesMessage method returns true for a valid message
    @Test
    public void testHandlesMessageValid() {
        XNetTurnoutCommandMessageFormatter formatter = new XNetTurnoutCommandMessageFormatter();
        XNetMessage message = XNetMessage.getTurnoutCommandMsg(5,false,true,true);
        assertTrue(formatter.handlesMessage(message));
    }

    // Test that the handlesMessage method returns false for an invalid message
    @Test
    public void testHandlesMessageInvalid() {
        XNetTurnoutCommandMessageFormatter formatter = new XNetTurnoutCommandMessageFormatter();
        Message message = new XNetMessage("01 04 05");
        assertFalse(formatter.handlesMessage(message));
    }

    // Test that the formatMessage method returns the expected string
    @Test
    public void testFormatMessage() {
        XNetTurnoutCommandMessageFormatter formatter = new XNetTurnoutCommandMessageFormatter();
        XNetMessage message;
        message = XNetMessage.getTurnoutCommandMsg(5,false,true,true);
        Assert.assertEquals("Monitor String","Accessory Decoder Operations Request: Turnout Address 5(Base Address 1,Sub Address 0) Turn Output 1 On.",
                formatter.formatMessage(message));
        message = XNetMessage.getTurnoutCommandMsg(5,true,false,true);
        Assert.assertEquals("Monitor String","Accessory Decoder Operations Request: Turnout Address 5(Base Address 1,Sub Address 0) Turn Output 0 On.",
                formatter.formatMessage(message));
        message = XNetMessage.getTurnoutCommandMsg(5,false,true,false);
        Assert.assertEquals("Monitor String","Accessory Decoder Operations Request: Turnout Address 5(Base Address 1,Sub Address 0) Turn Output 1 Off.",
                formatter.formatMessage(message));
        message = XNetMessage.getTurnoutCommandMsg(5,true,false,false);
        Assert.assertEquals("Monitor String","Accessory Decoder Operations Request: Turnout Address 5(Base Address 1,Sub Address 0) Turn Output 0 Off.",
                formatter.formatMessage(message));
    }
}
