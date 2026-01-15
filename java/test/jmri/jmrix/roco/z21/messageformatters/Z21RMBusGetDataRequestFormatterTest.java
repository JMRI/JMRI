package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Message;

import org.junit.jupiter.api.*;

/**
 * Tests for class Z21RMBusGetDataRequestFormatter.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21RMBusGetDataRequestFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testToMonitorStringLanRMBusGetDataRequest() {

        Z21Message msg = Z21Message.getLanRMBusGetDataRequestMessage(0);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Z21 RM Bus Data Request for group 0", formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21RMBusGetDataRequestFormatter();
    }

}
