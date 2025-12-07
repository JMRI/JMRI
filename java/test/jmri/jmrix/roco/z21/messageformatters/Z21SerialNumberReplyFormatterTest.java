package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Reply;

import org.junit.jupiter.api.*;

/**
 * Tests for the Z21SerialNumberReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21SerialNumberReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testMonitorStringSerialNumberReply() {

        byte msg[] = {(byte) 0x08, (byte) 0x00, (byte) 0x10, (byte) 0x00,
                (byte) 0xAE, (byte) 0xA7, (byte) 0x01, (byte) 0x00};
        Z21Reply message = new Z21Reply(msg, 8);
        Assertions.assertTrue(formatter.handlesMessage(message));
        Assertions.assertEquals("Z21 Serial Number Reply.  Serial Number: 108,462", formatter.formatMessage(message));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21SerialNumberReplyFormatter();
    }

}
