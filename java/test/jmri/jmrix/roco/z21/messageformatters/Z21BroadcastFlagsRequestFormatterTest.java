package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Message;

import org.junit.jupiter.api.*;

/**
 * Tests for the Z21BroadcastFlagsRequestFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21BroadcastFlagsRequestFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatter() {

        Z21Message msg = Z21Message.getLanGetBroadcastFlagsRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Request Z21 Broadcast flags", formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21BroadcastFlagsRequestFormatter();
    }

}
