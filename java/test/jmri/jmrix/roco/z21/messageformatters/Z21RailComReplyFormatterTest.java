package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Reply;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Z21RailComReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21RailComReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatReporterReply() {
        byte msg[] = {(byte) 0x11, (byte) 0x00, (byte) 0x88, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08};
        Z21Reply reply = new Z21Reply(msg, 17);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("RailCom Data Changed. Entries 1: Address 256(L) Receive Count 16,777,216 Error Count 0 Options 5 Speed 6 QOS 7\n", formatter.formatMessage(reply));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21RailComReplyFormatter();
    }

}
