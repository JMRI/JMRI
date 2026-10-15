package jmri.jmrix.can.adapters.gridconnect;

import jmri.jmrix.AbstractPortControllerScaffold;
import jmri.jmrix.can.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for GcTrafficController.
 * @author Paul Bender Copyright (C) 2016
 */
public class GcTrafficControllerTest extends jmri.jmrix.can.TrafficControllerTest {

    @Timeout(10)
    @Test
    public void testIncomingRtr() throws java.io.IOException {
        
        DummyCanListener listener = new DummyCanListener((GcTrafficController)tc);
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        
        AbstractPortControllerScaffold pcs = new AbstractPortControllerScaffold(memo);
        tc.connectPort(pcs);
        pcs.getRedirectedToInputStream().writeBytes(":SB0F0R;"); // RTR CanFrame SB0F0R
        
        JUnitUtil.waitFor(() -> {
            return !listener.getReplies().isEmpty();
        },"rtr reply did not happen");
        
        CanReply reply = listener.getReplies().get(0);
        Assertions.assertNotNull(reply, "CanReply generated and not null");
        Assertions.assertEquals(0,reply.getNumDataElements(),"no data elements");
        Assertions.assertTrue(reply.isRtr(),"rtr flag set");
        
        listener.dispose();
        tc.disconnectPort(pcs);
        pcs.dispose();
        
    }

    // A frame whose ';' is lost must not be merged with the following frame,
    // the partial frame is discarded and the following frame received intact.
    @Timeout(10)
    @Test
    public void testResyncOnLostTerminator() throws java.io.IOException {

        DummyCanListener listener = new DummyCanListener((GcTrafficController)tc);
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();

        AbstractPortControllerScaffold pcs = new AbstractPortControllerScaffold(memo);
        tc.connectPort(pcs);
        // first frame truncated, no ';', then a complete ACON N2 E2
        pcs.getRedirectedToInputStream().writeBytes(":SB020N91000100:SB020N9000020002;");

        JUnitUtil.waitFor(() -> {
            return !listener.getReplies().isEmpty();
        },"reply did not happen");

        CanReply reply = listener.getReplies().get(0);
        Assertions.assertEquals(5,reply.getNumDataElements(),"5 data elements from the complete frame");
        Assertions.assertEquals(0x90,reply.getElement(0),"ACON");
        Assertions.assertEquals(0x00,reply.getElement(1),"NN hi");
        Assertions.assertEquals(0x02,reply.getElement(2),"NN lo");
        Assertions.assertEquals(0x00,reply.getElement(3),"EN hi");
        Assertions.assertEquals(0x02,reply.getElement(4),"EN lo");
        Assertions.assertEquals(1,listener.getReplies().size(),"only the complete frame passed on");
        jmri.util.JUnitAppender.assertWarnMessageStartsWith("Incomplete frame discarded");

        listener.dispose();
        tc.disconnectPort(pcs);
        pcs.dispose();
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp(); 
        JUnitUtil.resetInstanceManager();
        tc = new GcTrafficController();
    }

    @Override
    @AfterEach
    public void tearDown(){
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
 
    }

}
