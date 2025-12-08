package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetRequestMultiUnitAddLocoMessageFormatter class
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetRequestMultiUnitAddLocoMessageFormatterTest {

    @Test
    public void testFormatAddNormalDirectionMessage() {
        XNetRequestMultiUnitAddLocoMessageFormatter formatter = new XNetRequestMultiUnitAddLocoMessageFormatter();
        XNetMessage msg = XNetMessage.getAddLocoToConsistMsg(42,1234,true);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals( "Mobile Decoder Operations Request: Add Locomotive: 1234 to Multi Unit Consist: 42 with Loco Direction Normal",formatter.formatMessage(msg));
            //msg = XNetMessage.getAddLocoToConsistMsg(42,1234,false);
            //Assert.assertEquals("Monitor String","Mobile Decoder Operations Request: Add Locomotive: 1234 to Multi Unit Consist: 42 with Loco Direction Reversed",msg.toMonitorString());
    }

    @Test
    public void testFormatAddReverseDirectionMessage() {
        XNetRequestMultiUnitAddLocoMessageFormatter formatter = new XNetRequestMultiUnitAddLocoMessageFormatter();
        XNetMessage msg = XNetMessage.getAddLocoToConsistMsg(42,1234,false);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Mobile Decoder Operations Request: Add Locomotive: 1234 to Multi Unit Consist: 42 with Loco Direction Reversed",formatter.formatMessage(msg));
    }

}
