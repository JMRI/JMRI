package jmri.jmrix.lenz.swing.li101;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.lenz.swing.li101 package
 *
 * @author Paul Bender
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    LI101FrameTest.class,
    LI101ActionTest.class,
    BundleTest.class
})
public class PackageTest {
}
