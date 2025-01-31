package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetEstopLocoRequest class.
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetEstopLocoRequestMessageFormatterTest {

    @Test
    public void testFormatEstopLocoRequestMessage() {
        XNetEstopLocoRequestMessageFormatter formatter = new XNetEstopLocoRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getAddressedEmergencyStop(1234);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("Emergency Stop 1234", formatter.formatMessage(msg), "Monitor String");
    }
}
