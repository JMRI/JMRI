package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetFunctionGroup7MomentaryRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup7MomentaryRequestMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetFunctionGroup7MomentaryRequestMessageFormatter formatter = new XNetFunctionGroup7MomentaryRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup7SetMomMsg(1234, false,false,false,false,false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 7 Momentary Status for Address: 1234 F37 Continuous; F38 Continuous; F39 Continuous; F40 Continuous; F41 Continuous; F42 Continuous; F43 Continuous; F44 Continuous; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetFunctionGroup7MomentaryRequestMessageFormatter formatter = new XNetFunctionGroup7MomentaryRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup7SetMomMsg(1234, true,true,true,true,true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 7 Momentary Status for Address: 1234 F37 Momentary; F38 Momentary; F39 Momentary; F40 Momentary; F41 Momentary; F42 Momentary; F43 Momentary; F44 Momentary; ",formatter.formatMessage(msg));
    }
}
