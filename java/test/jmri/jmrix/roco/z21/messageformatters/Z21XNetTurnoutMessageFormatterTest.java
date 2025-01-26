package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21XNetMessage;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Paul Bender Copyright (C) 2024
 */
public class Z21XNetTurnoutMessageFormatterTest {

    @Test
    public void testSetMessageFormatter(){
        Z21XNetTurnoutMessageFormatter formatter = new Z21XNetTurnoutMessageFormatter();
        Message message = Z21XNetMessage.getZ21SetTurnoutRequestMessage(1, true, true,true);
        assertTrue(formatter.handlesMessage(message));
        assertEquals(message.toMonitorString(), formatter.formatMessage(message));
    }

    @Test
    public void testGetMessageFormatter(){
        Z21XNetTurnoutMessageFormatter formatter = new Z21XNetTurnoutMessageFormatter();
        Message message = Z21XNetMessage.getZ21TurnoutInfoRequestMessage(1);
        assertTrue(formatter.handlesMessage(message));
        assertEquals(message.toMonitorString(), formatter.formatMessage(message));
    }

}
