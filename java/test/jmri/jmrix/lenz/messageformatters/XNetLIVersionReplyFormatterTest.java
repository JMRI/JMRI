package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetReply;

import org.junit.jupiter.api.*;

/**
 * Tests for the XNetLIVersionReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLIVersionReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testHandlesMessage() {
        XNetReply r = new XNetReply("02 01 36 34");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyLIVersion",0.1,3.6),formatter.formatMessage(r));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new XNetLIVersionReplyFormatter();
    }

}
