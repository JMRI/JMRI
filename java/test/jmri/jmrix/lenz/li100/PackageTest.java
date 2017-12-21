package jmri.jmrix.lenz.li100;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        LI100AdapterTest.class,
        LI100XNetInitializationManagerTest.class,
        LI100XNetProgrammerTest.class,
        ConnectionConfigTest.class,
        jmri.jmrix.lenz.li100.configurexml.PackageTest.class,
        LI100XNetPacketizerTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.lenz.li100 package
 *
 * @author Paul Bender
 */
public class PackageTest  {
}
