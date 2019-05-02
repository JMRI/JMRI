package jmri.jmrix.secsi.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.secsi.simulator.configurexml.PackageTest.class,
   SimulatorAdapterTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.secsi.serialdriver package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
