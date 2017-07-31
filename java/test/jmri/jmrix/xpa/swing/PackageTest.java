package jmri.jmrix.xpa.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.xpa.swing.xpamon.PackageTest.class,
    jmri.jmrix.xpa.swing.xpaconfig.PackageTest.class,
    jmri.jmrix.xpa.swing.packetgen.PackageTest.class,
    XpaMenuTest.class,
    XpaComponentFactoryTest.class,
    BundleTest.class
})

/**
 * Tests for the jmri.jmrix.xpa.swing package.
 *
 * @author Paul Bender Copyright 2016
 */
public class PackageTest {

}
