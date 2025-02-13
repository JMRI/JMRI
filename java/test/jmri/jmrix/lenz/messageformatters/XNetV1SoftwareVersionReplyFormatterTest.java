package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetV1SoftwareVersionReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetV1SoftwareVersionReplyFormatterTest {

    @Test
    public void testV1SoftwareVersionReply() {
        XNetV1SoftwareVersionReplyFormatter formatter = new XNetV1SoftwareVersionReplyFormatter();
        XNetReply r = new XNetReply("62 21 21 62");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyCSVersionV1",2.1,"32"),formatter.formatMessage(r));
    }
}
