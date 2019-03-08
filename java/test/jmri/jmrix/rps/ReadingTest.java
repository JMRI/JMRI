package jmri.jmrix.rps;

import org.junit.Test;
import org.junit.Assert;

/**
 * JUnit tests for the rps.Reading class.
 *
 * @author Bob Jacobsen Copyright 2006
 */
public class ReadingTest {

    @Test
    public void testCtorAndID() {
        double[] v = new double[]{0., 1., 2.};
        Reading r = new Reading("21", v);
        Assert.assertEquals("ID ok", "21", r.getId());
    }

    @Test
    public void testValues() {
        Reading r1 = new Reading("21", new double[]{0., 1., 2.});
        double[] val = r1.getValues();
        Assert.assertEquals("Value 1 array", 1, (int) val[1]);
        Assert.assertEquals("Value 1 call ", 1, (int) r1.getValue(1));
        Assert.assertEquals("Value 2 array", 2, (int) val[2]);
        Assert.assertEquals("Value 2 call ", 2, (int) r1.getValue(2));
    }

    @Test
    public void testImmutable() {
        Reading r1 = new Reading("21", new double[]{0., 1., 2.});
        double[] val = r1.getValues();
        val[1] = 3.;
        Assert.assertEquals("Value 1 call ", 1, (int) r1.getValue(1));
        Assert.assertEquals("Value 2 call ", 2, (int) r1.getValue(2));
    }

    @Test
    public void testCopyCtorID() {
        Reading r1 = new Reading("21", new double[]{0., 1., 2.});
        Reading r2 = new Reading(r1);
        Assert.assertEquals("ID ok", "21", r2.getId());
    }

    @Test
    public void testCopyCtorData() {
        Reading r1 = new Reading("21", new double[]{0., 1., 2.});
        Reading r2 = new Reading(r1);
        Assert.assertEquals("value 1", 1, (int) r2.getValue(1));
    }

}
