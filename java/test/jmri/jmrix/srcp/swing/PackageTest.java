package jmri.jmrix.srcp.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.srcp.swing.srcpmon.PackageTest.class,
    jmri.jmrix.srcp.swing.packetgen.PackageTest.class,
    SRCPComponentFactoryTest.class,
    SystemMenuTest.class,
    BundleTest.class
})

/**
 * Tests for the jmri.jmrix.srcp.swing package.
 *
 * @author Paul Bender Copyright 2016
 */
public class PackageTest {

}
