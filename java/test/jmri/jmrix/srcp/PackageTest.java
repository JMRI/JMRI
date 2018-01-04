package jmri.jmrix.srcp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SRCPReplyTest.class,
        SRCPMessageTest.class,
        SRCPTrafficControllerTest.class,
        SRCPSystemConnectionMemoTest.class,
        SRCPBusConnectionMemoTest.class,
        SRCPTurnoutManagerTest.class,
        SRCPTurnoutTest.class,
        SRCPSensorManagerTest.class,
        SRCPSensorTest.class,
        SRCPThrottleManagerTest.class,
        SRCPThrottleTest.class,
        SRCPPowerManagerTest.class,
        SRCPProgrammerTest.class,
        SRCPProgrammerManagerTest.class,
        SRCPClockControlTest.class,
        jmri.jmrix.srcp.parser.PackageTest.class,
        jmri.jmrix.srcp.networkdriver.PackageTest.class,
        jmri.jmrix.srcp.configurexml.PackageTest.class,
        jmri.jmrix.srcp.swing.PackageTest.class,
        SRCPPortControllerTest.class,
        SRCPTrafficControllerTest.class,
        SRCPConnectionTypeListTest.class,
})

/**
 * Tests for the jmri.jmrix.srcp package
 *
 * @author	Paul Bender
 */
public class PackageTest {
}
