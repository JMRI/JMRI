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
import org.junit.Ignore;
import org.junit.Test;

/**
 * Abstract Base Class for SensorManager tests in specific jmrix packages. This
 * is not itself a test class, e.g. should not be added to a suite. Instead,
 * this forms the base for test classes, including providing some common tests
 *
 * @author	Bob Jacobsen 2003, 2006, 2008, 2016
 * @author      Paul Bender Copyright(C) 2016
 */
public abstract class AbstractSensorMgrTestBase extends AbstractProvidingManagerTestBase<SensorManager, Sensor> {

    // implementing classes must provide these abstract members:
    //
    @Before
    abstract public void setUp();    	// load l with actual object; create scaffolds as needed

    abstract public String getSystemName(int i);

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
       Assert.assertNotNull("Sensor Manager Exists",l);
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
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("user name correct ", t, l.getByUserName("mine"));
        Assert.assertEquals("system name correct ", t, l.getBySystemName(getSystemName(getNumToTest1())));
    }

    // Quite a few tests overload this to create their own name process
    @Test
    public void testProvideName() {
        // create
        Sensor t = l.provide("" + getNumToTest1());
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("system name correct ", t, l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testDelete() {
        // create
        Sensor t = l.provide(getSystemName(getNumToTest1()));
        
        // two-pass delete, details not really tested
        
        try {
            l.deleteBean(t, "CanDelete");
        } catch (java.beans.PropertyVetoException e) {}
        try {
            l.deleteBean(t, "DoDelete");
        } catch (java.beans.PropertyVetoException e) {}
        
        // check for bean
        Assert.assertNull("no bean", l.getBySystemName(getSystemName(getNumToTest1())));
        // check for lengths
        Assert.assertEquals(0, l.getNamedBeanList().size());
        Assert.assertEquals(0, l.getNamedBeanSet().size());
        Assert.assertEquals(0, l.getSystemNameList().size());
        Assert.assertEquals(0, l.getSystemNameArray().length);
        jmri.util.JUnitAppender.suppressWarnMessage("Manager#getSystemNameArray() is deprecated");
        Assert.assertEquals(0, l.getObjectCount());
    }

    @Test
    public void testDefaultSystemName() {
        // create
        Sensor t = l.provideSensor("" + getNumToTest1());
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("system name correct ", t, l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testProvideFailure() {
        try {
            l.provideSensor("");
        } catch (IllegalArgumentException ex) {
            jmri.util.JUnitAppender.assertErrorMessage("Invalid system name for Sensor: System name must start with \"" + l.getSystemNamePrefix() + "\".");
            throw ex;
        }
    }

    @Test
    public void testSettings() {
        l.setDefaultSensorDebounceGoingActive(1234L);
        Assert.assertEquals(1234L, l.getDefaultSensorDebounceGoingActive());

        l.setDefaultSensorDebounceGoingInActive(12345L);
        Assert.assertEquals(12345L, l.getDefaultSensorDebounceGoingInActive());
    }

    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        Sensor t1 = l.newSensor(getSystemName(getNumToTest1()), "mine");
        Assert.assertNotNull("t1 real object returned ", t1);
        Assert.assertEquals("same by user ", t1, l.getByUserName("mine"));
        Assert.assertEquals("same by system ", t1, l.getBySystemName(getSystemName(getNumToTest1())));

        Sensor t2 = l.newSensor(getSystemName(getNumToTest1()), "mine");
        Assert.assertNotNull("t2 real object returned ", t2);
        // check
        Assert.assertEquals("same new ", t1, t2);
    }

    @Test
    public void testMisses() {
        // try to get nonexistant sensors
        Assert.assertNull(l.getByUserName("foo"));
        Assert.assertNull(l.getBySystemName("bar"));
    }

    @Test
    public void testMoveUserName() {
        Sensor t1 = l.provideSensor("" + getNumToTest1());
        Sensor t2 = l.provideSensor("" + getNumToTest2());
        t1.setUserName("UserName");
        Assert.assertEquals(t1, l.getByUserName("UserName"));

        t2.setUserName("UserName");
        Assert.assertEquals(t2, l.getByUserName("UserName"));

        Assert.assertNull(t1.getUserName());
    }

    @Test
    public void testUpperLower() {  // this is part of testing of (default) normalization
        Sensor t = l.provideSensor("" + getNumToTest2());
        String name = t.getSystemName();
        
        int prefixLength = l.getSystemPrefix().length()+1;     // 1 for type letter
        String lowerName = name.substring(0, prefixLength)+name.substring(prefixLength, name.length()).toLowerCase();
        
        Assert.assertEquals(t, l.getSensor(lowerName));
    }

    @Test
    public void testRename() {
        // get sensor
        Sensor t1 = l.newSensor(getSystemName(getNumToTest1()), "before");
        Assert.assertNotNull("t1 real object ", t1);
        t1.setUserName("after");
        Sensor t2 = l.getByUserName("after");
        Assert.assertEquals("same object", t1, t2);
        Assert.assertNull("no old object", l.getByUserName("before"));
    }

    @Test
    public void testPullResistanceConfigurable(){
       Assert.assertFalse("Pull Resistance Configurable", l.isPullResistanceConfigurable());
    }

    @Ignore("Sensor managers doesn't support auto system names")
    @Test
    @Override
    public void testAutoSystemNames() {
    }

    /**
     * Number of sensor to test. Made a separate method so it can be overridden
     * in subclasses that do or don't support various numbers
     * @return the number to test
     */
    protected int getNumToTest1() {
        return 9;
    }

    protected int getNumToTest2() {
        return 7;
    }

}
