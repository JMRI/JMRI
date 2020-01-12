package jmri.jmrix.tmcc.packetgen;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    SerialPacketGenActionTest.class,
    SerialPacketGenFrameTest.class,
    BundleTest.class
})

/**
 * Tests for the jmri.jmrix.tmcc.packetgen package
 *
 * @author  Paul Bender	Copyright (C) 2016
 */
public class PackageTest{
}
