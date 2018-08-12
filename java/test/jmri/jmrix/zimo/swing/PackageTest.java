package jmri.jmrix.zimo.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.jmrix.zimo.swing.packetgen.PackageTest.class,
        BundleTest.class,
        jmri.jmrix.zimo.swing.monitor.PackageTest.class,
        Mx1ComponentFactoryTest.class,
        Mx1MenuTest.class,
        Mx1NamedPaneActionTest.class,
})

/**
 * Tests for the jmri.jmrix.zimo.swing package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest  {
}
