package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Reply;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Z21 CAN Detector Reply message.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21CANDetectorReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testMonitorStringCanDetectorRailComReply() {
        byte msg[] = {(byte) 0x0E, (byte) 0x00, (byte) 0xC4, (byte) 0x00, (byte) 0xcd, (byte) 0xab, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x11, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        Z21Reply reply = new Z21Reply(msg, 14);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("Z21 CAN Detetector Reply: NetworkID=abcd Address=1 Port=1 Type=Occupancy Info Value1=1(S) direction unknown Value2=end of list", formatter.formatMessage(reply));
    }

    @Test
    public void testMonitorStringCanDetectorStatusReplyFreeWithVolt() {
        byte msg[] = {(byte) 0x0E, (byte) 0x00, (byte) 0xC4, (byte) 0x00, (byte) 0xcd, (byte) 0xab, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00};
        Z21Reply reply = new Z21Reply(msg, 14);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("Z21 CAN Detetector Reply: NetworkID=abcd Address=1 Port=1 Type=Input Status Value1=Free, with voltage Value2=", formatter.formatMessage(reply));
    }

    @Test
    public void testMonitorStringCanDetectorStatusReplyFreeWithoutVolt() {
        byte msg[] = {(byte) 0x0E, (byte) 0x00, (byte) 0xC4, (byte) 0x00, (byte) 0xcd, (byte) 0xab, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        Z21Reply reply = new Z21Reply(msg, 14);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("Z21 CAN Detetector Reply: NetworkID=abcd Address=1 Port=1 Type=Input Status Value1=Free, without voltage Value2=", formatter.formatMessage(reply));
    }

    @Test
    public void testMonitorStringCanDetectorStatusReplyBusyWithVolt() {
        byte msg[] = {(byte) 0x0E, (byte) 0x00, (byte) 0xC4, (byte) 0x00, (byte) 0xcd, (byte) 0xab, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x11, (byte) 0x00, (byte) 0x00};
        Z21Reply reply = new Z21Reply(msg, 14);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("Z21 CAN Detetector Reply: NetworkID=abcd Address=1 Port=1 Type=Input Status Value1=Busy, with voltage Value2=", formatter.formatMessage(reply));
    }

    @Test
    public void testMonitorStringCanDetectorStatusReplyBusyWithoutVolt() {
        byte msg[] = {(byte) 0x0E, (byte) 0x00, (byte) 0xC4, (byte) 0x00, (byte) 0xcd, (byte) 0xab, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x00};
        Z21Reply reply = new Z21Reply(msg, 14);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("Z21 CAN Detetector Reply: NetworkID=abcd Address=1 Port=1 Type=Input Status Value1=Busy, without voltage Value2=", formatter.formatMessage(reply));
    }

    @Test
    public void testMonitorStringCanDetectorStatusReplyBusyOverload1() {
        byte msg[] = {(byte) 0x0E, (byte) 0x00, (byte) 0xC4, (byte) 0x00, (byte) 0xcd, (byte) 0xab, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x12, (byte) 0x00, (byte) 0x00};
        Z21Reply reply = new Z21Reply(msg, 14);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("Z21 CAN Detetector Reply: NetworkID=abcd Address=1 Port=1 Type=Input Status Value1=Busy, Overload 1 Value2=", formatter.formatMessage(reply));
    }

    @Test
    public void testMonitorStringCanDetectorStatusReplyBusyOverload2() {
        byte msg[] = {(byte) 0x0E, (byte) 0x00, (byte) 0xC4, (byte) 0x00, (byte) 0xcd, (byte) 0xab, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x12, (byte) 0x00, (byte) 0x00};
        Z21Reply reply = new Z21Reply(msg, 14);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("Z21 CAN Detetector Reply: NetworkID=abcd Address=1 Port=1 Type=Input Status Value1=Busy, Overload 2 Value2=", formatter.formatMessage(reply));
    }

    @Test
    public void testMonitorStringCanDetectorStatusReplyBusyOverload3() {
        byte msg[] = {(byte) 0x0E, (byte) 0x00, (byte) 0xC4, (byte) 0x00, (byte) 0xcd, (byte) 0xab, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x03, (byte) 0x12, (byte) 0x00, (byte) 0x00};
        Z21Reply reply = new Z21Reply(msg, 14);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("Z21 CAN Detetector Reply: NetworkID=abcd Address=1 Port=1 Type=Input Status Value1=Busy, Overload 3 Value2=", formatter.formatMessage(reply));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21CANDetectorReplyFormatter();
    }

}
