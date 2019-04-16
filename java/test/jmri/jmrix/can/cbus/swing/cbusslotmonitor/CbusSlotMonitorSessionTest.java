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
        
        t.setSpeedSteps("28");
        Assert.assertEquals("28 speed steps","28",t.getSpeedSteps() );
        t = null;
        
    }
    
    @Test
    public void testDirections() {
        CbusSlotMonitorSession t = new CbusSlotMonitorSession( new DccLocoAddress (4,false) );
        
        Assert.assertTrue("0 rev",t.getDirection().contains("Rev"));
        t.setDccSpeed(1);
        Assert.assertTrue("1 rev",t.getDirection().contains("Rev"));
        t.setDccSpeed(77);
        Assert.assertTrue("77 rev",t.getDirection().contains("Rev"));
        t.setDccSpeed(127);
        Assert.assertTrue("127 rev",t.getDirection().contains("Rev"));
        t.setDccSpeed(128);
        Assert.assertTrue("128 fwd",t.getDirection().contains("For"));
        t.setDccSpeed(129);
        Assert.assertTrue("129 fwd",t.getDirection().contains("For"));
        t.setDccSpeed(211);
        Assert.assertTrue("211 fwd",t.getDirection().contains("For"));
        
        t.setSpeedSteps("28");
        t.setDccSpeed(1);
        Assert.assertTrue("28 1 rev",t.getDirection().contains("Rev"));
        t.setDccSpeed(28);
        Assert.assertTrue("28 28 fwd",t.getDirection().contains("For"));

        t.setSpeedSteps("28I");
        t.setDccSpeed(1);
        Assert.assertTrue("28i 1 rev",t.getDirection().contains("Rev"));
        t.setDccSpeed(28);
        Assert.assertTrue("28i 28 fwd",t.getDirection().contains("For"));

        t.setSpeedSteps("14");
        t.setDccSpeed(1);
        Assert.assertTrue("14 1 rev",t.getDirection().contains("Rev"));
        t.setDccSpeed(14);
        Assert.assertTrue("14 14 fwd",t.getDirection().contains("For"));
        
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
        Assert.assertEquals("speed steps by flag 28 I","28I",t.getSpeedSteps() );
        
        t.setFlags(0b0000_0010);
        Assert.assertEquals("speed steps by flag 14","14",t.getSpeedSteps() );
        
        t.setFlags(0b0000_0011);
        Assert.assertEquals("speed steps by flag 14","28",t.getSpeedSteps() );
        
        t.setFlags(0b0000_0000);
        Assert.assertEquals("speed steps by flag 128","128",t.getSpeedSteps() );
        t = null;
        
    }
    
    @Test
    public void testConsist() {
        
        CbusSlotMonitorSession t = new CbusSlotMonitorSession( new DccLocoAddress (1234,true) );
        Assert.assertTrue( t.getConsistId() == 0 );
        t.setConsistId(77);
        Assert.assertTrue( t.getConsistId() == 77 );
        t = null;
        
    }
    
    @Test
    public void testgetFlagString() {
        
        CbusSlotMonitorSession t = new CbusSlotMonitorSession( new DccLocoAddress (1234,true) );
        Assert.assertTrue("flags unset -1",t.getFlagString().isEmpty() );
        
        t.setFlags(0b0000_0000);
        Assert.assertTrue("flags 0",t.getFlagString().contains("Engine State:Active"));
        
        t.setFlags(0b0001_0000);
        Assert.assertTrue("flags 4",t.getFlagString().contains("State:Consist Master"));
        
        t.setFlags(0b0010_0000);
        Assert.assertTrue("flags 5",t.getFlagString().contains("State:Consisted"));
        
        t.setFlags(0b0011_0000);
        Assert.assertTrue("flags 4 5",t.getFlagString().contains("State:Inactive"));
        Assert.assertTrue("flags 2 0",t.getFlagString().contains("Lights:0"));
        Assert.assertTrue("flags 3 0",t.getFlagString().contains("Direction:0"));
        
        t.setFlags(0b0000_0100);
        Assert.assertTrue("flags 2 1",t.getFlagString().contains("Lights:1"));
        
        t.setFlags(0b0000_1000);
        Assert.assertTrue("flags 3",t.getFlagString().contains("Direction:1"));
        
        t = null;
        
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
