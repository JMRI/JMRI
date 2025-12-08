package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.junit.jupiter.api.*;

/**
 * Tests of XNetCommandStationRequestFormatter class
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetCommandStationRequestFormatterTest extends AbstractMessageFormatterTest {

    @Test
    void testEmergencyOffMessage() {
        XNetMessage msg = XNetMessage.getEmergencyOffMsg();
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST: Emergency Off",formatter.formatMessage(msg));
    }

    @Test
    void testResumeNormalOperationsRequestMessage() {
        XNetMessage msg = XNetMessage.getResumeOperationsMsg();
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST: Normal Operations Resumed",formatter.formatMessage(msg));
    }

    @Test
    void testServiceModeResultsRequestMessage() {
        XNetMessage msg = XNetMessage.getServiceModeResultsMsg();
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST: Service Mode Result", formatter.formatMessage(msg));
    }

    @Test
    void testOpsModeResultsRequestMessage() {
        XNetMessage msg = XNetMessage.getOpsModeResultsMsg();
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST: Ops Mode Result", formatter.formatMessage(msg));
    }

    @Test
    void testCSVersionRequestMessage() {
        XNetMessage msg = XNetMessage.getCSVersionRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST: Command Station Version", formatter.formatMessage(msg));
    }

    @Test
    void testCSStatusRequestMessage() {
        XNetMessage msg = XNetMessage.getCSStatusRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST: Command Station Status", formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp(){
        super.setUp(); // setup JUnit
        formatter = new XNetCommandStationRequestFormatter();
    }

}
