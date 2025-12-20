package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Reply;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the Z21FastClockDataReplyFormatter.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21FastClockDataReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatterZ21FastClockDataReply() {

        byte[] msg = {(byte) 0x0C, (byte) 0x00, (byte) 0xCD, (byte) 0x00, (byte) 0x66, (byte) 0x25, (byte) 0x95, (byte) 0x36, (byte) 0x00, (byte) 0x01, (byte) 0xCF, (byte) 0x3E};
        Z21Reply reply = new Z21Reply(msg, 12);
        assertThat(formatter.handlesMessage(reply)).isTrue();
        assertThat(formatter.formatMessage(reply)).isEqualTo("Fast Clock Data:  Day 4 time 21:54:0 rate 1  \n" + "settings LocoNet Clock Enabled (Polled)\n" + "XPressNet Broadcast Clock Enabled\n" + "DCC Broadcast Enabled\n" + "Fast Clock Stop on Emergency Stop Enabled\n" + "Fast Clock Enabled\n");
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21FastClockDataReplyFormatter();
    }

}
