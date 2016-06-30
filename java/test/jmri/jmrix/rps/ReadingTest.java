package jmri.jmrix.rps;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the rps.Reading class.
 *
 * @author	Bob Jacobsen Copyright 2006
 * @version	$Revision$
 */
public class ReadingTest extends TestCase {

    public void testCtorAndID() {
        double[] v = new double[]{0., 1., 2.};
        Reading r = new Reading("21", v);
        Assert.assertEquals("ID ok", "21", r.getID());
    }

    public void testValues() {
        Reading r1 = new Reading("21", new double[]{0., 1., 2.});
        double[] val = r1.getValues();
        Assert.assertEquals("Value 1 array", 1, (int) val[1]);
        Assert.assertEquals("Value 1 call ", 1, (int) r1.getValue(1));
        Assert.assertEquals("Value 2 array", 2, (int) val[2]);
        Assert.assertEquals("Value 2 call ", 2, (int) r1.getValue(2));
    }

    public void testImmutable() {
        Reading r1 = new Reading("21", new double[]{0., 1., 2.});
        double[] val = r1.getValues();
        val[1] = 3.;
        Assert.assertEquals("Value 1 call ", 1, (int) r1.getValue(1));
        Assert.assertEquals("Value 2 call ", 2, (int) r1.getValue(2));
    }

    public void testCopyCtorID() {
        Reading r1 = new Reading("21", new double[]{0., 1., 2.});
        Reading r2 = new Reading(r1);
        Assert.assertEquals("ID ok", "21", r2.getID());
    }

    public void testCopyCtorData() {
        Reading r1 = new Reading("21", new double[]{0., 1., 2.});
        Reading r2 = new Reading(r1);
        Assert.assertEquals("value 1", 1, (int) r2.getValue(1));
    }

    // from here down is testing infrastructure
    public ReadingTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ReadingTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ReadingTest.class);
        return suite;
    }

}
