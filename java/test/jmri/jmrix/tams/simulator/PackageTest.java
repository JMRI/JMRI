package jmri.jmrix.tams.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.tams.simulator.configurexml.PackageTest.class,
   SimulatorAdapterTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.tams.simulator package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
