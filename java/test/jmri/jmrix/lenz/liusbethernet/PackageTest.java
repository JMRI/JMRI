package jmri.jmrix.lenz.liusbethernet;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        LIUSBEthernetAdapterTest.class,
        LIUSBEthernetXNetPacketizerTest.class,
        ConnectionConfigTest.class,
        jmri.jmrix.lenz.liusbethernet.configurexml.PackageTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.lenz.liusbethernet package
 *
 * @author Paul Bender
 */
public class PackageTest  {
}
