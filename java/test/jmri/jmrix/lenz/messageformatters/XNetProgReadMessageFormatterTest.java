package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.junit.jupiter.api.*;

/**
 * Tests for the XNetProgReadMessageFormatter class
 * @author Paul Bender Copyright (C) 2024
 */
public class XNetProgReadMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    void handlesRegisterModeReadRequest(){
        XNetMessage msg = XNetMessage.getReadRegisterMsg(5);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Service Mode Request: Read Register 5", formatter.formatMessage(msg));
    }

    @Test
    void handlesPagedModeReadRequest(){
        XNetMessage msg = XNetMessage.getReadPagedCVMsg(29);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Service Mode Request: Read CV 29 in Paged Mode", formatter.formatMessage(msg));
    }

    @Test
    void handlesCVModeReadRequest(){
        XNetMessage msg = XNetMessage.getReadDirectCVMsg(29);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Service Mode Request: Read CV 29 in Direct Mode", formatter.formatMessage(msg));
    }

    @Test
    void handlesV36CVModeReadRequest(){
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

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // setup JUnit
        formatter = new XNetProgReadMessageFormatter();
    }

}
