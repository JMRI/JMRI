package jmri.jmrix.mrc.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   BundleTest.class,
   jmri.jmrix.mrc.swing.packetgen.PackageTest.class,
   jmri.jmrix.mrc.swing.monitor.PackageTest.class,
   MrcMenuTest.class,
   MrcComponentFactoryTest.class,
   MrcNamedPaneActionTest.class
})

/**
 * Tests for the jmri.jmrix.mrc.swing package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest {
}
