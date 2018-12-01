package jmri.jmrix.roco.z21.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.roco.z21.simulator package
 *
 * @author Paul Bender
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    Z21SimulatorAdapterTest.class,
    Z21XNetSimulatorAdapterTest.class,
    Z21SimulatorConnectionConfigTest.class,
    jmri.jmrix.roco.z21.simulator.configurexml.PackageTest.class,
    Z21SimulatorLocoDataTest.class,
    BundleTest.class
})
public class PackageTest {
}
