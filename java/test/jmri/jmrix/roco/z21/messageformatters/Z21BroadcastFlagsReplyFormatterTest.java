package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.roco.z21.Z21Reply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Z21BroadcastFlagsReplyFormatter.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21BroadcastFlagsReplyFormatterTest {

    @Test
    public void testMonitorStringZ21BroadcastFlagsReply() {
        Z21BroadcastFlagsReplyFormatter formatter = new Z21BroadcastFlagsReplyFormatter();
        byte msg[] = {(byte) 0x08, (byte) 0x00, (byte) 0x51, (byte) 0x00, (byte) 0xcd, (byte) 0xab, (byte) 0x01, (byte) 0x00};
        Z21Reply reply = new Z21Reply(msg, 8);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("Z21 Broadcast flags XpressNet Messages\nRailcom Messages\n", formatter.formatMessage(reply));
    }

}
