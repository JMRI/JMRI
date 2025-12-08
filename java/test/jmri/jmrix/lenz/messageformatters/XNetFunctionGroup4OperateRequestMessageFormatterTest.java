package jmri.jmrix.lenz.messageformatters;


import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.junit.jupiter.api.*;

/**
 * Tests of XNetFunctionGroup4OperateRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup4OperateRequestMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetMessage msg = XNetMessage.getFunctionGroup4OpsMsg(1234, false,false,false,false,false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 4 for Address: 1234 F13 Off; F14 Off; F15 Off; F16 Off; F17 Off; F18 Off; F19 Off; F20 Off; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetMessage msg = XNetMessage.getFunctionGroup4OpsMsg(1234, true,true,true,true,true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 4 for Address: 1234 F13 On; F14 On; F15 On; F16 On; F17 On; F18 On; F19 On; F20 On; ",formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp(){
        super.setUp(); // setup JUnit
        formatter = new XNetFunctionGroup4OperateRequestMessageFormatter();
    }

}
