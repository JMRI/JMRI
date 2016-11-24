package jmri.jmrix.rps.algorithms;

import jmri.jmrix.rps.Ash2_0AlgorithmTest;
import jmri.jmrix.rps.Ash2_1AlgorithmTest;
import jmri.jmrix.rps.Ash2_2AlgorithmTest;
import jmri.jmrix.rps.InitialAlgorithmTest;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test all the RPS algorithms.
 *
 * Separated from RpsTest to make it easy to run just the algorithms, not all
 * the package tests.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.rps.algorithms");

        suite.addTest(InitialAlgorithmTest.suite());

        // suite.addTest(Ash1_0AlgorithmTest.suite());
        // suite.addTest(Ash1_1AlgorithmTest.suite());
        suite.addTest(Ash2_0AlgorithmTest.suite());
        suite.addTest(Ash2_1AlgorithmTest.suite());
        suite.addTest(Ash2_2AlgorithmTest.suite());

        // suite.addTest(Analytic_AAlgorithmTest.suite());
        return suite;
    }

}
