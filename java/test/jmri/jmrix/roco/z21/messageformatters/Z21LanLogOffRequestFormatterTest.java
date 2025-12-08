package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Message;

import org.junit.jupiter.api.*;

/**
 * Tests for the Z21LanLogOffRequestFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21LanLogOffRequestFormatterTest extends AbstractMessageFormatterTest {

    @Test
    void testFormatter(){

        Z21Message msg = Z21Message.getLanLogoffRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Z21 Lan Logoff Request", formatter.formatMessage(msg));
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21LanLogOffRequestFormatter();
    }

}
