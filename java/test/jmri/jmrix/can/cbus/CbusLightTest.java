package jmri.jmrix.can.cbus;

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
 */
public class CbusLightTest {

    @Test
    public void testCTorShortEventSingle() {
        CbusLight t = new CbusLight("ML","+7",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorShortEventDouble() {
        CbusLight t = new CbusLight("ML","+1;-1",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }
    
    
    @Test
    public void testLongEventSingleNoN() {
        CbusLight t = new CbusLight("ML","+654e321",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }    


    @Test
    public void testLongEventDoubleNoN() {
        CbusLight t = new CbusLight("ML","-654e321;+123e456",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }    
    
    
    @Test
    public void testCTorLongEventSingle() {
        CbusLight t = new CbusLight("ML","+n654e321",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }    
    
    @Test
    public void testCTorLongEventDouble() {
        CbusLight t = new CbusLight("ML","+N299E17;-N123E456",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorHexEventJustOpsCode() {
        CbusLight t = new CbusLight("ML","X04;X05",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorHexEventOneByte() {
        CbusLight t = new CbusLight("ML","X2301;X30FF",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }
    
    
    @Test
    public void testCTorHexEventTwoByte() {
        CbusLight t = new CbusLight("ML","X410001;X56FFFF",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }

    
    @Test
    public void testCTorHexEventThreeByte() {
        CbusLight t = new CbusLight("ML","X6000010001;X72FFFFFF",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }    
    
    
    
    @Test
    public void testCTorHexEventFourByte() {
        CbusLight t = new CbusLight("ML","X9000010001;X91FFFFFFFF",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }


    @Test
    public void testCTorHexEventFiveByte() {
        CbusLight t = new CbusLight("ML","XB00D60010001;XB1FFFAAFFFFF",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }


    @Test
    public void testCTorHexEventSixByte() {
        CbusLight t = new CbusLight("ML","XD00D0060010001;XD1FFFAAAFFFFFE",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }
    
    
    @Test
    public void testCTorHexEventSevenByte() {
        CbusLight t = new CbusLight("ML","XF00D0A0600100601;XF1FFFFAAFAFFFFFE",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
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

    // private final static Logger log = LoggerFactory.getLogger(CbusLightTest.class);

}
