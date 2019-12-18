package jmri.jmrix.can.cbus.simulator;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (c) 2019
 */
public class CbusEventResponderTest {

    @Test
    public void testCTor() {
        CbusEventResponder t = new CbusEventResponder(null);
        Assert.assertNotNull("exists",t);
        t.dispose();
    }
        
    @Test
    public void testTCSetGet() {    
        
        Assert.assertTrue("0 listeners to start",tc.numListeners()==0);
        CbusEventResponder t = new CbusEventResponder(memo);
        Assert.assertTrue("1 listener",tc.numListeners()==1);
        
        t.setNode(129);
        Assert.assertEquals("getNode", 129, t.getNode());
        
        t.setMode(4);
        Assert.assertEquals("getMode", 4, t.getMode());        
        
        t.dispose();
        Assert.assertTrue("0 listeners",tc.numListeners()==0);
        t = null;

    }
    
    @Test
    public void testResponses() {

        CbusEventResponder t = new CbusEventResponder(memo);
        
        Assert.assertTrue(t.getDelay()>0);
        t.setDelay(0);
        Assert.assertTrue(t.getDelay()==0);

        Assert.assertEquals("start getProcessIn", false,t.getProcessIn());
        Assert.assertEquals("start getProcessOut", true,t.getProcessOut());
        Assert.assertEquals("start getSendIn", true,t.getSendIn());
        Assert.assertEquals("start getSendOut", false,t.getSendOut());        
        
        CanMessage m = new CanMessage(120);
        m.setNumDataElements(5);
        m.setElement(0, 0x9A); // short request opc
        m.setElement(1, 0x00);
        m.setElement(2, 0x00);
        m.setElement(3, 0xde);
        m.setElement(4, 0x00);
        
        CanReply r   = new CanReply(m);
        
        r.setExtended(true);
        t.reply(r);
        
        r.setExtended(false);
        r.setRtr(true);
        t.reply(r);
        
        r.setRtr(false);
        t.reply(r);
        
        
        t.message(m);
        // as frames passed direct to responder, tc inbound / outbound will just be event responses
        
        JUnitUtil.waitFor(()->{ return(tc.inbound.size()>0); }, "reply didn't arrive");
        Assert.assertTrue("message sent",tc.inbound.elementAt(tc.inbound.size() - 1).toString().contains("00 00 DE 00"));
        Assert.assertEquals("reply not acknowledged", 0,(tc.outbound.size()));
        
        t.setProcessIn(true);
        t.setProcessOut(false);
        t.setSendIn(false);
        t.setSendOut(true);
        Assert.assertEquals("getProcessIn", true,t.getProcessIn());
        Assert.assertEquals("getProcessOut", false,t.getProcessOut());
        Assert.assertEquals("getSendIn", false,t.getSendIn());
        Assert.assertEquals("getSendOut", true,t.getSendOut());    
    
        tc.inbound.clear();
        tc.outbound.clear();
    
        t.reply(r);
        t.message(m);
    
        JUnitUtil.waitFor(()->{ return(tc.outbound.size()>0); }, "message didn't arrive");
        Assert.assertTrue("message sent",tc.outbound.elementAt(tc.outbound.size() - 1).toString().contains("00 00 DE 00"));
        Assert.assertEquals("reply not acknowledged", 0,(tc.inbound.size()));

        tc.inbound.clear();
        tc.outbound.clear();

        t.setProcessIn(true);
        t.setProcessOut(true);
        t.setSendIn(true);
        t.setSendOut(false);
    
        m.setElement(0, 0x92); // long request opc
        m.setElement(1, 0x17); // add node number
        t.message(m);
        JUnitUtil.waitFor(()->{ return(tc.inbound.size()>0); }, "reply didn't arrive");
        Assert.assertTrue("long message sent",tc.inbound.elementAt(tc.inbound.size() - 1).toString().contains("17 00 DE 00"));
        
        m = null;
        t.dispose();
        
    }
        
        
        
    @Test
    public void testModes() {

        CbusEventResponder t = new CbusEventResponder(memo);
        
        CanMessage m = new CanMessage(120);
        m.setNumDataElements(5);
        m.setElement(0, 0x92); // long request 
        m.setElement(1, 0x17);
        m.setElement(2, 0x00);
        m.setElement(3, 0xde);
        m.setElement(4, 0x00);
    
        t.setMode(0); // off
        t.message(m); // no response should be generated
        
        m.setExtended(true);
        t.message(m); // no response should be generated
        
        m.setExtended(false);
        m.setRtr(true);
        t.message(m); // no response should be generated
        m.setRtr(false);
        
        t.setMode(2); // odd / even
        t.setNode(5777);
        t.message(m); // no response generated
        t.setNode(5888);
        
        t.message(m); // even event number
        JUnitUtil.waitFor(()->{ return(tc.inbound.size()>0); }, "reply didn't arrive");
        Assert.assertEquals("Event even response", "[5f8] 94 17 00 DE 00",tc.inbound.elementAt(tc.inbound.size() - 1).toString());
        
        Assert.assertEquals("just 1 reply", 1,(tc.inbound.size()));
        
        m.setElement(4, 0x11);
        t.message(m); // odd event number
        JUnitUtil.waitFor(()->{ return(tc.inbound.size()>1); }, "reply didn't arrive");
        Assert.assertEquals("Event odd response", "[5f8] 93 17 00 DE 11",tc.inbound.elementAt(tc.inbound.size() - 1).toString());
    
        t.setMode(4); // off
        t.message(m);
        JUnitUtil.waitFor(()->{ return(tc.inbound.size()>2); }, "reply didn't arrive");
        Assert.assertEquals("Event off response", "[5f8] 94 17 00 DE 11",tc.inbound.elementAt(tc.inbound.size() - 1).toString());
        
        t.setMode(3); // on
        t.message(m);
        JUnitUtil.waitFor(()->{ return(tc.inbound.size()>3); }, "reply didn't arrive");
        Assert.assertEquals("Event on response", "[5f8] 93 17 00 DE 11",tc.inbound.elementAt(tc.inbound.size() - 1).toString());
        
        m = null;
        t.dispose();
    
    }

    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tc;

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        
        memo = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
    }

    @After
    public void tearDown() {
        
        tc = null;
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventResponderTest.class);

}
