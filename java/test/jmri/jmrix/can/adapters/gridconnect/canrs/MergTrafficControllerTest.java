package jmri.jmrix.can.adapters.gridconnect.canrs;

import java.io.*;

import jmri.jmrix.*;
import jmri.jmrix.can.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for MergTrafficController.
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2022
 */
public class MergTrafficControllerTest extends jmri.jmrix.can.adapters.gridconnect.GcTrafficControllerTest {

    @Override
    @Test
    public void testGetCanid(){
        Assert.assertEquals("default canid value",122,((MergTrafficController)tc).getCanid());
    }

    @Test
    public void testRtrDecodeFromHardware() {

        MergReply g = new MergReply(":SB0F0R;");
        CanReply r = ((MergTrafficController)tc).decodeFromHardware(g);

        Assertions.assertNotNull(r);
        Assertions.assertTrue(r.isRtr(),"is RtR");
        Assertions.assertFalse(r.isExtended(),"not extended");
        Assertions.assertTrue(r.getNumDataElements()==0,"0 data elements");
        Assertions.assertEquals("[587]", r.toString(),"CanReply toString ok");
    }

    @Test
    public void testRtrEncodeForHardware() {
        
        CanMessage m = new CanMessage(0,0xB0F0); // header
        m.setRtr(true);
        m.setNumDataElements(0);
        
        AbstractMRMessage g = ((MergTrafficController)tc).encodeForHardware(m);
        Assertions.assertEquals(":S1E00R;", g.toString(),"Gridconnect toString ok");
        
    }
    
    @Test
    public void testdecodeFromHardwareRtr() {
        
        MergReply g = new MergReply(":SB0F0R;");
        CanReply r = ((MergTrafficController)tc).decodeFromHardware(g);
        
        Assertions.assertNotNull(r);
        Assertions.assertTrue(r.isRtr(),"is RtR");
        Assertions.assertFalse(r.isExtended(),"not extended");
        Assertions.assertTrue(r.getNumDataElements()==0,"0 data elements");
        
        Assertions.assertEquals("[587]", r.toString(),"CanReply toString ok");
        
    }
    
    @Test
    public void testFailDecodeForHardware() {
        
        MergReply g = new MergReply("NOT A MERG REPLY");
        CanReply r = ((MergTrafficController)tc).decodeFromHardware(g);
        Assertions.assertNotNull(r);
        Assertions.assertTrue(r.getNumDataElements()==0,"0 data elements");
        Assertions.assertEquals("[0]", r.toString(),"CanReply toString zero value");
        
    }

    @Test
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes") // CanReply and CanMessage can be tested via Equals
    public void sendRtrAsCanReplyCanMessageTest() throws IOException {
        MergTrafficController ltc = (MergTrafficController)tc;
        
        DummyCanListener listener = new DummyCanListener(ltc);
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        
        AbstractPortControllerScaffold pcs = new AbstractPortControllerScaffold(memo);
        ltc.connectPort(pcs);
        
        MergReply g = new MergReply(":SB0F0R;");
        CanReply r = g.createReply();
        ltc.sendCanReply(r, null);
        JUnitUtil.waitFor(() -> {
            return !listener.getReplies().isEmpty();
        },"rtr reply did not happen");
        
        CanReply rr = listener.getReplies().get(0);
        Assertions.assertEquals(r,rr,"CanReply matches");
        
        ltc.sendCanMessage(new CanMessage(r), null);
        JUnitUtil.waitFor(() -> {
            return !listener.getMessages().isEmpty();
        },"rtr message did not happen");
        
        CanMessage mm = listener.getMessages().get(0);
        Assertions.assertEquals(r,mm,"CanMessage matches");
        
        JUnitUtil.waitFor(() -> {
            return pcs.getOutputStream().size() >= g.getNumDataElements();
        },"dos size ok, message sent.");
        
        listener.dispose();
        ltc.disconnectPort(pcs);
        memo.dispose();

    }

    @Override
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp(); 
        JUnitUtil.resetInstanceManager();
        tc = new MergTrafficController();
    }

    @Override
    @AfterEach
    public void tearDown(){
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
 
    }

}
