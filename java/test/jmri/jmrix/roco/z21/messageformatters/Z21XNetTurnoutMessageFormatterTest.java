package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21XNetMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Paul Bender Copyright (C) 2024
 */
public class Z21XNetTurnoutMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testSetMessageFormatter(){

        Message message = Z21XNetMessage.getZ21SetTurnoutRequestMessage(1, true, true,true);
        assertTrue(formatter.handlesMessage(message));
        assertEquals(message.toMonitorString(), formatter.formatMessage(message));
    }

    @Test
    public void testGetMessageFormatter(){

        Message message = Z21XNetMessage.getZ21TurnoutInfoRequestMessage(1);
        assertTrue(formatter.handlesMessage(message));
        assertEquals(message.toMonitorString(), formatter.formatMessage(message));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21XNetTurnoutMessageFormatter();
    }

}
