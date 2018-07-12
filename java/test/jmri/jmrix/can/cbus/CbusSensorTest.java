package jmri.jmrix.can.cbus;

import jmri.Sensor;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TestTrafficController;
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
