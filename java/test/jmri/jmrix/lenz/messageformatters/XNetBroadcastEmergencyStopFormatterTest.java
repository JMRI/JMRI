package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetReply;

import org.junit.jupiter.api.*;

/**
 * Tests for the XNetBroadcastEmergencyStopFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetBroadcastEmergencyStopFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testEmergencyStopFormatter(){
        XNetReply r = new XNetReply("81 00 81");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyBCEverythingStop"), formatter.formatMessage(r));
    }

    @Override
    @BeforeEach
    public void setUp(){
        super.setUp(); // setup JUnit
        formatter = new XNetBroadcastEmergencyStopFormatter();
    }

}
