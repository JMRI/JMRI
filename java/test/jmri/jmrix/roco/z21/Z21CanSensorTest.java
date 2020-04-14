package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the jmri.jmrix.roco.z21.Z21CanSensor class.
 *
 * @author	Paul Bender Copyright 2019
 */
public class Z21CanSensorTest extends jmri.implementation.AbstractSensorTestBase {

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
        Assert.assertEquals("Sensor Status Request Sent", "07 00 C4 00 00 CD AB", znis.outbound.elementAt(0).toString());
    }

    // Z21CanSensor test for incoming status message
    @Test
    public void testZ21CanSensorStatusMsg() {

        // Verify this was created in UNKNOWN state
        Assert.assertTrue(t.getKnownState() == jmri.Sensor.UNKNOWN);

        // notify the Sensor that somebody else changed it...
        byte msg[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x11,(byte)0x00,(byte)0x00};
        Z21Reply reply = new Z21Reply(msg,14);
        ((Z21CanSensor)t).reply(reply); 
        jmri.util.JUnitUtil.waitFor(()->{return t.getState() == t.getRawState();}, "raw state = state");
        Assert.assertEquals("Known state after activate ", jmri.Sensor.ACTIVE, t.getKnownState());
        
        byte msg2[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00};
        reply = new Z21Reply(msg2,14);
        ((Z21CanSensor)t).reply(reply);

        Assert.assertEquals("Known state after inactivate ", jmri.Sensor.INACTIVE, t.getKnownState());

    }

    // Z21CanSensor test for setting state
    @Test
    public void testZ21CanSensorSetState() throws jmri.JmriException {
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
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        Z21SystemConnectionMemo memo = new Z21SystemConnectionMemo();
        znis = new Z21InterfaceScaffold();
        memo.setTrafficController(znis);
        t = new Z21CanSensor("ZSabcd:1","hello world",memo);
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
