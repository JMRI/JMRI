package jmri.jmrix.dccpp;

import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.dccpp.DCCppSensorManager class.
 *
 * @author	Paul Bender Copyright (c) 2003,2016
 * @author	Mark Underwood Copyright (c) 2015
 */
public class DCCppSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private DCCppInterfaceScaffold xnis = null;

    @Override
    public String getSystemName(int i) {
        return "DS" + i;
    }

    @Test
    public void testDCCppSensorCreate() {
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testByAddress() {
        // sample sensor object
        Sensor t = l.newSensor("DS22", "test");

        // test get
        Assert.assertTrue(t == l.getByUserName("test"));
        Assert.assertTrue(t == l.getBySystemName("DS22"));
    }

    @Test
    @Override
    public void testMisses() {
        // sample turnout object
        Sensor s = l.newSensor("DS22", "test");
        Assert.assertNotNull("exists", s);

        // try to get nonexistant turnouts
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
    }

    @Test
    public void testDCCppMessages() {
        // sample turnout object
        Sensor s = l.newSensor("DS22", "test");
        Assert.assertNotNull("exists", s);

        // send messages for feedbak encoder 22
        // notify the DCC++ that somebody else changed it...
        DCCppReply m1 = DCCppReply.parseDCCppReply("Q 22");
        xnis.sendTestMessage(m1);

        // see if sensor exists
        Assert.assertNotNull(l.getBySystemName("DS22"));

    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Sensor, and check type
        SensorManager t = jmri.InstanceManager.sensorManagerInstance();

        Sensor o = t.newSensor("DS21", "my name");

        Assert.assertNotNull(o);

        // make sure loaded into tables
        Assert.assertNotNull(t.getBySystemName("DS21"));
        Assert.assertNotNull(t.getByUserName("my name"));

    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DCCppSensorManagerTest.class);

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        // prepare an interface
        xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(xnis);
        xnis.setSystemConnectionMemo(memo);
        Assert.assertNotNull("exists", xnis);
        l = new DCCppSensorManager(xnis.getSystemConnectionMemo());
        jmri.InstanceManager.setSensorManager(l);
    }

    @After
    public void tearDown() {
        l.dispose();
        l = null;
        xnis = null;
        jmri.util.JUnitUtil.clearShutDownManager();
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.tearDown();
    }

}
