package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.junit.jupiter.api.*;

/**
 * Tests for the XNetEstopAllRequestMessageFormatter class.
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetEstopAllRequestMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatEstopAllRequestMessage() {
        XNetMessage msg = XNetMessage.getEmergencyStopMsg();
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST: Emergency Stop", formatter.formatMessage(msg), "Monitor String");
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new XNetEstopAllRequestMessageFormatter();
    }

}
