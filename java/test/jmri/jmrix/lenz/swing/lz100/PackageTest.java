package jmri.jmrix.lenz.swing.lz100;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.lenz.swing.lz100 package
 *
 * @author Paul Bender
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    LZ100FrameTest.class,
    LZ100InternalFrameTest.class,
    LZ100ActionTest.class,
    BundleTest.class
})
public class PackageTest {
}
