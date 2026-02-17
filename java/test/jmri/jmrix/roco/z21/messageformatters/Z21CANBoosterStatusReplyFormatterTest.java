package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Reply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Z21CANBoosterStatusReplyFormatter class
 * @author Paul Bender Copyright (C) 2026
 */
public class Z21CANBoosterStatusReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatterMultipleFlags(){
        byte msg[] = {(byte) 0x0E, (byte) 0x00, (byte) 0xCA, (byte) 0x00, (byte) 0xcd, (byte) 0xab,
                (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        Z21Reply reply = new Z21Reply(msg, 14);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("Z21 CAN Booster Status for abcd output 1:" +
                "\n\tactive status flags:" +
                "\n\t\tBrake generator active" +
                "\n\t\tTrack is disabled by user" +
                "\n\tvoltage 0mV\n\tcurrent 0mA", formatter.formatMessage(reply));
    }

    @Test
    public void testFormatterBrakeGeneratorFlags(){
        byte msg[] = {(byte) 0x0E, (byte) 0x00, (byte) 0xCA, (byte) 0x00, (byte) 0xcd, (byte) 0xab,
                (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        Z21Reply reply = new Z21Reply(msg, 14);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("Z21 CAN Booster Status for abcd output 1:" +
                "\n\tactive status flags:" +
                "\n\t\tBrake generator active" +
                "\n\tvoltage 0mV\n\tcurrent 0mA", formatter.formatMessage(reply));
    }

    @Test
    public void testFormatterTrackDisabledFlags(){
        byte msg[] = {(byte) 0x0E, (byte) 0x00, (byte) 0xCA, (byte) 0x00, (byte) 0xcd, (byte) 0xab,
                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        Z21Reply reply = new Z21Reply(msg, 14);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("Z21 CAN Booster Status for abcd output 1:" +
                "\n\tactive status flags:" +
                "\n\t\tTrack is disabled by user" +
                "\n\tvoltage 0mV\n\tcurrent 0mA", formatter.formatMessage(reply));
    }

    @Test
    public void testFormatterShortCircuitFlags(){
        byte msg[] = {(byte) 0x0E, (byte) 0x00, (byte) 0xCA, (byte) 0x00, (byte) 0xcd, (byte) 0xab,
                (byte) 0x01, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        Z21Reply reply = new Z21Reply(msg, 14);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("Z21 CAN Booster Status for abcd output 1:" +
                "\n\tactive status flags:" +
                "\n\t\tShort Circuit on track" +
                "\n\tvoltage 0mV\n\tcurrent 0mA", formatter.formatMessage(reply));
    }

    @Test
    public void testFormatterRailComActiveFlags(){
        byte msg[] = {(byte) 0x0E, (byte) 0x00, (byte) 0xCA, (byte) 0x00, (byte) 0xcd, (byte) 0xab,
                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x12};
        Z21Reply reply = new Z21Reply(msg, 14);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("Z21 CAN Booster Status for abcd output 1:" +
                "\n\tactive status flags:" +
                "\n\t\tRailCom cutout active" +
                "\n\tvoltage 18mV\n\tcurrent 4,608mA", formatter.formatMessage(reply));
    }

    @Test
    public void testFormatterTrackSwitchedOffFlags(){
        byte msg[] = {(byte) 0x0E, (byte) 0x00, (byte) 0xCA, (byte) 0x00, (byte) 0xcd, (byte) 0xab,
                (byte) 0x01, (byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        Z21Reply reply = new Z21Reply(msg, 14);
        Assertions.assertTrue(formatter.handlesMessage(reply));
        Assertions.assertEquals("Z21 CAN Booster Status for abcd output 1:" +
                "\n\tactive status flags:" +
                "\n\t\tTrack is switched off" +
                "\n\tvoltage 0mV\n\tcurrent 0mA", formatter.formatMessage(reply));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21CANBoosterStatusFormatter();
    }

}
