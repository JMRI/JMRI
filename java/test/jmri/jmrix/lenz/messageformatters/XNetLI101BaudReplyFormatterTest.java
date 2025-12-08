package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetReply;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetLI101BaudReplyFormatter class.
 * 
 * @author Paul Bender Copyright (C) 2025
 */
public class XNetLI101BaudReplyFormatterTest extends AbstractMessageFormatterTest {

    @Test
    public void testToMonitorStringLIBaud1Reply(){
        XNetReply r = new XNetReply("F2 02 01 F1");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyLIBaud","19,200 bps (default)" ),formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringLIBaud2Reply(){
        XNetReply r = new XNetReply("F2 02 02 F2");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals( Bundle.getMessage("XNetReplyLIBaud","38,400 bps" ),formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringLIBaud3Reply(){
        XNetReply r = new XNetReply("F2 02 03 F1");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals( Bundle.getMessage("XNetReplyLIBaud","57,600 bps"),formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringLIBaud4Reply(){
        XNetReply r = new XNetReply("F2 02 04 F4");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals( Bundle.getMessage("XNetReplyLIBaud","115,200 bps"),formatter.formatMessage(r));
    }

    @Test
    public void testToMonitorStringLIBaud5Reply(){
        XNetReply r = new XNetReply("F2 02 05 F1");
        Assertions.assertTrue(formatter.handlesMessage(r));
        Assertions.assertEquals(Bundle.getMessage("XNetReplyLIBaud","<undefined>"),formatter.formatMessage(r));
    }

    @Override
    @BeforeEach
    public void setUp(){
        super.setUp(); // setup JUnit
        formatter = new XNetLI101BaudReplyFormatter();
    }

}
