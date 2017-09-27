package jmri.jmrix.lenz.xntcp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.lenz.xntcp package
 *
 * @author Paul Bender
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    XnTcpAdapterTest.class,
    XnTcpXNetPacketizerTest.class,
    ConnectionConfigTest.class,
    jmri.jmrix.lenz.xntcp.configurexml.PackageTest.class,
    BundleTest.class
})
public class PackageTest {
}
