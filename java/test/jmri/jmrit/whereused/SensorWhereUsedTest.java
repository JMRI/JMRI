package jmri.jmrit.whereused;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the SensorWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class SensorWhereUsedTest {

    @Test
    public void testSensorWhereUsed() {
        SensorWhereUsed ctor = new SensorWhereUsed();
        Assert.assertNotNull("exists", ctor);
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
