package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Reply;

import org.junit.jupiter.api.*;

/**
 * Tests for the Z21HardwareInfoReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21HardwareInfoReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testMonitorStringVersionReply() {

        byte msg[] = {(byte) 0x0C, (byte) 0x00, (byte) 0x1A, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x32, (byte) 0x01, (byte) 0x00, (byte) 0x00};
        Z21Reply message = new Z21Reply(msg, 12);
        Assertions.assertTrue(formatter.handlesMessage(message));
        Assertions.assertEquals("Z21 Version Reply.  Hardware Version: 0x200 Software Version: 1.32", formatter.formatMessage(message));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21HardwareInfoReplyFormatter();
    }

}
