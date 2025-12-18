package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.junit.jupiter.api.*;

/**
 * Tests for the XNetEstopLocoRequest class.
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetEstopLocoRequestMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatEstopLocoRequestMessage() {
        XNetMessage msg = XNetMessage.getAddressedEmergencyStop(1234);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("Emergency Stop 1234", formatter.formatMessage(msg), "Monitor String");
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new XNetEstopLocoRequestMessageFormatter();
    }

}
