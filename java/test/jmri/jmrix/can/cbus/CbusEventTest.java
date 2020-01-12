package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventTest {

    @Test
    public void testCTor() {
        CbusEvent t = new CbusEvent(0,1);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testSetandGet() {
        CbusEvent t = new CbusEvent(123,456);
        Assert.assertEquals("get Node Number",123,t.getNn());
        Assert.assertEquals("get Event Number",456,t.getEn());
        Assert.assertEquals("get inital state",CbusEvent.EvState.UNKNOWN,t.getState());
        t.setState(CbusEvent.EvState.ON);
        Assert.assertEquals("getState on",CbusEvent.EvState.ON,t.getState());
        t.setState(CbusEvent.EvState.OFF);
        Assert.assertEquals("getState off",CbusEvent.EvState.OFF,t.getState());
        t.setName("Jon Smith");
        Assert.assertEquals("getName","Jon Smith",t.getName());
        t.setEn(4);
        t.setNn(7);
        Assert.assertEquals("get Node Number 7",7,t.getNn());
        Assert.assertEquals("get Event Number 4",4,t.getEn());        
        t = null;
    }    
    
    @Test
    public void testMatches() {
        CbusEvent t = new CbusEvent(123,456);
        Assert.assertEquals("no match event",false,t.matches(123,111));
        Assert.assertEquals("no match node",false,t.matches(111,456));
        Assert.assertEquals("no match node",false,t.matches(111,222));
        Assert.assertEquals("match",true,t.matches(123,456));
        t = null;
    }
    
    @Test
    public void testSending() {
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
        
        CbusEvent t = new CbusEvent(123,456);
        t.sendOn();
        Assert.assertEquals("node 123 ev 456 on","[5f8] 90 00 7B 01 C8" , 
            (tc.outbound.elementAt(tc.outbound.size() - 1).toString()) ); 
        t.sendOff();
        Assert.assertEquals("node 123 ev 456 off","[5f8] 91 00 7B 01 C8" , 
            (tc.outbound.elementAt(tc.outbound.size() - 1).toString()) );
        t.sendRequest();
        Assert.assertEquals("node 123 ev 456 request","[5f8] 92 00 7B 01 C8" , 
            (tc.outbound.elementAt(tc.outbound.size() - 1).toString()) );
            
        CbusEvent ta = new CbusEvent(0,12345);
        ta.sendOn();
        Assert.assertEquals("ev 12345 on","[5f8] 98 00 00 30 39" , 
            (tc.outbound.elementAt(tc.outbound.size() - 1).toString()) ); 
        ta.sendOff();
        Assert.assertEquals("ev 12345 off","[5f8] 99 00 00 30 39" , 
            (tc.outbound.elementAt(tc.outbound.size() - 1).toString()) );
        ta.sendRequest();
        Assert.assertEquals("ev 12345 request","[5f8] 9A 00 00 30 39" , 
            (tc.outbound.elementAt(tc.outbound.size() - 1).toString()) );

        ta.setState(CbusEvent.EvState.ON);
        Assert.assertEquals("toggle set on",CbusEvent.EvState.ON,ta.getState());
        ta.sendEvent(CbusEvent.EvState.TOGGLE);
        Assert.assertEquals("toggle off",CbusEvent.EvState.OFF,ta.getState());
        ta.sendEvent(CbusEvent.EvState.TOGGLE);    
        Assert.assertEquals("toggle on",CbusEvent.EvState.ON,ta.getState());
        
        t = null;
        ta = null;
        tc = null;
        memo = null;
    }    
    
    @Test
    public void testToString() {
        CbusEvent t = new CbusEvent(0,456);
        Assert.assertEquals("toString 1","EN:456 ",t.toString());
        t.setName("Jon Smith");
        Assert.assertEquals("toString 2","EN:456 Jon Smith ",t.toString());
        t = null;
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

    // private final static Logger log = LoggerFactory.getLogger(CbusEventTest.class);

}
