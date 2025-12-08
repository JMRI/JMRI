package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetFunctionGroup5OperateRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup5OperateRequestMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetFunctionGroup5OperateRequestMessageFormatter formatter = new XNetFunctionGroup5OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup5OpsMsg(1234, false,false,false,false,false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 5 for Address: 1234 F21 Off; F22 Off; F23 Off; F24 Off; F25 Off; F26 Off; F27 Off; F28 Off; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetFunctionGroup5OperateRequestMessageFormatter formatter = new XNetFunctionGroup5OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup5OpsMsg(1234, true,true,true,true,true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 5 for Address: 1234 F21 On; F22 On; F23 On; F24 On; F25 On; F26 On; F27 On; F28 On; ",formatter.formatMessage(msg));
    }
}
