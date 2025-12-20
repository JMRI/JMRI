package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetReply;

import org.junit.jupiter.api.*;

/**
 * Tests for the XNetLI101AddressReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLI101AddressReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testMonitorStringLIAddressReply(){
        XNetReply r = new XNetReply("F2 01 01 F2");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals( Bundle.getMessage("XNetReplyLIAddress",1),formatter.formatMessage(r));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new XNetLI101AddressReplyFormatter();
    }

}
