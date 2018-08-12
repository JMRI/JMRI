package jmri.jmrix.oaktree;

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
        jmri.jmrix.oaktree.serialdriver.PackageTest.class,
        jmri.jmrix.oaktree.configurexml.PackageTest.class,
        jmri.jmrix.oaktree.nodeconfig.PackageTest.class,
        jmri.jmrix.oaktree.packetgen.PackageTest.class,
        jmri.jmrix.oaktree.serialmon.PackageTest.class,
        OakTreeMenuTest.class,
        OakTreeSystemConnectionMemoTest.class,
        SerialPortControllerTest.class,
        SerialConnectionTypeListTest.class,
        SerialLightManagerTest.class,
        SerialReplyTest.class,
        SerialSensorTest.class,
        SerialLightTest.class,
        BundleTest.class,
        jmri.jmrix.oaktree.swing.PackageTest.class,
        jmri.jmrix.oaktree.simulator.PackageTest.class
})

/**
 * Tests for the jmri.jmrix.oaktree package.
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class PackageTest  {
}
