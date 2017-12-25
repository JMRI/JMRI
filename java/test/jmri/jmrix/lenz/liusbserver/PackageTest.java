package jmri.jmrix.lenz.liusbserver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        LIUSBServerAdapterTest.class,
        LIUSBServerXNetPacketizerTest.class,
        ConnectionConfigTest.class,
        jmri.jmrix.lenz.liusbserver.configurexml.PackageTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.lenz.liusbserver package
 *
 * @author Paul Bender
 */
public class PackageTest  {
}
