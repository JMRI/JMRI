package jmri.jmrix.dccpp.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    jmri.jmrix.dccpp.swing.mon.PackageTest.class,
    jmri.jmrix.dccpp.swing.packetgen.PackageTest.class,
    ConfigBaseStationActionTest.class,
    ConfigBaseStationFrameTest.class,
    DCCppComponentFactoryTest.class,
    DCCppMenuTest.class
})

/**
 * Tests for the jmri.jmrix.dccpp.swing package
 *
 * @author	Bob Jacobsen
 * @author  Paul Bender	Copyright (C) 2016
 */
public class PackageTest{
}
