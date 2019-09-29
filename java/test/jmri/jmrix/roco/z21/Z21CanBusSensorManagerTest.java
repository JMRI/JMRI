package jmri.jmrix.roco.z21;

import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the jmri.jmrix.roco.z21.Z21SensorManager class for CanBus sensors.
 *
 * @author	Paul Bender Copyright (c) 2018,2019
 */
public class Z21CanBusSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private Z21InterfaceScaffold znis;
    private Z21SystemConnectionMemo memo; 

    @Override
    public String getSystemName(int i) {
        return "ZSABCD:" + i;
    }

    @Test
    @Override
    public void testDefaultSystemName() {
        // create
        Sensor t = l.provideSensor("ZSABCD:5");
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("system name correct ", t,l.getBySystemName(getSystemName(5)));
    }

    @Test
    @Override
    public void testProvideName() {
        // create
        Sensor t = l.provide("ZSABCD:5");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("system name correct ", t,l.getBySystemName(getSystemName(5)));
    }

    @Test
    public void testDefaultSystemNameLowerCase() {
        // create
        Sensor t = l.provideSensor("ZSabcd:5");
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("system name same input correct ", t,l.getBySystemName("ZSabcd:5"));
        Assert.assertEquals("system name same value correct ", t,l.getBySystemName(getSystemName(5)));
    }

    @Test
    public void testDefaultSystemMixedDigit() {
        // create
        Sensor t = l.provideSensor("ZSa1c3:5");
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("system name same input correct ", t,l.getBySystemName("ZSa1c3:5"));
        Assert.assertEquals("system name same value correct ", t,l.getBySystemName("ZSA1C3:5"));
    }

    @Test
    public void testDefaultSystemMixedCase() {
        // create
        Sensor t = l.provideSensor("ZSaBcD:5");
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("system name same input correct",t,l.getBySystemName("ZSaBcD:5"));
        Assert.assertEquals("system name opposite input correct", t, l.getBySystemName("ZSAbCd:5"));
        Assert.assertEquals("system name same all lower", t,l.getBySystemName("ZSabcd:5"));
        Assert.assertEquals("system name same all upper", t,l.getBySystemName("ZSABCD:5"));
    }


    @Test
    public void testZ21CanBusCTor() {
        Assert.assertNotNull(l);
    }

    @Test
    public void testByAddress() {
        // sample sensor object
        Sensor t = l.newSensor("ZSABCD:3", "test");

        // test get
        Assert.assertTrue(t == l.getByUserName("test"));
        Assert.assertTrue(t == l.getBySystemName("ZSABCD:3"));
    }

    @Test
    @Override
    public void testMisses() {
        // sample turnout object
        Sensor s = l.newSensor("ZSABCD:2", "test");
        Assert.assertNotNull("exists", s);

        // try to get nonexistant turnouts
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
    }

    @Test
    public void testZ21CanBusMessages() {
        // send messages for feedback encoder abcd:1
        // notify the Z21 that somebody else changed it...
        byte msg[]={(byte)0x0E,(byte)0x00,(byte)0xC4,(byte)0x00,(byte)0xcd,(byte)0xab,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00};
        Z21Reply reply = new Z21Reply(msg,14);
        znis.sendTestMessage(reply);

        // see if sensor exists note that the node value is numeric,
        // but in hex format.
        Assert.assertNotNull(l.getBySystemName("ZSabcd:1"));
        Assert.assertNotNull(l.getBySystemName("ZSABCD:1"));
    }

    @Test
    public void testAsAbstractFactory() {
        jmri.InstanceManager.setSensorManager(l);

        // ask for a Sensor, and check type
        SensorManager t = jmri.InstanceManager.sensorManagerInstance();

        Sensor o = t.newSensor("ZSABCD:1", "my name");
        Assert.assertNotNull("received sensor value",o);

        // make sure loaded into tables
        Assert.assertNotNull("get by system name",t.getBySystemName("ZSABCD:1"));
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
    protected int getNumToTest1() {
        return 6; // overriding 9 since outside valid range
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
        znis.terminateThreads();
        znis = null;
        memo = null;
        JUnitUtil.tearDown();
    }

}
