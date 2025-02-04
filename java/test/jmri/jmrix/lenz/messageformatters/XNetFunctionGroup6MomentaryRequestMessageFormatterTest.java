package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetFunctionGroup6MomentaryRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup6MomentaryRequestMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetFunctionGroup6MomentaryRequestMessageFormatter formatter = new XNetFunctionGroup6MomentaryRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup6SetMomMsg(1234, false,false,false,false,false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 6 Momentary Status for Address: 1234 F29 Continuous; F30 Continuous; F31 Continuous; F32 Continuous; F33 Continuous; F34 Continuous; F35 Continuous; F36 Continuous; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetFunctionGroup6MomentaryRequestMessageFormatter formatter = new XNetFunctionGroup6MomentaryRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup6SetMomMsg(1234, true,true,true,true,true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 6 Momentary Status for Address: 1234 F29 Momentary; F30 Momentary; F31 Momentary; F32 Momentary; F33 Momentary; F34 Momentary; F35 Momentary; F36 Momentary; ",formatter.formatMessage(msg));
    }
}
