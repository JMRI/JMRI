package jmri.jmrix.maple.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   SimulatorAdapterTest.class,
   jmri.jmrix.maple.simulator.configurexml.PackageTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.maple.simulator package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
