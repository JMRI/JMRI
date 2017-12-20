package jmri.jmrix.maple;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SerialTurnoutTest.class,
        SerialTurnoutManagerTest.class,
        SerialSensorManagerTest.class,
        jmri.jmrix.maple.SerialNodeTest.class,
        jmri.jmrix.maple.SerialMessageTest.class,
        SerialTrafficControllerTest.class,
        SerialAddressTest.class,
        jmri.jmrix.maple.OutputBitsTest.class,
        jmri.jmrix.maple.InputBitsTest.class,
        jmri.jmrix.maple.serialdriver.PackageTest.class,
        jmri.jmrix.maple.configurexml.PackageTest.class,
        jmri.jmrix.maple.packetgen.PackageTest.class,
        jmri.jmrix.maple.serialmon.PackageTest.class,
        jmri.jmrix.maple.assignment.PackageTest.class,
        jmri.jmrix.maple.nodeconfig.PackageTest.class,
        MapleSystemConnectionMemoTest.class,
        SerialPortControllerTest.class,
        MapleMenuTest.class,
        SerialConnectionTypeListTest.class,
        SerialLightManagerTest.class,
        SerialReplyTest.class,
        SerialLightTest.class,
        SerialSensorTest.class,
        BundleTest.class,
        jmri.jmrix.maple.swing.PackageTest.class,
})

/**
 * Tests for the jmri.jmrix.maple package.
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class PackageTest  {
}
