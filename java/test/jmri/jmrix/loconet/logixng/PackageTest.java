package jmri.jmrix.loconet.logixng;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.loconet.logixng.swing.PackageTest.class,
    jmri.jmrix.loconet.logixng.configurexml.PackageTest.class,
    AnalogActionLocoNetOpcPeerTest.class,
    AnalogExpressionLocoNetOpcPeerTest.class,
    BundleTest.class,
    StringActionLocoNetOpcPeerTest.class,
    StringExpressionLocoNetOpcPeerTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logixng.analogactions tree
 *
 * @author Daniel Bergqvist 2018
 */
public class PackageTest {
}
