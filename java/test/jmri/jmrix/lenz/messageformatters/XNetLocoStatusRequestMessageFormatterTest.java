package jmri.jmrix.lenz.messageformatters;

import jmri.jmrix.AbstractMessageFormatterTest;
import jmri.jmrix.lenz.XNetMessage;

import org.junit.jupiter.api.*;

public class XNetLocoStatusRequestMessageFormatterTest extends AbstractMessageFormatterTest {

    @Test
    void testHandlesLocoInfoRequest(){
        XNetMessage msg = XNetMessage.getLocomotiveInfoRequestMsg(1234);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Request for Address 1234 speed/direction/function on/off status.",formatter.formatMessage(msg));
    }

    @Test
    void testHandlesLocoFunctionMomentaryStatusRequest(){
        XNetMessage msg = XNetMessage.getLocomotiveFunctionStatusMsg(1234);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Request for Address 1234 function momentary/continuous status.",formatter.formatMessage(msg));
    }

    @Test
    void testHandlesLocoFunctionHighStatusRequest(){
        XNetMessage  msg = XNetMessage.getLocomotiveFunctionHighOnStatusMsg(1234);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Request for Address 1234 F13-F28 on/off status.",formatter.formatMessage(msg));
    }

    @Test
    void testHandlesLocoFunctionHighMomentaryStatusRequest(){
        XNetMessage msg = XNetMessage.getLocomotiveFunctionHighMomStatusMsg(1234);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Request for Address 1234 F13-F28 momentary/continuous status.",formatter.formatMessage(msg));
    }

    @Test
    void testHandSearchStackForwardRequest(){
        XNetMessage msg = XNetMessage.getNextAddressOnStackMsg(1234,true);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Search Command Station Stack Forward - Start Address: 1234",formatter.formatMessage(msg));
    }

    @Test
    void testHandlesSearchStackBackwardsRequest(){
        XNetMessage msg = XNetMessage.getNextAddressOnStackMsg(1234,false);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Search Command Station Stack Backward - Start Address: 1234",formatter.formatMessage(msg));
    }

    @Test
    void testHandlesDeleteStackEntryRequest(){
        XNetMessage msg = XNetMessage.getDeleteAddressOnStackMsg(1234);
        Assertions.assertTrue(formatter.handlesMessage(msg));
        Assertions.assertEquals("Delete Address 1234 from Command Station Stack.",formatter.formatMessage(msg));
    }

    @Override
    @BeforeEach
    public void setUp(){
        super.setUp(); // setup JUnit
        formatter = new XNetLocoStatusRequestMessageFormatter();
    }

}
