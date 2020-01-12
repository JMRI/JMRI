package jmri.jmrix.roco.z21;

import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the jmri.jmrix.roco.z21.Z21SensorManager class for RMBus sensors.
 *
 * @author	Paul Bender Copyright (c) 2018,2019
 */
public class Z21RMBusSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private Z21InterfaceScaffold znis;
    private Z21SystemConnectionMemo memo;

    @Override
    public String getSystemName(int i) {
        return "ZS" + i;
    }

    @Test
    public void testZ21RMBusCTor() {
        Assert.assertNotNull(l);
    }

    @Test
    public void testByAddress() {
        // sample sensor object
        Sensor t = l.newSensor("ZS22", "test");

        // test get
        Assert.assertTrue(t == l.getByUserName("test"));
        Assert.assertTrue(t == l.getBySystemName("ZS22"));
    }

    @Test
    @Override
    public void testMisses() {
        // sample turnout object
        Sensor s = l.newSensor("ZS22", "test");
        Assert.assertNotNull("exists", s);

        // try to get nonexistant turnouts
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
    }

    @Test
    @Ignore("tests creation in response to feedback; not currently implemented")
    public void testZ21RMBusMessages() {
        // send messages for feedbak encoder 22
        // notify the Z21 that somebody else changed it...
        byte msg[]={(byte)0x0F,(byte)0x00,(byte)0x80,(byte)0x00,
           (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x20,(byte)0x00,
           (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
           (byte)0x00};
        Z21Reply m = new Z21Reply(msg,15);
        znis.sendTestMessage(m);

        // see if sensor exists
        Assert.assertTrue(null != l.getBySystemName("ZS22"));
    }

    @Test
    public void testAsAbstractFactory() {
        jmri.InstanceManager.setSensorManager(l);

        // ask for a Sensor, and check type
        SensorManager t = jmri.InstanceManager.sensorManagerInstance();

        Sensor o = t.newSensor("ZS21", "my name");
        Assert.assertNotNull("received sensor value",o);

        // make sure loaded into tables
        Assert.assertNotNull("get by system name",t.getBySystemName("ZS21"));
        Assert.assertNotNull("get by user name",t.getByUserName("my name"));
    }

    @Test
    public void testGetSystemPrefix() {
        Assert.assertEquals("prefix", "Z", l.getSystemPrefix());
    }

    @Test
    public void testAllowMultipleAdditions() {
        Assert.assertTrue(l.allowMultipleAdditions("foo"));
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        znis = new Z21InterfaceScaffold();
        memo = new Z21SystemConnectionMemo();
        memo.setTrafficController(znis);
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
        // create and register the manager object
        l = new Z21SensorManager(memo);
    }

    @After
    public void tearDown() {
        l.dispose();
        memo.getTrafficController().terminateThreads();
        memo = null;
        znis = null;
        JUnitUtil.tearDown();
    }

}
