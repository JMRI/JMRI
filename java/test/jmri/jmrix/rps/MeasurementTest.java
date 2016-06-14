package jmri.jmrix.rps;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the rps.Measurement class.
 *
 * @author	Bob Jacobsen Copyright 2006
 * @version	$Revision$
 */
public class MeasurementTest extends TestCase {

    public void testCtorAndID() {
        Reading r = new Reading("21", new double[]{0., 0., 0.});
        Measurement m = new Measurement(r);
        Assert.assertEquals("ID ok", "21", m.getID());
    }

    // from here down is testing infrastructure
    public MeasurementTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {MeasurementTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(MeasurementTest.class);
        return suite;
    }

}
