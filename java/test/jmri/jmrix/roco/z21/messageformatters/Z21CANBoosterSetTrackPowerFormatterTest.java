package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Z21CANBoosterSetTrackPowerFormatter class
 * @author Paul Bender Copyright (C) 2026
 */
public class Z21CANBoosterSetTrackPowerFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatterAllOff(){
        Z21Message m = Z21Message.getLanCanSetBoosterTrackPower(0xabcd,0x00);
        Assertions.assertTrue(formatter.handlesMessage(m));
        Assertions.assertEquals("Z21 CAN Booster Set Track Power for abcd to All outputs off", formatter.formatMessage(m));
    }

    @Test
    public void testFormatterAllOn(){
        Z21Message m = Z21Message.getLanCanSetBoosterTrackPower(0xabcd,0xFF);
        Assertions.assertTrue(formatter.handlesMessage(m));
        Assertions.assertEquals("Z21 CAN Booster Set Track Power for abcd to All outputs on", formatter.formatMessage(m));
    }

    @Test
    public void testFormatterFirstOff(){
        Z21Message m = Z21Message.getLanCanSetBoosterTrackPower(0xabcd,0x10);
        Assertions.assertTrue(formatter.handlesMessage(m));
        Assertions.assertEquals("Z21 CAN Booster Set Track Power for abcd to First output off", formatter.formatMessage(m));
    }

    @Test
    public void testFormatterFirstOn(){
        Z21Message m = Z21Message.getLanCanSetBoosterTrackPower(0xabcd,0x11);
        Assertions.assertTrue(formatter.handlesMessage(m));
        Assertions.assertEquals("Z21 CAN Booster Set Track Power for abcd to First output on", formatter.formatMessage(m));
    }

    @Test
    public void testFormatterSecondOff(){
        Z21Message m = Z21Message.getLanCanSetBoosterTrackPower(0xabcd,0x20);
        Assertions.assertTrue(formatter.handlesMessage(m));
        Assertions.assertEquals("Z21 CAN Booster Set Track Power for abcd to Second output off", formatter.formatMessage(m));
    }

    @Test
    public void testFormatterSecondOn(){
        Z21Message m = Z21Message.getLanCanSetBoosterTrackPower(0xabcd,0x22);
        Assertions.assertTrue(formatter.handlesMessage(m));
        Assertions.assertEquals("Z21 CAN Booster Set Track Power for abcd to Second output on", formatter.formatMessage(m));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21CANBoosterSetTrackPowerFormatter();
    }

}
