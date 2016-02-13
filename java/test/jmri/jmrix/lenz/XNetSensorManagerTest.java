// XNetSensorManagerTest.java
package jmri.jmrix.lenz;

import jmri.Sensor;
import jmri.SensorManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.lenz.XNetSensorManager class.
 *
 * @author	Paul Bender Copyright (c) 2003
 * @version $Revision$
 */
public class XNetSensorManagerTest extends TestCase {

    public void testXNetSensorCreate() {
        // prepare an interface
        XNetInterfaceScaffold xnis = new XNetInterfaceScaffold(new LenzCommandStation());
        Assert.assertNotNull("exists", xnis);

        // create and register the manager object in a new instance manager
        new jmri.InstanceManager() {
            protected void init() {
                super.init();
                root = null;
            }
        };
        XNetSensorManager l = new XNetSensorManager(xnis, "X");
        jmri.InstanceManager.setSensorManager(l);

    }

    public void testByAddress() {
        // prepare an interface
        XNetInterfaceScaffold xnis = new XNetInterfaceScaffold(new LenzCommandStation());
        Assert.assertNotNull("exists", xnis);

        // create and register the manager object
        XNetSensorManager l = new XNetSensorManager(xnis, "X");

        // sample sensor object
        Sensor t = l.newSensor("XS22", "test");

        // test get
        Assert.assertTrue(t == l.getByUserName("test"));
        Assert.assertTrue(t == l.getBySystemName("XS22"));
    }

    public void testMisses() {
        // prepare an interface
        XNetInterfaceScaffold xnis = new XNetInterfaceScaffold(new LenzCommandStation());
        Assert.assertNotNull("exists", xnis);

        // create and register the manager object
        XNetSensorManager l = new XNetSensorManager(xnis, "X");

        // sample turnout object
        Sensor s = l.newSensor("XS22", "test");
        Assert.assertNotNull("exists", s);

        // try to get nonexistant turnouts
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
    }

    public void testXNetMessages() {
        // prepare an interface, register
        XNetInterfaceScaffold xnis = new XNetInterfaceScaffold(new LenzCommandStation());

        // create and register the manager object
        XNetSensorManager l = new XNetSensorManager(xnis, "X");

        // send messages for feedbak encoder 22
        // notify the XPressNet that somebody else changed it...
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

    public void testAsAbstractFactory() {
        // prepare an interface, register
        XNetInterfaceScaffold xnis = new XNetInterfaceScaffold(new LenzCommandStation());
        // create and register the manager object in a new instance manager
        new jmri.InstanceManager() {
            protected void init() {
                super.init();
                root = null;
            }
        };
        XNetSensorManager l = new XNetSensorManager(xnis, "X");
        jmri.InstanceManager.setSensorManager(l);

        // ask for a Sensor, and check type
        SensorManager t = jmri.InstanceManager.sensorManagerInstance();

        Sensor o = t.newSensor("XS21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received sensor value " + o);
        }
        Assert.assertTrue(null != (XNetSensor) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + t.getBySystemName("XS21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + t.getByUserName("my name"));
        }

        Assert.assertTrue(null != t.getBySystemName("XS21"));
        Assert.assertTrue(null != t.getByUserName("my name"));

    }

    // from here down is testing infrastructure
    public XNetSensorManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetSensorManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XNetSensorManagerTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(XNetSensorManagerTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
