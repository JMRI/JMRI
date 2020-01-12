package jmri.jmrix.loconet.logixng.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    AnalogActionLocoNetOpcPeerSwingTest.class,
    AnalogExpressionLocoNetOpcPeerSwingTest.class,
    BundleTest.class,
    StringActionLocoNetOpcPeerSwingTest.class,
    StringExpressionLocoNetOpcPeerSwingTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logixng.analogactions tree
 *
 * @author Daniel Bergqvist 2018
 */
public class PackageTest {
}
