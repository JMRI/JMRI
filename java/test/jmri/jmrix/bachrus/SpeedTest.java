package jmri.jmrix.bachrus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SpeedTest {

    // the class under test has two static functions that perform a simple
    // calculation. No need for setup/teardown or to test a constructor.

    @Test
    public void testMPHToKPH() {
        assertEquals( 1.609344, Speed.mphToKph(1.0f), 0.000001, "1 mph in kph");
    }

    @Test
    public void testKPHtoMPH() {
        assertEquals( 1.0, Speed.kphToMph(1.609344f), 0.000001, "1.608344 kph in mph");
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
