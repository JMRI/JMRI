package jmri.jmrix.sprog.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.sprog.simulator package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
   SimulatorAdapterTest.class,
   ConnectionConfigTest.class,
   jmri.jmrix.sprog.simulator.configurexml.PackageTest.class,
   BundleTest.class
})

public class PackageTest {
}
