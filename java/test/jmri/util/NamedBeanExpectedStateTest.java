package jmri.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class NamedBeanExpectedStateTest {

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
     * @throws java.lang.Exception on test error.
     */
    @Test
    public void testSetExpectedState() throws Exception {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        NamedBeanExpectedState<Sensor> instance = new NamedBeanExpectedState<>(sm.provideSensor("IS12"), 3);
        Assert.assertEquals(3, instance.getExpectedState().intValue());

        Exception ex = Assertions.assertThrows(NullPointerException.class, () -> {
            setStateToNull(instance);
        });
        Assertions.assertNotNull(ex,"NPE thrown setting null state");

        Assert.assertEquals(3, instance.getExpectedState().intValue());
        instance.setExpectedState(5);
        Assert.assertEquals(5, instance.getExpectedState().intValue());
    }

    @SuppressWarnings("null")
    @SuppressFBWarnings( value = "NP_NONNULL_PARAM_VIOLATION",
        justification = "testing passing null to create exception ")
    private void setStateToNull(NamedBeanExpectedState<?> instance){
        instance.setExpectedState(null);
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
        Assert.assertEquals(sm.provideSensor("IS12").getDisplayName(), instance.getName());
    }

    /**
     * Test that NamedBeanExpectedState throws NPE if passed a null NamedBean
     */
    @Test
    public void testNullNamedBean() {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);

        Exception ex = Assertions.assertThrows(NullPointerException.class, () -> {
            cTorNpeNullBean();
        });
        Assertions.assertNotNull(ex,"NPE thrown for null bean");

        ex = Assertions.assertThrows(NullPointerException.class, () -> {
            provideValidSensorNullState(sm);
        });
        Assertions.assertNotNull(ex,"NPE thrown for null state value");
    }

    @SuppressFBWarnings( value = "NP_NONNULL_PARAM_VIOLATION",
        justification = "testing passing null to create exception ")
    private void cTorNpeNullBean() {
        Assertions.assertNotNull(new NamedBeanExpectedState<>(null, 3));
    }

    @SuppressFBWarnings( value = "NP_NONNULL_PARAM_VIOLATION",
        justification = "testing passing null to create exception ")
    private void provideValidSensorNullState(SensorManager sm){
        Assertions.assertNotNull(new NamedBeanExpectedState<>(sm.provideSensor("IS12"), null));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
