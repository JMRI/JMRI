package jmri.jmrit.sensorgroup;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for classes in the SensorGroup class
 *
 * @author Bob Jacobsen Copyright 2003, 2007
 * @author Paul Bender Copyright (C) 2017
 */
public class SensorGroupTest {

    @Test
    public void testCTor() {
        SensorGroup t = new SensorGroup("test");
        Assertions.assertNotNull(t, "exists");
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
