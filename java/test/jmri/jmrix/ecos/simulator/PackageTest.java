package jmri.jmrix.ecos.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    EcosSimulatorConnectionConfigTest.class,
    EcosSimulatorAdapterTest.class,
    EcosSimulatorTrafficControllerTest.class,
    jmri.jmrix.easydcc.simulator.configurexml.PackageTest.class,
    BundleTest.class
})
/**
 * Tests for the jmri.jmrix.ecos.simulator package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
