package jmri.jmrix.lenz.swing.packetgen;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.lenz.swing.packetgen.package
 *
 * @author Paul Bender
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    PacketGenFrameTest.class,
    PacketGenActionTest.class,
    BundleTest.class
})
public class PackageTest {
}
