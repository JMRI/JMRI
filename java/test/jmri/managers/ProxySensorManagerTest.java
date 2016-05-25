package jmri.managers;

import java.beans.PropertyChangeListener;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the ProxySensorManager
 *
 * @author	Bob Jacobsen 2003, 2006, 2008, 2014
 * @version	$Revision$
 */
public class ProxySensorManagerTest extends TestCase {

    public String getSystemName(int i) {
        return "JS" + i;
    }

    protected SensorManager l = null;	// holds objects under test

    static protected boolean listenerResult = false;

    protected class Listen implements PropertyChangeListener {

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            listenerResult = true;
        }
    }

    public void testDispose() {
        l.dispose();  // all we're really doing here is making sure the method exists
    }

    public void testPutGet() {
        // create
        Sensor t = l.newSensor(getSystemName(getNumToTest1()), "mine");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("user name correct ", t == l.getByUserName("mine"));
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    public void testDefaultSystemName() {
        // create
        Sensor t = l.provideSensor("" + getNumToTest1());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    public void testSingleObject() {
        // test that you always get the same representation
        Sensor t1 = l.newSensor(getSystemName(getNumToTest1()), "mine");
        Assert.assertTrue("t1 real object returned ", t1 != null);
        Assert.assertTrue("same by user ", t1 == l.getByUserName("mine"));
        Assert.assertTrue("same by system ", t1 == l.getBySystemName(getSystemName(getNumToTest1())));

        Sensor t2 = l.newSensor(getSystemName(getNumToTest1()), "mine");
        Assert.assertTrue("t2 real object returned ", t2 != null);
        // check
        Assert.assertTrue("same new ", t1 == t2);
    }

    public void testMisses() {
        // try to get nonexistant objects
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
    }

    public void testUpperLower() {
        Sensor t = l.provideSensor("" + getNumToTest2());
        String name = t.getSystemName();
        Assert.assertNull(l.getSensor(name.toLowerCase()));
    }

    public void testRename() {
        // get 
        Sensor t1 = l.newSensor(getSystemName(getNumToTest1()), "before");
        Assert.assertNotNull("t1 real object ", t1);
        t1.setUserName("after");
        Sensor t2 = l.getByUserName("after");
        Assert.assertEquals("same object", t1, t2);
        Assert.assertEquals("no old object", null, l.getByUserName("before"));
    }

    public void testTwoNames() {
        Sensor jl212 = l.provideSensor("JS212");
        Sensor jl211 = l.provideSensor("JS211");

        Assert.assertNotNull(jl212);
        Assert.assertNotNull(jl211);
        Assert.assertTrue(jl212 != jl211);
    }

    public void testDefaultNotInternal() {
        Sensor lut = l.provideSensor("211");

        Assert.assertNotNull(lut);
        Assert.assertEquals("JS211", lut.getSystemName());
    }

    public void testProvideUser() {
        Sensor l1 = l.provideSensor("211");
        l1.setUserName("user 1");
        Sensor l2 = l.provideSensor("user 1");
        Sensor l3 = l.getSensor("user 1");

        Assert.assertNotNull(l1);
        Assert.assertNotNull(l2);
        Assert.assertNotNull(l3);
        Assert.assertEquals(l1, l2);
        Assert.assertEquals(l3, l2);
        Assert.assertEquals(l1, l3);

        Sensor l4 = l.getSensor("JLuser 1");
        Assert.assertNull(l4);
    }

    public void testInstanceManagerIntegration() {
        jmri.util.JUnitUtil.resetInstanceManager();
        Assert.assertNotNull(InstanceManager.getDefault(SensorManager.class));

        jmri.util.JUnitUtil.initInternalSensorManager();

        Assert.assertTrue(InstanceManager.getDefault(SensorManager.class) instanceof ProxySensorManager);

        Assert.assertNotNull(InstanceManager.getDefault(SensorManager.class));
        Assert.assertNotNull(InstanceManager.getDefault(SensorManager.class).provideSensor("IS1"));

        InternalSensorManager m = new InternalSensorManager() {
            public String getSystemPrefix() {
                return "J";
            }
        };
        InstanceManager.setSensorManager(m);

        Assert.assertNotNull(InstanceManager.getDefault(SensorManager.class).provideSensor("JS1"));
        Assert.assertNotNull(InstanceManager.getDefault(SensorManager.class).provideSensor("IS2"));
    }

    /**
     * Number of unit to test. Made a separate method so it can be overridden in
     * subclasses that do or don't support various numbers
     */
    protected int getNumToTest1() {
        return 9;
    }

    protected int getNumToTest2() {
        return 7;
    }

    // from here down is testing infrastructure
    public ProxySensorManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ProxySensorManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ProxySensorManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        // create and register the manager object
        l = new InternalSensorManager() {
            public String getSystemPrefix() {
                return "J";
            }
        };
        jmri.InstanceManager.setSensorManager(l);
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
