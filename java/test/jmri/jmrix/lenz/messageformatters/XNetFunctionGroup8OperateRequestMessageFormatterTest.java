package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetFunctionGroup8OperateRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup8OperateRequestMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetFunctionGroup8OperateRequestMessageFormatter formatter = new XNetFunctionGroup8OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup8OpsMsg(1234, false,false,false,false,false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 8 for Address: 1234 F45 Off; F46 Off; F47 Off; F48 Off; F49 Off; F50 Off; F51 Off; F52 Off; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetFunctionGroup8OperateRequestMessageFormatter formatter = new XNetFunctionGroup8OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup8OpsMsg(1234, true,true,true,true,true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 8 for Address: 1234 F45 On; F46 On; F47 On; F48 On; F49 On; F50 On; F51 On; F52 On; ",formatter.formatMessage(msg));
    }
}
