package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetFunctionGroup10OperateRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup9OperateRequestMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetFunctionGroup9OperateRequestMessageFormatter formatter = new XNetFunctionGroup9OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup9OpsMsg(1234, false,false,false,false,false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 9 for Address: 1234 F53 Off; F54 Off; F55 Off; F56 Off; F57 Off; F58 Off; F59 Off; F60 Off; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetFunctionGroup9OperateRequestMessageFormatter formatter = new XNetFunctionGroup9OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup9OpsMsg(1234, true,true,true,true,true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 9 for Address: 1234 F53 On; F54 On; F55 On; F56 On; F57 On; F58 On; F59 On; F60 On; ",formatter.formatMessage(msg));
    }
}
