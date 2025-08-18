package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of XNetCommandStationRequestFormatter class
 *
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetCommandStationRequestFormatterTest {

    @Test
    void testEmergencyOffMessage() {
       XNetCommandStationRequestFormatter formatter = new XNetCommandStationRequestFormatter();
       XNetMessage msg = XNetMessage.getEmergencyOffMsg();
       Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
       Assertions.assertEquals("REQUEST: Emergency Off",formatter.formatMessage(msg));
    }

    @Test
    void testResumeNormalOperationsRequestMessage() {
        XNetCommandStationRequestFormatter formatter = new XNetCommandStationRequestFormatter();
        XNetMessage msg = XNetMessage.getResumeOperationsMsg();
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST: Normal Operations Resumed",formatter.formatMessage(msg));
    }

    @Test
    void testServiceModeResultsRequestMessage() {
        XNetCommandStationRequestFormatter formatter = new XNetCommandStationRequestFormatter();
        XNetMessage msg = XNetMessage.getServiceModeResultsMsg();
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST: Service Mode Result", formatter.formatMessage(msg));
    }

    @Test
    void testOpsModeResultsRequestMessage() {
        XNetCommandStationRequestFormatter formatter = new XNetCommandStationRequestFormatter();
        XNetMessage msg = XNetMessage.getOpsModeResultsMsg();
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST: Ops Mode Result", formatter.formatMessage(msg));
    }

    @Test
    void testCSVersionRequestMessage() {
        XNetCommandStationRequestFormatter formatter = new XNetCommandStationRequestFormatter();
        XNetMessage msg = XNetMessage.getCSVersionRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST: Command Station Version", formatter.formatMessage(msg));
    }

    @Test
    void testCSStatusRequestMessage() {
        XNetCommandStationRequestFormatter formatter = new XNetCommandStationRequestFormatter();
        XNetMessage msg = XNetMessage.getCSStatusRequestMessage();
        Assertions.assertTrue(formatter.handlesMessage(msg), "Formatter Handles Message");
        Assertions.assertEquals("REQUEST: Command Station Status", formatter.formatMessage(msg));
    }
}
