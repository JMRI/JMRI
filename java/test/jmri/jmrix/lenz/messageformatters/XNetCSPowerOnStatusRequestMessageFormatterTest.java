package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.junit.jupiter.api.*;

/**
 * Tests for the XNetCSPowerOnStatusRequestMessageFormatter class.
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetCSPowerOnStatusRequestMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatCSPowerOnAutoRequestMessage() {
        XNetMessage msg = XNetMessage.getCSAutoStartMessage(true);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST: Set Power-up Mode to Automatic", formatter.formatMessage(msg), "Monitor String");
    }

    @Test
    public void testFormatCSPowerOnManualRequestMessage() {
        XNetMessage msg = XNetMessage.getCSAutoStartMessage(false);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST: Set Power-up Mode to Manual", formatter.formatMessage(msg), "Monitor String");
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new XNetCSPowerOnStatusRequestMessageFormatter();
    }

}
