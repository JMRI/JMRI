package jmri.jmrix.oaktree.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.oaktree.simulator.configurexml.PackageTest.class,
   SimulatorAdapterTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.oaktree.serialdriver package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
