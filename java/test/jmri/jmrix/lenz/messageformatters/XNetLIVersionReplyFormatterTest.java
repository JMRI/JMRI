package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetLIVersionReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLIVersionReplyFormatterTest {

    @Test
    public void testHandlesMessage() {
        XNetLIVersionReplyFormatter formatter = new XNetLIVersionReplyFormatter();
        XNetReply r = new XNetReply("02 01 36 34");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyLIVersion",0.1,3.6),formatter.formatMessage(r));
    }
}
