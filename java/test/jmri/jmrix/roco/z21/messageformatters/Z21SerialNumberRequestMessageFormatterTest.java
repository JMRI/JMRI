package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Message;

import org.junit.jupiter.api.*;

/**
 * Tests for the Z21SerialNumberRequestMessageFormatter.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21SerialNumberRequestMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    void testFormatter() {

        Z21Message msg = Z21Message.getSerialNumberRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Z21 Serial Number Request", formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21SerialNumberRequestMessageFormatter();
    }

}
