package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetCommandStationInfoResponseFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetCommandStationInfoResponseFormatterTest {

    XNetCommandStationInfoResponseFormatter formatter;
    @BeforeEach
    public void setUp() {
        formatter = new XNetCommandStationInfoResponseFormatter();
    }

    @Test
    public void testToMonitorStringBCEmergencyOff(){
        XNetReply r = new XNetReply("61 00 61");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals( Bundle.getMessage("XNetReplyBCEverythingOff"),formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringBCNormalOpers(){
        XNetReply r = new XNetReply("61 01 60");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyBCNormalOpsResumed"), formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringBCServiceModeEntry(){
        XNetReply r = new XNetReply("61 02 63");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyBCServiceEntry"), formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringServiceModeCSReady(){
        XNetReply r = new XNetReply("61 11 70");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyServiceModeCSReady"), formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringServiceModeShortCircuit(){
        XNetReply r = new XNetReply("61 12 73");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyServiceModeShort"), formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringServiceModeByteNotFound(){
        XNetReply r = new XNetReply("61 13 72");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyServiceModeDataByteNotFound"), formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringServiceModeCSBusy(){
        XNetReply r = new XNetReply("61 1F 7E");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyServiceModeCSBusy"), formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringCSNotSupported(){
        XNetReply r = new XNetReply("61 82 E3");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyCSNotSupported"), formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringDHV1_V2ErrorNotOperated(){
        XNetReply r = new XNetReply("61 83 E2");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyV1DHErrorNotOperated"), formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringDHV1_V2ErrorInUse(){
        XNetReply r = new XNetReply("61 84 E5");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyV1DHErrorInUse"), formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringDHV1_V2ErrorAlreadyDH(){
        XNetReply r = new XNetReply("61 85 E4");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyV1DHErrorAlreadyDH"), formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringDHV1_V2ErrorNonZeroSpeed(){
        XNetReply r = new XNetReply("61 86 E7");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyV1DHErrorNonZeroSpeed"), formatter.formatMessage(r));
    }

}
