package jmri.jmrix.rps.algorithms;

import jmri.jmrix.rps.Analytic_AAlgorithmTest;
import jmri.jmrix.rps.Ash1_0AlgorithmTest;
import jmri.jmrix.rps.Ash1_1AlgorithmTest;
import jmri.jmrix.rps.Ash2_0AlgorithmTest;
import jmri.jmrix.rps.Ash2_1AlgorithmTest;
import jmri.jmrix.rps.Ash2_2AlgorithmTest;
import jmri.jmrix.rps.InitialAlgorithmTest;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;


/**
 * Test all the RPS algorithms.
 *
 * Separated from RpsTest to make it easy to run just the algorithms, not all
 * the package tests.
 *
 * @author Bob Jacobsen Copyright 2008
 */
@RunWith(JUnitPlatform.class)
@SelectPackages("jmri.jmrix.rps.algorithms")
public class PackageTest {
}
