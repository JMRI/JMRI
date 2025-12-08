package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.roco.z21.Z21Message;

import org.junit.jupiter.api.*;

/**
 * Tests for the Z21RailComDataRequestFormatter class.
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21RailComDataRequestFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void toMonitorStringRailComDataRequest() {

        Z21Message msg = Z21Message.getLanRailComGetDataRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals(Bundle.getMessage("Z21_RAILCOM_GETDATA"), formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21RailComDataRequestFormatter();
    }

}
