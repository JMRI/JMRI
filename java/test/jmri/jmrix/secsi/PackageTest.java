package jmri.jmrix.secsi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SerialTurnoutTest.class,
        SerialTurnoutManagerTest.class,
        SerialSensorManagerTest.class,
        SerialNodeTest.class,
        SerialMessageTest.class,
        SerialTrafficControllerTest.class,
        SerialAddressTest.class,
        jmri.jmrix.secsi.serialdriver.PackageTest.class,
        jmri.jmrix.secsi.configurexml.PackageTest.class,
        jmri.jmrix.secsi.nodeconfig.PackageTest.class,
        jmri.jmrix.secsi.packetgen.PackageTest.class,
        jmri.jmrix.secsi.serialmon.PackageTest.class,
        SerialPortControllerTest.class,
        SecsiSystemConnectionMemoTest.class,
        SecsiMenuTest.class,
        SerialConnectionTypeListTest.class,
        SerialLightManagerTest.class,
        SerialReplyTest.class,
        SerialSensorTest.class,
        SerialLightTest.class,
        jmri.jmrix.secsi.swing.PackageTest.class,
        jmri.jmrix.secsi.simulator.PackageTest.class,
        BundleTest.class,
})

/**
 * Tests for the jmri.jmrix.secsi package.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008
 */
public class PackageTest  {
}
