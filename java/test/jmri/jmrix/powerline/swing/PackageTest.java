package jmri.jmrix.powerline.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.powerline.swing.packetgen.PackageTest.class,
    jmri.jmrix.powerline.swing.serialmon.PackageTest.class,
    PowerlineComponentFactoryTest.class,
    PowerlineMenuTest.class,
    PowerlineNamedPaneActionTest.class,
    BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.powerline.swing package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
