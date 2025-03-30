package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.roco.z21.Z21Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for Z21RMBusProgramModuleRequestFormatter.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21RMBusProgramModuleRequestFormatterTest {

    @Test
    public void testToMonitorStringLanRMBusProgramModule() {
        Z21RMBusProgramModuleRequestFormatter formatter = new Z21RMBusProgramModuleRequestFormatter();
        Z21Message msg = Z21Message.getLanRMBusProgramModuleMessage(0);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Z21 RM Bus Program Module to Address 0", formatter.formatMessage(msg));
    }
}
