package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetLIReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLIReplyFormatterTest {

    XNetLIReplyFormatter formatter;

    @BeforeEach
    public void setUp() {
        formatter = new XNetLIReplyFormatter();
    }

    @Test
    public void testToMonitorStringErrorPCtoLI(){
        XNetReply r = new XNetReply("01 01 00");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyErrorPCtoLI"),formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringErrorLItoCS(){
        XNetReply r = new XNetReply("01 02 03");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assert.assertEquals("Monitor String", Bundle.getMessage("XNetReplyErrorLItoCS"),formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringErrorUnknown(){
        XNetReply r = new XNetReply("01 03 02");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assert.assertEquals("Monitor String", Bundle.getMessage("XNetReplyErrorUnknown"),formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringErrorNoTimeslot(){
        XNetReply r = new XNetReply("01 05 04");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assert.assertEquals("Monitor String", Bundle.getMessage("XNetReplyErrorNoTimeSlot"),formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringErrorBufferOverflow(){
        XNetReply r = new XNetReply("01 06 07");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assert.assertEquals("Monitor String", Bundle.getMessage("XNetReplyErrorBufferOverflow"),formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringTimeSlotRestored(){
        XNetReply r = new XNetReply("01 07 06");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assert.assertEquals("Monitor String", Bundle.getMessage("XNetReplyTimeSlotRestored"),formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringDataSentNoTimeslot(){
        XNetReply r = new XNetReply("01 08 09");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assert.assertEquals("Monitor String", Bundle.getMessage("XNetReplyRequestSentWhileNoTimeslot"),formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringErrorBadData(){
        XNetReply r = new XNetReply("01 09 08");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assert.assertEquals("Monitor String", Bundle.getMessage("XNetReplyBadDataInRequest"),formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringRetransmissionRequested(){
        XNetReply r = new XNetReply("01 0A 0B");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assert.assertEquals("Monitor String", Bundle.getMessage("XNetReplyRetransmitRequest"),formatter.formatMessage(r));
    }

}
