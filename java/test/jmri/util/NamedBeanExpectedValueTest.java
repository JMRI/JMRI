package jmri.util;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
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
public class NamedBeanExpectedValueTest {

    public NamedBeanExpectedValueTest() {
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
        JUnitUtil.initMemoryManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test of getExpectedState method, of class NamedBeanExpectedValue.
     */
    @Test
    public void testGetExpectedState() {
        MemoryManager mm = InstanceManager.getDefault(MemoryManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        NamedBeanExpectedValue<Memory, Sensor> instance = new NamedBeanExpectedValue<>(mm.provideMemory("IMTEST"), sm.provideSensor("IS12"));
        Assert.assertEquals(sm.getSensor("IS12"), instance.getExpectedState());
    }

    /**
     * Test of setExpectedState method, of class NamedBeanExpectedValue.
     */
    @Test
    public void testSetExpectedState() {
        MemoryManager mm = InstanceManager.getDefault(MemoryManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        NamedBeanExpectedValue<Memory, Sensor> instance = new NamedBeanExpectedValue<>(mm.provideMemory("IMTEST"), sm.provideSensor("IS12"));
        Assert.assertEquals(sm.getSensor("IS12"), instance.getExpectedState());
        instance.setExpectedState(null);
        Assert.assertNull(instance.getExpectedState());
        Assert.assertNotEquals(sm.getSensor("IS12"), instance.getExpectedState());
        instance.setExpectedState(sm.provideSensor("IS21"));
        Assert.assertEquals(sm.getSensor("IS21"), instance.getExpectedState());
    }

    /**
     * Test of getObject method, of class NamedBeanExpectedValue.
     */
    @Test
    public void testGetObject() {
        MemoryManager mm = InstanceManager.getDefault(MemoryManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        NamedBeanExpectedValue<Memory, Sensor> instance = new NamedBeanExpectedValue<>(mm.provideMemory("IMTEST"), sm.provideSensor("IS12"));
        Assert.assertEquals(mm.getMemory("IMTEST"), instance.getObject());
    }

    /**
     * Test of getName method, of class NamedBeanExpectedValue.
     */
    @Test
    public void testGetName() {
        MemoryManager mm = InstanceManager.getDefault(MemoryManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        NamedBeanExpectedValue<Memory, Sensor> instance = new NamedBeanExpectedValue<>(mm.provideMemory("IMTEST"), sm.provideSensor("IS12"));
        Assert.assertEquals(mm.getMemory("IMTEST").getDisplayName(), instance.getName());
    }

    /**
     * Test that NamedBeanExpectedValue throws NPE if passed a null NamedBean
     */
    @Test
    public void testNullNamedBean() {
        // JUnit 5 Assert.throwsException() would be nice...
        MemoryManager mm = InstanceManager.getDefault(MemoryManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        boolean thrown = false;
        try {
            NamedBeanExpectedValue<Memory, Sensor> instance = new NamedBeanExpectedValue<>(null, sm.provideSensor("IS12"));
        } catch (NullPointerException ex) {
            thrown = true;
        }
        Assert.assertTrue("NPE thrown for null bean", thrown);
        thrown = false;
        try {
            NamedBeanExpectedValue<Memory, Sensor> instance = new NamedBeanExpectedValue<>(mm.provideMemory("IMTEST"), null);
        } catch (NullPointerException ex) {
            thrown = true;
        }
        Assert.assertFalse("NPE thrown for null value", thrown);
    }
}
