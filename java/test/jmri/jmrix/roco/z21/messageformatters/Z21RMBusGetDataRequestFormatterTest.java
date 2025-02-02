package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.roco.z21.Z21Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for class Z21RMBusGetDataRequestFormatter.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21RMBusGetDataRequestFormatterTest {

    @Test
    public void testToMonitorStringLanRMBusGetDataRequest() {
        Z21RMBusGetDataRequestFormatter formatter = new Z21RMBusGetDataRequestFormatter();
        Z21Message msg = Z21Message.getLanRMBusGetDataRequestMessage(0);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Z21 RM Bus Data Request for group 0", formatter.formatMessage(msg));
    }

}
