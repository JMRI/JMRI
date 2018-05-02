package jmri.jmrix.lenz.li101;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.lenz.li101 package
 *
 * @author Paul Bender
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    LI101AdapterTest.class,
    ConnectionConfigTest.class,
    jmri.jmrix.lenz.li101.configurexml.PackageTest.class,
    BundleTest.class
})
public class PackageTest {
}
