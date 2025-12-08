package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetFunctionGroup5MomentaryRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup5MomentaryRequestMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetFunctionGroup5MomentaryRequestMessageFormatter formatter = new XNetFunctionGroup5MomentaryRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup5SetMomMsg(1234, false,false,false,false,false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 5 Momentary Status for Address: 1234 F21 Continuous; F22 Continuous; F23 Continuous; F24 Continuous; F25 Continuous; F26 Continuous; F27 Continuous; F28 Continuous; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetFunctionGroup5MomentaryRequestMessageFormatter formatter = new XNetFunctionGroup5MomentaryRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup5SetMomMsg(1234, true,true,true,true,true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 5 Momentary Status for Address: 1234 F21 Momentary; F22 Momentary; F23 Momentary; F24 Momentary; F25 Momentary; F26 Momentary; F27 Momentary; F28 Momentary; ",formatter.formatMessage(msg));
    }
}
