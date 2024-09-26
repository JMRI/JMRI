package jmri.jmrix.lenz.messageFormatters;

import jmri.jmrix.Message;
import jmri.jmrix.lenz.XNetMessage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
/**
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetMessageFormatterTest {

    @Test
    public void testFormatter(){
        XNetMessageFormatter formatter = new XNetMessageFormatter();
        Message message = XNetMessage.getExitProgModeMsg();
        assertTrue(formatter.handlesMessage(message));
        assertEquals(message.toMonitorString(), formatter.formatMessage(message));
    }
}
