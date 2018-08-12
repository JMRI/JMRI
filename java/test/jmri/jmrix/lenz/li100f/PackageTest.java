package jmri.jmrix.lenz.li100f;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        LI100fAdapterTest.class,
        ConnectionConfigTest.class,
        jmri.jmrix.lenz.li100f.configurexml.PackageTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.lenz.li100f package
 *
 * @author Paul Bender
 */
public class PackageTest  {
}
