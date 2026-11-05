package jmri.jmrix.can.adapters.loopback;

import jmri.jmrix.can.*;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for LoopbackTrafficController.
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2022
 */
public class LoopbackTrafficControllerTest extends jmri.jmrix.can.TrafficControllerTest {

    @Test
    public void sendRtrAsCanReplyCanMessageTest() {
        LoopbackTrafficController ltc = (LoopbackTrafficController)tc;
        DummyCanListener listener = new DummyCanListener(ltc);
        
        CanReply r = new CanReply();
        r.setRtr(true);
        r.setNumDataElements(0);
        r.setHeader(0xB0F0);
        
        ltc.sendCanReply(r, null);
        JUnitUtil.waitFor(() -> {
            return !listener.getReplies().isEmpty();
        },"rtr reply did not happen");
        
        ltc.sendCanMessage(new CanMessage(r), null);
        JUnitUtil.waitFor(() -> {
            return !listener.getMessages().isEmpty();
        },"rtr message did not happen");
        
        listener.dispose();
    }
    
    @Test
    public void testInterfaceMethods() {
        LoopbackTrafficController ltc = (LoopbackTrafficController)tc;
        
        Assertions.assertTrue(ltc.endOfMessage(null),"always end of message");
        JUnitAppender.assertErrorMessage("endOfMessage unexpected");
        
        Assertions.assertNotNull(ltc.newReply(),"new reply created");
        Assertions.assertNotNull(ltc.newMessage(),"new message created");
        
        Assertions.assertEquals(8, ltc.lengthOfByteStream(new CanMessage(4)),"byte tsream length");
        
        Assertions.assertFalse(ltc.isBootMode(),"always false");
        
        Assertions.assertNull(ltc.encodeForHardware(null),"does not encode");
        JUnitAppender.assertErrorMessage("encodeForHardware unexpected");
        
        Assertions.assertNull(ltc.decodeFromHardware(null),"does not decode");
        JUnitAppender.assertErrorMessage("decodeFromHardware unexpected");
        
        ltc.addTrailerToOutput(null, 0, null);

    }

    // With nothing to write to, a frame is sent when it is put on the
    // simulated bus: stamped with a time from before its listeners are told,
    // and its sent listener is told after them, on the layout thread.
    @Test
    public void testSentFrameStampedWhenPutOnBus() {
        LoopbackTrafficController ltc = (LoopbackTrafficController)tc;
        java.util.List<String> order = new java.util.concurrent.CopyOnWriteArrayList<>();
        java.util.concurrent.atomic.AtomicLong heard = new java.util.concurrent.atomic.AtomicLong();
        java.util.concurrent.atomic.AtomicLong toldTime = new java.util.concurrent.atomic.AtomicLong();
        java.util.concurrent.atomic.AtomicBoolean onLayoutThread = new java.util.concurrent.atomic.AtomicBoolean(false);
        CanListener monitor = new CanListener() {
            @Override
            public void message(CanMessage m) {
                heard.set(System.nanoTime());
                order.add("message");
            }

            @Override
            public void reply(CanReply r) {
            }
        };
        ltc.addCanListener(monitor);

        CanMessage m = new CanMessage(new int[]{0x0D}, 0x7A); // QNN
        m.setSentListener((message, timeNanos) -> {
            onLayoutThread.set(jmri.util.ThreadingUtil.isLayoutThread());
            toldTime.set(timeNanos);
            order.add("sent");
        });
        ltc.sendCanMessage(m, null);

        JUnitUtil.waitFor(() -> order.contains("sent"), "sent listener told");
        Assertions.assertEquals(java.util.List.of("message", "sent"), order, "listeners first, then the sent listener");
        Assertions.assertTrue(m.hasSentTime(), "stamped");
        Assertions.assertTrue(m.getSentTime() <= heard.get(), "time taken before the listeners were told");
        Assertions.assertEquals(m.getSentTime(), toldTime.get(), "listener told the same time");
        Assertions.assertTrue(onLayoutThread.get(), "listener told on the layout thread");

        ltc.removeCanListener(monitor);
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp(); 
        JUnitUtil.resetInstanceManager();
        tc = new LoopbackTrafficController();
    }

    @Override
    @AfterEach
    public void tearDown(){
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
    }

}
