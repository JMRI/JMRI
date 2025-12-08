package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.junit.jupiter.api.*;

/**
 * Tests of XNetFunctionGroup10OperateRequestMessageFormatter class
 *
 * @Paul Bender Copyright (C) 2024
 */
public class XNetFunctionGroup10OperateRequestMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatMessageAllOff() {
        XNetMessage msg = XNetMessage.getFunctionGroup10OpsMsg(1234, false,false,false,false,false,false,false,false);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 10 for Address: 1234 F61 Off; F62 Off; F63 Off; F64 Off; F65 Off; F66 Off; F67 Off; F68 Off; ",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatMessageAllOn() {
        XNetMessage msg = XNetMessage.getFunctionGroup10OpsMsg(1234, true,true,true,true,true,true,true,true);

        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Set Function Group 10 for Address: 1234 F61 On; F62 On; F63 On; F64 On; F65 On; F66 On; F67 On; F68 On; ",formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp(){
        super.setUp(); // setup JUnit
        formatter = new XNetFunctionGroup10OperateRequestMessageFormatter();
    }

}
