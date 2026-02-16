package jmri.jmrix.lenz.messageformatters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetReply;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetLIReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLIReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testToMonitorStringErrorPCtoLI(){
        XNetReply r = new XNetReply("01 01 00");
        assertTrue(formatter.handlesMessage(r));
        assertEquals(Bundle.getMessage("XNetReplyErrorPCtoLI"),formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringErrorLItoCS(){
        XNetReply r = new XNetReply("01 02 03");
        assertTrue(formatter.handlesMessage(r));
        assertEquals( Bundle.getMessage("XNetReplyErrorLItoCS"),formatter.formatMessage(r), "Monitor String");
    }

    @Test
    public void testToMonitorStringErrorUnknown(){
        XNetReply r = new XNetReply("01 03 02");
        assertTrue(formatter.handlesMessage(r));
        assertEquals( Bundle.getMessage("XNetReplyErrorUnknown"),formatter.formatMessage(r), "Monitor String");
    }

    @Test
    public void testToMonitorStringErrorNoTimeslot(){
        XNetReply r = new XNetReply("01 05 04");
        assertTrue(formatter.handlesMessage(r));
        assertEquals( Bundle.getMessage("XNetReplyErrorNoTimeSlot"),formatter.formatMessage(r), "Monitor String");
    }

    @Test
    public void testToMonitorStringErrorBufferOverflow(){
        XNetReply r = new XNetReply("01 06 07");
        assertTrue(formatter.handlesMessage(r));
        assertEquals( Bundle.getMessage("XNetReplyErrorBufferOverflow"),formatter.formatMessage(r), "Monitor String");
    }

    @Test
    public void testToMonitorStringTimeSlotRestored(){
        XNetReply r = new XNetReply("01 07 06");
        assertTrue(formatter.handlesMessage(r));
        assertEquals( Bundle.getMessage("XNetReplyTimeSlotRestored"),formatter.formatMessage(r), "Monitor String");
    }

    @Test
    public void testToMonitorStringDataSentNoTimeslot(){
        XNetReply r = new XNetReply("01 08 09");
        assertTrue(formatter.handlesMessage(r));
        assertEquals( Bundle.getMessage("XNetReplyRequestSentWhileNoTimeslot"),formatter.formatMessage(r), "Monitor String");
    }

    @Test
    public void testToMonitorStringErrorBadData(){
        XNetReply r = new XNetReply("01 09 08");
        assertTrue(formatter.handlesMessage(r));
        assertEquals( Bundle.getMessage("XNetReplyBadDataInRequest"),formatter.formatMessage(r), "Monitor String");
    }

    @Test
    public void testToMonitorStringRetransmissionRequested(){
        XNetReply r = new XNetReply("01 0A 0B");
        assertTrue(formatter.handlesMessage(r));
        assertEquals( Bundle.getMessage("XNetReplyRetransmitRequest"),formatter.formatMessage(r), "Monitor String");
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new XNetLIReplyFormatter();
    }

}
