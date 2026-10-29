package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks handle lookup, rename, move and release in the
 * NamedBeanHandleManager.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class NamedBeanHandleManagerLookupTest {

    private NamedBeanHandleManager nbhm;
    private SensorManager sm;

    @Test
    public void testOneHandlePerNameAndBean() {
        Sensor s1 = sm.provideSensor("IS1");
        Sensor s2 = sm.provideSensor("IS2");
        NamedBeanHandle<Sensor> h1 = nbhm.getNamedBeanHandle("IS1", s1);
        assertSame(h1, nbhm.getNamedBeanHandle("IS1", s1), "same name and bean");
        assertNotSame(h1, nbhm.getNamedBeanHandle("North", s1), "other name");
        assertNotSame(h1, nbhm.getNamedBeanHandle("IS1", s2), "other bean");
        assertTrue(nbhm.inUse("IS1", s1));
        assertTrue(nbhm.inUse("North", s1));
        assertFalse(nbhm.inUse("South", s1));
        assertFalse(nbhm.inUse("IS2", s1));
    }

    @Test
    public void testRenameAndMove() {
        Sensor s1 = sm.provideSensor("IS1");
        Sensor s2 = sm.provideSensor("IS2");
        NamedBeanHandle<Sensor> h = nbhm.getNamedBeanHandle("Platform", s1);

        nbhm.renameBean("Platform", "Platform 1", s1);
        assertEquals("Platform 1", h.getName());
        assertSame(h, nbhm.getNamedBeanHandle("Platform 1", s1), "found under new name");
        assertFalse(nbhm.inUse("Platform", s1), "old name no longer used");

        nbhm.moveBean(s1, s2, "Platform 1");
        assertSame(s2, h.getBean());
        assertSame(h, nbhm.getNamedBeanHandle("Platform 1", s2), "found for new bean");
        assertFalse(nbhm.inUse("Platform 1", s1), "old bean no longer used");
    }

    @Test
    public void testHandlesReleasedForDisposedBean() {
        Sensor s1 = sm.provideSensor("IS1");
        Sensor s2 = sm.provideSensor("IS2");
        nbhm.getNamedBeanHandle("IS1", s1);
        nbhm.getNamedBeanHandle("East", s1);
        nbhm.getNamedBeanHandle("IS2", s2);

        sm.deregister(s1);
        s1.dispose();

        assertFalse(nbhm.inUse("IS1", s1), "handle kept for disposed bean");
        assertFalse(nbhm.inUse("East", s1), "handle kept for disposed bean");
        assertTrue(nbhm.inUse("IS2", s2), "handle of other bean kept");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        sm = InstanceManager.getDefault(SensorManager.class);
        nbhm = InstanceManager.getDefault(NamedBeanHandleManager.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
