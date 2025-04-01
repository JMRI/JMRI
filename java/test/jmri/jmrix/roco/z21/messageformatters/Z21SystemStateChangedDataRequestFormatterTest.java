package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.roco.z21.Z21Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Z21SystemStateChangedDataRequestFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21SystemStateChangedDataRequestFormatterTest {

    @Test
    public void testFormatMessage() {
        Z21SystemStateChangedDataRequestFormatter formatter = new Z21SystemStateChangedDataRequestFormatter();
        Z21Message msg = Z21Message.getLanSystemStateDataChangedRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals(Bundle.getMessage("Z21MessageSystemStateChangeDataRequest"),formatter.formatMessage(msg));
    }
}
