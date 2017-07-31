package jmri.jmrix.lenz.swing.systeminfo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.lenz.swing.systeminfo package
 *
 * @author Paul Bender
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    SystemInfoFrameTest.class,
    SystemInfoActionTest.class,
    BundleTest.class
})
public class PackageTest {
}
