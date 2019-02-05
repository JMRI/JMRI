package jmri.jmrix.dccpp.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.dccpp.simulator.configurexml.PackageTest.class,
   DCCppSimulatorAdapterTest.class,
   BundleTest.class
})
/**
 * Tests for the jmri.jmrix.dccpp.simulator package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
