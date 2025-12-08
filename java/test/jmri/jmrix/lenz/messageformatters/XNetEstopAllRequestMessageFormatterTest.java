package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetEstopAllRequestMessageFormatter class.
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetEstopAllRequestMessageFormatterTest{

    @Test
    public void testFormatEstopAllRequestMessage() {
        XNetEstopAllRequestMessageFormatter formatter = new XNetEstopAllRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getEmergencyStopMsg();
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST: Emergency Stop", formatter.formatMessage(msg), "Monitor String");
    }
}
