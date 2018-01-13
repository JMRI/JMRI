package jmri.util;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class NamedBeanExpectedStateTest {

    public NamedBeanExpectedStateTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test of getExpectedState method, of class NamedBeanExpectedState.
     */
    @Test
    public void testGetExpectedState() {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        NamedBeanExpectedState<Sensor> instance = new NamedBeanExpectedState<>(sm.provideSensor("IS12"), 3);
        Assert.assertEquals(3, instance.getExpectedState().intValue()); // Assert needs both args to be int or Object
    }

    /**
     * Test of setExpectedState method, of class NamedBeanExpectedState.
     */
    @Test
    public void testSetExpectedState() {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        NamedBeanExpectedState<Sensor> instance = new NamedBeanExpectedState<>(sm.provideSensor("IS12"), 3);
        Assert.assertEquals(3, instance.getExpectedState().intValue());
        boolean thrown = false;
        try {
        instance.setExpectedState(null);
        Assert.assertNull(instance.getExpectedState());
        } catch (NullPointerException ex) {
            thrown = true;
        }
        Assert.assertTrue("NPE thrown setting null state", thrown);
        Assert.assertEquals(3, instance.getExpectedState().intValue());
        instance.setExpectedState(5);
        Assert.assertEquals(5, instance.getExpectedState().intValue());
    }

    /**
     * Test of getObject method, of class NamedBeanExpectedState.
     */
    @Test
    public void testGetObject() {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        NamedBeanExpectedState<Sensor> instance = new NamedBeanExpectedState<>(sm.provideSensor("IS12"), 3);
        Assert.assertEquals(sm.getSensor("IS12"), instance.getObject());
    }

    /**
     * Test of getName method, of class NamedBeanExpectedState.
     */
    @Test
    public void testGetName() {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        NamedBeanExpectedState<Sensor> instance = new NamedBeanExpectedState<>(sm.provideSensor("IS12"), 3);
        Assert.assertEquals(sm.getSensor("IS12").getDisplayName(), instance.getName());
    }

    /**
     * Test that NamedBeanExpectedState throws NPE if passed a null NamedBean
     */
    @Test
    public void testNullNamedBean() {
        // JUnit 5 Assert.throwsException() would be nice...
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        boolean thrown = false;
        try {
            new NamedBeanExpectedState<>(null, 3);
        } catch (NullPointerException ex) {
            thrown = true;
        }
        Assert.assertTrue("NPE thrown for null bean", thrown);
        thrown = false;
        try {
            new NamedBeanExpectedState<>(sm.provideSensor("IS12"), null);
        } catch (NullPointerException ex) {
            thrown = true;
        }
        Assert.assertTrue("NPE thrown for null value", thrown);
    }
}
