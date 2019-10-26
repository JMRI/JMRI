package jmri.jmrix.lenz;

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
 * Tests for the jmri.jmrix.lenz.XNetSensorManager class.
 *
 * @author	Paul Bender Copyright (c) 2003
 */
public class XNetSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private XNetInterfaceScaffold xnis;

    @Override
    public String getSystemName(int i) {
        return "XS" + i;
    }

    @Test
    public void testXNetCTor() {
        Assert.assertNotNull(l);
    }

    @Test
    public void testByAddress() {
        // sample sensor object
        Sensor t = l.newSensor("XS22", "test");

        // test get
        Assert.assertTrue(t == l.getByUserName("test"));
        Assert.assertTrue(t == l.getBySystemName("XS22"));
    }

    @Test
    @Override
    public void testMisses() {
        // sample turnout object
        Sensor s = l.newSensor("XS22", "test");
        Assert.assertNotNull("exists", s);

        // try to get nonexistant turnouts
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
    }

    @Test
    public void testXNetMessages() {
        // send messages for feedbak encoder 22
        // notify the XpressNet that somebody else changed it...
        XNetReply m1 = new XNetReply();
        m1.setElement(0, 0x42);     // Opcode for feedback response
        m1.setElement(1, 0x02);     // The feedback encoder address
        m1.setElement(2, 0x51);     // A bit pattern telling which
        // bits of the upper nibble
        // are on in the message.
        m1.setElement(3, 0x11);     // The XOR of everything above
        xnis.sendTestMessage(m1);

        // see if sensor exists
        Assert.assertTrue(null != l.getBySystemName("XS22"));
    }

    @Test
    public void testAsAbstractFactory() {
        jmri.InstanceManager.setSensorManager(l);

        // ask for a Sensor, and check type
        SensorManager t = jmri.InstanceManager.sensorManagerInstance();

        Sensor o = t.newSensor("XS21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received sensor value {}", o);
        }
        Assert.assertNotNull(o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: {}", t.getBySystemName("XS21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name: {}", t.getByUserName("my name"));
        }

        Assert.assertTrue(null != t.getBySystemName("XS21"));
        Assert.assertTrue(null != t.getByUserName("my name"));

    }

    @Test
    public void testGetSystemPrefix() {
        Assert.assertEquals("prefix", "X", l.getSystemPrefix());
    }

    @Test
    public void testAllowMultipleAdditions() {
        Assert.assertTrue(l.allowMultipleAdditions("foo"));
    }

    @Test
    public void testProvideAddressAndPin() {
        Assert.assertNotNull("Sensor XS99:3 provided",l.provideSensor("XS99:3"));
    }

    // from here down is testing infrastructure
    private final static Logger log = LoggerFactory.getLogger(XNetSensorManagerTest.class);

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        xnis = new XNetInterfaceScaffold(new LenzCommandStation());
        // create and register the manager object
        l = new XNetSensorManager(xnis.getSystemConnectionMemo());
    }

    @After
    public void tearDown() {
        l.dispose();
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
