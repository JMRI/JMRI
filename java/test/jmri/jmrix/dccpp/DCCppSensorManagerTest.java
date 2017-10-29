package jmri.jmrix.dccpp;

import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        return "DCCPPS" + i;
    }

    @Test
    public void testDCCppSensorCreate() {
        Assert.assertNotNull("exists", l);
    }

    @Test
    public void testByAddress() {
        // sample sensor object
        Sensor t = l.newSensor("DCCPPS22", "test");

        // test get
        Assert.assertTrue(t == l.getByUserName("test"));
        Assert.assertTrue(t == l.getBySystemName("DCCPPS22"));
    }

    @Test
    @Override
    public void testMisses() {
        // sample turnout object
        Sensor s = l.newSensor("DCCPPS22", "test");
        Assert.assertNotNull("exists", s);

        // try to get nonexistant turnouts
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
    }

    @Test
    public void testDCCppMessages() {
        // sample turnout object
        Sensor s = l.newSensor("DCCPPS22", "test");
        Assert.assertNotNull("exists", s);

        // send messages for feedbak encoder 22
        // notify the DCC++ that somebody else changed it...
        DCCppReply m1 = DCCppReply.parseDCCppReply("Q 22");
        xnis.sendTestMessage(m1);

        // see if sensor exists
        Assert.assertTrue(null != l.getBySystemName("DCCPPS22"));

    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Sensor, and check type
        SensorManager t = jmri.InstanceManager.sensorManagerInstance();

        Sensor o = t.newSensor("DCCPPS21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received sensor value " + o);
        }
        Assert.assertTrue(null != (DCCppSensor) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + t.getBySystemName("DCCPPS21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + t.getByUserName("my name"));
        }

        Assert.assertTrue(null != t.getBySystemName("DCCPPS21"));
        Assert.assertTrue(null != t.getByUserName("my name"));

    }

    private final static Logger log = LoggerFactory.getLogger(DCCppSensorManagerTest.class);

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        // prepare an interface
        xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        Assert.assertNotNull("exists", xnis);
        l = new DCCppSensorManager(xnis, "DCCPP");
        jmri.InstanceManager.setSensorManager(l);
    }

    @After
    public void tearDown() {
        l.dispose();
        JUnitUtil.tearDown();
    }

}
