package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21Reply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Paul Bender Copyright (C) 2024
 */
public class Z21ReplyFormatterTest {

    @Test
    public void testFormatter(){
        Z21ReplyFormatter formatter = new Z21ReplyFormatter();
        byte msg[] = {(byte) 0x07, (byte) 0x00, (byte) 0x40, (byte) 0x00, (byte) 0x61, (byte) 0x82, (byte) 0xE3};
         Message message = new Z21Reply(msg, 7);
        Assertions.assertTrue(formatter.handlesMessage(message));
        Assertions.assertEquals("XpressNet Tunnel Reply: 61 82 E3", message.toMonitorString(), "Monitor String");
    }
}
