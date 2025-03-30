package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetFunctionGroup10MomentaryRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup10MomentaryRequestMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetFunctionGroup10MomentaryRequestMessageFormatter formatter = new XNetFunctionGroup10MomentaryRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup10SetMomMsg(1234, false,false,false,false,false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 10 Momentary Status for Address: 1234 F61 Continuous; F62 Continuous; F63 Continuous; F64 Continuous; F65 Continuous; F66 Continuous; F67 Continuous; F68 Continuous; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetFunctionGroup10MomentaryRequestMessageFormatter formatter = new XNetFunctionGroup10MomentaryRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup10SetMomMsg(1234, true,true,true,true,true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 10 Momentary Status for Address: 1234 F61 Momentary; F62 Momentary; F63 Momentary; F64 Momentary; F65 Momentary; F66 Momentary; F67 Momentary; F68 Momentary; ",formatter.formatMessage(msg));
    }
}
