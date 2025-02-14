package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.roco.z21.Z21Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Z21SetBroadCastFlagsRequestFormatter class.
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21SetBroadCastFlagsRequestFormatterTest {

    @Test
    public void testToString() {
        Z21SetBroadCastFlagsRequestFormatter formatter = new Z21SetBroadCastFlagsRequestFormatter();
        Z21Message msg = Z21Message.getLanSetBroadcastFlagsRequestMessage(0x01020304);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Set Z21 Broadcast flags to Railcom Messages\nSystem State Messages\nLocoNet Messages\nCAN Booster Status Messages\n", formatter.formatMessage(msg));
    }
}
