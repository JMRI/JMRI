package jmri.jmrix.lenz.swing.liusb;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.lenz.swing.liusb package
 *
 * @author Paul Bender
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    LIUSBConfigFrameTest.class,
    LIUSBConfigActionTest.class,
    BundleTest.class
})
public class PackageTest {
}
