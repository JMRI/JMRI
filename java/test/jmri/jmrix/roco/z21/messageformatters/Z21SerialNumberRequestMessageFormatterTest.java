package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.roco.z21.Z21Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Z21SerialNumberRequestMessageFormatter.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21SerialNumberRequestMessageFormatterTest {

    @Test
    void testFormatter() {
        Z21SerialNumberRequestMessageFormatter formatter = new Z21SerialNumberRequestMessageFormatter();
        Z21Message msg = Z21Message.getSerialNumberRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Z21 Serial Number Request", formatter.formatMessage(msg));
    }
}
