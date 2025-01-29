package jmri.jmrix.lenz.hornbyelite.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the HornbyEliteMultiUnitInfoReplFormatter class
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class HornbyEliteMultiUnitInfoReplyFormatterTest {

    @Test
    public void testToMonitorStringMultiUnitInfoResponse() {
        HornbyEliteMultiUnitInfoReplyFormatter formatter = new HornbyEliteMultiUnitInfoReplyFormatter();
        XNetReply r = new XNetReply("E5 F8 C1 04 00 00 34");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals("Elite Speed/Direction Information: Locomotive 260,Reverse,in 14 Speed Step Mode,Speed Step: 0. Address is Free for Operation. ",
                formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringMultiUnitFnInfoResponse() {
        HornbyEliteMultiUnitInfoReplyFormatter formatter = new HornbyEliteMultiUnitInfoReplyFormatter();
        XNetReply r = new XNetReply("E5 F9 C1 04 00 00 34");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals("Elite Function Information: Locomotive 260 F0 Off; F1 Off; F2 Off; F3 Off; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off; ",
                formatter.formatMessage(r));
    }
}
