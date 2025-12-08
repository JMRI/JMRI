package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.junit.jupiter.api.*;

/**
 * Tests of XNetFunctionGroup4MomentaryRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup4MomentaryRequestMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetMessage msg = XNetMessage.getFunctionGroup4SetMomMsg(1234, false,false,false,false,false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 4 Momentary Status for Address: 1234 F13 Continuous; F14 Continuous; F15 Continuous; F16 Continuous; F17 Continuous; F18 Continuous; F19 Continuous; F20 Continuous; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetMessage msg = XNetMessage.getFunctionGroup4SetMomMsg(1234, true,true,true,true,true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 4 Momentary Status for Address: 1234 F13 Momentary; F14 Momentary; F15 Momentary; F16 Momentary; F17 Momentary; F18 Momentary; F19 Momentary; F20 Momentary; ",formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp(){
        super.setUp(); // setup JUnit
        formatter = new XNetFunctionGroup4MomentaryRequestMessageFormatter();
    }

}
