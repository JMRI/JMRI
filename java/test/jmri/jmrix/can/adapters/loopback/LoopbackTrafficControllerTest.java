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
