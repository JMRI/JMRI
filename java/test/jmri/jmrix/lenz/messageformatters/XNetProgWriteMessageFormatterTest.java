package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.lenz.XNetMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the XNetProgWriteMessageFormatter class
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetProgWriteMessageFormatterTest {

    @Test
    void handlesRegisterModeWriteRequest(){
        XNetProgWriteMessageFormatter formatter = new XNetProgWriteMessageFormatter();
        XNetMessage msg = XNetMessage.getWriteRegisterMsg(5,5);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Service Mode Request: Write 5 to Register 5", formatter.formatMessage(msg));
    }

    @Test
    void handlesPagedModeWriteRequest(){
        XNetProgWriteMessageFormatter formatter = new XNetProgWriteMessageFormatter();
        XNetMessage msg = XNetMessage.getWritePagedCVMsg(29,5);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Service Mode Request: Write 5 to CV 29 in Paged Mode", formatter.formatMessage(msg));
    }

    @Test
    void handlesCVModeWriteRequest(){
        XNetProgWriteMessageFormatter formatter = new XNetProgWriteMessageFormatter();
        XNetMessage msg = XNetMessage.getWriteDirectCVMsg(29,5);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Service Mode Request: Write 5 to CV 29 in Direct Mode", formatter.formatMessage(msg));
    }

    @Test
    void handlesV36CVModeWriteRequest(){
        XNetProgWriteMessageFormatter formatter = new XNetProgWriteMessageFormatter();
        XNetMessage msg = XNetMessage.getWriteDirectCVMsg(300,5);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Service Mode Request (V3.6): Write 5 to CV 300 in Direct Mode", formatter.formatMessage(msg));
        msg = XNetMessage.getWriteDirectCVMsg(600,5);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Service Mode Request (V3.6): Write 5 to CV 600 in Direct Mode", formatter.formatMessage(msg));
        msg = XNetMessage.getWriteDirectCVMsg(900,5);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Service Mode Request (V3.6): Write 5 to CV 900 in Direct Mode", formatter.formatMessage(msg));
        msg = XNetMessage.getWriteDirectCVMsg(1024,5);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Service Mode Request (V3.6): Write 5 to CV 1024 in Direct Mode", formatter.formatMessage(msg));
    }

}
