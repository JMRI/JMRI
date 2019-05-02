package jmri.jmrix.secsi.packetgen;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    SerialPacketGenActionTest.class,
    SerialPacketGenFrameTest.class,
    BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.secsi.packetgen package
 *
 * @author  Paul Bender	Copyright (C) 2016
 */
public class PackageTest{
}
