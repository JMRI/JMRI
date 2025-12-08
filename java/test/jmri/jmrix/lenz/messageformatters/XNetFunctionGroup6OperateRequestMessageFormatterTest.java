package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetFunctionGroup6OperateRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup6OperateRequestMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetFunctionGroup6OperateRequestMessageFormatter formatter = new XNetFunctionGroup6OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup6OpsMsg(1234, false,false,false,false,false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 6 for Address: 1234 F29 Off; F30 Off; F31 Off; F32 Off; F33 Off; F34 Off; F35 Off; F36 Off; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetFunctionGroup6OperateRequestMessageFormatter formatter = new XNetFunctionGroup6OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup6OpsMsg(1234, true,true,true,true,true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 6 for Address: 1234 F29 On; F30 On; F31 On; F32 On; F33 On; F34 On; F35 On; F36 On; ",formatter.formatMessage(msg));
    }
}
