package jmri.jmrix.loconet.logixng.configureswing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    AnalogActionLocoNet_OPC_PEERSwingTest.class,
    AnalogExpressionLocoNet_OPC_PEERSwingTest.class,
    BundleTest.class,
    StringActionLocoNet_OPC_PEERSwingTest.class,
    StringExpressionLocoNet_OPC_PEERSwingTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.logixng.analogactions tree
 *
 * @author Daniel Bergqvist 2018
 */
public class PackageTest {
}
