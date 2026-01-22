package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Reply;

import org.junit.jupiter.api.*;

/**
 * Tests for the Z21XPressNetTunnelReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21XpressNetTunnelReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testMonitorStringXPressNetReply() {

        byte msg[] = {(byte) 0x07, (byte) 0x00, (byte) 0x40, (byte) 0x00, (byte) 0x61, (byte) 0x82, (byte) 0xE3};
        Z21Reply message = new Z21Reply(msg, 7);
        Assertions.assertTrue(formatter.handlesMessage(message));
        Assertions.assertEquals("XpressNet Tunnel Reply: 61 82 E3", formatter.formatMessage(message));
    }

    @Test
    public void testXPressNetThrottleReplyToMonitorString() {

        byte msg[] = {(byte) 0x0E, (byte) 0x00, (byte) 0x40, (byte) 0x00, (byte) 0xEF, (byte) 0x00, (byte) 0x03, (byte) 0x04, (byte) 0x80, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x78};
        Z21Reply message = new Z21Reply(msg, 14);
        Assertions.assertTrue(formatter.handlesMessage(message));
        Assertions.assertEquals("XpressNet Tunnel Reply: Z21 Mobile decoder info reply for address 3: Forward,in 128 Speed Step Mode,Speed Step: 0. Address is Free for Operation. F0 On; F1 Off; F2 Off; F3 Off; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off;  F13 Off; F14 Off; F15 Off; F16 Off; F17 Off; F18 Off; F19 Off; F20 Off; F21 Off; F22 Off; F23 Off; F24 Off; F25 Off; F26 Off; F27 Off; F28 Off; ", formatter.formatMessage(message));
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21XPressNetTunnelReplyFormatter();
    }

}
