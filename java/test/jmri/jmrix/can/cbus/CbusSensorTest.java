package jmri.jmrix.can.cbus;

import jmri.Sensor;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TestTrafficController;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.can.cbus.CbusSensor class.
 *
 * @author	Bob Jacobsen Copyright 2008
 */
public class CbusSensorTest extends jmri.implementation.AbstractSensorTestBase {
        
    private TestTrafficController tc = null;

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkOnMsgSent() {
        Assert.assertTrue(new CbusAddress("+1").match(tc.rcvMessage));
    }

    @Override
    public void checkOffMsgSent() {
        Assert.assertTrue(new CbusAddress("-1").match(tc.rcvMessage));
    }

    @Override
    public void checkStatusRequestMsgSent() {}

    @Test
    public void testIncomingChange() {
        // message for Active and Inactive
        CanMessage mActive = new CanMessage(
                new int[]{CbusConstants.CBUS_ASON, 0x00, 0x00, 0x00, 0x01}, tc.getCanid()
        );
        CanMessage mInactive = new CanMessage(
                new int[]{CbusConstants.CBUS_ASOF, 0x00, 0x00, 0x00, 0x01}, tc.getCanid()
        );

        // check states
        Assert.assertTrue(t.getKnownState() == Sensor.UNKNOWN);

        ((CbusSensor)t).message(mActive);
        Assert.assertTrue(t.getKnownState() == Sensor.ACTIVE);

        ((CbusSensor)t).message(mInactive);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);

    }

    @Test
    public void testLocalChange() throws jmri.JmriException {
        tc.rcvMessage = null;
        t.setKnownState(Sensor.ACTIVE);
        Assert.assertTrue(t.getKnownState() == Sensor.ACTIVE);
        checkOnMsgSent();

        tc.rcvMessage = null;
        t.setKnownState(Sensor.INACTIVE);
        Assert.assertTrue(t.getKnownState() == Sensor.INACTIVE);
        checkOffMsgSent();
    }

    @Test
    public void testNameCreation() {
        Assert.assertTrue("create MSX0A;+N15E6", null != new CbusSensor("MS", "X0A;+N15E6", tc));
    }

    
    @Test
    public void testCTorShortEventSingle() {
        CbusSensor t = new CbusSensor("MS","+7",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorShortEventDouble() {
        CbusSensor t = new CbusSensor("MS","+1;-1",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }


    @Test
    public void testLongEventSingleNoN() {
        CbusSensor t = new CbusSensor("MS","+654e321",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }    


    @Test
    public void testLongEventDoubleNoN() {
        CbusSensor t = new CbusSensor("MS","-654e321;+123e456",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }    

    
    @Test
    public void testCTorLongEventSingle() {
        CbusSensor t = new CbusSensor("MS","+n654e321",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }    
    
    @Test
    public void testCTorLongEventDouble() {
        CbusSensor t = new CbusSensor("MS","+N299E17;-N123E456",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCTorHexEventJustOpsCode() {
        CbusSensor t = new CbusSensor("MS","X04;X05",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorHexEventOneByte() {
        CbusSensor t = new CbusSensor("MS","X2301;X30FF",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }
    
    
    @Test
    public void testCTorHexEventTwoByte() {
        CbusSensor t = new CbusSensor("MS","X410001;X56FFFF",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }

    
    @Test
    public void testCTorHexEventThreeByte() {
        CbusSensor t = new CbusSensor("MS","X6000010001;X72FFFFFF",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }    
    
    
    
    @Test
    public void testCTorHexEventFourByte() {
        CbusSensor t = new CbusSensor("MS","X9000010001;X91FFFFFFFF",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }


    @Test
    public void testCTorHexEventFiveByte() {
        CbusSensor t = new CbusSensor("MS","XB00D60010001;XB1FFFAAFFFFF",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }


    @Test
    public void testCTorHexEventSixByte() {
        CbusSensor t = new CbusSensor("MS","XD00D0060010001;XD1FFFAAAFFFFFE",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }
    
    
    @Test
    public void testCTorHexEventSevenByte() {
        CbusSensor t = new CbusSensor("MS","XF00D0A0600100601;XF1FFFFAAFAFFFFFE",new TrafficControllerScaffold());
        Assert.assertNotNull("exists",t);
    }
    
    
    
    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // load dummy TrafficController
        tc = new TestTrafficController();
        t = new CbusSensor("MS", "+1;-1", tc);
    }

    @Override
    @After
    public void tearDown() {
	t.dispose();
	tc=null;
        JUnitUtil.tearDown();
    }
}
