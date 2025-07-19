package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetFunctionGroup1OperateRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup1OperateRequestMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetFunctionGroup1OperateRequestMessageFormatter formatter = new XNetFunctionGroup1OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup1OpsMsg(1234, false,false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 1 for Address: 1234 F0 Off; F1 Off; F2 Off; F3 Off; F4 Off; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetFunctionGroup1OperateRequestMessageFormatter formatter = new XNetFunctionGroup1OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup1OpsMsg(1234, true,true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 1 for Address: 1234 F0 On; F1 On; F2 On; F3 On; F4 On; ",formatter.formatMessage(msg));
    }
}
