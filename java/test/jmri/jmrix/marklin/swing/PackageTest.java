package jmri.jmrix.marklin.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.marklin.swing.packetgen.PackageTest.class,
    jmri.jmrix.marklin.swing.monitor.PackageTest.class,
    MarklinComponentFactoryTest.class,
    MarklinMenuTest.class,
    BundleTest.class,
    MarklinNamedPaneActionTest.class
})
/**
 * Tests for the jmri.jmrix.marklin.swing package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
