package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;

/**
 * Abstract Base Class for SensorManager tests in specific jmrix packages.
 * This is not itself a test class, e.g. should not be added to a suite.
 * Instead, this forms the base for test classes,
 * including providing some common tests.
 *
 * @author Bob Jacobsen 2003, 2006, 2008, 2016
 * @author  Paul Bender Copyright(C) 2016
 */
public abstract class AbstractSensorMgrTestBase extends AbstractProvidingManagerTestBase<SensorManager, Sensor> {

    // implementing classes must provide these abstract members:
    //
    abstract public void setUp(); // load l with actual object; create scaffolds as needed

    abstract public String getSystemName(int i);

    protected boolean listenerResult = false;

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
        assertNotNull( l, "Sensor Manager Exists");
    }

    @Test
    public void testDispose() {
        l.dispose();  // all we're really doing here is making sure the method exists
    }

    @Test
    public void testSensorPutGet() {
        listenerResult = false;
        l.addPropertyChangeListener(new Listen());
        // create
        Sensor t = l.newSensor(getSystemName(getNumToTest1()), "mine");
        // check
        assertNotNull( t, "real object returned ");
        assertEquals( t, l.getByUserName("mine"), "user name correct ");
        assertEquals( t, l.getBySystemName(getSystemName(getNumToTest1())),
            "system name correct ");
        assertTrue(listenerResult);
    }

    // Quite a few tests overload this to create their own name process
    @Test
    public void testProvideName() {
        // create
        Sensor t = l.provide("" + getNumToTest1());
        // check
        assertNotNull( t, "real object returned ");
        assertEquals( t, l.getBySystemName(getSystemName(getNumToTest1())),
            "system name correct ");
    }

    @Test
    public void testDelete() {
        // create
        Sensor t = l.provide(getSystemName(getNumToTest1()));

        // two-pass delete, details not really tested
        PropertyVetoException ex = assertThrows( PropertyVetoException.class,
            () -> l.deleteBean(t, "CanDelete"));
        assertNotNull(ex);
        assertEquals( SensorManager.PROPERTY_CAN_DELETE, ex.getPropertyChangeEvent().getPropertyName());

        assertDoesNotThrow( () -> l.deleteBean(t, "DoDelete"));

        // check for bean
        assertNull( l.getBySystemName(getSystemName(getNumToTest1())),
            "no bean");
        // check for lengths
        assertEquals( 0, l.getNamedBeanSet().size());
        assertEquals( 0, l.getObjectCount());

        JUnitAppender.suppressWarnMessageStartsWith("getNamedBeanList");
        JUnitAppender.suppressWarnMessageStartsWith("getSystemNameList");

    }

    @Test
    public void testDefaultSystemName() {
        // create
        Sensor t = l.provideSensor("" + getNumToTest1());
        // check
        assertNotNull( t, "real object returned ");
        assertEquals( t, l.getBySystemName(getSystemName(getNumToTest1())),
            "system name correct ");
    }

    @Test
    public void testProvideFailure() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> l.provideSensor(""));
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: System name must start with \"" + l.getSystemNamePrefix() + "\".");
    }

    @Test
    public void testSettings() {
        l.setDefaultSensorDebounceGoingActive(1234L);
        assertEquals(1234L, l.getDefaultSensorDebounceGoingActive());

        l.setDefaultSensorDebounceGoingInActive(12345L);
        assertEquals(12345L, l.getDefaultSensorDebounceGoingInActive());
    }

    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        Sensor t1 = l.newSensor(getSystemName(getNumToTest1()), "mine");
        assertNotNull( t1, "t1 real object returned ");
        assertEquals( t1, l.getByUserName("mine"), "same by user ");
        assertEquals( t1, l.getBySystemName(getSystemName(getNumToTest1())),
            "same by system ");

        Sensor t2 = l.newSensor(getSystemName(getNumToTest1()), "mine");
        assertNotNull( t2, "t2 real object returned ");
        // check
        assertEquals( t1, t2, "same new ");
    }

    @Test
    public void testMisses() {
        // try to get nonexistant sensors
        assertNull(l.getByUserName("foo"));
        assertNull(l.getBySystemName("bar"));
    }

    @Test
    public void testMoveUserName() {
        Sensor t1 = l.provideSensor("" + getNumToTest1());
        Sensor t2 = l.provideSensor("" + getNumToTest2());
        t1.setUserName("UserName");
        assertEquals(t1, l.getByUserName("UserName"));

        t2.setUserName("UserName");
        assertEquals(t2, l.getByUserName("UserName"));

        assertNull(t1.getUserName());
    }

    @Test
    public void testUpperLower() {  // this is part of testing of (default) normalization
        Sensor t = l.provideSensor("" + getNumToTest2());
        String name = t.getSystemName();

        int prefixLength = l.getSystemPrefix().length()+1;     // 1 for type letter
        String lowerName = name.substring(0, prefixLength)+name.substring(prefixLength, name.length()).toLowerCase();

        assertEquals(t, l.getSensor(lowerName));
    }

    @Test
    public void testRename() {
        // get sensor
        Sensor t1 = l.newSensor(getSystemName(getNumToTest1()), "before");
        assertNotNull( t1, "t1 real object ");
        t1.setUserName("after");
        Sensor t2 = l.getByUserName("after");
        assertEquals( t1, t2, "same object");
        assertNull( l.getByUserName("before"), "no old object");
    }

    @Test
    public void testPullResistanceConfigurable(){
        assertFalse( l.isPullResistanceConfigurable(), "Pull Resistance Configurable");
    }

    @Disabled("Sensor managers doesn't support auto system names")
    @Test
    @Override
    public void testAutoSystemNames() {
    }

    @Test
    public void testGetEntryToolTip(){
        assertNotNull( l.getEntryToolTip(), "getEntryToolTip not null");
        assertTrue( l.getEntryToolTip().length() > 5, "Entry ToolTip Contains text");
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
