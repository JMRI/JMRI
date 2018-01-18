package jmri.jmrix.lenz.xnetsimulator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        XNetSimulatorAdapterTest.class,
        ConnectionConfigTest.class,
        jmri.jmrix.lenz.xnetsimulator.configurexml.PackageTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.lenz.xnetsimulator package
 *
 * @author Paul Bender
 */
public class PackageTest  {
}
