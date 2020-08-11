package jmri.jmrit.sensorgroup;

import jmri.util.JUnitUtil;

import org.junit.Assert;
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
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
