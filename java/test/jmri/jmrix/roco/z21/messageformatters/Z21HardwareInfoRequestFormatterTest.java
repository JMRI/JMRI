package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.roco.z21.Z21Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class for Z21HardwareInfoRequestFormatter
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21HardwareInfoRequestFormatterTest {

    @Test
    public void testFormatter(){
        Z21HardwareInfoRequestFormatter formatter = new Z21HardwareInfoRequestFormatter();
        Z21Message message = Z21Message.getLanGetHardwareInfoRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(message));
        Assertions.assertEquals("Z21 Version Request", formatter.formatMessage(message));
    }
}
