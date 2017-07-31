package jmri.jmrix.lenz.swing.stackmon;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.lenz.swing.stackmon package
 *
 * @author Paul Bender
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    StackMonFrameTest.class,
    StackMonActionTest.class,
    StackMonDataModelTest.class,
    BundleTest.class
})
public class PackageTest {
}
