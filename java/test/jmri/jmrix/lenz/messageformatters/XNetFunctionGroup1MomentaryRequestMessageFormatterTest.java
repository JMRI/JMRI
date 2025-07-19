package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetFunctionGroup1MomentaryRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup1MomentaryRequestMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetFunctionGroup1MomentaryRequestMessageFormatter formatter = new XNetFunctionGroup1MomentaryRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup1SetMomMsg(1234, false,false,false,false,false);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 1 Momentary Status for Address: 1234 F0 Continuous; F1 Continuous; F2 Continuous; F3 Continuous; F4 Continuous; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetFunctionGroup1MomentaryRequestMessageFormatter formatter = new XNetFunctionGroup1MomentaryRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup1SetMomMsg(1234, true,true,true,true,true);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 1 Momentary Status for Address: 1234 F0 Momentary; F1 Momentary; F2 Momentary; F3 Momentary; F4 Momentary; ",formatter.formatMessage(msg));
    }
}
