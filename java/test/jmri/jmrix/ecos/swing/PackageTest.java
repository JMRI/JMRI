package jmri.jmrix.ecos.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        jmri.jmrix.ecos.swing.locodatabase.PackageTest.class,
        jmri.jmrix.ecos.swing.packetgen.PackageTest.class,
        jmri.jmrix.ecos.swing.monitor.PackageTest.class,
        jmri.jmrix.ecos.swing.preferences.PackageTest.class,
        jmri.jmrix.ecos.swing.statusframe.PackageTest.class,
        EcosComponentFactoryTest.class,
        EcosMenuTest.class,
        BundleTest.class,
        EcosNamedPaneActionTest.class,
})

/**
 * Tests for the jmri.jmrix.ecos.swing package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest  {
}
