package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetFunctionGroup7OperateRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup7OperateRequestMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetFunctionGroup7OperateRequestMessageFormatter formatter = new XNetFunctionGroup7OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup7OpsMsg(1234, false,false,false,false,false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 7 for Address: 1234 F37 Off; F38 Off; F39 Off; F40 Off; F41 Off; F42 Off; F43 Off; F44 Off; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetFunctionGroup7OperateRequestMessageFormatter formatter = new XNetFunctionGroup7OperateRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getFunctionGroup7OpsMsg(1234, true,true,true,true,true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 7 for Address: 1234 F37 On; F38 On; F39 On; F40 On; F41 On; F42 On; F43 On; F44 On; ",formatter.formatMessage(msg));
    }
}
