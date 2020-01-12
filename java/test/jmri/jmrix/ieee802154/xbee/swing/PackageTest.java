package jmri.jmrix.ieee802154.xbee.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    XBeeMenuTest.class,
    XBeeComponentFactoryTest.class,
    jmri.jmrix.ieee802154.xbee.swing.nodeconfig.PackageTest.class,
    jmri.jmrix.ieee802154.xbee.swing.packetgen.PackageTest.class
})

/**
 * Tests for the jmri.jmrix.ieee802154.xbee.swing package
 *
 * @author  Paul Bender	Copyright (C) 2016
 */
public class PackageTest{
}
