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
