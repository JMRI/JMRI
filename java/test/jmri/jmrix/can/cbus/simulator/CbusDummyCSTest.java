package jmri.jmrix.can.cbus.simulator;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (c) 2019
 */
public class CbusDummyCSTest {

    TrafficControllerScaffold tc;

    @Test
    public void testCTor() {
        CbusDummyCS t = new CbusDummyCS(null);
        Assert.assertNotNull("exists",t);
        t.dispose();
        t = null;
    }

    @Test
    public void testCTorTC() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
        Assert.assertTrue("0 listeners",tc.numListeners()==0);
        CbusDummyCS t = new CbusDummyCS(memo);
        Assert.assertNotNull("exists",t);
        Assert.assertTrue("1 listener",tc.numListeners()==1);
        
        Assert.assertTrue("start 0 sessions",t.getNumberSessions()==0);
        Assert.assertTrue("start getDummyType",t.getDummyType()==1);
        
        t.setDelay(7);
        Assert.assertEquals("getSetDelay", 7,t.getDelay());
        
        t.dispose();
        t = null;
        Assert.assertTrue("0 listeners after dispose",tc.numListeners()==0);
        tc=null;
        memo = null;
    }


    @Test
    public void testProcessInOutTrackOnOff() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
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

        m = null;
        r = null;
        t.dispose();
        t = null;
        tc=null;
        memo = null;
    }


    @Test
    public void testProcessASession() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
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
        
        m = null;
        t.dispose();
        t = null;
        tc=null;
        memo = null;        
        
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusDummyCSTest.class);

}
