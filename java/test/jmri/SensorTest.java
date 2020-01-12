package jmri;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Light class
 *
 * @author	Bob Jacobsen Copyright (C) 2008, 2010
 */
public class SensorTest {

    @Test
    @SuppressWarnings("all")
    public void testStateConstants() {
        Assert.assertTrue("On and Off differ", (Sensor.ON & Sensor.OFF) == 0);
        Assert.assertTrue("On and Unknown differ", (Sensor.ON & Sensor.UNKNOWN) == 0);
        Assert.assertTrue("Off and Unknown differ", (Sensor.OFF & Sensor.UNKNOWN) == 0);
        Assert.assertTrue("On and Inconsistent differ", (Sensor.ON & Sensor.INCONSISTENT) == 0);
        Assert.assertTrue("Off and Inconsistent differ", (Sensor.OFF & Sensor.INCONSISTENT) == 0);
    }
}
