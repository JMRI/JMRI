package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.junit.jupiter.api.*;

/**
 * Tests of XNetRequestMultiUnitAddLocoMessageFormatter class
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetRequestMultiUnitAddLocoMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatAddNormalDirectionMessage() {

        XNetMessage msg = XNetMessage.getAddLocoToConsistMsg(42,1234,true);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals( "Mobile Decoder Operations Request: Add Locomotive: 1234 to Multi Unit Consist: 42 with Loco Direction Normal",formatter.formatMessage(msg));
            //msg = XNetMessage.getAddLocoToConsistMsg(42,1234,false);
            //Assert.assertEquals("Monitor String","Mobile Decoder Operations Request: Add Locomotive: 1234 to Multi Unit Consist: 42 with Loco Direction Reversed",msg.toMonitorString());
    }

    @Test
    public void testFormatAddReverseDirectionMessage() {

        XNetMessage msg = XNetMessage.getAddLocoToConsistMsg(42,1234,false);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Add Locomotive: 1234 to Multi Unit Consist: 42 with Loco Direction Reversed",formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp(){
        super.setUp(); // setup JUnit
        formatter = new XNetRequestMultiUnitAddLocoMessageFormatter();
    }

}
