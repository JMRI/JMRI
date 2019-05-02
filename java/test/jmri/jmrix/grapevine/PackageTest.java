package jmri.jmrix.grapevine;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SerialTurnoutTest.class,
        SerialTurnoutTest1.class,
        SerialTurnoutTest2.class,
        SerialTurnoutTest3.class,
        SerialTurnoutManagerTest.class,
        SerialLightTest.class,
        SerialLightManagerTest.class,
        SerialSensorManagerTest.class,
        SerialNodeTest.class,
        SerialMessageTest.class,
        SerialReplyTest.class,
        SerialTrafficControllerTest.class,
        SerialAddressTest.class,
        jmri.jmrix.grapevine.serialdriver.PackageTest.class,
        jmri.jmrix.grapevine.simulator.PackageTest.class,
        jmri.jmrix.grapevine.configurexml.PackageTest.class,
        GrapevineMenuTest.class,
        jmri.jmrix.grapevine.serialmon.PackageTest.class,
        GrapevineSystemConnectionMemoTest.class,
        SerialPortControllerTest.class,
        jmri.jmrix.grapevine.nodeconfig.PackageTest.class,
        jmri.jmrix.grapevine.nodetable.PackageTest.class,
        jmri.jmrix.grapevine.packetgen.PackageTest.class,
        SerialConnectionTypeListTest.class,
        SerialSensorTest.class,
        SerialSignalHeadTest.class,
        BundleTest.class,
        jmri.jmrix.grapevine.swing.PackageTest.class,
})

/**
 * Tests for the jmri.jmrix.grapevine package.
 *
 * @author Bob Jacobsen Copyright 2003, 2007
 */
public class PackageTest  {
}
