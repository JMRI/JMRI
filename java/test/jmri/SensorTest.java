package jmri;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the Light class
 *
 * @author	Bob Jacobsen Copyright (C) 2008, 2010
 */
public class SensorTest extends TestCase {

    @SuppressWarnings("all")
    public void testStateConstants() {
        Assert.assertTrue("On and Off differ", (Sensor.ON & Sensor.OFF) == 0);
        Assert.assertTrue("On and Unknown differ", (Sensor.ON & Sensor.UNKNOWN) == 0);
        Assert.assertTrue("Off and Unknown differ", (Sensor.OFF & Sensor.UNKNOWN) == 0);
        Assert.assertTrue("On and Inconsistent differ", (Sensor.ON & Sensor.INCONSISTENT) == 0);
        Assert.assertTrue("Off and Inconsistent differ", (Sensor.OFF & Sensor.INCONSISTENT) == 0);
    }

    // from here down is testing infrastructure
    public SensorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SensorTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SensorTest.class);
        return suite;
    }

}
