package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetFunctionGroup4OperateRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup4OperateRequestMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetFunctionGroup4OperateRequestMessageFormatter formatter = new XNetFunctionGroup4OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup4OpsMsg(1234, false,false,false,false,false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 4 for Address: 1234 F13 Off; F14 Off; F15 Off; F16 Off; F17 Off; F18 Off; F19 Off; F20 Off; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetFunctionGroup4OperateRequestMessageFormatter formatter = new XNetFunctionGroup4OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup4OpsMsg(1234, true,true,true,true,true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 4 for Address: 1234 F13 On; F14 On; F15 On; F16 On; F17 On; F18 On; F19 On; F20 On; ",formatter.formatMessage(msg));
    }
}
