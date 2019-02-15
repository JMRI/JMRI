package jmri.jmrix.jmriclient.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    JMRIClientMenuTest.class,
    JMRIClientComponentFactoryTest.class,
    jmri.jmrix.jmriclient.swing.mon.PackageTest.class,
    jmri.jmrix.jmriclient.swing.packetgen.PackageTest.class,
    BundleTest.class
})

/**
 * Tests for the jmri.jmrix.ieee802154.xbee.swing package
 *
 * @author  Paul Bender	Copyright (C) 2016
 */
public class PackageTest{
}
