package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.roco.z21.Z21Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Z21BroadcastFlagsRequestFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21BroadcastFlagsRequestFormatterTest {

    @Test
    public void testFormatter() {
        Z21BroadcastFlagsRequestFormatter formatter = new Z21BroadcastFlagsRequestFormatter();
        Z21Message msg = Z21Message.getLanGetBroadcastFlagsRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Request Z21 Broadcast flags", formatter.formatMessage(msg));
    }
}
