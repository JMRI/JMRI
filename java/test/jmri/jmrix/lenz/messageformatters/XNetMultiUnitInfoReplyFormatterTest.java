package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetMultiUnitInfoReplFormatter class
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetMultiUnitInfoReplyFormatterTest {

    @Test
    public void testToMonitorStringMultiUnitInfoResponse() {
        XNetMultiUnitInfoReplyFormatter formatter = new XNetMultiUnitInfoReplyFormatter();
        XNetReply r = new XNetReply("E5 14 C1 04 00 00 34");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals("Locomotive Information Response: Locomotive in Multiple Unit,Forward,in 128 Speed Step Mode,Speed Step: 64. Address is Free for Operation.F0 Off; F1 Off; F2 Off; F3 On; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off; ",
                formatter.formatMessage(r));
    }
}
