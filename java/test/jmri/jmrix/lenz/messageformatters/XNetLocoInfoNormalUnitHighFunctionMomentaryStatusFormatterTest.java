package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetLocoInfoNOrmalUnitHighFunctionMomentaryStatusFormatter class
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLocoInfoNormalUnitHighFunctionMomentaryStatusFormatterTest {

    @Test
    public void testToMonitorStringLocoInfoNormalUnitHighFunctionMomentaryStatusMixedStatus() {
        XNetLocoInfoNormalUnitHighFunctionMomentaryStatusFormatter formatter = new XNetLocoInfoNormalUnitHighFunctionMomentaryStatusFormatter();
        XNetReply r = new XNetReply("E4 51 00 54 04 E5");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals("Locomotive F13-F28 Momentary Status: F13 Continuous; F14 Continuous; F15 Continuous; F16 Continuous; F17 Continuous; F18 Continuous; F19 Continuous; F20 Continuous; F21 Continuous; F22 Continuous; F23 Momentary; F24 Continuous; F25 Momentary; F26 Continuous; F27 Momentary; F28 Continuous; ",
                formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringLocoInfoNormalUnitHighFunctionMomentaryStatusAllContinuous() {
        XNetLocoInfoNormalUnitHighFunctionMomentaryStatusFormatter formatter = new XNetLocoInfoNormalUnitHighFunctionMomentaryStatusFormatter();
        XNetReply r = new XNetReply("E4 51 00 00 00 95");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals("Locomotive F13-F28 Momentary Status: F13 Continuous; F14 Continuous; F15 Continuous; F16 Continuous; F17 Continuous; F18 Continuous; F19 Continuous; F20 Continuous; F21 Continuous; F22 Continuous; F23 Continuous; F24 Continuous; F25 Continuous; F26 Continuous; F27 Continuous; F28 Continuous; ",
                formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringLocoInfoNormalUnitHighFunctionMomentaryStatusAllMomentary() {
        XNetLocoInfoNormalUnitHighFunctionMomentaryStatusFormatter formatter = new XNetLocoInfoNormalUnitHighFunctionMomentaryStatusFormatter();
        XNetReply r = new XNetReply("E4 51 FF FF FF 45");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals("Locomotive F13-F28 Momentary Status: F13 Momentary; F14 Momentary; F15 Momentary; F16 Momentary; F17 Momentary; F18 Momentary; F19 Momentary; F20 Momentary; F21 Momentary; F22 Momentary; F23 Momentary; F24 Momentary; F25 Momentary; F26 Momentary; F27 Momentary; F28 Momentary; ",
                formatter.formatMessage(r));
    }

}
