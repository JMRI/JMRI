package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.junit.jupiter.api.*;

/**
 * Tests for XNetDoubleHeaderRequestMessageFormatter
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetDoubleHeaderRequestMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    void testFormatBuildDoubleHeader(){
        XNetMessage msg = XNetMessage.getBuildDoubleHeaderMsg(1234,4567);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Double Header Request: Establish Double Header with 1234 and 4567",formatter.formatMessage(msg));
    }

    @Test
    void testFormatDissolveDoubleHeader(){
        XNetMessage msg = XNetMessage.getDisolveDoubleHeaderMsg(1234);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Double Header Request: Dissolve Double Header that includes mobile decoder 1234",formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp(){
        super.setUp(); // setup JUnit
        formatter = new XNetDoubleHeaderRequestMessageFormatter();
    }

}
