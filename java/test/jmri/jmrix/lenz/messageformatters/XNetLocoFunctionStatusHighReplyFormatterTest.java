package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetLocoFunctionStatusHighReplyFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLocoFunctionStatusHighReplyFormatterTest {

    @Test
    void testMixedStatus() {
        XNetLocoFunctionStatusHighReplyFormatter formatter = new XNetLocoFunctionStatusHighReplyFormatter();
        XNetReply r = new XNetReply("E3 52 54 04 E3");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals("Locomotive Information Response: Locomotive F13-F28 Status: F13 Off; F14 Off; F15 On; F16 Off; F17 On; F18 Off; F19 On; F20 Off; F21 Off; F22 Off; F23 On; F24 Off; F25 Off; F26 Off; F27 Off; F28 Off; ", formatter.formatMessage(r));
    }

    @Test
    void testAllOff() {
        XNetLocoFunctionStatusHighReplyFormatter formatter = new XNetLocoFunctionStatusHighReplyFormatter();
        XNetReply r = new XNetReply("E3 52 00 00 91");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals("Locomotive Information Response: Locomotive F13-F28 Status: F13 Off; F14 Off; F15 Off; F16 Off; F17 Off; F18 Off; F19 Off; F20 Off; F21 Off; F22 Off; F23 Off; F24 Off; F25 Off; F26 Off; F27 Off; F28 Off; ", formatter.formatMessage(r));
    }

    @Test
    void testAllOn() {
        XNetLocoFunctionStatusHighReplyFormatter formatter = new XNetLocoFunctionStatusHighReplyFormatter();
        XNetReply r = new XNetReply("E3 52 FF FF 91");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals("Locomotive Information Response: Locomotive F13-F28 Status: F13 On; F14 On; F15 On; F16 On; F17 On; F18 On; F19 On; F20 On; F21 On; F22 On; F23 On; F24 On; F25 On; F26 On; F27 On; F28 On; ", formatter.formatMessage(r));
    }

}
