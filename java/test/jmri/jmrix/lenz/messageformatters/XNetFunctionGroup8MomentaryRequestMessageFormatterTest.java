package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetFunctionGroup8MomentaryRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup8MomentaryRequestMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetFunctionGroup8MomentaryRequestMessageFormatter formatter = new XNetFunctionGroup8MomentaryRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup8SetMomMsg(1234, false,false,false,false,false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 8 Momentary Status for Address: 1234 F45 Continuous; F46 Continuous; F47 Continuous; F48 Continuous; F49 Continuous; F50 Continuous; F51 Continuous; F52 Continuous; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetFunctionGroup8MomentaryRequestMessageFormatter formatter = new XNetFunctionGroup8MomentaryRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup8SetMomMsg(1234, true,true,true,true,true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 8 Momentary Status for Address: 1234 F45 Momentary; F46 Momentary; F47 Momentary; F48 Momentary; F49 Momentary; F50 Momentary; F51 Momentary; F52 Momentary; ",formatter.formatMessage(msg));
    }
}
