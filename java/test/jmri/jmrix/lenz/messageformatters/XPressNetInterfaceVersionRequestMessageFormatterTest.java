package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XPressNetInterfaceVersionRequestMessageFormatter class.
 * @author Paul Bender Copyright (C) 2024
 */
public class XPressNetInterfaceVersionRequestMessageFormatterTest {

    @Test
    void testHandleVersionRequest(){
        XPressnetInterfaceVersionRequestMessageFormatter formatter = new XPressnetInterfaceVersionRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getLIVersionRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("REQUEST LI10x hardware/software version",formatter.formatMessage(msg));

    }
}
