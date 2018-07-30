package jmri.jmrix.can.cbus;

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
public class CbusTurnoutTest {
    
    @Test
    public void testCTorShortEventSingle() {
        CbusTurnout t = new CbusTurnout("MT","+7",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorShortEventDouble() {
        CbusTurnout t = new CbusTurnout("MT","+1;-1",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorLongEventSingle() {
        CbusTurnout t = new CbusTurnout("MT","+n654e321",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }    
    
    @Test
    public void testCTorLongEventDouble() {
        CbusTurnout t = new CbusTurnout("MT","+N299E17;-N123E456",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorHexEventJustOpsCode() {
        CbusTurnout t = new CbusTurnout("MT","X04;X05",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorHexEventOneByte() {
        CbusTurnout t = new CbusTurnout("MT","X2301;X30FF",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorHexEventTwoByte() {
        CbusTurnout t = new CbusTurnout("MT","X410001;X56FFFF",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorHexEventThreeByte() {
        CbusTurnout t = new CbusTurnout("MT","X6000010001;X72FFFFFF",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }    
    
    @Test
    public void testCTorHexEventFourByte() {
        CbusTurnout t = new CbusTurnout("MT","X9000010001;X91FFFFFFFF",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorHexEventFiveByte() {
        CbusTurnout t = new CbusTurnout("MT","XB00D60010001;XB1FFFAAFFFFF",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorHexEventSixByte() {
        CbusTurnout t = new CbusTurnout("MT","XD00D0060010001;XD1FFFAAAFFFFFE",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorHexEventSevenByte() {
        CbusTurnout t = new CbusTurnout("MT","XF00D0A0600100601;XF1FFFFAAFAFFFFFE",new TrafficControllerScaffold());
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

    // private final static Logger log = LoggerFactory.getLogger(CbusTurnoutTest.class);

}
