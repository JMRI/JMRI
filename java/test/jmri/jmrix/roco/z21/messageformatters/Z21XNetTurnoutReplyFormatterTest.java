package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.roco.z21.Z21XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Z21XNetTurnoutReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21XNetTurnoutReplyFormatterTest {

    @Test
    void testFormatMessage() {
         Z21XNetTurnoutReplyFormatter formatter = new Z21XNetTurnoutReplyFormatter();
         Z21XNetReply reply = new Z21XNetReply("43 00 01 01 43");
         Assertions.assertTrue(formatter.handlesMessage(reply));
         Assertions.assertEquals(Bundle.getMessage("Z21LAN_X_TURNOUT_INFO", 2, "Closed"), formatter.formatMessage(reply));
    }
}
