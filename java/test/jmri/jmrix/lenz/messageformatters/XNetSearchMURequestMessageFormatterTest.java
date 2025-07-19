package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetSearchMURequestMessageFormatter class
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetSearchMURequestMessageFormatterTest {

    @Test
    void testSearchMUForwardRequestMessage() {
        XNetSearchMURequestMessageFormatter formatter = new XNetSearchMURequestMessageFormatter();
        XNetMessage msg = XNetMessage.getDBSearchMsgNextMULoco(42,1234,true);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("Search Command Station Stack Forward for next address in Consist 42 Starting at 1234",formatter.formatMessage(msg));
    }

    @Test
    void testSearchMUBackwardRequestMessage() {
        XNetSearchMURequestMessageFormatter formatter = new XNetSearchMURequestMessageFormatter();
        XNetMessage msg = XNetMessage.getDBSearchMsgNextMULoco(42,1234,false);
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("Search Command Station Stack Backward for next address in Consist 42 Starting at 1234",formatter.formatMessage(msg));
    }
}
