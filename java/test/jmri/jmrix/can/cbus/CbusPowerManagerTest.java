package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanReply;
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
 */
public class CbusPowerManagerTest {

    @Test
    public void testCTor() {
        TrafficControllerScaffold tc = new TrafficControllerScaffold();
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        CbusPowerManager t = new CbusPowerManager(memo);
        Assert.assertNotNull("exists",t);
        
        try {
            t.dispose();
        } catch (jmri.JmriException ex) {}
        t = null;
        tc = null;
        memo = null;
        
    }

    @Test
    public void testSetPower() {
        TrafficControllerScaffold tc = new TrafficControllerScaffold();
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        CbusPowerManager t = new CbusPowerManager(memo);        
        
        Assert.assertEquals("Memo name","CAN",t.getUserName());
        
        try {
            t.setPower(jmri.PowerManager.ON);
        } catch (jmri.JmriException ex) {}
        
        Assert.assertEquals("power reset",jmri.PowerManager.UNKNOWN,t.getPower());
        Assert.assertEquals("power on request sent","[5f8] 09" , 
            (tc.outbound.elementAt(tc.outbound.size() - 1).toString()) ); 
        try {
            t.setPower(jmri.PowerManager.OFF);
        } catch (jmri.JmriException ex) {}
        Assert.assertEquals("power off request sent","[5f8] 08" , 
            (tc.outbound.elementAt(tc.outbound.size() - 1).toString()) ); 
        
        try {
            t.dispose();
        } catch (jmri.JmriException ex) {}
        t = null;
        tc = null;
        memo = null;
        
    }    
    
    @Test
    public void testCanListeners() {
        TrafficControllerScaffold tc = new TrafficControllerScaffold();
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        
        Assert.assertEquals("no listeners",0,tc.numListeners());
        CbusPowerManager t = new CbusPowerManager(memo); 
        Assert.assertEquals("listener",1,tc.numListeners());
        
        CanReply r = new CanReply( new int[]{0x05 },0x12 );
        t.reply(r);
        Assert.assertEquals("confirm power on",jmri.PowerManager.ON,t.getPower());
        r = new CanReply( new int[]{0x04 },0x12 );
        t.reply(r);
        Assert.assertEquals("confirm power off",jmri.PowerManager.OFF,t.getPower());
        r = new CanReply( new int[]{0x05 },0x12 );
        t.reply(r);
        Assert.assertEquals("confirm power on",jmri.PowerManager.ON,t.getPower());
        
        try {
            t.dispose();
        } catch (jmri.JmriException ex) {}
        Assert.assertEquals("listener",0,tc.numListeners());
        
        try {
            t.setPower(jmri.PowerManager.ON);
            Assert.fail("After dispose Should have thrown an exception");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
        
        t = null;
        tc = null;
        memo = null;
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusPowerManagerTest.class);

}
