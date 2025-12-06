package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Message;

import org.junit.jupiter.api.*;

/**
 * Test class for Z21HardwareInfoRequestFormatter
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21HardwareInfoRequestFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatter(){

        Z21Message message = Z21Message.getLanGetHardwareInfoRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(message));
        Assertions.assertEquals("Z21 Version Request", formatter.formatMessage(message));
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21HardwareInfoRequestFormatter();
    }

}
