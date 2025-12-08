package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetBroadcastEmergencyStopFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetBroadcastEmergencyStopFormatterTest {

    @Test
    public void testEmergencyStopFormatter(){
        XNetBroadcastEmergencyStopFormatter formatter = new XNetBroadcastEmergencyStopFormatter();
        XNetReply r = new XNetReply("81 00 81");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyBCEverythingStop"), formatter.formatMessage(r));
    }
}
