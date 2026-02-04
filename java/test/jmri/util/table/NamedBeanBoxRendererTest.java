package jmri.util.table;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the NamedBeanBoxRendererTest class.
 * @author Steve Young Copyright (C) 2024
 */
public class NamedBeanBoxRendererTest {

    @Test
    public void testNamedBeanBoxRendererCtor() {
        var t = new NamedBeanBoxRenderer<jmri.Sensor>(jmri.InstanceManager.getDefault(jmri.SensorManager.class));
        Assertions.assertNotNull(t);
        t.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
