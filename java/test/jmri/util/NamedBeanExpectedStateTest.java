package jmri.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertEquals(3, instance.getExpectedState().intValue()); // Assert needs both args to be int or Object
    }

    /**
     * Test of setExpectedState method, of class NamedBeanExpectedState.
     */
    @Test
    public void testSetExpectedState() {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        NamedBeanExpectedState<Sensor> instance = new NamedBeanExpectedState<>(sm.provideSensor("IS12"), 3);
        assertEquals(3, instance.getExpectedState().intValue());

        Exception ex = assertThrows(NullPointerException.class, () -> {
            setStateToNull(instance);
        });
        assertNotNull(ex,"NPE thrown setting null state");

        assertEquals(3, instance.getExpectedState().intValue());
        instance.setExpectedState(5);
        assertEquals(5, instance.getExpectedState().intValue());
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
        assertEquals(sm.getSensor("IS12"), instance.getObject());
    }

    /**
     * Test of getName method, of class NamedBeanExpectedState.
     */
    @Test
    public void testGetName() {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        NamedBeanExpectedState<Sensor> instance = new NamedBeanExpectedState<>(sm.provideSensor("IS12"), 3);
        assertEquals(sm.provideSensor("IS12").getDisplayName(), instance.getName());
    }

    /**
     * Test that NamedBeanExpectedState throws NPE if passed a null NamedBean
     */
    @Test
    public void testNullNamedBean() {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);

        Exception ex = assertThrows(NullPointerException.class, () -> {
            cTorNpeNullBean();
        });
        assertNotNull(ex,"NPE thrown for null bean");

        ex = assertThrows(NullPointerException.class, () -> {
            provideValidSensorNullState(sm);
        });
        assertNotNull(ex,"NPE thrown for null state value");
    }

    @SuppressFBWarnings( value = "NP_NONNULL_PARAM_VIOLATION",
        justification = "testing passing null to create exception ")
    private void cTorNpeNullBean() {
        assertNotNull(new NamedBeanExpectedState<>(null, 3));
    }

    @SuppressFBWarnings( value = "NP_NONNULL_PARAM_VIOLATION",
        justification = "testing passing null to create exception ")
    private void provideValidSensorNullState(SensorManager sm){
        assertNotNull(new NamedBeanExpectedState<>(sm.provideSensor("IS12"), null));
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
