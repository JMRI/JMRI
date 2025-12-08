package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Message;

import org.junit.jupiter.api.*;

/**
 * Tests for the Z21SystemStateChangedDataRequestFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21SystemStateChangedDataRequestFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatMessage() {

        Z21Message msg = Z21Message.getLanSystemStateDataChangedRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals(Bundle.getMessage("Z21MessageSystemStateChangeDataRequest"),formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21SystemStateChangedDataRequestFormatter();
    }

}
