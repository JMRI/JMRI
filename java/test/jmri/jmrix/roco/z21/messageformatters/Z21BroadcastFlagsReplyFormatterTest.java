package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Reply;

import org.junit.jupiter.api.*;

/**
 * Tests for the Z21BroadcastFlagsReplyFormatter.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21BroadcastFlagsReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testMonitorStringZ21BroadcastFlagsReply() {

        byte msg[] = {(byte) 0x08, (byte) 0x00, (byte) 0x51, (byte) 0x00, (byte) 0xcd, (byte) 0xab, (byte) 0x01, (byte) 0x00};
        Z21Reply reply = new Z21Reply(msg, 8);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("Z21 Broadcast flags XpressNet Messages\nRailcom Messages\n", formatter.formatMessage(reply));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21BroadcastFlagsReplyFormatter();
    }

}
