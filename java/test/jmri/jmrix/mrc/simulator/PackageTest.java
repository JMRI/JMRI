package jmri.jmrix.mrc.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.mrc.simulator.configurexml.PackageTest.class,
   SimulatorAdapterTest.class
})
/**
 * Tests for the jmri.jmrix.mrc.simulator package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
