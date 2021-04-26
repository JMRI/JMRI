package jmri.jmrix.can.cbus.simulator;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Steve Young Copyright (c) 2019
 */
public class CbusDummyCSSessionTest {

    @Test
    public void testCTor() {
        CbusDummyCSSession t = new CbusDummyCSSession(null,0,0,false);
        Assert.assertNotNull("exists",t);
        t.dispose();
    }
    
    @Test
    public void testFullSession() {
        
        CbusDummyCS cs = new CbusDummyCS(memo);
        cs.setDelay(0);
        CbusDummyCSSession t = new CbusDummyCSSession(cs,1,1234,true);
        Assert.assertNotNull("exists",t);        
        
        t.sendPloc();
        JUnitUtil.waitFor(()->{ return(tc.inbound.size()>0); }, "ploc didn't arrive");
        Assert.assertEquals("ploc sent", "[5f8] E1 01 C4 D2 80 00 00",
        tc.inbound.elementAt(tc.inbound.size() - 1).toString());
        
        t.setSpd(200);
        t.sendPloc();

        JUnitUtil.waitFor(()->{ return(tc.inbound.size()>1); }, "ploc 2 didn't arrive");
        Assert.assertEquals("ploc 2 sent", "[5f8] E1 01 C4 D2 C8 00 00",
        tc.inbound.elementAt(tc.inbound.size() - 1).toString());
        
        Assert.assertFalse(t.getIsDispatched());
        Assert.assertTrue(t.getisLong());
        Assert.assertTrue(t.getrcvdIntAddr()==1234);
        Assert.assertTrue(t.getSessionNum()==1);
        
        t.dispose();
        cs.dispose();
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

    // private final static Logger log = LoggerFactory.getLogger(CbusDummyCSTest.class);

}
