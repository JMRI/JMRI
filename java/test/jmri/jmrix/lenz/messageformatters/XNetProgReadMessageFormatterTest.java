package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetProgReadMessageFormatter class
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetProgReadMessageFormatterTest {

    @Test
    void handlesRegisterModeReadRequest(){
        XNetProgReadMessageFormatter formatter = new XNetProgReadMessageFormatter();
        XNetMessage msg = XNetMessage.getReadRegisterMsg(5);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Service Mode Request: Read Register 5", formatter.formatMessage(msg));
    }

    @Test
    void handlesPagedModeReadRequest(){
        XNetProgReadMessageFormatter formatter = new XNetProgReadMessageFormatter();
        XNetMessage msg = XNetMessage.getReadPagedCVMsg(29);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Service Mode Request: Read CV 29 in Paged Mode", formatter.formatMessage(msg));
    }

    @Test
    void handlesCVModeReadRequest(){
        XNetProgReadMessageFormatter formatter = new XNetProgReadMessageFormatter();
        XNetMessage msg = XNetMessage.getReadDirectCVMsg(29);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Service Mode Request: Read CV 29 in Direct Mode", formatter.formatMessage(msg));
    }

    @Test
    void handlesV36CVModeReadRequest(){
        XNetProgReadMessageFormatter formatter = new XNetProgReadMessageFormatter();
        XNetMessage msg = XNetMessage.getReadDirectCVMsg(300);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Service Mode Request (V3.6): Read CV 300 in Direct Mode", formatter.formatMessage(msg));
        msg = XNetMessage.getReadDirectCVMsg(600);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Service Mode Request (V3.6): Read CV 600 in Direct Mode", formatter.formatMessage(msg));
        msg = XNetMessage.getReadDirectCVMsg(900);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Service Mode Request (V3.6): Read CV 900 in Direct Mode", formatter.formatMessage(msg));
        msg = XNetMessage.getReadDirectCVMsg(1024);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Service Mode Request (V3.6): Read CV 1024 in Direct Mode", formatter.formatMessage(msg));
    }
}
