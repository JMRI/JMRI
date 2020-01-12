package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the jmri.jmrix.roco.z21.Z21RMBusSensor class.
 *
 * @author	Paul Bender Copyright 2004,2018
 */
public class Z21RMBusSensorTest extends jmri.implementation.AbstractSensorTestBase {

    private Z21InterfaceScaffold znis = null;

    @Override
    public int numListeners() {
        return znis.numListeners();
    }

    @Override
    public void checkOnMsgSent() {}

    @Override
    public void checkOffMsgSent() {}
        
    @Override
    public void checkStatusRequestMsgSent(){
        Assert.assertEquals("Sensor Status Request Sent", "04 00 81 00 00", znis.outbound.elementAt(0).toString());
    }

    // Z21RMBusSensor test for incoming status message
    @Test
    public void testZ21RMBusSensorStatusMsg() {

        // Verify this was created in UNKNOWN state
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.UNKNOWN);

        // notify the Sensor that somebody else changed it...
        byte msg[]={(byte)0x0F,(byte)0x00,(byte)0x80,(byte)0x00,
           (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
           (byte)0x00,(byte)0x02,(byte)0x00,(byte)0x00,(byte)0x00,
           (byte)0x00};
        Z21Reply m = new Z21Reply(msg,15);
        ((Z21RMBusSensor)t).reply(m); 
        jmri.util.JUnitUtil.waitFor(()->{return t.getState() == t.getRawState();}, "raw state = state");
        Assert.assertEquals("Known state after activate ", jmri.Sensor.ACTIVE, t.getKnownState());
        
        byte msg2[]={(byte)0x0F,(byte)0x00,(byte)0x80,(byte)0x00,
           (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
           (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
           (byte)0x00};
        m = new Z21Reply(msg2,15);
        ((Z21RMBusSensor)t).reply(m);

        Assert.assertEquals("Known state after inactivate ", jmri.Sensor.INACTIVE, t.getKnownState());

    }

    // Z21RMBusSensor test for setting state
    @Test
    public void testZ21RMBusSensorSetState() throws jmri.JmriException {
        t.setKnownState(jmri.Sensor.ACTIVE);
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.ACTIVE);
        t.setKnownState(jmri.Sensor.INACTIVE);
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.INACTIVE);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        znis = new Z21InterfaceScaffold();
        t = new Z21RMBusSensor("ZS042", znis,"Z");
    }

    @Override
    @After
    public void tearDown() {
        t.dispose();
	    znis=null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
