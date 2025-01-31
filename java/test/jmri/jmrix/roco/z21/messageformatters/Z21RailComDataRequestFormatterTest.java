package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.roco.z21.Z21Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Z21RailComDataRequestFormatter class.
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21RailComDataRequestFormatterTest {

    @Test
    public void toMonitorStringRailComDataRequest() {
        Z21RailComDataRequestFormatter formatter = new Z21RailComDataRequestFormatter();
        Z21Message msg = Z21Message.getLanRailComGetDataRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals(Bundle.getMessage("Z21_RAILCOM_GETDATA"), formatter.formatMessage(msg));
    }
}
