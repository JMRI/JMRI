package jmri.jmrix.rps.algorithms;

import jmri.jmrix.rps.Analytic_AAlgorithmTest;
import jmri.jmrix.rps.Ash1_0AlgorithmTest;
import jmri.jmrix.rps.Ash1_1AlgorithmTest;
import jmri.jmrix.rps.Ash2_0AlgorithmTest;
import jmri.jmrix.rps.Ash2_1AlgorithmTest;
import jmri.jmrix.rps.Ash2_2AlgorithmTest;
import jmri.jmrix.rps.InitialAlgorithmTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test all the RPS algorithms.
 *
 * Separated from RpsTest to make it easy to run just the algorithms, not all
 * the package tests.
 *
 * @author Bob Jacobsen Copyright 2008
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    InitialAlgorithmTest.class,
    Ash1_0AlgorithmTest.class,
    Ash1_1AlgorithmTest.class,
    Ash2_0AlgorithmTest.class,
    Ash2_1AlgorithmTest.class,
    Ash2_2AlgorithmTest.class,
    Analytic_AAlgorithmTest.class
})
public class PackageTest {
}
