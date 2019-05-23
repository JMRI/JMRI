package jmri.jmrix.dccpp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	    DCCppCommandStationTest.class,
        DCCppConnectionTypeListTest.class,
        DCCppMessageTest.class,
        DCCppReplyTest.class,
        DCCppPacketizerTest.class,
        DCCppTrafficControllerTest.class,
        DCCppSystemConnectionMemoTest.class,
        DCCppThrottleTest.class,
        DCCppInitializationManagerTest.class,
        DCCppProgrammerTest.class,
        DCCppProgrammerManagerTest.class,
        DCCppPowerManagerTest.class,
        DCCppThrottleManagerTest.class,
        DCCppLightTest.class,
        DCCppLightManagerTest.class,
        DCCppOpsModeProgrammerTest.class,
        DCCppStreamPortControllerTest.class,
        DCCppSensorTest.class,
        DCCppSensorManagerTest.class,
        DCCppTurnoutTest.class,
        jmri.jmrix.dccpp.network.PackageTest.class,
        jmri.jmrix.dccpp.swing.PackageTest.class,
        jmri.jmrix.dccpp.dccppovertcp.PackageTest.class,
        jmri.jmrix.dccpp.simulator.PackageTest.class,
        jmri.jmrix.dccpp.serial.PackageTest.class,
        jmri.jmrix.dccpp.configurexml.PackageTest.class,
        DCCppNetworkPortControllerTest.class,
        DCCppSerialPortControllerTest.class,
        DCCppSimulatorPortControllerTest.class,
        DCCppMessageExceptionTest.class,
        DCCppConstantsTest.class,
        DCCppRegisterManagerTest.class,
        DCCppMultiMeterTest.class,
        DCCppTurnoutManagerTest.class,
        DCCppTurnoutReplyCacheTest.class,
        BundleTest.class,
	DCCppStreamConnectionConfigTest.class
})

/**
 * Tests for the jmri.jmrix.dccpp package
 *
 * @author	Bob Jacobsen
 * @author	Mark Underwood
 */
public class PackageTest  {
}
