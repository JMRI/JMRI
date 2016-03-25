// DCCppSensorManagerTest.java
package jmri.jmrix.dccpp;

import jmri.Sensor;
import jmri.SensorManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.dccpp.DCCppSensorManager class.
 *
 * @author	Paul Bender Copyright (c) 2003
 * @author	Mark Underwood Copyright (c) 2003
 */
public class DCCppSensorManagerTest extends TestCase {

    public void testDCCppSensorCreate() {
        // prepare an interface
        DCCppInterfaceScaffold xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        Assert.assertNotNull("exists", xnis);

        // create and register the manager object in a new instance manager
        new jmri.InstanceManager() {
            protected void init() {
                super.init();
                root = null;
            }
        };
        DCCppSensorManager l = new DCCppSensorManager(xnis, "DCCPP");
        jmri.InstanceManager.setSensorManager(l);

    }

    public void testByAddress() {
        // prepare an interface
        DCCppInterfaceScaffold xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        Assert.assertNotNull("exists", xnis);

        // create and register the manager object in a new instance manager
        new jmri.InstanceManager() {
            protected void init() {
                super.init();
                root = null;
            }
        };
        // create and register the manager object
        DCCppSensorManager l = new DCCppSensorManager(xnis, "DCCPP");

        // sample sensor object
        Sensor t = l.newSensor("DCCPPS22", "test");

        // test get
        Assert.assertTrue(t == l.getByUserName("test"));
        Assert.assertTrue(t == l.getBySystemName("DCCPPS22"));
    }

    public void testMisses() {
        // prepare an interface
        DCCppInterfaceScaffold xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        Assert.assertNotNull("exists", xnis);

        // create and register the manager object in a new instance manager
        new jmri.InstanceManager() {
            protected void init() {
                super.init();
                root = null;
            }
        };
        // create and register the manager object
        DCCppSensorManager l = new DCCppSensorManager(xnis, "DCCPP");

        // sample turnout object
        Sensor s = l.newSensor("DCCPPS22", "test");
        Assert.assertNotNull("exists", s);

        // try to get nonexistant turnouts
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
    }

    public void testDCCppMessages() {
        // prepare an interface, register
        DCCppInterfaceScaffold xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());

        // create and register the manager object in a new instance manager
        new jmri.InstanceManager() {
            protected void init() {
                super.init();
                root = null;
            }
        };
        // create and register the manager object
        DCCppSensorManager l = new DCCppSensorManager(xnis, "DCCPP");

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

    public void testAsAbstractFactory() {
        // prepare an interface, register
        DCCppInterfaceScaffold xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        // create and register the manager object in a new instance manager
        new jmri.InstanceManager() {
            protected void init() {
                super.init();
                root = null;
            }
        };
        DCCppSensorManager l = new DCCppSensorManager(xnis, "DCCPP");
        jmri.InstanceManager.setSensorManager(l);

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

    // from here down is testing infrastructure
    public DCCppSensorManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DCCppSensorManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DCCppSensorManagerTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppSensorManagerTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
