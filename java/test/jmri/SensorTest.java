package jmri;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the Light class
 *
 * @author Bob Jacobsen Copyright (C) 2008, 2010
 */
public class SensorTest {

    @Test
    @SuppressWarnings("all")
    public void testStateConstants() {
        assertTrue( (Sensor.ON & Sensor.OFF) == 0, "On and Off differ");
        assertTrue( (Sensor.ON & Sensor.UNKNOWN) == 0, "On and Unknown differ");
        assertTrue( (Sensor.OFF & Sensor.UNKNOWN) == 0, "Off and Unknown differ");
        assertTrue( (Sensor.ON & Sensor.INCONSISTENT) == 0, "On and Inconsistent differ");
        assertTrue( (Sensor.OFF & Sensor.INCONSISTENT) == 0, "Off and Inconsistent differ");
    }
}
