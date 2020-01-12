package jmri.jmrix.easydcc.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   SimulatorAdapterTest.class,
   jmri.jmrix.easydcc.simulator.configurexml.PackageTest.class,
   BundleTest.class,
   EasyDccSimulatorTrafficControllerTest.class
})
/**
 * Tests for the jmri.jmrix.easydcc.simulator package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
