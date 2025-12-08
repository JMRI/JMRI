package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetFunctionGroup9MomentaryRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup9MomentaryRequestMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetFunctionGroup9MomentaryRequestMessageFormatter formatter = new XNetFunctionGroup9MomentaryRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup9SetMomMsg(1234, false,false,false,false,false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 9 Momentary Status for Address: 1234 F53 Continuous; F54 Continuous; F55 Continuous; F56 Continuous; F57 Continuous; F58 Continuous; F59 Continuous; F60 Continuous; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetFunctionGroup9MomentaryRequestMessageFormatter formatter = new XNetFunctionGroup9MomentaryRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup9SetMomMsg(1234, true,true,true,true,true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 9 Momentary Status for Address: 1234 F53 Momentary; F54 Momentary; F55 Momentary; F56 Momentary; F57 Momentary; F58 Momentary; F59 Momentary; F60 Momentary; ",formatter.formatMessage(msg));
    }
}
