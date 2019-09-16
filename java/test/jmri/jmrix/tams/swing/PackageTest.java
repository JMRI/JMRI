package jmri.jmrix.tams.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.tams.swing.packetgen.PackageTest.class,
    jmri.jmrix.tams.swing.statusframe.PackageTest.class,
    jmri.jmrix.tams.swing.locodatabase.PackageTest.class,
    jmri.jmrix.tams.swing.monitor.PackageTest.class,
    TamsComponentFactoryTest.class,
    TamsMenuTest.class,
    TamsNamedPaneActionTest.class,
    BundleTest.class,
})
/**
 * Tests for the jmri.jmrix.tams.swing package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
