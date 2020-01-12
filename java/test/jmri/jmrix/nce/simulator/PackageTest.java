package jmri.jmrix.nce.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   jmri.jmrix.nce.simulator.configurexml.PackageTest.class,
   SimulatorAdapterTest.class
})
/**
 * Tests for the jmri.jmrix.nce.simulator package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
