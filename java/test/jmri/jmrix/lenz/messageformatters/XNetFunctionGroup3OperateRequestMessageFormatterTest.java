package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetFunctionGroup3OperateRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup3OperateRequestMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetFunctionGroup3OperateRequestMessageFormatter formatter = new XNetFunctionGroup3OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup3OpsMsg(1234, false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 3 for Address: 1234 F9 Off; F10 Off; F11 Off; F12 Off; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetFunctionGroup3OperateRequestMessageFormatter formatter = new XNetFunctionGroup3OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup3OpsMsg(1234, true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 3 for Address: 1234 F9 On; F10 On; F11 On; F12 On; ",formatter.formatMessage(msg));
    }
}
