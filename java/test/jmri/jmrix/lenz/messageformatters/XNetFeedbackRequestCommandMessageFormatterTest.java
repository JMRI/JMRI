package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetMessage;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Tests for the XNetFeedbackRequestCommandMessageFormater class.
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetFeedbackRequestCommandMessageFormatterTest {

    @Test
    public void testHandlesMessageValid() {
        XNetFeedbackRequestCommandMessageFormatter formatter = new XNetFeedbackRequestCommandMessageFormatter();
        assertTrue(formatter.handlesMessage(XNetMessage.getFeedbackRequestMsg(5,true)));
    }

    @Test
    public void testHandlesMessageInvalid() {
        XNetFeedbackRequestCommandMessageFormatter formatter = new XNetFeedbackRequestCommandMessageFormatter();
        assertFalse(formatter.handlesMessage(new XNetMessage("01 04 05")));
    }

    @Test
    public void testFormatMessage() {
        XNetFeedbackRequestCommandMessageFormatter formatter = new XNetFeedbackRequestCommandMessageFormatter();
        XNetMessage message;
        message = XNetMessage.getFeedbackRequestMsg(5,true);
        Assert.assertEquals("Monitor String","Accessory Decoder/Feedback Encoder Status Request: Base Address 1,Lower Nibble.",formatter.formatMessage(message));
        message = XNetMessage.getFeedbackRequestMsg(5,false);
        Assert.assertEquals("Monitor String","Accessory Decoder/Feedback Encoder Status Request: Base Address 1,Upper Nibble.",formatter.formatMessage(message));
    }
}
