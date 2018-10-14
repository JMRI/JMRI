package jmri.jmrix.lenz.swing.lv102;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.lenz.swing.lv102 package
 *
 * @author Paul Bender
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    LV102FrameTest.class,
    LV102InternalFrameTest.class,
    LV102ActionTest.class,
    BundleTest.class
})
public class PackageTest {
}
