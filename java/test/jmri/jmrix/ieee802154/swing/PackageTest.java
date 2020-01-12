package jmri.jmrix.ieee802154.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    jmri.jmrix.ieee802154.swing.mon.PackageTest.class,
    jmri.jmrix.ieee802154.swing.nodeconfig.PackageTest.class,
    jmri.jmrix.ieee802154.swing.packetgen.PackageTest.class,
    IEEE802154MenuTest.class,
    IEEE802154ComponentFactoryTest.class
})

/**
 * Tests for the jmri.jmrix.ieee802154.swing package
 *
 * @author  Paul Bender	Copyright (C) 2016
 */
public class PackageTest{
}
