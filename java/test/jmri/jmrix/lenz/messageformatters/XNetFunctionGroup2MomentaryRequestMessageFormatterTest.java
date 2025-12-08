package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.junit.jupiter.api.*;

/**
 * Tests of XNetFunctionGroup2MomentaryRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup2MomentaryRequestMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetMessage msg = XNetMessage.getFunctionGroup2SetMomMsg(1234, false,false,false,false);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 2 Momentary Status for Address: 1234 F5 Continuous; F6 Continuous; F7 Continuous; F8 Continuous; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetMessage msg = XNetMessage.getFunctionGroup2SetMomMsg(1234, true,true,true,true);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 2 Momentary Status for Address: 1234 F5 Momentary; F6 Momentary; F7 Momentary; F8 Momentary; ",formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp(){
        super.setUp(); // setup JUnit
        formatter = new XNetFunctionGroup2MomentaryRequestMessageFormatter();
    }

}
