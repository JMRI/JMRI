package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetReply;

import org.junit.jupiter.api.*;

/**
 * Tests for the XNetServiceModeResponseFormatter class.

 * @author Paul Bender Copyright (C) 2024
 */
public class XNetServiceModeResponseFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testDirectModeResponseFormatter() {
        XNetReply r = new XNetReply("63 14 01 04 72");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyServiceModeDirectResponse",1,4),formatter.formatMessage(r));
    }

    @Test
    public void testPagedModeResponseFormatter() {
        XNetReply r = new XNetReply("63 10 01 04 76");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyServiceModePagedResponse",1,4),formatter.formatMessage(r));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new XNetServiceModeResponseFormatter();
    }

}
