package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21XNetReply;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.roco.z21.messageformatters.Z21XNetLocoReplyFormatter class
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21XNetLocoReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatter(){

        Message message = new Z21XNetReply("E7 00 03 00 00 00 00 00 E4");
        Assertions.assertTrue(formatter.handlesMessage(message));
        Assertions.assertEquals( "Z21 Mobile decoder info reply for address 3: Reverse,in 14 Speed Step Mode,Speed Step: 0. Address is Free for Operation. F0 Off; F1 Off; F2 Off; F3 Off; F4 Off; F5 Off; F6 Off; F7 Off; F8 Off; F9 Off; F10 Off; F11 Off; F12 Off;  F13 Off; F14 Off; F15 Off; F16 Off; F17 Off; F18 Off; F19 Off; F20 Off; F21 Off; F22 Off; F23 On; F24 Off; F25 Off; F26 On; F27 On; F28 On; ", formatter.formatMessage(message), "Monitor String");
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new Z21XNetLocoReplyFormatter();
    }

}
