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

    // A listener which sent the last frame and was then removed must not
    // receive incoming frames. Only registered listeners get them.
    @Timeout(10)
    @Test
    public void testRemovedSenderReceivesNothing() throws java.io.IOException {

        DummyCanListener monitor = new DummyCanListener((GcTrafficController)tc);
        DummyCanListener sender = new DummyCanListener((GcTrafficController)tc);
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();

        AbstractPortControllerScaffold pcs = new AbstractPortControllerScaffold(memo);
        tc.connectPort(pcs);

        // sender sends a frame, which makes it the last sender, then goes away
        CanMessage m = new CanMessage(new int[]{0x0D}, 0x7A); // QNN
        ((GcTrafficController)tc).sendCanMessage(m, sender);
        JUnitUtil.waitFor(() -> {
            return !monitor.getMessages().isEmpty();
        },"frame not sent");
        sender.dispose();

        pcs.getRedirectedToInputStream().writeBytes(":SB020N9000020002;"); // ACON N2 E2

        JUnitUtil.waitFor(() -> {
            return !monitor.getReplies().isEmpty();
        },"reply did not reach the registered listener");
        JUnitUtil.waitFor(50); // let any delivery to the removed sender happen
        Assertions.assertEquals(1, monitor.getReplies().size(), "registered listener receives the frame");
        Assertions.assertTrue(sender.getReplies().isEmpty(), "removed sender receives nothing");

        monitor.dispose();
        tc.disconnectPort(pcs);
        pcs.dispose();
    }

}
