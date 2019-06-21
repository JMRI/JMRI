package jmri.jmrix.lenz;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        LenzCommandStationTest.class,
        LenzConnectionTypeListTest.class,
        XNetMessageTest.class,
        XNetReplyTest.class,
        XNetTurnoutTest.class,
        XNetSensorTest.class,
        XNetLightTest.class,
        XNetPacketizerTest.class,
        XNetTurnoutManagerTest.class,
        XNetSensorManagerTest.class,
        XNetLightManagerTest.class,
        XNetTrafficControllerTest.class,
        XNetTrafficRouterTest.class,
        XNetSystemConnectionMemoTest.class,
        XNetThrottleTest.class,
        XNetConsistManagerTest.class,
        XNetConsistTest.class,
        XNetInitializationManagerTest.class,
        XNetProgrammerTest.class,
        XNetProgrammerManagerTest.class,
        XNetOpsModeProgrammerTest.class,
        XNetPowerManagerTest.class,
        XNetThrottleManagerTest.class,
        XNetExceptionTest.class,
        XNetMessageExceptionTest.class,
        XNetStreamPortControllerTest.class,
        jmri.jmrix.lenz.li100.PackageTest.class,
        jmri.jmrix.lenz.li100f.PackageTest.class,
        jmri.jmrix.lenz.li101.PackageTest.class,
        jmri.jmrix.lenz.liusb.PackageTest.class,
        jmri.jmrix.lenz.xntcp.PackageTest.class,
        jmri.jmrix.lenz.liusbserver.PackageTest.class,
        jmri.jmrix.lenz.liusbethernet.PackageTest.class,
        jmri.jmrix.lenz.xnetsimulator.PackageTest.class,
        jmri.jmrix.lenz.hornbyelite.PackageTest.class,
        jmri.jmrix.lenz.ztc640.PackageTest.class,
        BundleTest.class,
        jmri.jmrix.lenz.swing.PackageTest.class,
        jmri.jmrix.lenz.configurexml.PackageTest.class,
        XNetNetworkPortControllerTest.class,
        XNetSerialPortControllerTest.class,
        XNetSimulatorPortControllerTest.class,
        XNetTimeSlotListenerTest.class,
        XNetConstantsTest.class,
        XNetFeedbackMessageCacheTest.class,
        AbstractXNetSerialConnectionConfigTest.class,
        AbstractXNetInitializationManagerTest.class,
        XNetAddressTest.class,
        XNetStreamConnectionConfigTest.class,
        XNetHeartBeatTest.class,
        jmri.jmrix.lenz.lzv200.PackageTest.class,
})

/**
 * Tests for the jmri.jmrix.lenz package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest  {
}
