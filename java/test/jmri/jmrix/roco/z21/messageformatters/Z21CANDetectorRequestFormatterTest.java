package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.roco.z21.Z21Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Z21CANDetectorRequestFormatter class
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21CANDetectorRequestFormatterTest {

    @Test
    public void testFormatter(){
        Z21CANDetectorRequestFormatter formatter = new Z21CANDetectorRequestFormatter();
        Z21Message m = Z21Message.getLanCanDetector(0x0005);
        Assertions.assertTrue(formatter.handlesMessage(m));
        Assertions.assertEquals("Z21 CAN Detector Request for 500", formatter.formatMessage(m));
    }
}
