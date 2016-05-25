package jmri.jmrix.rps;

import javax.vecmath.Point3d;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * JUnit tests for the rps.Ash2_1Algorithm class.
 *
 * This algorithm tends to pick arbitrary solutions with only three sensors, so
 * we test with four and more.
 *
 * The default transmitter location for the 7, 13, 13, 13 readings is (0,0,12)
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version	$Revision$
 */
public class Ash2_1AlgorithmTest extends AbstractAlgorithmTest {

    Calculator getAlgorithm(Point3d[] pts, double vs) {
        return new Ash2_1Algorithm(pts, vs);
    }

    // from here down is testing infrastructure
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {Ash2_1AlgorithmTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(Ash2_1AlgorithmTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
