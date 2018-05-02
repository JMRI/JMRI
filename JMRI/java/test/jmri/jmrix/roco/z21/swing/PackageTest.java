package jmri.jmrix.roco.z21.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.roco.z21.swing package
 *
 * @author	Bob Jacobsen
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.roco.z21.swing.configtool.PackageTest.class,
    BundleTest.class,
    Z21MenuTest.class,
    Z21ComponentFactoryTest.class,
    jmri.jmrix.roco.z21.swing.mon.PackageTest.class,
    jmri.jmrix.roco.z21.swing.packetgen.PackageTest.class
})
public class PackageTest {
}
