package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetServiceModeResponseFormatter class.

 * @author Paul Bender Copyright (C) 2024
 */
public class XNetServiceModeResponseFormatterTest {

    @Test
    public void testDirectModeResponseFormatter() {
        XNetServiceModeResponseFormatter formatter = new XNetServiceModeResponseFormatter();
        XNetReply r = new XNetReply("63 14 01 04 72");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyServiceModeDirectResponse",1,4),formatter.formatMessage(r));
    }

    @Test
    public void testPagedModeResponseFormatter() {
        XNetServiceModeResponseFormatter formatter = new XNetServiceModeResponseFormatter();
        XNetReply r = new XNetReply("63 10 01 04 76");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyServiceModePagedResponse",1,4),formatter.formatMessage(r));
    }

}
