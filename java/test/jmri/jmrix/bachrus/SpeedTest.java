package jmri.jmrix.bachrus;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SpeedTest {

    // the class under test has two static functions that perform a simple
    // calculation. No need for setup/teardown or to test a constructor.

    @Test
    public void testMPHToKPH() {
        Assert.assertEquals("1 mph in kph", 1.609344, Speed.mphToKph(1.0f), 0.000001);
    }

    @Test
    public void testKPHtoMPH() {
        Assert.assertEquals("1.608344 kph in mph", 1.0, Speed.kphToMph(1.609344f), 0.000001);
    }

}
