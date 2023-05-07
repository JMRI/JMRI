package jmri.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.Sensor;
import jmri.SensorManager;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class NamedBeanExpectedValueTest {

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
        instance.setExpectedState(sm.provideSensor("IS99"));
        Assert.assertEquals(sm.provideSensor("IS99"), instance.getExpectedState());
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
        Assert.assertEquals(mm.provideMemory("IMTEST").getDisplayName(), instance.getName());
    }

    /**
     * Test that NamedBeanExpectedValue throws NPE if passed a null NamedBean
     */
    @Test
    public void testNullNamedBean() {
        // JUnit 5 Assert.throwsException() would be nice...
        MemoryManager mm = InstanceManager.getDefault(MemoryManager.class);
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        
        Exception exc = Assertions.assertThrows(NullPointerException.class, () -> {
            cTorNpeNullBean(sm);
        });
        Assertions.assertNotNull(exc, "NPE thrown for null bean");
        
        Assertions.assertDoesNotThrow(() -> {
            Assertions.assertNotNull(new NamedBeanExpectedValue<>(mm.provideMemory("IMTEST"), null));
        },"NPE should not be thrown here for a null value");
    }

    @SuppressFBWarnings( value = "NP_NONNULL_PARAM_VIOLATION",
        justification = "testing passing null to create exception ")
    private void cTorNpeNullBean(SensorManager sm) {
        Assertions.assertNotNull(new NamedBeanExpectedValue<>(null, sm.provideSensor("IS12")));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
