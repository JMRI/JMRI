package jmri.jmrix.lenz.messageFormatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetReplyFormatterTest {

    @Test
    public void testFormatter(){
        XNetReplyFormatter formatter = new XNetReplyFormatter();
        Message message = new XNetReply("01 04 05");
        assertTrue(formatter.handlesMessage(message));
        assertEquals(message.toMonitorString(), formatter.formatMessage(message));
    }
}
