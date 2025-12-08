package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetFunctionGroup3MomentaryRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup3MomentaryRequestMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetFunctionGroup3MomentaryRequestMessageFormatter formatter = new XNetFunctionGroup3MomentaryRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup3SetMomMsg(1234, false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 3 Momentary Status for Address: 1234 F9 Continuous; F10 Continuous; F11 Continuous; F12 Continuous; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetFunctionGroup3MomentaryRequestMessageFormatter formatter = new XNetFunctionGroup3MomentaryRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup3SetMomMsg(1234, true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 3 Momentary Status for Address: 1234 F9 Momentary; F10 Momentary; F11 Momentary; F12 Momentary; ",formatter.formatMessage(msg));
    }
}
