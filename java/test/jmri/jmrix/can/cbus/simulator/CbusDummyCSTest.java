package jmri.jmrix.can.cbus.simulator;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (c) 2019
 */
public class CbusDummyCSTest {

    

    @Test
    public void testCTor() {
        CbusDummyCS t = new CbusDummyCS(null);
        Assert.assertNotNull("exists",t);
        t.dispose();
    }

    @Test
    public void testCTorTC() {
        
        Assert.assertTrue("0 listeners",tc.numListeners()==0);
        CbusDummyCS t = new CbusDummyCS(memo);
        Assert.assertNotNull("exists",t);
        Assert.assertTrue("1 listener",tc.numListeners()==1);
        
        Assert.assertTrue("start 0 sessions",t.getNumberSessions()==0);
        Assert.assertTrue("start getDummyType",t.getDummyType()==1);
        
        t.setDelay(7);
        Assert.assertEquals("getSetDelay", 7,t.getDelay());
        
        t.dispose();
        Assert.assertTrue("0 listeners after dispose",tc.numListeners()==0);


    }


    @Test
    public void testProcessInOutTrackOnOff() {
        
        CbusDummyCS t = new CbusDummyCS(memo);

        Assert.assertEquals("start getProcessIn", false,t.getProcessIn());
        Assert.assertEquals("start getProcessOut", true,t.getProcessOut());
        Assert.assertEquals("start getSendIn", true,t.getSendIn());
        Assert.assertEquals("start getSendOut", false,t.getSendOut());        
        
        
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_RTON },0x12 );
        CanReply r   = new CanReply(   new int[]{CbusConstants.CBUS_RTON },0x12 );
        t.reply(r);
        t.message(m);
        
        JUnitUtil.waitFor(()->{ return(tc.inbound.size()>0); }, "reply didn't arrive");
        Assert.assertEquals("rton acknowledged out to in", "[5f8] 05",
            tc.inbound.elementAt(tc.inbound.size() - 1).toString());
        Assert.assertEquals("rton not acknowledged in to out", 0,(tc.outbound.size()));
        
        t.setProcessIn(true);
        t.setProcessOut(false);
        t.setSendIn(false);
        t.setSendOut(true);
        
        Assert.assertEquals("getProcessIn", true,t.getProcessIn());
        Assert.assertEquals("getProcessOut", false,t.getProcessOut());
        Assert.assertEquals("getSendIn", false,t.getSendIn());
        Assert.assertEquals("getSendOut", true,t.getSendOut());
        
        m = new CanMessage( new int[]{CbusConstants.CBUS_RTOF },0x12 );
        r   = new CanReply(   new int[]{CbusConstants.CBUS_RTOF },0x12 );
        t.reply(r);
        t.message(m);

        JUnitUtil.waitFor(()->{ return(tc.outbound.size()>0); }, "message didn't arrive");
        Assert.assertEquals("rton acknowledged in to out", "[5f8] 04",
        tc.outbound.elementAt(tc.outbound.size() - 1).toString());
        Assert.assertEquals("rton not increased in to out", 1,(tc.inbound.size()));
        Assert.assertEquals("rton acknowledged out to in", "[5f8] 05",
            tc.inbound.elementAt(tc.inbound.size() - 1).toString());


        t.dispose();

    }


    @Test
    public void testProcessASession() {
        
        
        CbusDummyCS t = new CbusDummyCS(memo);

        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_RLOC, 0xC4, 0xD2 },0x12 ); // 1234 Long
        t.message(m);

        JUnitUtil.waitFor(()->{ return(tc.inbound.size()>0); }, "reply didn't arrive");
        Assert.assertEquals("rton acknowledged", "[5f8] E1 01 C4 D2 80 00 00",
            tc.inbound.elementAt(tc.inbound.size() - 1).toString()); // assign session 1
        Assert.assertTrue("rloc 1 session",t.getNumberSessions()==1);
        
        m = new CanMessage( new int[]{CbusConstants.CBUS_QLOC, 1 },0x12 ); // query session 1
        t.message(m);
        JUnitUtil.waitFor(()->{ return(tc.inbound.size()>1); }, "reply didn't arrive");
        Assert.assertEquals("qloc acknowledged", "[5f8] E1 01 C4 D2 80 00 00",
            tc.inbound.elementAt(tc.inbound.size() - 1).toString());        
        
        m = new CanMessage( new int[]{CbusConstants.CBUS_DKEEP, 1  },0x12 ); // keep alive session 1
        t.message(m);
        
        m = new CanMessage( new int[]{CbusConstants.CBUS_DKEEP, 2  },0x12 ); // keep alive session 2
        t.message(m);        
        JUnitUtil.waitFor(()->{ return(tc.inbound.size()>2); }, "error reply 2 didn't arrive");
        Assert.assertEquals("dkeep no session", "[5f8] 63 02 00 03",
            tc.inbound.elementAt(tc.inbound.size() - 1).toString());
        
        m = new CanMessage( new int[]{CbusConstants.CBUS_DSPD, 1  },0x12 ); // speed dir session 1
        t.message(m);
        
        m = new CanMessage( new int[]{CbusConstants.CBUS_DSPD, 2  },0x12 ); // speed dir session 2
        t.message(m);        
        JUnitUtil.waitFor(()->{ return(tc.inbound.size()>3); }, "error reply 3 didn't arrive");
        Assert.assertEquals("dspd no session", "[5f8] 63 02 00 03",
            tc.inbound.elementAt(tc.inbound.size() - 1).toString());         
        
        m = new CanMessage( new int[]{CbusConstants.CBUS_QLOC, 2  },0x12 ); // qloc session 2
        t.message(m);        
        JUnitUtil.waitFor(()->{ return(tc.inbound.size()>4); }, "error reply 4 didn't arrive");
        Assert.assertEquals("qloc no session", "[5f8] 63 02 00 03",
            tc.inbound.elementAt(tc.inbound.size() - 1).toString());
        
        m = new CanMessage( new int[]{CbusConstants.CBUS_RLOC,  0xC4, 0xD2  },0x12 ); // rloc existing 2
        t.message(m);        
        JUnitUtil.waitFor(()->{ return(tc.inbound.size()>5); }, "error reply 5 didn't arrive");
        Assert.assertEquals("rloc existing session", "[5f8] 63 C4 D2 02",
            tc.inbound.elementAt(tc.inbound.size() - 1).toString());
        
        m = new CanMessage( new int[]{CbusConstants.CBUS_KLOC, 2  },0x12 ); // kloc session 2
        t.message(m);        
        JUnitUtil.waitFor(()->{ return(tc.inbound.size()>6); }, "error reply 6 didn't arrive");
        Assert.assertEquals("kloc no session", "[5f8] 63 02 00 03",
            tc.inbound.elementAt(tc.inbound.size() - 1).toString());
        
        m = new CanMessage( new int[]{CbusConstants.CBUS_RESTP },0x12 ); // request e stop
        t.message(m);        
        JUnitUtil.waitFor(()->{ return(tc.inbound.size()>7); }, "reply 7 didn't arrive");
        Assert.assertEquals("restp no session", "[5f8] 06",
            tc.inbound.elementAt(tc.inbound.size() - 1).toString());        
        
        m = new CanMessage( new int[]{CbusConstants.CBUS_KLOC, 1  },0x12 ); // kloc kill session 2
        t.message(m);          
        
        Assert.assertTrue("kloc 0 session",t.getNumberSessions()==0);
        

        t.dispose();      
        
    }
    
    /** Records what a command station tells its session listeners. */
    private static final class Heard implements CbusDummyCS.SessionListener {
        final java.util.List<String> events = new java.util.concurrent.CopyOnWriteArrayList<>();

        @Override
        public void sessionChanged(CbusDummyCSSession s) {
            events.add("changed " + s.getSessionNumber() + " " + s.getLocoAddress() + " " + s.getSpeedDirection()
                    + " " + s.getSpeedStepMode().numSteps);
        }

        @Override
        public void sessionEnded(CbusDummyCSSession s) {
            events.add("ended " + s.getSessionNumber());
        }
    }

    @Test
    public void testSessionListenerToldOfStartSpeedModeAndEnd() {
        CbusDummyCS t = new CbusDummyCS(memo);
        Heard heard = new Heard();
        t.addSessionListener(heard);

        t.message(new CanMessage(new int[]{CbusConstants.CBUS_RLOC, 0xC4, 0xD2}, 0x12)); // 1234 long
        t.message(new CanMessage(new int[]{CbusConstants.CBUS_DSPD, 1, 0x80 | 50}, 0x12));
        t.message(new CanMessage(new int[]{CbusConstants.CBUS_STMOD, 1, CbusConstants.CBUS_SS_28}, 0x12));
        t.message(new CanMessage(new int[]{CbusConstants.CBUS_STMOD, 1, CbusConstants.CBUS_SS_14}, 0x12));
        t.message(new CanMessage(new int[]{CbusConstants.CBUS_STMOD, 1, CbusConstants.CBUS_SS_28_INTERLEAVE}, 0x12));
        t.message(new CanMessage(new int[]{CbusConstants.CBUS_STMOD, 1, CbusConstants.CBUS_SS_128}, 0x12));
        t.message(new CanMessage(new int[]{CbusConstants.CBUS_KLOC, 1}, 0x12));

        Assertions.assertEquals(java.util.List.of(
                "changed 1 1234(L) 128 126",
                "changed 1 1234(L) 178 126",
                "changed 1 1234(L) 178 28",
                "changed 1 1234(L) 178 14",
                "changed 1 1234(L) 178 28",
                "changed 1 1234(L) 178 126",
                "ended 1"), heard.events);

        t.removeSessionListener(heard);
        t.message(new CanMessage(new int[]{CbusConstants.CBUS_RLOC, 0x00, 0x03}, 0x12));
        Assertions.assertEquals(7, heard.events.size(), "not told once removed");
        t.dispose();
    }

    @Test
    public void testStmodSendsNoReplyAndUnknownSessionIsAnError() {
        CbusDummyCS t = new CbusDummyCS(memo);
        t.message(new CanMessage(new int[]{CbusConstants.CBUS_RLOC, 0x00, 0x03}, 0x12));
        JUnitUtil.waitFor(() -> tc.inbound.size() == 1, "PLOC");
        t.message(new CanMessage(new int[]{CbusConstants.CBUS_STMOD, 1, CbusConstants.CBUS_SS_28}, 0x12));
        t.message(new CanMessage(new int[]{CbusConstants.CBUS_STMOD, 2, CbusConstants.CBUS_SS_28}, 0x12));
        JUnitUtil.waitFor(() -> tc.inbound.size() == 2, "error for session 2");
        Assertions.assertEquals("[5f8] 63 02 00 03", tc.inbound.elementAt(1).toString(),
                "only the unknown session is answered, with an error");
        Assertions.assertEquals(jmri.SpeedStepMode.NMRA_DCC_28, t.getSessions().get(0).getSpeedStepMode());
        t.dispose();
    }

    @Test
    public void testEmergencyStopIsTheModesEmergencyStopValue() {
        CbusDummyCS t = new CbusDummyCS(memo);
        t.message(new CanMessage(new int[]{CbusConstants.CBUS_RLOC, 0x00, 0x03}, 0x12)); // session 1, 128 steps
        t.message(new CanMessage(new int[]{CbusConstants.CBUS_RLOC, 0x00, 0x04}, 0x12)); // session 2
        t.message(new CanMessage(new int[]{CbusConstants.CBUS_STMOD, 2, CbusConstants.CBUS_SS_28}, 0x12));
        t.message(new CanMessage(new int[]{CbusConstants.CBUS_DSPD, 1, 0x80 | 60}, 0x12));
        t.message(new CanMessage(new int[]{CbusConstants.CBUS_DSPD, 2, 0x80 | 20}, 0x12));
        t.message(new CanMessage(new int[]{CbusConstants.CBUS_RESTP}, 0x12));
        Assertions.assertEquals(1, t.getSessions().get(0).getSpeedDirection(), "128 steps: 1");
        Assertions.assertEquals(2, t.getSessions().get(1).getSpeedDirection(), "28 steps: 2");
        t.dispose();
    }

    @Test
    public void testResetEndsEverySession() {
        CbusDummyCS t = new CbusDummyCS(memo);
        Heard heard = new Heard();
        t.addSessionListener(heard);
        for (int a = 3; a < 6; a++) {
            t.message(new CanMessage(new int[]{CbusConstants.CBUS_RLOC, 0x00, a}, 0x12));
        }
        Assertions.assertEquals(3, t.getSessions().size());
        t.resetCS();
        Assertions.assertEquals(0, t.getNumberSessions());
        Assertions.assertEquals(java.util.List.of("ended 1", "ended 2", "ended 3"),
                heard.events.subList(3, heard.events.size()), "every session ended, in order");
        t.dispose();
    }

    private TrafficControllerScaffold tc;
    private CanSystemConnectionMemo memo;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        
        memo = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
        
    }

    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        memo.dispose();
        tc = null;
        memo = null;
        JUnitUtil.tearDown();

    }

    // private static final Logger log = LoggerFactory.getLogger(CbusDummyCSTest.class);

}
