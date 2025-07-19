package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetInterfaceRequestMessageFormatter class.
 * @author Paul Bender copyright (C) 2024
 */
public class XNetLI101RequestMessageFormatterTest {

    @Test
    public void testFormatLI101AddressRequestMessage() {
        XNetLI101RequestMessageFormatter formatter = new XNetLI101RequestMessageFormatter();
        XNetMessage msg = XNetMessage.getLIAddressRequestMsg(1);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST LI101 Address 1",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatLISpeedRequestMessage1(){
        XNetLI101RequestMessageFormatter formatter = new XNetLI101RequestMessageFormatter();
        XNetMessage msg = XNetMessage.getLISpeedRequestMsg(1);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST LI101 Baud Rate 19,200 bps (default)",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatLISpeedRequestMessage2(){
        XNetLI101RequestMessageFormatter formatter = new XNetLI101RequestMessageFormatter();
        XNetMessage msg = XNetMessage.getLISpeedRequestMsg(2);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST LI101 Baud Rate 38,400 bps",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatLISpeedRequestMessage3(){
        XNetLI101RequestMessageFormatter formatter = new XNetLI101RequestMessageFormatter();
        XNetMessage msg = XNetMessage.getLISpeedRequestMsg(3);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST LI101 Baud Rate 57,600 bps",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatLISpeedRequestMessage4(){
        XNetLI101RequestMessageFormatter formatter = new XNetLI101RequestMessageFormatter();
        XNetMessage msg = XNetMessage.getLISpeedRequestMsg(4);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST LI101 Baud Rate 115,200 bps",formatter.formatMessage(msg));
    }

    @Test
    public void testToMonitorStringLISpeedRequestMessage5(){
        XNetLI101RequestMessageFormatter formatter = new XNetLI101RequestMessageFormatter();
        XNetMessage msg = XNetMessage.getLISpeedRequestMsg(5);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST LI101 Baud Rate <undefined>",formatter.formatMessage(msg));
    }
}
