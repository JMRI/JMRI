package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetReply;

import org.junit.jupiter.api.*;

/**
 * Tests for the XNetCSStatusReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetCSStatusReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    void testHandlesSubsetStatusMessage() {
        XNetReply r = new XNetReply("62 22 00 40");
        Assertions.assertTrue(formatter.handlesMessage(r));
        String targetString = Bundle.getMessage("XNetReplyCSStatus") + " ";
        targetString += Bundle.getMessage("XNetCSStatusPowerModeManual") + "; ";
        Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

    @Test
    void testHandlesCompleteStatusMessage() {
        XNetReply r = new XNetReply("62 22 FF BF");
        Assertions.assertTrue(formatter.handlesMessage(r));
        String targetString = Bundle.getMessage("XNetReplyCSStatus") + " ";
        targetString += Bundle.getMessage("XNetCSStatusEmergencyOff") + "; ";
        targetString += Bundle.getMessage("XNetCSStatusEmergencyStop") + "; ";
        targetString += Bundle.getMessage("XNetCSStatusServiceMode") + "; ";
        targetString += Bundle.getMessage("XNetCSStatusPoweringUp") + "; ";
        targetString += Bundle.getMessage("XNetCSStatusPowerModeAuto") + "; ";
        targetString += Bundle.getMessage("XNetCSStatusRamCheck");
        Assertions.assertEquals(targetString, formatter.formatMessage(r));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new XNetCSStatusReplyFormatter();
    }

}
