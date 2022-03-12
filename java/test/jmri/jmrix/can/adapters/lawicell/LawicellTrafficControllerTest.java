package jmri.jmrix.can.adapters.lawicell;

import java.io.IOException;

import jmri.jmrix.AbstractPortControllerScaffold;
import jmri.jmrix.can.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for LawicellTrafficController.
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2022
 */
@Timeout(10)
public class LawicellTrafficControllerTest extends jmri.jmrix.can.TrafficControllerTest {
   
    @Test
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes") // CanReply and CanMessage can be tested via Equals
    public void sendRtrAsCanReplyCanMessageTest() throws IOException {
        LawicellTrafficController ltc = (LawicellTrafficController)tc;

        DummyCanListener listener = new DummyCanListener(ltc);
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();

        AbstractPortControllerScaffold pcs = new AbstractPortControllerScaffold(memo);
        ltc.connectPort(pcs);

        Reply g = new Reply("r1230\r");
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
        pcs.dispose();
    }

    @Test
    public void testIncomingRtr() throws IOException {

        DummyCanListener listener = new DummyCanListener((LawicellTrafficController)tc);
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();

        AbstractPortControllerScaffold pcs = new AbstractPortControllerScaffold(memo);
        tc.connectPort(pcs);
        pcs.getRedirectedToInputStream().writeBytes("r1230\r"); // RTR CanFrame

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
        jmri.util.JUnitUtil.setUp(); 
        JUnitUtil.resetInstanceManager();
        tc = new LawicellTrafficController();
    }

    @Override
    @AfterEach
    public void tearDown(){
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();
 
    }

}
