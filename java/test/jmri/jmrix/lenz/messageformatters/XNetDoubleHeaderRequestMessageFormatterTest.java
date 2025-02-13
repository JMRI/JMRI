package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for XNetDoubleHeaderRequestMessageFormatter
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetDoubleHeaderRequestMessageFormatterTest {

    @Test
    void testFormatBuildDoubleHeader(){
        XNetDoubleHeaderRequestMessageFormatter formatter = new XNetDoubleHeaderRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getBuildDoubleHeaderMsg(1234,4567);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Double Header Request: Establish Double Header with 1234 and 4567",formatter.formatMessage(msg));
    }

    @Test
    void testFormatDissolveDoubleHeader(){
        XNetDoubleHeaderRequestMessageFormatter formatter = new XNetDoubleHeaderRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getDisolveDoubleHeaderMsg(1234);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Double Header Request: Dissolve Double Header that includes mobile decoder 1234",formatter.formatMessage(msg));
    }
}
