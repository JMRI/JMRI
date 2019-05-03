package jmri.jmrix.grapevine.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   ConnectionConfigTest.class,
   SimulatorAdapterTest.class,
   jmri.jmrix.grapevine.simulator.configurexml.PackageTest.class,
   BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.grapevine.simulator package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {

}
