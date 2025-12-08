package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetThrottleTakenOverReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetThrottleTakenOverReplyFormatterTest {

    @Test
    public void testFormatTakenOverShortAddressReply() {
        XNetThrottleTakenOverReplyFormatter formatter = new XNetThrottleTakenOverReplyFormatter();
        XNetReply r = new XNetReply("E3 40 00 04 57");
        Assertions.assertTrue(formatter.handlesMessage(r));
        String targetString = Bundle.getMessage("XNetReplyLocoLabel") + " ";
        targetString += Bundle.getMessage("rsType") + " ";
        targetString += 4 + " ";
        targetString += Bundle.getMessage("XNetReplyLocoOperated");
        Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

    @Test
    public void testFormatTakenOverLongAddressReply() {
        XNetThrottleTakenOverReplyFormatter formatter = new XNetThrottleTakenOverReplyFormatter();
        XNetReply r = new XNetReply("E3 40 C1 04 61");
        Assertions.assertTrue(formatter.handlesMessage(r));
        String targetString =
                Bundle.getMessage("XNetReplyLocoLabel") + " ";
        targetString += Bundle.getMessage("rsType") + " ";
        targetString += 260 + " ";
        targetString += Bundle.getMessage("XNetReplyLocoOperated");
        Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

}
