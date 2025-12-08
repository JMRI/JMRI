package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the XNetFeedbackRequestCommandMessageFormatter class.
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetFeedbackRequestCommandMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testHandlesMessageValid() {
        assertTrue(formatter.handlesMessage(XNetMessage.getFeedbackRequestMsg(5,true)));
    }

    @Test
    public void testHandlesMessageInvalid() {
        assertFalse(formatter.handlesMessage(new XNetMessage("01 04 05")));
    }

    @Test
    public void testFormatMessage() {
        XNetMessage message;
        message = XNetMessage.getFeedbackRequestMsg(5,true);
        assertEquals( "Accessory Decoder/Feedback Encoder Status Request: Base Address 1,Lower Nibble.",
            formatter.formatMessage(message), "Monitor String");
        message = XNetMessage.getFeedbackRequestMsg(5,false);
        assertEquals( "Accessory Decoder/Feedback Encoder Status Request: Base Address 1,Upper Nibble.",
            formatter.formatMessage(message), "Monitor String");
    }

    @Override
    @BeforeEach
    public void setUp(){
        super.setUp(); // setup JUnit
        formatter = new XNetFeedbackRequestCommandMessageFormatter();
    }

}
