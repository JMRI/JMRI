package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.junit.jupiter.api.*;

/**
 * Tests for the XNetOpsModeRequestMessageFormatter class.
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetOpsModeRequestMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testFormatOpsModeRequestMessage() {
        XNetMessage msg = XNetMessage.getWriteOpsModeCVMsg(0,5,29,5);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("Operations Mode Programming Request: Byte Mode Write: 5 to CV 29 for Decoder Address 5", formatter.formatMessage(msg), "Monitor String");
    }

    @Test
    public void testFormatOpsModeVerifyMessage() {
        XNetMessage  msg = XNetMessage.getVerifyOpsModeCVMsg(0,5,29,5);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("Operations Mode Programming Request: Byte Mode Verify: 5 to CV 29 for Decoder Address 5",formatter.formatMessage(msg));
    }

    @Test
    public void testFormatWriteOpsBitModeCVMsg(){
        XNetMessage  msg = XNetMessage.getBitWriteOpsModeCVMsg(0,5,29,2,true);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("Operations Mode Programming Request: Bit Mode Write: 1 to CV 29 bit 2 for Decoder Address 5",formatter.formatMessage(msg));
        msg = XNetMessage.getBitWriteOpsModeCVMsg(0,5,29,2,false);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("Operations Mode Programming Request: Bit Mode Write: 0 to CV 29 bit 2 for Decoder Address 5",formatter.formatMessage(msg));
    }

    @Test
    public void testToMonitorStringVerifyOpsBitModeCVMsg(){
        XNetMessage  msg = XNetMessage.getBitVerifyOpsModeCVMsg(0,5,29,2,true);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("Operations Mode Programming Request: Bit Mode Verify: 1 to CV 29 bit 2 for Decoder Address 5",formatter.formatMessage(msg));
        msg = XNetMessage.getBitVerifyOpsModeCVMsg(0,5,29,2,false);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("Operations Mode Programming Request: Bit Mode Verify: 0 to CV 29 bit 2 for Decoder Address 5",formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new XNetOpsModeRequestMessageFormatter();
    }

}
