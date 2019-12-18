package jmri.jmrix.roco.z21;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.roco.z21 package.
 * 
 * @author Randall Wood (C) 2016
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    Z21AdapterTest.class,
    Z21MessageTest.class,
    Z21ReplyTest.class,
    Z21TrafficControllerTest.class,
    Z21SystemConnectionMemoTest.class,
    Z21XPressNetTunnelTest.class,
    Z21XNetProgrammerTest.class,
    Z21XNetThrottleManagerTest.class,
    Z21XNetThrottleTest.class,
    Z21XNetTurnoutManagerTest.class,
    Z21XNetTurnoutTest.class,
    jmri.jmrix.roco.z21.simulator.PackageTest.class,
    BundleTest.class,
    ConnectionConfigTest.class,
    jmri.jmrix.roco.z21.configurexml.PackageTest.class,
    Z21ReporterTest.class,
    Z21ReporterManagerTest.class,
    RocoZ21CommandStationTest.class,
    jmri.jmrix.roco.z21.swing.PackageTest.class,
    Z21XNetStreamPortControllerTest.class,
    Z21XNetPacketizerTest.class,
    Z21ConstantsTest.class,
    Z21XNetConnectionConfigTest.class,
    Z21XNetMessageTest.class,
    Z21XNetReplyTest.class,
    Z21MultiMeterTest.class,
    Z21XNetProgrammerManagerTest.class,
    Z21XNetOpsModeProgrammerTest.class,
    Z21LnStreamPortControllerTest.class,
    Z21LnStreamPortPacketizerTest.class,
    Z21RMBusSensorTest.class,
    Z21RMBusSensorManagerTest.class,
    Z21CanReporterTest.class,
    Z21ReporterManagerCanTest.class,
    Z21CanSensorTest.class,
    Z21CANBusAddressTest.class,
    Z21RMBusAddressTest.class,
})
public class PackageTest {

}
