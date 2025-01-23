package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetReply;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetLocoInfoDHUnitFormatter class.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLocoInfoDHUnitFormatterTest {

    @Test
    public void testHandlesMessage() {
         XNetLocoInfoDHUnitFormatter formatter = new XNetLocoInfoDHUnitFormatter();
        XNetReply r = new XNetReply("E6 64 00 64 C1 C1 04 E2");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals("Locomotive Information Response: Locomotive in Double Header,Reverse,in 128 Speed Step Mode,Speed Step: 0. Address is Free for Operation. F0 Off; F1 Off; F2 Off; F3 On; F4 Off; F5 On; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 On; F12 On;  Second Locomotive in Double Header is: 260",formatter.formatMessage(r));
    }
}
