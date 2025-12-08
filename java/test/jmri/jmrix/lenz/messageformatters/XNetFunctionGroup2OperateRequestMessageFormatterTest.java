package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetFunctionGroup2OperateRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup2OperateRequestMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetFunctionGroup2OperateRequestMessageFormatter formatter = new XNetFunctionGroup2OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup2OpsMsg(1234, false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 2 for Address: 1234 F5 Off; F6 Off; F7 Off; F8 Off; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetFunctionGroup2OperateRequestMessageFormatter formatter = new XNetFunctionGroup2OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup2OpsMsg(1234, true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 2 for Address: 1234 F5 On; F6 On; F7 On; F8 On; ",formatter.formatMessage(msg));
    }
}
