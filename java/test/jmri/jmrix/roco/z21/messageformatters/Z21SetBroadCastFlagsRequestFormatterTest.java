package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Message;

import org.junit.jupiter.api.*;

/**
 * Tests for the Z21SetBroadCastFlagsRequestFormatter class.
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21SetBroadCastFlagsRequestFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testToString() {

        Z21Message msg = Z21Message.getLanSetBroadcastFlagsRequestMessage(0x01020304);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Set Z21 Broadcast flags to Railcom Messages\nSystem State Messages\nLocoNet Messages\nCAN Booster Status Messages\n", formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21SetBroadCastFlagsRequestFormatter();
    }

}
