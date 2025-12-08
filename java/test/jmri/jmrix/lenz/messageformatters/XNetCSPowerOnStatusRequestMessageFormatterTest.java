package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetCSPowerOnStatusRequestMessageFormatter class.
 * @author Paul Bender Copyright (C) 2024
 */

public class XNetCSPowerOnStatusRequestMessageFormatterTest {

        @Test
        public void testFormatCSPowerOnAutoRequestMessage() {
            XNetCSPowerOnStatusRequestMessageFormatter formatter = new XNetCSPowerOnStatusRequestMessageFormatter();
            XNetMessage msg = XNetMessage.getCSAutoStartMessage(true);
            Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
            Assertions.assertEquals("REQUEST: Set Power-up Mode to Automatic", formatter.formatMessage(msg), "Monitor String");
        }

    @Test
    public void testFormatCSPowerOnManualRequestMessage() {
        XNetCSPowerOnStatusRequestMessageFormatter formatter = new XNetCSPowerOnStatusRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getCSAutoStartMessage(false);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST: Set Power-up Mode to Manual", formatter.formatMessage(msg), "Monitor String");
    }
}
