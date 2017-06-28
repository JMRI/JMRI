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
    ConnectionConfigTest.class,
    jmri.jmrix.roco.z21.simulator.configurexml.PackageTest.class,
    Z21SimulatorLocoDataTest.class,
    Z21SimulatorTest.class
})
public class PackageTest {
}
