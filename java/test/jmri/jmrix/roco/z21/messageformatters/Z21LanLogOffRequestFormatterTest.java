package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.roco.z21.Z21Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Z21LanLogOffRequestFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21LanLogOffRequestFormatterTest {

    @Test
    void testFormatter(){
        Z21LanLogOffRequestFormatter formatter = new Z21LanLogOffRequestFormatter();
        Z21Message msg = Z21Message.getLanLogoffRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Z21 Lan Logoff Request", formatter.formatMessage(msg));
    }
}
