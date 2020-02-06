package jmri.jmrit.whereused;

import org.junit.*;

import jmri.util.JUnitUtil;

/**
 * Tests for the SensorWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class SensorWhereUsedTest {

    @Test
    public void testCreate() {
        jmri.Sensor sensor = InstanceMaanger.getDefault(SensorManager.class).provideSensor("IT101");
        String result = SensorWhereUsed.checkTurnouts(sensor);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
