package jmri.jmrix.can.cbus.swing.cbusslotmonitor;

import jmri.DccLocoAddress;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of CbusSlotMonitorDataModel
 *
 * @author	Paul Bender Copyright (C) 2016
 * @author	Steve Young Copyright (C) 2019
 */
public class CbusSlotMonitorSessionTest {

    @Test
    public void testCtor() {
        CbusSlotMonitorSession t = new CbusSlotMonitorSession( new DccLocoAddress (179,true) );
        Assert.assertNotNull("exists", t);
        t = null;
    }
    
    @Test
    public void testReturnLocoAddress() {
        CbusSlotMonitorSession t = new CbusSlotMonitorSession( new DccLocoAddress (1234,true) );
        Assert.assertEquals("loco address returned",new DccLocoAddress (1234,true),t.getLocoAddr() );
        t = null;
    }

    @Test
    public void testGetSetSessionId() {
        CbusSlotMonitorSession t = new CbusSlotMonitorSession( new DccLocoAddress (4,false) );
        t.setSessionId(7);
        Assert.assertEquals("session id",7,t.getSessionId() );
        t = null;
    }

    @Test
    public void testSpeedSteps() {
        CbusSlotMonitorSession t = new CbusSlotMonitorSession( new DccLocoAddress (4,false) );
        Assert.assertEquals("default speed steps 128","128",t.getSpeedSteps() );
        t = null;
    }
    
    @Test
    public void testSpeeds() {
        CbusSlotMonitorSession t = new CbusSlotMonitorSession( new DccLocoAddress (4,false) );
        t.setDccSpeed(77);
        Assert.assertEquals("speed 77",77,t.getCommandedSpeed() );
        t.setDccSpeed(211);
        Assert.assertEquals("speed 211",83,t.getCommandedSpeed() );
        t.setDccSpeed(129);
        Assert.assertEquals("speed 0 fwd estop",0,t.getCommandedSpeed() );
        t = null;
    }
    
    @Test
    public void testFunctions() {
        CbusSlotMonitorSession t = new CbusSlotMonitorSession( new DccLocoAddress (4,false) );
        
        for (int i = 0; i <29; i++) {
            t.setFunction(i,true);
            Assert.assertTrue( t.getFunctionString().contains(Integer.toString(i) ) );
        }
        Assert.assertEquals("Full function string",
            "0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 ",
            t.getFunctionString()
        );

        t.setFunction(4,false);
        t.setFunction(19,false);
        t.setFunction(22,false);
        
        Assert.assertEquals("Partial function string",
            "0 1 2 3 5 6 7 8 9 10 11 12 13 14 15 16 17 18 20 21 23 24 25 26 27 28 ",
            t.getFunctionString()
        );
        
        for (int i = 0; i <29; i++) {
            t.setFunction(i,false);
        }
        Assert.assertTrue( t.getFunctionString().isEmpty() );
        
        t = null;
    }
    
    @Test
    public void testFlags() {
        
        CbusSlotMonitorSession t = new CbusSlotMonitorSession( new DccLocoAddress (1234,true) );
        
        Assert.assertTrue( t.getFlagString().isEmpty() );

        t.setFlags(0b0000_0001);
        Assert.assertEquals("speed steps by flag 28 I","28 Interleave",t.getSpeedSteps() );
        
        t.setFlags(0b0000_0010);
        Assert.assertEquals("speed steps by flag 14","14",t.getSpeedSteps() );
        
        t.setFlags(0b0000_0011);
        Assert.assertEquals("speed steps by flag 14","28",t.getSpeedSteps() );
        
        t.setFlags(0b0000_0000);
        Assert.assertEquals("speed steps by flag 128","128",t.getSpeedSteps() );
        
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();

    }

    @After
    public void tearDown() {        
        JUnitUtil.tearDown();    
    }

}
