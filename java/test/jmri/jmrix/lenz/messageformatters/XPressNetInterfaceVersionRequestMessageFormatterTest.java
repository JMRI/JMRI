package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.junit.jupiter.api.*;

/**
 * Tests for the XPressNetInterfaceVersionRequestMessageFormatter class.
 * @author Paul Bender Copyright (C) 2024
 */
public class XPressNetInterfaceVersionRequestMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    void testHandleVersionRequest(){
        formatter = new XPressnetInterfaceVersionRequestMessageFormatter();
        XNetMessage msg = XNetMessage.getLIVersionRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("REQUEST LI10x hardware/software version",formatter.formatMessage(msg));

    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new XPressnetInterfaceVersionRequestMessageFormatter();
    }

}
