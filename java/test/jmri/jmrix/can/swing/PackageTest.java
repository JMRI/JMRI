package jmri.jmrix.can.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.can.swing.monitor.PackageTest.class,
    jmri.jmrix.can.swing.send.PackageTest.class,
    CanMenuTest.class,
    CanComponentFactoryTest.class,
    BundleTest.class,
    CanNamedPaneActionTest.class
})
/**
 * Tests for the jmri.jmrix.can.swing.monitor package.
 *
 * @author Bob Jacobsen Copyright 2008
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
