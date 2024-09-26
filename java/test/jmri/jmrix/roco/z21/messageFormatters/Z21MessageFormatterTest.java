package jmri.jmrix.roco.z21.messageFormatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21Message;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Z21MessageFormatterTest {

    @Test
    public void testFormatter(){
        Z21MessageFormatter formatter = new Z21MessageFormatter();
        Message message = Z21Message.getSerialNumberRequestMessage();
        assertTrue(formatter.handlesMessage(message));
        assertEquals(message.toMonitorString(), formatter.formatMessage(message));
    }
}
