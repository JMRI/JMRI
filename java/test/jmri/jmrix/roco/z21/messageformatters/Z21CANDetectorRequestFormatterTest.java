package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Message;

import org.junit.jupiter.api.*;

/**
 * Tests for the Z21CANDetectorRequestFormatter class
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21CANDetectorRequestFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatter(){

        Z21Message m = Z21Message.getLanCanDetector(0x0005);
        Assertions.assertTrue(formatter.handlesMessage(m));
        Assertions.assertEquals("Z21 CAN Detector Request for 500", formatter.formatMessage(m));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21CANDetectorRequestFormatter();
    }

}
