package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetLocoFunctionMomentaryStatusReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLocoFunctionMomentaryStatusReplyFormatterTest {

    @Test
    public void testFormattMixed(){
        XNetLocoFunctionMomentaryStatusReplyFormatter formatter = new XNetLocoFunctionMomentaryStatusReplyFormatter();
        XNetReply r= new XNetReply("E3 50 54 04 E3");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals("Locomotive Information Response: Locomotive Function Status: F0 Momentary; F1 Continuous; F2 Continuous; F3 Momentary; F4 Continuous; F5 Continuous; F6 Continuous; F7 Momentary; F8 Continuous; F9 Continuous; F10 Continuous; F11 Continuous; F12 Continuous; ",formatter.formatMessage(r));
    }

    @Test
    public void testFormattAllContinous() {
        XNetLocoFunctionMomentaryStatusReplyFormatter formatter = new XNetLocoFunctionMomentaryStatusReplyFormatter();
        XNetReply r = new XNetReply("E3 50 00 00 93");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals("Locomotive Information Response: Locomotive Function Status: F0 Continuous; F1 Continuous; F2 Continuous; F3 Continuous; F4 Continuous; F5 Continuous; F6 Continuous; F7 Continuous; F8 Continuous; F9 Continuous; F10 Continuous; F11 Continuous; F12 Continuous; ",formatter.formatMessage(r));
    }

    @Test
    public void testFormattAllMomentary() {
        XNetLocoFunctionMomentaryStatusReplyFormatter formatter = new XNetLocoFunctionMomentaryStatusReplyFormatter();
        XNetReply r= new XNetReply("E3 50 5F FF 13");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals("Locomotive Information Response: Locomotive Function Status: F0 Momentary; F1 Momentary; F2 Momentary; F3 Momentary; F4 Momentary; F5 Momentary; F6 Momentary; F7 Momentary; F8 Momentary; F9 Momentary; F10 Momentary; F11 Momentary; F12 Momentary; ",formatter.formatMessage(r));
    }
}
