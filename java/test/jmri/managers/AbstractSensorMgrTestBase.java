/**
 * This is not itself a test class, e.g. should not be added to a suite.
 * Instead, this forms the base for test classes, including providing some
 * common tests.
 */
package jmri.managers;

import java.beans.PropertyChangeListener;
import jmri.Sensor;
import jmri.SensorManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Abstract Base Class for SensorManager tests in specific jmrix packages. This
 * is not itself a test class, e.g. should not be added to a suite. Instead,
 * this forms the base for test classes, including providing some common tests
 *
 * @author	Bob Jacobsen 2003, 2006, 2008, 2016
 * @author      Paul Bender Copyright(C) 2016
 */
public abstract class AbstractSensorMgrTestBase {

    // implementing classes must provide these abstract members:
    //
    @Before
    abstract public void setUp();    	// load t with actual object; create scaffolds as needed

    abstract public String getSystemName(int i);

    protected SensorManager l = null;	// holds objects under test

    static protected boolean listenerResult = false;

    protected class Listen implements PropertyChangeListener {
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            listenerResult = true;
        }
    }

    // start of common tests
    // test creation - real work is in the setup() routine
    @Test
    public void testCreate() {
    }

    @Test
    public void testDispose() {
        l.dispose();  // all we're really doing here is making sure the method exists
    }

    @Test
    public void testSensorPutGet() {
        // create
        Sensor t = l.newSensor(getSystemName(getNumToTest1()), "mine");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("user name correct ", t == l.getByUserName("mine"));
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testDefaultSystemName() {
        // create
        Sensor t = l.provideSensor("" + getNumToTest1());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testProvideFailure() {
        l.provideSensor("");
    }

    @Test
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

    @Test
    public void testMisses() {
        // try to get nonexistant sensors
        Assert.assertTrue(null == l.getByUserName("foo"));
        Assert.assertTrue(null == l.getBySystemName("bar"));
    }

    @Test
    public void testMoveUserName() {
        Sensor t1 = l.provideSensor("" + getNumToTest1());
        Sensor t2 = l.provideSensor("" + getNumToTest2());
        t1.setUserName("UserName");
        Assert.assertTrue(t1 == l.getByUserName("UserName"));

        t2.setUserName("UserName");
        Assert.assertTrue(t2 == l.getByUserName("UserName"));

        Assert.assertTrue(null == t1.getUserName());
    }

    @Test
    public void testUpperLower() {
        Sensor t = l.provideSensor("" + getNumToTest2());
        String name = t.getSystemName();
        Assert.assertNull(l.getSensor(name.toLowerCase()));
    }

    @Test
    public void testRename() {
        // get sensor
        Sensor t1 = l.newSensor(getSystemName(getNumToTest1()), "before");
        Assert.assertNotNull("t1 real object ", t1);
        t1.setUserName("after");
        Sensor t2 = l.getByUserName("after");
        Assert.assertEquals("same object", t1, t2);
        Assert.assertEquals("no old object", null, l.getByUserName("before"));
    }

    @Test
    public void testPullResistanceConfigurable(){
       Assert.assertFalse("Pull Resistance Configurable",l.isPullResistanceConfigurable());
    }

    /**
     * Number of sensor to test. Made a separate method so it can be overridden
     * in subclasses that do or don't support various numbers
     */
    protected int getNumToTest1() {
        return 9;
    }

    protected int getNumToTest2() {
        return 7;
    }
}
