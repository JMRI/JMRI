package jmri.jmrix.lenz.liusb;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        LIUSBAdapterTest.class,
        LIUSBXNetPacketizerTest.class,
        ConnectionConfigTest.class,
        jmri.jmrix.lenz.liusb.configurexml.PackageTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.lenz.liusb package
 *
 * @author Paul Bender
 */
public class PackageTest  {
}
