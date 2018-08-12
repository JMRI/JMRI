package jmri.jmrix.lenz.ztc640;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ZTC640AdapterTest.class,
        ZTC640XNetPacketizerTest.class,
        ConnectionConfigTest.class,
        jmri.jmrix.lenz.ztc640.configurexml.PackageTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.lenz.ztc640 package
 *
 * @author Paul Bender
 */
public class PackageTest  {
}
