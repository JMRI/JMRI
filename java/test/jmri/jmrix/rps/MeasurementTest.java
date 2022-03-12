package jmri.jmrix.rps;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the rps.Measurement class.
 *
 * @author Bob Jacobsen Copyright 2006
 */
public class MeasurementTest {

    @Test
    public void testCtorAndID() {
        Reading r = new Reading("21", new double[]{0., 0., 0.});
        Measurement m = new Measurement(r);
        Assert.assertEquals("ID ok", "21", m.getId());
    }

}
